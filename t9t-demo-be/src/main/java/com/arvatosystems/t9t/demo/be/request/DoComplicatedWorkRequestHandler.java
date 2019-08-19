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
package com.arvatosystems.t9t.demo.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.demo.be.init.IDemo;
import com.arvatosystems.t9t.demo.request.ComplicatedWorkResponse;
import com.arvatosystems.t9t.demo.request.DoComplicatedWorkRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;

import de.jpaw.dp.Jdp;

public class DoComplicatedWorkRequestHandler extends AbstractRequestHandler<DoComplicatedWorkRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoComplicatedWorkRequestHandler.class);

    IDemo justStuff = Jdp.getRequired(IDemo.class);

    @Override
    public boolean isReadOnly(DoComplicatedWorkRequest params) {
        return true;
    }

    @Override
    public ComplicatedWorkResponse execute(DoComplicatedWorkRequest rq) {
        LOGGER.info("Hi, I'm doing complicated work now!");

        ComplicatedWorkResponse response = new ComplicatedWorkResponse();
        response.setReturnCode(0);
        response.setSum(rq.getA() + rq.getB());
        return response;
    }

}
