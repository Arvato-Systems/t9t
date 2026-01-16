# Hibernate Search in t9t (Framework/Engine)

This documentation describes the **t9t framework module** around Hibernate Search:

- Bootstrapping / configuration (integration into Hibernate ORM)
- Programmatic mapping via JSON (`EntityConfigurer`)
- Analyzer/normalizer concept (Lucene vs Elasticsearch)
- Query building (predicate/sort), SearchFilter → predicate converters
- Core services (e.g., `IIndexMaintenance`, index status)

**Project/application details** (a28 adapter, a28 requests/flows, a28-specific entities & maintenance dependencies) are documented separately:
- see the a28 project's `hibernate-search-a28.md` documentation

---

## 1. Overview

Hibernate Search provides a uniform search API over different backends:

- **Lucene**: local index files (filesystem)
- **Elasticsearch**: remote index via HTTP/REST

In the t9t context, the most important tasks are:

- Configuring Hibernate Search properties during ORM initialization
- Loading a JSON-based mapping definition and creating the index mapping programmatically
- Providing a search engine that translates search filters/expressions into predicates
- Providing helper mechanisms (caches, sort fallbacks, converters)

---

## 2. Architecture (components & data flow)

### 2.1 Components (t9t)

- **Configuration**: `serverConfigurations/t9tconfig.xml` (provided via `ConfigProvider` / server configuration)
- **Bootstrapping**: `EMFCustomizer` sets Hibernate Search properties in the EMF/ORM configuration
- **Mapping**: `EntityConfigurer` loads JSON and registers entities/fields programmatically
- **Search**: `HibernateSearchEngine` + helpers/caches for building predicates and sorts
- **Maintenance**: abstraction via `IIndexMaintenance` + status/maintenance handlers

### 2.2 Startup flow (high level)

```text
t9tconfig.xml
  → EMFCustomizer.getCustomizedEmf(..., configureTextSearch=true)
    → sets Hibernate Search properties (enabled, backend.type, schema strategy, analysis configurer, mapping.configurer)
      → Hibernate ORM starts
        → Hibernate Search initializes
          → calls mapping.configurer: EntityConfigurer
            → EntityConfigurer loads the JSON configuration
              → creates/validates the schema in the backend
```

---

## 3. Configuration via `t9tconfig.xml` / EMFCustomizer

### 3.1 Example excerpt (schema/backend)

```xml
<searchConfiguration>
    <strategy>HIBERNATE-SEARCH</strategy>
</searchConfiguration>

<hibernateSearchConfiguration>
    <searchType>lucene</searchType>
    <schemaManagementStrategy>create-or-validate</schemaManagementStrategy>
    <luceneConfiguration>
        <directoryType>local-filesystem</directoryType>
        <directoryRoot>...</directoryRoot>
    </luceneConfiguration>
</hibernateSearchConfiguration>
```

### 3.2 Optional: `fuzzySearchLevel`

`<fuzzySearchLevel>` controls the default fuzziness used when building text predicates.

- 0: disabled
- 1: low
- 2: higher

### 3.3 Which Hibernate Search properties does t9t set?

In `EMFCustomizer`, among others, the following are set:

```text
putOpt(myProps, "hibernate.search.enabled", "true");
putOpt(myProps, "hibernate.search.backend.type", hibernateSearchConfiguration.getSearchType());
putOpt(myProps, "hibernate.search.schema_management.strategy", hibernateSearchConfiguration.getSchemaManagementStrategy());
putOpt(myProps, "hibernate.search.mapping.configurer", "com.arvatosystems.t9t.hs.configurate.be.core.impl.EntityConfigurer");

if (searchType == "lucene") {
  putOpt(myProps, "hibernate.search.backend.directory.type", ...);
  putOpt(myProps, "hibernate.search.backend.directory.root", ...);
  putOpt(myProps, "hibernate.search.backend.analysis.configurer", "...T9tLuceneAnalysisConfigurer");
} else if (searchType == "elasticsearch") {
  putOpt(myProps, "hibernate.search.backend.hosts", ...);
  putOpt(myProps, "hibernate.search.backend.username", ...);
  putOpt(myProps, "hibernate.search.backend.password", ...);
  putOpt(myProps, "hibernate.search.backend.analysis.configurer", "...T9tElasticsearchAnalysisConfigurer");
}
```

### 3.4 Property details & typical values (operational)

The following is a *practical* mapping of what these properties usually mean:

- `hibernate.search.enabled`
  - `true` enables Hibernate Search integration.
- `hibernate.search.backend.type`
  - `lucene` or `elasticsearch`.
- `hibernate.search.schema_management.strategy`
  - Typical values: `none`, `validate`, `create`, `create-or-validate`, `drop-and-create`.
  - **Production guideline**: prefer `validate` or `create-or-validate` depending on your deployment model.
- `hibernate.search.mapping.configurer`
  - Must point to the t9t mapping configurer (programmatic mapping from JSON).
- `hibernate.search.backend.analysis.configurer`
  - Backend-specific analysis configuration (defines analyzers/normalizers referenced by JSON).

Notes:

- Lucene uses filesystem directories and may create lock files (`write.lock`). In case of crashes, locks can remain.
- Elasticsearch uses an HTTP endpoint and may require credentials.

---

## 4. Mapping/Entities/Configuration loading (JSON)

### 4.1 Goal

The mapping is created **programmatically** from a JSON file. The format is designed so that projects (e.g., a28) can maintain their indexed entities/fields declaratively.

### 4.2 JSON structure (short form)

- `className`: fully-qualified JPA entity class
- `fields[]`: fields directly on the entity
  - `name`, `type` (`keywordfield | genericfield | fulltextfield`), optional `analyzer`
- `embeddedIndexEntities[]`: embedded structures
  - `targetEntity` (property name)
  - `includePaths[]` (whitelist of fields, including type/analyzer)

### 4.3 Embedded / dot notation / SHALLOW

- Embedded fields appear in the index using **dot notation**, e.g. `countryProps.countryCode`.
- `EntityConfigurer` typically uses `ReindexOnUpdate.SHALLOW` for `indexedEmbedded`.

Map-value entities are supported in a way that their field types can be registered in the mapping without creating separate indexes.

### 4.4 Caches

`EntityConfigurer` fills caches for later classification:

- keyword fields
- fulltext fields
- sort fields (logical → physical, usually `field` → `field_sort`)

### 4.5 Field types & `_sort` mechanism

- **keywordfield**: exact match, filters, sortable (normalizer)
- **genericfield**: numeric/temporal, sortable
- **fulltextfield**: analyzed; additionally, a keyword `_sort` field is created automatically

---

## 5. Backends: Lucene & Elasticsearch (same API, different operational details)

### Commonalities

- same Hibernate Search API
- same mapping configurer (`EntityConfigurer`)
- JSON configuration is backend-agnostic
- analyzer/normalizer names are a contract between JSON and the backend-specific analysis configurer

### Differences (operational)

**Lucene**
- local index via `hibernate.search.backend.directory.root`
- filesystem locks (e.g. `write.lock`)

**Elasticsearch**
- remote index via `hosts` / credentials
- mapping conflicts show up as schema/validation problems

---

## 6. Searching & sorting (engine/helper)

### 6.1 Query types

`HibernateSearchEngine` typically supports:

- SearchFilter → predicate via `HibernateSearchHelper.getSearchPredicate(...)`
- Expression → predicate via `getBool(..., expression)`
- otherwise `matchAll`

### 6.2 Sorting (with fallback)

Sorting first uses the sort-cache mappings, e.g. `name` → `name_sort`. If this fails in the backend, it falls back to the original field.

```java
String sortField = EntityConfigurer.getCachedSortFields().get(originalField);
try {
  query = query.sort(f -> desc ? f.field(sortField).desc() : f.field(sortField).asc());
} catch (Exception e) {
  query = query.sort(f -> desc ? f.field(originalField).desc() : f.field(originalField).asc());
}
```

### 6.3 Expression search (`getBool()`): tokenization, wildcards, fuzzy, boosting

`getBool()` builds a boolean predicate from field groups:


Pre-processing:

- `%` → `*`
- `_` → `?`
- split into terms by whitespace

Fuzziness:

- via `<fuzzySearchLevel>` or heuristics (e.g., term length)

Note: a28-specific topics like “how do the field names get in?” are described in the a28 documentation.

---

## 7. SearchFilter → predicate conversion (converter)

If `SearchCriteria.searchFilter` is set, the filter is converted into a Hibernate Search predicate.

High level:

- determine filter type via `searchFilter.ret$PQON()`
- resolve converter via `Jdp.getOptional(ISearchFilterToPredicate.class, pqon)`
- error if no converter exists (`SEARCH_FILTER_CONVERTER_NOT_FOUND`)

### 7.1 Standard converters

Standard converters are located in the `t9t-hibernate-search-be` module (package `.../converter/`), e.g.:

- `AndFilterToPredicate`
- `OrFilterToPredicate`
- `NotFilterToPredicate` (including special case NOT NULL → exists(field))
- `NullFilterToPredicate`
- numeric/temporal converters (ranges/terms/match)
- string converters (`AsciiFilterToPredicate`, `UnicodeFilterToPredicate`) delegate equals/like to `HibernateSearchHelper.getBool()`

---

## 8. Index maintenance

- `IIndexMaintenance` provides the abstraction for index maintenance.
- Status/checks are implemented via request handlers, e.g. a handler that iterates over all configured entities and checks a status per entity.

---

## 9. Appendix: t9t-relevant files/classes

### Bootstrapping / EMF

- `t9t-orm-hibernate-jpa/src/main/java/com/arvatosystems/t9t/orm/jpa/hibernate/impl/EMFCustomizer.java`

### Mapping / configuration loading

- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/core/impl/EntityConfigurer.java`
- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/core/util/ConfigurationLoader.java`
- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/core/service/impl/ConfigurationService.java`

### Search

- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/search/be/HibernateSearchEngine.java`

### Maintenance / status

- `t9t-base-sapi/src/main/java/com/arvatosystems/t9t/base/services/IIndexMaintenance.java`
- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/request/CheckIndexStatusForEntitiesRequestHandler.java`
- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/request/IndexCreationForEntitiesRequestHandler.java`
- `t9t-hibernate-search-be/src/main/java/com/arvatosystems/t9t/hs/configurate/be/request/IndexUpdateForEntitiesRequestHandler.java`
