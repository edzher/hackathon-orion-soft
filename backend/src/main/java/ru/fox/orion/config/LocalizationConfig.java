package ru.fox.orion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor
@Configuration
public class LocalizationConfig {
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String RUSSIAN_LANGUAGE = "ru";

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setSupportedLocales(
                Arrays.asList(new Locale(RUSSIAN_LANGUAGE), new Locale(ENGLISH_LANGUAGE))
        );
        acceptHeaderLocaleResolver.setDefaultLocale(new Locale(ENGLISH_LANGUAGE));
        return acceptHeaderLocaleResolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("localization/message");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
