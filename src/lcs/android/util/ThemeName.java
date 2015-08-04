package lcs.android.util;

import lcs.android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/** Adds user-defined "themes" to LCS. Had some comments from users that Amiga-themed is not very
 * colour-blind friendly.
 * @author addie */
public enum ThemeName {
  /** Default palette set for an Amiga.
   * @author addie */
  AMIGA("Amiga") {
    @Override public int intOfColor(final Color color) {
      switch (color) {
      case BLACK:
      default:
        return 0xff000000;
      case WHITE:
        return 0xffffffff;
      case BLUE:
        return 0xff0000aa;
      case CYAN:
        return 0xff00a7ad;
      case GREEN:
        return 0xff00ff09;
      case MAGENTA:
        return 0xffff00a8;
      case RED:
        return 0xffa80309;
      case YELLOW:
        return 0xffbbbb00;
      }
    }

    @Override public int styleName() {
      return R.style.amigaos;
    }
  },
  /** Default colour set for Android.
   * @author addie */
  ANDROID("Android") {
    @Override public int intOfColor(final Color color) {
      switch (color) {
      case BLACK:
      default:
        return 0xffffffff;
      case WHITE:
        return 0xff000000;
      case RED:
        return 0xffff0000;
      case BLUE:
        return 0xff0000ff;
      case GREEN:
        return 0xff00ff00;
      case CYAN:
        return 0xff00ffff;
      case MAGENTA:
        return 0xffff00ff;
      case YELLOW:
        return 0xffffff00;
      }
    }

    @Override public int styleName() {
      return R.style.android;
    }
  },
  /** Default colour set for DOS.
   * @author addie */
  DOS("Win3.1") {
    @Override public int intOfColor(final Color color) {
      switch (color) {
      case BLACK:
      case WHITE:
      default:
        return 0xff000000;
      case RED:
        return 0xffaa0000;
      case YELLOW:
        return 0xffaaaa00;
      case GREEN:
        return 0xff00aa00;
      case BLUE:
        return 0xff0000aa;
      case MAGENTA:
        return 0xffaa00aa;
      case CYAN:
        return 0xff00aaaa;
      }
    }

    @Override public int styleName() {
      return R.style.dos;
    }
  };
  private ThemeName(final String toString) {
    mToString = toString;
  }

  private final String mToString;

  /** Change the theme to this style, and save the change in Android preferences.
   * @return true if the theme needed to be changed. */
  public boolean changeStyle() {
    if (currentTheme == this)
      return false;
    Log.i("Theme", "Changing theme:" + this);
    ThemeName.currentTheme = this;
    final SharedPreferences sharedPref = Statics.instance().getPreferences(Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString("theme", super.toString());
    editor.commit();
    // Statics.instance().setTheme(styleName());
    return true;
  }

  /** Returns the android colour value for the named Color.
   * @param color a named Color.
   * @return an AARRGGBB value. */
  public abstract int intOfColor(Color color);

  /** Name of the style.
   * @return */
  public abstract int styleName();

  @Override public String toString() {
    return mToString;
  }

  public static ThemeName currentTheme;

  /** Restores the theme saved in the Android preferences. */
  static void restoreTheme() {
    final SharedPreferences sharedPref = Statics.instance().getPreferences(Context.MODE_PRIVATE);
    final String theme = sharedPref.getString("theme", "AMIGA");
    // ThemeName.valueOf(theme).changeStyle();
    currentTheme = ThemeName.valueOf(theme);
  }
}
