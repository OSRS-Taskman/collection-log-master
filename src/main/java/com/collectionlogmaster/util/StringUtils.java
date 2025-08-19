package com.collectionlogmaster.util;

import lombok.NonNull;

public class StringUtils {
    public static @NonNull String kebabCase(@NonNull String snakeCase) {
        return snakeCase.toLowerCase().replace('_', '-');
    }
}
