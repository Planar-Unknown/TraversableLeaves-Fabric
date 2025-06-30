package com.dreu.traversableleaves.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

import static com.dreu.traversableleaves.TraversableLeaves.MODID;
import static com.dreu.traversableleaves.TraversableLeaves.configHasBeenPopulated;
import static com.dreu.traversableleaves.config.TLConfig.*;

public class SyncConfigS2CPacket {
  public static final ResourceLocation SYNC_CONFIG_PACKET_ID = new ResourceLocation(MODID, "sync_config_s2c_packet");

  public static FriendlyByteBuf getByteBuffer() {
    if (!configHasBeenPopulated) {
      parse();
      populate();
      configHasBeenPopulated = true;
    }
    FriendlyByteBuf buf = PacketByteBufs.create();
    buf.writeFloat(MOVEMENT_MULTIPLIER);
    buf.writeFloat(ARMOR_SCALE_FACTOR);
    buf.writeBoolean(IS_ENTITIES_WHITELIST);
    buf.writeBoolean(CAN_CLIMB);

    buf.writeInt(TL_BLOCKS.size());
    for (ResourceLocation block : TL_BLOCKS) {
      buf.writeInt(block.toString().length());
      buf.writeCharSequence(block.toString(), StandardCharsets.UTF_8);
    }

    buf.writeInt(TL_ENTITIES.size());
    for (ResourceLocation entity : TL_ENTITIES) {
      buf.writeInt(entity.toString().length());
      buf.writeCharSequence(entity.toString(), StandardCharsets.UTF_8);
    }
    return buf;
  }

  @SuppressWarnings("unused")
  public static void handle(Minecraft minecraft, ClientPacketListener clientPacketListener, FriendlyByteBuf buf, PacketSender packetSender) {
    TL_BLOCKS.clear();
    TL_ENTITIES.clear();
    MOVEMENT_MULTIPLIER = buf.readFloat();
    ARMOR_SCALE_FACTOR = buf.readFloat();
    IS_ENTITIES_WHITELIST = buf.readBoolean();
    CAN_CLIMB = buf.readBoolean();

    int bounds = buf.readInt();
    for (int i = 0; i < bounds; i++)
      TL_BLOCKS.add(new ResourceLocation(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString()));

    bounds = buf.readInt();
    for (int i = 0; i < bounds; i++)
      TL_ENTITIES.add(new ResourceLocation(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString()));
    configHasBeenPopulated = false;
  }
}
