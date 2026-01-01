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
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/** The item of bottled experience points. */
public class BottledExperienceItem extends Item {
  public static final int[] EXPERIENCE_LIST =
      new int[] {100, 500, 1000, 5000, 10000, 50000, 100000, 500000};
  private static final int MAX_USE_TIME = 32;

  public BottledExperienceItem(net.minecraft.item.Item.Settings settings) {
    super(settings);
  }

  @Override
  public ActionResult use(World world, PlayerEntity user, Hand hand) {
    return ItemUsage.consumeHeldItem(world, user, hand);
  }

  public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
    PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

    if (player instanceof ServerPlayerEntity serverPlayer) {
      Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    if (player != null) {
      if (!world.isClient()) {
        int experience = BottledExperienceComponent.getExperienceValue(stack);
        if (experience > 0) {
          ExperienceUtil.addExperience(player, experience);
        }
      }

      player.incrementStat(Stats.USED.getOrCreateStat(this));
      if (!player.isCreative()) {
        stack.decrement(1);
      }
    }

    if (player == null || !player.isCreative()) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.GLASS_BOTTLE);
      }

      if (player != null) {
        player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
      }
    }

    user.emitGameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public UseAction getUseAction(ItemStack stack) {
    return UseAction.DRINK;
  }

  @Override
  public int getMaxUseTime(ItemStack stack, LivingEntity user) {
    return MAX_USE_TIME;
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
