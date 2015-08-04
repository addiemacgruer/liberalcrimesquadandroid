package lcs.android.game;

import static lcs.android.game.Game.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Produces a random number in a range.
 * <p>
 * A number of the xml files require that we produce a random number in the form
 * <q>fixed+random</q>, exampli gratia Agent skill in Shotgun is
 * <q>8+2</q>. This stores the numbers are integers rather than as a string, so we're not parsing it
 * each time, and lets us get a value from it easily.
 * <p>
 * Ranges are inclusive, so 8+2 returns either 8, 9, or 10. */
public @NonNullByDefault class Range {
  /** Creates a new random number range object.
   * @param s A string of the form either <q>fixed+random</q>, or <q>fixed</q>, probably obtained
   *          from an xml file. */
  public Range(final String s) {
    if (s.contains("+")) {
      final String[] vals = s.split("\\+");
      range = Integer.parseInt(vals[0]);
      base = Integer.parseInt(vals[1]);
    } else {
      base = Integer.parseInt(s);
      range = 0;
    }
  }

  private Range(final int base, final int range) {
    this.base = base;
    this.range = range;
  }

  public final int base;

  public final int range;

  /** Get a value between fixed and fixed+range, inclusive.
   * @return A suitable value in the range, */
  public int aValue() {
    if (range == 0) {
      return base;
    }
    return i.rng.nextInt(range + 1) + base;
  }

  private static Range ZERO = new Range(0, 0);

  /** @param i
   * @return */
  public static Range of(int base) {
    if (base == 0) {
      return ZERO;
    }
    return new Range(base, 0);
  }

  /** @param i
   * @return */
  public static Range of(int base, int range) {
    if (base == 0 && range == 0) {
      return ZERO;
    }
    return new Range(base, range);
  }
}