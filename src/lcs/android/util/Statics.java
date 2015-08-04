package lcs.android.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lcs.android.R;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** The GUI thread {@link LiberalCrimeSquadActivity} is recreated by Android whenever one of many
 * things change: significantly, screen rotation: this @NonNullByDefault class holds onto a number
 * of things that need to persist between GUI thread changes.
 * <p>
 * Loading of this @NonNullByDefault class also starts the main game thread,
 * <q>Your Liberal Agenda</q>. It's never accessed again, but this @NonNullByDefault class holds
 * onto it so it doesn't end up in the garbage.
 * <p>
 * in theory, the fields are only accessed by the GUI thread (methods not though!). Uses concurrent
 * versions of maps and lists: prefer potential screen corruption to crashes in the event that the
 * the screen is rotated when the GUI looper is still processing new objects. C.synchronizedList has
 * kind of poor behaviour in the event of many concurrent writes, but these stores are written every
 * time there's a gui update (ie. only from the running gui thread) and only read when there's a gui
 * thread change, so that's okay. */
public @NonNullByDefault class Statics {
  private Statics() {}

  /** Contains a list of extra UI Elements which have been added to a View container */
  protected static final List<UIElement.AbstractElement> EXTRAS = Collections
      .synchronizedList(new ArrayList<UIElement.AbstractElement>());

  /** Holds our synchronized GetCh @NonNullByDefault class, to obtain user input */
  protected static final GetCh GETCH = GetCh.instance();

  /** Contains the list of ViewStubs from a layout which have been inflated. */
  protected static final List<Integer> INFLATE = Collections
      .synchronizedList(new ArrayList<Integer>());

  /** Contains items from a layout which have had their color changed, stored as
   * R.id.something:android color value. */
  protected static final Map<Integer, Integer> VIEWCOLOR = new ConcurrentHashMap<Integer, Integer>();

  /** Contains items from a layout which have had their enabled status changed from default, stored
   * as R.id.something:boolean */
  protected static final Map<Integer, Boolean> VIEWENABLED = new ConcurrentHashMap<Integer, Boolean>();

  /** Contains items from a layout which have had their text changed, stored as R.id.something:text */
  protected static final Map<Integer, String> VIEWTEXT = new ConcurrentHashMap<Integer, String>();

  // synchronization is okay, because they're final
  private static final Runnable GAME;

  private static final Thread GAMETHREAD;

  @Nullable private static String htmlPage;

  @Nullable private static LiberalCrimeSquadActivity instance;

  // okay to synchronize volatiles on an int
  private static int lastView = R.layout.main;

  // changes to instance and htmlpage are synchronized on this key.
  private final static Object SYNCKEY = new Object();
  static {
    GAME = new Runnable() {
      @Override public void run() {
        Game.main();
      }
    };
    GAMETHREAD = new Thread(Statics.GAME, "Your liberal agenda.");
    GAMETHREAD.start();
  }

  /** Gets the currently running UI instance: this is needed by the main game thread to access the
   * various XML resources and to send messages. Blocks in the case of a null value until
   * {@link #setInstance} does its work, to prevent NullPointerExceptions. Interestingly, these
   * never used to happen when running on Froyo, but IceCreamSandwich must alter the way resources
   * are loaded.
   * @return The currently running UI instance. Or possibly the last one; it takes about 100 ms to
   *         redraw the screen in the event of a rotation. but that instance can still load
   *         resources. */
  public static LiberalCrimeSquadActivity instance() {
    synchronized (SYNCKEY) {
      do {
        if (instance != null) {
          break;
        }
        Log.w(Game.LCS, "Waiting on LCSA instance...");
        try {
          SYNCKEY.wait(1000);
        } catch (final InterruptedException ie) {
          Log.e("LCS", "Interrupted waiting on LCSA instance", ie);
        }
      } while (true);
      if (instance != null)
        return instance;
      throw new NullPointerException("Oops, no LCSA instance");
    }
  }

  /** Gets the HTML page requested to view: this is used by {@link HtmlViewActivity}.
   * @return a string, <q>something.html</q> */
  protected static String htmlPage() {
    synchronized (SYNCKEY) {
      if (htmlPage != null)
        return htmlPage;
      throw new NullPointerException("Oops, no HTML Page");
    }
  }

  /** Gets the last XML layout drawn to the screen: inquiring minds want to know. The main game
   * thread sending a request for a new layout, expanding it and drawing it to the screen takes 100
   * ms or so on an S2, so we can be a good bit more responsive if we don't do wasteful updates.
   * @return the last view. */
  protected synchronized static int lastView() {
    return lastView;
  }

  /** Sets the {@link HtmlViewActivity}'s html target: this is used by
   * {@link LiberalCrimeSquadActivity} to communicate */
  protected static void setHtmlPage(final String htmlPage) {
    synchronized (SYNCKEY) {
      Statics.htmlPage = htmlPage;
    }
  }

  /** Sets a reference to the currently running GUI thread. Wakes {@link #instance} if needs be */
  protected static void setInstance(final LiberalCrimeSquadActivity instance) {
    synchronized (SYNCKEY) {
      Statics.instance = instance;
      SYNCKEY.notifyAll();
    }
  }

  /** Updates what the last XML layout drawn was, for {@link #lastView} */
  protected synchronized static void setLastView(final int lastView) {
    Statics.lastView = lastView;
  }
}
