package com.arvatosystems.t9t.hs.be.elasticsearch.configurate.impl;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.hs.configurate.be.service.IPerformanceServiceDistributor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/*
 *   for backend specific performance services
 */
@Singleton
@Named("elasticsearch")
public class PerformanceServiceElasticsearch implements IPerformanceServiceDistributor {

    private final String searchType = ConfigProvider.getConfiguration().getHibernateSearchConfiguration().getSearchType();
}
