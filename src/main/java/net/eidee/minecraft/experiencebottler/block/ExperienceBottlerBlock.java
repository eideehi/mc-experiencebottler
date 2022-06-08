/*
 * MIT License
 *
 * Copyright (c) 2021-2022 EideeHi
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

import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.eidee.minecraft.experiencebottler.stat.Stats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** The block for bottling the player's experience points. */
public class ExperienceBottlerBlock extends HorizontalFacingBlock {
  private static final Text CONTAINER_TITLE;
  private static final VoxelShape TOP_SHAPE;
  private static final VoxelShape BOTTOM_SHAPE;
  private static final VoxelShape NORTH_SHAPE;
  private static final VoxelShape SOUTH_SHAPE;
  private static final VoxelShape EAST_SHAPE;
  private static final VoxelShape WEST_SHAPE;

  static {
    CONTAINER_TITLE = Text.translatable("container.experiencebottler.experience_bottler");
    TOP_SHAPE = Block.createCuboidShape(0, 13, 0, 16, 16, 16);
    BOTTOM_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 2, 16);
    VoxelShape baseShape = VoxelShapes.union(TOP_SHAPE, BOTTOM_SHAPE);
    NORTH_SHAPE =
        VoxelShapes.union(
            baseShape,
            Block.createCuboidShape(0, 2, 10, 16, 13, 16),
            Block.createCuboidShape(0, 2, 0, 3, 6, 10),
            Block.createCuboidShape(13, 2, 0, 16, 6, 10));
    SOUTH_SHAPE =
        VoxelShapes.union(
            baseShape,
            Block.createCuboidShape(0, 2, 0, 16, 13, 6),
            Block.createCuboidShape(0, 2, 6, 3, 6, 16),
            Block.createCuboidShape(13, 2, 6, 16, 6, 16));
    EAST_SHAPE =
        VoxelShapes.union(
            baseShape,
            Block.createCuboidShape(0, 2, 0, 6, 13, 16),
            Block.createCuboidShape(6, 2, 0, 16, 6, 3),
            Block.createCuboidShape(6, 2, 13, 16, 6, 16));
    WEST_SHAPE =
        VoxelShapes.union(
            baseShape,
            Block.createCuboidShape(10, 2, 0, 16, 13, 16),
            Block.createCuboidShape(0, 2, 0, 10, 6, 3),
            Block.createCuboidShape(0, 2, 13, 10, 6, 16));
  }

  public ExperienceBottlerBlock(Settings settings) {
    super(settings);
    setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
      ShapeContext context) {
    return switch (state.get(FACING)) {
      case NORTH -> NORTH_SHAPE;
      case SOUTH -> SOUTH_SHAPE;
      case EAST -> EAST_SHAPE;
      case WEST -> WEST_SHAPE;
      default -> VoxelShapes.fullCube();
    };
  }

  @Override
  public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
    return VoxelShapes.fullCube();
  }

  @Override
  protected void appendProperties(Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public boolean canPathfindThrough(
      BlockState state, BlockView world, BlockPos pos, NavigationType type) {
    return false;
  }

  @Nullable
  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
  }

  @Nullable
  @Override
  public NamedScreenHandlerFactory createScreenHandlerFactory(
      BlockState state, World world, BlockPos pos) {
    return new SimpleNamedScreenHandlerFactory(
        (syncId, inventory, player) ->
            new ExperienceBottlerScreenHandler(
                syncId, inventory, ScreenHandlerContext.create(world, pos)),
        CONTAINER_TITLE);
  }

  @Override
  public ActionResult onUse(
      BlockState state,
      World world,
      BlockPos pos,
      PlayerEntity player,
      Hand hand,
      BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }
    player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
    player.incrementStat(Stats.INTERACT_WITH_EXPERIENCE_BOTTLER);
    return ActionResult.CONSUME;
  }
}
