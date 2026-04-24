package mc.central.hk.config;

import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.context.CommandContext;
import mc.central.hk.DailyRewardEnhanced;
import mc.central.hk.i18n.ServerI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DailyRewardConfig {

    private static final Type STRING_LIST_TYPE = new TypeToken<ArrayList<String>>() {}.getType();
    private final ArrayList<String> blacklist = new ArrayList<>();

    private void setBlackList(List<String> savedConfig) {
        this.blacklist.clear();
        this.blacklist.addAll(savedConfig);
    }

    public int addBlackList(CommandContext<CommandSourceStack> ctx, String itemName) {
        if (this.blacklist.contains(itemName)) {
            ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.add.exists", itemName).withStyle(ChatFormatting.RED));
            return 0;
        } else {
            DailyRewardEnhanced.CONFIG.blacklist.add(itemName);
            int result = Manager.writeAndReload(DailyRewardEnhanced.CONFIG.blacklist);
            if (result == 1) {
                ctx.getSource().sendSuccess(() -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.add.success", itemName).withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.add.error"));
                return 0;
            }

        }
    }

    public int removeBlackList(CommandContext<CommandSourceStack> ctx, String itemName) {
        if (!this.blacklist.contains(itemName)) {
            ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.delete.not_found", itemName).withStyle(ChatFormatting.RED));
            return 0;
        }

        DailyRewardEnhanced.CONFIG.blacklist.remove(itemName);
        int result = Manager.writeAndReload(DailyRewardEnhanced.CONFIG.blacklist);
        if (result == 1) {
            ctx.getSource().sendSuccess(() -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.delete.success", itemName).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }

        ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.delete.error"));
        return 0;
    }

    public ArrayList<String> loadBlackList() {
        return DailyRewardEnhanced.CONFIG.blacklist;
    }

    private ArrayList<String> initDefaultBlackList() {
        ArrayList<String> res = new ArrayList<>();
        res.add("recovery_compass");
        res.add("command_block_minecart");
        return res;
    }

    public static class Manager {
        private static final String LEGACY_FILE_NAME = DailyRewardEnhanced.MOD_ID + ".json";
        private static final String CONFIG_FILE_NAME = "blacklist.json";
        private static Path configPath;

        private static synchronized void prepareConfigFile() {
            if (configPath != null) {
                return;
            }
            configPath = ConfigPaths.resolve(CONFIG_FILE_NAME);
            ConfigPaths.migrateLegacyFile(LEGACY_FILE_NAME, configPath);
        }

        private static void createDefaultConfig() {
            prepareConfigFile();
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                DailyRewardEnhanced.GSON.toJson(new DailyRewardConfig().initDefaultBlackList(), writer);
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
            }
        }

        public static int writeConfig(List<String> newBlacklist) {
            prepareConfigFile();
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                DailyRewardEnhanced.GSON.toJson(newBlacklist, writer);
                return 1;
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save daily-reward-enhanced config.", e);
                return 0;
            }
        }

        public static int writeAndReload(List<String> newBlacklist) {
            if (writeConfig(newBlacklist) != 1) {
                return 0;
            }
            loadConfig();
            return 1;
        }

        public static void loadConfig() {
            prepareConfigFile();
            try {
                if (!Files.exists(configPath)) {
                    createDefaultConfig();
                }

                try (Reader reader = Files.newBufferedReader(configPath)) {
                    ArrayList<String> savedConfig = DailyRewardEnhanced.GSON.fromJson(reader, STRING_LIST_TYPE);
                    DailyRewardEnhanced.CONFIG.setBlackList(savedConfig == null ? new DailyRewardConfig().initDefaultBlackList() : savedConfig);
                }
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't load configuration for daily-reward-enhanced. Reverting to default.", e);
                createDefaultConfig();
            }
        }
    }
}
