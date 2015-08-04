package lcs.android.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** <q>keypresses</q> have to be shared between the UI and the Game threads on Android, so this @NonNullByDefault
 * class contains synchronized methods to enable that. There's only a singleton instance of this @NonNullByDefault
 * class. */
@NonNullByDefault class GetCh {
  private GetCh() {}

  private int keyPressed;

  /** waits for a key to be pressed, sleeps until one is.
   * @return An integer, usually corresponding to an ASCII value */
  public int getKeyPressed() {
    synchronized (this) {
      keyPressed = 0;
      while (keyPressed == 0) {
        try {
          wait();
        } catch (final InterruptedException ie) {
          Log.e("LCS", "Interrupted wait for getKeyPressed", ie);
        }
      }
      return keyPressed;
    }
  }

  /** presses a key, and wakes the background thread to let it know its been pressed.
   * @param keyPressed An integer, usually corresponding to an ASCII value */
  public void setKeyPressed(final int keyPressed) {
    synchronized (this) {
      if (keyPressed == 0)
        return;
      this.keyPressed = keyPressed;
      notifyAll();
    }
  }

  /** singleton. */
  private static final GetCh instance = new GetCh();

  /** get the instance */
  protected static GetCh instance() {
    return instance;
  }
}
