package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dreu.traversableleaves.interfaces.ITraversableEntity.canTraverse;

@SuppressWarnings({"unused", "deprecation"})
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

  @Inject(method = "isSuffocating", at = @At("HEAD"), cancellable = true)
  private void onIsSuffocating(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    if (getBlock() instanceof ITraversableBlock block && block.isTraversable())
      cir.setReturnValue(false);
  }

  @Inject(method = "isPathfindable", at = @At("HEAD"), cancellable = true)
  public void onIsPathfindable(BlockGetter level, BlockPos blockPos, PathComputationType pathType, CallbackInfoReturnable<Boolean> cir) {
    if (this.getBlock() instanceof ITraversableBlock iTraversable && iTraversable.isTraversable())
      cir.setReturnValue(true);
  }

  @Inject(
      method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
      at = @At("HEAD"),
      cancellable = true
  )
  private void onGetCollisionShapeWithContext(BlockGetter level, BlockPos blockPos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
    if (getBlock() instanceof ITraversableBlock block && block.isTraversable()) {
      if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
        if (entityContext.getEntity() instanceof LivingEntity livingEntity) {
          if (livingEntity instanceof Player player && player.isCreative() && player.getAbilities().flying)
            cir.setReturnValue(Shapes.empty());
          if (context.isAbove(getBlock().getCollisionShape(asState(), level, blockPos, context), blockPos, false) && !context.isDescending())
            cir.setReturnValue(getBlock().getCollisionShape(asState(), level, blockPos, context));
          if (canTraverse(livingEntity)) {
            cir.setReturnValue(Shapes.empty());
          }
        } else if (!(entityContext.getEntity() instanceof ItemEntity)) {
          cir.setReturnValue(Shapes.empty());
        }
      }
    }
  }


  @Shadow
  protected abstract BlockState asState();

  @Shadow
  public abstract Block getBlock();


}