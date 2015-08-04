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

@Xml.Name(name = "CORPORATE_HOUSE") public @NonNullByDefault class House extends AbstractSiteType {
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
    return "Richard Dawkins Food Bank.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.HOUSE_PHOTOS;
  }

  @Override public void generateName(final Location l) {
    if (i.issue(Issue.CORPORATECULTURE).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.TAX).law() == Alignment.ARCHCONSERVATIVE) {
      l.setName("CEO Castle");
    } else {
      l.setName("CEO Residence");
    }
  }

  @Override public String lcsSiteOpinion() {
    return ", a building with enough square footage enough to house a hundred people if it weren't in Conservative Hands.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.TAX, Issue.CEOSALARY };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(50)) {
      return i.rng.choice("ARMOR_EXPENSIVEDRESS", "ARMOR_EXPENSIVESUIT", "ARMOR_EXPENSIVESUIT",
          "ARMOR_EXPENSIVESUIT", "ARMOR_BONDAGEGEAR");
    }
    if (i.rng.chance(8)) {
      return "LOOT_TRINKET";
    } else if (i.rng.chance(7)) {
      return "LOOT_WATCH";
    } else if (i.rng.chance(6)) {
      return "LOOT_PDA";
    } else if (i.rng.chance(5)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(4)) {
      return "LOOT_SILVERWARE";
    } else if (i.rng.chance(3)) {
      return "LOOT_CHEAPJEWELERY";
    } else if (i.rng.chance(2)) {
      return "LOOT_FAMILYPHOTO";
    }
    return "LOOT_COMPUTER";
  }

  @Override public String siegeUnit() {
    return "MERC";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
