package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.monthly.EndGame;
import lcs.android.news.NewsCherryBusted;
import lcs.android.politics.Alignment;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.util.Color;
import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Survey extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature c : this) {
      ui().text(c.toString() + " surfs the Net for recent opinion polls.").add();
      c.skill().train(Skill.COMPUTERS, Math.max(3 - c.skill().skill(Skill.COMPUTERS), 1));
      survey(c);
    }
  }

  private static final long serialVersionUID = Game.VERSION;

  private static int boxMueller(final double mu, final double sigma) {
    double r, x, y;
    // find a uniform random point (x, y) inside unit circle
    do {
      x = 2.0 * Math.random() - 1.0;
      y = 2.0 * Math.random() - 1.0;
      r = x * x + y * y;
    } while (r > 1 || r == 0);
    final double z = x * Math.sqrt(-2.0 * Math.log(r) / r);
    return (int) (mu + z * sigma);
  }

  private static void survey(final Creature cr) {
    final int creatureskill = cr.skill().skillRoll(Skill.COMPUTERS);
    int misschance = 30 - creatureskill, noise = 2;
    if (misschance < 5) {
      misschance = 5;
    }
    if (creatureskill < 3) {
      noise = 30;
    } else if (creatureskill < 7) {
      noise = 15;
    } else if (creatureskill < 10) {
      noise = 10;
    } else if (creatureskill < 15) {
      noise = 8;
    } else if (creatureskill < 20) {
      noise = 5;
    }
    final Map<Issue, Integer> survey = new EnumMap<Issue, Integer>(Issue.class);
    Issue maxview = null;
    for (final Issue v : Issue.values()) {
      int value = boxMueller(i.issue(v).attitude(), noise);
      if (value < 0) {
        value = -value;
      }
      if (value > 100) {
        value = 200 - value;
      }
      // Log.i("LCS", "Survey: " + v + " actual:" + i.issue(v).attitude() + " surveyed:" + value
      // + " (noise:" + noise);
      if (v == Issue.LIBERALCRIMESQUAD && i.issue(v).attitude() == 0) {
        value = -1;
      }
      if (v == Issue.LIBERALCRIMESQUADPOS && survey.get(Issue.LIBERALCRIMESQUAD) <= 0) {
        value = -1;
      }
      survey.put(v, value);
      if (v != Issue.LIBERALCRIMESQUAD && v != Issue.LIBERALCRIMESQUADPOS) {
        if (maxview != null) {
          if (i.issue(v).publicInterest() > i.issue(maxview).publicInterest()) {
            maxview = v;
          }
        } else if (i.issue(v).publicInterest() > 0) {
          maxview = v;
        }
      }
    }
    // setView(R.layout.generic);
    final int approval = Politics.presidentapproval();
    ui().text("Survey of Public Opinion, According to Recent Polls:").add();
    final Creature p = i.execs.get(Exec.PRESIDENT);
    ui().text(
        (boxMueller(approval, noise * 10) / 10) + "% had a favorable opinion of " + p.alignment()
            + " President " + p.toString() + ".").add();
    // Top excitement issue
    if (maxview != null) {
      switch (maxview) {
      default:
      case GAY:
        if (i.issue(Issue.GAY).attitude() > 50) {
          ui().text("The people are most concerned about protecting gay rights.").add();
        } else {
          ui().text("The people are most concerned about protecting the traditional family.").add();
        }
        break;
      case DEATHPENALTY:
        if (i.issue(Issue.DEATHPENALTY).attitude() > 50) {
          ui().text("The people are most concerned about the unjust death penalty.").add();
        } else if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
          ui().text("The people are most concerned about restoring the death penalty.").add();
        } else {
          ui().text("The people are most concerned about protecting the death penalty.").add();
        }
        break;
      case TAX:
        if (i.issue(Issue.TAX).attitude() > 50) {
          ui().text("The people are most concerned about the oppressive tax structure.").add();
        } else {
          ui().text("The people are most concerned about the excessive tax burden.").add();
        }
        break;
      case NUCLEARPOWER:
        if (i.issue(Issue.NUCLEARPOWER).attitude() > 50) {
          ui().text("The people are most concerned about the dangers of nuclear power.").add();
        } else if (i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
          ui().text("The people are most concerned about legalizing nuclear power.").add();
        } else {
          ui().text("The people are most concerned about threats to nuclear power.").add();
        }
        break;
      case ANIMALRESEARCH:
        if (i.issue(Issue.ANIMALRESEARCH).attitude() > 50) {
          ui().text("The people are most concerned about brutal animal research practices.").add();
        } else {
          ui().text("The people are most concerned about excessive regulation of animal research.")
              .add();
        }
        break;
      case POLICEBEHAVIOR:
        if (i.issue(Issue.POLICEBEHAVIOR).attitude() > 50) {
          ui().text("The people are most concerned about preventing police brutality.").add();
        } else {
          ui().text("The people are most concerned about expanding police powers.").add();
        }
        break;
      case PRIVACY:
        if (i.issue(Issue.PRIVACY).attitude() > 50) {
          ui().text("The people are most concerned about civil liberties and personal privacy.")
              .add();
        } else {
          ui().text("The people are most concerned about national security and intelligence.")
              .add();
        }
        break;
      case FREESPEECH:
        if (i.issue(Issue.FREESPEECH).attitude() > 50) {
          ui().text("The people are most concerned about protecting free speech.").add();
        } else {
          ui().text("The people are most concerned about ending hate speech.").add();
        }
        break;
      case GENETICS:
        if (i.issue(Issue.GENETICS).attitude() > 50) {
          ui().text("The people are most concerned about the dangers of genetic engineering.")
              .add();
        } else {
          ui().text("The people are most concerned about excessive regulation of genetic research.")
              .add();
        }
        break;
      case JUSTICES:
        if (i.issue(Issue.JUSTICES).attitude() > 50) {
          ui().text("The people are most concerned about appointing proper Liberal justices.")
              .add();
        } else {
          ui().text("The people are most concerned about appointing proper Conservative justices.")
              .add();
        }
        break;
      case LABOR:
        if (i.issue(Issue.LABOR).attitude() > 50) {
          ui().text("The people are most concerned about threats to labor rights.").add();
        } else {
          ui().text("The people are most concerned about excessive regulation of labor practices.")
              .add();
        }
        break;
      case POLLUTION:
        if (i.issue(Issue.POLLUTION).attitude() > 50) {
          ui().text("The people are most concerned about threats to the environment.").add();
        } else {
          ui().text("The people are most concerned about excessive regulation of industry.").add();
        }
        break;
      case CORPORATECULTURE:
        if (i.issue(Issue.CORPORATECULTURE).attitude() > 50) {
          ui().text("The people are most concerned about corporate corruption.").add();
        } else {
          ui().text("The people are most concerned about excessive regulation of corporations.")
              .add();
        }
        break;
      case CEOSALARY:
        if (i.issue(Issue.CEOSALARY).attitude() > 50) {
          ui().text("The people are most concerned about severe income inequality.").add();
        } else {
          ui().text("The people are most concerned about resisting communist wage limits.").add();
        }
        break;
      case IMMIGRATION:
        if (i.issue(Issue.IMMIGRATION).attitude() > 50) {
          ui().text("The people are most concerned about immigrant rights.").add();
        } else if (i.issue(Issue.IMMIGRATION).lawGTE(Alignment.LIBERAL)) {
          ui().text("The people are most concerned about uncontrolled immigration.").add();
        } else {
          ui().text("The people are most concerned about illegal immigration.").add();
        }
        break;
      case DRUGS:
        if (i.issue(Issue.DRUGS).attitude() > 50) {
          ui().text("The people are most concerned about drug rights.").add();
        } else {
          ui().text("The people are most concerned about drug abuse.").add();
        }
        break;
      case ABORTION:
        if (i.issue(Issue.ABORTION).attitude() > 50) {
          ui().text("The people are most concerned about women's equality.").add();
        } else {
          ui().text("The people are most concerned about women.").add();
        }
        break;
      case CIVILRIGHTS:
        if (i.issue(Issue.CIVILRIGHTS).attitude() > 50) {
          ui().text("The people are most concerned about civil rights.").add();
        } else {
          ui().text("The people are most concerned about troublemaking minorities.").add();
        }
        break;
      case GUNCONTROL:
        if (i.issue(Issue.GUNCONTROL).attitude() > 50) {
          ui().text("The people are most concerned about gun violence.").add();
        } else {
          ui().text("The people are most concerned about protecting the Second Amendment.").add();
        }
        break;
      case MILITARY:
        if (i.issue(Issue.MILITARY).attitude() > 50) {
          ui().text("The people are most concerned about the large military.").add();
        } else {
          ui().text("The people are most concerned about strengthening the military.").add();
        }
        break;
      case LIBERALCRIMESQUAD:
      case LIBERALCRIMESQUADPOS:
        if (i.issue(Issue.LIBERALCRIMESQUAD).attitude() < 50) {
          ui().text("The people are most concerned about activist political groups.").add();
          break;
        }
        if (i.issue(Issue.LIBERALCRIMESQUADPOS).attitude() > 50) {
          ui().text("The people are most concerned about the Liberal Crime Squad.").add();
        } else {
          ui().text("The people are most concerned about the LCS terrorists.").add();
        }
        break;
      case CONSERVATIVECRIMESQUAD:
        if (i.issue(Issue.CONSERVATIVECRIMESQUAD).attitude() < 50) {
          ui().text("The people are most concerned about the Conservative Crime Squad.").add();
          break;
        }
        ui().text("The people are most concerned about the CCS terrorists.").add();
        break;
      case TORTURE:
        if (i.issue(Issue.TORTURE).attitude() > 50) {
          ui().text("The people are most concerned about ending the use of torture.").add();
        } else {
          ui().text("The people are most concerned about enhancing interrogations.").add();
        }
        break;
      case AMRADIO:
      case CABLENEWS:
        if (i.issue(Issue.AMRADIO).attitude() + i.issue(Issue.CABLENEWS).attitude() > 100) {
          ui().text("The people are most concerned about Conservative Media Bias.").add();
        } else {
          ui().text("The people are most concerned about Liberal Media Bias.").add();
        }
        break;
      }
    } else {
      ui().text("The public is not concerned with politics right now.").add();
    }
    ui().text("Additional notable findings:").add();
    ui().text("XX% Issue (Public Interest)").bold().add();
    ui().text("Results are +/- " + noise + " Liberal percentage points.").add();
    Color color = Color.WHITE;
    for (final Issue v : Issue.values()) {
      if (v == Issue.CONSERVATIVECRIMESQUAD
          && (i.endgameState.ordinal() >= EndGame.CCS_DEFEATED.ordinal() || i.newscherrybusted != NewsCherryBusted.CCS_IN_NEWS)) {
        continue;
      }
      final StringBuilder str = new StringBuilder();
      if (survey.get(v) == -1) {
        color = Color.BLACK;
      } else if (survey.get(v) < 10) {
        color = Color.RED;
      } else if (survey.get(v) < 30) {
        color = Color.MAGENTA;
      } else if (survey.get(v) < 50) {
        color = Color.YELLOW;
      } else if (survey.get(v) < 70) {
        color = Color.BLUE;
      } else if (survey.get(v) < 90) {
        color = Color.CYAN;
      } else {
        color = Color.GREEN;
      }
      if (survey.get(v) == -1) {
        str.append("??");
      } else {
        str.append(survey.get(v).toString());
      }
      str.append("% ");
      switch (v) {
      default:
        str.append("!!! ERROR !!! " + v);
        break;
      case GAY:
        str.append("were in favor of equal rights for homosexuals");
        break;
      case DEATHPENALTY:
        str.append("opposed the death penalty");
        break;
      case TAX:
        str.append("were against cutting taxes");
        break;
      case NUCLEARPOWER:
        str.append("were terrified of nuclear power");
        break;
      case ANIMALRESEARCH:
        str.append("deplored animal research");
        break;
      case POLICEBEHAVIOR:
        str.append("were critical of the police");
        break;
      case TORTURE:
        str.append("wanted stronger measures to prevent torture");
        break;
      case PRIVACY:
        str.append("thought the intelligence community invades privacy");
        break;
      case FREESPEECH:
        str.append("believed in unfettered free speech");
        break;
      case GENETICS:
        str.append("abhorred genetically altered food products");
        break;
      case JUSTICES:
        str.append("were for the appointment of Liberal justices");
        break;
      case LABOR:
        str.append("would boycott companies that used sweatshops");
        break;
      case POLLUTION:
        str.append("thought industry should lower pollution");
        break;
      case CORPORATECULTURE:
        str.append("were disgusted by corporate malfeasance");
        break;
      case CEOSALARY:
        str.append("believed that CEO salaries are too great");
        break;
      case ABORTION:
        str.append("favored doing more for gender equality");
        break;
      case CIVILRIGHTS:
        str.append("felt more work was needed for racial equality");
        break;
      case GUNCONTROL:
        str.append("are concerned about gun violence");
        break;
      case DRUGS:
        if (i.issue(Issue.DRUGS).lawGTE(Alignment.LIBERAL)) {
          str.append("supported keeping marijuana legal");
        } else {
          str.append("believed in legalizing marijuana");
        }
        break;
      case IMMIGRATION:
        if (i.issue(Issue.IMMIGRATION).lawGTE(Alignment.LIBERAL)) {
          str.append("condemned unnecessary immigration regulations");
        } else {
          str.append("wanted amnesty for illegal immigrants");
        }
        break;
      case MILITARY:
        str.append("opposed increasing military spending");
        break;
      case LIBERALCRIMESQUAD:
        str.append("respected the power of the Liberal Crime Squad");
        break;
      case LIBERALCRIMESQUADPOS:
        str.append("of these held the Liberal Crime Squad in high regard");
        break;
      case CONSERVATIVECRIMESQUAD:
        str.append("held the Conservative Crime Squad in contempt");
        break;
      case AMRADIO:
        str.append("do not like AM radio");
        break;
      case CABLENEWS:
        str.append("have a negative opinion of cable news programs");
        break;
      case ELECTIONS:
        str.append("are concerned about standards in elections");
        break;
      case FLAGBURNING:
        str.append("are concerned about flag-burning");
        break;
      case WOMEN:
        str.append("believe more should be done for equal rights for women.");
        break;
      }
      if (noise >= 7 || survey.get(v) == -1) {
        str.append(" (Unknown)");
      } else if (noise >= 4) {
        if (i.issue(v).publicInterest() > 50) {
          str.append(" (High)");
        } else {
          str.append(" (Low)");
        }
      } else if (i.issue(v).publicInterest() >= 100) {
        str.append(" (Very High)");
      } else if (i.issue(v).publicInterest() > 50) {
        str.append(" (High)");
      } else if (i.issue(v).publicInterest() > 10) {
        str.append(" (Moderate)");
      } else if (i.issue(v).publicInterest() > 0) {
        str.append(" (Low)");
      } else {
        str.append("(None)");
      }
      Curses.ui().text(str.toString()).color(color).add();
      str.setLength(0);
    }
  }
}
