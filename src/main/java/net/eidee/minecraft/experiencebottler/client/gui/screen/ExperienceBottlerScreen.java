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

package net.eidee.minecraft.experiencebottler.client.gui.screen;

import static net.eidee.minecraft.experiencebottler.ExperienceBottler.identifier;

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
import net.minecraft.text.TranslatableText;
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
    this.player = inventory.player;
    this.backgroundWidth = 200;
    this.backgroundHeight = 210;
    this.playerInventoryTitleX = 20;
    this.playerInventoryTitleY = this.backgroundHeight - 94;
  }

  private void buttonPressed(ExperienceTypeToggleButton button) {
    if (button.getId() == 0) {
      this.playerExperienceLabelType = this.playerExperienceLabelType.rotate();
    } else if (button.getId() == 1) {
      this.bottlingValue.setExperienceType(this.bottlingValue.getExperienceType().rotate());
    } else if (button.getId() == 2) {
      this.afterBottling.setExperienceType(this.afterBottling.getExperienceType().rotate());
    }
  }

  private void adjustValueOfInactiveInput() {
    ExperienceInputField active;
    ExperienceInputField inactive;
    if (!this.bottlingValue.isFocused() && !this.afterBottling.isFocused()) {
      if (this.lastActiveInputField == null) {
        return;
      }
      active = this.lastActiveInputField;
      inactive = active == this.bottlingValue ? this.afterBottling : this.bottlingValue;
    } else {
      active = this.bottlingValue.isFocused() ? this.bottlingValue : this.afterBottling;
      inactive = this.bottlingValue.isFocused() ? this.afterBottling : this.bottlingValue;
    }

    String activeValue = active.getValueAs(ExperienceType.POINT);
    if (!activeValue.isEmpty()) {
      int activeExperience = NumberUtils.toInt(activeValue);
      int playerExperience = ExperienceUtil.getTotalExperience(this.player);
      String newValue = Integer.toString(playerExperience - activeExperience);
      inactive.setValueAs(newValue, ExperienceType.POINT);
    } else if (!inactive.isEmpty()) {
      inactive.setValue("");
    }
  }

  private void sendBottlingExperience() {
    ExperienceInputField active = null;
    if (!this.bottlingValue.isFocused() && !this.afterBottling.isFocused()) {
      if (this.lastActiveInputField != null) {
        active = this.lastActiveInputField;
      }
    } else {
      active = this.bottlingValue.isFocused() ? this.bottlingValue : this.afterBottling;
    }

    int experience = 0;
    if (active != null) {
      String value = active.getValueAs(ExperienceType.POINT);
      if (!value.isEmpty()) {
        if (active == this.afterBottling) {
          int playerExperience = ExperienceUtil.getTotalExperience(this.player);
          int afterExperience = NumberUtils.toInt(value);
          value = Integer.toString(playerExperience - afterExperience);
        }
        experience = NumberUtils.toInt(value);
      }
    }

    if (experience != this.lastSendExperience) {
      this.lastSendExperience = experience;
      this.getScreenHandler().setBottlingExperience(experience);
      BottlingExperiencePacket.send(experience);
    }
  }

  @Override
  protected void init() {
    super.init();

    this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.getTitle())) / 2;
    this.playerExperienceLabelType = ExperienceType.POINT;
    this.lastPlayerExperience = ExperienceUtil.getTotalExperience(this.player);
    this.isNeedExperienceUpdate = false;

    this.bottlingValue =
        this.addDrawableChild(
            new ExperienceInputField(this.textRenderer, this.x + 67, this.y + 63, this));
    this.afterBottling =
        this.addDrawableChild(
            new ExperienceInputField(this.textRenderer, this.x + 67, this.y + 95, this));

    for (int i = 0; i < 3; i++) {
      int x = this.x + 163;
      int y = this.y + 31 + (i * 32);
      this.addDrawableChild(new ExperienceTypeToggleButton(i, x, y, this::buttonPressed));
    }

    this.setInitialFocus(this.bottlingValue);

    this.getScreenHandler().addListener(this);
  }

  @Override
  protected void handledScreenTick() {
    this.bottlingValue.tick();
    this.afterBottling.tick();

    this.isNeedExperienceUpdate =
        this.isNeedExperienceUpdate
            || this.lastPlayerExperience != ExperienceUtil.getTotalExperience(this.player);
    this.lastPlayerExperience = ExperienceUtil.getTotalExperience(this.player);

    if (this.isNeedExperienceUpdate) {
      this.isNeedExperienceUpdate = false;
      this.adjustValueOfInactiveInput();
      this.sendBottlingExperience();
    }
  }

  @Override
  public void removed() {
    super.removed();
    this.getScreenHandler().removeListener(this);
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    if (this.isNeedExperienceUpdate) {
      this.adjustValueOfInactiveInput();
    }

    this.renderBackground(matrices);
    super.render(matrices, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(matrices, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);

    this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
  }

  @Override
  protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    super.drawForeground(matrices, mouseX, mouseY);

    Text label = new TranslatableText("gui.experiencebottler.label.player_experience");
    int labelX = 68;
    int labelY = 20;
    this.textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    label = new TranslatableText("gui.experiencebottler.label.bottling_experience");
    labelY += 32;
    this.textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    label = new TranslatableText("gui.experiencebottler.label.after_experience");
    labelY += 32;
    this.textRenderer.draw(matrices, label, labelX, labelY, 0x404040);

    String experience;
    if (this.playerExperienceLabelType.isPoint()) {
      experience = Integer.toString(ExperienceUtil.getTotalExperience(this.player));
    } else {
      experience = Integer.toString(this.player.experienceLevel);
    }
    this.textRenderer.draw(
        matrices,
        experience,
        67 + 90 - this.textRenderer.getWidth(experience) - this.textRenderer.getWidth("_") - 3,
        31 + 18 - this.textRenderer.fontHeight - 3,
        0x404040);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    super.mouseClicked(mouseX, mouseY, button);
    if (this.children().stream().noneMatch(x -> x.isMouseOver(mouseX, mouseY))) {
      this.bottlingValue.setFocused(false);
      this.afterBottling.setFocused(false);
    }
    return true;
  }

  /* NOTE: When an input(ExperienceInputField) has the focus but cannot be retrieved by getFocused(), the input functionality does not work properly, so we override all the content of the parent class. */
  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
      this.close();
      return true;
    }

    Element focused = this.getFocused();
    if (focused != null && focused.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    } else if ((this.bottlingValue.isFocused()
            && this.bottlingValue.keyPressed(keyCode, scanCode, modifiers))
        || (this.afterBottling.isFocused()
            && this.afterBottling.keyPressed(keyCode, scanCode, modifiers))) {
      return true;
    }

    if (this.client != null && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
      this.close();
      return true;
    }

    this.handleHotbarKeyPressed(keyCode, scanCode);

    if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
      if (this.client.options.pickItemKey.matchesKey(keyCode, scanCode)) {
        this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.CLONE);
      } else if (this.client.options.dropKey.matchesKey(keyCode, scanCode)) {
        this.onMouseClick(
            this.focusedSlot,
            this.focusedSlot.id,
            HandledScreen.hasControlDown() ? 1 : 0,
            SlotActionType.THROW);
      }
    }
    return true;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    return (this.bottlingValue.isFocused() && this.bottlingValue.charTyped(chr, modifiers))
        || (this.afterBottling.isFocused() && this.afterBottling.charTyped(chr, modifiers));
  }

  @Override
  public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    if (slotId == 0 || slotId == 1) {
      if (!stack.isEmpty()) {
        if (!this.bottlingValue.isFocused() && !this.afterBottling.isFocused()) {
          this.bottlingValue.setFocused(this.bottlingValue.isEmpty());
          return;
        }
      }
      this.isNeedExperienceUpdate = true;
    }
  }

  @Override
  public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

  @Override
  public void onValueChanged(ExperienceInputField inputField, String value) {
    this.isNeedExperienceUpdate = true;
  }

  @Override
  public void onFocusChanged(ExperienceInputField inputField, boolean focused) {
    if (focused) {
      this.lastActiveInputField = inputField;
      if (inputField == this.bottlingValue) {
        this.afterBottling.setFocused(false);
      } else {
        this.bottlingValue.setFocused(false);
      }
    }
  }
}
