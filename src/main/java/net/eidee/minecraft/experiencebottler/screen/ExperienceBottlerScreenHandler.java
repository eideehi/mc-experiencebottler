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

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.item.BottledExperienceItem;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExperienceBottlerScreenHandler extends ScreenHandler {
  private final ExperienceSource experienceSource;
  private final Inventory input;
  private final Inventory result;
  private int bottlingExperience;

  public ExperienceBottlerScreenHandler(
      int syncId, PlayerInventory inventory, ExperienceSource experienceSource) {
    super(ScreenHandlerTypes.EXPERIENCE_BOTTLER, syncId);
    this.experienceSource = experienceSource;

    experienceSource.onOpen(inventory.player);

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
        new Slot(result, 0, 20, 85) {
          @Override
          public boolean canInsert(ItemStack stack) {
            return false;
          }

          @Override
          public boolean canTakeItems(PlayerEntity playerEntity) {
            if (!playerEntity.isCreative()) {
              int experience = BottledExperienceItem.readExperienceTag(getStack());
              return experience > 0 && experience <= experienceSource.getTotalExperience();
            }
            return true;
          }

          @Override
          public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (!player.isCreative()) {
              ItemStack glassBottle = input.getStack(0).copy();
              if (!glassBottle.isEmpty()) {
                glassBottle.decrement(1);
                input.setStack(0, glassBottle);
              }
              int experience = BottledExperienceItem.readExperienceTag(stack);
              if (experience <= experienceSource.getTotalExperience()) {
                experienceSource.removeExperience(experience);
              }
              updateResult();
            }
          }
        });

    addSlot(
        new HiddenSlot(experienceSource, 0) {
          @Override
          public void markDirty() {
            onContentChanged(experienceSource);
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
    this(syncId, inventory, ExperienceSource.forClient());
  }

  private void updateResult() {
    if (bottlingExperience > 0
        && !input.getStack(0).isEmpty()
        && experienceSource.getTotalExperience() >= bottlingExperience) {
      ItemStack bottledExperience = new ItemStack(Items.BOTTLED_EXPERIENCE);
      BottledExperienceItem.writeExperienceTag(bottledExperience, bottlingExperience);
      result.setStack(0, bottledExperience);
    } else {
      result.setStack(0, ItemStack.EMPTY);
    }
    sendContentUpdates();
  }

  @Environment(EnvType.CLIENT)
  public Text getSourceName() {
    return experienceSource.getSourceName();
  }

  @Environment(EnvType.CLIENT)
  public long getSourceExperience() {
    return experienceSource.getTotalExperience();
  }

  public void setBottlingExperience(int value) {
    bottlingExperience = value;
    updateResult();
  }

  @Override
  public void close(PlayerEntity player) {
    super.close(player);
    experienceSource.onClose(player);
    dropInventory(player, input);
  }

  @Override
  public boolean canUse(PlayerEntity player) {
    return experienceSource.canPlayerUse(player);
  }

  @Override
  public void onContentChanged(Inventory inventory) {
    super.onContentChanged(inventory);
    if (inventory == input || inventory == result || inventory == experienceSource) {
      updateResult();
    }
  }

  @Override
  public ItemStack quickMove(PlayerEntity player, int slotIndex) {
    ItemStack stack = ItemStack.EMPTY;
    Slot slot = slots.get(slotIndex);
    if (slot.hasStack()) {
      ItemStack stackInSlot = slot.getStack();
      stack = stackInSlot.copy();
      if (slot.inventory == input) {
        if (!insertItem(stackInSlot, 3, slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (slot.inventory == result) {
        if (!insertItem(stackInSlot, 3, slots.size(), true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickTransfer(stackInSlot, stack);
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
