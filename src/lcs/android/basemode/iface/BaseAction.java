package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.LocationActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Creature;
import lcs.android.game.Ledger;
import lcs.android.items.Vehicle;
import lcs.android.site.Squad;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.UIElement.UIBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault class BaseAction {
  private BaseAction() {}

  protected static void burnFlag(final String image) {
    final StringBuilder str = new StringBuilder();
    str.append(image);
    int count = 209;
    do {
      final int point = i.rng.nextInt(str.length());
      switch (str.charAt(point)) {
      case ':':
      case '*':
      case '=':
        str.setCharAt(point, '#');
        break;
      case '#':
        str.setCharAt(point, '.');
        break;
      case '.':
        str.setCharAt(point, ' ');
        break;
      case '\n':
      default:
        count++; // didn't burn anything
        continue;
      }
      setText(R.id.flag, str.toString());
      pauseMs(20);
    } while (count-- > 0);
    pauseMs(1000);
  }

  protected static void getslogan() {
    i.score.slogan = Curses.query(R.string.newslogan, i.score.slogan);
  }

  protected static void investlocation() {
    do {
      Curses.setView(R.layout.generic);
      investLocationButton(2000, 'w', "Fortify the Compound for a Siege ($2000)", Compound.BASIC);
      investLocationButton(2000, 'c', "Place Security Cameras around the Compound ($2000)",
          Compound.CAMERAS);
      investLocationButton(3000, 'b', "Place Booby Traps throughout the Compound ($3000)",
          Compound.TRAPS);
      investLocationButton(3000, 't', "Ring the Compound with Tank Traps ($3000)",
          Compound.TANKTRAPS);
      investLocationButton(3000, 'g', "Buy a Generator for emergency electricity ($3000)",
          Compound.GENERATOR);
      investLocationButton(3000, 'p', "Buy a Printing Press to start your own newspaper ($3000)",
          Compound.PRINTINGPRESS);
      investLocationButton(3000, 'f', "Setup a Business Front to ward off suspicion ($3000)",
          i.currentLocation.frontBusiness() == null);
      investLocationButton(150, 'r', "Stockpile 20 daily rations of food ($150)",
          Compound.PRINTINGPRESS);
      Curses.ui().button('x').text("Done").add();
      final int c = Curses.getch();
      switch (c) {
      case 'w':
        i.ledger.subtractFunds(2000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.BASIC);
        break;
      case 'c':
        i.ledger.subtractFunds(2000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.CAMERAS);
        break;
      case 'b':
        i.ledger.subtractFunds(3000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.TRAPS);
        break;
      case 't':
        i.ledger.subtractFunds(3000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.TANKTRAPS);
        break;
      case 'g':
        i.ledger.subtractFunds(3000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.GENERATOR);
        break;
      case 'p':
        i.ledger.subtractFunds(3000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.compoundWalls().add(Compound.PRINTINGPRESS);
        break;
      case 'r':
        i.ledger.subtractFunds(150, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.lcs().compoundStores += 20;
        break;
      case 'f':
        i.ledger.subtractFunds(3000, Ledger.ExpenseType.COMPOUND);
        i.currentLocation.generateFrontName();
        break;
      case 'x':
      default:
        return;
      }
    } while (true);
  }

  protected static void raiseFlag(final String image) {
    final String[] pieces = image.split("\n");
    final StringBuilder str = new StringBuilder();
    for (int x = 0; x <= pieces.length; x++) {
      for (int y = 0; y < pieces.length - x; y++) {
        str.append('\n');
      }
      for (int y = 0; y < x; y++) {
        str.append(pieces[y] + "\n");
      }
      setText(R.id.flag, str.toString());
      pauseMs(1000);
      str.setLength(0);
    }
    pauseMs(1000);
  }

  protected static void selectVehicles() {
    final StringBuilder str = new StringBuilder();
    final List<Squad> squads = new ArrayList<Squad>();
    do {
      setView(R.layout.generic);
      ui().text("Choosing the right Liberal vehicle").add();
      ui().text("Select a car from the Liberal garage:").add();
      int y = 'a';
      for (final Vehicle v : i.vehicle) {
        squads.clear();
        str.append(v.fullname(false));
        for (final Squad sq : i.squad) {
          for (final Creature p : sq) {
            if ((p.car().getNullable() == v || p.prefCar() == v) && !squads.contains(sq)) {
              squads.add(sq);
            }
          }
        }
        if (!squads.isEmpty()) {
          str.append(" (");
          for (final Squad sq : squads) {
            str.append(sq.toString() + ", ");
          }
          str.setLength(str.length() - 2);
          str.append(')');
        }
        if (squads.isEmpty()) {
          ui(R.id.gcontrol).button(y++).text(str.toString()).add();
        } else if (squads.contains(i.activeSquad)) {
          ui(R.id.gcontrol).button(y++).text(str.toString()).color(Color.GREEN).add();
        } else {
          ui(R.id.gcontrol).button(y++).text(str.toString()).color(Color.YELLOW).add();
        }
        str.setLength(0);
      }
      ui(R.id.gcontrol).button(10).text("Continue the struggle").add();
      int c = getch();
      if (c == 10) {
        return;
      }
      final Vehicle v = i.vehicle.get(c - 'a');
      do {
        setView(R.layout.generic);
        ui(R.id.gcontrol).text("Select passengers for the " + v.fullname(false)).add();
        v.displayStats(R.id.gcontrol);
        y = 'a';
        boolean driver = false;
        for (final Creature p : i.activeSquad) {
          if (p.prefCar() == v && p.prefIsDriver()) {
            ui(R.id.gcontrol).button(y++).text(p.toString() + " (driver)").color(Color.GREEN).add();
            driver = true;
          } else if (p.prefCar() == v) {
            ui(R.id.gcontrol).button(y++).text(p.toString() + " (passenger)").color(Color.YELLOW)
                .add();
          } else if (p.prefCar() != v) {
            ui(R.id.gcontrol).button(y++)
                .text(p.toString() + " (" + p.prefCar().fullname(false) + ")").add();
          } else {
            ui(R.id.gcontrol).button(y++).text(p.toString()).add();
          }
        }
        ui(R.id.gcontrol).button(10).text("Continue the struggle").add();
        c = getch();
        if (c == 10) {
          break;
        }
        if (c >= '1') {
          final Creature liberal = i.activeSquad.member(c - 'a');
          if (liberal.prefCar() == v) {
            liberal.prefCar(null);
            if (liberal.prefIsDriver()) {
              liberal.prefIsDriver(false);
              for (final Creature q : i.activeSquad) {
                if (q.prefCar() == v) {
                  q.prefIsDriver(true);
                  break;
                }
              }
            }
          } else {
            liberal.prefCar(v);
            if (!driver) {
              liberal.prefIsDriver(true);
            }
          }
        }
      } while (true);
    } while (true);
  }

  protected static void stopEvil() {
    boolean havecar = false;
    for (final Creature p : i.activeSquad) {
      if (p.prefCar() != null) {
        havecar = true;
        break;
      }
    }
    Curses.setView(R.layout.generic);
    ui().text(getString(R.string.seWhere)).bold().add();
    // Curses.getch();
    int y = 'a' - 1;
    final StringBuilder name = new StringBuilder();
    for (final Location l : i.location) {
      y++;
      name.setLength(0);
      name.append(l.toString());
      boolean visitable = true;
      if (l == i.activeSquad.location().getNullable()) {
        name.append(" (Current Location)");
        name.append(" Heat:" + l.heat() + "%");
        name.append(" Secrecy:" + l.heatProtection() + "%");
      } else if (l.renting() == CrimeSquad.CCS) {
        name.append(" (Enemy Safe House)");
      } else if (l.renting() == CrimeSquad.LCS) {
        name.append(" (Safe House)");
        name.append(" Heat:" + l.heat() + "%");
        name.append(" Secrecy:" + l.heatProtection() + "%");
      }
      if (l.closed()) {
        name.append(" (Closed Down)");
        visitable = false;
      }
      if (l.highSecurity() > 0) {
        name.append(" (High Security)");
      }
      if (l.needCar() && !havecar) {
        name.append(" (Need Car)");
        visitable = false;
      }
      if (l.lcs().siege.siege) {
        name.append(" (Under Siege)");
      }
      if (l.isHidden()) {
        continue;
      } else if (l.parent() == null) {
        Curses.ui(R.id.gcontrol).text(name.toString()).add();
      } else {
        final UIBuilder button = ui(R.id.gcontrol).text(name.toString());
        if (visitable) {
          button.button(y);
        } else {
          button.button();
        }
        if (l.renting() != null) {
          button.color(l.renting().color());
        }
        button.add();
      }
    }
    ui(R.id.gcontrol).button(10).text(getString(R.string.seNotYet)).add();
    final int c = Curses.getch();
    if (c == 10 || c == 32) {
      i.activeSquad.activity(BareActivity.noActivity());
      return;
    }
    i.activeSquad.activity(new LocationActivity(Activity.VISIT, i.location.get(c - 'a')));
  }

  private static void investLocationButton(final int cost, final char key, final String text,
      final boolean enablable) {
    if (!enablable) {
      return;
    } else if (i.ledger.funds() > cost) {
      Curses.ui().button(key).text(text).add();
    } else {
      Curses.ui().button().text(text).add();
    }
  }

  private static void investLocationButton(final int cost, final char key, final String text,
      @Nullable final Compound prereq) {
    if (prereq == null || i.currentLocation.compoundWalls().contains(prereq)) {
      return;
    } else if (i.ledger.funds() > cost) {
      Curses.ui().button(key).text(text).add();
    } else {
      Curses.ui().button().text(text).add();
    }
  }
}
