package lcs.android.util;

/** Something which can be built.
 * @author addie
 * @param <T> the kind of item which will be built. */
public interface IBuilder<T> {
  /** Build it!
   * @return the item built */
  T build();
}
