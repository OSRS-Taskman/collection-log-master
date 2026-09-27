package com.collectionlogmaster.util;

import lombok.NonNull;

import java.time.Duration;

public class StringUtils {
	public static @NonNull String toggleString(@NonNull String curValue, @NonNull String onValue, @NonNull String offValue) {
		return curValue.equals(onValue) ? offValue : onValue;
	}

	public static @NonNull String kebabCase(@NonNull String snakeCase) {
		return snakeCase.toLowerCase().replace('_', '-');
	}

	public static @NonNull String ticksToDuration(int ticks) {
		Duration duration = Duration.ofMillis(ticks * 600L);

		StringBuilder sb = new StringBuilder();

		if (duration.toDays() > 0) {
			sb.append(String.format("%02d:", duration.toDaysPart()));
		}
		if (duration.toHours() > 0) {
			sb.append(String.format("%02d:", duration.toHoursPart()));
		}
		if (duration.toMinutes() > 0) {
			sb.append(String.format("%02d:", duration.toMinutesPart()));
		}

		sb.append(String.format("%02d.%03d", duration.toSecondsPart(), duration.toMillisPart()));

		return sb.toString();
	}
}
