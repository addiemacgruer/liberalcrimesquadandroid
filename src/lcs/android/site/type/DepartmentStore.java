package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.shop.Shop;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_DEPTSTORE") public @NonNullByDefault class DepartmentStore extends
    AbstractSiteType implements IShop {
  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + "'s Department Store");
  }

  @Override public void shop(final Location loc) {
    i.activeSquad.location(loc);
    final Shop deptstore = new Shop("deptstore.xml");
    deptstore.enter(i.activeSquad);
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
