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

import com.arvatosystems.t9t.annotations.InitializeLazy
import com.arvatosystems.t9t.annotations.NotUpdatable
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyRequest
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyResponse
import com.arvatosystems.t9t.base.crud.CrudStringKeyRequest
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse
import com.arvatosystems.t9t.base.crud.CrudSuperclassKeyRequest
import com.arvatosystems.t9t.base.crud.CrudSuperclassKeyResponse
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.crud.RefResolverResponse
import com.arvatosystems.t9t.base.jpa.IEntityMapper42
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudCompositeKey42RequestHandler
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudStringKey42RequestHandler
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSuperclassKey42RequestHandler
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler
import com.arvatosystems.t9t.base.jpa.impl.AbstractEntityMapper42
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.impl.LazyInjection
import de.jpaw.bonaparte.jpa.BonaKey
import de.jpaw.bonaparte.jpa.BonaPersistableKey
import de.jpaw.bonaparte.jpa.BonaPersistableTracking
import de.jpaw.bonaparte.jpa.BonaTracking
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.apiw.Ref
import de.jpaw.dp.Singleton
import java.util.HashMap
import java.util.HashSet
import java.util.List
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility

import static extension com.arvatosystems.t9t.annotations.jpa.Tools.*

@Active(AutoMap42Processor) annotation AutoMap42 {}

/** The automapper generates data copies for elements of same name and type only. Everything else must be handcoded. */
class AutoMap42Processor extends AbstractClassProcessor {
    static private final String REQUEST_PACKAGE_COMPONENT = "be.request"        // the piece after module...
    static private final String REQUEST_CRUD    = "CrudRequest"
    static private final String REQUEST_READALL = "ReadAllRequest"
    static private final String REQUEST_RESOLVE = "ResolverRequest"
    static private final String REQUEST_SEARCH  = "SearchRequest"
    static private final String HANDLER         = "Handler"
    val mapperRevision = "2017-01-31 16:07 CET (Xtend 2.10.0, Java 8, SearchSuperclass)"

    def getMapperClassName(ClassDeclaration m, String r) {
        return m.packageName + "impl." + r + "Mapper"
    }
    def getMapperInterfaceName(ClassDeclaration m, String r) {
        return m.packageName + "I" + r + "Mapper"
    }
    def getMapperClassName(ClassDeclaration m, TypeReference r) {
        return m.packageName + "impl." + r.simpleName + "Mapper"
    }
    def getMapperInterfaceName(ClassDeclaration m, TypeReference r) {
        return m.packageName + "I" + r.simpleName + "Mapper"
    }

    def private static getRequestHandlerPackageName(ClassDeclaration c) {
        return c.packageName.substring(0, c.packageName.lastIndexOf('.', c.packageName.length-11)+1) + REQUEST_PACKAGE_COMPONENT + "."
    }

    /** Creates an interface and an implementation class for every method of the supported pattern. */
    override doRegisterGlobals(ClassDeclaration c, RegisterGlobalsContext context) {
        val autoHandlerAnnoFQN = AutoHandler.canonicalName
        // for the package name, strip jpa.mapping (11 characters)
        val requestHandlerPackageName = c.getRequestHandlerPackageName
        val allDtos = new HashSet<String>(20);
        val allHandlers = new HashMap<String,String>(20);
        c.declaredMethods.filter[simpleName.startsWith("e2d") || simpleName.startsWith("d2e")].forEach[
            allDtos.add(simpleName.substring(3))
            val anno = annotations.findFirst[annotationTypeDeclaration.qualifiedName == autoHandlerAnnoFQN]
            if (anno !== null) {
                allHandlers.put(simpleName.substring(3), anno.getValue("value") as String)  // remember which classes to create
            }
        ]
        for (d : allDtos) {
            context.registerClass(c.getMapperClassName(d))
            context.registerInterface(c.getMapperInterfaceName(d))
            val autoHandler = allHandlers.get(d)
            if (autoHandler !== null) {
                val lastDot = autoHandler.lastIndexOf(".")
                val classNameComponent = if (d.endsWith("DTO") || d.endsWith("Dto")) d.substring(0, d.length-3) else d  // skip an optional ending
                val pkgName = if (lastDot < 0) requestHandlerPackageName else autoHandler.substring(0, lastDot+1)
                if (autoHandler.indexOf("C", lastDot+1) >= 0)
                    context.registerClass(pkgName + classNameComponent + REQUEST_CRUD + HANDLER)
                if (autoHandler.indexOf("A", lastDot+1) >= 0)
                    context.registerClass(pkgName + classNameComponent + REQUEST_READALL + HANDLER)
                if (autoHandler.indexOf("R", lastDot+1) >= 0)
                    context.registerClass(pkgName + classNameComponent + REQUEST_RESOLVE + HANDLER)
                if (autoHandler.indexOf("S", lastDot+1) >= 0)
                    context.registerClass(pkgName + classNameComponent + REQUEST_SEARCH + HANDLER)
                if (autoHandler.indexOf("J", lastDot+1) >= 0)
                    context.registerClass(pkgName.substring(0, pkgName.length - 11) + "jpa.impl." + classNameComponent + "JpaResolver")
            }
        }
    }


    def getter(String name) '''get«name.toFirstUpper»()'''

    def resolverClassName(String name) {
        return name.substring(0, name.length-3).toFirstUpper + 'Resolver'
    }

    override doTransform(MutableClassDeclaration c, extension TransformationContext context) {
        val allDtos = new HashMap<TypeReference,TypeReference>(20);  // map DTO to entity
        val allHandlers = new HashMap<TypeReference,String>(20);  // map DTO to request handler creation instructions
        val entity2DtoMap = new HashMap<TypeReference,MutableMethodDeclaration>(20)
        val dto2EntityMap = new HashMap<TypeReference,MutableMethodDeclaration>(20)
        val collection2ListMap = new HashMap<TypeReference,MutableMethodDeclaration>(20)
        val dtoMapperNeedInjectors = new HashSet<TypeReference>(20)

        // classes
        val t9tException = T9tException.newTypeReference
        val stringType = String.newTypeReference
        val longType = Long.newTypeReference
        val refType = Ref.newTypeReference
        val operationTypeType = OperationType.newTypeReference

        val overrideAnno = Override.newAnnotationReference
        val deprecatedAnno = Deprecated.newAnnotationReference
        val applicationScopedAnno = Singleton.newAnnotationReference
        val noUpdateAnnoType = NotUpdatable.newTypeReference.type
        val needMappingAnnoType = NeedMapping.newTypeReference.type
        val autoHandlerAnnoType = AutoHandler.newTypeReference.type
        val requireLazyAnnoType = InitializeLazy.newTypeReference.type

        // We don't want this annotation on the java class (unwanted dependency)
        c.removeAnnotation(c.annotations.findFirst[annotationTypeDeclaration === AutoMap.newTypeReference.type])

        // step 0: look for entity resolver reference
        val entityResolver = c.declaredFields.head
        // for now, as long as we don't support extension, it must be there!
        if (entityResolver === null) {
            c.addError("Classes annotated with @AutoMap must have a declared field for the entityResolver")
            return
        }

        // Step 1: collect all data and store references to both specified mappers
        for (r: c.declaredMethods) {
            if (r.parameters.size >= 2 && r.returnType == primitiveVoid) {
                val TypeReference entity = r.parameters.get(0).type
                val TypeReference dto = r.parameters.get(1).type
                if (r.simpleName.startsWith("c2l")) {
                    collection2ListMap.put(dto, r)
                } else {
                    if (entity.isSimpleClass && dto.isSimpleClass) {
                        if (r.simpleName.startsWith("e2d")) {
                            entity2DtoMap.put(dto, r)
                            val ah = r.annotations.findFirst[annotationTypeDeclaration == autoHandlerAnnoType]
                            if (ah !== null)
                                allHandlers.put(dto, ah.getValue("value") as String)
                        } else if (r.simpleName.startsWith("d2e")) {
                            dto2EntityMap.put(dto, r)
                            val ah = r.annotations.findFirst[annotationTypeDeclaration == autoHandlerAnnoType]
                            if (ah !== null)
                                allHandlers.put(dto, ah.getValue("value") as String)
                        } else {
                            r.addError("Method of bad name: must be either e2d... or d2e... or c2l..., found " + r.simpleName);
                            return;
                        }
                        if (r.simpleName.substring(3) != dto.simpleName) {
                            r.addError('''Bad name for «r.simpleName»: should be ???«dto.simpleName»''')
                            return
                        }
                        // plausi to check for overwriting
                        if (c.simpleName == dto.simpleName + "Mapper") {
                            r.addError("The generated class for this method could be overwritten by the container class stub. Rename the class from "
                                + c.simpleName + " to something else");
                            return
                        }
                        val previousType = allDtos.put(dto, entity)
                        if (previousType !== null && previousType != entity) {
                            c.addError('''Conflicting types for mapping of DTO «dto.simpleName» and entity «entity.simpleName»''')
                            return
                        }
                        // look for annotation. if present, mark the DTO mapper as "require injectors"
                        if (r.findAnnotation(needMappingAnnoType) !== null)
                            dtoMapperNeedInjectors.add(dto)
                    } else {
                        c.addError('''
                            Classes annotated with @AutoMap may only contain parameters of simple type,
                            offending method «r.simpleName» has «r.parameters.size» parameters: «entity.simpleName»: «entity.isSimpleClass» and «dto.simpleName»: «dto».isSimpleClass»
                        ''')
                        return;
                    }
                }
            } else {
                c.addError('''Classes annotated with @AutoMap may only contain methods of return type void with 2 parameters, offending method «r.simpleName» has «r.parameters.size» parameters»''')
                return;
            }
            r.visibility = Visibility::PROTECTED  // avoid warning!
        }
        // if we get here, there are no conflicts, and we have a map of all DTO types and their associated JPA entity types

        // Step 2: create the required interfaces, if this class does not extend some other, which contains a mapping already for this DTO
        allDtos.forEach [ dto, entity |
            val dtoClass = dto.type as ClassDeclaration
            val entityClass = entity.type as ClassDeclaration
            val myKeyType = entityClass.findInterfaceRecursively(BonaKey.simpleName, BonaPersistableKey.simpleName)
            val myTrackingType = entityClass.findInterfaceRecursively(BonaTracking.simpleName, BonaPersistableTracking.simpleName)

            if (myKeyType === null) {
                c.addError('''Entity type «entityClass.simpleName» must implement BonaKey<something>''')
                return
            }
            if (myTrackingType === null) {
                c.addError('''Entity type «entityClass.simpleName» must implement BonaTracking<something>''')
                return
            }
            val extendedAbstractInterface = IEntityMapper42.newTypeReference(myKeyType, dto, myTrackingType, entity)
            val extendedMapperClass = AbstractEntityMapper42.newTypeReference(myKeyType, dto, myTrackingType, entity)
            val myInterface = findInterface(c.getMapperInterfaceName(dto))
            myInterface => [
                visibility = Visibility::PUBLIC
                extendedInterfaces = #[ extendedAbstractInterface ]
                docComment = '''Generated by AutoMap, revision «mapperRevision»'''
            ]
            val myClass = findClass(c.getMapperClassName(dto))
            myClass => [
                visibility = Visibility::PUBLIC         // r.visibility // copy visibility from method
                extendedClass = extendedMapperClass
                implementedInterfaces = #[ myInterface.newTypeReference ]
                addAnnotation(applicationScopedAnno)
                docComment = '''Generated by AutoMap, revision «mapperRevision»'''

                // copy declared fields, and add injection for them, but only if we're on the main DTO, not for final "Key-only" DTOs
                if (dtoClass.final && !dtoMapperNeedInjectors.contains(dto)) {
                    // just do it for the main entity injector
                    addField(c.declaredFields.head.simpleName) [
                        visibility = Visibility::PROTECTED
                        injected(context)
                        type = c.declaredFields.head.type
                    ]
                } else {
                    c.declaredFields.forEach [ f |
                        if (f.findAnnotation(requireLazyAnnoType) !== null) {
                            addField(f.simpleName + "Provider") [
                                visibility = Visibility::PROTECTED
                                injectedLazy(f.type, context)
                                type = LazyInjection.newTypeReference(f.type)
                            ]
                        } else {
                            addField(f.simpleName) [
                                visibility = Visibility::PROTECTED
                                injected(context)
                                type = f.type
                            ]
                        }
                    ]
                }

                if (entityClass.findFieldRecursively(T9tConstants.TENANT_REF_FIELD_NAME42) !== null) {
                    // add methods to get and set a tenant ref
                    addMethod("getTenantRef") [
                        visibility = Visibility::PUBLIC
                        returnType = longType
                        addAnnotation(overrideAnno)
                        addParameter("entity", entity)
                        body = [ '''
                            return entity.getTenantRef();
                        ''']
                    ]
                    addMethod("setTenantRef") [
                        visibility = Visibility::PUBLIC
                        returnType = primitiveVoid
                        addAnnotation(overrideAnno)
                        addParameter("entity", entity)
                        addParameter(T9tConstants.TENANT_REF_FIELD_NAME42, longType)
                        body = [ '''
                            entity.setTenantRef(tenantRef);
                        ''']
                    ]
                }
                addMethod("getRtti") [
                    visibility = Visibility::PUBLIC
                    returnType = primitiveInt
                    final = true
                    addAnnotation(overrideAnno)
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        return «toJavaCode(dto)».class$rtti();
                    ''']
                ]
                addMethod("getProperty") [
                    visibility = Visibility::PUBLIC
                    returnType = stringType
                    final = true
                    addAnnotation(overrideAnno)
                    addParameter("_propname", stringType)
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        return «toJavaCode(dto)».BClass.getInstance().getProperty(_propname);
                    ''']
                ]
                addMethod("getBaseDtoClass") [
                    visibility = Visibility::PUBLIC
                    returnType = Class.newTypeReference(dto)
                    final = true
                    addAnnotation(overrideAnno)
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        return «toJavaCode(dto)».class;
                    ''']
                ]
                // now the specific mapping methods
                addMethod("mapToDto") [
                    visibility = Visibility::PUBLIC
                    returnType = dto
                    final = true
                    addAnnotation(overrideAnno)
                    addParameter("entity", entity)
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        if (entity == null)
                            return null;
                        «toJavaCode(dto)» dto = fromCache(entity, «toJavaCode(dto)».class);
                        if (dto != null)
                            return dto;
                        «IF dtoClass.final»
                            dto = new «toJavaCode(dto)»();
                        «ELSE»
                            // dto = contextProvider.get().customization.newDtoInstance(getRtti(), getBaseDtoClass());
                            dto = newDtoInstance();
                        «ENDIF»
                        entity2dto(entity, dto);
                        toCache(entity, «toJavaCode(dto)».class, dto);
                        return dto;
                    ''']
                ]
                // possibly the special collection mapper method...
                val collRef = List.newTypeReference(dto)
                val collMapper = collection2ListMap.get(collRef)
                if (collMapper !== null) {
                    // create special collection mapper methods...
                    addMethod("haveCollectionToDtoMapper") [
                        visibility = Visibility::PROTECTED
                        returnType = primitiveBoolean
                        final = false
                        addAnnotation(overrideAnno)
                        docComment = "{@inheritDoc}"
                        body = [ '''
                            return true;
                        ''']
                    ]
                    addMethod("batchMapToDto") [
                        visibility = Visibility::PROTECTED
                        returnType = primitiveVoid
                        final = false
                        addAnnotation(overrideAnno)
                        xferParameters(collMapper)     // use the names provided in the xtend source
                        docComment = collMapper.docComment
                        body = collMapper.body
                    ]
                }
                if (dtoClass.final) {
                    addMethod("newDtoInstance") [
                        visibility = Visibility::PUBLIC
                        returnType = dto
                        final = true
                        addAnnotation(overrideAnno)
                        docComment = "{@inheritDoc}"
                        body = [ '''
                            return new «toJavaCode(dto)»();
                        ''']
                    ]
                    addMethod("getDtoClass") [
                        visibility = Visibility::PUBLIC
                        returnType = Class.newTypeReference(dto)
                        final = true
                        addAnnotation(overrideAnno)
                        docComment = "{@inheritDoc}"
                        body = [ '''
                            return «toJavaCode(dto)».class;
                        ''']
                    ]
                }
                addMethod("mapToDto") [
                    visibility = Visibility::PUBLIC
                    returnType = dto
                    final = true
                    addAnnotation(overrideAnno)
                    addParameter("key", myKeyType) // not typeof(Long).newTypeReference), that's only valid for artificial keys
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        if (key == null)
                            return null;
                        «toJavaCode(entity)» entity = «entityResolver.simpleName».find(key);
                        if (entity == null)
                            throw new «toJavaCode(t9tException)»(«toJavaCode(t9tException)».RECORD_DOES_NOT_EXIST, «entityResolver.simpleName».entityNameAndKey(key));
                        return mapToDto(entity);
                    ''']
                ]
                addMethod("mapToEntity") [
                    visibility = Visibility::PUBLIC
                    returnType = entity
                    final = true
                    addAnnotation(overrideAnno)
                    addParameter("dto", dto)
                    addParameter("onlyActive", primitiveBoolean)
                    docComment = "{@inheritDoc}"
                    body = [ '''
                        if (dto == null)
                            return null;
                        «toJavaCode(entity)» entity = «entityResolver.simpleName».newEntityInstance();
                        dto2entity(entity, dto, onlyActive);
                        return entity;
                    ''']
                ]

                // mapping direction 1 BEGIN
                {
                val suppliedMapping = entity2DtoMap.get(dto)
                // create a private method and move the code there
                addMethod("_entity2dto") [
                    visibility = Visibility::PRIVATE
                    returnType = primitiveVoid
                    if (suppliedMapping !== null) {
                        if (suppliedMapping.exceptions !== null) exceptions = suppliedMapping.exceptions
                        xferParameters(suppliedMapping)     // use the names provided in the xtend source
                        docComment = suppliedMapping.docComment
                        body = suppliedMapping.body
                    } else {
                        addParameter("entity", entity);     // use default names
                        addParameter("dto", dto);
                        body = [ '''''' ]
                    }
                ] // myClass
                addMethod("entity2dto") [
                    visibility = Visibility::PROTECTED
                    returnType = primitiveVoid
                    if (suppliedMapping?.exceptions !== null) exceptions = suppliedMapping?.exceptions
                    addParameter("entity", entity);
                    addParameter("dto", dto);
                    docComment = '''convert '''
                    body = [ '''
                        «dto.buildMapping(entityClass, "dto", "entity", true)»
                        _entity2dto(entity, dto);
                    ''' ]
                ]
                if (suppliedMapping !== null) {
                    suppliedMapping.body = ['''''']
                }
                }
                // mapping direction 1 END                {
                // mapping direction 2 BEGIN
                {
                val suppliedMapping = dto2EntityMap.get(dto)
                // create a private method and move the code there, call it
                addMethod("_dto2entity") [
                    visibility = Visibility::PRIVATE
                    returnType = primitiveVoid
                    if (suppliedMapping !== null) {
                        if (suppliedMapping.exceptions !== null) exceptions = suppliedMapping.exceptions
                        xferParameters(suppliedMapping)
                        docComment = suppliedMapping.docComment
                        body = suppliedMapping.body
                    } else {
                        addParameter("entity", entity);     // use default names
                        addParameter("dto", dto);
                        addParameter("onlyActive", primitiveBoolean)
                        body = [ '''''' ]
                    }
                ]
                addMethod("dto2entity") [
                    visibility = Visibility::PROTECTED
                    returnType = primitiveVoid
                    addAnnotation(overrideAnno) // is declared abstract in abstract superclass
                    if (suppliedMapping?.exceptions !== null) exceptions = suppliedMapping?.exceptions
                    addParameter("entity", entity);
                    addParameter("dto", dto);
                    addParameter("onlyActive", primitiveBoolean)
                    docComment = '''convert '''
                    body = [ '''
                        «IF dtoClass.findFieldRecursively(T9tConstants.TENANT_ID_FIELD_NAME) === null && entityClass.findFieldRecursively(T9tConstants.TENANT_ID_FIELD_NAME) !== null»
                            // tenantId not contained in DTO, but in entity, set it via environment
                            entity.setTenantId(«entityResolver.simpleName».getSharedTenantId());
                        «ENDIF»
                        «dto.buildMapping(entityClass, "entity", "dto", true)»
                        _dto2entity(entity, dto, onlyActive);
                    ''' ]
                ]
                if (suppliedMapping !== null) {
                    // the current method needs a body just to become valid (we don't use that class)
                    suppliedMapping.body = ['''''']
                }
                }
                // mapping direction 2 END                {

                if (!dtoClass.final) {  // for final classes we assume they are a key only and not used for CRUD operations
                    // exclude Ref fields, because we now compare DTO with entity, and the Ref will be of different type (Long) on the entity
                    val fieldsToCheck = dtoClass.declaredFields.filter[findAnnotation(noUpdateAnnoType) !== null && !refType.isAssignableFrom(type)]
                    if (!fieldsToCheck.isEmpty) {
                        // check for fields with notupdatable. If any exists, overwrite method checkNoUpdateFields
                        addMethod("checkNoUpdateFields") [
                            visibility = Visibility::PUBLIC
                            returnType = primitiveVoid
                            addAnnotation(overrideAnno)
                            docComment = "{@inheritDoc}"
                            addParameter("current", entity);
                            addParameter("intended", dto);
                            body = [
                                fieldsToCheck.createCheckNoUpdateFields
                            ]
                        ]
                    }
                }

                // create the CARS if required
                val autoHandler = allHandlers.get(dto)
                if (autoHandler !== null) {
                    val d = dto.simpleName
                    val classNameComponent = if (d.endsWith("DTO") || d.endsWith("Dto")) d.substring(0, d.length-3) else d  // skip an optional ending
                    val lastDot = autoHandler.lastIndexOf(".")
                    val rqPkgName = if (lastDot < 0) dto.type.qualifiedName.substring(0, dto.type.qualifiedName.length - dto.type.simpleName.length) + "request." else autoHandler.substring(0, lastDot+1)
                    val rqhPkgName = c.getRequestHandlerPackageName

                    if (autoHandler.indexOf("J", lastDot+1) >= 0) {
                        // TODO...
//                        val servicePkgName = rqPkgName.substring(0, rqPkgName.length - 8) + "services"
                    }
                    if (autoHandler.indexOf("S", lastDot+1) >= 0) {
                        val requestClassTypeRef = getRequestClassTypeRef(c, rqPkgName, classNameComponent + REQUEST_SEARCH, context)
                        if (requestClassTypeRef !== null) {
                            createHandler(c, rqhPkgName, requestClassTypeRef, AbstractSearchRequestHandler.newTypeReference(requestClassTypeRef), myInterface.newTypeReference, context, false) => [
                                returnType = ReadAllResponse.newTypeReference(dto, myTrackingType)
                                body = [ '''
                                    «IF autoHandler.indexOf("P", lastDot+1) >= 0»
                                        mapper.processSearchPrefixForDB(request);       // convert the field with searchPrefix
                                    «ENDIF»
                                    return mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());'''
                                ]
                            ]
                        }
                    }
                    if (autoHandler.indexOf("A", lastDot+1) >= 0) {
                        val requestClassTypeRef = getRequestClassTypeRef(c, rqPkgName, classNameComponent + REQUEST_READALL, context)
                        if (requestClassTypeRef !== null) {
                            createHandler(c, rqhPkgName, requestClassTypeRef, AbstractRequestHandler.newTypeReference(requestClassTypeRef), myInterface.newTypeReference, context, false) => [ m |
                                m.returnType = ReadAllResponse.newTypeReference(dto, myTrackingType)
                                m.body = [ '''
                                    «toJavaCode(m.returnType)» rs = new «toJavaCode(m.returnType)»();
                                    rs.setDataList(mapper.mapListToDwt(resolver.readAll(request.getReturnOnlyActive())));
                                    rs.setReturnCode(0);
                                    return rs;'''
                                ]
                            ]
                        }
                    }
                    if (autoHandler.indexOf("R", lastDot+1) >= 0) {
                        val requestClassTypeRef = getRequestClassTypeRef(c, rqPkgName, classNameComponent + REQUEST_RESOLVE, context)
                        if (requestClassTypeRef !== null) {
                            createHandler(c, rqhPkgName, requestClassTypeRef, AbstractRequestHandler.newTypeReference(requestClassTypeRef), null, context, false) => [ m |
                                m.returnType = RefResolverResponse.newTypeReference
                                m.body = [ '''
                                    Long ref = resolver.getRef(request.getRef(), false);
                                    «toJavaCode(m.returnType)» resp = new «toJavaCode(m.returnType)»();
                                    resp.setKey(ref);
                                    resp.setReturnCode(0);
                                    return resp;'''
                                ]
                            ]
                        }
                    }
                    if (autoHandler.indexOf("C", lastDot+1) >= 0) {
                        val invalidateCache = autoHandler.indexOf("I", lastDot+1) >= 0
                        val requestClassTypeRef = getRequestClassTypeRef(c, rqPkgName, classNameComponent + REQUEST_CRUD, context)
                        if (requestClassTypeRef !== null) {
                            val parentOfRq = ((requestClassTypeRef.type) as ClassDeclaration).extendedClass
                            if (parentOfRq === null || parentOfRq.actualTypeArguments.get(0) === null || parentOfRq.actualTypeArguments.get(1) === null) {
                                c.addError("The request class " + requestClassTypeRef.simpleName + " is not inherited or the parent specifies less than 2 generic parameters")
                            } else {
                                val p0 = parentOfRq.actualTypeArguments.get(0)
                                val p1 = parentOfRq.actualTypeArguments.get(1)
                                var TypeReference myParentClass = null
                                var TypeReference myReturnType = null
                                if (CrudSurrogateKeyRequest.newTypeReference.isAssignableFrom(requestClassTypeRef)) {
                                    // artificial key: REF, DTO, TRACKING, REQUEST, ENTITY...
                                    myParentClass = AbstractCrudSurrogateKey42RequestHandler.newTypeReference(p0, dto, myTrackingType, requestClassTypeRef, entity)
                                    myReturnType = CrudSurrogateKeyResponse.newTypeReference(dto, myTrackingType)
                                } else if (CrudCompositeKeyRequest.newTypeReference.isAssignableFrom(requestClassTypeRef)) {
                                    // artificial key: KEY, DTO, TRACKING, REQUEST, ENTITY...
                                    myParentClass = AbstractCrudCompositeKey42RequestHandler.newTypeReference(p0, dto, myTrackingType, requestClassTypeRef, entity)
                                    myReturnType = CrudCompositeKeyResponse.newTypeReference(p0, dto, myTrackingType)
                                } else if (CrudStringKeyRequest.newTypeReference.isAssignableFrom(requestClassTypeRef)) {
                                    // String key: DTO, TRACKING, REQUEST, ENTITY...
                                    myParentClass = AbstractCrudStringKey42RequestHandler.newTypeReference(dto, myTrackingType, requestClassTypeRef, entity)
                                    myReturnType = CrudStringKeyResponse.newTypeReference(dto, myTrackingType)
                                } else if (CrudSuperclassKeyRequest.newTypeReference.isAssignableFrom(requestClassTypeRef)) {
                                    // BonaPortable key: REF, KEY, DTO, TRACKING, REQUEST, ENTITY...
                                    myParentClass = AbstractCrudSuperclassKey42RequestHandler.newTypeReference(p0, p1, dto, myTrackingType, requestClassTypeRef, entity)
                                    myReturnType = CrudSuperclassKeyResponse.newTypeReference(p1, dto, myTrackingType)  // 3 params!!!
                                } else {
                                    c.addError("Unknown CRUD parent for " + requestClassTypeRef.simpleName);
                                }
                                if (myReturnType !== null) {
                                    // one of the optional was valid
                                    val myFinalReturnType = myReturnType
                                    createHandler(c, rqhPkgName, requestClassTypeRef, myParentClass, myInterface.newTypeReference, context, invalidateCache) => [
                                        returnType = myFinalReturnType
                                        body = [ '''
                                            «IF invalidateCache»
                                                if (request.getCrud() != «toJavaCode(operationTypeType)».READ)
                                                    executor.clearCache(«toJavaCode(dto)».class.getSimpleName(), null);
                                            «ENDIF»
                                            return execute(mapper, resolver, request);'''
                                        ]
                                    ]
                                }
                            }
                        }
                    }
                }
            ] // myClass
        ] // alldtos.foreach

        for (f: c.declaredFields) {
            f.remove
        }
        for (m: c.declaredMethods) {
            m.remove
        }
        // this class is no longer deprecated
        c.addAnnotation(deprecatedAnno)
        // add a comment, that this is just a collection class
        c.docComment = '''This is just a code generator utility class and not intended to be used in productive code.'''

    }

    def private getRequestClassTypeRef(MutableClassDeclaration c, String rqPkgName, String rqClassSimpleName, extension TransformationContext context) {
        val rqClass = findTypeGlobally(rqPkgName + rqClassSimpleName)
        if (rqClass === null) {
            c.addError("Cannot find request class " + rqPkgName + rqClassSimpleName)
            return null
        }
        return rqClass.newTypeReference
    }

    def private MutableMethodDeclaration createHandler(MutableClassDeclaration c, String rqhPkgName, TypeReference rqClassRef, TypeReference parent,
        TypeReference mapperType, extension TransformationContext context, boolean invalidateCache) {

        val rqhClass = findClass(rqhPkgName + rqClassRef.simpleName + "Handler")
        if (rqhClass === null) {
            c.addError("Cannot find request class " + rqhPkgName + rqClassRef.simpleName + "Handler")
            return null
        }
        rqhClass => [
            extendedClass = parent
            visibility = Visibility::PUBLIC
//            addAnnotation(Singleton.newAnnotationReference)   // not required (nor desired) in T9t!
            docComment = '''Generated by AutoMap, revision «mapperRevision»'''

            // add fields for resolver and mapper
            addField("resolver") [
                visibility = Visibility::PROTECTED
                injected(context)
                type = c.declaredFields.head.type
            ]
            if (mapperType !== null) {
                addField("mapper") [
                    visibility = Visibility::PROTECTED
                    injected(context)
                    type = mapperType
                ]
            }
            if (invalidateCache) {
                addField("executor") [
                    visibility = Visibility::PROTECTED
                    injected(context)
                    type = IExecutor.newTypeReference
                ]
            }
            addMethod("execute") [
                visibility = Visibility::PUBLIC
                addAnnotation(Override.newAnnotationReference)
                addParameter("request", rqClassRef)
                exceptions = Exception.newTypeReference
            ]
        ]
        return rqhClass.findDeclaredMethod("execute", rqClassRef)
    }
}
