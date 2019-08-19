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
package com.arvatosystems.t9t.rep.be.request;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportMailingDTO;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.T9tRepException;
import com.arvatosystems.t9t.rep.be.IJasperReportFiller;
import com.arvatosystems.t9t.rep.request.RunReportRequest;
import com.arvatosystems.t9t.rep.services.IJasperParameterEnricher;
import com.arvatosystems.t9t.rep.services.IRepPersistenceAccess;
import com.arvatosystems.t9t.rep.services.IReportMailNotifier;
import com.arvatosystems.t9t.rep.services.impl.T9tJasperParameterEnricher;
import com.google.common.base.Strings;

import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

/** No new functionality, just an extension in order to avoid conflicts of CDI. */
public class RunReportRequestHandler extends AbstractRequestHandler<RunReportRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunReportRequestHandler.class);

    // names of predefined Jasper parameters
    public static final String DATE_FROM = "dateFrom";
    public static final String DATE_TO = "dateTo";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_REF = "tenantRef";        // obsolete
    public static final String USER_ID = "userId";
    public static final String USER_REF = "userRef";            // obsolete
    public static final String PROCESS_REF = "processRef";
    private static final String JR_TEMPLATES_SUBFOLDER = "reports/src";
    private static final String COMPILED_JR_TEMPLATES_SUBFOLDER = "reports/bin";
    private static final String JR_TEMPLATE_EXT = ".jrxml";
    private static final String COMPILED_JR_TEMPLATE_EXT = ".jasper";
    //protected static final DateTimeFormatter dayFormatter = DateTimeFormat.forStyle("M-").withZoneUTC();  // this gives a local format
    protected static final DateTimeFormatter dayFormatter = ISODateTimeFormat.basicDate(); // this gives yyyyMMdd

    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    private final IJasperParameterEnricher jasperParamEnricher = Jdp.getRequired(IJasperParameterEnricher.class);
    private final IJasperReportFiller jasperReportFiller = Jdp.getRequired(IJasperReportFiller.class);
    private final IRepPersistenceAccess dpl = Jdp.getRequired(IRepPersistenceAccess.class);
    private final IReportMailNotifier reportNotifier = Jdp.getRequired(IReportMailNotifier.class);

    @Override
    public boolean isReadOnly(RunReportRequest request) {
        return false;
    }

    protected void resolveInterval(Map<String, Object> parameters, LocalDateTime relevantDate, ReportParamsDTO interval,
            Map<String, Object> outputSessionAdditionalParametersList) {
        int millisSinceMidnight = relevantDate.getMillisOfDay();
        // default setting is toDate = most recent midnight
        LocalDateTime fromDate = null, toDate = relevantDate.minus(new Duration(millisSinceMidnight));
        switch (interval.getIntervalCategory()) {
        case BY_DURATION:
            if (interval.getIntervalDays() > 0) {
                fromDate = toDate.minusDays(interval.getIntervalDays());
            } else {
                // sub-day interval: must use different rounding: millisOfDay modulus interval
                if (interval.getIntervalSeconds() > 0) {
                    int relevantDiff = millisSinceMidnight % (interval.getIntervalSeconds() * 1000);
                    toDate = relevantDate.minus(new Duration(relevantDiff));
                } else {
                    toDate = relevantDate;
                }
                fromDate = toDate.minusSeconds(interval.getIntervalSeconds());
            }
            break;
        case BY_RANGE:
            fromDate = interval.getFromDate();
            toDate   = interval.getToDate();
            break;
        case BY_TIME:
            int factor = interval.getFactor() == null ? 1 : interval.getFactor().intValue();
            switch (interval.getInterval()) {
            case DAILY:
                fromDate = toDate.minusDays(factor);
                break;
            case MINUTELY:
                toDate = relevantDate.minus(new Duration(millisSinceMidnight % 60000));
                fromDate = toDate.minusMinutes(factor);
                break;
            case HOURLY:
                toDate = relevantDate.minus(new Duration(millisSinceMidnight % 3600000));
                fromDate = toDate.minusHours(factor);
                break;
            case MONTHLY:
                fromDate = toDate.minusMonths(factor);
                break;
            case WEEKLY:
                fromDate = toDate.minusWeeks(factor);
                break;
            case YEARLY:
                fromDate = toDate.minusYears(factor);
                break;
            default:
                throw new T9tException(T9tRepException.BAD_INTERVAL, interval.getInterval().toString());
            }
            break;
        default:
            throw new T9tException(T9tRepException.BAD_INTERVAL_CLASS, interval.getIntervalCategory().toString());
        }
        LOGGER.info("Report run for period from {} to {}", fromDate.toString(), toDate.toString());
        parameters.put(DATE_FROM, fromDate);
        parameters.put(DATE_TO, toDate);
        outputSessionAdditionalParametersList.put("reportDateFrom", dayFormatter.print(fromDate));
        outputSessionAdditionalParametersList.put("reportDateTo", dayFormatter.print(toDate.minusSeconds(1)));
    }


    protected void addDefaultParameters(RequestContext ctx, Map<String, Object> parameters) {
        parameters.put(PROCESS_REF, ctx.requestRef);
        parameters.put(TENANT_ID,   ctx.tenantId);
        parameters.put(USER_ID,     ctx.userId);
        parameters.put(TENANT_REF,  ctx.tenantRef);
        parameters.put(USER_REF,    ctx.userRef);
//        parameters.put(DIALECT, configuration.getDatabaseDialect().toString());
    }



    // filter parameters suitable for slim debugging (do not output whole objects)
    private Map<String, Object> filterBasic(Map<String, Object> parameters) {
        Map<String, Object> mapForOutput = new HashMap<>(parameters.size());
        for (String k : parameters.keySet()) {
            Object val = parameters.get(k);
            if ((val != null)
                    && ((val instanceof String) || (val instanceof Integer) || (val instanceof Long) || (val instanceof LocalDate)
                            || (val instanceof LocalDateTime) || (val instanceof Date) || (val instanceof Boolean))) {
                mapForOutput.put(k, val);
            }
        }
        return mapForOutput;
    }

    @Override
    public SinkCreatedResponse execute(RunReportRequest request) throws Exception {
        Long sinkRef; // holds the result
        final Map<String, Object> outputSessionAdditionalParametersList = new HashMap<String, Object>(10);
        // @Inject
        final RequestContext ctx = Jdp.getRequired(RequestContext.class);

        final ReportParamsDTO reportParamsDTO = (request.getReportParamsRef() instanceof ReportParamsDTO) ?
            // nothing to do, all data has been provided (adhoc report request)
            (ReportParamsDTO) request.getReportParamsRef()
        :
            dpl.getParamsDTO(request.getReportParamsRef())
        ;

        final ReportConfigDTO reportConfigDTO = dpl.getConfigDTO(reportParamsDTO.getReportConfigRef());

        outputSessionAdditionalParametersList.put("reportConfigId", reportConfigDTO.getReportConfigId());
        outputSessionAdditionalParametersList.put("reportParamsId", reportParamsDTO.getReportParamsId());

        MediaType outputFileType = reportParamsDTO.getOutputFileType();

        Instant relevantDate = ctx.internalHeaderParameters.getPlannedRunDate();
        if (relevantDate == null) {
            relevantDate = new Instant();
        }

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;

        try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
            jasperReport = loadJasperReport(ctx, reportConfigDTO.getJasperReportTemplateName());

            OutputSessionParameters outputSessionParameters = new OutputSessionParameters();
            Map<String, Object> parameters = new HashMap<String, Object>();
            addDefaultParameters(ctx, parameters);
            jasperParamEnricher.enrichParameter(parameters, reportParamsDTO, outputSessionAdditionalParametersList, outputSessionParameters);

            //if timezone information still empty after enricher. set timezone
            if (ctx.internalHeaderParameters != null && !Strings.isNullOrEmpty(ctx.internalHeaderParameters.getJwtInfo().getZoneinfo())) {
                parameters.putIfAbsent(T9tJasperParameterEnricher.TIME_ZONE, ctx.internalHeaderParameters.getJwtInfo().getZoneinfo());
            }
            else {
                parameters.putIfAbsent(T9tJasperParameterEnricher.TIME_ZONE, "UTC");
            }

            if (reportParamsDTO.getIntervalCategory() != null) {
                resolveInterval(parameters, LocalDateTime.now(), reportParamsDTO, outputSessionAdditionalParametersList);
            }

            if (LOGGER.isInfoEnabled()) {
                // avoid extensive object creation unless INFO is active
                LOGGER.info("Basic JASPER parameters are: {}", filterBasic(parameters).toString());
            }
            outputSessionParameters.setAdditionalParameters(outputSessionAdditionalParametersList);
            outputSessionParameters.setOriginatorRef(reportParamsDTO.getObjectRef());
            outputSessionParameters.setConfigurationRef(reportConfigDTO.getObjectRef());
            outputSessionParameters.setAsOf(relevantDate);
            outputSessionParameters.setDataSinkId(reportParamsDTO.getDataSinkId());
            outputSessionParameters.setCommunicationFormatType(outputFileType); // might be overwritten by main configuration -- PDF, etc
            sinkRef = outputSession.open(outputSessionParameters);

            // fill additional parameters
            if (reportParamsDTO != null && reportParamsDTO.getZ() != null && !reportParamsDTO.getZ().isEmpty()) {
                parameters.putAll(reportParamsDTO.getZ());
            }

            jasperPrint = jasperReportFiller.fillReport(jasperReport, reportParamsDTO, parameters);
            Exporter<ExporterInput, ?, ?, ?> exporter = null;

            MediaXType tmpType = outputSession.getCommunicationFormatType(); // get merged type (with configuration)
            if (tmpType == null || !(tmpType.getBaseEnum() instanceof MediaType)) {
                throw new T9tException(T9tRepException.JASPER_REPORT_CREATION_JR_EXCEPTION, "null or bad OFT");
            }
            outputFileType = (MediaType)(tmpType.getBaseEnum());
            switch (outputFileType) {
            case CSV:
                JRCsvExporter exporterCsv = new JRCsvExporter();
                exporterCsv.setExporterOutput(new SimpleWriterExporterOutput(outputSession.getOutputStream()));
                exporter = exporterCsv;
                break;
            case JSON:
                JRCsvExporter exporterJson = new JRCsvExporter();
                exporterJson.setExporterOutput(new SimpleWriterExporterOutput(outputSession.getOutputStream()));
                exporter = exporterJson;
                break;
            case HTML:
                HtmlExporter exporterHtml = new HtmlExporter();
                exporterHtml.setExporterOutput(new SimpleHtmlExporterOutput(outputSession.getOutputStream()));
                exporter = exporterHtml;
                break;
            case PDF:
                JRPdfExporter exporterPdf = new JRPdfExporter();
                exporterPdf.setExporterOutput(new SimpleOutputStreamExporterOutput(outputSession.getOutputStream()));
                exporter = exporterPdf;
                break;
            case XLS:
                JRXlsExporter exporterXls = new JRXlsExporter();
                exporterXls.setExporterOutput(new SimpleOutputStreamExporterOutput(outputSession.getOutputStream()));
                exporter = exporterXls;
                break;
            case XLSX:
                JRXlsxExporter exporterXlsx = new JRXlsxExporter();
                exporterXlsx.setExporterOutput(new SimpleOutputStreamExporterOutput(outputSession.getOutputStream()));
                exporter = exporterXlsx;
                break;
            default:
                throw new T9tException(T9tRepException.JASPER_REPORT_CREATION_JR_EXCEPTION, outputFileType);
            }
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

//            outputSession.close();
        } catch (JRException e) {
            LOGGER.error("Jasper throw an exception:", e);
            throw new T9tException(T9tRepException.JASPER_REPORT_CREATION_JR_EXCEPTION, e.getMessage());
        }

        if (reportParamsDTO.getMailingGroupRef() != null) {
            ReportMailingDTO reportMailingDTO = ((ReportMailingDTO)reportParamsDTO.getMailingGroupRef()); // already mapper in ReportParam dto mapper
            DocumentSelector selector= new DocumentSelector("-", ctx.internalHeaderParameters.getLanguageCode() != null ? ctx.internalHeaderParameters.getLanguageCode() : "en", "XX", "XXX");
            reportNotifier.sendEmail(reportConfigDTO, reportParamsDTO, reportMailingDTO.getMailingList(), reportMailingDTO.getDocConfigId(), sinkRef, selector);
        }

        SinkCreatedResponse response = new SinkCreatedResponse();
        response.setReturnCode(0);
        response.setSinkRef(sinkRef);
        return response;
    }

    private JasperReport loadJasperReport(RequestContext ctx, String pathToReport) throws JRException {
        String currentTenantId = ctx.tenantId;
        String filesRootLocation = fileUtil.getFilePathPrefix();

        JasperReport report = getCompiledJrTemplate(filesRootLocation, currentTenantId, pathToReport);
        if (report == null) {
            report = getCompiledJrTemplate(filesRootLocation, T9tConstants.GLOBAL_TENANT_ID, pathToReport);
        }

        if (report == null) {
            LOGGER.error("Report template was not found. Root location: '{}', tenant '{}', templates subfolder: '{}', template name: '{}'", filesRootLocation,
                    currentTenantId, JR_TEMPLATES_SUBFOLDER, pathToReport);
            throw new T9tException(T9tRepException.JASPER_REPORT_CREATION_JR_EXCEPTION);
        }

        return report;
    }

    private JasperReport getCompiledJrTemplate(String filesRootLocation, String tenantId, String jrTemplateRelativePath) throws JRException {
        String jrTemplatePath = fileUtil.buildPath(filesRootLocation, tenantId, JR_TEMPLATES_SUBFOLDER, jrTemplateRelativePath);
        LOGGER.debug("Report template path: '{}'", jrTemplatePath);

        File jrTemplateFile = new File(jrTemplatePath);
        if (!jrTemplateFile.exists()) {
            LOGGER.info("Report template does not exist. {}", jrTemplatePath);
            return null;
        }

        String compiledTemplatePath = buildCompiledJrTemplatePath(filesRootLocation, tenantId, jrTemplateRelativePath);
        LOGGER.debug("Compiled report template path: '{}'", compiledTemplatePath);
        File compiledJrTemplateFile = new File(compiledTemplatePath);

        if (!compiledJrTemplateFile.exists() || (compiledJrTemplateFile.lastModified() < jrTemplateFile.lastModified())) {
            fileUtil.createFileLocation(compiledTemplatePath);
            JasperCompileManager.compileReportToFile(jrTemplateFile.getAbsolutePath(), compiledJrTemplateFile.getAbsolutePath());
            compiledJrTemplateFile.setLastModified(new LocalDateTime().toDate().getTime());
        }

        return (JasperReport) JRLoader.loadObject(compiledJrTemplateFile);
    }

    private String buildCompiledJrTemplatePath(String filesRootLocation, String tenantId, String pathToReport) {
        String compilerJrTemplatePath;

        if (pathToReport.endsWith(JR_TEMPLATE_EXT)) {
            int ind = pathToReport.lastIndexOf(JR_TEMPLATE_EXT);
            compilerJrTemplatePath = pathToReport.substring(0, ind) + COMPILED_JR_TEMPLATE_EXT;
        } else {
            compilerJrTemplatePath = pathToReport + COMPILED_JR_TEMPLATE_EXT;
        }

        return fileUtil.buildPath(filesRootLocation, tenantId, COMPILED_JR_TEMPLATES_SUBFOLDER, compilerJrTemplatePath);
    }
}
