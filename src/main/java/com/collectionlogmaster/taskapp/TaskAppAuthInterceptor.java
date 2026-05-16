package com.collectionlogmaster.taskapp;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.taskapp.response.LoginResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class TaskAppAuthInterceptor implements Interceptor {
	private static final Duration TOKEN_DURATION = Duration.ofHours(12);

	private final TaskAppClient taskAppClient;

	private final CollectionLogMasterConfig config;

	private String jwtToken;

	private Instant tokenExpiresAt;

	public TaskAppAuthInterceptor(TaskAppClient taskAppClient, CollectionLogMasterConfig config) {
		this.taskAppClient = taskAppClient;
		this.config = config;
	}

	private boolean isTokenValid() {
		return jwtToken != null
			&& tokenExpiresAt.isAfter(Instant.now());
	}

	private boolean isLoginRequest(Request req) {
		return req.url().encodedPath().endsWith("/login");
	}

	private void authenticate() {
		LoginResponse res = taskAppClient.login(config.username(), config.password()).join();
		jwtToken = res.getToken();
		tokenExpiresAt = Instant.now().plus(TOKEN_DURATION);
	}

	@Override
	public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
		Request originalRequest = chain.request();

		if (isLoginRequest(originalRequest)) {
			return chain.proceed(originalRequest);
		}

		synchronized (this) {
			if (!isTokenValid()) {
				authenticate();
			}
		}

		Request newRequest = originalRequest.newBuilder()
			.header("Authorization", "Bearer " + jwtToken)
			.build();

		return chain.proceed(newRequest);
	}
}
