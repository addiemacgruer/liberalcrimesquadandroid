package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "LABORATORY_COSMETICS") public @NonNullByDefault class Cosmetics extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Animal Shelter";
  }

  @Override @Nullable public SpecialBlocks commonSpecial() {
    return SpecialBlocks.LAB_COSMETICS_CAGEDANIMALS;
  }

  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + " Cosmetics");
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", a Conservative animal rights abuser.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.ANIMALRESEARCH, Issue.ABORTION };
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(20)) {
      return "LOOT_RESEARCHFILES";
    } else if (i.rng.chance(2)) {
      return "LOOT_LABEQUIPMENT";
    } else if (i.rng.chance(2)) {
      return "LOOT_COMPUTER";
    } else if (i.rng.chance(5)) {
      return "LOOT_PDA";
    } else if (i.rng.chance(5)) {
      return "LOOT_CHEMICAL";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
