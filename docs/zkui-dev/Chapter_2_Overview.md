## 1.1      Purpose of the UI

The ZK UI is used by customer service. It targets users which use aroma on a daily base, and are equipped with a screen of high resolution – at least full HD.

Users of this UI typically want as much information as possible per page.

Most of the screens of this UI belong to one of two categories:

* configuration (CRUD screens)

* data retrieval (search screens)

Others screens (change password etc.) are exceptions and won't be described in detail in this document.



## 1.2      Layout

The screens are always built using the same structure:

* a primary selection / filter area

* a main result overview grid

* a detail area which either offers CRUD functionality, or lists additional attributes. This area often is a tabbed form.

The user enters filter criteria in the top section, then presses a SEARCH button, and sees a result overview in the main grid. One of the rows on the main grid is selected, and provides the selection of what is shown in the bottom area. The bottom area is optional, in rare cases, the overview grid covers everything beneath the filter screen.



## 1.3      Key principles

ZK UI development should be done without Java code in zul files, because any syntax problems cannot be detected in the development IDE, and lead to exceptions at run time. Despite most examples on the ZK web site use that feature, they also warn that interpreted Java code is a performance hog.



A design principle also is to avoid mapping of identifiers where possible. This means that an id of a component is also used as the key to look up its translation, and as a key to query user permissions (where suitable).





## 1.4      Term: View Model ID

A view model is the POJO on which the UI works. These POJOs do not reference any UI objects. We use the Bonaparte plugins to create the POJOs and therefore additional meta / type information is available which will be used by the UI components.

In the context of this description, we associate the "ViewModelId" with a String which references a description structure of the data itself, as well as the basic commands to operate on it.

It essentially has 4 components (at time of writing):

* a reference to the metadata of the data part itself (dtoClass). This can be used for example to get a list of field descriptions, or to instantiate an instance of the DTO without using Java reflection (reflection is slow and forces you to catch all kinds of unusual exceptions).

* the same for the "tracking" part (in case the DTO is an object persisted in the database)

* a reference to a search request's metadata, which can be used by grids to retrieve data for a given filter

* a reference to a CRUD request's metadata, this is used in maintenance configuration screens only.

This structure allows to create standard screens in a generic way.



Multiple view model IDs can reference the same DTO. This is used for example for sales or delivery order, here there is one view model ID which defines search access by a SOLR type search request, while the other references a database-only (standard) search request.



Because these structures are used by multiple UIs (ZK, Android native, Angular 2), they are defined in the API projects. At application startup, they are collected and the lookup map is then available as

IViewModelContainer._**CRUD_VIEW_MODEL_REGISTRY**_.





**Example**

```java
public final class T9tCoreModels implements IViewModelContainer {

    private static final CrudViewModel<StatisticsDTO, WriteTracking> STATISTICS_VIEW_MODEL
      = new CrudViewModel<>(
        StatisticsDTO.BClass.INSTANCE,
        WriteTracking.BClass.INSTANCE,
        StatisticsSearchRequest.BClass.INSTANCE,
        StatisticsCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("statistics",       STATISTICS_VIEW_MODEL);
    }
}
```






## 1.5      Term: Grid ID

A grid definition is a description of an appearance of such a view model in the UI. This essentially consists of

* the list of filter fields, and their types

* the list of displayed columns, their ordering and the column widths

* a reference to the view model ID



Multiple grid definitions can reference the same view model, the order items for example could have some selection of columns in one screen, and a different one in another screen.

A grid ID references such a grid definition. Usually, every grid in the UI has its own grid ID, and therefore this is sometimes also referred to as screen ID.



**Historic naming conventions**

Historic grid definition IDs used a notation of "(_menu ID_)/(_screen ID_)/(_grid number in screen_)".

The number was not very self-explanatory, and in practice often a different second part of the name has been used, leaving the grid number always as "1".

The slashes caused issues because the grid definitions are stored in files, and on UNIX type systems they collide with directory separators. As a workaround, slashes are temporarily replaced by dollar signs for file access.



**New naming convention**

The recommended approach is to use qualified IDs as anywhere else for grid IDs, for example "salesOrder.orderItem".



**Storing grid definitions**

Historically, all grid definitions were always stored in the database, in the generic configuration table. In t9t/a28, (grid config v2) a default was loaded directly from a grid configuration JSON file, and only user (or tenant) specific changes were stored in the database, in a dedicated table (p28_cfg_grid_config). For both approaches, the JSON files were lengthy and had to be updated whenever a new field was added to the DTO.

With the new approach (grid config v3), the major part of the grid configuration is always generated from the DTOs and the grid config JSON only lists the relevant parts (filters and visible column names).

**Example**
```json
{
    "viewModel": "statistics",
    "filters": [
        { "filterType": "R", "fieldName": "startTime" },
        { "filterType": "L", "fieldName": "recordsProcessed" },
        { "filterType": "L", "fieldName": "recordsError" },
        { "filterType": "R", "fieldName": "count1" },
        { "filterType": "R", "fieldName": "count2" },
        { "filterType": "W", "fieldName": "processId" },
        { "filterType": "W", "fieldName": "info1" },
        { "filterType": "W", "fieldName": "info2" }

    ],
    "fields": [
        "processId", "startTime", "endTime", "durationInMs", "recordsProcessed", "recordsError", "count1", "count2", "count3", "count4", "info1", "info2"
    ],
    "fieldWidths": [ 140, 140, 140, 180, 180, 180, 80, 80, 80, 80, 340, 340 ]
}
```


Note: For most components, if only either ID or grid ID is given, it serves as a default for the other of the two. If both are specified, the ID should be specified first in the zul file.

Windows of different IDs can refer to the same grid ID, but not vice versa.



## 1.6      Permissions

Permissions are stored a lot more compact than before. They also no longer use a separate ID. Instead the ID given to the window itself (which should match the ID of the main grid). A permission in t9t is not just a yes / no, but defines an Enumset of Enum OperationType.



The operation types are used intuitively:

| **OperationType** | **Required for** | **Comments** |
| --- | --- | --- |
| EXECUTE | screen use | screen not visible in menu if permission missing |
| SEARCH | search using own filters | set in most cases, if unset only predefined filter criteria will work (entry via other screens) |
| EXPORT | data export to Excel / CSV | Export button always grey if unset |
| CONFIGURE | grid customization | Allow the use to alter his / her own grid configuration preferences |
| CONFIGURE + ADMIN | grid customization (tenant default) | allow to define tenant default settings of grid configuration |
| CONTEXT | use of context menus | Specific permissions defined per context menu entry |



CRUD type screens use additional criteria:

| **OperationType** | **Required for** | **Comments** |
| --- | --- | --- |
| CREATE | create new records |   |
| UPDATE | update existing records |   |
| DELETE | deletion of records |   |
| ACTIVATE | activation of records |   |
| INACTIVATE | deactivation of records |   |
|   |   |   |



If a window uses tabbed detail areas (additional grids), each of the subgrids have their own permission (again, matching the corresponding ID of the tab).

(Note: In case ZK component ID and grid ID are different, the ZK component ID is used to determine the permission.)
