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

import java.util.stream.LongStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Utility class for experience. */
public class ExperienceUtil {
  public static final int TOTAL_EXP_LV_30 = 1395;
  public static final int TOTAL_EXP_LV_31 = 1507;
  public static final int TOTAL_EXP_LV_1000 = 4339720;

  private ExperienceUtil() {}

  private static long calcTotalExperienceGteLv31(long level) {
    long result = TOTAL_EXP_LV_30;
    result += 112L * (level - 30);
    result += 9L * (level - 31) * (level - 30) / 2;
    return result;
  }

  /** Calculates and returns the experience required from the specified level to the next level. */
  public static long getNextLevelExperience(long level) {
    if (level >= 30) {
      return 112 + (level - 30) * 9;
    } else {
      return level >= 15 ? 37 + (level - 15) * 5L : 7 + level * 2;
    }
  }

  /** Calculates and returns the level corresponding to the specified total experience. */
  public static int getLevelFromTotalExperience(long experience) {
    int level = 0;
    if (experience < TOTAL_EXP_LV_31) {
      long amount = experience;
      while (amount >= 0) {
        long experienceForLevel = getNextLevelExperience(level);
        if (amount < experienceForLevel) {
          break;
        }
        level++;
        amount -= experienceForLevel;
      }
      return level;
    }

    boolean isOverLv10000 = experience >= TOTAL_EXP_LV_1000;
    level = 31;
    while (true) {
      if (calcTotalExperienceGteLv31(level) >= experience) {
        if (calcTotalExperienceGteLv31(level - 1) > experience) {
          level--;
          continue;
        }
        break;
      }
      if (isOverLv10000) {
        level += 1000;
      } else {
        level++;
      }
    }

    if (getTotalExperienceToReachLevel(level, 0) > experience) {
      level--;
    }

    return level;
  }

  /**
   * Calculates and returns the experience required to reach the specified level.
   *
   * @param level Current level
   * @param progress Progress to the next level
   */
  public static long getTotalExperienceToReachLevel(long level, float progress) {
    if (level > 30) {
      long total = calcTotalExperienceGteLv31(level);
      return total + Math.round(getNextLevelExperience(level) * progress);
    }
    long total = LongStream.range(0, level).map(ExperienceUtil::getNextLevelExperience).sum();
    return total + Math.round(getNextLevelExperience(level) * progress);
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
      // If totalExperience is not changed, synchronization packets will not be sent to the client, so change it here.
      player.totalExperience--;
    }
  }
}
