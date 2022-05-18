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
package com.arvatosystems.t9t.be.arch.testlib;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import com.arvatosystems.t9t.arch.testlib.StandardArchTests;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Singleton;

public class BackendArchTests extends StandardArchTests {

    @ArchTest
    protected final ArchRule request_handler_naming_convention =
        classes().that().haveSimpleNameEndingWith("RequestHandler").and().areNotInterfaces()
                .should().implement(IRequestHandler.class)
                .andShould().notBeAnnotatedWith(Singleton.class)
                .andShould().notBeAnnotatedWith(Dependent.class)
                .andShould().bePublic()
                .because("Naming convention of RequestHandlers should not be violated, and they do not use dependency injection");

    @ArchTest
    protected final ArchRule request_handler_naming_convention_reverse =
        classes().that().implement(IRequestHandler.class).and().areNotInterfaces()  // exclude IRequestHandler itself...
                .should().haveSimpleNameEndingWith("RequestHandler")
                .because("Naming convention of RequestHandlers should not be violated");

    @ArchTest
    protected final ArchRule jpa_entity_package_naming_convention =
        classes().that().areAnnotatedWith(Entity.class).or().areAnnotatedWith(Embeddable.class)
                .should().resideInAPackage("..entities")
                .because("Convention on package naming of JPA entities and embeddables");
}
