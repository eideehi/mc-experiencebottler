/*
 * MIT License
 *
 * Copyright (c) 2022-2023 EideeHi
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
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

  private long experiencePoint;
  private long experienceLevel;
  private long inputValue;
  private String displayText = "0";
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

  private void setDisplayText(long value) {
    long absValue = Math.abs(value);
    if (absValue > Integer.MAX_VALUE) {
      String symbol = value < 0 ? "--" : "++";
      String text = Long.toString(absValue);
      displayText = symbol + (text.substring(text.length() - 9));
    } else {
      displayText = Long.toString(value);
    }
  }

  private int getTextColor() {
    if (displayText.startsWith("-")) {
      return colors[2];
    } else {
      return active ? colors[0] : colors[1];
    }
  }

  private void changeInputValue(long inputValue) {
    if (this.inputValue != inputValue) {
      if (experienceType.isPoint()) {
        setExperiencePoint(inputValue);
      } else {
        setExperienceLevel(inputValue);
      }
      onInputValueChanged();
    }
  }

  private void onExperienceChanged() {
    if (experienceType.isPoint()) {
      inputValue = experiencePoint;
      setDisplayText(experiencePoint);
    } else {
      inputValue = experienceLevel;
      setDisplayText(experienceLevel);
    }
  }

  public long getExperiencePoint() {
    return experiencePoint;
  }

  public void setExperiencePoint(long experiencePoint) {
    long oldExperiencePoint = this.experiencePoint;
    this.experiencePoint = experiencePoint;
    if (experiencePoint != oldExperiencePoint) {
      experienceLevel = convertExperience(experiencePoint, ExperienceType.LEVEL);
      onExperienceChanged();
    }
  }

  public long getExperienceLevel() {
    return experienceLevel;
  }

  public void setExperienceLevel(long experienceLevel) {
    long oldExperienceLevel = this.experienceLevel;
    this.experienceLevel = experienceLevel;
    if (experienceLevel != oldExperienceLevel) {
      experiencePoint = convertExperience(experienceLevel, ExperienceType.POINT);
      onExperienceChanged();
    }
  }

  public ExperienceType getExperienceType() {
    return experienceType;
  }

  public void setExperienceType(ExperienceType experienceType) {
    ExperienceType oldExperienceType = this.experienceType;
    this.experienceType = experienceType;
    if (experienceType != oldExperienceType) {
      if (active && isFocused()) {
        changeInputValue(convertExperience(inputValue, experienceType));
      } else {
        setDisplayText(experienceType.isPoint() ? experiencePoint : experienceLevel);
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

  public void onInputValueChanged() {
    inputChangeListener.accept(this);
  }

  public void tick() {
    ++frame;
  }

  @Override
  public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
    if (!visible) {
      return;
    }

    int left = getX();
    int right = left + getWidth();
    int top = getY();
    int bottom = top + getHeight();

    if (!isFocused() && isMouseOver(mouseX, mouseY)) {
      context.fill(left + 1, top + 1, right - 1, bottom - 1, 0x40FFFFFF);
    }

    if (isFocused()) {
      context.drawHorizontalLine(left, right - 1, top, 0xFFFFFFFF);
      context.drawHorizontalLine(left, right - 1, bottom - 1, 0xFFFFFFFF);
      context.drawVerticalLine(left, top, bottom, 0xFFFFFFFF);
      context.drawVerticalLine(right - 1, top, bottom, 0xFFFFFFFF);
    }

    String text = displayText;

    int marginRight = textRenderer.getWidth("_");
    if (active && isFocused()) {
      if (frame / 6 % 2 == 0) {
        marginRight = 0;
        text += "_";
      }
    }

    context.drawText(
        textRenderer,
        text,
        right - textRenderer.getWidth(text) - marginRight - 3,
        bottom - textRenderer.fontHeight - 3,
        getTextColor(),
        false);
  }

  @Override
  public void setFocused(boolean focused) {
    super.setFocused(focused);
    if (focused) {
      changeInputValue(0);
      frame = 0;
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean result = super.mouseClicked(mouseX, mouseY, button);
    if (result && !isFocused()) {
      setFocused(true);
    } else if (!result && isFocused()) {
      setFocused(false);
    }
    return result;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (!active || !visible || !isFocused()) {
      return false;
    }
    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
      changeInputValue(inputValue / 10);
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
      changeInputValue(0);
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    if (!active || !visible || !isFocused()) {
      return false;
    }
    if (!Character.isDigit(chr) || (chr == '0' && inputValue == 0)) {
      return false;
    }
    changeInputValue(adjustExperienceValue((inputValue * 10) + Character.getNumericValue(chr)));
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
            "narration.experiencebottler.experience_input_field.info.value", inputValue, type));
  }
}
