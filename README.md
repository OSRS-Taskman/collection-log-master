# TaskApp Integration Beta Test

This is the third - and hopefully final - beta release of _Collection Log Master_ with integration to the _TaskApp_ website.

## Running the beta

To run this beta version of the _Collection Log Master_ plugin, please follow these instructions:

1. [Download](https://github.com/OSRS-Taskman/collection-log-master/archive/refs/heads/beta/taskapp-integration.zip) and extract the beta files anywhere in your computer.
2. Make sure you have Java 11+ installed. You can run (double click) the `check-java.bat` file to verify your version.
	- If you don't have it, **or have version lower than 11**, use [this link](https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.28%2B6/OpenJDK11U-jre_x64_windows_hotspot_11.0.28_6.msi) to download Java 11 for Windows.
3. If you use a Jagex account, follow [this RuneLite guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) to setup your login.
4. Run (double click) the `run-beta.bat` file

## What to test

> [!NOTE]
> Your save file for the main release will not be erased or overwritten, don't worry.

1. Open the plugin configuration and input your _TaskApp_ credentials at the very top
2. Open the task dashboard and check whether the UI reflects your current and completed tasks
   1. If you were already a _Collection Log Master_ user, check migration instructions below
3. Just keep using the plugin, completing tasks as you go

## Migration

If you were already using the _Collection Log Master_ plugin, you'll need to migrate your state to the website. To do so, simply open the task dashboard and click the _Sync_ button on the bottom left corner. If you had an active task, this will allow you to migrate that too, but only once!


If you have any issues, please tag @Phapha in the [#runelite-plugin](https://discord.com/channels/569436224457146388/1013862714243821649) discord channel.

Thank you for helping out!