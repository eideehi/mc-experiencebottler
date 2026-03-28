package net.eidee.minecraft.experiencebottler.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.eidee.minecraft.experiencebottler.component.DataComponentTypes;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record BottledExperienceComponent(int experience) implements TooltipProvider {
  public static final BottledExperienceComponent DEFAULT = new BottledExperienceComponent(0);
  public static final Codec<BottledExperienceComponent> CODEC =
      ExtraCodecs.NON_NEGATIVE_INT.xmap(
          BottledExperienceComponent::new, BottledExperienceComponent::experience);
  public static final StreamCodec<ByteBuf, BottledExperienceComponent> STREAM_CODEC =
      ByteBufCodecs.VAR_INT.map(BottledExperienceComponent::new, BottledExperienceComponent::experience);

  /**
   * Get the experience value set in the item stack. If no experience value is set, it returns 0.
   */
  public static int getExperienceValue(ItemStack stack) {
    BottledExperienceComponent component =
        stack.getComponents().getOrDefault(DataComponentTypes.BOTTLED_EXPERIENCE, DEFAULT);
    return component.experience();
  }

  /** Set the experience value in item stack. */
  public static void setExperienceValue(ItemStack stack, int value) {
    stack.set(DataComponentTypes.BOTTLED_EXPERIENCE, new BottledExperienceComponent(value));
  }

  private void doAppendTooltip(Consumer<Component> textConsumer, Player player) {
    if (experience < 0) return;
    Component arg = Component.literal(String.format("%,d", experience));
    textConsumer.accept(
        Component.translatable("item.experiencebottler.bottled_experience.tooltip.0", arg));

    if (player != null) {
      long playerExperience = ExperienceUtil.getTotalExperience(player);
      int level = ExperienceUtil.getLevelFromTotalExperience(experience + playerExperience);
      arg = Component.literal(String.format("%,d", level));
      textConsumer.accept(
          Component.translatable("item.experiencebottler.bottled_experience.tooltip.1", arg));
    }
  }

  @Override
  public void addToTooltip(
      Item.TooltipContext context,
      Consumer<Component> textConsumer,
      TooltipFlag type,
      DataComponentGetter components) {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      appendTooltipClient(textConsumer);
    } else {
      doAppendTooltip(textConsumer, null);
    }
  }

  @Environment(EnvType.CLIENT)
  private void appendTooltipClient(Consumer<Component> textConsumer) {
    doAppendTooltip(textConsumer, Minecraft.getInstance().player);
  }
}
