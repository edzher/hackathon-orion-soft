package ru.fox.orion.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.MDC;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static ru.fox.orion.core.util.MDCUtil.ERROR_CODE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_MESSAGE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_STACK_TRACE;
import static ru.fox.orion.core.util.MDCUtil.ERROR_TYPE;

@Slf4j
@UtilityClass
public class LogUtil {

    public static final String REQUEST_START_TIME = "requestStartTime";

    static final String DOLLAR_ONE = "$1";
    static final String HIDDEN_STRING = "***HIDDEN***";
    static final String IMAGE_TYPE = "image";

    private static final Pattern PATTERN_FOR_HEADERS = Pattern.compile(
            "^((([a-zA-Z_0-9-]*)credit([a-zA-Z_0-9-]*))|(([a-zA-Z_0-9-]*)(secret|key|token))"
                    + "|(credit|passwd|pwd|sign|set-cookie|authorization): )(.*)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_FOR_PARAMS = Pattern.compile(
            "((^|&)((([a-zA-Z_0-9-]*)credit([a-zA-Z_0-9-]*))|(([a-zA-Z_0-9-]*)(secret|key|token))"
                    + "|(credit|passwd|pwd|sign|set-cookie|authorization))=)([^&]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_FOR_BODY = Pattern.compile(
            "(\"(face_photo|photo|client_login|client_password)\"\s*:\s*)(\"(.*?)\")",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_FOR_BODY_TOKEN = Pattern.compile(
            "(\"(([a-zA-Z_0-9-]*)token)\":\")([^\"]{0,10000})",
            Pattern.CASE_INSENSITIVE
    );

    public static String sanitizeLogWithContentType(String body, MediaType contentType) {
        if (contentType.getType().equals(IMAGE_TYPE)) {
            return HIDDEN_STRING;
        }
        return sanitizeLog(body);
    }

    public static String sanitizeLog(String str) {
        try {
            if (str.startsWith("{") && str.endsWith("}")) {
                return sanitizeBody(escapeUnicode(str));
            } else {
                return sanitizeParams(sanitizeHeaders(str));
            }
        } catch (Throwable e) {
            log.error("Cannot sanitize logs", e);
            return str;
        }
    }

    private String escapeUnicode(String str) {
        return StringEscapeUtils.unescapeJava(str);
    }

    private String sanitizeHeaders(String str) {
        return PATTERN_FOR_HEADERS.matcher(str).replaceAll(DOLLAR_ONE + HIDDEN_STRING);
    }

    private String sanitizeParams(String str) {
        return PATTERN_FOR_PARAMS.matcher(str).replaceAll(DOLLAR_ONE + HIDDEN_STRING);
    }

    private String sanitizeBody(String str) {
        String result = PATTERN_FOR_BODY.matcher(str).replaceAll(DOLLAR_ONE + HIDDEN_STRING);
        return PATTERN_FOR_BODY_TOKEN.matcher(result).replaceAll(DOLLAR_ONE + HIDDEN_STRING);
    }

    public static void logError(Exception ex) {
        try {
            MDC.put(ERROR_STACK_TRACE, getStackTraceString(ex));
            MDC.put(ERROR_MESSAGE, ex.getMessage());
            MDC.put(ERROR_TYPE, ex.getClass().getName());
            log.error(ex.getMessage(), ex);
        } finally {
            MDC.remove(ERROR_STACK_TRACE);
            MDC.remove(ERROR_MESSAGE);
            MDC.remove(ERROR_TYPE);
            MDC.remove(ERROR_CODE);
        }
    }

    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}