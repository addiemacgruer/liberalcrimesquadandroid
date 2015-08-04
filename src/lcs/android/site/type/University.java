package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "HOSPITAL_UNIVERSITY") public @NonNullByDefault class University extends Clinic {
  @Override public void generateName(final Location l) {
    l.setName("The University Hospital");
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
