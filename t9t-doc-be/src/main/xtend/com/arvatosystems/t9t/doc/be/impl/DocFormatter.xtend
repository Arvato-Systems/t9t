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
package com.arvatosystems.t9t.doc.be.impl

import com.arvatosystems.t9t.barcode.api.FlipMode
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry
import com.arvatosystems.t9t.doc.DocComponentDTO
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.be.converters.impl.ConverterToHtmlUsingCids
import com.arvatosystems.t9t.doc.services.IDocComponentConverter
import com.arvatosystems.t9t.doc.services.IDocFormatter
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.arvatosystems.t9t.doc.services.IImageGenerator
import com.arvatosystems.t9t.doc.services.valueclass.ImageParameter
import com.arvatosystems.t9t.translation.services.ITranslationProvider
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.bonaparte.util.ToStringHelper
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import de.jpaw.dp.Optional
import de.jpaw.dp.Singleton
import de.jpaw.util.ByteArray
import de.jpaw.util.ExceptionUtil
import freemarker.ext.beans.BeanModel
import freemarker.ext.beans.MapModel
import freemarker.ext.beans.StringModel
import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import freemarker.template.SimpleNumber
import freemarker.template.SimpleScalar
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import freemarker.template.TemplateHashModel
import freemarker.template.TemplateMethodModelEx
import freemarker.template.TemplateModelException
import java.io.InputStream
import java.io.StringReader
import java.io.StringWriter
import java.net.URL
import java.util.Currency
import java.util.List
import java.util.Locale
import java.util.Map
import java.util.concurrent.TimeUnit
import org.eclipse.xtend.lib.annotations.Data
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import java.util.HashMap
import java.util.TimeZone
import org.joda.time.format.DateTimeFormatter

@Singleton
@AddLogger
class DocFormatter implements IDocFormatter {
    protected static final DefaultObjectWrapper WRAPPER = new DefaultObjectWrapper(Configuration.VERSION_2_3_23)
    protected static final char MINUS = '-'

    @Inject IDocPersistenceAccess       persistenceAccess
    @Inject IDocModuleCfgDtoResolver    moduleConfigResolver
    @Inject @Optional ICacheInvalidationRegistry cacheInvalidationRegistry

    // converts a freemarker Model to a Java String
    def static protected String fmToString(Object x) {
        if (x === null)
            return ""
        if (x instanceof SimpleScalar)
            return x.asString
        if (x instanceof StringModel)
            return x.asString
        return '''(? class «x.class.canonicalName» ?)'''
    }

    /** Class serves as an internal key to the cached components. */
    @Data
    protected static class ComponentCacheKey {
        Long                            tenantRef
        DocumentSelector                selector
    }

    /** Class serves as a debugging aid. */
    @Data
    protected static class DebugModel implements TemplateMethodModelEx {

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        override exec(List arguments) throws TemplateModelException {
            LOGGER.info("Debug called with {} parameters", arguments.size)
            for (var int i = 0; i < arguments.size; i += 1)
                LOGGER.info("    parameter {} is of type {}", i, arguments.get(i).class.canonicalName)
            return "debug"
        }
    }

    /** Class serves as formatter for date and time. */
    @Data
    protected static class TimestampGeneratorModel implements TemplateMethodModelEx {
        private Locale        locale
        private DateTimeZone  zone

        def private formatTime(DateTimeFormatter localFormatter, LocalDateTime t) {
            // return localFormatter.print(t.toDate().time);
            return localFormatter.print(t.toDate(TimeZone.getTimeZone("UTC")).time);
        }

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        // usage is 1st parameter: ISO string
        // 2nd parameter: style as in http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
        // 2nd paramter consists of 2 chars. First is for date, second is for time. S=short format, M=medium format, L=long format, -= skip . Example: 'S-' in combination with third parameter 'D' converts LocalDateTime to Date only
        // optional parameter 3: force conversion from timestamp to day or time
        // optional parameter 4: separately supplied format in JODA format
        override exec(List arguments) throws TemplateModelException {
            if (arguments.size < 2)
                throw new Exception("TimestampGenerator model called with less than 2 parameters")
            val isoString   = arguments.get(0)
            val style       = arguments.get(1)
            val isoStringS  = isoString.fmToString
            val styleS      = style.fmToString
            val forceConversion = if (arguments.size >= 3) arguments.get(2).fmToString
            val jodaFormat      = if (arguments.size >= 4) arguments.get(3).fmToString
//            LOGGER.debug("$t(a, b) called, Parameter 1 was {}, parameter 2 was {}, with types {}, {}",
//                isoStringS, styleS, isoString.class.simpleName, style.class.simpleName
//            )

            if (styleS.length != 2)
                LOGGER.warn("Bad parameter: argument 2 for TimestampGenerator must be a 2 character string, but is {}", styleS)
            if (isoStringS === null || isoStringS.length == 0) {
                LOGGER.warn("null or empty string passed to date formatter with style {} - returning empty string", styleS)
                return ""
            }

            val formatter = if (jodaFormat === null) DateTimeFormat.forStyle(styleS) else DateTimeFormat.forPattern(jodaFormat)
            val localFormatter = formatter.withLocale(locale).withZone(zone)

            if (forceConversion !== null) {
                // arg 3 to convert timestamp to day or time, and apply the style formatter
                switch (forceConversion) {
                    case "D": {// timestamp to date, interpreted as day
                        val t = LocalDateTime.parse(isoStringS, ISODateTimeFormat.dateTimeParser())
                        return localFormatter.print(t.toLocalDate);
                    }
                    case "T": {// timestamp to date, interpreted as time
                        val t = LocalDateTime.parse(isoStringS, ISODateTimeFormat.dateTimeParser())
                        return localFormatter.print(t.toLocalTime);
                    }
                    case "d": {// day
                        val t = LocalDate.parse(isoStringS, ISODateTimeFormat.dateParser())
                        return localFormatter.print(t);
                    }
                    case "t": {// time
                        val t = LocalTime.parse(isoStringS, ISODateTimeFormat.timeParser())
                        return localFormatter.print(t);
                    }
                    case "i": {// instant
                        val t = LocalDateTime.parse(isoStringS, ISODateTimeFormat.dateTimeParser())
                        return formatTime(localFormatter, t)
                    }
                    default: {
                    }
                }
            }

            if (styleS.charAt(0) === MINUS) {
                // time only
                val t = LocalTime.parse(isoStringS, ISODateTimeFormat.timeParser())
                return localFormatter.print(t);
            } else if (styleS.charAt(1) === MINUS) {
                // day only
                val t = LocalDate.parse(isoStringS, ISODateTimeFormat.dateParser())
                return localFormatter.print(t);
            } else {
                // day + time
                val t = LocalDateTime.parse(isoStringS, ISODateTimeFormat.dateTimeParser())
                return formatTime(localFormatter, t)
            }
        }
    }

    /** Class serves as a getter to dynamic barcode creation. */
    @Data
    protected static class ImageGeneratorModel implements TemplateMethodModelEx {
        private MediaXType              desiredFormat
        private IDocComponentConverter  converter
//        @Inject IBarcodeGenerator       generator

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        override exec(List arguments) throws TemplateModelException {
            if (arguments.size < 2)
                throw new Exception("ImageGenerator model (i) called with less than 2 parameters")
            val formatObj   = arguments.get(0)
            val textObj     = arguments.get(1)
            val format      = formatObj.fmToString
            val text        = textObj.fmToString
            var int width   = 0
            var int height  = 0
            var int rotation  = 0
            var FlipMode flipMode = FlipMode.NO_FLIPPING
            if (arguments.size >= 4) {
                val widthObj    = arguments.get(2)
                val heightObj   = arguments.get(3)
                if (widthObj instanceof SimpleNumber)
                    width       = widthObj.asNumber.intValue
                else
                  LOGGER.warn("Bad parameter: argument 3 of i (width) should be an integer (SimpleNumber), but is {}", widthObj.class.canonicalName)
                if (heightObj instanceof SimpleNumber)
                    height      = heightObj.asNumber.intValue
                else
                  LOGGER.warn("Bad parameter: argument 4 of i (height) should be an integer (SimpleNumber), but is {}", heightObj.class.canonicalName)
            }
            if (arguments.size >= 5) {
                val rotObj    = arguments.get(4)
                if (rotObj instanceof SimpleNumber)
                    rotation       = rotObj.asNumber.intValue
                else
                  LOGGER.warn("Bad parameter: argument 5 of i (rotation) should be an integer (SimpleNumber), but is {}", rotObj.class.canonicalName)
            }
            if (arguments.size >= 6) {
                switch (arguments.get(5).fmToString) {
                case 'H': flipMode = FlipMode.FLIP_HORIZONTALLY
                case 'V': flipMode = FlipMode.FLIP_VERTICALLY
                case 'N': flipMode = FlipMode.NO_FLIPPING
                default:  LOGGER.warn("Bad parameter: argument 6 of i (flip mode) should be either H or V or N")
                }
            }
            LOGGER.debug("ImageGenerator({}, {}, {}, {}) called", format, text, width, height)
            // barcode V1: specific barcode class
//            val generator = Jdp.getRequired(IBarcodeGenerator)
//            val dataInCache = generator.generateBarcode(BarcodeFormat.valueOf(format), text, width, height)
            // barcode V2: generate a generic image
            val parameters = new ImageParameter(width, height, rotation, flipMode, null)
            val generator = Jdp.getRequired(IImageGenerator, format)
            val data = generator.generateImage(text, parameters)

            // generate a CID for the generated image
            val cid = format + "_" + text
            if (data.z === null) data.z = new HashMap
            data.z.put("cid", cid)

            // already correct format: just wrap it and return it
            if (data.mediaType == desiredFormat)
                return WRAPPER.wrap(data.text)

            // different format: convert it, if a converter is available
            if (converter === null) {
                LOGGER.warn("Missing converter from {} to {} for image creation of format {}", data.mediaType.name, desiredFormat.name, format)
                return null
            }
            val converted = converter.convertFrom(data)
            if (converted === null)
                return null
            return WRAPPER.wrap(converted)
        }
    }

    /** Class resolves URLs and creates base64 data from it. */
    protected static class Base64FromUrlGeneratorModel implements TemplateMethodModelEx {

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        override exec(List arguments) throws TemplateModelException {
            if (arguments.size < 1)
                throw new Exception("Base64FromUrlGeneratorModel model (u) called without parameter")
            val url   = arguments.get(0).toString
            val fallback = (if (arguments.size >= 2) arguments.get(1).toString) ?: ""
            val myURL = new URL(url)
            var InputStream is = null // missing trz with resources in xtend!
            try {
                is = myURL.openStream();
                val ba = ByteArray.fromInputStream(is, 16000000)
                return WRAPPER.wrap(ba.asBase64)
            } catch (Exception e) {
                LOGGER.error("Could not open URL {}: {}", url, ExceptionUtil.causeChain(e))
            } finally {
                if (is !== null) is.close
            }
            return WRAPPER.wrap(fallback)
        }
    }

    /** Class serves as a getter to language translations via property files. */
    protected static class PropertyTranslationsFuncModel implements TemplateMethodModelEx {
        private String                 language
        private String []              languages
        private ITranslationProvider   translationProvider = Jdp.getOptional(ITranslationProvider)

        new (String language) {
            this.language = language
            languages     = translationProvider?.resolveLanguagesToCheck(language, true)
        }

        override exec(List arguments) throws TemplateModelException {
            if (arguments.size < 1)
                throw new Exception("PropertyTranslationsFuncModel model (q) called without parameter")
            val varname = arguments.get(0).toString
            val path    = if (arguments.size >= 2) arguments.get(1).toString
            if (translationProvider === null)
                return WRAPPER.wrap("${" + path + "}")

            return WRAPPER.wrap(translationProvider.getTranslation(
                T9tConstants.GLOBAL_TENANT_ID,
                languages,
                path, varname
            ))
        }
    }

    /** Class serves as a getter to language translations via property files. */
    protected static class PropertyTranslations implements TemplateHashModel {
        private String                 language
        private String []              languages
        private ITranslationProvider   translationProvider = Jdp.getOptional(ITranslationProvider)

        new (String language) {
            this.language = language
            languages     = translationProvider?.resolveLanguagesToCheck(language, true)
        }

        override get(String key) throws TemplateModelException {
            // convert dollars to dots, then call the translator
            val path = key.replace('$', '.')
            if (translationProvider === null)
                return WRAPPER.wrap("${" + path + "}")

            val lastDot = path.lastIndexOf('.')
            return WRAPPER.wrap(translationProvider.getTranslation(
                T9tConstants.GLOBAL_TENANT_ID,
                languages,
                if (lastDot >= 0) path.substring(0, lastDot),
                if (lastDot >= 0) path.substring(lastDot+1) else path
            ))
        }

        override isEmpty() throws TemplateModelException {
            return false
        }
    }

    /** Class serves as a getter to components, which performs media type conversion. */
    @Data
    protected static class ComponentAccessor implements TemplateHashModel {
        private Map<String, MediaData>  components
        private MediaXType              desiredFormat
        private IDocComponentConverter  converter
        private Long                    tenantRef    // just for logging

        override get(String key) throws TemplateModelException {
            val dataInCache = components.get(key);
            if (dataInCache === null)
                return null
            if (dataInCache.mediaType === desiredFormat)
                return WRAPPER.wrap(dataInCache.text)

            // different format: convert it, if a converter is available
            if (converter === null) {
                LOGGER.warn("Missing converter from {} to {} for component {}, tenantRef {}", dataInCache.mediaType.name, desiredFormat.name, key, tenantRef)
                return null
            }
            val converted = converter.convertFrom(dataInCache)
            if (converted === null)
                return null
            return WRAPPER.wrap(converted)
        }

        override isEmpty() throws TemplateModelException {
            return components.isEmpty
        }
    }


    protected static final Cache<ComponentCacheKey, Map<String, MediaData>> componentCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build

    def static clearCache() {
        componentCache.invalidateAll
    }

    new() {
        cacheInvalidationRegistry?.registerInvalidator(DocComponentDTO.simpleName, [ componentCache.invalidateAll ])
    }

    def protected getZone(String zoneId) {
        if (zoneId === null)
            return DateTimeZone.UTC
        try {
            return DateTimeZone.forID(zoneId)
        } catch (Exception e) {
            LOGGER.error("Invalid time zone, exception for ID {}", zoneId)
        }
        return DateTimeZone.UTC
    }

    override formatDocument(String tenantId, Long sharedTenantRef, TemplateType templateType, String template, DocumentSelector selector, String timeZone, Object data,
        Map<String, MediaData> attachments
    ) {
        LOGGER.debug("formatDocument for template {}, type {} with selector {}, time zone {}", template, templateType, selector, timeZone)

        if (LOGGER.traceEnabled)  // avoid expensive formatting unless it is desired
            LOGGER.trace("data received is {}", if (data !== null) ToStringHelper.toStringML(data) else "null")

        // read the tenant configuration
        val moduleCfg = moduleConfigResolver.moduleConfiguration

        // compute the effective time zone
        val effectiveTimeZone = getZone(timeZone)

        // cache images and texts
        // freeze the selector to allow caching the hash code
        selector.freeze
        val cacheKey = new DocFormatter.ComponentCacheKey(sharedTenantRef, selector)
        val components = componentCache.get(cacheKey, [ persistenceAccess.getDocComponents(moduleCfg, selector) ])

        var boolean                 escapeToRaw         // escape special characters by CDATA or similar
        var String                  templateText = ""   // the template - will be filled within switch
        var MediaXType              mediaType           // the media type of the template
        var IDocComponentConverter  converter           // a converter from other media types (components, parameters) into the desired format

        switch (templateType) {
            case DOCUMENT_ID: {
                // read the relevant template record
                val templateDto = persistenceAccess.getDocTemplateDTO(moduleCfg, template, selector)
                templateText    = templateDto.template
                mediaType       = templateDto.mediaType
                escapeToRaw     = templateDto.escapeToRaw ?: false
                if (attachments !== null && mediaType == MediaTypes.MEDIA_XTYPE_HTML)
                    converter   = new ConverterToHtmlUsingCids(attachments)
                else
                    converter   = Jdp.getOptional(IDocComponentConverter, mediaType.name)
            }
            case COMPONENT: {
                return components.get(template)
            }
            case INLINE: {
                templateText    = template
                mediaType       = MediaTypes.MEDIA_XTYPE_TEXT
                escapeToRaw     = false
            }
        }

        var Locale myLocale = Locale.ENGLISH
        try {
            if ("xx" != selector.languageCode) {
                // there are 2 cases we need to adapt: if selector.languageCode is null, we use "en", if it is "??_??" we have to shorten it, because cannot work with "de_DE" for example.
                val effectiveLanguage = if (selector.languageCode === null) "en" else selector.languageCode.substring(0, 2);
                // keep ENGLISH for undefined language, as a fallback
                if ("XX" != selector.countryCode)
                    myLocale = new Locale(effectiveLanguage, selector.countryCode)
                else
                    myLocale = new Locale(effectiveLanguage)
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot obtain locale for language {}, country {}, falling back to English", selector.languageCode, selector.countryCode)
        }

        var String currencySymbol = "?"
        try {
            currencySymbol = Currency.getInstance(selector.currencyCode).getSymbol(myLocale)
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid currency code provided: {}", selector.currencyCode)
        }

        // val repo = new DocFormatter.Repo(selector, data, new DocFormatter.ComponentAccessor(components, mediaType), #{ "currencySymbol" -> currencySymbol })
        val repo = new MapModel( #{
            "s" -> new BeanModel(selector, WRAPPER),
            "d" -> new BeanModel(data, new EscapingWrapper(mediaType, converter, escapeToRaw)),
            "c" -> new DocFormatter.ComponentAccessor(components, mediaType, converter, sharedTenantRef),
            "p" -> new DocFormatter.PropertyTranslations(selector.languageCode),
            "q" -> new DocFormatter.PropertyTranslationsFuncModel(selector.languageCode),
            "i" -> new ImageGeneratorModel(mediaType, converter),
            "u" -> new Base64FromUrlGeneratorModel,
            "t" -> new TimestampGeneratorModel(myLocale, effectiveTimeZone),
            "e" -> new MapModel(#{
                "currencySymbol" -> currencySymbol,
                "tenantId" -> tenantId,
                "timeZone" -> effectiveTimeZone.ID
            }, WRAPPER),
            "debug" -> new DebugModel
        }, WRAPPER)

        val cfg = new Configuration(Configuration.VERSION_2_3_23)
        cfg.defaultEncoding             = "UTF-8"
        cfg.templateExceptionHandler    = TemplateExceptionHandler.DEBUG_HANDLER
        cfg.locale                      = myLocale
        val fmTemplate                  = new Template(null, new StringReader(templateText), cfg)

        // create temporary storage for formatted address
        val out     = new StringWriter(1000);
        fmTemplate.process(repo, out)

        // create the output
        val m       = new MediaData
        m.mediaType = mediaType
        m.text      = out.toString
        return m
    }

    @Data
    protected static class EscapingWrapper extends DefaultObjectWrapper {
        private MediaXType              desiredFormat
        private IDocComponentConverter  converter
        private boolean                 escapeToRaw

        override wrap(Object obj) throws TemplateModelException {
            if (obj === null) {
                return super.wrap(null);
            }
            if (obj instanceof String) {
                val format = desiredFormat?.baseEnum
                if (escapeToRaw && (format === MediaType.HTML || format === MediaType.XHTML))
                    return new SimpleScalar("<![CDATA[" + obj + "]]>")
                if (converter !== null)
                    return super.wrap(converter.convertFrom(new MediaData(MediaTypes.MEDIA_XTYPE_TEXT, obj, null, null)))
            }
            return super.wrap(obj)
        }
    }
}
