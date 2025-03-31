/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
import java.util.Base64;
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
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.T9tDocException;
import com.arvatosystems.t9t.doc.T9tDocTools;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.TemplateType;
import com.arvatosystems.t9t.doc.be.converters.impl.ConverterToHtmlUsingCids;
import com.arvatosystems.t9t.doc.services.IDocComponentConverter;
import com.arvatosystems.t9t.doc.services.IDocFormatter;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess;
import com.arvatosystems.t9t.doc.services.IDocTextReplacer;
import com.arvatosystems.t9t.doc.services.IImageGenerator;
import com.arvatosystems.t9t.doc.services.ImageParameter;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;
import freemarker.core.TemplateClassResolver;
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

    protected enum DocumentEncoderType {
        BASE64, BASE64URL
    }

    /** Class serves as an internal key to the cached components. */
    record ComponentCacheKey(String tenantId, DocumentSelector selector) {
    }

    /** Class serves as a debugging aid. */
    record DebugModel() implements TemplateMethodModelEx {
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
    record TimestampGeneratorModel (Locale locale, ZoneId zone) implements TemplateMethodModelEx {
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
            final String isoString       = fmToString(arguments, 0);
            final String style           = fmToString(arguments, 1);
            final String forceConversion = fmToString(arguments, 2);
            final String dateTimeFormat  = fmToString(arguments, 3);

            if (style.length() != 2) {
                LOGGER.warn("Bad parameter: argument 2 for TimestampGenerator must be a 2 character string, but is {}", style);
            }
            if (isoString == null) {
                LOGGER.warn("null or empty string passed to date formatter with style {} - returning empty string", style);
                return "";
            }

            final DateTimeFormatter formatter = dateTimeFormat == null ? getFormatterForStyle(style) : DateTimeFormatter.ofPattern(dateTimeFormat);
            final DateTimeFormatter localFormatter = formatter.withLocale(locale);

            if (forceConversion != null) {
                try {
                    // arg 3 to convert timestamp to day or time, and apply the style formatter
                    switch (forceConversion) {
                    case "D": // timestamp to date, interpreted as day
                        // workaround for bad input: adapt to cases when just a date is given
                        final int l = isoString.length();
                        if (l == 10) {
                            // do the same as for 'd'
                            LOGGER.warn("Date/time conversion mismatch: force=\'D\', style={}, format={}, input={}: You should use \'d\' in this case!", style,
                                    dateTimeFormat, isoString);
                            final LocalDate timestampDayT = LocalDate.parse(isoString);
                            return localFormatter.format(timestampDayT);
                        } else {
                            // another workaround for for extra Z suffix: drop it, it would cause a conversion error
                            final boolean gotTimezoneSuffix = isoString.endsWith("Z");
                            if (gotTimezoneSuffix) {
                                LOGGER.warn("Date/time conversion mismatch: force='D', style={}, format={}, input={}: Dropping time zone suffix."
                                        + " Bug in data provider?", style, dateTimeFormat, isoString);
                            }
                            final LocalDateTime timestampDayT = LocalDateTime.parse(gotTimezoneSuffix ? isoString.substring(0, l - 1) : isoString);
                            // timezone conversion
                            final LocalDateTime timestampDayTt = timestampDayT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                            return localFormatter.format(timestampDayTt.toLocalDate());
                        }
                    case "T": // timestamp to date, interpreted as time
                        final LocalDateTime timestampTimeT = LocalDateTime.parse(isoString);
                        // timezone conversion
                        final LocalDateTime timestampTimeTt = timestampTimeT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                        return localFormatter.format(timestampTimeTt.toLocalTime());
                    case "d": // day
                        final LocalDate dayT = LocalDate.parse(isoString);
                        return localFormatter.format(dayT);
                    case "t": // time
                        final LocalTime timeT = LocalTime.parse(isoString);
                        return localFormatter.format(timeT);
                    case "i": // instant
                        final LocalDateTime instantT = LocalDateTime.parse(isoString);
                        final LocalDateTime instantTt = instantT.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime();
                        return instantTt.format(localFormatter);
                    default:
                        LOGGER.error("Unknown conversion rule: {}", forceConversion);
                        break;
                    }
                } catch (final Exception e) {
                    LOGGER.error("Date/time conversion error: force={}, style={}, format={}, input={}", forceConversion, style, dateTimeFormat, isoString);
                    return "***ERROR***";
                }
            }

            try {
                if (style.charAt(0) == MINUS) {
                    // time only
                    final LocalTime t = LocalTime.parse(isoString);
                    return localFormatter.format(t);
                } else if (style.charAt(1) == MINUS) {
                    // day only
                    final LocalDate t = LocalDate.parse(isoString);
                    return localFormatter.format(t);
                } else {
                    // day + time
                    final LocalDateTime t = LocalDateTime.parse(isoString);
                    final LocalDateTime tt = t.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime(); // timezone conversion
                    return tt.format(localFormatter);
                }
            } catch (Exception e) {
                LOGGER.error("Date/time conversion error: style={}, format={}, input={}", forceConversion, style, dateTimeFormat, isoString);
                return "***ERROR***";
            }
        }

        private DateTimeFormatter getFormatterForStyle(final String style) {
            if (style == null || style.length() != 2) {
                LOGGER.error("Bad style given: {}, returning default formatter", style);
                return DateTimeFormatter.ISO_INSTANT;
            }
            if (style.charAt(0) == MINUS) {
                // time only
                return DateTimeFormatter.ofLocalizedTime(T9tDocTools.styleFor(style.charAt(1)));
            } else if (style.charAt(1) == MINUS) {
                // day only
                return DateTimeFormatter.ofLocalizedDate(T9tDocTools.styleFor(style.charAt(0)));
            } else {
                return DateTimeFormatter.ofLocalizedDateTime(T9tDocTools.styleFor(style.charAt(0)), T9tDocTools.styleFor(style.charAt(1)));
            }
        }
    }

    /** Class serves as a getter to dynamic barcode creation. */
    record ImageGeneratorModel(MediaXType desiredFormat, IDocComponentConverter converter) implements TemplateMethodModelEx {
        // parameters are of type freemarker.template.SimpleScalar / SimpleNumber etc.
        @SuppressWarnings("rawtypes")
        @Override
        public Object exec(final List arguments) throws TemplateModelException {
            final int argumentsSize = arguments.size();
            if (argumentsSize < 2) {
                throw new TemplateModelException("ImageGenerator model (i) called with less than 2 parameters");
            }
            final String format = fmToString(arguments, 0);
            final String text = fmToString(arguments, 1);
            int width = 0;
            int height = 0;
            int rotation = 0;
            FlipMode flipMode = FlipMode.NO_FLIPPING;

            if (argumentsSize >= 4) {
                final Object widthObj = arguments.get(2);
                final Object heightObj = arguments.get(3);
                if (widthObj instanceof SimpleNumber widthObjSimpleNumber) {
                    width = widthObjSimpleNumber.getAsNumber().intValue();
                } else {
                    LOGGER.warn("Bad parameter: argument 3 of i (width) should be an integer (SimpleNumber), but is {}",
                            widthObj.getClass().getCanonicalName());
                }
                if (heightObj instanceof SimpleNumber heightObjSimpleNumber) {
                    height = heightObjSimpleNumber.getAsNumber().intValue();
                } else {
                    LOGGER.warn("Bad parameter: argument 4 of i (height) should be an integer (SimpleNumber), but is {}",
                            heightObj.getClass().getCanonicalName());
                }
            }

            if (argumentsSize >= 5) {
                final Object rotObj = arguments.get(4);
                if (rotObj instanceof SimpleNumber rotObjSimpleNumber) {
                    rotation = rotObjSimpleNumber.getAsNumber().intValue();
                } else if (rotObj != null) {
                    LOGGER.warn("Bad parameter: argument 5 of i (rotation) should be an integer (SimpleNumber), but is {}",
                            rotObj.getClass().getCanonicalName());
                }
            }

            final String flipModeS = fmToString(arguments, 5);
            if (flipModeS != null) {
                switch (flipModeS) {
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
                    LOGGER.warn("Bad parameter: argument 6 of i (flip mode) should be either H or V or N, found {}", flipModeS);
                    break;
                }
            }

            LOGGER.debug("ImageGenerator({}, {}, {}, {}) called", format, text, width, height);
            // barcode V2: generate a generic image
            final String encoding = fmToString(arguments, 6);
            final String qualifier = fmToString(arguments, 7);
            final ImageParameter parameters = new ImageParameter(width, height, Integer.valueOf(rotation), flipMode, null, encoding, qualifier, 0, 0xffffff);
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
    }

    record EncoderModel(DocumentEncoderType conversionType) implements TemplateMethodModelEx {

        @Override
        public Object exec(final List args) throws TemplateModelException {
            if (args.size() != 1 || args.get(0) == null) {
                throw new TemplateModelException("EncoderModel must be called with exactly one non-null parameter");
            }
            final String parameterAsText = args.get(0).toString();
            switch (conversionType) {
            case BASE64:
                return Base64.getEncoder().encodeToString(parameterAsText.getBytes(Charsets.UTF_8));
            case BASE64URL:
                return Base64.getUrlEncoder().encodeToString(parameterAsText.getBytes(Charsets.UTF_8));
            default:
                return "";  // not yet supported
            }
        }
    }

    /**
     * Class serves as a character replacer.
     * The first parameter is the input string (the string to convert).
     * The second parameter is a qualifier to use when pulling an implementation, to allow using multiple replacers.
     * The third parameter is a string of characters which should be treated as separator. The first of them will be used as space.
     */
    record CharacterReplacerModel(Locale locale) implements TemplateMethodModelEx {

        @Override
        public Object exec(List args) throws TemplateModelException {
            if (args.size() < 1 || args.get(0) == null) {
                throw new TemplateModelException("CharacterReplacerModel must be called with at least one non-null parameter");
            }
            final String inputString = args.get(0).toString();
            final String qualifier = args.size() >= 2 && args.get(1) != null ? args.get(1).toString() : null;
            final String spaceChars = args.size() >= 3 && args.get(2) != null ? args.get(2).toString() : null;
            final IDocTextReplacer replacer = Jdp.getOptional(IDocTextReplacer.class, qualifier);
            if (replacer == null) {
                LOGGER.error("No implementation found for IDocTextReplacer with qualifier {}", qualifier);
                throw new TemplateModelException("No implementation found for IDocTextReplacer with qualifier " + qualifier);
            }
            return WRAPPER.wrap(replacer.textReplace(locale, inputString, spaceChars));
        }
    }

    /** Class resolves URLs and creates base64 data from it. */
    record Base64FromUrlGeneratorModel() implements TemplateMethodModelEx {
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
    record PropertyTranslationsFuncModel(String language, String[] languages, ITranslationProvider translationProvider) implements TemplateMethodModelEx {

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
    record PropertyTranslations (String language, String[] languages, ITranslationProvider translationProvider) implements TemplateHashModel {

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
    record ComponentAccessor(Map<String, MediaData> components, MediaXType desiredFormat,
            IDocComponentConverter converter, String tenantId) implements TemplateHashModel {

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
            if (obj instanceof String sObj) {
                final Enum<?> format = desiredFormat != null ? desiredFormat.getBaseEnum() : null;
                if (escapeToRaw && (format == MediaType.HTML || format == MediaType.XHTML)) {
                    return new SimpleScalar("<![CDATA[" + sObj + "]]>");
                }
                if (converter != null) {
                    final MediaData md = new MediaData(MediaTypes.MEDIA_XTYPE_TEXT);
                    md.setText(sObj);
                    return super.wrap(converter.convertFrom(md));
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

            final ITranslationProvider translationProvider = Jdp.getOptional(ITranslationProvider.class);
            final String[] languages = translationProvider != null ? translationProvider.resolveLanguagesToCheck(languageCode, true) : null;

            final Map<String, Object> map = new HashMap<>(16);
            map.put("s", new BeanModel(selector, WRAPPER));
            map.put("d", new BeanModel(data, new EscapingWrapper(mediaType, converter, escapeToRaw)));
            map.put("c", new ComponentAccessor(components, mediaType, converter, sharedTenantId));
            map.put("p", new PropertyTranslations(languageCode, languages, translationProvider));
            map.put("q", new PropertyTranslationsFuncModel(languageCode, languages, translationProvider));
            map.put("i", new ImageGeneratorModel(mediaType, converter));
            map.put("u", new Base64FromUrlGeneratorModel());
            map.put("r", new CharacterReplacerModel(myLocale));
            map.put("base64",    new EncoderModel(DocumentEncoderType.BASE64));
            map.put("base64url", new EncoderModel(DocumentEncoderType.BASE64URL));
            map.put("t", new TimestampGeneratorModel(myLocale, effectiveTimeZone));
            map.put("e", eRepo);
            map.put("debug", new DebugModel());
            final MapModel repo = new MapModel(map, WRAPPER);

            final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
            cfg.setLocale(myLocale);
            cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
            Template fmTemplate = new Template(null, new StringReader(templateText), cfg);

            // create temporary storage for formatted address
            final StringWriter out = new StringWriter(1000);
            fmTemplate.process(repo, out);

            // create the output
            final MediaData m = new MediaData();
            m.setMediaType(mediaType);
            m.setText(out.toString());
            return m;
        } catch (final ApplicationException e) {
            LOGGER.error("Problem formatting the document: code {}: {} {}", e.getErrorCode(), e.getMessage(), ExceptionUtil.causeChain(e));
            throw e;
        } catch (final Exception e) {
            LOGGER.error("Problem formatting the document", e);
            throw new T9tException(T9tDocException.FORMATTING_ERROR);
        }
    }

    // converts a freemarker Model to a Java String
    protected static String fmToString(final List arguments, final int pos) {
        if (arguments.size() <= pos) {
            return null;
        }
        final Object x = arguments.get(pos);
        if (x == null) {
            return null;
        }
        if (x instanceof SimpleScalar xSimpleScalar) {
            return xSimpleScalar.getAsString();
        }
        if (x instanceof StringModel xStringModel) {
            return xStringModel.getAsString();
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
