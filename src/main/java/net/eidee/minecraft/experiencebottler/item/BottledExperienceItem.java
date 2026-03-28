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

package net.eidee.minecraft.experiencebottler.item;

import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** The item of bottled experience points. */
public class BottledExperienceItem extends Item {
  public static final int[] EXPERIENCE_LIST =
      new int[] {100, 500, 1000, 5000, 10000, 50000, 100000, 500000};
  private static final int MAX_USE_TIME = 32;

  public BottledExperienceItem(Item.Properties settings) {
    super(settings);
  }

  @Override
  public InteractionResult use(Level world, Player user, InteractionHand hand) {
    return ItemUtils.startUsingInstantly(world, user, hand);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
    Player player = user instanceof Player castUser ? castUser : null;

    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    if (player != null) {
      if (!world.isClientSide()) {
        int experience = BottledExperienceComponent.getExperienceValue(stack);
        if (experience > 0) {
          ExperienceUtil.addExperience(player, experience);
        }
      }

      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.isCreative()) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.isCreative()) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.GLASS_BOTTLE);
      }

      if (player != null) {
        player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
      }
    }

    user.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.DRINK;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity user) {
    return MAX_USE_TIME;
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }
}
