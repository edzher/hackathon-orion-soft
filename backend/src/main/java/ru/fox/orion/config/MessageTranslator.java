package ru.fox.orion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class MessageTranslator {

    private static final String RUSSIAN_LANGUAGE = "ru";
    private final MessageSource messageSource;

    public String get(String code, Locale locale) {
        if (locale == null) return get(code);
        return messageSource.getMessage(code, new Object[]{""}, new Locale(RUSSIAN_LANGUAGE));
    }

    public String get(String code) {
        return messageSource.getMessage(code, new Object[]{""}, new Locale(RUSSIAN_LANGUAGE));
    }

    public String get(String code, Object[] args) {
        return messageSource.getMessage(code, args, new Locale(RUSSIAN_LANGUAGE));
    }

    public String translateOrReturnOriginal(String code) {
        try {
            return messageSource.getMessage(code, new Object[]{""}, new Locale(RUSSIAN_LANGUAGE));
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
}