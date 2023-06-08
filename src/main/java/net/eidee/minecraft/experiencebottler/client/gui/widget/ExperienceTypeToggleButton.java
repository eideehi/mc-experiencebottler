/*
 * MIT License
 *
 * Copyright (c) 2021-2023 EideeHi
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

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** A button widget to switch between displaying experience as either levels or points. */
@Environment(EnvType.CLIENT)
public class ExperienceTypeToggleButton extends PressableWidget {
  private static final Text TEXT_POINT;
  private static final Text TEXT_LEVEL;

  static {
    TEXT_POINT = Text.translatable("gui.experiencebottler.experience_bottler.exp_display.point");
    TEXT_LEVEL = Text.translatable("gui.experiencebottler.experience_bottler.exp_display.level");
  }

  private final int id;
  private final Consumer<ExperienceTypeToggleButton> action;

  private ExperienceType experienceType;

  public ExperienceTypeToggleButton(
      int id, int x, int y, Consumer<ExperienceTypeToggleButton> action) {
    super(x, y, 18, 18, ScreenTexts.EMPTY);
    this.id = id;
    this.action = action;
    experienceType = ExperienceType.POINT;
  }

  private int getTextureY() {
    int i = 1;
    if (!this.active) {
      i = 0;
    } else if (this.isHovered()) {
      i = 2;
    }
    return 46 + (i * 20);
  }

  public int getId() {
    return id;
  }

  public ExperienceType getExperienceType() {
    return experienceType;
  }

  public void setExperienceType(ExperienceType experienceType) {
    this.experienceType = experienceType;
  }

  @Override
  public Text getMessage() {
    return getExperienceType() == ExperienceType.POINT ? TEXT_POINT : TEXT_LEVEL;
  }

  @Override
  public void setMessage(Text message) {}

  @Override
  public void onPress() {
    setExperienceType(getExperienceType().rotate());
    action.accept(this);
  }

  @Override
  public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
    context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.enableDepthTest();

    final int width = getWidth();
    final int height = getHeight();
    final int halfWidth = width / 2;
    final int halfHeight = height / 2;

    final int left = getX();
    final int top = getY();
    final int xCenter = left + halfWidth;
    final int yCenter = top + halfHeight;

    int offsetY = getTextureY();

    context.drawTexture(WIDGETS_TEXTURE, left, top, 0, offsetY, halfWidth, halfHeight);
    context.drawTexture(
        WIDGETS_TEXTURE, xCenter, top, 200 - halfWidth, offsetY, halfWidth, halfHeight);
    context.drawTexture(
        WIDGETS_TEXTURE, left, yCenter, 0, (offsetY + 20) - halfHeight, halfWidth, halfHeight);
    context.drawTexture(
        WIDGETS_TEXTURE,
        xCenter,
        yCenter,
        200 - halfWidth,
        (offsetY + 20) - halfHeight,
        halfWidth,
        halfHeight);

    MinecraftClient minecraft = MinecraftClient.getInstance();
    int textColor = active ? 0xFFFFFF : 0xA0A0A0;
    TextRenderer textRenderer = minecraft.textRenderer;
    Text message = getMessage();
    context.drawText(
        textRenderer,
        message,
        xCenter - (textRenderer.getWidth(message) / 2),
        top + (height - 8) / 2,
        textColor | MathHelper.ceil(alpha * 255.0f) << 24,
        false);
  }

  @Override
  protected MutableText getNarrationMessage() {
    return Text.translatable("narration.experiencebottler.experience_type_toggle_button");
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    builder.put(NarrationPart.TITLE, getNarrationMessage());
    if (active) {
      if (isFocused()) {
        builder.put(
            NarrationPart.USAGE,
            Text.translatable(
                "narration.experiencebottler.experience_type_toggle_button.usage.focused"));
      } else {
        builder.put(
            NarrationPart.USAGE,
            Text.translatable(
                "narration.experiencebottler.experience_type_toggle_button.usage.hovered"));
      }

      Text type = Text.translatable(getExperienceType().getNarrationKey());
      builder.put(
          NarrationPart.HINT,
          Text.translatable(
              "narration.experiencebottler.experience_type_toggle_button.info.current_type", type));
    }
  }
}
