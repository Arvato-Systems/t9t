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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Interface to the BPM module for applications.
 * All accesses to the BPM module from business logic should be done via this interface.
 */
public interface IBpmApplicationService {
    /** Returns the number of partitions of the cluster topic. */
    int getPartitionCount();

    /**
     * Returns the shard / node which is most suitable to run the workflow on.
     * This implementation ensures that all workflows for a given customer are run on the same node, such that JVM locking works best.
     * Returns null if and only if the input parameter is null.
     **/
    @Nullable Integer getNode(@Nullable String partitionKey);

    /**
     * Starts a new workflow process for a persisted item. This is an asynchronous operation.
     *
     * @param ctx the request context
     * @param workflowId the workflowId
     * @param objectRef the objectRef of the respective business object
     * @param refToLock some reference to aquire a semaphore lock on, can be null. For example an order reference
     * @param partitionKey the key to use to determine a node (partition) in clustered environments, will also be used to lock for synchronization. For example a customer ID
     */
    void startBusinessProcess(@Nonnull RequestContext ctx, @Nonnull String workflowId, @Nonnull Long objectRef, @Nullable Long refToLock, @Nullable String partitionKey);

    /**
     * Starts a new workflow process.
     * Same as the above, but without a RequestContext.
     */
    void startBusinessProcess(@Nonnull String workflowId, @Nonnull Long objectRef, @Nullable Long refToLock, @Nullable String partitionKey);


    /**
     * Restarts existing workflow process at the first step
     *
     * @param ref the objectRef of the respective business object
     * @param workflowId the workflowId
     * @return the ref of the process table
     */
    void restartExistingBusinessProcess(@Nonnull Long ref, @Nonnull String workflowId);

    /**
     * Continues existing workflow process at the current step or the step defined in parameter "workflowStep" (if filled)
     *
     * @param ref the objectRef of the respective business object
     * @param workflowId the workflowId
     * @return the ref of the process table
     */
    void continueExistingBusinessProcess(@Nonnull Long ref, @Nonnull String workflowId);

    /**
     * Restart or continues an existing workflow process, depending on the configuration.
     *
     * @param ref the objectRef of the respective business object
     * @param workflowId the workflowId
     * @return the ref of the process table
     */
    void restartOrContinueExistingBusinessProcess(@Nonnull Long ref, @Nonnull String workflowId);

    /**
     * Continues existing workflow process at the current step or the step defined in parameter "workflowStep" (if filled)
     *
     * @param ref the objectRef of the respective business object
     * @param workflowId the workflowId
     * @param createNewWf create new workflow if none is present
     * @param workflowStep the label of the workflow step where to resume the workflow. if null, workflow will resume at current step
     * @return the ref of the process table
     */
    void continueExistingBusinessProcess(@Nonnull Long ref, @Nonnull String workflowId, boolean createNewWf, @Nullable String workflowStep);
}
