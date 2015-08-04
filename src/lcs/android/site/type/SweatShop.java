package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "INDUSTRY_SWEATSHOP") public @NonNullByDefault class SweatShop extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Labor Union HQ.";
  }

  @Override @Nullable public SpecialBlocks commonSpecial() {
    return SpecialBlocks.SWEATSHOP_EQUIPMENT;
  }

  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + " Garment Makers");
  }

  @Override public String lcsSiteOpinion() {
    return ", a Conservative sweatshop and human rights abuser.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.LABOR, Issue.IMMIGRATION };
  }

  @Override public String randomLootItem() {
    return "LOOT_FINECLOTH";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}