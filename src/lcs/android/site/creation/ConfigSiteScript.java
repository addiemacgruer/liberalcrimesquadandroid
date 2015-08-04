package lcs.android.site.creation;

import static lcs.android.game.Game.*;

import java.util.EnumSet;

import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault class ConfigSiteScript extends AbstractConfigSiteCommand {
  @Nullable private SiteMapScript script;

  private int xstart, xend, ystart, yend, zstart, zend;

  @Override public String toString() {
    return "ConfigSiteScript:" + script;
  }

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    // no action
  }

  @Override public void xmlSet(final String key, final String value) {
    if ("type".equals(key)) {
      script = SiteMapScript.valueOf(value);
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
    if (script == SiteMapScript.ROOM) {
      for (int z = zstart; z <= zend; z++) {
        generateroom(xstart, ystart, xend - xstart, yend - ystart, z);
      }
    } else if (script == SiteMapScript.HALLWAY_YAXIS) {
      for (int z = zstart; z <= zend; z++) {
        generatehallwayY(xstart, ystart, xend - xstart, yend - ystart, z);
      }
    } else if (script == SiteMapScript.STAIRS) {
      generatestairs(xstart, ystart, zstart, xend - xstart, yend - ystart, zend - zstart);
    }
  }

  /* generates a hallway with rooms on either side */
  private void generatehallwayY(final int rx, final int ry, final int dx, final int dy, final int z) {
    SiteMap.allocateFloor(z);
    for (int y = ry; y < ry + dy; y++) {
      // Clear hallway
      i.site.siteLevelmap()[rx][y][z].flag.clear();
      // Every four tiles
      if (y % 4 == 0) {
        // Pick a door location for the left
        int door_y = y + SiteMap.SITERNG.nextInt(3) - 1;
        // Create the left door
        i.site.siteLevelmap()[rx - 1][door_y][z].flag.remove(TileSpecial.BLOCK);
        i.site.siteLevelmap()[rx - 1][door_y][z].flag.add(TileSpecial.DOOR);
        // Construct apartment on the left
        generateroom(rx - dx - 1, y - 1, dx, 3, z);
        // Pick a door location for the right
        door_y = y + SiteMap.SITERNG.nextInt(3) - 1;
        // Create the right door
        i.site.siteLevelmap()[rx + 1][door_y][z].flag.remove(TileSpecial.BLOCK);
        i.site.siteLevelmap()[rx + 1][door_y][z].flag.add(TileSpecial.DOOR);
        // Construct apartment on the right
        generateroom(rx + 2, y - 1, dx, 3, z);
      }
    }
  }

  /* recursive dungeon-generating algorithm */
  private void generateroom(final int rx, final int ry, final int dx, final int dy, final int z) {
    for (int x = rx; x < rx + dx; x++) {
      for (int y = ry; y < ry + dy; y++) {
        i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.BLOCK);
      }
    }
    // Chance to stop iterating for large rooms
    if ((dx <= ROOMDIMENSION + 1 || dy <= ROOMDIMENSION + 1) && dx < dy * 2 && dy < dx * 2
        && SiteMap.SITERNG.likely(2)) {
      return;
    }
    // Very likely to stop iterating for small rooms
    if (dx <= ROOMDIMENSION && dy <= ROOMDIMENSION) {
      return;
    }
    // Guaranteed to stop iterating for hallways
    if (dx <= 1 || dy <= 1) {
      return;
    }
    // LAY DOWN WALL AND ITERATE
    if ((SiteMap.SITERNG.likely(2) || dy <= ROOMDIMENSION) && dx > ROOMDIMENSION) {
      final int wx = rx + SiteMap.SITERNG.nextInt(dx - ROOMDIMENSION) + 1;
      for (int wy = 0; wy < dy; wy++) {
        i.site.siteLevelmap()[wx][ry + wy][z].flag.add(TileSpecial.BLOCK);
      }
      final int rny = SiteMap.SITERNG.nextInt(dy);
      i.site.siteLevelmap()[wx][ry + rny][z].flag.remove(TileSpecial.BLOCK);
      i.site.siteLevelmap()[wx][ry + rny][z].flag.add(TileSpecial.DOOR);
      if (SiteMap.SITERNG.likely(3)) {
        i.site.siteLevelmap()[wx][ry + rny][z].flag.add(TileSpecial.LOCKED);
      }
      generateroom(rx, ry, wx - rx, dy, z);
      generateroom(wx + 1, ry, rx + dx - wx - 1, dy, z);
    } else {
      final int wy = ry + SiteMap.SITERNG.nextInt(dy - ROOMDIMENSION) + 1;
      for (int wx = 0; wx < dx; wx++) {
        i.site.siteLevelmap()[rx + wx][wy][z].flag.add(TileSpecial.BLOCK);
      }
      final int rnx = SiteMap.SITERNG.nextInt(dx);
      i.site.siteLevelmap()[rx + rnx][wy][z].flag.remove(TileSpecial.BLOCK);
      i.site.siteLevelmap()[rx + rnx][wy][z].flag.add(TileSpecial.DOOR);
      if (SiteMap.SITERNG.likely(3)) {
        i.site.siteLevelmap()[rx + rnx][wy][z].flag.add(TileSpecial.LOCKED);
      }
      generateroom(rx, ry, dx, wy - ry, z);
      generateroom(rx, wy + 1, dx, ry + dy - wy - 1, z);
    }
  }

  private static final int ROOMDIMENSION = 3;

  /* generates a stairwell, must have (dx or dy) and dz at least 1 */
  private static void generatestairs(final int rx, final int ry, final int rz, final int dx,
      final int dy, final int dz) {
    for (int z = rz; z <= rz + dz; z++) {
      // If not bottom floor, add down stairs
      if (z > rz) {
        if (z % 2 == 0) // Causes stairwell to swap sides every
        // other
        // floor
        {
          i.site.siteLevelmap()[rx][ry][z].flag = EnumSet.of(TileSpecial.RESTRICTED);
          i.site.siteLevelmap()[rx][ry][z].special = SpecialBlocks.STAIRS_DOWN;
        } else {
          // Purge all tiles other than restriction, add stairs
          i.site.siteLevelmap()[rx + dx][ry + dy][z].flag = EnumSet.of(TileSpecial.RESTRICTED);
          i.site.siteLevelmap()[rx + dx][ry + dy][z].special = SpecialBlocks.STAIRS_DOWN;
        }
      }
      // If not top floor, add up stairs
      if (z < rz + dz) {
        if (z % 2 == 0) {
          // Purge all tiles other than restriction, add stairs
          i.site.siteLevelmap()[rx + dx][ry + dy][z].flag = EnumSet.of(TileSpecial.RESTRICTED);
          i.site.siteLevelmap()[rx + dx][ry + dy][z].special = SpecialBlocks.STAIRS_UP;
        } else {
          i.site.siteLevelmap()[rx][ry][z].flag = EnumSet.of(TileSpecial.RESTRICTED);
          i.site.siteLevelmap()[rx][ry][z].special = SpecialBlocks.STAIRS_UP;
        }
      }
    }
  }
}