package br.edu.utfpr.tsi.xenon.application.controller;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

public interface EndpointsTranslator {

    MessageSource getMessage();

    default Locale getLocale(String language) {
        if (StringUtils.isBlank(language) || "*".equals(language)) {
            return Locale.US;
        }

        if (language.contains(",")) {
            var languages = language.split(",");
            return Locale.forLanguageTag(languages[0]);
        }

        return Locale.forLanguageTag(language);
    }

    default String getMessage(String messagesMapper, Locale locale, String... args) {
        return getMessage().getMessage(messagesMapper, args, locale);
    }
}
