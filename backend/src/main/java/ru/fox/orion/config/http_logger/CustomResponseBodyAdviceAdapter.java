package ru.fox.orion.config.http_logger;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class CustomResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    private static final List<String> SKIP_ENDPOINTS = List.of("/actuator/health/liveness",
            "/actuator/health/readiness");

    private final HttpLoggingService httpLoggingService;
    private final HttpServletRequest request;
    private final Tracer tracer;

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        String path = request.getRequestURI();
        return !SKIP_ENDPOINTS.contains(path);
    }

    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {


        if (serverHttpRequest instanceof ServletServerHttpRequest
                && serverHttpResponse instanceof ServletServerHttpResponse) {
            httpLoggingService.logResponse(
                    ((ServletServerHttpRequest) serverHttpRequest).getServletRequest(),
                    ((ServletServerHttpResponse) serverHttpResponse).getServletResponse(), o);
        }

        addTraceParent(serverHttpResponse);

        return o;
    }

    private void addTraceParent(ServerHttpResponse serverHttpResponse) {
        Span span = tracer.currentSpan();
        String traceparent;
        if (span != null) {
            String sampledFlag = span.context().sampled() ? "01" : "00";
            traceparent = "00-%s-%s-%s".formatted(span.context().traceId(), span.context().spanId(), sampledFlag);
        } else {
            traceparent = "00-00000000000000000000000000000000-0000000000000000-00";
        }
        serverHttpResponse.getHeaders().add("Traceparent", traceparent);
    }
}