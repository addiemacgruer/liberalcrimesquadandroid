package lcs.android.site.map;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lcs.android.encounters.EmptyEncounter;
import lcs.android.encounters.Encounter;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.site.SiegeUnitType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class MapTile {
  public Set<TileSpecial> flag = EnumSet.of(TileSpecial.BLOCK);

  public Encounter encounter = new EmptyEncounter();

  public final Set<SiegeUnitType> siegeFlag = EnumSet.noneOf(SiegeUnitType.class);

  @Nullable public SpecialBlocks special = null;

  private final List<AbstractItem<? extends AbstractItemType>> groundLoot = new ArrayList<AbstractItem<? extends AbstractItemType>>();

  public final boolean blocked() {
    return flag.contains(TileSpecial.BLOCK);
  }

  public final boolean fireDamaged() {
    return flag.contains(TileSpecial.FIRE_END) || flag.contains(TileSpecial.FIRE_PEAK)
        || flag.contains(TileSpecial.FIRE_START) || flag.contains(TileSpecial.DEBRIS);
  }

  public List<AbstractItem<? extends AbstractItemType>> groundLoot() {
    return groundLoot;
  }

  public final boolean known() {
    return flag.contains(TileSpecial.KNOWN);
  }

  public final boolean restricted() {
    return flag.contains(TileSpecial.RESTRICTED);
  }
}