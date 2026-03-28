package net.eidee.minecraft.experiencebottler.gametest;

import java.lang.reflect.Method;
import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.eidee.minecraft.experiencebottler.screen.ExperienceSource;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public class ExperienceBottlerGameTests implements CustomTestMethodInvoker {
  private static final BlockPos BOTTLER_POS = new BlockPos(1, 1, 1);
  private static final int INPUT_SLOT = 0;
  private static final int RESULT_SLOT = 1;
  private static final int BOTTLED_EXPERIENCE = 100;

  private static Player createPlayer(GameTestHelper context) {
    return context.makeMockPlayer(GameType.SURVIVAL);
  }

  private static ExperienceBottlerScreenHandler createHandler(Player player) {
    return new ExperienceBottlerScreenHandler(
        0, player.getInventory(), ExperienceSource.fromPlayer(player, ContainerLevelAccess.NULL));
  }

  private static void setInputBottle(ExperienceBottlerScreenHandler handler, int count) {
    handler.getSlot(INPUT_SLOT).set(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE, count));
  }

  @GameTest
  public void opensExperienceBottlerMenu(GameTestHelper context) {
    context.setBlock(BOTTLER_POS, Blocks.EXPERIENCE_BOTTLER.defaultBlockState());

    Player player = context.makeMockServerPlayerInLevel();
    context.useBlock(BOTTLER_POS, player);

    context.assertTrue(
        player.containerMenu instanceof ExperienceBottlerScreenHandler,
        "using the block should open the Experience Bottler menu");
    context.succeed();
  }

  @GameTest
  public void createsResultWhenBottleAndExperienceAreAvailable(GameTestHelper context) {
    Player player = createPlayer(context);
    ExperienceUtil.addExperience(player, 250);

    ExperienceBottlerScreenHandler handler = createHandler(player);
    setInputBottle(handler, 1);
    handler.setBottlingExperience(BOTTLED_EXPERIENCE);

    ItemStack result = handler.getSlot(RESULT_SLOT).getItem();
    context.assertFalse(result.isEmpty(), "result slot should contain a bottled experience item");
    context.assertTrue(
        result.getItem() == Items.BOTTLED_EXPERIENCE,
        "result slot should contain the mod's bottled experience item");
    context.assertValueEqual(
        BOTTLED_EXPERIENCE,
        BottledExperienceComponent.getExperienceValue(result),
        "result item should store the requested experience amount");
    context.succeed();
  }

  @GameTest
  public void keepsResultEmptyWhenPlayerLacksExperience(GameTestHelper context) {
    Player player = createPlayer(context);
    ExperienceUtil.addExperience(player, 50);

    ExperienceBottlerScreenHandler handler = createHandler(player);
    setInputBottle(handler, 1);
    handler.setBottlingExperience(BOTTLED_EXPERIENCE);

    context.assertTrue(
        handler.getSlot(RESULT_SLOT).getItem().isEmpty(),
        "result slot should stay empty when the player lacks enough experience");
    context.succeed();
  }

  @GameTest
  public void takingResultConsumesBottleAndExperience(GameTestHelper context) {
    Player player = createPlayer(context);
    ExperienceUtil.addExperience(player, 250);

    ExperienceBottlerScreenHandler handler = createHandler(player);
    setInputBottle(handler, 1);
    handler.setBottlingExperience(BOTTLED_EXPERIENCE);

    ItemStack taken = handler.getSlot(RESULT_SLOT).safeTake(1, 1, player);

    context.assertFalse(taken.isEmpty(), "taking the result should return the bottled XP stack");
    context.assertValueEqual(
        150L,
        ExperienceUtil.getTotalExperience(player),
        "taking the result should deduct the bottled experience from the player");
    context.assertTrue(
        handler.getSlot(INPUT_SLOT).getItem().isEmpty(),
        "taking the result should consume the glass bottle");
    context.assertTrue(
        handler.getSlot(RESULT_SLOT).getItem().isEmpty(),
        "result slot should be empty after taking the only output");
    context.succeed();
  }

  @GameTest
  public void shiftClickResultConsumesBottleAndTransfersOutput(GameTestHelper context) {
    Player player = createPlayer(context);
    ExperienceUtil.addExperience(player, 250);

    ExperienceBottlerScreenHandler handler = createHandler(player);
    setInputBottle(handler, 1);
    handler.setBottlingExperience(BOTTLED_EXPERIENCE);

    ItemStack moved = handler.quickMoveStack(player, RESULT_SLOT);

    context.assertFalse(moved.isEmpty(), "shift-click should move the crafted bottled XP stack");
    context.assertValueEqual(
        BOTTLED_EXPERIENCE,
        BottledExperienceComponent.getExperienceValue(moved),
        "shift-click should move the bottled XP stack with the requested experience value");
    context.assertValueEqual(
        150L,
        ExperienceUtil.getTotalExperience(player),
        "shift-click should deduct the bottled experience from the player");
    context.assertTrue(
        handler.getSlot(INPUT_SLOT).getItem().isEmpty(),
        "shift-click should consume the glass bottle");
    context.assertTrue(
        handler.getSlot(RESULT_SLOT).getItem().isEmpty(),
        "result slot should be empty after shift-clicking the only output");
    context.assertTrue(
        player
            .getInventory()
            .contains(
                stack ->
                    stack.getItem() == Items.BOTTLED_EXPERIENCE
                        && BottledExperienceComponent.getExperienceValue(stack)
                            == BOTTLED_EXPERIENCE),
        "shift-click should place the bottled XP stack into the player's inventory");
    context.succeed();
  }

  @Override
  public void invokeTestMethod(GameTestHelper context, Method method)
      throws ReflectiveOperationException {
    context.killAllEntities();
    method.invoke(this, context);
  }
}
