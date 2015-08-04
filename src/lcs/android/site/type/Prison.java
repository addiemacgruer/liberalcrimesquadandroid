package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "GOVERNMENT_PRISON") public @NonNullByDefault class Prison extends
    AbstractSiteType {
  @Override public String ccsSiteName() {
    return "Ace Ghetto Pool Hall.";
  }

  @Override @Nullable public SpecialBlocks firstSpecial() {
    return SpecialBlocks.PRISON_CONTROL;
  }

  @Override public void generateName(final Location l) {
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      l.setName(i.rng.choice("Happy", "Cheery", "Quiet", "Green", "Nectar") + " "
          + i.rng.choice("Valley", "Meadow", "Hills", "Glade", "Forest") + " Re-education Camp");
    } else {
      l.setName(CreatureName.lastname() + " Prison");
    }
  }

  @Override public boolean isPrison() {
    return true;
  }

  @Override public boolean isRestricted() {
    return true;
  }

  @Override public String lcsSiteOpinion() {
    return ", where innocent people are regularly beaten by Conservative guards.";
  }

  @Override public Issue[] opinionsChanged() {
    return new Issue[] { Issue.DEATHPENALTY, Issue.DRUGS, Issue.TORTURE };
  }

  @Override public int priority(final int oldPriority) {
    return oldPriority * 2;
  }

  @Override public String randomLootItem() {
    if (i.rng.chance(5)) {
      return "ARMOR_PRISONER";
    }
    return "WEAPON_SHANK";
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
