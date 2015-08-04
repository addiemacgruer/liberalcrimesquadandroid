package lcs.android.activities;

import lcs.android.activities.iface.Activity;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Activity with no further details of who / where required. Immutable.
 * @author addie */
public @NonNullByDefault class BareActivity extends AbstractActivity {
  public BareActivity(final Activity type) {
    super(type);
  }

  private static final BareActivity INSTANCE = new BareActivity(Activity.NONE);

  private static final long serialVersionUID = Game.VERSION;

  public static BareActivity noActivity() {
    return INSTANCE;
  }
}
