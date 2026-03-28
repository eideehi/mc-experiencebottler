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

package net.eidee.minecraft.experiencebottler.client.gui.screen;

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.identifier;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.experiencebottler.annotation.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceInput;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceType;
import net.eidee.minecraft.experiencebottler.client.gui.widget.ExperienceTypeToggleButton;
import net.eidee.minecraft.experiencebottler.network.packet.BottlingExperiencePacket;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class ExperienceBottlerScreen extends AbstractContainerScreen<ExperienceBottlerScreenHandler>
    implements ContainerListener {
  private static final Identifier BACKGROUND =
      identifier("textures/gui/container/experience_bottler.png");
  private static final Component BOTTLING_EXPERIENCE_LABEL =
      Component.translatable("gui.experiencebottler.label.bottling_experience");
  private static final Component AFTER_BOTTLING_EXPERIENCE_LABEL =
      Component.translatable("gui.experiencebottler.label.after_experience");

  private Component sourceExperienceLabel = Component.empty();
  private ExperienceInput sourceExperience;
  private ExperienceInput experienceValueToBottle;
  private ExperienceInput afterBottlingExperience;
  private long lastSendExperience = -1;
  private ExperienceInput lastFocusedInput;

  public ExperienceBottlerScreen(
      ExperienceBottlerScreenHandler handler, Inventory inventory, Component title) {
    super(handler, inventory, title, 200, 210);
    inventoryLabelX = 20;
    inventoryLabelY = imageHeight - 94;
  }

  private static Consumer<ExperienceTypeToggleButton> onExperienceTypeChanged(
      Consumer<ExperienceType> consumer) {
    return button -> consumer.accept(button.getExperienceType());
  }

  private void syncFocusedInput() {
    if (experienceValueToBottle == null || afterBottlingExperience == null) {
      return;
    }

    if (experienceValueToBottle.isFocused()) {
      lastFocusedInput = experienceValueToBottle;
      if (afterBottlingExperience.isFocused()) {
        afterBottlingExperience.setFocused(false);
      }
    } else if (afterBottlingExperience.isFocused()) {
      lastFocusedInput = afterBottlingExperience;
      if (experienceValueToBottle.isFocused()) {
        experienceValueToBottle.setFocused(false);
      }
    }
  }

  private boolean clearEditableFocus() {
    boolean cleared = false;
    if (experienceValueToBottle != null && experienceValueToBottle.isFocused()) {
      experienceValueToBottle.setFocused(false);
      cleared = true;
    }
    if (afterBottlingExperience != null && afterBottlingExperience.isFocused()) {
      afterBottlingExperience.setFocused(false);
      cleared = true;
    }
    if (cleared) {
      setFocused(null);
    }
    return cleared;
  }

  private void refreshSourceState() {
    sourceExperienceLabel =
        Component.translatable(
            "gui.experiencebottler.label.source_experience", menu.getSourceName());
    sourceExperience.setExperiencePoint(menu.getSourceExperience());
    onInputValueChange(sourceExperience);
  }

  private void onInputValueChange(ExperienceInput input) {
    if (input == sourceExperience) {
      long source = input.getExperiencePoint();
      if (experienceValueToBottle.isFocused()) {
        afterBottlingExperience.setExperiencePoint(
            source - experienceValueToBottle.getExperiencePoint());
      } else if (afterBottlingExperience.isFocused()) {
        experienceValueToBottle.setExperiencePoint(
            source - afterBottlingExperience.getExperiencePoint());
      } else if (lastFocusedInput == null || lastFocusedInput == experienceValueToBottle) {
        afterBottlingExperience.setExperiencePoint(
            source - experienceValueToBottle.getExperiencePoint());
      } else {
        experienceValueToBottle.setExperiencePoint(
            source - afterBottlingExperience.getExperiencePoint());
      }
    } else if (input == experienceValueToBottle) {
      if (input.isFocused()) {
        long source = sourceExperience.getExperiencePoint();
        long bottled = input.getExperiencePoint();
        afterBottlingExperience.setExperiencePoint(source - bottled);
      }
    } else if (input == afterBottlingExperience && input.isFocused()) {
      long source = sourceExperience.getExperiencePoint();
      long after = input.getExperiencePoint();
      experienceValueToBottle.setExperiencePoint(Math.min(source - after, Integer.MAX_VALUE));
    }

    int experience =
        (int) Math.min(experienceValueToBottle.getExperiencePoint(), Integer.MAX_VALUE);
    if (experience != lastSendExperience) {
      lastSendExperience = experience;
      menu.setBottlingExperience(experience);
      ClientPlayNetworking.send(new BottlingExperiencePacket(experience));
    }
  }

  @Override
  protected void init() {
    super.init();

    sourceExperience =
        addRenderableWidget(
            new ExperienceInput(font, leftPos + 67, topPos + 31, this::onInputValueChange));
    sourceExperience.active = false;
    sourceExperience.setEditable(false);
    sourceExperience.setDisabledTextColor(0xFF404040);
    addRenderableWidget(
        new ExperienceTypeToggleButton(
            leftPos + 163, topPos + 31, onExperienceTypeChanged(sourceExperience::setExperienceType)));

    experienceValueToBottle =
        addRenderableWidget(
            new ExperienceInput(font, leftPos + 67, topPos + 63, this::onInputValueChange));
    addRenderableWidget(
        new ExperienceTypeToggleButton(
            leftPos + 163,
            topPos + 63,
            onExperienceTypeChanged(experienceValueToBottle::setExperienceType)));

    afterBottlingExperience =
        addRenderableWidget(
            new ExperienceInput(font, leftPos + 67, topPos + 95, this::onInputValueChange));
    addRenderableWidget(
        new ExperienceTypeToggleButton(
            leftPos + 163,
            topPos + 95,
            onExperienceTypeChanged(afterBottlingExperience::setExperienceType)));

    titleLabelX = 20;
    menu.addSlotListener(this);
    refreshSourceState();
  }

  @Override
  public void removed() {
    menu.removeSlotListener(this);
    super.removed();
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
    super.extractBackground(extractor, mouseX, mouseY, delta);
    extractor.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND,
        leftPos,
        topPos,
        0.0F,
        0.0F,
        imageWidth,
        imageHeight,
        256,
        256);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
    super.extractLabels(extractor, mouseX, mouseY);

    int labelX = 68;
    int labelY = 20;
    extractor.text(font, sourceExperienceLabel, labelX, labelY, 0xFF404040, false);

    labelY += 32;
    extractor.text(font, BOTTLING_EXPERIENCE_LABEL, labelX, labelY, 0xFF404040, false);

    labelY += 32;
    extractor.text(font, AFTER_BOTTLING_EXPERIENCE_LABEL, labelX, labelY, 0xFF404040, false);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
    boolean result = super.mouseClicked(event, doubled);
    if (!experienceValueToBottle.isMouseOver(event.x(), event.y())
        && !afterBottlingExperience.isMouseOver(event.x(), event.y())) {
      clearEditableFocus();
    }
    syncFocusedInput();
    return result;
  }

  @Override
  public boolean keyPressed(KeyEvent input) {
    int keyCode = input.key();
    boolean tryInputDeactivate =
        keyCode == GLFW.GLFW_KEY_ENTER
            || keyCode == GLFW.GLFW_KEY_KP_ENTER
            || keyCode == GLFW.GLFW_KEY_ESCAPE
            || minecraft.options.keyInventory.matches(input);
    if (tryInputDeactivate && clearEditableFocus()) {
      return true;
    }

    boolean result = super.keyPressed(input);
    syncFocusedInput();
    return result;
  }

  @Override
  public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
    if (slotId == 0 || slotId == 1) {
      if (stack.isEmpty()) {
        return;
      }
      if (!experienceValueToBottle.isFocused()
          && !afterBottlingExperience.isFocused()
          && experienceValueToBottle.getExperiencePoint() == 0) {
        experienceValueToBottle.setFocused(true);
        setFocused(experienceValueToBottle);
        syncFocusedInput();
      }
    } else if (slotId == 2) {
      refreshSourceState();
    }
  }

  @Override
  public void dataChanged(AbstractContainerMenu handler, int property, int value) {}
}
