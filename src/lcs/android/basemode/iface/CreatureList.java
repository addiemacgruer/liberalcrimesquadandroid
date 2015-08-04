package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.util.Curses;
import lcs.android.util.DefaultValueKey;

/** These are the various lists of creatures which can have a {@link SortingChoice} */
public enum CreatureList implements DefaultValueKey<SortingChoice> {
  ACTIVATE,
  ACTIVATESLEEPERS,
  ASSEMBLESQUAD,
  AWAY,
  BASEASSIGN,
  CLINIC,
  DEAD,
  HOSTAGES,
  JUSTICE,
  LIBERALS,
  SLEEPERS;
  /** All lists are initially sorted by {@link SortingChoice#NAME}. */
  @Override public SortingChoice defaultValue() {
    return SortingChoice.NAME;
  }

  /** Prompt to decide how to sort liberals. */
  protected void sortingPrompt() {
    setView(R.layout.generic);
    Curses.ui().text("Choose how to sort list of:").add();
    switch (this) {
    case LIBERALS:
      Curses.ui().text("active Liberals.").add();
      break;
    case HOSTAGES:
      Curses.ui().text("hostages.").add();
      break;
    case CLINIC:
      Curses.ui().text("Liberals in hospital.").add();
      break;
    case JUSTICE:
      Curses.ui().text("oppressed Liberals.").add();
      break;
    case SLEEPERS:
      Curses.ui().text("sleepers.").add();
      break;
    case DEAD:
      Curses.ui().text("dead people.").add();
      break;
    case AWAY:
      Curses.ui().text("people away.").add();
      break;
    case ACTIVATE:
      Curses.ui().text("Liberal activity.").add();
      break;
    case ACTIVATESLEEPERS:
      Curses.ui().text("sleeper activity.").add();
      break;
    case ASSEMBLESQUAD:
      Curses.ui().text("available Liberals.").add();
      break;
    case BASEASSIGN:
      Curses.ui().text("squadless members.").add();
      break;
    default:
      Curses.ui().text("ERROR: INVALID VALUE FOR SORTINGCHOICE!").add();
      break;
    }
    Curses.ui(R.id.gcontrol).button('a').text("No sorting.").add();
    Curses.ui(R.id.gcontrol).button('b').text("Sort by name.").add();
    Curses.ui(R.id.gcontrol).button('c').text("Sort by location and name.").add();
    Curses.ui(R.id.gcontrol).button('d').text("Sort by squad and name.").add();
    while (true) {
      final int c = Curses.getch();
      switch (c) {
      case 'a':
        i.activeSortingChoice.put(this, SortingChoice.NONE);
        return;
      case 'b':
      default:
        i.activeSortingChoice.put(this, SortingChoice.NAME);
        return;
      case 'c':
        i.activeSortingChoice.put(this, SortingChoice.LOCATION_AND_NAME);
        return;
      case 'd':
        i.activeSortingChoice.put(this, SortingChoice.SQUAD_OR_NAME);
        return;
      }
    }
  }
}