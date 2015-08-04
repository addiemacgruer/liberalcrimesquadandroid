/**
 *
 */
package lcs.android.site.type;

import lcs.android.basemode.iface.Location;
import lcs.android.util.Xml;

/** @author addie */
@Xml.Name(name = "NOWHERE") public class Nowhere extends AbstractSiteType {
  @Override public void generateName(final Location l) {
    l.setName("NOWHERE");
  }

  @Override public Location getLocation() {
    return Location.none();
  }

  // private static final Location nowhere = new Location(this);
  /**
   *
   */
  private static final long serialVersionUID = 1L;
}
