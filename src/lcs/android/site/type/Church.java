package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.CreatureName;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "CHURCH") public @NonNullByDefault class Church extends AbstractSiteType {
  @Override public void generateName(final Location l) {
    final String saint = CreatureName.firstName();
    l.setName("Church of St. " + saint);
  }

  private static final long serialVersionUID = Game.VERSION;
}
