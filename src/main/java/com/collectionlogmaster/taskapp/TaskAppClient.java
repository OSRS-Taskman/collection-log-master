package com.collectionlogmaster.taskapp;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import com.collectionlogmaster.taskapp.request.LoginRequest;
import com.collectionlogmaster.taskapp.request.MigrateRequest;
import com.collectionlogmaster.taskapp.request.SyncRequest;
import com.collectionlogmaster.taskapp.request.UpdateTaskRequest;
import com.collectionlogmaster.taskapp.response.GenerateTaskResponse;
import com.collectionlogmaster.taskapp.response.LoginResponse;
import com.collectionlogmaster.taskapp.response.SyncResponse;
import com.collectionlogmaster.taskapp.response.TaskListResponse;
import com.collectionlogmaster.taskapp.response.UserProfileResponse;
import com.collectionlogmaster.util.HttpClient;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

@Slf4j
@Singleton
public class TaskAppClient extends HttpClient {
	private static final HttpUrl DEVELOPMENT_BASE_API_URL = new HttpUrl.Builder()
		.scheme("http")
		.host("127.0.0.1")
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

	private final TaskAppAuthInterceptor taskAppAuthInterceptor;

	@Inject
	public TaskAppClient(OkHttpClient okHttpClient, CollectionLogMasterConfig config) {
		super(okHttpClient);

		taskAppAuthInterceptor = new TaskAppAuthInterceptor(this, config);
		this.okHttpClient = okHttpClient.newBuilder()
			.addInterceptor(taskAppAuthInterceptor)
			.build();
	}

	private @NonNull HttpUrl buildApiUrl(String... segments) {
		HttpUrl baseApiUrl = isDeveloperMode ? DEVELOPMENT_BASE_API_URL : PRODUCTION_BASE_API_URL;
		HttpUrl.Builder builder = baseApiUrl.newBuilder();

		for (String segment : segments) {
			builder.addPathSegments(segment);
		}

		return builder.build();
	}

	public void invalidateToken() {
		taskAppAuthInterceptor.invalidateToken();
	}

	public CompletableFuture<LoginResponse> login(String username, String password) {
		HttpUrl url = buildApiUrl("login");
		LoginRequest data = new LoginRequest(username, password);

		return post(url, data, LoginResponse.class);
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
		UpdateTaskRequest data = new UpdateTaskRequest(completed);

		return patch(url, data, null);
	}

	public CompletableFuture<GenerateTaskResponse> generateTask() {
		HttpUrl url = buildApiUrl("user/generate-task");

		return post(url, GenerateTaskResponse.class);
	}

	public CompletableFuture<SyncResponse> sync(
		Set<Integer> collectionLog,
		Map<DiaryRegion, Map<DiaryDifficulty, Boolean>> diaries,
		Map<Skill, Integer> skills
	) {
		HttpUrl url = buildApiUrl("user/sync");
		SyncRequest data = new SyncRequest(collectionLog, diaries, skills);

		return post(url, data, SyncResponse.class);
	}

	public CompletableFuture<Void> migrate(String taskId) {
		HttpUrl url = buildApiUrl("user/migrate");
		MigrateRequest data = new MigrateRequest(taskId);

		return post(url, data, null);
	}
}
