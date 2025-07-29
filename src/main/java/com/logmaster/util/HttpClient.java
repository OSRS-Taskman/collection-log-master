package com.logmaster.util;

import com.logmaster.PluginUpdateNotifier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLiteProperties;
import okhttp3.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.logmaster.util.GsonOverride.GSON;

@Slf4j
public class HttpClient {
    private static final MediaType JSON_MEDIA_TYPE = Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8"));

    @Inject
    private OkHttpClient okHttpClient;

    private final String userAgent;

    @Inject
    public HttpClient() {
        String runeliteVersion = RuneLiteProperties.getVersion();
        String pluginVersion = PluginUpdateNotifier.getPluginVersion();
        userAgent = "RuneLite:" + runeliteVersion + "," + "CLogMaster:" + pluginVersion;
    }

    private Request.Builder buildRequest(String url, Consumer<Request.Builder> methodSetter) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("User-Agent", userAgent);
        methodSetter.accept(builder);
        return builder;
    }

    private CompletableFuture<Response> executeHttpRequestAsync(Request request) {
        log.debug("Sending {} request to {}; data = {}", request.method(), request.url(), request.body());

        CompletableFuture<Response> future = new CompletableFuture<>();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                log.warn("Async request failed.", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                future.complete(response);
            }
        });
        return future;
    }

    public <T> CompletableFuture<T> postHttpRequestAsync(String url, String data, @Nullable Class<T> clazz) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, data);
        Request request = buildRequest(url, builder -> builder.post(body)).build();
        return executeHttpRequestAsync(request)
                .thenApply((response) -> handleResponse(response, clazz));
    }

    public <T> CompletableFuture<T> putHttpRequestAsync(String url, String data, @Nullable Class<T> clazz) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, data);
        Request request = buildRequest(url, builder -> builder.put(body)).build();
        return executeHttpRequestAsync(request)
                .thenApply((response) -> handleResponse(response, clazz));
    }

    public <T> CompletableFuture<T> getHttpRequestAsync(String url, @Nullable Class<T> clazz) {
        Request request = buildRequest(url, Request.Builder::get)
                .build();
        return executeHttpRequestAsync(request)
                .thenApply((response) -> handleResponse(response, clazz));
    }

    private <T> T handleResponse(Response response, @Nullable Class<T> clazz) {
        try (Response res = response) {
            ResponseBody body = res.body();

            if (body == null) {
                throw new RuntimeException("Response body is null");
            }

            String bodyString = body.string();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Response unsuccessful");
            }

            if (clazz == null) {
                return null;
            }

            return GSON.fromJson(bodyString, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error reading response body");
        }
    }
}
