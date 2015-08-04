package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "GOVERNMENT_ARMYBASE") public @NonNullByDefault class ArmyBase extends
    AbstractSiteType {
  @Override public String alarmResponseString() {
    return ": SOLDIERS AND TANKS RESPONDING";
  }

  @Override public String carChaseCar() {
    return "VEHICLE_HMMWV";
  }

  @Override public String carChaseCreature() {
    return "SOLDIER";
  }

  @Override public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 5 + 1) + 3;
  }

  @Override public int carChaseMax() {
    return 6;
  }

  @Override public String ccsSiteName() {
    return "Greenpeace Offices.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.ARMYBASE_ARMORY;
  }

  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + " Army Base");
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", pride of Conservative torturers and warmongers everywhere.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.TORTURE, Issue.MILITARY };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(3)) {
      return i.rng.choice("WEAPON_SEMIPISTOL_9MM", "WEAPON_CARBINE_M4", "WEAPON_AUTORIFLE_M16");
    } else if (i.rng.chance(2)) {
      return "ARMOR_ARMYARMOR";
    } else if (i.rng.chance(20)) {
      return "LOOT_SECRETDOCUMENTS";
    } else if (i.rng.chance(3)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(2)) {
      return "LOOT_WATCH";
    } else {
      return "LOOT_TRINKET";
    }
  }

  @Override public String siegeUnit() {
    if (i.rng.chance(12)) {
      return "TANK";
    }
    return "SOLDIER";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
