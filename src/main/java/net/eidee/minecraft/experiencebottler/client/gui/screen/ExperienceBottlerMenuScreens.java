package net.eidee.minecraft.experiencebottler.client.gui.screen;

import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;

@Environment(EnvType.CLIENT)
public final class ExperienceBottlerMenuScreens {
  private ExperienceBottlerMenuScreens() {}

  public static void register(MenuType<ExperienceBottlerScreenHandler> menuType) {
    MenuScreens.register(menuType, ExperienceBottlerScreen::new);
  }
}
