package lcs.android.util;

/** Maps some named colors to their Android values, id est AARRGGBB. This version uses a standard
 * Android resource file in order to provide the DeluxPaint colors, since we've got an Amiga theme
 * on the go
 * <p>
 * Original LCS used the EGA? color set, hence the 8-color limit. */
public enum Color {
  BLACK(), //
  BLACKBLACK() {
    @Override int androidValue() {
      return 0xff000000;
    }
  }, //
  BLUE(), //
  CYAN(), //
  GREEN(), //
  INVISIBLE() {
    @Override int androidValue() {
      return 0x00000000;
    }
  }, //
  MAGENTA(), //
  RED(), //
  WHITE(), //
  YELLOW();
  /** The android value of a given colour.
   * @return a 32-bit int, as AARRGGBB. */
  int androidValue() {
    return ThemeName.currentTheme.intOfColor(this);
  }
}
