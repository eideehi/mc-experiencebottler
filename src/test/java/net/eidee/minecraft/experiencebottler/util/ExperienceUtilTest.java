package net.eidee.minecraft.experiencebottler.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExperienceUtilTest {
  private static long totalExperienceAtLevel(int level) {
    return level < 15
        ? (long) level * (level + 6)
        : level < 30
            ? (5L * level * level - 81L * level + 720L) >> 1
            : (9L * level * level - 325L * level + 4440L) >> 1;
  }

  @Test
  void getNextLevelExperienceMatchesVanillaBoundaries() {
    assertEquals(7L, ExperienceUtil.getNextLevelExperience(0));
    assertEquals(9L, ExperienceUtil.getNextLevelExperience(1));
    assertEquals(35L, ExperienceUtil.getNextLevelExperience(14));
    assertEquals(37L, ExperienceUtil.getNextLevelExperience(15));
    assertEquals(107L, ExperienceUtil.getNextLevelExperience(29));
    assertEquals(112L, ExperienceUtil.getNextLevelExperience(30));
  }

  @Test
  void getLevelFromTotalExperienceMatchesBoundaryValues() {
    assertEquals(0, ExperienceUtil.getLevelFromTotalExperience(-1));
    assertEquals(0, ExperienceUtil.getLevelFromTotalExperience(0));
    assertEquals(14, ExperienceUtil.getLevelFromTotalExperience(314));
    assertEquals(15, ExperienceUtil.getLevelFromTotalExperience(315));
    assertEquals(29, ExperienceUtil.getLevelFromTotalExperience(1394));
    assertEquals(30, ExperienceUtil.getLevelFromTotalExperience(1395));
    assertEquals(21863, ExperienceUtil.getLevelFromTotalExperience(Integer.MAX_VALUE));
  }

  @Test
  void getLevelFromTotalExperienceIsMonotonic() {
    int lastLevel = 0;
    for (int experience = 0; experience <= 10_000; ++experience) {
      int level = ExperienceUtil.getLevelFromTotalExperience(experience);
      assertTrue(level >= lastLevel, "level decreased at total XP " + experience);
      lastLevel = level;
    }
  }

  @Test
  void getLevelFromTotalExperienceMatchesExactLevelTotals() {
    for (int level = 0; level <= 250; ++level) {
      long totalExperience = totalExperienceAtLevel(level);
      assertEquals(
          level,
          ExperienceUtil.getLevelFromTotalExperience(totalExperience),
          "exact total XP should resolve to the same level");

      if (level > 0) {
        assertEquals(
            level - 1,
            ExperienceUtil.getLevelFromTotalExperience(totalExperience - 1),
            "one XP before a level threshold should still be the previous level");
      }
    }
  }
}
