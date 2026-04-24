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
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

public class StreakRewardsConfig {
    private static final String CONFIG_FILE_NAME = "streak-rewards.json";
    private static final Type CONFIG_TYPE = new TypeToken<StreakRewardsConfig>() {}.getType();

    public static class Rule {
        private int day = 1;
        private int countMultiplier = 1;
        private int extraDraws = 0;

        public int getDay() {
            return Math.max(1, day);
        }

        public int getCountMultiplier() {
            return Math.max(1, countMultiplier);
        }

        public int getExtraDraws() {
            return Math.max(0, extraDraws);
        }
    }

    public static class RewardBonus {
        private final int countMultiplier;
        private final int drawTimes;

        public RewardBonus(int countMultiplier, int drawTimes) {
            this.countMultiplier = countMultiplier;
            this.drawTimes = drawTimes;
        }

        public int getCountMultiplier() {
            return countMultiplier;
        }

        public int getDrawTimes() {
            return drawTimes;
        }
    }

    private boolean enabled = true;
    private boolean stackByThreshold = true;
    private int maxCountMultiplier = 8;
    private int maxDrawTimes = 4;
    private List<Rule> rules = createDefaultRules();

    public int addRule(CommandContext<CommandSourceStack> ctx, int day, int countMultiplier, int extraDraws) {
        int normalizedDay = Math.max(1, day);
        if (findRuleByDay(normalizedDay) != null) {
            ctx.getSource().sendFailure(
                    ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.add.exists", normalizedDay)
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        Rule rule = new Rule();
        rule.day = normalizedDay;
        rule.countMultiplier = Math.max(1, countMultiplier);
        rule.extraDraws = Math.max(0, extraDraws);
        rules.add(rule);
        normalize();

        if (Manager.writeAndReload(this) == 1) {
            ctx.getSource().sendSuccess(
                    () -> ServerI18n.tr(
                            ctx.getSource(),
                            "command.daily-reward-enhanced.config.add.success",
                            normalizedDay,
                            rule.getCountMultiplier(),
                            rule.getExtraDraws()
                    ).withStyle(ChatFormatting.GREEN),
                    true
            );
            return 1;
        }

        ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.add.error"));
        return 0;
    }

    public int removeRule(CommandContext<CommandSourceStack> ctx, int day) {
        int normalizedDay = Math.max(1, day);
        Rule existingRule = findRuleByDay(normalizedDay);
        if (existingRule == null) {
            ctx.getSource().sendFailure(
                    ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.remove.not_found", normalizedDay)
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        rules.remove(existingRule);
        normalize();

        if (Manager.writeAndReload(this) == 1) {
            ctx.getSource().sendSuccess(
                    () -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.remove.success", normalizedDay)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
            return 1;
        }

        ctx.getSource().sendFailure(ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.remove.error"));
        return 0;
    }

    public int listRules(CommandContext<CommandSourceStack> ctx) {
        StringJoiner joiner = new StringJoiner("\n");
        for (Rule rule : rules) {
            joiner.add("day=" + rule.getDay() + ", x" + rule.getCountMultiplier() + ", extraDraws=" + rule.getExtraDraws());
        }

        String listText = joiner.length() == 0 ? "(empty)" : joiner.toString();
        ctx.getSource().sendSuccess(
                () -> ServerI18n.tr(ctx.getSource(), "command.daily-reward-enhanced.config.list", listText),
                false
        );
        return 1;
    }

    public RewardBonus evaluate(int streakDays) {
        if (!enabled || streakDays <= 0) {
            return new RewardBonus(1, 1);
        }

        int multiplier = 1;
        int extraDraws = 0;

        for (Rule rule : rules) {
            boolean match = stackByThreshold ? streakDays >= rule.getDay() : streakDays == rule.getDay();
            if (!match) {
                continue;
            }
            multiplier *= rule.getCountMultiplier();
            extraDraws += rule.getExtraDraws();
        }

        int cappedMultiplier = Math.max(1, Math.min(maxCountMultiplier, multiplier));
        int cappedDrawTimes = Math.max(1, Math.min(maxDrawTimes, 1 + extraDraws));
        return new RewardBonus(cappedMultiplier, cappedDrawTimes);
    }

    private static List<Rule> createDefaultRules() {
        List<Rule> defaults = new ArrayList<>();

        Rule day3 = new Rule();
        day3.day = 3;
        day3.countMultiplier = 2;
        day3.extraDraws = 0;
        defaults.add(day3);

        Rule day7 = new Rule();
        day7.day = 7;
        day7.countMultiplier = 1;
        day7.extraDraws = 1;
        defaults.add(day7);

        return defaults;
    }

    private void normalize() {
        if (rules == null || rules.isEmpty()) {
            rules = createDefaultRules();
        }
        rules.sort(Comparator.comparingInt(Rule::getDay));
        maxCountMultiplier = Math.max(1, maxCountMultiplier);
        maxDrawTimes = Math.max(1, maxDrawTimes);
    }

    private Rule findRuleByDay(int day) {
        for (Rule rule : rules) {
            if (rule.getDay() == day) {
                return rule;
            }
        }
        return null;
    }

    public static class Manager {
        private static Path configPath;

        private static synchronized void prepareConfigFile() {
            if (configPath != null) {
                return;
            }
            configPath = ConfigPaths.resolve(CONFIG_FILE_NAME);
        }

        private static void createDefaultConfig() {
            prepareConfigFile();
            StreakRewardsConfig defaults = new StreakRewardsConfig();
            defaults.normalize();
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                DailyRewardEnhanced.GSON.toJson(defaults, CONFIG_TYPE, writer);
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save streak rewards config.", e);
            }
        }

        public static void loadConfig() {
            prepareConfigFile();
            try {
                if (!Files.exists(configPath)) {
                    createDefaultConfig();
                }

                try (Reader reader = Files.newBufferedReader(configPath)) {
                    StreakRewardsConfig loaded = DailyRewardEnhanced.GSON.fromJson(reader, CONFIG_TYPE);
                    if (loaded == null) {
                        loaded = new StreakRewardsConfig();
                    }
                    loaded.normalize();
                    DailyRewardEnhanced.STREAK_REWARDS = loaded;
                }
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't load streak rewards config. Reverting to default.", e);
                StreakRewardsConfig fallback = new StreakRewardsConfig();
                fallback.normalize();
                DailyRewardEnhanced.STREAK_REWARDS = fallback;
            }
        }

        public static int writeConfig(StreakRewardsConfig config) {
            prepareConfigFile();
            config.normalize();
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                DailyRewardEnhanced.GSON.toJson(config, CONFIG_TYPE, writer);
                return 1;
            } catch (IOException e) {
                DailyRewardEnhanced.LOGGER.error("Couldn't save streak rewards config.", e);
                return 0;
            }
        }

        public static int writeAndReload(StreakRewardsConfig config) {
            if (writeConfig(config) != 1) {
                return 0;
            }
            loadConfig();
            return 1;
        }
    }
}



