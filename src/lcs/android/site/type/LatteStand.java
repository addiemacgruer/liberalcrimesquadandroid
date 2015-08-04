package lcs.android.site.type;

import static lcs.android.game.Game.*;

import java.util.EnumSet;

import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_LATTESTAND") public @NonNullByDefault class LatteStand extends
    AbstractSiteType {
  @Override public void allocateMap(final LcsRandom SITERNG) {
    for (int x = SiteMap.MAPX / 2 - 4; x <= SiteMap.MAPX / 2 + 4; x++) {
      for (int y = 0; y < 7; y++) {
        if (x == SiteMap.MAPX / 2 - 4 || x == SiteMap.MAPX / 2 + 4 || y == 0 || y == 6) {
          i.site.siteLevelmap()[x][y][0].flag = EnumSet.of(TileSpecial.EXIT);
        } else {
          i.site.siteLevelmap()[x][y][0].flag.clear();
        }
        i.site.siteLevelmap()[x][y][0].special = null;
        i.site.siteLevelmap()[x][y][0].siegeFlag.clear();
      }
    }
  }

  @Override public void generateName(final Location l) {
    l.setName(i.rng.choice("Frothy", "Milky", "Caffeine", "Morning", "Evening") + " "
        + i.rng.choice("Mug", "Cup", "Jolt", "Wonder", "Express") + " Latte Stand");
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
