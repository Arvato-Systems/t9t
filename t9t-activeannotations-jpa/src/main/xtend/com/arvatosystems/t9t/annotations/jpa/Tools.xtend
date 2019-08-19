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
package com.arvatosystems.t9t.annotations.jpa

import de.jpaw.dp.Jdp
import java.util.List
import java.util.Set
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.InterfaceDeclaration
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTarget
import org.eclipse.xtend.lib.macro.declaration.Type

class Tools {
    /** Extension method for the String class to return the substring without the specified suffix (or the original string, if the parameter string does not end with the suffix). */
    def public static String without(String text, String endingToRemove) {
        if (text.length > endingToRemove.length && text.endsWith(endingToRemove))
            return text.substring(0, text.length - endingToRemove.length)
        else
            return text
    }
    /** Extension method for the String class to return the substring without the specified prefix (or the original string, if the parameter string does not end with the suffix). */
    def public static String behind(String text, String prefixToRemove) {
        if (text.length > prefixToRemove.length && text.startsWith(prefixToRemove))
            return text.substring(prefixToRemove.length)
        else
            return text
    }

    // the next 3 methods define the naming used for the generated classes and interfaces
    def public static getPackageName(ClassDeclaration m) {
        return m.qualifiedName.without(m.simpleName).without("impl.")
    }

    def public static isSimpleClass(TypeReference s) {
        return !(s.isVoid || s.primitive || s.wrapper || s.array) && s.type instanceof ClassDeclaration
    }

    def public static void xferParameters(MutableMethodDeclaration target, MutableMethodDeclaration src) {
        if (src !== null)
            for (p: src.parameters)
                target.addParameter(p.simpleName, p.type)
            // src.parameters.forEach[target.addParameter(simpleName, type)]
    }

    def public static parameterList(Iterable<? extends MutableParameterDeclaration> m)
        '''«m.map[simpleName].join(',')»'''

    def public static FieldDeclaration findFieldRecursively(ClassDeclaration cl, String fieldName) {
        // in some cases, there is no field, but only a getter/setter (for example for proxy methods
        // of embedded composite primary keys
        val field = cl.findDeclaredField(fieldName)
        return field ?: (cl.extendedClass?.type as ClassDeclaration)?.findFieldRecursively(fieldName)
    }

    def public static MethodDeclaration findGetterRecursively(ClassDeclaration cl, String fieldName) {
        // in some cases, there is no field, but only a getter/setter (for example for proxy methods
        // of embedded composite primary keys
        val method = cl.findDeclaredMethod("get" + fieldName.toFirstUpper)
        return method ?: (cl.extendedClass?.type as ClassDeclaration)?.findGetterRecursively(fieldName)
    }

    /** Creates a new list, consisting of the provided primary type reference, and adds optional additional list elements. */
    def static public List<TypeReference> andMaybeAsWell(TypeReference primary, Iterable<? extends TypeReference> maybeMore) {
        if (maybeMore === null)
            return #[ primary ]
        else
            return (#[ primary ] + maybeMore).toList
    }


    // common code extracted from AutoMap.xtend and AutoExtendMapper.xtend
    // this is used to determine if a field should be mapped.

    def private static boolean isInEntity(FieldDeclaration srcField, ClassDeclaration entity) {
        try {
            // run a second attempt, using the getter. This should cover enums as well as serialized fields
            val mm = entity.findDeclaredMethod('''get«srcField.simpleName.toFirstUpper»''')
            if (mm === null || mm.isStatic) {
                if (entity.extendedClass !== null)   // if there is a superclass, try that
                    return srcField.isInEntity(entity.extendedClass.type as ClassDeclaration)
                return false  // «fieldName» not found in target class or is static, and no superclass exists
            }
            //System::out.println('''«entity.simpleName»: Found «mm.simpleName» with type «mm.returnType.type», want «srcField.type»''')
            if (mm.returnType == srcField.type)
                return true
            /*
            val d = entity.findField(srcField.simpleName)

            if (d == null || d.isStatic) {
                if (entity.extendedClass !== null)   // if there is a superclass, try that
                    return srcField.isInEntity(entity.extendedClass as ClassDeclaration)
                return false  // «fieldName» not found in target class or is static, and no superclass exists
            }
            System::out.println('''«entity.simpleName»: Found «d.simpleName» with type «d.type», want «srcField.type»''')
            if (d.type == srcField.type)    // simple case, plain assignment
                return true  */
        } catch (Exception e) {
            System::out.println('''Exception «e» thrown for lookup of field «srcField.simpleName»''')
        }
        return false  // exists, but type differs, or got an exception
    }

    def static public toBeSkipped(FieldDeclaration f) {
        return f.annotations.exists[annotationTypeDeclaration.simpleName == "NoAutoMap"]  // findAnnotation(NoAutoMap) == null, but need newTypeReference for that
    }
    def static public CharSequence buildMapping(TypeReference dto, ClassDeclaration entityClass, String target, String src, boolean includeSuperClasses) {
        val dtoClass = dto.type as ClassDeclaration
        val superClassMapping = if (includeSuperClasses) (dtoClass.extendedClass?.buildMapping(entityClass, target, src, includeSuperClasses) ?: '''''') else ''''''
        return '''
            «superClassMapping»«dtoClass.declaredFields.filter[!isStatic && !toBeSkipped && isInEntity(entityClass)].map['''«target».set«simpleName.toFirstUpper»(«src».get«simpleName.toFirstUpper»());'''].join('\n')»
        '''
    }

    def static public exceptionCatcher(String f42Exception, String applException) '''
        } catch («applException» _e) {
            // convert ApplicationExceptions (most likely thrown by enums toToken()) into T9tExceptions
            if (_e instanceof «f42Exception»)
                throw («f42Exception»)_e;
            throw new «f42Exception»(«f42Exception».ENTITY_DATA_MAPPING_EXCEPTION, _e.toString());
        }
    '''

    def static public createCheckNoUpdateFields(Iterable<? extends FieldDeclaration> fields) {
        return fields.map[if (type.primitive) '''
            if (current.get«simpleName.toFirstUpper»() != intended.get«simpleName.toFirstUpper»())
                throw new T9tException(T9tException.FIELD_MAY_NOT_BE_CHANGED, "«simpleName»");
            ''' else '''
            if (current.get«simpleName.toFirstUpper»() == null ? (intended.get«simpleName.toFirstUpper»() != null) : !current.get«simpleName.toFirstUpper»().equals(intended.get«simpleName.toFirstUpper»()))
                throw new T9tException(T9tException.FIELD_MAY_NOT_BE_CHANGED, "«simpleName»");
            '''
        ].join
    }

    // return the interface as implemented by this class or a superclass
    def static public TypeReference findInterfaceRecursively(ClassDeclaration cd, String name, String name2) {
        val inClass = cd.implementedInterfaces.findFirst[type.simpleName == name || type.simpleName == name2]
        if (inClass === null)
            return (cd.extendedClass?.type as ClassDeclaration)?.findInterfaceRecursively(name, name2)
        if (inClass.actualTypeArguments.isEmpty)
           return null
        return inClass.actualTypeArguments.get(0)
    }

    // extension method to get all implemented interfaces of a type
    def static public void allImplementedInterfaces(TypeDeclaration typeDeclaration, Set<TypeReference> interfaces) {
        if (typeDeclaration === null) {
            return
        }

        if (typeDeclaration instanceof ClassDeclaration) {
            // add all interfaces directly implemented by this class
            typeDeclaration.implementedInterfaces.forEach [
                interfaces.add(it)
                // recurses to the interface
                allImplementedInterfaces(it.type as InterfaceDeclaration, interfaces)
            ]

            // recurse to the superclass if any
            if (typeDeclaration.extendedClass !== null) {
                allImplementedInterfaces(typeDeclaration.extendedClass?.type as ClassDeclaration, interfaces)
            }
        } else if (typeDeclaration instanceof InterfaceDeclaration) {
            // add all interfaces that are directly extended by this interface
            val extendedInterfaces = typeDeclaration.extendedInterfaces
            extendedInterfaces.forEach [
                interfaces.add(it)
                allImplementedInterfaces(it.type as InterfaceDeclaration, interfaces)
            ]
        }
    }

    def static public void injected(MutableFieldDeclaration fld, extension TransformationContext context) {
        // fld.addAnnotation(Inject.newAnnotationReference)
        fld.final = true
        fld.initializer = [ '''«toJavaCode(Jdp.newTypeReference)».getRequired(«toJavaCode(fld.type)».class)''' ]
    }

    def static public void injectedLazy(MutableFieldDeclaration fld, TypeReference target, extension TransformationContext context) {
        // fld.addAnnotation(Inject.newAnnotationReference)
        fld.final = true
        fld.initializer = [ '''new LazyInjection<«toJavaCode(target)»>(() -> «toJavaCode(Jdp.newTypeReference)».getRequired(«toJavaCode(target)».class))''' ]
    }

    def static public void removeAnnotation(MutableAnnotationTarget t, Type anno) {
        val myself = t.findAnnotation(anno)
        if (myself !== null)
            t.removeAnnotation(myself)
    }
}
