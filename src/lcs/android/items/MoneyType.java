package lcs.android.items;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** The abstract ideal of money. Represents $1: a singleton, created for consistency with the other
 * types of things, it has no other properties. */
public @NonNullByDefault class MoneyType extends AbstractItemType {
  private MoneyType() {}

  /* (non-Javadoc)
   * @see lcs.android.items.AbstractItemType#displayStats(int) */
  @Override public void displayStats(final int viewID) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  private static final MoneyType INSTANCE = new MoneyType();

  /**
     *
     */
  private static final long serialVersionUID = Game.VERSION;

  /** Money is the same the world over... */
  protected static MoneyType instance() {
    return INSTANCE;
  }
}
