package mc.central.hk.config;

import mc.central.hk.DailyRewardEnhanced;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigPaths {
    private static final String CONFIG_DIR_NAME = DailyRewardEnhanced.MOD_ID;

    private ConfigPaths() {
    }

    public static Path resolve(String fileName) {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR_NAME);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            DailyRewardEnhanced.LOGGER.error("Couldn't create config directory {}", dir, e);
        }
        return dir.resolve(fileName);
    }

    public static void migrateLegacyFile(String legacyFileName, Path newPath) {
        Path legacyPath = FabricLoader.getInstance().getConfigDir().resolve(legacyFileName);
        if (Files.exists(newPath) || !Files.exists(legacyPath)) {
            return;
        }

        try {
            Files.createDirectories(newPath.getParent());
            Files.move(legacyPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            DailyRewardEnhanced.LOGGER.info("Migrated legacy config from {} to {}", legacyPath, newPath);
        } catch (IOException e) {
            DailyRewardEnhanced.LOGGER.error("Failed to migrate legacy config from {} to {}", legacyPath, newPath, e);
        }
    }
}

