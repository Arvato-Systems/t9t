package com.arvatosystems.t9t.hs.be.lucene.configurate.impl;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.hs.configurate.be.service.IPerformanceServiceDistributor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/*
 *   for backend specific performance services
 */
@Singleton
@Named("lucene")
public class PerformanceServiceLucene implements IPerformanceServiceDistributor {

    private final String searchType = ConfigProvider.getConfiguration().getHibernateSearchConfiguration().getSearchType();
}
