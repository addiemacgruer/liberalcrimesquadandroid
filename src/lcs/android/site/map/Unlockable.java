package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.util.Filter;
import lcs.android.util.Maybe;

public enum Unlockable {
  ARMORY,
  CAGE,
  CAGE_HARD,
  CELL,
  DOOR,
  SAFE;
  /** unlock attempt */
  public SuccessTest unlock() {
    CheckDifficulty difficulty = null;
    switch (this) {
    case DOOR:
      if (i.site.current().type().securityLevel() == SecurityLevel.MEDIUM) {
        difficulty = CheckDifficulty.CHALLENGING;
      } else if (i.site.current().type().securityLevel() == SecurityLevel.HIGH) {
        difficulty = CheckDifficulty.HARD;
      } else {
        difficulty = CheckDifficulty.EASY;
      }
      break;
    case CAGE:
      difficulty = CheckDifficulty.VERYEASY;
      break;
    case CAGE_HARD:
      difficulty = CheckDifficulty.AVERAGE;
      break;
    case CELL:
      difficulty = CheckDifficulty.FORMIDABLE;
      break;
    case ARMORY:
      difficulty = CheckDifficulty.HEROIC;
      break;
    case SAFE:
      difficulty = CheckDifficulty.HEROIC;
      break;
    default:
      difficulty = CheckDifficulty.AUTOMATIC;
      break;
    }
    final Maybe<Creature> bestSecurityTest = Filter.best(i.activeSquad,
        Filter.skill(Skill.SECURITY));
    if (bestSecurityTest.exists()) {
      final Creature bestSecurity = bestSecurityTest.get();
      final int maxattack = bestSecurity.skill().skill(Skill.SECURITY);
      // lock pick succeeded.
      if (bestSecurity.skill().skillCheck(Skill.SECURITY, difficulty)) {
        // skill goes up in proportion to the chance of you failing.
        if (maxattack <= difficulty.value()) {
          bestSecurity.skill().train(Skill.SECURITY, 1 + difficulty.value() - maxattack);
        }
        switch (this) {
        case DOOR:
          ui().text(bestSecurity.toString() + " unlocks the door!").add();
          break;
        case CAGE_HARD:
        case CAGE:
          ui().text(bestSecurity.toString() + " unlocks the cage!").add();
          break;
        case SAFE:
          ui().text(bestSecurity.toString() + " cracks the safe!").add();
          break;
        case ARMORY:
          ui().text(bestSecurity.toString() + " opens the armory!").add();
          break;
        case CELL:
          ui().text(bestSecurity.toString() + " unlocks the cell!").add();
          break;
        default:
          ui().text(bestSecurity.toString() + " picks the lock!").add();
        }
        /* If people witness a successful unlock, they learn a little bit. */
        for (final Creature j : i.activeSquad) {
          if (j == bestSecurity) {
            continue;
          }
          if (j.health().alive() && j.skill().skill(Skill.SECURITY) < difficulty.value()) {
            j.skill().train(Skill.SECURITY, difficulty.value() - j.skill().skill(Skill.SECURITY));
          }
        }
        getch();
        return SuccessTest.SUCCEED_NOISILY;
      }
      /* gain some experience for failing only if you could have succeeded. */
      if (bestSecurity.skill().skillCheck(Skill.SECURITY, difficulty)) {
        bestSecurity.skill().train(Skill.SECURITY, 10);
        ui().text(bestSecurity.toString() + " is close, but can't quite get the lock open.").add();
      } else {
        ui().text(bestSecurity.toString() + " can't figure the lock out.").add();
      }
      getch();
      return SuccessTest.FAIL_NOISILY;
    }
    ui().text("You can't find anyone to do the job.").add();
    getch();
    return SuccessTest.FAIL_QUIETLY;
  }
}