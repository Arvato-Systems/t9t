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
package com.arvatosystems.t9t.rep.services;

import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportParamsDTO;

/**
 * Reponsible to send the email containing the report to user's email.
 * @author NGTZ001
 *
 */
public interface IReportMailNotifier {

    /**
     * @param reportConfigDTO report config used
     * @param reportParamsDTO report param used
     * @param mailGroup userIds separated by ","
     * @param docConfigId docConfig used for the document generation
     * @param sinkRef sink ref for the generated report
     * @param selector document selector
     */
    void sendEmail(ReportConfigDTO reportConfigDTO, ReportParamsDTO reportParamsDTO, String mailGroup, String docConfigId, Long sinkRef, DocumentSelector selector);

}
