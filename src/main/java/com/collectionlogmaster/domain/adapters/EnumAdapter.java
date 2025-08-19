package com.collectionlogmaster.domain.adapters;

import com.google.gson.*;
import com.collectionlogmaster.util.EnumUtils;
import com.collectionlogmaster.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
@RequiredArgsConstructor
public class EnumAdapter<T extends Enum<T>> implements JsonDeserializer<T>, JsonSerializer<T> {
    private final Class<T> clazz;

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return EnumUtils.fromString(clazz, jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext context) {
        return context.serialize(StringUtils.kebabCase(t.name()), String.class);
    }
}
