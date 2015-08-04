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
          if (!lhs.location().exists() || !rhs.location().exists())
            return 0;
          final int sqcmp = lhs.location().get().toString()
              .compareTo(rhs.location().get().toString());
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
          if (lhs.squad().getNullable() == null && rhs.squad().getNullable() == null)
            return 0;
          if (lhs.squad().getNullable() == null)
            return 1;
          if (rhs.squad().getNullable() == null)
            return -1;
          final int sqcmp = lhs.squad().get().toString().compareTo(rhs.squad().get().toString());
          if (sqcmp != 0)
            return sqcmp;
          return lhs.toString().compareTo(rhs.toString());
        }
      });
    }
  };
  public abstract void sort(List<Creature> list);
}