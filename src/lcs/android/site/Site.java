package lcs.android.site;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lcs.android.R;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.basemode.iface.ReviewMode;
import lcs.android.combat.Fight;
import lcs.android.combat.Fight.Fighting;
import lcs.android.combat.Kidnap;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Receptive;
import lcs.android.creature.health.Animal;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.Interrogation;
import lcs.android.encounters.CarChase;
import lcs.android.encounters.Encounter;
import lcs.android.encounters.FootChase;
import lcs.android.encounters.SiteEncounter;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.Quality;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Clip;
import lcs.android.items.ClipType;
import lcs.android.items.Item;
import lcs.android.items.Loot;
import lcs.android.items.Vehicle;
import lcs.android.items.Weapon;
import lcs.android.law.Crime;
import lcs.android.monthly.EndGame;
import lcs.android.news.NewsEvent;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.map.Bashable;
import lcs.android.site.map.DisplayMap;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.MapSpecials;
import lcs.android.site.map.MapTile;
import lcs.android.site.map.SecurityLevel;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.SuccessTest;
import lcs.android.site.map.TileSpecial;
import lcs.android.site.map.Unlockable;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.AmRadio;
import lcs.android.site.type.Apartment;
import lcs.android.site.type.ApartmentUpscale;
import lcs.android.site.type.BarAndGrill;
import lcs.android.site.type.BombShelter;
import lcs.android.site.type.Bunker;
import lcs.android.site.type.CableNews;
import lcs.android.site.type.CrackHouse;
import lcs.android.site.type.PoliceStation;
import lcs.android.site.type.Shelter;
import lcs.android.site.type.Tenement;
import lcs.android.site.type.Warehouse;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

final public @NonNullByDefault class Site {
  /** Any attempts to call this site are in error. */
  public Site() {
    current = null;
  }

  /** correctly set up by travelToSite */
  private Site(final Location loc) {
    type = loc.type();
    current = loc;
    alarm = false;
    alarmTimer(-1);
    postAlarmTimer = 0;
    onFire = false;
    siteAlienate = Alienation.NONE;
    crime = 0;
  }

  /** Whether you've set off the alarms at the current site. Determines the creatures created in an
   * encounter, whether people will speak to you, and what the status of some of the site specials
   * are. */
  private boolean alarm;

  /** How many turns will pass until you set off the {@link #alarm}. -1 until you start a countdown,
   * then decreases by 1 each turn. */
  private int alarmTimer;

  /** Whether you've performed any illegal actions in front of people. */
  private Alienation siteAlienate;

  /** How much crime you've committed on site. Determines whether you get chased out of the door,
   * afterwards. */
  private int crime;

  /** Where the LCS are currently active. Doesn't need to be saved, because we can only serialize in
   * basemode. */
  private final Location current;

  /** The map of the current level. Used by {@link SiteMap} and {@link Site}, where it also does some
   * shenanigans to minimize memory allocations. */
  private MapTile[][][] siteLevelmap;

  /** Whether you've set the site on fire. Determines whether the firefighters, start to appear. (The
   * firemen don't put out fires). */
  private boolean onFire;

  /** How long it is since the Conservatives raised the alarm at the site we're investigating.
   * Influences security response, car chases afterwards, etc. */
  private int postAlarmTimer;

  /** what kind of site you're at at the moment. */
  private AbstractSiteType type;

  /** Upwards arrow. */
  private final char UP = '\u2191';

  /** x location in the {@link #siteLevelmap} */
  public transient int locx;

  /** y location in the {@link #siteLevelmap} */
  public transient int locy;

  /** z location in the {@link #siteLevelmap} */
  public transient int locz;

  public boolean alarm() {
    checkImproperlyCalled();
    return alarm;
  }

  public Site alarm(final boolean alarm) {
    checkImproperlyCalled();
    this.alarm = alarm;
    return this;
  }

  public int alarmTimer() {
    checkImproperlyCalled();
    return alarmTimer;
  }

  public Site alarmTimer(final int alarmTimer) {
    checkImproperlyCalled();
    this.alarmTimer = alarmTimer;
    return this;
  }

  public Alienation alienate() {
    checkImproperlyCalled();
    return siteAlienate;
  }

  public Site alienate(final Alienation siteAlienate) {
    checkImproperlyCalled();
    this.siteAlienate = siteAlienate;
    return this;
  }

  /** Checks whether we've alienated the masses at this location. May change the view to R.id.generic
   * if we need to announce something
   * @param mistake true if we intended to alienate the masses
   * @return whether we have now alienated everyone. */
  public boolean alienationCheck(final boolean mistake) {
    checkImproperlyCalled();
    if (current.lcs().siege.siege)
      return false;
    boolean alienate = false;
    boolean alienatebig = false;
    // char sneak = 0;
    final Alienation oldsitealienate = siteAlienate;
    final List<Creature> noticer = new ArrayList<Creature>();
    for (final Creature e : i.currentEncounter().creatures()) {
      /* Prisoners should never be alienated by your crimes, as they're happy to have you attacking
       * their place of holding if(e.type==Type.PRISONER)continue; ...but Prisoners are now spawned
       * with a variety of creature types, so we'll go by name instead */
      if (e.isPrisoner()) {
        continue;
      }
      if (e.health().alive()
          && (e.alignment() == Alignment.MODERATE || e.alignment() == Alignment.LIBERAL && mistake)) {
        noticer.add(e);
      }
    }
    if (!noticer.isEmpty()) {
      Creature n;
      do {
        n = i.rng.randFromList(noticer);
        noticer.remove(n);
        if (n.alignment() == Alignment.LIBERAL) {
          alienatebig = true;
        } else {
          alienate = true;
        }
      } while (!noticer.isEmpty());
      if (alienatebig) {
        siteAlienate = Alienation.ALL;
      }
      if (alienate && siteAlienate != Alienation.ALL) {
        siteAlienate = Alienation.MODERATES;
      }
      if (oldsitealienate != siteAlienate) {
        if (siteAlienate == Alienation.MODERATES) {
          fact("We've alienated the masses here!");
        } else {
          fact("We've alienated absolutely everyone here!");
        }
        alarm = true;
        for (final Creature e : i.currentEncounter().creatures()) {
          if (e.alignment() != Alignment.CONSERVATIVE
              && (e.alignment() == Alignment.MODERATE || alienatebig)) {
            e.conservatise();
          }
        }
        i.currentEncounter().printEncounter();
        getch();
      }
    }
    return alienate;
  }

  public int crime() {
    checkImproperlyCalled();
    return crime;
  }

  public Site crime(final int crime) {
    checkImproperlyCalled();
    this.crime = crime;
    return this;
  }

  public Location current() {
    return current;
  }

  /** @return */
  public MapTile currentTile() {
    return siteLevelmap[locx][locy][locz];
  }

  public void noticeCheck(final CheckDifficulty difficulty, final Creature... exclude) {
    if (alarm)
      return;
    final Maybe<Creature> topTest = Filter.best(i.activeSquad, Filter.skill(Skill.STEALTH));
    if (topTest.missing())
      return;
    final Creature top = topTest.get();
    for (final Creature e : i.currentEncounter().creatures()) {
      // Prisoners shouldn't shout for help.
      if (e.isPrisoner()) {
        continue;
      }
      if (Arrays.asList(exclude).contains(e)) {
        continue;
      } else if (top.skill().skillCheck(Skill.STEALTH, difficulty)) {
        continue;
      } else {
        ui().text(e.toString()).add();
        ui().text(" observes your Liberal activity").add();
        if (e.alignment() == Alignment.CONSERVATIVE) {
          ui().text("and lets forth a piercing Conservative alarm cry!").add();
        } else {
          ui().text("and shouts for help!").add();
        }
        alarm = true;
        getch();
        break;
      }
    }
  }

  public void noticeCheck(final Creature... creature) {
    noticeCheck(CheckDifficulty.EASY, creature);
  }

  public boolean onFire() {
    checkImproperlyCalled();
    return onFire;
  }

  public int postAlarmTimer() {
    checkImproperlyCalled();
    return postAlarmTimer;
  }

  public Site postAlarmTimer(final int postAlarmTimer) {
    checkImproperlyCalled();
    this.postAlarmTimer = postAlarmTimer;
    return this;
  }

  public MapTile[][][] siteLevelmap() {
    checkImproperlyCalled();
    return siteLevelmap;
  }

  public Site siteLevelmap(final MapTile[][][] siteLevelmap) {
    checkImproperlyCalled();
    this.siteLevelmap = siteLevelmap;
    return this;
  }

  public void specialGraffiti() {
    // clearmessagearea();
    Curses.fact("The squad sprays Liberal Graffiti!");
    i.siteStory.claimed(true);
    int time = 20 + i.rng.nextInt(10);
    if (time < 1) {
      time = 1;
    }
    if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
      i.site.alarmTimer(time);
    }
    i.site.alienationCheck(false);
    i.site.noticeCheck(CheckDifficulty.HARD);
    i.site.siteLevelmap()[locx][locy][locz].flag.add(TileSpecial.GRAFFITI);
    i.site.siteLevelmap()[locx][locy][locz].flag.remove(TileSpecial.GRAFFITI_CCS);
    i.site.siteLevelmap()[locx][locy][locz].flag.remove(TileSpecial.GRAFFITI_OTHER);
    // if (i.cursite.high_security > 0) {
    // Erase any previous semi-permanent graffiti here
    for (final Iterator<MapChangeRecord> scsi = i.site.current().changes().iterator(); scsi
        .hasNext();) {
      final MapChangeRecord j = scsi.next();
      if (j.x == locx
          && j.y == locy
          && j.z == locz
          && (j.flag == TileSpecial.GRAFFITI || j.flag == TileSpecial.GRAFFITI_CCS || j.flag == TileSpecial.GRAFFITI_OTHER)) {
        scsi.remove();
        break;
      }
    }
    // Add new semi-permanent graffiti
    final MapChangeRecord change = new MapChangeRecord(locx, locy, locz, TileSpecial.GRAFFITI);
    i.site.current().changes().add(change);
    // }
    i.site.crime(i.site.crime() + 1);
    for (final Creature p : i.activeSquad) {
      p.addJuice(1, 50);
    }
    i.activeSquad.criminalizeParty(Crime.VANDALISM);
    i.siteStory.addNews(NewsEvent.TAGGING);
    return;
  }

  /** The maptile for a given location.
   * @param locx
   * @param locy
   * @param locz
   * @return The maptile. */
  public MapTile tileAtLocation(final int locx, final int locy, final int locz) {
    return siteLevelmap[locx][locy][locz];
  }

  public AbstractSiteType type() {
    checkImproperlyCalled();
    return type;
  }

  public Site type(final AbstractSiteType type) {
    checkImproperlyCalled();
    this.type = type;
    return this;
  }

  Site onFire(final boolean onFire) {
    checkImproperlyCalled();
    this.onFire = onFire;
    return this;
  }

  private void checkImproperlyCalled() {
    if (current == null) {
      Log.e("LCS", "Altered site property, but we are nowhere", new IllegalArgumentException(
          "improperly called"));
    }
  }

  private void defeatCCS() {
    // DEAL WITH PRISONERS AND STOP BLEEDING
    for (final Creature p : i.activeSquad) {
      if (p.prisoner().exists()) {
        if (p.prisoner().get().squad().exists()) {
          p.prisoner().get().squad(null).location(p.base().getNullable())
              .base(p.base().getNullable());
        } else {
          Interrogation.create(p.prisoner().get(), i.activeSquad.member(0));
        }
        p.prisoner(null);
      }
    }
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      p.health().stopBleeding();
    }
    ui().text("The CCS has been broken!").add();
    getch();
    current.renting(CrimeSquad.LCS);
    current.closed(0);
    current.lcs().heat = 100;
    // CCS Safehouse killed?
    if (current.type().isCcsSafeHouse()) {
      i.score.ccsKills++;
      if (i.score.ccsKills < 3) {
        i.endgameState = EndGame.values()[i.endgameState.ordinal() - 1];
      } else {
        i.endgameState = EndGame.CCS_DEFEATED;
      }
    }
    current.lcs().siege.conquerTextCcs();
    i.mode(GameMode.BASE);
  }

  private int disguiseCheckU(final int oldtimer) {
    int timer = oldtimer;
    Quality weapon = Quality.GOOD;
    boolean forcecheck = false;
    final Map<Creature, Boolean> weaponar = new HashMap<Creature, Boolean>();
    // boolean spotted = false;
    Creature blew_it = null;
    // Only start to penalize the player's disguise/stealth checks after the
    // first turn.
    timer--;
    for (final Creature p : i.activeSquad) {
      if (p.isNaked() && p.type().animal() != Animal.ANIMAL) {
        forcecheck = true;
      }
      final Quality thisweapon = p.weaponCheck();
      if (thisweapon.level() < weapon.level()) {
        weapon = thisweapon;
      }
      if (thisweapon == Quality.NONE) {
        weaponar.put(p, true);
      } else {
        weaponar.put(p, false);
      }
    }
    // Nothing suspicious going on here
    if (alarmTimer() == -1 && weapon.level() > Quality.POOR.level() && !forcecheck) {
      if (!current.type().disguisesite()
          && !siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.RESTRICTED))
        return timer;
    }
    final List<Creature> noticer = new ArrayList<Creature>();
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.isPrisoner()) {
        continue;
      }
      if (e.health().alive() && e.enemy()) {
        noticer.add(e);
      }
    }
    if (noticer.isEmpty())
      return timer;
    Creature noticed = null;
    Collections.shuffle(noticer);
    noticers: for (final Creature n : noticer) {
      int difficulty = n.observationSkill().value();
      // Increase difficulty if Conservatives suspicious...
      if (alarmTimer() == 1) {
        difficulty += 3;
      }
      // Make the attempt!
      for (final Creature p : i.activeSquad) {
        // Try to sneak.
        int result = p.skill().skillRoll(Skill.STEALTH);
        result -= timer;
        // Sneaking with a party is hard
        if (result < difficulty + i.activeSquad.size() - 1) {
          // Guns of death and doom are not very casual.
          if (p.weaponCheck() == Quality.POOR) {
            noticed = n;
            break noticers;
          }
          result = p.skill().skillRoll(Skill.DISGUISE);
          result -= timer;
          if (result < difficulty) {
            // That was not very casual, dude.
            if (result < 0) {
              blew_it = p;
            }
            noticed = n;
            break noticers;
          }
        }
      }
    } // noticers
    setView(R.layout.generic);
    // Give feedback on the Liberal Performance
    if (noticed == null) {
      for (final Creature p : i.activeSquad) {
        p.skill().train(Skill.STEALTH, 10);
      }
      if (timer == 0) {
        if (i.activeSquad.size() > 1) {
          ui().text("The squad fades into the shadows.").color(Color.CYAN).add();
        } else {
          ui().text(i.activeSquad.member(0).toString() + " fades into the shadows.")
              .color(Color.CYAN).add();
        }
      }
    } else {
      if (blew_it == null) {
        for (final Creature p : i.activeSquad) {
          if (p.hasDisguise() != Quality.NONE) {
            p.skill().train(Skill.DISGUISE, 10);
          }
        }
      }
      if (blew_it != null && i.rng.chance(2)) {
        ui().text(
            blew_it.toString()
                + i.rng.choice(" coughs.", " accidentally mumbles the slogan.", " paces uneasily.",
                    " stares at the Conservatives.", " laughs nervously.")).color(Color.YELLOW)
            .add();
      }
    }
    if (noticed == null) {
      waitOnOK();
      return timer;
    }
    if (alarmTimer() != 0 && weapon != Quality.GOOD
        && noticed.type() != CreatureType.valueOf("GUARDDOG")) {
      if (type.isResidential()
          && siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.RESTRICTED)) {
        alarm = true;
        ui().text(noticed.toString() + " shouts in alarm at the squad's Liberal Trespassing!")
            .color(Color.RED).add();
      } else {
        ui().text(noticed.toString() + " looks at the Squad suspiciously.").color(Color.RED).add();
        int time = 20 + i.rng.nextInt(10)
            - noticed.skill().getAttribute(Attribute.INTELLIGENCE, true)
            - noticed.skill().getAttribute(Attribute.WISDOM, true);
        if (time < 1) {
          time = 1;
        }
        if (alarmTimer() > time || alarmTimer() == -1) {
          alarmTimer(time);
        } else {
          alarmTimer(alarmTimer() - 5);
          if (alarmTimer() < 0) {
            alarmTimer(0);
          }
        }
      }
    } else {
      if (weapon != Quality.GOOD && noticed.type() != CreatureType.valueOf("GUARDDOG")) {
        if (noticed.alignment() == Alignment.CONSERVATIVE) {
          ui().text(
              noticed.toString()
                  + " sees the Squad's Liberal Weapons and lets forth a piercing Conservative alarm cry!")
              .color(Color.RED).add();
        } else {
          ui().text(noticed.toString() + " sees the Squad's Liberal Weapons and shouts for help!")
              .color(Color.RED).add();
        }
      } else if (noticed.alignment() == Alignment.CONSERVATIVE) {
        if (noticed.type() == CreatureType.valueOf("GUARDDOG")) {
          ui().text(
              noticed.toString()
                  + " looks at the Squad with Intolerance and launches into angry Conservative barking!")
              .color(Color.RED).add();
        } else {
          ui().text(
              noticed.toString()
                  + " looks at the Squad with Intolerance and lets forth a piercing Conservative alarm cry!")
              .color(Color.RED).add();
        }
      } else {
        ui().text(noticed.toString() + " looks at the Squad with Intolerance and shouts for help!")
            .color(Color.RED).add();
      }
      alarm = true;
    }
    waitOnOK();
    return timer;
  }

  private void displayMap() {
    final StringBuilder sb = new StringBuilder();
    for (int y = 0; y < SiteMap.MAPY; y++) {
      for (int x = 0; x < SiteMap.MAPX; x++) {
        if (current.isInterrogated() || siteLevelmap[x][y][locz].flag.contains(TileSpecial.KNOWN)) {
          if (x == locx && y == locy) {
            sb.append(SMILEY);
          } else if (siteLevelmap[x][y][locz].flag.contains(TileSpecial.BLOCK)) {
            sb.append('#');
          } else if (siteLevelmap[x][y][locz].flag.contains(TileSpecial.DOOR)) {
            sb.append('+');
          } else if (siteLevelmap[x][y][locz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)
              && current.lcs().compoundWalls.contains(Compound.CAMERAS)
              && !current.lcs().siege.camerasOff) {
            sb.append('Y');
          } else if (siteLevelmap[x][y][locz].siegeFlag.contains(SiegeUnitType.UNIT)
              && current.lcs().compoundWalls.contains(Compound.CAMERAS)
              && !current.lcs().siege.camerasOff) {
            sb.append(SMILEY);
          } else if (siteLevelmap[x][y][locz].siegeFlag.contains(SiegeUnitType.UNIT_DAMAGED)
              && current.lcs().compoundWalls.contains(Compound.CAMERAS)
              && !current.lcs().siege.camerasOff) {
            sb.append(SMILEY);
          } else if (siteLevelmap[x][y][locz].special == SpecialBlocks.STAIRS_UP) {
            sb.append(UP);
          } else if (siteLevelmap[x][y][locz].special == SpecialBlocks.STAIRS_DOWN) {
            sb.append(DOWN);
          } else if (siteLevelmap[x][y][locz].special != null) {
            sb.append('!');
          } else if (siteLevelmap[x][y][locz].siegeFlag.contains(SiegeUnitType.TRAP)) {
            sb.append('!');
          } else if (siteLevelmap[x][y][locz].flag.contains(TileSpecial.LOOT)) {
            sb.append('$');
          } else if (siteLevelmap[x][y][locz].flag.contains(TileSpecial.RESTRICTED)) {
            sb.append('.');
          } else {
            sb.append(' ');
          }
        } else {
          sb.append('?');
        }
      }
      sb.append('\n');
    }
    setView(R.layout.sitemap);
    setText(R.id.sitemap, sb.toString());
    getch();
  }

  private void drawControls(final int partysize, final int freeable, final int enemy,
      final int talkers, final int libnum) {
    if (partysize > 1) {
      ui(R.id.smcontrols).button('o').text("Change the squad's Liberal Order").add();
    }
    if (i.groundLoot().size() > 0 || siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.LOOT)) {
      ui(R.id.smcontrols).button('g').text("Get Loot").add();
    }
    ui(R.id.smcontrols).button('m').text("Map").add();
    ui(R.id.smcontrols).button('s').text("Wait").add();
    if (enemy == 0 || !alarm) {
      for (final Creature p : i.activeSquad) {
        if (p.weapon().canReload()) {
          ui(R.id.smcontrols).button('l').text("Reload").add();
          break;
        }
      }
    }
    if (enemy != 0) {
      ui(R.id.smcontrols).button('k').text("Kidnap").add();
    }
    if (talkers != 0) {
      ui(R.id.smcontrols).button('t').text("Talk").add();
    }
    boolean graffiti = false;
    if (!(siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.GRAFFITI) || siteLevelmap[locx][locy][locz].flag
        .contains(TileSpecial.BLOODY2))) {
      if (siteLevelmap[locx + 1][locy][locz].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[locx - 1][locy][locz].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[locx][locy + 1][locz].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[locx][locy - 1][locz].flag.contains(TileSpecial.BLOCK)) {
        for (final Creature p : i.activeSquad) {
          if (p.weapon().weapon().canGraffiti()) {
            graffiti = true;
            break;
          }
        }
      }
    }
    if (graffiti) {
      ui(R.id.smcontrols).button('u').text("Graffiti").add();
    } else if (siteLevelmap[locx][locy][locz].special != null
        && siteLevelmap[locx][locy][locz].special.usable()) {
      ui(R.id.smcontrols).button('u').text("Use").add();
    }
    if (enemy != 0 && alarm) {
      boolean evade = false;
      setMovementEnabled(false);
      for (final Creature e : i.currentEncounter().creatures()) {
        if (e.health().alive() && e.receptive() == Receptive.ANGERED) {
          /* You can't sneak past this person; they already know you're there */
          evade = true;
          break;
        }
      }
      if (evade) {
        ui(R.id.smcontrols).button('v').text("Run").add();
      } else {
        ui(R.id.smcontrols).button('v').text("Sneak").add();
      }
    } else {
      setMovementEnabled(true);
    }
    ui(R.id.smcontrols).button('e').text("Equip").add();
    if (enemy != 0) {
      ui(R.id.smcontrols).button('f').text("Fight!").add();
    }
    if (!current.lcs().siege.siege) {
      if (freeable != 0 && (enemy == 0 || alarm)) {
        ui(R.id.smcontrols).button('r').text("Release oppressed").add();
      }
    } else if (libnum > 6) {
      ui(R.id.smcontrols).button('r').text("Reorganize").add();
    }
  }

  private void exitSite() { // TODO WTF?
    // CHASE SEQUENCE OR FOOT CHASE
    int level = crime;
    if (!alarm) {
      level = 0;
    }
    if (i.rng.likely(3) && level < 4) {
      level = 0;
    }
    if (i.rng.likely(2) && level < 8) {
      level = 0;
    }
    if (postAlarmTimer < 10 + i.rng.nextInt(20)) {
      level = 0;
    } else if (postAlarmTimer < 20 + i.rng.nextInt(20) && i.rng.likely(3)) {
      level = 0;
    } else if (postAlarmTimer < 40 + i.rng.nextInt(20) && i.rng.chance(3)) {
      level = 0;
    }
    if (current.lcs().siege.siege) {
      level = 1000;
    }
    // MAKE SURE YOU ARE GUILTY OF SOMETHING
    boolean guilty = false;
    for (final Creature p : i.activeSquad) {
      if (p.crime().isCriminal()) {
        guilty = true;
      }
    }
    if (!guilty) {
      level = 0;
    }
    final FootChase fc = new FootChase(Encounter.createEncounter(type, level)); // TODO check all
                                                                                // this
    boolean havecar = false;
    for (final Creature p : i.activeSquad) {
      if (p.car().exists()) {
        havecar = true;
        for (final Creature creature : i.activeSquad) {
          if (creature != null && creature.car().exists()) {
            creature.car(p.car().get());
          }
        }
        break;
      }
    }
    boolean gotout;
    if (havecar) {
      gotout = new CarChase(current).encounter();
    } else {
      gotout = fc.encounter();
    }
    // If you survived
    if (gotout) {
      // Check for hauled prisoners/corpses
      for (final Creature p : i.activeSquad) {
        if (p == null) {
          continue;
        }
        if (p.prisoner().exists()) {
          // If this is an LCS member or corpse being
          // hauled (marked as in the squad)
          if (p.prisoner().get().squad().exists()) {
            // Take them out of the squad
            // Set base and current location to
            // squad's safehouse
            p.prisoner().get().squad(null).location(p.base().getNullable())
                .base(p.base().getNullable());
          } else {
            Interrogation.create(p.prisoner().get(), i.activeSquad.member(0));
          }
          // delete
          // p.prisoner;
          p.prisoner(null);
        }
      }
      // Clear all bleeding and prison escape flags
      for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
        p.removeFlag(CreatureFlag.JUST_ESCAPED);
        p.health().stopBleeding();
      }
      // END SITE MODE
      if (current.lcs().siege.siege) {
        // Special handling for escaping siege
        current.lcs().siege.escapeSiege(false);
      } else {
        resolvesite();
      }
    }
    // You didn't survive -- handle squad death (unless that
    // ended the game)
    else {
      EndGame.endcheck(null);
      if (current.lcs().siege.siege) {
        // Report on squad killed during siege
        if (current.lcs().siege.underAttack) {
          i.siteStory.type(StoryType.SQUAD_KILLED_SIEGEATTACK);
        } else {
          i.siteStory.type(StoryType.SQUAD_KILLED_SIEGEESCAPE);
        }
        current.lcs().siege.siege = false;
      } else {
        // Or report on your failed raid
        i.siteStory.type(StoryType.SQUAD_KILLED_SITE);
        // Would juice the party here, but you're all
        // dead, so...
        resolvesite();
      }
    }
    i.mode(GameMode.BASE);
    return;
  }

  private boolean hostageCheck() {
    boolean havehostage = false;
    // Check your whole squad
    for (final Creature p : i.activeSquad) {
      // If they're unarmed and dragging someone
      if (p.prisoner().exists() && !p.weapon().isArmed()) {
        // And that someone is not an LCS member
        if (!p.prisoner().get().squad().exists()) {
          // They scream for help -- flag them kidnapped,
          // cause alarm
          p.prisoner().get().addFlag(CreatureFlag.KIDNAPPED);
          if (p.type() == CreatureType.valueOf("RADIOPERSONALITY")) {
            i.offended.put(CrimeSquad.AMRADIO, true);
          }
          if (p.type() == CreatureType.valueOf("NEWSANCHOR")) {
            i.offended.put(CrimeSquad.CABLENEWS, true);
          }
          havehostage = true;
        }
      }
    }
    if (havehostage) {
      alienationCheck(true);
      crime += 5;
      i.activeSquad.criminalizeParty(Crime.KIDNAPPING);
    }
    return havehostage;
  }

  private void landlordEncounter(final boolean moved, final int olocx, final int olocy,
      final int olocz, final boolean newenc) {
    if (moved && newenc) {
      // PUT BACK SPECIALS
      for (final Creature e : i.currentEncounter().creatures()) {
        if (e.receptive() != Receptive.LISTENING && e.type() == CreatureType.valueOf("LANDLORD")) {
          siteLevelmap[olocx][olocy][olocz].special = SpecialBlocks.APARTMENT_LANDLORD;
        }
      }
    }
  }

  private void lootAndBleed(final int olocx, final int olocy, final int olocz) {
    // NUKE GROUND LOOT
    i.groundLoot().clear();
    // MOVE BLOOD
    if (siteLevelmap[olocx][olocy][olocz].flag.contains(TileSpecial.BLOODY2)) {
      siteLevelmap[locx][locy][locz].flag.add(TileSpecial.BLOODY);
    }
  }

  private boolean makeEncounterU(@Nullable final SpecialBlocks makespecial) {
    final Encounter e = siteLevelmap[locx][locy][locz].encounter = new SiteEncounter();
    i.currentEncounter(e);
    if (makespecial != null) {
      switch (makespecial) {
      case CAFE_COMPUTER:
        if (alarm || siteAlienate != Alienation.NONE) {
          fact("The computer has been unplugged.");
          siteLevelmap[locx][locy][locz].special = null;
        } else {
          fact("The computer is occupied.");
          siteLevelmap[locx][locy][locz].special = null;
          type.prepareEncounter(e, false);
        }
        break;
      case RESTAURANT_TABLE:
        if (alarm || siteAlienate != Alienation.NONE) {
          fact("Some people are hiding under the table.");
          siteLevelmap[locx][locy][locz].special = null;
          type.prepareEncounter(e, false);
        } else {
          fact("The table is occupied.");
          siteLevelmap[locx][locy][locz].special = null;
          type.prepareEncounter(e, false);
        }
        break;
      case PARK_BENCH:
        if (alarm || siteAlienate != Alienation.NONE) {
          fact("The bench is empty.");
          siteLevelmap[locx][locy][locz].special = null;
        } else {
          fact("The there are people sitting here.");
          siteLevelmap[locx][locy][locz].special = null;
          type.prepareEncounter(e, false);
        }
        break;
      case CLUB_BOUNCER:
        MapSpecials.specialBouncerAssessSquad();
        break;
      case CLUB_BOUNCER_SECONDVISIT:
        MapSpecials.specialBouncerGreetSquad();
        break;
      case HOUSE_CEO:
        if (alarm || siteAlienate != Alienation.NONE || current.lcs().siege.siege) {
          if (i.freeSpeech()) {
            fact("Damn! The CEO must have fled to a panic room.");
          } else {
            fact("[Rats!] The CEO must have fled to a panic room.");
          }
          siteLevelmap[locx][locy][locz].special = null;
        } else {
          fact("The CEO is in his study.");
          siteLevelmap[locx][locy][locz].special = null;
          e.creatures().clear();
          e.creatures().add(CreatureType.withType("CORPORATE_CEO"));
          break;
        }
        break;
      case APARTMENT_LANDLORD:
        if (alarm || siteAlienate != Alienation.NONE || current.lcs().siege.siege) {
          fact("The landlord is out of the office.");
          siteLevelmap[locx][locy][locz].special = null;
        } else {
          fact("The landlord is in.");
          siteLevelmap[locx][locy][locz].special = null;
          e.creatures().clear();
          e.creatures().add(CreatureType.withType(CreatureType.valueOf("LANDLORD")));
          e.creatures().get(0).receptive(Receptive.LISTENING);
        }
        break;
      default:
        break;
      }
    } else {
      if (current.type().isResidential()) {
        if (i.rng.likely(3))
          return false; // Rarely encounter someone in
      }
      // apartments. (Was i.rng.getInt(5), seemed too
      // easy to burgle people all day.)
      fact("There is someone up ahead.");
      type.prepareEncounter(e, current.highSecurity() > 0);
      return false;
    }
    return true;
  }

  private void modeSite() {
    if (i.activeSquad == null)
      return;
    Fight.reloadparty();
    boolean bail_on_base = true;
    if (current == i.activeSquad.base().getNullable()) {
      bail_on_base = false;
    }
    SiteMap.knowmap(locx, locy, locz);
    boolean hostcheck = true;
    int encounter_timer = 0;
    boolean secure = false;
    boolean changes = true;
    do {
      // Log.i(Game.LCS, "SiteMode.mode_site prepare frame.");
      if (lastView() != R.layout.sitemode) {
        changes = true;
      }
      if (changes) {
        setView(R.layout.sitemode);
      }
      final int partysize = i.activeSquad.size();
      int partyalive = Filter.count(i.activeSquad, Filter.LIVING);
      int freeable = 0;
      int enemy = 0;
      int talkers = 0;
      if (!i.currentEncounter().isEmpty()) {
        for (final Creature e : i.currentEncounter().creatures()) {
          // encsize++;
          if (e.enemy()) {
            enemy++;
          }
          if (e.type() == CreatureType.valueOf("WORKER_SERVANT")
              || e.type() == CreatureType.valueOf("WORKER_FACTORY_CHILD")
              || e.type() == CreatureType.valueOf("WORKER_SWEATSHOP") || e.isPrisoner()
              && e.alignment() == Alignment.LIBERAL) {
            freeable++;
          } else if ((e.receptive() != Receptive.HEARD || alarm)
              && !(e.alignment() == Alignment.LIBERAL && alarm && enemy != 0)) {
            talkers++;
          }
        }
      }
      // If in combat, do a second check
      if (talkers != 0 && alarm && enemy != 0) {
        talkers = 0;
        for (final Creature e : i.currentEncounter().creatures()) {
          if (!e.enemy()) {
            talkers++;
          }
        }
      }
      int libnum = 0;
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(current))) {
        if (!p.hasFlag(CreatureFlag.SLEEPER)) {
          libnum++;
        }
      }
      // Let the squad stop stressing out over the encounter if there
      // are no enemies this round
      if (enemy == 0) {
        encounter_timer = 0;
      }
      if (current.lcs().siege.siege) {
        setText(R.id.smlocation, current.toString() + ", Level " + (locz + 1)
            + ": Escape or Engage");
      } else {
        setHeader();
      }
      // PRINT PARTY
      if (changes) {
        i.activeSquad.printParty();
      }
      // PRINT SITE INSTRUCTIONS
      if (partyalive > 0) {
        drawControls(partysize, freeable, enemy, talkers, libnum);
      } else {
        ui(R.id.smcontrols).button('c').text("Reflect on your Conservative ineptitude").add();
      }
      // Log.d(Game.LCS, "SiteMode.mode_site did buttons");
      // PRINT SITE MAP
      DisplayMap.printSiteMap(locx, locy, locz);
      // Log.d(Game.LCS, "SiteMode.mode_site printed site map");
      // CHECK IF YOU HAVE A SQUIRMING AMATEUR HOSTAGE
      // hostcheck SHOULD ONLY BE 1 WHEN A NEWENC IS CREATED
      if (hostcheck) {
        hostcheck = hostageCheck();
      }
      // Log.d(Game.LCS, "SiteMode.mode_site awaiting input");
      int c = 0; // = getch();
      if (siteLevelmap[locx][locy][locz].special == SpecialBlocks.CLUB_BOUNCER) {
        // Log.i(Game.LCS, "Bouncer");
        if (current.renting() == CrimeSquad.LCS) {
          siteLevelmap[locx][locy][locz].special = null;
        } else {
          c = 's';
        }
      } else {
        c = getch();
      }
      changes = true; // redraw unless it's just movement
      clearChildren(R.id.smcontrols);
      // Log.d(Game.LCS, "SiteMode.mode_site mulling it over");
      if (partyalive == 0 && c == 'c') {
        reflectOnIneptitude();
        return;
      }
      final int olocx = locx;
      final int olocy = locy;
      final int olocz = locz;
      boolean override = false;
      boolean moved = false;
      if (c == 'v' && enemy != 0 && alarm) {
        // //clearmessagearea();
        //
        // move(16, 1);
        // ui().text("Which way?  (W,A,D, and X to move, ENTER to abort)").add();
        // refresh();
        setMovementEnabled(true);
        do {
          final int c2 = getch();
          if (c2 == 'w' || c2 == 'a' || c2 == 'd' || c2 == 'x') {
            c = c2;
            override = true;
            break;
          }
          if (c2 == 10) {
            break;
          }
        } while (true);
      }
      // nb. not else if.
      if (c == 'w' && locy > 0 && (enemy == 0 || !alarm || override)) {
        if (!siteLevelmap[locx][locy - 1][locz].flag.contains(TileSpecial.BLOCK)) {
          moved = true;
          locy--;
        }
      } else if (c == 'a' && locx > 0 && (enemy == 0 || !alarm || override)) {
        if (!siteLevelmap[locx - 1][locy][locz].flag.contains(TileSpecial.BLOCK)) {
          moved = true;
          locx--;
        }
      } else if (c == 'd' && locx < SiteMap.MAPX - 1 && (enemy == 0 || !alarm || override)) {
        if (!siteLevelmap[locx + 1][locy][locz].flag.contains(TileSpecial.BLOCK)) {
          moved = true;
          locx++;
        }
      } else if (c == 'x' && locy < SiteMap.MAPY - 1 && (enemy == 0 || !alarm || override)) {
        if (!siteLevelmap[locx][locy + 1][locz].flag.contains(TileSpecial.BLOCK)) {
          moved = true;
          locy++;
        }
      } else if (c == 'k' && enemy != 0) {
        Kidnap.kidnapAttempt();
      } else if (c == 'u') {
        useMapSpecialOrGraffiti();
      } else if (c == 't' && talkers != 0) {
        encounter_timer = talk(encounter_timer, enemy);
      } else if (c == 'l' && (enemy == 0 || !alarm)) {
        setView(R.layout.generic);
        Fight.reloadparty(true);
        // SquadSt.printparty();
        Advance.creatureAdvance();
        encounter_timer++;
        // waitOnOK();
      } else if (c == 'o' && partysize > 1) {
        Squad.orderParty();
      } else if (c == '0') {
        i.activeSquad.highlightedMember(-1);
      } else if (c == 'm') {
        displayMap();
      } else if (c == 'f' && enemy != 0) {
        if (i.currentEncounter().creatures().get(0).health().blood() > 60
            && i.currentEncounter().creatures().get(0).type() == CreatureType.valueOf("COP")) {
          boolean subdue = true;
          for (final Creature d : i.activeSquad) {
            if (d.health().alive() && d.health().blood() > 40) {
              subdue = false;
              break;
            }
          }
          if (subdue) {
            fightSubduedU();
            return;
          }
        }
        setView(R.layout.generic);
        Fight.fight(Fighting.BOTH);
        Advance.creatureAdvance();
        // waitOnOK();
        encounter_timer++;
      } else if (c == 'r' && current.lcs().siege.siege && libnum > 6) {
        ReviewMode.assemblesquad(i.activeSquad);
        current.autoPromote();
      } else if (c == 'r' && freeable > 0 && (enemy == 0 || !alarm) && !current.lcs().siege.siege) {
        releaseOppressed(partysize, enemy);
      } else if (c >= '1' && c <= '6') {
        if (i.activeSquad.member(c - '1') != null) {
          i.activeSquad.highlightedMember(c - '1');
        }
      } else if (c == '7') {
        Squad.fullStatus(i.activeSquad.highlightedMember());
      } else if (c == 'e') {
        AbstractItem.equip(i.activeSquad.loot(), null);
        if (enemy > 0 && alarm) {
          setView(R.layout.generic);
          Fight.fight(Fighting.THEM);
        } else if (enemy > 0) {
          encounter_timer = disguiseCheckU(encounter_timer);
        }
        // Advance.creatureadvance();
        setView(R.layout.sitemode);
        c = 's'; // now, we wait.
      } else if (c == 'g'
          && (i.groundLoot().size() > 0 || siteLevelmap[locx][locy][locz].flag
              .contains(TileSpecial.LOOT))) {
        encounter_timer = takeLoot(encounter_timer, enemy);
        c = 's'; // now, we wait.
        setView(R.layout.sitemode);
        // done with buttons
      } else if (c >= 128 && c <= 128 + i.currentEncounter().size()) {
        i.currentEncounter().creatures().get(c - 128).scrutinize();
      }
      if (moved || c == 's') {
        // NEED TO GO BACK TO OLD LOCATION IN CASE COMBAT
        // REFRESHES THE SCREEN
        final int nlocx = locx;
        final int nlocy = locy;
        final int nlocz = locz;
        locx = olocx;
        locy = olocy;
        locz = olocz;
        // ENEMIES SHOULD GET FREE SHOTS NOW
        if (enemy > 0 && alarm) {
          enemyShots();
        } else if (enemy > 0) {
          disguiseCheckU(encounter_timer);
        }
        Advance.creatureAdvance();
        encounter_timer++;
        partyalive = 0;
        for (final Creature p : i.activeSquad) {
          if (p.health().alive()) {
            partyalive++;
          }
        }
        if (partyalive == 0) {
          continue;
        }
        // AFTER DEATH CHECK CAN MOVE BACK TO NEW LOCATION
        locx = nlocx;
        locy = nlocy;
        locz = nlocz;
        // CHECK FOR EXIT
        if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.EXIT)
            || i.activeSquad.base().getNullable() == current && !current.lcs().siege.siege
            && bail_on_base) {
          exitSite();
          return;
        }
        // DO DOORS
        if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.DOOR)) {
          tryDoor(olocx, olocy, olocz);
          changes = true;
          moved = false;
          continue;
        }
        // SEE IF THERE IS AN ENCOUNTER
        boolean newenc = false;
        // 10% chance of encounter normally
        // 20% chance of encounter after massive response
        // 0% chance of encounter during sieges
        if (moved && !current.lcs().siege.siege && i.rng.chance(5)) {
          if (postAlarmTimer > 80 || i.rng.chance(2)) {
            newenc = true;
          }
        }
        if (c != 's') { // if we moved.
          if (i.currentEncounter().isEmpty()) {
            changes = false;
          }
          /* we need to update our disguise rating if we've moved from secure - insecure or
           * vice-versa */
          final boolean newsecure = siteLevelmap[locx][locy][locz].flag
              .contains(TileSpecial.RESTRICTED);
          if (newsecure != secure) {
            changes = true;
          }
          secure = newsecure;
        }
        if (!i.currentEncounter().isEmpty()) {
          newenc = false;
        }
        // LOOK FOR SPECIALS
        SpecialBlocks makespecial = null;
        final SpecialBlocks squarespecial = siteLevelmap[locx][locy][locz].special;
        if (squarespecial != null) {
          switch (squarespecial) {
          case CLUB_BOUNCER:
          case CLUB_BOUNCER_SECONDVISIT:
          case APARTMENT_LANDLORD:
          case HOUSE_CEO:
          case RESTAURANT_TABLE:
          case CAFE_COMPUTER:
          case PARK_BENCH:
            makespecial = siteLevelmap[locx][locy][locz].special;
            newenc = true;
            break;
          default:
            break;
          }
        }
        // BAIL UPON VICTORY (version 2 -- defeated CCS safehouse)
        if (i.score.ccsSiegeKills >= 12 && !current.lcs().siege.siege
            && current.renting() == CrimeSquad.CCS) {
          defeatCCS();
          return;
        }
        if (current.lcs().siege.siege && siegeMovement(moved))
          return;
        if (!current.lcs().siege.siege && newenc) {
          hostcheck = makeEncounterU(makespecial);
          changes = true;
        }
        if (!current.lcs().siege.siege) {
          landlordEncounter(moved, olocx, olocy, olocz, newenc);
        }
        if (moved) {
          lootAndBleed(olocx, olocy, olocz);
        }
        i.currentEncounter(siteLevelmap[locx][locy][locz].encounter);
        SiteMap.knowmap(locx, locy, locz);
      }
    } while (true);
  }

  private void placeTraps(final Location loc) {
    int lx;
    int ly;
    int lz;
    // PLACE TRAPS
    if (loc.lcs().compoundWalls.contains(Compound.TRAPS)) {
      for (int t = 0; t < TRAPNUM; t++) {
        do {
          lx = i.rng.nextInt(SiteMap.MAPX);
          ly = i.rng.nextInt(SiteMap.MAPY);
          lz = 0;
        } while (siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.BLOCK)
            || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.DOOR)
            || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.EXIT)
            || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.LOOT));
        siteLevelmap[lx][ly][lz].siegeFlag.add(SiegeUnitType.TRAP);
      }
    }
  }

  private void placeUnits() {
    int lx;
    int ly;
    int lz;
    // PLACE UNITS
    int count = 50000;
    for (int t = 0; t < UNITNUM; t++) {
      do {
        lx = i.rng.nextInt(11) + SiteMap.MAPX / 2 - 5;
        ly = i.rng.nextInt(8);
        lz = 0;
        count--;
        if (count == 0) {
          break;
        }
      } while (siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.DOOR)
          || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.EXIT)
          || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.UNIT)
          || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)
          || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.TRAP));
      siteLevelmap[lx][ly][lz].siegeFlag.add(SiegeUnitType.UNIT);
    }
  }

  private void prepareNormal(final Location loc) {
    i.score.ccsSiegeKills = 0;
    // Start at entrance to map
    locx = SiteMap.MAPX / 2;
    locy = 1;
    locz = 0;
    // check for sleeper infiltration
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      // sleeper infiltration :D
      if (p.base().getNullable() == loc || loc.isInterrogated()) {
        // make entire site known
        for (int x = 0; x < SiteMap.MAPX; x++) {
          for (int y = 0; y < SiteMap.MAPY; y++) {
            for (int z = 0; z < i.topfloor; z++) {
              siteLevelmap[x][y][z].flag.add(TileSpecial.KNOWN);
            }
          }
        }
        break;
      }
    }
  }

  private void prepareSiege(final Location loc) {
    alarm = true;
    loc.lcs().siege.attackTime = 0;
    loc.lcs().siege.kills = 0;
    loc.lcs().siege.tanks = 0;
    try {
      for (int x = 0; x < SiteMap.MAPX; x++) {
        for (int y = 0; y < SiteMap.MAPY; y++) {
          for (int z = 0; z < i.topfloor; z++) {
            if (!loc.lcs().siege.lightsOff) {
              siteLevelmap[x][y][z].flag.add(TileSpecial.KNOWN); // Oops NPE
            }
            siteLevelmap[x][y][z].flag.remove(TileSpecial.LOCKED);
            siteLevelmap[x][y][z].flag.remove(TileSpecial.LOOT);
          }
        }
      }
    } catch (final NullPointerException npe) {
      Log.e("SiteMode", "NullPointerException", npe);
    }
    // Cops have tanks; firemen have fire.
    if (loc.lcs().siege.siegetype == CrimeSquad.FIREMEN) {
      int firesstarted = 0;
      int firex = i.rng.nextInt(SiteMap.MAPX);
      int firey = i.rng.nextInt(SiteMap.MAPY);
      do {
        firex = i.rng.nextInt(SiteMap.MAPX);
        firey = i.rng.nextInt(SiteMap.MAPY);
        firesstarted++;
        siteLevelmap[firex][firey][0].flag.add(TileSpecial.FIRE_START);
      } while (!(siteLevelmap[firex][firey][0].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[firex][firey][0].flag.contains(TileSpecial.DOOR) || siteLevelmap[firex][firey][0].flag
            .contains(TileSpecial.EXIT)) && firesstarted < 4);
    }
    do {
      // Some bugs with degenarate spawn outside the map are occurring
      // Unknown why, but hard-coding limits to spawn location should
      // help
      // locx=i.rng.getInt(Includes.MAPX);
      // locy=maxy-i.rng.getInt(3);
      locx = SiteMap.MAPX / 2 + i.rng.nextInt(25) - 12;
      locy = 15 - i.rng.nextInt(3);
      // if(locy<3)locy=3;
      locz = 0;
    } while (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.BLOCK)
        || siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.DOOR)
        || siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.FIRE_START)
        || siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.FIRE_PEAK)
        || siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.FIRE_END));
    // PLACE LOOT
    int lootnum = loc.lcs().loot.size();
    if (lootnum > 10) {
      lootnum = 10;
    }
    int lx, ly, lz;
    for (int l = 0; l < lootnum; l++) {
      do {
        lx = i.rng.nextInt(SiteMap.MAPX);
        ly = i.rng.nextInt(SiteMap.MAPY);
        lz = 0;
      } while (siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.BLOCK)
          || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.DOOR)
          || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.EXIT));
      siteLevelmap[lx][ly][lz].flag.add(TileSpecial.LOOT);
    }
    placeTraps(loc);
    placeUnits();
    if (!(loc.lcs().compoundWalls.contains(Compound.TANKTRAPS)
        && loc.lcs().siege.siegetype == CrimeSquad.POLICE && loc.lcs().siege.escalationState >= 2)) {
      siteLevelmap[SiteMap.MAPX / 2][1][0].siegeFlag.add(SiegeUnitType.HEAVYUNIT);
      loc.lcs().siege.tanks = 1;
    }
  }

  private void reflectOnIneptitude() {
    // DESTROY ALL CARS BROUGHT ALONG WITH PARTY
    for (final Creature p : i.activeSquad) {
      if (!current.lcs().siege.siege && p.car().exists()) {
        final Vehicle v = p.car().get();
        i.vehicle.remove(v);
      }
      p.health().die();
    }
    // END OF GAME CHECK
    EndGame.endcheck(null);
    if (current.lcs().siege.siege) {
      if (current.lcs().siege.underAttack) {
        i.siteStory.type(StoryType.SQUAD_KILLED_SIEGEATTACK);
      } else {
        i.siteStory.type(StoryType.SQUAD_KILLED_SIEGEESCAPE);
      }
    } else if (!current.lcs().siege.siege) {
      i.siteStory.type(StoryType.SQUAD_KILLED_SITE);
      resolvesite();
    }
    i.mode(GameMode.BASE);
  }

  private void releaseOppressed(final int oldpartysize, final int enemy) {
    int partysize = oldpartysize;
    int followers = 0;
    int actgot = 0;
    if (enemy > 0) {
      alarm = true;
    }
    boolean freed;
    boolean flipstart;
    do {
      flipstart = false;
      freed = false;
      final Iterator<Creature> ei = i.currentEncounter().creatures().iterator();
      while (ei.hasNext()) {
        final Creature e = ei.next();
        if ((e.type() == CreatureType.valueOf("WORKER_SERVANT")
            || e.type() == CreatureType.valueOf("WORKER_FACTORY_CHILD")
            || e.type() == CreatureType.valueOf("WORKER_SWEATSHOP") || e.isPrisoner()
            && e.alignment() == Alignment.LIBERAL)
            && !flipstart) {
          if (e.isPrisoner()) {
            alarm = true; /* alarm for prisoner escape */
            e.crime().criminalize(Crime.ESCAPED);
          }
          followers++;
          flipstart = true;
          freed = true;
          if (partysize < 6) {
            Creature j = null;
            // Check for people who can recruit
            // followers
            for (final Creature k : i.activeSquad) {
              if (k.subordinatesLeft() > 0) {
                j = k;
                break;
              }
            }
            // If someone can, add this person as a
            // newly recruited Liberal!
            if (j != null) {
              final Creature newcr = e;
              newcr.location(j.location()
                  .orElse(AbstractSiteType.type(Shelter.class).getLocation()));
              newcr.base(j.base().getNullable());
              newcr.hire(j);
              i.pool.add(newcr);
              i.score.recruits++;
              i.activeSquad.add(newcr);
              actgot++;
              partysize++;
            }
          }
        }
        if (flipstart) {
          ei.remove();
        }
      }
      if (freed) {
        int time = 20 + i.rng.nextInt(10);
        if (time < 1) {
          time = 1;
        }
        if (alarmTimer() > time || alarmTimer() == -1) {
          alarmTimer(time);
        }
      }
    } while (freed);
    if (followers > 0) {
      clearChildren(R.id.smcontrols);
      if (followers > 1) {
        ui(R.id.smcontrols).text("You free some Oppressed Liberals from the Conservatives").add();
      } else {
        ui(R.id.smcontrols).text("You free an Oppressed Liberal from the Conservatives").add();
      }
      if (actgot < followers) {
        if (actgot == 0 && followers > 1) {
          ui(R.id.smcontrols).text("They all leave you, feeling safer getting out alone.").add();
        } else if (followers - actgot > 1) {
          ui(R.id.smcontrols).text("Some leave you, feeling safer getting out alone.").add();
        } else if (actgot == 0) {
          ui(R.id.smcontrols).text("The Liberal leaves you, feeling safer getting out alone.")
              .add();
        } else {
          ui(R.id.smcontrols).text("One Liberal leaves you, feeling safer getting out alone.")
              .add();
        }
      }
      getch();
    }
  }

  private void resolvesite() {
    if (siteAlienate == Alienation.NONE) {
      i.siteStory.positive = true;
    }
    // removed the 'alarmed' requirement for high security buildings, on the
    // principle that even if they didn't see you, they will presumably
    // notice later on that all their stuff has been stolen or whatever.
    if (// this.sitealarm==1&&
    crime > 5 + i.rng.nextInt(95))// was 100 but that meant I could
    // still steal everything from a building every day without anyone
    // caring...
    {
      if (current.renting() == null) {
        // Capture a warehouse or crack den?
        if (current.type().isType(Warehouse.class) || current.type().isType(CrackHouse.class)) {
          current.renting(CrimeSquad.LCS); // Capture
          // safehouse
          // for the
          // glory of
          // the LCS!
          current.closed(0);
          current.lcs().heat = 100;
        } else {
          // Close down site
          current.closed(crime / 10);
        }
      }
      // Out sleepers
      if (current.renting() == CrimeSquad.CCS) {
        for (final Creature p : Filter.of(i.pool, Filter.SLEEPER)) {
          if (p.location().getNullable() == current) {
            p.removeFlag(CreatureFlag.SLEEPER);
            ui().text("Sleeper " + p.toString() + " has been outed by your bold attack!").add();
            ui().text("The Liberal is now at your command as a normal squad member.").add();
            p.base(i.activeSquad.base().getNullable());
            p.location(p.base().getNullable());
            getch();
          }
        }
      }
    } else if ((alarm && crime > 10 || crime > 5 + i.rng.nextInt(95))
        && (current.renting() == null || current.rent() > 500)) {
      if (!(current.type().isType(BombShelter.class))
          && !(current.type().isType(BarAndGrill.class)) && !(current.type().isType(Bunker.class))
          && !(current.type().isType(Warehouse.class))
          && !(current.type().isType(CrackHouse.class))) {
        if (current.type().securityLevel() != SecurityLevel.POOR) {
          current.highSecurity(crime);
        } else {
          current.closed(7);
        }
      }
    }
    if (current.closed()) {
      if (current.type().isType(AmRadio.class)) {
        // AM Radio less effective if brought offline
        i.issue(Issue.AMRADIO).changeOpinion(10, 1, 100);
      }
      if (current.type().isType(PoliceStation.class)) {
        // People generally want to give police more power if they
        // get closed down
        i.issue(Issue.POLICEBEHAVIOR).changeOpinion(-10, 1, 100);
        AbstractSiteType.type(PoliceStation.class).getLocation().closed(30);
      }
      if (current.type().isType(CableNews.class)) {
        // Cable News less influential if brought offline
        i.issue(Issue.CABLENEWS).changeOpinion(10, 1, 100);
      }
    }
  }

  private void setHeader() {
    final StringBuilder sb = new StringBuilder();
    sb.append(current.toString());
    sb.append(", Level ");
    // char num[20];
    sb.append(locz + 1);
    if (postAlarmTimer > 80) {
      sb.append(current.type().alarmResponseString());
    } else if (postAlarmTimer > 60) {
      sb.append(": CONSERVATIVE REINFORCEMENTS INCOMING");
    } else if (siteAlienate == Alienation.MODERATES) {
      sb.append(": ALIENATED MASSES");
    } else if (siteAlienate == Alienation.ALL) {
      sb.append(": ALIENATED EVERYONE");
    } else if (alarm) {
      sb.append(": CONSERVATIVES ALARMED");
    } else if (alarmTimer() == 0) {
      sb.append(": CONSERVATIVES SUSPICIOUS");
    }
    setText(R.id.smlocation, sb.toString());
  }

  private void setMovementEnabled(final boolean enabled) {
    setEnabled(R.id.sbh,
        enabled && !siteLevelmap[locx][locy - 1][locz].flag.contains(TileSpecial.BLOCK));
    setEnabled(R.id.sbl,
        enabled && !siteLevelmap[locx - 1][locy][locz].flag.contains(TileSpecial.BLOCK));
    setEnabled(R.id.sbn,
        enabled && !siteLevelmap[locx + 1][locy][locz].flag.contains(TileSpecial.BLOCK));
    setEnabled(R.id.sbr,
        enabled && !siteLevelmap[locx][locy + 1][locz].flag.contains(TileSpecial.BLOCK));
  }

  private boolean siegeMovement(final boolean moved) { // return
    // true
    // if
    // exiting
    // sitemode
    {
      if (moved) {
        i.currentEncounter().creatures().clear();
      }
      // MOVE SIEGE UNITS AROUND
      // MOVE UNITS
      // vector<int> unitx;
      // vector<int> unity;
      // vector<int> unitz;
      final List<Integer> unitx = new ArrayList<Integer>();
      final List<Integer> unity = new ArrayList<Integer>();
      final List<Integer> unitz = new ArrayList<Integer>();
      for (int x = 0; x < SiteMap.MAPX; x++) {
        for (int y = 0; y < SiteMap.MAPY; y++) {
          for (int z = 0; z < SiteMap.MAPZ; z++) {
            if (siteLevelmap[x][y][z].siegeFlag.contains(SiegeUnitType.UNIT)) {
              unitx.add(x);
              unity.add(y);
              unitz.add(z);
            }
          }
        }
      }
      int sx = 0, sy = 0, sz = 0;
      for (int u = 0; u < unitx.size(); u++) {
        // don't leave tile if player is here
        if (unitx.get(u) == locx && unity.get(u) == locy && unitz.get(u) == locz) {
          continue;
        }
        // move into player's tile if possible
        if (((unitx.get(u) == locx - 1 || unitx.get(u) == locx + 1) && unity.get(u) == locy || (unity
            .get(u) == locy - 1 || unity.get(u) == locy + 1) && unitx.get(u) == locx)
            && unitz.get(u) == locz) {
          siteLevelmap[unitx.get(u)][unity.get(u)][unitz.get(u)].siegeFlag
              .remove(SiegeUnitType.UNIT);
          // Get torched
          if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.FIRE_PEAK)) {
            siteLevelmap[locx][locy][locz].siegeFlag.add(SiegeUnitType.UNIT_DAMAGED);
          }
          // BLOW TRAPS
          if (siteLevelmap[locx][locy][locz].siegeFlag.contains(SiegeUnitType.TRAP)) {
            siteLevelmap[locx][locy][locz].siegeFlag.remove(SiegeUnitType.TRAP);
            siteLevelmap[locx][locy][locz].siegeFlag.add(SiegeUnitType.UNIT_DAMAGED);
          } else {
            siteLevelmap[locx][locy][locz].siegeFlag.add(SiegeUnitType.UNIT);
          }
          continue;
        }
        sz = 0;
        switch (i.rng.nextInt(4)) {
        default:
          sx = -1;
          sy = 0;
          break;
        case 1:
          sx = 1;
          sy = 0;
          break;
        case 2:
          sx = 0;
          sy = 1;
          break;
        case 3:
          sx = 0;
          sy = -1;
          break;
        }
        sx = unitx.get(u) + sx;
        sy = unity.get(u) + sy;
        sz = unitz.get(u) + sz;
        if (sx >= 0 && sx < SiteMap.MAPX && sy >= 0 && sy < SiteMap.MAPY && sz >= 0
            && sz < SiteMap.MAPZ) {
          if (!siteLevelmap[sx][sy][sz].flag.contains(TileSpecial.BLOCK)) {
            if (siteLevelmap[sx][sy][sz].flag.contains(TileSpecial.DOOR)) {
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.DOOR);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.LOCKED);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.KLOCK);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.CLOCK);
            } else {
              char conf = 1;
              // BLOCK PASSAGE
              if (siteLevelmap[sx][sy][sz].siegeFlag.contains(SiegeUnitType.UNIT)) {
                conf = 0;
              }
              if (siteLevelmap[sx][sy][sz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)) {
                conf = 0;
              }
              if (conf != 0) {
                siteLevelmap[unitx.get(u)][unity.get(u)][unitz.get(u)].siegeFlag
                    .remove(SiegeUnitType.UNIT);
                // BLOW TRAPS
                if (siteLevelmap[sx][sy][sz].siegeFlag.contains(SiegeUnitType.TRAP)) {
                  siteLevelmap[sx][sy][sz].siegeFlag.remove(SiegeUnitType.TRAP);
                  siteLevelmap[sx][sy][sz].siegeFlag.add(SiegeUnitType.UNIT_DAMAGED);
                } else {
                  siteLevelmap[sx][sy][sz].siegeFlag.add(SiegeUnitType.UNIT);
                }
              }
            }
          }
        }
      }
      unitx.clear();
      unity.clear();
      unitz.clear();
      /* //MOVE HEAVY UNITS for(x=0;x<Includes.MAPX;x++) { for(int y=0;y<Includes.MAPY;y++) {
       * for(int z=0;z<Includes.MAPZ;z++) { if(i.levelmap[x][y][z].siegeFlag.& SiegeFlag.HEAVYUNIT)
       * { unitx.push_back(x); unity.push_back(y); unitz.push_back(z); } } } }
       * for(u=0;u<unitx.size();u++) { sz=0; switch(i.rng.getInt(4)) { case 0:sx=-1;sy=0; break;
       * case 1:sx=1;sy=0; break; case 2:sx=0;sy=1; break; case 3:sx=0;sy=-1; break; }
       * sx=unitx.get(u)+sx; sy=unity.get(u)+sy; sz=unitz.get(u)+sz;
       * if(sx>=0&&sx<Includes.MAPX&&sy>=0&&sy<Includes.MAPY&&sz >=0&&sz<Includes.MAPZ) {
       * if(!(i.levelmap[sx][sy][sz].flag .contains(SiteBlock.BLOCK))) { if((i.levelmap[sx][sy][
       * sz].flag.contains(SiteBlock.DOOR))) { i.levelmap[sx][sy][sz].flag&=~SiteBlock.DOOR;
       * i.levelmap[sx][sy][sz].flag&=~SiteBlock.LOCKED;
       * i.levelmap[sx][sy][sz].flag&=~SiteBlock.KLOCK;
       * i.levelmap[sx][sy][sz].flag&=~SiteBlock.CLOCK; } else { char conf=1; //BLOCK PASSAGE
       * if(i.levelmap[sx][sy][sz].siegeFlag.& SiegeFlag.UNIT)conf=0;
       * if(i.levelmap[sx][sy][sz].siegeFlag.& SiegeFlag.HEAVYUNIT)conf=0; if(conf) {
       * i.levelmap[unitx.get(u)][unity.get(u)][unitz .get(u)].siegeFlag &=~SiegeFlag.HEAVYUNIT;
       * i.levelmap[sx][sy][sz].siegeFlag |=SiegeFlag.HEAVYUNIT; //BLOW (DIFFUSE) TRAPS
       * if(i.levelmap[sx][sy][sz].siegeFlag ..contains(SiegeFlag.TRAP)) {
       * i.levelmap[sx][sy][sz].siegeFlag.=~SiegeFlag.TRAP; } } } } else { //BREAK WALLS
       * if(sy>=3&&sx>0&&sx<Includes .MAPX-1&&sy<Includes.MAPY-1) { sitechangest
       * change(sx,sy,sz,SiteBlock.DEBRIS); i.cursite.changes.push_back(change);
       * i.levelmap[sx][sy][sz].flag&=~SiteBlock.BLOCK;
       * i.levelmap[sx][sy][sz].flag|=SiteBlock.DEBRIS; } } } } unitx.clear(); unity.clear();
       * unitz.clear(); // End Heavy Units */
      for (int u = 0; u < unitx.size(); u++) {
        sz = 0;
        switch (i.rng.nextInt(4)) {
        default:
          sx = -1;
          sy = 0;
          break;
        case 1:
          sx = 1;
          sy = 0;
          break;
        case 2:
          sx = 0;
          sy = 1;
          break;
        case 3:
          sx = 0;
          sy = -1;
          break;
        }
        sx = unitx.get(u) + sx;
        sy = unity.get(u) + sy;
        sz = unitz.get(u) + sz;
        if (sx >= 0 && sx < SiteMap.MAPX && sy >= 0 && sy < SiteMap.MAPY && sz >= 0
            && sz < SiteMap.MAPZ) {
          if (!siteLevelmap[sx][sy][sz].flag.contains(TileSpecial.BLOCK)) {
            if (siteLevelmap[sx][sy][sz].flag.contains(TileSpecial.DOOR)) {
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.DOOR);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.LOCKED);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.KLOCK);
              siteLevelmap[sx][sy][sz].flag.remove(TileSpecial.CLOCK);
              // } else {
              // char conf = 1;
              //
              // // BLOCK PASSAGE
              // if (i.levelmap[sx][sy][sz].siegeFlag
              // .contains(SiegeFlag.UNIT))
              // conf = 0;
              // if (i.levelmap[sx][sy][sz].siegeFlag
              // .contains(SiegeFlag.HEAVYUNIT))
              // conf = 0;
            }
          }
        }
      }
      unitx.clear();
      unity.clear();
      unitz.clear();
      // NEW WAVES
      // IF THERE AREN'T ENOUGH UNITS AROUND
      // AND THEY HAVEN'T BEEN SCARED OFF
      // MORE WAVES WILL ATTACK
      // AND IT GETS WORSE AND WORSE
      // but not as bad as it used to get,
      // since the extra waves are small
      current.lcs().siege.attackTime++;
      if (current.lcs().siege.attackTime >= 100 + i.rng.nextInt(10)
          && (locz != 0 || locx < SiteMap.MAPX / 2 - 3 || locx > SiteMap.MAPX / 2 + 3 || locy > 5)) {
        current.lcs().siege.attackTime = 0;
        int existingUnits = 0;
        for (int x = 0; x < SiteMap.MAPX; x++) {
          for (int y = 0; y < SiteMap.MAPY; y++) {
            for (int z = 0; z < SiteMap.MAPZ; z++) {
              if (siteLevelmap[x][y][z].siegeFlag.contains(SiegeUnitType.UNIT)
                  || siteLevelmap[x][y][z].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)) {
                existingUnits++;
              }
            }
          }
        }
        // PLACE UNITS
        int lx, ly, lz;
        int unitnum = 7 - existingUnits;
        if (unitnum < 0) {
          unitnum = 0;
        }
        int count = 10000;
        for (int t = 0; t < unitnum; t++) {
          count = 10000;
          do {
            lx = i.rng.nextInt(7) + SiteMap.MAPX / 2 - 3;
            ly = i.rng.nextInt(5);
            lz = 0;
            count--;
            if (count == 0) {
              break;
            }
          } while (siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.BLOCK)
              || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.DOOR)
              || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.EXIT)
              || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.UNIT)
              || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)
              || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.TRAP));
          siteLevelmap[lx][ly][lz].siegeFlag.add(SiegeUnitType.UNIT);
        }
        if (!current.lcs().compoundWalls.contains(Compound.TANKTRAPS)
            && current.lcs().siege.siegetype == CrimeSquad.POLICE
            && current.lcs().siege.escalationState >= 2) {
          count = 10000;
          for (int t = 0; t < HUNITNUM; t++) {
            do {
              lx = i.rng.nextInt(7) + SiteMap.MAPX / 2 - 3;
              ly = i.rng.nextInt(5);
              lz = 0;
              count--;
              if (count == 0) {
                break;
              }
            } while (siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.BLOCK)
                || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.DOOR)
                || siteLevelmap[lx][ly][lz].flag.contains(TileSpecial.EXIT)
                || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.UNIT)
                || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)
                || siteLevelmap[lx][ly][lz].siegeFlag.contains(SiegeUnitType.TRAP));
            siteLevelmap[lx][ly][lz].siegeFlag.add(SiegeUnitType.HEAVYUNIT);
          }
        }
      }
      // CHECK FOR SIEGE UNITS
      // INCLUDING DAMAGED ONES
      if (siteLevelmap[locx][locy][locz].siegeFlag.contains(SiegeUnitType.UNIT)) {
        if (SiegeUnitType.UNIT.addSiegeEncounter()) {
          siteLevelmap[locx][locy][locz].siegeFlag.remove(SiegeUnitType.UNIT);
        }
      }
      if (siteLevelmap[locx][locy][locz].siegeFlag.contains(SiegeUnitType.HEAVYUNIT)) {
        if (SiegeUnitType.HEAVYUNIT.addSiegeEncounter()) {
          siteLevelmap[locx][locy][locz].siegeFlag.remove(SiegeUnitType.HEAVYUNIT);
        }
      }
      if (siteLevelmap[locx][locy][locz].siegeFlag.contains(SiegeUnitType.UNIT_DAMAGED)) {
        if (SiegeUnitType.UNIT_DAMAGED.addSiegeEncounter()) {
          siteLevelmap[locx][locy][locz].siegeFlag.remove(SiegeUnitType.UNIT_DAMAGED);
        }
      }
      // BAIL UPON VICTORY
      if (current.lcs().siege.kills >= 10 && current.lcs().siege.tanks == 0
          && current.lcs().siege.siege) {
        if (current.lcs().siege.underAttack) {
          i.siteStory.type(StoryType.SQUAD_DEFENDED);
        } else {
          i.siteStory.type(StoryType.SQUAD_BROKESIEGE);
        }
        if (current.lcs().siege.siegetype == CrimeSquad.CCS) {
          // CCS DOES NOT capture the warehouse -- reverse
          // earlier assumption of your defeat!
          if (current.type().isType(Warehouse.class) || current.type().isType(CrackHouse.class)) {
            current.renting(CrimeSquad.LCS);
          } else if (current.type().isType(Tenement.class)) {
            current.rent(200);
          } else if (current.type().isType(Apartment.class)) {
            current.rent(650);
          } else if (current.type().isType(ApartmentUpscale.class)) {
            current.rent(1500);
          }
        }
        // DEAL WITH PRISONERS AND STOP BLEEDING
        for (final Creature p : i.activeSquad) {
          if (p == null) {
            continue;
          }
          if (p.prisoner().exists()) {
            if (p.prisoner().get().squad().exists()) {
              // RESTORE POOL MEMBER
              p.prisoner().get().squad(null).location(p.base().getNullable())
                  .base(p.base().getNullable());
            } else {
              Interrogation.create(p.prisoner().get(), i.activeSquad.member(0));
            }
            // delete
            // p.prisoner;
            p.prisoner(null);
          }
        }
        for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
          p.removeFlag(CreatureFlag.JUST_ESCAPED);
          p.health().stopBleeding();
        }
        // INFORM
        // clearmessagearea();
        ui().text("The Conservatives have shrunk back under the power of your Liberal Convictions!")
            .add();
        getch();
        current.lcs().siege.conquerText();
        current.lcs().siege.escapeSiege(true);
        // RESET MODE
        i.mode(GameMode.BASE);
        return true;
      }
    }
    return false;
  }

  /* site - determines spin on site news story, "too hot" timer */
  private void takeGroundLoot() {
    final int time = 20 + i.rng.nextInt(10);
    if (alarmTimer() > time || alarmTimer() == -1) {
      alarmTimer(time);
    }
    final String lootName = type.randomLootItem();
    if (lootName == null)
      return;
    final AbstractItem<? extends AbstractItemType> item = Item.itemForName(lootName);
    if (item instanceof Armor) {
      final Armor a = (Armor) item;
      Armor.Rating quality = Armor.Rating.FIRST;
      if (i.rng.chance(3)) {
        quality = Armor.Rating.SECOND;
      }
      a.quality(quality);
      if (i.rng.chance(3)) {
        a.setDamaged(true);
      }
    }
    if (item instanceof Weapon) {
      final Weapon w = (Weapon) item;
      if (w.usesAmmo()) {
        final List<ClipType> cti = new ArrayList<ClipType>();
        for (final ClipType ct : Game.type.clip.values()) {
          if (w.acceptableAmmo(ct)) {
            cti.add(ct);
          }
          w.reload(new Clip(i.rng.randFromList(cti)));
        }
      }
    }
    i.activeSquad.loot().add(item);
    fact("You find " + item.equipTitle());
  }

  private int takeLoot(final int old_encounter_timer, final int enemy) {
    int encounter_timer = old_encounter_timer;
    boolean tookground = false;
    if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.LOOT)) {
      siteLevelmap[locx][locy][locz].flag.remove(TileSpecial.LOOT);
      if (current.lcs().siege.siege) {
        // GRAB SOME OF THE BASE LOOT
        int lcount = 1; // 1 FROM THE ONE DELETED ABOVE
        for (int x = 0; x < SiteMap.MAPX; x++) {
          for (int y = 0; y < SiteMap.MAPY; y++) {
            for (int z = 0; z < SiteMap.MAPZ; z++) {
              if (siteLevelmap[x][y][z].flag.contains(TileSpecial.LOOT)) {
                lcount++;
              }
            }
          }
        }
        int lplus = current.lcs().loot.size() / lcount;
        if (lcount == 1) {
          lplus = current.lcs().loot.size();
        }
        AbstractItem<? extends AbstractItemType> b;
        while (lplus > 0) {
          b = i.rng.randFromList(current.lcs().loot);
          i.activeSquad.loot().add(b);
          current.lcs().loot.remove(b);
          lplus--;
        }
      } else {
        takeGroundLoot();
      }
      tookground = true;
    }
    // MAKE GROUND LOOT INTO MISSION LOOT
    i.activeSquad.loot().addAll(i.groundLoot());
    i.groundLoot().clear();
    if (enemy > 0 && alarm) {
      setView(R.layout.generic);
      Fight.fight(Fighting.THEM);
      getch();
    } else if (enemy > 0) {
      disguiseCheckU(encounter_timer);
    }
    if (tookground) {
      final Creature beststealer = i.activeSquad.member(0);
      i.activeSquad.juice(1, 200);
      alienationCheck(false);
      noticeCheck();
      crime++;
      i.siteStory.addNews(NewsEvent.STOLE_GROUND);
      if (enemy > 0) {
        beststealer.crime().criminalize(Crime.THEFT);
      }
    }
    Advance.creatureAdvance();
    encounter_timer++;
    return encounter_timer;
  }

  private int talk(final int old_encounter_timer, final int enemy) {
    int encounter_timer = old_encounter_timer;
    int c;
    Creature forcesp = null;
    Creature forcetk = null;
    for (final Creature p : i.activeSquad) {
      if (p.health().alive()) {
        if (forcesp == null) {
          forcesp = p;
        } else {
          forcesp = null;
          break;
        }
      }
    }
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.health().alive()
          && !(e.type() == CreatureType.valueOf("WORKER_SERVANT") || e.type() == CreatureType
              .valueOf("WORKER_SWEATSHOP"))) {
        if (e.receptive() != Receptive.HEARD || alarm) {
          if (forcetk == null) {
            forcetk = e;
          } else {
            forcetk = null;
            break;
          }
        }
      }
    }
    Creature tk = null, sp = null;
    if (forcesp == null) {
      setView(R.layout.generic);
      ui().text("Which Liberal will speak? (Issues / Dating / Bluff").add();
      int y = '1';
      final StringBuilder sb = new StringBuilder();
      for (final Creature p : i.activeSquad) {
        if (p.health().alive()) {
          sb.setLength(0);
          sb.append(String.valueOf(y));
          sb.append(" - ");
          sb.append(p.toString());
          sb.append(p.skill().getAttribute(Attribute.CHARISMA, true) / 2
              + p.skill().skill(Skill.PERSUASION));
          sb.append('/');
          sb.append(p.skill().getAttribute(Attribute.CHARISMA, true) / 2
              + p.skill().skill(Skill.SEDUCTION));
          sb.append('/');
          sb.append(p.skill().getAttribute(Attribute.CHARISMA, true) / 2
              + p.skill().skill(Skill.DISGUISE));
          ui().button(y).text(sb.toString()).add();
          sb.setLength(0);
          y++;
        }
      }
      do {
        c = getch();
        if (c >= '1' && c <= '6') {
          sp = i.activeSquad.member(c - '1');
          if (sp != null) {
            if (sp.health().alive()) {
              break;
            }
          }
        }
        if (c == 10 || c == 27 || c == 32) {
          sp = null;
          break;
        }
      } while (true);
    } else {
      sp = forcesp;
    }
    if (sp != null) {
      if (forcetk == null) {
        do {
          setView(R.layout.generic);
          ui().text("To whom?").add();
          char ch = 'a';
          for (final Creature t : i.currentEncounter().creatures()) {
            if (t.receptive() != Receptive.HEARD) {
              ui().button(ch).text(t.longDescription()).color(t.alignment().color()).add();
            }
            ch++;
          }
          c = getch();
          if (c >= 'a' && c <= 'z') {
            if (c - 'a' > i.currentEncounter().creatures().size() - 1) {
              continue;
            }
            tk = i.currentEncounter().creatures().get(c - 'a');
            if (tk.health().alive()
                && !(tk.type() == CreatureType.valueOf("WORKER_SERVANT") || tk.type() == CreatureType
                    .valueOf("WORKER_SWEATSHOP"))) {
              if (tk.receptive() == Receptive.HEARD && !alarm) {
                clearChildren(R.id.smcontrols);
                ui(R.id.smcontrols).text(tk.toString() + " won't talk to you.").add();
                getch();
              } else if (!tk.enemy() && alarm && enemy != 0) {
                clearChildren(R.id.smcontrols);
                ui(R.id.smcontrols).text("You have to deal with the enemies first.").add();
                getch();
              } else {
                break;
              }
            }
          }
          if (c == 10 || c == 27 || c == 32) {
            tk = null;
            break;
          }
        } while (true);
      } else {
        tk = forcetk;
      }
      if (tk != null) {
        if (Talk.talk(sp, tk)) {
          if (enemy != 0 && alarm) {
            setView(R.layout.generic);
            Fight.fight(Fighting.THEM);
            // getch();
          } else if (enemy != 0) {
            disguiseCheckU(encounter_timer);
          }
          Advance.creatureAdvance();
          encounter_timer++;
        }
      }
    }
    c = 0; // oops, assault following a choice of talkee
    return encounter_timer;
  }

  private void tryDoor(final int olocx, final int olocy, final int olocz) {
    int c;
    boolean has_security = false;
    for (final Creature j : i.activeSquad) {
      if (j != null && j.skill().skill(Skill.SECURITY) != 0) {
        has_security = true;
        break;
      }
    }
    if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.LOCKED)
        && !siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.CLOCK) && has_security) {
      siteLevelmap[locx][locy][locz].flag.add(TileSpecial.KLOCK);
      c = yesOrNo("You try the door, but it is locked. Try to pick the lock?");
      if (c == 'y') {
        final SuccessTest actual = Unlockable.DOOR.unlock(); // 1
        // if
        // an
        // actual
        // attempt was
        // made, 0 otherwise
        // If the unlock was successful
        if (actual.succeeded()) {
          // Unlock the door
          siteLevelmap[locx][locy][locz].flag.remove(TileSpecial.LOCKED);
          i.siteStory.addNews(NewsEvent.UNLOCKED_DOOR);
          // criminalizeparty(LAWFLAG_BREAKING);
        }
        // Else perma-lock it if an attempt was made
        else if (actual.madeNoise()) {
          siteLevelmap[locx][locy][locz].flag.add(TileSpecial.CLOCK);
        }
        // Check for people noticing you fiddling
        // with the lock
        if (actual.madeNoise()) {
          alienationCheck(false);
          noticeCheck();
        }
      }
    } else if (siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.LOCKED)) {
      c = yesOrNo("You shake the handle but it is " + (has_security ? "still " : "")
          + "locked. Force it open?");
      if (c == 'y') {
        final SuccessTest actual = Bashable.DOOR.bash();
        if (actual.succeeded()) {
          siteLevelmap[locx][locy][locz].flag.remove(TileSpecial.DOOR);
          int time = 20 + i.rng.nextInt(10);
          if (time < 1) {
            time = 1;
          }
          if (alarmTimer() > time || alarmTimer() == -1) {
            alarmTimer(time);
          }
          crime++;
          i.siteStory.addNews(NewsEvent.BROKE_DOWN_DOOR);
          i.activeSquad.criminalizeParty(Crime.BREAKING);
        }
        if (actual.madeNoise()) {
          alienationCheck(true);
          noticeCheck(CheckDifficulty.HEROIC);
        }
      }
    } else {
      siteLevelmap[locx][locy][locz].flag.remove(TileSpecial.DOOR);
    }
    // doors stop you in your tracks.
    locx = olocx;
    locy = olocy;
    locz = olocz;
  }

  private void useMapSpecialOrGraffiti() {
    if (siteLevelmap[locx][locy][locz].special != null) {
      siteLevelmap[locx][locy][locz].special.special();
    } else if (!(siteLevelmap[locx][locy][locz].flag.contains(TileSpecial.GRAFFITI) || siteLevelmap[locx][locy][locz].flag
        .contains(TileSpecial.BLOODY2))
        && (siteLevelmap[locx + 1][locy][locz].flag.contains(TileSpecial.BLOCK)
            || siteLevelmap[locx - 1][locy][locz].flag.contains(TileSpecial.BLOCK)
            || siteLevelmap[locx][locy + 1][locz].flag.contains(TileSpecial.BLOCK) || siteLevelmap[locx][locy - 1][locz].flag
            .contains(TileSpecial.BLOCK))) {
      boolean spray = false;
      for (final Creature p : i.activeSquad) {
        if (p == null) {
          break;
        }
        if (p.weapon().weapon().canGraffiti()) {
          spray = true;
          break;
        }
      }
      if (spray) {
        specialGraffiti();
      }
    }
  }

  /* static * Downwards arrow */
  private final static char SMILEY = '@';

  private final static char DOWN = '\u2193';

  private final static int HUNITNUM = 1;

  private final static int TRAPNUM = 30;

  private static final int UNITNUM = 6;

  public static void travelToSite(final Location loc) {
    i.site = new Site(loc);
    SiteMap.initsite(loc);
    i.siteStory = i.newsStories.get(i.newsStories.size() - 1);
    i.mode(GameMode.SITE);
    if (loc.lcs() != null && loc.lcs().siege.siege) {
      i.site.prepareSiege(loc);
    } else {
      i.site.prepareNormal(loc);
    }
    i.site.modeSite();
    // clear the site memory, we're done with it.
    i.site = new Site();
  }

  private static void enemyShots() {
    // boolean snuck_away = true,
    boolean sneakable = true;
    // int e;
    // Try to sneak past
    for (final Creature f : i.currentEncounter().creatures()) {
      if (f.health().alive() && f.receptive() == Receptive.ANGERED) {
        // You can't sneak past this person; they
        // already know you're there
        fact(f + " already knows you're there!");
        sneakable = false;
        break;
      }
    }
    // If you can sneak past all enemies
    if (sneakable) {
      // for () {
      squad: for (final Creature f : i.currentEncounter().creatures()) {
        for (final Creature j : i.activeSquad) {
          if (!j.skill().skillCheck(Skill.STEALTH, CheckDifficulty.HARD)) {
            sneakable = false;
            fact(j + " is spotted by " + f + "!");
            break squad;
          }
        }
      }
    }
    // If snuck past everyone
    if (sneakable) {
      for (final Creature j : i.activeSquad) {
        j.skill().train(Skill.STEALTH, 10);
      }
      fact("The squad sneaks past the conservatives!");
    } else {
      setView(R.layout.generic);
      Fight.fight(Fighting.THEM);
      // getch();
    }
  }

  private static void fightSubduedU() {
    if (i.currentEncounter() instanceof CarChase) {
      ((CarChase) i.currentEncounter()).loseAllCars();
    }
    int hostagefreed = 0;
    int stolen = 0;
    // Police assess stolen goods in inventory
    for (final AbstractItem<? extends AbstractItemType> l : i.activeSquad.loot()) {
      if (l instanceof Loot) {
        stolen++;
      }
    }
    for (final Creature p : i.activeSquad) {
      if (p.prisoner().exists()) {
        p.prisoner(null);
        hostagefreed++;
      }
      p.crime().incrementCrime(Crime.THEFT, stolen);
      p.captureByPolice(Crime.THEFT);
    }
    i.activeSquad.clear();
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      p.health().stopBleeding();
    }
    setView(R.layout.generic);
    ui().text("The police subdue and arrest the squad.").add();
    if (hostagefreed > 1) {
      ui().text("Your hostages are free.").add();
    } else if (hostagefreed == 1) {
      ui().text("Your hostage is free.").add();
    }
    waitOnOK();
  }
}
