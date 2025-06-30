package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.network.SyncConfigS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dreu.traversableleaves.network.SyncConfigS2CPacket.SYNC_CONFIG_PACKET_ID;

@SuppressWarnings("unused")
@Mixin(PlayerList.class)
public class PlayerListServerMixin {
  @Inject(
      method = "placeNewPlayer",
      at = @At("TAIL"),
      cancellable = true
  )
  public void onPlayerJoin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
    ServerPlayNetworking.send(serverPlayer, SYNC_CONFIG_PACKET_ID, SyncConfigS2CPacket.getByteBuffer());
  }
}
