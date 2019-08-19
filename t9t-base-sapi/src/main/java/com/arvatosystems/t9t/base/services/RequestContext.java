/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.base.request.StackLevel;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.refsw.impl.AbstractRequestContext;
import de.jpaw.util.ExceptionUtil;

/** Holds the current request's environment.
 *
 * For every request, one of these is created.
 * Additional ones may be created for the asynchronous log writers (using dummy or null internalHeaderParameters)
 *
 * Any functionality relating to customization has been moved to a separate class (separation of concerns).
 */
public class RequestContext extends AbstractRequestContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContext.class);
    static private final Cache<Long, Semaphore> GLOBAL_JVM_LOCKS = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).initialCapacity(1000).build();

    public final InternalHeaderParameters internalHeaderParameters;
    public final Thread createdByThread = Thread.currentThread();

    public final ITenantCustomization customization;    // stored here for convenience
    public final ITenantMapping tenantMapping;          // stored here for convenience

    private List<IPostCommitHook> postCommitList = null;  // to be executed after successful requests / commits
    private List<IPostCommitHook> postFailureList = null; // to be executed after failures
    private final AtomicInteger progressCounter = new AtomicInteger(0);
    private final Map<BucketWriteKey, Integer> bucketsToWrite = new ConcurrentHashMap<BucketWriteKey, Integer>();
    private final Map<Long, Semaphore> OWNED_JVM_LOCKS = new ConcurrentHashMap<>();  // all locks held by this thread / request context (by ref)
    public volatile String statusText;
    private volatile boolean priorityRequest = false;   // can be set explicitly, or via ServiceRequestHeader
    // keeping track of call stack...
    private String currentPQON;                 // if a subrequest is called, this field keeps track of the innermost request type
    int depth = -1;                             // the current call stack depth. -1 = initialization, 0 = in main request, 1... in subrequest
    int numberOfCallsThisLevel = 0;             // the number of calls which have been started at this stack level
    //int numberOfCallsTotal = 0;                 // the total number of subexecutions
    private List<StackLevel> callStack = new ArrayList<StackLevel>();  // needs to be concurrent because the processStatus request will read it!
    private final Object lockForNesting = new Object();

    public void pushCallStack(String newPQON) {
        synchronized (lockForNesting) {
            ++depth;
            if (depth > 0) {
                ++numberOfCallsThisLevel;
                StackLevel level = new StackLevel(numberOfCallsThisLevel, progressCounter.get(), currentPQON, statusText);
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
                int d = callStack.size();
                if (d > 0) {
                    StackLevel prev = callStack.remove(d - 1);
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
     * Return a call stack for the process status call (called by another thread, which is the reason for the synchronization).
     * The returned list has at least one element and the status text is truncated to the allowed size.
     */
    public List<StackLevel> getCallStack() {
        synchronized (lockForNesting) {
            final List<StackLevel> copy = new ArrayList<>(callStack.size() + 1);
            final int maxLen = StackLevel.meta$$statusText.getLength();
            for (StackLevel sl: callStack) {
                final String status = sl.getStatusText();
                if (status == null || status.length() <= maxLen)
                    copy.add(sl);
                else
                    copy.add(new StackLevel(numberOfCallsThisLevel, progressCounter.get(), sl.getPqon(), status.substring(0, maxLen)));  // use a truncated message text
            }
            copy.add(new StackLevel(numberOfCallsThisLevel, progressCounter.get(), currentPQON, MessagingUtil.truncField(statusText, maxLen)));  // add one more for the current level
            return copy;
        }
    }

    public void incrementProgress() {
        progressCounter.incrementAndGet();
    }

    public int getProgressCounter() {
        return progressCounter.get();
    }

    public void addPostCommitHook(IPostCommitHook newHook) {
        if (postCommitList == null)
            postCommitList = new ArrayList<IPostCommitHook>(4);
        postCommitList.add(newHook);
    }

    public void addPostFailureHook(IPostCommitHook newHook) {
        if (postFailureList == null)
            postFailureList = new ArrayList<IPostCommitHook>(4);
        postFailureList.add(newHook);
    }


    public RequestContext(InternalHeaderParameters internalHeaderParameters, ICustomization customizationProvider) {
        super(internalHeaderParameters.getExecutionStartedAt(),
              internalHeaderParameters.getJwtInfo().getUserId(),
              internalHeaderParameters.getJwtInfo().getTenantId(),
              internalHeaderParameters.getJwtInfo().getUserRef(),
              internalHeaderParameters.getJwtInfo().getTenantRef(),
              internalHeaderParameters.getProcessRef());
        this.internalHeaderParameters = internalHeaderParameters;
        this.customization = customizationProvider.getTenantCustomization(tenantRef, tenantId);
        this.tenantMapping = customizationProvider.getTenantMapping(tenantRef, tenantId);
        this.currentPQON = internalHeaderParameters.getRequestParameterPqon();
    }

    public void fillResponseStandardFields(ServiceResponse response) {
        ServiceRequestHeader h = internalHeaderParameters.getRequestHeader();
        if (h != null)
            response.setMessageId(h.getMessageId());
        response.setTenantId(tenantId);
        response.setProcessRef(requestRef);
    }

    /** Returns a LongFilter condition on the current tenant and possibly the default tenant, if that one is different. */
    public LongFilter tenantFilter(String name) {
        final LongFilter filter = new LongFilter(name);
        if (T9tConstants.GLOBAL_TENANT_REF42.equals(tenantRef))
            filter.setEqualsValue(T9tConstants.GLOBAL_TENANT_REF42);
        else
            filter.setValueList(ImmutableList.of(T9tConstants.GLOBAL_TENANT_REF42, tenantRef));
        return filter;
    }

    public void discardPostCommitActions() {
        if (postCommitList != null) {
            LOGGER.info("Discarding {} stored post commit actions", postCommitList.size());
            postCommitList.clear();
        }
    }

    public void applyPostCommitActions(RequestParameters rq, ServiceResponse rs) {
        // all persistence units have successfully committed...
        // now invoke possible postCommit hooks
        if (postCommitList != null) {
            LOGGER.info("Performing {} stored post commit actions", postCommitList.size());
            for (IPostCommitHook hook : postCommitList)
                hook.postCommit(this, rq, rs);
            // avoid duplicate execution...
            postCommitList.clear();
        }
    }

    public void applyPostFailureActions(RequestParameters rq, ServiceResponse rs) {
        // request returned an error. You can now notify someone...
        if (postFailureList != null) {
            LOGGER.info("Performing {} stored post failure actions", postFailureList.size());
            for (IPostCommitHook hook : postFailureList)
                hook.postCommit(this, rq, rs);
            // avoid duplicate execution...
            postFailureList.clear();
        }
    }

    // queue a bucket writing command. All bucket writes will be kicked off asynchronously after a successful commit
    public void writeBucket(String typeId, Long ref, Integer mode) {
        final BucketWriteKey key = new BucketWriteKey(tenantRef, ref, typeId);
        // combine it with prior commands
        bucketsToWrite.merge(key, mode, (a, b) -> Integer.valueOf(a.intValue() | b.intValue()));
    }

    // the queue is handed in by the caller because we do not have injection facilities within this class
    public void postBucketEntriesToQueue(IBucketWriter writer) {
        if (!bucketsToWrite.isEmpty()) {
            LOGGER.debug("{} bucket entries have been collected, queueing them...", bucketsToWrite.size());
            writer.writeToBuckets(bucketsToWrite);
        }
    }

    // PER_JVM resource management

    /** Releases all locks held by this context. */
    public void releaseAllLocks() {
        if (!OWNED_JVM_LOCKS.isEmpty())
            LOGGER.debug("Releasing locks on {} refs", OWNED_JVM_LOCKS.size());
        for (Semaphore sem : OWNED_JVM_LOCKS.values())
            sem.release();
        OWNED_JVM_LOCKS.clear();
    }

    /** Acquires a new lock within a given timeout of n milliseconds. */
    public void lockRef(final Long ref, final long timeoutInMillis) {
        OWNED_JVM_LOCKS.computeIfAbsent(ref, myRef -> {
            // get a Semaphore from the global pool
            try {
                final Semaphore globalSem = GLOBAL_JVM_LOCKS.get(ref, () -> new Semaphore(1, true));  // get a global Semaphore, or create one if non exists
                if (!globalSem.tryAcquire(timeoutInMillis, TimeUnit.MILLISECONDS)) {
                    final String msg = ref + " after " + timeoutInMillis + " milliseconds";
                    LOGGER.error("Could not acquire JVM lock on {}", msg);
                    throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
                }
                LOGGER.debug("Acquired JVM lock on ref {}", ref);
                return globalSem;
            } catch (ExecutionException e) {
                final String msg = ref + " after " + timeoutInMillis + " milliseconds due to ExecutionException " + ExceptionUtil.causeChain(e);
                LOGGER.error("Could not acquire JVM lock on {}", msg);
                throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
            } catch (InterruptedException e) {
                final String msg = ref + " after " + timeoutInMillis + " milliseconds due to InterruptedException " + ExceptionUtil.causeChain(e);
                LOGGER.error("Could not acquire JVM lock on {}", msg);
                throw new T9tException(T9tException.COULD_NOT_ACQUIRE_LOCK, msg);
            }
        });
    }

    /** Acquires a new lock within the default timeout (of currently 5000 milliseconds). */
    public void lockRef(final Long ref) {
        lockRef(ref, 5000L);
    }

    public boolean isPriorityRequest() {
        return priorityRequest;
    }

    /** The priority flag affects all subsequently triggered subrequests. (Preregisters priority settings) */
    public void setPriorityRequest(boolean priorityRequest) {
        this.priorityRequest = priorityRequest;
    }
}
