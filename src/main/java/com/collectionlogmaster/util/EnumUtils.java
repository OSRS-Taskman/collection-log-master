package com.collectionlogmaster.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumUtils {
    public static <T extends Enum<T>> T fromString(@NonNull Class<T> enumClass, String name) {
        for (T e : enumClass.getEnumConstants()) {
            if (EnumUtils.toString(e).equals(name)) {
                return e;
            }
        }

        log.warn("No enum const {} for name '{}'", enumClass.getName(), name);
        return null;
    }

    public static <T extends Enum<T>> @NonNull String toString(T enumValue) {
        return StringUtils.kebabCase(enumValue.name());
    }
}
