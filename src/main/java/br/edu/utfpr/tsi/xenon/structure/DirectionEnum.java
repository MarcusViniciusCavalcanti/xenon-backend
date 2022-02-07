package br.edu.utfpr.tsi.xenon.structure;

import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum DirectionEnum {
    ASC("asc"),
    DESC("desc");

    private static final Map<String, DirectionEnum> values = Map.of(
        "desc", DESC,
        "asc", ASC
    );

    @Getter
    private final String value;

    DirectionEnum(String value) {
        this.value = value;
    }

    public static DirectionEnum fromValue(String text) {
        if (StringUtils.isNotBlank(text)) {
            return values.getOrDefault(text.toLowerCase(Locale.ROOT), DESC);
        }

        return DESC;
    }
}
