package mc.central.hk.mixin;

import mc.central.hk.DailyRewardEnhanced;
import mc.central.hk.i18n.ServerI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Mixin(PlayerList.class)
public class OnPlayerConnectMixin {

    @Unique
    private static ItemStack randItemStack() {
        int itemSize = BuiltInRegistries.ITEM.size();
        Random random = new Random();
        return new ItemStack(Item.byId(random.nextInt(itemSize)), random.nextInt(5) + 1);
    }

    @Unique
    private static boolean isPlayerInventoryFull(Player player) {
        return player.getInventory().getFreeSlot() < 0;
    }

    @Unique
    private static String getItemIdPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Inject(at = @At("RETURN"), method = "placeNewPlayer")
    private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        boolean isAlreadyLogin = false;
        String uuid = player.getUUID().toString();

        if (DailyRewardEnhanced.PLAYER_LOGIN_DATES.getPlayersLoginDate().containsKey(uuid)) {
            String lastLoginDate = DailyRewardEnhanced.PLAYER_LOGIN_DATES.getPlayersLoginDate().get(uuid);
            player.sendSystemMessage(ServerI18n.tr(player, "message.daily-reward-enhanced.last_login_date", lastLoginDate).withStyle(ChatFormatting.GRAY));
            if (LocalDate.now().toString().equals(lastLoginDate)) {
                isAlreadyLogin = true;
                player.sendSystemMessage(ServerI18n.tr(player, "message.daily-reward-enhanced.already_claimed").withStyle(ChatFormatting.GRAY));
            }
        }

        if (!isAlreadyLogin) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1500);
                    sendHello(player);
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                int titleShowMinTick = 2;
                int titleShowMaxTick = 20;
                int count = 0;
                Holder<SoundEvent> rollSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.DISPENSER_FAIL);

                while (titleShowMinTick < titleShowMaxTick) {
                    ItemStack itemStack = randItemStack();
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, titleShowMinTick, 0));
                    Component rewardText = ServerI18n.tr(player, "title.daily-reward-enhanced.rolling").withStyle(ChatFormatting.GOLD);
                    player.connection.send(new ClientboundSetTitleTextPacket(rewardText));
                    Component titleText = Component.translatable(itemStack.getItem().getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
                    player.connection.send(new ClientboundSetSubtitleTextPacket(titleText));
                    player.connection.send(new ClientboundSoundEntityPacket(rollSound, SoundSource.PLAYERS, player, 1.0F, 1.0F, 1L));

                    try {
                        Thread.sleep(titleShowMinTick * 45L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (count++ <= 10) {
                        titleShowMinTick += 1;
                    } else if (count++ <= 20) {
                        titleShowMinTick += 2;
                    } else {
                        titleShowMinTick *= 2;
                    }
                }

                boolean isItem = false;
                ItemStack itemStack = randItemStack();

                while (!isItem) {
                    String itemId = getItemIdPath(itemStack);
                    DailyRewardEnhanced.LOGGER.info("{} * {}", itemId, itemStack.getCount());
                    if (DailyRewardEnhanced.CONFIG.loadBlackList().contains(itemId)) {
                        itemStack = randItemStack();
                    } else {
                        isItem = true;
                    }
                }

                String rewardItemDescriptionId = itemStack.getItem().getDescriptionId();
                int rewardCount = itemStack.getCount();

                player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 40, 20));
                Component titleText = Component.translatable(rewardItemDescriptionId).withStyle(ChatFormatting.GOLD);
                player.connection.send(new ClientboundSetTitleTextPacket(titleText));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));

                Holder<SoundEvent> rewardSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EXPERIENCE_ORB_PICKUP);
                player.connection.send(new ClientboundSoundEntityPacket(rewardSound, SoundSource.PLAYERS, player, 1.0F, 1.0F, 1L));

                if (isPlayerInventoryFull(player)) {
                    Component fullInventoryText = ServerI18n.tr(player, "message.daily-reward-enhanced.inventory_full").withStyle(ChatFormatting.RED);
                    player.sendSystemMessage(fullInventoryText);
                    player.drop(itemStack, false, false);
                } else {
                    player.getInventory().add(itemStack);
                }

                Component rewardMessage = ServerI18n.tr(player, "message.daily-reward-enhanced.reward_obtained_prefix")
                        .append(Component.literal(" "))
                        .append(Component.translatable(rewardItemDescriptionId))
                        .append(Component.literal(" * " + rewardCount));
                player.sendSystemMessage(rewardMessage);
                DailyRewardEnhanced.LOGGER.info("{}", LocalDate.now());
                try {
                    DailyRewardEnhanced.PLAYER_LOGIN_DATES.putPlayerLoginDate(uuid, LocalDate.now().toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            sendHello(player);
        }
    }

    @Unique
    private void sendHello(ServerPlayer player) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 30, 5));
        Component titleText = ServerI18n.tr(player, "title.daily-reward-enhanced.welcome").withStyle(ChatFormatting.GOLD);
        Component subtitleText = ServerI18n.tr(player, "subtitle.daily-reward-enhanced.server_name").withStyle(ChatFormatting.GOLD);
        player.connection.send(new ClientboundSoundEntityPacket(SoundEvents.CAT_PURREOW_BABY, SoundSource.PLAYERS, player, 1.0F, 1.0F, new Random().nextLong(100000L)));
        player.connection.send(new ClientboundSetTitleTextPacket(titleText));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitleText));
    }

}