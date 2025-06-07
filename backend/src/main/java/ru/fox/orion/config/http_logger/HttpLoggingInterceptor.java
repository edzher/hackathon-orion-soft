package ru.fox.orion.config.http_logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.DispatcherType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static ru.fox.orion.core.util.LogUtil.REQUEST_START_TIME;

@Component
@RequiredArgsConstructor
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private final HttpLoggingService httpLoggingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())
                && request.getContentLength() <= 0) {
            request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
            httpLoggingService.logRequest(request, null);
        }
        return true;
    }
}
