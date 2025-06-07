package ru.fox.orion.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fox.orion.core.constants.ApiKeys;
import ru.fox.orion.service.ParseTelegramService;

@RestController
@RequiredArgsConstructor
@RequestMapping( ApiKeys.V1 + "/test")
public class TestController {

    private final ParseTelegramService parseTelegramService;

    @Operation(
            summary = "Тест",
            description = "Тест api"
    )
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(parseTelegramService.getParsed());
    }

}
