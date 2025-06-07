package ru.fox.orion.core.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.fox.orion.config.MessageTranslator;

import static ru.fox.orion.core.util.MDCUtil.ERROR_CODE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_MESSAGE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_STACK_TRACE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_TYPE;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseExceptionHandler extends ResponseEntityExceptionHandler {
    protected final MessageTranslator messageTranslator;

    protected String tryTranslate(String responseMessage, Object[] args) {
        try {
            if (args != null) {
                return messageTranslator.get(responseMessage, args);
            }
            return messageTranslator.get(responseMessage);
        } catch (Exception e) {
            return responseMessage;
        }
    }

    protected void logError(Exception ex, HttpStatus httpStatus) {
        try {
            MDC.put(ERROR_STACK_TRACE, getStackTraceString(ex));
            MDC.put(ERROR_MESSAGE, ex.getMessage());
            MDC.put(ERROR_TYPE, ex.getClass().getName());
            MDC.put(ERROR_CODE, httpStatus.name());
            log.error(ex.getMessage(), ex);
        } finally {
            MDC.remove(ERROR_STACK_TRACE);
            MDC.remove(ERROR_MESSAGE);
            MDC.remove(ERROR_TYPE);
            MDC.remove(ERROR_CODE);
        }
    }

    protected String getStackTraceString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}