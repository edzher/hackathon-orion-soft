package ru.fox.orion.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@UtilityClass
public class MDCUtil {

    public static final String TRACE_ID = "trace.id";

    public static final String TRANSACTION_ID = "transaction.id";

    public static final String HTTP_REQUEST_METHOD = "http.request.method";

    public static final String HTTP_REQUEST_BODY_CONTENT = "http.request.body.content";

    public static final String HTTP_REQUEST_BODY_BYTES = "http.request.body.bytes";

    public static final String HTTP_RESPONSE_BODY_BYTES = "http.response.body.bytes";

    public static final String HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";

    public static final String HTTP_RESPONSE_BODY_CONTENT = "http.response.body.content";

    public static final String URL_ORIGINAL = "url.original";

    public static final String URL_DOMAIN = "url.domain";

    public static final String URL_QUERY = "url.query";

    public static final String URL_PATH = "url.path";

    public static final String EVENT_CODE = "event.code";

    public static final String EVENT_ACTION = "event.action";

    public static final String HTTP_REQUEST_HEADERS = "http.request.headers";

    public static final String HTTP_RESPONSE_HEADERS = "http.response.headers";

    public static final String FP_ID = "user.fp.id";

    public static final String PHONE = "user.phone";

    public static final String SUB_ID = "user.sub.id";

    public static final String ERROR_ID = "error.id";

    public static final String ERROR_CODE = "error.code";

    public static final String ERROR_MESSAGE = "error.message";

    public static final String ERROR_TYPE = "error.type";

    public static final String ERROR_STACK_TRACE = "error.stack.trace";


    public static String getTraceAndTransactionId() {
        return String.format("%s-%s", getTraceId(), getTransactionId());
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static String getTransactionId() {
        return MDC.get(TRANSACTION_ID);
    }
}
