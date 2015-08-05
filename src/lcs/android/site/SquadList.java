/**
 *
 */
package lcs.android.site;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import android.util.Log;

/** @author addie */
public class SquadList implements Serializable, Iterable<Squad> {
  private final LinkedList<Squad> squads = new LinkedList<Squad>();

  public Squad add(Squad squad) {
    if (!squads.contains(squad)) {
      squads.add(squad);
    }
    return squad;
  }

  public Squad current() {
    return squads.getFirst();
  }

  /** @param i
   * @return */
  public Squad get(int i) {
    return squads.get(i);
  }

  @Override public Iterator<Squad> iterator() {
    purgeEmpty();
    return squads.iterator();
  }

  public Squad next() {
    purgeEmpty();
    return noPurgeNext();
  }

  public Squad select(Squad activeSquad) {
    if (!squads.contains(activeSquad)) {
      squads.add(activeSquad);
    }
    while (current() != activeSquad) {
      noPurgeNext();
    }
    return activeSquad;
  }

  /** @return */
  public int size() {
    return squads.size();
  }

  private Squad noPurgeNext() {
    squads.add(squads.removeFirst());
    return squads.peek();
  }

  private void purgeEmpty() {
    if (squads.size() == 1) {
      return; // don't purge last squad
    }
    for (Iterator<Squad> i = squads.iterator(); i.hasNext();) {
      final Squad next = i.next();
      if (next.isEmpty()) {
        final Location squadLocation = next.location().get();
        if (squadLocation != Location.none()) {
          squadLocation.lcs().loot.addAll(next.loot());
          next.loot().clear();
        }
        Log.i("LCS", "Purged squad because it is empty:" + next);
        i.remove();
      }
    }
    if (squads.isEmpty()) {
      squads.add(new Squad());
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
