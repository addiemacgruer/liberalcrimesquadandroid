package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "OUTDOOR_PUBLICPARK") public @NonNullByDefault class PublicPark extends
    AbstractSiteType {
  @Override public void generateName(final Location l) {
    l.setName(CreatureName.lastname() + " Park");
  }

  @Override public int graffitiQuota() {
    return 5;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
