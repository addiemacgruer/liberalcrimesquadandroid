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

public @NonNullByDefault class TShirts extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> tshirts = this;
    // TSHIRTS
    int money;
    final int mood = Politics.publicmood();
    for (final Creature s : tshirts) {
      if (!Activities.checkForArrest(s, "selling shirts")) {
        money = (s.skill().skillRoll(Skill.TAILORING) + s.skill().skillRoll(Skill.BUSINESS)) / 2;
        // Country's alignment affects effectiveness
        // In a Liberal country, there are many competing vendors
        if (mood > 65) {
          money /= 2;
        }
        if (mood > 35) {
          money /= 2;
        }
        s.income(money);
        ui().text(s.toString() + " made $" + s.income() + " selling T-shirts.").add();
        i.ledger.addFunds(money, Ledger.IncomeType.TSHIRTS);
        s.skill().train(Skill.TAILORING, Math.max(7 - s.skill().skill(Skill.TAILORING), 2));
        s.skill().train(Skill.BUSINESS, Math.max(7 - s.skill().skill(Skill.BUSINESS), 2));
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
