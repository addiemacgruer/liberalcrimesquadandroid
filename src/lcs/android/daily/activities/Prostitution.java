package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.law.Crime;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Prostitution extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature p : this) {
      // Business once every three days or so
      if (i.rng.likely(3)) {
        ui().text(p.toString() + " got no business today.").add();
        continue;
      }
      // Street sense check or deal with slimy people that reduce
      // dignity and juice
      if (i.rng.chance(3) && !p.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE)) {
        p.addJuice(-i.rng.nextInt(3), -20);
      }
      // Gain seduction and street sense
      p.skill().train(Skill.SEDUCTION, Math.max(10 - p.skill().skill(Skill.SEDUCTION), 0));
      p.skill().train(Skill.STREETSENSE, Math.max(10 - p.skill().skill(Skill.STREETSENSE), 0));
      /* Street sense to avoid a prostitution sting */
      if (i.rng.chance(50)) {
        if (!p.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE)) {
          ui().text(p + " has been arrested in a prostitution sting!").add();
          p.addJuice(-7, -30);
          p.captureByPolice(Crime.PROSTITUTION);
          continue; // don't make money
        }
        ui().text(p.toString() + " was nearly caught in a prostitution sting.").add();
        p.addJuice(5, 0);
      }
      // Skill determines how much money you get
      final int performance = p.skill().skillRoll(Skill.SEDUCTION);
      if (performance > CheckDifficulty.HEROIC.value()) {
        p.income(i.rng.nextInt(201) + 200);
      } else {
        p.income(i.rng.nextInt(10 * performance) + 10 * performance);
      }
      i.ledger.addFunds(p.income(), Ledger.IncomeType.PROSTITUTION);
      ui().text(p + " made $" + p.income() + " from prositution.").add();
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
