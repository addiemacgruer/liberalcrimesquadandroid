package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "RESIDENTIAL_APARTMENT_UPSCALE") public @NonNullByDefault class ApartmentUpscale
    extends Apartment {
  @Override public String ccsSiteName() {
    return "University Dormitory.";
  }

  @Override public String lcsSiteOpinion() {
    return ", known for its rich and snooty residents.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.TAX, Issue.CEOSALARY, Issue.GUNCONTROL };
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(30))
      return i.rng.choice("WEAPON_BASEBALLBAT", "WEAPON_KNIFE", "WEAPON_DAISHO",
          "WEAPON_SHOTGUN_PUMP", "WEAPON_REVOLVER_44", "WEAPON_SEMIPISTOL_45",
          "WEAPON_SEMIRIFLE_AR15", "WEAPON_AUTORIFLE_M16");
    else if (i.rng.chance(20))
      return i.rng.choice("ARMOR_EXPENSIVEDRESS", "ARMOR_BLACKDRESS", "ARMOR_EXPENSIVESUIT",
          "ARMOR_BLACKSUIT", "ARMOR_BONDAGEGEAR", "ARMOR_CIVILLIANARMOR", "ARMOR_BLACKROBE",
          "ARMOR_LABCOAT");
    else if (i.rng.chance(10))
      return "LOOT_EXPENSIVEJEWELERY";
    else if (i.rng.chance(5))
      return "LOOT_CELLPHONE";
    else if (i.rng.chance(4))
      return "LOOT_SILVERWARE";
    else if (i.rng.chance(3))
      return "LOOT_PDA";
    else if (i.rng.chance(2))
      return "LOOT_CHEAPJEWELERY";
    else
      return "LOOT_COMPUTER";
  }

  @Override public int rent() {
    return 500;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
