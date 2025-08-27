package com.collectionlogmaster.util;

import lombok.NonNull;

public class StringUtils {
    public static @NonNull String toggleString(@NonNull String curValue, @NonNull String onValue, @NonNull String offValue) {
        return curValue.equals(onValue) ? offValue : onValue;
    }

    public static @NonNull String kebabCase(@NonNull String snakeCase) {
        return snakeCase.toLowerCase().replace('_', '-');
    }
}
