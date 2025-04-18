/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.base.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.AllowPublicAccess;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.base.request.StackLevel;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.refsw.impl.AbstractRequestContext;
import de.jpaw.util.ExceptionUtil;

/**
 * Holds the current request's environment.
 *
 * For every request, one of these is created.
 * Additional ones may be created for the asynchronous log writers (using dummy or null internalHeaderParameters)
 *
 * Any functionality relating to customization has been moved to a separate class (separation of concerns).
 */
public class RequestContext extends AbstractRequestContext {  // FIXME: this class should be final, but some unit test in t9t-ssm-be relies on non-finalness
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContext.class);
    private static final Cache<Long, Semaphore> GLOBAL_JVM_LOCKS = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(10000).build();
    private static final Cache<String, Semaphore> GLOBAL_JVM_LOCKS2 = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(10000).build();

    public final InternalHeaderParameters internalHeaderParameters;
    public final Thread createdByThread = Thread.currentThread();

    public final ITenantCustomization customization;    // stored here for convenience
    public final ITenantMapping tenantMapping;          // stored here for convenience

    private List<IPostCommitHook> postCommitList = null;  // to be executed after successful requests / commits
    private List<IPostCommitHook> postFailureList = null; // to be executed after failures
    private final AtomicInteger progressCounter = new AtomicInteger(0);
    private final Map<BucketWriteKey, Integer> bucketsToWrite = new ConcurrentHashMap<>();
    private final Map<Long, Semaphore> ownedJvmLocks = new ConcurrentHashMap<>();   // locks held by this thread / request context (by ref)
    private final Map<String, Semaphore> moreJvmLocks = new ConcurrentHashMap<>();  // locks held by this thread / request context (by String key)

    @AllowPublicAccess
    public volatile String statusText;

    private volatile boolean priorityRequest = false;   // can be set explicitly, or via ServiceRequestHeader
    // keeping track of call stack...
    private String currentPQON;                         // if a subrequest is called, this field keeps track of the innermost request type
    private int depth                   = -1;           // the current call stack depth. -1 = initialization, 0 = in main request, 1... in subrequest
    private int numberOfCallsThisLevel  = 0;            // the number of calls which have been started at this stack level
    private final List<StackLevel> callStack  = new ArrayList<>();  // needs to be concurrent because the processStatus request will read it!
    private final Object lockForNesting = new Object();
    private Boolean readOnlyDatabaseSession = null;
    private Boolean useShadowDatabaseSession = null;
    private Locale currentLocale = null;                // the current locale, computed on demand from the language code of internal header parameters

    public void pushCallStack(final String newPQON) {
        synchronized (lockForNesting) {
            ++depth;
            if (depth > 0) {
                ++numberOfCallsThisLevel;
                final StackLevel level = new StackLevel(numberOfCallsThisLevel, progressCounter.get(), currentPQON, statusText);
                level.freeze();
                callStack.add(level);
                currentPQON = newPQON;
                numberOfCallsThisLevel = 0;
            }
        }
    }
    public void popCallStack() {
        synchronized (lockForNesting) {
            --depth;
            if (depth >= 0) {
                final int d = callStack.size();
                if (d > 0) {
                    final StackLevel prev = callStack.remove(d - 1);
                    // restore status and PQON
                    currentPQON = prev.getPqon();
                    statusText  = prev.getStatusText();
                    numberOfCallsThisLevel = prev.getNumberOfCallsThisLevel();
                    progressCounter.set(prev.getProgressCounter());
                }
                if (d == 0)
                    LOGGER.error("Internal error: popping more data than was added!");
            }
        }
    }

    /**
     * Returns a call stack for the process status call (called by another thread, which is the reason for the synchronization).
     * The returned list has at least one element and the status text is truncated to the allowed size.
     */
    public List<StackLevel> getCallStack() {
        synchronized (lockForNesting) {
            final List<StackLevel> copy = new ArrayList<>(callStack.size() + 1);
            final int maxLen = StackLevel.meta$$statusText.getLength();
            for (final StackLevel sl: callStack) {
                final String status = sl.getStatusText();
                if (status == null || status.length() <= maxLen) {
                    // status text fits into field - use 1:1
                    copy.add(sl);
                } else {
                    // use a truncated message text
                    copy.add(new StackLevel(numberOfCallsThisLevel, progressCounter.get(), sl.getPqon(), status.substring(0, maxLen)));
                }
            }
         // add one more for the current level
            copy.add(new StackLevel(numberOfCallsThisLevel, progressCounter.get(), currentPQON, MessagingUtil.truncField(statusText, maxLen)));
            return copy;
        }
    }

    public void setReadOnlyMode(final boolean readOnly, final boolean useShadowDatabase) {
        if (isTopLevelRequest() && readOnlyDatabaseSession == null) {
            readOnlyDatabaseSession = readOnly;
            useShadowDatabaseSession = useShadowDatabase;
        } else {
            LOGGER.warn("Attempt to set readOnly session flag for level {} and previously set mode {}", depth, readOnlyDatabaseSession);
        }
    }

    public boolean getReadOnlyMode() {
        return Boolean.TRUE.equals(readOnlyDatabaseSession);
    }

    public boolean getUseShadowDatabase() {
        return Boolean.TRUE.equals(useShadowDatabaseSession);
    }

    /**
     * Returns true if the current request on the request context is the first request being invoked
     */
    public boolean isTopLevelRequest() {
        return depth == 0;
    }

    public void incrementProgress() {
        progressCounter.incrementAndGet();
    }

    public int getProgressCounter() {
        return progressCounter.get();
    }

    public void addPostCommitHook(final IPostCommitHook newHook) {
        if (postCommitList == null)
            postCommitList = new ArrayList<>(4);
        postCommitList.add(newHook);
    }

    public void addPostFailureHook(final IPostCommitHook newHook) {
        if (postFailureList == null)
            postFailureList = new ArrayList<>(4);
        postFailureList.add(newHook);
    }


    public RequestContext(final InternalHeaderParameters internalHeaderParameters, final ICustomization customizationProvider) {
        super(internalHeaderParameters.getExecutionStartedAt(),
              internalHeaderParameters.getJwtInfo().getUserId(),
              internalHeaderParameters.getJwtInfo().getTenantId(),
              internalHeaderParameters.getJwtInfo().getUserRef(),
              null,  // no more tenantRef
              internalHeaderParameters.getProcessRef());
        this.internalHeaderParameters = internalHeaderParameters;
        this.customization = customizationProvider.getTenantCustomization(tenantId);
        this.tenantMapping = customizationProvider.getTenantMapping(tenantId);
        this.currentPQON = internalHeaderParameters.getRequestParameterPqon();
    }

    public void fillResponseStandardFields(final ServiceResponse response) {
        final ServiceRequestHeader h = internalHeaderParameters.getRequestHeader();
        if (h != null)
            response.setMessageId(h.getMessageId());
        response.setTenantId(tenantId);
        response.setProcessRef(requestRef);
    }

    /** Returns a LongFilter condition on the current tenant and possibly the default tenant, if that one is different. */
    public UnicodeFilter tenantFilter(final String name) {
        final UnicodeFilter filter = new UnicodeFilter(name);
        if (T9tConstants.GLOBAL_TENANT_ID.equals(tenantId))
            filter.setEqualsValue(T9tConstants.GLOBAL_TENANT_ID);
        else
            filter.setValueList(ImmutableList.of(T9tConstants.GLOBAL_TENANT_ID, tenantId));
        return filter;
    }

    public void discardPostCommitActions() {
        if (postCommitList != null) {
            LOGGER.debug("Discarding {} stored post commit actions", postCommitList.size());
            postCommitList.clear();
        }
    }

    public void applyPostCommitActions(final RequestParameters rq, final ServiceResponse rs) {
        // all persistence units have successfully committed...
        // now invoke possible postCommit hooks
        if (postCommitList != null) {
            LOGGER.debug("Performing {} stored post commit actions", postCommitList.size());
            for (final IPostCommitHook hook : postCommitList) {
                hook.postCommit(this, rq, rs);
            }
            // avoid duplicate execution...
            postCommitList.clear();
        }
    }

    public void applyPostFailureActions(final RequestParameters rq, final ServiceResponse rs) {
        // request returned an error. You can now notify someone...
        if (postFailureList != null) {
            LOGGER.debug("Performing {} stored post failure actions", postFailureList.size());
            for (final IPostCommitHook hook : postFailureList) {
                hook.postCommit(this, rq, rs);
            }
            // avoid duplicate execution...
            postFailureList.clear();
        }
    }

    // queue a bucket writing command. All bucket writes will be kicked off asynchronously after a successful commit
    public void writeBucket(final String typeId, final Long ref, final Integer mode) {
        final BucketWriteKey key = new BucketWriteKey(tenantId, ref, typeId);
        // combine it with prior commands
        bucketsToWrite.merge(key, mode, (a, b) -> Integer.valueOf(a.intValue() | b.intValue()));
    }

    // the queue is handed in by the caller because we do not have injection facilities within this class
    public void postBucketEntriesToQueue(final IBucketWriter writer) {
        if (!bucketsToWrite.isEmpty()) {
            LOGGER.debug("{} bucket entries have been collected, queueing them...", bucketsToWrite.size());
            writer.writeToBuckets(bucketsToWrite);
        }
    }

    // PER_JVM resource management

    /** Releases all locks held by this context. */
    public void releaseAllLocks() {
        if (!ownedJvmLocks.isEmpty()) {
            LOGGER.trace("SEM: Releasing locks on {} refs", ownedJvmLocks.size());
            for (final Map.Entry<Long, Semaphore> sem : ownedJvmLocks.entrySet()) {
                LOGGER.trace("SEM: Releasing lock on ref {}", sem.getKey());
                sem.getValue().release();
            }
            ownedJvmLocks.clear();
        }
        if (!moreJvmLocks.isEmpty()) {
            LOGGER.trace("Releasing locks on {} IDs", moreJvmLocks.size());
            for (final Map.Entry<String, Semaphore> sem : moreJvmLocks.entrySet()) {
                LOGGER.trace("SEM: Releasing lock on ref {}", sem.getKey());
                sem.getValue().release();
            }
            moreJvmLocks.clear();
        }
    }

    /** Acquires a new lock within a given timeout of n milliseconds. */
    public void lockRef(final Long ref, final long timeoutInMillis) {
        ownedJvmLocks.computeIfAbsent(ref, myRef -> {
            // get a Semaphore from the global pool
            try {
                final Semaphore globalSem = GLOBAL_JVM_LOCKS.get(ref, unused -> new Semaphore(1, true)); // get a global Semaphore, or create one if non exists
                LOGGER.trace("SEM: Acquiring JVM lock on ref {}", ref);
                final long start = System.nanoTime();
                if (!globalSem.tryAcquire(timeoutInMillis, TimeUnit.MILLISECONDS)) {
                    final String msg = ref + " after " + timeoutInMillis + " milliseconds";
                    LOGGER.warn("SEM: Could not acquire JVM lock on ref {}", msg);
                    throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
                }
                final long end = System.nanoTime();
                LOGGER.trace("SEM: Acquiring JVM lock on ref {} SUCCESS after {} ns", ref, end - start);
                return globalSem;
            } catch (final InterruptedException e) {
                final String msg = ref + " after " + timeoutInMillis + " milliseconds due to InterruptedException " + ExceptionUtil.causeChain(e);
                LOGGER.warn("SEM: Could not acquire JVM lock on ref {}", msg);
                throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
            }
        });
    }

    /** Acquires a new lock within the default timeout (of currently 5000 milliseconds). */
    public void lockRef(final Long ref) {
        lockRef(ref, 5000L);
    }

    /** Acquires a new lock within a given timeout of n milliseconds. */
    public void lockString(final String key, final long timeoutInMillis) {
        moreJvmLocks.computeIfAbsent(key, myRef -> {
            // get a Semaphore from the global pool
            try {
                final Semaphore globalSem = GLOBAL_JVM_LOCKS2.get(key, unused -> new Semaphore(1, true)); // get a global Semaphore, or create one if non exists
                LOGGER.trace("SEM: Acquiring JVM lock on ID {}", key);
                final long start = System.nanoTime();
                if (!globalSem.tryAcquire(timeoutInMillis, TimeUnit.MILLISECONDS)) {
                    final String msg = key + " after " + timeoutInMillis + " milliseconds";
                    LOGGER.warn("SEM: Could not acquire JVM lock on ID {}", msg);
                    throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
                }
                final long end = System.nanoTime();
                LOGGER.trace("SEM: Acquiring JVM lock on ID {} SUCCESS after {} ns", key, end - start);
                return globalSem;
            } catch (final InterruptedException e) {
                final String msg = key + " after " + timeoutInMillis + " milliseconds due to InterruptedException " + ExceptionUtil.causeChain(e);
                LOGGER.warn("SEM: Could not acquire JVM lock on ID {}", msg);
                throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
            }
        });
    }

    /** Acquires a new lock within the default timeout (of currently 5000 milliseconds). */
    public void lockString(final String key) {
        lockString(key, 5000L);
    }

    public boolean isPriorityRequest() {
        return priorityRequest;
    }

    /** The priority flag affects all subsequently triggered subrequests. (Preregisters priority settings) */
    public void setPriorityRequest(final boolean priorityRequest) {
        this.priorityRequest = priorityRequest;
    }

    /** Safe getter for z field values, also works if z itself is null. */
    public Object getZEntry(final String key) {
        final Map<String, Object> z = internalHeaderParameters.getJwtInfo().getZ();
        return z == null ? null : z.get(key);
    }

    /** Safe getter for z field values, also works if z itself is null, returns a String typed result, if required, by conversion. */
    public String getZString(final String key) {
        final Object value = getZEntry(key);
        return value == null ? null : value.toString();
    }

    /** Safe getter for z field values, also works if z itself is null, returns a Long typed result, if required, by conversion. */
    public Long getZLong(final String key) {
        return JsonUtil.getZLong(internalHeaderParameters.getJwtInfo().getZ(), key, null);
    }

    /** Safe getter for z field values, also works if z itself is null, returns an Integer typed result, if required, by conversion. */
    public Integer getZInteger(final String key) {
        return JsonUtil.getZInteger(internalHeaderParameters.getJwtInfo().getZ(), key, null);
    }

    /** Retrieves the (cached) locale, constructed from the internal header parameters. */
    public Locale getLocale() {
        if (currentLocale == null) {
            final String languageCode = internalHeaderParameters.getLanguageCode();
            if (languageCode == null) {
                currentLocale = Locale.getDefault();
            } else {
                final int len = languageCode.length();
                if (len == 2) {
                    currentLocale = new Locale(languageCode);
                } else if (len >= 5 && (languageCode.charAt(2) == '-' || languageCode.charAt(2) == '_')) {
                    if (len == 5) {
                        currentLocale = new Locale(languageCode.substring(0, 2), languageCode.substring(3));
                    } else if (len > 7) {
                        currentLocale = new Locale(languageCode.substring(0, 2), languageCode.substring(3, 5), languageCode.substring(6));
                    } else {
                        LOGGER.warn("Invalid language code {} in internal header parameters", languageCode);
                        currentLocale = Locale.getDefault();
                    }
                }
            }
        }
        return currentLocale;
    }
}
