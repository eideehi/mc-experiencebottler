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

package net.eidee.minecraft.experiencebottler.core.init;

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
/** Experience Bottler's block initializer. */
public class BlockInitializer {
  private BlockInitializer() {}

  private static void register(
      Block block,
      BlockItem item,
      ItemGroup itemGroup,
      @Nullable ItemGroupEvents.ModifyEntries modifyEntries,
      Identifier identifier) {
    Registry.register(Registries.BLOCK, identifier, block);
    Registry.register(Registries.ITEM, identifier, item);
    if (modifyEntries != null) {
      ItemGroupEvents.modifyEntriesEvent(itemGroup).register(modifyEntries);
    } else {
      ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(item));
    }
  }

  /** Initializes the blocks. */
  static void init() {
    register(
        Blocks.EXPERIENCE_BOTTLER,
        new BlockItem(Blocks.EXPERIENCE_BOTTLER, new FabricItemSettings()),
        ItemGroups.FUNCTIONAL,
        null,
        Identifiers.EXPERIENCE_BOTTLER);
  }

  /** Initializes the blocks at client-side. */
  @Environment(EnvType.CLIENT)
  static void initClient() {
    BlockRenderLayerMap.INSTANCE.putBlock(Blocks.EXPERIENCE_BOTTLER, RenderLayer.getCutout());
  }
}
