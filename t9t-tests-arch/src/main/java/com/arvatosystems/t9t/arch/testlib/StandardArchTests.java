/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.arch.testlib;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import org.slf4j.Logger;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Inject;
import de.jpaw.dp.Singleton;

public class StandardArchTests {
    @ArchTest
    protected final ArchRule no_java_util_logging =
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    protected final ArchRule loggers_should_be_private_static_final =
        fields().that().haveRawType(Logger.class)
            .should().bePrivate()
            .andShould().beStatic()
            .andShould().beFinal()
            .andShould().haveName("LOGGER")
            .because("Development guidelines, section 4.6, requires it");

    @ArchTest
    protected final ArchRule injected_fields_should_be_final =
        fields().that().areAnnotatedWith(Inject.class)
            .should().beFinal()
            .because("Injection is a one-time initialization");

    // checks on Singletons
    @ArchTest
    protected final ArchRule checks_on_singletons =
        classes().that().areAnnotatedWith(Singleton.class)
            .should().notBeAnnotatedWith(Dependent.class)
             //   .andShould().haveOnlyFinalFields()  // unfortunately, $SWITCH_TABLE$ fields are generated non-final by the Java compiler, also the @Lazy annotation defines an exception
            .andShould().bePublic()
            .because("A @Singleton cannot be @Dependent or be non public, or have non-final fields");

    // checks on constructors
//    @ArchTest
//    protected final ArchRule checks_on_constructors =
//            constructors().that().areDeclaredInClassesThat().areAnnotatedWith(Singleton.class).or().areAnnotatedWith(Dependent.class).or().haveNameEndingWith("RequestHandler")
//                .should().bePublic().andShould(null).n
//                .because("A @Singleton cannot have a parametrized constructor");

    // checks on final requirement of fields
    @ArchTest
    protected final ArchRule checks_on_final_fields =
        fields().that().areDeclaredInClassesThat().areAnnotatedWith(Singleton.class).or().haveNameEndingWith("RequestHandler")
            .should().beFinal().orShould().beAnnotatedWith(IsLogicallyFinal.class).orShould().haveNameContaining("$SWITCH_TABLE$")  // SWITCH_TABLE variables are generated by the Java compiler - unfortunately we cannot fix that one
            .because("A @Singleton or RequestHandler cannot have instance variables unless they are constant");
}