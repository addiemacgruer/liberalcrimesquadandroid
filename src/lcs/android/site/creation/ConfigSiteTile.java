package lcs.android.site.creation;

import static lcs.android.game.Game.*;

import java.util.EnumSet;

import lcs.android.site.map.TileSpecial;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class ConfigSiteTile extends AbstractConfigSiteCommand {
  private enum SiteMapLogic {
    ADD,
    NONE,
    SUBTRACT
  }

  private SiteMapLogic addtype = SiteMapLogic.NONE;

  private TileSpecial tile = TileSpecial.OPEN;

  private int xstart, xend, ystart, yend, zstart, zend;

  @Override public String toString() {
    return "ConfigSiteTile:" + tile + "+" + addtype;
  }

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    // no action
  }

  @Override public void xmlSet(final String key, final String value) {
    if ("type".equals(key)) {
      tile = TileSpecial.valueOf(value);
    } else if ("xstart".equals(key)) {
      xstart = Xml.getInt(value) + SiteMap.MAPX / 2;
    } else if ("xend".equals(key)) {
      xend = Xml.getInt(value) + SiteMap.MAPX / 2;
    } else if ("x".equals(key)) {
      xstart = xend = Xml.getInt(value) + SiteMap.MAPX / 2;
    } else if ("ystart".equals(key)) {
      ystart = Xml.getInt(value);
    } else if ("yend".equals(key)) {
      yend = Xml.getInt(value);
    } else if ("y".equals(key)) {
      ystart = yend = Xml.getInt(value);
    } else if ("zstart".equals(key)) {
      zstart = Xml.getInt(value);
    } else if ("zend".equals(key)) {
      zend = Xml.getInt(value);
    } else if ("z".equals(key)) {
      zstart = zend = Xml.getInt(value);
    } else if ("note".equals(key)) {
      addtype = SiteMapLogic.valueOf(value);
    } else {
      super.xmlSet(key, value);
    }
  }

  @Override protected void build() {
    for (int z = zstart; z <= zend; z++) {
      SiteMap.allocateFloor(z);
      for (int x = xstart; x <= xend; x++) {
        for (int y = ystart; y <= yend; y++) {
          if (addtype == SiteMapLogic.ADD) {
            i.site.siteLevelmap()[x][y][z].flag.add(tile);
          } else if (addtype == SiteMapLogic.SUBTRACT) {
            i.site.siteLevelmap()[x][y][z].flag.remove(tile);
          } else if (tile == TileSpecial.OPEN) {
            i.site.siteLevelmap()[x][y][z].flag = EnumSet.noneOf(TileSpecial.class);
          } else {
            i.site.siteLevelmap()[x][y][z].flag = EnumSet.of(tile);
          }
        }
      }
    }
  }
}