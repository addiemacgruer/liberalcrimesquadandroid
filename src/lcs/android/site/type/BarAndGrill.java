package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_BARANDGRILL") public @NonNullByDefault class BarAndGrill extends
    AbstractSiteType {
  @Override public boolean canBuyGuns() {
    return true;
  }

  @Override public void generateName(final Location l) {
    l.setName("Desert Eagle Bar & Grill");
  }

  @Override public boolean isCcsSafeHouse() {
    return true;
  }

  @Override public String randomLootItem() {
    /* storming a CCS stronghold. Logically you ought to get all the leftover stuff if you win... */
    switch (i.rng.nextInt(3)) {
    case 0:
      return i.rng.choice("WEAPON_SEMIPISTOL_9MM", "WEAPON_SEMIPISTOL_45", "WEAPON_REVOLVER_38",
          "WEAPON_REVOLVER_44", "WEAPON_SMG_MP5", "WEAPON_CARBINE_M4", "WEAPON_AUTORIFLE_M16");
    case 1:
      return i.rng.choice("ARMOR_CHEAPSUIT", "ARMOR_CLOTHES", "ARMOR_TRENCHCOAT",
          "ARMOR_WORKCLOTHES", "ARMOR_SECURITYUNIFORM", "ARMOR_CIVILLIANARMOR", "ARMOR_ARMYARMOR",
          "ARMOR_HEAVYARMOR");
    default:
      return i.rng.choice("LOOT_CELLPHONE", "LOOT_SILVERWARE", "LOOT_TRINKET",
          "LOOT_CHEAPJEWELERY", "LOOT_COMPUTER");
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
