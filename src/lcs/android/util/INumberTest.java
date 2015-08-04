package lcs.android.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** An interface defining things which can return an integer value.
 * @author addie
 * @param <T> The kind of thing which will be tested. */
public @NonNullByDefault interface INumberTest<T> {
  /** Retreive the integer value of an item.
   * @param t the item
   * @return its integer value. */
  int valueOf(T t);
}
