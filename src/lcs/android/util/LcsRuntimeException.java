package lcs.android.util;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Generally, most exceptions are fatal to LCS, so we rethrow them as this exception */
public @NonNullByDefault class LcsRuntimeException extends RuntimeException {
  public LcsRuntimeException(final String string) {
    super(string);
  }

  public LcsRuntimeException(final String message, final Throwable t) {
    super(message, t);
  }

  public LcsRuntimeException(final Throwable t) {
    super(t);
  }

  private static final long serialVersionUID = Game.VERSION;
}
