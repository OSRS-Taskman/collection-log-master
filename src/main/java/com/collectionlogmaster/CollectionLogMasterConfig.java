package com.collectionlogmaster;

import static com.collectionlogmaster.CollectionLogMasterConfig.CONFIG_GROUP;

import com.collectionlogmaster.domain.DynamicTaskImages;
import com.collectionlogmaster.domain.TaskTier;
import net.runelite.client.config.*;

@ConfigGroup(CONFIG_GROUP)
public interface CollectionLogMasterConfig extends Config
{
	String CONFIG_GROUP = "collection-log-master";

	String TASK_APP_SECTION = "taskApp";
	String TASK_APP_USERNAME = TASK_APP_SECTION + ".username";
	String TASK_APP_PASSWORD = TASK_APP_SECTION + ".password";

	String GENERAL_SECTION = "general";

	String PLUGIN_VERSION_KEY = "plugin-version";
	String IS_LMS_ENABLED_KEY = "isLMSEnabled";
	String IS_COMMAND_ENABLED_KEY = "isCommandEnabled";

	@ConfigSection(
		name = "TaskApp Credentials",
		description = "The credentials for your account in TaskApp (osrstaskapp.com)",
		position = 10
	)
	String taskAppSection = TASK_APP_SECTION;

	@ConfigItem(
		keyName = TASK_APP_USERNAME,
		name = "Username",
		description = "Your account's username in TaskApp (osrstaskapp.com)",
		section = taskAppSection,
		position = 11
	)
	default String username()
	{
		return "";
	}

	@ConfigItem(
		keyName = TASK_APP_PASSWORD,
		name = "Password",
		description = "Your account's password in TaskApp (osrstaskapp.com)",
		section = taskAppSection,
		secret = true,
		position = 12
	)
	default String password()
	{
		return "";
	}


	@ConfigSection(
		name = "General",
		description = "General settings to control the plugin's behavior and appearance",
		position = 20
	)
	String generalSection = GENERAL_SECTION;

	@Range(
			min = 1000,
			max = 10000
	)
	@Units(Units.MILLISECONDS)
	@ConfigItem(
			keyName = "rollTime",
			name = "Roll Time",
			description = "How long new tasks will take to roll",
			section = generalSection,
			position = 21
	)
	default int rollTime()
	{
		return 5000;
	}

	@ConfigItem(
			keyName = "rollPastCompleted",
			name = "Roll past completed",
			description = "Include tasks you've already completed in the roll animation. Helpful when you're getting to the end of a tier!",
			section = generalSection,
			position = 22
	)
	default boolean rollPastCompleted()
	{
		return false;
	}

	@ConfigItem(
			keyName = "hideBelow",
			name = "Hide Tasks Below",
			description = "Disabled the showing up/assigning of tasks at or below the specified tier",
			section = generalSection,
			position = 23
	)
	default TaskTier hideBelow()
	{
		return TaskTier.EASY;
	}

	@ConfigItem(
			keyName = "displayCurrentTaskOverlay",
			name = "Display current task overlay",
			description = "Enable an overlay showing the currently assigned task (when one exists)",
			section = generalSection,
			position = 24
	)
	default boolean displayCurrentTaskOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "dynamicTaskImages",
			name = "Dynamic task images",
			description = "Display dynamic task images based on required/acquired items",
			section = generalSection,
			position = 25
	)
	default DynamicTaskImages dynamicTaskImages()
	{
		return DynamicTaskImages.COMPLETE;
	}

	@ConfigItem(
			keyName = IS_LMS_ENABLED_KEY,
			name = "Enable LMS tasks",
			description = "Whether to include LMS tasks in the list.",
			section = generalSection,
			position = 26
	)
	default boolean isLMSEnabled()
	{
		return true;
	}

	@ConfigSection(
			name = "!taskman Command",
			description = "Configuration options for the !taskman command",
			position = 30
	)
	String commandSection = "command";

	@ConfigItem(
			keyName = IS_COMMAND_ENABLED_KEY,
			name = "Enable command",
			description = "When you or others type !taskman in the chat, it will be replaced by your current task status",
			section = commandSection,
			position = 31
	)
	default boolean isCommandEnabled()
	{
		return true;
	}
}
