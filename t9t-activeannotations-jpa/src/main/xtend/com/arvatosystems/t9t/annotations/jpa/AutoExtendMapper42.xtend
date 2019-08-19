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
package com.arvatosystems.t9t.annotations.jpa;

import com.arvatosystems.t9t.annotations.NotUpdatable
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.jpa.impl.AbstractEntityMapper42
import de.jpaw.bonaparte.pojos.apiw.Ref
import de.jpaw.dp.Singleton
import de.jpaw.dp.Specializes
import de.jpaw.util.ApplicationException
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility

import static extension com.arvatosystems.t9t.annotations.jpa.Tools.*

@Active(AutoExtendMapper42Processor) annotation AutoExtendMapper42 {}

/** The AutoExtendMapper generates data copies for elements of same name and type only. Everything else must be handcoded.
 *
 * Application: An Automapper exists for a base class. Due to some custom extension, mapping of additional fields is required.
 * Therefore, a blank extension class must be created, which extends the existing class.
 *
 * Manual mapping code must be provided in the methods
  private void _entity2dto(final (extendedClass) entity, final (extendedDTO) dto) {}
  and
  private void _dto2entity(final (extendedClass) entity, final (extendedDTO) dto), final boolean onlyActive) {}
 *
 * If no manual code is required, at least the first method must be provided as a stub, it serves also the purpose
 * to determine the extended class parameters.
 *
 * The active annotation does not create any new classes / interfaces, it only provides a method body.
 */

class AutoExtendMapper42Processor extends AbstractClassProcessor {
//    val mapperRevision = "2016-09-14 10:00 CEST (Xtend 2.10.0, Java 8)"

    override doTransform(MutableClassDeclaration c, extension TransformationContext context) {
        val t9tException = T9tException.newTypeReference
        val applicationException = ApplicationException.newTypeReference
        val overrideAnno = Override.newAnnotationReference
        val noUpdateAnnoType = NotUpdatable.newTypeReference.type
        val refType = Ref.newTypeReference


        // We don't want this annotation on the java class (unwanted dependency)
        c.removeAnnotation(c.annotations.findFirst[annotationTypeDeclaration === AutoExtendMapper.newTypeReference.type])

        // add a CDI @Specializes annotation, as well ApplicationScoped
        c.addAnnotation(Specializes.newAnnotationReference)
        c.addAnnotation(Singleton.newAnnotationReference)

        val manualE2DCopyMethod = c.declaredMethods.findFirst[
            simpleName == '_entity2dto' &&
            returnType == primitiveVoid &&
            parameters.size == 2
        ]
        if (manualE2DCopyMethod === null) {
            c.addError('''A method _entity2dto with exactly 2 parameters and return type void must be provided''')
            return
        }
        val baseMapperClass = c.extendedClass
        val extendedEntityClass = manualE2DCopyMethod.parameters.get(0).type
        val extendedDtoClass = manualE2DCopyMethod.parameters.get(1).type

        if (baseMapperClass === null || !AbstractEntityMapper42.newTypeReference.isAssignableFrom(AbstractEntityMapper42.newTypeReference)) {
            c.addError('''This class must extend another class which inherits (directly or indirectly) AbstractEntityMapper''')
            return
        }
        val overriddenE2DCopyMethod = (baseMapperClass.type as ClassDeclaration).declaredMethods.findFirst[
            simpleName == 'entity2dto' &&
            returnType == primitiveVoid &&
            parameters.size == 2
        ]
        if (overriddenE2DCopyMethod === null) {
            c.addError('''Issue in the base class «baseMapperClass.simpleName»: No appropriate method entity2dto found!''')
            return
        }
        val manualD2ECopyMethod = c.declaredMethods.findFirst[
            simpleName == '_dto2entity' &&
            returnType == primitiveVoid &&
            parameters.size == 3
        ]

        val baseEntityClass = overriddenE2DCopyMethod.parameters.get(0).type
        val baseDtoClass = overriddenE2DCopyMethod.parameters.get(1).type

        if (!baseEntityClass.isAssignableFrom(extendedEntityClass)) {
            manualE2DCopyMethod.parameters.get(0).addError('''Parameter must be a subclass of «overriddenE2DCopyMethod.parameters.get(0).simpleName»''')
            return
        }
        if (!baseDtoClass.isAssignableFrom(extendedDtoClass)) {
            manualE2DCopyMethod.parameters.get(1).addError('''Parameter must be a subclass of «overriddenE2DCopyMethod.parameters.get(1).simpleName»''')
            return
        }

        // make compiler happy to find a suitable exception
        manualE2DCopyMethod.exceptions = applicationException.andMaybeAsWell(manualE2DCopyMethod?.exceptions)
        manualE2DCopyMethod.visibility = Visibility::PROTECTED

        // create a stub D2E (if not provided) just to hold the application exception
        if (manualD2ECopyMethod === null) {
            c.addMethod("_dto2entity") [
                visibility = Visibility::PROTECTED
                returnType = primitiveVoid
                addParameter("entity", extendedEntityClass);
                addParameter("dto", extendedDtoClass);
                addParameter("onlyActive", primitiveBoolean)
                exceptions = #[ applicationException ]
                docComment = '''(just an artificial stub)'''
                body = ['''''']
            ]
        } else {
            manualD2ECopyMethod.exceptions = applicationException.andMaybeAsWell(manualD2ECopyMethod?.exceptions)
        }

        // OK, all parameters exist and are fine!
        // now, create the actual methods we want!

        c.addMethod("entity2dto") [
            addAnnotation(overrideAnno)
            visibility = Visibility::PROTECTED
            returnType = primitiveVoid
            exceptions = t9tException // .andMaybeAsWell(manualE2DCopyMethod?.exceptions)
            addParameter("entity", baseEntityClass);
            addParameter("dto", baseDtoClass);
            docComment = '''{@inheritDoc}'''
            body = [ '''
                // first, map all fields of the superclass(es)
                super.entity2dto(entity, dto);
                // now, perform autogenerated mapping of fields, if extended objects are provided
                if (entity instanceof «toJavaCode(extendedEntityClass)» && dto instanceof «toJavaCode(extendedDtoClass)») {
                    try {
                        «toJavaCode(extendedDtoClass)» extDto = («toJavaCode(extendedDtoClass)»)dto;
                        «toJavaCode(extendedEntityClass)» extEntity = («toJavaCode(extendedEntityClass)»)entity;
                        «extendedDtoClass.buildMapping(extendedEntityClass.type as ClassDeclaration, "extDto", "extEntity", false)»
                        _entity2dto(extEntity, extDto);
                    «exceptionCatcher(toJavaCode(t9tException), toJavaCode(applicationException))»
                }
            ''']
        ]
        c.addMethod("dto2entity") [
            addAnnotation(overrideAnno)
            visibility = Visibility::PROTECTED
            returnType = primitiveVoid
            exceptions = t9tException // .andMaybeAsWell(manualD2ECopyMethod?.exceptions)
            addParameter("entity", baseEntityClass);
            addParameter("dto", baseDtoClass);
            addParameter("onlyActive", primitiveBoolean)
            docComment = '''{@inheritDoc}'''
            body = [ '''
                // first, map all fields of the superclass(es)
                super.dto2entity(entity, dto, onlyActive);
                // now, perform autogenerated mapping of fields, if extended objects are provided
                if (entity instanceof «toJavaCode(extendedEntityClass)» && dto instanceof «toJavaCode(extendedDtoClass)») {
                    try {
                        «toJavaCode(extendedDtoClass)» extDto = («toJavaCode(extendedDtoClass)»)dto;
                        «toJavaCode(extendedEntityClass)» extEntity = («toJavaCode(extendedEntityClass)»)entity;
                        «extendedDtoClass.buildMapping(extendedEntityClass.type as ClassDeclaration, "extEntity", "extDto", false)»
                        _dto2entity(extEntity, extDto, onlyActive);
                    «exceptionCatcher(toJavaCode(t9tException), toJavaCode(applicationException))»
                }
            ''']
        ]
        // possibly override (extend) the checkNoUpdateFields method
        val dtoClass = extendedDtoClass.type as ClassDeclaration

        if (!dtoClass.final) {
            val fieldsToCheck = dtoClass.declaredFields.filter[findAnnotation(noUpdateAnnoType) !== null && !refType.isAssignableFrom(type)]
            if (!fieldsToCheck.isEmpty) {
                // check for fields with notupdatable. If any exists, overwrite method checkNoUpdateFields
                c.addMethod("checkNoUpdateFields") [
                    visibility = Visibility::PUBLIC
                    returnType = primitiveVoid
                    addAnnotation(overrideAnno)
                    docComment = "{@inheritDoc}"
                    exceptions = applicationException
                    addParameter("baseCurrent", baseEntityClass);
                    addParameter("baseIntended", baseDtoClass);
                    body = [ '''
                        super.checkNoUpdateFields(baseCurrent, baseIntended);
                        if (baseCurrent instanceof «toJavaCode(extendedEntityClass)» && baseIntended instanceof «toJavaCode(extendedEntityClass)») {
                            «toJavaCode(extendedEntityClass)» current = («toJavaCode(extendedEntityClass)»)baseCurrent;
                            «toJavaCode(extendedDtoClass)» intended = («toJavaCode(extendedDtoClass)»)baseIntended;
                            «fieldsToCheck.createCheckNoUpdateFields»
                        }
                    ''' ]
                ]
            }
        }
    }
}
