package net.eidee.minecraft.experiencebottler.component;

import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.minecraft.component.ComponentType;

public class DataComponentTypes {
  public static final ComponentType<BottledExperienceComponent> BOTTLED_EXPERIENCE;

  static {
    BOTTLED_EXPERIENCE =
        ComponentType.<BottledExperienceComponent>builder()
            .codec(BottledExperienceComponent.CODEC)
            .packetCodec(BottledExperienceComponent.PACKET_CODEC)
            .build();
  }
}
