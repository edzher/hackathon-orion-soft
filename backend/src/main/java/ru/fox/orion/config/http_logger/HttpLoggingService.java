package ru.fox.orion.config.http_logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static ru.fox.orion.core.util.LogUtil.REQUEST_START_TIME;
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

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CONTROLLER_LOGGER")
public class HttpLoggingService {

    private static final String ERROR_STRING = "ERROR: ";
    private static final String SPACE_STRING = " ";
    private final ObjectMapper objectMapper;

    public void logRequest(HttpServletRequest httpServletRequest, Object body) {
        String originalUrl = buildOriginalUrl(httpServletRequest);
        MDC.put(URL_ORIGINAL, originalUrl);

        putRequestDetailsToMDC(httpServletRequest);

        String headers = extractAndSanitizeHeaders(httpServletRequest);
        MDC.put(HTTP_REQUEST_HEADERS, headers);

        if (body != null) {
            handleRequestBody(body);
        }

        String result = "REQUEST: --> "
                + httpServletRequest.getMethod()
                + SPACE_STRING
                + httpServletRequest.getRequestURL();

        sanitizeLogMessage(result);

        log.info(result);

        MDC.remove(HTTP_REQUEST_BODY_BYTES);
        MDC.remove(HTTP_REQUEST_HEADERS);
        MDC.remove(HTTP_REQUEST_METHOD);
        MDC.remove(HTTP_REQUEST_BODY_CONTENT);
        MDC.remove(URL_ORIGINAL);
        MDC.remove(URL_DOMAIN);
        MDC.remove(URL_PATH);
        MDC.remove(URL_QUERY);
    }

    public void logResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object body) {
        String result = "RESPONSE: <-- "
                + httpServletResponse.getStatus()
                + SPACE_STRING
                + httpServletRequest.getRequestURL().toString();

        Long startTime = (Long) ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getAttribute(REQUEST_START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            result = result + " (" + duration + ")";
        }



        String originalUrl = buildOriginalUrl(httpServletRequest);
        MDC.put(URL_ORIGINAL, originalUrl);

        putResponseDetailsToMDC(httpServletRequest, httpServletResponse);

        if (body != null) {
            handleResponseBody(body);
        }

        sanitizeLogMessage(result);

        log.info(result);
        MDC.remove(HTTP_REQUEST_METHOD);
        MDC.remove(HTTP_RESPONSE_HEADERS);
        MDC.remove(HTTP_RESPONSE_BODY_BYTES);
        MDC.remove(HTTP_RESPONSE_STATUS_CODE);
        MDC.remove(HTTP_RESPONSE_BODY_CONTENT);
        MDC.remove(URL_ORIGINAL);
        MDC.remove(URL_DOMAIN);
        MDC.remove(URL_PATH);
    }

    private String buildOriginalUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURL().toString());
        if (request.getQueryString() != null) {
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }

    private void putRequestDetailsToMDC(HttpServletRequest request) {
        MDC.put(URL_PATH, request.getServletPath());
        MDC.put(URL_DOMAIN, getDomain(request));
        MDC.put(URL_QUERY, request.getQueryString());
        MDC.put(HTTP_REQUEST_METHOD, request.getMethod());
    }

    private void putResponseDetailsToMDC(HttpServletRequest request, HttpServletResponse response) {
        MDC.put(URL_PATH, request.getServletPath());
        MDC.put(URL_DOMAIN, getDomain(request));
        MDC.put(HTTP_RESPONSE_STATUS_CODE, String.valueOf(response.getStatus()));
        MDC.put(HTTP_REQUEST_METHOD, request.getMethod());
    }

    private void handleRequestBody(Object body) {
        try {
            String bodyString = objectMapper.writeValueAsString(body);
            MDC.put(HTTP_REQUEST_BODY_CONTENT, bodyString);
            MDC.put(HTTP_REQUEST_BODY_BYTES, String.valueOf(bodyString.getBytes().length));
        } catch (JsonProcessingException e) {
            MDC.put(HTTP_REQUEST_BODY_CONTENT, ERROR_STRING + e.getMessage());
        }
    }

    private void handleResponseBody(Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            MDC.put(HTTP_RESPONSE_BODY_CONTENT, jsonBody);
            MDC.put(HTTP_RESPONSE_BODY_BYTES, String.valueOf(jsonBody.getBytes().length));
        } catch (JsonProcessingException e) {
            MDC.put(HTTP_RESPONSE_BODY_CONTENT, ERROR_STRING + e.getMessage());
        }
    }

    private String extractAndSanitizeHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        buildHeadersMap(request).forEach((key, value) -> {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(key)) {
                headers.append("Authorization: [*HIDDEN*]\n");
            } else {
                headers.append(key).append(": ").append(value).append("\n");
            }
        });
        return headers.toString();
    }

    private void sanitizeLogMessage(String message) {
        message.replaceAll("authorization=[^,]*", "authorization=***")
                .replaceAll("\"refreshToken\":\"[^\"]*", "\"refreshToken\":\"***")
                .replaceAll("\"accessToken\":\"[^\"]*", "\"accessToken\":\"***")
                .replaceAll("\"password\":\"[^\"]*", "\"password\":\"***");
    }

    private String getDomain(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        try {
            URI uri = new URI(requestURL);
            return uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            map.put(header, response.getHeader(header));
        }

        return map;
    }
}
