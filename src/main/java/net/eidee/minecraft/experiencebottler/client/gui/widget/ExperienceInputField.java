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

package net.eidee.minecraft.experiencebottler.client.gui.widget;

import java.util.regex.Pattern;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;

/** A widget that allows the user to enter an experience value. */
@Environment(EnvType.CLIENT)
public class ExperienceInputField extends ClickableWidget {
  private static final Pattern NUMBER = Pattern.compile("^-?\\d+$");

  private final TextRenderer textRenderer;
  private final Listener listener;

  private ExperienceType experienceType;
  private String value = "";
  private int frame = 0;

  public ExperienceInputField(TextRenderer textRenderer, int x, int y, Listener listener) {
    super(x, y, 90, 18, ScreenTexts.EMPTY);
    this.textRenderer = textRenderer;
    this.listener = listener;
    experienceType = ExperienceType.POINT;
  }

  private static String convertValue(String value, ExperienceType experienceType) {
    if (value.isEmpty()) {
      return "";
    }
    boolean isMinus = value.startsWith("-");
    if (experienceType == ExperienceType.POINT) {
      int valueAsLevel = NumberUtils.toInt(value.substring(isMinus ? 1 : 0));
      int point = ExperienceUtil.getTotalExperienceToReachLevel(valueAsLevel, 0);
      String v = Integer.toString(point);
      return isMinus ? "-" + v : v;
    } else {
      int valueAsPoint = NumberUtils.toInt(value.substring(isMinus ? 1 : 0));
      int level = 0;
      while (valueAsPoint > 0) {
        int next = ExperienceUtil.getNextLevelExperience(level);
        if (valueAsPoint < next) {
          break;
        }
        valueAsPoint -= next;
        level++;
      }
      String v = Integer.toString(level);
      return isMinus ? "-" + v : v;
    }
  }

  private int getTextColor() {
    if (getValue().startsWith("-")) {
      return 0xE09090;
    } else {
      return isFocused() ? 0xFFFFFF : 0xA0A0A0;
    }
  }

  public ExperienceType getExperienceType() {
    return experienceType;
  }

  public void setExperienceType(ExperienceType experienceType) {
    if (this.experienceType != experienceType) {
      this.experienceType = experienceType;
      setValue(convertValue(value, experienceType));
    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    if (this.value.equals(value)) {
      return;
    }

    String newValue = value;
    if (NUMBER.matcher(newValue).matches()) {
      if (getExperienceType().isPoint()) {
        long point = NumberUtils.toLong(newValue);
        if (point > Integer.MAX_VALUE) {
          newValue = Integer.toString(Integer.MAX_VALUE);
        } else {
          newValue = Long.toString(point);
        }
      } else {
        int level = NumberUtils.toInt(newValue);
        if (level > 21863) {
          newValue = Integer.toString(21863);
        } else {
          newValue = Integer.toString(level);
        }
      }
    } else {
      newValue = "";
    }

    if (!this.value.equals(newValue)) {
      this.value = newValue;
      listener.onValueChanged(this, newValue);
    }
  }

  public boolean isEmpty() {
    return value.isEmpty();
  }

  public String getValueAs(ExperienceType experienceType) {
    return getExperienceType() == experienceType
        ? getValue()
        : convertValue(getValue(), experienceType);
  }

  public void setValueAs(String value, ExperienceType experienceType) {
    setValue(
        getExperienceType() == experienceType ? value : convertValue(value, getExperienceType()));
  }

  public void tick() {
    frame++;
  }

  @Override
  public void setFocused(boolean focused) {
    super.setFocused(focused);
    listener.onFocusChanged(this, focused);
  }

  @Override
  public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    String text = getValue();
    int marginX = 0;
    if (active && visible && isFocused()) {
      if (frame / 6 % 2 == 0) {
        text += "_";
      } else {
        marginX = textRenderer.getWidth("_");
      }
    } else {
      marginX = textRenderer.getWidth("_");
    }

    textRenderer.draw(
        matrices,
        text,
        x + width - textRenderer.getWidth(text) - marginX - 3,
        y + height - textRenderer.fontHeight - 3,
        getTextColor());
  }

  @Override
  protected void onFocusedChanged(boolean newFocused) {
    if (newFocused) {
      frame = 0;
      setValue("");
    }
    listener.onFocusChanged(this, newFocused);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (active && visible) {
      if (isValidClickButton(button)) {
        boolean flag = clicked(mouseX, mouseY);
        if (flag) {
          changeFocus(false);
          onClick(mouseX, mouseY);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (active && visible && isFocused()) {
      if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
        String value = getValue();
        int length = value.length();
        setValue(length > 0 ? value.substring(0, length - 1) : "");
        return true;
      } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
        setValue("");
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    if (active && visible && isFocused()) {
      String value = getValue();
      if (Character.isDigit(chr)) {
        if (chr == '0' && value.startsWith("0")) {
          return false;
        }
        setValue(value + chr);
        return true;
      }
    }
    return false;
  }

  @Override
  protected MutableText getNarrationMessage() {
    return Text.translatable("narration.experiencebottler.experience_input_field");
  }

  @Override
  public void appendNarrations(NarrationMessageBuilder builder) {
    builder.put(NarrationPart.TITLE, getNarrationMessage());
    if (active) {
      int experience = NumberUtils.toInt(getValue());
      Text type = Text.translatable(getExperienceType().getNarrationKey());
      builder.put(
          NarrationPart.HINT,
          Text.translatable(
              "narration.experiencebottler.experience_input_field.info.value", experience, type));
    }
  }

  /** ExpInputField's event listener. */
  public interface Listener {
    void onValueChanged(ExperienceInputField inputField, String value);

    void onFocusChanged(ExperienceInputField inputField, boolean focused);
  }
}
