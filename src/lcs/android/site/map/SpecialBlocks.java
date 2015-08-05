package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.UniqueWeapons;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Clip;
import lcs.android.items.Loot;
import lcs.android.items.Money;
import lcs.android.items.Weapon;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.Alienation;
import lcs.android.site.type.AmRadio;
import lcs.android.site.type.CableNews;
import lcs.android.util.Color;
import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Pair;

public @NonNullByDefault enum SpecialBlocks {
  APARTMENT_LANDLORD {
    @Override public void special() {
      // no action
    }
  },
  APARTMENT_SIGN {
    @Override public void special() {
      fact("The landlord's office is the first door on the left.");
    }
  },
  ARMYBASE_ARMORY {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You've found the armory. Break in?");
        if (c == 'y') {
          // clearmessagearea();
          i.site.alarm(true);
          ui().text("Alarms go off!").color(Color.RED).add();
          getch();
          boolean empty = true;
          AbstractItem<? extends AbstractItemType> it;
          if (!i.haveFoundUniqueWeapon(UniqueWeapons.M249)) {
            ui().text("Jackpot! The squad found a M249 Machine Gun!").add();
            getch();
            final Weapon de = new Weapon("WEAPON_M249_MACHINEGUN");
            final Clip r = new Clip("CLIP_DRUM");
            de.reload(r);
            i.activeSquad().loot().add(de);
            it = new Clip("CLIP_DRUM", 9);
            i.activeSquad().loot().add(it);
            i.findUniqueWeapon(UniqueWeapons.M249);
            empty = false;
          }
          if (i.rng.chance(2)) {
            ui().text("The squad finds some M16 Assault Rifles.").add();
            getch();
            int num = 0;
            do {
              final Weapon de = new Weapon("WEAPON_AUTORIFLE_M16");
              final Clip r = new Clip("CLIP_ASSAULT");
              de.reload(r);
              i.activeSquad().loot().add(de);
              it = new Clip("CLIP_ASSAULT", 5);
              i.activeSquad().loot().add(it);
              num++;
            } while (num < 2 || i.rng.chance(2) && num < 5);
            empty = false;
          }
          if (i.rng.chance(2)) {
            ui().text("The squad finds some M4 Carbines.").add();
            getch();
            int num = 0;
            do {
              final Weapon de = new Weapon("WEAPON_CARBINE_M4");
              final Clip r = new Clip("CLIP_ASSAULT");
              de.reload(r);
              i.activeSquad().loot().add(de);
              it = new Clip("CLIP_ASSAULT", 5);
              i.activeSquad().loot().add(it);
              num++;
            } while (num < 2 || i.rng.chance(2) && num < 5);
            empty = false;
          }
          if (i.rng.chance(2)) {
            ui().text("The squad finds some body armor.").add();
            getch();
            int num = 0;
            do {
              final Armor de = new Armor("ARMOR_ARMYARMOR");
              i.activeSquad().loot().add(de);
              num++;
            } while (num < 2 || i.rng.chance(2) && num < 5);
            empty = false;
          }
          if (empty) {
            i.activeSquad().criminalizeParty(Crime.TREASON);
            ui().text("It's a trap!  The armory is empty.").add();
            getch();
            int numleft = i.rng.nextInt(8) + 2;
            do {
              i.currentEncounter().makeEncounterCreature("SOLDIER");
              numleft--;
            } while (numleft > 0);
          } else {
            i.activeSquad().juice(50, 1000);
            i.site.crime(i.site.crime() + 40);
            i.siteStory.addNews(NewsEvent.ARMY_ARMORY);
            i.activeSquad().criminalizeParty(Crime.THEFT);
            i.activeSquad().criminalizeParty(Crime.TREASON);
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            ui().text("Time to put this gear to use!").add();
            getch();
            int numleft = i.rng.nextInt(4) + 2;
            do {
              i.currentEncounter().makeEncounterCreature("SOLDIER");
              numleft--;
            } while (numleft > 0);
          }
          i.site.alienationCheck(false);
          i.site.noticeCheck();
          i.site.currentTile().special = null;
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  CAFE_COMPUTER {
    @Override public void special() {
      // no action
    }
  },
  CLUB_BOUNCER {
    @Override public void special() {
      // no action
    }
  },
  CLUB_BOUNCER_SECONDVISIT {
    @Override public void special() {
      // no action
    }
  }, //
  CORPORATE_FILES {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You've found a safe. Open it?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.SAFE.unlock();
          if (actual.succeeded()) {
            // clearmessagearea();
            ui().text("The Squad has found some very interesting files.").add();
            AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_CORPFILES");
            i.activeSquad().loot().add(it);
            it = new Loot("LOOT_CORPFILES");
            i.activeSquad().loot().add(it);
            i.activeSquad().juice(50, 1000);
            i.site.crime(i.site.crime() + 40);
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            getch();
          }
          if (actual.madeNoise()) {
            i.site.alienationCheck(false);
            i.site.noticeCheck();
            i.site.currentTile().special = null;
            i.site.crime(i.site.crime() + 3);
            i.siteStory.addNews(NewsEvent.CORP_FILES);
            i.activeSquad().criminalizeParty(Crime.THEFT);
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  COURTHOUSE_JURYROOM {
    @Override public void special() {
      if (i.site.alarm() || i.site.alienate() != Alienation.NONE) {
        fact("It appears as if this room has been vacated in a hurry.");
        return;
      }
      do {
        final int c = yesOrNo("You've found a Jury in deliberations! Attempt to influence them?");
        if (c == 'y') {
          i.site.currentTile().special = null;
          boolean succeed = false;
          int maxattack = 0;
          Creature maxp = null;
          for (final Creature p : i.activeSquad()) {
            if (p.health().alive()) {
              if (p.skill().getAttribute(Attribute.CHARISMA, true)
                  + p.skill().getAttribute(Attribute.INTELLIGENCE, true)
                  + p.skill().skill(Skill.PERSUASION) + p.skill().skill(Skill.LAW) > maxattack) {
                maxattack = p.skill().getAttribute(Attribute.CHARISMA, true)
                    + p.skill().getAttribute(Attribute.INTELLIGENCE, true)
                    + p.skill().skill(Skill.PERSUASION) + p.skill().skill(Skill.LAW);
                maxp = p;
              }
            }
          }
          if (maxp != null) {
            final Creature p = maxp;
            p.skill().train(Skill.PERSUASION, 20);
            p.skill().train(Skill.LAW, 20);
            if (p.skill().skillCheck(Skill.PERSUASION, CheckDifficulty.HARD)
                && p.skill().skillCheck(Skill.LAW, CheckDifficulty.CHALLENGING)) {
              succeed = true;
            }
            if (succeed) {
              // clearmessagearea();
              ui().text(
                  p.toString()
                      + " works the room like in Twelve Angry Men, and the jury concludes that ")
                  .add();// XXX:
              // This
              // is
              // very
              // awkward
              // grammar.
              switch (i.rng.nextInt(16)) // Fixed. -Fox
              {
              default:
              case 0:
                ui().text("murder").add();
                break;
              case 1:
                ui().text("assault").add();
                break;
              case 2:
                ui().text("theft").add();
                break;
              case 3:
                ui().text("mugging").add();
                break;
              case 4:
                ui().text("burglary").add();
                break;
              case 5:
                ui().text("property destruction").add();
                break;
              case 6:
                ui().text("vandalism").add();
                break;
              case 7:
                ui().text("libel").add();
                break;
              case 8:
                ui().text("slander").add();
                break;
              case 9:
                ui().text("sodomy").add();
                break;
              case 10:
                ui().text("obstruction of justice").add();
                break;
              case 11:
                ui().text("breaking and entering").add();
                break;
              case 12:
                ui().text("public indecency").add();
                break;
              case 13:
                ui().text("arson").add();
                break;
              case 14:
                ui().text("resisting arrest").add();
                break;
              case 15:
                ui().text("tax evasion").add();
                break;
              }
              ui().text(" wasn't really wrong here.").add();
              getch();
              i.site.alienationCheck(false);
              i.site.noticeCheck();
              // INSTANT JUICE BONUS
              p.addJuice(25, 200);
            } else {
              ui().text(p.toString() + " wasn't quite convincing...").add();
              getch();
              int numleft = 12;
              do {
                i.currentEncounter().makeEncounterCreature("JUROR");
                numleft--;
              } while (numleft > 0);
              i.currentEncounter().printEncounter();
              i.site.alarm(true).alienate(Alienation.ALL).crime(i.site.crime() + 10);
              i.siteStory.addNews(NewsEvent.JURY_TAMPERING);
              i.activeSquad().criminalizeParty(Crime.JURY);
            }
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  COURTHOUSE_LOCKUP {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You see prisoners in the Court House jail. Free them?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.CELL.unlock();
          if (actual.succeeded()) {
            int numleft = i.rng.nextInt(8) + 2;
            do {
              i.currentEncounter().makeEncounterCreature("PRISONER");
              numleft--;
            } while (numleft > 0);
            i.activeSquad().juice(50, 1000);
            i.site.crime(i.site.crime() + 20);
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            i.currentEncounter().printEncounter();
            MapSpecials.partyrescue();
          }
          if (actual.madeNoise()) {
            i.site.alienationCheck(true);
            i.site.noticeCheck(CheckDifficulty.HARD);
            i.site.currentTile().special = null;
            i.site.crime(i.site.crime() + 3);
            i.siteStory.addNews(NewsEvent.COURTHOUSE_LOCKUP);
            i.activeSquad().criminalizeParty(Crime.HELPESCAPE);
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  HOUSE_CEO {
    @Override public void special() {
      // no action
    }
  },
  HOUSE_PHOTOS {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You've found a safe. Open it?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.SAFE.unlock();
          if (actual.succeeded()) {
            boolean empty = true;
            AbstractItem<? extends AbstractItemType> it;
            if (!i.haveFoundUniqueWeapon(UniqueWeapons.DEAGLE)) {
              // clearmessagearea();
              ui().text("The squad has found a Desert Eagle.").add();
              getch();
              final Weapon de = new Weapon("WEAPON_DESERT_EAGLE");
              final Clip r = new Clip("CLIP_50AE");
              de.reload(r);
              i.activeSquad().loot().add(de);
              it = new Clip("CLIP_50AE", 9);
              i.activeSquad().loot().add(it);
              i.findUniqueWeapon(UniqueWeapons.DEAGLE);
              empty = false;
            }
            if (i.rng.chance(2)) {
              // clearmessagearea();
              ui().text("This guy sure had a lot of $100 bills.").add();
              getch();
              it = new Money(1000 * (1 + i.rng.nextInt(10)));
              i.activeSquad().loot().add(it);
              empty = false;
            }
            if (i.rng.chance(2)) {
              // clearmessagearea();
              ui().text("Hmm... there is also some very expensive-looking").add();
              ui().text("jewelery here.  The squad will take that.").add();
              getch();
              it = new Loot("LOOT_EXPENSIVEJEWELERY", 3);
              i.activeSquad().loot().add(it);
              empty = false;
            }
            if (i.rng.chance(3)) {
              // clearmessagearea();
              ui().text("There are some... very compromising photos here.").add();
              getch();
              it = new Loot("LOOT_CEOPHOTOS");
              i.activeSquad().loot().add(it);
              empty = false;
            }
            if (i.rng.chance(3)) {
              // clearmessagearea();
              ui().text("There are some drugs here.").add();
              getch();
              empty = false;
            }
            if (i.rng.chance(3)) {
              // clearmessagearea();
              ui().text("Wow, get a load of these love letters.").add();
              ui().text("The squad will take those.").add();
              getch();
              it = new Loot("LOOT_CEOLOVELETTERS");
              i.activeSquad().loot().add(it);
              empty = false;
            }
            if (i.rng.chance(3)) {
              // clearmessagearea();
              ui().text("These documents show serious tax evasion.").add();
              getch();
              it = new Loot("LOOT_CEOTAXPAPERS");
              i.activeSquad().loot().add(it);
              empty = false;
            }
            if (empty) {
              // clearmessagearea();
              ui().text("Wow, it's empty.  That sucks.").add();
              getch();
            } else {
              i.activeSquad().juice(50, 1000);
              i.site.crime(i.site.crime() + 40);
              i.siteStory.addNews(NewsEvent.HOUSE_PHOTOS);
              i.activeSquad().criminalizeParty(Crime.THEFT);
              int time = 20 + i.rng.nextInt(10);
              if (time < 1) {
                time = 1;
              }
              if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
                i.site.alarmTimer(time);
              }
            }
          }
          if (actual.madeNoise()) {
            i.site.alienationCheck(false);
            i.site.noticeCheck();
            i.site.currentTile().special = null;
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  INTEL_SUPERCOMPUTER {
    @Override public void special() {
      if (i.site.alarm() || i.site.alienate() != Alienation.NONE) {
        // clearmessagearea();
        ui().text("The security alert has caused the").add();
        ui().text("computer to shut down.").add();
        getch();
        return;
      }
      do {
        // clearmessagearea();
        final int c = yesOrNo("You've found the Intelligence Super Computer. Hack it?");
        if (c == 'y') {
          final Pair<Boolean, Boolean> actual = Hackable.SUPERCOMPUTER.hack();
          if (actual.first) {
            // clearmessagearea();
            ui().text("The Squad obtains sensitive information.").add();
            i.activeSquad().juice(50, 1000);
            final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_INTHQDISK");
            i.activeSquad().loot().add(it);
            getch();
          }
          if (actual.second) {
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            i.site.alienationCheck(true);
            i.site.noticeCheck(CheckDifficulty.HARD);
            i.site.currentTile().special = null;
            i.site.crime(i.site.crime() + 3);
            i.siteStory.addNews(NewsEvent.HACK_INTEL);
            i.activeSquad().criminalizeParty(Crime.TREASON);
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  LAB_COSMETICS_CAGEDANIMALS {
    @Override public void special() {
      do {
        final int c = yesOrNo("You see fluffy white rabbits in a locked cage. Free them?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.CAGE.unlock();
          if (actual.succeeded()) {
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            i.site.crime(i.site.crime() + 1);
            i.activeSquad().juice(3, 100);
            i.siteStory.addNews(NewsEvent.FREE_RABBITS);
            i.activeSquad().criminalizeParty(Crime.VANDALISM);
          }
          if (actual.madeNoise()) {
            i.site.alienationCheck(false);
            i.site.noticeCheck();
            i.site.currentTile().special = null;
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  LAB_GENETIC_CAGEDANIMALS {
    @Override public void special() {
      do {
        final int c = yesOrNo("You see horrible misshapen creatures in a sealed cage. Free them?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.CAGE_HARD.unlock();
          if (actual.succeeded()) {
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            i.site.crime(i.site.crime() + 1);
            i.activeSquad().juice(5, 200);
            i.siteStory.addNews(NewsEvent.FREE_BEASTS);
            i.activeSquad().criminalizeParty(Crime.VANDALISM);
            if (i.rng.chance(2)) {
              // clearmessagearea();
              ui().text("Uh, maybe that idea was Conservative in retrospect...").add();
              int numleft = i.rng.nextInt(6) + 1;
              do {
                i.currentEncounter().makeEncounterCreature("GENETIC");
                numleft--;
              } while (numleft > 0);
              i.currentEncounter().printEncounter();
              getch();
              i.site.alarm(true);
              i.site.alienationCheck(true);
            } else {
              i.site.alienationCheck(false);
            }
          } else if (actual.madeNoise()) {
            i.site.noticeCheck();
          }
          if (actual.madeNoise()) {
            i.site.currentTile().special = null;
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  }, //
  NEWS_BROADCASTSTUDIO {
    @Override public void special() {
      do {
        int c = 0;
        if (i.site.alarm() || i.site.alienate() != Alienation.NONE) {
          c = yesOrNo("The Cable News broadcasters left the equipment on in their rush to get out. Take over the studio?");
        } else {
          c = yesOrNo("You've found a Cable News broadcasting studio. Start an impromptu news program?");
        }
        if (c == 'y') {
          if (CableNews.newsBroadcast()) {
            i.siteStory.claimed(true); // 2;
            i.site.currentTile().special = null;
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  NUCLEAR_ONOFF {
    @Override public void special() {
      do {
        // clearmessagearea();
        int c = 0;
        if (i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
          c = yesOrNo("You see the nuclear waste center control room.  Attempt to release nuclear waste?");
        } else {
          c = yesOrNo("You see the nuclear power plant control room. Attempt to shut down the reactor?");
        }
        if (c == 'y') {
          // clearmessagearea();
          i.site.currentTile().special = null;
          final CheckDifficulty max = CheckDifficulty.HARD;
          Creature maxs = null;
          for (final Creature p : i.activeSquad()) {
            if (p.health().alive()) {
              if (p.skill().skillCheck(Skill.SCIENCE, max)) {
                maxs = p;
                break;
              }
            }
          }
          if (maxs != null) {
            ui().text(maxs.toString()).add();
            ui().text(" presses the big red button!").add();
            getch();
            ui().text(".").add();
            getch();
            ui().text(".").add();
            getch();
            ui().text(".").add();
            getch();
            if (i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
              ui().text("The nuclear waste gets released into the state's water supply!").add();
              i.issue(Issue.NUCLEARPOWER).changeOpinion(15, 0, 95);
              i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(-100, 0, 0);
              getch();
              i.activeSquad().juice(40, 1000); // Instant juice!
              i.siteStory.addNews(NewsEvent.SHUTDOWN_REACTOR);
            } else {
              ui().text("The lights dim...  power must be out state-wide.").add();
              i.issue(Issue.NUCLEARPOWER).changeOpinion(15, 0, 95);
              getch();
              i.activeSquad().juice(100, 1000); // Instant juice!
              i.siteStory.addNews(NewsEvent.SHUTDOWN_REACTOR);
            }
          } else {
            ui().text("After some failed attempts, and a very loud alarm,").add();
            ui().text("the Squad resigns to just leaving a threatening note.").add();
            getch();
            i.activeSquad().juice(15, 500);
          }
          i.site.alarm(true);
          i.site.alienationCheck(true);
          i.site.currentTile().special = null;
          i.site.crime(i.site.crime() + 5);
          i.activeSquad().criminalizeParty(Crime.TERRORISM);
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  PARK_BENCH {
    @Override public void special() {
      // no action
    }
  },
  POLICESTATION_LOCKUP {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You see prisoners in the detention room. Free them?");
        if (c == 'y') {
          final SuccessTest actual = Unlockable.CELL.unlock();
          if (actual.succeeded()) {
            int numleft = i.rng.nextInt(8) + 2;
            do {
              i.currentEncounter().makeEncounterCreature("PRISONER");
              numleft--;
            } while (numleft > 0);
            i.activeSquad().juice(50, 1000);
            i.site.crime(i.site.crime() + 20);
            int time = 20 + i.rng.nextInt(10);
            if (time < 1) {
              time = 1;
            }
            if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
              i.site.alarmTimer(time);
            }
            i.currentEncounter().printEncounter();
            MapSpecials.partyrescue();
          }
          if (actual.madeNoise()) {
            i.site.alienationCheck(true);
            i.site.noticeCheck(CheckDifficulty.HARD);
            i.site.currentTile().special = null;
            i.site.crime(i.site.crime() + 2);
            i.siteStory.addNews(NewsEvent.POLICE_LOCKUP);
            i.activeSquad().criminalizeParty(Crime.HELPESCAPE);
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  POLLUTER_EQUIPMENT {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = Curses.yesOrNo("You see some industrial equipment. Destroy it?");
        if (c == 'y') {
          int time = 20 + i.rng.nextInt(10);
          if (time < 1) {
            time = 1;
          }
          if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
            i.site.alarmTimer(time);
          }
          i.issue(Issue.POLLUTION).changeOpinion(2, 1, 70);
          i.site.alienationCheck(false);
          i.site.noticeCheck(CheckDifficulty.HEROIC);
          i.site.currentTile().special = null;
          i.site.crime(i.site.crime() + 2);
          i.activeSquad().juice(5, 100);
          i.siteStory.addNews(NewsEvent.BREAK_FACTORY);
          i.activeSquad().criminalizeParty(Crime.VANDALISM);
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  PRISON_CONTROL {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = Curses.yesOrNo("You've found the prison control room. Free the prisoners?");
        if (c == 'y') {
          int numleft = i.rng.nextInt(8) + 2;
          do {
            i.currentEncounter().makeEncounterCreature("PRISONER");
            numleft--;
          } while (numleft > 0);
          int time = 20 + i.rng.nextInt(10);
          if (time < 1) {
            time = 1;
          }
          if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
            i.site.alarmTimer(time);
          }
          i.currentEncounter().printEncounter();
          MapSpecials.partyrescue();
          i.site.alienationCheck(true);
          i.site.noticeCheck();
          i.site.currentTile().special = null;
          i.site.crime(i.site.crime() + 30);
          i.activeSquad().juice(50, 1000);
          i.siteStory.addNews(NewsEvent.PRISON_RELEASE);
          i.activeSquad().criminalizeParty(Crime.HELPESCAPE);
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  RADIO_BROADCASTSTUDIO {
    @Override public void special() {
      do {
        // clearmessagearea();
        int c = 0;
        if (i.site.alarm() || i.site.alienate() != Alienation.NONE) {
          c = yesOrNo("The radio broadcasters left the equipment on in their rush to get out. Take over the studio?");
        } else {
          c = yesOrNo("You've found a radio broadcasting room. Interrupt this evening's programming?");
        }
        if (c == 'y') {
          if (AmRadio.radioBroadcast()) {
            i.siteStory.claimed(true); // 2;
            i.site.currentTile().special = null;
          }
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  },
  RESTAURANT_TABLE {
    @Override public void special() {
      // no action
    }
  },
  STAIRS_DOWN {
    @Override public void special() {
      i.site.locz--;
    }
  },
  STAIRS_UP {
    @Override public void special() {
      i.site.locz++;
    }
  },
  SWEATSHOP_EQUIPMENT {
    @Override public void special() {
      do {
        // clearmessagearea();
        final int c = yesOrNo("You see some textile equipment. Destroy it?");
        if (c == 'y') {
          int time = 20 + i.rng.nextInt(10);
          if (time < 1) {
            time = 1;
          }
          if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
            i.site.alarmTimer(time);
          }
          i.site.alienationCheck(false);
          i.site.noticeCheck(CheckDifficulty.HEROIC);
          i.site.currentTile().special = null;
          i.site.crime(i.site.crime() + 1);
          i.activeSquad().juice(5, 100);
          i.siteStory.addNews(NewsEvent.BREAK_SWEATSHOP);
          i.activeSquad().criminalizeParty(Crime.VANDALISM);
          return;
        } else if (c == 'n')
          return;
      } while (true);
    }
  };
  /** Activate the special feature at this location. */
  public abstract void special();

  /** Whether a 'use' button should be enabled at the location.
   * @return */
  public boolean usable() {
    switch (this) {
    case CLUB_BOUNCER:
    case CLUB_BOUNCER_SECONDVISIT:
    case APARTMENT_LANDLORD:
      return false;
    default:
      return true;
    }
  }
}