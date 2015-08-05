package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureType;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Clip;
import lcs.android.items.Weapon;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.PoliceStation;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Date implements Serializable {
  private enum DateResult {
    ARRESTED,
    BREAKUP,
    JOINED,
    MEETTOMORROW
  }

  public Date(Creature dater) {
    this.dater = dater;
  }

  public final Creature dater;

  public final List<Creature> dates = new ArrayList<Creature>();

  int timeleft = 0;

  /* daily - date - dater p goes on some dates */
  boolean completedate(final Creature p) {
    final StringBuilder sb = new StringBuilder();
    if (dates.size() == 1) {
      if (p.health().clinicMonths() > 0) {
        sb.append(p.toString() + " has a \"hot\" date with ");
      } else {
        sb.append(p.toString() + " has a hot date with ");
      }
    } else {
      sb.append(p.toString() + " has dates to manage with ");
    }
    for (int e = 0; e < dates.size(); e++) {
      sb.append(dates.get(e).toString());
      if (e <= dates.size() - 3) {
        sb.append(", ");
      } else if (e == dates.size() - 2) {
        sb.append(" and ");
      } else {
        if (p.health().clinicMonths() > 0 && p.location()!= null) {
          sb.append(" at ").append(p.location().toString());
        }
        sb.append('.');
      }
    }
    setView(R.layout.generic);
    ui().text(sb.toString()).add();
    if (dates.size() > 1 && i.rng.chance(dates.size() > 2 ? 4 : 6)) {
      return badDate(p);
    }
    for (final Iterator<Creature> di = new ArrayList<Creature>(dates).iterator(); di.hasNext();) {
      final Creature date = di.next();
      do {
        setView(R.layout.date);
        setText(R.id.dateTitle, "Seeing " + date.toString() + ", " + date.alignment());
        setText(R.id.dateMoney, "Money: " + i.ledger.funds());
        final List<AbstractItem<? extends AbstractItemType>> temp = new ArrayList<AbstractItem<? extends AbstractItemType>>();
        date.weapon().dropWeaponsAndClips(temp);
        final Armor atmp = new Armor("ARMOR_CLOTHES");
        date.giveArmor(atmp, temp);
        date.printCreatureInfo(255);
        for (final AbstractItem<? extends AbstractItemType> ai : temp) {
          if (ai instanceof Weapon) {
            date.weapon().giveWeapon((Weapon) ai, null);
          } else if (ai instanceof Armor) {
            date.giveArmor((Armor) ai, null);
          } else if (ai instanceof Clip) {
            date.weapon().takeClips((Clip) ai, ai.number());
          }
        }
        ui().text("How should " + p.toString() + " approach the situation?").add();
        maybeAddButton(R.id.gcontrol, 'a',
            "Spend a hundred bucks tonight to get the ball rolling.", i.ledger.funds() >= 100
                && p.health().clinicMonths() == 0); // FIXME check
        ui(R.id.gcontrol).button('b')
            .text("Try to get through the evening without spending a penny.").add();
        if (p.health().clinicMonths() == 0 && p.health().blood() == 100) {
          ui(R.id.gcontrol).button('c')
              .text("Spend a week on a cheap vacation (stands up any other dates).").add();
        } else {
          ui(R.id.gcontrol).button().text("Spend a week on a cheap vacation (must be uninjured).")
              .add();
        }
        ui(R.id.gcontrol).button('d').text("Break it off.").add();
        if (date.alignment() == Alignment.CONSERVATIVE && p.health().clinicMonths() == 0) {
          ui(R.id.gcontrol).button('e').text("Just kidnap the Conservative bitch.").add();
        }
        int thingsincommon = 0;
        for (final Skill s : Skill.values()) {
          if (date.skill().skill(s) >= 1 && p.skill().skill(s) >= 1) {
            // Has a skill that is between double and half the same
            // skill of the other person on the date.
            if (date.skill().skill(s) <= p.skill().skill(s) * 2
                && date.skill().skill(s) * 2 >= p.skill().skill(s)) {
              thingsincommon++;
            }
          }
        }
        final int c = getch();
        clearChildren(R.id.gcontrol);
        int aroll = p.skill().skillRoll(Skill.SEDUCTION);
        int troll = date.skill().attributeRoll(Attribute.WISDOM);
        if (date.alignment() == Alignment.CONSERVATIVE) {
          troll += troll * (date.juice() / 100);
        }
        boolean test = false;
        aroll += thingsincommon * 3;
        if (c == 'a' && i.ledger.funds() >= 100 && p.health().clinicMonths() == 0) {
          i.ledger.subtractFunds(100, Ledger.ExpenseType.DATING);
          aroll += i.rng.nextInt(10);
          test = true;
          p.skill().train(Skill.SEDUCTION, i.rng.nextInt(4) + 5);
          p.skill().train(Skill.SCIENCE,
              Math.max(date.skill().skill(Skill.SCIENCE) - p.skill().skill(Skill.SCIENCE), 0));
          p.skill().train(Skill.RELIGION,
              Math.max(date.skill().skill(Skill.RELIGION) - p.skill().skill(Skill.RELIGION), 0));
          p.skill().train(Skill.BUSINESS,
              Math.max(date.skill().skill(Skill.BUSINESS) - p.skill().skill(Skill.BUSINESS), 0));
        }
        if (c == 'b') {
          test = true;
          p.skill().train(Skill.SEDUCTION, i.rng.nextInt(4) + 5);
          p.skill().train(Skill.SCIENCE,
              Math.max(date.skill().skill(Skill.SCIENCE) - p.skill().skill(Skill.SCIENCE), 0));
          p.skill().train(Skill.RELIGION,
              Math.max(date.skill().skill(Skill.RELIGION) - p.skill().skill(Skill.RELIGION), 0));
          p.skill().train(Skill.BUSINESS,
              Math.max(date.skill().skill(Skill.BUSINESS) - p.skill().skill(Skill.BUSINESS), 0));
        }
        if (date.skill().skill(Skill.BUSINESS) > 0) {
          troll += date.skill().skillRoll(Skill.BUSINESS);
          aroll += p.skill().skillRoll(Skill.BUSINESS);
        }
        if (date.skill().skill(Skill.RELIGION) > 0) {
          troll += date.skill().skillRoll(Skill.RELIGION);
          aroll += p.skill().skillRoll(Skill.RELIGION);
        }
        if (date.skill().skill(Skill.SCIENCE) > 0) {
          troll += date.skill().skillRoll(Skill.SCIENCE);
          aroll += p.skill().skillRoll(Skill.SCIENCE);
        }
        if (test) {
          final DateResult result = dateresult(aroll, troll, date, p);
          if (result == DateResult.ARRESTED) {
            return true;
          }
          break;
        }
        if (c == 'c' && p.health().clinicMonths() != 0 && p.health().blood() == 100) {
          dates.clear();
          dates.add(date);
          timeleft = 7;
          p.skill().train(Skill.SEDUCTION, i.rng.nextInt(40) + 15);
          p.skill()
              .train(
                  Skill.SCIENCE,
                  Math.max(
                      (date.skill().skill(Skill.SCIENCE) - p.skill().skill(Skill.SCIENCE)) * 4, 0));
          p.skill().train(
              Skill.RELIGION,
              Math.max((date.skill().skill(Skill.RELIGION) - p.skill().skill(Skill.RELIGION)) * 4,
                  0));
          p.skill().train(
              Skill.BUSINESS,
              Math.max((date.skill().skill(Skill.BUSINESS) - p.skill().skill(Skill.BUSINESS)) * 4,
                  0));
          return false;
        }
        if (c == 'd') {
          dates.remove(date);
          break;
        }
        if (c == 'e' && date.alignment() == Alignment.CONSERVATIVE
            && p.health().clinicMonths() == 0) {
          int bonus = 0;
          if (p.weapon().weapon().isRanged()) {
            ui().text(
                p.toString() + " comes back from the bathroom toting the "
                    + p.weapon().weapon().toString()
                    + "and threatens to blow the Conservative's brains out!").color(Color.YELLOW)
                .add();
            bonus = 5;
          } else if (p.weapon().isArmed()) {
            ui().text(
                p.toString() + " grabs the Conservative from behind, holding the "
                    + p.weapon().weapon().toString() + "to the corporate slave's throat!").add();
            bonus = 2;
          } else {
            ui().text(
                p.toString() + " seizes the Conservative swine from behind and warns it "
                    + (i.freeSpeech() ? "not to fuck around!" : "not to [resist]!")).add();
          }
          // getch();
          // *JDS* Kidnap succeeds if the conservative isn't very
          // dangerous,
          // but might fail if the conservative is tough stuff.
          // TODO creaturetypes should have a 'dangerous' property.
          if (date.type() != CreatureType.valueOf("AGENT")
              && date.type() != CreatureType.valueOf("COP")
              && date.type() != CreatureType.valueOf("SWAT")
              && date.type() != CreatureType.valueOf("GANGUNIT")
              && date.type() != CreatureType.valueOf("DEATHSQUAD")
              && date.type() != CreatureType.valueOf("SOLDIER")
              && date.type() != CreatureType.valueOf("VETERAN")
              && date.type() != CreatureType.valueOf("HARDENED_VETERAN")
              && date.type() != CreatureType.valueOf("CCS_VIGILANTE")
              && date.type() != CreatureType.valueOf("CCS_ARCHCONSERVATIVE")
              && date.type() != CreatureType.valueOf("CCS_MOLOTOV")
              && date.type() != CreatureType.valueOf("CCS_SNIPER")
              && date.type() != CreatureType.valueOf("MERC") && i.rng.likely(15)
              || i.rng.likely(1 + bonus)) {
            if (bonus > 0) {
              ui().text(date.toString() + " doesn't resist.").color(Color.GREEN).add();
            } else {
              ui().text(date.toString() + " struggles and yells for help, but nobody comes.")
                  .color(Color.GREEN).add();
            }
            ui().text(p.toString() + " kidnaps the Conservative!").add();
            getch();
            // Create interrogation data
            Interrogation.create(date, p);
            dates.remove(date);
            break;
          } else if (i.rng.chance(2)) {
            ui().text(date.toString() + " manages to get away on the way back to the safehouse!")
                .add();
            ui().text(p.toString() + " has failed to kidnap the Conservative.").add();
            // Charge with kidnapping
            p.crime().criminalize(Crime.KIDNAPPING);
            getch();
            dates.remove(date);
            break;
          } else {
            ui().text(date + "'s fist is the last thing " + p.toString() + " remembers seeing!")
                .color(Color.RED).add();
            ui().text("The Liberal wakes up in the police station...").add();
            // Find the police station
            final Location ps = AbstractSiteType.type(PoliceStation.class).getLocation();
            // Arrest the Liberal
            p.removeSquadInfo();
            p.car(null);
            p.location(ps);
            p.weapon().dropWeaponsAndClips(null);
            p.activity(BareActivity.noActivity());
            // Charge with kidnapping
            p.crime().criminalize(Crime.KIDNAPPING);
            getch();
            dates.remove(date);
            return true;
          }
        }
      } while (true);
    }
    if (dates.size() > 0) {
      timeleft = 0;
      return false;
    }
    return true;
  }

  boolean completeVacation(final Creature p) {
    final Creature date = dates.get(0);
    ui().text(p.toString() + " is back from vacation.").add();
    int aroll = p.skill().skillRoll(Skill.SEDUCTION) * 2;
    int troll = date.skill().attributeRoll(Attribute.WISDOM);
    p.skill().train(Skill.SEDUCTION, i.rng.nextInt(11) + 15);
    p.skill().train(Skill.SCIENCE,
        Math.max(date.skill().skill(Skill.SCIENCE) - p.skill().skill(Skill.SCIENCE), 0));
    p.skill().train(Skill.RELIGION,
        Math.max(date.skill().skill(Skill.RELIGION) - p.skill().skill(Skill.RELIGION), 0));
    p.skill().train(Skill.BUSINESS,
        Math.max(date.skill().skill(Skill.BUSINESS) - p.skill().skill(Skill.BUSINESS), 0));
    if (date.skill().skillRoll(Skill.BUSINESS) > 0) {
      troll += date.skill().skillRoll(Skill.BUSINESS);
      aroll += p.skill().skillRoll(Skill.BUSINESS);
    }
    if (date.skill().skillRoll(Skill.RELIGION) > 0) {
      troll += date.skill().skillRoll(Skill.RELIGION);
      aroll += p.skill().skillRoll(Skill.RELIGION);
    }
    if (date.skill().skillRoll(Skill.SCIENCE) > 0) {
      troll += date.skill().skillRoll(Skill.SCIENCE);
      aroll += p.skill().skillRoll(Skill.SCIENCE);
    }
    return dateresult(aroll, troll, date, p) != DateResult.MEETTOMORROW;
  }

  private boolean badDate(final Creature p) {
    switch (i.rng.nextInt(3)) {
    case 0:
      if (dates.size() > 2) {
        ui().text(
            "Unfortunately, they all know each other and had been discussing " + p.toString()
                + ".  An ambush was set for the lying dog...").add();
      } else {
        ui().text(
            "Unfortunately, they know each other and had been discussing " + p.toString()
                + ".  An ambush was set for the lying dog...").add();
      }
      break;
    case 1:
      if (dates.size() > 2) {
        ui().text("Unfortunately, they all turn up at the same time.").add();
      } else {
        ui().text("Unfortunately, they turn up at the same time.").add();
      }
      ui().text("Uh oh...").add();
      break;
    default:
      if (dates.size() > 2) {
        ui().text(
            p.toString() + " realizes " + p.genderLiberal().heShe() + " has commited to eating "
                + dates.size() + " meals at once.").add();
      } else {
        ui().text(
            p.toString() + " mixes up the names of " + dates.get(0).toString() + " and "
                + dates.get(1).toString()).add();
      }
      ui().text("Things go downhill fast.").add();
      break;
    }
    switch (i.rng.nextInt(3)) {
    case 0:
      ui().text(p.toString() + " is publically humiliated.").add();
      break;
    case 1:
      ui().text(p.toString() + " runs away.").add();
      break;
    default:
      ui().text(p.toString() + " escapes through the bathroom window.").add();
      break;
    }
    p.addJuice(-5, -50);
    getch();
    return true;
  }

  private DateResult dateresult(final int aroll, final int troll, final Creature date,
      final Creature lcs) {
    if (aroll > troll) {
      ui().text(
          date.toString() + " is quite taken with " + lcs.toString()
              + "'s unique life philosophy...").color(Color.BLUE).add();
      if (i.rng.nextInt(date.skill().getAttribute(Attribute.HEART, false) + (aroll - troll) / 2) > date
          .skill().getAttribute(Attribute.WISDOM, false)) {
        if (lcs.loveSlavesLeft() == 0) {
          ui().text(
              "But when " + lcs.toString()
                  + " mentions having other lovers, things go downhill fast.").color(Color.RED)
              .add();
          ui().text("This relationship is over.").color(Color.RED).add();
          getch();
          dates.remove(date);
          return DateResult.BREAKUP;
        }
        ui().text(
            "In fact, " + date.toString() + " is " + lcs.toString()
                + "'s totally unconditional love-slave!").color(Color.GREEN).add();
        // Get map of their workplace
        date.workLocation().interrogated(true);
        date.workLocation().hidden(false);
        date.addFlag(CreatureFlag.LOVE_SLAVE);
        date.hire(lcs);
        getch();
        date.name(query("The Self-Nullifying Infatuation of " + date.properName()
            + "\n\nWhat name will you use for this " + date.type().jobtitle(date)
            + " in its presence?", date.properName()));
        date.sleeperizePrompt(lcs);
        date.giveFundsToLCS();
        i.pool.add(date);
        i.score.recruits++;
        dates.remove(date);
        return DateResult.JOINED;
      }
      if (date.skill().getAttribute(Attribute.HEART, false) < lcs.skill().getAttribute(
          Attribute.HEART, false) - 4) {
        date.skill().attribute(Attribute.HEART, +1);
      } else // Posibly date reveals map of location
      if (!date.workLocation().isInterrogated()
          && i.rng.chance(date.skill().getAttribute(Attribute.WISDOM, false))) {
        ui().text(
            date.toString() + " turns the topic of discussion to the "
                + date.workLocation().toString() + ".").add();
        if (date.workLocation().type().canMap()) {
          ui().text(lcs.toString() + " was able to create a map of the site with this information.")
              .add();
        } else {
          ui().text(lcs.toString() + " knows all about that already.").add();
        }
        date.workLocation().interrogated(true);
        date.workLocation().hidden(false);
      }
      ui().text("They'll meet again tomorrow.").add();
      getch();
      return DateResult.MEETTOMORROW;
    }
    // WISDOM POSSIBLE INCREASE
    if (date.alignment() == Alignment.CONSERVATIVE && aroll < troll / 2) {
      ui().text(
          "Talking with " + date.toString() + " actually increases " + lcs.toString()
              + "'s wisdom!!!").color(Color.RED).add();
      lcs.skill().attribute(Attribute.WISDOM, +1);
      if (date.skill().skill(Skill.RELIGION) > lcs.skill().skill(Skill.RELIGION)) {
        lcs.skill().train(Skill.RELIGION,
            20 * (date.skill().skill(Skill.RELIGION) - lcs.skill().skill(Skill.RELIGION)));
      }
      if (date.skill().skill(Skill.SCIENCE) > lcs.skill().skill(Skill.SCIENCE)) {
        lcs.skill().train(Skill.SCIENCE,
            20 * (date.skill().skill(Skill.SCIENCE) - lcs.skill().skill(Skill.SCIENCE)));
      }
      if (date.skill().skill(Skill.BUSINESS) > lcs.skill().skill(Skill.BUSINESS)) {
        lcs.skill().train(Skill.BUSINESS,
            20 * (date.skill().skill(Skill.BUSINESS) - lcs.skill().skill(Skill.BUSINESS)));
      }
      getch();
    }
    // BREAK UP
    // *JDS* If your squad member is wanted by the police, a
    // conservative who breaks up with
    // them has a 1 in 50 chance of ratting them out, unless the person
    // being dated is law
    // enforcement, prison guard, or agent, in which case there is a 1
    // in 4 chance.
    if (lcs.crime().isCriminal() && (i.rng.chance(50) || i.rng.chance(2) && date.type().isPolice())) {
      ui().text(date.toString() + " was leaking information to the police the whole time!")
          .color(Color.RED).add();
      getch();
      // 3/4 chance of being arrested if less than 50 juice,
      // 1/2 chance of being arrested if more than 50 juice
      if (lcs.juice() < 50 && i.rng.chance(2) || i.rng.chance(2)) {
        // Find the police station
        final Location ps = AbstractSiteType.type(PoliceStation.class).getLocation();
        ui().text(lcs.toString() + " has been arrested.").color(Color.MAGENTA).add();
        lcs.removeSquadInfo().car(null);
        lcs.location(ps);
        lcs.weapon().dropWeaponsAndClips(null);
        lcs.activity(BareActivity.noActivity());
        getch();
        dates.remove(date);
        return DateResult.ARRESTED;
      }
      ui().text("But " + lcs.toString() + " escapes the police ambush!").color(Color.GREEN).add();
    } else {
      ui().text(date.toString() + " can sense that things just aren't working out.")
          .color(Color.MAGENTA).add();
      ui().text("This relationship is over.").add();
    }
    getch();
    dates.remove(date);
    return DateResult.BREAKUP;
  }

  private static final long serialVersionUID = Game.VERSION;
}