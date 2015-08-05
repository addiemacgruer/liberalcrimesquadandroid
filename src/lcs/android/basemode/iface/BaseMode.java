package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.HashMap;
import java.util.Map;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Creature;
import lcs.android.daily.Daily;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.game.Visibility;
import lcs.android.items.AbstractItem;
import lcs.android.law.Crime;
import lcs.android.monthly.Monthly;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.scoring.LcsDate;
import lcs.android.site.Squad;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;
import lcs.android.util.LcsRuntimeException;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class BaseMode {
  private BaseMode() {}

  private static final String FLAG = "\n" + //
      "::::::========\n" + //
      "::::::========\n" + //
      "::::::========\n" + //
      "==============\n" + //
      "==============";

  private static long nonSightTime = 0;

  public static void mainScreen() {
    int oldForceMonth = i.score.date.month();
    do {
      Visibility.resolveVision();
      final int partySize = i.activeSquad().size();
      if (i.activeSquad().location().exists()) {
        i.currentLocation = i.activeSquad().location().get();
      } else {
        nextLocation();
      }
      final boolean sieged = i.currentLocation.lcs().siege.siege;
      final boolean underAttack = i.currentLocation.lcs().siege.underAttack;
      final boolean haveFlag = haveFlag();
      int c = ' ';
      if (Game.canSee()) {
        longTimeNoSee(nonSightTime);
        nonSightTime = 0;
        Curses.setView(R.layout.basemode);
        i.currentLocation.printLocationHeader();
        i.activeSquad().printParty();
        if (sieged) {
          updateSiegeDisplay(underAttack);
        }
        if (haveFlag) {
          setText(R.id.flag, FLAG);
        }
        Curses.setText(R.id.slogan, i.score.slogan);
        doButtons(partySize, sieged, underAttack, haveFlag);
        while (c == ' ') {
          c = getch();
        }
      } else {
        c = 'w'; // if you can't see, you wait.
        Curses.setViewIfNeeded(R.layout.calendar);
        final StringBuilder str = new StringBuilder();
        LcsDate.monthName(str, i.score.date.month());
        str.append(" " + i.score.date.day() + ", " + i.score.date.year());
        Curses.setText(R.id.date, str.toString());
        Curses.setText(R.id.calendar, i.score.date.calendar());
        if (oldForceMonth != i.score.date.month()) {
          Game.save();
          LiberalAgenda.liberalprogress();
          oldForceMonth = i.score.date.month();
        }
        Curses.pauseMs(25);
      }
      if (i.activeSquad().displaySquadInfo(c)) {
        continue;
      }
      switch (c) {
      case 'x':
      default:
        return;
      case 'i':
        BaseAction.investlocation();
        break;
      case 'l':
        LiberalAgenda.liberalagenda(Alignment.MODERATE);
        break;
      case 'c':
        if (sieged) {
          i.currentLocation.lcs().siege.giveup();
          Squad.cleanGoneSquads();
        } else {
          i.activeSquad().activity(BareActivity.noActivity());
        }
        break;
      case 'f':
        // NOTE THAT THERE ARE TWO MORE OF THESE
        // IDENTICAL LINES BELOW
        if (!sieged) {
          BaseAction.stopEvil();
        } else if (underAttack) {
          i.currentLocation.lcs().siege.escapeEngage();
          Squad.cleanGoneSquads();
        } else if (sieged) {
          i.currentLocation.lcs().siege.sallyForth();
          Squad.cleanGoneSquads();
        }
        break;
      case 'o':
        Squad.orderParty();
        break;
      case 'a':
        Activate.activate();
        break;
      case 'b':
        Activate.activateSleepers();
        break;
      case 't':
        nextSquad();
        break;
      case 'z':
        nextLocation();
        break;
      case 'e':
        i.activeSquad().highlightedMember(-1);
        if (i.activeSquad().location().exists()) {
          AbstractItem.equip(i.activeSquad().loot(), i.activeSquad().location().get());
        }
        break;
      case 'r':
        ReviewMode.review();
        break;
      case 'w':
        if (Game.canSee()) {
          Game.save();
        } else {
          nonSightTime++;
        }
        Daily.advanceday();
        if (i.score.date.isMonthOver()) {
          Monthly.passMonth();
        }
        Daily.advancelocations();
        break;
      case 'v':
        BaseAction.selectVehicles();
        break;
      case 'p':
        if (haveFlag) {
          burnFlag(sieged);
        } else {
          buyFlag();
        }
        break;
      case 's':
        BaseAction.getslogan();
        break;
      }
    } while (true);
  }

  private static void burnFlag(final boolean sieged) {
    BaseAction.burnFlag(FLAG);
    i.score.flagsBurnt++;
    i.currentLocation.lcs().haveFlag = false;
    i.currentLocation.criminalizeLiberalsInLocation(Crime.BURNFLAG);
    // PUBLICITY IF BURN FLAG DURING SIEGE
    // ESPECIALLY IF IT IS REALLY ILLEGAL
    if (sieged) {
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(1, 1, 100);
      i.issue(Issue.FREESPEECH).changeOpinion(1, 1, 30);
      switch (i.issue(Issue.FLAGBURNING).law()) {
      case MODERATE:
        i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(1, 1, 100);
        i.issue(Issue.FREESPEECH).changeOpinion(1, 1, 50);
        //$FALL-THROUGH$
      case CONSERVATIVE:
        i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(5, 1, 100);
        i.issue(Issue.FREESPEECH).changeOpinion(2, 1, 70);
        //$FALL-THROUGH$
      case ARCHCONSERVATIVE:
        i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(15, 1, 100);
        i.issue(Issue.FREESPEECH).changeOpinion(5, 1, 90);
        break;
      default:
      }
    }
  }

  private static void buyFlag() {
    i.ledger.subtractFunds(20, Ledger.ExpenseType.COMPOUND);
    i.currentLocation.lcs().haveFlag = true;
    if (i.activeSquad().base().exists()) {
      i.activeSquad().base().get().lcs().haveFlag = true;
    }
    i.score.flagsBought++;
    BaseAction.raiseFlag(FLAG);
  }

  private static boolean cannotWait() {
    final Map<Location, Integer> locationCount = new HashMap<Location, Integer>();
    for (final Location j : i.location) {
      locationCount.put(j, 0);
    }
    for (final Creature p : Filter.of(i.pool, Filter.LIBERAL)) {
      if (p.location().exists()) {
        continue; // Vacationers don't count
      }
      locationCount.put(p.location().get(), locationCount.get(p.location().get()) + 1);
    }
    for (final Location l : i.location) {
      if (!l.lcs().siege.siege) {
        continue;
      }
      if (l.lcs().siege.underAttack) {
        // Allow siege if no liberals present
        if (locationCount.get(l) != 0) {
          return true;
        }
        break;
      }
      // NOTE: returns -1 if no eaters, so is okay
      if (l.foodDaysLeft() == 0) {
        return true;
      }
    }
    return false;
  }

  private static void doButtons(final int partysize, final boolean sieged,
      final boolean underattack, final boolean haveflag) {
    i.currentLocation.printlocation();
    Curses.setEnabled(R.id.basemodeI,
        i.currentLocation.type().isInvestable() && !i.currentLocation.lcs().siege.siege);
    Curses.setEnabled(R.id.basemodeE, partysize > 0 && !underattack);
    Curses.setEnabled(R.id.basemodeV, i.vehicle.size() > 0 && partysize > 0);
    Curses.setEnabled(R.id.basemodeR, i.pool.size() > 0);
    Curses.setEnabled(R.id.basemodeOrder, partysize > 1 && !sieged);
    Curses.setText(R.id.squadname, i.activeSquad().toString());
    Curses.setEnabled(R.id.basemodeNextSquad, i.squad.size() > 1 || i.squad.size() > 0);
    Curses.setEnabled(R.id.basemodeLocation, lcsLocations() > 1);
    Curses.setEnabled(R.id.basemodeB, false);
    if (Filter.any(i.pool, Filter.AVAILABLE)) {
      Curses.setEnabled(R.id.basemodeB, true);
    }
    if (sieged) {
      setText(R.id.basemodeF, "Escape / Engage");
      setText(R.id.basemodeC, "Give up");
    } else {
      Curses.setEnabled(R.id.basemodeF, partysize > 0);
      Curses.setEnabled(R.id.basemodeC, partysize > 0
          && i.activeSquad().activity().type() != Activity.NONE);
    }
    if (cannotWait()) {
      setText(R.id.basemodeW, "Cannot Wait until Siege Resolved");
      setEnabled(R.id.basemodeW, false);
    } else if (i.score.date.day() == i.score.date.daysInMonth()) {
      setText(R.id.basemodeW, "Next Month");
    }
    if (haveflag) {
      Curses.setEnabled(R.id.basemodeP, true);
      Curses.setText(R.id.basemodeP, R.string.basemodePburn);
    } else if (i.ledger.funds() >= 20 && !sieged) {
      Curses.setEnabled(R.id.basemodeP, true);
    } else {
      Curses.setEnabled(R.id.basemodeP, false);
    }
  }

  private static boolean haveFlag() {
    boolean haveflag = true;
    haveflag = i.currentLocation.lcs().haveFlag;
    if (i.activeSquad().location().exists()) {
      haveflag = i.activeSquad().location().get().lcs().haveFlag;
    }
    return haveflag;
  }

  private static int lcsLocations() {
    return Filter.count(i.location, Filter.LCS_LOCATION);
  }

  private static void longTimeNoSee(final long nonsighttime) {
    if (nonsighttime >= 365 * 4) {
      String str;
      if (nonsighttime >= 365 * 16) {
        str = "How long since you've heard these sounds...  times have changed.";
      } else if (nonsighttime >= 365 * 8) {
        str = "It has been a long time.  A lot must have changed...";
      } else {
        str = "It sure has been a while.  Things might have changed a bit.";
      }
      Curses.fact(str);
    }
  }

  private static void nextLocation() {
    if (lcsLocations() == 0) {
      throw new LcsRuntimeException("No LCS safehouses!");
    }
    i.setActiveSquad(Squad.none());
    int sl = 0;
    sl = i.location.indexOf(i.currentLocation);
    sl = (sl + 1) % i.location.size();
    for (int l = sl + 1; l < i.location.size(); l++) {
      if (i.location.get(l).renting() == CrimeSquad.LCS) {
        i.currentLocation = i.location.get(l);
        break;
      } else if (l == i.location.size() - 1) {
        l = -1;
      }
    }
  }

  private static void nextSquad() {
    i.squad.next();
  }

  private static void updateSiegeDisplay(final boolean underattack) {
    if (underattack) {
      ui().text("Under Attack").color(Color.RED).add();
      return;
    }
    ui().text("Under Siege").color(Color.YELLOW).add();
    int stock = 1;
    stock = i.currentLocation.compoundStores();
    if (stock == 0) {
      ui().text(" (No Food)").add();
    }
  }
}
