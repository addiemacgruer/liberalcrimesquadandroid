package lcs.android.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** Subclassed ObjectOutputStream that writes strings as UTF-16 C-Strings rather than java strings.
 * We save a lot, but restore very little, so we'd rather speed writing and take a hit while
 * reading.
 * <p>
 * Cuts serialization time by more than half on my S2 (sometimes by 80%), increases deserialization
 * by about 40%. */
public @NonNullByDefault class LcsStream {
  /** overrides readUTF to read 16-bit null-terminated character strings. */
  public static class In extends ObjectInputStream {
    public In(final InputStream input) throws StreamCorruptedException, IOException {
      super(input);
    }
    // @Override
    // public String readUTF() throws IOException {
    // final StringBuilder sb = new StringBuilder();
    // while (true) {
    // final char c = super.readChar();
    // if (c != 0) {
    // sb.append(c);
    // } else {
    // break;
    // }
    // }
    // return sb.toString();
    // }
  }

  /** Get some crude stats on how long it takes to serialize. Slows down serialization while we're
   * doing it, and causes extra GC work, so it's not strictly accurate, but better than guess work. */
  public static class InstrumentedOut extends Out {
    public InstrumentedOut(final ByteArrayOutputStream stream) throws IOException {
      super(stream);
      mOutput = stream;
      enableReplaceObject(true);
      last = System.currentTimeMillis();
    }

    public InstrumentedOut(final OutputBuffer out) throws IOException {
      super(out.stream());
      mOutput = out.stream();
      enableReplaceObject(true);
      last = System.currentTimeMillis();
    }

    private long last;

    private long lastSize = 0;

    private final ByteArrayOutputStream mOutput;

    @Nullable private Class<?> previousC;

    @Nullable private String previousName;

    @Override @Nullable protected Object replaceObject(final @Nullable Object object) {
      final long now = System.currentTimeMillis();
      final long size = mOutput.size() - lastSize;
      lastSize = mOutput.size();
      final long duration = now - last;
      last = now;
      if (previousC != null) {
        Log.d(Game.LCS, duration + " : " + size + " : " + previousC + " : " + previousName);
      }
      if (object != null) {
        previousC = object.getClass();
        previousName = object.toString();
      }
      return object;
    }
  }

  /** overrides writeUTF to write 16-bit null-terminated character strings. */
  public static class Out extends ObjectOutputStream {
    public Out(final OutputStream stream) throws IOException {
      super(stream);
    }
    // @Override
    // public void writeUTF(final String value) throws IOException {
    // super.writeChars(value);
    // super.writeChar(0);
    // }
  }
}