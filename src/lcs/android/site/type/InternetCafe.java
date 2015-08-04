package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "BUSINESS_INTERNETCAFE") public @NonNullByDefault class InternetCafe extends
    AbstractSiteType {
  @Override public void allocateMap(final LcsRandom SITERNG) {
    AbstractSiteType.type(JuiceBar.class).allocateMap(SITERNG);
  }

  @Override @Nullable public SpecialBlocks commonSpecial() {
    return SpecialBlocks.CAFE_COMPUTER;
  }

  @Override public void generateName(final Location l) {
    l.setName(i.rng.choice("Electric", "Wired", "Nano", "Micro", "Techno") + " "
        + i.rng.choice("Panda", "Troll", "Latte", "Unicorn", "Pixie") + " Internet Cafe");
  }

  @Override public boolean hasLoot() {
    return false;
  }

  @Override public boolean hasTables() {
    return true;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
