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

@Xml.Name(name = "GOVERNMENT_INTELLIGENCEHQ") public @NonNullByDefault class IntelligenceHq extends
    AbstractSiteType {
  @Override public String alarmResponseString() {
    return ": AGENTS RESPONDING";
  }

  @Override public String carChaseCar() {
    return "VEHICLE_AGENTCAR";
  }

  @Override public String carChaseCreature() {
    return "AGENT";
  }

  @Override public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 5 + 1) + 3;
  }

  @Override public int carChaseMax() {
    return 6;
  }

  @Override public String ccsSiteName() {
    return "ACLU Branch Office.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.INTEL_SUPERCOMPUTER;
  }

  @Override public void generateName(final Location l) {
    if (i.issue(Issue.PRIVACY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      l.setName("Ministry of Love");
    } else {
      l.setName("Intelligence HQ");
    }
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", the Conservative headquarters of one of the biggest privacy violators in the world.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.PRIVACY, Issue.TORTURE };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(24)) {
      return i.rng.choice("WEAPON_FLAMETHROWER", "WEAPON_SEMIPISTOL_45", "WEAPON_SMG_MP5",
          "WEAPON_CARBINE_M4", "WEAPON_AUTORIFLE_M16");
    } else if (i.rng.chance(30)) {
      return "ARMOR_HEAVYARMOR";
    } else if (i.rng.chance(20)) {
      return "LOOT_SECRETDOCUMENTS";
    } else if (i.rng.chance(3)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(2)) {
      return "LOOT_PDA";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  @Override public String siegeUnit() {
    return "AGENT";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
