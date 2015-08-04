package lcs.android.util;

/** for {@link SparseMap}s which need to know what the default value of a key is, this interface
 * needs to be implemented. If default values are mutable, then a copy needs to be provided each
 * time! */
public interface DefaultValueKey<V> {
  /** a most common, or initial value of a Key-Value pair, which will not be permanently stored.
   * Needs to be a copy if a mutable value!
   * @return a default value */
  V defaultValue();
}
