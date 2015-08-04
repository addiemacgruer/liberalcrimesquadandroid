package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.Activities;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.politics.Politics;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Solicit extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> solicit = this;
    int total_income = 0;
    for (final Creature s : solicit) {
      if (!Activities.checkForArrest(s, "soliciting donations")) {
        int income = s.skill().skillRoll(Skill.PERSUASION) * s.getArmor().professionalism() + 1;
        // Country's alignment dramatically affects effectiveness
        // The more conservative the country, the more effective
        if (Politics.publicmood(null) > 90) {
          income /= 2;
        }
        if (Politics.publicmood(null) > 65) {
          income /= 2;
        }
        if (Politics.publicmood(null) > 35) {
          income /= 2;
        }
        if (Politics.publicmood(null) > 10) {
          income /= 2;
        }
        s.income(income);
        ui().text(s + " made $" + s.income() + " soliciting donations.").add();
        total_income += income;
        s.skill().train(Skill.PERSUASION, Math.max(5 - s.skill().skill(Skill.PERSUASION), 2));
      }
    }
    i.ledger.addFunds(total_income, Ledger.IncomeType.DONATIONS);
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
