package mc.central.hk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mc.central.hk.config.DailyRewardConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.*;

public class DailyRewardEnhanced implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("daily-reward-enhanced");
    public static final String MOD_ID = "daily-reward-enhanced";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final DailyRewardConfig CONFIG = new DailyRewardConfig();
    public static final HashMap<String, LocalDate> PLAYER_LOGIN_DATES = new HashMap<>();

    @Override
    public void onInitialize() {
        try {
            PLAYER_LOGIN_DATES.clear();
            DailyRewardConfig.Manager.loadConfig();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax in the config file", e);
        }
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
//        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
//				.register(literal("rewardBlackList")
//				.requires(source -> source.hasPermissionLevel(2))
//				.then(literal("add "))
//                .then(argument("itemName", StringArgumentType.string())
//                        .executes(ctx -> addBlackListItem(ctx, StringArgumentType.getString(ctx, "itemName"))))));
//        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
//                .register(literal("rewardBlackList")
//                .requires(source -> source.hasPermissionLevel(2))
//                .then(literal("list "))
//				.executes(context -> {
//            	// 对于 1.19 之前的版本，把“Text.literal”替换为“new LiteralText”。
//            	// 对于 1.20 之前的版本，请移除“() ->”。
//            		context.getSource().sendFeedback(() -> Text.literal(CONFIG.loadBlackList().toString()), false);
//            		return 1;
//        })));
        LOGGER.info(DailyRewardEnhanced.CONFIG.loadBlackList().toString());
        LOGGER.info("Hello Stupid Fabric world!");
    }
//
//    private static int addBlackListItem(CommandContext<ServerCommandSource> ctx, String itemName) {
//        // 添加到黑名单
//        CONFIG.addBlackList(itemName);
//        return 1;
//    }
}