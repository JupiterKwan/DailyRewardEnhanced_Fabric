package mc.central.hk.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mc.central.hk.DailyRewardEnhanced;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
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
import java.util.Random;

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
            // Roll Control
            // ====GameTick is 20 per second====
            // int titleShowMinTick = 1;
            int titleShowMaxTick = 2 * 20;
            // =================================

//        while (titleShowMinTick < titleShowMaxTick) {
//            ItemStack itemStack = randItemStack();
//            Text stupid = Text.translatable(String.valueOf(itemStack.getName()));
//            TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(0, titleShowMinTick, 0);
//            player.networkHandler.sendPacket(titleFadeS2CPacket);
//            Text titleText = Text.literal(String.valueOf(stupid)).setStyle(Style.EMPTY.withColor(Formatting.GOLD));
//            player.networkHandler.sendPacket(new TitleS2CPacket(titleText));
//            player.playSound(SoundEvents.BLOCK_DISPENSER_LAUNCH);
//            titleShowMinTick += titleShowMaxTick + 2;
//        }

            boolean isItem = false;
            ItemStack itemStack = randItemStack();

            while (!isItem) {
                DailyRewardEnhanced.LOGGER.info(itemStack.getItem().getTranslationKey().split("\\.")[2]);
                if (DailyRewardEnhanced.CONFIG.loadBlackList().toString().contains(itemStack.getItem().getTranslationKey().split("\\.")[2])) {
                    itemStack = randItemStack();
                } else {
                    isItem = true;
                }
            }

            // Text stupid = Text.translatable(String.valueOf(itemStack.getName()));
            TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(0, titleShowMaxTick, 20);
            player.networkHandler.sendPacket(titleFadeS2CPacket);
            Text titleText = Text.literal("歡迎翻來!").setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA));
            player.networkHandler.sendPacket(new TitleS2CPacket(titleText));
            Text rewardText = Text.literal("今日登錄獎勵係：" + itemStack.getItem().getTranslationKey().split("\\.")[2] + " * " + itemStack.getCount()).setStyle(Style.EMPTY.withColor(Formatting.GOLD));
            player.sendMessage(rewardText, false);
            // PlaySoundFromEntityS2CPacket playSoundFromEntityS2CPacket = new PlaySoundFromEntityS2CPacket(SoundEvent.of(SoundEvents.ENTITY_PLAYER_HURT.getId()));
            if (isPlayerInventoryFull(player)) {
                Text fullInventoryText = Text.literal("注意啦，獎勵喺你嘅腳下喔").setStyle(Style.EMPTY.withColor(Formatting.RED));
                player.sendMessage(fullInventoryText, false);
                player.dropItem(itemStack, false);
            } else {
                player.getInventory().insertStack(itemStack);
            }
            DailyRewardEnhanced.LOGGER.info(String.valueOf(LocalDate.now()));
            DailyRewardEnhanced.PLAYER_LOGIN_DATES.put(player.getUuid().toString(), LocalDate.now());
        }
    }

}