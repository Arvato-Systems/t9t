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
   
   **Note:** If the DTO and its natural key are already defined, this is not an error. In this case, skip this step and consider the existing definitions as part of the specification.

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
   
   **Important:** The annotations `@AllCanAccessGlobalTenant` and `@GlobalTenantCanAccessAll` are normally **NOT** used. These annotations should only be added if explicitly specified by the requester. If the requirement specifies their use:
   - Use `@AllCanAccessGlobalTenant` if the entity can be accessed globally (e.g., configuration data accessible by all tenants):
    ```
    @AllCanAccessGlobalTenant
    def ExampleEntity getExampleEntity(ExampleRef ref) {}
    ```
   - Use `@GlobalTenantCanAccessAll` if the global tenant should have access to all tenant's data:
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
   
   Additionally, you need to create 2 or 3 Java request handler classes in the same JPA maven module. Create these in the `src/main/java/.../jpa/request/` directory, using the existing request handlers for `CsvConfiguration` (in maven module `t9t-io-jpa`) as a template:
   
   **8.1. Create the Search request handler** (`ExampleSearchRequestHandler.java`):
    ```java
    package com.arvatosystems.t9t.{module}.jpa.request;
    
    import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
    import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
    import com.arvatosystems.t9t.base.search.ReadAllResponse;
    import com.arvatosystems.t9t.base.services.RequestContext;
    import com.arvatosystems.t9t.{module}.ExampleDTO;
    import com.arvatosystems.t9t.{module}.jpa.entities.ExampleEntity;
    import com.arvatosystems.t9t.{module}.jpa.mapping.IExampleDTOMapper;
    import com.arvatosystems.t9t.{module}.jpa.persistence.IExampleEntityResolver;
    import com.arvatosystems.t9t.{module}.request.ExampleSearchRequest;
    
    import de.jpaw.dp.Jdp;
    
    public class ExampleSearchRequestHandler extends
            AbstractSearchWithTotalsRequestHandler<Long, ExampleDTO, FullTrackingWithVersion, ExampleSearchRequest, ExampleEntity> {
    
        private final IExampleEntityResolver resolver = Jdp.getRequired(IExampleEntityResolver.class);
        private final IExampleDTOMapper mapper = Jdp.getRequired(IExampleDTOMapper.class);
    
        @Override
        public ReadAllResponse<ExampleDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
                final ExampleSearchRequest request) throws Exception {
            return execute(ctx, request, resolver, mapper);
        }
    }
    ```
   
   **8.2. Create the CRUD request handler** (`ExampleCrudRequestHandler.java`):
    ```java
    package com.arvatosystems.t9t.{module}.jpa.request;
    
    import com.arvatosystems.t9t.base.api.ServiceResponse;
    import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
    import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
    import com.arvatosystems.t9t.base.services.RequestContext;
    import com.arvatosystems.t9t.{module}.ExampleDTO;
    import com.arvatosystems.t9t.{module}.ExampleRef;
    import com.arvatosystems.t9t.{module}.jpa.entities.ExampleEntity;
    import com.arvatosystems.t9t.{module}.jpa.mapping.IExampleDTOMapper;
    import com.arvatosystems.t9t.{module}.jpa.persistence.IExampleEntityResolver;
    import com.arvatosystems.t9t.{module}.request.ExampleCrudRequest;
    
    import de.jpaw.dp.Jdp;
    
    public class ExampleCrudRequestHandler extends AbstractCrudSurrogateKeyRequestHandler<ExampleRef, ExampleDTO,
      FullTrackingWithVersion, ExampleCrudRequest, ExampleEntity> {
    
        private final IExampleEntityResolver resolver = Jdp.getRequired(IExampleEntityResolver.class);
        private final IExampleDTOMapper mapper = Jdp.getRequired(IExampleDTOMapper.class);
    
        @Override
        public ServiceResponse execute(final RequestContext ctx, final ExampleCrudRequest crudRequest) throws Exception {
            return execute(ctx, mapper, resolver, crudRequest);
        }
    }
    ```
   
   **8.3. Create the Lean Search request handler (optional)** (`LeanExampleSearchRequestHandler.java`):
   
   This handler is only needed if the `ExampleLeanSearchRequest` was defined in step 2 (i.e., when the natural key consists of a single field).
    ```java
    package com.arvatosystems.t9t.{module}.jpa.request;
    
    import com.arvatosystems.t9t.base.jpa.impl.AbstractLeanSearchRequestHandler;
    import com.arvatosystems.t9t.base.search.Description;
    import com.arvatosystems.t9t.{module}.jpa.entities.ExampleEntity;
    import com.arvatosystems.t9t.{module}.jpa.persistence.IExampleEntityResolver;
    import com.arvatosystems.t9t.{module}.request.LeanExampleSearchRequest;
    
    import de.jpaw.dp.Jdp;
    
    public class LeanExampleSearchRequestHandler extends AbstractLeanSearchRequestHandler<LeanExampleSearchRequest, ExampleEntity> {
        public LeanExampleSearchRequestHandler() {
            super(Jdp.getRequired(IExampleEntityResolver.class), (final ExampleEntity it) -> {
                final String description = it.getDescription() == null ? "?" : it.getDescription();
                return new Description(null, it.getExampleId(), description, false, false);
            });
        }
    }
    ```
   
   In all the code above, replace `{module}` with the actual module name (e.g., `voice`, `doc`, `io`, etc.) and `Example` with the actual entity name.

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

11. Create a screen ZUL file for the user interface.
    If your entity requires a user interface screen, you need to create a ZUL (ZK UI) file in the repository's UI project.
    
    **Location:** The ZUL file should be created in `src/main/webapp/screens/<subfolder>/` within the UI project (typically `t9t-zkui-screens` or similar).
    
    **File naming convention:** The file should be named `<viewModelName>28.zul`, where `<viewModelName>` is the view model name you registered in step 3 (e.g., `example28.zul` for view model name `example`).
    
    **Choosing the subfolder:**
    Common subfolders include:
    - `data_admin` - for administrative and configuration screens
    - `user_admin` - for user and authentication management screens
    - `monitoring` - for monitoring and statistics screens
    - `report` - for reporting and data I/O screens
    - `ai` - for AI-related screens
    - `voice_setup` - for voice and AI assistant configuration
    - `t9t_int_comm` - for communication and document screens
    - `session` - for user session-related screens
    
    Choose the subfolder that best fits the purpose of your entity. If you're unsure, use `data_admin` for configuration entities.
    
    **Structure of the ZUL file:**
    
    Use `genericConfig28.zul` as a template. The basic structure is:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <?init class="com.arvatosystems.t9t.zkui.init.WorkbenchInit" pagename="<viewModelName>"?>
    
    <window28 id="<viewModelName>Win">
        <threesections28 gridId="<viewModelName>">
            <crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM', vmId='<viewModelName>')"
                    currentMode="@load(vm.currentMode)">
                <form28 id="<viewModelName>Crud" aspect="2" numColumns="2">
                    <rows>
                        <cells228 id ="field1"         value ="@load(vm.data.field1)    @save(vm.data.field1,   before='commandSave')"
                                  id2="field2"         value2="@load(vm.data.field2)    @save(vm.data.field2,   before='commandSave')"/>
                        <cells228 id ="field3"         value ="@bind(vm.data.field3)"
                                  id2="field4"         value2="@bind(vm.data.field4)"/>
                        <!-- Add more rows for additional fields -->
                    </rows>
                </form28>
            </crud28>
        </threesections28>
    </window28>
    ```
    
    Replace `<viewModelName>` with your actual view model name (e.g., `example`).
    Replace `field1`, `field2`, etc. with the actual field names from your DTO.
    
    **Field binding patterns:**
    - For fields that should NOT be updatable (like natural key fields): Use `@load(vm.data.fieldName) @save(vm.data.fieldName, before='commandSave')`
    - For regular editable fields: Use `@bind(vm.data.fieldName)`
    - For multi-line text fields: Add `rows1="4"` (or another number) to the cells28 element
    - For fields with conditional visibility: Add `visible="@load(vm.data.someCondition)"`
    
    **Layout options:**
    - `cells228` - displays two fields side by side in a row
    - `cells28` - displays a single field spanning the full width (use `colspan1="3"` to span across columns)
    - `aspect="2"` - controls the aspect ratio of form cells (1 for square, 2 for wider)
    - `numColumns="2"` - defines the number of columns in the form
    
    **For entities with multiple tabs:**
    If your entity has many fields that logically group into categories, you can use a tabbed layout similar to `aiAssistant28.zul`:
    ```xml
    <crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM', vmId='<viewModelName>')"
            currentMode="@load(vm.currentMode)">
        <tabbox id="tabboxDetail" vflex="1">
            <tabs>
                <tab28 id="mainTab"/>
                <tab28 id="additionalTab"/>
            </tabs>
            <tabpanels vflex="1" id="tabpanelsDetail">
                <tabpanel vflex="1" id="<viewModelName>.main.panel">
                    <form28 id="<viewModelName>CrudMain" aspect="1" numColumns="2">
                        <!-- Main fields here -->
                    </form28>
                </tabpanel>
                <tabpanel vflex="1" id="<viewModelName>.additional.panel">
                    <form28 id="<viewModelName>CrudAdditional" aspect="1" numColumns="2">
                        <!-- Additional fields here -->
                    </form28>
                </tabpanel>
            </tabpanels>
        </tabbox>
    </crud28>
    ```

12. Create a dummy icon file for the new screen.
    Every screen requires an associated icon file for display in the menu. The actual icon design will be added later, but a dummy placeholder is needed immediately.
    
    **Location:** `src/main/webapp/img/menu/` within the UI project (typically `t9t-zkui-screens`).
    
    **File naming convention:** The icon must be named `<viewModelName>Screen.png`, where `<viewModelName>` is the view model name you registered in step 3.
    
    For example, if your view model name is `example`, the icon file should be named `exampleScreen.png`.
    
    **Creating the dummy icon:**
    1. Navigate to the icon directory:
       ```bash
       cd <ui-project>/src/main/webapp/img/menu/
       ```
    
    2. Copy any existing icon file as a placeholder:
       ```bash
       cp genericConfigScreen.png <viewModelName>Screen.png
       ```
       
       For example:
       ```bash
       cp genericConfigScreen.png exampleScreen.png
       ```
    
    **Note:** The actual icon design will typically be provided by a designer or UI team later. The dummy icon ensures the screen can be displayed in the menu immediately without breaking the UI.

13. Register the new screen in the configuration properties file.
    The new screen must be registered in the ZK UI configuration file to make it accessible through the application menu.
    
    **Location:** `src/main/webapp/WEB-INF/resources/t9t-zkui-configuration.properties` within the UI project (typically `t9t-zkui-screens`).
    
    **Note:** If you're using this documentation for a different repository (not `t9t`), the file might be named differently (e.g., `<prefix>-zkui-configuration.properties` where `<prefix>` is your project prefix).
    
    **Configuration format:**
    Each menu section in the file follows this format:
    ```properties
    menu.<section>= {
      <iconName>,                         <category>,               <name>,                             <zul-file-path>,                                      <menuItemVisible>
    }
    ```
    
    Where:
    - `<iconName>` - The icon file name WITHOUT the `.png` extension (e.g., `exampleScreen`)
    - `<category>` - The menu category (e.g., `systemAdmin`, `monitoring`, `communication`)
    - `<name>` - The view model name (e.g., `example`)
    - `<zul-file-path>` - The relative path to the ZUL file (e.g., `screens/data_admin/example28.zul`)
    - `<menuItemVisible>` - Always set to `true` to make the menu item visible
    
    **Available menu sections:**
    - `menu.job_report` - For jobs, reports, data I/O, and scheduling screens
    - `menu.sysadmin` - For system administration and configuration screens
    - `menu.monitor` - For monitoring, statistics, and logging screens
    - `menu.communication` - For document and email management screens
    - `menu.voice` - For voice and AI-related screens
    - `menu.session` - For user session-related screens
    
    **Steps to add your screen:**
    1. Identify the appropriate menu section for your screen. **If this information is not provided in the issue, ask the requester to specify which menu section (e.g., `menu.sysadmin`) and at which position the new screen should be added.**
    
    2. Open the configuration file and locate the appropriate `menu.<section>` block.
    
    3. Add your screen entry at the specified position within that section. For example, to add an `example` screen to `menu.sysadmin`:
       ```properties
       menu.sysadmin= {
         tenantScreen,                           systemAdmin,              tenant,                             screens/user_admin/tenant28.zul,                      true
         userScreen,                             systemAdmin,              user,                               screens/user_admin/user28.zul,                        true
         exampleScreen,                          systemAdmin,              example,                            screens/data_admin/example28.zul,                     true
         roleScreen,                             systemAdmin,              role,                               screens/user_admin/role28.zul,                        true
         ...
       }
       ```
    
    4. Ensure proper formatting:
       - Maintain consistent column alignment with other entries in the section
       - Each entry must end with a comma (except the last entry)
       - Use appropriate spacing to align the columns for readability
    
    **Example entry:**
    ```properties
    exampleScreen,                          systemAdmin,              example,                            screens/data_admin/example28.zul,                     true
    ```
    
    After adding the configuration entry, the screen will appear in the application menu under the specified section.

