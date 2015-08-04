package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.site.type.AbstractSiteType.UniqueNamedSite;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "RESIDENTIAL_APARTMENT") public @NonNullByDefault class Apartment extends
    UniqueNamedSite {
  @Override public boolean canMap() {
    return false;
  }

  @Override public boolean isResidential() {
    return true;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(25))
      return i.rng.choice("WEAPON_BASEBALLBAT", "WEAPON_KNIFE", "WEAPON_REVOLVER_38",
          "WEAPON_REVOLVER_44", "WEAPON_NIGHTSTICK", "WEAPON_GUITAR");
    else if (i.rng.chance(20))
      return i.rng.choice("ARMOR_CHEAPDRESS", "ARMOR_CHEAPSUIT", "ARMOR_CLOTHES",
          "ARMOR_TRENCHCOAT", "ARMOR_WORKCLOTHES", "ARMOR_CLOWNSUIT", "ARMOR_ELEPHANTSUIT",
          "ARMOR_DONKEYSUIT");
    else if (i.rng.chance(5))
      return "LOOT_CELLPHONE";
    else if (i.rng.chance(4))
      return "LOOT_SILVERWARE";
    else if (i.rng.chance(3))
      return "LOOT_TRINKET";
    else if (i.rng.chance(2))
      return "LOOT_CHEAPJEWELERY";
    else
      return "LOOT_COMPUTER";
  }

  @Override public int rent() {
    return 200;
  }

  @Override void uniqueName(final Location l) {
    final String str = CreatureName.lastname();
    l.setName(str + " Apartments");
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
