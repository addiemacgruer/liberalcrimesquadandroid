package lcs.android.site.creation;

import lcs.android.game.Game;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

abstract @NonNullByDefault class AbstractConfigSiteCommand implements Configurable {
  @Override public String toString() {
    return "ConfigSiteCommand:";
  }

  @Override public void xmlSet(final String key, final String value) {
    Log.w(Game.LCS, "Unknown K/V in sitemap configuration:" + key + "=" + value);
  }

  protected abstract void build();
}