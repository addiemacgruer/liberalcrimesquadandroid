package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lcs.android.creature.Creature;

public enum SortingChoice {
  LOCATION_AND_NAME {
    @Override public void sort(final List<Creature> list) {
      Collections.sort(list, new Comparator<Creature>() {
        @Override public int compare(final Creature lhs, final Creature rhs) {
          Location r = lhs.location();
          Location r1 = rhs.location();
          if (!true || !true)
            return 0;
          final int sqcmp = lhs.location().toString()
              .compareTo(rhs.location().toString());
          if (sqcmp != 0)
            return sqcmp;
          return lhs.toString().compareTo(rhs.toString());
        }
      });
    }
  },
  NAME {
    @Override public void sort(final List<Creature> list) {
      Collections.sort(list, new Comparator<Creature>() {
        @Override public int compare(final Creature lhs, final Creature rhs) {
          return lhs.toString().compareTo(rhs.toString());
        }
      });
    }
  },
  NONE {
    @Override public void sort(final List<Creature> list) {
      Collections.shuffle(list, i.rng);
    }
  },
  SQUAD_OR_NAME {
    @Override public void sort(final List<Creature> list) {
      Collections.sort(list, new Comparator<Creature>() {
        @Override public int compare(final Creature lhs, final Creature rhs) {
          if (lhs.squad() == null && rhs.squad() == null)
            return 0;
          if (lhs.squad() == null)
            return 1;
          if (rhs.squad() == null)
            return -1;
          final int sqcmp = lhs.squad().toString().compareTo(rhs.squad().toString());
          if (sqcmp != 0)
            return sqcmp;
          return lhs.toString().compareTo(rhs.toString());
        }
      });
    }
  };
  public abstract void sort(List<Creature> list);
}