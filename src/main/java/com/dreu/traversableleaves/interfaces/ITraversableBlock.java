package com.dreu.traversableleaves.interfaces;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import static com.dreu.traversableleaves.config.TLConfig.TL_BLOCKS;

public interface ITraversableBlock {
  default boolean isTraversable() {
    return TL_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey((Block) this));
  }
}