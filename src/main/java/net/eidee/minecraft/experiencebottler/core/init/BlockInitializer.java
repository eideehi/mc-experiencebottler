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

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.annotation.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/** Experience Bottler's block initializer. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockInitializer {
  private BlockInitializer() {}

  private static void register(
      Block block,
      BlockItem item,
      ResourceKey<CreativeModeTab> itemGroup,
      @Nullable CreativeModeTabEvents.ModifyOutput modifyEntries,
      Identifier identifier) {
    Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    Registry.register(BuiltInRegistries.ITEM, identifier, item);
    if (modifyEntries != null) {
      CreativeModeTabEvents.modifyOutputEvent(itemGroup).register(modifyEntries);
    } else {
      CreativeModeTabEvents.modifyOutputEvent(itemGroup).register(entries -> entries.accept(item));
    }
  }

  /** Initializes the blocks. */
  static void init() {
    register(
        Blocks.EXPERIENCE_BOTTLER,
        new BlockItem(
            Blocks.EXPERIENCE_BOTTLER,
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifiers.EXPERIENCE_BOTTLER))
                .useBlockDescriptionPrefix()),
        CreativeModeTabs.FUNCTIONAL_BLOCKS,
        null,
        Identifiers.EXPERIENCE_BOTTLER);
  }
}
