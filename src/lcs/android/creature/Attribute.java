package lcs.android.creature;

import lcs.android.util.DefaultValueKey;

/** Seven values that define the 'base abilities' of a character.
 * @author addie */
public enum Attribute implements DefaultValueKey<Integer> {
  AGILITY,
  CHARISMA,
  HEALTH,
  HEART,
  INTELLIGENCE,
  STRENGTH,
  WISDOM;
  @Override public Integer defaultValue() {
    return DEFAULT;
  }

  private static final Integer DEFAULT = Integer.valueOf(1);
}
