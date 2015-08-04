package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "INDUSTRY_NUCLEAR") public @NonNullByDefault class Nuclear extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Whirled Peas Museum.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.NUCLEAR_ONOFF;
  }

  @Override public void generateName(final Location l) {
    if (i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
      l.setName("Nuclear Waste Center");
    } else {
      l.setName("Nuclear Power Plant");
    }
  }

  @Override public String lcsSiteOpinion() {
    return ", also known to be a Conservative storage facility for radioactive waste.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.NUCLEARPOWER };
  }

  @Override public String randomLootItem() {
    return AbstractSiteType.type(Cosmetics.class).randomLootItem();
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
