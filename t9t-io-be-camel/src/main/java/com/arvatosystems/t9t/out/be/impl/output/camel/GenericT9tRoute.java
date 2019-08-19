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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.io.DataSinkDTO;

public class GenericT9tRoute extends RouteBuilder {

    private DataSinkDTO dataSinkDTO;
    private IFileUtil fileUtil;

    public GenericT9tRoute(DataSinkDTO dataSinkDTO, IFileUtil fileUtil) {
        this.dataSinkDTO = dataSinkDTO;
        this.dataSinkDTO.freeze();

        this.fileUtil = fileUtil;
    }

    @Override
    public void configure() throws Exception {
        if (dataSinkDTO.getIsInput()) { // this has to be extended in the future for outgoing routes.
            if (dataSinkDTO.getImportQueueName() != null) {
                final String queueDirectory = fileUtil.getAbsolutePath(".import-queue/" + dataSinkDTO.getImportQueueName()).replace('\\', '/');

                from(dataSinkDTO.getFileOrQueueNamePattern())
                    .routeId(getQueueInRouteId(dataSinkDTO))
                    .process(exchange -> initMDC(exchange, dataSinkDTO))
                    .to("file://"+queueDirectory+"?autoCreate=true&tempFileName=${file:name}.intrans&flatten=true");

                from("file://"+queueDirectory+"?initialDelay=1000&delay=10000&delete=true&moveFailed=.failed&antExclude=*.intrans&sortBy=${file:modified}")
                    .routeId(getQueueOutRouteId(dataSinkDTO))
                    .process(exchange -> initMDC(exchange, dataSinkDTO))
                    .setProperty("dataSinkDTO", constant(dataSinkDTO))
                    .to(dataSinkDTO.getCamelRoute());
            } else {
                from(dataSinkDTO.getFileOrQueueNamePattern())
                    .routeId(getDirectRouteId(dataSinkDTO))
                    .process(exchange -> initMDC(exchange, dataSinkDTO))
                    .setProperty("dataSinkDTO", constant(dataSinkDTO))
                    .to(dataSinkDTO.getCamelRoute());
            }
        } else {
            throw new UnsupportedOperationException("Output Route with t9t: component is currently not supported!");
        }
    }


    private static String getDirectRouteId(DataSinkDTO dataSinkDTO) {
        return "DataSink-" + dataSinkDTO.getDataSinkId() + "-" + dataSinkDTO.getObjectRef();
    }

    private static String getQueueInRouteId(DataSinkDTO dataSinkDTO) {
        return "DataSink-" + dataSinkDTO.getDataSinkId() + "-" + dataSinkDTO.getObjectRef() + "-QueueIn";
    }

    private static String getQueueOutRouteId(DataSinkDTO dataSinkDTO) {
        return "DataSink-" + dataSinkDTO.getDataSinkId() + "-" + dataSinkDTO.getObjectRef() + "-QueueOut";
    }

    /**
     * Provide all route ids, which might be configured by this Builder.
     */
    public static List<String> getPossibleRouteIds(DataSinkDTO dataSinkDTO) {
        final List<String> result = new LinkedList<>();

        result.add(getQueueInRouteId(dataSinkDTO));
        result.add(getQueueOutRouteId(dataSinkDTO));
        result.add(getDirectRouteId(dataSinkDTO));

        return result;
    }

    private void initMDC(Exchange exchange, DataSinkDTO dataSinkDTO) {
        MDC.clear();
        MDC.put(T9tConstants.MDC_IO_DATA_SINK_ID, dataSinkDTO.getDataSinkId());
    }

}
