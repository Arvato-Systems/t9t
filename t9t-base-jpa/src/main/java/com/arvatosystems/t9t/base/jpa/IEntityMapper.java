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
package com.arvatosystems.t9t.base.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAll28Response;
import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** Defines a set of conversion methods between DTO and JPA entity classes.
 * The interface is parameterized and should be specialized for every DTO, using the name I(DTOname)Mapper (because the entity is implied by the DTO).
 * The core implementation provides final newInstance methods, as well as the core implementations, customizations extend the helper methods to include
 * more mappings. Extensions are defined synchronous to the DTO type hierarchy. Mappings can go to any level of the JPA entity object hierarchy.
 *
 * Sources are the impl/someName classes, which either
 * 1) inherit from some other class, the inheritance root class implements an interface which extends this generic one
 * 2) do not inherit, then it's the root mapping, defining Rtti / new etc.
 *
 * The class must define either dto2Entity or entity2Dto, from which the actual type parameters will be derived.
 * If both methods are defined, the parameter types must match.
 * The generator will perform any code referenced in these methods after automated mappings have been done.
 * For this purpose, the code is moved to a private method, which is invoked.
 */
public interface IEntityMapper<KEY extends Serializable, DTO extends BonaPortable, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>> {

    /** Returns a new instance of the customized class of the DTO.
     *
     * @return a new instance of the customization of the DTO class object.
     * @throws T9tException if the class could not be resolved or not instantiated.
     */
    public DTO newDtoInstance(); // create a new (customized extension) of the DTO

    public int getRtti(); // get the DTO's RTTI value

    public Class<DTO> getBaseDtoClass(); // get the DTO's base class

    /** Returns the customized class of the DTO.
     *
     * @return the class object of the DTO which is valid for the given customization.
     * @throws T9tException if the class could not be resolved.
     */
    public Class<? extends DTO> getDtoClass();

    /**
     * Converts a JPA entity into a DTO. The caller does not need to worry about allocating a new object. If the entity has been converted to a DTO before and
     * no update statement was performed in the meantime, the identical instance is returned (identity preservation: mapToDto(e1) == mapToDto(e2) if and only if
     * (e1 == e2)).
     *
     * The call is null-safe, i.e. if the entity is null, the returned DTO will be null.
     *
     * @param entity
     *            - the JPA entity
     * @return - the entity mapped to a DTO
     */
    public DTO mapToDto(ENTITY entity);

    /**
     * Converts a JPA entity into a DTO, when the entity itself is not available, only its primary key. This is just a shorthand for
     * mapToDto(resolver.find(KEY)). The call is null-safe, i.e. if the key is null, the returned DTO will be null.
     *
     * @param key
     *            - the key
     * @return - the entity mapped to a DTO
     */
    public DTO mapToDto(KEY key);

    /**
     * Creates a new Entity class of the current customization and fills it from the entity provided as parameter. The call is null-safe, i.e. if the DTO is
     * null, the returned entity will be null.
     *
     * @param dto
     * @param onlyActive
     * @return the entity
     */
    public ENTITY mapToEntity(DTO dto, boolean onlyActive); // convert a DTO into an entity

    // protected void dto2Entity(ENTITY entity, DTO dto, boolean onlyActive);  // helper method which is overridden by extensions / customizations
    // protected void entity2Dto(ENTITY entity, DTO dto);  // helper method which is overridden by extensions / customizations

    /**
     * Merges the fields of the provided DTO into an existing entity. This is used for updates, i.e. the JPA attachment status of the existing entity is not
     * changed.
     *
     * @param entity
     *            - an existing instance of a JPA entity
     * @param dto
     *            - a DTO with data to be updated.
     * @param onlyActive
     */
    public void merge2Entity(ENTITY entity, DTO dto, boolean onlyActive); // synonym for protected dto2Entity

    /**
     * Maps a collection of entities to a list of standard DTOs.
     *
     * @param entityList
     *            The input list. List entries may not be null.
     * @return a list, which may be empty.
     */
    public List<DTO> mapListToDto(Collection<ENTITY> entityList);

    /**
     * Maps a collection of DTOs to entities and adds those to an existing collection.
     *
     * @param dtoList
     *            The input list. List entries may not be null.
     */
    public void mapCollectionToEntity(Collection<ENTITY> target, Collection<DTO> dtoList, boolean onlyActive);

    /** Maps an entity to a DataWithTrackingS.
     */
    public DataWithTrackingS<DTO, TRACKING> mapToDwt(ENTITY entity);

    /** Maps a collection of entities to a list of special DTOs which include the tracking columns.
     * Used by generic search and generic "ReadAll".
     * @param entityList The input list. List entries may not be null.
     * @return a list, which may be empty.
     */
    public List<DataWithTrackingS<DTO, TRACKING>> mapListToDwt(Collection<ENTITY> entityList);

    /** Postprocesses a search output, either mapping it to some ReadAllResponse, or exporting it via IOutputSession and
     * returning the sinkRef (if op != null).
     * @param data input data (list of entities)
     * @param op  OutputSessionParameters - if not null, then the data will be exported instead of returned as a list
     * @return the full web service response structure
     * @throws Exception
     */
    public ReadAll28Response<DTO, TRACKING> createReadAllResponse(List<ENTITY> data, OutputSessionParameters op) throws Exception;

    /**
     * Verifies that no field with property "notupdatable" has a different value in the intended entity. Throws a T9tException.FIELD_MAY_NOT_BE_CHANGED if a
     * field with different value has been found.
     *
     * @param current
     * @param intended
     */
    public void checkNoUpdateFields(ENTITY current, DTO intended);

    /**
     * Queries a property of the DTO class for its value. Returns the value or null if no property has been set. Returns an empty string if no value was assigned.
     * Implementation currently works for properties of the base DTO only. Subclass mappers have to override it in order to provide properties of subclasses.
     */
    public String getProperty(String propertyname);

    /**
     * Replaces field references in a generic search request which are path elements (indicated by property searchPrefix), for the critera builder.
     *
     * @param searchRequest the entity specific search request
     */
    public void processSearchPrefixForDB(SearchCriteria searchCriteria);

    /**
     * Replaces field references in a generic search request which are path elements (indicated by property searchPrefix), for the critera builder - two parameter version.
     *
     */
    public void processSearchPrefixForDB(SearchFilter filter, List<SortColumn> sortColumns);

    /** returns the entity's tenantId without the use of reflection, or null if the entity does not contain
     * a tenantId field.
     * @param e
     * @return the tenantRef
     */
    public String getTenantId(ENTITY e);

    /** Sets the entity's tenantId without the use of reflection, or NOOP if the entity does not contain
     * a tenantId field.
     * @param e - an instance of the Entity
     * @param tenantId - the tenant to be set (if null, the current call's tenant ID will be used)
     */
    public void setTenantId(ENTITY e, String tenantId);
}
