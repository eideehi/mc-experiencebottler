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

package net.eidee.minecraft.experiencebottler.core.init;

import net.eidee.minecraft.experiencebottler.block.Blocks;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/** Experience Bottler's block initializer. */
public class BlockInitializer {
  private BlockInitializer() {}

  private static void register(Block block, BlockItem item, Identifier identifier) {
    Registry.register(Registry.BLOCK, identifier, block);
    Registry.register(Registry.ITEM, identifier, item);
  }

  /** Initializes the blocks. */
  public static void init() {
    register(
        Blocks.EXPERIENCE_BOTTLER,
        new BlockItem(
            Blocks.EXPERIENCE_BOTTLER, new FabricItemSettings().group(ItemGroup.DECORATIONS)),
        Identifiers.EXPERIENCE_BOTTLER);
  }

  /** Initializes the blocks at client-side. */
  @Environment(EnvType.CLIENT)
  public static void initClient() {
    BlockRenderLayerMap.INSTANCE.putBlock(Blocks.EXPERIENCE_BOTTLER, RenderLayer.getCutout());
  }
}
