package com.arvatosystems.t9t.all.be.request;

import com.arvatosystems.t9t.all.T9tModuleConfigs;
import com.arvatosystems.t9t.all.request.SetT9tModuleConfigsRequest;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver;

import de.jpaw.dp.Jdp;

public class SetT9tModuleConfigsRequestHandler extends AbstractReadOnlyRequestHandler<SetT9tModuleConfigsRequest> {
    protected final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    protected final IDocModuleCfgDtoResolver docModuleCfgResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    protected final IEmailModuleCfgDtoResolver emailModuleCfgResolver = Jdp.getRequired(IEmailModuleCfgDtoResolver.class);
    protected final ISolrModuleCfgDtoResolver solrModuleCfgResolver = Jdp.getRequired(ISolrModuleCfgDtoResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final SetT9tModuleConfigsRequest request) throws Exception {
        final T9tModuleConfigs moduleConfigs = request.getModuleConfigs();
        if (moduleConfigs.getAuthModuleConfig() != null) {
            authModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getAuthModuleConfig());
        }
        if (moduleConfigs.getDocModuleConfig() != null) {
            docModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getDocModuleConfig());
        }
        if (moduleConfigs.getEmailModuleConfig() != null) {
            emailModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getEmailModuleConfig());
        }
        if (moduleConfigs.getSolrModuleConfig() != null) {
            solrModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getSolrModuleConfig());
        }
        return ok();
    }
}
