package lcs.android.monthly;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Map;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureType;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.Encounter;
import lcs.android.encounters.SiteEncounter;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Loot;
import lcs.android.items.Weapon;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.PoliceStation;
import lcs.android.site.type.Shelter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** *JDS* ----- The sleeper system has been completely reworked. - Sleepers no longer directly
 * influence the issues. They now affect the broad "liberal power" stats across many issues, which
 * are used as a kind of monthly liberal roll akin to AM Radio and Cable News. - Each sleeper can
 * affect one or more issue, throwing their power into the "abstracted debate" on that issue. -
 * After all of the sleepers have contributed to the liberal power stats, a roll is made on each
 * issue to see whether the liberals make background progress on those issues. - Several sleepers
 * have special abilities. Lawyers and Judges, as always, can aid your people in the legal system.
 * Police officers, corporate managers, CEOs, and agents can all now leak secret documents of the
 * appropriate types, and they will make a check each month. This will only happen if the homeless
 * shelter is not under siege, and "canseethings" is enabled (eg, you're not in prison or disbanded
 * or some other situation where your sleeper can't get in touch with anyone in your squad). - News
 * Anchors and Radio Personalities remain the two most powerful sleepers. */
@NonNullByDefault class SleeperUpdate {
  /** Add sleeper effect from a given creature to the libpower Map.
   * @param cr
   * @param libpower */
  static void sleeperEffect(final Creature cr, final Map<Issue, Integer> libpower) {
    if (i.disbanding()) {
      cr.activity(new BareActivity(Activity.SLEEPER_LIBERAL));
    }
    switch (cr.activity().type()) {
    case SLEEPER_LIBERAL:
      sleeperInfluence(cr, libpower);
      cr.infiltration(cr.infiltration() - 2);
      break;
    case SLEEPER_EMBEZZLE:
      sleeperEmbezzle(cr);
      break;
    case SLEEPER_STEAL:
      sleeperSteal(cr);
      break;
    case SLEEPER_RECRUIT:
      sleeperRecruit(cr);
      break;
    case SLEEPER_SPY:
      sleeperSpy(cr);
      break;
    case SLEEPER_SCANDAL:
      sleeperScandal(cr);
      break;
    case NONE:
    case SLEEPER_JOINLCS:
    default:
      break;
    }
    cr.infiltration(cr.infiltration() + i.rng.nextInt(8) - 2);
    if (cr.infiltration() >= 100) {
      cr.infiltration(100);
    }
    if (cr.infiltration() <= 0) {
      cr.infiltration(0);
    }
  }

  /********************************* SLEEPERS EMBEZZLING FUNDS **********************************/
  private static void sleeperEmbezzle(final Creature cr) {
    if (i.rng.nextInt(100) > cr.infiltration()) {
      cr.juice(cr.juice() - 1);
      if (cr.juice() < -2) {
        ui().text("Sleeper " + cr + " has been arrested while embezzling funds!").add();
        getch(); // TODO change next to imprison(reason).
        final Location ps = AbstractSiteType.type(PoliceStation.class).getLocation();
        cr.crime().incrementCrime(Crime.COMMERCE);
        cr.removeSquadInfo();
        cr.location(ps);
        cr.weapon().dropWeaponsAndClips(null);
        cr.activity(BareActivity.noActivity());
        cr.removeFlag(CreatureFlag.SLEEPER);
      }
      return;
    }
    // Improves juice, as confidence improves
    if (cr.juice() < 100) {
      cr.juice(cr.juice() + 10);
      if (cr.juice() > 100) {
        cr.juice(100);
      }
    }
    int income;
    if (cr.type().ofType("SCIENTIST_EMINENT")) {
      income = 50 * cr.infiltration();
    } else if (cr.type().ofType("CORPORATE_CEO")) {
      income = 500 * cr.infiltration();
    } else if (cr.type().ofType("CORPORATE_MANAGER")) {
      income = 50 * cr.infiltration();
    } else {
      income = 5 * cr.infiltration();
    }
    ui().text(cr + " has embezzled $" + income + " from " + cr.workLocation() + ".").add();
    i.ledger.addFunds(income, Ledger.IncomeType.EMBEZZLEMENT);
  }

  /********************************* SLEEPERS INFLUENCING PUBLIC OPINION **********************************/
  private static void sleeperInfluence(final Creature cr, final Map<Issue, Integer> libpower) {
    int power = cr.skill().getAttribute(Attribute.CHARISMA, true)
        + cr.skill().getAttribute(Attribute.HEART, true)
        + cr.skill().getAttribute(Attribute.INTELLIGENCE, true)
        + cr.skill().skill(Skill.PERSUASION);
    ui(R.id.gcontrol).text(cr + " has been influencing opinion.").add();
    // Profession specific skills
    if (cr.type().coreSkill() != null) {
      power += cr.skill().skill(cr.type().coreSkill());
    }
    // Adjust power for super sleepers
    power *= cr.type().influence();
    power *= cr.infiltration();
    /* Radio Personalities and News Anchors subvert Conservative news stations by reducing their
     * audience and twisting views on the issues. As their respective media establishments become
     * marginalized, so does their influence. */
    if (cr.type().ofType("RADIOPERSONALITY")) {
      i.issue(Issue.AMRADIO).changeOpinion(1, 1, 100);
      for (final Issue v : Issue.values()) {
        if (v == Issue.LIBERALCRIMESQUAD || v == Issue.LIBERALCRIMESQUADPOS
            || v == Issue.CONSERVATIVECRIMESQUAD) {
          continue;
        }
        libpower.put(v, libpower.get(v) + power * i.issue(Issue.AMRADIO).attitude() / 100);
      }
    } else if (cr.type().ofType("NEWSANCHOR")) {
      for (final Issue v : Issue.values()) {
        if (v == Issue.LIBERALCRIMESQUAD || v == Issue.LIBERALCRIMESQUADPOS
            || v == Issue.CONSERVATIVECRIMESQUAD) {
          continue;
        }
        libpower.put(v, libpower.get(v) + power * i.issue(Issue.CABLENEWS).attitude() / 100);
      }
    } else if (cr.type().ofType("PRIEST") || cr.type().ofType("PAINTER")
        || cr.type().ofType("SCULPTOR") || cr.type().ofType("AUTHOR")
        || cr.type().ofType("JOURNALIST") || cr.type().ofType("PSYCHOLOGIST")
        || cr.type().ofType("MUSICIAN") || cr.type().ofType("CRITIC_ART")
        || cr.type().ofType("CRITIC_MUSIC") || cr.type().ofType("ACTOR")) {
      libpower.put(Issue.ABORTION, libpower.get(Issue.ABORTION) + power);
      libpower.put(Issue.CIVILRIGHTS, libpower.get(Issue.CIVILRIGHTS) + power);
      libpower.put(Issue.GAY, libpower.get(Issue.GAY) + power);
      libpower.put(Issue.FREESPEECH, libpower.get(Issue.FREESPEECH) + power);
      libpower.put(Issue.DRUGS, libpower.get(Issue.DRUGS) + power);
      libpower.put(Issue.IMMIGRATION, libpower.get(Issue.IMMIGRATION) + power);
    }
    /* Legal block - influences an array of social issues */
    else if (cr.type().ofType("JUDGE_CONSERVATIVE")) {
      libpower.put(Issue.JUSTICES, libpower.get(Issue.JUSTICES) + power);
      libpower.put(Issue.FREESPEECH, libpower.get(Issue.FREESPEECH) + power);
      libpower.put(Issue.PRIVACY, libpower.get(Issue.PRIVACY) + power);
    } else if (cr.type().ofType("LAWYER")) {
      libpower.put(Issue.POLICEBEHAVIOR, libpower.get(Issue.POLICEBEHAVIOR) + power);
      libpower.put(Issue.DEATHPENALTY, libpower.get(Issue.DEATHPENALTY) + power);
      libpower.put(Issue.GUNCONTROL, libpower.get(Issue.GUNCONTROL) + power);
      libpower.put(Issue.DRUGS, libpower.get(Issue.DRUGS) + power);
    }
    /* Scientists block */
    else if (cr.type().ofType("SCIENTIST_EMINENT")) {
      libpower.put(Issue.POLLUTION, libpower.get(Issue.POLLUTION) + power);
    } else if (cr.type().ofType("SCIENTIST_LABTECH")) {
      libpower.put(Issue.NUCLEARPOWER, libpower.get(Issue.NUCLEARPOWER) + power);
      libpower.put(Issue.ANIMALRESEARCH, libpower.get(Issue.ANIMALRESEARCH) + power);
      libpower.put(Issue.GENETICS, libpower.get(Issue.GENETICS) + power);
    }
    /* Corporate block */
    else if (cr.type().ofType("CORPORATE_CEO")) {
      libpower.put(Issue.CEOSALARY, libpower.get(Issue.CEOSALARY) + power);
    } else if (cr.type().ofType("CORPORATE_MANAGER")) {
      libpower.put(Issue.ABORTION, libpower.get(Issue.ABORTION) + power);
      libpower.put(Issue.TAX, libpower.get(Issue.TAX) + power);
      libpower.put(Issue.CORPORATECULTURE, libpower.get(Issue.CORPORATECULTURE) + power);
      libpower.put(Issue.LABOR, libpower.get(Issue.LABOR) + power);
      libpower.put(Issue.POLLUTION, libpower.get(Issue.POLLUTION) + power);
      libpower.put(Issue.CIVILRIGHTS, libpower.get(Issue.CIVILRIGHTS) + power);
    }
    /* Law enforcement block */
    else if (cr.type().ofType("DEATHSQUAD")) {
      libpower.put(Issue.DEATHPENALTY, libpower.get(Issue.DEATHPENALTY) + power);
    } else if (cr.type().ofType("SWAT") || cr.type().ofType("COP") || cr.type().ofType("GANGUNIT")) {
      libpower.put(Issue.POLICEBEHAVIOR, libpower.get(Issue.POLICEBEHAVIOR) + power);
      libpower.put(Issue.DRUGS, libpower.get(Issue.DRUGS) + power);
      libpower.put(Issue.TORTURE, libpower.get(Issue.TORTURE) + power);
      libpower.put(Issue.GUNCONTROL, libpower.get(Issue.GUNCONTROL) + power);
    }
    /* Prison block */
    else if (cr.type().ofType("EDUCATOR") || cr.type().ofType("PRISONGUARD")
        || cr.type().ofType("PRISONER")) {
      libpower.put(Issue.POLICEBEHAVIOR, libpower.get(Issue.POLICEBEHAVIOR) + power);
      libpower.put(Issue.DEATHPENALTY, libpower.get(Issue.DEATHPENALTY) + power);
      libpower.put(Issue.DRUGS, libpower.get(Issue.DRUGS) + power);
      libpower.put(Issue.TORTURE, libpower.get(Issue.TORTURE) + power);
    }
    /* Intelligence block */
    else if (cr.type().ofType("AGENT")) {
      libpower.put(Issue.PRIVACY, libpower.get(Issue.PRIVACY) + power);
      libpower.put(Issue.TORTURE, libpower.get(Issue.TORTURE) + power);
      libpower.put(Issue.FREESPEECH, libpower.get(Issue.FREESPEECH) + power);
    }
    /* Military block */
    else if (cr.type().ofType("MERC")) {
      libpower.put(Issue.GUNCONTROL, libpower.get(Issue.GUNCONTROL) + power);
    } else if (cr.type().ofType("SOLDIER") || cr.type().ofType("VETERAN")) {
      libpower.put(Issue.MILITARY, libpower.get(Issue.MILITARY) + power);
      libpower.put(Issue.TORTURE, libpower.get(Issue.TORTURE) + power);
      libpower.put(Issue.GAY, libpower.get(Issue.GAY) + power);
      libpower.put(Issue.ABORTION, libpower.get(Issue.ABORTION) + power);
    }
    /* Sweatshop workers */
    else if (cr.type().ofType("WORKER_SWEATSHOP")) {
      libpower.put(Issue.IMMIGRATION, libpower.get(Issue.IMMIGRATION) + power);
      libpower.put(Issue.LABOR, libpower.get(Issue.LABOR) + power);
    }
    /* No influence at all block - for people were liberal anyway, or have no way of doing any good */
    else if (cr.type().ofType(
        new String[] { "WORKER_FACTORY_CHILD", "GENETIC", "GUARDDOG", "BUM", "CRACKHEAD", "TANK",
            "HIPPIE", "WORKER_FACTORY_UNION", "JUDGE_LIBERAL", "POLITICALACTIVIST", "MUTANT" }))
      return;
    else if (cr.type().ofType("FIREFIGHTER")) {
      if (!i.freeSpeech()) {
        libpower.put(Issue.FREESPEECH, libpower.get(Issue.FREESPEECH) + power);
      }
    } else { // Affect a random issue
      Issue.randomissueinit(true);
      final Issue v = Issue.randomissue();
      libpower.put(v, libpower.get(v) + power);
    }
  }

  /********************************* SLEEPERS RECRUITING **********************************/
  private static void sleeperRecruit(final Creature cr) {
    if (cr.subordinatesLeft() == 0) {
      ui().text(cr + " can't recruit any more Liberals at the moment.").add();
      return;
    }
    final Encounter e = new SiteEncounter();
    cr.workLocation().type().prepareEncounter(e, false);
    for (final Creature recruit : e.creatures()) {
      if (recruit.workLocation() == cr.workLocation() || i.rng.chance(5)) {
        if (recruit.alignment() != Alignment.LIBERAL && i.rng.likely(5)) {
          continue;
        }
        recruit.liberalize(false).hire(cr);
        if (recruit.infiltration() > cr.infiltration()) {
          recruit.infiltration(cr.infiltration());
        }
        recruit.addFlag(CreatureFlag.SLEEPER);
        recruit.workLocation().interrogated(true).hidden(false);
        i.pool.add(recruit);
        ui().text(
            "Sleeper " + cr + " has recruited a new " + recruit.type().jobtitle(recruit) + ".")
            .add();
        ui().text(recruit + " looks forward serving the Liberal cause!").add();
        if (cr.subordinatesLeft() == 0) {
          cr.activity(BareActivity.noActivity());
        }
        i.score.recruits++;
        return;
      }
    }
    ui().text(cr + " didn't succeed in recruiting this month.").add();
  }

  /********************************* SLEEPERS CREATING SCANDALS **********************************/
  private static void sleeperScandal(final Creature cr) {
    // TOODO Add content here!
    ui().text(cr + " would have been creating a scandal, if it was implemented.").add();
    return;
  }

  /********************************* SLEEPERS SNOOPING AROUND **********************************/
  private static void sleeperSpy(final Creature cr) {
    ui().text(cr.toString() + " has been spying at the " + cr.workLocation().toString()).add();
    final Location homes = AbstractSiteType.type(Shelter.class).getLocation();
    if (i.rng.nextInt(100) > 100 * cr.infiltration()) {
      cr.juice(cr.juice() - 1);
      if (cr.juice() < -2) {
        ui().text(
            "Sleeper " + cr
                + " has been caught snooping around.\nThe Liberal is now homeless and jobless...")
            .add();
        getch();
        cr.removeSquadInfo();
        cr.location(homes);
        cr.weapon().dropWeaponsAndClips(null);
        cr.activity(BareActivity.noActivity());
        cr.removeFlag(CreatureFlag.SLEEPER);
      }
      return;
    }
    // Improves juice, as confidence improves
    if (cr.juice() < 100) {
      cr.juice(cr.juice() + 10);
      if (cr.juice() > 100) {
        cr.juice(100);
      }
    }
    if (cr.base().exists()) {
      cr.base().get().interrogated(true);
    }
    boolean pause = false;
    if (cr.type().ofType("AGENT")) {
      // Agents can leak intelligence files to you
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(i.issue(Issue.PRIVACY).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_SECRETDOCUMENTS");
        homes.lcs().loot.add(it);
        ui().text("Sleeper ").add();
        ui().text(cr.toString()).add();
        ui().text(" has leaked secret intelligence files.").add();
        ui().text("They are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("DEATHSQUAD") || cr.type().ofType("SWAT")
        || cr.type().ofType("COP") || cr.type().ofType("GANGUNIT")) {
      // Cops can leak police files to you
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(i.issue(Issue.POLICEBEHAVIOR).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_POLICERECORDS");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked secret police records.").add();
        ui().text("They are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("CORPORATE_MANAGER") || cr.type().ofType("CORPORATE_CEO")) {
      // Can leak corporate files to you
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(i.issue(Issue.CORPORATECULTURE).law().trueOrdinal() + 3)
            && cr.type() != CreatureType.valueOf("CORPORATE_CEO"))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_CORPFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked secret corporate documents.").add();
        ui().text("They are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("EDUCATOR") || cr.type().ofType("PRISONGUARD")) {
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(i.issue(Issue.POLICEBEHAVIOR).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_PRISONFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked internal prison records.").add();
        ui().text("They are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("NEWSANCHOR")) {
      if (!homes.lcs().siege.siege) {
        /* More likely to leak these documents the more restrictive free speech is -- because the
         * more free the society, the less any particular action the media takes seems scandalous */
        if (i.rng.likely(i.issue(Issue.FREESPEECH).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_CABLENEWSFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked proof of systemic Cable News bias.").add();
        ui().text("The papers are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("RADIOPERSONALITY")) {
      if (!homes.lcs().siege.siege) {
        /* More likely to leak these documents the more restrictive free speech is -- because the
         * more free the society, the less any particular action the media takes seems scandalous */
        if (i.rng.likely(i.issue(Issue.FREESPEECH).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_AMRADIOFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked proof of systemic AM Radio bias.").add();
        ui().text("The papers are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("SCIENTIST_LABTECH") || cr.type().ofType("SCIENTIST_EMINENT")) {
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(i.issue(Issue.ANIMALRESEARCH).law().trueOrdinal() + 3))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_RESEARCHFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked internal animal research reports.").add();
        ui().text("They are stashed at the homeless shelter.").add();
        pause = true;
      }
    } else if (cr.type().ofType("JUDGE_CONSERVATIVE")) {
      if (!homes.lcs().siege.siege) {
        if (i.rng.likely(5))
          return;
        final AbstractItem<? extends AbstractItemType> it = new Loot("LOOT_JUDGEFILES");
        homes.lcs().loot.add(it);
        ui().text("Sleeper " + cr + " has leaked proof of corruption in the judiciary.").add();
        ui().text("The papers are stashed at the homeless shelter.").add();
        pause = true;
      }
    }
    if (pause) {
      getch();
    }
  }

  /********************************* SLEEPERS STEALING THINGS **********************************/
  private static void sleeperSteal(final Creature cr) {
    if (i.rng.nextInt(100) > cr.infiltration()) {
      cr.juice(cr.juice() - 1);
      if (cr.juice() < -2) {
        ui().text("Sleeper " + cr + " has been arrested while stealing things.").add();
        getch();
        final Location ps = AbstractSiteType.type(PoliceStation.class).getLocation();
        cr.crime().incrementCrime(Crime.THEFT);
        cr.removeSquadInfo();
        cr.location(ps);
        cr.weapon().dropWeaponsAndClips(null);
        cr.activity(BareActivity.noActivity());
        cr.removeFlag(CreatureFlag.SLEEPER);
      } else {
        ui().text("Sleeper " + cr.toString() + " couldn't find anything to steal").add();
      }
      return;
    }
    // Improves juice, as confidence improves
    if (cr.juice() < 100) {
      cr.juice(cr.juice() + 10);
      if (cr.juice() > 100) {
        cr.juice(100);
      }
    }
    // TODO clean up display.
    String item;
    final Location shelter = AbstractSiteType.type(Shelter.class).getLocation();
    int number_of_items = i.rng.nextInt(10) + 1;
    ui().text("Sleeper " + cr.toString() + " has dropped a package containing ").add();
    while (number_of_items-- > 0) {
      if (cr.location().exists() && cr.location().get().type().loot.size() > 0) {
        item = i.rng.randFromList(cr.location().get().type().loot);
      } else {
        item = "LOOT_DIRTYSOCK";
      }
      AbstractItem<? extends AbstractItemType> o = null;
      if (item.startsWith("LOOT")) {
        o = new Loot(item);
      } else if (item.startsWith("WEAPON")) {
        o = new Weapon(item);
      } else if (item.startsWith("ARMOR")) {
        o = new Armor(item);
      } else {
        // then WTF is it? Avoid NPE
        continue;
      }
      shelter.lcs().loot.add(o);
      ui().text(o.toString() + (number_of_items > 0 ? ", " : "")).add();
    }
    ui().text(" off at the homeless shelter.").add();
    getch();
  }
}
