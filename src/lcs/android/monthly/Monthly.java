package lcs.android.monthly;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lcs.android.R;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.LiberalAgenda;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.Daily;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.game.Visibility;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Loot;
import lcs.android.law.Crime;
import lcs.android.law.Justice;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.scoring.EndType;
import lcs.android.scoring.HighScore;
import lcs.android.site.Squad;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.SecurityLevel;
import lcs.android.site.map.TileSpecial;
import lcs.android.site.type.CourtHouse;
import lcs.android.site.type.CrackHouse;
import lcs.android.site.type.FireStation;
import lcs.android.site.type.House;
import lcs.android.site.type.IntelligenceHq;
import lcs.android.site.type.Nuclear;
import lcs.android.site.type.PawnShop;
import lcs.android.site.type.PoliceStation;
import lcs.android.site.type.Prison;
import lcs.android.util.Color;
import lcs.android.util.Filter;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Monthly {
  public static void passMonth() {
    final Map<Issue, Alignment> oldlaw = new HashMap<Issue, Alignment>();
    for (final Issue l : Issue.values()) {
      oldlaw.put(l, i.issue(l).law());
    }
    // TIME ADVANCE
    i.score.date.nextMonth();
    i.endgameState.monthlyUpdate();
    // CLEAR RENT EXEMPTIONS
    for (final Location l : i.location) {
      l.lcs().newRental = false;
    }
    // YOUR PAPER AND PUBLIC OPINION AND STUFF
    final List<Location> nploc = new ArrayList<Location>();
    for (final Location l : i.location) {
      if (l.lcs().compoundWalls.contains(Compound.PRINTINGPRESS) && !l.lcs().siege.siege
          && l.renting() != CrimeSquad.CCS) {
        nploc.add(l);
      }
    }
    // Check for game over
    lcs.android.monthly.EndGame.endcheck(EndType.DEAD);
    Daily.dispersalcheck();
    // int guardianpower = 0;
    if (nploc.size() > 0 && !i.disbanding()) {
      // DO SPECIAL EDITIONS
      final Loot loottypeindex = Monthly.choosespecialedition();
      // guardianpower += 10 * nploc.size();
      Monthly.printnews(loottypeindex, nploc.size());
      if (loottypeindex.id().equals("LOOT_INTHQDISK") ||
      /* For special edition xml file? -XML */
      loottypeindex.id().equals("LOOT_SECRETDOCUMENTS")) {
        for (final Location l : nploc) {
          l.criminalizeLiberalsInLocation(Crime.TREASON);
        }
      }
    }
    // int[] libpower = new int[Issue.values().length];
    final Map<Issue, Integer> libpower = new HashMap<Issue, Integer>();
    for (final Issue v : Issue.values()) {
      libpower.put(v, 0);
    }
    // STORIES STALE EVEN IF NOT PRINTED
    for (final Issue v : Issue.values()) {
      i.issue(v).publicInterest(i.issue(v).publicInterest() / 2);
    }
    final int conspower = 200 - i.issue(Issue.AMRADIO).attitude()
        - i.issue(Issue.CABLENEWS).attitude();
    if (i.visibility == Visibility.CAN_SEE) {
      setView(R.layout.generic);
      ui().text("Sleeper actions:").bold().add();
    }
    // HAVING SLEEPERS
    for (final Creature pl : Filter.of(i.pool, Filter.SLEEPER)) {
      SleeperUpdate.sleeperEffect(pl, libpower);
    }
    if (i.visibility == Visibility.CAN_SEE) {
      ui(R.id.gcontrol).button(' ').text("Continue the struggle.").add();
      getch();
    }
    // Manage graffiti
    location: for (final Location l : i.location) // Check each location
    {
      final Iterator<MapChangeRecord> ci = l.changes().iterator();
      while (ci.hasNext()) // Each change to the map
      {
        final MapChangeRecord c = ci.next(); // FIXME reported as CME?
        if (c.flag == TileSpecial.GRAFFITI || c.flag == TileSpecial.GRAFFITI_CCS
            || c.flag == TileSpecial.GRAFFITI_OTHER) // Find
        // changes
        // that
        // refer
        // specifically
        // to
        // graffiti
        {
          int power = 0;
          int align = 0;
          if (c.flag == TileSpecial.GRAFFITI) {
            align = 1;
          }
          if (c.flag == TileSpecial.GRAFFITI_CCS) {
            align = -1;
          }
          /* Purge graffiti from more secure sites (or from non-secure sites about once every five
           * i.hs.years), but these will influence people more for the current i.hs.month */
          if (l.type().securityLevel() != SecurityLevel.POOR) {
            l.changes().clear();
            power = 5;
            updateInfluence(power, align);
            break location;
          } else if (l.renting() == CrimeSquad.CCS) {
            c.flag = TileSpecial.GRAFFITI_CCS; // Convert to CCS
          } else if (l.renting() == CrimeSquad.LCS) {
            c.flag = TileSpecial.GRAFFITI; // Convert to LCS tags
          } else {
            power = 1;
            if (i.rng.chance(10)) {
              c.flag = TileSpecial.GRAFFITI_OTHER; // Convert to
            }
            // other tags
            if (i.rng.chance(10) && i.endgameState.ccsActive()) {
              c.flag = TileSpecial.GRAFFITI_CCS; // Convert to
            }
            // CCS tags
            if (i.rng.chance(30)) {
              ci.remove(); // Clean up
            }
          }
          updateInfluence(power, align);
        }
      }
    }
    // int mediabalance = 0;
    // int[] issuebalance = new int[Issue.coreValues().length];
    final Map<Issue, Integer> issuebalance = new HashMap<Issue, Integer>();
    // int stimulus = 0;
    // double cost = 0;
    // double tax = 0;
    // PUBLIC OPINION NATURAL MOVES
    for (final Issue v : Issue.values()) {
      // Liberal essays add their power to the effect of sleepers
      libpower.put(v, libpower.get(v) + i.issue(v).backgroundInfluence());
      i.issue(v).backgroundInfluence(i.issue(v).backgroundInfluence() * 2 / 3);
      if (v == Issue.LIBERALCRIMESQUADPOS) {
        continue;
      }
      if (v == Issue.LIBERALCRIMESQUAD) {
        continue;
      }
      if (v == Issue.CONSERVATIVECRIMESQUAD) {
        continue;
      }
      if (v != Issue.AMRADIO && v != Issue.CABLENEWS) {
        issuebalance.put(v, libpower.get(v) - conspower);
        // mediabalance += issuebalance.get(v);
        // Heavy randomization -- balance of power just biases the roll
        final int roll = issuebalance.get(v) + i.rng.nextInt(400) - 200;
        // If +/-50 to either side, that side wins the tug-of-war
        if (roll < -50) {
          i.issue(v).changeOpinion(-1, 0, 100);
        } else if (roll > 50) {
          i.issue(v).changeOpinion(1, 0, 100);
        } else {
          i.issue(v).changeOpinion(i.rng.nextInt(2) * 2 - 1, 0, 100);
        }
      }
      /* AM Radio and Cable News popularity slowly shift to reflect public opinion over time -- if
       * left unchecked, their subtle influence on society will become a self-perpetuating
       * Conservative nightmare! */
      else if (v == Issue.AMRADIO || v == Issue.CABLENEWS) {
        if (Politics.publicmood() < i.issue(v).attitude()) {
          i.issue(v).changeOpinion(-1, 1, 100);
        } else {
          i.issue(v).changeOpinion(1, 1, 100);
        }
      }
    }
    /* Seduction monthly experience stipends for those liberals who have been getting it on with
     * their love slaves/masters in the background */
    for (final Creature s : Filter.of(i.pool, Filter.LIVING)) {
      s.skill().train(Skill.SEDUCTION, s.loveSlaves() * 5);
      if (s.hasFlag(CreatureFlag.LOVE_SLAVE)) {
        s.skill().train(Skill.SEDUCTION, 5);
      }
    }
    // ELECTIONS
    if (i.score.date.month() == 11) {
      Politics.elections();
    }
    // SUPREME COURT
    if (i.score.date.month() == 6) {
      Politics.supremecourt();
    }
    // CONGRESS
    if (i.score.date.month() == 3) {
      Politics.congress();
    }
    if (i.score.date.month() == 9) {
      Politics.congress();
    }
    // DID YOU WIN?
    if (Politics.wincheck()) {
      LiberalAgenda.liberalagenda(Alignment.LIBERAL);
      HighScore.savehighscore(EndType.WON);
      HighScore.viewhighscores();
      Game.endGame();
    }
    // CONTROL LONG DISBANDS
    if (i.disbanding() && i.score.date.year() - i.disbandYear >= 50) {
      fact("The Liberal Crime Squad is now just a memory.\n\n"
          + "The last LCS members have all been hunted down.\n\n"
          + "They will never see the utopia they dreamed of...");
      HighScore.savehighscore(EndType.DISBANDLOSS);
      HighScore.viewhighscores();
      Game.endGame();
    }
    // UPDATE THE WORLD IN CASE THE LAWS HAVE CHANGED
    updateworld_laws(oldlaw);
    // THE SYSTEM!
    if (!i.disbanding()) {
      for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
        if (p.hasFlag(CreatureFlag.SLEEPER)) {
          continue;
        }
        if (!p.location().exists()) {
          continue;
        }
        if (p.location().get().type().isType(PoliceStation.class)) {
          if (p.hasFlag(CreatureFlag.MISSING)) {
            ui().text(p.toString() + " has been rehabilitated from LCS brainwashing.")
                .color(Color.MAGENTA).add();
            getch();
            p.removeSquadInfo();
            i.pool.remove(p);
            continue;
          } else if (p.hasFlag(CreatureFlag.ILLEGAL_ALIEN)
              && i.issue(Issue.IMMIGRATION).law() != Alignment.ELITELIBERAL) {
            ui().text(p.toString() + " has been shipped out to the INS to face ")
                .color(Color.MAGENTA).add();
            if (i.issue(Issue.IMMIGRATION).law() == Alignment.ARCHCONSERVATIVE
                && i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE) {
              ui().text("execution.").add();
            } else {
              ui().text("deportation.").add();
            }
            getch();
            p.removeSquadInfo();
            i.pool.remove(p);
            continue;
          } else {
            // TRY TO GET RACKETEERING CHARGE
            int copstrength = 100;
            int heat = 0;
            if (i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
              copstrength = 200;
            }
            if (i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.CONSERVATIVE) {
              copstrength = 150;
            }
            if (i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.LIBERAL) {
              copstrength = 75;
            }
            if (i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ELITELIBERAL) {
              copstrength = 50;
            }
            heat += p.crime().heat() * Crime.values().length;
            copstrength = copstrength * heat / 4;
            if (copstrength > 200) {
              copstrength = 200;
            }
            // Confession check
            if (i.rng.nextInt(copstrength) > p.juice()
                + p.skill().getAttribute(Attribute.HEART, true) * 5
                - p.skill().getAttribute(Attribute.WISDOM, true) * 5
                + p.skill().skill(Skill.PSYCHOLOGY) * 5
                /* + p.getSkill(Skill.SURVIVAL)*5 */&& !p.isFounder()) {
              final boolean nullify = false;
              final Maybe<Creature> p2 = p.hire();
              if (p2.exists()
                  && p2.get().health().alive()
                  && (!p2.get().location().exists() || !p2.get().location().get().type()
                      .isType(Prison.class))) {
                // Charge the boss with racketeering!
                p2.get().crime().criminalize(Crime.RACKETEERING).addTestimony();
              }
              if (!nullify) {
                // Issue a raid on this guy's base!
                if (p.base().exists()) {
                  p.base().get().lcs().heat += 300;
                }
                ui().text(p + " has broken under the pressure and ratted you out!").add();
                ui().text("The traitor will testify in court, and safehouses may be compromised.")
                    .add();
                getch();
                p.removeSquadInfo();
                i.pool.remove(p);
                continue;
                /* no trial for this person; skip to next person */
              }
              // else continue to trial
            }
            ui().text(p.toString() + " is moved to the courthouse for trial.").add();
            getch();
            for (final Location l : i.location) {
              if (l.type().isType(CourtHouse.class)) {
                p.location(l);
              }
            }
            final Armor prisoner = new Armor("ARMOR_PRISONER");
            p.giveArmor(prisoner, null);
          }
        } else if (p.location().exists() && p.location().get().type().isType(CourtHouse.class)) {
          Justice.trial(p);
        } else if (p.location().exists() && p.location().get().type().isType(Prison.class)) {
          setView(R.layout.generic);
          if (Justice.prisonMonthlyUpdate(p)) {
            waitOnOK();
          }
        }
      }
    }
    // MUST DO AN END OF GAME CHECK HERE BECAUSE OF EXECUTIONS
    lcs.android.monthly.EndGame.endcheck(EndType.EXECUTED);
    // DISPERSAL CHECK
    Daily.dispersalcheck();
    // FUND REPORTS
    Ledger.fundReport();
    // HEAL CLINIC PEOPLE
    if (!i.disbanding()) {
      for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
        if (p.health().clinicMonths() > 0) {
          p.health().doClinic();
        }
      }
    }
  }

  /* monthly - lets the player choose a special edition for the guardian */
  private static Loot choosespecialedition() {
    final List<Loot> suitableLoot = new ArrayList<Loot>();
    // FIND ALL LOOT TYPES
    for (final Location loc : i.location) {
      if (loc.renting() == null) {
        continue;
      }
      for (final AbstractItem<? extends AbstractItemType> l : loc.lcs().loot) {
        if (!(l instanceof Loot)) {
          continue;
        }
        // Temporary, maybe put special edition definition into an xml
        // file. -XML
        if (l.id().equals("LOOT_CEOPHOTOS") || l.id().equals("LOOT_CEOLOVELETTERS")
            || l.id().equals("LOOT_CEOTAXPAPERS") || l.id().equals("LOOT_INTHQDISK")
            || l.id().equals("LOOT_CORPFILES") || l.id().equals("LOOT_JUDGEFILES")
            || l.id().equals("LOOT_RESEARCHFILES") || l.id().equals("LOOT_PRISONFILES")
            || l.id().equals("LOOT_CABLENEWSFILES") || l.id().equals("LOOT_AMRADIOFILES")
            || l.id().equals("LOOT_SECRETDOCUMENTS") || l.id().equals("LOOT_POLICERECORDS")) {
          continue;
        }
        suitableLoot.add((Loot) l);
      }
    }
    for (final Squad sq : i.squad) {
      for (final AbstractItem<? extends AbstractItemType> l : sq.loot()) {
        if (!(l instanceof Loot)) {
          continue;
        }
        // Temporary, maybe put special edition definition into an xml
        // file. -XML
        if (l.id().equals("LOOT_CEOPHOTOS") || l.id().equals("LOOT_CEOLOVELETTERS")
            || l.id().equals("LOOT_CEOTAXPAPERS") || l.id().equals("LOOT_INTHQDISK")
            || l.id().equals("LOOT_CORPFILES") || l.id().equals("LOOT_JUDGEFILES")
            || l.id().equals("LOOT_RESEARCHFILES") || l.id().equals("LOOT_PRISONFILES")
            || l.id().equals("LOOT_CABLENEWSFILES") || l.id().equals("LOOT_AMRADIOFILES")
            || l.id().equals("LOOT_SECRETDOCUMENTS") || l.id().equals("LOOT_POLICERECORDS")) {
          continue;
        }
        suitableLoot.add((Loot) l);
      }
    }
    if (suitableLoot.size() == 0) {
      return new Loot("LOOT_DIRTYSOCK");
    }
    // PICK ONE
    do {
      ui().text("Do you want to run a special edition?").bold().add();
      int y = 'a';
      for (final Loot l : suitableLoot) {
        ui(R.id.gcontrol).button(y++).text(l.toString()).add();
      }
      ui(R.id.gcontrol).button(10).text("Not in this month's Liberal Guardian").add();
      final int c = getch();
      if (c == 10) {
        return null;
      }
      if (c >= 'a') {
        final Loot rval = suitableLoot.get(c - 'a');
        for (final Squad sq : i.squad) {
          sq.loot().remove(rval);
        }
        for (final Location loc : i.location) {
          loc.lcs().loot.remove(rval);
        }
        return rval;
      }
    } while (true);
  }

  /* monthly - guardian - prints liberal guardian special editions */
  private static void printnews(final Loot li, final int newspaper) {
    if (!i.freeSpeech()) {
      i.offended.put(CrimeSquad.FIREMEN, true);
    }
    setView(R.layout.generic);
    if (li.id().equals("LOOT_CEOPHOTOS")) // Tmp -XML
    {
      ui().text("The Liberal Guardian runs a story featuring photos of a major CEO").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(10)) {
      default:
        ui().text("engaging in lewd behavior with animals.").add();
        i.issue(Issue.ANIMALRESEARCH).changeOpinion(15, 1, 100);
        break;
      case 1:
        ui().text("digging up graves and sleeping with the dead.").add();
        break;
      case 2:
        ui().text("participating in a murder.").add();
        i.issue(Issue.POLICEBEHAVIOR).changeOpinion(15, 1, 100);
        i.issue(Issue.JUSTICES).changeOpinion(10, 1, 100);
        break;
      case 3:
        ui().text("engaging in heavy bondage.  A cucumber was involved in some way.").add();
        break;
      case 4:
        ui().text("tongue-kissing an infamous dictator.").add();
        break;
      case 5:
        ui().text("making out with an FDA official overseeing the CEO's products.").add();
        i.issue(Issue.GENETICS).changeOpinion(10, 1, 100);
        i.issue(Issue.POLLUTION).changeOpinion(10, 1, 100);
        break;
      case 6:
        ui().text("castrating himself.").add();
        break;
      case 7:
        ui().text("waving a Nazi flag at a supremacist rally.").add();
        break;
      case 8:
        ui().text("torturing an employee with a hot iron.").add();
        i.issue(Issue.LABOR).changeOpinion(10, 1, 100);
        break;
      case 9:
        ui().text("playing with feces and urine.").add();
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Corporations a little riled up.").add();
      i.issue(Issue.CEOSALARY).changeOpinion(50, 1, 100);
      i.issue(Issue.CORPORATECULTURE).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CORPORATE, true);
    } else if (li.id().equals("LOOT_CEOLOVELETTERS")) {
      ui().text("The Liberal Guardian runs a story featuring love letters from a major CEO").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(8)) {
      default:
        ui().text("addressed to his pet dog.  Yikes.").add();
        i.issue(Issue.ANIMALRESEARCH).changeOpinion(15, 1, 100);
        break;
      case 1:
        ui().text("to the judge that acquit him in a corruption trial.").add();
        i.issue(Issue.JUSTICES).changeOpinion(15, 1, 100);
        break;
      case 2:
        ui().text("to an illicit gay lover.").add();
        i.issue(Issue.GAY).changeOpinion(15, 1, 100);
        break;
      case 3:
        ui().text("to himself.  They're very steamy.").add();
        break;
      case 4:
        ui().text("implying that he has enslaved his houseservants.").add();
        i.issue(Issue.LABOR).changeOpinion(10, 1, 100);
        break;
      case 5:
        ui().text("to the FDA official overseeing the CEO's products.").add();
        i.issue(Issue.GENETICS).changeOpinion(10, 1, 100);
        i.issue(Issue.POLLUTION).changeOpinion(10, 1, 100);
        break;
      case 6:
        ui().text("that seem to touch on every fetish known to man.").add();
        break;
      case 7:
        ui().text("promising someone company profits in exchange for sexual favors.").add();
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Corporations a little riled up.").add();
      i.issue(Issue.CEOSALARY).changeOpinion(50, 1, 100);
      i.issue(Issue.CORPORATECULTURE).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CORPORATE, true);
    } else if (li.id().equals("LOOT_CEOTAXPAPERS")) {
      ui().text("The Liberal Guardian runs a story featuring a major CEO's tax papers").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(1)) {
      default:
        ui().text("showing that he has engaged in consistent tax evasion.").add();
        i.issue(Issue.TAX).changeOpinion(25, 1, 100);
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Corporations a little riled up.").add();
      i.issue(Issue.CEOSALARY).changeOpinion(50, 1, 100);
      i.issue(Issue.CORPORATECULTURE).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CORPORATE, true);
    } else if (li.id().equals("LOOT_CORPFILES")) {
      ui().text("The Liberal Guardian runs a story featuring Corporate files").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(newspaper * 10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(newspaper * 10, 1, 100);
      switch (i.rng.nextInt(5)) {
      default:
        ui().text("describing a genetic monster created in a lab.").add();
        i.issue(Issue.GENETICS).changeOpinion(50, 1, 100);
        break;
      case 1:
        ui().text("with a list of gay employees entitled \"Homo-workers\".").add();
        i.issue(Issue.GAY).changeOpinion(50, 1, 100);
        break;
      case 2:
        ui().text("containing a memo: \"Terminate the pregnancy, I terminate you.\"").add();
        i.issue(Issue.ABORTION).changeOpinion(50, 1, 100);
        break;
      case 3:
        ui().text("cheerfully describing foreign corporate sweatshops.").add();
        i.issue(Issue.LABOR).changeOpinion(50, 1, 100);
        break;
      case 4:
        ui().text("describing an intricate tax scheme.").add();
        i.issue(Issue.TAX).changeOpinion(50, 1, 100);
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Corporations a little riled up.").add();
      i.issue(Issue.CEOSALARY).changeOpinion(50, 1, 100);
      i.issue(Issue.CORPORATECULTURE).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CORPORATE, true);
    } else if (li.id().equals("LOOT_INTHQDISK") || li.id().equals("LOOT_SECRETDOCUMENTS")) {
      ui().text("The Liberal Guardian runs a story featuring CIA and other intelligence files")
          .add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(6)) {
      default:
        ui().text("documenting the overthrow of a government.").add();
        break;
      case 1:
        ui().text("documenting the planned assassination of a Liberal federal judge.").add();
        i.issue(Issue.JUSTICES).changeOpinion(50, 1, 100);
        break;
      case 2:
        ui().text("containing private information on innocent citizens.").add();
        break;
      case 3:
        ui().text("documenting \"harmful speech\" made by innocent citizens.").add();
        i.issue(Issue.FREESPEECH).changeOpinion(50, 1, 100);
        break;
      case 4:
        ui().text("used to keep tabs on gay citizens.").add();
        i.issue(Issue.GAY).changeOpinion(50, 1, 100);
        break;
      case 5:
        ui().text("documenting the infiltration of a pro-choice group.").add();
        i.issue(Issue.ABORTION).changeOpinion(50, 1, 100);
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Government a little riled up.").add();
      i.issue(Issue.PRIVACY).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CIA, true);
    } else if (li.id().equals("LOOT_POLICERECORDS")) {
      ui().text("The Liberal Guardian runs a story featuring police records").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(6)) {
      default:
        ui().text("documenting human rights abuses by the force.").add();
        i.issue(Issue.TORTURE).changeOpinion(15, 1, 100);
        break;
      case 1:
        ui().text("documenting a police torture case.").add();
        i.issue(Issue.TORTURE).changeOpinion(50, 1, 100);
        break;
      case 2:
        ui().text("documenting a systematic invasion of privacy by the force.").add();
        i.issue(Issue.PRIVACY).changeOpinion(15, 1, 100);
        break;
      case 3:
        ui().text("documenting a forced confession.").add();
        break;
      case 4:
        ui().text("documenting widespread corruption in the force.").add();
        break;
      case 5:
        ui().text("documenting gladiatorial matches held between prisoners by guards.").add();
        i.issue(Issue.DEATHPENALTY).changeOpinion(50, 1, 100);
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      i.issue(Issue.POLICEBEHAVIOR).changeOpinion(50, 1, 100);
    } else if (li.id().equals("LOOT_JUDGEFILES")) {
      ui().text("The Liberal Guardian runs a story with evidence of a Conservative judge").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(2)) {
      default:
        ui().text("taking bribes to acquit murderers.").add();
        break;
      case 1:
        ui().text("promising Conservative rulings in exchange for appointments.").add();
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      i.issue(Issue.JUSTICES).changeOpinion(50, 1, 100);
    } else if (li.id().equals("LOOT_RESEARCHFILES")) {
      ui().text("The Liberal Guardian runs a story featuring research papers").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(4)) {
      default:
        ui().text("documenting horrific animal rights abuses.").add();
        i.issue(Issue.ANIMALRESEARCH).changeOpinion(50, 1, 100);
        break;
      case 1:
        ui().text("studying the effects of torture on cats.").add();
        i.issue(Issue.ANIMALRESEARCH).changeOpinion(50, 1, 100);
        break;
      case 2:
        ui().text("covering up the accidental creation of a genetic monster.").add();
        i.issue(Issue.GENETICS).changeOpinion(50, 1, 100);
        break;
      case 3:
        ui().text("showing human test subjects dying under genetic research.").add();
        i.issue(Issue.GENETICS).changeOpinion(50, 1, 100);
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
    } else if (li.id().equals("LOOT_PRISONFILES")) {
      ui().text("The Liberal Guardian runs a story featuring prison documents").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(4)) {
      default:
        ui().text("documenting human rights abuses by prison guards.").add();
        break;
      case 1:
        ui().text("documenting a prison torture case.").add();
        i.issue(Issue.TORTURE).changeOpinion(50, 1, 100);
        break;
      case 2:
        ui().text("documenting widespread corruption among prison employees.").add();
        break;
      case 3:
        ui().text("documenting gladiatorial matches held between prisoners by guards.").add();
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      i.issue(Issue.DEATHPENALTY).changeOpinion(50, 1, 100);
    } else if (li.id().equals("LOOT_CABLENEWSFILES")) {
      ui().text("The Liberal Guardian runs a story featuring cable news memos").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(4)) {
      default:
        ui().text("calling their news 'the vanguard of Conservative thought'.").add();
        break;
      case 1:
        ui().text("mandating negative coverage of Liberal politicians.").add();
        break;
      case 2:
        ui().text("planning to drum up a false scandal about a Liberal figure.").add();
        break;
      case 3:
        ui().text("instructing a female anchor to 'get sexier or get a new job'.").add();
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Conservative masses a little riled up.").add();
      i.issue(Issue.CABLENEWS).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.CABLENEWS, true);
    } else if (li.id().equals("LOOT_AMRADIOFILES")) {
      ui().text("The Liberal Guardian runs a story featuring AM radio plans").add();
      i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
      i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(10, 1, 100);
      switch (i.rng.nextInt(3)) {
      default:
        ui().text("calling listeners 'sheep to be told what to think'.").add();
        break;
      case 1:
        ui().text("saying 'it's okay to lie, they don't need the truth'.").add();
        break;
      case 2:
        ui().text("planning to drum up a false scandal about a Liberal figure.").add();
        break;
      }
      ui().text("The major networks and publications take it up and run it for weeks.").add();
      ui().text("This is bound to get the Conservative masses a little riled up.").add();
      i.issue(Issue.AMRADIO).changeOpinion(50, 1, 100);
      i.offended.put(CrimeSquad.AMRADIO, true);
    }
    getch();
  }

  private static void updateInfluence(final int power, final int align) {
    i.issue(Issue.LIBERALCRIMESQUAD).addBackgroundInfluence(power * align);
    i.issue(Issue.CONSERVATIVECRIMESQUAD).addBackgroundInfluence(power * align);
  }

  /* rename various buildings according to the new laws */
  private static void updateworld_laws(final Map<Issue, Alignment> oldlaw) {
    if ((i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE || oldlaw
        .get(Issue.DEATHPENALTY) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.DEATHPENALTY).law().equals(oldlaw.get(Issue.DEATHPENALTY))) {
      for (final Location l : i.location) {
        if (l.type().isType(Prison.class)) {
          l.initlocation();
        }
        if (l.type().isType(CourtHouse.class)) {
          l.initlocation();
        }
      }
    }
    if (i.issue(Issue.GUNCONTROL).law() == Alignment.ELITELIBERAL
        && oldlaw.get(Issue.GUNCONTROL).ordinal() < Alignment.ELITELIBERAL.ordinal()) {
      for (final Location l : i.location) {
        if (l.type().isType(PawnShop.class)) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE || oldlaw
        .get(Issue.POLICEBEHAVIOR) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.POLICEBEHAVIOR).law().equals(oldlaw.get(Issue.POLICEBEHAVIOR))) {
      for (final Location l : i.location) {
        if (l.type().isType(Prison.class)) {
          l.initlocation();
        }
        if (l.type().isType(IntelligenceHq.class)) {
          l.initlocation();
        }
      }
    }
    if ((!i.freeSpeech() || oldlaw.get(Issue.FREESPEECH) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.FREESPEECH).law().equals(oldlaw.get(Issue.FREESPEECH))) {
      for (final Location l : i.location) {
        if (l.type().isType(FireStation.class)) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.PRIVACY).law() == Alignment.ARCHCONSERVATIVE || oldlaw.get(Issue.PRIVACY) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.PRIVACY).law().equals(oldlaw.get(Issue.PRIVACY))) {
      for (final Location l : i.location) {
        if (l.type().isType(IntelligenceHq.class)) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.CORPORATECULTURE).law() == Alignment.ARCHCONSERVATIVE || oldlaw
        .get(Issue.CORPORATECULTURE) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.CORPORATECULTURE).law().equals(oldlaw.get(Issue.CORPORATECULTURE))) {
      for (final Location l : i.location) {
        if (l.type().isType(House.class)) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.TAX).law() == Alignment.ARCHCONSERVATIVE || oldlaw.get(Issue.TAX) == Alignment.ARCHCONSERVATIVE)
        && !i.issue(Issue.TAX).law().equals(oldlaw.get(Issue.TAX))) {
      for (final Location l : i.location) {
        if (l.type().isType(House.class)) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.DRUGS).law() == Alignment.ELITELIBERAL || oldlaw.get(Issue.DRUGS) == Alignment.ELITELIBERAL)
        && !i.issue(Issue.DRUGS).law().equals(oldlaw.get(Issue.DRUGS))) {
      for (final Location l : i.location) {
        if (l.type().isType(CrackHouse.class) // Crack House, or
            // Recreational
            // Drugs Center?
            && l.renting() != null) {
          l.initlocation();
        }
      }
    }
    if ((i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL || oldlaw
        .get(Issue.NUCLEARPOWER) == Alignment.ELITELIBERAL)
        && !i.issue(Issue.NUCLEARPOWER).law().equals(oldlaw.get(Issue.NUCLEARPOWER))) {
      for (final Location l : i.location) {
        if (l.type().isType(Nuclear.class)) {
          l.initlocation();
        }
      }
    }
  }
}
