package lcs.android.site.type;

import static lcs.android.game.Game.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Uniform;
import lcs.android.encounters.Encounter;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.LcsRandom;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.map.CreatureDistributionLawModification;
import lcs.android.site.map.SecurityLevel;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.LcsRuntimeException;
import lcs.android.util.NoDupeNoEmptyMap;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public abstract @NonNullByDefault class AbstractSiteType implements Configurable, Serializable {
  public enum Specials {
    ARCHCONSERVATIVES {
      @Override void apply(final AbstractSiteType st, final boolean sec) {
        AbstractSiteType.adjust(st.creaturearray, CreatureType.valueOf("CCS_ARCHCONSERVATIVE"),
            i.endgameState.ordinal());
      }
    },
    CCSVIGILANTES {
      @Override void apply(final AbstractSiteType st, final boolean sec) {
        if (i.endgameState.ccsActive()) {
          AbstractSiteType.adjust(st.creaturearray, "CCS_VIGILANTE", 5);
        }
      }
    },
    RESIDENTIALENCOUNTERS {
      @Override void apply(final AbstractSiteType st, final boolean sec) {
        st.encmax = 1;
        if (i.mode() == GameMode.SITE
            && !i.site.currentTile().flag.contains(TileSpecial.RESTRICTED)) {
          st.encmax = 4;
        }
      }
    },
    RESIDENTIALUPSCALE {
      @Override void apply(final AbstractSiteType st, final boolean sec) {
        if (i.mode() == GameMode.SITE
            && !i.site.currentTile().flag.contains(TileSpecial.RESTRICTED)) {
          if (sec) {
            AbstractSiteType.adjust(st.creaturearray, "SECURITYGUARD", 100);
          } else {
            AbstractSiteType.adjust(st.creaturearray, "SECURITYGUARD", 10);
          }
          if (sec) {
            AbstractSiteType.adjust(st.creaturearray, "GUARDDOG", 50);
          }
        } else if (i.mode() == GameMode.SITE && sec) {
          // inside someone's room when security is high. Might
          // meet a
          // policeman.
          if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
              && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
            AbstractSiteType.adjust(st.creaturearray, "DEATHSQUAD", 5);
          }
          if (i.issue(Issue.POLICEBEHAVIOR).lawLTE(Alignment.CONSERVATIVE)) {
            AbstractSiteType.adjust(st.creaturearray, "GANGUNIT", 10);
          }
          AbstractSiteType.adjust(st.creaturearray, "COP", 15);
        }
      }
    };
    abstract void apply(AbstractSiteType st, boolean sec);
  }

  public abstract static class UniqueNamedSite extends AbstractSiteType {
    @Override public void generateName(final Location l) {
      do {
        uniqueName(l);
      } while (l.isDuplicateLocation());
    }

    abstract void uniqueName(Location l);

    private static final long serialVersionUID = Game.VERSION;
  }

  public abstract static class UnnamedSite extends AbstractSiteType {
    @Override public void generateName(final Location l) {
      Log.e("LCS", "Not a named site:" + this.getClass(), new IllegalArgumentException(
          "not a named site"));
    }

    private static final long serialVersionUID = Game.VERSION;
  }

  private static class ASTSerialProxy implements Serializable {
    private ASTSerialProxy(final String idname) {
      this.idname = idname;
    }

    private final String idname;

    @Override public String toString() {
      return idname;
    }

    Object readResolve() {
      return AbstractSiteType.type(idname);
    }

    private static final long serialVersionUID = Game.VERSION;
  }

  private enum Mode {
    ALARM {
      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        st.alarmarray.put(CreatureType.valueOf(key.toUpperCase(Locale.US)), Xml.getInt(value));
      }
    },
    DEFAULT {
      @Override public void finish(AbstractSiteType abstractSiteType) {
        // Log.i("LCS", "Completed SiteType:" + abstractSiteType.toString());
      }

      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        if ("security".equals(key)) {
          st.security = SecurityLevel.valueOf(value);
        } else if ("disguisesite".equals(key)) {
          st.disguisesite = Xml.getBoolean(value);
        } else if ("rent".equals(key)) {
          st.rent = Xml.getInt(value);
        } else if ("heat_protection".equals(key)) {
          st.heatProtection = Xml.getInt(value);
        } else if ("sitepattern".equals(key)) {
          st.sitepattern = Xml.getText(value);
        } else if ("topnews".equals(key)) {
          st.topNews = Issue.valueOf(Xml.getText(value).toUpperCase(Locale.ENGLISH));
        }
      }
    },
    DISTRIBUTION {
      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        st.defaultarray.put(CreatureType.valueOf(key.toUpperCase(Locale.US)), Xml.getInt(value));
      }
    },
    LOOT {
      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        if ("item".equals(key)) {
          st.loot.add(Xml.getText(value));
        } else {
          Log.w(Game.LCS, "Bad key:" + key + "=" + value);
        }
      }
    },
    SECURE {
      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        st.securearray.put(CreatureType.valueOf(key.toUpperCase(Locale.US)), Xml.getInt(value));
      }
    },
    SPECIALS {
      @Override public void set(final AbstractSiteType st, final String key, final String value) {
        if ("item".equals(key)) {
          st.specials.add(Specials.valueOf(Xml.getText(value)));
        } else {
          Log.w(Game.LCS, "Bad key:" + key + "=" + value);
        }
      }
    },
    OTHER {
      @Override public void set(AbstractSiteType st, String key, String value) {
        Log.e("LCS", "Tried to set a sub-category:" + st + ":" + key + "=" + value);
      }
    };
    public void finish(AbstractSiteType abstractSiteType) {
      // Log.i("LCS", "Completed section of " + abstractSiteType + ":" + abstractSiteType.mode);
      abstractSiteType.mode = Mode.DEFAULT;
    }

    abstract public void set(AbstractSiteType st, String key, String value);
  }

  public final Map<CreatureType, Integer> creaturearray = new HashMap<CreatureType, Integer>();

  public int heatProtection = 0;

  public final List<String> loot = new ArrayList<String>();

  public int rent = 200;

  public String sitepattern = "GENERIC_LOBBY";

  public final Uniform uniform = new Uniform();

  private int encmax = 6;

  @Nullable private Issue topNews;

  private final Map<CreatureType, Integer> alarmarray = new HashMap<CreatureType, Integer>();

  private final Map<CreatureType, Integer> defaultarray = new HashMap<CreatureType, Integer>();

  private boolean disguisesite;

  private final List<CreatureDistributionLawModification> legals = new ArrayList<CreatureDistributionLawModification>();

  private Mode mode = Mode.DEFAULT;

  private final Map<CreatureType, Integer> securearray = new HashMap<CreatureType, Integer>();

  private SecurityLevel security = SecurityLevel.POOR;

  private final List<Specials> specials = new ArrayList<Specials>();

  /** If there is an alarm on site and there are incoming respondees, how should it be presented to
   * the plater?
   * @return a string starting with a colon for appending to the header */
  public String alarmResponseString() {
    if (i.site.current().renting() == CrimeSquad.CCS) {
      return ": CCS VIGILANTIES RESPONDING";
    } else if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      return ": DEATH SQUADS RESPONDING";
    } else {
      return ": POLICE RESPONDING";
    }
  }

  /** Build a floor for this map. */
  public void allocateMap(final LcsRandom SITERNG) {
    final int dx = SITERNG.nextInt(5) * 2 + 35;
    final int dy = SITERNG.nextInt(3) * 2 + 15;
    final int rx = SiteMap.MAPX / 2 - dx / 2;
    final int ry = 3;
    generateroom(SITERNG, rx, ry, dx, dy, 0);
  }

  /** Whether you can buy guns at this location.
   * @return true if so. */
  public boolean canBuyGuns() {
    return false;
  }

  /** Whether it's possible to get a map of this site.
   * @return true if so */
  public boolean canMap() {
    return true;
  }

  /** The vehicle which chases you at this location.
   * @return a VehicleType name */
  public String carChaseCar() {
    return "VEHICLE_POLICECAR";
  }

  /** The creature who will pursue you in a car chase, at this location.
   * @return a CreatureType name */
  public String carChaseCreature() {
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      return "DEATHSQUAD";
    } else if (i.issue(Issue.POLICEBEHAVIOR).lawLTE(Alignment.CONSERVATIVE)) {
      return "GANGUNIT";
    } else {
      return "COP";
    }
  }

  /** How intently you'll be chased for your crimes at this location.
   * @param siteCrime
   * @return */
  public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 5 + 1) + 1;
  }

  public int carChaseMax() {
    return 6;
  }

  /** The CCS has other opinions about what sites might be called. * @return */
  public String ccsSiteName() {
    return getLocation().toString() + ".";
  }

  /** Gets any special blocks commonly associated with this location. (1/10 chance of appearing in
   * any square)
   * @return */
  @Nullable public SpecialBlocks commonSpecial() {
    return null;
  }

  public boolean disguisesite() {
    return disguisesite;
  }

  /** Gets any special first blocks associated with this location. (only in one location)
   * @return */
  @Nullable public SpecialBlocks firstSpecial() {
    return null;
  }

  abstract public void generateName(Location l);

  public Location getLocation() {
    final List<Location> good = new ArrayList<Location>();
    for (final Location l : i.location) {
      if (l.type() == this) {
        good.add(l);
      }
    }
    if (good.isEmpty()) {
      throw new LcsRuntimeException("No such sitetype:" + this);
    }
    return i.rng.randFromList(good);
  }

  /** Some sites need a minimum amount of graffiti.
   * @return */
  public int graffitiQuota() {
    return 0;
  }

  /** Whether there is loot to be found in restricted areas.
   * @return */
  public boolean hasLoot() {
    return true;
  }

  /** Whether there are tables at this location.
   * @return */
  public boolean hasTables() {
    return false;
  }

  public boolean isCcsSafeHouse() {
    return false;
  }

  public boolean isHospital() {
    return this instanceof IHospital;
  }

  public boolean isInvestable() {
    return this instanceof IInvestable;
  }

  public boolean isPrison() {
    return false;
  }

  public boolean isResidential() {
    return false;
  }

  /** Whether this is a restricted site.
   * @return */
  public boolean isRestricted() {
    return false;
  }

  /** Check whether a sitetype is of a specified type
   * @param aType not null
   * @return */
  public boolean isType(final Class<? extends AbstractSiteType> aType) {
    return aType.equals(this.getClass());
  }

  /** Check whether a sitetype is of a specified type
   * @param aType not null
   * @return */
  public boolean isType(final String aType) {
    if (!nameToType.containsKey(aType)) {
      Log.e("LCS", "Trying to check if type '" + aType + "', but no such SiteType");
    }
    return aType.equals(type());
  }

  /** What the LCS think of a site when writing in the Liberal Guardian.
   * @return a description string, starting with a comma (or just a period). */
  public String lcsSiteOpinion() {
    return "."; // no opinion.
  }

  public Issue[] opinionsChanged() {
    return EMPTY_VIEW;
  }

  public void prepareEncounter(final Encounter encounter, final boolean sec) {
    AbstractSiteType st = this;
    if (i.site.current().renting() == CrimeSquad.CCS) {
      st = AbstractSiteType.nameToType.get("CCSBASEGENERIC");
    } else if (defaultarray.isEmpty()) {
      st = AbstractSiteType.nameToType.get("RESIDENTIAL_SHELTER");
    }
    creaturearray.clear();
    creaturearray.putAll(st.defaultarray);
    for (final CreatureDistributionLawModification l : st.legals) {
      l.apply(this);
    }
    if (sec) {
      for (final Entry<CreatureType, Integer> e : st.securearray.entrySet()) {
        AbstractSiteType.adjust(creaturearray, e.getKey(), e.getValue());
      }
    }
    for (final AbstractSiteType.Specials s : st.specials) {
      s.apply(this, sec);
    }
    if (!alarmarray.isEmpty()) {
      st = this;
    } else {
      st = AbstractSiteType.nameToType.get("RESIDENTIAL_SHELTER");
    }
    if (i.site.postAlarmTimer() > 80) {
      for (final Entry<CreatureType, Integer> e : st.alarmarray.entrySet()) {
        AbstractSiteType.adjust(creaturearray, e.getKey(), e.getValue());
      }
      if (i.site.onFire() && i.issue(Issue.FREESPEECH).law() != Alignment.ARCHCONSERVATIVE) {
        creaturearray.put(CreatureType.valueOf("FIREFIGHTER"), 1000);
      }
    }
    if (specials.contains(AbstractSiteType.Specials.RESIDENTIALENCOUNTERS)) {
      AbstractSiteType.Specials.RESIDENTIALENCOUNTERS.apply(this, sec);
    }
    final int encnum = i.rng.nextInt(st.encmax) + 1;
    for (int k = 0; k < encnum; k++) {
      encounter.makeEncounterCreature(AbstractSiteType.getrandomcreaturetype(creaturearray));
    }
  }

  /** The extent to which events at this site make interesting news.
   * @param oldPriority basic priority of events
   * @return adjusted priority. */
  public int priority(final int oldPriority) {
    return oldPriority;
  }

  /** Some random loot item which might be found in this location.
   * @return by default, "LOOT_DIRTYSOCK". */
  public String randomLootItem() {
    return "LOOT_DIRTYSOCK";
  }

  /** Try and get the rental value of various properties.
   * @return rent in bucks
   * @throws UnsupportedOperationException if not a rentable property. */
  public int rent() {
    Log.e("LCS", "Tried to get the rental value of:" + this.getClass());
    return rent;
  }

  /** Gets any special second blocks associated with this location. (only in one location)
   * @return */
  @Nullable public SpecialBlocks secondSpecial() {
    return null;
  }

  /** daily - returns true if the site type supports high security */
  public SecurityLevel securityLevel() {
    return security;
  }

  /** Who responds to sieges?
   * @return a CreatureType */
  public String siegeUnit() {
    if (i.site.current().renting() == CrimeSquad.CCS) {
      if (i.rng.chance(12)) {
        return "CCS_ARCHCONSERVATIVE";
      } else if (i.rng.chance(11)) {
        return "CCS_MOLOTOV";
      } else if (i.rng.chance(10)) {
        return "CCS_SNIPER";
      }
      return "CCS_VIGILANTE";
    }
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      return "DEATHSQUAD";
    }
    return "SWAT";
  }

  public @Nullable Issue topNews() {
    return topNews;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString() */
  @Override public String toString() {
    return this.getClass().getSimpleName();
  }

  @Override public Configurable xmlChild(final String value) {
    if ("alarm".equals(value)) {
      mode = Mode.ALARM;
    } else if ("distribution".equals(value)) {
      mode = Mode.DISTRIBUTION;
    } else if ("special".equals(value)) {
      mode = Mode.SPECIALS;
    } else if ("secure".equals(value)) {
      mode = Mode.SECURE;
    } else if ("legal".equals(value)) {
      final CreatureDistributionLawModification l = new CreatureDistributionLawModification(this);
      legals.add(l);
      mode = Mode.OTHER;
      return l;
    } else if ("loot".equals(value)) {
      mode = Mode.LOOT;
    } else if ("uniform".equals(value)) {
      mode = Mode.OTHER;
      return uniform;
    } else {
      Log.w(Game.LCS, "No such sitetype mode:" + value + " (" + this + ")");
    }
    return this;
  }

  @Override public void xmlFinishChild() {
    mode.finish(this);
  }

  @Override public void xmlSet(final String key, final String value) {
    mode.set(this, key, value);
  }

  Object writeReplace() {
    return new ASTSerialProxy(type());
  }

  /** The sitetype.
   * @return */
  private String type() {
    return Xml.getName(this.getClass());
  }

  public final static Configurable CONFIG = new Configurable() {
    @Override public Configurable xmlChild(final String value) {
      // Log.i("LCS", "Looking for child called:" + value);
      return nameToType.get(value.toUpperCase(Locale.ENGLISH));
    }

    @Override public void xmlFinishChild() {}

    @Override public void xmlSet(final String key, final String value) {
      Log.e(Game.LCS, "Tried to set ST configureable");
    }
  };

  private static final Issue[] EMPTY_VIEW = {};

  private static final Map<String, AbstractSiteType> nameToType = new NoDupeNoEmptyMap<String, AbstractSiteType>(
      new HashMap<String, AbstractSiteType>());

  private static final long serialVersionUID = Game.VERSION;
  static {
    final AbstractSiteType[] ALL_TYPES = { new AmRadio(), new Apartment(), new ApartmentUpscale(),
        new ArmsDealer(), new ArmyBase(), new BarAndGrill(), new BombShelter(), new Bunker(),
        new CableNews(), new CarDealership(), new CcsBaseGeneric(), new Church(), new CigarBar(),
        new Clinic(), new Commercial(), new Cosmetics(), new CourtHouse(), new CrackHouse(),
        new DepartmentStore(), new Downtown(), new FireStation(), new Genetic(),
        new HalloweenStore(), new Headquarters(), new House(), new Industrial(),
        new IntelligenceHq(), new InternetCafe(), new JuiceBar(), new LatteStand(), new Nuclear(),
        new OutOfTown(), new PawnShop(), new PoliceStation(), new Polluter(), new Prison(),
        new PublicPark(), new Shelter(), new SweatShop(), new Tenement(), new UDistrict(),
        new University(), new VeganCoop(), new Warehouse() };
    for (final AbstractSiteType st : ALL_TYPES) {
      nameToType.put(st.type(), st);
    }
  }

  public final static void adjust(final Map<CreatureType, Integer> map, final CreatureType ct,
      final int value) {
    if (map.containsKey(ct)) {
      map.put(ct, map.get(ct) + Math.abs(value));
    } else {
      map.put(ct, Math.abs(value));
    }
  }

  /** Return the SiteType concrete instance given a @NonNullByDefault class
   * @param aClass the @NonNullByDefault class of a site
   * @return an existing SiteType instance
   * @throws NullPointerException if the class does not have a concrete instance */
  public static AbstractSiteType type(final Class<? extends AbstractSiteType> aClass) {
    return nameToType.get(Xml.getName(aClass));
  }

  /** Return the site type for a given name.
   * @param aSiteType non-null
   * @return the site type
   * @throws IllegalArgumentException if site type does not exist */
  public static AbstractSiteType type(final String aSiteType) {
    return nameToType.get(aSiteType);
  }

  /* recursive dungeon-generating algorithm */
  static void generateroom(final LcsRandom SITERNG, final int rx, final int ry, final int dx,
      final int dy, final int z) {
    for (int x = rx; x < rx + dx; x++) {
      for (int y = ry; y < ry + dy; y++) {
        i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.BLOCK);
      }
    }
    if ((dx <= 3 || dy <= 3) && SITERNG.likely(2)) {
      return;
    }
    if (dx <= 2 && dy <= 2) {
      return;
    }
    // LAY DOWN WALL AND ITERATE
    if ((SITERNG.likely(2) || dy <= 2) && dx > 2) {
      final int wx = rx + SITERNG.nextInt(dx - 2) + 1;
      for (int wy = 0; wy < dy; wy++) {
        i.site.siteLevelmap()[wx][ry + wy][z].flag.add(TileSpecial.BLOCK);
      }
      final int rny = SITERNG.nextInt(dy);
      i.site.siteLevelmap()[wx][ry + rny][z].flag.remove(TileSpecial.BLOCK);
      i.site.siteLevelmap()[wx][ry + rny][z].flag.add(TileSpecial.DOOR);
      if (SITERNG.likely(3)) {
        i.site.siteLevelmap()[wx][ry + rny][z].flag.add(TileSpecial.LOCKED);
      }
      generateroom(SITERNG, rx, ry, wx - rx, dy, z);
      generateroom(SITERNG, wx + 1, ry, rx + dx - wx - 1, dy, z);
    } else {
      final int wy = ry + SITERNG.nextInt(dy - 2) + 1;
      for (int wx = 0; wx < dx; wx++) {
        i.site.siteLevelmap()[rx + wx][wy][z].flag.add(TileSpecial.BLOCK);
      }
      final int rnx = SITERNG.nextInt(dx);
      i.site.siteLevelmap()[rx + rnx][wy][z].flag.remove(TileSpecial.BLOCK);
      i.site.siteLevelmap()[rx + rnx][wy][z].flag.add(TileSpecial.DOOR);
      if (SITERNG.likely(3)) {
        i.site.siteLevelmap()[rx + rnx][wy][z].flag.add(TileSpecial.LOCKED);
      }
      generateroom(SITERNG, rx, ry, dx, wy - ry, z);
      generateroom(SITERNG, rx, wy + 1, dx, ry + dy - wy - 1, z);
    }
  }

  private static void adjust(final Map<CreatureType, Integer> map, final String s, final int value) {
    adjust(map, CreatureType.valueOf(s), value);
  }

  /** rolls up a random creature type according to the passed weighted map
   * @param cr a map containing the weights of various creaturetypes.
   * @return a weighted choice from the array, or null if the map was empty. */
  private static CreatureType getrandomcreaturetype(final Map<CreatureType, Integer> cr) {
    int sum = 0;
    for (final Entry<CreatureType, Integer> e : cr.entrySet()) {
      sum += e.getValue();
    }
    if (sum > 0) {
      int roll = i.rng.nextInt(sum);
      for (final Entry<CreatureType, Integer> e : cr.entrySet()) {
        roll -= e.getValue();
        if (roll < 0) {
          return e.getKey();
        }
      }
    }
    throw new AssertionError("Didn't manage to select a CreatureType from map");
  }
}