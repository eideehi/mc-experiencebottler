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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Utility class for experience. */
public class ExperienceUtil {
  public static final int TOTAL_EXP_LV_30 = 1395;
  private static final int TOTAL_EXP_LV_15 = 315;
  private static final int MAX_EXPERIENCE = Integer.MAX_VALUE;
  private static final int MAX_SAFE_LEVEL = 21863; // Highest level whose total XP fits in int

  private ExperienceUtil() {}

  private static long totalExperienceAtLevel(long level) {
    // Closed-form totals per vanilla XP curve. Caller must ensure 0 <= level <= MAX_SAFE_LEVEL.
    // Segment 0-14:  T(L) = L(L+6)           derived from sum of (7+2i) for i=0..L-1
    // Segment 15-29: T(L) = (5L²-81L+720)/2  derived from T(15) + sum of (37+5(i-15)) for i=15..L-1
    // Segment 30+:   T(L) = (9L²-325L+4440)/2 derived from T(30) + sum of (112+9(i-30)) for
    // i=30..L-1
    return level < 15
        ? level * (level + 6)
        : level < 30
            ? (5L * level * level - 81L * level + 720L) >> 1
            : (9L * level * level - 325L * level + 4440L) >> 1;
  }

  /**
   * Calculates and returns the experience required from the specified level to the next level.
   *
   * @param level The current level of the player.
   * @return The experience required to reach the next level from the current level.
   */
  public static long getNextLevelExperience(long level) {
    // Vanilla XP cost per level. Caller should ensure level is within valid range.
    // 0-14:  7 + 2L
    // 15-29: 37 + 5(L-15) = 5L - 38
    // 30+:   112 + 9(L-30) = 9L - 158
    return level >= 30 ? 9L * level - 158L : level >= 15 ? 5L * level - 38L : 7L + (level << 1);
  }

  /**
   * Calculates and returns the level corresponding to the specified total experience.
   *
   * @param experience The total experience points.
   * @return The level corresponding to the total experience.
   */
  public static int getLevelFromTotalExperience(long experience) {
    // Clamp input to valid XP range; return max level if at int cap.
    if (experience <= 0) return 0;
    if (experience >= MAX_EXPERIENCE) return MAX_SAFE_LEVEL;

    // Analytical inversion of piecewise quadratic/linear total XP formulas.
    // Floating-point sqrt may introduce ±1 error, corrected below.
    int level;
    if (experience < TOTAL_EXP_LV_15) {
      // Invert L(L+6)=xp => L = floor(sqrt(xp+9) - 3)
      level = (int) (Math.sqrt(experience + 9.0) - 3.0);
    } else if (experience < TOTAL_EXP_LV_30) {
      // Invert (5L²-81L+720)/2=xp => L = floor((81 + sqrt(40*xp - 7839)) / 10)
      level = (int) ((81.0 + Math.sqrt(40.0 * experience - 7839.0)) * 0.1);
    } else {
      // Invert (9L²-325L+4440)/2=xp => L = floor((325 + sqrt(72*xp - 54215)) / 18)
      level = (int) ((325.0 + Math.sqrt(72.0 * experience - 54215.0)) / 18.0);
    }

    // Clamp then correct for floating-point rounding (at most ±1 adjustment).
    if (level < 0) level = 0;
    else if (level > MAX_SAFE_LEVEL) level = MAX_SAFE_LEVEL;

    long total = totalExperienceAtLevel(level);
    if (total > experience) {
      // Overshot: step back once.
      return level > 0 ? level - 1 : 0;
    }
    // Check if next level is reachable.
    if (level < MAX_SAFE_LEVEL && totalExperienceAtLevel(level + 1) <= experience) {
      return level + 1;
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
    // Returns total XP at given level + fractional progress toward next level.
    if (level <= 0) return progress > 0f ? Math.round(7L * MathHelper.clamp(progress, 0f, 1f)) : 0L;
    long clampedLevel = level > MAX_SAFE_LEVEL ? MAX_SAFE_LEVEL : level;
    long base = totalExperienceAtLevel(clampedLevel);
    if (progress <= 0f) return base;
    float clampedProgress = progress > 1f ? 1f : progress;
    long extra = Math.round(getNextLevelExperience(clampedLevel) * clampedProgress);
    long result = base + extra;
    return result > MAX_EXPERIENCE ? MAX_EXPERIENCE : result;
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
