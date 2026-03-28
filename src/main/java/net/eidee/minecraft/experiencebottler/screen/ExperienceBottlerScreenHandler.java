/*
 * MIT License
 *
 * Copyright (c) 2022-2023 EideeHi
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
import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExperienceBottlerScreenHandler extends AbstractContainerMenu {
  private final ExperienceSource experienceSource;
  private final Container input;
  private final Container result;
  private int bottlingExperience;
  private int resultExperience;

  private final class ResultSlot extends Slot {
    private int pendingResultExperience;

    private ResultSlot() {
      super(result, 0, 20, 85);
    }

    private void prepareTake() {
      pendingResultExperience = resultExperience;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return false;
    }

    @Override
    public boolean mayPickup(Player player) {
      return player.isCreative()
          || (resultExperience > 0 && resultExperience <= experienceSource.getTotalExperience());
    }

    @Override
    public ItemStack remove(int amount) {
      if (hasItem() && amount > 0) {
        prepareTake();
      }
      return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
      int experience = pendingResultExperience;
      pendingResultExperience = 0;
      if (!player.isCreative()) {
        ItemStack glassBottle = input.getItem(0).copy();
        if (!glassBottle.isEmpty()) {
          glassBottle.shrink(1);
          input.setItem(0, glassBottle);
        }
        if (experience > 0 && experience <= experienceSource.getTotalExperience()) {
          experienceSource.removeExperience(experience);
        }
        updateResult();
      }
    }
  }

  public ExperienceBottlerScreenHandler(
      int syncId, Inventory inventory, ExperienceSource experienceSource) {
    super(ScreenHandlerTypes.EXPERIENCE_BOTTLER, syncId);
    this.experienceSource = experienceSource;

    experienceSource.startOpen(inventory.player);

    input =
        new SimpleContainer(1) {
          @Override
          public void setChanged() {
            slotsChanged(this);
          }
        };

    result =
        new ResultContainer() {
          @Override
          public void setChanged() {
            slotsChanged(this);
          }
        };

    addSlot(
        new Slot(input, 0, 20, 44) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == net.minecraft.world.item.Items.GLASS_BOTTLE;
          }
        });

    addSlot(new ResultSlot());

    addSlot(
        new HiddenSlot(experienceSource, 0) {
          @Override
          public void setChanged() {
            slotsChanged(experienceSource);
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

  public ExperienceBottlerScreenHandler(int syncId, Inventory inventory) {
    this(syncId, inventory, ExperienceSource.forClient());
  }

  private void updateResult() {
    if (bottlingExperience > 0
        && !input.getItem(0).isEmpty()
        && experienceSource.getTotalExperience() >= bottlingExperience) {
      ItemStack bottledExperience = new ItemStack(Items.BOTTLED_EXPERIENCE);
      BottledExperienceComponent.setExperienceValue(bottledExperience, bottlingExperience);
      resultExperience = bottlingExperience;
      result.setItem(0, bottledExperience);
    } else {
      resultExperience = 0;
      result.setItem(0, ItemStack.EMPTY);
    }
    broadcastChanges();
  }

  @Environment(EnvType.CLIENT)
  public Component getSourceName() {
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
  public void removed(Player player) {
    super.removed(player);
    experienceSource.stopOpen(player);
    clearContainer(player, input);
  }

  @Override
  public boolean stillValid(Player player) {
    return experienceSource.stillValid(player);
  }

  @Override
  public void slotsChanged(Container inventory) {
    super.slotsChanged(inventory);
    if (inventory == input || inventory == result || inventory == experienceSource) {
      updateResult();
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    ItemStack stack = ItemStack.EMPTY;
    Slot slot = slots.get(slotIndex);
    if (slot.hasItem()) {
      ItemStack stackInSlot = slot.getItem();
      stack = stackInSlot.copy();
      if (slot.container == input) {
        if (!moveItemStackTo(stackInSlot, 3, slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (slot.container == result) {
        if (slot instanceof ResultSlot resultSlot) {
          resultSlot.prepareTake();
        }
        if (!moveItemStackTo(stackInSlot, 3, slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
        return ItemStack.EMPTY;
      }
      if (stackInSlot.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      if (stackInSlot.getCount() == stack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(player, stack);
    }
    return stack;
  }
}
