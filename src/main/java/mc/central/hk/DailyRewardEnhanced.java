package mc.central.hk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import mc.central.hk.config.DailyRewardConfig;
import mc.central.hk.config.PlayersLastLoginConfig;
import mc.central.hk.config.StreakRewardsConfig;
import mc.central.hk.i18n.ServerI18n;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
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
    public static StreakRewardsConfig STREAK_REWARDS = new StreakRewardsConfig();

    private static int reloadConfigs(CommandContext<CommandSourceStack> ctx) {
        try {
            DailyRewardConfig.Manager.loadConfig();
            StreakRewardsConfig.Manager.loadConfig();
            ctx.getSource().sendSuccess(() -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.configreload.success"), true);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to reload DailyRewardEnhanced configs.", e);
            ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.configreload.error"));
            return 0;
        }
    }

    @Override
    public void onInitialize() {
        try {
            DailyRewardConfig.Manager.loadConfig();
            PLAYER_LOGIN_DATES.loadPlayersLoginState();
            StreakRewardsConfig.Manager.loadConfig();
            ServerI18n.init();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax in the config file", e);
        }
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("dailyreward")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                        .then(literal("blacklist")
                                .then(literal("list").executes(ctx -> {
                                    ctx.getSource().sendSuccess(
                                            () -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.blacklist.list", CONFIG.loadBlackList().toString()),
                                            false
                                    );
                                    return 1;
                                }))
                                .then(literal("add")
                                        .then(argument("itemName", StringArgumentType.string())
                                                .executes(ctx -> CONFIG.addBlackList(ctx, StringArgumentType.getString(ctx, "itemName")))))
                                .then(literal("remove")
                                        .then(argument("itemName", StringArgumentType.string())
                                                .executes(ctx -> CONFIG.removeBlackList(ctx, StringArgumentType.getString(ctx, "itemName"))))))
                        .then(literal("config")
                                .then(literal("list").executes(ctx -> STREAK_REWARDS.listRules(ctx)))
                                .then(literal("add")
                                        .then(argument("day", IntegerArgumentType.integer(1))
                                                .then(argument("countMultiplier", IntegerArgumentType.integer(1))
                                                        .then(argument("extraDraws", IntegerArgumentType.integer(0))
                                                                .executes(ctx -> STREAK_REWARDS.addRule(
                                                                        ctx,
                                                                        IntegerArgumentType.getInteger(ctx, "day"),
                                                                        IntegerArgumentType.getInteger(ctx, "countMultiplier"),
                                                                        IntegerArgumentType.getInteger(ctx, "extraDraws")
                                                                ))))))
                                .then(literal("remove")
                                        .then(argument("day", IntegerArgumentType.integer(1))
                                                .executes(ctx -> STREAK_REWARDS.removeRule(ctx, IntegerArgumentType.getInteger(ctx, "day")))))
                                .then(literal("reload").executes(DailyRewardEnhanced::reloadConfigs)))
        ));

        LOGGER.info("{}", CONFIG.loadBlackList());
        LOGGER.info("DailyRewardEnhanced initialized.");
    }
}