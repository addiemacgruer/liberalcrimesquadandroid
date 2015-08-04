package lcs.android.game;

import lcs.android.util.Color;

/** Named values for various notions of <q>Quality</q>. The original C++ code used the values of
 * 0=NONE, 1=POOR, 2=GOOD throughout, which was a touch inscrutable in places */
public enum Quality {
  NONE(0, Color.YELLOW),
  POOR(1, Color.RED),
  GOOD(2, Color.GREEN);
  private Quality(final int ordinal, final Color aColor) {
    color = aColor;
    this.ordinal = ordinal;
  }

  private final int ordinal;

  private final Color color;

  /** Color associated with this quality.
   * @return a color. */
  public Color color() {
    return color;
  }

  public int level() {
    return ordinal;
  }
}