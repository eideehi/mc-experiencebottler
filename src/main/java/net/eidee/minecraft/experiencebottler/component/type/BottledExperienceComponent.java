package net.eidee.minecraft.experiencebottler.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.eidee.minecraft.experiencebottler.component.DataComponentTypes;
import net.eidee.minecraft.experiencebottler.util.ExperienceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

public record BottledExperienceComponent(int experience) implements TooltipAppender {
  public static final BottledExperienceComponent DEFAULT = new BottledExperienceComponent(0);
  public static final Codec<BottledExperienceComponent> CODEC =
      Codecs.NON_NEGATIVE_INT.xmap(
          BottledExperienceComponent::new, BottledExperienceComponent::experience);
  public static final PacketCodec<ByteBuf, BottledExperienceComponent> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.INTEGER,
          BottledExperienceComponent::experience,
          BottledExperienceComponent::new);

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

  private void doAppendTooltip(Consumer<Text> textConsumer, PlayerEntity player) {
    if (experience < 0) return;
    Text arg = Text.literal(String.format("%,d", experience));
    textConsumer.accept(
        Text.translatable("item.experiencebottler.bottled_experience.tooltip.0", arg));

    if (player != null) {
      long playerExperience = ExperienceUtil.getTotalExperience(player);
      int level = ExperienceUtil.getLevelFromTotalExperience(experience + playerExperience);
      arg = Text.literal(String.format("%,d", level));
      textConsumer.accept(
          Text.translatable("item.experiencebottler.bottled_experience.tooltip.1", arg));
    }
  }

  @Override
  public void appendTooltip(
      Item.TooltipContext context,
      Consumer<Text> textConsumer,
      TooltipType type,
      ComponentsAccess components) {
    // FIXME: If there is a better side detection method, it will be modified.
    String threadName = Thread.currentThread().getName();
    if (threadName.equals("Render thread")) {
      appendTooltipClient(textConsumer);
    } else {
      doAppendTooltip(textConsumer, null);
    }
  }

  @Environment(EnvType.CLIENT)
  private void appendTooltipClient(Consumer<Text> textConsumer) {
    doAppendTooltip(textConsumer, MinecraftClient.getInstance().player);
  }
}
