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

package net.eidee.minecraft.experiencebottler.screen;

import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.item.BottledExperienceItem;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

/** The screen handler of the Experience Bottler block. */
public class ExperienceBottlerScreenHandler extends ScreenHandler {
  private final PlayerEntity player;
  private final ScreenHandlerContext context;
  private final Inventory input;
  private final Inventory result;
  private int bottlingExperience;

  public ExperienceBottlerScreenHandler(
      int id, PlayerInventory inventory, ScreenHandlerContext context) {
    super(ScreenHandlerTypes.EXPERIENCE_BOTTLER, id);
    player = inventory.player;
    this.context = context;
    input =
        new SimpleInventory(1) {
          @Override
          public void markDirty() {
            onContentChanged(this);
          }
        };
    result =
        new CraftingResultInventory() {
          @Override
          public void markDirty() {
            onContentChanged(this);
          }
        };

    addSlot(
        new Slot(input, 0, 20, 44) {
          @Override
          public boolean canInsert(ItemStack stack) {
            return stack.getItem() == net.minecraft.item.Items.GLASS_BOTTLE;
          }
        });

    addSlot(
        new Slot(result, 1, 20, 85) {
          @Override
          public boolean canInsert(ItemStack stack) {
            return false;
          }

          @Override
          public boolean canTakeItems(PlayerEntity playerEntity) {
            if (!player.isCreative()) {
              int experience = BottledExperienceItem.readExperienceTag(getStack());
              return experience > 0 && experience <= ExperienceUtil.getTotalExperience(player);
            }
            return true;
          }

          @Override
          public void onTakeItem(PlayerEntity player, ItemStack stack) {
            BottledExperienceItem.writeBottledTag(stack, true);
            if (!player.isCreative()) {
              int experience = BottledExperienceItem.readExperienceTag(stack);
              if (experience > 0) {
                player.addExperience(-experience);
              }
              ItemStack glassBottle = input.getStack(0).copy();
              if (!glassBottle.isEmpty()) {
                glassBottle.decrement(1);
                input.setStack(0, glassBottle);
              }
            }
          }
        });

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        addSlot(new Slot(inventory, 9 + j + (i * 9), 20 + (j * 18), 128 + (i * 18)));
      }
    }

    for (int i = 0; i < 9; ++i) {
      addSlot(new Slot(inventory, i, 20 + (i * 18), 186));
    }
  }

  public ExperienceBottlerScreenHandler(int syncId, PlayerInventory inventory) {
    this(syncId, inventory, ScreenHandlerContext.EMPTY);
  }

  private void updateResult() {
    boolean canResultCreate = bottlingExperience > 0 && !input.getStack(0).isEmpty();
    if (canResultCreate) {
      canResultCreate = ExperienceUtil.getTotalExperience(player) >= bottlingExperience;
      if (canResultCreate) {
        ItemStack result = new ItemStack(Items.BOTTLED_EXPERIENCE);
        BottledExperienceItem.writeExperienceTag(result, bottlingExperience);
        this.result.setStack(0, result);
      }
    }
    if (!canResultCreate) {
      result.setStack(0, ItemStack.EMPTY);
    }
    sendContentUpdates();
  }

  public void setBottlingExperience(int value) {
    bottlingExperience = value;
    updateResult();
  }

  @Override
  public void close(PlayerEntity player) {
    super.close(player);
    context.run((world, pos) -> dropInventory(player, input));
  }

  @Override
  public boolean canUse(PlayerEntity player) {
    return canUse(context, player, Blocks.EXPERIENCE_BOTTLER);
  }

  @Override
  public void onContentChanged(Inventory inventory) {
    super.onContentChanged(inventory);
    if (inventory == input || inventory == result) {
      updateResult();
    }
  }

  @Override
  public ItemStack transferSlot(PlayerEntity player, int index) {
    ItemStack stack = ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (slot.hasStack()) {
      ItemStack stackInSlot = slot.getStack();
      stack = stackInSlot.copy();
      if (index == 1) {
        if (!insertItem(stackInSlot, 2, 38, true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickTransfer(stackInSlot, stack);
      } else if (index == 0) {
        if (!insertItem(stackInSlot, 2, 38, true)) {
          return ItemStack.EMPTY;
        }
      } else if (!insertItem(stackInSlot, 0, 1, false)) {
        return ItemStack.EMPTY;
      }
      if (stackInSlot.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }
      if (stackInSlot.getCount() == stack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTakeItem(player, stack);
    }
    return stack;
  }
}
