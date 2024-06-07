package com.arvatosystems.t9t.ai.tools.coding.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.IAITool;
import com.arvatosystems.t9t.ai.tools.coding.AIToolExplainErrorCode;
import com.arvatosystems.t9t.ai.tools.coding.AIToolExplainErrorCodeResult;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Named(AIToolExplainErrorCode.my$PQON)
@Singleton
public class AIToolErrorCodes implements IAITool<AIToolExplainErrorCode, AIToolExplainErrorCodeResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIToolErrorCodes.class);

    @Override
    public AIToolExplainErrorCodeResult performToolCall(final RequestContext ctx, final AIToolExplainErrorCode request) {
        final AIToolExplainErrorCodeResult result = new AIToolExplainErrorCodeResult();
        final int errorCode = request.getErrorCode();
        LOGGER.info("AIToolErrorCodes.performToolCall called with parameter: {}", errorCode);
        if (errorCode < 0 || errorCode >= 10 * ApplicationException.CLASSIFICATION_FACTOR) {
            result.setDescription("Error code " + errorCode + " is not in the valid range of codes");
        } else {
            result.setDescription(ApplicationException.codeToString(errorCode));
            result.setClassification(errorCode / ApplicationException.CLASSIFICATION_FACTOR);
        }
        return result;
    }
}
