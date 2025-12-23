# 3       Technical Implementation

## 3.1      General information

The screens are built as single pages, using ZK. We use the MVVM approach – the displayed data objects don't contain references to UI specific components. More specifically

* All objects displayed in a grid are generated classes of type

**BonaPortable**. In more than 90%, they correspond to persisted data, and the main object is an instance of the class **DataWithTrackingW**. (The W suffix denotes an additional component, a tenant reference as a wrapped Long (**DataWithTrackingP** would reference a primitive long to store the tenant reference)).

* For every grid, the information which fields are displayed is configured (by a JSON file embedded in the API projects for defaults, in the database for user specific preferences).

* For every grid, the data as displayed can be exported to CSV, XLS or XLSX (given the user has the corresponding permission).

In addition, data collected in modal popup windows also correspond to a **BonaPortable**. This allows a screen to let the user key in directly into some **RequestParameters** class which is then sent to the backend.



Jdp is used as a dependency injection replacement.





## 3.2      Component Overview

In order to simplify development, we use standard ZK components as well as customized components. The customized components for example offer embedded @Converter functionality, on most cases you can bind them directly to the data types as used in the Bonaparte files.

Most of the custom components sit in package **com.arvatosystems.t9t.zkui.components.basic**.

### 3.2.1    Standard ZK components

For documentation of the standard ZK components, please see the documentation by zkoss at https://www.zkoss.org/javadoc/8.0.0/zk/org/zkoss/zul/package-summary.html



### 3.2.2    Dropdown28\*

A couple of dropdowns are provided, some with a fixed data set (country codes, currency codes) and some which can depend on the tenant and also possibly on the user (in case of restrictions to only selected entities within the tenant). The fixed set dropdowns operate without translation of displayed labels and related values (both are Strings), but the others display IDs of a database entity and can map this to a reference (subclass of Ref, such as LocationRef, TaxGroupRef etc.).



The dropdowns are normally named by the Java field name of the displayed label in the DTO. (Some exceptions exist, marked below by an asterisk)



| Name | dropdown28\* (for possible values see list below) |
| --- | --- |
| Inherits from | combobox |
| Functionality | selection of one of multiple entries, with suggestions |
| Required parent | - |
| Allowed children | none |
|   |   |
|   |   |





The following dropdowns exist:



#### 3.2.2.1   Fixed set dropdowns

·         countryCode

·         currencyCode

·         languageCode

·         timeZoneId

·         charsetEncoding



#### 3.2.2.2   t9t dropdowns

·         asyncChannelId                                               (ASYNC)

·         asyncQueueId                                                 (ASYNC)

·         bucketCounterId                                              (STATS)

·         cannedRequestId (\* fieldname = requestId)       (CORE)

·         csvConfigurationId                                           (IO)

·         dataSinkId                                                       (IO)

·         docConfigId (\* fieldname = documentId)            (DOC)

·         processDefinitionId                                          (BPMN)

·         reportConfigId                                                 (REP)

·         roleId                                                              (AUTH)

·         schedulerSetupId                                             (SSM)

·         ticketId                                                           (VOICE)

·         userId                                                              (AUTH)

·         voiceApplicationId                                           (VOICE)






### 3.2.3    Window28

The window28 is a very slim component with little functionality. It just encapsules the translation of the header.



| Name | window28 |
| --- | --- |
| Inherits from | window |
| Functionality | adds a translated title, based on the id |
| Required parent | - |
| Allowed children | any |



**Example**
```
<window28 id="mainSearch">
    ...
</window28>
```



### 3.2.4    Modal28

The modal28 is a container for modal dialogs. It adds OK and cancel buttons. The user should set information about the viewModel as attribute. This is currently constant, the attribute

viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.GenericVM', inst=inst)"

should be copied to all modal28 screens; it is boilerplate code (a future refinement could create a binder programmatically, omitting the need for this).



| Name | modal28 |
| --- | --- |
| Inherits from | window |
| Functionality | adds buttons to accept the screen or cancel |
| Required parent | - |
| Allowed children | form28 |
|   |   |



**Example**

```
<modal28 id="export" viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.GenericVM', inst=inst)">
    <form28 id="exportParams" viewModelId="exportParams" aspect="2">
        <rows>
            <cells28 id="communicationFormatType" value="@bind(vm.data.communicationFormatType)"/>
            <cells28 id="offset"         value="@bind(vm.data.offset)"/>
            <cells28 id="limit"          value="@bind(vm.data.limit)"/>
            <cells28 id="enumOutputType" value="@bind(vm.data.enumOutputType)"/>
        </rows>
    </form28>
</modal28>
```



Execution of the modal dialog is done by a static utility method in class ModalWindows:


```java
public static <T extends BonaPortable> Component runModal(
            final String zulFile,
            final Component parent,
            final T srcViewModel,
            boolean deepCopy,
            final Consumer<T> onOK);
```


The parameters are
* reference of a zul file which defines the UI (as the one listed in the above example)
* a (suitable) parent component for the modal window
* a reference to an instance of the BonaPortable view model
* information if the dialog should work on the existing instance or a copy of it
* a lambda expression which is invoked in case the user has pressed OK, with the updated view model instance as a parameter.

The modal window is closed by the runModal method after execution of the lambda.











### 3.2.5    Label28

The label28 component provides a translated label, wrapped in a cell. The key for the translation is obtained from a view model ID (which must be defined in a parent component) and the id of this component, which is a path name of the field in the view model. An asterisk is automatically added if the field is required.



| Name | label28 |
| --- | --- |
| Inherits from | cell |
| Functionality | translated label, with marker if field is required |
| Required parent | any which defines a view model ID |
| Allowed children | none |





### 3.2.6    Field28

The field28 component provides a dynamic input field as suitable for the field defined by its ID and a view model as defined by one of the component's parents, wrapped into a cell component.



In other words, if the data type of the field in the bon file is an int, it will create an Intbox, if it is a Day, it will create a Datebox, or a combobox for enums etc.

If the field definition specifies a "dropdown" property, the according combobox will be created.



Suitable constraints are defined as far as they can be obtained from the field's metadata (for example uppercase or lowercase restrictions, the "no empty" constraint for required fields).



| Name | field28 |
| --- | --- |
| Inherits from | cell |
| Functionality | automatically selected InputElement (or Checkbox) |
| Required parent | row |
| Allowed children | none |
|   |   |
|   |   |



The field28 component is often used in conjunction with a label28 component, for example in crud screens:



**Example**

```
<row>
    <label28 id="dataSinkId"/>     <field28 id="dataSinkId" value="@bind(vm.data.dataSinkId)"/>
    <label28 id="outputEncoding"/> <field28 id="encoding"   value="@bind(vm.data.encoding)"/>
</row>
```

### 3.2.7    Cells28

The cells28 can be used as a shortcut to define labels and data fields in forms which have a single column.

The cells28 component creates a row with two cells, the first contains a label for the field, the second the field itself. That is, it combines the components label28 and cell28, and wraps them into a row.



| Name | cells28 |
| --- | --- |
| Inherits from | row |
| Functionality | Provides cells with label and a \*box component |
| Required parent | rows |
| Allowed children | none |



**Cells28 attributes**

| **Name** | **Meaning** |
| --- | --- |
| id | defines the path of the field in the view model |
| value | ZK data binding. Pure boilerplate, must be set to value="@bind(vm.data._id_)" Can hopefully omitted in future versions when a binder is created programmatically. |
| colspan1 | defines the number of multiple columns occupied by this field (integral parameter, used to achieve textual input fields of larger size) |
| rows1 | defines the number of multiple rows occupied by this field (integral parameter, used to achieve textual input fields of larger size) |
| readonly1 | (with optional boolean parameter): changes the field into a display-only field (for example for transient fields provided for information) |
| required1 | (boolean parameter). allows to override if the field allows blank input (null values). Usually this setting is derived from the **required/optional** field attribute in the DTO. |
| disabled1 | similar to readonly1, but can be updated dynamically modified. |
| enums1 | can be used to restrict enum values. See example in section "Reducing possible enum instances". |
| decimals1 | specifies the displayed number of decimal digits. This can be dynamic, based on currency. Please see the separate section "variable fractional digits " in chapter "Examples". |
| type1 | passed to ZK's Textbox.setType (allowed values: "password"). |
| group1 | (special function for dropdowns depending on a parameter) |



**General note:**

Any field with property "**notupdatable**" in the DTO will automatically be set to disabled in EDIT mode, but enabled in create mode. This also applies to the fields in the related controls **Cells228** and **Cells328**.



**Example**

(see modal28)







### 3.2.8    Cells228

Cells228 inherits from cells28 and adds a second field. That is, it combines 2 of the components label28 and cell28, and wraps them into a row.



| Name | cells228 |
| --- | --- |
| Inherits from | row / cells28 |
| Functionality | Provides cells with label and a \*box component |
| Required parent | rows |
| Allowed children | none |





**Cells228 additional attributes**

| **Name** | **Meaning** |
| --- | --- |
| id2 | defines the path of the second field in the view model |
| value2 | ZK data binding. Pure boilerplate, must be set to value2="@bind(vm.data._id_)" Can hopefully omitted in future versions when a binder is created programmatically. |
| colspan2, rows2, readonly2, enums2, disabled2, required2, decimals2, type2, group2 | (These have corresponding meaning to their counterparts with suffix 1 in the Cells28 component.) |



**Example**
```
<cells228 id="xmlDefaultNamespace"   value="@bind(vm.data.xmlDefaultNamespace)"
          id2="xmlRootElementName"   value2="@bind(vm.data.xmlRootElementName)"/>
```


### 3.2.9    Cells328

Cells228 inherits from cells228 and adds a third field. That is, it combines 3 of the components label28 and cell28, and wraps them into a row.



| Name | cells328 |
| --- | --- |
| Inherits from | row / cells28 / cells228 |
| Functionality | Provides cells with label and a \*box component |
| Required parent | rows |
| Allowed children | none |





**Cells328 additional attributes**

| **Name** | **Meaning** |
| --- | --- |
| id3 | defines the path of the second field in the view model |
| value3 | ZK data binding. Pure boilerplate, must be set to value2="@bind(vm.data._id_)" Can hopefully omitted in future versions when a binder is created programmatically. |
| colspan3, rows3, readonly3, enums3, disabled3, required3, decimals3, type3, group3 | (These have corresponding meaning to their counterparts with suffix 1 in the Cells28 component.) |



**Example**

```
<cells328 id="xmlDefaultNamespace"   value="@bind(vm.data.xmlDefaultNamespace)"
          id2="xmlRootElementName"   value2="@bind(vm.data.xmlRootElementName)"
          id3="xmladditionalStuff"   value3="@bind(vm.data.xmladditionalStuff)"/>
```


### 3.2.10 Cells8

The cells8 is a row component with an empty cell of a given height, to be used as padding in tabbed dialogs (the tab height is determined by the height of the first tab).



| Name | cells8 |
| --- | --- |
| Inherits from | row |
| Functionality | Provides a cell with default height. |
| Required parent | rows |
| Allowed children | none |





### 3.2.11 Form28

This component defines a grid which acts as a container for fields.



| Name | form28 |
| --- | --- |
| Inherits from | grid |
| Functionality | Provides information about the view model |
| Required parent | modal28 or tabpanel28 or crud28 |
| Allowed children | rows |



**Form28 attributes**

| **Name** | **Meaning** |
| --- | --- |
| aspect | the width ratio of label columns to data field columns |
| numColumns | Sets the number of parallel columns of label/entryfield pairs. Allowed values are 1, 2, 3. This should be in sync to the use of Cells28, Cells228 and Cells328 components to define the fields. |
| viewModelId | Defines the key by which the view model description can be found (these are defined in the API projects, in classes named \*Model. |



**Example**

(see modal28)









### 3.2.12 Filter28

The filters28 component creates a grid of filter fields. These are typically fed into a grid28 component to define the result set. The filter fields are defined by the grid definition (defaults are defined in the API projects in JSON files).



| Name | filter28 |
| --- | --- |
| Inherits from | grid |
| Functionality | A grid of filter selection inputs |
| Required parent |   |
| Allowed children | none |
|   |   |
|   |   |



The filter28 component needs a gridId assignment. If none is provided, it will try to get one from a parent component.



**Example (filter definition as part of grid definition, see previous chapter)**


It is possible to add custom filters. In order to use this, the filter definition needs to get an additional attribute "qualifier". If no standard filter type applies, the type "C" (custom) can be used, but the qualifier is valid for the standard filter types as well. The filter factory will then obtain an instance of a class implementing IFieldCustomFactory, with the qualifier (usually a @Singleton). This factory is then asked to provide a specific instance of the filter component.



**Example (custom filter)**
```
{
    "viewModel": "salesOrderSOLRxyz",

    "filters": [
        { "filterType": "W", "fieldName": "orderId" },
        { "filterType": "C", "fieldName": "shopRef", "qualifier": "myFilter1" }
    ],
    "fields": [
        "orderId", "orderDate", "entityRef.entityId", "carrierRef.carrierId"
    ]
}
```

```
@Singleton
@Named("myFilter1")
public class MyCustomFilter1Factory implements IFieldCustomFactory<Combobox> {

    @Override
    public IField<Combobox> createField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) throws Exception {
        return new MyCustomFilterImplementation(fieldname, cfg, desc, gridId, session);
    }
}
```



### 3.2.13 Groupbox28

A collapsible groupbox.



| Name | groupbox28 |
| --- | --- |
| Inherits from | groupbox |
| Functionality | An anchor for resizing |
| Required parent |   |
| Allowed children | any |







### 3.2.14 Grid28

The grid28 component provides the presentation of a result set.  It corresponds to the former "Listbox28", but only provides the search / display part, for better modularity. It adds the export button, which is essential for every grid. Therefore the same component can be used for CRUD screens main area and search screen detail areas.



| Name | grid28 |
| --- | --- |
| Inherits from | div |
| Functionality | provides search result display and export functionality |
| Required parent | any (typically groupbox28 or tabpanel28) |
| Allowed children | none |
|   |   |



The grid28 offers two slots for filters, filter1 and filter2. Both are combined by AND condition. Filter1 is fed by a filter28 component, filter2 by a selected row of the main grid, if this grid is part of a detail area.



The gridContext attribute can be used to define a context menu on the result rows. This will create an additional context28 component.

The numRows attribute can be used to specify the size of the component.



### 3.2.15 Twosections28

This is a composite component, it provides two components of type groupbox28, a filter28 component as child of the first and a grid28 as child of the second. The twosections28 wires the events such that a push on the search button triggers a search in the main grid.





| Name | twosections28 |
| --- | --- |
| Inherits from | vlayout |
| Functionality | provides combined filter input, search result display and export functionality |
| Required parent | window28 |
| Allowed children | none |
|   |   |



The gridContext attribute can be used to define a context menu on the result rows. This will create an additional context28 component.





### 3.2.16 Threesections28

This is a composite component, it provides three components of type groupbox28, a filter28 component as child of the first and a grid28 as child of the second. The child of the last one must be provided by the user.

threesections28 wires the events such that a push on the search button triggers a search in the main grid and a selection of a row in the main grid sends an event to the user content.





| Name | threesections28 |
| --- | --- |
| Inherits from | twosections28 |
| Functionality | provides combined filter input, search result display and export functionality, with a user section |
| Required parent | window28 |
| Allowed children | any (typically tabbox28 or crud28) |
|   |   |





### 3.2.17 Context28

This is a context menu which is provided in a very condensed form.

It accepts an attribute contextOptions, which is a string, listing IDs separated by comma. The component will create menuitems for every option, or a menuseparator if the option is the empty string. See a separate section for a more detailed discussion how this is used.

The ids of the menuitems are composed as the id of the context menu, plus ".", plus the option ID.



| Name | context28 |
| --- | --- |
| Inherits from | Menupopup |
| Functionality | provides a context menu |
| Required parent | any |
| Allowed children | none |
|   |   |



**Example**

```
<context28 id="myMenu" contextOptions="first,second,third,,more,evenMore"/>
```




### 3.2.18 Bandpopup28

This is a component which hosts the popup of a bandbox.



| Name | context28 |
| --- | --- |
| Inherits from | Bandpopup |
| Functionality | provides container for a bandbox popup |
| Required parent | Bandbox |
| Allowed children | filter28, grid28 |
|   |   |



### 3.2.19 Tabbox28

The tabbox28 is a container for tabpanels. It provides functionality to send "item selected" events to the currently shown tab panel. In case the tab is changed, the most recent event will also be sent to the newly selected tab. The tabbox28 also offers possibilities to define default conversions from selected rows to search filters, which is an advantage if multiple tabs take the same filter conversions.



| Name | tabbox28 |
| --- | --- |
| Inherits from | tabbox |
| Functionality | provides event forwarding |
| Required parent | any |
| Allowed children | tabpanels |
|   |   |



**tabbox attributes**

| **Name** | **Meaning** |
| --- | --- |
| filterFieldname | (If the selected data inherits from "Ref"): Defines a name which will be used as target column for a LongFilter with the selected object's objectRef field as the equality value. |
| filterName | Defines the qualifier by which a filter generator can be obtained via Jdp. |
| extension28 | Defines the qualifier for a customization (class which implements ITabboxExtension) |



**Example**
```
<window28 id="salesOrder">
    <threesections28 gridId="salesOrder.main" gridContext="fullCancel,overview ">
           <tabbox28 filterFieldname="salesOrderRef">
               <tabpanels>
                  <tabpanel28 id="orderItemTab">
                      <grid28 numRows="8" gridId="salesOrder.item" gridContext="gwc,partialCancel"/>
                  </tabpanel28>
                  <tabpanel28 id="historyTab">
                      <grid28 numRows="8" gridId="history"/>
                  </tabpanel28>
                  <tabpanel28 id="documentsTab">
                      <grid28 numRows="8" gridId="salesOrder.document"/>
                  </tabpanel28>
               </tabpanels>
           </tabbox28>
    </threesections28>
</window28>
```







### 3.2.20 Tabpanel28

The tabpanel28 is a container for detail sections. It accepts "item selected" events from the tabbox and converts the selected item into a filter for an embedded grid. If no filter converter is defined for the tabpanel, it uses a default one defined at tabbox level.

**A tabpanel28 defines its own Idspace!**



| Name | tabpanel28 |
| --- | --- |
| Inherits from | tabpanel |
| Functionality | converts selected items to search filters |
| Required parent | tabpanels |
| Allowed children | any |
|   |   |



**tabpanel attributes**

| **Name** | **Meaning** |
| --- | --- |
| filterFieldname | (If the selected data inherits from "Ref"): Defines a name which will be used as target column for a LongFilter with the selected object's objectRef field as the equality value. |
| filterName | Defines the qualifier by which a filter generator can be obtained via Jdp. |
| extension28 | Defines the qualifier for a customization (class which implements ITabpanelExtension) |



**Example**

(See tabbox28)



**Filters by name**

If a filter generator is defined by name, an implementation of interface IFilterGenerator for the given qualifier is obtained.



### 3.2.21 Togglefilter28

The togglefilter28 wraps filter28 if SOLR search is enabled. It offers a single textbox to enter the search criteria. By a toggle button it switches to the classical view.

The togglefilter28 issues onSearch events with String type event data.



| Name | togglefilter28 |
| --- | --- |
| Inherits from | tabbox |
| Functionality | wraps a filter28 and offers switching functionality |
| Required parent | groupbox28 (technical), twosection28 (logically) |
| Allowed children | none |
|   |   |





### 3.2.22 Crud28

The crud28 component sits in the detail section of the threesections28 screen and deals with the CRUD operations. It provides the required buttons.



| Name | crud28 |
| --- | --- |
| Inherits from | vlayout |
| Functionality | provides the standard CRUD buttons, evaluates permissions |
| Required parent | threesections28 |
| Allowed children | tabbox28, form28 |
|   |   |



The crud28 component references a view model, which is a subclass of AbstractCrudVM. There is one subclass per key type (for example CrudSurrogateKeyVM for DTOs which inherit from Ref), but it is possible to further subclass this one, which is required if the DTO contains optional arrays which must be initialized after load (ZK does not like references to arrays when the array itself is null.)



The crud28 components must define the screen's viewModel, provide the viewModelId (as defined in the API project) as parameter **vmId** to @init, and declare an artifical variable **currentMode**, which is needed for communication between view model and component (namely enabling / disabling the command buttons).



**crud28 attributes**

| **Name** | **Meaning** |
| --- | --- |
| cachesDropdown | If this CRUD screen modifies the values of a cached dropdown, the ID of that dropdown should be specified here. This will case the dropdown cache to be invalidated after record creation or deletion. |
| hide | With a parameter which is a comma separated list of the standard button names SAVE,NEW,COPY, DELETE,ACTIVATE,DEACTIVATE,  the specified buttons will not be shown on this screen. |


```
<crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.a28sku.viewmodel.SafetyStockViewModel',   vmId='safetyStockConfig')" currentMode="@load(vm.currentMode)">
```


### 3.2.23 View28

The view28 component offers a subset of the Crud28 functionality, namely only display of data.



| Name | view28 |
| --- | --- |
| Inherits from | div |
| Functionality | container to display data |
| Required parent | threesections28 or tabpanel28 (with postSelected="true") |
| Allowed children | any |
|   |   |



The view28 component references a view model, which is ViewOnlyVM or a subclass of it.



**Example:**

```
<tabpanel28 id="addressTab" postSelected="true">
    <view28 viewModel="@id('vm') @init('com.arvatosystems.t9t.viewmodel.SalesOrderAddressDisplayViewModel', vmId='salesOrder')">
        <hlayout>
            <textbox rows="13" cols="80" multiline="true" value="@load(vm.billToLines)"/>
            <textbox rows="13" cols="80" multiline="true" value="@load(vm.shipToLines)"/>
        </hlayout>
    </view28>
</tabpanel28>
```





### 3.2.24 Direct28

The direct28 component is a replacement of a tabbox28 with a single tabpanel28.



| Name | direct28 |
| --- | --- |
| Inherits from | div |
| Functionality | container to display data |
| Required parent | threesections28 |
| Allowed children | any |
|   |   |



The direct28 component forwards selectedItem events from the grid of the parent "threesections" component to a child grid. It offers the filterName parameter to select a IFilterGenerator by dependency injection, or alternatively names a column if the filter is a simple LongFilter with the upper grid's DTO's objectRef.



### 3.2.25 Button28

The button28 component provides a button with automatic label translation based on the button's ID. It extends the standard ZK Button component with permission checking and automatic focus management.



| Name | button28 |
| --- | --- |
| Inherits from | button |
| Functionality | button with translated label and permission support |
| Required parent | any |
| Allowed children | none |



**Button28 features**

* Automatic label translation using the button's ID
* Automatic blur after click to prevent focus issues
* Permission-based visibility control via resourceId
* Can be used with image icons (using setImage())
* Supports autoblur parameter to control focus behavior



**Example**

```
<button28 id="exportButton"/>
```



### 3.2.26 Tab28

The tab28 component provides a tab with automatic label translation. It must be used as a child of a component which defines a view model, in order to resolve the translation key.



| Name | tab28 |
| --- | --- |
| Inherits from | tab |
| Functionality | tab with automatic label translation |
| Required parent | tabs (must be under a component with viewModelId) |
| Allowed children | none |



**Example**

```
<tabbox id="taboxDetail">
  <tabs>
    <tab28 id="mainTab"/>
    <tab28 id="detailTab"/>
  </tabs>
  <tabpanels>
    ...
  </tabpanels>
</tabbox>
```



### 3.2.27 Div28

The div28 component is a container component that extends the standard ZK Div. It provides permission management and grid ID propagation features.



| Name | div28 |
| --- | --- |
| Inherits from | div |
| Functionality | container with permission support |
| Required parent | any |
| Allowed children | any |



The div28 component is typically used as a container when permission checks are needed. It implements IPermissionOwner to support permission-based visibility.



### 3.2.28 ViewModel28

The ViewModel28 component is a lightweight container that provides view model context without CRUD functionality. It is a simpler alternative to Form28 when only view model access is needed.



| Name | viewModel28 |
| --- | --- |
| Inherits from | div |
| Functionality | provides view model context for child components |
| Required parent | any |
| Allowed children | any |



**ViewModel28 attributes**

| **Name** | **Meaning** |
| --- | --- |
| viewModelId | Defines the key by which the view model description can be found (defined in the API projects) |



**Example**

```
<viewModel28 viewModelId="myViewModel">
    <!-- child components can access the view model -->
</viewModel28>
```



### 3.2.29 Permissions28

The permissions28 component provides a visual grid for editing permission sets. It displays checkboxes for various operation types (EXECUTE, CREATE, READ, UPDATE, DELETE, etc.) and allows the user to toggle permissions.



| Name | permissions28 |
| --- | --- |
| Inherits from | groupbox |
| Functionality | permission editing grid with checkboxes |
| Required parent | any |
| Allowed children | none (internally generates grid structure) |



The permissions28 component supports all standard operation types defined in OperationType, including EXECUTE, CREATE, READ, UPDATE, DELETE, SEARCH, LOOKUP, INACTIVATE, ACTIVATE, VERIFY, MERGE, PATCH, EXPORT, IMPORT, CONFIGURE, CONTEXT, ADMIN, APPROVE, REJECT, and CUSTOM.

The component also provides convenience buttons for "Select All", "Clear All", and common permission combinations.



### 3.2.30 MultilineJson

The multilineJson component provides a multi-line text input field with JSON validation. It extends the standard ZK Textbox and automatically validates that the entered text is valid JSON.



| Name | multilineJson |
| --- | --- |
| Inherits from | textbox |
| Functionality | multi-line text input with JSON validation |
| Required parent | any |
| Allowed children | none |



**MultilineJson attributes**

| **Name** | **Meaning** |
| --- | --- |
| nullAllowed | (boolean parameter): allows null/empty values. Default is true. |



The component will display an error message if invalid JSON is entered. It is commonly used for configuration fields that accept JSON data.



**Example**

```
<multilineJson value="@bind(vm.data.jsonConfig)" nullAllowed="false"/>
```



### 3.2.31 CkEditor28

The ckEditor28 component provides a rich text editor based on CKEditor. It extends the standard CKEditor ZK component and integrates it with the t9t view model system.



| Name | ckEditor28 |
| --- | --- |
| Inherits from | CKeditor (from org.zkforge.ckez) |
| Functionality | rich text editor with view model integration |
| Required parent | any (typically within a form28 or cells28) |
| Allowed children | none |



The ckEditor28 component provides full rich text editing capabilities including text formatting, images, tables, and other HTML content. It can be bound to string fields in the view model to store HTML content.



**Example**

```
<cells28 id="description" value="@bind(vm.data.description)"/>
```

If the field metadata indicates it should use a rich text editor, a ckEditor28 will be automatically created by the field28 component.











## 3.3      Reducing possible enum instances

Sometimes it is required to reduce enums to allow only a certain subset of instances. This can either be screen specific (data exports may not support media types such as WAV or MP3), or tenant specific (some tenants do not want to use certain return or GWC reason codes).

The following cases are supported:

* reducing possible instances of an enum throughout the whole application, by tenant: Define a "translation" of the following form:
```
(tenantId).(enumPQON).$enums=(comma separated list of allowed enum instance names)
```
for example
```
ACME.t9t.base.FunnyNumberEnum.$enums=TWO,FOUR,SIX
```

* reducing possible instances related to a DTO: This is defined in the bon file, by setting a property named "enums". A direct setting as well as a tenant specific setting is supported:

```
    required enum FunnyNumberEnum   myNumber   properties enums="TWO,FOUR,SIX";
```
restricts the instances only for UI components based on this DTO, use of the same enum in other DTOs will allow all instances.

```
    required enum FunnyNumberEnum   myNumber   properties enums="#numberKey";
```
will look up the effective list via translation
```
ACME. numberKey.$enums=TWO,FOUR,SIX
```

The second option allows to use multiple different restrictions for the the tenant for different DTOs. Use this to model restricted filters in search screens.

* reducing possible instances related to a zul file: This is implemented by setting properties enums1 or enums2 as parameter to the components cell28 / cells228, with the same syntax as in the bon file. This is usable if the restriction should be used for the form portion / details section of a CRUD screen or in a popup.

If multiple types of restriction apply for the same field, the intersection is used. This means, the possible instance names for an enum in a popup will be the intersection of
* tenant specific global settings
* restrictions defined in the DTO in the bon file
* restrictions defined in the zul file









**Example:**



zul file:
```
<modal28 id="createGwcWin" viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.GenericVM', inst=inst)">
    <form28 id="createGwc" viewModelId="createGwc" aspect="2">
        <rows>
            <cells28 id="amountGWC"             value="@bind(vm.data.amountGWC)"/>
            <cells28 id="refundPayIinstrument"  value="@bind(vm.data.refundPayIinstrument)" enums1="#gwcRefundPayInstrument"/>
            <cells28 id="reason"                value="@bind(vm.data.reason)"               enums1="#gwcRefundReason"/>
            <cells28 id="comment"               value="@bind(vm.data.comment)"/>
        </rows>
    </form28>
</modal28>
```


Translation properties file for "en":
```
ACME.gwcRefundPayInstrument.$enums=ORIGINAL
ACME.gwcRefundReason.$enums=DEFECTIVE,PRICE_REDUCTION,ITEM_MISSING,SHIPPING_COST_REFUND
```
