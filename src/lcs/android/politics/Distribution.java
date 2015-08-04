package lcs.android.politics;

import java.io.Serializable;
import java.util.Arrays;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Distribution implements Serializable {
  private final int[] distribution = new int[5];

  public int[] headcount() {
    final int total = length();
    final int[] rval = new int[total];
    for (int s = 0; s < total; s++) {
      if (s < distribution[0]) {
        rval[s] = -2;
      } else if (s < distribution[0] + distribution[1]) {
        rval[s] = -1;
      } else if (s < distribution[0] + distribution[1] + distribution[2]) {
        rval[s] = 0;
      } else if (s < distribution[0] + distribution[1] + distribution[2] + distribution[3]) {
        rval[s] = 1;
      } else {
        rval[s] = 2;
      }
    }
    return rval;
  }

  public int[] makeup() {
    final int[] rval = new int[distribution.length];
    java.lang.System.arraycopy(distribution, 0, rval, 0, distribution.length);
    return rval;
  }

  public void set(final int ac, final int c, final int m, final int l, final int el) {
    distribution[0] = ac;
    distribution[1] = c;
    distribution[2] = m;
    distribution[3] = l;
    distribution[4] = el;
  }

  public void setFromHeadCount(final int[] hc) {
    Arrays.fill(distribution, 0);
    for (final int h : hc) {
      distribution[h + 2]++;
    }
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Distribution:");
    for (final int x : distribution) {
      sb.append(x).append(',');
    }
    return sb.toString();
  }

  int length() {
    int total = 0;
    for (final int x : distribution) {
      total += x;
    }
    return total;
  }

  private static final long serialVersionUID = Game.VERSION;
}
