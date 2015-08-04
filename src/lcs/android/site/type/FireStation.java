package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "GOVERNMENT_FIRESTATION") public @NonNullByDefault class FireStation extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "ACLU Branch Office.";
  }

  @Override public void generateName(final Location l) {
    if (!i.freeSpeech()) {
      l.setName("Fireman HQ");
    } else {
      l.setName("Fire Station");
    }
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(25))
      return "ARMOR_BUNKERGEAR";
    else if (i.rng.likely(2))
      return "LOOT_TRINKET";
    else
      return "LOOT_COMPUTER";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
