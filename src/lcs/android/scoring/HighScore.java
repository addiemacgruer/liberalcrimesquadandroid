package lcs.android.scoring;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lcs.android.R;
import lcs.android.game.Game;
import lcs.android.util.Color;
import lcs.android.util.Statics;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.content.Context;
import android.util.Log;

public @NonNullByDefault class HighScore implements Serializable {
  public int ccsKills;

  public int ccsSiegeKills;

  public final LcsDate date = new LcsDate(1, 1, 2009);

  public int dead;

  public int flagsBought;

  public int flagsBurnt;

  public int funds;

  public int kidnappings;

  public int kills;

  public int recruits;

  public String slogan = "We need a slogan!";

  public int spent;

  @Nullable private EndType endtype;

  @Override public String toString() {
    final StringBuilder str = new StringBuilder();
    if (endtype != EndType.HIGH_SCORE_AGGREGATE) {
      str.append("\"" + slogan + "\"\n");
      if (endtype == null) {
        str.append("The Liberal Crime Squad is active in ");
      } else {
        str.append(endtype);
      }
      LcsDate.monthName(str, date.month());
      str.append(" " + date.year() + ".  ");
    }
    str.append("Recruits: " + recruits);
    str.append(" Martyrs: " + dead);
    str.append(" Kills: " + kills);
    str.append(" Kidnappings: " + kidnappings);
    str.append(" Taxed: $" + funds);
    str.append(" Spent: $" + spent);
    str.append(" Flags Bought: " + flagsBought);
    str.append(" Flags Burned: " + flagsBurnt);
    return str.toString();
  }

  private final static String HIGHSCORESFILE = "highscores.object";

  private static final long serialVersionUID = 1;

  public static void savehighscore(final EndType endtype) {
    final List<HighScore> highScoresArray = loadhighscores();
    // PLACE THIS HIGH SCORE BY DATE IF NECESSARY
    i.score.endtype = endtype;
    highScoresArray.add(i.score);
    Collections.sort(highScoresArray, new Comparator<HighScore>() {
      @Override public int compare(final @Nullable HighScore lhs, final @Nullable HighScore rhs) {
        assert lhs != null;
        assert rhs != null;
        return lhs.date.compareTo(rhs.date);
      }
    });
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(Statics.instance().openFileOutput(HIGHSCORESFILE,
          Context.MODE_PRIVATE));
      out.writeObject(highScoresArray);
    } catch (final IOException e) {
      Log.e(Game.LCS, "HighScores.savehighscore", e);
      return;
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (final IOException ioe) {
        Log.e(Game.LCS, "Highscores problem", ioe);
      }
    }
  }

  /* loads the high scores file */
  /* displays the high score board */
  public static void viewhighscores() {
    final List<HighScore> highScoresArray = HighScore.loadhighscores();
    setView(R.layout.generic);
    ui().text("The Liberal ELITE").bold().color(Color.GREEN).add();
    final HighScore u = new HighScore();
    u.endtype = EndType.HIGH_SCORE_AGGREGATE;
    for (final HighScore s : highScoresArray) {
      ui().text(s.toString()).add();
      u.recruits += s.recruits;
      u.dead += s.dead;
      u.kills += s.kills;
      u.kidnappings += s.kidnappings;
      u.funds += s.funds;
      u.spent += s.spent;
      u.flagsBought += s.flagsBought;
      u.flagsBurnt += s.flagsBurnt;
    }
    // UNIVERSAL STATS
    ui().text("Universal Liberal Statistics:").bold().color(Color.GREEN).add();
    ui().text(u.toString()).add();
    ui(R.id.gcontrol).button(' ').text("Continue the struggle.").add();
    getch();
  }

  @SuppressWarnings("unchecked") private static List<HighScore> loadhighscores() {
    List<HighScore> highScoresArray = null;
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(Statics.instance().openFileInput(HIGHSCORESFILE));
      highScoresArray = (List<HighScore>) in.readObject();
    } catch (final IOException e) {
      Log.e(Game.LCS, "HighScore.loadhighscores:", e);
      return new ArrayList<HighScore>();
    } catch (final Exception exception) {
      Log.e("LCS", "Couldn't read high scores", exception);
      highScoresArray = new ArrayList<HighScore>();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (final IOException ioe) {
          Log.e("LCS", "Couldn't close high scores", ioe);
        }
      }
    }
    return highScoresArray;
  }
}