package mc.central.hk;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PlayerJoinCallback {

    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, (listeners) -> (player, server) -> {
        for (PlayerJoinCallback listener : listeners) {
            listener.joinServer(player, server);
        }
    });

    void joinServer(ServerPlayerEntity player, MinecraftServer server);

//    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, (listeners) -> (player) -> {
//        for (PlayerJoinCallback listener : listeners) {
//            ActionResult result = listener.onPlayerJoin(player);
//            if (result != ActionResult.PASS) {
//                return result;
//            }
//        }
//        return ActionResult.PASS;
//    });
//
//    ActionResult onPlayerJoin(ServerPlayerEntity player);
}
