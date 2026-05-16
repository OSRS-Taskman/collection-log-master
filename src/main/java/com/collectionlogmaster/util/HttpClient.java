package com.collectionlogmaster.util;

import com.collectionlogmaster.PluginUpdateNotifier;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLiteProperties;
import okhttp3.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.collectionlogmaster.util.GsonOverride.GSON;

@Slf4j
public class HttpClient {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    protected OkHttpClient okHttpClient;

    private final String userAgent;

    @Inject
    public HttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;

        String runeliteVersion = RuneLiteProperties.getVersion();
        String pluginVersion = PluginUpdateNotifier.getPluginVersion();
        userAgent = "RuneLite: " + runeliteVersion + ", CLogMaster: " + pluginVersion;
    }

    protected Request.Builder buildRequest(HttpUrl url, Consumer<Request.Builder> methodSetter) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("User-Agent", userAgent);

        methodSetter.accept(builder);

        return builder;
    }

    protected CompletableFuture<Response> executeRequest(Request request) {
        log.debug("Sending {} request to {}; data = {}", request.method(), request.url(), request.body());

        CompletableFuture<Response> future = new CompletableFuture<>();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                log.error("Async request failed.", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                future.complete(response);
            }
        });
        return future;
    }

    public <T> CompletableFuture<T> get(HttpUrl url, @Nullable Class<T> clazz) {
        Request request = buildRequest(url, Request.Builder::get).build();

        return executeRequest(request)
                .thenApply((response) -> parseResponse(response, clazz));
    }

    public <T> CompletableFuture<T> post(HttpUrl url, @Nullable Class<T> clazz) {
        return post(url, new JsonObject(), clazz);
    }

    public <T> CompletableFuture<T> post(HttpUrl url, JsonObject json, @Nullable Class<T> clazz) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json.toString());
        Request request = buildRequest(url, builder -> builder.post(body)).build();

        return executeRequest(request)
                .thenApply((response) -> parseResponse(response, clazz));
    }

    public <T> CompletableFuture<T> put(HttpUrl url, String jsonBody, @Nullable Class<T> clazz) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        Request request = buildRequest(url, builder -> builder.put(body)).build();

        return executeRequest(request)
                .thenApply((response) -> parseResponse(response, clazz));
    }

    public <T> CompletableFuture<T> patch(HttpUrl url, JsonObject json, @Nullable Class<T> clazz) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json.toString());
        Request request = buildRequest(url, builder -> builder.patch(body)).build();

        return executeRequest(request)
                .thenApply((response) -> parseResponse(response, clazz));
    }

    protected <T> T parseResponse(Response response, @Nullable Class<T> clazz) {
        try (Response res = response) {
            ResponseBody body = res.body();

            if (body == null) {
                throw new RuntimeException("Response body is null");
            }

            String bodyString = body.string();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Response unsuccessful: " + bodyString);
            }

            if (clazz == null) {
                return null;
            }

            return GSON.fromJson(bodyString, clazz);
        } catch (IOException e) {
            log.error("Failed to parse response body.", e);
            throw new RuntimeException("Failed to parse response body", e);
        }
    }
}
