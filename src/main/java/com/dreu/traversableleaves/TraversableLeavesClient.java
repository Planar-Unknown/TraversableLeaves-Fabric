package com.dreu.traversableleaves;

import com.dreu.traversableleaves.network.PacketHandler;
import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("unused")
public class TraversableLeavesClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    PacketHandler.register();
  }
}