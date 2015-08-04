package lcs.android.site.creation;

import static lcs.android.game.Game.*;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault class ConfigSiteSpecial extends AbstractConfigSiteCommand {
  private int freq;

  @Nullable private SpecialBlocks special;

  private int xstart, xend, ystart, yend, zstart, zend;

  @Override public String toString() {
    return "ConfigSiteSpecial:" + special + "/" + freq;
  }

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    // no action
  }

  @Override public void xmlSet(final String key, final String value) {
    if ("type".equals(key)) {
      special = SpecialBlocks.valueOf(value);
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
    } else if ("freq".equals(key)) {
      freq = Xml.getInt(value);
    } else {
      super.xmlSet(key, value);
    }
  }

  /** uses the global random number generator, not the local one. We want eg. latte table stands to
   * move each visit */
  @Override protected void build() {
    for (int x = xstart; x <= xend; x++) {
      for (int y = ystart; y <= yend; y++) {
        for (int z = zstart; z <= zend; z++) {
          if (i.rng.chance(freq)) {
            i.site.siteLevelmap()[x][y][z].special = special;
          }
        }
      }
    }
  }
}