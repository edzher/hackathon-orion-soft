package ru.fox.orion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.fox.orion.config.client.CustomClientHttpRequestInterceptor;
import ru.fox.orion.config.client.LoggService;
import ru.fox.orion.core.client.AnalyticClient;
import ru.fox.orion.core.client.ParserClient;
import ru.fox.orion.core.client.impl.AnalyticClientImpl;
import ru.fox.orion.core.client.impl.ParserClientImpl;

import java.time.Duration;

@Configuration
@Slf4j
public class ClientBeans {

    private static final String ANALYTIC_CLIENT = "ANALYTIC_CLIENT";
    private static final String PARSER_CLIENT = "PARSER_CLIENT";
    private static final String COMMA = ":";

    @Value("${spring.http.client.connect-timeout}")
    private Long connectTimeout;

    @Value("${spring.http.client.read-timeout}")
    private Long readTimeout;

    @Value("${client.parser.host}")
    private String parserHost;

    @Value("${client.parser.port}")
    private String parserPort;

    @Value("${client.analytic.host}")
    private String analyticHost;

    @Value("${client.analytic.port}")
    private String analyticPort;

    @Bean
    public ClientHttpRequestFactory getClientHttpRequestFactories() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(connectTimeout))
                .withReadTimeout(Duration.ofMillis(readTimeout));
        return ClientHttpRequestFactories.get(settings);
    }

    @Bean
    public ParserClient parserWebClient(
            ClientHttpRequestFactory clientHttpRequestFactory
    ) {
        return new ParserClientImpl(
                RestClient.builder()
                        .baseUrl(parserHost + COMMA + parserPort)
                        .requestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory))
                        .requestInterceptor(new CustomClientHttpRequestInterceptor(new LoggService(PARSER_CLIENT)))
                        .build()
        );
    }

    @Bean
    public AnalyticClient analyticWebClient(
            ClientHttpRequestFactory clientHttpRequestFactory
    ) {
        return new AnalyticClientImpl(
                RestClient.builder()
                        .baseUrl(analyticHost + COMMA + analyticPort)
                        .requestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory))
                        .requestInterceptor(new CustomClientHttpRequestInterceptor(new LoggService(ANALYTIC_CLIENT)))
                        .build()
        );
    }

}