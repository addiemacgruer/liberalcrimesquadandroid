package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import lcs.android.creature.CreatureName;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault enum BusinessFronts {
  INSURANCE {
    @Override public void generateName(final Location l) {
      l.lcs().frontName = CreatureName.lastname();
      l.lcs().frontName += " ";
      switch (i.rng.nextInt(3)) {
      case 0:
      default:
        l.lcs().frontName += "Auto";
        break;
      case 1:
        l.lcs().frontName += "Life";
        break;
      case 2:
        l.lcs().frontName += "Health";
        break;
      }
      l.lcs().frontName += " Insurance";
    }
  },
  TEMPAGENCY {
    @Override public void generateName(final Location l) {
      l.lcs().frontName = CreatureName.lastname();
      l.lcs().frontName += " ";
      switch (i.rng.nextInt(2)) {
      case 0:
      default:
        l.lcs().frontName += "Temp Agency";
        break;
      case 1:
        l.lcs().frontName += "Manpower, LLC";
        break;
      }
    }
  };
  abstract public void generateName(Location location);
}