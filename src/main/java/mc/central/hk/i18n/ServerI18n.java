package mc.central.hk.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mc.central.hk.DailyRewardEnhanced;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;

public final class ServerI18n {
    private static final Gson GSON = new Gson();
    private static final Type LANG_MAP_TYPE = new TypeToken<Map<String, String>>() { }.getType();
    private static final String DEFAULT_LANG = "en_us";
    private static final String[] SUPPORTED_LANGS = new String[]{"en_us", "zh_cn", "zh_tw", "zh_hk", "lzh"};
    private static final Map<String, Map<String, String>> LANG_DATA = new HashMap<>();
    private static boolean initialized;

    private ServerI18n() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        for (String lang : SUPPORTED_LANGS) {
            LANG_DATA.put(lang, loadLangFile(lang));
        }
        initialized = true;
    }

    public static MutableComponent tr(ServerPlayer player, String key, Object... args) {
        init();
        String lang = player == null ? DEFAULT_LANG : normalizeLanguage(player.clientInformation().language());
        return Component.literal(translate(lang, key, args));
    }

    public static MutableComponent tr(CommandSourceStack source, String key, Object... args) {
        init();
        ServerPlayer player = source.getPlayer();
        String lang = player == null ? DEFAULT_LANG : normalizeLanguage(player.clientInformation().language());
        return Component.literal(translate(lang, key, args));
    }

    private static String translate(String lang, String key, Object... args) {
        Map<String, String> selected = LANG_DATA.getOrDefault(lang, LANG_DATA.get(DEFAULT_LANG));
        String template = selected.get(key);

        if (template == null) {
            template = LANG_DATA.getOrDefault(DEFAULT_LANG, Map.of()).getOrDefault(key, key);
        }

        Object[] normalizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            normalizedArgs[i] = arg instanceof Component component ? component.getString() : String.valueOf(arg);
        }

        try {
            return normalizedArgs.length == 0 ? template : String.format(Locale.ROOT, template, normalizedArgs);
        } catch (IllegalFormatException ex) {
            DailyRewardEnhanced.LOGGER.warn("Invalid translation format for key {} and lang {}", key, lang, ex);
            return template;
        }
    }

    private static String normalizeLanguage(String rawLanguage) {
        if (rawLanguage == null || rawLanguage.isBlank()) {
            return DEFAULT_LANG;
        }

        String normalized = rawLanguage.toLowerCase(Locale.ROOT).replace('-', '_');
        if (normalized.startsWith("zh_hk")) {
            return "zh_hk";
        }
        if (normalized.startsWith("zh_tw")) {
            return "zh_tw";
        }
        if (normalized.startsWith("zh_cn") || normalized.startsWith("zh_sg")) {
            return "zh_cn";
        }
        if (normalized.startsWith("lzh")) {
            return "lzh";
        }
        if (normalized.startsWith("en_")) {
            return "en_us";
        }

        return DEFAULT_LANG;
    }

    private static Map<String, String> loadLangFile(String lang) {
        String resourcePath = "/assets/daily-reward-enhanced/lang/" + lang + ".json";
        try (InputStream inputStream = ServerI18n.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                DailyRewardEnhanced.LOGGER.warn("Missing language file: {}", resourcePath);
                return Map.of();
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                Map<String, String> map = GSON.fromJson(reader, LANG_MAP_TYPE);
                return map == null ? Map.of() : map;
            }
        } catch (Exception ex) {
            DailyRewardEnhanced.LOGGER.error("Failed to load language file {}", resourcePath, ex);
            return Map.of();
        }
    }
}


