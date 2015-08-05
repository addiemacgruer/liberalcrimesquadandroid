package lcs.android.encounters;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.combat.Fight;
import lcs.android.combat.Fight.Fighting;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.items.AbstractItem;
import lcs.android.items.Vehicle;
import lcs.android.law.Crime;
import lcs.android.monthly.EndGame;
import lcs.android.news.NewsEvent;
import lcs.android.site.Advance;
import lcs.android.site.Squad;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.util.Color;
import lcs.android.util.Filter;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class CarChase extends Encounter {
  /** @param current where to have the car chase. */
  public CarChase(final Location current) {
    location = current;
  }

  final List<Vehicle> enemycar = new ArrayList<Vehicle>();

  final List<Vehicle> friendcar = new ArrayList<Vehicle>();

  @Nullable private Location location;

  boolean canpullover = false;

  CarChaseObstacles obstacle = CarChaseObstacles.NONE;

  /** Chase a specific liberal.
   * @param liberal who to chase
   * @param vehicle which vehicle he's being chased in
   * @return true if he escaped. */
  public boolean chaseSequence(final Creature liberal, final Vehicle vehicle) {
    final Squad oldSquad = liberal.squad().getNullable();
    final Squad sq = new Squad();
    sq.add(liberal);
    liberal.car(vehicle).driver(true);
    boolean ret = false;
    if (i.activeSquad() != null) {
      final Squad originalActiveSquad = i.activeSquad();
      final int ops = i.activeSquad().highlightedMember();
      i.setActiveSquad(sq);
      i.activeSquad().highlightedMember(0);
      ret = encounter();
      i.activeSquad().highlightedMember(ops);
      i.setActiveSquad(originalActiveSquad);
    } else {
      i.setActiveSquad(sq);
      ret = encounter();
    }
    if (ret) {
      liberal.squad(oldSquad).car(null);
    } else if (oldSquad != null) {
      oldSquad.add(liberal);
    }
    return ret;
  }

  /** Perform an escape sequence.
   * @return true if anyone escapes */
  @Override public boolean encounter() {
    Fight.reloadparty();
    if (encounter.isEmpty())
      return true;
    friendcar.clear();
    if (i.activeSquad() != null) {
      addFriendlyCarDrivers();
    }
    i.mode(GameMode.CHASECAR);
    fact("As you pull away from the site, you notice that you are being followed by Conservative swine!");
    do {
      location = i.rng.randFromList(i.location);
    } while (location == null || location.needCar());
    do {
      int partysize = 0;
      int partyalive = 0;
      for (final Creature p : i.activeSquad()) {
        if (p != null) {
          partysize++;
        } else {
          continue;
        }
        if (p.health().alive()) {
          partyalive++;
        }
      }
      setView(R.layout.hospital);
      ui().text(location.toString()).add();
      // PRINT PARTY
      i.activeSquad().printParty();
      if (partyalive > 0) {
        // PRINT DRIVING SITUATION AND INSTRUCTIONS
        if (obstacle == CarChaseObstacles.NONE) {
          ui(R.id.gcontrol).button('d').text("Try to lose them!").add();
          ui(R.id.gcontrol).button('e').text("Equip").add();
          ui(R.id.gcontrol).button('f').text("Fight!").add();
        } else {
          switch (obstacle) {
          case FRUITSTAND:
            ui().text("You are speeding toward a fruit-stand!").color(Color.MAGENTA).add();
            ui(R.id.gcontrol).button('d').text("Evasive driving!").add();
            ui(R.id.gcontrol).button('f').text("Plow through it!").add();
            break;
          case TRUCKPULLSOUT:
            ui().text("A truck pulls out in your path!").color(Color.MAGENTA).add();
            ui(R.id.gcontrol).button('d').text("Speed around it!").add();
            ui(R.id.gcontrol).button('f').text("Slow down!").add();
            break;
          case CROSSTRAFFIC:
            ui().text("There's a red light with cross traffic ahead!").color(Color.MAGENTA).add();
            ui(R.id.gcontrol).button('d').text("Run the light anyway!").add();
            ui(R.id.gcontrol).button('f').text("Slow down and turn!").add();
            break;
          default:
            break;
          }
        }
        ui(R.id.gcontrol).button('b').text("Bail out and run!").add();
        if (canpullover) {
          ui(R.id.gcontrol).button('p').text("Pull over").add();
        }
      } else {
        ui(R.id.gcontrol).button('c').text("Reflect on your lack of skill.").add();
      }
      printEncounter();
      final int c = getch();
      clearChildren(R.id.gcontrol);
      if (partyalive == 0 && c == 'c') {
        // DESTROY ALL CARS BROUGHT ALONG WITH PARTY
        for (final Creature p : i.activeSquad()) {
          if (p == null) {
            continue;
          }
          if (p.car().exists()) {
            i.vehicle.remove(p.car().get());
          }
        }
        for (final Creature p : i.activeSquad()) {
          p.health().die();
        }
        i.activeSquad().clear();
        EndGame.endcheck(null);
        i.mode(GameMode.BASE);
        return false;
      }
      if (partyalive > 0) {
        if (c == 'o' && partysize > 1) {
          Squad.orderParty();
        }
        i.activeSquad().displaySquadInfo(c);
        if (c == 'b') {
          for (final Vehicle v : friendcar) {
            i.vehicle.remove(v);
          }
          friendcar.clear();
          for (final Creature p : i.activeSquad()) {
            if (p == null) {
              continue;
            }
            p.car(null);
          }
          return new FootChase(this).encounter();
        } else if (c == 'p') {
          if (canpullover) {
            chaseGiveUp();
            return false;
          }
        } else if (obstacle == CarChaseObstacles.NONE) {
          if (c == 'd') {
            if (encounter.get(0).type() == CreatureType.valueOf("COP")) {
              if (location != null) {
                i.siteStory.addNews(NewsEvent.CARCHASE);
              }
              i.activeSquad().criminalizeParty(Crime.RESIST);
            }
            evasiveDrive();
            Fight.fight(Fighting.BOTH);
            Advance.creatureAdvance();
            if (obstacle != CarChaseObstacles.NONE && CarChaseObstacles.drivingUpdate(this)) {
              getch();
              return new FootChase(this).encounter();
            }
            if (i.rng.chance(3)) {
              obstacle = i.rng.randFromArray(CarChaseObstacles.values());
            }
          }
          if (c == 'f') {
            if (encounter.get(0).type() == CreatureType.valueOf("COP")) {
              if (location != null) {
                i.siteStory.addNews(NewsEvent.CARCHASE);
              }
              i.activeSquad().criminalizeParty(Crime.RESIST);
            }
            Fight.fight(Fighting.BOTH);
            Advance.creatureAdvance();
            if (CarChaseObstacles.drivingUpdate(this))
              return new FootChase(this).encounter();
          }
          if (c == 'e') {
            AbstractItem.equip(i.activeSquad().loot(), null);
          }
        } else {
          switch (obstacle) {
          case CROSSTRAFFIC:
          case TRUCKPULLSOUT:
          case FRUITSTAND:
            if (c == 'd') {
              if (obstacle.obstacleDrive(this, false))
                return new FootChase(this).encounter();
              Advance.creatureAdvance();
              CarChaseObstacles.drivingUpdate(this);
            }
            if (c == 'f') {
              if (obstacle.obstacleDrive(this, true))
                return new FootChase(this).encounter();
              Advance.creatureAdvance();
              if (CarChaseObstacles.drivingUpdate(this))
                return new FootChase(this).encounter();
            }
            break;
          default:
            break;
          }
        }
        // HAVE YOU LOST ALL OF THEM?
        // THEN LEAVE
        partysize = 0;
        partyalive = 0;
        for (final Creature p : i.activeSquad()) {
          if (p != null) {
            partysize++;
          } else {
            continue;
          }
          if (p.health().alive()) {
            partyalive++;
          }
        }
        int baddiecount = 0;
        for (final Creature e : encounter) {
          if (e.car().exists() && e.enemy() && e.health().alive()) {
            baddiecount++;
          }
        }
        if (partyalive > 0 && baddiecount == 0) {
          ui().text("It looks like you've lost them!").add();
          getch();
          for (final Creature p : i.pool) {
            p.health().stopBleeding();
          }
          i.mode(GameMode.BASE);
          return true;
        }
      }
      ui(R.id.gcontrol).button(' ').text("OK").add();
      getch();
    } while (true);
  }

  /** Disaster! Lose all the cars associated with this chase. */
  public void loseAllCars() {
    i.vehicle.removeAll(friendcar);
  }

  /** Describes the cars and drivers of the people following you. */
  @Override public void printEncounter() {
    ui().text("Vehicles Chasing:").bold().add();
    for (final Vehicle vehicle : enemycar) {
      ui().text(vehicle.fullname(true)).add();
      for (final Creature enemy : encounter) {
        if (enemy.car().getNullable() != vehicle) {
          continue;
        }
        ui().text("--" + enemy + " " + enemy.vagueAge() + (enemy.isDriver() ? "-D" : ""))
            .color(enemy.alignment().color()).add();
      }
    }
  }

  private void addFriendlyCarDrivers() {
    for (final Creature p : i.activeSquad()) {
      if (p.car().exists()) {
        final Vehicle v = p.car().get();
        for (final Vehicle v2 : friendcar) {
          if (v2 == v) {
            break;
          }
        }
        friendcar.add(v);
      }
    }
  }

  private void chaseGiveUp() {
    final Location ps = AbstractSiteType.type("GOVERNMENT_POLICESTATION").getLocation();
    i.vehicle.removeAll(friendcar);
    friendcar.clear();
    int hostagefreed = 0;
    for (final Creature p : i.activeSquad()) {
      p.squad(null).car(null).location(ps);
      p.weapon().dropWeaponsAndClips(null);
      p.activity(BareActivity.noActivity());
      if (p.prisoner().exists()) {
        if (!p.prisoner().get().squad().exists()) {
          hostagefreed++;
        }
        p.freeHostage(Creature.Situation.CAPTURED);
      }
    }
    i.activeSquad().clear();
    for (final Creature p : i.pool) {
      p.health().stopBleeding();
    }
    if (i.mode() != GameMode.CHASECAR) {
      ui().text("You stop and are arrested.").color(Color.MAGENTA).add();
    } else {
      ui().text("You pull over and are arrested.").color(Color.MAGENTA).add();
    }
    if (hostagefreed > 0) {
      ui().text("Your hostage" + (hostagefreed > 1 ? "s are " : " is ") + "free").add();
    }
  }

  private void evasiveDrive() {
    final List<Integer> yourrolls = new ArrayList<Integer>();
    for (final Creature p : i.activeSquad()) {
      if (p == null) {
        continue;
      }
      if (p.health().alive() && p.isDriver()) {
        final Maybe<Vehicle> v = p.car();
        if (v.exists()) {
          yourrolls.add(driveSkill(p, v.get()));
        }
        p.skill().train(Skill.DRIVING, i.rng.nextInt(20));
      }
    }
    if (yourrolls.isEmpty()) {
      yourrolls.add(0);// error -- and for this you get a 0
    }
    final List<Integer> theirrolls = new ArrayList<Integer>();
    final List<Vehicle> theirrolls_id = new ArrayList<Vehicle>();
    final List<Creature> theirrolls_drv = new ArrayList<Creature>();
    for (final Iterator<Creature> ei = encounter.iterator(); ei.hasNext();) {
      final Creature e = ei.next();
      if (e.car().exists() && e.enemy() && e.health().alive() && e.isDriver() && e.car().exists()) {
        final Vehicle v = e.car().get();
        theirrolls.add(driveSkill(e, v));
        theirrolls_id.add(e.car().get());
        theirrolls_drv.add(e);
      } else if (e.car().missing()) {
        ei.remove();
      }
    }
    final int yourworst = Filter.lowest(yourrolls, Filter.value());
    ui().text(
        i.rng.choice("You keep the gas floored!", "You swerve around the next corner!",
            "You screech through an empty lot to the next street!",
            yourworst > 15 ? "You boldly weave through oncoming traffic!"
                : "You make obscene gestures at the pursuers!")).add();
    for (int j = 0; j < theirrolls.size(); j++) {
      final int cnt = i.rng.randFromList(yourrolls);
      if (theirrolls.get(j) < cnt) {
        for (final Creature e : encounter) {
          if (e == theirrolls_drv.get(j)) {
            ui().text(e.toString()).color(Color.CYAN).add();
            break;
          }
        }
        switch (i.rng.nextInt(cnt / 5)) {
        case 1:
          ui().text(" skids out!").add();
          break;
        case 2:
          ui().text(" backs off for safety.").add();
          break;
        case 3:
          ui().text(" breaks hard and nearly crashes!").add();
          break;
        default:
          ui().text(" falls behind!").add();
          break;
        }
        for (final Iterator<Creature> iter = encounter.iterator(); iter.hasNext();) {
          if (iter.next().car().get() == theirrolls_id.get(j)) {
            iter.remove();
          }
        }
        for (final Iterator<Vehicle> iter = enemycar.iterator(); iter.hasNext();) {
          if (iter.next() == theirrolls_id.get(j)) {
            iter.remove();
          }
        }
        printEncounter();
      } else {
        for (final Creature e : encounter) {
          if (e == theirrolls_drv.get(j)) {
            ui().text(e + " is still on your tail!").color(Color.YELLOW).add();
            break;
          }
        }
      }
    }
  }

  private static final long serialVersionUID = Game.VERSION;

  private final static int DRIVING_RANDOMNESS = 13;

  public static int driveSkill(final Creature liberal, @Nullable final Vehicle vehicle) {
    if (vehicle == null)
      throw new IllegalArgumentException("vehicle was null");
    int driveskill = liberal.skill().skill(Skill.DRIVING) * (3 + vehicle.driveBonus())
        + i.rng.nextInt(CarChase.DRIVING_RANDOMNESS);
    driveskill -= liberal.health().modRoll();
    if (driveskill < 0) {
      driveskill = 0;
    }
    driveskill *= liberal.health().blood() / 50.0;
    return driveskill;
  }
}
