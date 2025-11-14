# Steps to be done to add a new DTO / entity

This documentation describes the steps to create a new JPA entity (and related DTO).
The documentation focuses on an entity with a primary key which is a surrogate key, because this is the case in 99% of all entities.
The documentation describes the essential changes, java files will need additional imports, please add those as required.

1. Create the DTO. The required input is:
   - a value for the RTTI (run time type information). This is a unique 4 digit number which represents the 4 least significant digits of the surrogate key of the related entities.
     Plausibility check: the number must be unique in the project. It is advisable to use a common range for all DTOs in a single file.
   - fields for one or more natural keys
   - fields of the DTO
   The issue to create the DTOs must provide this information. In case it is missing or incomplete, reject the task, or ask for the missing input.

   The structure to generate is:
    ```
    class ExampleRef RTTI 2004 extends Ref {
    }
    final class ExampleKey extends ExampleRef {
        (all fields of the natural key go here. They all have the required specifier)
    }
    class ExampleDTO extends ExampleRef {
        (all fields of the DTO go here. The fields of the natural key should be the first. They also have a property notupdatable, which prevents them from being changed.)
    }
    ```
   (Substitute Example by the name of the actual DTO.)

   This code block goes into a bon file of the `dto` folder (`src/main/bon/dto` in a maven module of suffix `-api`).

2. Define the standard requests for search and CRUD, and optionally also lean search.
   Define the request handlers as follows:
    ```
    class ExampleCrudRequest                        extends CrudSurrogateKeyRequest<ExampleRef, ExampleDTO, FullTrackingWithVersion> {}
    class ExampleSearchRequest                      extends SearchRequest<ExampleDTO, FullTrackingWithVersion> {}
    class ExampleLeanSearchRequest                  extends LeanSearchRequest {}
    ```
   The LeanSearchRequest is defined only, if the natural key consists of a single field only.
   The code block goes into a bon file in the `request` folder (`src/main/bon/request`) in the same maven module.

3. Define the view model information for the user interface.
   In the same maven module, there should be a Java class which implements the interface `IViewModelContainer`. Extend this by an additional field
    ```
    private static final CrudViewModel<ExampleDTO, FullTrackingWithVersion> EXAMPLE_VIEW_MODEL
      = new CrudViewModel<>(
        ExampleDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ExampleSearchRequest.BClass.INSTANCE,
        ExampleCrudRequest.BClass.INSTANCE);
    ```
   where the field name (in caps, because it is a constant) consists of the DTO name (without the DTO suffix), and the suffix `_VIEW_MODEL`. The constructor parameters relate to:
   - DTO name
   - `FullTrackingWithVersion`
   - the name of the search request
   - the name of the CRUD request

   Also register that field **inside the `register()` method**, which is a required override from the `IViewModelContainer` interface:
    ```
    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("example", EXAMPLE_VIEW_MODEL);
    }
    ```
   where the first parameter is the name of the DTO (without the DTO suffix), and first character in lowercase, which will below be referred to as view model name.

4. Define properties for the English and German translations.
   Again in the same maven module, in folder `src/main/resources/translations`, there are files `headers_en.properties` (for English) and `headers_de.properties` (for German).
   Add lines of format
    ```
    @.example.fieldname=(translation)
    ```
   for every fieldname of the DTO, where example is the view model name and `(translation)` is the placeholder for the English, respectively German translation (label) of the field name.

5. Define the default filters and result grid columns for the screen.
   In folder `src/main/resources/gridconfig`, create a new file, named (view model name).json, which contains a valid JSON specification, as follows:
    ```
    {
        "viewModel": "example",
        "filters": [
            { "filterType": "W", "fieldName": "exampleId" }
        ],
        "fields": [
            "exampleId", "field1", "field2"
        ]
    }
    ```

6. Define the DSL specification which will create the JPA entity.
   The following code goes into a bddl file in the `entity` folder of a maven module of suffix `-jpa-entity` or just `-jpa`, in case `-jpa-entity` does not exist.
    ```
    entity ExampleEntity category cfg is ExampleDTO inheritance tablePerClass {
        cacheable
        tenantClass InternalTenantId
        pk (objectRef)
        index unique (tenantId, exampleId)
    }
    ```
   In this example, `exampleId` has been used as a placeholder for the field(s) in the natural key (if the natural key consists of multiple fields, separate them by comma).

7. Add a resolver entry for the new entity.
   In the same JPA maven module (suffix `-jpa` or `-jpa-entity`), locate the `*Resolvers.xtend` class in the `src/main/xtend/.../jpa/persistence/impl/` directory.
   Add a new method declaration for the entity resolver:
    ```
    def ExampleEntity getExampleEntity(ExampleRef ref) {}
    ```
   This method should be added inside the class annotated with `@AutoResolver42`. The method body remains empty as the actual implementation is generated.
   
   Optionally, if the entity can be accessed globally (e.g., configuration data), add the `@AllCanAccessGlobalTenant` annotation:
    ```
    @AllCanAccessGlobalTenant
    def ExampleEntity getExampleEntity(ExampleRef ref) {}
    ```
   Or if the global tenant should have access to all tenant's data, use `@GlobalTenantCanAccessAll`:
    ```
    @GlobalTenantCanAccessAll
    def ExampleEntity getExampleEntity(ExampleRef ref) {}
    ```

8. Create a mapper definition file for the entity.
   In the same JPA maven module, create a new Xtend class in the `src/main/xtend/.../jpa/mapping/impl/` directory, named `ExampleEntityMapper.xtend`.
   The mapper class should follow this structure:
    ```
    package com.arvatosystems.t9t.{module}.jpa.mapping.impl
    
    import com.arvatosystems.t9t.annotations.jpa.AutoHandler
    import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
    import com.arvatosystems.t9t.{module}.ExampleDTO
    import com.arvatosystems.t9t.{module}.jpa.entities.ExampleEntity
    import com.arvatosystems.t9t.{module}.jpa.persistence.IExampleEntityResolver
    
    @AutoMap42
    class ExampleEntityMapper {
        IExampleEntityResolver entityResolver
    
        def void d2eExampleDTO(ExampleEntity entity, ExampleDTO dto) {}
        def void e2dExampleDTO(ExampleEntity entity, ExampleDTO dto) {}
    }
    ```
   where `{module}` is a placeholder for the actual module name (e.g., `voice`, `doc`, etc.).
   
   The `@AutoMap42` annotation indicates that the mapper should be automatically generated. The two methods define the mapping directions:
   - `d2eExampleDTO`: maps from DTO to Entity (database to entity)
   - `e2dExampleDTO`: maps from Entity to DTO (entity to database)
   
   Optionally, you can add the `@AutoHandler` annotation to one of the methods if special handling is required (e.g., `@AutoHandler("CSP42")` for create/store/persist or `@AutoHandler("SP42")` for store/persist operations).

9. Create the SQL migration file.
   After completing the previous steps and successfully compiling the project, SQL files will be automatically generated into the `src/generated/sql` folder of the JPA maven module.
   
   You need to create a migration file in the `src/main/sql/POSTGRES/Migration` directory (or the appropriate database subdirectory) that contains:
   - The creation of the sequence (for generating unique IDs)
   - The creation of the main configuration/data table
   - The creation of the history table
   - The creation of views (typically `_nt` for no-tracking and `_v` for with-tracking)
   - The creation of the trigger function and trigger to write to the history table
   
   **Migration file naming convention:**
   The migration file must follow the pattern: `V<version>.<date>_<time>__<ticket-number>.sql`
   
   For example: `V8.0.20250227_1856__TBE1381.sql` where:
   - `V8.0` is the version prefix
   - `20250227` is the date in YYYYMMDD format
   - `1856` is the time in HHMM format
   - `TBE1381` is the ticket/issue number
   
   **Steps to create the migration file:**
   1. Compile the project using Maven: `mvn clean compile`
   2. Navigate to the JPA module's `src/generated/sql` directory
   3. Locate the generated SQL files for your new entity (they will match the table names)
   4. Copy the relevant SQL statements (CREATE SEQUENCE, CREATE TABLE, CREATE INDEX, COMMENT ON COLUMN, history table, views, trigger function and trigger)
   5. Create a new file in `src/main/sql/POSTGRES/Migration` with the proper naming convention
   6. Paste the SQL content into this migration file
   7. Review and ensure the SQL is correct and complete
   
   **Note:** The generated SQL files serve as templates. The migration file consolidates these into a single, versioned script that will be executed during database schema updates.

10. Create an Extension method for the merge operation (optional but recommended for testing).
    In one of the `t9t-tests-*` maven modules (typically `t9t-tests-embedded`, `t9t-tests-unit`, or a module-specific test project), create or extend an Extensions class to provide a convenient merge method for testing purposes.
    
    **Location:** Create or update a file in `src/main/xtend/com/arvatosystems/t9t/{module}/extensions/` (for example: `ExampleExtensions.xtend`)
    
    **Structure of the Extension class:**
    ```xtend
    package com.arvatosystems.t9t.{module}.extensions
    
    import com.arvatosystems.t9t.base.ITestConnection
    import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
    import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
    import com.arvatosystems.t9t.{module}.ExampleDTO
    import com.arvatosystems.t9t.{module}.ExampleKey
    import com.arvatosystems.t9t.{module}.request.ExampleCrudRequest
    import de.jpaw.bonaparte.pojos.api.OperationType
    
    class ExampleExtensions {
        // Extension method for merge operation with surrogate key
        def static CrudSurrogateKeyResponse<ExampleDTO, FullTrackingWithVersion> merge(ExampleDTO dto, ITestConnection dlg) {
            dto.validate
            return dlg.typeIO(new ExampleCrudRequest => [
                crud            = OperationType.MERGE
                data            = dto
                naturalKey      = new ExampleKey(dto.exampleId)  // Use the natural key field(s)
            ], CrudSurrogateKeyResponse)
        }
    }
    ```
    
    Replace `{module}` with the actual module name (e.g., `voice`, `doc`, `genconf`, etc.).
    Replace `ExampleDTO`, `ExampleKey`, `ExampleCrudRequest`, and `exampleId` with the actual names from your DTO definition.
    
    **For composite key entities:**
    If your entity uses a composite key instead of a surrogate key, use `CrudCompositeKeyResponse` and adjust the structure accordingly:
    ```xtend
    import com.arvatosystems.t9t.base.crud.CrudCompositeKeyResponse
    import com.arvatosystems.t9t.{module}.ExampleRef
    
    def static CrudCompositeKeyResponse<ExampleRef, ExampleDTO, FullTrackingWithVersion> merge(ExampleDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ExampleCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            key             = new ExampleKey(dto.field1, dto.field2)  // All composite key fields
        ], CrudCompositeKeyResponse)
    }
    ```
    
    This extension method allows you to easily test CRUD operations in integration tests by calling `dto.merge(testConnection)` using Xtend's extension method syntax.

