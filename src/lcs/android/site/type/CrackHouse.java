package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.type.AbstractSiteType.UniqueNamedSite;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_CRACKHOUSE") public @NonNullByDefault class CrackHouse extends
    UniqueNamedSite implements IInvestable {
  @Override public String alarmResponseString() {
    return ": GANG MEMBERS RESPONDING";
  }

  @Override public void allocateMap(final LcsRandom SITERNG) {
    final int dx = SITERNG.nextInt(5) * 2 + 19;
    final int dy = SITERNG.nextInt(3) * 2 + 7;
    final int rx = SiteMap.MAPX / 2 - dx / 2;
    final int ry = 3;
    generateroom(SITERNG, rx, ry, dx, dy, 0);
  }

  @Override public boolean canBuyGuns() {
    return true;
  }

  @Override public String carChaseCar() {
    return "VEHICLE_STATIONWAGON";
  }

  @Override public String carChaseCreature() {
    return "GANGMEMBER";
  }

  @Override public int carChaseIntensity(final int siteCrime) {
    return i.rng.nextInt(siteCrime / 3 + 1) + 1;
  }

  @Override public int carChaseMax() {
    return 18;
  }

  @Override public int graffitiQuota() {
    return 30;
  }

  @Override public boolean hasLoot() {
    return false;
  }

  @Override public int priority(final int oldPriority) {
    return 0; // not even reported.
  }

  @Override public int rent() {
    return 0;
  }

  @Override public String siegeUnit() {
    return "GANGMEMBER";
  }

  @Override void uniqueName(final Location l) {
    l.setName(CreatureName.lastname() + " St. ");
    if (i.issue(Issue.DRUGS).law() == Alignment.ELITELIBERAL) {
      switch (i.rng.nextInt(4)) {
      default:
      case 0:
        l.setName(l.toString() + "Recreational Drugs Center");
        break;
      case 1:
        l.setName(l.toString() + "Coffee House");
        break;
      case 2:
        l.setName(l.toString() + "Cannabis Lounge");
        break;
      case 3:
        l.setName(l.toString() + "Marijuana Dispensary");
        break;
      }
    } else {
      l.setName(l.toString() + "Crack House");
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
