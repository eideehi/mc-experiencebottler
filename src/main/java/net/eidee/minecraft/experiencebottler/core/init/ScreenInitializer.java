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

import net.eidee.minecraft.experiencebottler.client.gui.screen.ExperienceBottlerScreen;
import net.eidee.minecraft.experiencebottler.core.constants.Identifiers;
import net.eidee.minecraft.experiencebottler.screen.ScreenHandlerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Experience Bottler's screen initializer. */
public class ScreenInitializer {
  private ScreenInitializer() {}

  /** Initializes the screens. */
  static void init() {
    Registry.register(
        Registries.SCREEN_HANDLER,
        Identifiers.EXPERIENCE_BOTTLER,
        ScreenHandlerTypes.EXPERIENCE_BOTTLER);
  }

  /** Initializes the screens at client-side. */
  @Environment(EnvType.CLIENT)
  static void initClient() {
    HandledScreens.register(ScreenHandlerTypes.EXPERIENCE_BOTTLER, ExperienceBottlerScreen::new);
  }
}
