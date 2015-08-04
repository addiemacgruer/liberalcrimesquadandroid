package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.site.type.AbstractSiteType.UniqueNamedSite;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "RESIDENTIAL_TENEMENT") public @NonNullByDefault class Tenement extends
    UniqueNamedSite {
  @Override public boolean canBuyGuns() {
    return true;
  }

  @Override public boolean canMap() {
    return false;
  }

  @Override public int graffitiQuota() {
    return 10;
  }

  @Override public boolean isResidential() {
    return true;
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority / 8;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(25))
      return i.rng.choice("WEAPON_BASEBALLBAT", "WEAPON_CROWBAR", "WEAPON_KNIFE", "WEAPON_SHANK",
          "WEAPON_SYRINGE", "WEAPON_CHAIN", "WEAPON_GUITAR", "WEAPON_SPRAYCAN");
    else if (i.rng.chance(20))
      return i.rng.choice("ARMOR_CHEAPDRESS", "ARMOR_CHEAPSUIT", "ARMOR_CLOTHES",
          "ARMOR_TRENCHCOAT", "ARMOR_WORKCLOTHES", "ARMOR_TOGA", "ARMOR_PRISONER");
    else if (i.rng.chance(3))
      return "LOOT_KIDART";
    else if (i.rng.chance(2))
      return "LOOT_DIRTYSOCK";
    else
      return "LOOT_FAMILYPHOTO";
  }

  @Override public int rent() {
    return 100;
  }

  @Override void uniqueName(final Location l) {
    l.setName(CreatureName.lastname() + " St. ");
    l.setName(l.toString() + "Housing Projects");
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
