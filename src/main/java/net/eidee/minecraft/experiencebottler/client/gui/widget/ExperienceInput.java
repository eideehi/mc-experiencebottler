/*
 * MIT License
 *
 * Copyright (c) 2022-2024 EideeHi
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
import net.eidee.minecraft.experiencebottler.annotation.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class ExperienceInput extends EditBox {
  private final Font font;
  private final Consumer<ExperienceInput> inputChangeListener;

  private long experiencePoint;
  private long experienceLevel;
  private long inputValue;
  private String displayText = "0";
  private final int[] colors = new int[] {0xFFFFFFFF, 0xFFA0A0A0, 0xFFE09090};
  private ExperienceType experienceType = ExperienceType.POINT;

  public ExperienceInput(Font font, int x, int y, Consumer<ExperienceInput> inputChangeListener) {
    super(font, x, y, 90, 18, Component.empty());
    this.font = font;
    this.inputChangeListener = inputChangeListener;
    setBordered(false);
    setMaxLength(20);
    updateDisplayedValue(0);
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

  private void updateDisplayedValue(long value) {
    long absValue = Math.abs(value);
    if (absValue > Integer.MAX_VALUE) {
      String symbol = value < 0 ? "--" : "++";
      String text = Long.toString(absValue);
      displayText = symbol + text.substring(text.length() - 9);
    } else {
      displayText = Long.toString(value);
    }

    setValue(Long.toString(value));
    moveCursorToEnd(false);
    updateTextColors();
  }

  private void updateTextColors() {
    boolean negative = displayText.startsWith("-");
    int activeColor = negative ? colors[2] : colors[0];
    int inactiveColor = negative ? colors[2] : colors[1];
    setTextColor(activeColor);
    setTextColorUneditable(inactiveColor);
  }

  private int getDisplayTextColor() {
    if (displayText.startsWith("-")) {
      return colors[2];
    }
    return active ? colors[0] : colors[1];
  }

  private void changeInputValue(long newInputValue) {
    if (inputValue == newInputValue) {
      return;
    }

    if (experienceType.isPoint()) {
      setExperiencePoint(newInputValue);
    } else {
      setExperienceLevel(newInputValue);
    }
    onInputValueChanged();
  }

  private void onExperienceChanged() {
    if (experienceType.isPoint()) {
      inputValue = experiencePoint;
      updateDisplayedValue(experiencePoint);
    } else {
      inputValue = experienceLevel;
      updateDisplayedValue(experienceLevel);
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
        onExperienceChanged();
      }
    }
  }

  public void setTextColors(int normal, int disabled, int minus) {
    colors[0] = normal;
    colors[1] = disabled;
    colors[2] = minus;
    updateTextColors();
  }

  public void setDefaultTextColor(int color) {
    colors[0] = color;
    updateTextColors();
  }

  public void setDisabledTextColor(int color) {
    colors[1] = color;
    updateTextColors();
  }

  public void setErrorTextColor(int color) {
    colors[2] = color;
    updateTextColors();
  }

  public void onInputValueChanged() {
    inputChangeListener.accept(this);
  }

  @Override
  public void extractWidgetRenderState(
      net.minecraft.client.gui.GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
    if (!visible) {
      return;
    }

    int left = getX();
    int right = left + getWidth();
    int top = getY();
    int bottom = top + getHeight();

    if (!isFocused() && isMouseOver(mouseX, mouseY)) {
      extractor.fill(left + 1, top + 1, right - 1, bottom - 1, 0x40FFFFFF);
    }

    if (isFocused()) {
      extractor.horizontalLine(left, right - 1, top, 0xFFFFFFFF);
      extractor.horizontalLine(left, right - 1, bottom - 1, 0xFFFFFFFF);
      extractor.verticalLine(left, top, bottom - 1, 0xFFFFFFFF);
      extractor.verticalLine(right - 1, top, bottom - 1, 0xFFFFFFFF);
    }

    String text = displayText;
    int marginRight = font.width("_");
    if (active && isFocused() && (System.currentTimeMillis() / 300L) % 2L == 0L) {
      marginRight = 0;
      text += "_";
    }

    extractor.text(
        font,
        text,
        right - font.width(text) - marginRight - 3,
        bottom - font.lineHeight - 3,
        getDisplayTextColor(),
        false);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
    if (!active || !visible) {
      return false;
    }

    boolean result = isMouseOver(event.x(), event.y());
    if (result && !isFocused()) {
      setFocused(true);
    } else if (!result && isFocused()) {
      setFocused(false);
    }
    return result;
  }

  @Override
  public boolean keyPressed(KeyEvent input) {
    if (!active || !visible || !isFocused()) {
      return false;
    }

    int keyCode = input.key();
    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
      changeInputValue(inputValue / 10);
      return true;
    }
    if (keyCode == GLFW.GLFW_KEY_DELETE) {
      changeInputValue(0);
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(CharacterEvent charInput) {
    if (!active || !visible || !isFocused()) {
      return false;
    }

    char character = (char) charInput.codepoint();
    if (!Character.isDigit(character) || (character == '0' && inputValue == 0)) {
      return false;
    }

    changeInputValue(
        adjustExperienceValue((inputValue * 10) + Character.getNumericValue(character)));
    return true;
  }

  @Override
  protected MutableComponent createNarrationMessage() {
    return Component.translatable("narration.experiencebottler.experience_input_field");
  }

  @Override
  public void updateWidgetNarration(NarrationElementOutput builder) {
    builder.add(NarratedElementType.TITLE, createNarrationMessage());
    if (!active) {
      return;
    }

    Component type = Component.translatable(getExperienceType().getNarrationKey());
    builder.add(
        NarratedElementType.HINT,
        Component.translatable(
            "narration.experiencebottler.experience_input_field.info.value", inputValue, type));
  }

  @Override
  public void setFocused(boolean focused) {
    boolean wasFocused = isFocused();
    super.setFocused(focused);
    if (focused && !wasFocused) {
      changeInputValue(0);
      moveCursorToEnd(false);
    } else if (!focused && wasFocused) {
      onExperienceChanged();
    }
  }
}
