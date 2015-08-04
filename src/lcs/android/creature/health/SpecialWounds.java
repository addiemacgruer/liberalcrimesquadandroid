package lcs.android.creature.health;

import lcs.android.util.DefaultValueKey;

public enum SpecialWounds implements DefaultValueKey<Integer> {
  HEART("Heart punctured", 1, true, 9) {
    @Override public int healingDifficulty() {
      return 16;
    }
  },
  LEFTEYE("No left eye", 1, false),
  LEFTKIDNEY("L. kidney damaged", 1, false, 1),
  LEFTLUNG("L. lung collapsed", 1, true, 1),
  LIVER("Liver damaged", 1, false, 1),
  LOWERSPINE("Broken lw. spine", 1, false),
  NECK("Broken neck", 1, false),
  NOSE("No nose", 1, false),
  RIBS("", 10, false) {
    @Override public String description(final int count) {
      if (count == RIBS.healthyCount)
        return RIBS.toString();
      else if (count == 0)
        return "All ribs broken";
      else if (count == TEETH.healthyCount - 1)
        return "Broken rib";
      else
        return "Broken ribs";
    }
  },
  RIGHTEYE("No right eye", 1, false),
  RIGHTKIDNEY("R. kidney damaged", 1, false, 1),
  RIGHTLUNG("R. lung collapsed", 1, true, 1),
  SPLEEN("Busted spleen", 1, false, 1),
  STOMACH("Stomach injured", 1, false, 1),
  TEETH("", 32, true) {
    @Override public String description(final int count) {
      if (count == TEETH.healthyCount)
        return TEETH.toString();
      else if (count == 0)
        return "No teeth";
      else if (count == TEETH.healthyCount - 1)
        return "Missing a tooth";
      else
        return "Missing teeth";
    }
  },
  TONGUE("No tongue", 1, false),
  UPPERSPINE("Broken up. spine", 1, false);
  SpecialWounds(final String damagedDescription, final int count, final boolean causesHealthDamage) {
    this.damagedDescription = damagedDescription;
    healthyCount = count;
    this.causesHealthDamage = causesHealthDamage;
    bleed = 0;
  }

  SpecialWounds(final String damagedDescription, final int count, final boolean causesHealthDamage,
      final int bleed) {
    this.damagedDescription = damagedDescription;
    healthyCount = count;
    this.causesHealthDamage = causesHealthDamage;
    this.bleed = bleed;
  }

  public final boolean causesHealthDamage;

  final String damagedDescription;

  private final int bleed;

  private final int healthyCount;

  /** how much additional bleeding this wound causes
   * @return bloodloss */
  public int bleed() {
    return bleed;
  }

  @Override public Integer defaultValue() {
    return healthyCount;
  }

  public String description(final int count) {
    if (healthyCount != count)
      return damagedDescription;
    return toString();
  }

  /** how hard it is to heal these injuries.
   * @return a checkdifficulty */
  public int healingDifficulty() {
    return 14;
  }

  boolean isDamaged(final int count) {
    return healthyCount != count;
  }
}
