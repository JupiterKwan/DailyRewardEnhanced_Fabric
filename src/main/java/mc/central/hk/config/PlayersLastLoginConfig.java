package mc.central.hk.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mc.central.hk.DailyRewardEnhanced;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayersLastLoginConfig {

    private static final String LEGACY_FILE_NAME = DailyRewardEnhanced.MOD_ID + "-players.json";
    private static final String CONFIG_FILE_NAME = "players.json";

    private static final HashMap<String, PlayerLoginState> playersLoginState = new HashMap<>();
    private static Path configPath;

    public static class PlayerLoginState {
        private String lastLoginDate;
        private int streakDays;

        public PlayerLoginState() {
        }

        public PlayerLoginState(String lastLoginDate, int streakDays) {
            this.lastLoginDate = lastLoginDate;
            this.streakDays = Math.max(1, streakDays);
        }

        public String getLastLoginDate() {
            return lastLoginDate;
        }

        public int getStreakDays() {
            return Math.max(1, streakDays);
        }
    }

    private static synchronized void prepareConfigFile() {
        if (configPath != null) {
            return;
        }
        configPath = ConfigPaths.resolve(CONFIG_FILE_NAME);
        ConfigPaths.migrateLegacyFile(LEGACY_FILE_NAME, configPath);
    }

    private static void createDefaultConfig() {
        prepareConfigFile();
        playersLoginState.clear();
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            DailyRewardEnhanced.GSON.toJson(playersLoginState, writer);
        } catch (IOException e) {
            DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
        }
    }

    public void loadPlayersLoginState() {
        prepareConfigFile();
        try {
            if (!Files.exists(configPath)) {
                createDefaultConfig();
            }

            try (Reader reader = Files.newBufferedReader(configPath)) {
                JsonObject root = DailyRewardEnhanced.GSON.fromJson(reader, JsonObject.class);
                playersLoginState.clear();

                if (root == null) {
                    return;
                }

                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    String uuid = entry.getKey();
                    JsonElement value = entry.getValue();

                    if (value == null || value.isJsonNull()) {
                        continue;
                    }

                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        playersLoginState.put(uuid, new PlayerLoginState(value.getAsString(), 1));
                        continue;
                    }

                    if (value.isJsonObject()) {
                        PlayerLoginState state = DailyRewardEnhanced.GSON.fromJson(value, PlayerLoginState.class);
                        if (state != null && state.getLastLoginDate() != null && !state.getLastLoginDate().isBlank()) {
                            playersLoginState.put(uuid, new PlayerLoginState(state.getLastLoginDate(), state.getStreakDays()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            DailyRewardEnhanced.LOGGER.error("Couldn't load configuration for players-login-date.", e);
            createDefaultConfig();
        }
    }

    public Map<String, PlayerLoginState> getPlayersLoginState() {
        return Collections.unmodifiableMap(playersLoginState);
    }

    public PlayerLoginState getPlayerLoginState(String uuid) {
        return playersLoginState.get(uuid);
    }

    public void putPlayerLoginState(String uuid, String loginDate, int streakDays) throws IOException {
        prepareConfigFile();
        playersLoginState.put(uuid, new PlayerLoginState(loginDate, streakDays));
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            DailyRewardEnhanced.GSON.toJson(playersLoginState, writer);
        }
    }

}
