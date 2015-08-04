package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.site.creation.SiteMap;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_JUICEBAR") public @NonNullByDefault class JuiceBar extends
    AbstractSiteType {
  @Override public void allocateMap(final LcsRandom SITERNG) {
    for (int x = SiteMap.MAPX / 2 - 4; x <= SiteMap.MAPX / 2 + 4; x++) {
      for (int y = 3; y < 10; y++) {
        i.site.siteLevelmap()[x][y][0].flag.clear();
        i.site.siteLevelmap()[x][y][0].special = null;
        i.site.siteLevelmap()[x][y][0].siegeFlag.clear();
      }
    }
  }

  @Override public void generateName(final Location l) {
    l.setName(i.rng.choice("Natural", "Harmonious", "Restful", "Healthy", "New You") + " "
        + i.rng.choice("Diet", "Methods", "Plan", "Orange", "Carrot") + " Juice Bar");
  }

  @Override public boolean hasLoot() {
    return false;
  }

  @Override public boolean hasTables() {
    return true;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
