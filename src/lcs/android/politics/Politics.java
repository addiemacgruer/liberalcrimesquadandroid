package lcs.android.politics;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lcs.android.R;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureName;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.game.WinConditions;
import lcs.android.monthly.EndGame;
import lcs.android.util.Color;
import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class Politics {
  private static final int WAIT = 5;

  public static void congress() {
    setView(R.layout.generic);
    ui().text("Congress is acting on legislation!").add();
    // CHANGE THINGS AND REPORT
    ui().text("Legislative Agenda " + i.score.date.year()).add();
    final List<Issue> bill = new ArrayList<Issue>();
    final Map<Issue, AtomicInteger> lawpriority = new EnumMap<Issue, AtomicInteger>(Issue.class);
    final Map<Issue, AtomicInteger> lawdir = new EnumMap<Issue, AtomicInteger>(Issue.class);
    final Map<Issue, AtomicInteger> killbill = new HashMap<Issue, AtomicInteger>();
    for (final Issue l : Issue.values()) {
      lawpriority.put(l, new AtomicInteger());
      lawdir.put(l, new AtomicInteger());
      killbill.put(l, new AtomicInteger());
    }
    // DETERMINE BILLS
    determinePriority(lawpriority, lawdir);
    selectHighestPriority(bill, lawpriority, lawdir);
    // addText(R.id.gmessages, "Press any key to watch the votes unfold.");
    waitOnOK();
    for (final Issue c : bill) {
      int yesvotes_h = 0;
      int yesvotes_s = 0;
      int vote;
      int s = -1;
      setView(R.layout.politics);
      setText(R.id.title, "Joint Resolution " + i.score.date.year() + "-" + (bill.indexOf(c) + 1)
          + "\nTo " + describeResolution(c, lawdir.get(c).intValue()));
      setColor(R.id.title, lawdir.get(c).intValue() == 1 ? Color.GREEN : Color.RED);
      final int[] house = i.house.headcount();
      final int[] senate = i.senate.headcount();
      for (int l = 0; l < house.length; l++) {
        vote = house[l];
        if (vote >= -1 && vote <= 1) {
          vote += i.rng.choice(-1, 0, 1);
        }
        if (i.issue(c).law().trueOrdinal() > vote && lawdir.get(c).intValue() == -1) {
          yesvotes_h++;
        }
        if (i.issue(c).law().trueOrdinal() < vote && lawdir.get(c).intValue() == 1) {
          yesvotes_h++;
        }
        if (l % 4 == 0 && s < 99) {
          s++;
          vote = senate[s];
          if (vote >= -1 && vote <= 1) {
            vote += i.rng.nextInt(3) - 1;
          }
          if (i.issue(c).law().trueOrdinal() > vote && lawdir.get(c).intValue() == -1) {
            yesvotes_s++;
          }
          if (i.issue(c).law().trueOrdinal() < vote && lawdir.get(c).intValue() == 1) {
            yesvotes_s++;
          }
        }
        voteProgress(yesvotes_h, yesvotes_s, l);
      }
      boolean yeswin_h = false;
      boolean yeswin_s = false;
      if (yesvotes_h >= 218) {
        yeswin_h = true;
      }
      if (yesvotes_h >= 290) {
        killbill.get(c).set(-2);
      }
      if (yesvotes_s >= 51) {
        yeswin_s = true;
      }
      if (yesvotes_s < 67 && killbill.get(c).intValue() == -2) {
        killbill.get(c).set(0);
      }
      if (yesvotes_s == 50) {
        // TIE BREAKER
        vote = (i.execs.get(Exec.PRESIDENT).alignment().ord + i.execs.get(Exec.VP).alignment().ord
            + i.execs.get(Exec.STATE).alignment().ord + i.execs.get(Exec.ATTORNEY).alignment().ord
            + i.rng.nextInt(9) - 4) / 4;
        if (i.issue(c).law().trueOrdinal() > vote && lawdir.get(c).intValue() == -1) {
          yeswin_s = true;
          setText(R.id.senateY, "51 Yea VP");
        } else if (i.issue(c).law().trueOrdinal() < vote && lawdir.get(c).intValue() == 1) {
          yeswin_s = false;
          setText(R.id.senateN, "51 Nay VP");
        }
        // ASSURED SIGNING BY PRESIDENT IF VP VOTED YES
        if (yeswin_s) {
          killbill.get(c).set(-1);
        }
      }
      if (yeswin_h) {
        killbill.get(c).set(1);
      }
      if (yeswin_s) {
        killbill.get(c).set(1);
      }
      int sign = 0;
      if (killbill.get(c).intValue() == 1) {
        sign = 1;
      } else {
        vote = (i.execs.get(Exec.PRESIDENT).alignment().ord + i.execs.get(Exec.VP).alignment().ord
            + i.execs.get(Exec.STATE).alignment().ord + i.execs.get(Exec.ATTORNEY).alignment().ord
            + i.rng.nextInt(9) - 4) / 4;
        if (i.execs.get(Exec.PRESIDENT).alignment().ord == 2) {
          vote = 2;
        }
        if (i.execs.get(Exec.PRESIDENT).alignment().ord == -2) {
          vote = -2;
        }
        if (i.issue(c).law().trueOrdinal() > vote && lawdir.get(c).intValue() == -1) {
          sign = 1;
        }
        if (i.issue(c).law().trueOrdinal() < vote && lawdir.get(c).intValue() == 1) {
          sign = 1;
        }
        if (killbill.get(c).intValue() == -1) {
          sign = 1;
        }
      }
      if (yeswin_h && yeswin_s) {
        ui().text("Signed by " + i.execs.get(Exec.PRESIDENT).toString()).add();
      } else if (!yeswin_h && !yeswin_s) {
        ui().text("Dead in Congress").add();
      } else if (killbill.get(c).intValue() == -2) {
        ui().text("FORCED BY CONGRESS").add();
        sign = 1;
      } else {
        ui().text("*** VETO ***").color(Color.RED).add();
      }
      waitOnOK();
      if (sign == 1) {
        i.issue(c).law(Alignment.values()[i.issue(c).law().ordinal() + lawdir.get(c).intValue()]);
      }
    }
    checkConstitutionChanges();
  }

  public static void elections() {
    Curses.setView(R.layout.generic);
    ui().text("The Elections are being held today!").bold().add();
    waitOnOK();
    // PRESIDENTIAL
    if (i.score.date.year() % 4 == 0) {
      presidentialElections();
    }
    // SENATE
    if (i.score.date.year() % 2 == 0) {
      int senmod = -1;
      if (i.score.date.year() % 6 == 0) {
        senmod = 0;
      }
      if (i.score.date.year() % 6 == 2) {
        senmod = 1;
      }
      if (i.score.date.year() % 6 == 4) {
        senmod = 2;
      }
      elections_senate(senmod);
    }
    // HOUSE
    if (i.score.date.year() % 2 == 0) {
      elections_house();
    }
    // PROPOSITIONS
    propositions();
  }

  /* politics - causes congress to act on legislation */
  /* politics - checks the prevailing attitude on a specific i.law, or overall */
  public static void elections_house() {
    // char[] num = new char[10];
    final int mood = publicmood(null);
    displayPolitical();
    setText(R.id.title, "House Elections " + i.score.date.year());
    int vote;
    final int change[] = { 0, 0, 0, 0, 0 };
    final int total[] = { 0, 0, 0, 0, 0 };
    final List<Integer> shuffle = new ArrayList<Integer>();
    final int[] house = i.house.headcount();
    for (int s = 0; s < house.length; s++) {
      shuffle.add(s);
    }
    Collections.shuffle(shuffle, i.rng);
    for (final int h : shuffle) {
      pauseMs(WAIT);
      vote = 0;
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (i.termlimits) {
        change[house[h] + 2]--;
        change[vote]++;
        house[h] = vote - 2;
      } else {
        change[house[h] + 2]--;
        if (house[h] > 0 && vote < 3 && i.rng.nextInt(mood + 11) > 10) {
          vote++;
        }
        if (house[h] > 1 && vote < 4) {
          vote++;
        }
        if (house[h] < 0 && vote > -1 && i.rng.nextInt(100 - mood + 11) > 10) {
          vote--;
        }
        if (house[h] < -1 && vote > 0) {
          vote--;
        }
        switch (house[h]) {
        case -2:
          if (mood < 60) {
            break;
          }
          if (vote >= 3) {
            house[h] = vote - 1;
          }
          break;
        case -1:
          if (vote == 0 && i.rng.nextInt(100 - mood + 1) > 60) {
            house[h] = -2;
          }
          if (mood < 50 && i.rng.likely(7)) {
            break;
          }
          if (vote >= 3 && i.rng.likely(3)) {
            house[h] = vote - 1;
          }
          break;
        default:
          if (i.rng.chance(3)) {
            house[h] = vote - 2;
          }
          break;
        case 1:
          if (vote == 4 && i.rng.nextInt(mood + 1) > 60) {
            house[h] = 2;
          }
          if (mood > 50 && i.rng.likely(7)) {
            break;
          }
          if (vote <= 1 && i.rng.likely(3)) {
            house[h] = vote - 1;
          }
          break;
        case 2:
          if (mood > 40) {
            break;
          }
          if (vote <= 1) {
            house[h] = vote - 1;
          }
          break;
        }
        if (house[h] > 2) {
          house[h] = 2;
        }
        if (house[h] < -2) {
          house[h] = -2;
        }
        change[house[h] + 2]++;
        total[house[h] + 2]++;
        updateStars(total);
      }
      // Alignment a = Alignment.fromInt(i.house[h]);
      // addText(R.id.gmessages, a.toShortString(), a.lawColor());
    }
    ui().text("Net change: ").bold().add();
    ui().text("C+: " + change[0]).color(Alignment.ARCHCONSERVATIVE.lawColor()).add();
    ui().text("C : " + change[1]).color(Alignment.CONSERVATIVE.lawColor()).add();
    ui().text("m : " + change[2]).color(Alignment.MODERATE.lawColor()).add();
    ui().text("L : " + change[3]).color(Alignment.LIBERAL.lawColor()).add();
    ui().text("L+: " + change[4]).color(Alignment.ELITELIBERAL.lawColor()).add();
    if (change[0] + change[1] > change[3] + change[4]) {
      if (change[1] < 0 && mood < 25) {
        ui().text("The $$ U.S.A. Flag Eagle $$ Conservative Party claims victory!").add();
      } else {
        ui().text("The Conservative Party claims victory!").add();
      }
    } else if (change[0] + change[1] < change[3] + change[4]) {
      if (change[3] < 0 && mood > 75) {
        ui().text("The Progressive Elite Social Liberal Party claims victory!").add();
      } else {
        ui().text("The Liberal Party claims victory!").add();
      }
    } else {
      ui().text("The next two years promise to be more of the same.").add();
    }
    i.house.setFromHeadCount(house);
    waitOnOK();
  }

  /* politics - causes the people to vote (presidential, congressional, propositions) */
  public static void elections_senate(final int senmod) {
    // char[] num = new char[10];
    final int mood = publicmood(null);
    // setView(R.layout.generic);
    // addBoldText(R.id.gmessages, "Senate Elections " + i.hs.year);
    /* for (int s = 0; s < 100; s++) if (senmod != -1 && s % 3 != senmod) continue; */
    // addText(R.id.gmessages,
    // "Press any key to watch the elections unfold.");
    // waitOnOK();
    displayPolitical();
    setText(R.id.title, "Senate Elections " + i.score.date.year());
    int vote;
    final int change[] = { 0, 0, 0, 0, 0 };
    final int total[] = { 0, 0, 0, 0, 0 };
    final List<Integer> shuffle = new ArrayList<Integer>();
    final int[] senate = i.senate.headcount();
    for (int s = 0; s < 100; s++) {
      shuffle.add(s);
    }
    Collections.shuffle(shuffle, i.rng);
    for (final int s : shuffle) {
      pauseMs(WAIT);
      if (senmod != -1 && s % 3 != senmod) {
        total[senate[s] + 2]++;
        updateStars(total);
        continue;
      }
      vote = 0;
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (mood > i.rng.nextInt(100)) {
        vote++;
      }
      if (i.termlimits) {
        change[senate[s] + 2]--;
        change[vote]++;
        senate[s] = vote - 2;
      } else {
        change[senate[s] + 2]--;
        if (senate[s] > 0 && vote < 3 && i.rng.nextInt(mood + 11) > 10) {
          vote++;
        }
        if (senate[s] > 1 && vote < 4) {
          vote++;
        }
        if (senate[s] < 0 && vote > -1 && i.rng.nextInt(100 - mood + 11) > 10) {
          vote--;
        }
        if (senate[s] < -1 && vote > 0) {
          vote--;
        }
        switch (senate[s]) {
        case -2:
          if (mood < 60) {
            break;
          }
          if (vote >= 3) {
            senate[s] = vote - 1;
          }
          break;
        case -1:
          if (vote == 0 && i.rng.nextInt(100 - mood + 1) > 60) {
            senate[s] = -2;
          }
          if (mood < 50 && i.rng.likely(7)) {
            break;
          }
          if (vote >= 3 && i.rng.likely(3)) {
            senate[s] = vote - 1;
          }
          break;
        default:
          if (i.rng.chance(3)) {
            senate[s] = vote - 2;
          }
          break;
        case 1:
          if (vote == 4 && i.rng.nextInt(mood + 1) > 60) {
            senate[s] = 2;
          }
          if (mood > 50 && i.rng.likely(7)) {
            break;
          }
          if (vote <= 1 && i.rng.likely(3)) {
            senate[s] = vote - 1;
          }
          break;
        case 2:
          if (mood > 40) {
            break;
          }
          if (vote <= 1) {
            senate[s] = vote - 1;
          }
          break;
        }
        if (senate[s] > 2) {
          senate[s] = 2;
        }
        if (senate[s] < -2) {
          senate[s] = -2;
        }
        change[senate[s] + 2]++;
      }
      total[senate[s] + 2]++;
      updateStars(total);
      // Alignment a = Alignment.fromInt(senate[s]);
      // addText(R.id.gmessages, a.toString(), a.lawColor());
      // char[] buffer = new char[10];
    }
    ui().text("Net change: ").bold().add();
    ui().text("C+: " + change[0]).color(Alignment.ARCHCONSERVATIVE.lawColor()).add();
    ui().text("C : " + change[1]).color(Alignment.CONSERVATIVE.lawColor()).add();
    ui().text("m : " + change[2]).color(Alignment.MODERATE.lawColor()).add();
    ui().text("L : " + change[3]).color(Alignment.LIBERAL.lawColor()).add();
    ui().text("L+: " + change[4]).color(Alignment.ELITELIBERAL.lawColor()).add();
    if (change[0] + change[1] > change[3] + change[4]) {
      if (change[1] < 0 && mood < 25) {
        ui().text("The $$ U.S.A. Flag Eagle $$ Conservative Party claims victory!").add();
      } else {
        ui().text("The Conservative Party claims victory!").add();
      }
    } else if (change[0] + change[1] < change[3] + change[4]) {
      if (change[3] < 0 && mood > 75) {
        ui().text("The Progressive Elite Social Liberal Party claims victory!").add();
      } else {
        ui().text("The Liberal Party claims victory!").add();
      }
    } else {
      ui().text("The next two years promise to be more of the same.").add();
    }
    // addText(R.id.gmessages,
    // "Press any key to continue the elections.    ");
    i.senate.setFromHeadCount(senate);
    waitOnOK();
  }

  /* politics - causes the supreme court to hand down decisions */
  /* politics - calculate presidential approval */
  public static int presidentapproval() {
    // Calculate Presidental approval rating
    int approval = 0;
    Issue.randomissueinit(true);
    for (int j = 0; j < 1000; j++) {
      if (j % 2 == 0 && i.rng.likely(2)) {
        approval++;
      } else if (j % 2 != 0 && i.rng.likely(2)) {
        continue;
      } else // Swing issue voter (~50%) (should be more than in the real
      // election)
      {
        // Get their leanings as an issue voter
        final int vote = getswingvoter();
        // If their views are close to the President's views, they
        // should
        // approve, but might not if their party leaning conflicts with
        // the president's
        if (Math.abs(i.execs.get(Exec.PRESIDENT).alignment().ord - vote) <= 1) {
          // Moderate president from the Conservative party is only
          // supported
          // by moderates and Conservatives
          if (i.presparty == 1) {
            if (vote <= 0) {
              approval++;
            }
          } else if (vote >= 0) {
            approval++;
          }
        }
      }
    }
    return approval;
  }

  public static int publicmood() {
    int sum = 0;
    for (final Issue v : Issue.values()) {
      if (v == Issue.LIBERALCRIMESQUAD) {
        continue;
      }
      if (v == Issue.LIBERALCRIMESQUADPOS) {
        continue;
      }
      if (v == Issue.CONSERVATIVECRIMESQUAD) {
        continue;
      }
      sum += i.issue(v).attitude();
    }
    sum /= Issue.values().length - 3;
    return sum;
  }

  public static int publicmood(@Nullable final Issue l) {
    if (l == null) {
      Log.e("LCS", "Called publicmood with null", new RuntimeException(
          "Called publicmood with null"));
      return publicmood();
    }
    switch (l) {
    /* All laws should be affected by exactly one issue if there is a direct correlation between
     * that law and an issue. For example, police behavior as a law should depend only upon police
     * behavior as an issue. This keeps the game logical to the player and ensures that the public
     * opinion polls displayed in-game accurately predict how people will vote in specific issues.
     * For a handful of laws, we might not have a directly correllating issue; for example, as of
     * this writing, there is no issue asking people's opinions on torture. In this case, we can use
     * the nearest issue, or we can mix two closely related ones. As a general principle, try to
     * avoid getting too complicated here; this is under-the-hood stuff the player will never
     * appreciate, so it should be kept as simple and transparent as possible so as to avoid
     * creating unexpected results that will only confuse players, like people refusing to further
     * regulate nuclear power because one of the other issues besides nuclear power is conservative,
     * even when the nuclear power issue is 100% Liberal. - Jonathan S. Fox */
    case ABORTION:
      /* XXX: No Issue.ABORTION! Do not forget this! */
    case WOMEN:
      /* plus political violence. Ideologically, there's no association between flag burning and
       * violence. - Jonathan S. Fox */
      return i.issue(Issue.ABORTION).attitude();
    case ANIMALRESEARCH:
      return i.issue(Issue.ANIMALRESEARCH).attitude();
    case POLICEBEHAVIOR:
      return i.issue(Issue.POLICEBEHAVIOR).attitude();
    case PRIVACY:
      return i.issue(Issue.PRIVACY).attitude();
    case DEATHPENALTY:
      return i.issue(Issue.DEATHPENALTY).attitude();
    case NUCLEARPOWER:
      return i.issue(Issue.NUCLEARPOWER).attitude();
    case POLLUTION:
      return i.issue(Issue.POLLUTION).attitude();
    case LABOR:
      return i.issue(Issue.LABOR).attitude();
    case GAY:
      return i.issue(Issue.GAY).attitude();
    case CORPORATECULTURE:
      /* We'll be merging these two views here because there is no CEO salary law. The issue is
       * there for flavor, and falls under the same umbrella of corporate regulation. - Jonathan S.
       * Fox */
      return i.issue(Issue.CORPORATECULTURE).attitude() + i.issue(Issue.CEOSALARY).attitude() / 2;
    case FREESPEECH:
    case FLAGBURNING:
      return i.issue(Issue.FREESPEECH).attitude();
      /* <-- I'm keeping this pure free speech instead of free speech */
    case TAX:
      return i.issue(Issue.TAX).attitude();
    case CIVILRIGHTS:
      return i.issue(Issue.CIVILRIGHTS).attitude();
    case DRUGS:
      return i.issue(Issue.DRUGS).attitude();
    case IMMIGRATION:
      return i.issue(Issue.IMMIGRATION).attitude();
    case MILITARY:
      return i.issue(Issue.MILITARY).attitude();
    case TORTURE:
      return i.issue(Issue.TORTURE).attitude();
    case GUNCONTROL:
      return i.issue(Issue.GUNCONTROL).attitude();
    case ELECTIONS:
    default: // eg. -1
      return 0;
    }
  }

  /* politics - checks if the game is won */
  public static void supremecourt() {
    setView(R.layout.generic);
    ui().text("The Supreme court is handing down decisions!").bold().add();
    ui().text("Supreme Court Watch " + i.score.date.year()).add();
    final int cnum = i.rng.nextInt(5) + 2;
    int bias = 0;
    final List<Issue> scase = new ArrayList<Issue>();
    final Map<Issue, AtomicInteger> scasedir = new EnumMap<Issue, AtomicInteger>(Issue.class);
    final Map<Issue, Boolean> lawtaken = new EnumMap<Issue, Boolean>(Issue.class);
    final Map<Issue, String> description = new EnumMap<Issue, String>(Issue.class);
    for (final Issue l : Issue.values()) {
      scasedir.put(l, new AtomicInteger());
      lawtaken.put(l, false);
    }
    for (int c = 0; c < cnum; c++) {
      Issue scasec;
      do {
        scasec = i.rng.randFromArray(Issue.values());
      } while (lawtaken.get(scasec));
      scase.add(scasec);
      lawtaken.put(scasec, true);
      // Constitutional bias -- free speech, flag burning issues, supreme
      // court
      // is extra liberal, gun control, supreme court is extra
      // conservative
      switch (scasec) {
      case FREESPEECH:
      case FLAGBURNING:
        bias = 1;
        break;
      case GUNCONTROL:
        bias = -1;
        break;
      default:
        bias = 0;
      }
      if (i.issue(scasec).law() == Alignment.ELITELIBERAL) {
        scasedir.get(scasec).set(-1);
      } else if (i.issue(scasec).law() == Alignment.ARCHCONSERVATIVE) {
        scasedir.get(scasec).set(1);
      } else if (bias != 0) {
        scasedir.get(scasec).set(bias);
      } else if (i.rng.chance(2)) {
        scasedir.get(scasec).set(1);
      } else {
        scasedir.get(scasec).set(-1);
      }
      final int scasedirc = scasedir.get(scasec).intValue();
      final StringBuilder str = new StringBuilder();
      String name1, name2;
      if (i.rng.chance(5)) {
        name1 = "United States";
      } else {
        name1 = CreatureName.lastname();
      }
      name2 = CreatureName.lastname();
      if ((scasec == Issue.LABOR || scasec == Issue.CORPORATECULTURE
          || scasec == Issue.ANIMALRESEARCH || scasec == Issue.POLLUTION)
          && i.rng.likely(5)) {
        name2 += i.rng.choice(", Inc.", ", LLC.", " Corp.", " Co.", ", Ltd.");
      }
      str.append(name1);
      str.append(" vs. ");
      str.append(name2);
      str.append("A Decision could ");
      str.append(describeResolution(scasec, scasedirc));
      if (scasedirc == 1) {
        ui().text(str.toString()).color(Color.GREEN).add();
      } else {
        ui().text(str.toString()).color(Color.RED).add();
      }
      description.put(scasec, str.toString());
      str.setLength(0);
    }
    // addText(R.id.gmessages,
    // "Press any key to watch the decisions unfold.");
    waitOnOK();
    setView(R.layout.generic);
    for (final Issue scasec : scase) {
      boolean yeswin = false;
      int yesvotes = 0;
      int vote;
      // Constitutional bias -- free speech, flag burning issues, supreme
      // court
      // is extra liberal, gun control, supreme court is extra
      // conservative
      if (scasec == Issue.FREESPEECH || scasec == Issue.FLAGBURNING) {
        bias = 1;
      } else if (scasec == Issue.GUNCONTROL) {
        bias = -1;
      } else {
        bias = 0;
      }
      if (scasedir.get(scasec).intValue() == 1) {
        ui().text(description.get(scasec)).color(Color.GREEN).add();
      } else {
        ui().text(description.get(scasec)).color(Color.RED).add();
      }
      for (int l = 0; l < i.supremeCourt.length; l++) {
        vote = i.supremeCourt[l].alignment().ord;
        if (vote >= -1 && vote <= 1) {
          vote += i.rng.nextInt(3) - 1 + bias;
        }
        boolean statusquo = true;
        if (i.issue(scasec).law().trueOrdinal() > vote && scasedir.get(scasec).intValue() == -1) {
          yesvotes++;
          statusquo = false;
        }
        if (i.issue(scasec).law().trueOrdinal() < vote && scasedir.get(scasec).intValue() == 1) {
          yesvotes++;
          statusquo = false;
        }
        if (l == 8) {
          if (yesvotes >= 5) {
            yeswin = true;
          }
        }
        ui().text(
            i.supremeCourt[l].toString() + " votes for " + (statusquo ? "status quo" : "change")
                + ".").color(i.supremeCourt[l].alignment().lawColor()).add();
      }
      if (yeswin) {
        i.issue(scasec).law(
            Alignment.values()[i.issue(scasec).law().ordinal() + scasedir.get(scasec).intValue()]);
        ui().text("The law is changed!").add();
      } else {
        ui().text("The law remains the same!").add();
      }
    }
    // addText(R.id.gmessages,
    // "Press any key to reflect on what has happened.");
    waitOnOK();
    // CHANGE A JUSTICE 40% OF THE TIME
    if (i.rng.nextInt(10) >= 6) {
      final int[] senate = i.senate.headcount();
      ui().text("Changing the Guard!").bold().add();
      final int j = i.rng.nextInt(9);
      ui().text(
          "Justice " + i.supremeCourt[j].toString() + ", " + i.supremeCourt[j].alignment()
              + ", is stepping down.").add();
      // addText(R.id.gmessages, "Press any key to see what happens.");
      waitOnOK();
      i.supremeCourt[j] = CreatureType.withType("JUDGE_SUPREME");
      final int president = i.execs.get(Exec.PRESIDENT).alignment().ord * 100;
      int sen = 0;
      for (int s = 0; s < 100; s++) {
        sen += senate[s];
      }
      i.supremeCourt[j].alignment(Alignment.fromInt((president + sen) / 200));
      ui().text(
          "After much debate and televised testimony, a new justice, the Honorable "
              + i.supremeCourt[j].toString() + ", " + i.supremeCourt[j].alignment()
              + ", is appointed to the bench.").add();
      // addText(R.id.gmessages,
      // "Press any key to reflect on what has happened.");
      waitOnOK();
    }
  }

  public static boolean wincheck() {
    for (final Exec e : Exec.values()) {
      if (i.execs.get(e).alignment() != Alignment.ELITELIBERAL) {
        return false;
      }
    }
    if (!i.wincondition.victory()) {
      return false;
    }
    final int housemake[] = i.house.makeup();
    if (housemake[4] + housemake[3] / 2 < (i.wincondition == WinConditions.ELITE ? 290 : 270)) {
      return false;
    }
    final int senatemake[] = i.senate.makeup();
    if (senatemake[4] + senatemake[3] / 2 < (i.wincondition == WinConditions.ELITE ? 67 : 60)) {
      return false;
    }
    int elibjudge = 0;
    int libjudge = 0;
    for (final Creature c : i.supremeCourt) {
      if (c.alignment() == Alignment.ELITELIBERAL) {
        elibjudge++;
      }
      if (c.alignment() == Alignment.LIBERAL) {
        libjudge++;
      }
    }
    if (i.wincondition == WinConditions.ELITE) {
      if (elibjudge < 5) {
        return false;
      } else if (elibjudge < 5 && elibjudge + libjudge / 2 < 6) {
        return false;
      }
    }
    return true;
  }

  private static void checkConstitutionChanges() {
    // CONGRESS CONSTITUTION CHANGES
    final int housemake[] = i.house.makeup();
    final int senatemake[] = i.senate.makeup();
    // Throw out non-L+ Justices?
    boolean tossj = false;
    for (final Creature j : i.supremeCourt) {
      if (j.alignment() != Alignment.ELITELIBERAL) {
        tossj = true;
        break;
      }
    }
    if (housemake[4] + housemake[3] / 2 >= 290 && senatemake[4] + senatemake[3] / 2 >= 67 && tossj) {
      EndGame.tossjustices();
    }
    // Purge Congress, implement term limits, and hold new elections?
    if ((housemake[4] + housemake[3] / 2 < 290 || senatemake[4] + senatemake[3] / 2 < 67)
        && publicmood(null) > 80) {
      EndGame.amendmentTermlimits();
    }
    // REPEAL THE CONSTITUTION AND LOSE THE GAME?
    if (housemake[0] >= 290 && senatemake[0] >= 67) {
      EndGame.reaganify();
    }
  }

  private static String describeResolution(final Issue billc, final int billdirc) {
    {
      switch (billc) {
      case ABORTION:
        if (billdirc == 1) {
          return "Strengthen Abortion Rights";
        }
        return "Protect the Unborn Child";
      case ANIMALRESEARCH:
        if (billdirc == 1) {
          return "Limit Animal Cruelty";
        }
        return "Expand Animal Research";
      case POLICEBEHAVIOR:
        if (billdirc == 1) {
          return "Curtail Police Misconduct";
        }
        return "Stop Harassment of Police Officers";
      case PRIVACY:
        if (billdirc == 1) {
          return "Enhance Privacy Protection";
        }
        return "Allow Corporations Information Access";
      case DEATHPENALTY:
        if (billdirc == 1) {
          return "Limit the Death Penalty";
        }
        return "Expand Capital Punishment";
      case NUCLEARPOWER:
        if (billdirc == 1) {
          return "Limit Nuclear Power";
        }
        return "Expand Nuclear Power";
      case POLLUTION:
        if (billdirc == 1) {
          return "Punish Polluters";
        }
        return "Reward Industry";
      case LABOR:
        if (billdirc == 1) {
          return "Enhance Labor Standards";
        }
        return "End Undue Union Influence";
      case GAY:
        if (billdirc == 1) {
          return "Expand Homosexual Rights";
        }
        return "Support the Sanctity of Marriage";
      case CORPORATECULTURE:
        if (billdirc == 1) {
          return "Stop Corporate Criminals";
        }
        return "Reward Small Businesses";
      case FREESPEECH:
        if (billdirc == 1) {
          return "Protect Free Speech";
        }
        return "Limit Hurtful Speech";
      case TAX:
        if (billdirc == 1) {
          return "Punish the Wealthy";
        }
        return "Stimulate Economic Growth";
      case FLAGBURNING:
        if (billdirc == 1) {
          return "Limit Prohibitions on Flag Burning";
        }
        return "Protect the Symbol of Our Nation";
      case GUNCONTROL:
        if (billdirc == 1) {
          return "Prevent Gun Violence";
        }
        return "Protect our Second Amendment Rights";
      case WOMEN:
        if (billdirc == 1) {
          return "Expand Women's Rights";
        }
        return "Preserve Traditional Gender Roles";
      case CIVILRIGHTS:
        if (billdirc == 1) {
          return "Expand Civil Rights";
        }
        return "Fight Reverse Discrimination";
      case DRUGS:
        if (billdirc == 1) {
          return "Limit Oppressive Drug Laws";
        }
        return "Strengthen the War On Drugs";
      case IMMIGRATION:
        if (billdirc == 1) {
          return "Protect Immigrant Rights";
        }
        return "Protect our Borders";
      case ELECTIONS:
        if (billdirc == 1) {
          return "Fight Political Corruption";
        }
        return "Limit Regulation of Political Speech";
      case MILITARY:
        if (billdirc == 1) {
          return "Limit Military Spending";
        }
        return "Strengthen our National Defense";
      default:
      case TORTURE:
        if (billdirc == 1) {
          return "Ban Torture Techniques";
        }
        return "Permit Strong Tactics in Interrogations";
      }
    }
  }

  private static void determinePriority(final Map<Issue, AtomicInteger> lawpriority,
      final Map<Issue, AtomicInteger> lawdir) {
    int swing, priority;
    final int[] house = i.house.headcount();
    final int[] senate = i.senate.headcount();
    for (final Issue l : Issue.values()) {
      swing = 0;
      priority = 0;
      final int lawWeight = i.issue(l).law().trueOrdinal();
      if (i.rng.chance(3)) {
        for (final int cl : house) {
          if (lawWeight < cl) {
            swing++;
          } else if (lawWeight > cl) {
            swing--;
          }
          priority += Math.abs(cl - lawWeight);
        }
      } else if (i.rng.likely(2)) {
        for (final int sl : senate) {
          if (lawWeight < sl) {
            swing += 4;
          } else if (lawWeight > sl) {
            swing -= 4;
          }
          priority += Math.abs(sl - lawWeight) * 4;
        }
      } else {
        for (final int cl : house) {
          if (lawWeight < cl) {
            swing++;
          } else if (lawWeight > cl) {
            swing--;
          }
          priority += Math.abs(cl - lawWeight);
        }
        for (final int sl : senate) {
          if (lawWeight < sl) {
            swing += 4;
          } else if (lawWeight > sl) {
            swing -= 4;
          }
          priority += Math.abs(sl - lawWeight) * 4;
        }
      }
      if (swing > 0) {
        lawdir.get(l).set(1);
      } else if (swing == 0) {
        lawdir.get(l).set(i.rng.choice(-1, 1));
      } else {
        lawdir.get(l).set(-1);
      }
      if (lawWeight == -2) {
        lawdir.get(l).set(1);
      }
      if (lawWeight == 2) {
        lawdir.get(l).set(-1);
      }
      // CALC PRIORITY
      lawpriority.get(l).set(priority);
    }
  }

  private static void displayPolitical() {
    setView(R.layout.politicalbranch);
    setColor(R.id.arch, Alignment.ARCHCONSERVATIVE.lawColor());
    setColor(R.id.cons, Alignment.CONSERVATIVE.lawColor());
    setColor(R.id.mod, Alignment.MODERATE.lawColor());
    setColor(R.id.lib, Alignment.LIBERAL.lawColor());
    setColor(R.id.elite, Alignment.ELITELIBERAL.lawColor());
  }

  /* politics -- gets the leaning of a partyline voter for an election */
  private static int getsimplevoter(final int aLeaning) {
    int leaning = aLeaning;
    // int vote = leaning - 1;
    for (int j = 0; j < 2; j++) {
      if (i.rng.nextInt(100) < i.issue(Issue.randomissue()).attitude()) {
        leaning++;
      }
    }
    return leaning;
  }

  /* politics -- gets the leaning of an issue voter for an election */
  private static int getswingvoter() {
    // Take a random voter, calculate how liberal or conservative they are
    int bias = publicmood() - i.rng.nextInt(100);
    if (bias > 25) {
      bias = 25;
    }
    if (bias < -25) {
      bias = -25;
    }
    // Each issue they roll for their opinion on a 50-point subset of the
    // spectrum, determined by bias -- high liberal bias only rolls on the
    // liberal end of the spectrum, high conservative bias only rolls on
    // the conservative end of the spectrum
    int vote = -2;
    for (int j = 0; j < 4; j++) {
      if (25 + i.rng.nextInt(50) - bias < i.issue(Issue.randomissue()).attitude()) {
        vote++;
      }
    }
    return vote;
  }

  private static void presidentialElections() {
    int c;
    boolean oldwinner = false;
    setView(R.layout.generic);
    ui().text("Presidential General Election " + i.score.date.year()).bold().add();
    ui().text("After a long primary campaign, the people have rallied around two leaders...").add();
    final Creature[] candidate = new Creature[2];
    final Alignment[] cints = new Alignment[2];
    final int[] votes = { 0, 0 };
    // Primaries
    int approvepres = 0; // presidential approval within own party
    int approveveep = 0; // vice-presidential approval within own party
    final int[] libvotes = new int[3]; // liberal party's candidates votes
    // recieved
    final int[] consvotes = new int[3]; // conservative party's candidates
    // votes recieved
    // run primaries for 100 voters
    Issue.randomissueinit(true);
    for (int j = 0; j < 100; j++) {
      final int[] voters = { 0, 0 };
      // liberal party voter decides
      voters[0] += getsimplevoter(-1);
      // conservative party voter decides
      voters[1] += getsimplevoter(0);
      // Incumbent can win primary automatically if their approval in
      // their party is over 50%,
      // so we need to know what their inter-party approval rating is.
      // check if this voter supports the president (1/2 chance if
      // closely aligned)
      if (voters[i.presparty] == Math.abs(i.execs.get(Exec.PRESIDENT).alignment().ord)
          || Math.abs(i.execs.get(Exec.PRESIDENT).alignment().ord - Math.abs(voters[i.presparty])) == 1
          && i.rng.chance(2)) {
        approvepres++;
      }
      // check if this voter supports the vice-president (1/3 chance
      // if closely aligned)
      if (voters[i.presparty] == Math.abs(i.execs.get(Exec.VP).alignment().ord)
          || Math.abs(i.execs.get(Exec.VP).alignment().ord - Math.abs(voters[i.presparty])) == 1
          && i.rng.chance(3)) {
        approveveep++;
      }
      // count ballots
      consvotes[voters[1]]++;
      libvotes[voters[0] + 1]++;
    }
    // determine conservative winner
    if (consvotes[0] > consvotes[1]) {
      if (consvotes[0] > consvotes[2]) {
        cints[1] = Alignment.ARCHCONSERVATIVE;
      } else {
        cints[1] = Alignment.MODERATE;
      }
    } else if (consvotes[1] > consvotes[2]) {
      cints[1] = Alignment.CONSERVATIVE;
    } else {
      cints[1] = Alignment.MODERATE;
    }
    // determine liberal winner
    if (libvotes[0] > libvotes[1]) {
      if (libvotes[0] > libvotes[2]) {
        cints[0] = Alignment.MODERATE;
      } else {
        cints[0] = Alignment.ELITELIBERAL;
      }
    } else if (libvotes[1] > libvotes[2]) {
      cints[0] = Alignment.LIBERAL;
    } else {
      cints[0] = Alignment.ELITELIBERAL;
    }
    // name the candidates
    candidate[0] = CreatureType.withType("POLITICIAN");
    if (cints[0] == Alignment.ARCHCONSERVATIVE) {
      candidate[0].gender(Gender.WHITEMALEPATRIARCH);
    } else if (cints[0] == Alignment.CONSERVATIVE) {
      candidate[0].gender(Gender.MALE);
    }
    candidate[1] = CreatureType.withType("POLITICIAN");
    /* Special Incumbency Rules: If the incumbent president or vice president has approval of over
     * 50% in their party, they win their primary automatically. Even if they don't have over 50%,
     * if their alignment wins using the normal primary process, they are the chosen candidate for
     * that alignment (this last bit only applies to President; unpopular VP candidates just don't
     * run, and if their alignment wins it will be someone else). */
    if (i.execterm == 1) // President running for re-election
    {
      if (approvepres >= 50) {
        cints[i.presparty] = i.execs.get(Exec.PRESIDENT).alignment();
      }
      if (cints[i.presparty] == i.execs.get(Exec.PRESIDENT).alignment()) {
        candidate[i.presparty] = i.execs.get(Exec.PRESIDENT);
      } else {
        i.execterm = 2; // Boom! Incumbent president was defeated
        // in their own party. New candidate works with a clean slate.
      }
    } else if (approveveep >= 50) {
      if (approvepres >= 50) {
        cints[i.presparty] = i.execs.get(Exec.PRESIDENT).alignment();
        candidate[i.presparty] = i.execs.get(Exec.PRESIDENT);
      }
    }
    // Print candidates
    for (c = 0; c < 2; c++) {
      // Choose title -- president or vice president special
      // titles, otherwise
      // pick based on historically likely titles (eg, governor
      // most likely...)
      final StringBuilder str = new StringBuilder();
      if (c == i.presparty && i.execterm == 1) {
        str.append("President ");
      } else if (c == i.presparty && !candidate[c].equals(i.execs.get(Exec.VP).toString())) {
        str.append("Vice President ");
      } else if (i.rng.likely(2)) {
        str.append("Governor ");
      } else if (i.rng.likely(2)) {
        str.append("Senator ");
      } else if (i.rng.likely(2)) {
        str.append("Ret. General ");
      } else if (i.rng.likely(2)) {
        str.append("Representative ");
      } else if (i.rng.likely(2)) {
        str.append("Mr. ");
      } else {
        str.append("Mrs. ");
      }
      str.append(candidate[c]);
      final Alignment a = cints[c];
      str.append(", " + a);
      ui().text(str.toString()).color(a.color()).add();
    }
    waitOnOK();
    setView(R.layout.politics);
    setText(R.id.title, "Presidential General Election " + i.score.date.year());
    setText(R.id.house, candidate[0].toString());
    setColor(R.id.house, cints[0].lawColor());
    setText(R.id.senate, candidate[1].toString());
    setColor(R.id.senate, cints[1].lawColor());
    int winner = -1;
    // boolean recount = false;
    int vote;
    for (int l = 0; l < 1000; l++) // 1000 Voters!
    {
      vote = -2;
      if (l % 2 == 0 && i.rng.likely(5)) {
        votes[0]++;
      } else if (l % 2 != 0 && i.rng.likely(5)) {
        votes[1]++;
      } else // Swing Issue Voters (~20%)
      {
        // Get the leanings of an issue voter
        vote = getswingvoter();
        // If they are to the left or equal to the liberal
        // candidate,
        // and they disagree with the conservative candidate, cast a
        // vote for the liberal candidate.
        if (vote >= cints[0].trueOrdinal() && vote != cints[1].trueOrdinal()) {
          votes[0]++;
        } else if (vote <= cints[1].trueOrdinal() && vote != cints[0].trueOrdinal()) {
          votes[1]++;
        } else if (i.rng.likely(2)) {
          votes[0]++;
        } else {
          votes[1]++;
        }
      }
      if (l == 999) {
        int maxvote = 0;
        for (c = 0; c < 2; c++) {
          if (votes[c] > maxvote) {
            maxvote = votes[c];
          }
        }
        final List<Integer> eligible = new ArrayList<Integer>();
        for (c = 0; c < 2; c++) {
          if (votes[c] == maxvote) {
            eligible.add(c);
          }
        }
        if (eligible.size() > 1) {
          winner = i.rng.randFromList(eligible);
          // recount = true;
        } else {
          winner = eligible.get(0);
        }
      }
      if (l % 5 == 4) {
        setText(R.id.houseY, voteToPercent(votes[0]));
        setText(R.id.senateY, voteToPercent(votes[1]));
        // if (c == winner && recount)
        // addText(R.id.gmessages, " (After Recount)");
        pauseMs(WAIT);
      }
    }
    if (winner == i.presparty && i.execterm == 1) {
      oldwinner = true;
    }
    // CONSTRUCT EXECUTIVE BRANCH
    if (oldwinner) {
      i.execterm = 2;
    } else {
      i.presparty = winner;
      i.execterm = 1;
      final Creature p = candidate[winner];
      p.alignment(cints[winner]);
      i.execs.put(Exec.PRESIDENT, p);
      for (final Exec e : Exec.values()) {
        if (e == Exec.PRESIDENT) {
          continue;
        }
        final Creature ex = CreatureType.withType("POLITICIAN");
        i.execs.put(e, ex);
        if (p.alignment() == Alignment.ARCHCONSERVATIVE) {
          ex.alignment(Alignment.ARCHCONSERVATIVE);
        } else if (p.alignment() == Alignment.ELITELIBERAL) {
          ex.alignment(Alignment.ELITELIBERAL);
        } else {
          ex.alignment(Alignment.fromInt(cints[winner].trueOrdinal() + i.rng.nextInt(3) - 1));
        }
        if (ex.alignment() == Alignment.ARCHCONSERVATIVE) {
          ex.gender(Gender.WHITEMALEPATRIARCH);
        } else if (ex.alignment() == Alignment.CONSERVATIVE) {
          ex.gender(Gender.MALE);
        }
      }
    }
    ui().text(
        "President " + i.execs.get(Exec.PRESIDENT).toString() + " is " + (oldwinner ? "re-" : "")
            + "elected!").color(i.execs.get(Exec.PRESIDENT).alignment().lawColor()).add();
    waitOnOK();
  }

  private static void propositions() {
    int p;
    int mood;
    setView(R.layout.generic);
    ui().text("Important Propositions " + i.score.date.year()).bold().add();
    final List<Issue> prop = new ArrayList<Issue>();
    final List<Integer> propdir = new ArrayList<Integer>();
    final int pnum = i.rng.nextInt(4) + 4;
    final Map<Issue, Boolean> lawtaken = new EnumMap<Issue, Boolean>(Issue.class);
    final Map<Issue, Integer> lawpriority = new EnumMap<Issue, Integer>(Issue.class);
    final Map<Issue, AtomicInteger> lawdir = new EnumMap<Issue, AtomicInteger>(Issue.class);
    for (final Issue l : Issue.values()) {
      lawtaken.put(l, false);
      lawdir.put(l, new AtomicInteger());
    }
    // DETERMINE PROPS
    int pmood;
    int pvote;
    for (final Issue l : Issue.values()) {
      pmood = publicmood(l);
      pvote = 0;
      if (i.rng.nextInt(100) < pmood) {
        pvote++;
      }
      if (i.rng.nextInt(100) < pmood) {
        pvote++;
      }
      if (i.rng.nextInt(100) < pmood) {
        pvote++;
      }
      if (i.rng.nextInt(100) < pmood) {
        pvote++;
      }
      pvote -= 2;
      if (i.issue(l).law().trueOrdinal() < pvote) {
        lawdir.get(l).set(1);
      }
      if (i.issue(l).law().trueOrdinal() >= pvote) {
        lawdir.get(l).set(-1);
      }
      if (i.issue(l).law().trueOrdinal() == -2) {
        lawdir.get(l).set(1);
      }
      if (i.issue(l).law().trueOrdinal() == 2) {
        lawdir.get(l).set(-1);
      }
      // CALC PRIORITY
      if (i.issue(l).law().trueOrdinal() == -2) {
        pvote = 0;
      } else if (i.issue(l).law().trueOrdinal() == -1) {
        pvote = 25;
      } else if (i.issue(l).law().trueOrdinal() == 0) {
        pvote = 50;
      } else if (i.issue(l).law().trueOrdinal() == 1) {
        pvote = 75;
      } else {
        pvote = 100;
      }
      lawpriority.put(l, Math.abs(pvote - pmood) + i.rng.nextInt(10)
          + i.issue(l.viewForLaw()).publicInterest());
    }
    final List<Issue> canlaw = new ArrayList<Issue>();
    for (p = 0; p < pnum; p++) {
      int maxprior = 0;
      for (final Issue l : Issue.values()) {
        if (lawpriority.get(l) > maxprior && !lawtaken.get(l)) {
          maxprior = lawpriority.get(l);
        }
      }
      canlaw.clear();
      for (final Issue l : Issue.values()) {
        if (lawpriority.get(l).intValue() == maxprior && !lawtaken.get(l)) {
          canlaw.add(l);
        }
      }
      prop.add(i.rng.randFromList(canlaw));
      lawtaken.put(prop.get(p), true);
      final int propdird = lawdir.get(prop.get(p)).intValue();
      propdir.add(propdird);
      int propnum = 0;
      switch (p) {
      default:
        propnum = 2 * (17 - i.rng.nextInt(2) * 6) * (19 - i.rng.nextInt(2) * 6);
        break;
      case 1:
        propnum = 7 * (17 - i.rng.nextInt(2) * 6) * (19 - i.rng.nextInt(2) * 6);
        break;
      case 2:
        propnum = 3 * (17 - i.rng.nextInt(2) * 6) * (19 - i.rng.nextInt(2) * 6);
        break;
      case 3:
        propnum = 5 * (17 - i.rng.nextInt(2) * 6) * (2 - i.rng.nextInt(2) * 1);
        break;
      case 4:
        propnum = 11 * (17 - i.rng.nextInt(2) * 6) * (2 - i.rng.nextInt(2) * 1);
        break;
      case 5:
        propnum = 13 * (17 - i.rng.nextInt(2) * 6) * (2 - i.rng.nextInt(2) * 1);
        break;
      }
      ui().text("Proposition " + propnum + ": To " + describeResolution(prop.get(p), propdird))
          .color(propdird == 1 ? Color.GREEN : Color.RED).add();
    }
    // addText(R.id.gmessages,
    // "Press any key to watch the elections unfold.");
    waitOnOK();
    setView(R.layout.generic);
    ui().text("Important Propositions " + i.score.date.year()).bold().add();
    for (p = 0; p < pnum; p++) {
      boolean yeswin = false;
      int yesvotes = 0;
      boolean recount = false;
      mood = publicmood(prop.get(p));
      ui().text("Proposition " + p + ": To " + describeResolution(prop.get(p), propdir.get(p)))
          .color(propdir.get(p) == 1 ? Color.GREEN : Color.RED).add();
      for (int l = 0; l < 1000; l++) {
        if (i.rng.nextInt(100) < mood) {
          if (propdir.get(p) == 1) {
            yesvotes++;
          }
        } else if (propdir.get(p) == -1) {
          yesvotes++;
        }
        if (l == 999) {
          if (yesvotes > 500) {
            yeswin = true;
          } else if (yesvotes == 500) {
            if (i.rng.chance(2)) {
              yeswin = true;
            }
            recount = true;
          }
        }
      }
      pauseMs(WAIT * 20);
      ui().text(voteToPercent(yesvotes) + " Yes").add();
      ui().text(voteToPercent(1001 - yesvotes) + " No").add();
      if (recount) {
        ui().text("A Recount was Necessary").add();
      }
      if (yeswin) {
        i.issue(prop.get(p)).law(Alignment.values()[prop.get(p).ordinal() + propdir.get(p)]);
        ui().text("The proposition is passed.")
            .color(propdir.get(p) == 1 ? Color.GREEN : Color.RED).add();
      } else {
        ui().text("The proposition is rejected.")
            .color(propdir.get(p) == 1 ? Color.RED : Color.GREEN).add();
      }
    }
    // addText(R.id.gmessages,
    // "Press any key to reflect on what has happened.");
    waitOnOK();
  }

  private static void selectHighestPriority(final List<Issue> bill,
      final Map<Issue, AtomicInteger> lawpriority, final Map<Issue, AtomicInteger> lawdir) {
    final List<Issue> canlaw = new ArrayList<Issue>();
    for (int c = 0, cnum = i.rng.nextInt(3) + 4; c < cnum; c++) {
      int maxprior = 0;
      for (final Issue l : Issue.values()) {
        if (lawpriority.get(l) != null && lawpriority.get(l).intValue() > maxprior) {
          maxprior = lawpriority.get(l).intValue();
        }
      }
      canlaw.clear();
      for (final Issue l : Issue.values()) {
        if (lawpriority.get(l) != null && lawpriority.get(l).intValue() == maxprior) {
          canlaw.add(l);
        }
      }
      final Issue billc = i.rng.randFromList(canlaw);
      bill.add(billc);
      lawpriority.remove(billc);
      ui().text(
          "Joint Resolution " + i.score.date.year() + "-" + (c + 1) + "\n"
              + describeResolution(billc, lawdir.get(billc).intValue()))
          .color(lawdir.get(billc).intValue() == 1 ? Color.GREEN : Color.RED).add();
    }
  }

  private static String stars(final int number) {
    final StringBuilder str = new StringBuilder();
    for (int j = 0; j <= number; j++) {
      str.append('*');
    }
    return str.toString();
  }

  private static void updateStars(final int[] total) {
    setText(R.id.arch, Alignment.ARCHCONSERVATIVE + ":\n" + stars(total[0]));
    setText(R.id.cons, Alignment.CONSERVATIVE + ":\n" + stars(total[1]));
    setText(R.id.mod, Alignment.MODERATE + ":\n" + stars(total[2]));
    setText(R.id.lib, Alignment.LIBERAL + ":\n" + stars(total[3]));
    setText(R.id.elite, Alignment.ELITELIBERAL + ":\n" + stars(total[4]));
  }

  private static void voteProgress(final int yesvotes_h, final int yesvotes_s, final int l) {
    setText(R.id.houseY, yesvotes_h + " Yea");
    setText(R.id.houseN, l + 1 - yesvotes_h + " Nay");
    setText(R.id.senateY, yesvotes_s + " Yea");
    setText(R.id.senateN, Math.min(l / 4 + 1, i.senate.length()) - yesvotes_s + " Nay");
    pauseMs(WAIT);
  }

  /* FIXED: At the present time, Issue.CIVILRIGHTS has far too much sway. However, before this was
   * the case, as an example, ABORTION and WOMEN, had the same "return attitude[]" attribute, and
   * both returned Issue.WOMEN. What I think is needed, is some sort of indicators for things such
   * as: 1) Desire for change, which could be based on the approval rate of the president. 2)
   * Liberal, Conservative, and Stalinist percentage of the country, i.e. how much support each
   * party has. 3) Issue.HUMANRIGHTS, which should be based off of the two previous things, (i.e. A
   * higher liberal percentage, and a higher desire for change would make Issue.HUMANRIGHTS get
   * closer to 100.) -- LiteralKa ADDENDUM (20090812): Keeping this for historical purposes, and to
   * possibly improve future changes to this issue. */
  /* FIXME, PART1: HUMANRIGHTS is added as a sort of an indictator, but it relies on all the other
   * Human Rights issue, rather than affecting the issues to be more "pro-Human Rights".
   * Essentially, if you support Gay Rights but not Abortion Rights, you will not be considered as
   * someone who supports 'human rights'. ---Servant Corps ///// /////
   * @Servant: As it stands, (revision 316) the only alarming thing is that this may influence *s
   * that are affected by HUMANRIGHTS. This is only midly alarming because the * itself is effected,
   * and not the attitude[Issue.*]. -- LiteralKa */
  private static String voteToPercent(final int vote) {
    return vote / 10 + "." + vote % 10 + "%";
  }
}
