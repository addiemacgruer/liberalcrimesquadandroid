package lcs.android.encounters;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.combat.Fight;
import lcs.android.combat.Fight.Fighting;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.util.Color;

enum CarChaseObstacles {
  CROSSTRAFFIC {
    @Override boolean obstacleDrive(final CarChase carChase, final boolean choice) {
      if (!choice) {
        if (dodgedrive(carChase))
          return true;
      } else if (choice) {
        ui().text("You slow down, and turn the corner.").color(Color.YELLOW).add();
        if (i.rng.chance(3)) {
          ui().text("Here they come!").color(Color.YELLOW).add();
          Fight.fight(Fighting.BOTH);
        }
      }
      return false;
    }
  },
  FRUITSTAND {
    @Override boolean obstacleDrive(final CarChase carChase, final boolean choice) {
      if (!choice) {
        if (dodgedrive(carChase))
          return true;
      } else if (choice) {
        ui().text("You plow through the fruit stand!").color(Color.YELLOW).add();
        if (i.rng.chance(5)) {
          ui().text("The fruit-seller has been squashed!").color(Color.RED).add();
          i.activeSquad().criminalizeParty(Crime.MURDER);
        }
      }
      return false;
    }
  },
  NONE {
    @Override boolean obstacleDrive(final CarChase carChase, final boolean choice) {
      return false;
    }
  },
  TRUCKPULLSOUT {
    @Override boolean obstacleDrive(final CarChase carChase, final boolean choice) {
      if (!choice) {
        if (dodgedrive(carChase))
          return true;
      } else if (choice) {
        ui().text("You slow down, and carefully evade the truck.").color(Color.YELLOW).add();
        if (i.rng.chance(3)) {
          ui().text("Here they come!").color(Color.YELLOW).add();
          Fight.fight(Fighting.BOTH);
        }
      }
      return false;
    }
  };
  abstract boolean obstacleDrive(CarChase carChase, boolean choice);

  static boolean drivingUpdate(final CarChase carChase) {
    // CHECK TO SEE WHICH CARS ARE BEING DRIVEN
    final List<Creature> passenger = new ArrayList<Creature>();
    Creature driver;
    final List<Creature> goodp = new ArrayList<Creature>();
    for (int v = carChase.friendcar.size() - 1; v >= 0; v--) {
      passenger.clear();
      driver = null;
      for (final Creature p : i.activeSquad()) {
        if (p.car() == carChase.friendcar.get(v)) {
          if (p.isDriver()) {
            if (p.health().canWalk()) {
              driver = p;
            } else {
              p.driver(false);
            }
          }
          passenger.add(p);
        }
      }
      if (!passenger.isEmpty() && driver == null) {
        // MAKE BEST DRIVING PASSENGER INTO A DRIVER
        goodp.clear();
        int max = 0;
        for (final Creature p : passenger) {
          if (CarChase.driveSkill(p, carChase.friendcar.get(v)) > max && p.health().canWalk()) {
            max = CarChase.driveSkill(p, carChase.friendcar.get(v));
          }
        }
        for (final Creature p : passenger) {
          if (CarChase.driveSkill(p, carChase.friendcar.get(v)) == max && p.health().canWalk()) {
            goodp.add(p);
          }
        }
        if (!goodp.isEmpty()) {
          final Creature p = i.rng.randFromList(goodp);
          p.driver(true);
          driver = p;
          ui().text(p.toString() + " takes over the wheel.").color(Color.YELLOW).add();
        }
      }
      if (driver == null) {
        crashfriendlycar(carChase, v);
        i.siteStory.addNews(NewsEvent.CARCHASE);
        return true;
      }
    }
    for (int v = carChase.enemycar.size() - 1; v >= 0; v--) {
      passenger.clear();
      driver = null;
      for (final Creature p : carChase.creatures()) {
        if (p.car() == carChase.enemycar.get(v) && p.isDriver()) {
          if (p.health().canWalk()) {
            driver = p;
          } else {
            p.driver(false);
          }
        }
      }
      // Enemies don't take over the wheel when driver incapacitated
      if (driver == null) {
        crashenemycar(carChase, v);
        i.siteStory.addNews(NewsEvent.CARCHASE);
      }
    }
    // SET UP NEXT OBSTACLE
    if (i.rng.chance(3)) {
      carChase.obstacle = i.rng.choice(FRUITSTAND, TRUCKPULLSOUT, CROSSTRAFFIC);
    } else {
      carChase.obstacle = NONE;
    }
    return false;
  }

  private static void crashenemycar(final CarChase carChase, final int v) {
    final String str = carChase.enemycar.get(v).fullname(true);
    int victimsum = 0;
    for (final Creature p : carChase.creatures()) {
      if (p.car() == carChase.enemycar.get(v)) {
        victimsum++;
        if (i.mode() == GameMode.SITE) {
          p.dropLoot(i.groundLoot());
        }
      }
    }
    carChase.enemycar.remove(v);
    // CRASH CAR
    ui().text("The " + str).color(Color.CYAN).add();
    switch (i.rng.nextInt(3)) {
    case 0:
    default:
      ui().text(" slams into a building.").add();
      break;
    case 1:
      ui().text(" spins out and crashes.").add();
      break;
    case 2:
      ui().text(" hits a parked car and flips over.").add();
      break;
    }
    if (victimsum > 1) {
      ui().text("Everyone inside is peeled off against the pavement.").add();
    } else if (victimsum == 1) {
      ui().text("The person inside is squashed into a cube.").add();
    }
  }

  private static void crashfriendlycar(final CarChase carChase, final int v) {
    ui().text(
        "Your "
            + carChase.friendcar.get(v).fullname(true)
            + i.rng.choice(" slams into a building!", " skids out and crashes!",
                " hits a parked car and flips over!")).color(Color.MAGENTA).add();
    final List<Creature> squadcopy = new ArrayList<Creature>(i.activeSquad());
    for (final Creature p : squadcopy) {
      if (p.car() == carChase.friendcar.get(v)) {
        // Inflict injuries on Liberals
        for (final BodyPart w : BodyPart.values()) {
          // If limb is intact
          if (!p.health().missing(w)) {
            // Inflict injuries
            if (i.rng.chance(2)) {
              p.health().wounds().get(w).add(Wound.TORN);
              p.health().wounds().get(w).add(Wound.BLEEDING);
              p.health().blood(p.health().blood() - (1 + i.rng.nextInt(25)));
            }
            if (i.rng.chance(3)) {
              p.health().wounds().get(w).add(Wound.CUT);
              p.health().wounds().get(w).add(Wound.BLEEDING);
              p.health().blood(p.health().blood() - (1 + i.rng.nextInt(25)));
            }
            if (i.rng.chance(2) || p.health().wounds().get(w).isEmpty()) {
              p.health().wounds().get(w).add(Wound.BRUISED);
              p.health().blood(p.health().blood() - (1 + i.rng.nextInt(10)));
            }
          }
        }
        // Kill off hostages
        if (p.prisoner()!= null) {
          final Creature prisoner = p.prisoner();
          if (prisoner.health().alive()) {
            ui().text(
                p.prisoner().toString()
                    + i.rng.choice(" is crushed inside the car.",
                        "'s lifeless body smashes through the windshield.",
                        " is thrown from the car and killed instantly.")).color(Color.RED).add();
          }
          prisoner.health().die();
          p.prisoner(null);
        }
        // Handle i.squad() member death
        if (!p.health().alive()) {
          ui().text(
              p
                  + i.rng.choice(" slumps in " + p.genderLiberal().possesive
                      + " seat, out cold, and dies.", " is crushed by the impact.",
                      " struggles free of the car, then collapses lifelessly.")).color(Color.RED)
              .add();
          // Remove dead Liberal from squad
          i.activeSquad().remove(p);
        } else {
          // Inform the player of character survival
          ui().text(p.toString()).color(Color.YELLOW).add();
          switch (i.rng.nextInt(3)) {
          case 0:
          default:
            ui().text(" grips the ").add();
            if (p.weapon().isArmed()) {
              ui().text(p.weapon().weapon().shortName()).add();
            } else {
              ui().text("car frame").add();
            }
            ui().text(" and struggles to " + p.genderLiberal().possesive).add();
            if (p.hasFlag(CreatureFlag.WHEELCHAIR)) {
              ui().text(" wheelchair.").add();
            } else {
              ui().text(" feet.").add();
            }
            break;
          case 1:
            ui().text(" gasps in pain, but lives, for now.").add();
            break;
          case 2:
            ui().text(" crawls free of the car, shivering with pain.").add();
            p.weapon().dropWeapon(null);
            break;
          }
        }
      }
    }
    // GET RID OF CARS
    i.vehicle.remove(carChase.friendcar.get(v));
    carChase.friendcar.remove(v);
    for (final Creature p : i.activeSquad()) {
      if (p == null) {
        continue;
      }
      p.car(null);
    }
  }

  private static boolean dodgedrive(final CarChase carChase) {
    int v;
    ui().text("You swerve to avoid the obstacle!").color(Color.YELLOW).add();
    Creature driver;
    for (v = carChase.friendcar.size() - 1; v >= 0; v--) {
      driver = null;
      for (final Creature p : i.activeSquad()) {
        if (p.car() == carChase.friendcar.get(v) && p.isDriver()) {
          driver = p;
          break;
        }
      }
      if (driver != null && !driver.skill().skillCheck(Skill.DRIVING, CheckDifficulty.EASY)) {
        crashfriendlycar(carChase, v);
        i.siteStory.addNews(NewsEvent.CARCHASE);
        return true;
      }
    }
    for (v = carChase.enemycar.size() - 1; v >= 0; v--) {
      driver = null;
      for (final Creature p : carChase.creatures()) {
        if (p.car() == carChase.enemycar.get(v) && p.isDriver()) {
          driver = p;
          break;
        }
      }
      if (driver != null && !driver.skill().skillCheck(Skill.DRIVING, CheckDifficulty.EASY)) {
        crashenemycar(carChase, v);
        i.siteStory.addNews(NewsEvent.CARCHASE);
      }
    }
    return false;
  }
}