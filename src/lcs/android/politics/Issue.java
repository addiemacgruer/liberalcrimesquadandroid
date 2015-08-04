/**
 *
 */
package lcs.android.politics;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lcs.android.R;
import lcs.android.game.Game;
import android.util.Log;

/** Blends together the previous Views and Laws, which ended up a confusing mess...
 * @author addie */
public enum Issue {
  ABORTION(Alignment.LIBERAL),
  AMRADIO,
  ANIMALRESEARCH(Alignment.CONSERVATIVE),
  CABLENEWS,
  CEOSALARY,
  CORPORATECULTURE(Alignment.MODERATE),
  CIVILRIGHTS(Alignment.LIBERAL),
  CONSERVATIVECRIMESQUAD,
  DEATHPENALTY(Alignment.CONSERVATIVE),
  DRUGS(Alignment.CONSERVATIVE),
  ELECTIONS(Alignment.MODERATE),
  FLAGBURNING(Alignment.LIBERAL),
  FREESPEECH(Alignment.MODERATE),
  GAY(Alignment.LIBERAL),
  GENETICS,
  GUNCONTROL(Alignment.CONSERVATIVE),
  IMMIGRATION(Alignment.MODERATE),
  JUSTICES,
  LABOR(Alignment.MODERATE),
  LIBERALCRIMESQUAD,
  LIBERALCRIMESQUADPOS,
  MILITARY(Alignment.CONSERVATIVE),
  NUCLEARPOWER(Alignment.CONSERVATIVE),
  POLICEBEHAVIOR(Alignment.CONSERVATIVE),
  POLLUTION(Alignment.CONSERVATIVE),
  PRIVACY(Alignment.CONSERVATIVE),
  TAX(Alignment.MODERATE),
  TORTURE(Alignment.CONSERVATIVE),
  WOMEN(Alignment.LIBERAL);
  Issue() {
    core = false;
    gameStartAlignment = Alignment.MODERATE;
  }

  Issue(final Alignment gameStartAlignment) {
    core = true;
    this.gameStartAlignment = gameStartAlignment;
  }

  public final Alignment gameStartAlignment;

  public final boolean core;

  public String eruditeConservativeResponse() { // TODO fix
    return "\"" + stringArray(R.array.eruditeresponse)[ordinal()] + "\"";
  }

  public String eruditeOpinion() {// TODO fix
    return "\"" + stringArray(R.array.eruditeopinion)[ordinal()] + "\"";
  }

  public String getview() {
    switch (this) {
    case GAY:
      return "LGBT Rights";
    case DEATHPENALTY:
      return "The Death Penalty";
    case TAX:
      return "Taxes";
    case NUCLEARPOWER:
      return "Nuclear Power";
    case ANIMALRESEARCH:
      return "Animal Cruelty";
    case POLICEBEHAVIOR:
      return "The Police";
    case TORTURE:
      return "Torture";
    case PRIVACY:
      return "Privacy";
    case FREESPEECH:
      return "Free Speech";
    case GENETICS:
      return "Genetics";
    case JUSTICES:
      return "The Judiciary";
    case GUNCONTROL:
      return "Gun Control";
    case LABOR:
      return "Labor";
    case POLLUTION:
      return "Pollution";
    case CORPORATECULTURE:
      return "Corporate Culture";
    case CEOSALARY:
      return "CEO Compensation";
    case ABORTION:
      return "Gender Equality";
    case CIVILRIGHTS:
      return "Racial Equality";
    case DRUGS:
      return "Drugs";
    case IMMIGRATION:
      return "Immigration";
    case MILITARY:
      return "The Military";
    case AMRADIO:
      return "AM Radio";
    case CABLENEWS:
      return "Cable News";
    case LIBERALCRIMESQUAD:
      return "Who We Are";
    case LIBERALCRIMESQUADPOS:
      return "Why We Rock";
    case CONSERVATIVECRIMESQUAD:
      return "The CCS Criminals";
    default:
      return "WTF?";
    }
  }

  public String getviewsmall() {
    switch (this) {
    case GAY:
      return "LGBT rights";
    case DEATHPENALTY:
      return "the death penalty";
    case TAX:
      return "taxes";
    case NUCLEARPOWER:
      return "nuclear power";
    case ANIMALRESEARCH:
      return "animal cruelty";
    case POLICEBEHAVIOR:
      return "the cops";
    case TORTURE:
      return "torture";
    case PRIVACY:
      return "privacy";
    case FREESPEECH:
      return "free speech";
    case GENETICS:
      return "genetic research";
    case JUSTICES:
      return "judges";
    case GUNCONTROL:
      return "gun control";
    case LABOR:
      return "labor rights";
    case POLLUTION:
      return "pollution";
    case CORPORATECULTURE:
      return "corporations";
    case CEOSALARY:
      return "CEO compensation";
    case ABORTION:
      return "gender equality";
    case CIVILRIGHTS:
      return "racial equality";
    case DRUGS:
      return "drugs";
    case IMMIGRATION:
      return "immigration";
    case MILITARY:
      return "the military";
    case AMRADIO:
      return "AM radio";
    case CABLENEWS:
      return "cable news";
    case LIBERALCRIMESQUAD:
      return "the LCS";
    case LIBERALCRIMESQUADPOS:
      return "the LCS";
    case CONSERVATIVECRIMESQUAD:
      return "the CCS";
    default:
      return "wtf";
    }
  }

  public String moderatelyStupidOpinion() {// TODO fix
    return "\"" + stringArray(R.array.dimopinion)[ordinal()] + "\"";
  }

  public String stupidOpinion() {// TODO fix
    return "\"" + stringArray(R.array.stupidopinion)[ordinal()] + "\"";
  }

  @Override public String toString() {// TODO fix
    return stringArray(R.array.lawstostring)[ordinal()];
  }

  public Issue viewForLaw() { // TODO inline.
    return this;
  }

  // static Map<Issue, Integer> interest_array;
  // static int total_interest = 0;
  // static Issue[] views;
  private static final Issue[] coreValues;
  static {
    final List<Issue> cv = new ArrayList<Issue>();
    for (final Issue v : Issue.values()) {
      if (v.core) {
        cv.add(v);
      }
    }
    coreValues = cv.toArray(new Issue[cv.size()]);
  }

  private static int total_interest;

  private static Map<Issue, Integer> interest_array;

  private static Issue[] views = new Issue[Issue.values().length];

  public static Issue[] coreValues() {
    return coreValues.clone();
  }

  public static Issue randomissue() {
    final int roll = i.rng.nextInt(Issue.total_interest);
    for (final Issue j : Issue.views) {
      if (roll > Issue.interest_array.get(j)) {
        continue;
      }
      return j;
    }
    Log.e(Game.LCS, "CommonActions.randomissue random issue didn't pick : roll=" + roll);
    return Issue.views[0]; // shouldn't get here, but avoid NPEs
  }

  public static void randomissueinit(final boolean core_only) {
    // int interest_array[VIEWNUM];
    Issue.interest_array = new EnumMap<Issue, Integer>(Issue.class);
    Issue.total_interest = 0;
    Issue.views = core_only ? coreValues() : values();
    for (final Issue j : Issue.views) {
      Issue.interest_array.put(j, i.issue(j).publicInterest() + Issue.total_interest + 25);
      Issue.total_interest += i.issue(j).publicInterest() + 25;
    }
  }
}
