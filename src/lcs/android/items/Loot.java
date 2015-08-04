package lcs.android.items;

import java.util.Map.Entry;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Things you can steal when out and about. */
public @NonNullByDefault class Loot extends AbstractItem<LootType> {
  public Loot(final String string) {
    super(stringToLootType(string));
  }

  public Loot(final String string, final int count) {
    super(stringToLootType(string));
    number = count;
  }

  private Loot(final LootType loot) {
    super(loot);
  }

  @Override public String equipTitle() {
    return platonicIdeal.toString();
  }

  /** Whether this is cloth, and can be used to make item crafting cheaper. */
  public boolean isCloth() {
    return platonicIdeal.isCloth();
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> i) {
    if (isStackable() && i instanceof Loot && isSameType(i)) {
      number += i.number();
      i.number = 0;
      return true;
    }
    return false;
  }

  /** Prevents you from selling the item. If the item is unique, such as CEO photos, then it needs to
   * be used up in the newsletter instead. */
  public boolean noQuickFencing() {
    return platonicIdeal.noQuickFencing();
  }

  @Override public Loot split(final int aNumber) throws IllegalArgumentException {
    int lNumber = aNumber;
    if (lNumber < 0)
      throw new IllegalArgumentException("Tried to split fewer than zero:" + lNumber);
    if (lNumber > number) {
      lNumber = number;
    }
    final Loot newi = new Loot(platonicIdeal);
    newi.number = lNumber;
    number -= lNumber;
    return newi;
  }

  @Override public String toString() {
    return platonicIdeal.toString();
  }

  private boolean isStackable() {
    return platonicIdeal.isStackable();
  }

  private static final long serialVersionUID = Game.VERSION;

  private static LootType stringToLootType(final String string) {
    LootType r = null;
    for (final Entry<String, LootType> at : Game.type.loot.entrySet()) {
      if (at.getKey().equals(string)) {
        r = at.getValue();
        break;
      }
    }
    if (r == null)
      throw new RuntimeException("Couldn't create: " + string);
    return r;
  }
}
