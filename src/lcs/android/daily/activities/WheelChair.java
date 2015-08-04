package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;

import lcs.android.activities.BareActivity;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Daily activity for Liberals looking to obtain a wheelchair. 50% chance of success.
 * @author addie */
public @NonNullByDefault class WheelChair extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature c : this) {
      getwheelchair(c);
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  private static void getwheelchair(final Creature cr) {
    if (i.rng.chance(2)) {
      ui().text(cr + " has procured a wheelchair.").add();
      cr.addFlag(CreatureFlag.WHEELCHAIR);
      cr.activity(BareActivity.noActivity());
    } else {
      ui().text(cr + " was unable to get a wheelchair.  Maybe tomorrow...").add();
    }
  }
}
