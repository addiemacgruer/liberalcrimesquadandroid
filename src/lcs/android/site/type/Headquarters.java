package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "CORPORATE_HEADQUARTERS") public @NonNullByDefault class Headquarters extends
    AbstractSiteType {
  @Override public String alarmResponseString() {
    return ": MERCENARIES RESPONDING";
  }

  @Override public String carChaseCar() {
    return i.rng.choice("VEHICLE_SUV", "VEHICLE_JEEP");
  }

  @Override public String carChaseCreature() {
    return "MERC";
  }

  @Override public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 5 + 1) + 1;
  }

  @Override public int carChaseMax() {
    return 6;
  }

  @Override public String ccsSiteName() {
    return "Welfare Assistance Agency.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.CORPORATE_FILES;
  }

  @Override public void generateName(final Location l) {
    l.setName("Corporate HQ");
  }

  @Override public String lcsSiteOpinion() {
    return ", where evil and Conservatism coagulate in the hallways.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.TAX, Issue.CORPORATECULTURE, Issue.ABORTION };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(50)) {
      return "LOOT_CORPFILES";
    } else if (i.rng.chance(3)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(2)) {
      return "LOOT_PDA";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  @Override public String siegeUnit() {
    return "MERC";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
