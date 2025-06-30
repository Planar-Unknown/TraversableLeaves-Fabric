package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(PistonMovingBlockEntity.class)
public class PistonMovingBlockEntityMixin {

  @Inject(
      method = "getCollisionRelatedBlockState",
      at = @At("RETURN"),
      cancellable = true
  )
  private void modifyCollisionState(CallbackInfoReturnable<BlockState> cir) {
    if (cir.getReturnValue().getBlock() instanceof ITraversableBlock iTraversable && iTraversable.isTraversable())
      cir.setReturnValue(Blocks.AIR.defaultBlockState());
  }
}



