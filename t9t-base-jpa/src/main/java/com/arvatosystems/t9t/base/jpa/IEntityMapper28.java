package com.arvatosystems.t9t.base.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAll28Response;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

public interface IEntityMapper28<KEY extends Serializable, DTO extends BonaPortable, TRACKING extends TrackingBase,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>> extends IEntityMapper<KEY, DTO, TRACKING, ENTITY> {

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
