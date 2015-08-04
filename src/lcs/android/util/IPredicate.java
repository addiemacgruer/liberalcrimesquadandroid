package lcs.android.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** An item which can return a true/false value.
 * @author addie
 * @param <T> the item to test. */
public @NonNullByDefault interface IPredicate<T> {
  /** Test an item
   * @param item the item to test
   * @return a true/false value of the test. */
  boolean apply(T item);
}