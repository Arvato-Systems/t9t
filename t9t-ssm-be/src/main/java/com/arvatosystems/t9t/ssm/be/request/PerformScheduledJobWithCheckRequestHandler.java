package com.arvatosystems.t9t.ssm.be.request;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesRequest;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesResponse;
import com.arvatosystems.t9t.ssm.request.PerformScheduledJobWithCheckRequest;

import de.jpaw.dp.Jdp;

public class PerformScheduledJobWithCheckRequestHandler extends AbstractRequestHandler<PerformScheduledJobWithCheckRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformScheduledJobWithCheckRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final RequestContextScope requestContextScope = Jdp.getRequired(RequestContextScope.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformScheduledJobWithCheckRequest request) throws Exception {
        // perform analysis of prior instances
        final Long setupRef = request.getSchedulerSetupRef();
        final List<ProcessStatusDTO> priorInstances = requestContextScope.getProcessStatusForScheduler(setupRef);
        if (!priorInstances.isEmpty()) {
            if (request.getConcurrencyType() == SchedulerConcurrencyType.SKIP_INSTANCE) {
                LOGGER.info("Scheduler congestion: {} prior instances running for setup {}, and we are not allowed to run in parallel, skipping run!",
                  priorInstances.size(), setupRef);
                return ok();
            } else {
                // there are prior instances, and we expect some action required
                LOGGER.info("Scheduler congestion: There are {} prior instances running for setup objectRef {}", priorInstances.size(), setupRef);
                final DealWithPriorJobInstancesResponse dwpjir = executor.executeSynchronousAndCheckResult(ctx,
                  new DealWithPriorJobInstancesRequest(setupRef), DealWithPriorJobInstancesResponse.class);
                if (!dwpjir.getInvokeNewInstance()) {
                    return ok();
                }
            }
        }
        // regular invocation of job
        return executor.executeSynchronousWithPermissionCheck(ctx, request.getRequest());
    }
}
