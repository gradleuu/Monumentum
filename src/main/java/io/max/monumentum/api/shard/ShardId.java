package io.max.monumentum.api.shard;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ShardId(String name, int instance) {

	/** Used for {@link #instance()} when there is no shard number. */
	public static final int NO_INSTANCE = -1;

	/** Used instead of {@code null}. */
	public static final ShardId UNKNOWN = new ShardId("unknown", NO_INSTANCE);

	private static final Pattern PATTERN = Pattern.compile("^(.+?)(?:-(\\d+))?$");

	public ShardId {
		Objects.requireNonNull(name, "name");
	}

	public static ShardId parse(String raw) {
		Matcher m = PATTERN.matcher(raw);
		if (m.matches() && m.group(2) != null) {
			try {
				return new ShardId(m.group(1), Integer.parseInt(m.group(2)));
			} catch (NumberFormatException ignored) {
				// digits overflow int: whole string is the name
			}
		}
		return new ShardId(raw, NO_INSTANCE);
	}

	/**
	 * @return {@code true} if the shard had a number.
	 */
	public boolean hasInstance() {
		return instance != NO_INSTANCE;
	}
}