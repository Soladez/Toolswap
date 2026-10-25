package de.autotool;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Simple properties-based config stored in {@code config/autotool.properties}. */
public final class AutoToolConfig {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autotool.properties");

	/** Whether the mod is active. Can also be toggled in-game with the key mapping. */
	public static boolean enabled = true;
	/** Switch back to the previously held slot once the attack key is released. */
	public static boolean switchBack = true;
	/** Tools with this much durability left (or less) are never selected, so they don't break. */
	public static int minDurability = 5;
	/** Don't switch tools in creative mode. */
	public static boolean ignoreCreative = true;

	private AutoToolConfig() {
	}

	public static void load() {
		Properties props = new Properties();

		if (Files.exists(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE)) {
				props.load(reader);
			} catch (IOException e) {
				AutoToolClient.LOGGER.warn("Could not read {}", FILE, e);
			}
		}

		enabled = Boolean.parseBoolean(props.getProperty("enabled", String.valueOf(enabled)));
		switchBack = Boolean.parseBoolean(props.getProperty("switchBack", String.valueOf(switchBack)));
		ignoreCreative = Boolean.parseBoolean(props.getProperty("ignoreCreative", String.valueOf(ignoreCreative)));

		try {
			minDurability = Math.max(0, Integer.parseInt(props.getProperty("minDurability", String.valueOf(minDurability)).trim()));
		} catch (NumberFormatException e) {
			AutoToolClient.LOGGER.warn("Invalid minDurability in {}, using {}", FILE, minDurability);
		}

		save();
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("enabled", String.valueOf(enabled));
		props.setProperty("switchBack", String.valueOf(switchBack));
		props.setProperty("minDurability", String.valueOf(minDurability));
		props.setProperty("ignoreCreative", String.valueOf(ignoreCreative));

		try {
			Files.createDirectories(FILE.getParent());

			try (Writer writer = Files.newBufferedWriter(FILE)) {
				props.store(writer, "AutoTool config");
			}
		} catch (IOException e) {
			AutoToolClient.LOGGER.warn("Could not write {}", FILE, e);
		}
	}
}
