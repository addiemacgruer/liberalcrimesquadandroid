package lcs.android.util;

/** Android's inter-thread communications system's Message.what use integer values to represent
 * conditions: this is an enumeration of them. */
public interface IProtocol {
  int ADD_ELEMENT = 2100; // parent, 0, Elements.Extra

  int CLEAR_CHILDREN = 2005; // parent

  int CONTROL_ABOUT = 3001;

  int CONTROL_DELETE_SAVE = 3003;

  // int CONTROL_RESTART = 3004;
  int CONTROL_FORCE_CLICK = 3005;

  int CONTROL_HELP = 3002;

  // int UPDATE_SCROLL = 5;
  int DISPLAY_TOAST = 2001; // OBJ = text

  int ON_CLICK = 1101; // OBJ = view

  int ON_CREATE = 1001; // OBJ = bundle

  int ON_DESTROY = 1006;

  int ON_PAUSE = 1004;

  int ON_RESUME = 1003;

  int ON_START = 1002;

  int ON_STOP = 1005;

  int ON_TOUCH_EVENT = 1103;

  int SET_THEME = 5001;

  int SHOW_HELP_PAGE = 4001;

  int SHOW_LICENSE = 4002;

  int UPDATE_COLOR = 2;

  int UPDATE_ENABLED = 3;

  int UPDATE_INFLATE = 4;

  int UPDATE_TEXT = 1;

  int UPDATE_VIEW = 0;
}
