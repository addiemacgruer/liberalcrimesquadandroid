package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "RESIDENTIAL_SHELTER") public @NonNullByDefault class Shelter extends
    AbstractSiteType {
  @Override public boolean canMap() {
    return false;
  }

  @Override public void generateName(final Location l) {
    l.setName("Homeless Shelter");
  }

  @Override public boolean hasLoot() {
    return false;
  }

  @Override public int rent() {
    return 0;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
