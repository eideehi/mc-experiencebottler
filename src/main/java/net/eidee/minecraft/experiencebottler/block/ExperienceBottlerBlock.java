/*
 * MIT License
 *
 * Copyright (c) 2021-2024 EideeHi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.eidee.minecraft.experiencebottler.block;

import com.mojang.serialization.MapCodec;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.eidee.minecraft.experiencebottler.screen.ExperienceSource;
import net.eidee.minecraft.experiencebottler.stat.Stats;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** The block for bottling the player's experience points. */
public class ExperienceBottlerBlock extends HorizontalDirectionalBlock {
  private static final Component CONTAINER_TITLE;
  private static final VoxelShape TOP_SHAPE;
  private static final VoxelShape BOTTOM_SHAPE;
  private static final VoxelShape NORTH_SHAPE;
  private static final VoxelShape SOUTH_SHAPE;
  private static final VoxelShape EAST_SHAPE;
  private static final VoxelShape WEST_SHAPE;
  private static final MapCodec<ExperienceBottlerBlock> CODEC;

  static {
    CONTAINER_TITLE = Component.translatable("container.experiencebottler.experience_bottler");
    TOP_SHAPE = Block.box(0, 13, 0, 16, 16, 16);
    BOTTOM_SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    VoxelShape baseShape = Shapes.or(TOP_SHAPE, BOTTOM_SHAPE);
    NORTH_SHAPE =
        Shapes.or(
            baseShape,
            Block.box(0, 2, 10, 16, 13, 16),
            Block.box(0, 2, 0, 3, 6, 10),
            Block.box(13, 2, 0, 16, 6, 10));
    SOUTH_SHAPE =
        Shapes.or(
            baseShape,
            Block.box(0, 2, 0, 16, 13, 6),
            Block.box(0, 2, 6, 3, 6, 16),
            Block.box(13, 2, 6, 16, 6, 16));
    EAST_SHAPE =
        Shapes.or(
            baseShape,
            Block.box(0, 2, 0, 6, 13, 16),
            Block.box(6, 2, 0, 16, 6, 3),
            Block.box(6, 2, 13, 16, 6, 16));
    WEST_SHAPE =
        Shapes.or(
            baseShape,
            Block.box(10, 2, 0, 16, 13, 16),
            Block.box(0, 2, 0, 10, 6, 3),
            Block.box(0, 2, 13, 10, 6, 16));
    CODEC = BlockBehaviour.simpleCodec(ExperienceBottlerBlock::new);
  }

  public ExperienceBottlerBlock(BlockBehaviour.Properties settings) {
    super(settings);
    registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return switch (state.getValue(FACING)) {
      case NORTH -> NORTH_SHAPE;
      case SOUTH -> SOUTH_SHAPE;
      case EAST -> EAST_SHAPE;
      case WEST -> WEST_SHAPE;
      default -> Shapes.block();
    };
  }

  @Override
  protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
    return Shapes.block();
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  protected boolean isPathfindable(BlockState state, PathComputationType type) {
    return false;
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext ctx) {
    return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Nullable
  @Override
  protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
    return new ExtendedMenuProvider<>() {
      @Override
      public Unit getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return Unit.INSTANCE;
      }

      @Override
      public Component getDisplayName() {
        return CONTAINER_TITLE;
      }

      @Override
      public ExperienceBottlerScreenHandler createMenu(
          int syncId, net.minecraft.world.entity.player.Inventory inventory, Player player) {
        return new ExperienceBottlerScreenHandler(
            syncId,
            inventory,
            ExperienceSource.fromPlayer(player, ContainerLevelAccess.create(world, pos)));
      }
    };
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    if (world.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    player.openMenu(state.getMenuProvider(world, pos));
    player.awardStat(Stats.INTERACT_WITH_EXPERIENCE_BOTTLER);
    return InteractionResult.CONSUME;
  }
}
