package lcs.android.site.map;

import static lcs.android.game.Game.*;

import java.io.Serializable;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Records modifications to sitechanges due to eg. debris.
 * @author addie */
public @NonNullByDefault class MapChangeRecord implements Serializable {
  public MapChangeRecord(final int x, final int y, final int z, final TileSpecial flag) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.flag = flag;
  }

  public MapChangeRecord(final TileSpecial flag) {
    this(i.site.locx, i.site.locy, i.site.locz, flag);
  }

  public TileSpecial flag;

  public final int x;

  public final int y;

  public final int z;

  private static final long serialVersionUID = Game.VERSION;
}