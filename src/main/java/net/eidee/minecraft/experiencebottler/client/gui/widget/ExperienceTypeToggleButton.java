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

package net.eidee.minecraft.experiencebottler.client.gui.widget;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** A button widget to switch between displaying experience as either levels or points. */
@Environment(EnvType.CLIENT)
public class ExperienceTypeToggleButton extends Button {
  private static final Component TEXT_POINT =
      Component.translatable("gui.experiencebottler.experience_bottler.exp_display.point");
  private static final Component TEXT_LEVEL =
      Component.translatable("gui.experiencebottler.experience_bottler.exp_display.level");

  private final Consumer<ExperienceTypeToggleButton> action;
  private ExperienceType experienceType;

  public ExperienceTypeToggleButton(int x, int y, Consumer<ExperienceTypeToggleButton> action) {
    super(x, y, 18, 18, TEXT_POINT, button -> {}, DEFAULT_NARRATION);
    this.action = action;
    this.experienceType = ExperienceType.POINT;
    updatePresentation();
  }

  public ExperienceType getExperienceType() {
    return experienceType;
  }

  public void setExperienceType(ExperienceType experienceType) {
    this.experienceType = experienceType;
    updatePresentation();
  }

  private void updatePresentation() {
    Component message = experienceType == ExperienceType.POINT ? TEXT_POINT : TEXT_LEVEL;
    setMessage(message);
    setTooltip(Tooltip.create(message));
  }

  @Override
  public void onPress(InputWithModifiers input) {
    setExperienceType(experienceType.rotate());
    action.accept(this);
    setFocused(false);
  }

  @Override
  protected MutableComponent createNarrationMessage() {
    return Component.translatable("narration.experiencebottler.experience_type_toggle_button");
  }

  @Override
  public void updateWidgetNarration(NarrationElementOutput builder) {
    builder.add(NarratedElementType.TITLE, createNarrationMessage());
    if (!active) {
      return;
    }

    if (isFocused()) {
      builder.add(
          NarratedElementType.USAGE,
          Component.translatable(
              "narration.experiencebottler.experience_type_toggle_button.usage.focused"));
    } else {
      builder.add(
          NarratedElementType.USAGE,
          Component.translatable(
              "narration.experiencebottler.experience_type_toggle_button.usage.hovered"));
    }

    Component type = Component.translatable(experienceType.getNarrationKey());
    builder.add(
        NarratedElementType.HINT,
        Component.translatable(
            "narration.experiencebottler.experience_type_toggle_button.info.current_type", type));
  }

  @Override
  protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
    extractDefaultSprite(extractor);

    var font = Minecraft.getInstance().font;
    var message = getMessage();
    int color = active ? 0xFFFFFFFF : 0xFFA0A0A0;
    int textX = getX() + ((getWidth() - font.width(message)) / 2);
    int textY = getY() + ((getHeight() - font.lineHeight) / 2) + 1;
    extractor.text(font, message, textX, textY, color, false);
  }
}
