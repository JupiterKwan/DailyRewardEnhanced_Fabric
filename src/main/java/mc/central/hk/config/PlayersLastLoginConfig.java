package mc.central.hk.config;

import com.mojang.authlib.minecraft.client.ObjectMapper;
import mc.central.hk.DailyRewardEnhanced;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

public class PlayersLastLoginConfig {

    private static final HashMap<String, String> playersLoginDate = new HashMap<>();
    private static File configFile;

    private static void prepareConfigFile() {
        if (configFile != null) {
            return;
        }
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), DailyRewardEnhanced.MOD_ID + "-players.json");
    }

    private static void createConfigFile() {
        createConfig();
    }

    private static void createConfig() {
        prepareConfigFile();
        playersLoginDate.put("File-Generating-in", LocalDate.now().toString());
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(DailyRewardEnhanced.GSON.toJson(playersLoginDate));
        } catch (IOException e) {
            DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
        }
    }

    public void loadPlayersLoginDate() {
        prepareConfigFile();
        try {
            if (!configFile.exists()) {
                createConfigFile();
            }
            if (configFile.exists()) {
                BufferedReader bReader = new BufferedReader(new FileReader(configFile));
                HashMap<String, String> savedConfig = DailyRewardEnhanced.GSON.fromJson(bReader, HashMap.class);
                if (savedConfig != null) {
                    playersLoginDate.clear();
                    playersLoginDate.putAll(savedConfig);
                }
            }
        } catch (FileNotFoundException e) {
            DailyRewardEnhanced.LOGGER.error("Couldn't load configuration for players-login-date.", e);
            createConfigFile();
        }
    }

    public HashMap<String, String> getPlayersLoginDate() {
        return playersLoginDate;
    }

    public void putPlayerLoginDate(String playerName, String loginDate) throws IOException {
        playersLoginDate.put(playerName, loginDate);
        boolean isFileDelete = configFile.delete();
        if (isFileDelete) {
            prepareConfigFile();
            try (FileWriter fileWriter = new FileWriter(configFile)) {
                fileWriter.write(DailyRewardEnhanced.GSON.toJson(playersLoginDate));
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
            }
        }
    }

}
