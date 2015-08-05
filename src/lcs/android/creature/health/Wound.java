package lcs.android.creature.health;

public enum Wound {
  BLEEDING,
  BRUISED,
  BURNED,
  CLEANOFF,
  CUT,
  NASTYOFF,
  NONE,
  SHOT,
  TORN;
  /** @return */
  public Wound get() { // TODO inline
    return this;
  }
}