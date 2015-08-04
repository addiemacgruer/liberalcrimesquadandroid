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

@Xml.Name(name = "GOVERNMENT_COURTHOUSE") public @NonNullByDefault class CourtHouse extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Abortion Clinic.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.COURTHOUSE_LOCKUP;
  }

  @Override public void generateName(final Location l) {
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE) {
      l.setName("Halls of Ultimate Judgment");
    } else {
      l.setName("Court House");
    }
  }

  @Override public boolean isPrison() {
    return true;
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", site of numerous Conservative Injustices.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.DEATHPENALTY, Issue.JUSTICES, Issue.FREESPEECH, Issue.GAY,
        Issue.ABORTION, Issue.CIVILRIGHTS };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(20)) {
      return "LOOT_JUDGEFILES";
    } else if (i.rng.chance(3)) {
      return "LOOT_CELLPHONE";
    } else if (i.rng.chance(2)) {
      return "LOOT_PDA";
    } else {
      return "LOOT_COMPUTER";
    }
  }

  @Override @Nullable public SpecialBlocks secondSpecial() {
    return SpecialBlocks.COURTHOUSE_JURYROOM;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
