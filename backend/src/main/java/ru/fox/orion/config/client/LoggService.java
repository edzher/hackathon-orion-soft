package ru.fox.orion.config.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import ru.fox.orion.core.util.LogUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ru.fox.orion.core.util.MDCUtil.HTTP_REQUEST_BODY_BYTES;
import static ru.fox.orion.core.util.MDCUtil.HTTP_REQUEST_BODY_CONTENT;
import static ru.fox.orion.core.util.MDCUtil.HTTP_REQUEST_HEADERS;
import static ru.fox.orion.core.util.MDCUtil.HTTP_REQUEST_METHOD;
import static ru.fox.orion.core.util.MDCUtil.HTTP_RESPONSE_BODY_BYTES;
import static ru.fox.orion.core.util.MDCUtil.HTTP_RESPONSE_BODY_CONTENT;
import static ru.fox.orion.core.util.MDCUtil.HTTP_RESPONSE_HEADERS;
import static ru.fox.orion.core.util.MDCUtil.HTTP_RESPONSE_STATUS_CODE;
import static ru.fox.orion.core.util.MDCUtil.URL_DOMAIN;
import static ru.fox.orion.core.util.MDCUtil.URL_ORIGINAL;
import static ru.fox.orion.core.util.MDCUtil.URL_PATH;
import static ru.fox.orion.core.util.MDCUtil.URL_QUERY;

@RequiredArgsConstructor
public class LoggService {

    private final Logger logger;
    private final String serviceName;

    public LoggService(String serviceName) {
        this.logger = LoggerFactory.getLogger(serviceName);
        this.serviceName = serviceName;
    }

    public void logResponse(ClientHttpResponse response, HttpRequest request, long duration) throws IOException {
        try {
            logHttpResponse(response, request, duration);
        } finally {
            MDC.remove(URL_ORIGINAL);
            MDC.remove(URL_DOMAIN);
            MDC.remove(URL_PATH);
            MDC.remove(URL_QUERY);
            MDC.remove(HTTP_REQUEST_METHOD);
            MDC.remove(HTTP_RESPONSE_STATUS_CODE);
            MDC.remove(HTTP_RESPONSE_HEADERS);
            if (response.getBody().available() > 0) {
                MDC.remove(HTTP_RESPONSE_BODY_CONTENT);
                MDC.remove(HTTP_RESPONSE_BODY_BYTES);
            }
        }
    }

    private void logHttpResponse(ClientHttpResponse response, HttpRequest request, long duration) throws IOException {
        MDC.put(HTTP_RESPONSE_STATUS_CODE, String.valueOf(response.getStatusCode().value()));

        String logMessage = String.format("\n %s RESPONSE: <-- %s %s (%s)\n", serviceName,
                response.getStatusCode().value(), request.getURI(), duration);

        MDC.put(HTTP_REQUEST_METHOD, request.getMethod().name());
        putUrlMDC(request);

        String headers = extractHeaders(request.getHeaders());
        if (!headers.isEmpty()) {
            MDC.put(HTTP_RESPONSE_HEADERS, headers);
        }

        if (response.getBody().available() > 0) {
            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            responseBody = LogUtil.sanitizeLog(responseBody);
            MDC.put(HTTP_RESPONSE_BODY_CONTENT, responseBody);
            MDC.put(HTTP_RESPONSE_BODY_BYTES, String.valueOf(responseBody.getBytes().length));
        }

        logger.info(logMessage);
    }

    public void logRequest(HttpRequest request, byte[] body) {
        try {
            logHttpRequest(request, body);
        } finally {
            if (body != null) {
                MDC.remove(HTTP_REQUEST_BODY_CONTENT);
                MDC.remove(HTTP_REQUEST_BODY_BYTES);
            }
            MDC.remove(HTTP_REQUEST_HEADERS);
            MDC.remove(HTTP_REQUEST_METHOD);
            MDC.remove(URL_ORIGINAL);
            MDC.remove(URL_DOMAIN);
            MDC.remove(URL_PATH);
            MDC.remove(URL_QUERY);
        }
    }

    private void logHttpRequest(HttpRequest request, byte[] body) {
        putUrlMDC(request);

        String headers = extractHeaders(request.getHeaders());
        MDC.put(HTTP_REQUEST_HEADERS, headers);

        if (body != null && body.length > 0) {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            String sanitizedBody;
            HttpHeaders httpHeaders = request.getHeaders();
            if (httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE) && httpHeaders.getContentType() != null) {
                sanitizedBody = LogUtil.sanitizeLogWithContentType(bodyString, httpHeaders.getContentType());
            } else {
                sanitizedBody = LogUtil.sanitizeLog(bodyString);
            }

            MDC.put(HTTP_REQUEST_BODY_BYTES, String.valueOf(body.length));
            MDC.put(HTTP_REQUEST_BODY_CONTENT, sanitizedBody);
        }

        MDC.put(HTTP_REQUEST_METHOD, request.getMethod().toString());
        String logMessage = String.format("\n %s REQUEST: --> %s %s \n", serviceName, request.getMethod(), request.getURI());
        logger.info(logMessage);
    }

    private void putUrlMDC(HttpRequest request) {
        MDC.put(URL_ORIGINAL, request.getURI().toString());
        MDC.put(URL_DOMAIN, request.getURI().getHost());
        MDC.put(URL_PATH, request.getURI().getPath());
        MDC.put(URL_QUERY, request.getURI().getQuery());
    }

    private String extractHeaders(HttpHeaders headers) {
        StringBuilder headerBuilder = new StringBuilder();
        headers.toSingleValueMap().forEach((key, value) -> {
            if (HttpHeaders.AUTHORIZATION.equals(key)) {
                headerBuilder.append("Authorization: [*HIDDEN*]\n");
            } else {
                headerBuilder.append(key).append(": ").append(value).append("\n");
            }
        });
        return headerBuilder.toString();
    }
}