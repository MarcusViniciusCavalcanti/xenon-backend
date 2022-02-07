package br.edu.utfpr.tsi.xenon.structure;

import java.util.Locale;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
public class ParamsQuerySearchRecognizeDto {

    private final Integer size;
    private final Integer page;
    private final Boolean onlyError;
    private final String driverName;
    private final SortedRecognizePropertyEnum sorted;
    private final DirectionEnum direction;

    public enum SortedRecognizePropertyEnum {
        DRIVER_NAME("driverName"),
        ORIGIN("origin"),
        ONLY_ERROR("onlyError"),
        CREATED("createdAt");

        private static final Map<String, SortedRecognizePropertyEnum> values = Map.of(
            "driverName", DRIVER_NAME,
            "origin", ORIGIN,
            "onlyError", ONLY_ERROR,
            "createdAt", CREATED
        );

        @Getter
        private final String value;

        SortedRecognizePropertyEnum(String value) {
            this.value = value;
        }

        public static SortedRecognizePropertyEnum fromValue(String text) {
            if (StringUtils.isNotBlank(text)) {
                return values.getOrDefault(text.toLowerCase(Locale.ROOT), CREATED);
            }

            return CREATED;
        }
    }
}
