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

package net.eidee.minecraft.experiencebottler.util;

import java.util.stream.IntStream;
import net.minecraft.entity.player.PlayerEntity;

/** Utility class for experience. */
public class ExperienceUtil {
  private ExperienceUtil() {}

  /** Return the experience required to level up from the level specified in the argument. */
  public static int getNextLevelExperience(int level) {
    if (level >= 30) {
      return 112 + (level - 30) * 9;
    } else {
      return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }
  }

  /** Return the total experience to reach the level of the specified argument. */
  public static int getTotalExperienceToReachLevel(int level, float progress) {
    int sum = IntStream.range(0, level).map(ExperienceUtil::getNextLevelExperience).sum();
    return sum + Math.round(getNextLevelExperience(level) * progress);
  }

  /** Return the current total experience of the player. */
  public static int getTotalExperience(PlayerEntity player) {
    return getTotalExperienceToReachLevel(player.experienceLevel, player.experienceProgress);
  }
}
