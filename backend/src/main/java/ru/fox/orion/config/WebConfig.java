package ru.fox.orion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.fox.orion.config.http_logger.HttpLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final HttpLoggingInterceptor httpLoggingInterceptor;
    private final TraceInterceptor traceInterceptor;
    private static final String ASTERICS = "*";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor);
        registry.addInterceptor(httpLoggingInterceptor);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOriginPatterns(ASTERICS)
                .allowedMethods(ASTERICS)
                .allowedHeaders(ASTERICS)
                .allowCredentials(true);
    }

}
