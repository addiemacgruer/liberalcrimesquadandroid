package lcs.android.items;

import java.util.Map.Entry;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** A clip full of ammunition, such as you might hold in your hand. */
public @NonNullByDefault class Clip extends AbstractItem<ClipType> {
  public Clip(final ClipType ct) {
    this(ct, 1);
  }

  public Clip(final ClipType ct, final int i) {
    super(ct);
    number = i;
  }

  public Clip(final String string) {
    this(string, 1);
  }

  public Clip(final String string, final int i) {
    super(stringToClipType(string));
    number = i;
  }

  @Override public String equipTitle() {
    return ideal().toString();
  }

  @Override public boolean isEmpty() {
    return number == 0;
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> i) {
    if (i instanceof Clip && ideal() == i.ideal()) {
      number += i.number();
      i.number = 0;
      return true;
    }
    return false;
  }

  @Override public Clip split(final int aNumber) throws IllegalArgumentException {
    if (aNumber < 0)
      throw new IllegalArgumentException("Tried to split fewer than zero:" + aNumber);
    int lNumber = aNumber;
    if (aNumber > number) {
      lNumber = number;
    }
    final Clip newi = new Clip(ideal());
    newi.number = lNumber;
    number -= lNumber;
    return newi;
  }

  @Override public String toString() {
    return ideal().toString();
  }

  /** The number of bullets contained in a clip of this kind. */
  int ammoAmmount() {
    return ideal().ammoAmmount();
  }

  private static final long serialVersionUID = Game.VERSION;

  private static ClipType stringToClipType(final String string) {
    ClipType r = null;
    for (final Entry<String, ClipType> at : Game.type.clip.entrySet()) {
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
