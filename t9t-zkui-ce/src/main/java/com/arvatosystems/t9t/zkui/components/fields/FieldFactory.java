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
package com.arvatosystems.t9t.zkui.components.fields;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.types.TenantIsolationCategoryType;
import com.arvatosystems.t9t.init.InitContainers;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.DataCategory;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.dp.Jdp;

/** Creates ZK components for filters. */
public class FieldFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldFactory.class);
    private final CrudViewModel<?, ?> viewModel;
    private final ApplicationSession session;
    private final String gridId;

    public FieldFactory(final CrudViewModel<?, ?> viewModel, final String gridId, final ApplicationSession session)  {
        this.viewModel = viewModel;
        this.gridId = gridId;
        this.session = session;
    }

    public IField createField(final String fieldname, final UIFilter filter, final FieldDefinition desc) {
        try {
            final String fieldName = filter.getFieldName();
            LOGGER.debug("creating dynamic field for field name {}", fieldName);

            if (filter.getFilterType() == UIFilterType.NULL) {
                // data category / type do not matter: It's always a checkbox
                return new NullFilterField(fieldname, filter, desc, gridId, session);
            }

            final DataCategory dataCategory = desc.getDataCategory();
            final String dataType = desc.getDataType().toLowerCase();  // the java type
            final String bonaparteType = desc.getBonaparteType().toLowerCase();  // the type as described in the bon file
            final Map<String, String> fieldProperties = desc.getProperties();
            final String dropdownType = fieldProperties != null ? fieldProperties.get(Constants.UiFieldProperties.DROPDOWN) : null;
            final String javaType = dataType.toLowerCase();

            if (filter.getQualifier() != null) {
                // use custom
                IFieldCustomFactory factory = Jdp.getRequired(IFieldCustomFactory.class, filter.getQualifier());
                return factory.createField(fieldName, filter, desc, gridId, session);
            }
            switch (dataCategory) {
            case BASICNUMERIC:
                switch (javaType) {
                case "int":
                case "integer":
                    if (dropdownType != null) {
                        return new DropdownField(fieldname, filter, desc, gridId, session, dropdownType);
                    }
                    return new IntField(fieldname, filter, desc, gridId, session);
                case "long":
                    if (dropdownType != null) {
                        return new DropdownField(fieldname, filter, desc, gridId, session, dropdownType);
                    }
                    // check for bandboxes
                    final String bandbox = null != fieldProperties ? fieldProperties.get(Constants.UiFieldProperties.BANDBOX) : null;
                    if (bandbox != null) {
                        return new BandboxField(fieldname, filter, desc, gridId, session, bandbox);
                    }
                    return new LongField(fieldname, filter, desc, gridId, session);
                case "biginteger":
                    return new NumberField(fieldname, filter, desc, gridId, session);
                case "double":
                    return new DoubleField(fieldname, filter, desc, gridId, session);
                case "float":
                    return new FloatField(fieldname, filter, desc, gridId, session);
                case "microunits":
                case "milliunits":
                case "nanounits":
                    return new FixedPointField(fieldName, filter, null, gridId, session);
                }
            case NUMERIC:
                if (bonaparteType.equals("decimal")) {
                    return new DecimalField(fieldName, filter, null, gridId, session);
                }
                break;
            case BINARY:
                return new BinaryField(fieldname, filter, desc, gridId, session);
            case ENUM:
            case ENUMALPHA:
                return new EnumField(fieldname, filter, desc, gridId, session);
            case ENUMSET:
                break;
            case ENUMSETALPHA:
                return new EnumsetField(fieldname, filter, desc, gridId, session, ((AlphanumericEnumSetDataItem)desc).getBaseEnumset());
            case MISC:
                if (bonaparteType.equals("uuid"))
                    return new UuidField(fieldName, filter, null, gridId, session);
                if (bonaparteType.equals("boolean"))
                    return new BooleanField(fieldname, filter, desc, gridId, session);
                break;
            case OBJECT:
                if (dropdownType != null)
                    return new DropdownField(fieldname, filter, desc, gridId, session, dropdownType);
                // check for bandboxes
                String bandbox = null != fieldProperties ? fieldProperties.get(Constants.UiFieldProperties.BANDBOX) : null;
                ObjectReference objRef = (ObjectReference)desc;
                if (bandbox == null && objRef.getLowerBound() != null)
                    bandbox = objRef.getLowerBound().getName();  // use the ref's PQON
                if (bandbox != null) {
                    return new BandboxField(fieldname, filter, desc, gridId, session, bandbox);
                }
                break;
            case STRING:
                if (fieldName.endsWith("tenantId") && !fieldProperties.containsKey("nodropdown"))
                    return new TenantField(fieldname, filter, desc, gridId, session,
                        TenantIsolationCategoryType.factory(viewModel.dtoClass.getProperty("tenantCategory")));
                if (fieldProperties == null || fieldProperties.isEmpty())  // shortcut: avoid further map lookups
                    return new TextField(fieldname, filter, desc, gridId, session);
                if (filter.getFilterType() == UIFilterType.EQUALITY) {
                    // in case of equality, dropdowns are possible
                    if (dropdownType != null)
                        return new DropdownField(fieldname, filter, desc, gridId, session, dropdownType);
                    String qualifierFor = fieldProperties.get(Constants.UiFieldProperties.QUALIFIER_FOR);
                    if (qualifierFor != null)
                        return new QualifierSelectionField(fieldname, filter, desc, gridId, session, qualifierFor);
                }
                // check for special enumset properties
                String enumSet = fieldProperties.get(Constants.UiFieldProperties.ENUMSET);
                if (enumSet != null) {
                    EnumSetDefinition esd = InitContainers.getEnumsetByPQON(enumSet);
                    if (esd == null)
                        LOGGER.error("No enumset found for pqon {}: field {}", enumSet, fieldname);
                    else
                        return new EnumsetField(fieldname, filter, desc, gridId, session, esd);
                }
                String xenumSet = fieldProperties.get(Constants.UiFieldProperties.XENUMSET);
                if (xenumSet != null) {
                    XEnumSetDefinition xesd = InitContainers.getXEnumsetByPQON(xenumSet);
                    if (xesd == null)
                        LOGGER.error("No xenumset found for pqon {}: field {}", xenumSet, fieldname);
                    else
                        return new XenumsetField(fieldname, filter, desc, gridId, session, xesd);
                }
                final String multiDropdown = fieldProperties.get(Constants.UiFieldProperties.MULTI_DROPDOWN);
                if (multiDropdown != null) {
                    return new DropdownField(fieldname, filter, desc, gridId, session, multiDropdown);
                }
                return new TextField(fieldname, filter, desc, gridId, session);
            case TEMPORAL:
                switch (bonaparteType) {
                case "instant":
                    return new InstantField(fieldname, filter, desc, gridId, session);
                case "day":
                    final boolean withToday = ZulUtils.readBooleanConfig(T9tConfigConstants.DATE_PICKER_SHOW_TODAY)
                      || (fieldProperties != null && fieldProperties.get(Constants.UiFieldProperties.SHOW_TODAY) != null);
                    return new DayField(fieldname, filter, desc, gridId, session, withToday);
                case "time":
                    return new TimeField(fieldname, filter, desc, gridId, session);
                case "timestamp":
                    return new TimestampField(fieldname, filter, desc, gridId, session);
                }
                break;
            case XENUM:
                return new XenumField(fieldname, filter, desc, gridId, session);
            case XENUMSET:
                return new XenumsetField(fieldname, filter, desc, gridId, session, ((XEnumSetDataItem)desc).getBaseXEnumset());
            default:
                break;

            }
            LOGGER.error(
                    "No matches found for {} in {} (dataCategory={}, bonType={}, javaType={}, dropdown={}. Possibly something misconfigured in "
                    + "the grid configuration?",
                    fieldName, viewModel.dtoClass.getBonaPortableClass().getCanonicalName(), dataCategory, bonaparteType, javaType, dropdownType);

        } catch (Exception e) {
            LOGGER.error("Problems in the grid configuration for field {}? {}", fieldname, e);
        }

        return null;
    }
}
