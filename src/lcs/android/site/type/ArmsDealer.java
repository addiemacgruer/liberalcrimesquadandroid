package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.shop.Shop;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_ARMSDEALER") public @NonNullByDefault class ArmsDealer extends
    AbstractSiteType {
  @Override public boolean canBuyGuns() {
    return true;
  }

  @Override public void generateName(final Location l) {
    l.setName("Black Market");
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  /* select a gun for arms dealership */
  public static void armsdealer(final Location loc) {
    i.activeSquad.location(loc);
    final Shop armsdealer = new Shop("armsdealer.xml");
    armsdealer.enter(i.activeSquad);
  }
}
