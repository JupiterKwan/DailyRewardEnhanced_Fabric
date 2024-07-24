package mc.central.hk.config;

import com.mojang.brigadier.context.CommandContext;
import mc.central.hk.DailyRewardEnhanced;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.*;
import java.util.*;

public class DailyRewardConfig {

    private final ArrayList<Object> blacklist = new ArrayList<>();

    private void setBlackList(ArrayList<Object> savedConfig) {
        this.blacklist.clear();
        this.blacklist.addAll(savedConfig);
    }

    public int addBlackList(CommandContext<ServerCommandSource> ctx, String itemName) {
        if (this.blacklist.contains(itemName)) {
            ctx.getSource().sendFeedback(() -> Text.literal("%s is already in black list!".formatted(itemName)), false);
            return 0;
        } else {
            DailyRewardEnhanced.CONFIG.blacklist.add(itemName);
            int result = Manager.writeConfig(DailyRewardEnhanced.CONFIG.blacklist);
            if (result == 1) {
                ctx.getSource().sendFeedback(() -> Text.literal("%s is added to black list.".formatted(itemName)), true);
                return 1;
            } else {
                ctx.getSource().sendFeedback(() -> Text.literal("Something went wrong! So sad."), true);
                return 0;
            }

        }
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

    public static class Manager {
        private static File configFile;

        private static void prepareConfigFile() {
            if (configFile != null) {
                return;
            }
            configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), DailyRewardEnhanced.MOD_ID + ".json");
        }

        private static void createConfigFile() {
            createConfig();
        }

        private static void createConfig() {
            prepareConfigFile();
            try (FileWriter fileWriter = new FileWriter(configFile)) {
                fileWriter.write(new DailyRewardConfig().initDefaultBlackList().toString());
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
            }
        }

        public static int writeConfig(ArrayList<Object> newBlacklist) {
            boolean isFileDelete = configFile.delete();
            if (isFileDelete) {
                prepareConfigFile();
                try (FileWriter fileWriter = new FileWriter(configFile)) {
                    fileWriter.write(newBlacklist.toString());
                    return 1;
                } catch (IOException e) {
                    DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
                }
            } else {
                return 0;
            }
            return 0;
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
}
