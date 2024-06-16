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

package net.eidee.minecraft.experiencebottler.core.init;

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.identifier;

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.eidee.minecraft.experiencebottler.item.BottledExperienceItem;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

/** Experience Bottler's item initializer. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemInitializer {
  private ItemInitializer() {}

  private static void registerItem(
      Item item,
      RegistryKey<ItemGroup> itemGroup,
      @Nullable ItemGroupEvents.ModifyEntries modifyEntries,
      Identifier id) {
    Registry.register(Registries.ITEM, id, item);
    if (modifyEntries != null) {
      ItemGroupEvents.modifyEntriesEvent(itemGroup).register(modifyEntries);
    } else {
      ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(item));
    }
  }

  private static void registerDataComponent(String id, ComponentType<?> type) {
    Registry.register(Registries.DATA_COMPONENT_TYPE, identifier(id), type);
  }

  /** Initializes the items. */
  static void init() {
    registerDataComponent("experience", BottledExperienceItem.EXPERIENCE);

    registerItem(
        Items.BOTTLED_EXPERIENCE,
        ItemGroups.FOOD_AND_DRINK,
        entries -> {
          for (int experience : BottledExperienceItem.EXPERIENCE_LIST) {
            ItemStack stack = new ItemStack(Items.BOTTLED_EXPERIENCE);
            BottledExperienceItem.writeExperienceTag(stack, experience);
            entries.add(stack);
          }
        },
        Identifiers.BOTTLED_EXPERIENCE);
  }
}
