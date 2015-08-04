package lcs.android.site.creation;

import lcs.android.game.Game;
import lcs.android.items.LootType;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

@NonNullByDefault class ConfigSiteLoot extends AbstractConfigSiteCommand {
  @Nullable private LootType loot;

  private int weight;

  @Override public String toString() {
    return "Loot:" + loot + "/" + weight;
  }

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {
    if (loot == null) {
      Log.e(Game.LCS, "ConfigSiteLoot: loot unspecified.");
    }
  }

  // Adds a loot type during map creation
  @Override public void xmlSet(final String key, final String value) {
    if ("type".equals(key)) {
      loot = Game.type.loot.get("LOOT_" + value);
      if (loot == null) {
        Log.e(Game.LCS, "ConfigSiteLoot: unknown loottype:" + value);
      }
    } else if ("weight".equals(key)) {
      weight = Xml.getInt(value);
    } else {
      super.xmlSet(key, value);
    }
  }

  @Override protected void build() {
    // currently no-op until loot system is revised
  }
}