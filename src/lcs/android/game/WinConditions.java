package lcs.android.game;

import static lcs.android.game.Game.*;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;

/** The winning condition chosen by the player at the start of the game. */
public enum WinConditions {
  /** Most laws are elite liberal, remainder are liberal; elites + liberals / 2 > 270 in house, 60 in
   * senate. */
  EASY {
    @Override public boolean victory() {
      int liberalLaws = 0;
      int eliteLaws = 0;
      for (final Issue l : Issue.values()) {
        if (!l.core) {
          continue;
        }
        if (i.issue(l).lawLT(Alignment.LIBERAL))
          return false;
        if (i.issue(l).law() == Alignment.LIBERAL) {
          liberalLaws++;
        } else {
          eliteLaws++;
        }
      }
      if (eliteLaws < liberalLaws)
        return false;
      return true;
    }
  },
  /** All laws are elite liberal; elites + liberals / 2 > 290 in house, 67 in senate; 5 elite supreme
   * court judges. */
  ELITE {
    @Override public boolean victory() {
      for (final Issue l : Issue.values()) {
        if (i.issue(l).lawLT(Alignment.ELITELIBERAL))
          return false;
      }
      return true;
    }
  };
  /** Are the laws sufficiently liberal for victory?
   * @return if we've won the game. */
  abstract public boolean victory();
}