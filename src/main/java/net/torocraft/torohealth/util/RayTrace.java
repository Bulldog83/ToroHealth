package net.torocraft.torohealth.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class RayTrace implements BlockView {
  private static Predicate<Entity> isVisible = entity -> !entity.isSpectator() && entity.collides();

  @Override
  public BlockEntity getBlockEntity(BlockPos pos) {
    return MinecraftClient.getInstance().world.getBlockEntity(pos);
  }

  @Override
  public BlockState getBlockState(BlockPos pos) {
    return MinecraftClient.getInstance().world.getBlockState(pos);
  }

  @Override
  public FluidState getFluidState(BlockPos pos) {
    return MinecraftClient.getInstance().world.getFluidState(pos);
  }

  public LivingEntity getEntityInCrosshair (float partialTicks, double reachDistance) {
    MinecraftClient client = MinecraftClient.getInstance();
    Entity viewer = client.getCameraEntity();

    if (viewer == null) {
      return null;
    }

    Vec3d position = viewer.getCameraPosVec(partialTicks);
    Vec3d look = viewer.getRotationVec(1.0F);
    Vec3d max = position.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
    Box searchBox = viewer.getBoundingBox().stretch(look.multiply(reachDistance)).expand(1.0D, 1.0D, 1.0D);

    EntityHitResult result = ProjectileUtil.rayTrace(viewer, position, max, searchBox, isVisible, reachDistance * reachDistance);

    if (result == null || result.getEntity() == null) {
      return null;
    }

    if (result.getEntity() instanceof LivingEntity) {
      LivingEntity target = (LivingEntity) result.getEntity();

      HitResult blockHit = rayTrace(setupRayTraceContext(client.player, reachDistance, FluidHandling.NONE));

      if (!blockHit.getType().equals(Type.MISS)) {
        double blockDistance = blockHit.getPos().distanceTo(position);
        if (blockDistance > target.distanceTo(client.player)) {
          return target;
        }
      } else {
        return target;
      }
    }

    return null;
  }

  private RayTraceContext setupRayTraceContext(PlayerEntity player, double distance, RayTraceContext.FluidHandling fluidHandling) {
    float pitch = player.pitch;
    float yaw = player.yaw;
    Vec3d fromPos = player.getCameraPosVec(1.0F);
    float float_3 = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
    float float_4 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
    float float_5 = -MathHelper.cos(-pitch * 0.017453292F);
    float xComponent = float_4 * float_5;
    float yComponent = MathHelper.sin(-pitch * 0.017453292F);
    float zComponent = float_3 * float_5;
    Vec3d toPos = fromPos.add((double)xComponent * distance, (double)yComponent * distance, (double)zComponent * distance);
    return new RayTraceContext(fromPos, toPos, RayTraceContext.ShapeType.OUTLINE, fluidHandling, player);
  }

  @Override
  public BlockHitResult rayTrace(RayTraceContext context) {
    return BlockView.rayTrace(context, (c, pos) -> {
      BlockState block = this.getBlockState(pos);
      if (!block.isOpaque()) {
        return null;
      }
      VoxelShape blockShape = c.getBlockShape(block, this, pos);
      return this.rayTraceBlock(c.getStart(), c.getEnd(), pos, blockShape, block);
    }, (c) -> {
      Vec3d v = c.getStart().subtract(c.getEnd());
      return BlockHitResult.createMissed(c.getEnd(), Direction.getFacing(v.x, v.y, v.z), new BlockPos(c.getEnd()));
    });
  }
}