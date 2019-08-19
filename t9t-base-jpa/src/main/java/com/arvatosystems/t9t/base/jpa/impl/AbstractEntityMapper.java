/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAll28Response;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.ISearchTools;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

/** base implementation of the IEntityMapper interface, only suitable for simple configuration data tables */
public abstract class AbstractEntityMapper<KEY extends Serializable, DTO extends BonaPortable, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>> implements IEntityMapper<KEY, DTO, TRACKING, ENTITY> {
    static private final String SEARCH_PREFIX_PROPERTY = "searchprefix";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityMapper.class);

    //@Inject
    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    private final List<DataWithTrackingS<DTO, TRACKING>> EMPTY_RESULT_LIST = new ArrayList<DataWithTrackingS<DTO, TRACKING>>(0);

    //@Inject
    protected final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);

    //@Inject
    protected final ISearchTools searchTools = Jdp.getRequired(ISearchTools.class);

    // overridden for final classes (to have a shortcut)
    @Override
    public DTO newDtoInstance() {
        return contextProvider.get().customization.newDtoInstance(getRtti(), getBaseDtoClass());
    }

    // overridden for final classes (to have a shortcut)
    @Override
    public Class<? extends DTO> getDtoClass() {
        return contextProvider.get().customization.getDtoClass(getRtti(), getBaseDtoClass());
    }

    @Override
    public final DataWithTrackingS<DTO, TRACKING> mapToDwt(ENTITY entity) {
        if (entity == null) {
            return null;
        }
        DataWithTrackingS<DTO, TRACKING> entry = new DataWithTrackingS<DTO, TRACKING>();
        entry.setTracking(entity.ret$Tracking());
        // entry.setIsActive(entity.ret$Active());
        entry.setData(mapToDto(entity));
        entry.setTenantId(getTenantId(entity));  // either tenantId or tenantRef has been defined in the data (category D) or it is of no interest to the caller
        return entry;
    }

    @Override
    public final List<DataWithTrackingS<DTO, TRACKING>> mapListToDwt(Collection<ENTITY> entityList) {
        if (entityList == null) {
            return null;
        }
        List<DataWithTrackingS<DTO, TRACKING>> resultList = new ArrayList<>(entityList.size());
        for (ENTITY entity : entityList) {
            resultList.add(mapToDwt(entity));
        }
        return resultList;
    }

    @Override
    public final List<DTO> mapListToDto(Collection<ENTITY> entityList) {
        if (entityList == null) {
            return null;
        }
        List<DTO> resultList = new ArrayList<>(entityList.size());
        for (ENTITY entity : entityList) {
            resultList.add(mapToDto(entity));
        }
        return resultList;
    }

    @Override
    public final void mapCollectionToEntity(Collection<ENTITY> target, Collection<DTO> dtoList, boolean onlyActive) {
        if (dtoList != null) {
            for (DTO dto : dtoList) {
                target.add(mapToEntity(dto, onlyActive));
            }
        }
    }

    @Override
    public final ReadAll28Response<DTO, TRACKING> createReadAllResponse(List<ENTITY> data, OutputSessionParameters op) throws Exception {
        ReadAll28Response<DTO, TRACKING> rs = new ReadAll28Response<DTO, TRACKING>();
        if (op == null) {
            // fill the result
            rs.setDataList(mapListToDwt(data));
        } else {
            // push output into an outputSession (export it)
            try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
                Long sinkRef = outputSession.open(op);
                if (outputSession.getUnwrapTracking(op.getUnwrapTracking())) {
                    op.setSmartMappingForDataWithTracking(Boolean.FALSE);
                    for (ENTITY entity : data) {
                        outputSession.store(mapToDto(entity));
                    }
                } else {
                    op.setSmartMappingForDataWithTracking(Boolean.TRUE);
                    DataWithTrackingS<DTO, TRACKING> entry = new DataWithTrackingS<DTO, TRACKING>();
                    for (ENTITY entity : data) {
                        entry.setTracking(entity.ret$Tracking());
                        entry.setData(mapToDto(entity));
                        entry.setTenantId(getTenantId(entity));  // either tenantId or tenantRef has been defined in the data (category D) or it is of no interest to the caller
                        outputSession.store(entry);
                    }
                }
                // successful close: store ref
                rs.setSinkRef(sinkRef);
                rs.setDataList(EMPTY_RESULT_LIST);
            }
        }
        rs.setReturnCode(0);
        return rs;
    }

    @Override
    public final void merge2Entity(ENTITY entity, DTO dto, boolean onlyActive) {
        dto2entity(entity, dto, onlyActive);
    }

    /** just a hook not defined in the public interface which allows to jump up to the specific implementation. */
    abstract protected void dto2entity(ENTITY entity, DTO dto, boolean onlyActive);

    protected final DTO fromCache(ENTITY e, Class<DTO> clazz) {
        // xtend: ServiceSessionContext.getDtoCache()?.get(e)?.get(clazz) as DTO
        // DTO CACHE GONE
//        Map<BonaPersistableNoData<?, ?>, Map<Class<? extends BonaPortable>, BonaPortable>> cache = jpaContextProvider.get().dtoCache;
//        if (cache != null) {
//            Map<Class<? extends BonaPortable>, BonaPortable> map = cache.get(e);
//            if (map != null) {
//                return (DTO) map.get(clazz);
//            }
//        }
        return null;
    }

    protected final void toCache(ENTITY e, Class<DTO> clazz, DTO dto) {
        // xtend: ServiceSessionContext.getDtoCache()?.get(e)?.get(clazz) as DTO
        // DTO CACHE GONE
//        Map<BonaPersistableNoData<?, ?>, Map<Class<? extends BonaPortable>, BonaPortable>> cache = jpaContextProvider.get().dtoCache;
//        if (cache != null) {
//            Map<Class<? extends BonaPortable>, BonaPortable> map = cache.get(e);
//            if (map == null) {
//                map = new HashMap<Class<? extends BonaPortable>, BonaPortable>(4);
//                cache.put((BonaPersistableNoData<?, ?>) e, map);
//            }
//            map.put(clazz, dto);
//        }
    }

    /**
     * Verifies that no field with property "notupdatable" has a different value in the intended entity. Throws a T9tException.FIELD_MAY_NOT_BE_CHANGED if a
     * field with different value has been found. Base implementation - no field to check.
     *
     * @param current
     * @param intended
     * @throws T9tException
     */
    @Override
    public void checkNoUpdateFields(ENTITY current, DTO intended) {
    }

    /**
     * Provides a single level search and sort prefix replacement, based on the DTO's properties "searchprefix". This method must be called from GenericSearch
     * request handler implementations before the search() method of the resolver is called. As Java does not know interface declarations for static methods,
     * access to the DTO's static methods is provided via workaround, namely autogenerated methods of the mapper class.
     *
     * See "SinkEntity" referencing "DataSinkDTO" for an example.
     */
    @Override
    public final void processSearchPrefixForDB(SearchCriteria searchCriteria) {
        processSearchPrefixForDB(searchCriteria.getSearchFilter(), searchCriteria.getSortColumns());
    }

    /**
     * Provides a single level search and sort prefix replacement, based on the DTO's properties "searchprefix". This method must be called from GenericSearch
     * request handler implementations before the search() method of the resolver is called. As Java does not know interface declarations for static methods,
     * access to the DTO's static methods is provided via workaround, namely autogenerated methods of the mapper class.
     *
     * See "SinkEntity" referencing "DataSinkDTO" for an example.
     */
    @Override
    public final void processSearchPrefixForDB(SearchFilter filter, List<SortColumn> sortColumns) {
        if (filter != null) {
            searchTools.mapNames(filter, this::processSearchPrefixForDBSub);
            searchTools.mapNames(filter, this::unrollIndexedFieldNames);
        }

        if (sortColumns != null) {
            searchTools.mapNames(sortColumns, this::processSearchPrefixForDBSub);
            searchTools.mapNames(sortColumns, this::unrollIndexedFieldNames);
        }
    }

    /** Method to replace components which refer to a child entity name at DTO level
     * by the internal JPA child entity name (which is defined in the bon file as a field property 'searchprefix').
     * @param fieldName - the input field name, for example xyz.locationRef.name
     * @return the converted name, for example xyz.location.name
     *
     * This implementation is currently restricted to replacements at top level only.
     */
    private String processSearchPrefixForDBSub(final String fieldName) {
        int dotPos = fieldName.indexOf('.');
        if ((dotPos >= 0) && (fieldName.length() > dotPos)) {  // TODO: can the second condition ever be true? dotPos < length should always hold!
            // this is a candidate: if the initial portion has a property "searchprefix", then replace it
            String alternatePrefix = getProperty(fieldName.substring(0, dotPos) + "." + SEARCH_PREFIX_PROPERTY);
            if (alternatePrefix != null) {
                // special case: replace it by nothing... (skipping it, as required by some composite objects)
                if (alternatePrefix.length() == 0)
                    ++dotPos;  // skip the "."
                String result = alternatePrefix + fieldName.substring(dotPos); // temporary var to avoid duplicate construction of string when log level is debug
                LOGGER.debug("{}: replacing column {} by {}", getClass().getCanonicalName(), fieldName, result);
                return result;
            }
        }
        return fieldName;
    }

    /** Method to replace components which refer to an indexed array instance name at DTO level
     * by the internal JPA field name, which is created by unrolling loops. Currently a fixed suffix of 2 digits is assumed.
     * @param fieldName - the input field name, for example xyz.location[0].name
     * @return the converted name, for example xyz.location00.name
     */
    private String unrollIndexedFieldNames(final String fieldName) {
        int dotPos = fieldName.indexOf('[');
        if (dotPos < 0)
            return fieldName;  // no change: shortcut!
        // has to replace at least one array index
        final int l = fieldName.length();
        StringBuilder newName = new StringBuilder(l);
        int currentSrc = 0;
        while (dotPos >= 0) {
            // copy all until the array index start
            newName.append(fieldName.substring(currentSrc, dotPos));
            currentSrc = dotPos + 1;
            dotPos = fieldName.indexOf(']', currentSrc);
            if (dotPos <= currentSrc)
                throw new T9tException(T9tException.MALFORMATTED_FIELDNAME, fieldName);
            // insert the index + 1 as 2 digit number
            int num = Integer.parseInt(fieldName.substring(currentSrc, dotPos));
            newName.append(String.format("%02d", num+1));
            currentSrc = dotPos + 1;
            dotPos = fieldName.indexOf('[', currentSrc);
        }
        // all instances found, copy any remaining characters
        if (currentSrc < l)
            newName.append(fieldName.substring(currentSrc));
        String result = newName.toString();  // temporary var to avoid duplicate construction of string when log level is debug
        LOGGER.debug("{}: replacing column {} by {}", getClass().getCanonicalName(), fieldName, result);
        return result;
    }

    /** returns the entity's tenantRef without the use of reflection, or null if the entity does not contain
     * a tenantRef field.
     * @param e
     * @return the tenantRef
     */
    @Override
    public String getTenantId(ENTITY e) {
        return null;
    }

    /** Sets the entity's tenantRef without the use of reflection, or NOOP if the entity does not contain
     * a tenantRef field.
     * @param e - an instance of the Entity
     * @param tenantRef - the tenant to be set (if null, the current call's tenant ref wil be used)
     */
    @Override
    public void setTenantId(ENTITY e, String tenantId) {
    }
}
