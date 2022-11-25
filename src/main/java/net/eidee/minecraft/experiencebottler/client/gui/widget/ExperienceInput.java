/*
 * MIT License
 *
 * Copyright (c) 2022 EideeHi
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
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.lwjgl.glfw.GLFW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class ExperienceInput extends ClickableWidget {

  private final TextRenderer textRenderer;
  private final Consumer<ExperienceInput> inputChangeListener;

  private long value;
  private String valueAsText = "0";
  private int[] colors = new int[] {0xFFFFFF, 0xA0A0A0, 0xE09090};
  private ExperienceType experienceType = ExperienceType.POINT;
  private int frame;

  public ExperienceInput(
      TextRenderer textRenderer, int x, int y, Consumer<ExperienceInput> inputChangeListener) {
    super(x, y, 90, 18, Text.empty());
    this.textRenderer = textRenderer;
    this.inputChangeListener = inputChangeListener;
    active = true;
  }

  private static long convertExperience(long experience, ExperienceType convertTo) {
    boolean isMinus = experience < 0;
    long converted =
        convertTo.isPoint()
            ? ExperienceUtil.getTotalExperienceToReachLevel(Math.abs(experience), 0)
            : ExperienceUtil.getLevelFromTotalExperience(Math.abs(experience));
    return isMinus ? -converted : converted;
  }

  private long adjustExperienceValue(long value) {
    return switch (experienceType) {
      case POINT -> Math.min(value, Integer.MAX_VALUE);
      case LEVEL -> Math.min(value, 21863);
    };
  }

  private int getTextColor() {
    if (valueAsText.startsWith("-")) {
      return colors[2];
    } else {
      return active ? colors[0] : colors[1];
    }
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    long oldValue = this.value;
    this.value = value;
    String minus = value < 0 ? "-" : "";
    if (Math.abs(value) > Integer.MAX_VALUE) {
      minus = value < 0 ? "--" : "++";
      valueAsText = minus + Integer.MAX_VALUE;
    } else {
      valueAsText = minus + Math.abs(value);
    }
    if (oldValue != value) {
      inputChangeListener.accept(this);
    }
  }

  public ExperienceType getExperienceType() {
    return experienceType;
  }

  public void setExperienceType(ExperienceType experienceType) {
    ExperienceType oldType = this.experienceType;
    this.experienceType = experienceType;
    if (oldType != experienceType) {
      if (active) {
        setValue(convertExperience(value, experienceType));
      } else {
        long val = experienceType.isLevel() ? convertExperience(value, experienceType) : value;
        valueAsText = Long.toString(val);
      }
    }
  }

  public void setTextColors(int normal, int disabled, int minus) {
    colors = new int[] {normal, disabled, minus};
  }

  public void setDefaultTextColor(int color) {
    colors[0] = color;
  }

  public void setDisabledTextColor(int color) {
    colors[1] = color;
  }

  public void setErrorTextColor(int color) {
    colors[2] = color;
  }

  public long getExperiencePoint() {
    return experienceType.isPoint() ? value : convertExperience(value, ExperienceType.POINT);
  }

  public void setExperiencePoint(long experiencePoint) {
    if (experienceType.isPoint()) {
      setValue(experiencePoint);
    } else {
      setValue(convertExperience(experiencePoint, ExperienceType.LEVEL));
    }
  }

  public void tick() {
    ++frame;
  }

  @Override
  public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    if (!visible) {
      return;
    }

    int left = getX();
    int right = left + getWidth();
    int top = getY();
    int bottom = top + getHeight();

    if (!isFocused() && isMouseOver(mouseX, mouseY)) {
      fill(matrices, left + 1, top + 1, right - 1, bottom - 1, 0x40FFFFFF);
    }

    if (isFocused()) {
      drawHorizontalLine(matrices, left, right - 1, top, 0xFFFFFFFF);
      drawHorizontalLine(matrices, left, right - 1, bottom - 1, 0xFFFFFFFF);
      drawVerticalLine(matrices, left, top, bottom, 0xFFFFFFFF);
      drawVerticalLine(matrices, right - 1, top, bottom, 0xFFFFFFFF);
    }

    String text = valueAsText;

    int marginRight = textRenderer.getWidth("_");
    if (active && isFocused()) {
      if (frame / 6 % 2 == 0) {
        marginRight = 0;
        text += "_";
      }
    }

    textRenderer.draw(
        matrices,
        text,
        right - textRenderer.getWidth(text) - marginRight - 3,
        bottom - textRenderer.fontHeight - 3,
        getTextColor());
  }

  @Override
  protected void onFocusedChanged(boolean newFocused) {
    if (newFocused) {
      setValue(0);
      frame = 0;
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean result = super.mouseClicked(mouseX, mouseY, button);
    if (result && !isFocused()) {
      setFocused(true);
      onFocusedChanged(isFocused());
    } else if (!result && isFocused()) {
      setFocused(false);
      onFocusedChanged(isFocused());
    }
    return result;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (!active || !visible || !isFocused()) {
      return false;
    }
    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
      setValue(value / 10);
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
      setValue(0);
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    if (!active || !visible || !isFocused()) {
      return false;
    }
    if (!Character.isDigit(chr) || (chr == '0' && value == 0)) {
      return false;
    }
    setValue(adjustExperienceValue((value * 10) + Character.getNumericValue(chr)));
    return true;
  }

  @Override
  public void playDownSound(SoundManager soundManager) {}

  @Override
  public boolean isNarratable() {
    return visible;
  }

  @Override
  protected MutableText getNarrationMessage() {
    return Text.translatable("narration.experiencebottler.experience_input_field");
  }

  @Override
  protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    builder.put(NarrationPart.TITLE, getNarrationMessage());
    if (!active) {
      return;
    }
    Text type = Text.translatable(getExperienceType().getNarrationKey());
    builder.put(
        NarrationPart.HINT,
        Text.translatable(
            "narration.experiencebottler.experience_input_field.info.value", value, type));
  }
}
