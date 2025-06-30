package com.dreu.traversableleaves.mixin;

import com.dreu.traversableleaves.interfaces.ITraversableBlock;
import com.dreu.traversableleaves.interfaces.ITraversableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.dreu.traversableleaves.config.TLConfig.MOVEMENT_MULTIPLIER;

@SuppressWarnings({"deprecation", "DataFlowIssue", "unused"})
@Mixin(Entity.class)
public class EntityMixin {

  private Entity self() {
    return (Entity) (Object) this;
  }

  @ModifyVariable(
      method = "moveRelative",
      at = @At(value = "HEAD"),
      index = 2,
      argsOnly = true
  )
  private Vec3 modifyMoveRelativeVec3(Vec3 originalVec) {
    if (self() instanceof ITraversableEntity iTraversableEntity && iTraversableEntity.isStuckInLeaves()) {
      iTraversableEntity.setStuckInLeaves(false);
      float armorMultiplier = MOVEMENT_MULTIPLIER + iTraversableEntity.getArmorBonus();
      return originalVec.multiply(armorMultiplier, 1f, armorMultiplier);
    }
    return originalVec;
  }

  @Redirect(
      method = "checkInsideBlocks",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"
      )
  )
  private void redirectEntityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    if (entity instanceof LivingEntity livingEntity && shouldTraverse(blockState, level, blockPos, entity)) {
      ((ITraversableEntity) livingEntity).setStuckInLeaves(true);
      livingEntity.resetFallDistance();
      if (livingEntity.isDescending())
        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().multiply(1, 0.5f, 1f));
      createAmbience(entity, blockPos, blockState);
    }
    blockState.entityInside(level, blockPos, entity);
  }

  private boolean shouldTraverse(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    return blockState.getBlock() instanceof ITraversableBlock block && block.isTraversable()
        && !(entity instanceof Player player && player.isCreative() && player.getAbilities().flying)
        && !(entity.position().y >= blockState.getBlock()
        .getCollisionShape(blockState, level, blockPos, CollisionContext.empty())
        .max(Direction.Axis.Y) + blockPos.getY());
  }

  private void createAmbience(Entity entity, BlockPos blockPos, BlockState blockState){
    if (!entity.position().equals(new Vec3(entity.xOld, entity.yOld, entity.zOld))) {
      if (entity.level.getGameTime() % 15 == 1) {
        entity.playSound(blockState.getSoundType().getStepSound(), 0.1f, 0.6f);
      }
      if (entity.level.getGameTime() % 4 == 1){
        double d0 = (double) blockPos.getX() + entity.level.random.nextDouble();
        double d1 = (double) blockPos.getY() + 1;
        double d2 = (double) blockPos.getZ() + entity.level.random.nextDouble();
        entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d0, d1, d2, 0, 0, 0);
      }
    }
  }
}

