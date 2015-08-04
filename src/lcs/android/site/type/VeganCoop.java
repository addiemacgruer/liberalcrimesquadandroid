package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_VEGANCOOP") public @NonNullByDefault class VeganCoop extends
    AbstractSiteType {
  @Override public void allocateMap(final LcsRandom SITERNG) {
    AbstractSiteType.type(JuiceBar.class).allocateMap(SITERNG);
  }

  @Override public void generateName(final Location l) {
    l.setName(i.rng.choice("Asparagus", "Tofu", "Broccoli", "Radish", "Eggplant") + " "
        + i.rng.choice("Forest", "Rainbow", "Garden", "Farm", "Meadow") + " Vegan Co-op");
  }

  @Override public boolean hasLoot() {
    return false;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
