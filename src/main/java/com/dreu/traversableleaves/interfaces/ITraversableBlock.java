package com.dreu.traversableleaves.interfaces;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

import static com.dreu.traversableleaves.config.TLConfig.TL_BLOCKS;

public interface ITraversableBlock {
  default boolean isTraversable() {
    return TL_BLOCKS.contains(Registry.BLOCK.getKey((Block) this));
  }
}