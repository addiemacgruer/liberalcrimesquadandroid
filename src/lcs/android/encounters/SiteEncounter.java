/**
 *
 */
package lcs.android.encounters;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** @author addie */
public @NonNullByDefault class SiteEncounter extends Encounter {
  @Override public boolean encounter() {
    Log.w("LCS", "Encountered a site encounter.");
    return true;
  }

  private static final long serialVersionUID = Game.VERSION;
}
