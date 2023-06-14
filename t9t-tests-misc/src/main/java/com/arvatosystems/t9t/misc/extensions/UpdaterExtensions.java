package com.arvatosystems.t9t.misc.extensions;

import java.util.function.Consumer;
import java.util.function.Function;

import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.request.GetServerInformationRequest;
import com.arvatosystems.t9t.base.request.GetServerInformationResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.updater.AidDataRequest;
import com.arvatosystems.t9t.base.updater.ReadDataRequest;
import com.arvatosystems.t9t.base.updater.SearchDataRequest;
import com.arvatosystems.t9t.base.updater.UpdateDataRequest;
import com.arvatosystems.t9t.updates.UpdateStatusDTO;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusRequest;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusResponse;
import com.arvatosystems.t9t.updates.request.StartUpdateRequest;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A set of static utility methods to be imported for setup / update tests.
 */
public final class UpdaterExtensions {
    private UpdaterExtensions() {
    }

    /** Retrieves information about the server, to allow additional security checks in case a production environment is configured. */
    public static GetServerInformationResponse getServerInformation(@Nonnull final ITestConnection dlg) {
        return dlg.typeIO(new GetServerInformationRequest(), GetServerInformationResponse.class);
    }

    /**
     * Retrieves the update status data record.
     *
     * @param dlg      the backend connection
     * @param ticketId identifies the ticket (for example TBE-1025)
     * @return         the data record which describes the current status
     */
    public static UpdateStatusDTO getUpdateStatus(@Nonnull final ITestConnection dlg, @Nonnull final String ticketId) {
        return dlg.typeIO(new GetUpdateStatusRequest(ticketId), GetUpdateStatusResponse.class).getUpdateStatus();
    }

    /**
     * Marks an update to be "in progress".
     * The call passes sufficient information to create the ticket update status data record.
     *
     * @param dlg             the backend connection
     * @param ticketId        identifies the ticket (for example TBE-1025)
     * @param applySequenceId identifies some sequence information (recommended execution order)
     * @param description     a short description of the related ticket (purpose)
     * @param allowRestartOfPending if set to false, an attempt to restart a previously unfinished update
     *                        will result in an error. Ste to true if the update process can safely be
     *                        restarted / resumed at any point (i.e. operations are idempotent, such as setting some flag).
     */
    public static void startUpdate(
            @Nonnull final ITestConnection dlg,
            @Nonnull final String ticketId,
            @Nonnull final String applySequenceId,
            @Nonnull final String description,
            final boolean allowRestartOfPending) {
        final StartUpdateRequest updateRq = new StartUpdateRequest();
        updateRq.setTicketId(ticketId);
        updateRq.setApplySequenceId(applySequenceId);
        updateRq.setDescription(description);
        updateRq.setAllowRestartOfPending(allowRestartOfPending);
        dlg.okIO(updateRq);
    }

    /**
     * Marks an update as complete.
     *
     * @param dlg      the backend connection
     * @param ticketId identifies the ticket (for example TBE-1025)
     */
    public static void finishUpdate(
            @Nonnull final ITestConnection dlg,
            @Nonnull final String ticketId) {
        dlg.okIO(new FinishUpdateRequest(ticketId));
    }


    /**
     * Patches a DTO (specified by key).
     *
     * @param <T>        the class of the DTO
     * @param dlg        the backend connection
     * @param dtoClass   the class of the DTO
     * @param key        any unique key for the DTO
     * @param updater    a lambda consumer of the DTO, which applies the desired changes
     */
    public static <T extends BonaPortable> void patch(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<T> dtoClass,
            @Nonnull final BonaPortable key,
            @Nonnull final Consumer<T> updater) {
        // first, read the DTO from the backend
        final ReadDataRequest readRq = new ReadDataRequest();
        readRq.setDtoClassCanonicalName(dtoClass.getCanonicalName());
        readRq.setKey(key);
        final CrudAnyKeyResponse resp = dlg.typeIO(readRq, CrudAnyKeyResponse.class);

        // now, apply the patch
        final T dto = dtoClass.cast(resp.getData());
        updater.accept(dto);

        // finally, write back the change
        final UpdateDataRequest updateRq = new UpdateDataRequest();
        updateRq.setDtoClassCanonicalName(dtoClass.getCanonicalName());
        updateRq.setKey(key);
        updateRq.setData(dto);
        dlg.okIO(updateRq);
    }

    /**
     * Patches multiple (none to many) DTOs (specified by search filter).
     *
     * @param <T>        the class of the DTO
     * @param dlg        the backend connection
     * @param dtoClass   the class of the DTO
     * @param filter     an optional filter criteria for the search
     * @param updater    function which is intended to have side effects (modifying its passed parameter),
     *                   returning the key under which it should be written back, or null, if no further backend call is desired.
     *                   The method is allowed to perform other backend requests during evaluation, which could include deletion
     *                   of the passed data record.
     */
    public static <T extends BonaPortable, K extends BonaPortable> int patch(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<T> dtoClass,
            @Nullable final SearchFilter filter,
            @Nonnull final Function<T, K> updater) {
        // first, read the DTOs from the backend
        final SearchDataRequest readRq = new SearchDataRequest();
        readRq.setDtoClassCanonicalName(dtoClass.getCanonicalName());
        readRq.setFilter(filter);
        final ReadAllResponse resp = dlg.typeIO(readRq, ReadAllResponse.class);

        // now, apply the patch
        for (final Object data: resp.getDataList()) {
            final T dto = dtoClass.cast(((DataWithTracking)data).getData());
            final BonaPortable key = updater.apply(dto);
            if (key != null) {
                // desired change: write it back!
                final UpdateDataRequest updateRq = new UpdateDataRequest();
                updateRq.setDtoClassCanonicalName(dtoClass.getCanonicalName());
                updateRq.setKey(key);
                updateRq.setData(dto);
                dlg.okIO(updateRq);
            }
        }
        return resp.getDataList().size();
    }

    /** Assembles and invokes the AidRequest. Internal helper method. */
    private static void aidRequest(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<?> dtoClass,
            @Nonnull final BonaPortable key,
            @Nonnull final OperationType aid) {
        final AidDataRequest aidRq = new AidDataRequest();
        aidRq.setDtoClassCanonicalName(dtoClass.getCanonicalName());
        aidRq.setKey(key);
        aidRq.setOperation(aid);
        dlg.okIO(aidRq);
    }

    /**
     * Deletes a DTO (specified by key).
     *
     * @param <T>        the class of the DTO
     * @param dlg        the backend connection
     * @param dtoClass   the class of the DTO
     * @param key        any unique key for the DTO
     */
    public static <T extends BonaPortable> void delete(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<T> dtoClass,
            @Nonnull final BonaPortable key) {
        aidRequest(dlg, dtoClass, key, OperationType.DELETE);
    }

    /**
     * Activates a configuration DTO (specified by key).
     *
     * @param <T>        the class of the DTO
     * @param dlg        the backend connection
     * @param dtoClass   the class of the DTO
     * @param key        any unique key for the DTO
     */
    public static <T extends BonaPortable> void activate(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<T> dtoClass,
            @Nonnull final BonaPortable key) {
        aidRequest(dlg, dtoClass, key, OperationType.ACTIVATE);
    }

    /**
     * Deactivates a configuration DTO (specified by key).
     *
     * @param <T>        the class of the DTO
     * @param dlg        the backend connection
     * @param dtoClass   the class of the DTO
     * @param key        any unique key for the DTO
     */
    public static <T extends BonaPortable> void deactivate(
            @Nonnull final ITestConnection dlg,
            @Nonnull final Class<T> dtoClass,
            @Nonnull final BonaPortable key) {
        aidRequest(dlg, dtoClass, key, OperationType.INACTIVATE);
    }
}
