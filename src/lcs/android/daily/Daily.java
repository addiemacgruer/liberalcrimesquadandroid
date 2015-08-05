package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.LocationActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.health.Animal;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.SpecialWounds;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.CarChase;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.Vehicle;
import lcs.android.monthly.EndGame;
import lcs.android.news.News;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.scoring.EndType;
import lcs.android.site.Site;
import lcs.android.site.Squad;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.Clinic;
import lcs.android.site.type.IHospital;
import lcs.android.site.type.IShop;
import lcs.android.site.type.Prison;
import lcs.android.site.type.Shelter;
import lcs.android.site.type.University;
import lcs.android.site.type.Warehouse;
import lcs.android.util.Filter;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class Daily {
  private enum Dispersal {
    ABANDONLCS,
    BOSSINHIDING,
    BOSSINPRISON,
    BOSSSAFE,
    DEAD,
    HIDING,
    IN_PRISON,
    NOCONTACT,
    SAFE
  }

  private static final Map<Creature, Dispersal> dispersal_status = new HashMap<Creature, Dispersal>();

  /* squad members with no chain of command lose contact */
  /* daily - manages too hot timer and when a site map should be re-seeded and renamed */
  public static void advanceday() {
    doAging();
    final Squad oactivesquad = i.activeSquad();
    doVisiting();
    i.setActiveSquad(oactivesquad);
    Log.i("LCS", "Living members:" + Filter.count(i.pool, Filter.LIVING));
    if (Filter.count(i.pool, Filter.LIVING) == 0) {
      Game.endGame();
    }
    doHostages();
    Activities.funds_and_trouble();
    doHealing();
    dispersalcheck();
    doRent();
    doMeetings();
    doDates();
    i.score.date.nextDay();
    doDaily();
    News.majornewspaper();
    Squad.cleanGoneSquads();
    Siege.siegeturn();
    Siege.siegeCheck();
    Squad.cleanGoneSquads();
  }

  public static void advancelocations() {
    for (final Location l : i.location) {
      if (l.closed()) {
        l.decreaseCloseTimer();
      } else if (l.highSecurity() > 0) {
        l.highSecurity(l.highSecurity() - 1);
      }
    }
  }

  public static void dispersalcheck() {
    // NUKE DISPERSED SQUAD MEMBERS WHOSE MASTERS ARE NOT AVAILABLE
    dispersal_status.clear();
    for (final Creature c : i.pool) {
      if (!dispersal_status.containsKey(c)) {
        dispersalCheck(c);
      }
    }
    // After checking through the entire command structure, proceed
    // to nuke all squad members who are unable to make contact with
    // the LCS.
    for (final Creature p : Filter.of(i.pool, Filter.LIBERAL)) {
      if (dispersal_status.get(p) == Dispersal.NOCONTACT
          || dispersal_status.get(p) == Dispersal.HIDING
          || dispersal_status.get(p) == Dispersal.ABANDONLCS) {
        if (!i.disbanding()) {
          if (p.hiding() == 0 && dispersal_status.get(p) == Dispersal.HIDING) {
            setView(R.layout.generic);
            ui().text(p.toString() + " has lost touch with the Liberal Crime Squad.").add();
            ui().text("The Liberal has gone into hiding...").add();
            getch();
          } else if (dispersal_status.get(p) == Dispersal.ABANDONLCS) {
            fact(p.toString() + " has abandoned the LCS.");
          } else if (dispersal_status.get(p) == Dispersal.NOCONTACT) {
            fact(p.toString() + " has lost touch with the Liberal Crime Squad.");
          }
        }
        p.removeSquadInfo();
        if (dispersal_status.get(p) == Dispersal.NOCONTACT
            || dispersal_status.get(p) == Dispersal.ABANDONLCS) {
          i.pool.remove(p);
        } else {
          final Location hs = AbstractSiteType.type(Shelter.class).getLocation();
          p.location(Location.none());
          if (!p.hasFlag(CreatureFlag.SLEEPER)) {
            // end up in
            // shelter
            // otherwise.
            p.base(hs);
          }
          p.activity(BareActivity.noActivity());
          p.hiding(-1); // Hide indefinitely
        }
      }
    }
    // MUST DO AN END OF GAME CHECK HERE BECAUSE OF DISPERSAL
    EndGame.endcheck(EndType.DISPERSED);
    Squad.cleanGoneSquads();
  }

  private static void dispersalCheck(final Creature p) {
    if (!p.hire().exists()) { // LCS leader is safe
      if (!i.disbanding()) {
        dispersal_status.put(p, Dispersal.SAFE);
        if (p.hiding() == -1) {
          p.hiding(i.rng.nextInt(10) + 5);
        }
      } else {
        dispersal_status.put(p, Dispersal.HIDING);
      }
      return;
    }
    final Creature boss = p.hire().get(); // won't be null, as not founder.
    if (!dispersal_status.containsKey(boss)) {
      dispersalCheck(boss);
    }
    /* have you died? */
    if (!p.health().alive() && !i.disbanding()) {
      dispersal_status.put(p, Dispersal.DEAD);
      p.promoteSubordinates();
      return;
    }
    /* are you in jail? */
    if (p.location().exists() && p.location().get().type().isType(Prison.class)
        && !p.hasFlag(CreatureFlag.SLEEPER)) {
      dispersal_status.put(p, Dispersal.IN_PRISON);
      return;
    }
    /* Love slaves bleed juice when not in prison with their lover */
    if (dispersal_status.get(boss) == Dispersal.IN_PRISON && p.hasFlag(CreatureFlag.LOVE_SLAVE)) {
      p.juice(p.juice() - 1);
      if (p.juice() < -50) {
        dispersal_status.put(p, Dispersal.ABANDONLCS);
      } else {
        dispersal_status.put(p, Dispersal.SAFE);
      }
      return;
    }
    /* otherwise, you're okay (can always find boss in prison) */
    if (dispersal_status.get(boss) == Dispersal.IN_PRISON) {
      dispersal_status.put(p, Dispersal.SAFE);
    }
    /* If your boss is in hiding, then so are you */
    if (dispersal_status.get(boss) == Dispersal.HIDING) {
      dispersal_status.put(p, Dispersal.HIDING);
      return;
    } else if (p.hiding() != 0) {
      /* If they're hiding indefinitely and their boss isn't hiding, then have them discreetly
       * return in a couple of weeks */
      dispersal_status.put(p, Dispersal.HIDING);
      if (p.hiding() == -1) {
        p.hiding(i.rng.nextInt(10) + 3);
      }
    }
    /* if your boss is safe, then so are you */
    if (dispersal_status.get(boss) == Dispersal.SAFE) {
      dispersal_status.put(p, Dispersal.SAFE);
      return;
    }
    // fell off the bottom
    Log.w("LCS", "Unknown dispersal status:" + p);
    dispersal_status.put(p, Dispersal.NOCONTACT);
  }

  private static void doAging() {
    // SHUFFLE AROUND THE SQUADLESS
    final Location homes = AbstractSiteType.type(Shelter.class).getLocation();
    // Aging
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      // CLEAR CAR STATES
      p.car(null);
      p.stunned(0); // For lack of a better place, make stunning expire
      // here
      if (p.type().animal() != Animal.HUMAN) {
        continue;
      }
      /* animals, tanks don't have age effects at the moment TODO: Start aging effects for animals
       * at age 12, take into account if they are genetic monsters or not. */
      if (p.age() > 60) {
        int decrement = 0;
        while (p.age() - decrement > 60) {
          if (i.rng.chance(365 * 10)) {
            p.skill().attribute(Attribute.HEALTH, -1);
            if (p.skill().getAttribute(Attribute.HEALTH, false) <= 0
                && p.skill().getAttribute(Attribute.HEALTH, true) <= 1) {
              p.health().die();
              setView(R.layout.generic);
              ui().text(
                  p.toString() + " has passed away at the age of " + p.age()
                      + ". The Liberal will be missed.").add();
              getch();
              break;
            }
          }
          decrement += 10;
        }
        if (!p.health().alive()) {
          continue;
        }
      }
      p.checkBirthday();
      if (i.disbanding()) {
        continue;
      }
      if (p.health().clinicMonths() != 0) {
        continue;
      }
      if (p.datingVacation() != 0) {
        continue;
      }
      if (p.hiding() != 0) {
        continue;
      }
      if (p.squad().exists()) {
        continue;
      }
      if (p.location().exists() && p.location().get().type().isPrison()) {
        /* Prevent moving people to a sieged location, but don't evacuate people already under
         * siege. - wisq */
        if (p.base().exists() && p.location().get() != p.base().getNullable()
            && p.base().get().lcs().siege.siege) {
          p.base(homes);
        }
        p.location(p.base().get());
      }
    }
  }

  private static void doDaily() {
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      // Heal over time
      if (p.health().blood() < 100) {
        p.health().blood(p.health().blood() + 1);
      }
      // Updating for in hiding
      if (p.hiding() > 0) {
        p.hiding(p.hiding() - 1);
        if (p.hiding() == 0) {
          if (p.base().exists() && p.base().get().lcs().siege.siege) {
            p.hiding(1);
          } else {
            p.location(p.base().get());
            fact(p.toString() + " regains contact with the LCS.");
            getch();
          }
        }
      }
      // Check if news reports kidnapping
      if (p.hasFlag(CreatureFlag.MISSING) && !p.hasFlag(CreatureFlag.KIDNAPPED)) {
        if (i.rng.nextInt(14) + 4 < p.joindays()) {
          p.addFlag(CreatureFlag.KIDNAPPED);
          final NewsStory ns = new NewsStory(StoryType.KIDNAPREPORT).creature(p);
          i.newsStories.add(ns);
        }
      }
      // Increment number of days since joined/kidnapped
      p.joindays(p.joindays() + 1);
      // Increment number of days been dead if dead
      if (!p.health().alive()) {
        p.deathDays(p.deathDays() + 1);
        continue;
      }
      // Gain skill levels for anything where you have enough experience
      p.skill().skillUp();
    }
  }

  private static void doDates() {
    for (final Iterator<Date> di = i.dates.iterator(); di.hasNext();) {
      final Date d = di.next();
      if (i.disbanding()) {
        break;
      }
      final Creature p = d.dater;
      /* Stand up dates if 1) dater does not exist, or 2) dater was not able to return to a
       * safehouse today (and is not in the hospital) */
      if (p.location().exists() && !(p.location().get().type().isHospital()) || d.timeleft != 0) {
        // VACATION
        if (d.timeleft > 0) {
          d.timeleft--;
          p.datingVacation(d.timeleft);
          if (d.timeleft == 0) {
            final Location hs = AbstractSiteType.type(Shelter.class).getLocation();
            if (p.base().exists() && p.base().get().lcs().siege.siege) {
              p.base(hs);
            }
            p.location(p.base().get());
            if (d.completeVacation(p)) {
              di.remove();
              continue;
            }
          }
        } else if (p.location().exists() && p.location().get().lcs().siege.siege) {
          di.remove();
          continue;
        }
        // DO DATE
        else if (d.completedate(p)) {
          di.remove();
          continue;
        } else {
          p.datingVacation(d.timeleft);
          if (p.datingVacation() > 0) {
            Squad sq = null;
            /* IF YOU ARE THE LAST PERSON IN YOUR SQUAD YOU HAVE TO DROP OFF THE EQUIPMENT WHEREVER
             * YOUR BASE IS BECAUSE YOUR SQUAD IS ABOUT TO BE DESTROYED */
            if (p.squad().exists()) {
              sq = p.squad().get();
            }
            // NOW KICK THE DATER OUT OF THE SQUAD AND LOCATION
            p.removeSquadInfo();
            p.location(Location.none());
            if (sq != null && p.base().exists()) {
              sq.testClear(p.base().get());
            }
          }
        }
      } else {
        di.remove();
      }
    }
  }

  private static void doHealing() {
    // Healing - determine medical support at each location
    final Map<Location, Integer> healingSkill = new HashMap<Location, Integer>();
    final Map<Location, Integer> healingExp = new HashMap<Location, Integer>();
    for (final Location p : i.location) {
      healingSkill.put(p, locationSkill(p));
      healingExp.put(p, 0);
    }
    for (final Creature p : Filter.of(i.pool, Filter.ACTIVE)) {
      /* People will help heal even if they aren't specifically assigned to do so. Having a specific
       * healing activity helps bookkeeping for the player, though. Only the highest medical skill
       * is considered */
      if (p.activity().type() == Activity.HEAL || p.activity().type() == Activity.NONE) {
        if (p.location().exists()
            && healingSkill.get(p.location().get()) < p.skill().skill(Skill.FIRSTAID)) {
          healingSkill.put(p.location().get(), p.skill().skill(Skill.FIRSTAID));
          p.activity(new BareActivity(Activity.HEAL));
        }
      }
    }
    // Don't let starving locations heal
    for (final Location p : i.location) {
      if (!p.type().isHospital() && p.foodDaysLeft() == 0 && p.lcs().siege.siege) {
        healingSkill.put(p, 0);
      }
    }
    // HEAL NON-CLINIC PEOPLE AND TRAIN
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      if (p.health().clinicTime() != 0) {
        // For people in LCS home treatment
        if (p.health().clinicMonths() != 0) {
          int damage = 0; // Amount health degrades
          boolean transfer = false; // needs to be treated professionally
          // Give experience to caretakers
          if (p.location().exists()) {
            healingExp.put(p.location().get(), healingExp.get(p.location().get()) + 100
                - p.health().blood());
          }
          // Cap blood at 100-injurylevel*20
          if (p.health().blood() < 100 - (p.health().clinicTime() - 1) * 20) {
            // Add health
            if (p.location().get() != Location.none()) {
              p.health().blood(p.health().blood() + 1 + healingSkill.get(p.location().get()) / 10);
            }
            if (p.health().blood() > 100 - (p.health().clinicTime() - 1) * 20) {
              p.health().blood(100 - (p.health().clinicTime() - 1) * 20);
            }
          }
          for (final BodyPart w : BodyPart.values()) {
            // Limbs blown off
            if (p.health().wounds().get(w).contains(Wound.NASTYOFF)) {
              /* Chance to stabilize/amputate wound Difficulty 12 (Will die if not treated) */
              if (p.location().get() != Location.none()
                  && healingSkill.get(p.location().get()) + i.rng.nextInt(10) > 12) {
                p.health().wounds().get(w).remove(Wound.NASTYOFF);
                p.health().wounds().get(w).add(Wound.CLEANOFF);
              }
              // Else take bleed damage (4)
              else {
                damage += 4;
                // release = 0;
                if (p.location().get() != Location.none()
                    && healingSkill.get(p.location().get()) + 9 <= 12) {
                  transfer = true;
                }
              }
            }
            // Bleeding wounds
            else if (p.health().wounds().get(w).contains(Wound.BLEEDING)) {
              /* Chance to stabilize wound Difficulty 8 (1 in 10 of happening naturally) */
              if (p.location().get() != Location.none()
                  && healingSkill.get(p.location().get()) + i.rng.nextInt(10) > 8) {
                // Toggle bleeding off
                p.health().wounds().get(w).remove(Wound.BLEEDING);
              } else {
                damage += 1;
                // release = 0;
              }
            } else if (p.health().blood() >= 95
                && p.health().wounds().get(w).contains(Wound.CLEANOFF)) {
              /* Erase wound if almost fully healed, but preserve loss of limbs. */
              p.health().wounds().get(w).clear();
              p.health().wounds().get(w).add(Wound.CLEANOFF);
            } else if (p.health().blood() >= 95) {
              p.health().wounds().get(w).clear();
            }
          }
          // Critical hit wounds
          for (final SpecialWounds sw : SpecialWounds.values()) { // TODO permanent injuries.
            // If wounded
            if (sw != SpecialWounds.TEETH && p.health().getWound(sw) != sw.defaultValue()
                && (sw == SpecialWounds.RIBS || p.health().getWound(sw) != 1)) {
              // Chance to stabilize wound
              if (p.location().get() != Location.none()
                  && healingSkill.get(p.location().get()) + i.rng.nextInt(10) > sw
                      .healingDifficulty()) {
                // Remove wound
                p.health().wound(sw, sw.defaultValue());
                if (sw.causesHealthDamage) {
                  /* May take permanent health damage depending on quality of care */
                  if (i.rng.nextInt(20) > healingSkill.get(p.location().get())) {
                    p.skill().attribute(Attribute.HEALTH, -1);
                    if (p.skill().getAttribute(Attribute.HEALTH, false) <= 0) {
                      p.skill().attribute(Attribute.HEALTH, 1);
                    }
                  }
                }
              } else { // Else take bleed damage
                damage += sw.bleed();
                if (healingSkill.get(p.location().get()) + 9 <= sw.healingDifficulty()) {
                  transfer = true;
                }
              }
            }
          }
          // Apply damage
          p.health().blood(p.health().blood() - damage);
          if (transfer && p.health().alive() && p.location().exists()
              && p.alignment() == Alignment.LIBERAL
              && !p.location().get().type().isType(University.class)) {
            fact(p.toString() + "'s injuries require professional treatment.");
            p.activity(new BareActivity(Activity.CLINIC));
          }
        }
      }
      if (!p.health().alive()) {
        fact(p.toString() + " has died of injuries.");
        continue;
      }
    }
    giveExperienceToMedics(healingExp);
  }

  private static void doHostages() {
    final List<Interrogation> todaysInterrogations = new ArrayList<Interrogation>(i.interrogations);
    for (final Interrogation in : todaysInterrogations) {
      in.tendhostage();
    }
  }

  private static void doMeetings() {
    // MEET WITH POTENTIAL RECRUITS
    final Map<Creature, AtomicInteger> meetings = new HashMap<Creature, AtomicInteger>();
    for (final Iterator<Recruit> ri = i.recruits.iterator(); ri.hasNext();) {
      final Recruit r = ri.next();
      if (i.disbanding()) {
        break;
      }
      final Creature p = r.recruiter;
      if (!meetings.containsKey(p)) {
        meetings.put(p, new AtomicInteger());
      }
      /* Stand up recruits if 1) recruiter does not exist, or 2) recruiter was not able to return to
       * a safehouse today */
      if (p.location().get() != Location.none()) {
        // MEET WITH RECRUIT
        meetings.get(p).addAndGet(1);
        // TERMINATE null RECRUIT MEETINGS
        if (p.location().exists() && p.location().get().lcs().siege.siege) {
          ri.remove();
          continue;
        } else if (meetings.get(p).intValue() > 5 && i.rng.likely(meetings.get(p).intValue() - 5)) {
          fact(p + " accidentally missed the meeting with " + r.recruit
              + "due to multiple booking of recruitment sessions.\n\nGet it together, " + p + '!');
          ri.remove();
          continue;
        } else if (r.completeRecruitMeeting()) {
          ri.remove();
          continue;
        }
      } else {
        ri.remove();
        continue;
      }
    }
  }

  private static void doRent() {
    if (i.score.date.day() == 3 && !i.disbanding()) {
      for (final Location l : i.location) {
        if (l.renting() == CrimeSquad.LCS && l.rent() > 0 && !l.lcs().newRental) {
          if (i.ledger.funds() >= l.rent()) {
            i.ledger.subtractFunds(l.rent(), Ledger.ExpenseType.RENT);
          } else {
            fact("EVICTION NOTICE: " + l.toString() + ".  Possessions go to the shelter.");
            l.renting(CrimeSquad.NO_ONE);
            final Location hs = AbstractSiteType.type(Shelter.class).getLocation();
            for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
              if (p.location().getNullable() == l) {
                p.location(hs);
              }
              if (p.base().getNullable() == l) {
                p.base(hs);
              }
            }
            hs.lcs().loot.addAll(l.lcs().loot);
            l.lcs().loot.clear();
            l.lcs().compoundWalls = EnumSet.noneOf(Compound.class);
            l.lcs().compoundStores = 0;
            l.lcs().frontBusiness = null;
          }
        }
      }
    }
  }

  private static void doVisiting() {
    // ADVANCE SQUADS
    final List<Vehicle> caridused = new ArrayList<Vehicle>();
    for (final Squad sq : i.squad) {
      if (i.disbanding()) {
        break;
      }
      // MAKE SURE MEMBERS DON'T ACT IF SQUAD DOES
      if (sq.activity().type() != Activity.NONE) {
        for (final Creature p : sq) {
          if (p.activity().type() != Activity.NONE && p.activity().type() != Activity.VISIT) {
            setView(R.layout.generic);
            ui().text(
                p.toString() + " acted with " + sq.toString() + " instead of "
                    + p.activity().toString() + ".").add();
            getch();
          }
          p.activity(sq.activity());
        }
      }
      if (sq.activity().type() == Activity.VISIT) {
        // TURN AWAY SQUADS FROM RECENTLY CLOSED OR SIEGED SITES
        final Location location = ((LocationActivity) sq.activity()).location();
        if (location.closed() || location.lcs().siege.siege) {
          fact(sq.toString() + " decided " + location + " was too hot to risk.");
          // ON TO THE NEXT SQUAD
          sq.activity(BareActivity.noActivity());
          continue;
        }
        // CAR UP AS NECESSARY
        final List<Vehicle> wantcar = new ArrayList<Vehicle>();
        for (final Creature p : sq) {
          Vehicle wid = p.prefCar();
          if (!i.vehicle.contains(wid)) {
            // that have been sold.
            p.prefCar(null);
            wid = null;
          }
          if (wid == null) {
            continue;
          }
          if (wantcar.contains(wid)) {
            continue;
          }
          wantcar.add(wid);
        }
        // CULL UNAVAILABLE CARS
        final Iterator<Vehicle> iter = wantcar.iterator();
        while (iter.hasNext()) {
          final Vehicle c = iter.next();
          if (caridused.contains(c)) {
            final Vehicle v = c;
            if (v != null) {
              fact(sq.toString() + " couldn't use the " + v.fullname(false) + ".");
            }
            iter.remove();
          }
        }
        // ASSIGN AVAILABLE CARS
        if (wantcar.size() > 0) {
          final List<Creature> driver = new ArrayList<Creature>();
          final List<Creature> passenger = new ArrayList<Creature>();
          for (final Vehicle w : wantcar) {
            driver.clear();
            passenger.clear();
            caridused.add(w);
            // FILL CAR WITH DESIGNATED DRIVERS AND PASSENGERS
            for (final Creature p : sq) {
              if (p.prefCar() == w) {
                p.car(w);
                p.driver(p.prefIsDriver());
                if (p.isDriver()) {
                  driver.add(p);
                } else {
                  passenger.add(p);
                }
              }
            }
            // NO DRIVER?
            if (driver.size() == 0) {
              // MAKE BEST DRIVING PASSENGER INTO A DRIVER
              if (passenger.size() > 0) {
                int max = 0;
                final List<Creature> goodp = new ArrayList<Creature>();
                for (final Creature p : passenger) {
                  final Maybe<Vehicle> v = p.car();
                  if (v.exists()) {
                    if (!p.health().canWalk()) {
                      continue;
                    }
                    final int ds = CarChase.driveSkill(p, v.get());
                    if (ds > max) {
                      max = ds;
                      goodp.clear();
                      goodp.add(p);
                    } else if (ds == max) {
                      goodp.add(p);
                    }
                  }
                }
                if (goodp.size() > 0) {
                  final Creature p = i.rng.randFromList(goodp);
                  p.driver(true);
                  driver.add(p);
                  passenger.remove(p);
                }
              }
            }
            // TOO MANY DRIVERS?
            else if (driver.size() > 1) {
              // TOSS ALL BUT THE BEST
              int max = 0;
              final List<Creature> goodp = new ArrayList<Creature>();
              for (final Creature p : driver) {
                final Maybe<Vehicle> v = p.car();
                if (v.exists()) {
                  if (!p.health().canWalk()) {
                    continue;
                  }
                  final int ds = CarChase.driveSkill(p, v.get());
                  if (ds > max) {
                    max = ds;
                    goodp.clear();
                    goodp.add(p);
                  } else if (ds == max) {
                    goodp.add(p);
                  }
                }
              }
              if (goodp.size() > 0) {
                final Creature p = i.rng.randFromList(goodp);
                for (final Creature p2 : driver) {
                  if (p2 == p) {
                    continue;
                  }
                  p2.driver(false);
                }
              }
            }
          }
          // PUT PEOPLE WITHOUT CARS INTO RANDOM CARS
          // THESE PEOPLE WILL NOT DRIVE
          for (final Creature p : sq) {
            if (p.car().missing()) {
              p.car(i.rng.randFromList(wantcar));
              p.driver(false);
            }
          }
        }
        // IF NEED CAR AND DON'T HAVE ONE...
        // NOTE: SQUADS DON'T TAKE FREE CARS
        if (location.needCar() && sq.size() > 0) {
          if (sq.member(0).car().missing()) {
            fact(sq + " didn't have a car to get to " + location + ".");
            // ON TO THE NEXT SQUAD
            sq.activity(BareActivity.noActivity());
            continue;
          }
        }
        // Give drivers experience if they actually travel
        if (location != sq.base().getNullable()) {
          for (final Creature j : sq) {
            if (j.car().exists() && j.isDriver()) {
              j.skill().train(Skill.DRIVING, 5);
            }
          }
        }
        // GO PLACES
        // switch (location.type) {
        if (location.type() instanceof IShop) {
          fact(sq.toString() + " has arrived at " + location.toString() + ".");
          i.setActiveSquad(sq);
          ((IShop) location.type()).shop(location);
          if (i.activeSquad().size() > 0) {
            i.activeSquad().location(i.activeSquad().base().get());
          }
        } else if (location.type() instanceof IHospital) {
          fact(sq.toString() + " has arrived at " + location.toString() + ".");
          i.setActiveSquad(sq);
          ((IHospital) location.type()).hospital(location);
          if (i.activeSquad().base().exists()) {
            i.activeSquad().location(i.activeSquad().base().get());
          }
        } else {
          setView(R.layout.generic);
          if (sq.base().getNullable() == location) {
            ui().text(sq.toString() + " looks around " + location.toString() + ".").add();
          } else {
            ui().text(sq.toString() + " has arrived at " + location + ".").add();
          }
          int c = 't';
          if (location.renting() != CrimeSquad.NO_ONE && location.type().isType(Warehouse.class)) {
            c = 's';
            ui(R.id.gcontrol).button(10).text("OK").add();
            getch();
          } else if (location.renting() == CrimeSquad.LCS && sq.base().getNullable() != location) {
            ui().text("Why is the squad here?").add();
            ui().button('s').text("Safe House").add();
            ui().button('t').text("Cause Trouble").add();
            ui().button('b').text("Both").add();
            do {
              c = getch();
            } while (c != 's' && c != 'b' && c != 't');
          } else {
            ui(R.id.gcontrol).button(10).text("OK").add();
            getch();
          }
          if (c == 's' || c == 'b') {
            sq.base(location);
          }
          if (c == 't' || c == 'b') {
            i.setActiveSquad(sq);
            final NewsStory ns = new NewsStory(StoryType.SQUAD_SITE);
            ns.positive = true;
            ns.location(location);
            i.newsStories.add(ns);
            Site.travelToSite(location);
          }
          i.activeSquad().activity(new BareActivity(Activity.NONE));
          if (i.activeSquad().base().exists()) {
            i.activeSquad().location(i.activeSquad().base().get());
          }
          break;
        }
        sq.activity(BareActivity.noActivity());
      }
    }
  }

  private static void giveExperienceToMedics(final Map<Location, Integer> healingExp) {
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      if (p.location().exists() && p.activity().type() == Activity.HEAL) {
        if (healingExp.get(p.location().getNullable()) == 0) {
          p.activity(BareActivity.noActivity());
        } else {
          p.skill().train(
              Skill.FIRSTAID,
              Math.max(0, healingExp.get(p.location().get()) / 5 - p.skill().skill(Skill.FIRSTAID)
                  * 2));
        }
      }
    }
  }

  /** @param p
   * @return */
  private static int locationSkill(final Location p) {
    // Clinic is equal to a skill 6 liberal
    if (p.type().isType(Clinic.class)) { // TODO NPE
      return 6;
    } else if (p.type().isType(University.class)) {
      return 12;
    }
    return 0;
  }
}
