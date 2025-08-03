package com.arvatosystems.t9t.hs.be.elasticsearch.configurate.impl;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.hs.configurate.be.service.IConfigurationServiceDistributor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/*
 *   for backend specific configuration services
 */
@Singleton
@Named("elasticsearch")
public class ConfigurationServiceElasticsearch implements IConfigurationServiceDistributor {

    private final String searchType = ConfigProvider.getConfiguration().getHibernateSearchConfiguration().getSearchType();

}
