package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.dreu.traversableleaves.interfaces.ITraversableEntity.canTraverse;

@SuppressWarnings({"unused", "deprecation"})
@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {

  @Redirect(
      method = "computeNext",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
      )
  )
  private VoxelShape redirectGetCollisionShape(BlockState blockState, BlockGetter level, BlockPos blockPos, CollisionContext context) {
    if (blockState.getBlock() instanceof ITraversableBlock iTraversableBlock && iTraversableBlock.isTraversable()) {
      if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
        if (entityContext.getEntity() instanceof LivingEntity livingEntity) {
          if (livingEntity instanceof Player player && player.isCreative() && player.getAbilities().flying)
            return Shapes.empty();
          if (context.isAbove(blockState.getBlock().getCollisionShape(blockState, level, blockPos, context), blockPos, false) && !context.isDescending())
            return blockState.getBlock().getCollisionShape(blockState, level, blockPos, CollisionContext.empty());
          if (canTraverse(livingEntity))
              return Shapes.empty();
        } else if (!(entityContext.getEntity() instanceof ItemEntity)) {
          return Shapes.empty();
        }
      }
    }
    return blockState.getCollisionShape(level, blockPos, context);
  }
}
