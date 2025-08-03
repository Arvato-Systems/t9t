## Example T9T server configuration for Hibernate Search with Lucene

    <hibernateSearchConfiguration>
        <searchType>lucene</searchType>
        <schemaManagementStrategy>create-or-validate</schemaManagementStrategy>
        <mappingConfigurer>com.arvatosystems.t9t.hs.configurate.be.core.impl.EntityConfigurer</mappingConfigurer>
        <luceneConfiguration>
            <directoryType>local-filesystem</directoryType>
            <directoryRoot>C:\DEV\Aroma\Workspaces\_Egger\lucene-indexes</directoryRoot>
        </luceneConfiguration>
    </hibernateSearchConfiguration>

## Usage to obtain the backend specific services 
   
   ```java
   private final HibernateSearchConfiguration sc = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();
   // sc.getSearchType() can be "lucene" or "elasticsearch".
   private final IConfigurationServiceDistributor service = Jdp.getRequired(IConfigurationServiceDistributor.class, sc.getSearchType());
   private final PerformanceServiceElasticsearch service = Jdp.getRequired(PerformanceServiceElasticsearch.class, sc.getSearchType());

