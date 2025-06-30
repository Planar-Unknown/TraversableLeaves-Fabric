package com.dreu.traversableleaves.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import static com.dreu.traversableleaves.network.SyncConfigS2CPacket.SYNC_CONFIG_PACKET_ID;

public class PacketHandler {

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(SYNC_CONFIG_PACKET_ID, SyncConfigS2CPacket::handle);
  }
}
