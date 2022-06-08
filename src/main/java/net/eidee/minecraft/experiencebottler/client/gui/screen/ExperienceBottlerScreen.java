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

package net.eidee.minecraft.experiencebottler.client.gui.screen;

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.identifier;

import com.mojang.blaze3d.systems.RenderSystem;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceInputField;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceType;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceTypeToggleButton;
import net.eidee.minecraft.experiencebottler.network.packet.BottlingExperiencePacket;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;

// HACK: The process flow is not elegant and I want to improve it.
/** The screen of the Experience Bottler block. */
@Environment(EnvType.CLIENT)
public class ExperienceBottlerScreen extends HandledScreen<ExperienceBottlerScreenHandler>
    implements ScreenHandlerListener, ExperienceInputField.Listener {
  private static final Identifier BACKGROUND;

  static {
    BACKGROUND = identifier("textures/gui/container/experience_bottler.png");
  }

  private final PlayerEntity player;

  private ExperienceInputField bottlingValue;
  private ExperienceInputField afterBottling;
  private ExperienceType playerExperienceLabelType;
  private boolean isNeedExperienceUpdate;
  private int lastPlayerExperience;
  private int lastSendExperience;
  private ExperienceInputField lastActiveInputField;

  public ExperienceBottlerScreen(
      ExperienceBottlerScreenHandler handler, PlayerInventory inventory, Text text) {
    super(handler, inventory, text);
    player = inventory.player;
    backgroundWidth = 200;
    backgroundHeight = 210;
    playerInventoryTitleX = 20;
    playerInventoryTitleY = backgroundHeight - 94;
  }

  private void buttonPressed(ExperienceTypeToggleButton button) {
    if (button.getId() == 0) {
      playerExperienceLabelType = playerExperienceLabelType.rotate();
    } else if (button.getId() == 1) {
      bottlingValue.setExperienceType(bottlingValue.getExperienceType().rotate());
    } else if (button.getId() == 2) {
      afterBottling.setExperienceType(afterBottling.getExperienceType().rotate());
    }
  }

  private void adjustValueOfInactiveInput() {
    ExperienceInputField active;
    ExperienceInputField inactive;
    if (!bottlingValue.isFocused() && !afterBottling.isFocused()) {
      if (lastActiveInputField == null) {
        return;
      }
      active = lastActiveInputField;
      inactive = active == bottlingValue ? afterBottling : bottlingValue;
    } else {
      active = bottlingValue.isFocused() ? bottlingValue : afterBottling;
      inactive = bottlingValue.isFocused() ? afterBottling : bottlingValue;
    }

    String activeValue = active.getValueAs(ExperienceType.POINT);
    if (!activeValue.isEmpty()) {
      int activeExperience = NumberUtils.toInt(activeValue);
      int playerExperience = ExperienceUtil.getTotalExperience(player);
      String newValue = Integer.toString(playerExperience - activeExperience);
      inactive.setValueAs(newValue, ExperienceType.POINT);
    } else if (!inactive.isEmpty()) {
      inactive.setValue("");
    }
  }

  private void sendBottlingExperience() {
    ExperienceInputField active = null;
    if (!bottlingValue.isFocused() && !afterBottling.isFocused()) {
      if (lastActiveInputField != null) {
        active = lastActiveInputField;
      }
    } else {
      active = bottlingValue.isFocused() ? bottlingValue : afterBottling;
    }

    int experience = 0;
    if (active != null) {
      String value = active.getValueAs(ExperienceType.POINT);
      if (!value.isEmpty()) {
        if (active == afterBottling) {
          int playerExperience = ExperienceUtil.getTotalExperience(player);
          int afterExperience = NumberUtils.toInt(value);
          value = Integer.toString(playerExperience - afterExperience);
        }
        experience = NumberUtils.toInt(value);
      }
    }

    if (experience != lastSendExperience) {
      lastSendExperience = experience;
      getScreenHandler().setBottlingExperience(experience);
      BottlingExperiencePacket.send(experience);
    }
  }

  @Override
  protected void init() {
    super.init();

    titleX = (backgroundWidth - textRenderer.getWidth(getTitle())) / 2;
    playerExperienceLabelType = ExperienceType.POINT;
    lastPlayerExperience = ExperienceUtil.getTotalExperience(player);
    isNeedExperienceUpdate = false;

    bottlingValue = addDrawableChild(new ExperienceInputField(textRenderer, x + 67, y + 63, this));
    afterBottling = addDrawableChild(new ExperienceInputField(textRenderer, x + 67, y + 95, this));

    for (int i = 0; i < 3; i++) {
      int x = this.x + 163;
      int y = this.y + 31 + (i * 32);
      addDrawableChild(new ExperienceTypeToggleButton(i, x, y, this::buttonPressed));
    }

    setInitialFocus(bottlingValue);

    getScreenHandler().addListener(this);
  }

  @Override
  protected void handledScreenTick() {
    bottlingValue.tick();
    afterBottling.tick();

    isNeedExperienceUpdate =
        isNeedExperienceUpdate || lastPlayerExperience != ExperienceUtil.getTotalExperience(player);
    lastPlayerExperience = ExperienceUtil.getTotalExperience(player);

    if (isNeedExperienceUpdate) {
      isNeedExperienceUpdate = false;
      adjustValueOfInactiveInput();
      sendBottlingExperience();
    }
  }

  @Override
  public void removed() {
    super.removed();
    getScreenHandler().removeListener(this);
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    if (isNeedExperienceUpdate) {
      adjustValueOfInactiveInput();
    }

    renderBackground(matrices);
    super.render(matrices, mouseX, mouseY, delta);
    drawMouseoverTooltip(matrices, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);

    drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
  }

  @Override
  protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    super.drawForeground(matrices, mouseX, mouseY);

    Text label = Text.translatable("gui.experiencebottler.label.player_experience");
    int labelX = 68;
    int labelY = 20;
    textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    label = Text.translatable("gui.experiencebottler.label.bottling_experience");
    labelY += 32;
    textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    label = Text.translatable("gui.experiencebottler.label.after_experience");
    labelY += 32;
    textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    String experience;
    if (playerExperienceLabelType.isPoint()) {
      experience = Integer.toString(ExperienceUtil.getTotalExperience(player));
    } else {
      experience = Integer.toString(player.experienceLevel);
    }
    textRenderer.draw(
        matrices,
        experience,
        67 + 90 - textRenderer.getWidth(experience) - textRenderer.getWidth("_") - 3,
        31 + 18 - textRenderer.fontHeight - 3,
        0x404040);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    super.mouseClicked(mouseX, mouseY, button);
    if (children().stream().noneMatch(x -> x.isMouseOver(mouseX, mouseY))) {
      bottlingValue.setFocused(false);
      afterBottling.setFocused(false);
    }
    return true;
  }

  /* NOTE: When an input(ExperienceInputField) has the focus but cannot be retrieved by getFocused(), the input functionality does not work properly, so we override all the content of the parent class. */
  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE && shouldCloseOnEsc()) {
      close();
      return true;
    }

    Element focused = getFocused();
    if (focused != null && focused.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    } else if ((bottlingValue.isFocused() && bottlingValue.keyPressed(keyCode, scanCode, modifiers))
        || (afterBottling.isFocused() && afterBottling.keyPressed(keyCode, scanCode, modifiers))) {
      return true;
    }

    if (client != null && client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
      close();
      return true;
    }

    handleHotbarKeyPressed(keyCode, scanCode);

    if (focusedSlot != null && focusedSlot.hasStack()) {
      if (client.options.pickItemKey.matchesKey(keyCode, scanCode)) {
        onMouseClick(focusedSlot, focusedSlot.id, 0, SlotActionType.CLONE);
      } else if (client.options.dropKey.matchesKey(keyCode, scanCode)) {
        onMouseClick(
            focusedSlot,
            focusedSlot.id,
            HandledScreen.hasControlDown() ? 1 : 0,
            SlotActionType.THROW);
      }
    }
    return true;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    return (bottlingValue.isFocused() && bottlingValue.charTyped(chr, modifiers))
        || (afterBottling.isFocused() && afterBottling.charTyped(chr, modifiers));
  }

  @Override
  public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    if (slotId == 0 || slotId == 1) {
      if (!stack.isEmpty()) {
        if (!bottlingValue.isFocused() && !afterBottling.isFocused()) {
          bottlingValue.setFocused(bottlingValue.isEmpty());
          return;
        }
      }
      isNeedExperienceUpdate = true;
    }
  }

  @Override
  public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

  @Override
  public void onValueChanged(ExperienceInputField inputField, String value) {
    isNeedExperienceUpdate = true;
  }

  @Override
  public void onFocusChanged(ExperienceInputField inputField, boolean focused) {
    if (focused) {
      lastActiveInputField = inputField;
      if (inputField == bottlingValue) {
        afterBottling.setFocused(false);
      } else {
        bottlingValue.setFocused(false);
      }
    }
  }
}
