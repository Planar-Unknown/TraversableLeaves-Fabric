package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.config.TLConfig;
import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import com.dreu.traversableleaves.interfaces.ITraversableEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ITraversableEntity {

  @ModifyExpressionValue(
      method = "onClimbable",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"
      )
  )
  private boolean extendClimbableCheck(boolean original) {
    if (TLConfig.CAN_CLIMB
        && ITraversableEntity.canTraverse(self())
        && ((ITraversableBlock) self().getFeetBlockState().getBlock()).isTraversable()
        && this.jumping
        && !(self() instanceof Player player && player.isCreative() && player.getAbilities().flying))
      return true;
    return original;
  }

  @SuppressWarnings("DataFlowIssue")
  private LivingEntity self() {
    return (LivingEntity) (Object) this;
  }

  @Shadow
  protected boolean jumping;

  @Unique
  private boolean isStuckInLeaves = false;

  @Override
  public boolean isStuckInLeaves() {
    return isStuckInLeaves;
  }

  @Override
  public void setStuckInLeaves(boolean bool) {
    isStuckInLeaves = bool;
  }
}
