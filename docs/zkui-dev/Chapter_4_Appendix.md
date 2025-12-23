## 1.1      Naming reference

As many IDs are generated automatically, this section provides an overview about the generated IDs, these are relevant for translations.



| **Suffix** | **Used for** |
| --- | --- |
| .title | window title |
| .exportButton | grid export button |
| .okButton | form OK button |
| .cancelButton | from CANCEL button |
| .l | label part of Cells28 |
| .c | InputElement component part of Cells28 |
| .ctx | context menu (for grid) |
| .ctx.(_option_) | Option (_option_) in context menu |
|   |   |



## 1.2      Checklist

In order to create a new screen, the following steps must be done:

* If the CrudViewModel does not yet exist, create an entry in the API project and reference it:
```
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
```

* Describe the filters and grid columns as a lean grid config (json file), which also sits in the same API project:
```
{
    "viewModel": "subscriberConfig",
    "filters": [
        { "filterType": "W", "fieldName": "eventID" }
    ],
    "fields": [
        "eventID", "handlerClassName", "remark"
    ]
}
```

* In exceptional cases, reference the json in the lean grid config java class: (Only if this information cannot be derived from the CrudViewModel, in that case it would cause an error because it is created implicitly).

```
    private static String [] GRID_CONFIGS = {
        "subscriberConfig.main",
        "sliceTracking.main"
     };
```

* Create the grid header translations, they go into the API project as well

* Create the zul for the screen, this goes into the UI screens project

```
<?xml version="1.0" encoding="UTF-8"?>
<?init class="com.arvatosystems.t9t.zkui.init.WorkbenchInit" pagename="subscriberConfig28"?>

<window28 id="subscriberConfigWin">
    <threesections28 gridId="subscriberConfig.main">
        <crud28>
            <form28 id="subscriberConfigCrud" viewModelId="subscriberConfig" aspect="2" viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.GenericVM', inst=inst)">
                <rows>
                    <cells28 id="eventID"           value="@bind(vm.data.eventID)"/>
                    <cells28 id="handlerClassName"  value="@bind(vm.data.handlerClassName)"/>
                    <cells28 id="remark"            value="@bind(vm.data.remark)"/>
                </rows>
            </form28>
        </crud28>
    </threesections28>
</window28>
```

* Create the translations for the screen labels, this (currently) goes into the UI project as well.

## 1.3      Global Configuration (in UI)

Some configuration / UI behaviour can be enabled globally.

These are set in the file **t9t-zkui-configuration.properties** in the screens project, the files is usually deployed with the application.



The following values can be set:

| **Configuration name** | **Description** |
| --- | --- |
| crud.protected.view.enable | If set, fields in CRUD screens will be disabled by default and only enabled after the user pushes the "edit" button. This button is only shown if this configuration has been activated. |
| crud.show.message | A save action will notify completion via popup if enabled. This popup must be manually closed. |
| grid.results.autoCollapse | If activated, a search with at most one result will automatically minimize the result overview screen portion in order to assign more screen space to the details portion. |
| grid.lineWrap | If set, then text in result overview grid cells will be wrapped if it exceeds the column width. |
| grid.dynamicColumnSize | If set, then data returned from searches will be used to determine the width of the result overview grid columns. (Will usually result in flickering / changing widths when pagination is used). |
| grid.markRedOnSort | If set, changes of sort order for the result overview grid will be marked red to indicate "configuration change". |
| menu.useMenuIcons | If set, icons will be shown in the screen selection menus.  If unset, only the screen name will be shown. |
| export.defaultLimit | (numeric parameter) sets the default number of rows for the export data popup. |
| datePicker.showToday | Activates the "today" button in date pickers. |





## 1.4      Global Configuration (in Backend)

Some configuration / UI behaviour can be enabled globally in the backend configuration file.

These are set in the generic z fields of the backend's config.xml:

| **Configuration name** | **Description** |
| --- | --- |
| environment | Some text to display in the application's header, usually to prominently display the type of environment (test / integration / UAT / production) |
| environment.css | A specific CSS class to use for the environment display (for example to also color-code the environment type). |
