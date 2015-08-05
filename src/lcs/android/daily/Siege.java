package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.CreatureActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.combat.Fight;
import lcs.android.combat.Fight.Fighting;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureName;
import lcs.android.creature.CreatureType;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.Encounter;
import lcs.android.encounters.FootChase;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.Vehicle;
import lcs.android.law.Crime;
import lcs.android.monthly.EndGame;
import lcs.android.news.News;
import lcs.android.news.NewsEvent;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.Advance;
import lcs.android.site.Site;
import lcs.android.site.Squad;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.PoliceStation;
import lcs.android.site.type.Shelter;
import lcs.android.site.type.Warehouse;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class Siege implements Serializable {
  public Siege(final Location locale) {
    this.locale = locale;
  }

  private final Location locale;

  public int attackTime;

  public boolean camerasOff;

  public int escalationState;

  public int kills;

  public boolean lightsOff;

  public boolean siege;

  public CrimeSquad siegetype = null;

  public int tanks;

  public int timeUntilLocated = -1;

  public boolean underAttack;

  private int timeUntilCCS = -1;

  private int timeUntilCia = -1;

  private int timeUntilCorps = -1;

  private int timeUntilFiremen = -1;

  private final FootChase siegeEncounter = new FootChase();

  /** siege - flavor text when you fought off the raid */
  public void conquerText() {
    if (siegetype == CrimeSquad.POLICE) {
      fact("* * * * *   VICTORY   * * * * *\n\n"
          + "The Conservative automatons have been driven back -- for "
          + "the time being.  While they are regrouping, you might consider "
          + "abandoning this safe house for a safer location.");
    } else {
      fact("* * * * *   VICTORY   * * * * *\n\n"
          + "The Conservative automatons have been driven back. "
          + "Unfortunately, you will never truly be safe from "
          + "this filth until the Liberal Agenda is realized.");
    }
  }

  /** siege - flavor text when you crush a CCS safe house */
  public void conquerTextCcs() {
    if (i.score.ccsKills < 3) {
      fact("* * * * *   VICTORY   * * * * *\n\n"
          + "Gunfire still ringing in their ears, the squad revels in "
          + "their victory.  The CCS is bound to have another safe house, but "
          + "for now, their power has been severely weakened.  This will make "
          + "a fine base for future Liberal operations.");
    } else {
      fact("* * * * *   VICTORY   * * * * *\n\n"
          + "Gunfire still ringing in their ears, the squad revels in "
          + "their final victory.  As your Liberals pick through the remains of the safehouse, "
          + "it is increasingly clear that this was the CCS's last safe house. "
          + "The CCS has been completely destroyed.  Now wasn't there a revolution to attend to?\n\n"
          + "+200 JUICE TO EVERYONE FOR ERADICATING THE CONSERVATIVE CRIME SQUAD");
      for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
        p.addJuice(200, 1000);
      }
    }
  }

  /** siege - prepares for entering site i.mode to fight the siege */
  public void escapeEngage() {
    Location loc = i.currentLocation;
    if (i.activeSquad().location().get() != Location.none()) {
      loc = i.activeSquad().location().get();
    }
    if (loc == Location.none()) {
      return;
    }
    fact("UNDER ATTACK: ESCAPE OR ENGAGE\n\n"
        + "You are about to engage Conservative forces in battle. "
        + "You will find yourself in the Liberal safehouse, and it will "
        + "be swarming with Conservative units.  The Liberal Crime "
        + "Squad will be located far from the entrance to the safehouse. "
        + "It is your task to bring your squad out to safety, or fight "
        + "off the Conservatives within the perimeter.  Either way you "
        + "choose, any equipment from the safehouse which isn't held by a "
        + "Liberal will be scattered about the compound.  Save what "
        + "you can.  You might notice your Squad has filled out to "
        + "six members if any were available.  If you have a larger pool "
        + "of Liberals, they will be traveling behind the Squad. "
        + "There is a new button, (R)eorganize, which reflects this. "
        + "Squad members in the back with firearms can provide cover "
        + "fire.  If you have at least six people total, then six must "
        + "be in the Squad.  If less than six, then they all must."
        + (loc.lcs().compoundWalls.contains(Compound.CAMERAS) ? "Your security cameras let you see units on the map."
            : "")
        + (loc.lcs().compoundWalls.contains(Compound.TRAPS) ? "Your traps will harass the enemy, but not the Squad."
            : ""));
    if (siegetype == CrimeSquad.CCS) {
      if (loc.type().isType(Warehouse.class)) {
        loc.renting(CrimeSquad.CCS); // CCS Captures warehouse
      }
    }
    // -- this will be reversed if you fight them off
    // CRIMINALIZE
    if (siegetype == CrimeSquad.POLICE) {
      loc.criminalizeLiberalsInLocation(Crime.RESIST);
    }
    // DELETE ALL SQUADS IN THIS AREA UNLESS THEY ARE THE activesquad
    for (final Iterator<Squad> si = i.squad.iterator(); si.hasNext();) {
      final Squad sq = si.next();
      if (sq == i.activeSquad()) {
        continue;
      }
      if (sq.size() > 0) {
        if (sq.location().getNullable() == loc) {
          if (i.activeSquad() != null) {
            for (final Creature p : sq) {
              p.squad(null);
            }
            si.remove();
          } else {
            i.setActiveSquad(sq);
          }
        }
      }
    }
    if (i.activeSquad() == null) {
      final Squad squad = new Squad();
      i.squad.add(squad);
      if (i.currentLocation.lcs().frontBusiness == null) {
        squad.name(i.currentLocation + " Defense");
      } else {
        squad.name(i.currentLocation.lcs().toString() + " Defense");
      }
      int j = 0;
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(i.currentLocation))) {
        squad.add(p);
        j++;
        if (j >= 6) {
          break;
        }
      }
      i.setActiveSquad(squad);
    }
    loc.autoPromote();
    final NewsStory ns = new NewsStory(underAttack ? StoryType.SQUAD_FLEDATTACK
        : StoryType.SQUAD_ESCAPED);
    ns.positive = true;
    ns.location(loc);
    ns.siegetype = siegetype;
    i.newsStories.add(ns);
    Site.travelToSite(loc);
  }

  /** siege - what happens when you escaped the siege */
  public void escapeSiege(final boolean won) {
    if (!won) {
      fact("You have escaped!\n\n" + "The Conservatives thought that the Liberal Crime Squad was "
          + "finished, but once again, Conservative Thinking has proven "
          + "itself to be based on Unsound Notions. "
          + "However, all is not well.  In your haste to escape you have "
          + "lost everything that you've left behind.  You'll have "
          + "to start from scratch in a new safe house.  Your "
          + "funds remain under your control, fortunately.  Your flight has "
          + "given you some time to regroup, but the Conservatives will "
          + "doubtless be preparing another assault.  Time to split up and "
          + "lay low for a few days");
      final Location homes = AbstractSiteType.type(Shelter.class).getLocation();
      // dump retrieved loot in homeless shelter - is there anywhere
      // better to put it?
      if (i.activeSquad() != null && homes != null) {
        homes.lcs().loot.addAll(i.activeSquad().loot());
        i.activeSquad().loot().clear();
      }
      i.setActiveSquad(null);
      /* active squad cannot be disbanded in CommonActions.removesquadinfo, but we need to disband
       * current squad as the people are going to be 'away'. GET RID OF DEAD, etc. */
      i.site.current().renting(null);
      for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
        if (p.location().getNullable() != i.site.current()) {
          continue;
        }
        if (!p.health().alive()) {
          i.pool.remove(p);
          continue;
        }
        // BASE EVERYONE LEFT AT HOMELESS SHELTER
        p.removeSquadInfo();
        p.hiding(i.rng.nextInt(3) + 2);
        if (p.alignment() == Alignment.LIBERAL) {
          p.location(null);
        } else {
          // hostages don't go into hiding, just shove em into the
          // homeless shelter
          p.location(homes);
        }
        p.base(homes);
      }
      i.site.current().lcs().loot.clear();
      for (final Iterator<Vehicle> vi = i.vehicle.iterator(); vi.hasNext();) {
        final Vehicle v = vi.next();
        if (v.location() == i.site.current()) {
          vi.remove();
        }
      }
      i.site.current().lcs().compoundWalls.clear();
      i.site.current().lcs().compoundStores = 0;
      i.site.current().lcs().frontBusiness = null;
      i.site.current().initlocation();
    }
    // SET UP NEW SIEGE CHARACTERISTICS, INCLUDING TIMING
    final Siege THIS = i.site.current().lcs().siege;
    THIS.siege = false;
    if (won) {
      if (THIS.siegetype == CrimeSquad.POLICE) {
        THIS.timeUntilLocated = i.rng.nextInt(4) + 4;
        THIS.escalationState++;
      }
    }
  }

  /** siege - handles giving up */
  public void giveup() {
    final Siege THIS = i.currentLocation.lcs().siege;
    Location loc = locale;
    if (i.currentLocation != null) {
      loc = i.currentLocation;
    }
    if (i.activeSquad() != null && i.activeSquad().location().exists()) {
      loc = i.activeSquad().location().get();
    }
    if (loc == null) {
      return;
    }
    loc.renting(null);
    // IF POLICE, END SIEGE
    if (THIS.siegetype == CrimeSquad.POLICE || THIS.siegetype == CrimeSquad.FIREMEN) {
      final Location polsta = AbstractSiteType.type(PoliceStation.class).getLocation();
      // END SIEGE
      setView(R.layout.generic);
      if (THIS.siegetype == CrimeSquad.POLICE && THIS.escalationState == 0) {
        ui().text("The police confiscate everything, including Squad weapons.").add();
      } else if (THIS.siegetype == CrimeSquad.POLICE && THIS.escalationState == 0) {
        ui().text("The soldiers confiscate everything, including Squad weapons.").add();
      } else {
        ui().text("The firemen confiscate everything, including Squad weapons.").add();
      }
      int kcount = 0;
      int pcount = 0;
      String kname = "", pname = "", pcname = "";
      int icount = 0;
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(loc))) {
        if (p.hasFlag(CreatureFlag.ILLEGAL_ALIEN)) {
          icount++;
        }
        if (p.hasFlag(CreatureFlag.MISSING) && p.alignment() == Alignment.CONSERVATIVE) {
          kcount++;
          kname = p.properName();
          if (p.type() == CreatureType.valueOf("RADIOPERSONALITY")) {
            i.offended.put(CrimeSquad.AMRADIO, true);
          }
          if (p.type() == CreatureType.valueOf("NEWSANCHOR")) {
            i.offended.put(CrimeSquad.CABLENEWS, true);
          }
        }
      }
      // CRIMINALIZE POOL IF FOUND WITH KIDNAP VICTIM OR ALIEN
      if (kcount > 0) {
        loc.criminalizeLiberalsInLocation(Crime.KIDNAPPING);
      }
      if (icount > 0) {
        loc.criminalizeLiberalsInLocation(Crime.HIREILLEGAL);
      }
      if (THIS.siegetype == CrimeSquad.FIREMEN) {
        if (loc.lcs().compoundWalls.contains(Compound.PRINTINGPRESS)) {
          // Criminalize i.pool for unacceptable speech
          if (icount > 0) {
            loc.criminalizeLiberalsInLocation(Crime.SPEECH);
          }
        }
      }
      // LOOK FOR PRISONERS (MUST BE AFTER CRIMINALIZATION ABOVE)
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(loc))) {
        if (p.crime().isCriminal()
            && !(p.hasFlag(CreatureFlag.MISSING) && p.alignment() == Alignment.CONSERVATIVE)) {
          pcount++;
          pname = p.properName();
          pcname = p.toString();
        }
      }
      if (kcount == 1) {
        ui().text(kname + " is rehabilitated and freed.").add();
      }
      if (kcount > 1) {
        ui().text("The kidnap victims are rehabilitated and freed.").add();
      }
      if (pcount == 1) {
        if (!pname.equals(pcname)) {
          ui().text(pname + ", aka " + pcname + ", is taken to the police station.").add();
        } else {
          ui().text(pname + " is taken to the police station.").add();
        }
      }
      if (pcount > 1) {
        ui().text(pcount + " Liberals are taken to the police station.").add();
      }
      if (i.ledger.funds() > 0) {
        if (i.ledger.funds() <= 10000 || THIS.siegetype == CrimeSquad.FIREMEN) {
          ui().text("Fortunately, your funds remain intact.").add();
        } else {
          final int confiscated = i.rng.nextInt(i.rng.nextInt(i.ledger.funds() - 10000) + 1) + 1000;
          ui().text("Law enforcement have confiscated $" + confiscated + " in LCS funds.").add();
          i.ledger.subtractFunds(confiscated, Ledger.ExpenseType.CONFISCATED);
        }
      }
      if (THIS.siegetype == CrimeSquad.FIREMEN) {
        if (loc.lcs().compoundWalls.contains(Compound.PRINTINGPRESS)) {
          // Criminalize i.pool for unacceptable speech
          if (icount > 0) {
            loc.criminalizeLiberalsInLocation(Crime.SPEECH);
          }
          ui().text("The printing press is dismantled and burned.").add();
          loc.lcs().compoundWalls.remove(Compound.PRINTINGPRESS);
        }
      } else if (!loc.lcs().compoundWalls.isEmpty()) {
        ui().text("The compound is dismantled.").add();
        loc.lcs().compoundWalls.clear();
      }
      if (loc.lcs().frontBusiness != null) {
        ui().text("Materials relating to the business front have been taken.").add();
        loc.lcs().frontBusiness = null;
      }
      getch();
      THIS.siege = false;
      if (THIS.siegetype == CrimeSquad.FIREMEN) {
        i.offended.put(CrimeSquad.FIREMEN, false); // Firemen do not
      }
      // hold grudges
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(loc))) {
        // ALL KIDNAP VICTIMS FREED REGARDLESS OF CRIMES
        if (p.hasFlag(CreatureFlag.MISSING) || !p.health().alive()) {
          // Clear actions for anybody who was tending to this person
          for (final Creature j : Filter.of(i.pool, Filter.LIVING)) {
            if (j.activity().type() == Activity.HOSTAGETENDING) {
              final CreatureActivity ca = (CreatureActivity) j.activity();
              if (ca.creature() == p) {
                j.activity(BareActivity.noActivity());
              }
            }
          }
          p.removeSquadInfo();
          i.pool.remove(p);
          continue;
        }
        // TAKE SQUAD EQUIPMENT
        if (p.squad().exists()) {
          p.squad().get().loot().clear();
        }
        p.weapon().dropWeaponsAndClips(null);
        if (p.crime().isCriminal()) {
          p.removeSquadInfo();
          p.location(polsta);
          p.activity(BareActivity.noActivity());
        }
      }
      THIS.siege = false;
    } else {
      // OTHERWISE IT IS SUICIDE
      int killnumber = 0;
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(loc))) {
        killnumber++;
        p.health().die();
      }
      if (THIS.siegetype == CrimeSquad.CCS) {
        if (loc.type().isType(Warehouse.class)) {
          loc.renting(CrimeSquad.CCS); // CCS Captures
        }
      }
      // warehouse
      setView(R.layout.generic);
      ui().text("Everyone in the " + loc + " is slain.").add();
      getch();
      final NewsStory ns = new NewsStory(StoryType.MASSACRE);
      ns.location(loc);
      ns.siegetype = THIS.siegetype;
      ns.count = killnumber;
      i.newsStories.add(ns);
      // MUST SET i.cursite TO SATISFY CommonActions.endcheck() CODE
      EndGame.endcheck(EndGame.endCauseFromSite(loc));
      THIS.siege = false;
    }
    // CONFISCATE MATERIAL
    loc.lcs().loot.clear();
    for (final Iterator<Vehicle> vi = i.vehicle.iterator(); vi.hasNext();) {
      final Vehicle v = vi.next();
      if (v.location() == i.site.current()) {
        vi.remove();
      }
    }
  }

  /** siege - prepares for exiting the siege to fight the attackers head on */
  public void sallyForth() {
    Location loc = locale;
    // GIVE INFO SCREEN
    fact("UNDER SIEGE: ESCAPE OR ENGAGE\n\n"
        + "You are about to exit the compound to lift the Conservative "
        + "siege on your safehouse.  The enemy is ready for you, and "
        + "you will have to defeat them all or run away to survive this " + "encounter.\n\n"
        + "Your Squad has filled out to six members if any were "
        + "available.  If you have a larger pool of Liberals, they "
        + "will provide cover fire from the compound until needed.");
    if (i.currentLocation != null) {
      loc = i.currentLocation;
    }
    if (i.activeSquad() != null && i.activeSquad().location().exists()) {
      loc = i.activeSquad().location().get();
    }
    if (loc == null) {
      return;
    }
    if (loc.lcs().siege.siegetype == CrimeSquad.CCS) {
      if (loc.type().isType(Warehouse.class)) {
        loc.renting(CrimeSquad.CCS); // CCS Captures warehouse
      }
    }
    // -- this will be
    // reversed if you fight
    // them off
    // CRIMINALIZE
    if (loc.lcs().siege.siegetype == CrimeSquad.POLICE) {
      loc.criminalizeLiberalsInLocation(Crime.RESIST);
    }
    // DELETE ALL SQUADS IN THIS AREA UNLESS THEY ARE THE i.activesquad
    for (final Iterator<Squad> sqi = i.squad.iterator(); sqi.hasNext();) {
      final Squad sq = sqi.next();
      if (sq == i.activeSquad()) {
        continue;
      }
      if (sq.size() > 0) {
        if (sq.location().getNullable() == loc) {
          if (i.activeSquad() != null) {
            for (final Creature p : sq) {
              p.squad(null);
            }
            // delete sq;
            sqi.remove();
          } else {
            i.setActiveSquad(sq);
          }
        }
      }
    }
    // No squads at the location? Form a new one.
    final Squad squad = new Squad();
    i.squad.add(squad);
    if (i.currentLocation.lcs().frontBusiness == null) {
      squad.name(i.currentLocation.toString().toString() + " Defense");
    } else {
      squad.name(i.currentLocation.lcs().toString() + " Defense");
    }
    int j = 0;
    for (final Creature p : Filter.of(i.pool, Filter.livingIn(i.currentLocation))) {
      squad.add(p);
      j++;
      if (j >= 6) {
        break;
      }
    }
    i.setActiveSquad(squad);
    // MAKE SURE PARTY IS ORGANIZED
    loc.autoPromote();
    // START FIGHTING
    final int result = sallyForthAux();
    final NewsStory ns = new NewsStory(result == 2 ? StoryType.SQUAD_BROKESIEGE
        : StoryType.SQUAD_ESCAPED);
    ns.positive = true;
    ns.location(loc);
    ns.siegetype = loc.lcs().siege.siegetype;
    i.newsStories.add(ns);
    i.siteStory = ns;
  }

  private void generateSiegeSpecials() {
    final Location loc = locale;
    switch (siegetype) {
    case CIA:
    case HICKS:
    case CORPORATE:
    case CCS:
    case FIREMEN:
    default:
      break;
    case POLICE: // Currently only police sieges should allow
      // this
      // SWAT teams
      if (escalationState == 0) {
        for (int e = 0; e < Encounter.ENCMAX - 9; e++) {
          CreatureType.valueOf("SWAT");
          siegeEncounter.makeEncounterCreature("SWAT");
        }
      } else if (escalationState >= 1) {
        for (int e = 0; e < Encounter.ENCMAX - 9; e++) {
          CreatureType.valueOf("SOLDIER");
          siegeEncounter.makeEncounterCreature("SOLDIER");
        }
      }
      // M1 Tank
      if (escalationState >= 2 && !loc.lcs().compoundWalls.contains(Compound.TANKTRAPS)) {
        CreatureType.valueOf("TANK");
        siegeEncounter.makeEncounterCreature("TANK");
      }
      break;
    }
  }

  /** Siege -- Mass combat outside safehouse */
  private int sallyForthAux() {
    Fight.reloadparty();
    // i.site.current = locale; TODO bad code...
    generateSiegeSpecials();
    i.mode(GameMode.CHASEFOOT);
    boolean ranaway = false;
    do {
      // Count heroes
      int partysize = 0;
      int partyalive = 0;
      for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
        if (p.alignment() == Alignment.LIBERAL && p.location().getNullable() == i.currentLocation
            && !p.hasFlag(CreatureFlag.SLEEPER)) {
          partysize++;
          if (p.health().alive()) {
            partyalive++;
          }
        }
      }
      // Let's roll
      locale.autoPromote();
      setView(R.layout.hospital);
      locale.toString();
      // Player's party
      i.activeSquad().printParty();
      if (partyalive > 0) {
        // Options
        maybeAddButton(R.id.gcontrol, 'o', "Change the squad's Liberal order", partysize > 1);
        ui(R.id.gcontrol).button('d').text("Escape").add();
        ui(R.id.gcontrol).button('e').text("Equip").add();
        ui(R.id.gcontrol).button('f').text("Fight!").add();
        ui(R.id.gcontrol).button('g').text("Surrender").add();
      } else {
        ui(R.id.gcontrol).button('c').text("Reflect on your Conservative judgment.").add();
      }
      // Enemies
      siegeEncounter.printEncounter();
      final int c = getch();
      clearChildren(R.id.gcontrol);
      // Reflecting on your poor judgment
      if (partyalive == 0 && c == 'c') {
        EndGame.endcheck(null);
        i.mode(GameMode.BASE);
        return 0;
      }
      // Providing orders
      if (partyalive > 0) {
        // Reorder
        if (c == 'o' && partysize > 1) {
          Squad.orderParty();
        }
        // View status
        i.activeSquad().displaySquadInfo(c);
        // Surrender
        if (c == 'g') {
          giveup();
          return 0;
        }
        // Run away
        if (c == 'd') {
          if (siegeEncounter.creatures().get(0).type() == CreatureType.valueOf("COP")) {
            i.siteStory.addNews(NewsEvent.FOOT_CHASE);
            i.activeSquad().criminalizeParty(Crime.RESIST);
          }
          final FootChase fc = new FootChase(siegeEncounter);
          fc.encounter();
          ranaway = true;
        }
        if (c == 'f') {
          Fight.fight(Fighting.BOTH);
          Advance.creatureAdvance();
          getch();
        }
        if (c == 'e') {
          AbstractItem.equip(locale.lcs().loot, null);
        }
        // Check for victory
        partysize = 0;
        partyalive = 0;
        for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
          if (p.alignment() == Alignment.LIBERAL && p.location().getNullable() == i.site.current()
              && !p.hasFlag(CreatureFlag.SLEEPER)) {
            partysize++;
            if (p.health().alive()) {
              partyalive++;
            }
          }
        }
        final int baddiecount = siegeEncounter.creatures().size();
        if (partyalive > 0 && baddiecount == 0) {
          for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
            for (final BodyPart bp : BodyPart.values()) {
              p.health().wounds().get(bp).remove(Wound.BLEEDING);
            }
          }
          i.mode(GameMode.BASE);
          if (ranaway) {
            // clearmessagearea();
            ui().text("You're free!").add();
            getch();
            i.site.current().lcs().siege.escapeSiege(false);
            return 1;
          }
          // clearmessagearea();
          ui().text("The siege is broken!").add();
          getch();
          i.site.current().lcs().siege.conquerText();
          i.site.current().lcs().siege.escapeSiege(true);
          return 2;
        }
      }
    } while (true);
  }

  private void siegecheck() {
    final Location location = locale;
    // FIRST, THE COPS
    int numpres;
    if (AbstractSiteType.type(PoliceStation.class).getLocation().closed()) {
      location.lcs().heat = (int) (location.lcs().heat * 0.95);
      return;
    }
    final Siege THIS = location.lcs().siege;
    if (THIS.siege) {
      return;
    }
    if (location.renting() != CrimeSquad.LCS) {
      return;
    }
    numpres = 0;
    if (THIS.timeUntilLocated == -2) {
      // IF JUST SIEGED, BUY SOME TIME
      THIS.timeUntilLocated = -1;
    } else {
      // HUNTING
      if (THIS.timeUntilLocated > 0) {
        if (location.lcs().frontBusiness == null || i.rng.chance(2)) {
          THIS.timeUntilLocated--;
          // Hunt faster if location is extremely hot
          if (location.lcs().heat > 300) {
            int hunt_speed;
            hunt_speed = location.lcs().heat / 150;
            while (hunt_speed > 0 && THIS.timeUntilLocated > 1) {
              THIS.timeUntilLocated--;
              hunt_speed--;
            }
          }
        }
      }
      // CHECK FOR CRIMINALS AT THIS BASE
      int crimes = 0;
      final int kidnapped = calculateKidnapped();
      // int heatprotection = 0;
      for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
        // Sleepers and people not at this base don't count
        if (p.location().getNullable() != location || p.hasFlag(CreatureFlag.SLEEPER)) {
          continue;
        }
        if (!p.health().alive()) // Corpses attract attention
        {
          crimes += 10;
          continue;
        }
        if (p.hasFlag(CreatureFlag.KIDNAPPED) && p.alignment() != Alignment.LIBERAL) {
          crimes += 5 * p.joindays(); // Kidnapped persons
          // increase heat
          continue;
        }
        if (p.alignment() != Alignment.LIBERAL) {
          continue; // Non-liberals don't count other than that
        }
        numpres++;
        // Heat doesn't matter for sieges until it gets high
        crimes += Math.max(0, p.crime().heat() - 10);
      }
      // Let the place cool off if nobody is present
      if (crimes == 0) {
        location.lcs().heat -= 5;
        if (location.lcs().heat < 0) {
          location.lcs().heat = 0;
        }
      } else { // TODO check the sums on this
        // Determine how effective your current safehouse
        // is at keeping the police confused
        location.heatProtection();
        // Having hostages increases heat
        if (kidnapped != 0) {
          crimes += kidnapped * 20;
        }
        // Update location heat
        final int max_heat = crimes * (100 - location.heatProtection());
        location.lcs().heat += (max_heat - location.lcs().heat) / 10;
        // Begin planning siege if high heat on location
        if (i.rng.nextInt(3000) < location.lcs().heat && !(THIS.timeUntilLocated >= 0)) // Do
        // not
        // re-plan
        // siege.
        {
          // Set force deployment (military, bombers, etc.)
          if (i.rng.nextInt(location.lcs().heat) > 100) {
            THIS.escalationState++;
          }
          if (i.rng.nextInt(location.lcs().heat) > 250) {
            THIS.escalationState++;
          }
          if (i.rng.nextInt(location.lcs().heat) > 500) {
            THIS.escalationState++;
          }
          if (THIS.escalationState > 3) {
            THIS.escalationState = 3;
          }
          // Set time until siege is carried out
          THIS.timeUntilLocated += 14 + i.rng.nextInt(15);
        }
      }
      // *JDS* Sleepers at the police department may give a warning
      // just before police raids
      if (THIS.timeUntilLocated == 1) {
        boolean policesleeperwarning = false;
        for (final Creature pl : Filter.of(i.pool, Filter.ALL)) {
          if (pl.hasFlag(CreatureFlag.SLEEPER) && pl.location().exists()
              && pl.location().get().type().isType(PoliceStation.class)) {
            policesleeperwarning = true;
            break;
          }
        }
        if (policesleeperwarning) {
          setView(R.layout.generic);
          ui().text(
              "You have received advance warning from your sleepers regarding an imminent police raid on "
                  + location + ".").add();
          if (THIS.escalationState >= 2) {
            ui().text("The fighting force will be composed of national guard troops.").add();
          }
          if (THIS.escalationState >= 2) {
            ui().text("A tank will cover the entrance to the compound.").add();
          }
          if (THIS.escalationState >= 3) {
            ui().text("Planes will bomb the compound during the siege.").add();
          }
          waitOnOK();
        }
      }
      // COPS RAID THIS LOCATION
      if (THIS.timeUntilLocated == 0) {
        THIS.timeUntilLocated = -2;
        /* l.lcs.heat-=250; if(l.lcs.heat<0) */location.lcs().heat = 0;
        if (numpres > 0) {
          setView(R.layout.generic);
          ui().text("The police have surrounded the " + location.toString() + "!").add();
          THIS.underAttack = false;
          // MENTION ESCALATION STATE
          if (THIS.escalationState >= 1) {
            ui().text("National Guard troops are replacing normal SWAT units.").add();
          }
          if (THIS.escalationState >= 2) {
            if (location.lcs().compoundWalls.contains(Compound.TANKTRAPS)) {
              ui().text("An M1 Abrams Tank is stopped by the tank traps.").add();
            } else {
              ui().text("An M1 Abrams Tank takes up position outside the compound.").add();
            }
          }
          if (THIS.escalationState >= 3) {
            ui().text("You hear jet bombers streak overhead.").add();
          }
          location.stateBrokenLaws();
          THIS.siege = true;
          THIS.siegetype = CrimeSquad.POLICE;
          THIS.lightsOff = false;
          THIS.camerasOff = false;
          waitOnOK();
        } else {
          setView(R.layout.generic);
          ui().text("The cops have raided the " + location + ", an unoccupied safehouse.").add();
          for (final Creature p : Filter.of(i.pool, Filter.livingIn(location))) {
            if (!p.health().alive()) {
              ui().text(p + "'s corpse has been recovered.").add();
              i.pool.remove(p);
              continue;
            }
            if (p.alignment() != Alignment.LIBERAL) {
              ui().text(p + " has been rescued.").add();
              i.pool.remove(p);
              continue;
            }
          }
          location.lcs().loot.clear();
          for (final Iterator<Vehicle> vi = i.vehicle.iterator(); vi.hasNext();) {
            final Vehicle v = vi.next();
            if (v.location() == i.site.current()) {
              vi.remove();
            }
          }
          waitOnOK();
        }
      }
      // OTHER OFFENDABLE ENTITIES
      // CORPS
      if (location.lcs().heat > 0 && THIS.timeUntilCorps == -1 && !THIS.siege
          && i.offended.get(CrimeSquad.CORPORATE) && i.rng.chance(600) && numpres > 0) {
        THIS.timeUntilCorps = i.rng.nextInt(3) + 1;
        // *JDS* CEO sleepers may give a warning before corp raids
        boolean ceosleepercount = false;
        for (final Creature pl : Filter.of(i.pool, Filter.ALL)) {
          if (pl.hasFlag(CreatureFlag.SLEEPER)
              && pl.type() == CreatureType.valueOf("CORPORATE_CEO")) {
            ceosleepercount = true;
            break;
          }
        }
        if (ceosleepercount || i.rng.chance(5)) {
          setView(R.layout.generic);
          ui().text("You have received ").add();
          if (ceosleepercount) {
            ui().text("your sleeper CEO's warning").add();
          } else {
            ui().text("an anonymous tip").add();
          }
          ui().text(" that the Corporations").add();
          ui().text("are hiring mercenaries to attack ").add();
          if (ceosleepercount) {
            ui().text(location.toString()).add();
          } else {
            ui().text("the LCS").add();
          }
          ui().text(".").add();
          waitOnOK();
        }
      } else if (THIS.timeUntilCorps > 0) {
        THIS.timeUntilCorps--; // Corp raid countdown!
      } else if (THIS.timeUntilCorps == 0 && !THIS.siege && i.offended.get(CrimeSquad.CORPORATE)
          && numpres > 0) {
        THIS.timeUntilCorps = -1;
        // Corps raid!
        setView(R.layout.generic);
        ui().text("Corporate mercenaries are raiding the ").add();
        location.toString();
        ui().text("!").add();
        waitOnOK();
        THIS.siege = true;
        THIS.siegetype = CrimeSquad.CORPORATE;
        THIS.underAttack = true;
        THIS.lightsOff = false;
        THIS.camerasOff = false;
        i.offended.put(CrimeSquad.CORPORATE, false);
      } else if (THIS.timeUntilCorps == 0) {
        THIS.timeUntilCorps = -1; // Silently call off
      }
      // foiled corp raids
      // CONSERVATIVE CRIME SQUAD
      final boolean ccs_active = i.endgameState.ccsActive();
      final boolean target_interesting = ccs_active
          || location.lcs().compoundWalls.contains(Compound.PRINTINGPRESS);
      if (ccs_active && target_interesting) {
        if (location.lcs().heat > 0 && THIS.timeUntilCCS == -1 && !THIS.siege && i.rng.chance(60)
            && numpres > 0) {
          THIS.timeUntilCCS = i.rng.nextInt(3) + 1;
          // CCS sleepers may give a warning before raids
          int ccssleepercount = 0;
          for (final Creature pl : Filter.of(i.pool, Filter.ALL)) {
            if (pl.hasFlag(CreatureFlag.SLEEPER)
                && (pl.type() == CreatureType.valueOf("CCS_VIGILANTE")
                    || pl.type() == CreatureType.valueOf("CCS_ARCHCONSERVATIVE")
                    || pl.type() == CreatureType.valueOf("CCS_MOLOTOV") || pl.type() == CreatureType
                    .valueOf("CCS_SNIPER"))) {
              ccssleepercount = 1;
              break;
            }
          }
          if (ccssleepercount > 1) {
            fact("You have received a sleeper warning that the CCS is gearing up to attack "
                + location.toString() + ".");
          }
        } else if (THIS.timeUntilCCS > 0) {
          THIS.timeUntilCCS--; // CCS raid countdown!
        } else if (THIS.timeUntilCCS == 0 && !THIS.siege && numpres > 0) {
          THIS.timeUntilCCS = -1;
          // CCS raid!
          setView(R.layout.generic);
          ui().text("A screeching truck pulls up to ").add();
          location.toString();
          ui().text("!").add();
          if (!location.lcs().compoundWalls.contains(Compound.TANKTRAPS) && i.rng.chance(5)) {
            // CCS Carbombs safehouse!!
            ui().text("The truck plows into the building and explodes!").color(Color.RED).add();
            getch();
            setView(R.layout.generic);
            ui().text("CCS CAR BOMBING CASUALTY REPORT").add();
            final List<Creature> killed = new ArrayList<Creature>();
            final List<Creature> injured = new ArrayList<Creature>();
            for (final Creature j : Filter.of(i.pool, Filter.livingIn(location))) {
              if (i.rng.chance(2)) {
                j.health().blood(j.health().blood() - (i.rng.nextInt(101 - j.juice() / 10) + 10));
                if (!j.health().alive()) {
                  killed.add(j);
                } else {
                  injured.add(j);
                }
              }
            }
            ui().text("KILLED: ").bold().add();
            for (final Creature j : killed) {
              ui().text(j.toString()).color(j.alignment().color()).add();
            }
            ui().text("INJURED: ").bold().add();
            for (final Creature j : injured) {
              ui().text(j.toString()).color(j.alignment().color()).add();
            }
            waitOnOK();
          } else {
            // CCS Raids safehouse
            setView(R.layout.generic);
            ui().text("CCS members pour out of the truck and shoot in the front doors!")
                .color(Color.RED).add();
            getch();
            THIS.siege = true;
            THIS.siegetype = CrimeSquad.CCS;
            THIS.underAttack = true;
            THIS.lightsOff = false;
            THIS.camerasOff = false;
          }
        } else if (THIS.timeUntilCCS == 0) {
          THIS.timeUntilCCS = -1; // Silently call
        }
      }
      // off foiled
      // ccs raids
      // CIA
      if (location.lcs().heat > 0 && THIS.timeUntilCia == -1 && !THIS.siege
          && i.offended.get(CrimeSquad.CIA) && i.rng.chance(300) && numpres > 0) {
        THIS.timeUntilCia = i.rng.nextInt(3) + 1;
        // *JDS* agent sleepers may give a warning before cia raids
        boolean agentsleepercount = false;
        for (final Creature pl : Filter.of(i.pool, Filter.LIVING)) {
          if (pl.hasFlag(CreatureFlag.SLEEPER) && pl.type() == CreatureType.valueOf("AGENT")) {// if(pl.infiltration*100>i.rng.getInt(100))
            agentsleepercount = true;
            break;
          }
        }
        if (agentsleepercount) {
          setView(R.layout.generic);
          ui().text("A sleeper agent has reported that the CIA is planning").add();
          ui().text("to launch an attack on ").add();
          ui().text(location.toString()).add();
          ui().text(".").add();
          waitOnOK();
        }
      } else if (THIS.timeUntilCia > 0) {
        THIS.timeUntilCia--; // CIA raid countdown!
      } else if (THIS.timeUntilCia == 0 && !THIS.siege && i.offended.get(CrimeSquad.CIA)
          && numpres > 0) {
        THIS.timeUntilCia = -1;
        // CIA raids!
        setView(R.layout.generic);
        ui().text("Unmarked black vans are surrounding the " + location + "!").add();
        if (location.lcs().compoundWalls.contains(Compound.CAMERAS)) {
          ui().text("Through some form of high technology, they've managed").add();
          ui().text("to shut off the lights and the cameras!").add();
        } else if (location.lcs().compoundWalls.contains(Compound.GENERATOR)) {
          ui().text("Through some form of high technology, they've managed").add();
          ui().text("to shut off the lights!").add();
        } else {
          ui().text("They've shut off the lights!").add();
        }
        waitOnOK();
        THIS.siege = true;
        THIS.siegetype = CrimeSquad.CIA;
        THIS.underAttack = true;
        THIS.lightsOff = true;
        THIS.camerasOff = true;
      } else if (THIS.timeUntilCia == 0) {
        THIS.timeUntilCia = -1; // Silently call off
      }
      // foiled cia raids
      // HICKS
      if (!THIS.siege && i.offended.get(CrimeSquad.AMRADIO)
          && i.issue(Issue.AMRADIO).attitude() <= 35 && i.rng.chance(600) && numpres > 0) {
        setView(R.layout.generic);
        ui().text("Masses dissatisfied with your lack of respect for AM Radio").add();
        ui().text("are storming the ").add();
        location.toString();
        ui().text("!").add();
        waitOnOK();
        THIS.siege = true;
        THIS.siegetype = CrimeSquad.HICKS;
        THIS.underAttack = true;
        THIS.lightsOff = false;
        THIS.camerasOff = false;
        i.offended.put(CrimeSquad.AMRADIO, false);
      }
      if (!THIS.siege && i.offended.get(CrimeSquad.CABLENEWS)
          && i.issue(Issue.CABLENEWS).attitude() <= 35 && i.rng.chance(600) && numpres > 0) {
        setView(R.layout.generic);
        ui().text(
            "Masses dissatisfied with your lack of respect for Cable News are storming the "
                + location.toString() + "!").add();
        waitOnOK();
        THIS.siege = true;
        THIS.siegetype = CrimeSquad.HICKS;
        THIS.underAttack = true;
        THIS.lightsOff = false;
        THIS.camerasOff = false;
        i.offended.put(CrimeSquad.CABLENEWS, false);
      }
      // Firemen
      if (!i.freeSpeech() && THIS.timeUntilFiremen == -1 && !THIS.siege
          && i.offended.get(CrimeSquad.FIREMEN) && numpres > 0
          && location.lcs().compoundWalls.contains(Compound.PRINTINGPRESS) && i.rng.chance(90)) {
        THIS.timeUntilFiremen = i.rng.nextInt(3) + 1;
        // Sleeper Firemen can warn you of an impending raid
        int firemensleepercount = 0;
        for (final Creature pl : Filter.of(i.pool, Filter.LIVING)) {
          if (pl.hasFlag(CreatureFlag.SLEEPER) && pl.type() == CreatureType.valueOf("FIREFIGHTER")) {
            firemensleepercount++;
          }
        }
        if (i.rng.nextInt(firemensleepercount + 1) > 0 || i.rng.chance(10)) {
          setView(R.layout.generic);
          if (firemensleepercount == 1) {
            ui().text("A sleeper Fireman has informed you that").add();
          } else {
            ui().text("Word in the underground is that").add();
          }
          ui().text("the Firemen are planning to burn ").add();
          ui().text(location.toString()).add();
          ui().text(".").add();
          waitOnOK();
        }
      } else if (THIS.timeUntilFiremen > 0) {
        THIS.timeUntilFiremen--;
      } else if (THIS.timeUntilFiremen == 0 && !THIS.siege && numpres > 0) {
        THIS.timeUntilFiremen = -1;
        // Firemen raid!
        setView(R.layout.generic);
        ui().text("Screaming fire engines pull up to the " + location + "!").add();
        ui().text("Armored firemen swarm out, pilot lights burning.").add();
        setView(R.layout.generic);
        ui().text("You hear a screeching voice over the sound of fire engine sirens:").add();
        ui().text("Surrender yourselves!").add();
        ui().text("Unacceptable Speech has occurred at this location.").add();
        ui().text("Come quietly and you will not be harmed.").add();
        waitOnOK();
        THIS.siege = true;
        THIS.siegetype = CrimeSquad.FIREMEN;
        THIS.underAttack = true;
        THIS.lightsOff = false;
        THIS.camerasOff = false;
        i.offended.put(CrimeSquad.FIREMEN, false);
      } else if (THIS.timeUntilFiremen == 0) {
        THIS.timeUntilFiremen = -1;
        setView(R.layout.generic);
        ui().text("The Firemen have raided the ").add();
        location.toString();
        ui().text(", an unoccupied safehouse.").add();
        for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
          if (p.location().getNullable() != location) {
            continue;
          }
          if (!p.health().alive()) {
            ui().text(p.toString()).add();
            ui().text("'s corpse has been recovered.").add();
            getch();
            // delete p;
            i.pool.remove(p);
            continue;
          }
          if (p.alignment() != Alignment.LIBERAL) {
            fact(p + " has been rescued.");
            i.pool.remove(p);
            continue;
          }
        }
        location.lcs().loot.clear();
        if (location.lcs().compoundWalls.contains(Compound.PRINTINGPRESS)) {
          ui().text("The printing press is dismantled and burned.").add();
          location.lcs().compoundWalls.remove(Compound.PRINTINGPRESS);
          i.offended.put(CrimeSquad.FIREMEN, false);
        }
        if (location.lcs().frontBusiness != null) {
          ui().text("Materials relating to the business front have been destroyed.").add();
          location.lcs().frontBusiness = null;
        }
        waitOnOK();
      }
    }
  }

  private static final long serialVersionUID = Game.VERSION;

  /** siege - updates upcoming sieges */
  static void siegeCheck() {
    for (final Location l : i.location) {
      if (l.lcs() != null) {
        l.lcs().siege.siegecheck();
      }
    }
  }

  /** siege - updates sieges in progress */
  static void siegeturn() {
    if (i.disbanding()) {
      return;
    }
    // Count people at each location
    final Map<Location, Integer> liberalcount = new HashMap<Location, Integer>();
    for (final Location l : i.location) {
      liberalcount.put(l, 0);
    }
    for (final Creature p : Filter.of(i.pool, Filter.LIBERAL)) {
      if (!p.location().exists()) {
        continue; // Vacationers don't count
      }
      liberalcount.put(p.location().get(), liberalcount.get(p.location().get()) + 1);
    }
    for (final Location l : i.location) {
      if (l.lcs().siege.siege) {
        // resolve sieges with no people
        if (liberalcount.get(l) == 0) {
          setView(R.layout.generic);
          ui().text("Conservatives have raided the " + l.toString() + ", an unoccupied safehouse.")
              .add();
          if (l.lcs().siege.siegetype == CrimeSquad.CCS) {
            if (l.type().isType(Warehouse.class)) {
              l.renting(CrimeSquad.CCS); // CCS Captures
            }
          }
          // warehouse
          getch();
          for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
            if (p.location().getNullable() != l) {
              continue;
            }
            if (!p.health().alive()) {
              ui().text(p.toString() + "'s corpse has been recovered.").add();
              getch();
              // delete p;
              i.pool.remove(p);
              continue;
            }
            if (p.alignment() != Alignment.LIBERAL) {
              ui().text(p.toString() + " has been rescued.").add();
              getch();
              // delete p;
              i.pool.remove(p);
              continue;
            }
          }
          // for (int l2 = 0; l2 < l.lcs.loot.size(); l2++) {
          // delete l.lcs.loot[l2];
          // }
          l.lcs().loot.clear();
          final Iterator<Vehicle> vi = i.vehicle.iterator();
          while (vi.hasNext()) {
            final Vehicle v = vi.next();
            if (v.location() == i.site.current()) {
              vi.remove();
            }
          }
        }
        if (!l.lcs().siege.underAttack) {
          // EAT
          boolean starving = false;
          final int eaters = l.numberEating();
          if (l.lcs().compoundStores == 0) {
            starving = true;
          }
          if (l.lcs().compoundStores >= eaters) {
            l.lcs().compoundStores -= eaters;
          } else {
            l.lcs().compoundStores = 0;
          }
          // ATTACK!
          boolean attack = false;
          // int kidnapped = 0;
          // int criminalcount = 0;
          // int fortified = 0;
          for (final Creature p : Filter.of(i.pool, Filter.livingIn(l))) {
            if (starving) {
              p.health().blood(p.health().blood() - (i.rng.nextInt(5) + 1));
            }
          }
          // if (l.lcs.compound_walls.contains(Compound.BASIC))
          // fortified = 5;
          if (i.rng.chance(5)) {
            attack = true;
          }
          if (attack) {
            ui().text("The cops are coming!").add();
            getch();
            l.lcs().siege.underAttack = true;
          } else {
            boolean no_bad = true;
            // CUT LIGHTS
            if (!l.lcs().siege.lightsOff && !l.lcs().compoundWalls.contains(Compound.GENERATOR)
                && i.rng.chance(10)) {
              no_bad = false;
              ui().text("The police have cut the lights!").add();
              getch();
              l.lcs().siege.lightsOff = true;
            }
            // SNIPER
            if (!l.lcs().compoundWalls.contains(Compound.BASIC) && i.rng.chance(5)) {
              no_bad = false;
              final List<Creature> pol = new ArrayList<Creature>(Filter.of(i.pool,
                  Filter.livingIn(l)));
              if (pol.size() > 0) {
                final Creature targ = i.rng.randFromList(pol);
                if (i.rng.nextInt(50) > targ.juice()) {
                  ui().text("A sniper takes out " + targ.toString() + "!").add();
                  if (targ.alignment() == Alignment.LIBERAL) {
                    liberalcount.put(l, liberalcount.get(l) - 1);
                  }
                  targ.health().die();
                  i.pool.remove(targ);
                } else {
                  ui().text("A sniper nearly hits " + targ.toString() + "!").add();
                }
                getch();
              }
            }
            if (l.lcs().siege.escalationState >= 3 && i.rng.chance(3)) {
              no_bad = false;
              // AIR STRIKE!
              boolean hit = i.rng.chance(3);
              if (!l.lcs().compoundWalls.contains(Compound.GENERATOR)) {
                hit = false;
              }
              ui().text("You hear planes streak overhead!").add();
              getch();
              ui().text("Explosions rock the compound!").add();
              getch();
              if (hit) {
                ui().text("The generator has been destroyed!").add();
                getch();
                ui().text("The lights fade and all is dark. ").add();
                getch();
              }
              if (i.rng.chance(2)) {
                final List<Creature> pol = new ArrayList<Creature>(Filter.of(i.pool,
                    Filter.livingIn(l)));
                if (pol.size() > 0) {
                  final Creature targ = i.rng.randFromList(pol);
                  if (i.rng.nextInt(100) > targ.juice()) {
                    ui().text(targ.toString() + " was killed in the bombing!").add();
                    if (targ.alignment() == Alignment.LIBERAL) {
                      liberalcount.put(l, liberalcount.get(l) - 1);
                    }
                    targ.removeSquadInfo();
                    targ.health().die();
                    i.pool.remove(targ);
                  } else {
                    ui().text(targ.toString() + " narrowly avoided death!").add();
                  }
                  getch();
                }
              } else {
                ui().text("Fortunately, no one is hurt.").add();
                getch();
              }
              if (hit) {
                l.lcs().compoundWalls.remove(Compound.GENERATOR);
                l.lcs().siege.lightsOff = true;
              }
            }
            if (l.lcs().compoundWalls.contains(Compound.TANKTRAPS)
                && l.lcs().siege.escalationState >= 3 && i.rng.chance(15)) {
              no_bad = false;
              // ENGINEERS
              ui().text("Army engineers have removed your tank traps.").add();
              ui().text("The tank moves forward to your compound entrance.").add();
              getch();
              l.lcs().compoundWalls.remove(Compound.TANKTRAPS);
            }
            // NEED GOOD THINGS TO BALANCE THE BAD
            // ELITE REPORTER SNEAKS IN
            if (i.rng.chance(20) && no_bad && liberalcount.get(l) > 0) {
              final String repname = CreatureName.generateName();
              setView(R.layout.generic);
              ui().text(
                  "Elitist "
                      + repname
                      + " from the "
                      + i.rng.choice("news program", "news magazine", "website", "scandal rag",
                          "newspaper") + News.newspaperName() + "got into the compound somehow!")
                  .add();
              Creature best = null;
              int bestvalue = 0, sum;
              for (final Creature p : Filter.of(i.pool, Filter.livingIn(l))) {
                sum = 0;
                sum += p.skill().getAttribute(Attribute.INTELLIGENCE, true);
                sum += p.skill().getAttribute(Attribute.HEART, true);
                sum += p.skill().skill(Skill.PERSUASION);
                sum += p.juice();
                if (sum > bestvalue || best == null) {
                  best = p;
                  bestvalue = sum;
                }
              }
              if (best == null) {
                ui().text("Somehow, no-one was alive to interview.");
                Log.e("LCS", "Null pointer");
                Curses.waitOnOK();
                return;
              }
              ui().text(best.toString() + " decides to give an interview.").add();
              ui().text("The interview is wide-ranging, covering a variety of topics.").add();
              sum = 0;
              sum += best.skill().attributeRoll(Attribute.INTELLIGENCE);
              sum += best.skill().attributeRoll(Attribute.HEART);
              sum += best.skill().skillRoll(Skill.PERSUASION);
              sum += best.skill().skillRoll(Skill.PERSUASION);
              sum += best.skill().skillRoll(Skill.PERSUASION);
              final int segmentpower = sum;
              if (segmentpower < 15) {
                ui().text(
                    repname
                        + " cancelled the interview halfway through "
                        + "and later used the material for a Broadway play called "
                        + i.rng.choice("Flaming", !i.freeSpeech() ? "Dumb" : "Retarded", "Insane",
                            "Crazy", "Loopy", "Idiot", "Empty-Headed", "Nutty", "Half-Baked",
                            "Pot-Smoking", "Stoner")
                        + " "
                        + i.rng.choice("Liberal", "Socialist", "Anarchist", "Communist", "Marxist",
                            "Green", "Elite", "Guerrilla", "Commando", "Soldier") + ".").add();
              } else if (segmentpower < 20) {
                ui().text("But the interview is so boring that " + repname + " falls asleep.")
                    .add();
              } else if (segmentpower < 25) {
                ui().text("But " + best.toString() + " stutters nervously the whole time.").add();
              } else if (segmentpower < 30) {
                ui().text(best.toString() + "'s verbal finesse leaves something to be desired.")
                    .add();
              } else if (segmentpower < 45) {
                ui().text(best.toString() + " represents the LCS well.").add();
              } else if (segmentpower < 60) {
                ui().text(
                    "The discussion was exciting and dynamic. Even the Cable News and AM Radio spend days talking about it.")
                    .add();
              } else {
                ui().text(
                    repname
                        + " later went on to win a Pulitzer for it. Virtually everyone in America was moved by "
                        + best.toString() + "'s words.").add();
              }
              waitOnOK();
              // CHECK PUBLIC OPINION
              i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(20, 1, 100);
              i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion((segmentpower - 25) / 2,
                  segmentpower + 50, 100);
              Issue viewhit;
              for (int v = 0; v < 5; v++) {
                viewhit = i.rng.randFromArray(Issue.coreValues());
                i.issue(viewhit).changeOpinion((segmentpower - 25) / 2, 1, 100);
              }
            }
          }
        }
      }
    }
  }

  private static int calculateKidnapped() {
    Log.w("LCS", "Siege.calculateKidnapped not implemented");
    return 0;
  }
}