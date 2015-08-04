package lcs.android.game;

import static lcs.android.game.Game.*;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.politics.Alignment;
import lcs.android.scoring.EndType;
import lcs.android.scoring.HighScore;
import lcs.android.site.Squad;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

/** Determine what you can see at this point in time. {@link #resolveVision} gets called every day by
 * basemode, and updates the status as necessary in {@link Game#visibility}. {@link #DISBANDING}
 * causes time to fly by, and prompts you to reform the LCS at the end of each month. If you've
 * anyone who can take any actions at all, then it gets set to {@link #CAN_SEE} and you can take
 * actions; otherwise a suitable other value is set and the calendar rolls by until that's resolved.
 * <p>
 * The other values don't make much difference, but if we have a gameover during periods of
 * no-visibility it'll show up in our high scores list. */
public enum Visibility {
  CAN_SEE,
  DISBANDING,
  IN_CLINIC,
  IN_HIDING,
  IN_JAIL,
  VACATION,
  UNDECIDED;
  private static final String[] PHRASES = { "Corporate Accountability", "Deregulation", //
      "Free Speech", "Child Safety", "Thoughtcrime", //
      "Gay Marriage", "Sanctity of Marriage", //
      "Abortion Rights", "Right to Life", //
      "Separation Clause", "Under God", //
      "Racial Equality", "Emmett Till", //
      "Gun Control", "Second Amendment", "Firing Squad", //
      "Campaign Finance Reform", "Freedom to Campaign", //
      "Animal Rights", "Animal Abuse", //
      "Worker's Rights", "Right to Work", "Gulag", //
      "Police Responsibility", "Rodney King", "Red Guard", //
      "Global Warming", "Self-Regulation", //
      "Immigration Reform", "Border Control", "Berlin Wall", //
      "Human Rights", "National Security", "Reeducation", //
      "Woman's Suffrage", "Traditional Gender Roles", //
      "Right To Privacy", "Wiretapping", "Big Brother", //
      "Medical Marijuana", "War on Drugs", "Soma", //
      "Flag Burning", "Patriotism", "Daily Speech", //
      "Life Imprisonment", "Zero Tolerance", "Mass Grave", //
      "Conflict Resolution", "Preemptive Strike", "Cuban Missile Crisis", //
      "Radiation Poisoning", "Nuclear Power", "Arms Race", //
      "Tax Bracket", "Flat Tax", "Proletariat" };

  /** prompts the player whether it's time to disband the LCS */
  public static void confirmDisband() {
    final String keyphrase = i.rng.choice(PHRASES);
    final String confirm = Curses.query("Are you sure you want to disband?\n\n"
        + "Disbanding scatters the Liberal Crime Squad, sending all of its members "
        + "into hiding, free to pursue their own lives.  You will be able to observe "
        + "the political situation in brief, and wait until a resolution is reached.\n\n"
        + "If at any time you determine that the Liberal Crime Squad will be needed "
        + "again, you may return to the homeless shelter to restart the campaign.\n\n"
        + "Do not make this decision lightly.  If you do need to return to action, "
        + "only the most devoted of your former members will return.\n\n" + "Phrase to confirm: \""
        + keyphrase + "\"", "");
    if (confirm.equals(keyphrase)) {
      Curses.fact("LCS disbanded.");
      // SET UP THE DISBAND
      for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
        if (!p.health().alive()) {
          i.pool.remove(p);
        }
        if (!p.hasFlag(CreatureFlag.SLEEPER)) {
          p.removeSquadInfo();
          p.hiding(-1);
        }
      }
      Squad.cleanGoneSquads();
      i.disbandYear = i.score.date.year();
      i.visibility = Visibility.DISBANDING;
      return;
    }
    Curses.fact("LCS not disbanded.");
    return;
  }

  /** Confirm whether you want to quit. May not return. */
  public static void confirmQuit() {
    final String keyphrase = i.rng.choice(PHRASES);
    final String confirm = Curses.query("Are you sure you want to quit?\n\n"
        + "Please enter the following phrase to confirm.\n\n\"" + keyphrase + "\"", "");
    if (confirm.equals(keyphrase)) {
      HighScore.savehighscore(EndType.QUIT);
      endGame();
    }
    return;
  }

  /** Determines whether we can take any actions in base mode. If {@link #DISBANDING}, just return.
   * Otherwise determine whether any of our Liberals {@link #CAN_SEE}, or whether they're tied up
   * somewhere. */
  public static void resolveVision() {
    if (i.visibility != DISBANDING) {
      i.visibility = UNDECIDED;
      Creature theboss = null;
      for (final Creature p : Filter.of(i.pool, Filter.LIBERAL)) {
        if (!p.hire().exists()) {
          theboss = p;
        }
        if (p.health().alive() && p.alignment() == Alignment.LIBERAL && p.datingVacation() == 0
            && p.hiding() == 0 && p.health().clinicMonths() == 0
            && !p.hasFlag(CreatureFlag.SLEEPER) && p.location().exists()
            && !p.location().get().type().isPrison()) {
          i.visibility = CAN_SEE;
        }
      }
      if (theboss == null)
        return;
      if (i.visibility == UNDECIDED) {
        if (theboss.datingVacation() != 0) {
          i.visibility = VACATION;
        } else if (theboss.hiding() != 0) {
          i.visibility = IN_HIDING;
        } else if (theboss.health().clinicMonths() != 0) {
          i.visibility = IN_CLINIC;
        } else {
          i.visibility = IN_JAIL;
        }
      }
    }
  }
}