package ru.fox.orion.core.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import ru.fox.orion.core.client.AnalyticClient;

@RequiredArgsConstructor
public class AnalyticClientImpl implements AnalyticClient {

    private final RestClient restClient;

    @Override
    public String getParsed() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/parse")
                        .build()
                )
                .retrieve()
                .body(String.class);

    }

}
