package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.site.type.AbstractSiteType.UniqueNamedSite;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "INDUSTRY_WAREHOUSE") public @NonNullByDefault class Warehouse extends
    UniqueNamedSite implements IInvestable {
  @Override public boolean hasLoot() {
    return false;
  }

  @Override void uniqueName(final Location l) {
    l.setName("Old ");
    l.setName(CreatureName.lastname() + " ");
    switch (i.rng.nextInt(10)) {
    default:
    case 0:
      l.setName(l.toString() + "Meat Plant");
      break;
    case 1:
      l.setName(l.toString() + "Warehouse");
      break;
    case 2:
      l.setName(l.toString() + "Paper Mill");
      break;
    case 3:
      l.setName(l.toString() + "Cement Factory");
      break;
    case 4:
      l.setName(l.toString() + "Fertilizer Plant");
      break;
    case 5:
      l.setName(l.toString() + "Drill Factory");
      break;
    case 6:
      l.setName(l.toString() + "Steel Plant");
      break;
    case 7:
      l.setName(l.toString() + "Packing Plant");
      break;
    case 8:
      l.setName(l.toString() + "Toy Factory");
      break;
    case 9:
      l.setName(l.toString() + "Building Site");
      break;
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
