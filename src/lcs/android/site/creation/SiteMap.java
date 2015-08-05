package lcs.android.site.creation;

import static lcs.android.game.Game.*;

import java.util.EnumSet;
import java.util.Set;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.encounters.EmptyEncounter;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.MapTile;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.TileSpecial;
import lcs.android.site.type.Cosmetics;
import lcs.android.site.type.CrackHouse;
import lcs.android.site.type.Tenement;
import lcs.android.util.LcsRuntimeException;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class SiteMap {
  private static enum Side {
    BOTTOM,
    LEFT,
    RIGHT,
    TOP
  }

  private SiteMap() {}

  public static final Configurable CONFIG = new Configurable() {
    @Override public Configurable xmlChild(final String value) {
      if ("sitemap".equals(value)) {
        final ConfigSiteMap csm = new ConfigSiteMap();
        return csm;
      }
      return Xml.UNCONFIGURABLE;
    }

    @Override public void xmlFinishChild() {
      // no action
    }

    @Override public void xmlSet(final String key, final String value) {
      // no action
    }
  };

  public final static int MAPX = 70;

  public final static int MAPY = 23;

  public final static int MAPZ = 10;

  protected static final LcsRandom SITERNG = new LcsRandom();

  private static final Set<Side> COMPLETELYBLOCKED = EnumSet.allOf(Side.class);

  /* re-create site from seed before squad arrives */
  public static void initsite(final Location loc) {
    // PREP
    i.currentEncounter(new EmptyEncounter());
    for (final Creature p : i.activeSquad()) {
      p.forceIncapacitated(false);
    }
    i.groundLoot().clear();
    // MAKE MAP
    // int oldseed = seed;
    // seed = loc.mapseed;
    SITERNG.setSeed(loc.mapSeed());
    i.site.siteLevelmap(new MapTile[MAPX][MAPY][MAPZ]);
    i.topfloor = -1;
    // allocateFloor(0);
    buildSite(loc.type().sitepattern);
    // CLEAR AWAY BLOCKED DOORWAYS
    clearBlockedDoors();
    Log.d(Game.LCS, "SiteMap.initSite cleared blocked doors.");
    // DELETE NON-DOORS
    deleteNonDoors();
    Log.d(Game.LCS, "SiteMap.initSite cleared non doors.");
    if (!false) // SAV - Did I mention we have some more things
    // to
    // do?
    {
      Log.d(Game.LCS, "SiteMap.initSite more to do....");
      // ADD RESTRICTIONS
      // boolean restricted = false;
      if (loc.type().isRestricted()) {
        for (int x = 2; x < MAPX - 2; x++) {
          for (int y = 2; y < MAPY - 2; y++) {
            for (int z = 0; z < i.topfloor; z++) {
              i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.RESTRICTED);
            }
          }
        }
      }
      // ADD ACCESSORIES
      // seed = oldseed;
      for (int x = 2; x < MAPX - 2; x++) {
        for (int y = 2; y < MAPY - 2; y++) {
          for (int z = 0; z < i.topfloor; z++) {
            // don't add loot before we've sorted out restricted
            // areas
            /* this lot uses i.rng, so the location varies on each visit */
            if (loc.type().commonSpecial() != null
                && !i.site.siteLevelmap()[x][y][0].flag.contains(TileSpecial.DOOR)
                && !i.site.siteLevelmap()[x][y][0].blocked()
                && !i.site.siteLevelmap()[x][y][0].flag.contains(TileSpecial.LOOT)
                && i.site.siteLevelmap()[x][y][0].restricted()
                && loc.type().isType(Cosmetics.class) && i.rng.chance(10)) {
              i.site.siteLevelmap()[x][y][z].special = loc.type().commonSpecial();
            } else if (i.site.siteLevelmap()[x][y][0].flag.isEmpty() && (loc.type().hasTables())
                && i.rng.chance(10)) {
              i.site.siteLevelmap()[x][y][z].special = SpecialBlocks.RESTAURANT_TABLE;
            }
          }
        }
      }
      final int freez = addFirstSpecial(loc);
      if (loc.type().secondSpecial() != null) {
        addSecondSpecial(loc, freez);
      }
    }
    // SAV - End more old map stuff.
    Log.d(Game.LCS, "SiteMap.initSite more done.");
    // Clear out restrictions
    clearRestrictions();
    Log.d(Game.LCS, "SiteMap.initSite clear restrictions.");
    // ADD LOOT
    addLoot(loc);
    Log.d(Game.LCS, "SiteMap.initSite add loot.");
    /******************************************************* Add semi-permanent changes inflicted by LCS and others *******************************************************/
    addGraffiti(loc);
    Log.d(Game.LCS, "SiteMap.initSite add graffiti.");
  }

  /* marks the area around the specified tile as explored */
  public static void knowmap(final int locx, final int locy, final int locz) {
    i.site.siteLevelmap()[locx][locy][locz].known();
    if (locx > 0) {
      i.site.siteLevelmap()[locx - 1][locy][locz].known();
    }
    if (locx < MAPX - 1) {
      i.site.siteLevelmap()[locx + 1][locy][locz].known();
    }
    if (locy > 0) {
      i.site.siteLevelmap()[locx][locy - 1][locz].known();
    }
    if (locy < MAPY - 1) {
      i.site.siteLevelmap()[locx][locy + 1][locz].known();
    }
    if (locx > 0
        && locy > 0
        && (!i.site.siteLevelmap()[locx - 1][locy][locz].blocked() || !i.site.siteLevelmap()[locx][locy - 1][locz]
            .blocked())) {
      i.site.siteLevelmap()[locx - 1][locy - 1][locz].known();
    }
    if (locx < MAPX - 1
        && locy > 0
        && (!i.site.siteLevelmap()[locx + 1][locy][locz].blocked() || !i.site.siteLevelmap()[locx][locy - 1][locz]
            .blocked())) {
      i.site.siteLevelmap()[locx + 1][locy - 1][locz].known();
    }
    if (locx > 0
        && locy < MAPY - 1
        && (!i.site.siteLevelmap()[locx - 1][locy][locz].blocked() || !i.site.siteLevelmap()[locx][locy + 1][locz]
            .blocked())) {
      i.site.siteLevelmap()[locx - 1][locy + 1][locz].known();
    }
    if (locx < MAPX - 1
        && locy < MAPY - 1
        && (!i.site.siteLevelmap()[locx + 1][locy][locz].blocked() || !i.site.siteLevelmap()[locx][locy + 1][locz]
            .blocked())) {
      i.site.siteLevelmap()[locx + 1][locy + 1][locz].known();
    }
  }

  protected static void allocateFloor(final int z) {
    // this is a massive hit on android: spread it out a bit and only do
    // each floor as needs be.
    if (z <= i.topfloor) {
      return;
    }
    do {
      i.topfloor++;
      Log.d(Game.LCS, "SiteMap.initSite allocating floor " + i.topfloor + " (SiteBlockSt:" + MAPX
          * MAPY + ")");
      for (int x = 0; x < MAPX; x++) {
        for (int y = 0; y < MAPY; y++) {
          i.site.siteLevelmap()[x][y][i.topfloor] = new MapTile();
        }
      }
    } while (i.topfloor < z);
  }

  protected static void buildSite(final String name) {
    Log.d(Game.LCS, "SiteMap.build_site:" + name);
    if (Game.type.sitemaps.containsKey(name)) {
      Game.type.sitemaps.get(name).build();
      return;
    }
    Log.w(Game.LCS, "SiteMap.build_site failed:" + name);
    if ("GENERIC_UNSECURE".equals(name)) {
      throw new LcsRuntimeException("SiteMap.build_site: can't even make GENERIC_UNSECURE");
    }
    buildSite("GENERIC_UNSECURE");
  }

  private static int addFirstSpecial(final Location loc) {
    int freex = 0, freey = 0;
    final int freez = 0;
    // ADD FIRST SPECIAL
    int count = 100000;
    do {
      freex = SITERNG.nextInt(MAPX - 4) + 2;
      freey = SITERNG.nextInt(MAPY - 4) + 2;
      if (freex >= MAPX / 2 - 2 && freex <= MAPX / 2 + 2) {
        freey = SITERNG.nextInt(MAPY - 6) + 4;
      }
      count--;
    } while ((i.site.siteLevelmap()[freex][freey][freez].flag.contains(TileSpecial.DOOR)
        || i.site.siteLevelmap()[freex][freey][freez].blocked()
        || i.site.siteLevelmap()[freex][freey][freez].flag.contains(TileSpecial.LOOT) || i.site
        .siteLevelmap()[freex][freey][freez].special != null) && count > 0);
    if (loc.type().firstSpecial() != null) {
      i.site.siteLevelmap()[freex][freey][freez].special = loc.type().firstSpecial();
    }
    return freez;
  }

  private static void addGraffiti(final Location loc) {
    // Some sites need a minimum amount of graffiti
    int graffitiquota = loc.type().graffitiQuota();
    for (final MapChangeRecord j : loc.changes()) {
      switch (j.flag) {
      case GRAFFITI_OTHER: // Other tags
      case GRAFFITI_CCS: // CCS tags
      case GRAFFITI: // LCS tags
        graffitiquota--;
        i.site.siteLevelmap()[j.x][j.y][j.z].flag.add(j.flag);
        break;
      case DEBRIS: // Smashed walls, ash
        i.site.siteLevelmap()[j.x][j.y][j.z].flag.remove(TileSpecial.BLOCK);
        i.site.siteLevelmap()[j.x][j.y][j.z].flag.remove(TileSpecial.DOOR);
        i.site.siteLevelmap()[j.x][j.y][j.z].flag.add(j.flag);
        break;
      default:
        i.site.siteLevelmap()[j.x][j.y][j.z].flag.add(j.flag);
        break;
      }
    }
    // If there isn't enough graffiti for this site type, add some
    while (graffitiquota > 0) {
      final int x = SITERNG.nextInt(MAPX - 2) + 1;
      final int y = SITERNG.nextInt(MAPY - 2) + 1;
      final int z = (loc.type().isType(Tenement.class)) ? SITERNG.nextInt(6) : 0;
      if (!i.site.siteLevelmap()[x][y][z].blocked()
          && (!i.site.siteLevelmap()[x][y][z].restricted() || loc.type().isType(CrackHouse.class))
          && !i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.EXIT)
          && !i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.GRAFFITI)
          && !i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.GRAFFITI)
          && !i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.GRAFFITI)
          && (i.site.siteLevelmap()[x + 1][y][z].blocked()
              || i.site.siteLevelmap()[x - 1][y][z].blocked()
              || i.site.siteLevelmap()[x][y + 1][z].blocked() || i.site.siteLevelmap()[x][y - 1][z]
              .blocked())) {
        final MapChangeRecord change = new MapChangeRecord(x, y, z, TileSpecial.GRAFFITI_OTHER);
        loc.changes().add(change);
        i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.GRAFFITI_OTHER);
        graffitiquota--;
      }
    }
  }

  private static void addLoot(final Location loc) {
    if (!loc.type().hasLoot()) {
      return;
    }
    for (int x = 2; x < MAPX - 2; x++) {
      for (int y = 2; y < MAPY - 2; y++) {
        for (int z = 0; z <= i.topfloor; z++) {
          if (!i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)
              && !i.site.siteLevelmap()[x][y][z].blocked()
              && i.site.siteLevelmap()[x][y][z].restricted() && i.rng.chance(10)) {
            // time
            i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.LOOT);
          }
        }
      }
    }
  }

  private static void addSecondSpecial(final Location loc, final int freez) {
    int freex;
    int freey;
    int count;
    count = 100000;
    // ADD SECOND SPECIAL
    do {
      freex = SITERNG.nextInt(MAPX - 4) + 2;
      freey = SITERNG.nextInt(MAPY - 4) + 2;
      if (freex >= MAPX / 2 - 2 && freex <= MAPX / 2 + 2) {
        freey = SITERNG.nextInt(MAPY - 6) + 4;
      }
      count--;
    } while ((i.site.siteLevelmap()[freex][freey][freez].flag.contains(TileSpecial.DOOR)
        || i.site.siteLevelmap()[freex][freey][freez].blocked()
        || i.site.siteLevelmap()[freex][freey][freez].flag.contains(TileSpecial.LOOT) || i.site
        .siteLevelmap()[freex][freey][freez].special != null) && count > 0);
    i.site.siteLevelmap()[freex][freey][freez].special = loc.type().secondSpecial();
  }

  private static void clearBlockedDoors() {
    Set<Side> block;
    for (int x = 0; x < MAPX; x++) {
      for (int y = 0; y < MAPY; y++) {
        for (int z = 0; z <= i.topfloor; z++) {
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)) {
            // Check what sides are blocked around the door
            block = EnumSet.allOf(Side.class);
            if (x > 0 && !i.site.siteLevelmap()[x - 1][y][z].blocked()) {
              block.remove(Side.LEFT);
            }
            if (x < MAPX - 1 && !i.site.siteLevelmap()[x + 1][y][z].blocked()) {
              block.remove(Side.RIGHT);
            }
            if (y > 0 && !i.site.siteLevelmap()[x][y - 1][z].blocked()) {
              block.remove(Side.TOP);
            }
            if (y < MAPY - 1 && !i.site.siteLevelmap()[x][y + 1][z].blocked()) {
              block.remove(Side.BOTTOM);
            }
            // Blast open everything around a totally blocked door
            // (door will later be deleted)
            if (block.equals(COMPLETELYBLOCKED)) {
              if (x > 0) {
                i.site.siteLevelmap()[x - 1][y][z].flag.remove(TileSpecial.BLOCK);
              }
              if (x < MAPX - 1) {
                i.site.siteLevelmap()[x + 1][y][z].flag.remove(TileSpecial.BLOCK);
              }
              if (y > 0) {
                i.site.siteLevelmap()[x][y - 1][z].flag.remove(TileSpecial.BLOCK);
              }
              if (y < MAPY - 1) {
                i.site.siteLevelmap()[x][y + 1][z].flag.remove(TileSpecial.BLOCK);
              }
            }
            // Open up past doors that lead to walls
            if (!block.contains(Side.TOP)) {
              if (y < MAPY - 1) {
                int y1 = y + 1;
                do {
                  i.site.siteLevelmap()[x][y1][z].flag.remove(TileSpecial.BLOCK);
                  i.site.siteLevelmap()[x][y1][z].flag.remove(TileSpecial.DOOR);
                  y1++;
                } while (!(i.site.siteLevelmap()[x + 1][y1][z].blocked() || i.site.siteLevelmap()[x + 1][y1][z].flag
                    .contains(TileSpecial.DOOR))
                    && !(i.site.siteLevelmap()[x - 1][y1][z].blocked() || i.site.siteLevelmap()[x - 1][y1][z].flag
                        .contains(TileSpecial.DOOR)));
              } else {
                i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.BLOCK);
              }
            } else if (!block.contains(Side.BOTTOM)) {
              if (y > 0) {
                int y1 = y - 1;
                do {
                  i.site.siteLevelmap()[x][y1][z].flag.remove(TileSpecial.BLOCK);
                  i.site.siteLevelmap()[x][y1][z].flag.remove(TileSpecial.DOOR);
                  y1--;
                } while (!(i.site.siteLevelmap()[x + 1][y1][z].blocked() || i.site.siteLevelmap()[x + 1][y1][z].flag
                    .contains(TileSpecial.DOOR))
                    && !(i.site.siteLevelmap()[x - 1][y1][z].blocked() || i.site.siteLevelmap()[x - 1][y1][z].flag
                        .contains(TileSpecial.DOOR)));
              } else {
                i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.BLOCK);
              }
            } else if (!block.contains(Side.LEFT)) {
              if (x < MAPX - 1) {
                int x1 = x + 1;
                do {
                  i.site.siteLevelmap()[x1][y][z].flag.remove(TileSpecial.BLOCK);
                  i.site.siteLevelmap()[x1][y][z].flag.remove(TileSpecial.DOOR);
                  x1++;
                } while (!(i.site.siteLevelmap()[x1][y + 1][z].blocked() || i.site.siteLevelmap()[x1][y + 1][z].flag
                    .contains(TileSpecial.DOOR))
                    && !(i.site.siteLevelmap()[x1][y - 1][z].blocked() || i.site.siteLevelmap()[x1][y - 1][z].flag
                        .contains(TileSpecial.DOOR)));
              } else {
                i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.BLOCK);
              }
            } else if (!block.contains(Side.RIGHT)) {
              if (x > 0) {
                int x1 = x - 1;
                do {
                  i.site.siteLevelmap()[x1][y][z].flag.remove(TileSpecial.BLOCK);
                  i.site.siteLevelmap()[x1][y][z].flag.remove(TileSpecial.DOOR);
                  x1--;
                } while (!(i.site.siteLevelmap()[x1][y + 1][z].blocked() || i.site.siteLevelmap()[x1][y + 1][z].flag
                    .contains(TileSpecial.DOOR))
                    && !(i.site.siteLevelmap()[x1][y - 1][z].blocked() || i.site.siteLevelmap()[x1][y - 1][z].flag
                        .contains(TileSpecial.DOOR)));
              } else {
                i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.BLOCK);
              }
            }
          }
        }
      }
    }
  }

  private static void clearRestrictions() {
    boolean acted;
    do {
      acted = false;
      for (int x = 2; x < MAPX - 2; x++) {
        for (int y = 2; y < MAPY - 2; y++) {
          for (int z = 0; z <= i.topfloor; z++) {
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
            // Un-restrict and unlock doors if they lead between two
            // unrestricted sections. If they lead between one
            // unrestricted section and a restricted section, lock
            // them instead.
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
  }

  private static void deleteNonDoors() {
    Set<Side> block;
    for (int x = 0; x < MAPX; x++) {
      for (int y = 0; y < MAPY; y++) {
        for (int z = 0; z <= i.topfloor; z++) {
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.DOOR)) {
            block = EnumSet.allOf(Side.class);
            if (x > 0 && !i.site.siteLevelmap()[x - 1][y][z].blocked()) {
              block.remove(Side.LEFT);
            }
            if (x < MAPX - 1 && !i.site.siteLevelmap()[x + 1][y][z].blocked()) {
              block.remove(Side.RIGHT);
            }
            if (y > 0 && !i.site.siteLevelmap()[x][y - 1][z].blocked()) {
              block.remove(Side.TOP);
            }
            if (y < MAPY - 1 && !i.site.siteLevelmap()[x][y + 1][z].blocked()) {
              block.remove(Side.BOTTOM);
            }
            if (!block.contains(Side.TOP) && !block.contains(Side.BOTTOM)) {
              continue;
            }
            if (!block.contains(Side.LEFT) && !block.contains(Side.RIGHT)) {
              continue;
            }
            i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.DOOR);
            i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.LOCKED);
          }
        }
      }
    }
  }
}
