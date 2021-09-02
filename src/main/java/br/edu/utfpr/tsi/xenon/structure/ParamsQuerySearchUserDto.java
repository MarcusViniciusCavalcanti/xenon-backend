package br.edu.utfpr.tsi.xenon.structure;

import java.util.Locale;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
public class ParamsQuerySearchUserDto {

    private final Long size;
    private final Long page;
    private final String nameOrEmail;
    private final String type;
    private final SortedEnum sorted;
    private final DirectionEnum direction;

    public enum SortedEnum {
        NAME("name"),
        EMAIL("email"),
        TYPE("type"),
        CREATED("createdAt");

        private static final Map<String, SortedEnum> values = Map.of(
            "name", NAME,
            "email", EMAIL,
            "type", TYPE
        );

        @Getter
        private final String value;

        SortedEnum(String value) {
            this.value = value;
        }

        public static SortedEnum fromValue(String text) {
            if (StringUtils.isNotBlank(text)) {
                return values.getOrDefault(text.toLowerCase(Locale.ROOT), CREATED);
            }

            return CREATED;
        }
    }

    public enum DirectionEnum {
        ASC("asc"),
        DESC("desc");

        private static final Map<String, DirectionEnum> values = Map.of(
            "desc", DESC,
            "asc", ASC
        );

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

    public enum Type {
        STUDENTS, SERVICE, SPEAKER;

        private static final Map<String, Type> values  = Map.of(
            "STUDENTS", STUDENTS,
            "SERVICE", SERVICE,
            "SPEAKER", SPEAKER
        );

        public static String fromValue(String text) {
            if (StringUtils.isNotBlank(text)) {
                return values.getOrDefault(text.toUpperCase(Locale.ROOT), SERVICE).name();
            }

            return null;
        }
    }

    @Override
    public String toString() {
        return "[size:%d, page:%d, nameOrEmail:%s, sorted:%s, direction:%s, type:%s]"
            .formatted(size, page, nameOrEmail, sorted.value, direction.value, type);
    }
}
