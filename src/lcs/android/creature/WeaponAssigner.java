package lcs.android.creature;

import lcs.android.game.Game;
import lcs.android.game.Range;
import lcs.android.items.Clip;
import lcs.android.items.ClipType;
import lcs.android.items.Weapon;
import lcs.android.items.WeaponType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault class WeaponAssigner {
  /** Create a weapon type from an xml definition string such as <item>REVOLVER_44:44=4</item>
   * <item>SEMIPISTOL_9MM:9=4</item> <item>AUTORIFLE_AK47:ASSAULT=4</item>
   * <item>SHOTGUN_PUMP:BUCKSHOT=4</item> <item>CARBINE_M4:ASSAULT=4</item>
   * <item>SMG_MP5:SMG=4</item> <item>MOLOTOV=5</item>
   * @param weapon the weapon description string. */
  WeaponAssigner(final String weapon) {
    final String[] type = weapon.split(":");
    if (type[0].contains("=")) { // multiples of weapon, ie. throwing
      final String[] weap = type[0].split("=");
      weaponType = Game.type.weapon.get("WEAPON_" + weap[0]);
      weaponTypeCount = new Range(weap[1]);
    } else {
      weaponType = Game.type.weapon.get("WEAPON_" + type[0]);
      weaponTypeCount = new Range("0+1");
    }
    if (type.length == 2) { // ammo, too
      final String[] ammo = type[1].split("=");
      clipType = Game.type.clip.get("CLIP_" + ammo[0]);
      if (clipType == null) {
        throw new IllegalArgumentException("No such ClipType:" + weapon);
      }
      clipTypeCount = new Range(ammo[1]);
    } else {
      clipType = null;
      clipTypeCount = null;
    }
  }

  @Nullable private final ClipType clipType;

  @Nullable private final Range clipTypeCount;

  private final WeaponType weaponType;

  private final Range weaponTypeCount;

  @Override public String toString() {
    return "WeaponAssigner:" + weaponType;
  }

  void assign(final Creature creature) {
    for (int x = 0, count = weaponTypeCount.aValue(); x < count; x++) {
      creature.weapon().giveWeapon(new Weapon(weaponType), null);
    }
    if (clipType != null) {
      final int count = clipTypeCount.aValue();
      final Clip clip = new Clip(clipType, count);
      creature.weapon().takeClips(clip, count);
    }
    creature.weapon().reload(false);
  }
}