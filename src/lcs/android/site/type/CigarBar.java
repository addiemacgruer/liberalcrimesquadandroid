package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.game.LcsRandom;
import lcs.android.politics.Issue;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_CIGARBAR") public @NonNullByDefault class CigarBar extends
    AbstractSiteType {
  @Override public void allocateMap(final LcsRandom SITERNG) {
    AbstractSiteType.type(JuiceBar.class).allocateMap(SITERNG);
  }

  @Override public String ccsSiteName() {
    return "Lady Luck Strip Club.";
  }

  @Override public void generateName(final Location l) {
    l.setName("The " + CreatureName.lastname() + " Gentlemen's Club");
  }

  @Override public boolean hasLoot() {
    return false;
  }

  @Override public boolean hasTables() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", a spawning ground of Wrong Conservative Ideas.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.TAX, Issue.CEOSALARY, Issue.ABORTION };
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
