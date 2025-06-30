package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.TraversableLeaves;
import com.dreu.traversableleaves.config.TLConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
@Mixin(PlayerList.class)
public class PlayerListClientMixin {
  @Inject(
      method = "placeNewPlayer",
      at = @At("TAIL"),
      cancellable = true
  )
  public void onPlayerJoin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
    if (Minecraft.getInstance().isLocalServer() && !TraversableLeaves.configHasBeenPopulated) {
      TLConfig.parse();
      TLConfig.populate();
      TraversableLeaves.configHasBeenPopulated = true;
    }
  }
}
