package lcs.android.monthly;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.basemode.iface.LiberalAgenda;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.game.Game;
import lcs.android.game.Visibility;
import lcs.android.politics.Alignment;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.scoring.EndType;
import lcs.android.scoring.HighScore;
import lcs.android.util.Curses;
import lcs.android.util.RomanNumeral;

import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public enum EndGame { // TODO order shouldn't matter.
  NONE {
    @Override public void monthlyUpdate() {
      if (Politics.publicmood() > 60) {
        i.endgameState = EndGame.CCS_APPEARANCE;
        i.issue(Issue.CONSERVATIVECRIMESQUAD).attitude(0);
      }
    }
  },
  CCS_APPEARANCE {
    @Override public void monthlyUpdate() {
      if (Politics.publicmood() > 80) {
        i.endgameState = EndGame.CCS_ATTACKS;
      }
    }
  },
  CCS_ATTACKS {
    @Override public void monthlyUpdate() {
      if (Politics.publicmood() > 90) {
        i.endgameState = EndGame.CCS_SIEGES;
      }
    }
  },
  CCS_SIEGES {
    @Override public void monthlyUpdate() {
      if (Politics.publicmood() > 85 && i.presparty == 1) {
        i.endgameState = EndGame.MARTIALLAW;
      }
    }
  },
  CCS_DEFEATED {
    @Override public void monthlyUpdate() {
      if (Politics.publicmood() > 85 && i.presparty == 1) {
        i.endgameState = EndGame.MARTIALLAW;
      }
    }
  },
  MARTIALLAW {
    @Override public void monthlyUpdate() {}
  };
  public boolean ccsActive() {
    if (this == CCS_ATTACKS || this == CCS_SIEGES) {
      return true;
    }
    return false;
  }

  /** Perform monthly updates based on the current state of the game. */
  public abstract void monthlyUpdate();

  private static final String[] US_STATES = { "Alabama", "Alaska", "Arkansas", "Arizona",
      "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho",
      "Illinois", "Indiana", "Iowa", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts",
      "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada",
      "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota",
      "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina",
      "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington",
      "West Virginia", "Wisconsin", "Wyoming" };

  /** endgame - attempts to pass a constitutional amendment to help win the game */
  public static void amendmentTermlimits() {
    if (i.termlimits) {
      return; // Durr~! Don't pass this amendment if it's already passed!
    }
    // int j;
    fact("A National Convention has proposed an ELITE LIBERAL AMENDMENT!\n\n"
        + "In light of the Conservative nature of entrenched politicians, "
        + "and the corrupting influence of incumbency on the democratic process, "
        + "all members of the House of Representatives and Senate shall henceforth "
        + "be limited to one term in office.  This shall be immediately enforced "
        + "by holding elections to replace all members of Congress upon the "
        + "ratification of this amendment.");
    if (EndGame.ratify(2, null, null, 0)) {
      i.termlimits = true;
      ui().text("Press any key to hold new elections!").add();
      getch();
      Politics.elections_senate(0);
      Politics.elections_senate(1);
      Politics.elections_senate(2);
      Politics.elections_house();
      i.amendNum++;
    }
    ui().text("Press any key to reflect on what has happened.").add();
    getch();
  }

  public static EndType endCauseFromSite(final Location site) {
    if (site != null && site.lcs().siege.siege) {
      switch (i.site.current().lcs().siege.siegetype) {
      case POLICE:
        return (EndType.POLICE);
      case CIA:
        return (EndType.CIA);
      case HICKS:
        return (EndType.HICKS);
      case CORPORATE:
        return (EndType.CORP);
      case CCS:
        return (EndType.CCS);
      case FIREMEN:
        return (EndType.FIREMEN);
      default:
      }
    }
    return (EndType.DEAD);
  }

  /** common - test for possible game over
   * @param cause why it happened, not null */
  public static void endcheck(final EndType cause) {
    for (final Creature p : i.pool) {
      if (p.health().alive() && p.alignment() == Alignment.LIBERAL
          && !(p.hasFlag(CreatureFlag.SLEEPER) && p.hire() != null)) {
        return;
      }
    }
    if (cause == null) {
      Log.e("LCS", "end cause was null", new IllegalArgumentException("end cause was null"));
      HighScore.savehighscore(EndType.DEAD);
    } else {
      HighScore.savehighscore(cause);
    }
    HighScore.viewhighscores();
    Game.endGame();
  }

  /** endgame - attempts to pass a constitutional amendment to help win the game */
  public static void reaganify() {
    fact("The Arch Conservative Congress is proposing an ARCH-CONSERVATIVE AMENDMENT!\n\n"
        + "In recognition of the fact that society is degenerating under "
        + "the pressure of the elite liberal threat, WE THE PEOPLE HEREBY "
        + "REPEAL THE CONSTITUTION.  The former United States are to be "
        + "reorganized into the CONFEDERATED STATES OF AMERICA, with new "
        + "boundaries to be determined by leading theologians. "
        + "Ronald Reagan is to be King, forever, even after death.\n\n"
        + "The following Executive Officers are also chosen in perpetuity:\n\n"
        + "Vice President Strom Thurmond, Secretary of State Jesse Helms, and Attorney General Jerry Falwell.\n\n"
        + "In the event of the deaths of any of the aforementioned "
        + "persons, though they shall still nominally hold these posts, "
        + "actual decisions shall be made by business representatives, "
        + "chosen by respected business leaders.\n\n"
        + "People may petition Jesus for a redress of grievances, as He will be the only one listening.\n\n"
        + "Have a nice day.");
    if (EndGame.ratify(-2, null, null, 1)) {
      ui().text("Press any key to reflect on what has happened ONE LAST TIME.").add();
      getch();
      i.amendNum = 1; // Constitution repealed...
      // REAGANIFY
      if (i.visibility == Visibility.CAN_SEE) {
        i.execs.get(Exec.PRESIDENT).name("Ronald Reagan");
        i.execs.get(Exec.VP).name("Strom Thurmond");
        i.execs.get(Exec.STATE).name("Jesse Helms");
        i.execs.get(Exec.ATTORNEY).name("Jerry Falwell");
        for (final Creature e : i.execs.values()) {
          e.alignment(Alignment.ARCHCONSERVATIVE).gender(Gender.WHITEMALEPATRIARCH);
        }
        LiberalAgenda.liberalagenda(Alignment.CONSERVATIVE);
        HighScore.savehighscore(EndType.REAGAN);
      } else {
        switch (i.visibility) {
        case VACATION:
          // DATING AND REAGANIFIED
          fact("You went on vacation when the country was on the verge of collapse.\n\n"
              + "The Conservatives have made the world in their image.\n\n"
              + "They'll round up the last of you eventually.  All is lost.");
          HighScore.savehighscore(EndType.DATING);
          break;
        case IN_HIDING:
          // HIDING AND REAGANIFIED
          fact("You went into hiding when the country was on the verge of collapse.\n\n"
              + "The Conservatives have made the world in their image.\n\n"
              + "They'll round the last of you up eventually.  All is lost.");
          HighScore.savehighscore(EndType.HIDING);
          break;
        case IN_JAIL:
          // IF YOU ARE ALL IN PRISON, JUST PASS AWAY QUIETLY
          fact("While you were on the inside, the country degenerated...\n\n"
              + "Your kind are never released these days.\n\n" + "Ain't no sunshine...");
          HighScore.savehighscore(EndType.PRISON);
          break;
        case DISBANDING:
          // DISBANDED AND REAGANIFIED
          fact("You disappeared safely, but you hadn't done enough.\n\n"
              + "The Conservatives have made the world in their image.\n\n"
              + "They'll round the last of you up eventually.  All is lost.");
          HighScore.savehighscore(EndType.DISBANDLOSS);
          break;
        default:
          break;
        }
      }
      HighScore.viewhighscores();
      Game.endGame();
    } else {
      fact("Breathe a sigh of relief.");
    }
  }

  /** endgame - attempts to pass a constitutional amendment to lose the game */
  public static void tossjustices() {
    ui().text("The Elite Liberal Congress is proposing an ELITE LIBERAL AMENDMENT!").add();
    getch();
    // STATE THE AMENDMENT
    int tossnum = 0;
    for (final Creature element : i.supremeCourt) {
      if (element.alignment() != Alignment.ELITELIBERAL) {
        tossnum++;
      }
    }
    EndGame.amendmentheading();
    ui().text(
        "The following former citizen" + (tossnum != 1 ? "s are" : " is")
            + " branded Arch-Conservative:").add();
    for (int j = 0; j < 9; j++) {
      if (i.supremeCourt[j].alignment() != Alignment.ELITELIBERAL) {
        ui().text(i.supremeCourt[j].toString()).add();
      }
    }
    ui().text(
        "In particular, the aforementioned former citizens may not serve on the Supreme Court.  Said former citizens will be deported to Conservative countries of the President's choosing to be replaced by Proper Justices, also of the President's choosing with the advice and consent of the Senate.")
        .add();
    ui().text("Press 'C' to watch the ratification process unfold.").add();
    getch();
    if (EndGame.ratify(2, null, null, 1)) {
      // BLAST JUSTICES
      for (int j = 0; j < 9; j++) {
        if (i.supremeCourt[j].alignment() != Alignment.ELITELIBERAL) {
          i.supremeCourt[j] = CreatureType.withType("JUDGE_SUPREME");
          i.supremeCourt[j].alignment(Alignment.ELITELIBERAL);
        }
      }
      i.amendNum++;
    }
    ui().text("Press any key to reflect on what has happened.").add();
    Curses.waitOnOK();
  }

  /** endgame - header for announcing constitutional amendments */
  static void amendmentheading() {
    setView(R.layout.generic);
    ui().text(
        "Proposed Amendment " + new RomanNumeral(i.amendNum)
            + " to the United States Constitution:").add();
  }

  /** endgame - checks if a constitutional amendment is ratified */
  static boolean ratify(final int level, final Issue lawview, @Nullable final Issue view,
      final int j) {
    // TODO test / androidify
    ui().text("The Ratification Process:").add();
    // THE STATE VOTE WILL BE BASED ON VIEW OF LAW
    int mood = Politics.publicmood(lawview);
    // OR OF A PARTICULAR ISSUE
    if (view != null) {
      mood = i.issue(view).attitude();
    }
    // CONGRESS
    // char num[20];
    boolean ratified = false;
    // int y = 0;
    if (j > 0) {
      ratified = true;
      ui().text("House").add();
      ui().text("Senate").add();
      ui().text("Press any key to watch the Congressional votes unfold.").add();
      getch();
      // nodelay(stdscr,TRUE);
      boolean yeswin_h = false;
      boolean yeswin_s = false;
      int yesvotes_h = 0;
      int yesvotes_s = 0;
      int vote;
      int s = -1;
      final int[] house = i.house.headcount();
      final int[] senate = i.senate.headcount();
      for (int l = 0; l < 435; l++) {
        vote = house[l];
        if (vote >= -1 && vote <= 1) {
          vote += i.rng.nextInt(3) - 1;
        }
        if (level == vote) {
          yesvotes_h++;
        }
        if (l == 434) {
          if (yesvotes_h >= 290) {
            yeswin_h = true;
          }
        }
        ui().text(yesvotes_h + " Yea").add();
        ui().text(l + 1 - yesvotes_h + " Nay").add();
        if (l % 4 == 0 && s < 99) {
          s++;
          vote = senate[s];
          if (vote >= -1 && vote <= 1) {
            vote += i.rng.nextInt(3) - 1;
          }
          if (level == vote) {
            yesvotes_s++;
          }
        }
        if (l == 434) {
          if (yesvotes_s >= 67) {
            yeswin_s = true;
          }
        }
        ui().text(yesvotes_s + " Yea").add();
        ui().text(s + 1 - yesvotes_s + " Nay").add();
        if (l % 5 == 0) {
          pauseMs(10);
        }
        getch();
      }
      if (!yeswin_h) {
        ratified = false;
      }
      if (!yeswin_s) {
        ratified = false;
      }
    } else {
      ratified = true;
    }
    // STATES
    if (ratified) {
      ratified = false;
      int yesstate = 0;
      for (int s = 0; s < US_STATES.length; s++) {
        // XXX: I really think that states past voting records
        // XXX: *eyes Massachusetts* should influence this...
        ui().text(US_STATES[s]).add();
      }
      ui().text("Press any key to watch the State votes unfold.").add();
      getch();
      // nodelay(stdscr, TRUE);
      int vote;
      int smood;
      for (int s = 0; s < US_STATES.length; s++) {
        smood = mood;
        // State biases.
        switch (s) {
        case 0:
          smood -= 10;
          break; // Alabama
        case 4:
          smood = 100;
          break; // California (Always L+)
        case 9:
          smood -= 10;
          break; // Georgia
        case 11:
          smood -= 10;
          break; // Idaho
        case 20:
          smood = 100;
          break; // Massachusetts (Always L+, even though this is
        // an
        // unfair advantage, SEE: McGovern)
        case 23:
          smood -= 10;
          break; // Mississippi
        case 39:
          smood -= 10;
          break; // South Carolina
        case 42:
          smood -= 10;
          break; // Texas
        case 43:
          smood = 0;
          break; // Utah (Always C+)
        default:
        }
        vote = 0;
        if (i.rng.nextInt(100) < smood) {
          vote++;
        }
        if (i.rng.nextInt(100) < smood) {
          vote++;
        }
        if (i.rng.nextInt(100) < smood) {
          vote++;
        }
        if (i.rng.nextInt(100) < smood) {
          vote++;
        }
        vote -= 2;
        if (vote == level && s != 44) {
          yesstate++;
          ui().text("Yea").add();
        } else {
          ui().text("Nay").add();
        }
        ui().text(yesstate + " Yea").add();
        ui().text(s + 1 - yesstate + " Nay").add();
        pauseMs(50);
      }
      if (yesstate >= 37) {
        ratified = true;
      }
      if (!ratified) {
        ui().text("AMENDMENT REJECTED.").add();
      } else {
        ui().text("AMENDMENT ADOPTED.").add();
      }
    } else {
      ui().text("AMENDMENT REJECTED.").add();
    }
    return ratified;
  }
}