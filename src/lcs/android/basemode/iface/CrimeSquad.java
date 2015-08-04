package lcs.android.basemode.iface;

import lcs.android.creature.Uniform;
import lcs.android.game.Game;
import lcs.android.util.Color;
import lcs.android.util.DefaultValueKey;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault enum CrimeSquad implements Configurable, DefaultValueKey<Boolean> {
  AMRADIO(Color.RED),
  CABLENEWS(Color.RED),
  CCS(Color.RED),
  CIA(Color.MAGENTA),
  CORPORATE(Color.CYAN),
  FIREMEN(Color.YELLOW),
  HICKS(Color.RED),
  LCS(Color.GREEN),
  POLICE(Color.BLUE),
  NO_ONE(Color.BLACK);
  private CrimeSquad(final Color color) {
    this.color = color;
  }

  public final transient Uniform uniform = new Uniform();

  private final Color color;

  @Override public Boolean defaultValue() {
    return offended;
  }

  @Override public Configurable xmlChild(final String value) {
    if (value.equals("uniform")) {
      return uniform;
    }
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    // no code
  }

  @Override public void xmlSet(final String key, final String value) {
    Log.e(Game.LCS, "Set key on CrimeSquads:" + key + "=" + value);
  }

  protected Color color() {
    return color;
  }

  public final static Configurable configurable = new Configurable() {
    @Override public Configurable xmlChild(final String value) {
      return CrimeSquad.valueOf(value);
    }

    @Override public void xmlFinishChild() {
      // no code
    }

    @Override public void xmlSet(final String key, final String value) {
      Log.e(Game.LCS, "Tried to set CSq configureable");
    }
  };

  private static final boolean offended = false;
}