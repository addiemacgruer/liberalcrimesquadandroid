package lcs.android.site.creation;

import static lcs.android.game.Game.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.game.Game;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

@NonNullByDefault class ConfigSiteUnique extends AbstractConfigSiteCommand {
  @Nullable private SpecialBlocks unique;

  private int xstart, xend, ystart, yend, zstart, zend;

  @Override public String toString() {
    return "ConfigSiteUnique:" + unique;
  }

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    // no action
  }

  @Override public void xmlSet(final String key, final String value) {
    if ("type".equals(key)) {
      unique = SpecialBlocks.valueOf(value);
    } else if ("xstart".equals(key)) {
      xstart = Xml.getInt(value) + SiteMap.MAPX / 2;
    } else if ("xend".equals(key)) {
      xend = Xml.getInt(value) + SiteMap.MAPX / 2;
    } else if ("ystart".equals(key)) {
      ystart = Xml.getInt(value);
    } else if ("yend".equals(key)) {
      yend = Xml.getInt(value);
    } else if ("zstart".equals(key)) {
      zstart = Xml.getInt(value);
    } else if ("zend".equals(key)) {
      zend = Xml.getInt(value);
    } else {
      super.xmlSet(key, value);
    }
  }

  @Override protected void build() {
    tidyRestrictions();
    final List<Integer> securex = new ArrayList<Integer>();
    final List<Integer> securey = new ArrayList<Integer>();
    final List<Integer> securez = new ArrayList<Integer>();
    final List<Integer> unsecurex = new ArrayList<Integer>();
    final List<Integer> unsecurey = new ArrayList<Integer>();
    final List<Integer> unsecurez = new ArrayList<Integer>();
    // Place unique
    for (int x = xstart; x <= xend; x++) {
      for (int y = ystart; y <= yend; y++) {
        for (int z = zstart; z <= zend; z++) {
          if (!(i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)
              || i.site.siteLevelmap()[x][y][z].blocked()
              || i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.EXIT) || i.site
                .siteLevelmap()[x][y][z].flag.contains(TileSpecial.OUTDOOR))
              && i.site.siteLevelmap()[x][y][z].special == null) {
            if (i.site.siteLevelmap()[x][y][z].restricted()) {
              securex.add(x);
              securey.add(y);
              securez.add(z);
            } else {
              unsecurex.add(x);
              unsecurey.add(y);
              unsecurez.add(z);
            }
          }
        }
      }
    }
    Log.d(Game.LCS, "SiteMap.ConfigSiteUnique.build:allocated (un)?secure");
    int x, y, z;
    if (!securex.isEmpty()) {
      final int choice = SiteMap.SITERNG.nextInt(securex.size());
      x = securex.get(choice);
      y = securey.get(choice);
      z = securez.get(choice);
    } else if (!unsecurex.isEmpty()) {
      final int choice = SiteMap.SITERNG.nextInt(unsecurex.size());
      x = unsecurex.get(choice);
      y = unsecurey.get(choice);
      z = unsecurez.get(choice);
    } else {
      return;
    }
    i.site.siteLevelmap()[x][y][z].special = unique;
  }

  private static void tidyRestrictions() {
    boolean acted;
    do {
      acted = false;
      for (int x = 2; x < SiteMap.MAPX - 2; x++) {
        for (int y = 2; y < SiteMap.MAPY - 2; y++) {
          for (int z = 0; z < i.topfloor; z++) {
            // MAPZ, but
            // let's try and
            // optimise.
            // Un-restrict blocks if they have neighboring
            // unrestricted blocks
            if (!i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)
                && !i.site.siteLevelmap()[x][y][z].blocked()
                && i.site.siteLevelmap()[x][y][z].restricted()) {
              if (!i.site.siteLevelmap()[x - 1][y][z].restricted()
                  && !i.site.siteLevelmap()[x - 1][y][z].blocked()
                  || !i.site.siteLevelmap()[x + 1][y][z].restricted()
                  && !i.site.siteLevelmap()[x + 1][y][z].blocked()
                  || !i.site.siteLevelmap()[x][y - 1][z].restricted()
                  && !i.site.siteLevelmap()[x][y - 1][z].blocked()
                  || !i.site.siteLevelmap()[x][y + 1][z].restricted()
                  && !i.site.siteLevelmap()[x][y + 1][z].blocked()) {
                i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.RESTRICTED);
                acted = true;
                continue;
              }
            }
            /* Un-restrict and unlock doors if they lead between two unrestricted sections. If they
             * lead between one unrestricted section and a restricted section, lock them instead. */
            else if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)
                && !i.site.siteLevelmap()[x][y][z].blocked()
                && i.site.siteLevelmap()[x][y][z].restricted()) {
              // Unrestricted on two opposite sides?
              if (!i.site.siteLevelmap()[x - 1][y][z].restricted()
                  && !i.site.siteLevelmap()[x + 1][y][z].restricted()
                  || !i.site.siteLevelmap()[x][y - 1][z].restricted()
                  && !i.site.siteLevelmap()[x][y + 1][z].restricted()) {
                // Unlock and unrestrict
                i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.LOCKED);
                i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.RESTRICTED);
                acted = true;
                continue;
              }
              // Unrestricted on at least one side and I'm not
              // locked?
              else if ((!i.site.siteLevelmap()[x - 1][y][z].restricted()
                  || !i.site.siteLevelmap()[x + 1][y][z].restricted()
                  || !i.site.siteLevelmap()[x][y - 1][z].restricted() || !i.site.siteLevelmap()[x][y + 1][z].flag
                  .contains(TileSpecial.RESTRICTED))
                  && !i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.LOCKED)) {
                // Lock doors leading to restricted areas
                i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.LOCKED);
                acted = true;
                continue;
              }
            }
          }
        }
      }
    } while (acted);
    Log.d(Game.LCS, "SiteMap.ConfigSiteUnique.build:tidied up restrictions");
  }
}