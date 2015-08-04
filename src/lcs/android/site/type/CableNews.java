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

@Xml.Name(name = "MEDIA_CABLENEWS") public @NonNullByDefault class CableNews extends
    AbstractSiteType {
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
    return "Network News Station.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.NEWS_BROADCASTSTUDIO;
  }

  @Override public void generateName(final Location l) {
    l.setName("Cable News Station");
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
      return "LOOT_CABLENEWSFILES";
    } else if (i.rng.chance(4)) {
      return "LOOT_MICROPHONE";
    } else if (i.rng.chance(3)) {
      return "LOOT_PDA";
    } else if (i.rng.chance(2)) {
      return "LOOT_CELLPHONE";
    }
    return "LOOT_COMPUTER";
  }

  @Override public String siegeUnit() {
    return "HICK";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  /** run a tv broadcast */
  public static boolean newsBroadcast() {
    i.site.alarm(true);
    final int enemy = i.currentEncounter().enemyCount();
    if (enemy > 0) {
      ui().text("The Conservatives in the room hurry the Squad, so the broadcast never happens.")
          .add();
      getch();
      return false;
    }
    i.activeSquad.criminalizeParty(Crime.DISTURBANCE);
    ui().text("The Squad steps in front of the cameras and").add();
    Issue viewhit = i.rng.randFromArray(Issue.values());
    switch (viewhit) {
    case GAY:
      ui().text("discusses homosexual rights.").add();
      break;
    case DEATHPENALTY:
      ui().text("examines the death penalty.").add();
      break;
    case TAX:
      ui().text("discusses the tax code.").add();
      break;
    case NUCLEARPOWER:
      ui().text("runs down nuclear power.").add();
      break;
    case ANIMALRESEARCH:
      ui().text("discusses the horrors of animal research.").add();
      break;
    case POLICEBEHAVIOR:
      ui().text("goes over cases of police brutality.").add();
      break;
    case TORTURE:
      ui().text("discusses prisoner abuse and torture.").add();
      break;
    case PRIVACY:
      ui().text("debates privacy law.").add();
      break;
    case FREESPEECH:
      ui().text("talks about free speech.").add();
      break;
    case GENETICS:
      ui().text("discusses the implications of genetic research.").add();
      break;
    case JUSTICES:
      ui().text("talks about the record of a Conservative judge.").add();
      break;
    case GUNCONTROL:
      ui().text("talks about gun control.").add();
      break;
    case LABOR:
      ui().text("brings details about sweatshops to light.").add();
      break;
    case POLLUTION:
      ui().text("does a show on industrial pollution.").add();
      break;
    case CORPORATECULTURE:
      ui().text("jokes about corporate culture.").add();
      break;
    case CEOSALARY:
      ui().text("gives examples of CEO excesses.").add();
      break;
    case ABORTION:
      ui().text("discusses abortion.").add();
      break;
    case CIVILRIGHTS:
      ui().text("debates affirmative action.").add();
      break;
    case DRUGS:
      ui().text("has a frank talk about drugs.").add();
      break;
    case IMMIGRATION:
      ui().text("examines the issue of immigration.").add();
      break;
    case MILITARY:
      ui().text("talks about militarism in modern culture.").add();
      break;
    case AMRADIO:
      ui().text("discusses other AM radio shows.").add();
      break;
    case CABLENEWS:
      ui().text("talks about Cable News.").add();
      break;
    case LIBERALCRIMESQUAD:
      ui().text("lets people know about the Liberal Crime Squad.").add();
      break;
    default:
    case LIBERALCRIMESQUADPOS:
      ui().text("extols the virtues of the Liberal Crime Squad.").add();
      break;
    case CONSERVATIVECRIMESQUAD:
      ui().text("demonizes the Conservative Crime Squad.").add();
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
    final int segmentbonus = segmentpower / 4;
    if (partysize > 1) {
      segmentpower /= partysize;
    }
    segmentpower += segmentbonus;
    // clearmessagearea();
    if (segmentpower < 25) {
      ui().text("The Squad looks completely insane.").add();
    } else if (segmentpower < 35) {
      ui().text("The show really sucks.").add();
    } else if (segmentpower < 45) {
      ui().text("It is a very boring hour.").add();
    } else if (segmentpower < 55) {
      ui().text("It is mediocre TV.").add();
    } else if (segmentpower < 70) {
      ui().text("The show was all right.").add();
    } else if (segmentpower < 85) {
      ui().text("The Squad put on a good show.").add();
    } else if (segmentpower < 100) {
      ui().text("It was thought-provoking, even humorous.").add();
    } else {
      ui().text("It was the best hour of Cable TV EVER.").add();
    }
    getch();
    // CHECK PUBLIC OPINION
    i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(10, 1, 100);
    i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion((segmentpower - 50) / 10, 1, 100);
    if (viewhit != Issue.LIBERALCRIMESQUAD) {
      i.issue(viewhit).changeOpinion((segmentpower - 50) / 5, 1, 100);
    } else {
      i.issue(viewhit).changeOpinion(segmentpower / 10, 1, 100);
    }
    // PRISONER PARTS
    for (final Creature p : i.activeSquad) {
      if (p.prisoner().exists() && p.prisoner().get().health().alive()) {
        if (p.prisoner().get().type() == CreatureType.valueOf("NEWSANCHOR")) {
          viewhit = i.rng.randFromArray(Issue.values());
          // clearmessagearea();
          ui().text("The hostage ").add();
          ui().text(p.prisoner().toString()).add();
          ui().text(" is forced on to").add();
          switch (viewhit) {
          case GAY:
            ui().text("discuss homosexual rights.").add();
            break;
          case DEATHPENALTY:
            ui().text("examine the death penalty.").add();
            break;
          case TAX:
            ui().text("discuss the tax code.").add();
            break;
          case NUCLEARPOWER:
            ui().text("run down nuclear power.").add();
            break;
          case ANIMALRESEARCH:
            ui().text("discuss the horrors of animal research.").add();
            break;
          case POLICEBEHAVIOR:
            ui().text("go over cases of police brutality.").add();
            break;
          case TORTURE:
            ui().text("discuss prisoner abuse and torture.").add();
            break;
          case PRIVACY:
            ui().text("debate privacy law.").add();
            break;
          case FREESPEECH:
            ui().text("talk about free speech.").add();
            break;
          case GENETICS:
            ui().text("discuss the implications of genetic research.").add();
            break;
          case JUSTICES:
            ui().text("talk about the record of a Conservative judge.").add();
            break;
          case GUNCONTROL:
            ui().text("talk about gun control.").add();
            break;
          case LABOR:
            ui().text("bring details about sweatshops to light.").add();
            break;
          case POLLUTION:
            ui().text("do a show on industrial pollution.").add();
            break;
          case CORPORATECULTURE:
            ui().text("joke about corporate culture.").add();
            break;
          case CEOSALARY:
            ui().text("give examples of CEO excesses.").add();
            break;
          case ABORTION:
            ui().text("discuss abortion.").add();
            break;
          case CIVILRIGHTS:
            ui().text("debate affirmative action.").add();
            break;
          case DRUGS:
            ui().text("have a frank talk about drugs.").add();
            break;
          case IMMIGRATION:
            ui().text("examine the issue of immigration.").add();
            break;
          case MILITARY:
            ui().text("talk about militarism in modern culture.").add();
            break;
          case AMRADIO:
            ui().text("discuss other AM radio shows.").add();
            break;
          case CABLENEWS:
            ui().text("talk about Cable News.").add();
            break;
          case LIBERALCRIMESQUAD:
            ui().text("let people know about the Liberal Crime Squad.").add();
            break;
          default:
          case LIBERALCRIMESQUADPOS:
            ui().text("extol the virtues of the Liberal Crime Squad.").add();
            break;
          case CONSERVATIVECRIMESQUAD:
            ui().text("demonize the Conservative Crime Squad.").add();
            break;
          }
          usegmentpower = 10; // FAME BONUS
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.INTELLIGENCE, true);
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.HEART, true);
          usegmentpower += p.prisoner().get().skill().getAttribute(Attribute.CHARISMA, true);
          usegmentpower += p.prisoner().get().skill().skill(Skill.PERSUASION);
          if (viewhit != Issue.LIBERALCRIMESQUAD) {
            i.issue(viewhit).changeOpinion((usegmentpower - 10) / 2, 1, 100);
          } else {
            i.issue(viewhit).changeOpinion(usegmentpower / 2, 1, 100);
          }
          segmentpower += usegmentpower;
          getch();
        } else {
          // clearmessagearea();
          ui().text(p.prisoner().toString() + ", the hostage, is kept off-air.").add();
          getch();
        }
      }
    }
    if (i.site.alienate() != Alienation.NONE && segmentpower >= 40) {
      i.site.alienate(Alienation.NONE);
      ui().text("Moderates at the station appreciated the show.").add();
      ui().text("They no longer feel alienated.").add();
      getch();
    }
    if (segmentpower < 85 && segmentpower >= 25) {
      ui().text("Security is waiting for the Squad after the show!").add();
      getch();
      int numleft = i.rng.nextInt(8) + 2;
      do {
        i.currentEncounter().makeEncounterCreature("SECURITYGUARD");
        numleft--;
      } while (numleft > 0);
    } else {
      // clearmessagearea();
      ui().text("The show was so ").add();
      if (segmentpower < 50) {
        ui().text("hilarious").add();
      } else {
        ui().text("entertaining").add();
      }
      ui().text(" that security watched it").add();
      ui().text("at their desks.  The Squad might yet escape.").add();
      getch();
    }
    return true;
  }
}
