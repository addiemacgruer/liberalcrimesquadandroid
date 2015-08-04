package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.Activities;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.politics.Politics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Sketch portraits for money.
 * @author addie */
public @NonNullByDefault class Art extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature s : this) {
      if (!Activities.checkForArrest(s, "sketching portraits")) {
        int money = s.skill().skillRoll(Skill.ART);
        // Country's alignment affects effectiveness
        // In a Liberal country, there are many competing vendors
        if (Politics.publicmood() > 65) {
          money /= 2;
        }
        if (Politics.publicmood() > 35) {
          money /= 2;
        }
        s.income(money);
        i.ledger.addFunds(money, Ledger.IncomeType.SKETCHES);
        s.skill().train(Skill.ART, Math.max(7 - s.skill().skill(Skill.ART), 4));
        ui().text(s.toString() + " made $" + s.income() + " sketching portraits.").add();
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
