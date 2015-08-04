package lcs.android.basemode.iface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lcs.android.daily.Siege;
import lcs.android.game.Game;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** contains properties which are only relevant if the location is rented to the LCS */
public @NonNullByDefault class LCSLocation implements Serializable { // NO_UCD
  LCSLocation(final Location parent) {
    siege = new Siege(parent);
  }

  public int compoundStores;

  public Set<Compound> compoundWalls = EnumSet.noneOf(Compound.class);

  @Nullable public BusinessFronts frontBusiness = null;

  public int heat;

  public final List<AbstractItem<? extends AbstractItemType>> loot = new ArrayList<AbstractItem<? extends AbstractItemType>>();

  public boolean newRental;

  public final Siege siege;

  protected boolean haveFlag;

  String frontName = "";

  @Override public String toString() {
    return frontName;
  }

  private static final long serialVersionUID = Game.VERSION;
}