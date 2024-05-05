/*
 * MIT License
 *
 * Copyright (c) 2021-2024 EideeHi
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

import java.util.Arrays;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Utility class for experience. */
public class ExperienceUtil {
  public static final int TOTAL_EXP_LV_30 = 1395;

  private static final long[] TOTAL_EXP_CACHE;

  static {
    TOTAL_EXP_CACHE = new long[256];
    TOTAL_EXP_CACHE[0] = 0;
    for (int level = 1; level < TOTAL_EXP_CACHE.length; level++) {
      int prev = level - 1;
      TOTAL_EXP_CACHE[level] = TOTAL_EXP_CACHE[prev] + getNextLevelExperience(prev);
    }
  }

  private ExperienceUtil() {}

  private static long calcTotalExperienceGteLv31(long level) {
    long result = TOTAL_EXP_LV_30;
    result += 112L * (level - 30);
    result += 9L * (level - 31) * (level - 30) / 2;
    return result;
  }

  /**
   * Calculates and returns the experience required from the specified level to the next level.
   *
   * @param level The current level of the player.
   * @return The experience required to reach the next level from the current level.
   */
  public static long getNextLevelExperience(long level) {
    if (level >= 30) {
      return 112 + (level - 30) * 9;
    } else {
      return level >= 15 ? 37 + (level - 15) * 5L : 7 + level * 2;
    }
  }

  /**
   * Calculates and returns the level corresponding to the specified total experience.
   *
   * @param experience The total experience points.
   * @return The level corresponding to the total experience.
   */
  public static int getLevelFromTotalExperience(long experience) {
    if (experience <= TOTAL_EXP_CACHE[255]) {
      int index = Arrays.binarySearch(TOTAL_EXP_CACHE, experience);
      return index >= 0 ? index : -index - 2;
    }

    int level = 512;
    while (calcTotalExperienceGteLv31(level) < experience) {
      level *= 2;
    }

    int left = level / 2;
    int right = level;

    while (left <= right) {
      int mid = (left + right) / 2;
      long total = calcTotalExperienceGteLv31(mid);

      if (total == experience) {
        return mid;
      } else if (total < experience) {
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }

    return right;
  }

  /**
   * Calculates and returns the experience required to reach the specified level.
   *
   * @param level Current level
   * @param progress Progress to the next level
   */
  public static long getTotalExperienceToReachLevel(long level, float progress) {
    if (level < 0) {
      return 0;
    }
    if (level < 255) {
      int index = (int) level;
      return TOTAL_EXP_CACHE[index] + Math.round(getNextLevelExperience(index) * progress);
    }
    long total = calcTotalExperienceGteLv31(level);
    return progress > 0f ? total + Math.round(getNextLevelExperience(level) * progress) : total;
  }

  /** Return the current total experience of the player. */
  public static long getTotalExperience(PlayerEntity player) {
    return getTotalExperienceToReachLevel(player.experienceLevel, player.experienceProgress);
  }

  /** Gives the player experience. A negative integer removes experience from the player. */
  public static void addExperience(PlayerEntity player, int experience) {
    if (experience == 0) {
      return;
    }

    final int prevTotalExperience = player.totalExperience;
    final long currentExperience = getTotalExperience(player);
    final long newExperience = currentExperience + experience;

    player.experienceLevel = 0;
    player.experienceProgress = 0;
    player.totalExperience = 0;

    long total = currentExperience;
    long limit = Math.abs((long) Integer.MIN_VALUE - player.getScore());
    int score;
    do {
      score = (int) Math.min(Math.min(total, Integer.MAX_VALUE), limit);
      player.addScore(-score);
      limit -= score;
      total -= score;
    } while (total > 0 && limit > 0);

    total = newExperience;
    limit = (long) Integer.MAX_VALUE - player.getScore();
    do {
      score = (int) Math.min(Math.min(total, limit), Integer.MAX_VALUE);
      player.addScore(score);
      limit -= score;
      total -= score;
    } while (total > 0 && limit > 0);

    int level = getLevelFromTotalExperience(newExperience);
    if (experience > 0 && level > 5) {
      player.addExperienceLevels(5);
      level -= 5;
    }

    player.experienceLevel += level;
    player.experienceProgress =
        (newExperience - getTotalExperienceToReachLevel(player.experienceLevel, 0))
            / (float) getNextLevelExperience(player.experienceLevel);
    if (player.experienceProgress == 1.0) {
      ++player.experienceLevel;
      player.experienceProgress = 0;
    }

    player.totalExperience = (int) MathHelper.clamp(newExperience, 0, Integer.MAX_VALUE);
    if (player.totalExperience == prevTotalExperience) {
      // If totalExperience is not changed, synchronization packets will not be sent to the client,
      // so change it here.
      player.totalExperience--;
    }
  }
}
