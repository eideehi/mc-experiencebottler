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

import net.eidee.minecraft.experiencebottler.stat.Stats;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/** Experience Bottler's stat initializer. */
public class StatInitializer {
  private StatInitializer() {}

  private static void register(Identifier identifier, StatFormatter formatter) {
    Registry.register(Registry.CUSTOM_STAT, identifier, identifier);
    net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(identifier, formatter);
  }

  /** Initializes the stats. */
  public static void init() {
    register(Stats.INTERACT_WITH_EXPERIENCE_BOTTLER, StatFormatter.DEFAULT);
  }
}
