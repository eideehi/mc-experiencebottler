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

package net.eidee.minecraft.experiencebottler.client.gui.screen;

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.identifier;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceInput;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceType;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceTypeToggleButton;
import net.eidee.minecraft.experiencebottler.item.BottledExperienceItem;
import net.eidee.minecraft.experiencebottler.item.Items;
import net.eidee.minecraft.experiencebottler.network.packet.BottlingExperiencePacket;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext.Default;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class ExperienceBottlerScreen extends HandledScreen<ExperienceBottlerScreenHandler>
    implements ScreenHandlerListener {
  private static final Identifier BACKGROUND;

  static {
    BACKGROUND = identifier("textures/gui/container/experience_bottler.png");
  }

  private Text sourceExperienceLabel;
  private ExperienceInput sourceExperience;
  private Text experienceValueToBottleLabel;
  private ExperienceInput experienceValueToBottle;
  private Text afterBottlingExperienceLabel;
  private ExperienceInput afterBottlingExperience;
  private long lastSendExperience = -1;

  public ExperienceBottlerScreen(
      ExperienceBottlerScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
    backgroundWidth = 200;
    backgroundHeight = 210;
    playerInventoryTitleX = 20;
    playerInventoryTitleY = backgroundHeight - 94;
  }

  private static Consumer<ExperienceTypeToggleButton> onExperienceTypeChanged(
      Consumer<ExperienceType> consumer) {
    return button -> consumer.accept(button.getExperienceType());
  }

  private void onInputValueChange(ExperienceInput input) {
    if (input == sourceExperience) {
      if (afterBottlingExperience.isFocused()) {
        return;
      }
      long source = input.getExperiencePoint();
      long bottled = experienceValueToBottle.getExperiencePoint();
      afterBottlingExperience.setExperiencePoint(source - bottled);
    } else if (input == experienceValueToBottle) {
      if (input.isFocused()) {
        long source = sourceExperience.getExperiencePoint();
        long bottled = input.getExperiencePoint();
        afterBottlingExperience.setExperiencePoint(source - bottled);
      }

      int experience =
          (int) Math.min(experienceValueToBottle.getExperiencePoint(), Integer.MAX_VALUE);
      if (experience != lastSendExperience) {
        lastSendExperience = experience;
        getScreenHandler().setBottlingExperience(experience);
        BottlingExperiencePacket.send(experience);
      }
    } else if (input == afterBottlingExperience) {
      if (!input.isFocused()) {
        return;
      }
      long source = sourceExperience.getExperiencePoint();
      long after = input.getExperiencePoint();
      experienceValueToBottle.setExperiencePoint(Math.min(source - after, Integer.MAX_VALUE));
    }
  }

  @Override
  protected void init() {
    super.init();

    sourceExperienceLabel = Text.empty();
    sourceExperience =
        addDrawableChild(
            new ExperienceInput(textRenderer, x + 67, y + 31, this::onInputValueChange));
    sourceExperience.active = false;
    sourceExperience.setDisabledTextColor(0x404040);
    addDrawableChild(
        new ExperienceTypeToggleButton(
            0, x + 163, y + 31, onExperienceTypeChanged(sourceExperience::setExperienceType)));

    experienceValueToBottleLabel =
        Text.translatable("gui.experiencebottler.label.bottling_experience");
    experienceValueToBottle =
        addDrawableChild(
            new ExperienceInput(textRenderer, x + 67, y + 63, this::onInputValueChange));
    addDrawableChild(
        new ExperienceTypeToggleButton(
            0,
            x + 163,
            y + 63,
            onExperienceTypeChanged(experienceValueToBottle::setExperienceType)));

    afterBottlingExperienceLabel =
        Text.translatable("gui.experiencebottler.label.after_experience");
    afterBottlingExperience =
        addDrawableChild(
            new ExperienceInput(textRenderer, x + 67, y + 95, this::onInputValueChange));
    addDrawableChild(
        new ExperienceTypeToggleButton(
            0,
            x + 163,
            y + 95,
            onExperienceTypeChanged(afterBottlingExperience::setExperienceType)));

    getScreenHandler().addListener(this);
  }

  @Override
  public void removed() {
    super.removed();
    getScreenHandler().removeListener(this);
  }

  @Override
  public void setFocused(@Nullable Element focused) {
    super.setFocused(focused);
    if (experienceValueToBottle == null || afterBottlingExperience == null) {
      return;
    }
    if (focused == experienceValueToBottle) {
      if (afterBottlingExperience.isFocused()) {
        afterBottlingExperience.changeFocus(false);
      }
    } else if (focused == afterBottlingExperience) {
      if (experienceValueToBottle.isFocused()) {
        experienceValueToBottle.changeFocus(false);
      }
    }
  }

  @Override
  protected void handledScreenTick() {
    experienceValueToBottle.tick();
    afterBottlingExperience.tick();
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    super.render(matrices, mouseX, mouseY, delta);
    drawMouseoverTooltip(matrices, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    renderBackground(matrices);

    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);

    drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
  }

  @Override
  protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    super.drawForeground(matrices, mouseX, mouseY);

    final int labelX = 68;
    int labelY = 20;
    textRenderer.draw(matrices, sourceExperienceLabel, labelX, labelY, 0x404040);

    labelY += 32;
    textRenderer.draw(matrices, experienceValueToBottleLabel, labelX, labelY, 0x404040);

    labelY += 32;
    textRenderer.draw(matrices, afterBottlingExperienceLabel, labelX, labelY, 0x404040);
  }

  @Override
  protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
    if (getScreenHandler().getCursorStack().isEmpty()
        && focusedSlot != null
        && focusedSlot.hasStack()) {
      ItemStack stack = focusedSlot.getStack();
      if (focusedSlot.inventory instanceof PlayerInventory
          || !stack.isOf(Items.BOTTLED_EXPERIENCE)) {
        renderTooltip(matrices, stack, x, y);
      } else if (client != null) {
        ItemStack copy = stack.copy();
        copy.addHideFlag(TooltipSection.ADDITIONAL);
        List<Text> tooltip =
            copy.getTooltip(
                client.player,
                client.options.advancedItemTooltips ? Default.ADVANCED : Default.BASIC);
        tooltip.addAll(BottledExperienceItem.getAppendTooltip(stack, null));
        renderTooltip(matrices, tooltip, x, y);
      }
    }
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ENTER) {
      if (experienceValueToBottle.isFocused()) {
        experienceValueToBottle.changeFocus(false);
        setFocused(null);
        return true;
      } else if (afterBottlingExperience.isFocused()) {
        afterBottlingExperience.changeFocus(false);
        setFocused(null);
        return true;
      }
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    if (slotId == 0 || slotId == 1) {
      if (stack.isEmpty()) {
        return;
      }
      if (!experienceValueToBottle.isFocused() && !afterBottlingExperience.isFocused()) {
        if (experienceValueToBottle.getValue() == 0 && experienceValueToBottle.changeFocus(false)) {
          setFocused(experienceValueToBottle);
        }
      }
    } else if (slotId == 2) {
      sourceExperienceLabel =
          Text.translatable(
              "gui.experiencebottler.label.source_experience", getScreenHandler().getSourceName());
      sourceExperience.setExperiencePoint(getScreenHandler().getSourceExperience());
    }
  }

  @Override
  public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}
}
