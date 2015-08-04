package lcs.android.game;

import android.util.Log;

/** Named values for how challenging a {@link SkillRoll} is to succeed on */
public enum CheckDifficulty {
  AUTOMATIC(1),
  AVERAGE(7),
  CHALLENGING(9),
  EASY(5),
  FORMIDABLE(13),
  HARD(11),
  HEROIC(15),
  IMPOSSIBLE(19),
  SUPERHEROIC(17),
  VERYEASY(3);
  CheckDifficulty(final int value) {
    this.value = value;
  }

  private final int value;

  /** integer difficulty as a {@link SkillRoll}
   * @return the value. */
  public int value() {
    return value;
  }

  private final static CheckDifficulty[] ORDER = { AUTOMATIC, VERYEASY, EASY, AVERAGE, CHALLENGING,
      HARD, FORMIDABLE, HEROIC, SUPERHEROIC, IMPOSSIBLE };

  /** @param makeDifficulty
   * @return */
  public static CheckDifficulty fromInt(final int difficulty) {
    for (final CheckDifficulty cd : ORDER) {
      if (cd.value > difficulty) {
        Log.i("LCS", "Difficuly:" + difficulty + "=" + cd);
        return cd;
      }
    }
    Log.i("LCS", "Difficuly:" + difficulty + " not found");
    return IMPOSSIBLE;
  }
}
