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
import net.eidee.minecraft.experiencebottler.annotation.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.experiencebottler.component.DataComponentTypes;
import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.eidee.minecraft.experiencebottler.item.BottledExperienceItem;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Experience Bottler's item initializer. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemInitializer {
  private ItemInitializer() {}

  private static void registerItem(
      Item item,
      ResourceKey<CreativeModeTab> itemGroup,
      @Nullable CreativeModeTabEvents.ModifyOutput modifyEntries,
      Identifier id) {
    Registry.register(BuiltInRegistries.ITEM, id, item);
    if (modifyEntries != null) {
      CreativeModeTabEvents.modifyOutputEvent(itemGroup).register(modifyEntries);
    } else {
      CreativeModeTabEvents.modifyOutputEvent(itemGroup).register(entries -> entries.accept(item));
    }
  }

  private static void registerDataComponent(String id, DataComponentType<?> type) {
    Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, identifier(id), type);
  }

  /** Initializes the items. */
  static void init() {
    registerDataComponent("experience", DataComponentTypes.BOTTLED_EXPERIENCE);
    ItemComponentTooltipProviderRegistry.addLast(DataComponentTypes.BOTTLED_EXPERIENCE);

    registerItem(
        Items.BOTTLED_EXPERIENCE,
        CreativeModeTabs.FOOD_AND_DRINKS,
        entries -> {
          for (int experience : BottledExperienceItem.EXPERIENCE_LIST) {
            ItemStack stack = new ItemStack(Items.BOTTLED_EXPERIENCE);
            BottledExperienceComponent.setExperienceValue(stack, experience);
            entries.accept(stack);
          }
        },
        Identifiers.BOTTLED_EXPERIENCE);
  }
}
