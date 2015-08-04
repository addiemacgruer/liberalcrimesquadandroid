package lcs.android.activities;

import static lcs.android.util.Curses.*;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class LocationActivity extends AbstractActivity {
  /** Activity which has a location. Imutable.
   * @param type
   * @param location
   * @throws IllegalArgumentException if location==null */
  public LocationActivity(final Activity type, final Location location) {
    super(type);
    loc = location;
  }

  private final Location loc;

  public Location location() {
    return loc;
  }

  @Override public String toString() {
    switch (type) {
    case VISIT: {
      if (loc.frontBusiness() == null)
        return format("Going to %s", loc.toString());
      return format("Going to %s", loc.frontName());
    }
    default:
      return super.toString();
    }
  }

  private static final long serialVersionUID = Game.VERSION;
}
