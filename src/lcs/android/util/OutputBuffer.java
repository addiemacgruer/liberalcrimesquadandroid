package lcs.android.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.content.Context;
import android.util.Log;

/** saves a stream of data to the storage device in a background thread, just in case our device is
 * super-slow. My Galaxy S2 manages about 10kb per millisecond, so this isn't generally the big
 * hold-up in serialization, but really slow devices might benefit.
 * <p>
 * Use:
 * <p>
 * OutputBuffer ob = new OutputBuffer();
 * <p>
 * ObjectOutputStream oos = new ObjectOutputStream(ob.stream());
 * <p>
 * oos.writeObject(...);
 * <p>
 * ob.run(); */
public @NonNullByDefault class OutputBuffer implements Runnable {
  public OutputBuffer(final long saveNumber) {
    this.saveNumber = saveNumber;
    savesInProgress++;
  }

  private final ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 4096);

  private final long saveNumber;

  @Override public void run() {
    Log.i("LCS", "Began serialisation task: " + saveNumber);
    if (baos.size() == 0) {
      Log.e(Game.LCS, "Attempted to write null output");
      savesInProgress--;
      return;
    }
    final long time = System.currentTimeMillis();
    FileOutputStream out = null;
    try {
      out = Statics.instance().openFileOutput(Game.saveFileName(saveNumber), Context.MODE_PRIVATE);
      out.write(baos.toByteArray());
    } catch (final IOException e) {
      Log.e("LCS", "Outputbuffer:", e);
    } finally {
      if (out != null) {
        try {
          out.close();
          baos.close();
        } catch (final IOException e) {
          Log.e("LCS", "Failed to close OutputBuffer", e);
        }
      }
      Log.i("LCS",
          "Written to disk in: " + (System.currentTimeMillis() - time) + " ms (" + baos.size()
              + " bytes)");
      savesInProgress--;
    }
  }

  public ByteArrayOutputStream stream() {
    return baos;
  }

  public static volatile int savesInProgress = 0;
}
