package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lcs.android.R;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.game.Game;
import lcs.android.game.NewGame;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.SecurityLevel;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.AbstractSiteType.UnnamedSite;
import lcs.android.site.type.CrackHouse;
import lcs.android.site.type.Nowhere;
import lcs.android.site.type.Warehouse;
import lcs.android.util.Color;
import lcs.android.util.Filter;
import lcs.android.util.IBuilder;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class Location implements Serializable {
  /** @author addie */
  public static class Builder implements Configurable, IBuilder<Location> {
    private Location parent;

    private int rent;

    private CrimeSquad renting = CrimeSquad.NO_ONE;

    private boolean interrogated = NewGame.hasmaps;

    private boolean hidden;

    private String name;

    private boolean needCar;

    private AbstractSiteType type;

    @Override public Location build() {
      return new Location(this);
    }

    @Override public Configurable xmlChild(final String value) {
      if ("location".equals(value)) {
        final Location.Builder l = new Location.Builder();
        l.parent = this;
        return l;
      }
      return this;
    }

    @Override public void xmlFinishChild() {
      i.location.add(build());
    }

    @Override public void xmlSet(final String key, final String value) {
      if ("hasmaps".equals(key)) {
        interrogated = NewGame.hasmaps;
      } else if ("name".equals(key)) {
        name = Xml.getText(value);
      } else if ("sitetype".equals(key)) {
        type = AbstractSiteType.type(value.toUpperCase(Locale.ENGLISH));
      } else if ("hidden".equals(key)) {
        hidden = Xml.getBoolean(value);
      } else if ("renting".equals(key)) {
        renting = CrimeSquad.valueOf(value);
      } else if ("rent".equals(key)) {
        rent = Xml.getInt(value);
      } else if ("need_car".equals(key)) {
        needCar = Xml.getBoolean(value);
      } else {
        Log.w(Game.LCS, "Location oops:" + key + "=" + value);
      }
    }
  }

  private static class LazyInit {
    private static final Location nowhere;
    static {
      Log.i("LCS", "Lazy Init of location");
      nowhere = new Location(new Nowhere());
    }
  }

  public Location(AbstractSiteType type) {
    i.location.add(this);
    this.type = type;
  }

  /** @param builder */
  public Location(Builder builder) {
    super();
    hidden = builder.hidden;
    interrogated = builder.interrogated;
    name = builder.name;
    needCar = builder.needCar;
    parent = builder.parent;
    rent = builder.rent;
    renting = builder.renting;
    type = builder.type;
    initlocation();
  }

  private Location(Nowhere nowhere) {
    this.type = nowhere;
  }

  private int closed = 0;

  private int highSecurity;

  private LCSLocation lcs = new LCSLocation(this);

  private long mapSeed;

  private String name = "";

  private boolean needCar;

  private Location parent;

  private int rent;

  private CrimeSquad renting = CrimeSquad.NO_ONE;

  private final AbstractSiteType type;

  @Nullable private List<MapChangeRecord> changes;

  private boolean hidden;

  private boolean interrogated;

  /** pushes people into the current squad (used in a siege) */
  public void autoPromote() {
    final int partysize = i.activeSquad.size();
    if (partysize == 6) {
      return;
    }
    for (final Creature p : Filter.of(i.pool, Filter.livingIn(this))) {
      if (p.squad().getNullable() != i.activeSquad && p.alignment() == Alignment.LIBERAL) {
        i.activeSquad.add(p);
        if (i.activeSquad.size() == 6) {
          return;
        }
      }
    }
  }

  /** how much rent is due each month. nb. only sites which are rented to the LCS (ie.
   * renting=CrimeSquads.LCS) should be counted */
  public int basicRent() {
    return type.rent;
  }

  /** returns the list of changes to the current site. nb. this is kept as null internally if empty
   * in order to speed serialization; a new list is created if empty and requested. empty
   * changelists are purged during serialization
   * @return the list of site changes */
  public List<MapChangeRecord> changes() {
    if (changes == null) {
      changes = new ArrayList<MapChangeRecord>();
    }
    assert changes != null;
    return changes;
  }

  public boolean closed() {
    return closed != 0;
  }

  public Location closed(final int days) {
    closed = days;
    return this;
  }

  public void criminalizeLiberalsInLocation(final Crime crime) {
    for (final Creature p : Filter.of(i.pool, Filter.livingIn(this))) {
      p.crime().criminalize(crime);
    }
  }

  public void decreaseCloseTimer() {
    closed -= 1;
    if (closed > 0) {
      return;
    }
    updateClosedLocation();
    closed = 0;
  }

  /** siege - checks how many days of food left at the site. returns -1 if no-one at site */
  public int foodDaysLeft() {
    final int eaters = numberEating();
    if (eaters == 0) {
      return -1;
    }
    int days = lcs.compoundStores / eaters;
    if (lcs.compoundStores % eaters > eaters / 2) {
      days++;
    }
    return days;
  }

  public BusinessFronts frontBusiness() {
    return lcs.frontBusiness;
  }

  public String frontName() {
    return lcs.frontName;
  }

  /** returns the protection against heat as a percentage from (0 to 95) % */
  public int heatProtection() {
    int heatProtection = 0;
    if (lcs.frontBusiness != null) {
      heatProtection = 12;
    } else {
      heatProtection = type.heatProtection;
    }
    if (i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE && lcs.haveFlag) {
      heatProtection += 6;
    } else if (i.issue(Issue.FLAGBURNING).law() != Alignment.ARCHCONSERVATIVE && lcs.haveFlag) {
      heatProtection += 2;
    } else if (i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE && !lcs.haveFlag) {
      heatProtection -= 2;
    }
    final int numpres = Filter.of(i.pool, Filter.livingIn(this)).size();
    if (numpres > 60) {
      heatProtection -= 20;
    }
    if (numpres > 40) {
      heatProtection -= 12;
    }
    if (numpres > 20) {
      heatProtection -= 6;
    }
    if (numpres < 10) {
      heatProtection += 1;
    }
    if (numpres < 4) {
      heatProtection += 2;
    }
    if (numpres < 2) {
      heatProtection += 3;
    }
    // lcs.heat_protection *= 0.05;
    heatProtection *= 5;
    // range of (0 to 95) %
    heatProtection = Math.max(heatProtection, 0);
    heatProtection = Math.min(heatProtection, 95);
    return heatProtection;
  }

  /** Set whether the location is hidden
   * @param hidden new setting
   * @return this */
  public Location hidden(final boolean hidden) {
    this.hidden = hidden;
    return this;
  }

  public int highSecurity() {
    return highSecurity;
  }

  public Location highSecurity(final int highSecurity) {
    this.highSecurity = highSecurity;
    return this;
  }

  public void initlocation() {
    lcs = new LCSLocation(this);
    closed = 0;
    interrogated = false;
    highSecurity = 0;
    mapSeed = i.rng.nextLong();
    changes().clear();
    if (!(type instanceof UnnamedSite)) {
      type.generateName(this);
    }
  }

  public Location interrogated(final boolean interrogated) {
    this.interrogated = interrogated;
    return this;
  }

  /** common - Checks if a site (typically safehouse) has a unique short name, and for business
   * fronts, if the front has a unique shortname. */
  public boolean isDuplicateLocation() {
    for (final Location l : i.location) {
      if (l == this) {
        continue;
      }
      if (l.lcs.frontBusiness != null && lcs.frontBusiness != null) {
        return true;
      }
    }
    return false;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isInterrogated() {
    return interrogated;
  }

  public LCSLocation lcs() {
    return lcs;
  }

  public long mapSeed() {
    return mapSeed;
  }

  public boolean needCar() {
    return needCar;
  }

  /** siege - checks how many people are eating at the site */
  public int numberEating() {
    int eaters = 0;
    for (final Creature p : i.pool) {
      // Not here? Not eating here!
      if (p.location().getNullable() != this) {
        continue;
      }
      // Not alive? Not eating!
      if (!p.health().alive()) {
        continue;
      }
      // Don't count Conservatives as eaters. Just assume they get fed
      // scraps or something.
      if (p.alignment() == Alignment.CONSERVATIVE) {
        continue;
      }
      // You're a sleeper agent? Sleepers don't eat! Feh! (Rather, they
      // eat on Conservatives' expense, not yours.)
      if (p.hasFlag(CreatureFlag.SLEEPER)) {
        continue;
      }
      // None of the above? You're eating!
      eaters++;
    }
    return eaters;
  }

  public Location parent() {
    return parent;
  }

  /** Fill in the R.id.locationdate with the current squad, location and date, and the R.id.activity
   * with what they're up to. */
  public void printLocationHeader() {
    final StringBuilder str = new StringBuilder();
    if (i.activeSquad.location().exists()) {
      if (lcs.siege.siege) {
        if (lcs.siege.underAttack) {
          setColor(R.id.locationdate, Color.RED);
        } else {
          setColor(R.id.locationdate, Color.YELLOW);
        }
      } else {
        setColor(R.id.locationdate, Color.WHITE);
      }
    } else {
      if (i.currentLocation.lcs.siege.siege) {
        if (i.currentLocation.lcs.siege.underAttack) {
          setColor(R.id.locationdate, Color.RED);
        } else {
          setColor(R.id.locationdate, Color.YELLOW);
        }
      } else {
        setColor(R.id.locationdate, Color.WHITE);
      }
    }
    if (i.activeSquad.location().exists()) {
      addlocationname(str);
      str.append(", ");
      // } else if (i.currentLocation == null) {
      // str.append("No Squad Selected, ");
    } else {
      i.currentLocation.addlocationname(str);
      str.append(", ");
    }
    str.append(i.score.date.longString());
    setText(R.id.locationdate, str.toString());
    setText(R.id.money, R.string.moneyString, i.ledger.funds());
    setText(R.id.activity, i.activeSquad.squadActivity());
  }

  public int rent() {
    return rent;
  }

  public Location rent(final int rent) {
    this.rent = rent;
    return this;
  }

  public CrimeSquad renting() {
    return renting;
  }

  public Location renting(final CrimeSquad renting) {
    this.renting = renting;
    return this;
  }

  public Location setName(final String name) {
    this.name = name;
    return this;
  }

  /** siege - "you are wanted for _______ and other crimes..." */
  public void stateBrokenLaws() {
    final Set<Crime> crimesCommitted = new HashSet<Crime>();
    Creature kidnappee = null;
    int kidnapped = 0;
    for (final Creature p : Filter.of(i.pool, Filter.livingIn(this))) {
      if (p.hasFlag(CreatureFlag.KIDNAPPED)) {
        kidnappee = p;
        kidnapped++;
      }
      if (kidnapped > 0) {
        continue; /* don't need to check up on other crimes */
      }
      for (final Crime lf : Crime.values()) {
        if (p.crime().crimesSuspected(lf) != 0) {
          crimesCommitted.add(lf);
        }
      }
    }
    setView(R.layout.generic);
    ui().text(
        lcs.siege.underAttack ? "You hear shouts:" : "You hear a blaring voice on a loudspeaker:")
        .add();
    if (lcs.siege.escalationState >= 2 && Politics.publicmood() < 20) {
      ui().text("In the name of God, your campaign of terror ends here!").add();
    } else {
      ui().text("Surrender yourselves!").add();
    }
    if (kidnapped > 0) {
      ui().text("Release " + kidnappee + (kidnapped > 1 ? " and the others" : "") + " unharmed!")
          .add();
    } else { /* state the most serious */
      for (final Crime lf : Crime.PRIORITY) {
        if (crimesCommitted.contains(lf)) {
          ui().text(
              "You are wanted for " + lf
                  + (crimesCommitted.size() > 1 ? " and other crimes!" : "!")).add();
          break;
        }
      }
    }
    getch();
  }

  @Override public String toString() { // addlocationname
    if (lcs.frontBusiness != null) {
      return lcs.frontName;
    }
    return name;
  }

  public AbstractSiteType type() {
    return type;
  }

  protected int compoundStores() {
    return lcs.compoundStores;
  }

  protected Set<Compound> compoundWalls() {
    return lcs.compoundWalls;
  }

  protected void generateFrontName() {
    do {
      lcs.frontBusiness = i.rng.randFromArray(BusinessFronts.values());
      lcs.frontBusiness.generateName(this);
    } while (i.currentLocation.isDuplicateLocation());
  }

  protected int heat() {
    return lcs.heat;
  }

  protected void printlocation() {
    final StringBuilder str = new StringBuilder();
    Color color = null;
    if (lcs.siege.siege) {
      if (!lcs.siege.underAttack) {
        color = Color.YELLOW;
        str.append("The police have surrounded this location.");
      } else {
        color = Color.RED;
        switch (lcs.siege.siegetype) {
        case POLICE:
          str.append("The police are raiding this location!");
          break;
        case CIA:
          str.append("The CIA is raiding this location!");
          break;
        case HICKS:
          str.append("The masses are storming this location!");
          break;
        case CORPORATE:
          str.append("The Corporations are raiding this location!");
          break;
        case CCS:
          str.append("The CCS is raiding this location!");
          break;
        case FIREMEN:
          str.append("Firemen are raiding this location!");
          break;
        default:
          break;
        }
      }
    } else {
      str.append("You are not under siege...  yet.");
    }
    if (type.isType(Warehouse.class) || type.isType(CrackHouse.class)) {
      str.append('\n');
      if (numberEating() > 0) {
        if (foodDaysLeft() > 0) {
          if (foodDaysLeft() < 4) {
            str.append("This location has food for only a few days.\n");
          }
        } else {
          if (lcs.siege.siege) {
            color = Color.RED;
          }
          str.append("This location has insufficient food stores.\n");
        }
      }
      if (lcs.compoundWalls.contains(Compound.BASIC)) {
        str.append("FORTIFIED COMPOUND ");
      }
      if (lcs.compoundWalls.contains(Compound.PRINTINGPRESS)) {
        str.append("PRINTING PRESS ");
      }
      if (lcs.frontBusiness != null) {
        str.append("BUSINESS FRONT ");
      }
      if (lcs.compoundWalls.contains(Compound.CAMERAS)) {
        if (lcs.siege.siege && lcs.siege.camerasOff) {
          str.append("CAMERAS [OFF] ");
        } else {
          str.append("CAMERAS [ON] ");
        }
      }
      if (lcs.compoundWalls.contains(Compound.TRAPS)) {
        str.append("BOOBY TRAPS");
      }
      if (lcs.compoundWalls.contains(Compound.TANKTRAPS)) {
        str.append("TANK TRAPS");
      }
      if (lcs.siege.siege && lcs.siege.lightsOff) {
        str.append("LIGHTS OUT");
      } else if (lcs.compoundWalls.contains(Compound.GENERATOR)) {
        str.append("GENERATOR");
      }
      final int eaters = numberEating();
      final int days = foodDaysLeft();
      if (eaters > 0) {
        if (days >= 1) {
          str.append(days + " Day");
          if (days != 1) {
            str.append('s');
          }
          str.append(" of Food Left");
        } else if (days == 0) {
          color = Color.RED;
          str.append("Not Enough Food");
        }
      }
      str.append('\n');
      str.append(lcs.compoundStores + " Daily Ration");
      if (lcs.compoundStores != 1) {
        str.append('s');
      }
      str.append(" - " + eaters + " Eating");
    }
    if (color != null) {
      setColor(R.id.siegeStatus, color);
    }
    setText(R.id.siegeStatus, str.toString());
  }

  private void addlocationname(final StringBuilder sb) {
    if (lcs.frontBusiness != null) {
      sb.append(lcs.frontName);
    } else {
      sb.append(name);
    }
  }

  /** Refresh a location that's been closed by LCS activities. */
  private void updateClosedLocation() {
    // Clean up graffiti, patch up walls, restore fire damage
    changes().clear();
    switch (i.rng.nextInt(2)) {
    default:
    case 0:
      // If high security is supported
      if (type.securityLevel() != SecurityLevel.POOR) {
        // Throw guards everywhere
        highSecurity = 60;
      } else {
        initlocation();
      }
      break;
    case 1:
      // Remodel, invalidate maps
      initlocation();
      break;
    }
  }

  public final transient static Configurable CONFIG = new Configurable() {
    @Override public Configurable xmlChild(final String value) {
      return new Location.Builder();
    }

    @Override public void xmlFinishChild() {}

    @Override public void xmlSet(final String key, final String value) {}
  };

  private static final long serialVersionUID = Game.VERSION;

  /** @return */
  public static Location none() {
    return LazyInit.nowhere;
  }
}