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

import com.arvatosystems.t9t.annotations.jpa.relations.OrderBy
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.jpa.IResolverCompositeKey42
import com.arvatosystems.t9t.base.jpa.IResolverLongKey42
import com.arvatosystems.t9t.base.jpa.IResolverNewCompositeKey42
import com.arvatosystems.t9t.base.jpa.IResolverStringKey42
import com.arvatosystems.t9t.base.jpa.IResolverSuperclassKey42
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverCompositeKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverLongKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverNewCompositeKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverStringKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverSuperclassKey42
import com.arvatosystems.t9t.base.jpa.impl.AbstractResolverSurrogateKey42
import com.google.common.collect.ImmutableList
import de.jpaw.bonaparte.api.CompositeKey
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.jpa.BonaKey
import de.jpaw.bonaparte.jpa.BonaPersistableKey
import de.jpaw.bonaparte.jpa.BonaPersistableTracking
import de.jpaw.bonaparte.jpa.BonaTracking
import de.jpaw.bonaparte.pojos.api.CompositeKeyRef
import de.jpaw.bonaparte.pojos.api.SortColumn
import de.jpaw.bonaparte.pojos.apiw.Ref
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.util.ArrayList
import java.util.Collection
import java.util.List
import javax.persistence.NoResultException
import javax.persistence.TypedQuery
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility

import static extension com.arvatosystems.t9t.annotations.jpa.Tools.*

@Active(AutoResolver42Processor) annotation AutoResolver42 {}

/** The automapper generates data copies for elements of same name and type. It can also apply lookups / resolvers. */
class AutoResolver42Processor extends AbstractClassProcessor {
    val resolverRevision = "2016-12-05 13:03 CET - added safeguard for empty IN parameters in findBy methods"

    def getResolverClassName(ClassDeclaration m, MethodDeclaration r) {
        return m.packageName + "impl." + r.simpleName.substring(3) + "Resolver"
    }
    def getResolverInterfaceName(ClassDeclaration m, MethodDeclaration r) {
        return m.packageName + "I" + r.simpleName.substring(3) + "Resolver"
    }
    def getResolverClassName(ClassDeclaration m, TypeReference r) {
        return m.packageName + "impl." + r.simpleName + "Resolver"
    }
    def getResolverInterfaceName(ClassDeclaration m, TypeReference r) {
        return m.packageName + "I" + r.simpleName + "Resolver"
    }

    /** Creates an interface and an implementation class for every method of the supported pattern. */
    override doRegisterGlobals(ClassDeclaration m, RegisterGlobalsContext context) {
        for (r: m.declaredMethods) {
            if (r.simpleName.startsWith("get")) {
                context.registerClass(m.getResolverClassName(r))
                context.registerInterface(m.getResolverInterfaceName(r))
            }
        }

    }

    def static void logme(CharSequence x) {
        System::out.println(x.toString)
    }

    override doTransform(MutableClassDeclaration m, extension TransformationContext context) {
        // annotations
        val orderByAnnoType = OrderBy.newTypeReference.type
        val globalTenantCanAccessAllAnnoType = GlobalTenantCanAccessAll.newTypeReference.type
        val allCanAccessGlobalTenantAnnoType = AllCanAccessGlobalTenant.newTypeReference.type
        val namedAnnoType = Named.newTypeReference.type
        val applicationScopedAnno = Singleton.newAnnotationReference
        val overrideAnno = Override.newAnnotationReference

        // interfaces
        val bonaType = BonaPortable.newTypeReference

        // classes
        val refType = Ref.newTypeReference
        val natKeyRef = CompositeKeyRef.newTypeReference
        val stringType = String.newTypeReference
        val longType = Long.newTypeReference
        val collectionType = Collection.newTypeReference
        val listType = List.newTypeReference
        val immutableListType = ImmutableList.newTypeReference
        val noResultExceptionType = NoResultException.newTypeReference
        val constantsType = T9tConstants.newTypeReference
        val sortColumnType = SortColumn.newTypeReference
        val sortOrderType = List.newTypeReference(sortColumnType)
        val sortOrderInitType = ArrayList.newTypeReference(sortColumnType)

        // We don't want this annotation on the java class (unwanted dependency)
        m.removeAnnotation(m.annotations.findFirst[annotationTypeDeclaration === AutoResolver.newTypeReference.type])

        for (r: m.declaredMethods) {
              val rt = r.returnType
              val rtElement = //rt.element
              //if (rt.simpleClass) rt else if (rt.type.simpleName == "java.util.List") rt.actualTypeArguments.head else null
            if (rt.simpleClass) rt else if (rt.type == listType.type) rt.actualTypeArguments.head else null

              if (rtElement === null) {
                  r.addError('''Return type of «r.simpleName» must be a class or List of it, found «rt.simpleName»''')
                  return
              }
              // plausi to check for overwriting
            if (m.simpleName == rtElement.simpleName + "Resolver") {
                r.addError("The generated class for this method could be overwritten by the container class stub. Rename the class from "
                    + m.simpleName + " to something else");
                return
            }

              // extract the implemented interface from the Entity class in order to find the type of the tracking columns class
            val entityClass = rtElement.type as ClassDeclaration
            val entityHasActiveField = entityClass.findFieldRecursively("isActive") !== null
            val injectedFieldName = rtElement.simpleName.toFirstLower + "Resolver"

            val myKeyType = entityClass.findInterfaceRecursively(BonaKey.simpleName, BonaPersistableKey.simpleName)
            val myTrackingType = entityClass.findInterfaceRecursively(BonaTracking.simpleName, BonaPersistableTracking.simpleName)

            if (myKeyType === null) {
                r.addError('''Entity type «entityClass.simpleName» must implement BonaKey<something>, found «entityClass.implementedInterfaces.map[type.simpleName].join(',')»''')
                return
            }
            if (myTrackingType === null) {
                r.addError('''Entity type «entityClass.simpleName» must implement BonaTracking<something>, found «entityClass.implementedInterfaces.map[simpleName].join(',')»''')
                return
            }
              // temporary debug info to very correct retrieval of generics type parameters
            if (r.simpleName.startsWith("get")) {
                if (r.simpleName.substring(3) != r.returnType.simpleName) {
                    r.addError("Method of name get... must match return type, found " + r.returnType.simpleName)
                    return  // stop activity
                }
                if (!rt.isSimpleClass) {
                    r.addError("Method of name get... must return a JPA entity, found " + rt)
                    return  // stop activity
                }
                // ensure the method has exactly two parameters, one which extends Ref, one which is a boolean
                if (r.parameters === null || r.parameters.size != 2) {
                    r.addError("Methods of name get... must have exactly two parameters.")
                    return  // stop activity
                }
                val source = r.parameters.get(0)
//                if (!source.type.isSimpleClass) {
//                    r.parameters.get(0).addError("First parameter of method named get... must be a class reference, found " + source.type)
//                    return  // stop activity
//                }
                val surrogateKey = refType.isAssignableFrom(source.type)
                val stringKey = stringType.isAssignableFrom(source.type)
                val longKey = longType.isAssignableFrom(source.type)
                val natKey = natKeyRef.isAssignableFrom(source.type)
                val newNatKey = CompositeKey.newTypeReference.isAssignableFrom(myKeyType)
                if (!bonaType.isAssignableFrom(source.type) && !stringKey && !longKey) {
                    r.parameters.get(0).addError("First parameter of method named get... must be a BonaPortable or a String or Long, found " + source.type)
                    return  // stop activity
                }
                val bool = r.parameters.get(1)
                if (!bool.type.primitive || bool.type.array || bool.type.name != "boolean") {
                    r.parameters.get(1).addError("Second parameter of method named get... must be a boolean, found " + bool.type.name)
                    return  // stop activity
                }

                // all good, I think
                val extendedResolverInterface = if (surrogateKey)
                        typeof(IResolverSurrogateKey42).newTypeReference(source.type, myTrackingType, rt)
                    else if (stringKey)
                        typeof(IResolverStringKey42).newTypeReference(myTrackingType, rt)
                    else if (longKey)
                        typeof(IResolverLongKey42).newTypeReference(myTrackingType, rt)
                    else if (natKey)
                        typeof(IResolverCompositeKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                    else if (newNatKey)
                        typeof(IResolverNewCompositeKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                    else
                        typeof(IResolverSuperclassKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                val extendedResolverClass = if (surrogateKey)
                        typeof(AbstractResolverSurrogateKey42).newTypeReference(source.type, myTrackingType, rt)
                    else if (stringKey)
                        typeof(AbstractResolverStringKey42).newTypeReference(myTrackingType, rt)
                    else if (longKey)
                        typeof(AbstractResolverLongKey42).newTypeReference(myTrackingType, rt)
                    else if (natKey)
                        typeof(AbstractResolverCompositeKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                    else if (newNatKey)
                        typeof(AbstractResolverNewCompositeKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                    else
                        typeof(AbstractResolverSuperclassKey42).newTypeReference(source.type, myKeyType, myTrackingType, rt)
                val resolverInterface = findInterface(m.getResolverInterfaceName(r))

                if (resolverInterface === null) {
                    r.addError('''Cannot get resolver interface «m.getResolverInterfaceName(r)», simple class=«r.returnType.isSimpleClass»''')
                    return
                }
                resolverInterface => [
                    visibility = Visibility::PUBLIC          // r.visibility // copy visibility from method
                    extendedInterfaces = #[ extendedResolverInterface ]
                    docComment = '''Generated by AutoResolver, revision «resolverRevision»'''
                ]
                val resolverClass = findClass(m.getResolverClassName(r))
                val hasNamedAnno = r.findAnnotation(namedAnnoType)

                if (resolverClass === null) {
                    r.addError('''Cannot get resolver class «m.getResolverClassName(r)»''')
                    return
                }
                resolverClass => [
                    visibility = Visibility::PUBLIC          // r.visibility // copy visibility from method
                    extendedClass = extendedResolverClass
                    implementedInterfaces = #[ resolverInterface.newTypeReference ]
                    addAnnotation(applicationScopedAnno)
                    docComment = '''
                        Generated by AutoResolver, revision «resolverRevision»
                     '''

                    if (r.findAnnotation(allCanAccessGlobalTenantAnnoType) !== null) {
                        addMethod("isTenantMeOrGlobal") [
                            visibility = Visibility::PUBLIC
                            returnType = primitiveBoolean
                            docComment = "Returns if the JPA Entity class allows view to current OR global tenant (latter for defaults)"
                            final = true
                            addAnnotation(overrideAnno)
                            body = [ '''
                                return true;
                            ''']
                        ]
                    }
                    if (r.findAnnotation(globalTenantCanAccessAllAnnoType) !== null) {
                        addMethod("globalTenantCanAccessAll") [
                            visibility = Visibility::PUBLIC
                            returnType = primitiveBoolean
                            docComment = "Returns if the JPA Entity class allows access to all tenants from the global admin"
                            final = true
                            addAnnotation(overrideAnno)
                            body = [ '''
                                return true;
                            ''']
                        ]
                    }
                    if (hasNamedAnno !== null) {  // @Named with explicit name: transfer existing anno including value
                        addAnnotation(hasNamedAnno)
//                    } else if (allBpm) {        // implicit @Named
//                        addAnnotation(namedAnno).set("value", rtElement.simpleName.toFirstLower + "Resolver")
                    }
                    if (entityClass.findFieldRecursively(T9tConstants.TENANT_REF_FIELD_NAME42) === null) {
                        addMethod("isTenantIsolated") [
                            visibility = Visibility::PUBLIC
                            returnType = primitiveBoolean
                            docComment = "Returns if the JPA Entity class contains a tenant reference / is tenant specific"
                            final = true
                            addAnnotation(overrideAnno)
                            body = [ '''
                                return false;
                            ''']
                        ]
                    } else {
                        // add methods to get and set a tenant ref
                        addMethod("getTenantRef") [
                            visibility = Visibility::PUBLIC
                            returnType = longType
                            addAnnotation(overrideAnno)
                            addParameter("entity", rt)
                            body = [ '''
                                return entity.getTenantRef();
                            ''']
                        ]
                        addMethod("setTenantRef") [
                            visibility = Visibility::PUBLIC
                            returnType = primitiveVoid
                            addAnnotation(overrideAnno)
                            addParameter("entity", rt)
                            addParameter(T9tConstants.TENANT_REF_FIELD_NAME42, longType)
                            body = [ '''
                                entity.setTenantRef(tenantRef);
                            ''']
                        ]
                    }
                    addMethod("getRtti") [
                        visibility = Visibility::PUBLIC
                        returnType = primitiveInt
                        docComment = "Returns the JPA Entity class's RTTI value"
                        final = true
                        addAnnotation(overrideAnno)
                        body = [ '''
                            return «toJavaCode(rt)».class$rtti();
                        ''']
                    ]
                    addMethod("getBaseJpaEntityClass") [
                        visibility = Visibility::PUBLIC
                        returnType = typeof(Class).newTypeReference(rt)
                        docComment = "Returns the JPA Entity base class"
                        final = true
                        addAnnotation(overrideAnno)
                        body = [ '''
                            return «toJavaCode(rt)».class;
                        ''']
                    ]
                    addMethod("getKeyClass") [
                        visibility = Visibility::PUBLIC
                        returnType = typeof(Class).newTypeReference(myKeyType)
                        docComment = "Returns the JPA Entity key class"
                        final = true
                        addAnnotation(overrideAnno)
                        body = [ '''
                            return «toJavaCode(myKeyType)».class;
                        ''']
                    ]
                    addMethod("getTrackingClass") [
                        visibility = Visibility::PUBLIC
                        returnType = typeof(Class).newTypeReference(myTrackingType)
                        docComment = "Returns the JPA Entity tracking data class"
                        final = true
                        addAnnotation(overrideAnno)
                        body = [ '''
                            return «toJavaCode(myTrackingType)».class;
                        ''']
                    ]
                if (entityClass.final) {
                    addMethod("newEntityInstance") [
                        visibility = Visibility::PUBLIC
                        returnType = rt
                        final = true
                        addAnnotation(overrideAnno)
                        docComment = "{@inheritDoc}"
                        body = [ '''
                            «toJavaCode(rt)» e = new «toJavaCode(rt)»();
                            setTenantRef(e, ctx.customization.getSharedTenantRef(getRtti()));
                            return e;
                        ''']
                    ]
                    addMethod("getEntityClass") [
                        visibility = Visibility::PUBLIC
                        returnType = Class.newTypeReference(rt)
                        final = true
                        addAnnotation(overrideAnno)
                        docComment = "{@inheritDoc}"
                        body = [ '''
                            return «toJavaCode(rt)».class;
                        ''']
                    ]
                }
                ]

                // add an injected field of the new interface type
                m.addField(injectedFieldName) [
                    visibility = Visibility::PROTECTED
                    type = resolverInterface.newTypeReference
                    injected(context)
                ]
                // refer to the injected resolver for this method's body
                r.body = [ '''return «injectedFieldName».getEntityData(«source.simpleName», «bool.simpleName»);''' ]

                if (surrogateKey) {
                    // create an additional method to get the reference only (similar to r)
                    m.addMethod("getRef") [
                        visibility = r.visibility // copy visibility from specified method
                        exceptions = r.exceptions
                        returnType = primitiveLong
                        final = true
                        addParameter(source.simpleName, source.type)
                        addParameter(bool.simpleName, bool.type)
                        body = [ '''return «injectedFieldName».getRef(«source.simpleName», «bool.simpleName»);''' ]
                    ]

                } else {
                    // add a static list which provides a default sort order, unless the key class is a wrapper of a primitive type (Long)
                    resolverClass.addField("DEFAULT_SORT_ORDER") [
                        visibility = Visibility::PRIVATE
                        static = true
                        type = sortOrderType
                        initializer = [ '''new «toJavaCode(sortOrderInitType)»()«IF (!myKeyType.isWrapper)»{{«makeInitializer(myKeyType.type as ClassDeclaration, toJavaCode(sortColumnType))»}}«ENDIF»''']
                    ]
                    resolverClass.addMethod("getDefaultSortColumns") [
                        addAnnotation(overrideAnno)
                        visibility = Visibility::PROTECTED
                        returnType = sortOrderType
                        docComment = '''{@inheritDoc}'''
                        body = [ '''return DEFAULT_SORT_ORDER;''']
                    ]
                }

            } else if (r.simpleName.startsWith("findBy")) {        // additional multi-parameter selects
                if (r.parameters === null || r.parameters.size < 1) {
                    r.addError("Methods of name findBy* must have at least one parameter.")
                    return  // stop activity
                }
                val bool = r.parameters.get(0)
                if (!bool.type.primitive || bool.type.array || bool.type != primitiveBoolean) {
                    r.addError("First parameter of method named findBy* must be a boolean, found " + bool.type.name)
                    return  // stop activity
                }
                r.abstract = false
                r.body = [ '''return «injectedFieldName».«r.simpleName»(«r.parameters.filter[needsParam].parameterList»);''' ]

                // extend the interface
                val resolverInterface = findInterface(m.getResolverInterfaceName(rtElement))
                resolverInterface.addMethod(r.simpleName) [
                    abstract   = true
                    visibility = Visibility::PUBLIC
                    returnType = rt
                    for (p : r.parameters)
                        if (p.needsParam)
                            addParameter(p.simpleName, p.type)
                ]

                // extend the resolver class
                val resolverClass = findClass(m.getResolverClassName(rtElement))
                val queryClass = typeof(TypedQuery).newTypeReference(rtElement.newWildcardTypeReference)
                val isExtraTenantRefRequired = r.parameters.filter[simpleName.equals(T9tConstants.TENANT_REF_FIELD_NAME42)].empty
                    && !(rtElement.type as ClassDeclaration).declaredMethods.filter[simpleName.equals("setTenantRef")].empty
                val wantsAccessToDefault = r.simpleName.endsWith("WithDefault")
                if (wantsAccessToDefault) {
                    if (!isExtraTenantRefRequired) {
                         r.addError("Methods with the reserved suffix WithDefault must have a tenantRef in the underlying entity, but may not specify it as parameter")
                         return;
                     }
                }
                resolverClass.addMethod(r.simpleName) [
                    addAnnotation(overrideAnno)
                    visibility = Visibility::PUBLIC
                    returnType = rt
                    val paramsList = new ArrayList<MutableParameterDeclaration> => [
                        addAll(r.parameters)
                    ]
                    val params = paramsList.subList(1, r.parameters.size)
                    for (p : params) {
                        val paramType = if (p.type.simpleClass || p.type.wrapper) p.type else if (p.type.type == listType.type) p.type.actualTypeArguments.head else p.type
                        if (p.simpleName.indexOf('_') < 0) {
                            // perform a plausibility check on the type, unless a subtype is referenced (plausi would be more complex here and is skipped atm)
                            val entityField = entityClass.findFieldRecursively(p.simpleName);
                            val getter = entityClass.findGetterRecursively(p.simpleName);
                            if (getter === null && entityField === null) {
                                p.addError('''Entity class «entityClass.simpleName» has no field named «p.simpleName»''')
                                return
                            }
                            val relevantType = if (entityField !== null) entityField.type else getter.returnType  // TODO: unsolved in cases an enum is part of the primary key....
                            if (relevantType != paramType) {
                                p.addError('''Entity class «entityClass.simpleName», field «p.simpleName» differs in type («relevantType» in the entity, «paramType» in parameter)''')
                                return
                            }
                        }
                    }
                    for (p : r.parameters)
                        if (p.needsParam)
                            addParameter(p.simpleName, p.type)
                    val hasOrderByAnno = r.findAnnotation(orderByAnnoType)
                    val orderByString =
                        if (hasOrderByAnno !== null)
                            ''' ORDER BY e.«hasOrderByAnno.getValue("value") as String»'''
                        else if (wantsAccessToDefault)
                            ''' ORDER BY e.tenantRef DESC'''       // have data of the global tenant last (it's the fallback)

                    body = [ '''
                        logFindBy("«r.simpleName»"); // call superclass because a) we do not have our own LOGGER and b) it is easier to configure this way
                        «IF !rt.simpleClass»
                            «FOR p: params.filter[collectionType.isAssignableFrom(type)]»
                                if («p.simpleName».isEmpty())
                                    return «toJavaCode(immutableListType)».of();
                            «ENDFOR»
                        «ENDIF»
                        String activeCondition = «IF entityHasActiveField»«bool.simpleName» ? " AND e.isActive = :isActive" : «ENDIF»"";
                        Class <? extends «toJavaCode(rtElement)»> entityClass = getEntityClass();
                        «toJavaCode(queryClass)» query = constructQuery(String.format(
                            "SELECT e FROM %s e WHERE «writeTenantCond(isExtraTenantRefRequired, wantsAccessToDefault, !params.empty)»«FOR p: params SEPARATOR ' AND '»«condition(p)»«ENDFOR»%s«orderByString»",
                            entityClass.getSimpleName(), activeCondition));
                        «IF entityHasActiveField»
                            if («bool.simpleName»)
                                query.setParameter("isActive", true);  // must set it via parameter because Oracle does not understand a verbatim "true"
                        «ENDIF»
                        «IF isExtraTenantRefRequired»
                            query.setParameter(«toJavaCode(constantsType)».TENANT_REF_FIELD_NAME42, getSharedTenantRef());
                            «IF wantsAccessToDefault»
                                query.setParameter("globalTenantRef", «toJavaCode(constantsType)».GLOBAL_TENANT_REF42);
                            «ENDIF»
                        «ENDIF»
                        «FOR p: params»
                            «IF p.needsParam»
                                query.setParameter("«p.simpleName»", «p.simpleName»);
                            «ENDIF»
                        «ENDFOR»
                        «IF rt.simpleClass»
                            try {
                                return query.getSingleResult();
                            } catch («toJavaCode(noResultExceptionType)» ex) {
                                return null;
                            }
                        «ELSE»
                            return («toJavaCode(rt)»)query.getResultList();
                        «ENDIF»
                    ''']
                ]
            } else {
                r.addError("method names should equal 'getEntityData' or start with 'findBy'")
            }
        }
        // add an "ApplicationScoped" annotation to the original class, as well as @Deprecated
        m.addAnnotation(applicationScopedAnno)
        // m.addAnnotation(deprecatedType.type)
        // add a comment, that this is just a collection class
        m.docComment = '''This is just a code generator utility class. Most likely you want to use the classes referenced in here, instead of this one.'''
    }

    def static private writeTenantCond(boolean isExtraTenantRefRequired, boolean wantsAccessToDefault, boolean more) {
        val andStr = if (more) " AND " else "";
        return if (isExtraTenantRefRequired) {
            if (wantsAccessToDefault)
                "e.tenantRef IN (:tenantRef, :globalTenantRef)" + andStr
            else
                "e.tenantRef=:tenantRef" + andStr
        } else {
            ""
        }
    }

//    def static private getElement(TypeReference r) {
//        if (r.simpleClass || r.wrapper) r else if (r.type.simpleName == "java.util.List") r.actualTypeArguments.head else null
//    }

    def static private getRelation(MutableParameterDeclaration p) {
        for (u : p.annotations) {
            switch u.annotationTypeDeclaration.simpleName {
            case 'GreaterThan':     return '>'
            case 'LessThan':        return '<'
            case 'GreaterEquals':   return '>='
            case 'LessEquals':      return '<='
            case 'NotEqual':        return '!='
            case 'Like':            return 'LIKE'
            case 'IsNotNull':       return 'IS NOT NULL'
            case 'IsNull':          return 'IS NULL'
            default:                return '='  // only ever use first annotation!
            }
        }
        return '='
    }

    def static private needsParam(MutableParameterDeclaration p) {
        for (u : p.annotations) {
            switch u.annotationTypeDeclaration.simpleName {
            case 'IsNotNull':       return false
            case 'IsNull':          return false
            default:                return true  // only ever use first annotation!
            }
        }
        return true
    }

    def private condition(MutableParameterDeclaration p) {
        val eParam = p.simpleName.replaceAll('_', '.')
        return if (p.type.simpleClass || p.type.wrapper)
            '''e.«eParam» «p.relation»«IF p.needsParam» :«p.simpleName»«ENDIF»'''
        else
            '''e.«eParam» IN :«p.simpleName»'''
    }

    def private static makeInitializer(ClassDeclaration cl, String sortType) '''
        /* fields of key «cl.simpleName» */
        «FOR f: cl.declaredFields.filter[!static && !simpleName.startsWith("_")]»
           add(new «sortType»("«f.simpleName»", true));
        «ENDFOR»
    '''
}
