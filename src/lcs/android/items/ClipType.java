package lcs.android.items;

import lcs.android.game.Game;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** The mental picture of a clip that allows us to recognize specific instances of them. */
public @NonNullByDefault class ClipType extends AbstractItemType {
  static class Builder extends AbstractItemTypeBuilder<ClipType> {
    private int ammo;

    @Override public ClipType build() {
      return new ClipType(this);
    }

    @Override public Configurable xmlChild(final String value) {
      return Xml.UNCONFIGURABLE;
    }

    @Override public void xmlFinishChild() {
      Builder ltb = this;
      ClipType lt = ltb.build();
      Game.type.clip.put(lt.idname, lt);
    }

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("ammo")) {
        ammo = Xml.getInt(value);
      } else {
        super.xmlSet(key, value);
      }
    }
  }

  private static class LazyInit {
    private static final ClipType none;
    static {
      Log.i("LCS", "Lazy Init of ClipType");
      none = new ClipType();
    }
  }

  ClipType(Builder ltb) {
    super(ltb);
    this.ammo = ltb.ammo;
  }

  private ClipType() {
    super();
    ammo = 0;
  }

  /** The number of bullets contained in a clip of this kind. */
  final int ammo;

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

  /** @return */
  public static ClipType none() {
    return LazyInit.none;
  }
}
