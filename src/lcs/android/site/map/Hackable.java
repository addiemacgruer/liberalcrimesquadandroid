package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.creature.Creature;
import lcs.android.creature.health.SpecialWounds;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import android.util.Pair;

enum Hackable {
  SUPERCOMPUTER(CheckDifficulty.HEROIC);
  Hackable(final CheckDifficulty difficulty) {
    this.difficulty = difficulty;
  }

  private final CheckDifficulty difficulty;

  /* computer hack attempt */
  Pair<Boolean, Boolean> hack() {
    int maxattack = 0;
    boolean blind = false;
    Creature hacker = null;
    for (final Creature p : i.activeSquad) {
      if (p.health().alive() && p.skill().skill(Skill.COMPUTERS) > 0) {
        final int roll = p.skill().skillRoll(Skill.COMPUTERS);
        if (roll > maxattack) {
          if (p.health().getWound(SpecialWounds.RIGHTEYE) == 0
              && p.health().getWound(SpecialWounds.LEFTEYE) == 0) {
            blind = true;
          } else {
            maxattack = roll;
            hacker = p;
          }
        }
      }
    }
    if (hacker != null) {
      hacker.skill().train(Skill.COMPUTERS, difficulty.value());
      if (maxattack > difficulty.value()) {
        ui().text(hacker.toString()).add();
        switch (this) {
        case SUPERCOMPUTER:
        default:
          ui().text(" has burned a disk of top secret files!").add();
          break;
        }
        getch();
        return Pair.create(true, true);
      }
      ui().text(hacker.toString()).add();
      switch (this) {
      case SUPERCOMPUTER:
      default:
        ui().text(" couldn't bypass the supercomputer security.").add();
        break;
      }
      getch();
      return Pair.create(false, true);
    }
    ui().text("You can't find anyone to do the job.").add();
    if (blind) {
      // Screen readers FTW. Honestly, it should just be a handicap
      // instead of an impossibility, just make the chances
      // much, much less.
      // -- LK
      ui().text("Including the UNSIGHTED HACKER you brought.").add();
    }
    getch();
    return Pair.create(false, false);
  }
}