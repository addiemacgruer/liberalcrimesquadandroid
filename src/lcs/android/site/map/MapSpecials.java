package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.creature.Receptive;
import lcs.android.creature.health.Animal;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.SiteEncounter;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Quality;
import lcs.android.items.Armor;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.type.CigarBar;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class MapSpecials {
  private static enum BouncerRejectReason {
    BLOODYCLOTHES,
    CCS,
    DAMAGEDCLOTHES,
    DRESSCODE,
    FEMALE,
    FEMALEISH,
    GUESTLIST,
    NOT_REJECTED,
    NUDE,
    SECONDRATECLOTHES,
    SMELLFUNNY,
    UNDERAGE,
    WEAPONS
  }

  public static void specialBouncerAssessSquad() {
    final Creature sleeperBouncer = specialBouncerGreetSquad();
    if (sleeperBouncer != null) {
      fact("Sleeper " + sleeperBouncer + " smirks and lets the squad in.");
      i.site.currentTile().special = null;
    } else {
      if (i.site.current().renting() == CrimeSquad.CCS) {
        fact("The Conservative scum block the door.");
      } else {
        fact("The bouncer assesses your squad.");
      }
      i.site.currentTile().special = SpecialBlocks.CLUB_BOUNCER_SECONDVISIT;
    }
    i.currentEncounter().printEncounter();
    BouncerRejectReason rejected = BouncerRejectReason.NOT_REJECTED;
    // Size up the squad for entry
    if (sleeperBouncer != null) {
      for (final Creature s : i.activeSquad()) {
        // Wrong clothes? Gone
        if (s.isNaked() && s.type().animal() != Animal.ANIMAL) {
          rejected = BouncerRejectReason.NUDE;
        } else if (s.hasDisguise() == Quality.NONE) {
          rejected = BouncerRejectReason.DRESSCODE;
        } else if (s.getArmor().isBloody()) {
          rejected = BouncerRejectReason.BLOODYCLOTHES;
        } else if (s.getArmor().isDamaged()) {
          rejected = BouncerRejectReason.DAMAGEDCLOTHES;
        } else if (s.getArmor().quality() != Armor.Rating.FIRST) {
          rejected = BouncerRejectReason.SECONDRATECLOTHES;
        } else if (s.weaponCheck() != Quality.POOR) {
          rejected = BouncerRejectReason.WEAPONS;
        } else if (i.site.type().disguisesite()
            && !s.skill().skillCheck(Skill.DISGUISE, CheckDifficulty.CHALLENGING)) {
          rejected = BouncerRejectReason.SMELLFUNNY;
        } else if (s.age() < 18) {
          rejected = BouncerRejectReason.UNDERAGE;
        } else if (i.site.type().isType(CigarBar.class)
            && (s.genderConservative() != Gender.MALE || s.genderLiberal() == Gender.FEMALE)
            && i.issue(Issue.WOMEN).lawLT(Alignment.LIBERAL)) {
          /* Are you passing as a man? Are you skilled enough to pull it off? */
          if (s.genderLiberal() == Gender.FEMALE) {
            /* Not a man by your own definition either */
            rejected = BouncerRejectReason.FEMALE;
          } else if (i.site.type().disguisesite()
              && !s.skill().skillCheck(Skill.DISGUISE, CheckDifficulty.HARD)
              && i.issue(Issue.GAY).law() != Alignment.ELITELIBERAL) {
            /* Not skilled enough to pull it off */
            rejected = BouncerRejectReason.FEMALEISH;
          }
        }
        /* High security in gentleman's club? Gone */
        else if (i.site.type().isType(CigarBar.class) && i.site.current().highSecurity() > 0) {
          rejected = BouncerRejectReason.GUESTLIST;
        } else if (i.site.current().renting() == CrimeSquad.CCS) {
          rejected = BouncerRejectReason.CCS;
        }
      }
      switch (rejected) {
      case CCS:
        ui().text(
            i.rng.choice("\"Can I see... heh heh... some ID?\"",
                "\"Woah... you think you're coming in here?\"", "\"Check out this fool. Heh.\"",
                "\"Want some trouble, dumpster breath?\"",
                "\"You're gonna stir up the hornet's nest, fool.\"",
                "\"Come on, take a swing at me. Just try it.\"",
                "\"You really don't want to fuck with me.\"",
                "\"Hey girly, have you written your will?\"",
                "\"Oh, you're trouble. I *like* trouble.\"",
                "\"I'll bury you in those planters over there.\"",
                "\"Looking to check on the color of your blood?\"")).color(Color.RED).add();
        break;
      case NUDE:
        ui().text(
            i.rng.choice("\"No shirt, no underpants, no service.\"",
                "\"Put some clothes on! That's disgusting.\"",
                "\"No! No, you can't come in naked! God!!\"")).color(Color.RED).add();
        break;
      case UNDERAGE:
        ui().text(
            i.rng.choice("\"Hahaha, come back in a few years.\"", "\"Find some kiddy club.\"",
                "\"You don't look 18 to me.\"", "\"Go back to your treehouse.\"",
                "\"Where's your mother?\"")).color(Color.RED).add();
        break;
      case FEMALE:
        ui().text(
            i.rng.choice("\"Move along ma'am, this club's for men.\"",
                "\"This 'ain't no sewing circle, ma'am.\"",
                "\"Sorry ma'am, this place is only for the men.\"", "\"Where's your husband?\""))
            .color(Color.RED).add();
        break;
      case FEMALEISH:
        ui().text(
            i.rng.choice("\"You /really/ don't look like a man to me...\"",
                "\"Y'know... the \'other\' guys won't like you much.\"",
                "\"Uhh... can't let you in, ma'am. Sir. Whatever.\"")).color(Color.RED).add();
        break;
      case DRESSCODE:
        ui().text(
            i.rng.choice("\"Check the dress code.\"", "\"We have a dress code here.\"",
                "\"I can't let you in looking like that.\"")).color(Color.RED).add();
        break;
      case SMELLFUNNY:
        ui().text(
            i.rng.choice("\"God, you smell.\"", "\"Not letting you in. Because I said so.\"",
                "\"There's just something off about you.\"", "\"Take a shower.\"",
                "\"You'd just harass the others, wouldn't you?\"", "\"Get the "
                    + (!i.freeSpeech() ? "[heck]"
                        : i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL ? "fuck"
                            : "hell") + "out of here.\"")).color(Color.RED).add();
        break;
      case BLOODYCLOTHES:
        ui().text(
            i.rng.choice("\"Good God! What is wrong with your clothes?\"",
                "\"Absolutely not. Clean up a bit.\"",
                "\"This isn't a goth club, bloody clothes don't cut it here.\"",
                "\"Uh, maybe you should wash... replace... those clothes.\"",
                "\"Did you spill something on your clothes?\"",
                "\"Come back when you get the red wine out of your clothes.\"")).color(Color.RED)
            .add();
        break;
      case DAMAGEDCLOTHES:
        ui().text(
            i.rng.choice("\"Good God! What is wrong with your clothes?\"",
                "\"This isn't a goth club, ripped clothes don't cut it here.\"")).color(Color.RED)
            .add();
        break;
      case SECONDRATECLOTHES:
        ui().text(
            i.rng.choice("\"That looks like you sewed it yourself.\"",
                "\"If badly cut clothing is a hot new trend, I missed it.\"")).color(Color.RED)
            .add();
        break;
      case WEAPONS:
        ui().text(
            i.rng.choice("\"No weapons allowed.\"", "\"I can't let you in carrying that.\"",
                "\"I can't let you take that in.\"",
                "\"Come to me armed, and I'll tell you to take a hike.\"",
                "\"Real men fight with fists. And no, you can't come in.\"")).color(Color.RED)
            .add();
        break;
      case GUESTLIST:
      default:
        ui().text("\"This club is by invitation only.\"").color(Color.RED).add();
        break;
      case NOT_REJECTED:
        ui().text(
            i.rng.choice("\"Keep it civil and don't drink too much.\"",
                "\"Let me get the door for you.\"", "\"Ehh, alright, go on in.\"",
                "\"Come on in.\"")).color(Color.GREEN).add();
        break;
      }
      getch();
    } else {
      i.currentEncounter().creatures().remove(0);
    }
    if (rejected != BouncerRejectReason.NOT_REJECTED) {
      i.site.tileAtLocation(i.site.locx, i.site.locy + 1, i.site.locz).flag.add(TileSpecial.LOCKED);
      i.site.tileAtLocation(i.site.locx, i.site.locy + 1, i.site.locz).flag.add(TileSpecial.CLOCK);
    } else {
      i.site.tileAtLocation(i.site.locx, i.site.locy + 1, i.site.locz).flag
          .remove(TileSpecial.DOOR);
    }
    i.currentEncounter().creatures().get(0).receptive(Receptive.HEARD);
  }

  @Nullable public static Creature specialBouncerGreetSquad() {
    Creature sleeperBouncer = null;
    // add a bouncer if there isn't one in the first slot
    i.currentEncounter(new SiteEncounter());
    if (!i.site.alarm() && i.site.current().renting() != CrimeSquad.LCS) {
      if (i.site.current().renting() == CrimeSquad.CCS) {
        i.currentEncounter().makeEncounterCreature("CCS_VIGILANTE");
        i.currentEncounter().makeEncounterCreature("CCS_VIGILANTE");
      } else {
        i.currentEncounter().makeEncounterCreature("BOUNCER");
        i.currentEncounter().makeEncounterCreature("BOUNCER");
      }
    }
    /* do we have a sleeper here? */
    for (final Creature p : i.pool) {
      if (p.base().getNullable() == i.site.current() && p.type() == CreatureType.valueOf("BOUNCER")
          && p.health().alive() && i.rng.chance(3)) {
        sleeperBouncer = p;
        i.currentEncounter().creatures().set(0, p);
        break;
      }
    }
    return sleeperBouncer;
  }

  static void partyrescue() {
    int freeslots = i.activeSquad().size();
    int hostslots = 0;
    for (final Creature p : i.activeSquad()) {
      if (p.health().alive() && p.prisoner().missing()) {
        hostslots++;
      }
    }
    for (final Creature pl : i.pool) {
      if (pl.location().getNullable() == i.site.current() && !pl.hasFlag(CreatureFlag.SLEEPER)) {
        if (i.rng.chance(2) && freeslots > 0) {
          for (Creature p : i.activeSquad()) {
            p = pl;
            p.squad(i.activeSquad());
            p.crime().criminalize(Crime.ESCAPED);
            p.addFlag(CreatureFlag.JUST_ESCAPED);
            break;
          }
          hostslots++;
          freeslots--;
          // clearmessagearea();
          ui().text("You've rescued " + pl.toString() + " from the Conservatives.").add();
          i.activeSquad().printParty();
          getch();
          pl.location(Location.none()).base(i.activeSquad().base().getNullable());
        }
      }
    }
    for (final Creature pl : i.pool) {
      if (pl.location().getNullable() == i.site.current() && !pl.hasFlag(CreatureFlag.SLEEPER)) {
        if (hostslots > 0) {
          for (final Creature p : i.activeSquad()) {
            if (p.health().alive() && p.prisoner().missing()) {
              p.prisoner(pl);
              pl.squad(i.activeSquad());
              pl.crime().criminalize(Crime.ESCAPED);
              pl.addFlag(CreatureFlag.JUST_ESCAPED);
              ui().text("You've rescued " + pl.toString() + " from the Conservatives.").add();
              getch();
              ui().text(
                  pl.toString()
                      + " "
                      + i.rng.choice("was tortured recently", "was beaten severely yesterday",
                          "was on a hunger strike") + "so " + p.toString()
                      + " will have to haul a Liberal.").add();
              pl.location(Location.none());
              pl.base(p.base().getNullable());
              i.activeSquad().printParty();
              getch();
              break;
            }
          }
          hostslots--;
        }
        if (hostslots == 0) {
          break;
        }
      }
    }
    int stillpcount = 0;
    String stillpname = "NameError";
    for (final Creature pl : i.pool) {
      if (pl.location().getNullable() == i.site.current() && !pl.hasFlag(CreatureFlag.SLEEPER)) {
        stillpcount++;
        if (stillpcount == 1) {
          stillpname = pl.toString();
        }
      }
    }
    if (stillpcount == 1) {
      ui().text("There's nobody left to carry " + stillpname + ".").color(Color.YELLOW).add();
      ui().text("You'll have to come back later.").add();
      getch();
    } else if (stillpcount > 1) {
      ui().text("There's nobody left to carry the others.").color(Color.YELLOW).add();
      ui().text("You'll have to come back later.").add();
      getch();
    }
  }
}
