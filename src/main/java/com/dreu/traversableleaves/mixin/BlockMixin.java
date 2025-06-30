package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unused")
@Mixin(Block.class)
public class BlockMixin implements ITraversableBlock {}
