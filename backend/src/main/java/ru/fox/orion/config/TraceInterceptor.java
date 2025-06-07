package ru.fox.orion.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class TraceInterceptor implements HandlerInterceptor {

    private final Tracer tracer;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        if (!response.isCommitted()) {
            response.addHeader("traceparent", getTraceParent());
        }
    }

    private String getTraceParent() {
        Span span = tracer.currentSpan();
        if (span != null) {
            String sampledFlag = span.context().sampled() ? "01" : "00";
            return "00-%s-%s-%s".formatted(span.context().traceId(), span.context().spanId(), sampledFlag);
        } else {
            return "00-00000000000000000000000000000000-0000000000000000-00";
        }
    }

}
