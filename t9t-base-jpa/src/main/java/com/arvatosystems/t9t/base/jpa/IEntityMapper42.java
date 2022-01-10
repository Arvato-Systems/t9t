/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;

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
public interface IEntityMapper42<KEY extends Serializable, DTO extends BonaPortable, TRACKING extends TrackingBase,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>> extends IEntityMapper<KEY, DTO, TRACKING, ENTITY> {

    /** Maps an entity to a DataWithTrackingW.
     */
    DataWithTrackingW<DTO, TRACKING> mapToDwt(ENTITY entity);

    /** Maps a collection of entities to a list of special DTOs which include the tracking columns.
     * Used by generic search and generic "ReadAll".
     * @param entityList The input list. List entries may not be null.
     * @return a list, which may be empty.
     */
    List<DataWithTrackingW<DTO, TRACKING>> mapListToDwt(Collection<ENTITY> entityList);

    /** Postprocesses a search output, either mapping it to some ReadAllResponse, or exporting it via IOutputSession and
     * returning the sinkRef (if op != null).
     * @param data input data (list of entities)
     * @param op  OutputSessionParameters - if not null, then the data will be exported instead of returned as a list
     * @return the full web service response structure
     */
    ReadAllResponse<DTO, TRACKING> createReadAllResponse(List<ENTITY> data, OutputSessionParameters op) throws Exception;

    /** returns the entity's tenantRef without the use of reflection, or null if the entity does not contain
     * a tenantRef field.
     * @param e
     * @return the tenantRef
     */
    Long getTenantRef(ENTITY e);

    /** Sets the entity's tenantRef without the use of reflection, or NOOP if the entity does not contain
     * a tenantRef field.
     * @param e - an instance of the Entity
     * @param tenantRef - the tenant to be set (if null, the current call's tenant ref will be used)
     */
    void setTenantRef(ENTITY e, Long tenantRef);
}
