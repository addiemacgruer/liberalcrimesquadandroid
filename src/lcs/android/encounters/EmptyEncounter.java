/**
 *
 */
package lcs.android.encounters;

import java.util.List;

import lcs.android.creature.Creature;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** @author addie */
public @NonNullByDefault class EmptyEncounter extends Encounter {
  @Override public List<Creature> creatures() {
    Log.w("LCS", "Got empty encounter creatures", new Exception());
    return encounter;
  }

  @Override public boolean encounter() {
    Log.w("LCS", "Escaped an empty encounter.");
    return true; // no encounter, everyone escapes.
  }

  @Override public boolean isEmpty() {
    return true;
  }

  @Override public void printEncounter() {
    Log.w("LCS", "Printed an empty encounter.");
  }

  private static final long serialVersionUID = Game.VERSION;
}
