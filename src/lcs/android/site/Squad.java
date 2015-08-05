package lcs.android.site;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.AbstractActivity;
import lcs.android.activities.BareActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.Gender;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Money;
import lcs.android.items.Vehicle;
import lcs.android.law.Crime;
import lcs.android.util.Color;
import lcs.android.util.Getter;
import lcs.android.util.Maybe;
import lcs.android.util.Setter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class Squad extends ArrayList<Creature> {
  private static class LazyInit {
    private static final Squad none;
    static {
      Log.i("LCS", "Lazy Init of Squad");
      none = new NoSquad();
    }
  }

  private AbstractActivity activity = BareActivity.noActivity();

  private final List<AbstractItem<? extends AbstractItemType>> loot = new ArrayList<AbstractItem<? extends AbstractItemType>>();

  private int highlightedMember = -1;

  private String name = i.rng.choice(possibleTeamNames);

  @Getter public AbstractActivity activity() {
    return activity;
  }

  @Setter public Squad activity(final AbstractActivity instance) {
    activity = instance;
    return this;
  }

  @Override public boolean add(@Nullable final Creature p) {
    if (p == null) {
      throw new NullPointerException("Tried to add null to Squad:" + toString());
    }
    if (contains(p)) {
      p.removeSquadInfo();
    }
    final boolean rval = super.add(p);
    if (p.squad().getNullable() != this) {
      p.squad(this);
    }
    return rval;
  }

  @Getter public Maybe<Location> base() {
    if (size() > 0) {
      return get(0).base();
    }
    return Maybe.empty();
  }

  @Setter public void base(final Location loc) {
    for (final Creature p : this) {
      p.base(loc);
    }
  }

  public void criminalizeParty(final Crime lawFlag) {
    for (final Creature p : this) {
      if (!p.health().alive()) {
        continue;
      }
      p.crime().criminalize(lawFlag);
    }
  }

  /** Tests whether the key is used for squad information, and displays that info if so. Responds to
   * 0 - 7 (display all, display member 1 to 6, details for selected) and also 'o' to order the
   * party.
   * @param c a key press
   * @return true if the request was used, false otherwise. */
  public boolean displaySquadInfo(final int c) {
    if (c == 'o') {
      orderParty();
      return true;
    }
    if (c == '0') {
      highlightedMember = -1;
      return true;
    }
    if (c >= '1' && c <= '6') {
      highlightedMember = c - '1';
      return true;
    }
    if (c == '7') {
      fullStatus(i.activeSquad().highlightedMember());
      return true;
    }
    return false;
  }

  @Getter public int highlightedMember() {
    if (highlightedMember > size()) {
      highlightedMember = -1;
    }
    return highlightedMember;
  }

  @Setter public Squad highlightedMember(final int member) {
    highlightedMember = member;
    return this;
  }

  /** common - gives juice to everyone in the active party
   * @param juice some juice
   * @param cap a maximum
   * @return this */
  public Squad juice(final int juice, final int cap) {
    for (final Creature p : this) {
      if (p != null && p.health().alive()) {
        p.addJuice(juice, cap);
      }
    }
    return this;
  }

  public Maybe<Location> location() {
    if (size() > 0) {
      return get(0).location();
    }
    return Maybe.empty();
  }

  public void location(final Location loc) {
    for (final Creature p : this) {
      p.location(loc);
      if (p.car().exists()) {
        p.car().get().setLocation(loc);
      }
    }
  }

  public List<AbstractItem<? extends AbstractItemType>> loot() {
    return loot;
  }

  public Creature member(final int p) {
    if (p >= size()) {
      throw new ArrayIndexOutOfBoundsException("Squad member " + p + " of " + size());
    }
    return get(p);
  }

  public Squad name(final String aName) {
    name = aName;
    return this;
  }

  /** Inflates the R.id.activesquad stub, and fills it with party details. */
  public void printParty() {
    if (highlightedMember != -1 && highlightedMember >= size()) {
      highlightedMember = -1;
    }
    if (highlightedMember != -1) {
      get(highlightedMember).printCreatureInfo(255);
      return;
    }
    setInflate(R.id.activesquad);
    final StringBuilder str = new StringBuilder();
    for (int p = 0; p < size(); p++) {
      str.setLength(0);
      final Creature a = get(p);
      if (a == null) {
        continue;
      }
      final int[] row;
      try {
        row = Squad.as[p];
      } catch (final ArrayIndexOutOfBoundsException aioobe) {
        throw new RuntimeException("out-of-bounds", aioobe);
      }
      if (a.prisoner().exists()) {
        setColor(row[0], Color.MAGENTA);
        setText(row[0], a.toString() + "+H");
      } else {
        setText(row[0], a.toString());
      }
      int skill = 0;
      boolean bright = false;
      for (final Skill sk : Skill.values()) {
        skill += a.skill().skill(sk);
        if (a.skill().skillXp(sk) >= 100 + 10 * a.skill().skill(sk)
            && a.skill().skill(sk) < a.skill().skillCap(sk, true)) {
          bright = true;
        }
      }
      if (bright) {
        setColor(row[1], Color.CYAN);
      }
      str.append(skill);
      str.append('/');
      Skill wsk = Skill.HANDTOHAND;
      if (a.weapon().weapon().isMusical()) {
        wsk = Skill.MUSIC;
      } else if (a.weapon().hasThrownWeapon() && a.weapon().getExtraThrowingWeapons().size() != 0) {
        wsk = a.weapon().getExtraThrowingWeapons().get(0).attack(false, false, false).skill;
      } else {
        wsk = a.weapon().weapon().attack(false, false, false).skill;
      }
      str.append(a.skill().skill(wsk));
      setText(row[1], str.toString());
      str.setLength(0);
      if (i.mode() == GameMode.SITE) {
        setColor(row[2], a.weaponCheck().color());
      }
      if (a.weapon().hasThrownWeapon() && a.weapon().getExtraThrowingWeapons().size() != 0) {
        str.append(a.weapon().getExtraThrowingWeapons().get(0).shortName());
      } else {
        str.append(a.weapon().weapon().shortName());
      }
      if (a.weapon().weapon().get_ammoamount() > 0) {
        str.append(" (");
        str.append(a.weapon().weapon().get_ammoamount());
        str.append(')');
      } else if (a.weapon().weapon().usesAmmo()) {
        if (a.weapon().countClips() != 0) {
          str.append(" (");
          str.append(a.weapon().countClips());
          str.append(')');
        } else {
          str.append(" (XX)");
        }
      } else if (a.weapon().weapon().isThrowable() && !a.weapon().hasThrownWeapon()) {
        str.append(" (1)");
      } else if (a.weapon().hasThrownWeapon() && a.weapon().getExtraThrowingWeapons().size() != 0) {
        str.append(" (");
        str.append(a.weapon().countWeapons() - (a.weapon().isArmed() ? 1 : 0));
        str.append(')');
      }
      setText(row[2], str.toString());
      if (i.mode() == GameMode.SITE) {
        setColor(row[3], a.hasDisguise().color());
      }
      setText(row[3], a.getArmor().toString());
      setText(row[4], a.health().healthStat());
      str.setLength(0);
      Vehicle v = null;
      if (Squad.showCarPrefs()) {
        v = a.prefCar();
      } else {
        v = a.car().getNullable();
      }
      if (v != null && Squad.showCarPrefs()) {
        str.append(v.shortname());
        if (a.isDriver()) {
          str.append("-D");
        }
        setText(row[5], str.toString());
      } else {
        setText(row[5], a.health().describeLegCount());
      }
    }
  }

  public boolean remove(final Creature p) {
    final boolean removed = super.remove(p);
    if (removed) {
      p.removeSquadInfo();
    }
    return removed;
  }

  /** What the squad are up to.
   * @return A description. */
  public String squadActivity() {
    String s = activity.toString();
    if (activity.type() == Activity.NONE) {
      final int count = size();
      boolean haveact = false;
      for (final Creature p : this) {
        if (p.activity().type() != Activity.NONE) {
          s = p.activity().toString();
          haveact = true;
        }
      }
      if (haveact && count > 1) {
        return "Acting Individually";
      }
    }
    return s;
  }

  public boolean testClear(final Location obase) {
    if (isEmpty()) {
      obase.lcs().loot.addAll(loot);
      loot.clear();
      return true;
    }
    return false;
  }

  @Override public String toString() {
    return name;
  }

  private static final int[][] as;

  private static final int[] as1 = { R.id.as1name, R.id.as1skill, R.id.as1weapon, R.id.as1armor,
      R.id.as1health, R.id.as1transport };

  private static final int[] as2 = { R.id.as2name, R.id.as2skill, R.id.as2weapon, R.id.as2armor,
      R.id.as2health, R.id.as2transport };

  private static final int[] as3 = { R.id.as3name, R.id.as3skill, R.id.as3weapon, R.id.as3armor,
      R.id.as3health, R.id.as3transport };

  private static final int[] as4 = { R.id.as4name, R.id.as4skill, R.id.as4weapon, R.id.as4armor,
      R.id.as4health, R.id.as4transport };

  private static final int[] as5 = { R.id.as5name, R.id.as5skill, R.id.as5weapon, R.id.as5armor,
      R.id.as5health, R.id.as5transport };

  private static final int[] as6 = { R.id.as6name, R.id.as6skill, R.id.as6weapon, R.id.as6armor,
      R.id.as6health, R.id.as6transport };

  /** stolen from TVtropes with no shame */
  private final static String[] possibleTeamNames = { "Wreckers",
      "Mayhem Attack Squad", // transformers
      "Task Force X", // suicide squad
      "Roughnecks", // starship troopers
      "Rogue Squadron", "Wraith Squadron",
      "Screaming Wookie Squadron", // Star Wars
      "Ins and Outs", "Cheesemongers",
      "Pheasant Pluckers", // Monstrous Regiment
      "Fightin' Hellfish", // Simpsons
      "GI Joes", // GI Joe
      "Dambusters", "Easy Company", "Flying Circus", "Black Sheep", "Hell on Wheels",
      "Night Witches" // real life
  };

  private static final long serialVersionUID = Game.VERSION;
  static {
    as = new int[][] { as1, as2, as3, as4, as5, as6 };
  }

  /** common - purges empty squads from existence */
  public static void cleanGoneSquads() {
    boolean hasmembers;
    for (final Iterator<Squad> ssi = i.squad.iterator(); ssi.hasNext();) {
      final Squad ss = ssi.next();
      // NUKE SQUAD IF IT IS GONE
      hasmembers = false;
      for (final Creature p : ss) {
        if (p != null) {
          /* Let's do a bit of housekeeping here And see if we can't gracefully eliminate that pesky
           * dead liberal in my squad bug */
          if (!p.health().alive()) {
            p.removeSquadInfo();
          } else {
            hasmembers = true;
          }
        }
      }
      if (!hasmembers) {
        // SQUAD LOOT WILL BE DESTROYED
        if (i.activeSquad() == ss) {
          i.squad.next();
        }
        ssi.remove();
      }
      // OTHERWISE YOU CAN TAKE ITS MONEY (and other gear)
      else {
        for (final AbstractItem<? extends AbstractItemType> l : ss.loot) {
          if (l instanceof Money) {
            final Money m = (Money) l; // cast -XML
            i.ledger.addFunds(m.getAmount(), Ledger.IncomeType.THIEVERY);
          } else // Empty i.squad inventory into base inventory
          if (ss.get(0).base().exists()) {
            ss.get(0).base().get().lcs().loot.add(l);
          }
        }
        ss.loot.clear();
      }
    }
  }

  /** @return */
  public static Squad none() {
    return LazyInit.none;
  }

  public static void orderParty() {
    setView(R.layout.generic);
    ui().text("Order Party").bold().add();
    ui().text("Choose a squad member to move to front:").add();
    do {
      int y = 'a';
      for (final Creature p : i.activeSquad()) {
        if (p != null) {
          ui(R.id.gcontrol).button(y++).text(p.toString()).add();
        }
      }
      ui(R.id.gcontrol).button(ENTER).text("Continue the struggle").add();
      final int c = getch();
      if (c == ENTER) {
        return;
      }
      clearChildren(R.id.gcontrol);
      if (c >= 'a') {
        final Creature first = i.activeSquad().get(c - 'a');
        if (first == null) {
          continue;
        }
        i.activeSquad().remove(first);
        i.activeSquad().add(0, first);
      }
    } while (true);
  }

  static void fullStatus(final int start) {
    int p = start;
    int code = ' ';
    do {
      final Creature cr = i.activeSquad().get(p);
      setView(R.layout.profile);
      cr.filloutFullStatus();
      code = ' ';
      do {
        code = getch();
      } while (code == ' ');
      if (code == '[') {
        p--;
        if (p < 0) {
          p = i.activeSquad().size() - 1;
        }
      }
      if (code == ']') {
        p++;
        if (p >= i.activeSquad().size()) {
          p = 0;
        }
      }
      if (code == 'n') {
        cr.name(query(R.string.cdNewCodeName, cr.toString()));
      }
      if (code == 'g') {
        switch (cr.genderLiberal()) {
        case MALE:
          cr.genderLiberal(Gender.NEUTRAL);
          break;
        case FEMALE:
          cr.genderLiberal(Gender.MALE);
          break;
        default:
          cr.genderLiberal(Gender.FEMALE);
          break;
        }
      }
    } while (code != 'x');
  }

  /** Driving preferences need to be shown if we're in the base and ready to go somewhere, or if
   * we're in a car chase.
   * @return whether car preferences should be shown (creature name-D for driver) */
  private static boolean showCarPrefs() {
    return Game.i.mode() == GameMode.BASE || Game.i.mode() == GameMode.CHASECAR;
  }
}