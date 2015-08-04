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

@Xml.Name(name = "GOVERNMENT_POLICESTATION") public @NonNullByDefault class PoliceStation extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Seedy Back Alley(tm).";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.POLICESTATION_LOCKUP;
  }

  @Override public void generateName(final Location l) {
    l.setName("Police Station");
  }

  @Override public boolean isPrison() {
    return true;
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", headquarters of one of the most oppressive and Conservative police forces in the country.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.POLICEBEHAVIOR, Issue.DRUGS };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(25)) {
      return i.rng.choice("WEAPON_NIGHTSTICK", "WEAPON_NIGHTSTICK", "WEAPON_SHOTGUN_PUMP",
          "WEAPON_SEMIPISTOL_9MM", "WEAPON_SMG_MP5", "WEAPON_CARBINE_M4", "WEAPON_AUTORIFLE_M16",
          "WEAPON_AUTORIFLE_M16");
    } else if (i.rng.chance(25)) {
      return i.rng.choice("ARMOR_POLICEUNIFORM", "ARMOR_POLICEUNIFORM", "ARMOR_POLICEARMOR",
          "ARMOR_POLICEUNIFORM", "ARMOR_SWATARMOR", "ARMOR_POLICEUNIFORM", "ARMOR_POLICEARMOR",
          "ARMOR_DEATHSQUADUNIFORM");
    } else if (i.rng.chance(20)) {
      return "LOOT_POLICERECORDS";
    } else if (i.rng.chance(3)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(2)) {
      return "LOOT_PDA";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  @Override public String siegeUnit() {
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      return "DEATHSQUAD";
    }
    return "SWAT";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
