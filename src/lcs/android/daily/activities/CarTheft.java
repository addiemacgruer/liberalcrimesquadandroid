package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.CarChase;
import lcs.android.encounters.Encounter;
import lcs.android.encounters.FootChase;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.items.Vehicle;
import lcs.android.items.VehicleType;
import lcs.android.law.Crime;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.site.type.PoliceStation;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class CarTheft extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> stealcars = this;
    for (final Creature p : stealcars) {
      if (stealcar(p)) {
        p.activity(BareActivity.noActivity());
      } else if (p.location().exists() && p.location().get().type().isType(PoliceStation.class)) {
        p.crime().criminalize(Crime.CARTHEFT);
      }
    }
  }

  private static final long serialVersionUID = Game.VERSION;

  private static VehicleType carselect(final Creature cr) {
    final List<VehicleType> cart = new ArrayList<VehicleType>();
    for (final VehicleType a : Game.type.vehicle.values()) {
      if (a.stealDifficultyToFind() < 10) {
        cart.add(a);
      }
    }
    do {
      setView(R.layout.generic);
      ui().text("What type of car will " + cr.toString() + " try to find and steal today?").add();
      ui().text("TYPE - DIFFICULTY TO FIND UNATTENDED").add();
      int y = 'a';
      final StringBuilder str = new StringBuilder();
      for (final VehicleType p : cart) {
        str.append(p.toString());
        str.append(" - ");
        final int difficulty = p.stealDifficultyToFind();
        Color color = Color.BLACK;
        switch (difficulty) {
        case 0:
          color = Color.GREEN;
          str.append("Simple");
          break;
        case 1:
          color = Color.CYAN;
          str.append("Very Easy");
          break;
        case 2:
          color = Color.CYAN;
          str.append("Easy");
          break;
        case 3:
          color = Color.BLUE;
          str.append("Below Average");
          break;
        case 4:
          str.append("Average");
          break;
        case 5:
          str.append("Above Average");
          break;
        case 6:
          color = Color.YELLOW;
          str.append("Hard");
          break;
        case 7:
          color = Color.MAGENTA;
          str.append("Very Hard");
          break;
        case 8:
          color = Color.MAGENTA;
          str.append("Extremely Difficult");
          break;
        case 9:
          color = Color.RED;
          str.append("Nearly Impossible");
          break;
        default:
          color = Color.RED;
          str.append("Impossible");
          break;
        }
        ui(R.id.gcontrol).button(y++).text(str.toString()).color(color).add();
        str.setLength(0);
      }
      final int c = getch();
      if (c >= 'a') {
        final VehicleType p = cart.get(c - 'a');
        return p;
      }
    } while (true);
  }

  private static boolean stealcar(final Creature cr) {
    VehicleType cartype = carselect(cr);
    if (cartype != null) {
      cr.squad(null); // otherwise they'll not be free to reassign to squads.
      final int diff = cartype.stealDifficultyToFind() * 2;
      final VehicleType old = cartype;
      cr.skill().train(Skill.STREETSENSE, 5);
      // THEFT SEQUENCE
      setView(R.layout.hospital);
      ui().text("Adventures in Liberal Car Theft").add();
      cr.printCreatureInfo(255);
      ui().text(cr.toString() + " looks around for an accessible vehicle...").add();
      // ROUGH DAY
      if (!cr.skill().skillCheck(Skill.STREETSENSE, diff)) {
        do {
          cartype = i.rng.randFromCollection(Game.type.vehicle.values());
          if (i.rng.nextInt(10) < cartype.stealDifficultyToFind()) {
            cartype = old;
          }
        } while (cartype == old);
      }
      final Vehicle v = new Vehicle(cartype);
      final String carname = v.fullname(false);
      if (old != cartype) {
        ui().text(
            cr.toString() + " was unable to find a " + old.toString() + " but did find a "
                + v.longName() + ".").add();
      } else {
        ui().text(cr.toString() + " found a " + v.longName() + ".").add();
      }
      // APPROACH?
      ui().text(cr.toString() + " looks from a distance at an empty " + carname + ".").add();
      ui(R.id.gcontrol).button('a').text("Approach the driver's side door.").add();
      ui(R.id.gcontrol).button('x').text("Call it a day.").add();
      int c;
      do {
        c = getch();
        if (c == 'a') {
          break;
        }
        if (c == 'x') {
          return true;
        }
      } while (true);
      clearChildren(R.id.gcontrol);
      // SECURITY?
      boolean alarmon = false;
      final boolean sensealarm = (i.rng.nextInt(100) < v.senseAlarmChance());
      final boolean touchalarm = (i.rng.nextInt(100) < v.touchAlarmChance());
      int windowdamage = 0;
      do {
        if (alarmon) {
          if (sensealarm) {
            ui().text("THE VIPER").add();
          } else {
            ui().text(carname).add();
          }
          ui().text(":   ").add();
          if (sensealarm) {
            ui().text("STAND AWAY FROM THE VEHICLE!   <BEEP!!> <BEEP!!>").color(Color.RED).add();
          } else {
            ui().text("<BEEP!!> <BEEP!!> <BEEP!!> <BEEP!!>").add();
          }
        } else if (sensealarm) {
          ui().text("THE VIPER: THIS IS THE VIPER! STAND AWAY!").color(Color.RED).add();
        } else {
          ui().text(cr.toString() + " stands by the " + carname + ".").add();
        }
        ui(R.id.gcontrol).button('a').text("Pick the lock.").add();
        ui(R.id.gcontrol).button('b').text("Break the window.").add();
        if (!sensealarm) {
          ui(R.id.gcontrol).button('x').text("Call it a day.").add();
        } else if (!alarmon) {
          ui(R.id.gcontrol).button('x').text("The Viper? " + cr.toString() + " is deterred.").add();
        } else {
          ui(R.id.gcontrol).button('x').text("Yes, the Viper has deterred " + cr.toString() + ".")
              .add();
        }
        int method = 0;
        do {
          c = getch();
          if (c == 'a') {
            break;
          }
          if (c == 'b') {
            method = 1;
            break;
          }
          if (c == 'x') {
            return false;
          }
        } while (true);
        clearChildren(R.id.gcontrol);
        boolean entered = false;
        // PICK LOCK
        if (method == 0) {
          if (cr.skill().skillCheck(Skill.SECURITY, CheckDifficulty.AVERAGE)) {
            cr.skill().train(Skill.SECURITY, Math.max(5 - cr.skill().skill(Skill.SECURITY), 0));
            ui().text(cr.toString() + " jimmies the car door open.").add();
            entered = true;
          } else {
            ui().text(cr.toString() + " fiddles with the lock with no luck.").add();
          }
        }
        // BREAK WINDOW
        if (method == 1) {
          final int difficulty = (int) (CheckDifficulty.EASY.value() / cr.weapon().weapon()
              .bashstrengthmod())
              - windowdamage;
          if (cr.skill().attributeCheck(Attribute.STRENGTH, difficulty)) {
            ui().text(cr.toString()).add();
            ui().text(" smashes the window").add();
            if (cr.weapon().weapon().bashstrengthmod() > 1) {
              ui().text(" with a ").add();
              ui().text(cr.weapon().weapon().toString()).add();
            }
            ui().text(".").add();
            windowdamage = 10;
            entered = true;
          } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(cr.toString());
            sb.append(" cracks the window");
            if (cr.weapon().weapon().bashstrengthmod() > 1) {
              sb.append(" with a ");
              sb.append(cr.weapon().weapon().toString());
            }
            sb.append(" but it is still somewhat intact.");
            ui().text(sb.toString()).add();
            windowdamage += 1;
          }
        }
        if (touchalarm || sensealarm) {
          if (!alarmon) {
            ui().text("An alarm suddenly starts blaring!").color(Color.YELLOW).add();
            alarmon = true;
          }
        }
        // NOTICE CHECK
        if (i.rng.chance(50) || i.rng.chance(5) && alarmon) {
          ui().text(cr.toString() + " has been spotted by a passerby!").color(Color.RED).add();
          getch();
          // FOOT CHASE
          final NewsStory ns = new NewsStory(StoryType.CARTHEFT);
          i.newsStories.add(ns);
          i.siteStory = ns;
          FootChase.attemptArrest(cr, "trying to steal a car");
          i.mode(GameMode.BASE);
          return false;
        }
        if (entered) {
          break;
        }
      } while (true);
      // START CAR
      boolean keys_in_car = false;
      final int key_location = i.rng.nextInt(5);
      // int ignition_progress = 0;
      int key_search_total = 0;
      int nervous_counter = 0;
      if (i.rng.chance(5)) {
        keys_in_car = true;
      }
      ui().text(cr.toString() + " is behind the wheel of a " + carname + ".").add();
      do {
        nervous_counter++;
        if (alarmon) {
          if (sensealarm) {
            ui().text("THE VIPER").add();
          } else {
            ui().text(carname).add();
          }
          ui().text(":   ").add();
          if (sensealarm) {
            ui().text("REMOVE YOURSELF FROM THE VEHICLE!   <BEEP!!> <BEEP!!>").color(Color.RED)
                .add();
          } else {
            ui().text("<BEEP!!> <BEEP!!> <BEEP!!> <BEEP!!>").color(Color.RED).add();
          }
        }
        ui(R.id.gcontrol).button('a').text("Hotwire the car.").add();
        ui(R.id.gcontrol).button('b').text("Desperately search for keys.").add();
        if (!sensealarm) {
          ui(R.id.gcontrol).button('x').text("Call it a day.").add();
        } else {
          ui(R.id.gcontrol).button('x')
              .text("The Viper has finally deterred " + cr.toString() + ".").add();
        }
        int method = 0;
        do {
          c = getch();
          if (c == 'a') {
            break;
          }
          if (c == 'b') {
            method = 1;
            break;
          }
          if (c == 'x') {
            return false;
          }
        } while (true);
        clearChildren(R.id.gcontrol);
        boolean started = false;
        // HOTWIRE CAR
        if (method == 0) {
          if (cr.skill().skillCheck(Skill.SECURITY, CheckDifficulty.CHALLENGING)) {
            cr.skill().train(Skill.SECURITY, Math.max(10 - cr.skill().skill(Skill.SECURITY), 0));
            ui().text(cr.toString() + " hotwires the car!").add();
            started = true;
          } else {
            int flavor_text;
            if (cr.skill().skill(Skill.SECURITY) < 4) {
              flavor_text = i.rng.nextInt(3);
            } else {
              flavor_text = i.rng.nextInt(5);
            }
            switch (flavor_text) {
            default:
              ui().text(cr.toString() + " fiddles with the ignition, but the car doesn't start.")
                  .add();
              break;
            case 1:
              ui().text(
                  cr.toString() + " digs around in the steering column, but the car doesn't start.")
                  .add();
              break;
            case 2:
              ui().text(cr.toString() + " touches some wires together, but the car doesn't start.")
                  .add();
              break;
            case 3:
              ui().text(
                  cr.toString()
                      + " makes something in the engine click, but the car doesn't start.").add();
              break;
            case 4:
              ui().text(
                  cr.toString()
                      + " manages to turn on some dash lights, but the car doesn't start.").add();
              break;
            }
          }
        }
        // KEYS
        if (method == 1) {
          CheckDifficulty difficulty = CheckDifficulty.AVERAGE;
          String location = "";
          if (!keys_in_car) {
            difficulty = CheckDifficulty.IMPOSSIBLE;
            location = "in SPACE. With ALIENS. Seriously.";
          } else {
            switch (key_location) {
            default:
              difficulty = CheckDifficulty.AUTOMATIC;
              location = "in the ignition.  Damn.";
              break;
            case 1:
              difficulty = CheckDifficulty.EASY;
              location = "above the pull-down sunblock thingy!";
              break;
            case 2:
              difficulty = CheckDifficulty.EASY;
              location = "in the glove compartment!";
              break;
            case 3:
              difficulty = CheckDifficulty.AVERAGE;
              location = "under the front seat!";
              break;
            case 4:
              difficulty = CheckDifficulty.HARD;
              location = "under the back seat!";
              break;
            }
          }
          if (cr.skill().attributeCheck(Attribute.INTELLIGENCE, difficulty.value())) {
            if (!i.freeSpeech()) {
              ui().text("Holy [Car Keys]!  ").color(Color.GREEN).add();
            } else {
              ui().text("Holy shit!  " + cr.toString() + " found the keys " + location)
                  .color(Color.GREEN).add();
            }
            started = true;
          } else {
            key_search_total++;
            ui().text(cr.toString() + ": <rummaging> ").add();
            if (key_search_total == 5) {
              ui().text("Are they even in here?").color(Color.GREEN).add();
            } else if (key_search_total == 10) {
              ui().text("I don't think they're in here...").color(Color.GREEN).add();
            } else if (key_search_total == 15) {
              ui().text("If they were here, I'd have found them by now.").color(Color.GREEN).add();
            } else if (key_search_total > 15) {
              switch (i.rng.nextInt(5)) {
              default:
                ui().text("This isn't working!").color(Color.GREEN).add();
                break;
              case 1:
                ui().text("Why me?").color(Color.GREEN).add();
                break;
              case 2:
                ui().text("What do I do now?").color(Color.GREEN).add();
                break;
              case 3:
                ui().text("Oh no...").color(Color.GREEN).add();
                break;
              case 4:
                ui().text("I'm going to get arrested, aren't I?").color(Color.GREEN).add();
                break;
              }
            } else {
              switch (i.rng.nextInt(5)) {
              default:
                ui().text("Please be in here somewhere...").color(Color.GREEN).add();
                break;
              case 1:
                if (!i.freeSpeech()) {
                  ui().text("[Shoot]!  Where are they?!").color(Color.GREEN).add();
                } else {
                  ui().text("Fuck!  Where are they?!").color(Color.GREEN).add();
                }
                break;
              case 2:
                ui().text("Come on, baby, come to me...").color(Color.GREEN).add();
                break;
              case 3:
                if (!i.freeSpeech()) {
                  ui().text("[Darn] it...").color(Color.GREEN).add();
                } else {
                  ui().text("Dammit...").color(Color.GREEN).add();
                }
                break;
              case 4:
                ui().text("I wish I could hotwire this thing...").color(Color.GREEN).add();
                break;
              }
            }
          }
        }
        // NOTICE CHECK
        if (!started && (i.rng.chance(50) || i.rng.chance(5) && alarmon)) {
          ui().text(cr.toString() + " has been spotted by a passerby!").color(Color.RED).add();
          getch();
          // FOOT CHASE
          final NewsStory ns = new NewsStory(StoryType.CARTHEFT);
          i.newsStories.add(ns);
          i.siteStory = ns;
          final FootChase fc = new FootChase(Encounter.createEncounter(null, 5));
          fc.footchase(cr);
          i.mode(GameMode.BASE);
          return false;
        }
        // Nervous message check
        else if (!started && i.rng.nextInt(7) + 5 < nervous_counter) {
          nervous_counter = 0;
          switch (i.rng.nextInt(3)) {
          default:
            ui().text(cr.toString() + " hears someone nearby making a phone call.")
                .color(Color.YELLOW).add();
            break;
          case 1:
            ui().text(cr.toString() + " is getting nervous being out here this long.")
                .color(Color.YELLOW).add();
            break;
          case 2:
            ui().text(cr.toString() + " sees a police car driving around a few blocks away.")
                .color(Color.YELLOW).add();
            break;
          }
        }
        if (started) {
          break;
        }
      } while (true);
      getch();
      // CHASE SEQUENCE
      // CAR IS OFFICIAL, THOUGH CAN BE DELETE BY chasesequence()
      cr.addJuice(v.stealJuice(), 100);
      v.addHeat(14 + v.stealExtraHeat());
      int chaselev = i.rng.nextInt(13 - windowdamage);
      if (chaselev > 0 || v.ideal() == Game.type.vehicle.get("POLICECAR") && i.rng.chance(2)) // Identify
      // police
      // cruiser.
      // Temporary solution? -XML
      {
        v.addHeat(10);
        chaselev = 1;
        final NewsStory ns = new NewsStory(StoryType.CARTHEFT);
        i.newsStories.add(ns);
        i.siteStory = ns;
        Encounter.createEncounter(null, chaselev).encounter();
      }
      i.vehicle.add(v);
      if (cr.base().exists() && new CarChase(Location.none()).chaseSequence(cr, v)) {
        v.setLocation(cr.base().get());
        // Automatically assign this car to this driver, if no other one
        // is present
        if (cr.prefCar() == null) {
          cr.prefCar(v);
          cr.prefIsDriver(true);
        }
        return true;
      }
      return false;// do not need to delete vehicle
    }
    return false;
  }
}
