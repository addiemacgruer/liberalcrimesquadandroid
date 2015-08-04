package lcs.android.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class RomanNumeral {
  public RomanNumeral(final int number) {
    this.number = number;
  }

  private final int number;

  @Override public String toString() {
    int x = number;
    final StringBuilder rval = new StringBuilder();
    for (int i = 0; i < RomanNumeral.values.length; i++) {
      while (x > RomanNumeral.values[i]) {
        x -= RomanNumeral.values[i];
        rval.append(numerals[i]);
      }
    }
    return rval.toString();
  }

  private static final String[] numerals = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX",
      "V", "IV", "I" };

  private static final int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
}
