package lcs.android.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** SparseMaps are like a normal map, but the keys have a <q>default value</q>, which is not stored
 * on serialization. LCS generally serializes every day, so this time soon adds up */
public @NonNullByDefault class SparseMap<K extends DefaultValueKey<V>, V> implements Map<K, V>,
    Serializable {
  private static class EmptySMProxy<K extends DefaultValueKey<?>> implements Serializable {
    private EmptySMProxy(final Class<K> keyclass) {
      this.keyclass = keyclass;
    }

    private final Class<K> keyclass;

    @Override public String toString() {
      return "ESMP:" + keyclass;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) Object readResolve() {
      return new SparseMap(keyclass);
    }

    private static final long serialVersionUID = Game.VERSION;
  }

  private SparseMap(final Class<K> keyclass) {
    this.keyclass = keyclass;
    backingStore = new HashMap<K, V>();
  }

  private final Map<K, V> backingStore;

  private final Class<K> keyclass;

  /** clears the map. */
  @Override public void clear() {
    backingStore.clear();
  }

  /** returns true if key is an instance of the keyclass, or false otherwise. */
  @Override public boolean containsKey(final @Nullable Object key) {
    if (keyclass.isInstance(key))
      return true;
    return false;
  }

  /** returns whether the map contains a given value. Note that default values are not stored. */
  @Override public boolean containsValue(final @Nullable Object value) {
    return backingStore.containsValue(value);
  }

  /** returns the set of entries with non-default values. */
  @Override public Set<java.util.Map.Entry<K, V>> entrySet() {
    return backingStore.entrySet();
  }

  /** gets the value of a key from the map. Will return the default value if the key is missing. */
  @Nullable @SuppressWarnings("unchecked")// we have checked that key is an instance of
  // the keyclass.
  @Override public V get(final @Nullable Object key) {
    if (key == null || !keyclass.isInstance(key))
      return null;
    V value = backingStore.get(key);
    if (value == null) {
      /* the following shenanigans are needed for eg. Sets, which may be changed and not returned to
       * the collection. */
      value = ((DefaultValueKey<V>) key).defaultValue();
      backingStore.put((K) key, value);
    }
    return value;
  }

  /** returns false: the map always appears to contain the default values for its keys. */
  @Override public boolean isEmpty() {
    return false;
  }

  /** returns the set of keys with non-default values */
  @Override public Set<K> keySet() {
    if (keyclass.isEnum())
      return new HashSet<K>(Arrays.asList(keyclass.getEnumConstants()));
    return backingStore.keySet();
  }

  /** adds the key / value pair to the map and returns the old value. */
  @Override @Nullable public V put(final @Nullable K key, final @Nullable V value) {
    if (key == null)
      return null;
    if (key.defaultValue().equals(value)) {
      final V old = backingStore.get(key);
      backingStore.remove(key);
      return old;
    }
    return backingStore.put(key, value);
  }

  /** adds a given map to the backing map. */
  @Override public void putAll(final @Nullable Map<? extends K, ? extends V> map) {
    if (map == null)
      return;
    for (final Entry<? extends K, ? extends V> e : map.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  /** removes a given key from the map, returning true if the key had a non-default value. */
  @Override public V remove(final @Nullable Object key) {
    return backingStore.remove(key);
  }

  /** returns the number of non-default keys in the map. */
  @Override public int size() {
    if (keyclass.isEnum())
      return keyclass.getEnumConstants().length;
    return backingStore.size();
  }

  @Override public String toString() {
    return backingStore.toString();
  }

  /** returns the list of non-default values. */
  @Override public Collection<V> values() {
    return backingStore.values();
  }

  Object writeReplace() {
    final Set<K> backkeys = new HashSet<K>(backingStore.keySet());
    for (final K entry : backkeys) {
      if (entry.defaultValue().equals(backingStore.get(entry))) {
        backingStore.remove(entry);
      }
    }
    if (backingStore.size() == 0)
      return new EmptySMProxy<K>(keyclass);
    return this;
  }

  private static final long serialVersionUID = Game.VERSION;

  /** returns a new SparseMap. Behaves similarly to a normal Map in practice, although keys have
   * associated default values which are not stored if unnecessary, for faster serialization where
   * that is important.
   * @param keyclass A @NonNullByDefault class which implements DefaultValueKey to be used as a map
   *          key.
   * @return A new SparseMap instance. */
  public static <K extends DefaultValueKey<V>, V> SparseMap<K, V> of(final Class<K> keyclass) {
    return new SparseMap<K, V>(keyclass);
  }
}
