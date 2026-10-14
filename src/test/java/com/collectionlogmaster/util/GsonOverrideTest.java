package com.collectionlogmaster.util;

import static org.junit.Assert.assertEquals;

import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.taskapp.response.UserProfileResponse;
import com.google.gson.Gson;
import org.junit.Test;

public class GsonOverrideTest {
	@Test
	public void deserializesAllTaskTiersFromApiValues() {
		new GsonOverride(new Gson());

		for (TaskTier tier : TaskTier.values()) {
			String json = String.format(
				"{\"hide_below\":\"%s\"}",
				tier.name().toLowerCase()
			);
			UserProfileResponse response = GsonOverride.GSON.fromJson(json, UserProfileResponse.class);

			assertEquals(tier, response.getHideBelow());
		}
	}
}
