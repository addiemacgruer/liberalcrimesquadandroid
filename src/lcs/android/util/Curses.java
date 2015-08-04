package lcs.android.util;

import static lcs.android.game.Game.*;

import java.util.List;

import lcs.android.R;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.content.res.Resources.NotFoundException;
import android.os.Message;
import android.util.Log;

/** implements functionality similar to the Curses library, used by original C++ version, to maintain
 * code similarity.
 * <p>
 * The android layouts are stored as xml in the layouts/ subfolder, and can be drawn to the screen
 * with {@link #setView} . The screen is then most commonly updated by either:
 * <ul>
 * <li>using a {@link #setText} method to change a specific text or button.
 * <li>using a {@link #ui} method to add a text or button into a ListView in the layout
 * </ul> */
public @NonNullByDefault class Curses {
  private Curses() {}

  /** Ascii code for enter key. */
  public final static int ENTER = 10;

  /** Ascii code for escape key. */
  public final static int ESCAPE = 27;

  private static int lastView = 0;

  /** Presents a list of options to choose from.
   * @param prompt The text: 'which of these?'
   * @param option A list of strings: "Choice A","Choice B"
   * @param allowexitwochoice Can exit w/o choosing
   * @param exitstring The text prompt is allowexitwochoice
   * @return the offset of the chose choice in the list, or -1 if not chosen */
  public static int choiceprompt(final CharSequence prompt,
      final List<? extends CharSequence> option, final boolean allowexitwochoice,
      final CharSequence exitstring) {
    setView(R.layout.generic);
    int y = 'a';
    int c;
    ui().text(prompt).add();
    for (final CharSequence s : option) {
      ui(R.id.gcontrol).button(y++).text(s).add();
    }
    if (allowexitwochoice) {
      ui(R.id.gcontrol).button(10).text(exitstring).add();
    }
    do {
      c = getch();
    } while (!(c >= 'a' || c == 10 && allowexitwochoice));
    return c == 10 ? -1 : c - 'a';
  }

  /** Empties a container on screen
   * @param parent The container to clear. */
  public static void clearChildren(final int parent) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.CLEAR_CHILDREN, parent, 0, null)
        .sendToTarget();
  }

  /** Pause the game to tell you something unmissable. Retains generic view if already in place, and
   * clears the OK buttons afterwards.
   * @param f the fact you need to know */
  public static void fact(final CharSequence f) {
    setViewIfNeeded(R.layout.generic);
    ui().text(f).add();
    ui(R.id.gcontrol).button(' ').text("OK").add();
    getch();
    clearChildren(R.id.gcontrol);
  }

  /** Format a string with a number of objects
   * @param form the string, with format codes
   * @param objects
   * @return the formatted string */
  public static String format(final CharSequence form, final Object... objects) {
    return String.format(form.toString(), objects);
  }

  /** Formats a string from an xml resource file with a number of objects
   * @param resId resource ID, R.string.something, which contains format codes
   * @param objects
   * @return the formatted string */
  public static String format(final int resId, final Object... objects) {
    final String t = Statics.instance().getString(resId);
    return String.format(t, objects);
  }

  /** appends a StringBuilder with a string from an xml resource file
   * @param str the StringBuilder to append to.
   * @param resId resource ID, R.string.something, which contains format codes
   * @param objects */
  public static void format(final StringBuilder str, final int resId, final Object... objects) {
    final String t = Statics.instance().getString(resId);
    str.append(String.format(t, objects));
  }

  /** gets a 'character input', which in this case is a code attached to a button. Note that Android
   * requires the UI and the logic threads to run separately (or at least, separately to maintain
   * the C++'ness of the original), and so this is more complicated than it first appears. Blocks
   * until there's a response
   * @return an int, typically representing an ascii character. */
  public static int getch() {
    if (lastView == R.layout.generic && Statics.EXTRAS.size() == 0
        && !Statics.instance().uiHandler.hasMessages(IProtocol.ADD_ELEMENT)) {
      // then potentially we've a generic view with no items in, and we're
      // trying to get a key input. Oops.
      if (Statics.EXTRAS.size() == 0) { // just in case the handler completed
        // between the above tests.
        Log.e("LCS", "Tried to wait on a key input, but the page is blank.  Pressing return.");
        return 10; // press enter instead.
      }
    }
    return Statics.GETCH.getKeyPressed();
  }

  /** gets a string from an xml resource
   * @param resId the resource ID, typically R.string.something
   * @return the string in question. */
  public static String getString(final int resId) {
    try {
      return Statics.instance().getString(resId);
    } catch (final NotFoundException e) {
      Log.e(Game.LCS, "String resource not found: " + Integer.toHexString(resId), e);
      return "String resource not found: " + Integer.toHexString(resId);
    }
  }

  /** returns the last R.layout.something set with setView or setViewIfNeeded
   * @return the integer value of R.layout.something. */
  public static int lastView() {
    return lastView;
  }

  /** Adds a button to the specified view container, enabled if it passes a test
   * @param parent the view container
   * @param ch the character which will be {@link #getch}'d if pressed.
   * @param text the text of the button
   * @param test the test to be passed to enable the button */
  public static void maybeAddButton(final int parent, final int ch, final CharSequence text,
      final boolean test) {
    if (test) {
      ui(parent).button(ch).text(text).add();
    } else {
      ui(parent).button().text(text).add();
    }
  }

  /** Adds text to the specified container if it passes a test */
  public static void maybeAddText(final int parent, final CharSequence text, final boolean test) {
    if (test) {
      ui(parent).text(text).add();
    }
  }

  /** Get a number value from the user
   * @param question 'Think of a number'
   * @param answer 'the first number you've thought of.
   * @return the actual number they've thought of. */
  public static int numberquery(final CharSequence question, final int answer) {
    Curses.setView(R.layout.numberquestion);
    Curses.setText(R.id.question, question);
    Curses.setText(R.id.answer, String.valueOf(answer));
    int ch = '?';
    do {
      ch = Curses.getch();
    } while (ch != 'x');
    return Integer.parseInt(Curses.getText(R.id.answer));
  }

  /** pause for a number of milliseconds, or maybe more or less.
   * @param time some milliseconds to wait. */
  public static void pauseMs(final int time) {
    try {
      Thread.sleep(time);
    } catch (final InterruptedException e) {
      Log.e("LCS", "Interrupted sleep", e);
    }
  }

  /** get a text response, using a given string
   * @param question 'what is your favourite colour?'
   * @param answer default value
   * @return the user's input */
  public static String query(final CharSequence question, final CharSequence answer) {
    Curses.setView(R.layout.textquestion);
    Curses.setText(R.id.question, question);
    Curses.setText(R.id.answer, answer);
    int ch = '?';
    do {
      ch = Curses.getch();
    } while (ch != 'x');
    return Curses.getText(R.id.answer);
  }

  /** get a text response, using a string resource as the prompt
   * @param resId
   * @param answer default value
   * @return the user's input */
  public static String query(final int resId, final CharSequence answer) {
    return query(getString(resId), answer);
  }

  /** obtains a random string from an xml resource file, typically in res/values/*.xml
   * @param id typically R.array.something
   * @return one of the resources in that bundle. */
  public static String randomString(final int id) {
    return i.rng.choice(stringArray(id));
  }

  /** sets one of the TextView / Buttons on screen to a specified color
   * @param view R.id.something
   * @param color */
  public static void setColor(final int view, final Color color) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.UPDATE_COLOR, view, 0, color)
        .sendToTarget();
  }

  /** sets whether a Button on screen is enabled.
   * @param view R.id.something
   * @param enabled */
  public static void setEnabled(final int view, final boolean enabled) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.UPDATE_ENABLED, view, 0, enabled)
        .sendToTarget();
  }

  /** inflate a ViewStub defined in the xml layout file */
  public static void setInflate(final int view) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.UPDATE_INFLATE, view, 0, null)
        .sendToTarget();
  }

  /** change the text of a TextView / Button on screen to a given string
   * @param view R.id.something
   * @param string */
  public static void setText(final int view, final CharSequence string) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.UPDATE_TEXT, view, 0, string)
        .sendToTarget();
  }

  /** change the text of a TextView / Button on screen to a string from an xml resource
   * @param view R.id.something
   * @param resId */
  public static void setText(final int view, final int resId) {
    final String t = Statics.instance().getString(resId);
    setText(view, t);
  }

  /** change the text of a TextView / Button on screen to a format string from an xml resource, with
   * items.
   * @param view R.id.something
   * @param resId R.string.something, contains format codes
   * @param objects the objects to substitute. */
  public static void setText(final int view, final int resId, final Object... objects) {
    setText(view, format(resId, objects));
  }

  /** changes the on-screen display to a different res/layout/, erasing all content.
   * @param view R.layout.something */
  public static void setView(final int view) {
    Statics.instance().uiHandler.obtainMessage(IProtocol.UPDATE_VIEW, view).sendToTarget();
    lastView = view;
  }

  /** does a {@code setView}, but only if that would be a change from what's on screen already. We
   * make a lot of use of R.layout.generic, and this helps make it a wall of text, not a hundred
   * string updates like the DOS version, which is annoying to tap through on Android.
   * @param view R.layout.something */
  public static void setViewIfNeeded(final int view) {
    if (lastView != view) {
      setView(view);
    }
  }

  /** shows the html help page, which is a separate activity and doesn't stop what we're doing. */
  public static void showHelp() {
    Statics.instance().uiHandler.sendEmptyMessage(IProtocol.SHOW_HELP_PAGE);
  }

  /** shows the html license page, which is a separate activity and doesn't stop what we're doing. */
  public static void showLicense() {
    Statics.instance().uiHandler.sendEmptyMessage(IProtocol.SHOW_LICENSE);
  }

  /** gets a string array from the res/values/*.xml file
   * @param id R.array.something
   * @return the array. */
  public static String[] stringArray(final int id) {
    return Statics.instance().getResources().getStringArray(id);
  }

  public static void toast(final CharSequence toast) {
    final Message msg = Message.obtain();
    msg.what = IProtocol.DISPLAY_TOAST;
    msg.obj = toast;
    Statics.instance().uiHandler.sendMessage(msg);
  }

  /** get a new UIBuilder, so we can put some generic items on screen. this version adds to
   * R.id.gmessages, which most layouts contain.
   * @return a blank UIBuilder. */
  public static UIElement.UIBuilder ui() {
    return ui(R.id.gmessages);
  }

  /** get a new UIBuilder, so we can put some generic items on screen.
   * @param parent the container to add to.
   * @return a blank UIBuilder. */
  public static UIElement.UIBuilder ui(final int parent) {
    return new UIElement.UIBuilder(parent);
  }

  /** adds an <q>OK</q> button to R.id.gcontrol, which must be in the current layout, and waits for
   * it (or the screen) to be pressed. Clears R.id.gcontrol once done */
  public static void waitOnOK() {
    ui(R.id.gcontrol).button(10).text("OK").add();
    getch();
    clearChildren(R.id.gcontrol);
  }

  /** Ask a yes or no question; overwrites the screen content.
   * @param string A yes/no question.
   * @return the characters 'y' or 'n' */
  public static int yesOrNo(final CharSequence string) {
    setView(R.layout.generic);
    ui().text(string).add();
    ui(R.id.gcontrol).button('y').text("Yes").add();
    ui(R.id.gcontrol).button('n').text("No").add();
    final int r = getch();
    clearChildren(R.id.gcontrol);
    return r;
  }

  private static String getText(final int viewId) {
    return Statics.instance().getEditText(viewId);
  }
}
