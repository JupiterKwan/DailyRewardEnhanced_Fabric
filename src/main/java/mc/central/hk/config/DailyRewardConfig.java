package mc.central.hk.config;

import mc.central.hk.DailyRewardEnhanced;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.*;

public class DailyRewardConfig {

    private final ArrayList<Object> blacklist = new ArrayList<>();

    public static class Manager {
        private static File configFile;

        public static void prepareConfigFile() {
            if (configFile != null) {
                return;
            }
            configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), DailyRewardEnhanced.MOD_ID + ".json");
        }

        public static void createConfigFile() {
            createConfig();
        }

        public static void createConfig() {
            prepareConfigFile();
            String jsonString = DailyRewardEnhanced.GSON.toJson(new DailyRewardConfig().initDefaultBlackList());
            try (FileWriter fileWriter = new FileWriter(configFile)) {
                fileWriter.write(jsonString);
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
            }
        }

        public static void loadConfig() {
            prepareConfigFile();
            try {
                if (!configFile.exists()) {
                    createConfigFile();
                }
                if (configFile.exists()) {
                    BufferedReader bReader = new BufferedReader(new FileReader(configFile));
                    ArrayList<Object> savedConfig = DailyRewardEnhanced.GSON.fromJson(bReader, ArrayList.class);
                    if (savedConfig != null) {
                        DailyRewardEnhanced.CONFIG.setBlackList(savedConfig);
                    }
                }
            } catch (FileNotFoundException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't load configuration for daily-reward-enhanced. Reverting to default.", e);
                createConfigFile();
            }
        }
    }

    private void setBlackList(ArrayList<Object> savedConfig) {
        this.blacklist.clear();
        this.blacklist.add(savedConfig);
    }

    public void addBlackList(String itemName) {
        this.blacklist.add(itemName);
    }

    public ArrayList<Object> loadBlackList() {
        return DailyRewardEnhanced.CONFIG.blacklist;
    }

    private ArrayList<Object> initDefaultBlackList() {
        ArrayList<Object> res = new ArrayList<>();
        res.add("recovery_compass");
        res.add("command_block_minecart");
        return res;
    }
}
