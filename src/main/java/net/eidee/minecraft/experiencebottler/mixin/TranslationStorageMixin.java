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

package net.eidee.minecraft.experiencebottler.mixin;

import static net.eidee.minecraft.experiencebottler.ExperienceBottler.MOD_ID;
import static net.eidee.minecraft.experiencebottler.ExperienceBottler.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.eidee.minecraft.experiencebottler.core.i18n.CrowdinTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/** Mixin for {@link TranslationStorage} to add translations from Crowdin. */
@Environment(EnvType.CLIENT)
@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
  @Inject(
      method =
          "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private static void loadMixin(
      ResourceManager resourceManager,
      List<LanguageDefinition> definitions,
      CallbackInfoReturnable<TranslationStorage> cir,
      Map<String, String> map) {
    getLogger().info("Starting Crowdin translation loading...");

    CrowdinTranslations translations =
        CrowdinTranslations.newInstance(MOD_ID, "eideehi-minecraft-mods");

    for (LanguageDefinition definition : definitions) {
      String language = definition.getCode();
      if (language.equals("en_us")) {
        continue;
      }

      try (InputStream stream = translations.getAsStream(language)) {
        if (stream != null) {
          Language.load(stream, map::put);
        }
      } catch (IOException e) {
        getLogger().error("Failed to load translations for language " + language, e);
      }
    }

    getLogger().info("Finished Crowdin translation loading.");
  }
}
