package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "INDUSTRY_POLLUTER") public @NonNullByDefault class Polluter extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Greenpeace Offices.";
  }

  @Override @Nullable public SpecialBlocks commonSpecial() {
    return SpecialBlocks.POLLUTER_EQUIPMENT;
  }

  @Override public void generateName(final Location l) {
    switch (i.rng.nextInt(5)) {
    default:
    case 0:
      l.setName("Aluminum Factory");
      break;
    case 1:
      l.setName("Plastic Factory");
      break;
    case 2:
      l.setName("Oil Refinery");
      break;
    case 3:
      l.setName("Auto Plant");
      break;
    case 4:
      l.setName("Chemical Factory");
      break;
    }
  }

  @Override public String lcsSiteOpinion() {
    return ", a factory whose Conservative smokestacks choke the city with deadly pollutants.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.LABOR, Issue.POLLUTION };
  }

  @Override public String randomLootItem() {
    return "LOOT_CHEMICAL";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
