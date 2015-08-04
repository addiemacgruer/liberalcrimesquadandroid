package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "LABORATORY_GENETIC") public @NonNullByDefault class Genetic extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Research Ethics Commission HQ.";
  }

  @Override @Nullable public SpecialBlocks commonSpecial() {
    return SpecialBlocks.LAB_GENETIC_CAGEDANIMALS;
  }

  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + " Genetics");
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", a dangerous Conservative genetic research lab.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.ANIMALRESEARCH, Issue.GENETICS };
  }

  @Override public String randomLootItem() {
    return AbstractSiteType.type(Cosmetics.class).randomLootItem();
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
