package net.eidee.minecraft.experiencebottler.component;

import net.eidee.minecraft.experiencebottler.component.type.BottledExperienceComponent;
import net.minecraft.core.component.DataComponentType;

public class DataComponentTypes {
  public static final DataComponentType<BottledExperienceComponent> BOTTLED_EXPERIENCE;

  static {
    BOTTLED_EXPERIENCE =
        DataComponentType.<BottledExperienceComponent>builder()
            .persistent(BottledExperienceComponent.CODEC)
            .networkSynchronized(BottledExperienceComponent.STREAM_CODEC)
            .build();
  }
}
