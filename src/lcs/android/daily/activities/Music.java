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

public @NonNullByDefault class Music extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> music = this;
    int money;
    for (final Creature s : music) {
      if (!Activities.checkForArrest(s, "playing music")) {
        money = s.skill().skillRoll(Skill.MUSIC) / 2;
        final boolean has_instrument = s.weapon().weapon().isInstrument();
        if (has_instrument) {
          money *= 4;
        }
        // Country's alignment affects effectiveness
        // In a Liberal country, there are many competing vendors
        if (Politics.publicmood() > 65) {
          money /= 2;
        }
        if (Politics.publicmood() > 35) {
          money /= 2;
        }
        i.ledger.addFunds(money, Ledger.IncomeType.BUSKING);
        s.income(money);
        ui().text(s + " made $" + s.income() + " playing music.").add();
        if (has_instrument) {
          s.skill().train(Skill.MUSIC, Math.max(7 - s.skill().skill(Skill.MUSIC), 4));
        } else {
          s.skill().train(Skill.MUSIC, Math.max(5 - s.skill().skill(Skill.MUSIC), 2));
        }
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
