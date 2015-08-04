package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "RESIDENTIAL_BOMBSHELTER") public @NonNullByDefault class BombShelter extends
    AbstractSiteType {
  @Override public boolean canBuyGuns() {
    return true;
  }

  @Override public void generateName(final Location l) {
    l.setName("Fallout Shelter");
  }

  @Override public boolean isCcsSafeHouse() {
    return true;
  }

  @Override public String randomLootItem() {
    return AbstractSiteType.type(BarAndGrill.class).randomLootItem();
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
