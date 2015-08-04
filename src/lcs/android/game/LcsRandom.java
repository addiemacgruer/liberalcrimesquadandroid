package lcs.android.game;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ac.hiroshimau.sci.math.Twister;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Our random number generator, based on the Mersenne Twister algorithm. Adds a few methods to the
 * base random number @NonNullByDefault class (randFrom...) so that we can play with arrays and
 * collections a bit more readily. */
public final @NonNullByDefault class LcsRandom extends Twister {
  /** Gets the probability of something which is unlikely. eg. chance(5) has a 1-in-five chance of
   * occurrence.
   * @param chance the chance
   * @return whether it succeeded. */
  public boolean chance(final int chance) {
    return !likely(chance);
  }

  /** returns one element from a given list.
   * @param list the list
   * @return one element of it. */
  public <T> T choice(final T... list) {
    return list[nextInt(list.length)];
  }

  /** Gets the probability of something which is likely. eg. likely(5) has a four-in-five chance of
   * occurrence.
   * @param chance the chance
   * @return whether it succeeded. */
  public boolean likely(final int chance) {
    if (chance == 0) {
      return false;
    }
    if (chance == 2) {
      return nextBoolean();
    }
    return nextInt(chance) != 0;
  }

  /** returns an int in the range [0..n), where 0 is inclusive and n is exclusive. Over-rides the
   * base method which throws {@link IllegalArgumentException} if you ask for 0, to just return 0
   * instead. */
  @Override public int nextInt(final int n) {
    /* nextInt(0) causes an IAE in Java's built-in Random @NonNullByDefault class, so we need to
     * check for it. */
    return n <= 0 ? 0 : super.nextInt(n);
  }

  /** Gets a random element from an array.
   * @param t an array
   * @return an element from the array
   * @throws IllegalArgumentException if array is empty. */
  public <T> T randFromArray(final T[] t) {
    if (t.length == 0) {
      throw new IllegalArgumentException("array is empty");
    }
    return t[nextInt(t.length)];
  }

  /** Gets a random element from a collection.
   * @param collection a collection
   * @return an element from the collection, or {@code null} if the collection is empty. */
  public <T> T randFromCollection(final Collection<T> collection) {
    if (collection.isEmpty()) {
      throw new IllegalArgumentException("collection is empty");
    }
    int rval = nextInt(collection.size());
    final Iterator<T> it = collection.iterator();
    while (rval != 0) {
      it.next();
      rval--;
    }
    return it.next();
  }

  /** Gets a random element from a list.
   * @param l a list
   * @return an element from the list. */
  public <T> T randFromList(final List<T> l) {
    if (l.isEmpty()) {
      throw new IllegalArgumentException("List empty");
    }
    return l.get(nextInt(l.size()));
  }

  /** @param creature
   * @return */
  public <T, U> U randFromMap(Map<T, U> map) {
    return randFromCollection(map.values());
  }

  /** Gets a random element from a set.
   * @param s a set
   * @return an element from the set, or {@code null} if the set is empty. */
  public <T> T randFromSet(final Set<T> s) {
    return randFromCollection(s);
  }

  private static final long serialVersionUID = Game.VERSION;
}