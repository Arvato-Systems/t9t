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
package com.arvatosystems.t9t.doc.extensions

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.doc.DocComponentDTO
import com.arvatosystems.t9t.doc.DocComponentKey
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocConfigKey
import com.arvatosystems.t9t.doc.DocEmailCfgDTO
import com.arvatosystems.t9t.doc.DocEmailCfgKey
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.DocTemplateKey
import com.arvatosystems.t9t.doc.request.DocComponentCrudRequest
import com.arvatosystems.t9t.doc.request.DocConfigCrudRequest
import com.arvatosystems.t9t.doc.request.DocEmailCfgCrudRequest
import com.arvatosystems.t9t.doc.request.DocTemplateCrudRequest
import com.google.common.io.CharStreams
import de.jpaw.bonaparte.api.media.MediaDataUtil
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.util.ByteArray
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.List

class DocExtensions {
    public static final String DEFAULT_LANGUAGE = "xx"
    public static final String DEFAULT_COUNTRY  = "XX"
    public static final String DEFAULT_CURRENCY = "XXX"
    public static final String DEFAULT_ENTITY   = "-"

    // shorthands to perform a basic initialization of the key fields for defaults
    def static void defaultKey(DocComponentDTO it) {
        languageCode        = DEFAULT_LANGUAGE
        countryCode         = DEFAULT_COUNTRY
        currencyCode        = DEFAULT_CURRENCY
        entityId            = DEFAULT_ENTITY
    }
    def static void defaultKey(DocTemplateDTO it) {
        languageCode        = DEFAULT_LANGUAGE
        countryCode         = DEFAULT_COUNTRY
        currencyCode        = DEFAULT_CURRENCY
        entityId            = DEFAULT_ENTITY
    }
    def static void defaultKey(DocEmailCfgDTO it) {
        languageCode        = DEFAULT_LANGUAGE
        countryCode         = DEFAULT_COUNTRY
        currencyCode        = DEFAULT_CURRENCY
        entityId            = DEFAULT_ENTITY
    }
    def static void defaultKey(DocComponentKey it) {
        documentId          = "tbd"  // variable, set for example in batch loader
        languageCode        = DEFAULT_LANGUAGE
        countryCode         = DEFAULT_COUNTRY
        currencyCode        = DEFAULT_CURRENCY
        entityId            = DEFAULT_ENTITY
    }

    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<DocConfigDTO, FullTrackingWithVersion> merge(DocConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DocConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new DocConfigKey(dto.documentId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<DocComponentDTO, FullTrackingWithVersion> merge(DocComponentDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DocComponentCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new DocComponentKey(dto.documentId, dto.entityId, dto.languageCode, dto.countryCode, dto.currencyCode)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<DocTemplateDTO, FullTrackingWithVersion> merge(DocTemplateDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DocTemplateCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new DocTemplateKey(dto.documentId, dto.entityId, dto.languageCode, dto.countryCode, dto.currencyCode)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<DocEmailCfgDTO, FullTrackingWithVersion> merge(DocEmailCfgDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DocEmailCfgCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new DocEmailCfgKey(dto.documentId, dto.entityId, dto.languageCode, dto.countryCode, dto.currencyCode)
        ], CrudSurrogateKeyResponse)
    }

    // image loaders
    def static MediaData resourceAsPNG(String path) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_PNG
            rawData         = MediaDataUtil.getBinaryResource(path)
        ]
    }
    def static MediaData resourceAsGIF(String path) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_GIF
            rawData         = MediaDataUtil.getBinaryResource(path)
        ]
    }
    def static MediaData resourceAsJPG(String path) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_JPG
            rawData         = MediaDataUtil.getBinaryResource(path)
        ]
    }

    // text loaders
    def static MediaData resourceAsText(String path) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_TEXT
            text            = MediaDataUtil.getTextResource(path)
        ]
    }
    def static MediaData resourceAsHTML(String path) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_HTML
            text            = MediaDataUtil.getTextResource(path)
        ]
    }

    def static List<String> resourceAsCSV(String resourcePath) {
        MediaDataUtil.getTextResource(resourcePath).split("\n").filter[length > 0].toList
    }

    /** Classloader aware version of resourceAsPNG */
    def static MediaData resourceAsPNG(String path, Class<?> contextClass) {
        return new MediaData => [
            mediaType = MediaTypes.MEDIA_XTYPE_PNG
            rawData = getBinaryResource(path, contextClass)
        ]
    }

    /** Classloader aware version of resourceAsGIF */
    def static MediaData resourceAsGIF(String path, Class<?> contextClass) {
        return new MediaData => [
            mediaType = MediaTypes.MEDIA_XTYPE_GIF
            rawData = getBinaryResource(path, contextClass)
        ]
    }

    /** Classloader aware version of resourceAsJPG */
    def static MediaData resourceAsJPG(String path, Class<?> contextClass) {
        return new MediaData => [
            mediaType = MediaTypes.MEDIA_XTYPE_JPG
            rawData = getBinaryResource(path, contextClass)
        ]
    }

    /** Classloader aware version of resourceAsText */
    def static MediaData resourceAsText(String path, Class<?> contextClass) {
        return new MediaData => [
            mediaType       = MediaTypes.MEDIA_XTYPE_TEXT
            text            = getTextResource(path, contextClass)
        ]
    }

    /** Classloader aware version of resourceAsHTML */
    def static MediaData resourceAsHTML(String path, Class<?> contextClass) {
        return new MediaData => [
            mediaType = MediaTypes.MEDIA_XTYPE_HTML
            text = getTextResource(path, contextClass)
        ]
    }

    /** Classloader aware version of resourceAsCSV */
    def static List<String> resourceAsCSV(String resourcePath, Class<?> contextClass) {
        getTextResource(resourcePath, contextClass).split("\n").filter[length > 0].toList
    }


    /** Classloader aware version of getBinaryResource */
    def static ByteArray getBinaryResource(String path, Class<?> contextClass) throws IOException {
        val fis = contextClass.getResourceAsStream(path);
        val result = ByteArray.fromInputStream(fis, 0);
        fis.close();
        return result;
    }

    /** Classloader aware version of getTextResource */
    def static String getTextResource(String path, Class<?> contextClass) throws IOException {
        val is = contextClass.getResourceAsStream(path)
        try {  // xtend, please give me try with resources! (https://bugs.eclipse.org/bugs/show_bug.cgi?id=366020)
            return CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
        } finally {
            is.close
        }
    }
}
