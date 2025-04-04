/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.zkui.components.datafields;

import java.util.Collections;
import java.util.Map;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.types.TenantIsolationCategoryType;
import com.arvatosystems.t9t.zkui.components.IDataFieldFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28BasicFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IGroupedDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28Ext;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28Registry;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DataFieldFactory implements IDataFieldFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFieldFactory.class);

    // ZK edition specific data fields
    // ZK standard edition implementation
    protected IDataField createEnumsetNumDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new EnumsetNumIntboxDataField(params, enumDtoRestrictions);
    }

    protected IDataField createEnumsetAlphaDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new EnumsetAlphaTextboxDataField(params, enumDtoRestrictions);
    }

    protected IDataField createXenumsetDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new XEnumsetTextboxDataField(params, enumDtoRestrictions);
    }

    @Nonnull
    protected IDataField createMultiStringDropdownDataField(@Nonnull final DataFieldParameters params) {
        return new TextDataField(params);
    }

    @Override
    public IDataField createField(final DataFieldParameters params, final CrudViewModel<BonaPortable, TrackingBase> crudViewModel) {
        try {
            final String path = params.path;
            LOGGER.debug("creating dynamic field for field name {}", path);
            final FieldDefinition columnDescriptor = params.cfg;
            final Map<String, String> fieldProperties = columnDescriptor.getProperties() != null ? columnDescriptor.getProperties() : Collections.emptyMap();
            final String dropdownType = fieldProperties.get(Constants.UiFieldProperties.DROPDOWN);
            final String enumDtoRestrictions = fieldProperties.get(Constants.UiFieldProperties.ENUMS);
            final String javaType = columnDescriptor.getDataType().toLowerCase();
            final String bonaparteType = columnDescriptor.getBonaparteType().toLowerCase();
            // Boolean isSerialized= null != fieldProperties ? fieldProperties.containsKey("serialized") : false;
            switch (columnDescriptor.getDataCategory()) {
            case STRING:
                if (dropdownType != null) {
                    final IDropdown28BasicFactory<Dropdown28Ext> factory = Dropdown28Registry.getFactoryById(dropdownType);
                    if (factory == null) {
                        LOGGER.warn("API specified a dropdown of type {} for {}, but it does not exist", dropdownType, path);
                        throw new RuntimeException("unknown dropdown " + dropdownType);
                    }
                    if (factory instanceof IDropdown28DbFactory) {
                        // DB based
                        final IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
                        return new DropdownDbAsStringDataField(params, dropdownType, dbFactory);
                    }
                    // String based (Currency, Country etc...)
                    return new DropdownBasicDataField(params, dropdownType, factory);
                }
                final String qualifierFor = fieldProperties.get(Constants.UiFieldProperties.QUALIFIER_FOR);
                if (qualifierFor != null) {
                    LOGGER.debug("Creating dropdown for qualifier {} for {}", qualifierFor, path);
                    return new DropdownComboBoxItemDataField(params, qualifierFor);
                }
                if (path.endsWith("tenantId") && !fieldProperties.containsKey("nodropdown"))
                    return new TenantDataField(params, TenantIsolationCategoryType.factory(crudViewModel.dtoClass.getProperty("tenantCategory")));
                if (fieldProperties.containsKey(Constants.UiFieldProperties.MULTI_DROPDOWN)) {
                    return createMultiStringDropdownDataField(params);
                }
                return new TextDataField(params);
            case BASICNUMERIC:
                switch (javaType) {
                case "double":
                    return new DoubleDataField(params);
                case "float":
                    return new FloatDataField(params);
                case "int":
                case "integer":
                    if (dropdownType != null) {
                        IDropdown28BasicFactory<Dropdown28Ext> factory = Dropdown28Registry.getFactoryById(dropdownType);
                        if (factory == null) {
                            LOGGER.warn("API specified a dropdown of type {} for {}, but it does not exist", dropdownType, path);
                            throw new RuntimeException("unknown dropdown " + dropdownType);
                        }
                        if (factory instanceof IDropdown28DbFactory) {
                            // DB based
                            final IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
                            return new DropdownDbAsIntegerDataField(params, dropdownType, dbFactory);
                        }
                        // none supported yet
                        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "No known non-DB dropdowns of integer value");
                    }
                    return new IntDataField(params);
                case "long":
                    if (dropdownType != null) {
                        final IDropdown28BasicFactory<Combobox> factory = Dropdown28Registry.getFactoryById(dropdownType);
                        if (factory == null) {
                            LOGGER.warn("API specified a dropdown of type {} for {}, but it does not exist", dropdownType, path);
                            throw new RuntimeException("unknown dropdown " + dropdownType);
                        }
                        IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
                        return new DropdownDbAsLongDataField(params, dropdownType, dbFactory);
                    }
                    // check for bandboxes
                    final String bandbox = fieldProperties.get(Constants.UiFieldProperties.BANDBOX);
                    if (bandbox != null) {
                        // create bandbox object via specific class
                        ILongBandboxFactory bbFactory = Jdp.getOptional(ILongBandboxFactory.class, bandbox);
                        if (bbFactory != null)
                            return bbFactory.createBandbox(params);
                    }
                    return new LongDataField(params);
                case "biginteger":
                    return new NumberDataField(params);
                case "microunits":
                    return new MicroUnitsDataField(params);
                case "milliunits":
                    return new MilliUnitsDataField(params);
                case "nanounits":
                    return new NanoUnitsDataField(params);
                }
                break;
            case NUMERIC:
                return new DecimalDataField(params);
            case BINARY:
                break;
            case ENUM:
                return new EnumDataField(params, enumDtoRestrictions);
            case ENUMALPHA:
                return new EnumAlphaDataField(params, enumDtoRestrictions);
            case ENUMSET:
                final NumericEnumSetDataItem en = (NumericEnumSetDataItem)columnDescriptor;
                if (en.getBaseEnumset().getName().equals("api.auth.Permissionset")) {
                    LOGGER.debug("detected special Permissionset type for field {}", path);
                    return new PermissionsetDataField(params);
                }
                return createEnumsetNumDataField(params, enumDtoRestrictions);  // ZK edition specific widget
            case ENUMSETALPHA:
                return createEnumsetAlphaDataField(params, enumDtoRestrictions);  // ZK edition specific widget
            case MISC:
                switch (javaType) {
                case "boolean":
                    final boolean tristate = fieldProperties.get(Constants.UiFieldProperties.TRISTATE) != null;
                    return tristate ? new BooleanTristateDataField(params) : new BooleanDataField(params);
                case "uuid":
                    return new UUIDDataField(params);
                }
                break;
            case OBJECT:
                if (dropdownType != null) {
                    final IDropdown28BasicFactory<Combobox> factory = Dropdown28Registry.getFactoryById(dropdownType);
                    if (factory == null) {
                        LOGGER.warn("API specified a dropdown of type {} for {}, but it does not exist", dropdownType, path);
                        throw new RuntimeException("unknown dropdown " + dropdownType);
                    }
                    if (factory instanceof IDropdown28DbFactory) {
                        IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
                        return new DropdownDataField(params, dropdownType, dbFactory);
                    } else if (factory instanceof IGroupedDropdown28DbFactory) {
                        IGroupedDropdown28DbFactory dbFactory = (IGroupedDropdown28DbFactory) factory;
                        return new GroupedDropdownDataField(params, dropdownType, dbFactory);
                    } else {
                        LOGGER.error("Unsupported type of IDropdown28BasicFactory, implementation is needed in DataFieldLFactory");
                    }
                }
                // check for bandboxes
                final ObjectReference objRef = (ObjectReference)columnDescriptor;
                String bandbox = fieldProperties.get(Constants.UiFieldProperties.BANDBOX);
                if (bandbox == null && objRef.getLowerBound() != null)
                    bandbox = objRef.getLowerBound().getName();  // use the ref's PQON
                if (bandbox != null) {
                    // create bandbox object via specific class
                    IBandboxFactory bbFactory = Jdp.getOptional(IBandboxFactory.class, bandbox);
                    if (bbFactory != null)
                        return bbFactory.createBandbox(params);
                }
                switch (bonaparteType) {
                case "array":
                    return new ArrayDataField(params);
                case "json":
                    return new JsonDataField(params);
                case "element":
                    return new ElementDataField(params);
                default:  // ref, object (both are subclasses of BonaPortable)
                    return new SerializedObjectDataField(params, bonaparteType);
                }
            case TEMPORAL:
                switch (bonaparteType) {
                case "day":
                    final boolean withToday = ZulUtils.readBooleanConfig(T9tConfigConstants.DATE_PICKER_SHOW_TODAY)
                      || (fieldProperties.get(Constants.UiFieldProperties.SHOW_TODAY) != null);
                    return new DayDataField(params, withToday);
                case "time":
                    return new TimeDataField(params);
                case "timestamp":
                    return new TimestampDataField(params);
                case "instant":
                    return new InstantDataField(params);
                }
                break;
            case XENUM:
                return new XenumDataField(params, enumDtoRestrictions);
            case XENUMSET:
                return createXenumsetDataField(params, enumDtoRestrictions);  // ZK edition specific widget
            default:
                break;
            }
            LOGGER.error(
                    "No matches found for {} in {}. Possibly something misconfigured in the grid configuration? (category = {}, java type = {}, "
                    + "bonaparte type = {})",
                    path, crudViewModel.dtoClass.getBonaPortableClass().getCanonicalName(), columnDescriptor.getDataCategory(), javaType, bonaparteType);
        } catch (final Exception e) {
            LOGGER.error("Problems in the grid configuration for field {}? {}", params.path, e);
        }

        return null;
    }
}
