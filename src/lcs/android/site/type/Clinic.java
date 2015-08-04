package lcs.android.site.type;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "HOSPITAL_CLINIC") public @NonNullByDefault class Clinic extends AbstractSiteType
    implements IHospital {
  @Override public void generateName(final Location l) {
    l.setName("The Free CLINIC");
  }

  @Override public void hospital(final Location loc) {
    // if (i.activeSquad == null)
    // return;
    i.activeSquad.location(loc);
    do {
      setView(R.layout.hospital);
      i.activeSquad.location().get().printLocationHeader();
      i.activeSquad.printParty();
      if (i.activeSquad.highlightedMember() != -1) {
        ui(R.id.gcontrol).button('f').text("Go in and fix up Conservative wounds").add();
      }
      ui(R.id.gcontrol).button(10).text("Leave").add();
      final int c = getch();
      i.activeSquad.displaySquadInfo(c);
      if (c == 'f') {
        i.activeSquad.member(i.activeSquad.size() - 1).health().hospitalize(loc);
      }
      if (i.activeSquad.isEmpty() || c == ENTER) {
        break;
      }
    } while (true);
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
