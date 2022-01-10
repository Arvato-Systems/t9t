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
package com.arvatosystems.t9t.out.be.tests;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorCsv;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorJson;
import com.arvatosystems.t9t.out.be.impl.output.OutputResourceInMemory;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OutputTest {
    private static final String NL = System.lineSeparator();

    @BeforeAll
    public static void setup() {
        Jdp.reset();
        // use real implementations only if they are publicined within this project and use stubs for everything else
    //    Jdp.bindInstanceTo(new ItemTaxComputerFactory, IItemTaxComputerFactory)
    }

    private static final DataSinkDTO MY_DATA_SINK;
    private static final OutputSessionParameters MY_OUTPUT_SESSION_PARAMETERS;
    private static final ServiceResponse MY_RECORD;

    static {
        MY_DATA_SINK = new DataSinkDTO();
        MY_DATA_SINK.setDataSinkId("test");

        MY_OUTPUT_SESSION_PARAMETERS = new OutputSessionParameters();

        MY_RECORD = new ServiceResponse();
        MY_RECORD.setReturnCode(4711);
        MY_RECORD.setErrorDetails("bad field xyz");
        MY_RECORD.setProcessRef(12L);
    }

    @Test
    public void testCsv() throws IOException {
        final MediaTypeDescriptor type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_CSV);
        final OutputResourceInMemory iors = new OutputResourceInMemory();
        iors.open(MY_DATA_SINK, MY_OUTPUT_SESSION_PARAMETERS, 1L, "dummy", type, StandardCharsets.UTF_8);

        final FormatGeneratorCsv csv = new FormatGeneratorCsv();
        csv.open(MY_DATA_SINK, MY_OUTPUT_SESSION_PARAMETERS, type.getMediaType(), null, iors, StandardCharsets.UTF_8, "ACME");
        csv.generateData(1, 1, 6263636363L, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, MY_RECORD);
        csv.generateData(2, 2, 6263636364L, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, MY_RECORD);
        csv.close();

        final String expected
          = "4711;;12;;\"bad field xyz\";" + NL
          + "4711;;12;;\"bad field xyz\";" + NL;
        Assertions.assertEquals(expected, iors.toString());
    }

    @Test
    public void testJson() throws IOException {
        final MediaTypeDescriptor type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_JSON);
        final OutputResourceInMemory iors = new OutputResourceInMemory();
        iors.open(MY_DATA_SINK, MY_OUTPUT_SESSION_PARAMETERS, 1L, "dummy", type, StandardCharsets.UTF_8);

        final FormatGeneratorJson csv = new FormatGeneratorJson();
        csv.open(MY_DATA_SINK, MY_OUTPUT_SESSION_PARAMETERS, type.getMediaType(), null, iors, StandardCharsets.UTF_8, "ACME");
        csv.generateData(1, 1, 6263636363L, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, MY_RECORD);
        csv.generateData(2, 2, 6263636364L, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, MY_RECORD);
        csv.close();

        final String expected
          = "[{\"@PQON\":\"t9t.base.api.ServiceResponse\",\"returnCode\":4711,\"processRef\":12,\"errorDetails\":\"bad field xyz\"}" + NL
          + ",{\"@PQON\":\"t9t.base.api.ServiceResponse\",\"returnCode\":4711,\"processRef\":12,\"errorDetails\":\"bad field xyz\"}" + NL
          + "]";
        Assertions.assertEquals(expected, iors.toString());
    }
}
