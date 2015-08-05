package lcs.android.site.type;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.shop.Shop;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_PAWNSHOP") public @NonNullByDefault class PawnShop extends
    AbstractSiteType implements IShop {
  @Override public void generateName(final Location l) {
    if (i.issue(Issue.GUNCONTROL).law() == Alignment.ELITELIBERAL) {
      l.setName(CreatureName.lastname() + "'s Pawnshop");
    } else {
      l.setName(CreatureName.lastname() + " Pawn & Gun");
    }
  }

  @Override public void shop(final Location location) {
    pawnshop(location);
  }

  private static final long serialVersionUID = Game.VERSION;

  public static void pawnshop(final Location loc) {
    i.activeSquad().location(loc);
    final Shop pawnshop = new Shop("pawnshop.xml");
    pawnshop.enter(i.activeSquad());
  }
}
