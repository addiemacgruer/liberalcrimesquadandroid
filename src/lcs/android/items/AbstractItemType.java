package lcs.android.items;

import static lcs.android.game.Game.*;

import java.io.Serializable;

import lcs.android.game.Game;
import lcs.android.util.IBuilder;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** The ideal of an item of some type. */
abstract public @NonNullByDefault class AbstractItemType implements Serializable {
  static abstract class AbstractItemTypeBuilder<T> implements Xml.Configurable, IBuilder<T> {
    private int fencevalue;

    private String idname = "";

    private String name = "";

    private String nameFuture = "";

    private String shortname = "";

    private String shortnameFuture = "";

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("name")) {
        name = Xml.getText(value);
      } else if (key.equals("idname")) {
        idname = Xml.getText(value);
      } else if (key.equals("name_future")) {
        nameFuture = Xml.getText(value);
      } else if (key.equals("fencevalue")) {
        fencevalue = Xml.getInt(value);
      } else if (key.equals("shortname")) {
        shortname = Xml.getText(value);
      } else if (key.equals("shortname_future")) {
        shortnameFuture = Xml.getText(value);
      } else if (key.startsWith("name_")) {
        Log.w(Game.LCS, "Redundant key:" + key + "=" + value);
      } else {
        Log.w(Game.LCS, "Unknown K/V:" + key + "=" + value);
      }
    }
  }

  private static class AITSerialProxy implements Serializable {
    private AITSerialProxy(final String idname) {
      this.idname = idname;
    }

    private final String idname;

    @Override public String toString() {
      return idname;
    }

    Object readResolve() {
      return Item.itemTypeForName(idname);
    }

    private static final long serialVersionUID = Game.VERSION;
  };

  /**
   *
   */
  protected AbstractItemType() {
    idname = name = nameFuture = shortname = shortnameFuture = "";
    fencevalue = 0;
  }

  protected AbstractItemType(AbstractItemTypeBuilder<? extends AbstractItemType> aitb) {
    this.fencevalue = aitb.fencevalue;
    this.idname = aitb.idname;
    this.name = aitb.name;
    this.nameFuture = aitb.nameFuture;
    this.shortname = aitb.shortname;
    this.shortnameFuture = aitb.shortnameFuture;
  }

  /** How many $$$ you get for selling this item. dollars */
  public final int fencevalue;

  /** The unique ID for an item of this type. Generally all upper-cased, and starting with an
   * indicator of what type it is: ARMOR_, CLIP_, LOOT_, MONEY_, VEHICLE, WEAPON_. */
  public final String idname;

  private final String name;

  private final String nameFuture;

  private final String shortname;

  private final String shortnameFuture;

  abstract public void displayStats(int viewID);

  /** The generic name for items of this type. If you're still playing after 2100 AD, returns a
   * space-age version, but the item is unchanged.
   * @return human-readable name. */
  @Override public String toString() {
    if (nameFuture.length() > 0 && i.score.date.year() >= 2100) {
      return nameFuture;
    }
    return name;
  }

  // Object readResolve() throws ObjectStreamException {
  // return Types.itemTypeForName(idname);
  // }
  /** A short, human-readable name for this item, or the original if none defined. If still playing
   * in 2100AD, returns a spacey version.
   * @return a short human-readable string. */
  String shortname() {
    if (shortnameFuture.length() > 0 && i.score.date.year() >= 2100) {
      return shortnameFuture;
    } else if (shortname.length() > 0) {
      return shortname;
    } else {
      return toString();
    }
  }

  Object writeReplace() {
    return new AITSerialProxy(idname);
  }

  private static final long serialVersionUID = Game.VERSION;
}
