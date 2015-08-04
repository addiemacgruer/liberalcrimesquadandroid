package lcs.android.items;

import lcs.android.game.Game;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** The dreams of thieves. */
public @NonNullByDefault class LootType extends AbstractItemType {
  static class Builder extends AbstractItemTypeBuilder<LootType> {
    private boolean cloth;

    private boolean noQuickFencing;

    private boolean stackable;

    @Override public LootType build() {
      return new LootType(this);
    }

    @Override public Configurable xmlChild(final String value) {
      return Xml.UNCONFIGURABLE;
    }

    @Override public void xmlFinishChild() {
      Builder ltb = this;
      LootType lt = ltb.build();
      Game.type.loot.put(lt.idname, lt);
    }

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("stackable")) {
        stackable = Xml.getBoolean(value);
      } else if (key.equals("no_quick_fencing")) {
        noQuickFencing = Xml.getBoolean(value);
      } else if (key.equals("cloth")) {
        cloth = Xml.getBoolean(value);
      } else {
        super.xmlSet(key, value);
      }
    }
  }

  LootType(Builder ltb) {
    super(ltb);
    this.cloth = ltb.cloth;
    this.noQuickFencing = ltb.noQuickFencing;
    this.stackable = ltb.stackable;
  }

  /** Returns if the loot is usable as cloth when making clothing. */
  final boolean cloth;

  /** Returns if the loot should be skipped when quick fencing all loot. */
  final boolean noQuickFencing;

  /** Returns if the loot type should be stacked or not. Unique things, such as CEO photos, don't
   * stack, even if you've two sets. */
  final boolean stackable;

  /* (non-Javadoc)
   * @see lcs.android.items.AbstractItemType#displayStats(int) */
  @Override public void displayStats(final int viewID) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
