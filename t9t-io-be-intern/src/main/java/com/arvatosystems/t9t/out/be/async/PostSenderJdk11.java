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
package com.arvatosystems.t9t.out.be.async;

import com.arvatosystems.t9t.out.services.IAsyncSender;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * The PostSender implements a simple client invocation via http POST of the JDK 11 HttpClient.
 */
@Dependent
@Named("jdk11JSON")
public class PostSenderJdk11 extends AbstractPostSenderJdk11 implements IAsyncSender {
}
