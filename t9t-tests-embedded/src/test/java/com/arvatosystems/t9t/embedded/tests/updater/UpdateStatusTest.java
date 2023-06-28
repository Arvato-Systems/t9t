/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.embedded.tests.updater;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;
import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.UpdateStatusLogDTO;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusRequest;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusResponse;
import com.arvatosystems.t9t.updates.request.StartUpdateRequest;
import com.arvatosystems.t9t.updates.request.UpdateStatusLogSearchRequest;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateStatusTest {
    private static ITestConnection dlg;
    private static int counter = 0;

    @BeforeAll
    public static void beforeAll() {
        dlg = new InMemoryConnection();
    }

    @Test
    public void testUpdateStatusTicketNotExist() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        verifyUpdateStatusLogEntry(response.getUpdateStatus().getObjectRef(), 1);
    }

    @Test
    public void testUpdateStatusTicketWithNotYetStarted() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, "Updated Description " + ticketId, false, null);
        });

        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, response.getUpdateStatus().getUpdateApplyStatus());

        verifyUpdateStatusLogEntry(response.getUpdateStatus().getObjectRef(), 2);
    }

    @Test
    public void testUpdateStatusTicketWithInProgressAndAllowRestartOfPending() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, "Updated Description " + ticketId, false, null);
        });
        final GetUpdateStatusResponse firstUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, firstUpdateResponse.getUpdateStatus().getUpdateApplyStatus());

        final String newDescription = "UPDATED Description " + ticketId;
        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, newDescription, true, null);
        });
        final GetUpdateStatusResponse secondUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, secondUpdateResponse.getUpdateStatus().getUpdateApplyStatus());
        assertEquals(newDescription, secondUpdateResponse.getUpdateStatus().getDescription());

        verifyUpdateStatusLogEntry(secondUpdateResponse.getUpdateStatus().getObjectRef(), 3);
    }

    @Test
    public void testUpdateStatusTicketWithInProgressAndNotAllowRestartOfPending() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        final String description = "Updated Description " + ticketId;
        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, description, false, null);
        });
        final GetUpdateStatusResponse firstUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, firstUpdateResponse.getUpdateStatus().getUpdateApplyStatus());

        final String newDescription = "UPDATED Description " + ticketId;
        assertThrows(Exception.class, () -> {
            fireStartUpdateRequest(ticketId, ticketId, description, false, null);
        }, "allowRestartOfPending is false and is not allow to override anything!");
        final GetUpdateStatusResponse secondUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, secondUpdateResponse.getUpdateStatus().getUpdateApplyStatus());
        assertEquals(description, secondUpdateResponse.getUpdateStatus().getDescription());

        verifyUpdateStatusLogEntry(secondUpdateResponse.getUpdateStatus().getObjectRef(), 2);
    }

    @Test
    public void testFinishUpdate() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        final String description = "Updated Description " + ticketId;
        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, description, false, null);
        });
        final GetUpdateStatusResponse firstUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, firstUpdateResponse.getUpdateStatus().getUpdateApplyStatus());

        assertDoesNotThrow(() -> {
            dlg.typeIO(new FinishUpdateRequest(ticketId), ServiceResponse.class);
        });
        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.COMPLETE, response.getUpdateStatus().getUpdateApplyStatus());

        verifyUpdateStatusLogEntry(response.getUpdateStatus().getObjectRef(), 3);
    }

    @Test
    public void testFinishUpdateTicketIsNotInProgress() {
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        assertThrows(Exception.class, () -> {
            dlg.typeIO(new FinishUpdateRequest(ticketId), ServiceResponse.class);
        }, "Ticket update status must be in progress for ticket " + ticketId);
        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.NOT_YET_STARTED, response.getUpdateStatus().getUpdateApplyStatus());

        verifyUpdateStatusLogEntry(response.getUpdateStatus().getObjectRef(), 1);
    }

    @Test
    public void testUpdateStatusTicketWithPrerequisites() {
        // prerequisite ticket
        final String prerequisiteTicketId = getTicketId();
        createNewUpdateStatus(prerequisiteTicketId);

        final String prerequisiteDescription = "Updated Description " + prerequisiteTicketId;
        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(prerequisiteTicketId, prerequisiteTicketId, prerequisiteDescription, false, null);
        });
        final GetUpdateStatusResponse prerequisiteFirstUpdateResponse = getUpdateStatusbyTicketId(prerequisiteTicketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, prerequisiteFirstUpdateResponse.getUpdateStatus().getUpdateApplyStatus());

        assertDoesNotThrow(() -> {
            dlg.typeIO(new FinishUpdateRequest(prerequisiteTicketId), ServiceResponse.class);
        });
        final GetUpdateStatusResponse prerequisiteResponse = getUpdateStatusbyTicketId(prerequisiteTicketId);
        assertEquals(UpdateApplyStatusType.COMPLETE, prerequisiteResponse.getUpdateStatus().getUpdateApplyStatus());

        verifyUpdateStatusLogEntry(prerequisiteResponse.getUpdateStatus().getObjectRef(), 3);

        // new ticket
        final String ticketId = getTicketId();
        createNewUpdateStatus(ticketId);

        final String description = "Updated Description " + ticketId;
        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, description, false, Arrays.asList(prerequisiteTicketId));
        }, "Not all prerequisite tickets are completed!");
        final GetUpdateStatusResponse firstUpdateResponse = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.IN_PROGRESS, firstUpdateResponse.getUpdateStatus().getUpdateApplyStatus());

        assertDoesNotThrow(() -> {
            dlg.typeIO(new FinishUpdateRequest(ticketId), ServiceResponse.class);
        });
        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.COMPLETE, response.getUpdateStatus().getUpdateApplyStatus());

        verifyUpdateStatusLogEntry(response.getUpdateStatus().getObjectRef(), 3);
    }

    private void createNewUpdateStatus(final String ticketId) {
        assertThrows(Exception.class, () -> {
            getUpdateStatusbyTicketId(ticketId);
        }, "Existing entry found with ticketId " + ticketId);

        assertDoesNotThrow(() -> {
            fireStartUpdateRequest(ticketId, ticketId, "Description " + ticketId, false, null);
        });

        final GetUpdateStatusResponse response = getUpdateStatusbyTicketId(ticketId);
        assertEquals(UpdateApplyStatusType.NOT_YET_STARTED, response.getUpdateStatus().getUpdateApplyStatus());
    }

    private void fireStartUpdateRequest(final String ticketId, final String applySequenceId,
            final String description, final boolean allowRestartOfPending, final List<String> prerequisites) {
        final StartUpdateRequest startUpdateRequest = new StartUpdateRequest();
        startUpdateRequest.setTicketId(ticketId);
        startUpdateRequest.setDescription(description);
        startUpdateRequest.setApplySequenceId(applySequenceId);
        startUpdateRequest.setAllowRestartOfPending(allowRestartOfPending);
        startUpdateRequest.setPrerequisites(prerequisites);
        dlg.typeIO(startUpdateRequest, ServiceResponse.class);
    }

    private GetUpdateStatusResponse getUpdateStatusbyTicketId(final String ticketId) {
        return dlg.typeIO(new GetUpdateStatusRequest(ticketId), GetUpdateStatusResponse.class);
    }

    private String getTicketId() {
        return String.valueOf(counter++);
    }

    private void verifyUpdateStatusLogEntry(final Long ticketRef, final int expectedNumOfEntry) {
        final UpdateStatusLogSearchRequest searchRequest = new UpdateStatusLogSearchRequest();
        final LongFilter filter = new LongFilter(UpdateStatusLogDTO.meta$$ticketRef.getName());
        filter.setEqualsValue(ticketRef);
        searchRequest.setSearchFilter(filter);

        final ReadAllResponse response = dlg.typeIO(searchRequest, ReadAllResponse.class);
        assertEquals(expectedNumOfEntry, response.getDataList().size());
    }
}
