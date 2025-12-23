Since version 5.10, there are 3 features added:

1. Allow overriding grid configs (TBE-739 @ 5.10)

    1. `overridesGridConfig` field was introduced in grid config JSON file.
    2. By specifying the target grid ID in  `overridesGridConfig` field, the target grid config will be overridden by the current grid config.

2. Extend grid configuration to allow display of components of fields with type JSON (TBE-739 @ 5.10)

    1. A map value is now able to be displayed in the table by specifying the map and the key now. Eg. `map[key]`.
    2. `mapColumns` field was introduced so that the map columns can be **pre-defined**.

        1. The columns that are specified in `mapColumns` field will be displayed in edit column selection modal.
        2. The map columns are loaded during application initialization.
        3. Any user-customized `mapColumns` will not work.


3. Allow extending grid configs (TBE-788 @ 6.0)

    1. `extendsGridConfig` field was introduced in grid config JSON file.
    2. By specifying the target grid ID in  `extendsGridConfig` field, the target grid config will be extended.
    3. `viewModel` & `fields` are mandatory. If there is nothing to be added in `fields`, please do include an empty list.
    4. Only 1 level of inheritance is supported.
    5. `overridesGridConfig` has higher precedence than `extendsGridConfig`.
    6. If both flags are found in the same grid config, log error and proceed with `overridesGridConfig` .
    7. If both `overridesGridConfig` & `extendsGridConfig` are found but in different grid config JSON, take `overridesGridConfig` and proceed.


‌

With the combination of these features, we could easily achieve:

1. Display z-field value in a table.
2. Override/Extend the existing grid config in client application without changing the ZUL files.
3. Override/Extend the existing grid config and the view model.

‌

## Additional notes

### 1. Overriding the existing grid config and display z-field value

The code snippet below shows that the grid config `salesOrder.solr` has been overridden by the `test.salesOrder.solr`. The Sales Order page will now display z-field values in the table. The user is able to select which table columns to be displayed. In the table column selection, `z[correlationId1]`, `z[correlationId2]` can be found at the bottom of the list.

```json
// test.salesOrder.solr.json
{
    ...Other Config
    "fields": [
        ...Other Fields
        "z[correlationId1]", "z[correlationId2]"
    ],
    "mapColumns": [
        "z[correlationId1]", "z[correlationId2]"
    ],
    "overridesGridConfig": "salesOrder.solr"
}
```

‌

In order to register the customized grid config, we need to register the grid config JSON file as shown below.

```java
// TestLeanGridConnfig.java
public class TestLeanGridConnfig implements ILeanGridConfigContainer {
    private static final String[] GRID_CONFIGS = {
        "test.salesOrder.solr"
    };

    @Override
    public List<String> getResourceNames() {
        return Arrays.asList(GRID_CONFIGS);
    }
}
```

‌

The z-field (or map value) header title can be specified as shown below. It is advisable to include the text in **defaults** translation file so that the translation text is used in both table header and column selection.

```plaintext
// defaults_en.properties
@.defaults.z[correlationId1]=Correlation Id 1
@.defaults.z[correlationId2]=Correlation Id 2
```

### 2. Extending the existing grid config

The code snippet below shows that the grid config `salesOrder.solr` has been extended by the `test.salesOrder.solr`. The `generalStatus` will be added to the screen and placed after the parent's filters. The fields `z[ip]` & `z[fraudCheckSkipped]` will be added to the table edit columns, and placed after the parent's fields. Please take note that if there is nothing to be added in `fields`, please do include an empty list. The `viewModel` is also mandatory, in this case, please populate the same value from parent grid config.

```json
// test.salesOrder.solr.json
{
    "viewModel": "follow the parent ViewModel, this is mandatory"
    "fields": [],
    "mapColumns": [
        "z[ip]", "z[fraudCheckSkipped]"
    ],
    "filters": [
      { "filterType":  "E", "fieldName": "generalStatus" }
    ],
    "extendsGridConfig": "salesOrder.solr"
}
```

### 3. Overriding the existing view model

In case, we are in the scenario of extending the existing view model in client application, we will need to register the new view model as shown below. Please take note that we will be using `IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.put`.

```
// TestModels.java
public final class TestModels implements IViewModelContainer {
    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.put("salesOrderSOLR", TEST_VIEW_MODEL);
    }
}
```
