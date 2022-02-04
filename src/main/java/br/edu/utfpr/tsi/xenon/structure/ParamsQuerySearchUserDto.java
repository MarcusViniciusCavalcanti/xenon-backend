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
    private final SortedUserPropertyEnum sorted;
    private final DirectionEnum direction;

    public enum SortedUserPropertyEnum {
        NAME("name"),
        EMAIL("email"),
        TYPE("type"),
        CREATED("createdAt");

        private static final Map<String, SortedUserPropertyEnum> values = Map.of(
            "name", NAME,
            "email", EMAIL,
            "type", TYPE
        );

        @Getter
        private final String value;

        SortedUserPropertyEnum(String value) {
            this.value = value;
        }

        public static SortedUserPropertyEnum fromValue(String text) {
            if (StringUtils.isNotBlank(text)) {
                return values.getOrDefault(text.toLowerCase(Locale.ROOT), CREATED);
            }

            return CREATED;
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
            .formatted(size, page, nameOrEmail, sorted.value, direction.getValue(), type);
    }
}
