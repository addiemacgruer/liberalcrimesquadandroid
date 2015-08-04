package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.game.Visibility;
import lcs.android.politics.Alignment;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class LiberalAgenda {
  private LiberalAgenda() {}

  private static final int[] abortion = { R.string.AbortionX, R.string.AbortionA,
      R.string.AbortionB, R.string.AbortionC, R.string.AbortionD, R.string.AbortionE };

  private static final int[] animalresearch = { R.string.AnimalResearchX, R.string.AnimalResearchA,
      R.string.AnimalResearchB, R.string.AnimalResearchC, R.string.AnimalResearchD,
      R.string.AnimalResearchE };

  private static final int[] civilrights = { R.string.CivilRightsX, R.string.CivilRightsA,
      R.string.CivilRightsB, R.string.CivilRightsC, R.string.CivilRightsD, R.string.CivilRightsE };

  private static final int[] corporate = { R.string.CorporateX, R.string.CorporateA,
      R.string.CorporateB, R.string.CorporateC, R.string.CorporateD, R.string.CorporateE };

  private static final int[] deathpenalty = { R.string.DeathPenaltyX, R.string.DeathPenaltyA,
      R.string.DeathPenaltyB, R.string.DeathPenaltyC, R.string.DeathPenaltyD,
      R.string.DeathPenaltyE };

  private static final int[] drugs = { R.string.DrugsX, R.string.DrugsA, R.string.DrugsB,
      R.string.DrugsC, R.string.DrugsD, R.string.DrugsE };

  private static final int[] elections = { R.string.ElectionsX, R.string.ElectionsA,
      R.string.ElectionsB, R.string.ElectionsC, R.string.ElectionsD, R.string.ElectionsE };

  private static final int[] flagburning = { R.string.FlagBurningX, R.string.FlagBurningA,
      R.string.FlagBurningB, R.string.FlagBurningC, R.string.FlagBurningD, R.string.FlagBurningE };

  private static final int[] freespeech = { R.string.FreeSpeechX, R.string.FreeSpeechA,
      R.string.FreeSpeechB, R.string.FreeSpeechC, R.string.FreeSpeechD, R.string.FreeSpeechE };

  private static final int[] gay = { R.string.GayX, R.string.GayA, R.string.GayB, R.string.GayC,
      R.string.GayD, R.string.GayE };

  private static final int[] guncontrol = { R.string.GunControlX, R.string.GunControlA,
      R.string.GunControlB, R.string.GunControlC, R.string.GunControlD, R.string.GunControlE };

  private static final int[] immigration = { R.string.ImmigrationX, R.string.ImmigrationA,
      R.string.ImmigrationB, R.string.ImmigrationC, R.string.ImmigrationD, R.string.ImmigrationE };

  private static final int[] labor = { R.string.LaborX, R.string.LaborA, R.string.LaborB,
      R.string.LaborC, R.string.LaborD, R.string.LaborE };

  private static final int[] military = { R.string.MilitaryX, R.string.MilitaryA,
      R.string.MilitaryB, R.string.MilitaryC, R.string.MilitaryD, R.string.MilitaryE };

  private static final int[] nuclearpower = { R.string.NuclearPowerX, R.string.NuclearPowerA,
      R.string.NuclearPowerB, R.string.NuclearPowerC, R.string.NuclearPowerD,
      R.string.NuclearPowerE };

  private static final int[] policebehavior = { R.string.PoliceBehaviorX, R.string.PoliceBehaviorA,
      R.string.PoliceBehaviorB, R.string.PoliceBehaviorC, R.string.PoliceBehaviorD,
      R.string.PoliceBehaviorE };

  private static final int[] pollution = { R.string.PollutionX, R.string.PollutionA,
      R.string.PollutionB, R.string.PollutionC, R.string.PollutionD, R.string.PollutionE };

  private static final int[] privacy = { R.string.PrivacyX, R.string.PrivacyA, R.string.PrivacyB,
      R.string.PrivacyC, R.string.PrivacyD, R.string.PrivacyE };

  private static final int[] tax = { R.string.TaxX, R.string.TaxA, R.string.TaxB, R.string.TaxC,
      R.string.TaxD, R.string.TaxE };

  private static final int[] torture = { R.string.TortureX, R.string.TortureA, R.string.TortureB,
      R.string.TortureC, R.string.TortureD, R.string.TortureE };

  private static final int[] women = { R.string.WomenX, R.string.WomenA, R.string.WomenB,
      R.string.WomenC, R.string.WomenD, R.string.WomenE };

  public static void liberalagenda(final Alignment won) {
    drawLiberalAgendaScreenP(won);
    ui(R.id.gcontrol).button(' ').text("Continue the struggle").add();
    ui(R.id.gcontrol).button('d').text("Disband the LCS").add();
    ui(R.id.gcontrol).button('q').text("Abandon the struggle").add();
    final int c = getch();
    if (c == 'd') {
      Visibility.confirmDisband();
    } else if (c == 'q') {
      Visibility.confirmQuit();
    }
  }

  protected static void liberalprogress() {
    drawLiberalAgendaScreenP(Alignment.MODERATE);
    setText(R.id.laStatus, "The Status of the Liberal Agenda, " + i.score.date.monthName() + " "
        + i.score.date.year());
    ui(R.id.gcontrol).button(' ').text("Continue to wait").add();
    ui(R.id.gcontrol).button('r').text("Reform the LCS").add();
    final int c = getch();
    if (c == 'r') {
      i.visibility = Visibility.CAN_SEE;
    }
  }

  @Nullable private static int[] arrayFromLaw(final Issue l) {
    int[] which = null;
    switch (l) {
    case ABORTION:
      which = abortion;
      break;
    case ANIMALRESEARCH:
      which = animalresearch;
      break;
    case POLICEBEHAVIOR:
      which = policebehavior;
      break;
    case PRIVACY:
      which = privacy;
      break;
    case DEATHPENALTY:
      which = deathpenalty;
      break;
    case NUCLEARPOWER:
      which = nuclearpower;
      break;
    case POLLUTION:
      which = pollution;
      break;
    case LABOR:
      which = labor;
      break;
    case GAY:
      which = gay;
      break;
    case CORPORATECULTURE:
      which = corporate;
      break;
    case FREESPEECH:
      which = freespeech;
      break;
    case FLAGBURNING:
      which = flagburning;
      break;
    case GUNCONTROL:
      which = guncontrol;
      break;
    case TAX:
      which = tax;
      break;
    case WOMEN:
      which = women;
      break;
    case CIVILRIGHTS:
      which = civilrights;
      break;
    case DRUGS:
      which = drugs;
      break;
    case IMMIGRATION:
      which = immigration;
      break;
    case ELECTIONS:
      which = elections;
      break;
    case MILITARY:
      which = military;
      break;
    case TORTURE:
      which = torture;
      break;
    }
    return which;
  }

  private static void drawLiberalAgendaScreenP(final Alignment won) {
    setView(R.layout.liberalagenda);
    if (won == Alignment.LIBERAL) {
      setColor(R.id.laStatus, Color.GREEN);
      setText(R.id.laStatus, R.string.laWon);
      setColor(R.id.laSCLabel, Color.GREEN);
    } else if (won == Alignment.CONSERVATIVE) {
      setColor(R.id.laStatus, Color.RED);
      setText(R.id.laStatus, R.string.laLost);
      setColor(R.id.laSCLabel, Color.RED);
    } else {
      setColor(R.id.laStatus, Color.WHITE);
    }
    if (won != Alignment.CONSERVATIVE) {
      if (i.execterm == 1) {
        setText(R.id.laPresLabel, R.string.laPres1);
      } else {
        setText(R.id.laPresLabel, R.string.laPres2);
      }
      setText(R.id.laSoSLabel, R.string.laSoS);
      final int[] housemake = i.house.makeup();
      int lsum = housemake[3] + housemake[4] - housemake[0] - housemake[1];
      if (lsum <= -145) {
        setColor(R.id.laHouse, Color.RED);
      } else if (lsum <= 0) {
        setColor(R.id.laHouse, Color.MAGENTA);
      } else if (lsum <= 145) {
        setColor(R.id.laHouse, Color.YELLOW);
      } else if (housemake[4] < 290) {
        setColor(R.id.laHouse, Color.BLUE);
      } else {
        setColor(R.id.laHouse, Color.GREEN);
      }
      setText(R.id.laHouse, R.string.laHouse, housemake[4], housemake[3], housemake[2],
          housemake[1], housemake[0]);
      final int[] senatemake = i.senate.makeup();
      lsum = senatemake[3] + senatemake[4] - senatemake[0] - senatemake[1];
      if (lsum <= -33) {
        setColor(R.id.laSenate, Color.RED);
      } else if (lsum <= 0) {
        setColor(R.id.laSenate, Color.MAGENTA);
      } else if (lsum <= 33) {
        setColor(R.id.laSenate, Color.YELLOW);
      } else if (senatemake[4] < 67) {
        setColor(R.id.laSenate, Color.BLUE);
      } else {
        setColor(R.id.laSenate, Color.GREEN);
      }
      setText(R.id.laSenate, R.string.laSenate, senatemake[4], senatemake[3], senatemake[2],
          senatemake[1], senatemake[0]);
    } else {
      setText(R.id.laPresLabel, R.string.laPresLost);
      setText(R.id.laSoSLabel, R.string.laSoSLost);
      setText(R.id.laSenate, R.string.laCongress);
      setColor(R.id.laSenate, Color.RED);
    }
    setText(R.id.laPres, i.execs.get(Exec.PRESIDENT).toString());
    setPoliticalColor(i.execs.get(Exec.PRESIDENT).alignment(), R.id.laPres, R.id.laPresLabel);
    setText(R.id.laVicePresident, i.execs.get(Exec.VP).toString());
    setPoliticalColor(i.execs.get(Exec.VP).alignment(), R.id.laVicePresident,
        R.id.laVicePresidentLabel);
    setText(R.id.laSoS, i.execs.get(Exec.STATE).toString());
    setPoliticalColor(i.execs.get(Exec.STATE).alignment(), R.id.laSoS, R.id.laSoSLabel);
    setText(R.id.laAG, i.execs.get(Exec.ATTORNEY).toString());
    setPoliticalColor(i.execs.get(Exec.ATTORNEY).alignment(), R.id.laAG, R.id.laAGLabel);
    final int[] sclabels = { R.id.laSC0, R.id.laSC1, R.id.laSC2, R.id.laSC3, R.id.laSC4,
        R.id.laSC5, R.id.laSC6, R.id.laSC7, R.id.laSC8 };
    for (int j = 0; j < i.supremeCourt.length; j++) {
      setText(sclabels[j], i.supremeCourt[j].toString());
      setColor(sclabels[j], i.supremeCourt[j].alignment().lawColor());
    }
    for (final Issue l : Issue.values()) {
      final int[] which = arrayFromLaw(l);
      if (which == null) {
        continue;
      }
      ui().restext(which[i.issue(l).law().ordinal()]).color(i.issue(l).law().color()).add();
    }
  }

  private static final void setPoliticalColor(final Alignment a, final int... views) {
    for (final int view : views) {
      setColor(view, a.lawColor());
    }
  }
}
