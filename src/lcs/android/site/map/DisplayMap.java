package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Set;

import lcs.android.R;
import lcs.android.site.SiegeUnitType;
import lcs.android.site.creation.SiteMap;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Code for displaying the site.
 * @author addie */
public @NonNullByDefault class DisplayMap {
  private static class Wall {
    private static final int DOWN = 1;

    private static final int LEFT = 2;

    private static final int RIGHT = 3;

    private static final int UP = 0;
  }

  private static final String down = "\u2193";

  private static final String empty = ".....";

  private static final String grassy = "'''''";

  private static final String restricted = "+++++";

  private static final int[][] row;

  private static final int[] row1 = { R.id.sba, R.id.sbb, R.id.sbc, R.id.sbd, R.id.sbe };

  private static final int[] row2 = { R.id.sbf, R.id.sbg, R.id.sbh, R.id.sbi, R.id.sbj };

  private static final int[] row3 = { R.id.sbk, R.id.sbl, R.id.sbm, R.id.sbn, R.id.sbo };

  private static final int[] row4 = { R.id.sbp, R.id.sbq, R.id.sbr, R.id.sbs, R.id.sbt };

  private static final int[] row5 = { R.id.sbu, R.id.sbv, R.id.sbw, R.id.sbx, R.id.sby };

  private static Color squarecolor = Color.BLACK;

  private static final String up = "\u2191";

  private static final String xxx = "?????\n?????\n?????";
  static {
    row = new int[][] { row1, row2, row3, row4, row5 };
  }

  /** Displays the sitemap, centered on a given location
   * @param locx x-location
   * @param locy y-location
   * @param locz z-location */
  public static void printSiteMap(final int locx, final int locy, final int locz) {
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        setText(row[y][x], printblock(locx + x - 2, locy + y - 2, locz));
        setColor(row[y][x], squarecolor);
      }
    }
  }

  private static String graffiti(final Set<TileSpecial> sb) {
    if (sb.contains(TileSpecial.GRAFFITI_OTHER)) {
      return "GNG";
    } else if (sb.contains(TileSpecial.GRAFFITI_CCS)) {
      return "CCS";
    } else if (sb.contains(TileSpecial.GRAFFITI)) {
      return "LCS";
    }
    return "###";
  }

  private static boolean lineOfSight(final int x, final int y, final int z) {
    if (x < 0 || y < 0 || z < 0 || x >= i.site.siteLevelmap().length
        || y >= i.site.siteLevelmap()[0].length || z >= i.site.siteLevelmap()[0][0].length) {
      return false;
    }
    if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.KNOWN)) {
      return true;
    }
    int x1, x2;
    int y1, y2;
    if (Math.abs(x - i.site.locx) <= 1 && Math.abs(y - i.site.locy) <= 1) {
      return true;
    }
    if (Math.abs(x - i.site.locx) == 1) {
      x1 = i.site.locx;
      x2 = x;
    } else {
      x1 = x2 = (x + i.site.locx) / 2;
    }
    if (Math.abs(y - i.site.locy) == 1) {
      y1 = i.site.locy;
      y2 = y;
    } else {
      y1 = y2 = (y + i.site.locy) / 2;
    }
    // Check for obstructions
    if (i.site.siteLevelmap()[x1][y2][z].flag.contains(TileSpecial.BLOCK)
        || i.site.siteLevelmap()[x1][y2][z].flag.contains(TileSpecial.DOOR)
        || i.site.siteLevelmap()[x1][y2][z].flag.contains(TileSpecial.EXIT)) {
      if (i.site.siteLevelmap()[x2][y1][z].flag.contains(TileSpecial.BLOCK)
          || i.site.siteLevelmap()[x2][y1][z].flag.contains(TileSpecial.DOOR)
          || i.site.siteLevelmap()[x2][y1][z].flag.contains(TileSpecial.EXIT)) {
        return false; // Blocked on some axis
      }
    }
    return true;
  }

  private static String overstrike(final String under, final String over) {
    switch (over.length()) {
    case 5:
      return over;
    case 4:
      return under.charAt(0) + over;
    case 3:
      return under.charAt(0) + over + under.charAt(4);
    case 2:
      return under.charAt(0) + over + under.substring(3);
    case 1:
      return under.substring(0, 2) + over + under.substring(3);
    case 0:
    default:
      return under;
    }
  }

  private static String printblock(final int x, final int y, final int z) {
    if (x < 0 || x >= SiteMap.MAPX - 1 || y < 0 || y >= SiteMap.MAPY - 1) {
      squarecolor = Color.INVISIBLE;
      return xxx;
    }
    if (!lineOfSight(x, y, z)) {
      squarecolor = Color.INVISIBLE;
      return xxx;
    }
    String base;
    squarecolor = Color.BLACKBLACK;
    final MapTile sq = i.site.siteLevelmap()[x][y][z];
    sq.flag.add(TileSpecial.KNOWN);
    if (sq.flag.contains(TileSpecial.BLOCK)) {
      return printwall(x, y, z);
    }
    if (sq.flag.contains(TileSpecial.DOOR)) {
      return printdoor(x, y, z);
    }
    if (sq.flag.contains(TileSpecial.RESTRICTED)) {
      base = restricted;
    } else if (sq.flag.contains(TileSpecial.OUTDOOR)) {
      base = grassy;
      squarecolor = Color.GREEN;
    } else {
      base = empty;
      squarecolor = Color.BLACKBLACK;
    }
    String a = base, b = base, c = base;
    if (sq.flag.contains(TileSpecial.EXIT)) {
      b = overstrike(b, "EXT");
    } else if (sq.flag.contains(TileSpecial.LOOT)) {
      /* doors are already handled, above */
      squarecolor = Color.MAGENTA;
      a = overstrike(a, "~$~");
    }
    if (sq.siegeFlag.contains(SiegeUnitType.TRAP)) {
      squarecolor = Color.YELLOW;
      b = overstrike(b, "TRAP!");
    } else if (sq.siegeFlag.contains(SiegeUnitType.UNIT_DAMAGED)) {
      squarecolor = Color.RED;
      b = overstrike(b, "enemy");
    } else if (sq.special != null) {
      squarecolor = Color.YELLOW;
      switch (sq.special) {
      case LAB_COSMETICS_CAGEDANIMALS:
        a = overstrike(a, "CAGES");
        break;
      case NUCLEAR_ONOFF:
        a = overstrike(a, "POWER");
        break;
      case LAB_GENETIC_CAGEDANIMALS:
        a = overstrike(a, "CAGES");
        break;
      case POLICESTATION_LOCKUP:
        a = overstrike(a, "CELLS");
        break;
      case COURTHOUSE_LOCKUP:
        a = overstrike(a, "CELLS");
        break;
      case COURTHOUSE_JURYROOM:
        a = overstrike(a, "JURY!");
        break;
      case PRISON_CONTROL:
        a = overstrike(a, "CTROL");
        break;
      case INTEL_SUPERCOMPUTER:
        a = overstrike(a, "INTEL");
        break;
      case SWEATSHOP_EQUIPMENT:
        a = overstrike(a, "EQUIP");
        break;
      case POLLUTER_EQUIPMENT:
        a = overstrike(a, "EQUIP");
        break;
      case HOUSE_PHOTOS:
        a = overstrike(a, "SAFE!");
        break;
      case ARMYBASE_ARMORY:
        a = overstrike(a, "ARMRY");
        break;
      case HOUSE_CEO:
        a = overstrike(a, "CEO");
        break;
      case CORPORATE_FILES:
        a = overstrike(a, "SAFE!");
        break;
      case RADIO_BROADCASTSTUDIO:
        a = overstrike(a, "MIC");
        break;
      case NEWS_BROADCASTSTUDIO:
        a = overstrike(a, "STAGE");
        break;
      case APARTMENT_LANDLORD:
        a = overstrike(a, "RENT?");
        break;
      case APARTMENT_SIGN:
        a = overstrike(a, "SIGN!");
        break;
      case STAIRS_UP:
        a = overstrike(a, "UP" + up);
        break;
      case STAIRS_DOWN:
        a = overstrike(a, "DN" + down);
        break;
      case RESTAURANT_TABLE:
        a = overstrike(a, "TABLE");
        break;
      case CAFE_COMPUTER:
        a = overstrike(a, "CPU");
        break;
      case PARK_BENCH:
        a = overstrike(a, "BENCH");
        break;
      default:
        break;
      }
    }
    if (sq.siegeFlag.contains(SiegeUnitType.HEAVYUNIT)) {
      squarecolor = Color.RED;
      c = overstrike(c, "ARMOR");
    } else if (sq.siegeFlag.contains(SiegeUnitType.UNIT)) {
      squarecolor = Color.RED;
      c = overstrike(c, "ENEMY");
    }
    if (!sq.encounter.isEmpty()) {
      c = "ENCTR";
    }
    if (x == i.site.locx && y == i.site.locy && z == i.site.locz) {
      squarecolor = Color.YELLOW;
      b = "SQUAD";
      if (i.groundLoot().size() > 0) {
        a = overstrike(a, "~$~");
      }
      if (!i.currentEncounter().isEmpty()) {
        i.currentEncounter().printEncounter();
      }
    }
    return a + "\n" + b + "\n" + c;
  }

  private static String printdoor(final int x, final int y, final int z) {
    final Set<TileSpecial> flag = i.site.siteLevelmap()[x][y][z].flag;
    if (flag.contains(TileSpecial.CLOCK) && flag.contains(TileSpecial.LOCKED)) {
      squarecolor = Color.RED;
    } else if (flag.contains(TileSpecial.KLOCK) && flag.contains(TileSpecial.LOCKED)) {
      squarecolor = Color.BLACKBLACK;
    } else {
      squarecolor = Color.YELLOW;
    }
    if (i.site.siteLevelmap()[x - 1][y][z].flag.contains(TileSpecial.BLOCK)) {
      return "     \n-----\n     ";
    }
    return "  |  \n  |  \n  |  ";
  }

  private static String printwall(final int x, final int y, final int z) {
    if (x == 0 || y == 0 || x >= i.site.siteLevelmap().length
        || y >= i.site.siteLevelmap()[0].length) {
      return xxx;
    }
    final boolean[] visible = { false, false, false, false };
    boolean bloody = false;
    final String[] graffiti = new String[4];
    // String[] rval = { "     ", "     ", "     " };
    // Now follows a series of checks to determine the faces of the wall
    // that should be
    // displayed. Note the order of these checks is important:
    //
    // 1) You will see the wall if it's the upward face and you're above it
    // (directional visibility)...
    // 2) ...unless your line of sight is blocked (LOS)...
    // 3) ...but line of sight and directional visibility is not important
    // if you have already seen that
    // tile (memory)...
    // 4) ...and regardless of any of the above, if there's a physical
    // obstruction that would prevent you
    // from seeing it even if you were near it, like a wall, it should not
    // be shown (blockages).
    //
    // The order of the remainder of the checks is not crucial.
    // 1) Check for directional visibility
    if (y < i.site.locy && y > 0) {
      visible[Wall.DOWN] = true;
    } else if (y > i.site.locy && y < SiteMap.MAPY) {
      visible[Wall.UP] = true;
    }
    if (x < i.site.locx && x > 0) {
      visible[Wall.RIGHT] = true;
    } else if (x > i.site.locx && x < SiteMap.MAPX) {
      visible[Wall.LEFT] = true;
    }
    // 2) Check LOS
    final Set<TileSpecial> lFlag = i.site.siteLevelmap()[x - 1][y][z].flag;
    final Set<TileSpecial> rFlag = i.site.siteLevelmap()[x + 1][y][z].flag;
    final Set<TileSpecial> uFlag = i.site.siteLevelmap()[x][y - 1][z].flag;
    final Set<TileSpecial> dFlag = i.site.siteLevelmap()[x][y + 1][z].flag;
    visible[Wall.LEFT] = lFlag.contains(TileSpecial.KNOWN) || lineOfSight(x - 1, y, z);
    visible[Wall.RIGHT] = rFlag.contains(TileSpecial.KNOWN) || lineOfSight(x + 1, y, z);
    visible[Wall.UP] = uFlag.contains(TileSpecial.KNOWN) || lineOfSight(x, y - 1, z);
    visible[Wall.DOWN] = dFlag.contains(TileSpecial.KNOWN) || lineOfSight(x, y + 1, z);
    // 4) Check for blockages
    if (lFlag.contains(TileSpecial.BLOCK)) {
      visible[Wall.LEFT] = false;
    }
    if (rFlag.contains(TileSpecial.BLOCK)) {
      visible[Wall.RIGHT] = false;
    }
    if (uFlag.contains(TileSpecial.BLOCK)) {
      visible[Wall.UP] = false;
    }
    if (dFlag.contains(TileSpecial.BLOCK)) {
      visible[Wall.DOWN] = false;
    }
    // Check for bloody walls
    if (lFlag.contains(TileSpecial.BLOODY2)) {
      bloody = true;
    }
    if (rFlag.contains(TileSpecial.BLOODY2)) {
      bloody = true;
    }
    if (uFlag.contains(TileSpecial.BLOODY2)) {
      bloody = true;
    }
    if (dFlag.contains(TileSpecial.BLOODY2)) {
      bloody = true;
    }
    if (bloody) {
      squarecolor = Color.RED;
    }
    graffiti[Wall.LEFT] = graffiti(lFlag);
    graffiti[Wall.RIGHT] = graffiti(rFlag);
    graffiti[Wall.UP] = graffiti(uFlag);
    graffiti[Wall.DOWN] = graffiti(dFlag);
    final StringBuilder rval = new StringBuilder();
    if (visible[Wall.LEFT] || visible[Wall.UP]) {
      rval.append(graffiti[Wall.LEFT].charAt(0));
    } else {
      rval.append(' ');
    }
    if (visible[Wall.UP]) {
      rval.append(graffiti[Wall.UP]);
    } else {
      rval.append("   ");
    }
    if (visible[Wall.RIGHT] || visible[Wall.UP]) {
      rval.append(graffiti[Wall.RIGHT].charAt(0));
    } else {
      rval.append(' ');
    }
    rval.append('\n');
    if (visible[Wall.LEFT]) {
      rval.append(graffiti[Wall.LEFT].charAt(1));
    } else {
      rval.append(' ');
    }
    rval.append("   ");
    if (visible[Wall.RIGHT]) {
      rval.append(graffiti[Wall.RIGHT].charAt(1));
    } else {
      rval.append(' ');
    }
    rval.append('\n');
    if (visible[Wall.LEFT] || visible[Wall.DOWN]) {
      rval.append(graffiti[Wall.LEFT].charAt(2));
    } else {
      rval.append(' ');
    }
    if (visible[Wall.DOWN]) {
      rval.append(graffiti[Wall.DOWN]);
    } else {
      rval.append("   ");
    }
    if (visible[Wall.RIGHT] || visible[Wall.DOWN]) {
      rval.append(graffiti[Wall.RIGHT].charAt(2));
    } else {
      rval.append(' ');
    }
    return rval.toString();
  }
}
