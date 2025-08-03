## Example T9T server configuration for Hibernate Search with Elasticsearch

    <hibernateSearchConfiguration>
        <searchType>elasticsearch</searchType>
        <schemaManagementStrategy>validate</schemaManagementStrategy>
        <mappingConfigurer>com.arvatosystems.t9t.hs.configurate.be.core.impl.EntityConfigurer</mappingConfigurer>
        <elasticSearchConfiguration>
            <hosts>localhost:9200</hosts>
            <username>elastic</username>
            <password>pwd</password>
        </elasticSearchConfiguration>
    </hibernateSearchConfiguration>

## Usage to obtain the backend specific services 
   
   ```java
   private final HibernateSearchConfiguration sc = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();
   // sc.getSearchType() can be "lucene" or "elasticsearch".
   private final IConfigurationServiceDistributor service = Jdp.getRequired(IConfigurationServiceDistributor.class, sc.getSearchType());
   private final PerformanceServiceElasticsearch service = Jdp.getRequired(PerformanceServiceElasticsearch.class, sc.getSearchType());

