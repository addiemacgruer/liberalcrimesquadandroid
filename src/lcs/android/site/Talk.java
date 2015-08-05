package lcs.android.site;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.EnumSet;
import java.util.Iterator;

import lcs.android.R;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.creature.Receptive;
import lcs.android.creature.health.Animal;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.Date;
import lcs.android.daily.Recruit;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Ledger;
import lcs.android.law.Crime;
import lcs.android.news.NewsCherryBusted;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.map.TileSpecial;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.ArmsDealer;
import lcs.android.util.Color;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault class Talk {
  @Nullable private static Creature last = null;

  static boolean talk(final Creature a, final Creature t) {
    // BLUFFING
    setView(R.layout.generic);
    if ((i.site.alarm() || i.site.current().lcs().siege.siege) && t.enemy()) {
      return siteAlarmTalking(a, t);
    }
    if (t.type().ofType("GUARDDOG") && t.alignment() != Alignment.LIBERAL) {
      return talkToDog(t);
    }
    do {
      int c = 'a';
      ui().text(format("%1$s talks to %2$s:", a.toString(), t.longDescription())).bold().add();
      final String nudey = a.isNaked() && a.type().animal() != Animal.ANIMAL ? getString(" (while naked)")
          : "";
      ui(R.id.gcontrol).button('a')
          .text(getString("Strike up a conversation about politics") + nudey + ".").add();
      if (t.canDate(a)) {
        ui(R.id.gcontrol).button('b').text(getString("Drop a pickup line") + nudey + ".").add();
      } else {
        ui(R.id.gcontrol).button().text(getString("Drop a pickup line") + nudey + ".").add();
      }
      ui(R.id.gcontrol).button('c')
          .text(getString("On second thought, don't say anything") + nudey + ".").add();
      if (t.type().ofType("LANDLORD") && i.site.current().renting() == CrimeSquad.NO_ONE) {
        ui(R.id.gcontrol).button('d').text(getString("Rent a room") + nudey + ".").add();
      } else if (t.type().ofType("LANDLORD") && i.site.current().renting() != CrimeSquad.NO_ONE) {
        ui(R.id.gcontrol).button('d').text(getString("Stop renting a room") + nudey + ".").add();
      }
      if (t.type().ofType("GANGMEMBER") || t.type().ofType("MERC")) {
        ui(R.id.gcontrol).button('d').text(getString("Buy weapons") + nudey + ".").add();
      }
      c = getch();
      clearChildren(R.id.gcontrol);
      if (c == 'a') {
        return talkAboutIssues(a, t, t);
      } else if (c == 'b' && t.canDate(a)) {
        return dropAPickupLine(a, t);
      } else if (c == 'c') {
        return false;
      } else if (t.type().ofType("LANDLORD") && i.site.current().renting() == CrimeSquad.NO_ONE
          && c == 'd') {
        return startRenting(a, t);
      } else if (t.type().ofType("LANDLORD") && i.site.current().renting() != CrimeSquad.NO_ONE
          && c == 'd') {
        return stopRenting(a, t);
      } else if (t.type().ofType("GANGMEMBER") || t.type().ofType("MERC") && c == 'd') {
        return buyWeapons(a, t);
      }
    } while (true);
  }

  private static void bluff(final Creature a, final Creature t) {
    if (i.site.current().lcs().siege.siege) {
      switch (i.site.current().lcs().siege.siegetype) {
      case POLICE:
        ui().text(format("%1$s pretends to be part of a police raid.", a.toString())).add();
        break;
      case CIA:
        ui().text(format("%1$s pretends to be a Secret Agent.", a.toString())).add();
        break;
      case CCS:
      case HICKS:
        switch (i.rng.nextInt(2)) {
        default:
        case 0:
          ui().text(
              format("%1$s pretends to be Mountain like Patrick Swayze in Next of Kin.",
                  a.toString())).add();
          break;
        case 1:
          ui().text(format("%1$s squeals like Ned Beatty in Deliverance.", a.toString())).add();
          break;
        }
        break;
      case CORPORATE:
        ui().text(format("%1$s pretends to be a mercenary.", a.toString())).add();
        break;
      case FIREMEN:
        ui().text(format("%1$s lights a match and throws it on the ground.", a.toString())).add();
        if ((!i.site.currentTile().flag.contains(TileSpecial.FIRE_END)
            || !i.site.currentTile().flag.contains(TileSpecial.FIRE_PEAK)
            || !i.site.currentTile().flag.contains(TileSpecial.FIRE_START) || !i.site.currentTile().flag
            .contains(TileSpecial.DEBRIS)) && i.rng.chance(10)) {
          i.site.currentTile().flag.add(TileSpecial.FIRE_START);
          ui().text("The carpet smolders, then bursts into flame.").add();
          ui().text("Perhaps that was a bad idea…").add();
        }
        break;
      default:
        break;
      }
    } else {
      ui().text(format("%1$s talks like a Conservative and pretends to belong here.")).add();
    }
    boolean fooled = true;
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.health().alive() && e.enemy()) {
        if (e.skill().getAttribute(Attribute.WISDOM, true) > 10) {
          fooled = a.skill().skillCheck(Skill.DISGUISE, CheckDifficulty.CHALLENGING);
        } else {
          fooled = a.skill().skillCheck(Skill.DISGUISE, CheckDifficulty.AVERAGE);
        }
        if (!fooled) {
          break;
        }
      }
    }
    a.skill().train(Skill.DISGUISE, 20);
    if (!fooled) {
      if (t.type().ofType("HICK")) {
        ui().text(format("But %s weren't born yesterday.", t.toString())).add();
      } else if (!i.freeSpeech()) {
        ui().text(format("%1$s is not fooled by that [act].", t.toString())).add();
      } else {
        ui().text(format("%1$s is not fooled by that crap.", t.toString())).add();
      }
      getch();
    } else {
      ui().text("The Enemy is fooled and departs.").color(Color.GREEN).add();
      getch();
      for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei.hasNext();) {
        final Creature e = ei.next();
        if (e.enemy() && e.health().alive()) {
          ei.remove();
        }
      }
    }
  }

  private static boolean buyWeapons(final Creature a, final Creature tk) {
    clearChildren(R.id.gcontrol);
    ui().text(format("%s says,", a.toString())).add();
    ui().text("\"Hey, I need a gun.\"").color(Color.GREEN).add();
    if (a.isNaked() && a.type().animal() != Animal.ANIMAL) {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"Jesus…\"").color(Color.BLUE).add();
      return true;
    }
    if (a.getArmor().isPolice()) {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"I don't sell guns, officer.\"").color(Color.BLUE).add();
      getch();
      return true;
    }
    if (i.site.alarm()) {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"We can talk when things are calm.\"").color(Color.BLUE).add();
      getch();
      return true;
    }
    if (i.site.current().type().canBuyGuns()) {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"What exactly do you need?\"").color(Color.BLUE).add();
      getch();
    } else {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"Uhhh… not a good place for this.\"").color(Color.BLUE).add();
      getch();
      return true;
    }
    ArmsDealer.armsdealer(i.site.current());
    return true;
  }

  private static boolean dropAPickupLine(final Creature lcs, final Creature tk) {
    clearChildren(R.id.gcontrol);
    int line;
    if (!i.freeSpeech()) {
      final String[] pickups = stringArray(R.array.hayspickups);
      line = i.rng.nextInt(pickups.length);
      speak(lcs, '"' + pickups[line] + '"');
    } else {
      final String[] pickups = stringArray(R.array.pickups);
      line = i.rng.nextInt(pickups.length);
      speak(lcs, '"' + pickups[line] + '"');
    }
    final int difficulty = tk.type().seductionDifficulty().value();
    final boolean succeeded = (lcs.skill().skillCheck(Skill.SEDUCTION, difficulty));
    if (tk.type().animal() == Animal.ANIMAL
        && i.issue(Issue.ANIMALRESEARCH).law() != Alignment.ELITELIBERAL
        || tk.type().animal() == Animal.TANK) {
      if (tk.type().idName().equals("TANK")) {
        ui().text(format("%s shakes its turret a firm 'no'.", tk.toString())).add();
      } else if (tk.type().idName().equals("GUARDDOG")) {
        speak(tk, randomString(R.array.dogkb));
        tk.alignment(Alignment.CONSERVATIVE);
        tk.receptive(Receptive.HEARD);
      } else {
        ui().text(format("%s doesn't quite pick up on the subtext.", tk.toString())).add();
      }
      waitOnOK();
      return true;
    }
    lcs.skill().train(Skill.SEDUCTION, i.rng.nextInt(5) + 2);
    if (lcs.getArmor().isPolice() && tk.type().ofType("PROSTITUTE")) {
      speak(tk, "Dirty. You know that's illegal, officer.");
      getch();
      tk.receptive(Receptive.HEARD);
    } else if (succeeded) {
      if (!i.freeSpeech()) {
        speak(tk, '"' + stringArray(R.array.hayspickedups)[line] + '"');
      } else {
        speak(tk, '"' + stringArray(R.array.pickedups)[line] + '"');
      }
      ui().text(format("%1$s and %2$s make plans for tonight.", lcs.toString(), tk.toString()))
          .add();
      if (tk.type().ofType("PRISONER")) {
        ui().text(format("(and %s breaks for the exit.)", tk.toString())).add();
        tk.crime().criminalize(Crime.ESCAPED);
      }
      getch();
      Date newd = null;
      for (final Date d : i.dates) {
        if (d.dater == lcs) {
          newd = d;
          break;
        }
      }
      if (newd == null) {
        newd = new Date(lcs);
        i.dates.add(newd);
      }
      tk.location(lcs.location().get());
      tk.base(lcs.base().get());
      newd.dates.add(tk);
      i.currentEncounter().creatures().remove(tk);
    } else {
      if (tk.type().ofType("CORPORATE_CEO")) {
        if (lcs.genderLiberal() != Gender.MALE) {
          speak(tk, "\"I'm a happily married man, sweetie.\"");
        } else {
          speak(tk, "\"This ain't Brokeback Mountain, son.\"");
        }
      } else {
        speak(tk, '"' + randomString(R.array.knockbacks) + '"');
        ui().text(randomString(R.array.kbmotions)).add();
      }
      tk.receptive(Receptive.HEARD);
    }
    return true;
  }

  /** @param string
   * @return */
  private static String getString(final String string) {
    return string;
  }

  private static void intimidate(final Creature a) {
    ui().text(a.toString() + ":").add();
    switch (i.rng.nextInt(3)) {
    default:
    case 0: { /* Formatting the i.hs.slogan so that it always has quotes around it and punctuation */
      if (i.score.slogan.length() == 0) {
        ui().text("\"We need a slogan!\"").color(Color.GREEN).add();
      } else {
        final StringBuilder sb = new StringBuilder();
        if (i.score.slogan.charAt(0) != '"') {
          sb.append('"');
        }
        sb.append(i.score.slogan);
        final char last = i.score.slogan.charAt(i.score.slogan.length() - 1);
        if (last != '"' && last != '!' && last != '.' && last != '?') {
          sb.append('!');
        }
        if (last != '"') {
          sb.append('"');
        }
        ui().text(sb.toString()).color(Color.GREEN).add();
      }
      i.siteStory.claimed(true);
      break;
    }
    case 1:
      ui().text("Die you Conservative dogs!").color(Color.GREEN).add();
      break;
    case 2:
      ui().text("We're the Liberal Crime Squad").color(Color.GREEN).add();
      break;
    case 3:
      ui().text("Praying won't help you now!").color(Color.GREEN).add();
      break;
    }
    // getch();
    for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei.hasNext();) {
      final Creature e = ei.next();
      if (e.health().alive() && e.enemy()) {
        final int attack = a.juice() / 50 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 10;
        final int defense = e.skill().attributeRoll(Attribute.WISDOM);
        if (attack > defense) {
          if (e.type().isPolice()) {
            if (i.rng.likely(3)) {
              continue;
            }
          }
          ui().text(format(randomString(R.array.escapes), e.toString())).add();
          ei.remove();
          a.addJuice(2, 200); // Instant juice!
        }
      }
    }
    getch();
  }

  private static boolean siteAlarmTalking(final Creature a, final Creature t) {
    ui().text(format("%1$s talks to %2$s", a.toString(), t.toString())).bold().add();
    int c = 0;
    int hostages = 0;
    int weaponhostage = 0;
    final boolean cop = t.type().isPolice();
    for (final Creature p : i.activeSquad()) {
      if (p.prisoner().exists() && p.prisoner().get().health().alive()
          && p.prisoner().get().enemy()) {
        hostages++;
        if (p.weapon().isArmed() && p.weapon().weapon().canThreatenHostages()) {
          weaponhostage++;
        }
      }
    }
    ui(R.id.gcontrol).button('a').text("Intimidate").add();
    if (hostages > 0) {
      ui(R.id.gcontrol).button('b').text("Threaten hostages").add();
    } else {
      ui(R.id.gcontrol).button().text("Threaten hostages").add();
    }
    if (t.receptive() == Receptive.ANGERED) {
      ui(R.id.gcontrol).button('c').text("Bluff").add();
    } else {
      ui(R.id.gcontrol).button().text("Bluff").add();
    }
    if (cop) {
      ui(R.id.gcontrol).button('d').text("Surrender to police").add();
    } else {
      ui(R.id.gcontrol).button().text("Surrender to police").add();
    }
    while (true) {
      c = getch();
      if (c == 'a') {
        break;
      }
      if (c == 'b' && hostages > 0) {
        break;
      }
      if (c == 'c' && t.receptive() != Receptive.ANGERED) {
        break;
      }
      if (c == 'd' && cop) {
        break;
      }
    }
    clearChildren(R.id.gcontrol);
    if (c == 'a') {
      intimidate(a);
    } else if (c == 'b') {
      threatenHostages(a, t, hostages, weaponhostage);
    } else if (c == 'c') {
      bluff(a, t);
    } else {
      surrenderToPolice();
    }
    return true;
  }

  private static void speak(final Creature creature, final String words) {
    ui().text(words + (last != creature ? " says " + creature : "") + ".")
        .color(creature.alignment().color()).add();
    last = creature;
  }

  private static boolean startRenting(final Creature liberal, final Creature encounter) {
    int c;
    clearChildren(R.id.gcontrol);
    ui().text(format("%s says,", liberal.toString())).add();
    ui().text("\"I'd like to rent a room.\"").color(Color.GREEN).add();
    if (liberal.isNaked() && liberal.type().animal() != Animal.ANIMAL) {
      ui().text(format("%s responds,", encounter.toString())).add();
      ui().text("\"Put some clothes on before I call the cops.\"").color(Color.BLUE).add();
      getch();
      return true;
    }
    int rent = i.site.current().basicRent();
    ui().text(format("%s responds,", encounter.toString())).add();
    ui().text(format("\"It'll be $%s a month", rent)).color(Color.BLUE).add();
    ui().text(format("I'll need $%s now as a security deposit.\"", rent)).add();
    // if (i.ledger.get_funds() < rent)
    // ;
    ui(R.id.gcontrol).button('a').text("Accept.").add();
    ui(R.id.gcontrol).button('b').text("Decline.").add();
    ui(R.id.gcontrol).button('c').text("Threaten the landlord.").add();
    c = getch();
    clearChildren(R.id.gcontrol);
    if (c == 'a' && i.ledger.funds() >= rent) {
      clearChildren(R.id.gcontrol);
      ui().text(format("%s says,", liberal.toString())).add();
      ui().text("\"I'll take it.\"").color(Color.GREEN).add();
      ui().text(format("%s responds,", encounter.toString())).add();
      ui().text("\"Rent is due by the third of every month.").color(Color.BLUE).add();
      ui().text("We'll start next month.\"").color(Color.BLUE).add();
      ui().text("<turns away>").add();
      waitOnOK();
      i.ledger.subtractFunds(rent, Ledger.ExpenseType.RENT);
      i.site.current().renting(CrimeSquad.LCS);
      i.site.current().rent(rent);
      i.site.current().lcs().newRental = true;
      i.activeSquad().base(i.site.current());
      return true;
    }
    if (c == 'b') {
      clearChildren(R.id.gcontrol);
      ui().text(format("%s says,", liberal.toString())).add();
      ui().text("\"Whoa, I was looking for something cheaper.\"").color(Color.GREEN).add();
      ui().text(format("%s responds,", encounter.toString())).add();
      ui().text("\"Not my problem…\"").color(Color.BLUE).add();
      ui().text("<turns away>").add();
      getch();
      return true;
    }
    if (c == 'c') {
      Creature armed_liberal = null;
      for (final Creature p : i.activeSquad()) {
        if (p.weapon().weapon().isThreatening()) {
          armed_liberal = p;
          break;
        }
      }
      if (armed_liberal != null) {
        ui().text(
            format("%1$s brandishes the %2$s.", armed_liberal.toString(), armed_liberal.weapon()
                .weapon().shortName())).add();
      }
      ui().text(format("%s says,", liberal.toString())).add();
      ui().text("\"What's the price for the Liberal Crime Squad?\"").color(Color.GREEN).add();
      final int roll = liberal.skill().skillRoll(Skill.PERSUASION);
      int difficulty = CheckDifficulty.FORMIDABLE.value();
      if (i.newscherrybusted == NewsCherryBusted.UNKNOWN) {
        difficulty += 6;
      }
      if (armed_liberal == null) {
        difficulty += 6;
      }
      if (roll < difficulty - 1) {
        ui().text(format("%s responds,", encounter.toString())).add();
        ui().text("\"I think you'd better leave.\"").color(Color.BLUE).add();
        ui().text("<crosses arms>").add();
        getch();
        encounter.receptive(Receptive.HEARD);
        return true;
      }
      ui().text(format("%s responds,", encounter.toString())).add();
      ui().text("\"Jesus… it's yours…\"").color(Color.BLUE).add();
      getch();
      // Either he calls the cops...
      if (roll < difficulty) {
        for (final Creature p : i.activeSquad()) {
          p.crime().criminalize(Crime.EXTORTION);
        }
        i.site.current().lcs().siege.timeUntilLocated = 2;
        rent = 10000000; /* Yeah he's kicking you out next month */
      } else {
        rent = 0;
      }
      i.site.current().rent(rent).renting(CrimeSquad.LCS).lcs().newRental = true;
      i.activeSquad().base(i.site.current());
      return true;
    }
    return true;
  }

  private static boolean stopRenting(final Creature a, final Creature tk) {
    clearChildren(R.id.gcontrol);
    ui().text(format("%s says,", a.toString())).add();
    ui().text("\"I'd like cancel my room.\"").color(Color.GREEN).add();
    if (a.isNaked() && a.type().animal() != Animal.ANIMAL) {
      ui().text(format("%s responds,", tk.toString())).add();
      ui().text("\"Put some clothes on before I call the cops.\"").color(Color.BLUE).add();
      waitOnOK();
      return true;
    }
    ui().text(format("%s responds,", tk.toString())).add();
    ui().text("\"Alright. Please clear out your room.\"").color(Color.BLUE).add();
    ui().text("<Your possessions at this location have been moved to the shelter.>").add();
    i.site.current().renting(CrimeSquad.NO_ONE);
    // MOVE ALL ITEMS AND SQUAD MEMBERS
    final Location hs = AbstractSiteType.type("RESIDENTIAL_SHELTER").getLocation();
    for (final Creature p : i.pool) {
      if (p.location().getNullable() == i.site.current()) {
        p.location(hs);
      }
      if (p.base().exists() && p.base().get() == i.site.current()) {
        p.base(hs);
      }
    }
    hs.lcs().loot.addAll(i.site.current().lcs().loot);
    i.site.current().lcs().loot.clear();
    i.site.current().lcs().compoundWalls = EnumSet.noneOf(Compound.class);
    i.site.current().lcs().compoundStores = 0;
    i.site.current().lcs().frontBusiness = null;
    getch();
    return true;
  }

  private static void surrenderToPolice() {
    ui().text("The police arrest the Squad.").add();
    getch();
    final int stolen = Filter.count(i.activeSquad().loot(), Filter.IS_LOOT);
    for (final Creature j : i.activeSquad()) {
      j.crime().incrementCrime(Crime.THEFT, stolen);
      j.captureByPolice(Crime.TERRORISM); // TODO check which crime should be here.
    }
    i.site.current().lcs().siege.siege = false;
  }

  private static boolean talkAboutIssues(final Creature a, final Creature t, final Creature tk) {
    clearChildren(R.id.gcontrol);
    speak(a, "\"Do you want to hear something disturbing?\"");
    final boolean interested = tk.isTalkReceptive()
        || a.skill().skillCheck(Skill.PERSUASION, CheckDifficulty.AVERAGE);
    if (tk.type().animal() == Animal.ANIMAL && tk.alignment() != Alignment.LIBERAL
        || tk.type().animal() == Animal.TANK) {
      if (tk.type().idName().equals("TANK")) {
        ui().text(format("%s rumbles disinterestedly.", tk.toString())).add();
      } else if (tk.type().idName().equals("GUARDDOG")) {
        ui().text(format("%s barks.", tk.toString())).add();
      } else {
        ui().text(format("%s doesn't understand.", tk.toString())).add();
      }
    } else if (tk.type() != CreatureType.valueOf("PRISONER") && interested) {
      speak(tk, "\"What?\"");
      final Issue lw = i.rng.randFromArray(Issue.values());
      boolean succeeded = false;
      boolean you_are_stupid = false;
      boolean issue_too_liberal = false;
      if (!a.skill().attributeCheck(Attribute.INTELLIGENCE, CheckDifficulty.EASY.value())) {
        you_are_stupid = true;
      } else if (i.issue(lw).law() == Alignment.ELITELIBERAL
          && i.newscherrybusted != NewsCherryBusted.UNKNOWN) {
        issue_too_liberal = true;
      }
      if (you_are_stupid) {
        speak(a, lw.stupidOpinion());
      } else if (issue_too_liberal) {
        speak(a, lw.moderatelyStupidOpinion());
      } else {
        speak(a, lw.eruditeOpinion());
      }
      int difficulty1 = CheckDifficulty.VERYEASY.value();
      if (tk.alignment() == Alignment.CONSERVATIVE) {
        difficulty1 += 7;
      }
      if (!tk.isTalkReceptive()) {
        difficulty1 += 7;
      }
      if (you_are_stupid) {
        difficulty1 += 5;
      }
      if (issue_too_liberal) {
        difficulty1 += 5;
      }
      if (a.isNaked() && a.type().animal() != Animal.ANIMAL) {
        difficulty1 += 5;
      }
      succeeded = a.skill().skillCheck(Skill.PERSUASION, difficulty1);
      // Prisoners never accept to join you, you must
      // liberate them instead
      if (succeeded && tk.type() != CreatureType.valueOf("PRISONER")) {
        if (tk.type().ofType("MUTANT") && tk.skill().getAttribute(Attribute.INTELLIGENCE, true) < 3) {
          speak(tk, "\"Aaaahhh…\"");
        } else {
          final String[] issued = stringArray(R.array.issued);
          if (i.rng.nextInt(issued.length + 2) < issued.length) {
            speak(tk, i.rng.randFromArray(issued));
          } else {
            switch (i.rng.nextInt(2)) {
            default:
            case 0:
              speak(tk, "Oh, really?");
              speak(a, "Yeah, really!");
              break;
            case 1:
              speak(tk, "You got anything to smoke on you?");
              ui().text("*cough*").color(Color.WHITE).add();
              break;
            }
          }
        }
        ui().text(
            format("After more discussion, %s agrees to come by later tonight.", tk.toString()))
            .add();
        final Recruit newrst = new Recruit(tk, a);
        i.recruits.add(newrst);
        i.currentEncounter().creatures().remove(tk);
      } else {
        if (tk.type().ofType("MUTANT") && tk.skill().getAttribute(Attribute.INTELLIGENCE, true) < 3) {
          speak(tk, "\"Ugh.  Pfft.\"");
        } else if (tk.alignment() == Alignment.CONSERVATIVE && you_are_stupid) {
          if (tk.type().ofType("GANGUNIT")) {
            speak(tk, "\"Do you want me to arrest you?\"");
          } else if (tk.type().ofType("DEATHSQUAD")) {
            speak(tk, "\"If you don't shut up, I'm going to shoot you.\"");
          } else {
            speak(tk, randomString(R.array.goawayhippie));
          }
        } else if (tk.alignment() != Alignment.LIBERAL
            && tk.skill().attributeCheck(Attribute.WISDOM, CheckDifficulty.AVERAGE.value())) {
          speak(tk, lw.eruditeConservativeResponse());
        } else {
          speak(tk, "\"Whatever.\"");
        }
        ui().text("<turns away>").add();
        t.receptive(Receptive.HEARD);
      }
    } else {
      if (tk.type().ofType("PRISONER")) {
        if (tk.alignment() == Alignment.LIBERAL) {
          speak(tk, "\"Now's not the time!\"");
        } else {
          speak(tk, "\"Leave me alone.\"");
        }
      } else {
        speak(tk, "\"No.\"");
      }
      ui().text("<turns away>").add();
      t.receptive(Receptive.HEARD);
    }
    // waitOnOK();
    return true;
  }

  private static boolean talkToDog(final Creature tk) {
    // Find most Heartful Liberal
    final Creature bestp = Filter.best(i.activeSquad(), Filter.attribute(Attribute.HEART, true))
        .get();
    // Say something unbelievably hippie
    final int statement = i.rng.nextInt(stringArray(R.array.lovedogs).length);
    if (bestp.skill().getAttribute(Attribute.HEART, true) >= 20) {
      speak(bestp, stringArray(R.array.lovedogs)[statement]);
    } else {
      speak(bestp, stringArray(R.array.likedogs)[statement]);
    }
    if (bestp.skill().getAttribute(Attribute.HEART, true) >= 20) {
      speak(tk, stringArray(R.array.loveddog)[statement]);
      tk.alignment(Alignment.LIBERAL);
    } else {
      speak(tk, stringArray(R.array.likeddog)[statement]);
      tk.receptive(Receptive.HEARD);
    }
    return true;
  }

  private static void threatenHostages(final Creature liberal, final Creature enemy,
      final int hostages, final int weaponhostage) {
    int c;
    switch (i.rng.nextInt(6)) {
    default:
      speak(liberal, "\"Back off or the hostage dies!\"");
      break;
    case 1:
      speak(liberal, "\"Don't push the LCS!\"");
      i.siteStory.claimed(true);
      break;
    case 2:
      speak(liberal, "\"Hostage says you better leave!\"");
      break;
    case 3:
      speak(liberal, "\"I'll do it! I'll kill this one!\"");
      break;
    case 4:
      speak(liberal, "\"You gonna tell the family you pushed me?!\"");
      break;
    case 5:
      if (!i.freeSpeech()) {
        speak(liberal, "\"Don't [play] with me!\"");
      } else {
        speak(liberal, "\"Don't fuck with me!\"");
      }
      break;
    }
    i.site.crime(i.site.crime() + 5);
    i.activeSquad().criminalizeParty(Crime.KIDNAPPING);
    liberal.addJuice(-2, -10); // DE-juice for this shit
    boolean noretreat = false;
    if (weaponhostage > 0) {
      for (final Creature e : i.currentEncounter().creatures()) {
        if (e.health().alive() && e.enemy() && e.health().blood() > 70) {
          if ((e.type().ofType("DEATHSQUAD") || e.type().ofType("SOLDIER")
              || e.type().ofType("HARDENED_VETERAN") || e.type().ofType("CCS_ARCHCONSERVATIVE")
              || e.type().ofType("AGENT") || e.type().ofType("MERC") || e.type().ofType("COP")
              || e.type().ofType("GANGUNIT") || e.type() == CreatureType.valueOf("SWAT"))
              && i.rng.likely(5)) {
            if (e.alignment() != Alignment.CONSERVATIVE) {
              speak(e, '"' + randomString(R.array.moderatehostageresolution) + '"');
            } else if ((e.type().ofType("DEATHSQUAD") || e.type().ofType("AGENT")
                || e.type().ofType("MERC") || e.type() == CreatureType.valueOf("GANGUNIT"))
                && e.alignment() == Alignment.CONSERVATIVE) {
              speak(e, '"' + randomString(R.array.deathsquadhostageresolution) + '"');
            } else {
              switch (i.rng.nextInt(5)) {
              default:
              case 0:
                if (hostages > 1) {
                  speak(e, "\"Release your hostages, and nobody gets hurt.\"");
                } else {
                  speak(e, "\"Let the hostage go, and nobody gets hurt.\"");
                }
                break;
              case 1:
                speak(e, "\"You got about five seconds to back down.\"");
                break;
              case 2:
                speak(e, "\"You want to do this the hard way?\"");
                break;
              case 3:
                speak(e, "\"Big mistake.\"");
                break;
              case 4:
                speak(e, "\"Release them, and I'll let you go.\"");
                break;
              }
            }
            noretreat = true;
            break;
          }
        }
      }
    }
    if (!noretreat) {
      ui().text("The ploy works! The Conservatives back off.").add();
      for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei.hasNext();) {
        final Creature e = ei.next();
        if (e.health().alive() && e.enemy()) {
          if (e.health().alive() && e.alignment().trueOrdinal() <= -1) {
            ei.remove();
          }
        }
      }
    } else {
      ui().text(format("How should %s respond?", liberal.toString())).add();
      if (hostages > 1) {
        ui(R.id.gcontrol).button('a').text("Execute a hostage").add();
      } else {
        ui(R.id.gcontrol).button('a').text("Execute the hostage").add();
      }
      if (hostages > 1) {
        ui(R.id.gcontrol).button('b').text("Offer to trade the hostages for freedom").add();
      } else {
        ui(R.id.gcontrol).button('b').text("Offer to trade the hostage for freedom").add();
      }
      while (true) {
        c = getch();
        if (c == 'a' || c == 'b') {
          break;
        }
      }
      clearChildren(R.id.gcontrol);
      if (c == 'a') {
        Creature executer = i.activeSquad().member(0);
        if (liberal.prisoner().exists()) {
          executer = liberal;
        } else {
          for (final Creature p : i.activeSquad()) {
            if (p.prisoner().exists() && p.prisoner().get().health().alive()
                && p.prisoner().get().enemy()) {
              executer = p;
              break;
            }
          }
        }
        if (executer.weapon().weapon().isRanged()
            && executer.weapon().weapon().get_ammoamount() > 0) {
          ui().text("BLAM!").color(Color.RED).add();
          executer.weapon().weapon().decreaseAmmo(1); // What if it
          // doesn't
          // use ammo?
          // -XML
        } else {
          ui().text("CRUNCH!").color(Color.RED).add();
        }
        ui().text(
            format("%1$s drops %2$s's body.", executer.toString(), executer.prisoner().get()
                .toString())).add();
        executer.prisoner().get().health().die();
        liberal.addJuice(-5, -50);
        // DE-juice for this shit
        if (executer.prisoner().get().type().ofType("CORPORATE_CEO")
            || executer.prisoner().get().type().ofType("RADIOPERSONALITY")
            || executer.prisoner().get().type().ofType("NEWSANCHOR")
            || executer.prisoner().get().type().ofType("SCIENTIST_EMINENT")
            || executer.prisoner().get().type().ofType("JUDGE_CONSERVATIVE")) {
          i.site.crime(i.site.crime() + 30);
        }
        executer.prisoner().get().dropLoot(i.groundLoot());
        executer.prisoner(null);
        if (hostages > 1 && i.rng.likely(2)) {
          if (i.freeSpeech()) {
            speak(enemy, "\"Fuck! \"");
          } else {
            speak(enemy, "\"[No!] \"");
          }
          switch (i.rng.nextInt(5)) {
          default:
          case 0:
            speak(enemy, "Okay, okay, you win!\"");
            break;
          case 1:
            speak(enemy, "Don't shoot!\"");
            break;
          case 2:
            speak(enemy, "Do you even care?!\"");
            break;
          case 3:
            speak(enemy, "Heartless!\"");
            break;
          case 4:
            speak(enemy, "It's not worth it!\"");
            break;
          }
          for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei
              .hasNext();) {
            final Creature e = ei.next();
            if (e.enemy() && e.health().alive()) {
              ei.remove();
            }
          }
        }
      } else if (c == 'b') {
        ui().text(liberal.toString() + ":").add();
        switch (i.rng.nextInt(5)) {
        default:
        case 0:
          if (hostages > 1) {
            speak(liberal, "\"Back off and we'll let the hostages go.\"");
          } else {
            speak(liberal, "\"Back off and the hostage goes free.\"");
          }
          break;
        case 1:
          speak(liberal, "\"Freedom for freedom, understand?\"");
          break;
        case 2:
          speak(liberal, "\"Let me go in peace, okay?\"");
          break;
        case 3:
          speak(liberal, "\"Let's make a trade, then.\"");
          break;
        case 4:
          speak(liberal, "\"I just want out of here, yeah?\"");
          break;
        }
        if ((enemy.type().ofType("DEATHSQUAD") || enemy.type().ofType("AGENT")
            || enemy.type().ofType("MERC") || enemy.type() == CreatureType.valueOf("GANGUNIT"))
            && i.rng.likely(2) && enemy.alignment() == Alignment.CONSERVATIVE) {
          switch (i.rng.nextInt(5)) {
          default:
          case 0:
            speak(enemy, "\"Do I look like a loving person?\"");
            break;
          case 1:
            speak(enemy, "\"You don't take a hint, do you?\"");
            break;
          case 2:
            speak(enemy, "\"I'm doing the world a favor.\"");
            break;
          case 3:
            speak(enemy, "\"That's so pathetic…\"");
            break;
          case 4:
            speak(enemy, "\"It's a deal.\"");
            break;
          }
        } else {
          switch (i.rng.nextInt(4)) {
          default:
          case 0:
            speak(enemy, "\"Right. Let's do it.\"");
            break;
          case 1:
            speak(enemy, "\"No further conditions.\"");
            break;
          case 2:
            speak(enemy, "\"Let them go, and we're done.\"");
            break;
          case 3:
            speak(enemy, "\"No tricks, okay?\"");
            break;
          }
          for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei
              .hasNext();) {
            final Creature e = ei.next();
            if (e.enemy() && e.health().alive()) {
              ei.remove();
            }
          }
          i.activeSquad().juice(15, 200);
          /* Instant juice for successful hostage negotiation */
          if (hostages > 1) {
            ui().text("The squad releases all hostages in the trade.").add();
          } else {
            ui().text("The squad releases the hostage in the trade.").add();
          }
          for (final Creature p : i.activeSquad()) {
            if (p.prisoner().exists() && p.prisoner().get().enemy()) {
              p.prisoner(null);
            }
          }
        }
      } else {
        ui().text(format("%s isn't interested in your pathetic threats.", enemy.toString())).add();
      }
    }
    waitOnOK();
  }
}
