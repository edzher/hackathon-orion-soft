package ru.fox.orion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fox.orion.core.client.AnalyticClient;
import ru.fox.orion.service.ParseTelegramService;

@Service
@RequiredArgsConstructor
public class ParseTelegramServiceImpl implements ParseTelegramService {

    private final AnalyticClient analyticClient;

    @Override
    public String getParsed() {
        return analyticClient.getParsed();
    }

}
