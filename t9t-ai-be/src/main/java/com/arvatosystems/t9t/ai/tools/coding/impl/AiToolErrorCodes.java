package com.arvatosystems.t9t.ai.tools.coding.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.coding.AiToolExplainErrorCode;
import com.arvatosystems.t9t.ai.tools.coding.AiToolExplainErrorCodeResult;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Named(AiToolExplainErrorCode.my$PQON)
@Singleton
public class AiToolErrorCodes implements IAiTool<AiToolExplainErrorCode, AiToolExplainErrorCodeResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiToolErrorCodes.class);

    @Override
    public AiToolExplainErrorCodeResult performToolCall(final RequestContext ctx, final AiToolExplainErrorCode request) {
        final AiToolExplainErrorCodeResult result = new AiToolExplainErrorCodeResult();
        final int errorCode = request.getErrorCode();
        LOGGER.info("AiToolErrorCodes.performToolCall called with parameter: {}", errorCode);
        if (errorCode < 0 || errorCode >= 10 * ApplicationException.CLASSIFICATION_FACTOR) {
            result.setDescription("Error code " + errorCode + " is not in the valid range of codes");
        } else {
            result.setDescription(ApplicationException.codeToString(errorCode));
            result.setClassification(errorCode / ApplicationException.CLASSIFICATION_FACTOR);
        }
        return result;
    }
}
