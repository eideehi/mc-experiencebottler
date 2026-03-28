/*
 * MIT License
 *
 * Copyright (c) 2022-2024 EideeHi
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

package net.eidee.minecraft.experiencebottler.screen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.annotation.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ExperienceSource implements Container {
  private ItemStack stack;

  protected ExperienceSource(ItemStack stack) {
    this.stack = stack;
  }

  public static ExperienceSource forClient() {
    return new ExperienceSource(new ItemStack(net.minecraft.world.level.block.Blocks.GLASS)) {
      private String sourceName = "";
      private long totalExperience;

      @Override
      public Component getSourceName() {
        return Component.literal(sourceName);
      }

      @Override
      public long getTotalExperience() {
        return totalExperience;
      }

      @Override
      public void removeExperience(int experience) {
        totalExperience -= experience;
      }

      @Override
      public void setChanged() {
        CompoundTag nbt =
            getItem(0)
                .getComponents()
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        sourceName = nbt.getString("SourceName").orElse("");
        totalExperience = nbt.getLong("TotalExperience").orElse(0L);
      }

      @Override
      public boolean stillValid(Player player) {
        return true;
      }
    };
  }

  public static ExperienceSource fromPlayer(Player player, ContainerLevelAccess context) {
    return new ExperienceSource(new ItemStack(net.minecraft.world.level.block.Blocks.GLASS)) {
      @Override
      public Component getSourceName() {
        return Component.translatable("gui.experiencebottler.label.experience_source.player");
      }

      @Override
      public long getTotalExperience() {
        return ExperienceUtil.getTotalExperience(player);
      }

      @Override
      public void removeExperience(int experience) {
        ExperienceUtil.addExperience(player, -experience);
        setChanged();
      }

      @Override
      public void setChanged() {
        CustomData.update(
            DataComponents.CUSTOM_DATA,
            getItem(0),
            (nbt) -> {
              nbt.putString("SourceName", getSourceName().getString());
              nbt.putLong("TotalExperience", getTotalExperience());
            });
      }

      @Override
      public boolean stillValid(Player user) {
        return player.getUUID().equals(user.getUUID())
            && context.evaluate(
                (world, pos) ->
                    world.getBlockState(pos).is(Blocks.EXPERIENCE_BOTTLER)
                        && player.distanceToSqr(
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                            <= 64.0,
                false);
      }
    };
  }

  public abstract Component getSourceName();

  public abstract long getTotalExperience();

  public abstract void removeExperience(int experience);

  @Override
  public int getContainerSize() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  @Override
  public ItemStack getItem(int slot) {
    return stack;
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    if (stack.isEmpty() || amount <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack result = stack.split(amount);
    setChanged();
    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack result = stack;
    stack = ItemStack.EMPTY;
    return result;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    this.stack = stack;
    if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
      stack.setCount(getMaxStackSize());
    }
    setChanged();
  }

  @Override
  public void clearContent() {
    removeItemNoUpdate(0);
    setChanged();
  }

  @Override
  public void startOpen(ContainerUser user) {
    setChanged();
  }

  @Override
  public void stopOpen(ContainerUser user) {
    clearContent();
  }

  @Override
  public String toString() {
    if (!stack.isEmpty()) {
      CustomData component = stack.getComponents().get(DataComponents.CUSTOM_DATA);
      if (component != null) {
        return component.copyTag().toString();
      }
    }
    return "{}";
  }
}
