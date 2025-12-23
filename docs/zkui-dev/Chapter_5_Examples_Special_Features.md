This chapter provides examples for typical use cases.


## 1.1      Context menu – Document download

For typical download activities, the isEnabled check and the activity itself are implemented generically in a superclass and the document specific part only has to retrieve the sinkRef from the DTO.


```
@Singleton
@Named("deliveryOrder.main.ctx.downloadInvoice")
public class DownloadDeliveryOrderInvoiceContextHandler extends AbstractDownloadContextHandler<DeliveryOrderDTO> {

    @Override
    protected Long getSinkRef(DeliveryOrderDTO dto) {
        if (dto.getInvoiceDocRef() != null) {
            SinkRef ref = ((CustomerCommDTO)dto.getInvoiceDocRef()).getCommDocSinkRef();
            if (ref != null)
                return ref.getObjectRef();
        }
        return null;
    }
}
```

## 1.2      Context menu – Data update

In this example, the zul file (form28) directly operates on the Request object as defined in the API.



```
@Singleton
@Named("salesOrder.payInstrument.ctx.updatePayment")
public class SalesOrderPaymentUpdate implements IGridContextMenu<PayInstrumentDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesOrderPaymentUpdate.class);
    protected final T9TRemoteUtils t9tremoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    @Override
    public boolean isEnabled(DataWithTrackingW<PayInstrumentDTO, TrackingBase> dwt) {
        PayInstrumentDTO data = dwt.getData();
        if (Boolean.TRUE == data.getPaid())
            return false;
        if (data.getPaymentMethod() == PaymentMethodCodeEnum.TERMS || data.getPaymentMethod() == PaymentMethodCodeEnum.DIRECT_DEBIT)
            return true;   // TODO: check if order not yet complete
        return false;
    }

    @Override
    public void selected(Listbox lb, Listitem li, DataWithTrackingW<PayInstrumentDTO, TrackingBase> dwt) {
        // invoke a modal popup which allows editing of payment data
        PayInstrumentDTO data = dwt.getData();
        UpdatePayInstrumentRequest paymentUpdateRequest = new UpdatePayInstrumentRequest();
        paymentUpdateRequest.setPayInstrumentRef(new PayInstrumentRef(data.getObjectRef()));
        paymentUpdateRequest.setAccountNumber(data.getAccountNumber());
        paymentUpdateRequest.setBic(data.getBic());
        paymentUpdateRequest.setIban(data.getIban());
        paymentUpdateRequest.setCardholderName(data.getCardholderName());
        paymentUpdateRequest.setInstitutionName(data.getInstitutionName());
        // paymentUpdateRequest.setComments(data.getComments());  // initially empty, is for history

        ModalWindows.runModal("/context/editPayInstrument.zul", lb.getParent(), paymentUpdateRequest, false, (d) -> {
            LOGGER.debug("Updating payment instruments to {}", d);
            try {
                t9tremoteUtils.executeAndHandle(d, ServiceResponse.class);
                // TODO: if it worked, update the model...

            } catch (ReturnCodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }
}
```



## 1.3      Crud Screen

The following example shows a complete crud screen, which has 2 specialties:

* use of a customized view model class, in order to support array initalization

* use of array fields in the zul file (Note: pending issue with UI updates, ZK bug? Use currently not recommended)


```
<?xml version="1.0" encoding="UTF-8"?>
<?init class="com.arvatosystems.t9t.zkui.init.WorkbenchInit" pagename="safetyStockConfig28"?>

<window28 id="safetyStockConfigWin">
  <threesections28 gridId="safetyStockConfig.main" >
    <crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.a28sku.viewmodel.SafetyStockViewModel', vmId='safetyStockConfig')" currentMode="@load(vm.currentMode)">
      <form28 id="safetyStockConfigCrud" aspect="2">
        <rows>
          <cells28 id="levelType"           value="@bind(vm.data.levelType)"/>
          <cells28 id="levelValue"          value="@bind(vm.data.levelValue)"/>
          <cells28 id="safetyStockValue[0]" value="@bind(vm.data.safetyStockValue[0])"/>
          <cells28 id="safetyStockValue[1]" value="@bind(vm.data.safetyStockValue[1])"/>
          <cells28 id="safetyStockValue[2]" value="@bind(vm.data.safetyStockValue[2])"/>
          <cells28 id="isActive"            value="@bind(vm.data.isActive)"/>
        </rows>
      </form28>
    </crud28>
  </threesections28>
</window28>
```



The view model is as follows:

```
package com.arvatosystems.t9t.zkui.viewmodel;

import org.zkoss.bind.annotation.Init;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;

@Init(superclass=true)
public class CrudSurrogateKeyVM<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase>
  extends AbstractCrudVM<Long, DTO, TRACKING, CrudSurrogateKeyRequest<REF, DTO, TRACKING>, CrudAnyKeyResponse<DTO,TRACKING>> {
    @Override
    protected CrudSurrogateKeyRequest<REF, DTO, TRACKING> createCrudWithKey() {
        CrudSurrogateKeyRequest<REF, DTO, TRACKING> crudRq
           = (CrudSurrogateKeyRequest<REF, DTO, TRACKING>) crudViewModel.crudClass.newInstance();
        crudRq.setKey(data.getObjectRef());
        return crudRq;
    }

    @Override
    protected void clearKey() {
        data.setObjectRef(0L);
    }
}
```



An important aspect of the specialized view model class is that it needs the annotation

`@Init(superclass=true)`





## 1.4      Crud Screen (Multi-Tab, Multi-Column)

The same approach works if the detail section of the crud screen is a tabbox with panels. No further specific components are required, it works by using the standard ZK components.

If the detail section contains 2 columns of data fields, the component cells228 is used instead of cells28, and the  form28 component takes the parameter numColumns="2". An alternative to the use of cells228 is the explicit use of multiple label28 and field28 components. Both approaches are shown in the below example:




```
<?xml version="1.0" encoding="UTF-8"?>
<?init class="com.arvatosystems.t9t.zkui.init.WorkbenchInit" pagename="dataSink28"?>

<window28 id="dataSinkWin">
  <threesections28 gridId="dataSinkConfig">
    <crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM', vmId='dataSinkConfig')"
         currentMode="@load(vm.currentMode)">
      <tabbox id="taboxDetail" vflex="1">
       <tabs>
         <tab label="dataSink.main"/>
         <tab label="dataSink.xml" />
         <tab label="dataSink.test" />
       </tabs>
       <tabpanels vflex="1" id="tabpanelsDetail">
         <tabpanel vflex="1" id="tabpanel1">
           <form28 id="dataSinkConfigCrud" aspect="2" numColumns="2">
             <rows>
              <!-- compact form
             <cells228 id="env"       value="@bind(vm.data.env)"
                       id2="isActive" value2="@bind(vm.data.isActive)"/> -->
              <!-- explicit form -->
             <row>
               <label28 id="env"/>      <field28 id="env"      value="@bind(vm.data.env)"/>
               <label28 id="isActive"/> <field28 id="isActive" value="@bind(vm.data.isActive)"/>
             </row>
             <!—more rows here... -->
             </rows>
           </form28>
         </tabpanel>
         <tabpanel vflex="1" id="tabpanel2">
             <!—more fields here... -->
         </tabpanel>
       </tabpanels>
      </tabbox>
    </crud28>
  </threesections28>
</window28>
```





## 1.5      Hiding Buttons in a Crud Screen

Sometimes a crud screen should not allow a specific function, for example never to create new records, regardless of user's permissions.

In this case, you can provide the "hide" parameter to the crud28 component:



```
<crud28 viewModel="@id('vm')
     @init('com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM', vmId='dataSinkConfig')"
         currentMode="@load(vm.currentMode)" hide="new,copy">
```

The argument is a comma separate list of the values NEW, COPY, SAVE, DELETE, ACTIVATE, DEACTIVATE. Ordering is not relevant. The argument is case insensitive.

If the DTO (or one of its superclasses) does not contain a column named isActive, then the ACTIVATE and DEACTIVATE buttons are hidden automatically.






## 1.6      Different ratios for height in "threesections28"

If you do a CRUD screen or other overview / detail screen and want to modify the ratio of the middle section height to the lower section height, you can do so by setting vflex2 and vflex3 parameters. The arguments to those define the relative size of those two sections, for example 2 to 5:



```
<window28 id="dataSinkWin">
    <threesections28 gridId="dataSinkConfig" gridContext="showSinks" vflex2="2" vflex3="5">
        <crud28 viewModel="@id('vm') @init('com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM',
            vmId='dataSinkConfig' ....
```


will result in a lower section 2.5 times the height of the overview section:



## 1.7      Dropdowns feeding String and Long fields

Dropdown components which are DB based (i.e. userId, locationId etc...) can be used very flexible. The default use is to populate a Ref field (object reference), but they can be applied to String type fields (populating the ID) or Long type fields as well (populating the objectRef).



Ref based (typical use):

    `optional (OrgUnitRef...,OrgUnitKey)      entityRef   properties ref, dropdown="orgUnitId";`



String based:

   ` optional Unicode(16)                     entityId    properties dropdown="orgUnitId";`



Long based:

    `optional Long                            entityRef   properties dropdown="orgUnitId";`



This works in the filter area as well as detail section. Field of type Long usually look cryptic in the result overview grid (because the objectRef is usually not known to the user).

## 1.8      Bandboxes

Bandboxes (pop up windows which allow to select data similar to dropdowns, but designed for larger data sets which cannot be cached completely) exist for the following object types currently:

* products

* SKUs

* customers



A bandbox is automatically selected when a filter or data field references a child object, and no "dropdown" property is provided.

If the data field is of type "Long" (f42Ref), a property "bandbox" must be explicitly provided in the DTO definition of the bon file.



**Examples:**


```
    class SkuDTO extends SkuRef {
        required boolean                                isActive            properties active;
        required (ProductRef..., ProductDTO...)         productRef          properties notupdatable, ref, searchprefix="product";  // automatically refers to a bandbox
        required catalogId                              catalogId           properties notupdatable;
```






```
    class SetItemDTO RTTI 2211 extends CompositeKeyBase pk SetItemKey {
        optional f42Ref                                 skuRef          properties notupdatable, ref, searchprefix="sku", bandbox="skuBandbox", noupdate, notNull;        // needs explicit "bandbox" property
        required Integer                                sortOrder       properties notupdatable;
        required boolean                                isActive        properties active;
```





## 1.9      Extra Filter Parameters

If a search should always have filter parameters which are not selected by the user in the UI, those can be provided via property to the "twosections" component, either via

`fixedfilter="qualifier"`

which provides a Jdp qualifier of a singleton class implementing interface IFixedFilter.

or via

`additionalFilter="VM reference"`

which provides a SearchFilter object provides by the ViewModel.



**Examples**:



```
<modal28 id="showAvailabilityWin" viewModel="@id('vm')
    @init('com.arvatosystems.t9t.zkui.viewmodel.GenericVM', inst=inst, vmId='stockPerLocationVM')">
    <twosections28 gridId="stockPerLocation" vflex="1" listHeaders="@load(vm.data.skuIds)" additionalFilter="@load(vm.data.searchFilter)"/>
</modal28>
```




```
<?xml version="1.0" encoding="UTF-8"?>
<?init class="com.arvatosystems.t9t.zkui.init.WorkbenchInit" pagename="csaGwcSpend"?>

<window28 id="csaGwcSpendCfg">
    <threesections28 id="threesection" gridId="csaGwcSpendCfg" fixedFilter="newYear2000">
        <crud28>
...
```

## 1.10   Dropdowns with extra filter parameters

The "fixedFilter" parameter is also possible for database based dropdowns, such as LocationDropdown. This can select a subset of the result of the dropdown without the filter:

```
    <groupbox28 id="shipToLocation" visible="false">
        <dropdown28Location value='@load(item.name)' fixedFilter="retailStoreLocationId"
                selectedItem="@bind(vm.shipToLocationRef) @converter('com.arvatosystems.fortytwo.web.DropdownConverter')">
        </dropdown28Location>
    </groupbox28>
```



The implementation then provides the filter:

```
@Named("retailStoreLocationId")
@Singleton
public class RetailStoreLocationFilter implements IFixedFilter {

    @Override
    public SearchFilter get() {
        EnumFilter ef = new EnumFilter(LocationDTO.meta$$locationType.getName(), LocationTypeEnum.RETAIL_STORE.ret$PQON());
        ef.setEqualsName(LocationTypeEnum.RETAIL_STORE.name());
        return ef;
    }
}
```



In addition, the dropdowns can also apply filters to the result set, using the parameter "descriptionFilter".

Selected implementations must then implement

```
public interface IDescriptionFilter {
    List<Description> filter(List<Description> input);
}
```





## 1.11   Variable fractional digits

By default, for fields of type "decimal", the UI displays the number of fractional digits as set in the bon file. For some fields, the number of decimals depends on the context. In case of monetary totals for example, the currency determines the number of fractional digits, while the fields often offer 6 decimal digits. Currencies like EUR or USD have 2 decimal digits, TND and other Dinars 3, and some (like Japanese Yen) have no fractional part at all. For Bitcoin, 6 might be used.

In order to implement this, the currency must be available in the viewmodel (read only is sufficient, also the currency is not required to be described by a bon file). Then the currency field must be referenced in the zul using the decimals1/2 property in the field28, cells28 or cells228 components.

Example:



  `<cells28 id="myAmount" value="@bind(vm.data.myAmount)" decimals1="@load(vm.currency)"/>`



Of course the currency can also be provided directly:

  `<cells28 id="myAmount" value="@bind(vm.data.myAmount)" decimals1="USD"/>`



and in the general case, also Strings with numeric values are supported:

  `<cells28 id="myAmount" value="@bind(vm.data.myAmount)" decimals1="5"/>`

or

  `<cells28 id="myAmount" value="@bind(vm.data.myAmount)" decimals1="@load(vm.numDigits)"/>`









## 1.12   Custom Filter - Complete Example

The custom filter can be applied to a variety of cases – one example given here is to apply multiple different and absolutely independent predefined filters based on selection of a dropdown.

For this purpose, in the API projects, an enum has to be created which enumerates the different filter options. In addition, a dummy DTO is required which references this enum, in order to get a populated instanced of the EnumDescription class.



As outlined in the section on "Filter28", create the filter field in the JSON file in the API project:



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



In the UI section, the custom filter factory defines a local class which extends EnumBaseField then:


```
@Singleton
@Named("myFilter1")
public class MyCustomFilter1Factory implements IFieldCustomFactory<Combobox> {
    @Override
    public IField<Combobox> createField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId,
        ApplicationSession session) throws Exception {
        return new MyCustomFilterImplementation(fieldname, cfg, desc, gridId, session);
    }


    static class AllocatedOrNotField extends EnumBaseField {
        protected final EnumDefinition ed = (dto-name).meta$$(fieldname);  // reference to EnumDescription object

       @Override
       public SearchFilter getSearchFilter() {
           Comboitem ci = cb.getSelectedItem();
           if (ci == null || empty() || ci.getValue() == null)
               return null;
           switch (ci.getValue()) {
             // TODO: return various filters as required
           }
           return f;
       }

       public AllocatedOrNotField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
          super(fieldname, cfg, desc, gridId, session, (name of enum));
         createComp(ed, session);
       }
    }
}
```











## 1.13   Overriding "required" setting of DTO

For some screens, the required / optional specifier of the bon file may not be appropriate. This is the case when some post processing is applied in the view model (then usually defaults are provided for otherwise required fields). For the cells28 and cells228 components, the setting for the first and / or second data field can be overriden via required1 / required2:


```
<cells228
   id ="emailAddress.emailAddress" value ="@bind(vm.data.emailAddress.emailAddress)" required1="false"
   id2="phoneNumber.phoneNumber"   value2="@bind(vm.data.phoneNumber.phoneNumber)"   required2="false"
/>
```



## 1.14   Dropdowns for Backend Implementations

In some technical screens, it is desired to select a specific implementation of a Java interface. If these have been implemented as @Dependent class or  @Singleton, with a @Named annotation, then the list of available qualifiers can be shown in the dropdown. In order to achieve this, the field must be a text field, and the property "**qualifierFor**" must specify the PQON of the interface. It is also possible to specify multiple alternative interfaces.



**Example:**

Please see the definition of DataSinkDTO in project t9t-io-api:



```
    optional customizationQualifier  preTransformerName      properties qualifierFor="out.services.IPreOutputDataTransformer,in.services.IInputDataTransformer"
```
or
```
    optional customizationQualifier  commFormatName          properties qualifierFor="out.services.ICommunicationFormatGenerator,in.services.IInputFormatConverter"
```


## 1.15   Displaying a "today" button in date Pickers

There is an optional button in the ZK date picker, which allows to jump to the current date. To activate this button for a single field within a crud28 / form28 environment, provide the property

`properties showToday="y"`

in the bon file for the specific field.

If the datebox is "manually" coded, use the standard ZK properties of a datebox to activate it:

`showTodayLink="true" todayLinkLabel="${l:translate('datePicker','todayLabel')}`



To add this button to all date pickers, set the configuration value

datePicker.showToday
