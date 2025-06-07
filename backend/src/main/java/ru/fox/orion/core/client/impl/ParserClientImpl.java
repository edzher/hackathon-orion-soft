package ru.fox.orion.core.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import ru.fox.orion.core.client.ParserClient;

@RequiredArgsConstructor
public class ParserClientImpl implements ParserClient {

    private final RestClient restClient;

}
