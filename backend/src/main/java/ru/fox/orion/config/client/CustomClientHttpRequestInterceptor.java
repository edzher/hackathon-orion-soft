package ru.fox.orion.config.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final LoggService loggService;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        return getClientHttpResponse(request, body, execution);
    }

    protected ClientHttpResponse getClientHttpResponse(HttpRequest request, byte[] body,
                                                       ClientHttpRequestExecution execution) throws IOException {
        loggService.logRequest(request, body);
        long time = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long duration = System.currentTimeMillis() - time;
        loggService.logResponse(response, request, duration);
        return response;
    }
}
