package com.collectionlogmaster.taskapp;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.taskapp.response.GenerateTaskResponse;
import com.collectionlogmaster.taskapp.response.LoginResponse;
import com.collectionlogmaster.taskapp.response.TaskListResponse;
import com.collectionlogmaster.taskapp.response.UserProfileResponse;
import com.collectionlogmaster.util.HttpClient;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Singleton
public class TaskAppClient extends HttpClient {
	private static final HttpUrl DEVELOPMENT_BASE_API_URL = new HttpUrl.Builder()
		.scheme("http")
		.host("192.168.15.15")
		.port(5001)
		.addPathSegments("api/v2")
		.build();

	private static final HttpUrl PRODUCTION_BASE_API_URL = new HttpUrl.Builder()
		.scheme("https")
		.host("www.osrstaskapp.com")
		.addPathSegments("api/v2")
		.build();

	@Inject
	@Named("developerMode")
	private boolean isDeveloperMode;

	@Inject
	public TaskAppClient(OkHttpClient okHttpClient, CollectionLogMasterConfig config) {
		super(okHttpClient);

		TaskAppAuthInterceptor taskAppAuthInterceptor = new TaskAppAuthInterceptor(this, config);
		this.okHttpClient = okHttpClient.newBuilder()
			.addInterceptor(taskAppAuthInterceptor)
			.build();
	}

	private @NotNull HttpUrl buildApiUrl(String... segments) {
		HttpUrl baseApiUrl = isDeveloperMode ? DEVELOPMENT_BASE_API_URL : PRODUCTION_BASE_API_URL;
		HttpUrl.Builder builder = baseApiUrl.newBuilder();

		for (String segment : segments) {
			builder.addPathSegments(segment);
		}

		return builder.build();
	}

	public CompletableFuture<LoginResponse> login(String username, String password) {
		HttpUrl url = buildApiUrl("login");

		JsonObject body = new JsonObject();
		body.addProperty("username", username);
		body.addProperty("password", password);

		return post(url, body, LoginResponse.class);
	}

	public CompletableFuture<TaskListResponse> getTaskList() {
		HttpUrl url = buildApiUrl("task-list");

		return get(url, TaskListResponse.class);
	}

	public CompletableFuture<UserProfileResponse> getUserProfile() {
		HttpUrl url = buildApiUrl("user/profile");

		return get(url, UserProfileResponse.class);
	}

	public CompletableFuture<Void> updateTask(String taskId, boolean completed) {
		HttpUrl url = buildApiUrl("user/tasks", taskId);

		JsonObject body = new JsonObject();
		body.addProperty("completed", completed);

		return patch(url, body, null);
	}

	public CompletableFuture<GenerateTaskResponse> generateTask() {
		HttpUrl url = buildApiUrl("user/generate-task");

		return post(url, GenerateTaskResponse.class);
	}
}
