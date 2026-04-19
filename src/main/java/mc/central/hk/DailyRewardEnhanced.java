package mc.central.hk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import mc.central.hk.config.DailyRewardConfig;
import mc.central.hk.config.PlayersLastLoginConfig;
import mc.central.hk.i18n.ServerI18n;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DailyRewardEnhanced implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("daily-reward-enhanced");
    public static final String MOD_ID = "daily-reward-enhanced";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final DailyRewardConfig CONFIG = new DailyRewardConfig();
    public static final PlayersLastLoginConfig PLAYER_LOGIN_DATES = new PlayersLastLoginConfig();

    @Override
    public void onInitialize() {
        try {
            DailyRewardConfig.Manager.loadConfig();
            PLAYER_LOGIN_DATES.loadPlayersLoginDate();
            ServerI18n.init();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax in the config file", e);
        }
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("rewardBlackListAdd")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                        .then(argument("itemName", StringArgumentType.string())
                                .executes(ctx -> CONFIG.addBlackList(ctx, StringArgumentType.getString(ctx, "itemName"))))
        ));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("rewardBlackListDelete")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                        .then(argument("itemName", StringArgumentType.string())
                                .executes(ctx -> CONFIG.removeBlackList(ctx, StringArgumentType.getString(ctx, "itemName"))))
        ));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("rewardBlackList").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.list", CONFIG.loadBlackList().toString()), false);
                    return 1;
                })
        ));

        LOGGER.info("{}", CONFIG.loadBlackList());
        LOGGER.info("DailyRewardEnhanced initialized.");
    }
}