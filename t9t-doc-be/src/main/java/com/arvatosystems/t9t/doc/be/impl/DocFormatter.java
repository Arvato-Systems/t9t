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
package com.arvatosystems.t9t.doc.be.impl;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.barcode.api.FlipMode;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.T9tDocTools;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.TemplateType;
import com.arvatosystems.t9t.doc.be.converters.impl.ConverterToHtmlUsingCids;
import com.arvatosystems.t9t.doc.services.IDocComponentConverter;
import com.arvatosystems.t9t.doc.services.IDocFormatter;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess;
import com.arvatosystems.t9t.doc.services.IImageGenerator;
import com.arvatosystems.t9t.doc.services.ImageParameter;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.MapModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@Singleton
public class DocFormatter implements IDocFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocFormatter.class);

    protected static final DefaultObjectWrapper WRAPPER = new DefaultObjectWrapper(Configuration.VERSION_2_3_23);
    protected static final char MINUS = '-';

    // FIXME once we have Java 17! This was 4 lines in xtend, and will be readable again with Java 17 record types.
    /** Class serves as an internal key to the cached components. */
    protected static class ComponentCacheKey {
        private final String tenantId;
        private final DocumentSelector selector;

        public ComponentCacheKey(String tenantId, DocumentSelector selector) {
            super();
            this.tenantId = tenantId;
            this.selector = selector;
        }

        @Override
        public int hashCode() {
            return Objects.hash(selector, tenantId);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ComponentCacheKey other = (ComponentCacheKey) obj;
            return Objects.equals(selector, other.selector) && Objects.equals(tenantId, other.tenantId);
        }

        @Override
        public String toString() {
            return "ComponentCacheKey [tenantId=" + tenantId + ", selector=" + selector + "]";
        }

        public String getTenantId() {
            return tenantId;
        }

        public DocumentSelector getSelector() {
            return selector;
        }
    }

    /** Class serves as a debugging aid. */
    protected static class DebugModel implements TemplateMethodModelEx {
        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            LOGGER.info("Debug called with {} parameters", arguments.size());
            for (int i = 0; i < arguments.size(); i += 1) {
                LOGGER.info("    parameter {} is of type {}", i, arguments.get(i).getClass().getCanonicalName());
            }
            return "debug";
        }
    }

    /** Class serves as formatter for date and time. */
    protected static class TimestampGeneratorModel implements TemplateMethodModelEx {
        private final Locale locale;
        private final ZoneId zone;

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        // usage is 1st parameter: ISO string
        // 2nd parameter: style as in http://www.joda.java-time/apidocs/java/time/format/DateTimeFormat.html
        // 2nd parameter consists of 2 chars. First is for date, second is for time. S=short format, M=medium format, L=long format, -= skip .
        // Example: 'S-' in combination with third parameter 'D' converts LocalDateTime to Date only
        // optional parameter 3: force conversion from timestamp to day or time
        // optional parameter 4: separately supplied format in JODA format
        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            if (arguments.size() < 2) {
                throw new TemplateModelException("TimestampGenerator model called with less than 2 parameters");
            }
            final Object isoString = arguments.get(0);
            final Object style = arguments.get(1);
            final String isoStringS = fmToString(isoString);
            final String styleS = fmToString(style);
            final String forceConversion = arguments.size() >= 3 ? fmToString(arguments.get(2)) : null;
            final String jodaFormat = arguments.size() >= 4 ? fmToString(arguments.get(3)) : null;

            if (styleS.length() != 2) {
                LOGGER.warn("Bad parameter: argument 2 for TimestampGenerator must be a 2 character string, but is {}", styleS);
            }
            if (isoStringS == null || isoStringS.length() == 0) {
                LOGGER.warn("null or empty string passed to date formatter with style {} - returning empty string", styleS);
                return "";
            }

            final DateTimeFormatter formatter = jodaFormat == null ? getFormatterForStyle(styleS) : DateTimeFormatter.ofPattern(jodaFormat);
            final DateTimeFormatter localFormatter = formatter.withLocale(locale);

            if (forceConversion != null) {
                try {
                    // arg 3 to convert timestamp to day or time, and apply the style formatter
                    switch (forceConversion) {
                    case "D": // timestamp to date, interpreted as day
                        // workaround for bad input: adapt to cases when just a date is given
                        final int l = isoStringS.length();
                        if (l == 10) {
                            // do the same as for 'd'
                            LOGGER.warn("Date/time conversion mismatch: force=\'D\', style={}, format={}, input={}: You should use \'d\' in this case!", styleS,
                                    jodaFormat, isoStringS);
                            final LocalDate timeStampDayT = LocalDate.parse(isoStringS);
                            return localFormatter.format(timeStampDayT);
                        } else {
                            // another workaround for for extra Z suffix: drop it, it would cause a conversion error
                            final boolean gotTimezoneSuffix = isoStringS.endsWith("Z");
                            if (gotTimezoneSuffix) {
                                LOGGER.warn("Date/time conversion mismatch: force='D', style={}, format={}, input={}: Dropping time zone suffix."
                                        + " Bug in data provider?", styleS, jodaFormat, isoStringS);
                            }
                            final LocalDateTime timeStampDayT = LocalDateTime.parse(gotTimezoneSuffix ? isoStringS.substring(0, l - 1) : isoStringS);
                            // timezone conversion
                            final LocalDateTime timeStampDayTt = timeStampDayT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                            return localFormatter.format(timeStampDayTt.toLocalDate());
                        }
                    case "T": // timestamp to date, interpreted as time
                        final LocalDateTime timeStampTimeT = LocalDateTime.parse(isoStringS);
                        // timezone conversion
                        final LocalDateTime timeStampTimeTt = timeStampTimeT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                        return localFormatter.format(timeStampTimeTt.toLocalTime());
                    case "d": // day
                        final LocalDate dayT = LocalDate.parse(isoStringS);
                        return localFormatter.format(dayT);
                    case "t": // time
                        final LocalTime timeT = LocalTime.parse(isoStringS);
                        return localFormatter.format(timeT);
                    case "i": // instant
                        final LocalDateTime instantT = LocalDateTime.parse(isoStringS);
                        final LocalDateTime instantTt = instantT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                        return instantTt.format(localFormatter);
                    default:
                        LOGGER.error("Unknown conversion rule: {}", forceConversion);
                        break;
                    }
                } catch (final Exception e) {
                    LOGGER.error("Date/time conversion error: force={}, style={}, format={}, input={}", forceConversion, styleS, jodaFormat, isoStringS);
                    return "***ERROR***";
                }
            }

            try {
                if (styleS.charAt(0) == MINUS) {
                    // time only
                    final LocalTime t = LocalTime.parse(isoStringS);
                    return localFormatter.format(t);
                } else if (styleS.charAt(1) == MINUS) {
                    // day only
                    final LocalDate t = LocalDate.parse(isoStringS);
                    return localFormatter.format(t);
                } else {
                    // day + time
                    final LocalDateTime t = LocalDateTime.parse(isoStringS);
                    final LocalDateTime tt = t.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime(); // timezone conversion
                    return tt.format(localFormatter);
                }
            } catch (Exception e) {
                LOGGER.error("Date/time conversion error: style={}, format={}, input={}", forceConversion, styleS, jodaFormat, isoStringS);
                return "***ERROR***";
            }
        }

        private DateTimeFormatter getFormatterForStyle(final String styleS) {
            if (styleS == null || styleS.length() != 2) {
                LOGGER.error("Bad style given: {}, returning default formatter", styleS);
                return DateTimeFormatter.ISO_INSTANT;
            }
            if (styleS.charAt(0) == MINUS) {
                // time only
                return DateTimeFormatter.ofLocalizedTime(T9tDocTools.styleFor(styleS.charAt(1)));
            } else if (styleS.charAt(1) == MINUS) {
                // day only
                return DateTimeFormatter.ofLocalizedDate(T9tDocTools.styleFor(styleS.charAt(0)));
            } else {
                return DateTimeFormatter.ofLocalizedDateTime(T9tDocTools.styleFor(styleS.charAt(0)), T9tDocTools.styleFor(styleS.charAt(1)));
            }
        }

        public TimestampGeneratorModel(Locale locale, ZoneId zone) {
            super();
            this.locale = locale;
            this.zone = zone;
        }

        @Override
        public int hashCode() {
            return Objects.hash(locale, zone);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TimestampGeneratorModel other = (TimestampGeneratorModel) obj;
            return Objects.equals(locale, other.locale) && Objects.equals(zone, other.zone);
        }

        @Override
        public String toString() {
            return "TimestampGeneratorModel [locale=" + locale + ", zone=" + zone + "]";
        }

        public Locale getLocale() {
            return locale;
        }

        public ZoneId getZone() {
            return zone;
        }
    }

    /** Class serves as a getter to dynamic barcode creation. */
    protected static class ImageGeneratorModel implements TemplateMethodModelEx {
        private final MediaXType desiredFormat;
        private final IDocComponentConverter converter;

        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            final int argumentsSize = arguments.size();
            if (argumentsSize < 2) {
                throw new TemplateModelException("ImageGenerator model (i) called with less than 2 parameters");
            }
            final Object formatObj = arguments.get(0);
            final Object textObj = arguments.get(1);
            final String format = fmToString(formatObj);
            final String text = fmToString(textObj);
            int width = 0;
            int height = 0;
            int rotation = 0;
            FlipMode flipMode = FlipMode.NO_FLIPPING;

            if (argumentsSize >= 4) {
                final Object widthObj = arguments.get(2);
                final Object heightObj = arguments.get(3);
                if (widthObj instanceof SimpleNumber) {
                    width = ((SimpleNumber) widthObj).getAsNumber().intValue();
                } else {
                    LOGGER.warn("Bad parameter: argument 3 of i (width) should be an integer (SimpleNumber), but is {}",
                            widthObj.getClass().getCanonicalName());
                }
                if (heightObj instanceof SimpleNumber) {
                    height = ((SimpleNumber) heightObj).getAsNumber().intValue();
                } else {
                    LOGGER.warn("Bad parameter: argument 4 of i (height) should be an integer (SimpleNumber), but is {}",
                            heightObj.getClass().getCanonicalName());
                }
            }

            if (argumentsSize >= 5) {
                final Object rotObj = arguments.get(4);
                if (rotObj instanceof SimpleNumber) {
                    rotation = ((SimpleNumber) rotObj).getAsNumber().intValue();
                } else {
                    LOGGER.warn("Bad parameter: argument 5 of i (rotation) should be an integer (SimpleNumber), but is {}",
                            rotObj.getClass().getCanonicalName());
                }
            }

            if (argumentsSize >= 6) {
                switch (fmToString(arguments.get(5))) {
                case "H":
                    flipMode = FlipMode.FLIP_HORIZONTALLY;
                    break;
                case "V":
                    flipMode = FlipMode.FLIP_VERTICALLY;
                    break;
                case "N":
                    flipMode = FlipMode.NO_FLIPPING;
                    break;
                default:
                    LOGGER.warn("Bad parameter: argument 6 of i (flip mode) should be either H or V or N");
                    break;
                }
            }

            LOGGER.debug("ImageGenerator({}, {}, {}, {}) called", format, text, width, height);
            // barcode V2: generate a generic image
            final ImageParameter parameters = new ImageParameter(width, height, Integer.valueOf(rotation), flipMode, null);
            final IImageGenerator generator = Jdp.getRequired(IImageGenerator.class, format);
            final MediaData data;
            try {
                data = generator.generateImage(text, parameters);
            } catch (Exception e) {
                LOGGER.error("Fail to generate image.", e);
                throw new TemplateModelException("Fail to generate image.", e);
            }

            // generate a CID for the generated image
            final String cid = format + "_" + text;
            if (data.getZ() == null) {
                data.setZ(new HashMap<>());
            }
            data.getZ().put("cid", cid);

            // already correct format: just wrap it and return it
            if (data.getMediaType().equals(desiredFormat)) {
                return WRAPPER.wrap(data.getText());
            }

            // different format: convert it, if a converter is available
            if (converter == null) {
                LOGGER.warn("Missing converter from {} to {} for image creation of format {}", data.getMediaType().name(), desiredFormat.name(), format);
                return null;
            }
            final String converted = converter.convertFrom(data);
            if (converted == null) {
                return null;
            }
            return WRAPPER.wrap(converted);
        }

        public ImageGeneratorModel(MediaXType desiredFormat, IDocComponentConverter converter) {
            super();
            this.desiredFormat = desiredFormat;
            this.converter = converter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(converter, desiredFormat);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ImageGeneratorModel other = (ImageGeneratorModel) obj;
            return Objects.equals(converter, other.converter) && Objects.equals(desiredFormat, other.desiredFormat);
        }

        @Override
        public String toString() {
            return "ImageGeneratorModel [desiredFormat=" + desiredFormat + ", converter=" + converter + "]";
        }

        public MediaXType getDesiredFormat() {
            return desiredFormat;
        }

        public IDocComponentConverter getConverter() {
            return converter;
        }
    }

    /** Class resolves URLs and creates base64 data from it. */
    protected static class Base64FromUrlGeneratorModel implements TemplateMethodModelEx {
        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            if (arguments.size() < 1) {
                throw new TemplateModelException("Base64FromUrlGeneratorModel model (u) called without parameter");
            }
            final String url = arguments.get(0).toString();
            final String fallBackStr = arguments.size() >= 2 ? arguments.get(1).toString() : null;
            final String fallback = fallBackStr == null ? "" : fallBackStr;
            final URL myURL;
            try {
                myURL = new URL(url);
            } catch (final Exception e) {
                LOGGER.error("Could not open URL {}: {}", url, ExceptionUtil.causeChain(e));
                throw new TemplateModelException("Could not open URL " + url);
            }
            try (InputStream is = myURL.openStream()) {
                final ByteArray ba = ByteArray.fromInputStream(is, 16000000);
                return WRAPPER.wrap(ba.asBase64());
            } catch (final Exception e) {
                LOGGER.error("Failed to read URL {}: {}", url, ExceptionUtil.causeChain(e));
            }
            return WRAPPER.wrap(fallback);
        }
    }

    /** Class serves as a getter to language translations via property files. */
    protected static class PropertyTranslationsFuncModel implements TemplateMethodModelEx {
        private final String language;
        private final String[] languages;

        private final ITranslationProvider translationProvider = Jdp.getOptional(ITranslationProvider.class);

        public PropertyTranslationsFuncModel(final String language) {
            this.language = language;
            this.languages = translationProvider != null ? translationProvider.resolveLanguagesToCheck(language, true) : null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            if (arguments.size() < 1) {
                throw new TemplateModelException("PropertyTranslationsFuncModel model (q) called without parameter");
            }
            final String varname = arguments.get(0).toString();
            final String path = arguments.size() >= 2 ? arguments.get(1).toString() : null;
            if (translationProvider == null) {
                return WRAPPER.wrap("${" + path + "}");
            }

            return WRAPPER.wrap(translationProvider.getTranslation(T9tConstants.GLOBAL_TENANT_ID, languages, path, varname));
        }
    }

    /** Class serves as a getter to language translations via property files. */
    protected static class PropertyTranslations implements TemplateHashModel {
        private final String language;
        private final String[] languages;

        private final ITranslationProvider translationProvider = Jdp.getOptional(ITranslationProvider.class);

        public PropertyTranslations(final String language) {
            this.language = language;
            this.languages = translationProvider != null ? translationProvider.resolveLanguagesToCheck(language, true) : null;
        }

        @Override
        public TemplateModel get(final String key) throws TemplateModelException {
            // convert dollars to dots, then call the translator
            final String path = key.replace("$", ".");
            if (translationProvider == null) {
                return WRAPPER.wrap((("${" + path) + "}"));
            }

            final int lastDot = path.lastIndexOf(".");
            return WRAPPER.wrap(translationProvider.getTranslation(T9tConstants.GLOBAL_TENANT_ID, languages, lastDot >= 0 ? path.substring(0, lastDot) : null,
                    lastDot >= 0 ? path.substring(lastDot + 1) : path));
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }
    }

    /** Class serves as a getter to components, which performs media type conversion. */
    protected static class ComponentAccessor implements TemplateHashModel {
        private final Map<String, MediaData> components;
        private final MediaXType desiredFormat;
        private final IDocComponentConverter converter;
        private final String tenantId; // just for logging

        @Override
        public TemplateModel get(final String key) throws TemplateModelException {
            final MediaData dataInCache = this.components.get(key);
            if (dataInCache == null) {
                return null;
            }
            if (dataInCache.getMediaType().equals(desiredFormat)) {
                return WRAPPER.wrap(dataInCache.getText());
            }

            // different format: convert it, if a converter is available
            if (converter == null) {
                LOGGER.warn("Missing converter from {} to {} for component {}, tenantId {}", dataInCache.getMediaType().name(),
                        desiredFormat.name(), key, tenantId);
                return null;
            }
            final String converted = converter.convertFrom(dataInCache);
            if (converted == null) {
                return null;
            }
            return WRAPPER.wrap(converted);
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return this.components.isEmpty();
        }

        public ComponentAccessor(Map<String, MediaData> components, MediaXType desiredFormat, IDocComponentConverter converter, String tenantId) {
            super();
            this.components = components;
            this.desiredFormat = desiredFormat;
            this.converter = converter;
            this.tenantId = tenantId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(components, converter, desiredFormat, tenantId);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ComponentAccessor other = (ComponentAccessor) obj;
            return Objects.equals(components, other.components) && Objects.equals(converter, other.converter)
                    && Objects.equals(desiredFormat, other.desiredFormat) && Objects.equals(tenantId, other.tenantId);
        }

        @Override
        public String toString() {
            return "ComponentAccessor [components=" + components + ", desiredFormat=" + desiredFormat + ", converter=" + converter + ", tenantId=" + tenantId
                    + "]";
        }

        public Map<String, MediaData> getComponents() {
            return components;
        }

        public MediaXType getDesiredFormat() {
            return desiredFormat;
        }

        public IDocComponentConverter getConverter() {
            return converter;
        }

        public String getTenantId() {
            return tenantId;
        }
    }

    protected static class EscapingWrapper extends DefaultObjectWrapper {
        private final MediaXType desiredFormat;
        private final IDocComponentConverter converter;
        private final boolean escapeToRaw;

        @Override
        public TemplateModel wrap(final Object obj) throws TemplateModelException {
            if (obj == null) {
                return super.wrap(null);
            }
            if (obj instanceof String) {
                final Enum<?> format = desiredFormat != null ? desiredFormat.getBaseEnum() : null;
                if (escapeToRaw && (MediaType.HTML.equals(format) || MediaType.XHTML.equals(format))) {
                    return new SimpleScalar("<![CDATA[" + (String) obj + "]]>");
                }
                if (converter != null) {
                    return super.wrap(converter.convertFrom(new MediaData(MediaTypes.MEDIA_XTYPE_TEXT, ((String) obj), null, null)));
                }
            }
            return super.wrap(obj);
        }

        public EscapingWrapper(MediaXType desiredFormat, IDocComponentConverter converter, boolean escapeToRaw) {
            super();
            this.desiredFormat = desiredFormat;
            this.converter = converter;
            this.escapeToRaw = escapeToRaw;
        }

        @Override
        public int hashCode() {
            return Objects.hash(converter, desiredFormat, escapeToRaw);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EscapingWrapper other = (EscapingWrapper) obj;
            return Objects.equals(converter, other.converter) && Objects.equals(desiredFormat, other.desiredFormat) && escapeToRaw == other.escapeToRaw;
        }

        @Override
        public String toString() {
            return "EscapingWrapper [desiredFormat=" + desiredFormat + ", converter=" + converter + ", escapeToRaw=" + escapeToRaw + "]";
        }

        public MediaXType getDesiredFormat() {
            return desiredFormat;
        }

        public IDocComponentConverter getConverter() {
            return converter;
        }

        public boolean isEscapeToRaw() {
            return escapeToRaw;
        }
    }

    private final IDocPersistenceAccess persistenceAccess = Jdp.getRequired(IDocPersistenceAccess.class);
    private final IDocModuleCfgDtoResolver moduleConfigResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    private final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getOptional(ICacheInvalidationRegistry.class);
    protected static final Cache<ComponentCacheKey, Map<String, MediaData>> COMPONENT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).<ComponentCacheKey, Map<String, MediaData>>build();

    @Override
    public MediaData formatDocument(final String tenantId, final String sharedTenantId, final TemplateType templateType, final String template,
            final DocumentSelector selector, final String timeZone, final Object data, final Map<String, MediaData> attachments) {
        try {
            LOGGER.debug("formatDocument for template {}, type {} with selector {}, time zone {}", template, templateType, selector, timeZone);

            if (LOGGER.isTraceEnabled()) { // avoid expensive formatting unless it is desired
                LOGGER.trace("data received is {}", data != null ? ToStringHelper.toStringML(data) : "null");
            }

            // read the tenant configuration
            final DocModuleCfgDTO moduleCfg = moduleConfigResolver.getModuleConfiguration();

            // compute the effective time zone
            final ZoneId effectiveTimeZone = getZone(timeZone);

            // cache images and texts
            // freeze the selector to allow caching the hash code
            selector.freeze();
            final ComponentCacheKey cacheKey = new ComponentCacheKey(sharedTenantId, selector);
            final Map<String, MediaData> components = COMPONENT_CACHE.get(cacheKey, unused -> {
                return persistenceAccess.getDocComponents(moduleCfg, selector);
            });

            boolean escapeToRaw = false; // escape special characters by CDATA or similar
            String templateText = ""; // the template - will be filled within switch
            MediaXType mediaType = null; // the media type of the template
            IDocComponentConverter converter = null; // a converter from other media types (components, parameters) into the desired format

            switch (templateType) {
            case DOCUMENT_ID:
                // read the relevant template record
                final DocTemplateDTO templateDto = persistenceAccess.getDocTemplateDTO(moduleCfg, template, selector);
                templateText = templateDto.getTemplate();
                mediaType = templateDto.getMediaType();
                escapeToRaw = templateDto.getEscapeToRaw() == null ? false : templateDto.getEscapeToRaw();
                if (attachments != null && MediaTypes.MEDIA_XTYPE_HTML.equals(mediaType)) {
                    converter = new ConverterToHtmlUsingCids(attachments);
                } else {
                    converter = Jdp.getOptional(IDocComponentConverter.class, mediaType.name());
                }
                break;
            case COMPONENT:
                return components.get(template);
            case INLINE:
                templateText = template;
                mediaType = MediaTypes.MEDIA_XTYPE_TEXT;
                escapeToRaw = false;
                break;
            }

            final String languageCode = selector.getLanguageCode();
            final String countryCode = selector.getCountryCode();
            final String currencyCode = selector.getCurrencyCode();
            Locale myLocale = Locale.ENGLISH;
            try {
                if (!DocConstants.DEFAULT_LANGUAGE_CODE.equals(languageCode)) {
                    // there are 2 cases we need to adapt: if selector.languageCode is null, we use "en", if it is "??_??" we have to shorten it,
                    // because cannot work with "de_DE" for example.
                    final String effectiveLanguage = languageCode == null ? "en" : languageCode.substring(0, 2);
                    // keep ENGLISH for undefined language, as a fallback
                    if (!DocConstants.DEFAULT_COUNTRY_CODE.equals(countryCode)) {
                        myLocale = new Locale(effectiveLanguage, countryCode);
                    } else {
                        myLocale = new Locale(effectiveLanguage);
                    }
                }
            } catch (final Exception e) {
                LOGGER.warn("Cannot obtain locale for language {}, country {}, falling back to English", languageCode, countryCode);
            }

            String currencySymbol = "?";
            try {
                currencySymbol = Currency.getInstance(currencyCode).getSymbol(myLocale);
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("Invalid currency code provided: {}", currencyCode);
            }
            final Map<String, Object> eMap = new HashMap<>(3);
            eMap.put("currencySymbol", currencySymbol);
            eMap.put("tenantId", tenantId);
            eMap.put("timeZone", effectiveTimeZone.getId());
            final MapModel eRepo = new MapModel(eMap, WRAPPER);

            final Map<String, Object> map = new HashMap<>(10);
            map.put("s", new BeanModel(selector, WRAPPER));
            map.put("d", new BeanModel(data, new EscapingWrapper(mediaType, converter, escapeToRaw)));
            map.put("c", new ComponentAccessor(components, mediaType, converter, sharedTenantId));
            map.put("p", new PropertyTranslations(languageCode));
            map.put("q", new PropertyTranslationsFuncModel(languageCode));
            map.put("i", new ImageGeneratorModel(mediaType, converter));
            map.put("u", new Base64FromUrlGeneratorModel());
            map.put("t", new TimestampGeneratorModel(myLocale, effectiveTimeZone));
            map.put("e", eRepo);
            map.put("debug", new DebugModel());
            final MapModel repo = new MapModel(map, WRAPPER);

            final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
            cfg.setLocale(myLocale);
            Template fmTemplate = new Template(null, new StringReader(templateText), cfg);

            // create temporary storage for formatted address
            final StringWriter out = new StringWriter(1000);
            fmTemplate.process(repo, out);

            // create the output
            final MediaData m = new MediaData();
            m.setMediaType(mediaType);
            m.setText(out.toString());
            return m;
        } catch (final Exception e) {
            LOGGER.error("Fail to format document", e);
            return null; // Fail to format document
        }
    }

    // converts a freemarker Model to a Java String
    protected static String fmToString(final Object x) {
        if (x == null) {
            return "";
        }
        if (x instanceof SimpleScalar) {
            return ((SimpleScalar) x).getAsString();
        }
        if (x instanceof StringModel) {
            return ((StringModel) x).getAsString();
        }
        return "(? class " + x.getClass().getCanonicalName() + " ?)";
    }

    public static void clearCache() {
        COMPONENT_CACHE.invalidateAll();
    }

    public DocFormatter() {
        if (cacheInvalidationRegistry != null) {
            cacheInvalidationRegistry.registerInvalidator(DocComponentDTO.class.getSimpleName(), (final BonaPortable it) -> {
                COMPONENT_CACHE.invalidateAll();
            });
        }
    }

    protected ZoneId getZone(final String zoneId) {
        try {
            return ZoneId.of(zoneId == null ? "UTC" : zoneId);
        } catch (Exception e) {
            LOGGER.error("Invalid time zone, exception for ID {}", zoneId);
        }
        return ZoneId.of("UTC");
    }
}
