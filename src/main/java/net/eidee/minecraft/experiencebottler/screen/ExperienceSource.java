/*
 * MIT License
 *
 * Copyright (c) 2022 EideeHi
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

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.MOD_ID;

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ExperienceSource implements Inventory {
  private ItemStack stack;

  protected ExperienceSource(ItemStack stack) {
    this.stack = stack;
  }

  public abstract Text getSourceName();

  public abstract long getTotalExperience();

  public abstract void removeExperience(int experience);

  public static ExperienceSource forClient() {
    return new ExperienceSource(ItemStack.EMPTY) {
      private String sourceName = "";
      private long totalExperience;

      @Override
      public Text getSourceName() {
        return Text.of(sourceName);
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
      public void markDirty() {
        NbtCompound nbt = getStack(0).getSubNbt(MOD_ID);
        if (nbt != null) {
          sourceName = nbt.getString("SourceName");
          totalExperience = nbt.getLong("TotalExperience");
        }
      }

      @Override
      public boolean canPlayerUse(PlayerEntity player) {
        return true;
      }
    };
  }

  public static ExperienceSource fromPlayer(PlayerEntity player, ScreenHandlerContext context) {
    return new ExperienceSource(new ItemStack(net.minecraft.block.Blocks.GLASS)) {
      @Override
      public Text getSourceName() {
        return Text.translatable("gui.experiencebottler.label.experience_source.player");
      }

      @Override
      public long getTotalExperience() {
        return ExperienceUtil.getTotalExperience(player);
      }

      @Override
      public void removeExperience(int experience) {
        ExperienceUtil.addExperience(player, -experience);
        markDirty();
      }

      @Override
      public void markDirty() {
        NbtCompound nbt = getStack(0).getOrCreateSubNbt(MOD_ID);
        nbt.putString("SourceName", getSourceName().getString());
        nbt.putLong("TotalExperience", getTotalExperience());
      }

      @Override
      public boolean canPlayerUse(PlayerEntity user) {
        return player.getUuid().equals(user.getUuid())
            && context.get(
                (world, pos) ->
                    world.getBlockState(pos).isOf(Blocks.EXPERIENCE_BOTTLER)
                        && player.squaredDistanceTo(
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                            <= 64.0,
                false);
      }
    };
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  @Override
  public ItemStack getStack(int slot) {
    return stack;
  }

  @Override
  public ItemStack removeStack(int slot, int amount) {
    if (stack.isEmpty() || amount <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack result = stack.split(amount);
    markDirty();
    return result;
  }

  @Override
  public ItemStack removeStack(int slot) {
    ItemStack result = stack;
    stack = ItemStack.EMPTY;
    return result;
  }

  @Override
  public void setStack(int slot, ItemStack stack) {
    this.stack = stack;
    if (!stack.isEmpty() && stack.getCount() > getMaxCountPerStack()) {
      stack.setCount(getMaxCountPerStack());
    }
    markDirty();
  }

  @Override
  public void clear() {
    removeStack(0);
    markDirty();
  }

  @Override
  public void onOpen(PlayerEntity player) {
    markDirty();
  }

  @Override
  public void onClose(PlayerEntity player) {
    clear();
  }

  @Override
  public String toString() {
    return !stack.isEmpty() && stack.hasNbt() ? stack.getOrCreateNbt().asString() : "{}";
  }
}
