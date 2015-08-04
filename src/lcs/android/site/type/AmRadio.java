package lcs.android.site.type;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.law.Crime;
import lcs.android.politics.Issue;
import lcs.android.site.Alienation;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "MEDIA_AMRADIO") public @NonNullByDefault class AmRadio extends AbstractSiteType {
  @Override public String alarmResponseString() {
    return ": ANGRY MOB RESPONDING";
  }

  @Override public String carChaseCar() {
    return "VEHICLE_PICKUP";
  }

  @Override public String carChaseCreature() {
    return "HICK";
  }

  @Override public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 3 + 1) + 1;
  }

  @Override public int carChaseMax() {
    return 18;
  }

  @Override public String ccsSiteName() {
    return "Public Radio Station.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.RADIO_BROADCASTSTUDIO;
  }

  @Override public void generateName(final Location l) {
    l.setName("AM Radio Station");
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", known for its Extreme Conservative Bias.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.AMRADIO, Issue.FREESPEECH, Issue.GAY, Issue.ABORTION,
        Issue.CIVILRIGHTS };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(20)) {
      return "LOOT_AMRADIOFILES";
    } else if (i.rng.chance(4)) {
      return "LOOT_MICROPHONE";
    } else if (i.rng.chance(3)) {
      return "LOOT_PDA";
    } else if (i.rng.chance(2)) {
      return "LOOT_CELLPHONE";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  @Override public String siegeUnit() {
    return "HICK";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  /* run a radio broadcast */
  public static boolean radioBroadcast() {
    i.site.alarm(true);
    final int enemy = i.currentEncounter().enemyCount();
    if (enemy > 0) {
      ui().text("The Conservatives in the room hurry the Squad, so the broadcast never happens.")
          .add();
      getch();
      return false;
    }
    i.activeSquad.criminalizeParty(Crime.DISTURBANCE);
    Issue viewhit = i.rng.randFromArray(Issue.values());
    switch (viewhit) {
    case GAY:
      ui().text("The Squad takes control of the microphone and discusses homosexual rights.").add();
      break;
    case DEATHPENALTY:
      ui().text("The Squad takes control of the microphone and examines the death penalty.").add();
      break;
    case TAX:
      ui().text("The Squad takes control of the microphone and discusses the tax code.").add();
      break;
    case NUCLEARPOWER:
      ui().text("The Squad takes control of the microphone and runs down nuclear power.").add();
      break;
    case ANIMALRESEARCH:
      ui().text(
          "The Squad takes control of the microphone and discusses the horrors of animal research.")
          .add();
      break;
    case POLICEBEHAVIOR:
      ui().text(
          "The Squad takes control of the microphone and goes over cases of police brutality.")
          .add();
      break;
    case TORTURE:
      ui().text(
          "The Squad takes control of the microphone and discusses prisoner abuse and torture.")
          .add();
      break;
    case PRIVACY:
      ui().text("The Squad takes control of the microphone and debates privacy law.").add();
      break;
    case FREESPEECH:
      ui().text("The Squad takes control of the microphone and talks about free speech.").add();
      break;
    case GENETICS:
      ui().text(
          "The Squad takes control of the microphone and discusses the implications of genetic research.")
          .add();
      break;
    case JUSTICES:
      ui().text(
          "The Squad takes control of the microphone and talks about the record of a Conservative judge.")
          .add();
      break;
    case GUNCONTROL:
      ui().text("The Squad takes control of the microphone and talks about gun control.").add();
      break;
    case LABOR:
      ui().text(
          "The Squad takes control of the microphone and brings details about sweatshops to light.")
          .add();
      break;
    case POLLUTION:
      ui().text(
          "The Squad takes control of the microphone and does a show on industrial pollution.")
          .add();
      break;
    case CORPORATECULTURE:
      ui().text("The Squad takes control of the microphone and jokes about corporate culture.")
          .add();
      break;
    case CEOSALARY:
      ui().text("The Squad takes control of the microphone and gives examples of CEO excesses.")
          .add();
      break;
    case ABORTION:
      ui().text("The Squad takes control of the microphone and discusses abortion.").add();
      break;
    case CIVILRIGHTS:
      ui().text("The Squad takes control of the microphone and debates affirmative action.").add();
      break;
    case DRUGS:
      ui().text("The Squad takes control of the microphone and has a frank talk about drugs.")
          .add();
      break;
    case IMMIGRATION:
      ui().text("The Squad takes control of the microphone and examines the issue of immigration.")
          .add();
      break;
    case MILITARY:
      ui().text(
          "The Squad takes control of the microphone and talks about militarism in modern culture.")
          .add();
      break;
    case AMRADIO:
      ui().text("The Squad takes control of the microphone and discusses other AM radio shows.")
          .add();
      break;
    case CABLENEWS:
      ui().text("The Squad takes control of the microphone and talks about Cable News.").add();
      break;
    case LIBERALCRIMESQUAD:
      ui().text(
          "The Squad takes control of the microphone and lets people know about the Liberal Crime Squad.")
          .add();
      break;
    default:
    case LIBERALCRIMESQUADPOS:
      ui().text(
          "The Squad takes control of the microphone and extols the virtues of the Liberal Crime Squad.")
          .add();
      break;
    case CONSERVATIVECRIMESQUAD:
      ui().text(
          "The Squad takes control of the microphone and demonizes the Conservative Crime Squad.")
          .add();
      break;
    }
    getch();
    int segmentpower = 0;
    int usegmentpower;
    int partysize = 0;
    for (final Creature p : i.activeSquad) {
      if (!p.health().alive()) {
        continue;
      }
      segmentpower += p.skill().getAttribute(Attribute.INTELLIGENCE, true);
      segmentpower += p.skill().getAttribute(Attribute.HEART, true);
      segmentpower += p.skill().getAttribute(Attribute.CHARISMA, true);
      segmentpower += p.skill().skill(Skill.MUSIC);
      segmentpower += p.skill().skill(Skill.RELIGION);
      segmentpower += p.skill().skill(Skill.SCIENCE);
      segmentpower += p.skill().skill(Skill.BUSINESS);
      segmentpower += p.skill().skill(Skill.PERSUASION);
      p.skill().train(Skill.PERSUASION, 50);
      partysize++;
    }
    // LCS colors enhance the broadcast significantly
    // if(i.activesquad.stance==SQUADSTANCE_BATTLECOLORS)
    // segmentpower = (segmentpower * 3) / 2;
    final int segmentbonus = segmentpower / 4;
    if (partysize > 1) {
      segmentpower /= partysize;
    }
    segmentpower += segmentbonus;
    // clearmessagearea();
    if (segmentpower < 25) {
      ui().text("The Squad sounds wholly insane.").add();
    } else if (segmentpower < 35) {
      ui().text("The show really sucks.").add();
    } else if (segmentpower < 45) {
      ui().text("It is a very boring hour.").add();
    } else if (segmentpower < 55) {
      ui().text("It is mediocre radio.").add();
    } else if (segmentpower < 70) {
      ui().text("The show was all right.").add();
    } else if (segmentpower < 85) {
      ui().text("The Squad put on a good show.").add();
    } else if (segmentpower < 100) {
      ui().text("It was thought-provoking, even humorous.").add();
    } else {
      ui().text("It was the best hour of AM radio EVER.").add();
    }
    getch();
    // CHECK PUBLIC OPINION
    i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
    i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion((segmentpower - 50) / 2, 1, 100);
    if (viewhit != Issue.LIBERALCRIMESQUAD) {
      i.issue(viewhit).changeOpinion((segmentpower - 50) / 2, 1, 100);
    } else {
      i.issue(viewhit).changeOpinion(segmentpower / 2, 1, 100);
    }
    // PRISONER PARTS
    for (final Creature p : i.activeSquad) {
      if (p.prisoner().exists() && p.prisoner().get().health().alive()) {
        if (p.prisoner().get().type() == CreatureType.valueOf("RADIOPERSONALITY")) {
          viewhit = i.rng.randFromArray(Issue.values());
          switch (viewhit) {
          case GAY:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to discuss homosexual rights.")
                .add();
            break;
          case DEATHPENALTY:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to examine the death penalty.")
                .add();
            break;
          case TAX:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to discuss the tax code.")
                .add();
            break;
          case NUCLEARPOWER:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to run down nuclear power.")
                .add();
            break;
          case ANIMALRESEARCH:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to discuss the horrors of animal research.").add();
            break;
          case POLICEBEHAVIOR:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to go over cases of police brutality.").add();
            break;
          case TORTURE:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to discuss prisoner abuse and torture.").add();
            break;
          case PRIVACY:
            ui().text("The hostage " + p.prisoner().get() + " is forced on to debate privacy law.")
                .add();
            break;
          case FREESPEECH:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to talk about free speech.")
                .add();
            break;
          case GENETICS:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to discuss the implications of genetic research.").add();
            break;
          case JUSTICES:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to talk about the record of a Conservative judge.").add();
            break;
          case GUNCONTROL:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to talk about gun control.")
                .add();
            break;
          case LABOR:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to bring details about sweatshops to light.").add();
            break;
          case POLLUTION:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to do a show on industrial pollution.").add();
            break;
          case CORPORATECULTURE:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to joke about corporate culture.").add();
            break;
          case CEOSALARY:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to give examples of CEO excesses.").add();
            break;
          case ABORTION:
            ui().text("The hostage " + p.prisoner().get() + " is forced on to discuss abortion.")
                .add();
            break;
          case CIVILRIGHTS:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to debate affirmative action.")
                .add();
            break;
          case DRUGS:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to have a frank talk about drugs.").add();
            break;
          case IMMIGRATION:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to examine the issue of immigration.").add();
            break;
          case MILITARY:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to talk about militarism in modern culture.").add();
            break;
          case AMRADIO:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to discuss other AM radio shows.").add();
            break;
          case CABLENEWS:
            ui().text(
                "The hostage " + p.prisoner().get() + " is forced on to talk about Cable News.")
                .add();
            break;
          case LIBERALCRIMESQUAD:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to let people know about the Liberal Crime Squad.").add();
            break;
          default:
          case LIBERALCRIMESQUADPOS:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to extol the virtues of the Liberal Crime Squad.").add();
            break;
          case CONSERVATIVECRIMESQUAD:
            ui().text(
                "The hostage " + p.prisoner().get()
                    + " is forced on to demonize the Conservative Crime Squad.").add();
            break;
          }
          usegmentpower = 10; // FAME BONUS
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.INTELLIGENCE, true);
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.HEART, true);
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.CHARISMA, true);
          usegmentpower += p.prisoner().get().skill().skill(Skill.PERSUASION);
          if (viewhit != Issue.LIBERALCRIMESQUAD) {
            i.issue(viewhit).changeOpinion((usegmentpower - 10) / 2, 1, 80);
          } else {
            i.issue(viewhit).changeOpinion(usegmentpower / 2, 1, 100);
          }
          segmentpower += usegmentpower;
          getch();
        } else {
          // clearmessagearea();
          ui().text(p.prisoner().get() + ", the hostage, is kept off-air.").add();
          getch();
        }
      }
    }
    if (i.site.alienate() != Alienation.NONE && segmentpower >= 40) {
      i.site.alienate(Alienation.NONE);
      // clearmessagearea();
      ui().text("Moderates at the station appreciated the show.  They no longer feel alienated.")
          .add();
      getch();
    }
    // POST-SECURITY BLITZ IF IT SUCKED
    if (segmentpower < 90) {
      // clearmessagearea();
      ui().text("Security is waiting for the Squad after the show!").add();
      getch();
      int numleft = i.rng.nextInt(8) + 2;
      do {
        i.currentEncounter().makeEncounterCreature("SECURITYGUARD");
        numleft--;
      } while (numleft > 0);
    } else {
      // clearmessagearea();
      ui().text(
          "The show was so good that security listened to it at their desks.  The Squad might yet escape.")
          .add();
      getch();
    }
    return true;
  }
}
