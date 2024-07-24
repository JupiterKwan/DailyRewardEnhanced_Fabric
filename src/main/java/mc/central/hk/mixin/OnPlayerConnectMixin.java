package mc.central.hk.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mc.central.hk.DailyRewardEnhanced;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Mixin(PlayerManager.class)
public class OnPlayerConnectMixin {


    @Unique
    private static ItemStack randItemStack() {
        // 获取注册表中物品的数量
        int itemSize = Registries.ITEM.size();
        Random randNum = new Random();
        return new ItemStack(Item.byRawId(randNum.nextInt(itemSize)), randNum.nextInt(5) + 1);
    }

    @Unique
    private static boolean isPlayerInventoryFull(PlayerEntity player) {
        return player.getInventory().getEmptySlot() <= 0;
    }

    @Inject(at = @At(value = "RETURN"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) throws InterruptedException {
        boolean isAlreadyLogin = false;
        if (DailyRewardEnhanced.PLAYER_LOGIN_DATES.containsKey(player.getUuid().toString())) {
            LocalDate lastLoginDate = DailyRewardEnhanced.PLAYER_LOGIN_DATES.get(player.getUuid().toString());
            if (LocalDate.now().equals(lastLoginDate)) {
                isAlreadyLogin = true;
                player.sendMessage(Text.literal("You've logged in today! No reward this time!").setStyle(Style.EMPTY.withColor(Formatting.GRAY)), false);
            }
        }

        if (!isAlreadyLogin) {

            CompletableFuture<Void> futureRollReward = CompletableFuture.supplyAsync(() -> {

                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                SendHello(player);
                TitleFadeS2CPacket titleFadeS2CPacket;
                Text titleText;
                SoundEvent soundEvent;
                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Roll Control
                // ====GameTick is 20 per second====
                int titleShowMinTick = 2;
                int titleShowMaxTick = 20;
                int count = 0;
                // =================================

                while (titleShowMinTick < titleShowMaxTick) {
//                for (int i = 0; i < 30; i++) {
                    ItemStack itemStack = randItemStack();
                    titleFadeS2CPacket = new TitleFadeS2CPacket(2, titleShowMinTick, 2);
                    player.networkHandler.sendPacket(titleFadeS2CPacket);
                    Text rewardText = Text.literal("今日登錄獎勵係...").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
                    player.networkHandler.sendPacket(new TitleS2CPacket(rewardText));
                    titleText = Text.literal(String.valueOf(itemStack.getItem().getTranslationKey().split("\\.")[2])).setStyle(Style.EMPTY.withColor(Formatting.GOLD));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(titleText));
                    soundEvent = SoundEvents.BLOCK_DISPENSER_FAIL;
                    player.networkHandler.sendPacket(new PlaySoundFromEntityS2CPacket(RegistryEntry.of(soundEvent), SoundCategory.PLAYERS, player, 1,1, 1));
                    player.playSound(SoundEvents.BLOCK_DISPENSER_LAUNCH);
                    try {
                        Thread.sleep(titleShowMinTick * 45L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (count++ <= 10) {
                        titleShowMinTick = titleShowMinTick + 1;
                    } else if (count++ <= 20) {
                        titleShowMinTick = titleShowMinTick + 2;
                    } else {
                        titleShowMinTick = titleShowMinTick * 2;
                    }
                }

                boolean isItem = false;
                ItemStack itemStack = randItemStack();

                while (!isItem) {
                    DailyRewardEnhanced.LOGGER.info("{}{}", itemStack.getItem().getTranslationKey().split("\\.")[2], " * ", itemStack.getCount());
                    if (DailyRewardEnhanced.CONFIG.loadBlackList().toString().contains(itemStack.getItem().getTranslationKey().split("\\.")[2])) {
                        itemStack = randItemStack();
                    } else {
                        isItem = true;
                    }
                }

                Text rewardText = Text.literal("今日登錄獎勵係：" + itemStack.getItem().getTranslationKey().split("\\.")[2] + " * " + itemStack.getCount()).setStyle(Style.EMPTY.withColor(Formatting.GOLD));
                titleFadeS2CPacket = new TitleFadeS2CPacket(0, 20 * 2, 20);
                player.networkHandler.sendPacket(titleFadeS2CPacket);
                titleText = Text.literal(String.valueOf(itemStack.getItem().getTranslationKey().split("\\.")[2])).setStyle(Style.EMPTY.withColor(Formatting.GOLD));
                player.networkHandler.sendPacket(new TitleS2CPacket(titleText));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of("")));
                player.networkHandler.sendPacket(new GameMessageS2CPacket(rewardText, false));
                soundEvent = SoundEvents.ENTITY_PLAYER_LEVELUP;
                player.networkHandler.sendPacket(new PlaySoundFromEntityS2CPacket(RegistryEntry.of(soundEvent), SoundCategory.PLAYERS, player, 1,1, 1));
                if (isPlayerInventoryFull(player)) {
                    Text fullInventoryText = Text.literal("注意啦，獎勵喺你嘅腳下喔").setStyle(Style.EMPTY.withColor(Formatting.RED));
                    player.sendMessage(fullInventoryText, false);
                    player.dropItem(itemStack, false);
                } else {
                    player.getInventory().insertStack(itemStack);
                }
                DailyRewardEnhanced.LOGGER.info(String.valueOf(LocalDate.now()));
                DailyRewardEnhanced.PLAYER_LOGIN_DATES.put(player.getUuid().toString(), LocalDate.now());
                return null;
            });
        } else {
            SendHello(player);
        }
    }

    @Unique
    private void SendHello(ServerPlayerEntity player) {
        TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(5, 15 * 2, 5);
        player.networkHandler.sendPacket(titleFadeS2CPacket);
        Text titleText = Text.literal("歡迎翻來!").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
        Text titleText2 = Text.literal("Central HK").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
        SoundEvent soundEvent = SoundEvents.ENTITY_CAT_PURREOW;
        player.networkHandler.sendPacket(new PlaySoundFromEntityS2CPacket(RegistryEntry.of(soundEvent), SoundCategory.PLAYERS, player, 1,1, new Random().nextLong(100000L)));
        player.networkHandler.sendPacket(new TitleS2CPacket(titleText));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(titleText2));
    }

}