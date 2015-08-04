package lcs.android.site.map;

import lcs.android.game.CheckDifficulty;

public enum SecurityLevel {
  HIGH(CheckDifficulty.FORMIDABLE, false),
  MEDIUM(CheckDifficulty.CHALLENGING, true),
  POOR(CheckDifficulty.EASY, true);
  SecurityLevel(final CheckDifficulty doorDifficulty, final boolean crowbarable) {
    this.doorDifficulty = doorDifficulty;
    this.crowbarable = crowbarable;
  }

  private final boolean crowbarable;

  private final CheckDifficulty doorDifficulty;

  public CheckDifficulty doorDifficulty() {
    return doorDifficulty;
  }

  boolean crowbarable() {
    return crowbarable;
  }
}