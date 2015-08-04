package lcs.android.util;

import java.lang.ref.SoftReference;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** Simulates the effect of Optional from J8. Keeps a softreference to the last Maybe created, to
 * avoid massive amounts of object creation. Maybe's aren't meant to be stored or kept for a long
 * time.
 * @author addie
 * @param <T> the type which may or may not be present. */
public @NonNullByDefault class Maybe<T> {
  /** Creator: will return a softreference to the value
   * @param value the thing to create a Maybe from. */
  @SuppressWarnings("rawtypes") private Maybe(@Nullable final T value) {
    this.value = value;
    LAST_MAYBE = new SoftReference<Maybe>(this);
  }

  /** The value of this maybe. */
  @Nullable private final T value;

  /** Whether this maybe wraps a value.
   * @return true if the contents exist. */
  public boolean exists() {
    return value != null;
  }

  /** Get the value contained in this maybe.
   * @return the value.
   * @throws NoSuchElementException if there is no contents. */
  public T get() {
    final T rval = value;
    if (rval == null)
      throw new NoSuchElementException("tried to get an emtpy Maybe");
    return rval;
  }

  /** Returns the value contained in this maybe, which may be null.
   * @return the value. */
  @Nullable public T getNullable() {
    return value;
  }

  /** Whether this Maybe is empty
   * @return true if no value is present. */
  public boolean missing() {
    return value == null;
  }

  /** Returns the content of this maybe, or a default value if it is empty.
   * @param other the default value.
   * @return either the contents of the maybe, or the default. */
  public T orElse(final T other) {
    return value != null ? value : other;
  }

  @Override public String toString() {
    Log.e("LCS", "Called toString() on a Maybe:" + value);
    if (value != null)
      return "??" + value.toString() + "??";
    return "??NULL??";
  }

  /** Cached empty maybe. */
  @SuppressWarnings({ "rawtypes", "unchecked" }) private static final Maybe EMPTY = new Maybe(null);

  /** Cached last value. */
  @SuppressWarnings("rawtypes") private static SoftReference<Maybe> LAST_MAYBE = new SoftReference<Maybe>(
      EMPTY);

  /** Returns an empty maybe, which is cached.
   * @return An empty maybe. */
  public static <T> Maybe<T> empty() {
    return EMPTY;
  }

  /** Creates a new maybe from the parameter: might serve up the last cached value.
   * @param t not null.
   * @return a new maybe from the parameter. */
  public static <T> Maybe<T> of(@Nullable final T t) {
    if (t == null)
      throw new NullPointerException("tried to create a Maybe of null");
    return getCacheMaybe(t);
  }

  /** Returns a new maybe of a value, which may be null.
   * @param t the value to maybe
   * @return a new maybe of the parameter. */
  public static <T> Maybe<T> ofNullable(@Nullable final T t) {
    return getCacheMaybe(t);
  }

  /** Check whether this maybe is the same as the last one, and serve it from cache: if not, create a
   * new Maybe.
   * @param t the contents to check
   * @return a maybe containing the value */
  private static <T> Maybe<T> getCacheMaybe(@Nullable final T t) {
    if (t == null)
      return EMPTY;
    final Maybe<T> previous = LAST_MAYBE.get();
    /* if SoftReference exists, and is equal to our argument, return that */
    if (previous != null && previous.getNullable() == t)
      return previous;
    return new Maybe<T>(t);
  }
}
