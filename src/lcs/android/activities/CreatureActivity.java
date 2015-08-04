package lcs.android.activities;

import static lcs.android.util.Curses.*;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Creature;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Activity which has a creature as a target. (Basically, hostage tending). Immutable.
 * @author addie */
public @NonNullByDefault class CreatureActivity extends AbstractActivity {
  /** Activity which has a creature as a target. (Basically, hostage tending). Immutable.
   * @param type
   * @param creature
   * @throws IllegalArgumentException if creature==null. */
  public CreatureActivity(final Activity type, final Creature creature) {
    super(type);
    crarg = creature;
  }

  private final Creature crarg;

  public Creature creature() {
    return crarg;
  }

  @Override public String toString() {
    if (type == Activity.HOSTAGETENDING)
      return format("Tending to %s", crarg.toString());
    return super.toString();
  }

  private static final long serialVersionUID = Game.VERSION;
}
