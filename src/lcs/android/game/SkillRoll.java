package lcs.android.game;

import static lcs.android.game.Game.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** Rolls some dice to determine success and failure using an adapted EABA system. */
public @NonNullByDefault class SkillRoll {
  /** Alea iacta est. This die rolling system (and the associated difficulty ratings) is adapted from
   * EABA, which uses a system of rolling a number of six-sided dice equal to the ability score
   * divided by three. The top three dice are used, the rest discarded. Finally, any additional
   * points that didn't divide evenly into creating a die contribute to odd-shaped dice that don't
   * exist in the real world. This system gives diminishing returns for high skill levels. EABA
   * actually just adds the remainder to the die total, but there are some statistical problems with
   * that system. It is not possible to roll above an 18 using this system. It is possible to roll
   * below a 3, if you don't have at least 9 skill.
   * @param skill the skill value to be tested (generally from 0 to 20)
   * @return a roll from 0 to 18 inclusive. */
  public static int roll(final int skill) {
    final List<Integer> roll = new ArrayList<Integer>();
    roll.add(0);
    roll.add(0);
    roll.add(0);
    int s = skill;
    while (s >= 3) {
      s -= 3;
      roll.add(i.rng.nextInt(6) + 1);
    }
    while (s >= 2) {
      s -= 2;
      roll.add(i.rng.nextInt(5) + 1);
    }
    while (s >= 1) {
      s -= 1;
      roll.add(i.rng.nextInt(3) + 1);
    }
    Collections.sort(roll);
    Collections.reverse(roll);
    final int total = roll.get(0) + roll.get(1) + roll.get(2);
    final StringBuilder sb = new StringBuilder();
    for (int x = 0; x < roll.size(); x++) {
      sb.append(x <= 2 ? "[" + roll.get(x) + "] " : roll.get(x) + " ");
    }
    Curses.toast(sb.toString());
    Log.d(Game.LCS, "SkillRoll.check(" + skill + ")=" + roll + "=" + total);
    return total;
  }
}
