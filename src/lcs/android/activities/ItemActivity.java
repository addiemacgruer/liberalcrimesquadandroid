package lcs.android.activities;

import lcs.android.activities.iface.Activity;
import lcs.android.game.Game;
import lcs.android.items.AbstractItemType;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class ItemActivity extends AbstractActivity {
  /** Activity which targets in item (basically make armour). Immutable.
   * @param type
   * @param item */
  public ItemActivity(final Activity type, final AbstractItemType item) {
    super(type);
    iarg = item;
  }

  private final AbstractItemType iarg;

  public AbstractItemType itemType() {
    return iarg;
  }

  @Override public String toString() {
    switch (type) {
    case MAKE_ARMOR:
      return "Making " + iarg.toString();
    default:
      return super.toString();
    }
  }

  private static final long serialVersionUID = Game.VERSION;
}
