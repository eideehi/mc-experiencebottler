/*
 * MIT License
 *
 * Copyright (c) 2021 EideeHi
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

import java.util.List;
import net.eidee.minecraft.experiencebottler.ExperienceBottlerMod;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

/** The item of bottled experience points. */
public class BottledExperienceItem extends Item {
  private static final int MAX_USE_TIME = 32;
  private static final int[] EXPERIENCE_LIST = {100, 500, 1000, 5000, 10000, 50000, 100000, 500000};
  private static final String TAG_EXPERIENCE = ExperienceBottlerMod.MOD_ID + ":experience";
  private static final String TAG_BOTTLED = ExperienceBottlerMod.MOD_ID + ":bottled";

  public BottledExperienceItem(Settings settings) {
    super(settings);
  }

  /**
   * Get the experience value set in the item stack. If no experience value is set, it returns 0.
   */
  public static int readExperienceTag(ItemStack stack) {
    NbtCompound nbt = stack.getNbt();
    return nbt != null ? nbt.getInt(TAG_EXPERIENCE) : 0;
  }

  /** Get the bottled flag in the item stack. If no flag is set, it returns false. */
  public static boolean readBottledTag(ItemStack stack) {
    NbtCompound nbt = stack.getNbt();
    return nbt != null && nbt.getBoolean(TAG_BOTTLED);
  }

  /** Set the experience value in item stack. */
  public static void writeExperienceTag(ItemStack stack, int value) {
    stack.getOrCreateNbt().putInt(TAG_EXPERIENCE, value);
  }

  /** Set the bottled flag in item stack. */
  public static void writeBottledTag(ItemStack stack, boolean value) {
    stack.getOrCreateNbt().putBoolean(TAG_BOTTLED, value);
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
    return ItemUsage.consumeHeldItem(world, user, hand);
  }

  @Override
  public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
    PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

    if (player instanceof ServerPlayerEntity serverPlayer) {
      Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    if (player != null) {
      if (!world.isClient()) {
        int experience = readExperienceTag(stack);
        if (experience > 0) {
          player.addExperience(experience);
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
  public int getMaxUseTime(ItemStack stack) {
    return MAX_USE_TIME;
  }

  @Override
  public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
    if (!stack.isEmpty()) {
      int experience = readExperienceTag(stack);
      if (experience > 0) {
        Text value = Text.literal(String.format("%,d", experience));
        tooltip.add(Text.translatable("item.experiencebottler.bottled_experience.tooltip.0", value));

        if (world != null && world.isClient()) {
          this.appendTooltipForClient(stack, world, tooltip, context);
        }
      }
    }
  }

  @Environment(EnvType.CLIENT)
  private void appendTooltipForClient(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
    int experience = readExperienceTag(stack);
    if (experience > 0 && readBottledTag(stack)) {
      PlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
        int playerExperience = ExperienceUtil.getTotalExperience(player);
        int level = ExperienceUtil.getLevelFromTotalExperience(experience + playerExperience);
        Text value = Text.literal(String.format("%,d", level));
        tooltip.add(Text.translatable("item.experiencebottler.bottled_experience.tooltip.1", value));
      }
    }
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }

  @Override
  public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
    if (this.isIn(group)) {
      for (int experience : EXPERIENCE_LIST) {
        ItemStack stack = new ItemStack(this);
        writeExperienceTag(stack, experience);
        writeBottledTag(stack, true);
        stacks.add(stack);
      }
    }
  }
}
