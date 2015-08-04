package lcs.android.items;

import java.util.HashMap;
import java.util.Map;

import lcs.android.R;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.creature.CreatureType;
import lcs.android.game.Game;
import lcs.android.site.creation.ConfigSiteMap;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.util.Curses;
import lcs.android.util.LcsRuntimeException;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** A database of items that we use during the game. Because we can regenerate these from our xml
 * files, we don't need to serialize the item type during the save, speeding serialization.
 * {@link AbstractItem} for instance saves the name of its platonic ideal, and not the item itself,
 * which can then be recovered by {@link #itemTypeForName(String)}.
 * <p>
 * This @NonNullByDefault class is initialized first when {@link Game#load}ing a game from a save,
 * so that other classes can refer to it when deserializing themselves.
 * <p>
 * Android compiles all xml into a binary form during preparation of .apk files, so this is the
 * fastest way of doing it. Parsing pure text files with a scanner is much, much slower in
 * comparison.
 * @see Xml */
public @NonNullByDefault class Item { // NO_UCD
  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, ArmorType> armor = new HashMap<String, ArmorType>(40 * 2);

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, ClipType> clip = new HashMap<String, ClipType>(15 * 2);

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, CreatureType> creature = new HashMap<String, CreatureType>();

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, LootType> loot = new HashMap<String, LootType>(30 * 2);

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, ConfigSiteMap> sitemaps = new HashMap<String, ConfigSiteMap>(30 * 2);

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, VehicleType> vehicle = new HashMap<String, VehicleType>(20 * 2);

  /** Items of this type, loaded from xml during initialization. */
  final public Map<String, WeaponType> weapon = new HashMap<String, WeaponType>(40 * 2);

  private final Xml.Configurable xcc = new Xml.Configurable() {
    @Override public Configurable xmlChild(final String value) {
      if ("armortype".equals(value) || "masktype".equals(value)) {
        return new ArmorType.Builder();
      } else if ("cliptype".equals(value)) {
        return new ClipType.Builder();
      } else if ("loottype".equals(value)) {
        return new LootType.Builder();
      } else if ("vehicletype".equals(value)) {
        return new VehicleType.Builder();
      } else if ("weapontype".equals(value)) {
        return new WeaponType.Builder();
      }
      throw new LcsRuntimeException("Unknown type:" + value);
    }

    @Override public void xmlFinishChild() {
      // no action
    }

    @Override public void xmlSet(final String key, final String value) {
      final String exception = "Tried to set values on xcc:" + key + "=" + value;
      Log.e(Game.LCS, exception);
      throw new LcsRuntimeException(exception);
    }
  };

  /** loads everything in from xml. The order of loading is important:
   * <ul>
   * <li>res/xml/armors.xml
   * <li>res/xml/masks.xml
   * <li>res/xml/clips.xml
   * <li>res/xml/loot.xml
   * <li>res/xml/vehicles.xml
   * <li>res/xml/weapons.xml
   * <li>res/xml/creaturetype.xml (which depends on weapons and armor)
   * <li>res/xml/crimesquads.xml (whose uniform depends on weapons and armor)
   * <li>res/xml/sitemaps.xml (which depends on loot)
   * <li>res/xml/sitetypes.xml (which depends on sitemaps and creaturetype)
   * </ul> */
  public void init() {
    Curses.setViewIfNeeded(R.layout.main);
    // do these first: don't depend on anything.
    initXml("Armors", armor, "armors.xml", "masks.xml");
    initXml("Clips", clip, "clips.xml");
    initXml("Loot", loot, "loot.xml");
    initXml("Vehicles", vehicle, "vehicles.xml");
    initXml("Weapons", weapon, "weapons.xml");
    // creature depends on weapons and armor
    initOther("Creatures", CreatureType.configurable, creature, "creaturetype.xml");
    initOther("Crime Squads", CrimeSquad.configurable, null, "crimesquads.xml");
    initOther("Site Maps", SiteMap.CONFIG, sitemaps, "sitemaps.xml");
    initOther("Site Types", AbstractSiteType.CONFIG, null, "sitetypes.xml");
  }

  private void initXml(String what, Map<String, ?> storage, String... xmlFiles) {
    Curses.setText(R.id.loadeditem, what);
    for (String file : xmlFiles) {
      assert file != null;
      new Xml(file).init(xcc).close();
    }
    Log.i("LCS", "Loaded " + what + ":" + storage.size());
  }

  public static AbstractItem<? extends AbstractItemType> itemForName(final String item) {
    final AbstractItemType ait = itemTypeForName(item);
    if (ait instanceof ArmorType) {
      return new Armor(item);
    }
    if (ait instanceof ClipType) {
      return new Clip(item);
    }
    if (ait instanceof LootType) {
      return new Loot(item);
    }
    if (ait instanceof MoneyType) {
      return new Money(0);
    }
    if (ait instanceof VehicleType) {
      return new Vehicle(item);
    }
    if (ait instanceof WeaponType) {
      return new Weapon(item);
    }
    throw new IllegalArgumentException("No such itemtype:" + item);
  }

  /** Searches through the Armor, Weapons, Clips, and Vehicles stores looking for a given item.
   * @param item the ID name of the item, which should start <q>ARMOR_</q>, <q>WEAPON_</q>, <q>CLIP_
   *          </q> or <q>VEHICLE_</q>
   * @return a child of {@link AbstractItemType} with the given ID name, or {@code null} if it
   *         wasn't found. {@code null} logs an error, but should probably throw an exception as the
   *         save game is either corrupted, or there's a speling mistaek somewhere in the code. */
  public static AbstractItemType itemTypeForName(final String item) {
    AbstractItemType iarg = Game.type.armor.get(item);
    if (iarg == null) {
      iarg = Game.type.clip.get(item);
    }
    if (iarg == null) {
      iarg = Game.type.loot.get(item);
    }
    if (iarg == null) {
      iarg = Game.type.vehicle.get(item);
    }
    if (iarg == null) {
      iarg = Game.type.weapon.get(item);
    }
    if (iarg == null) {
      throw new LcsRuntimeException("Couldn't deserialize ItemType:" + item);
    }
    return iarg;
  }

  /** @param what
   * @param configurable
   * @param string2 */
  private static void initOther(String what, Configurable configurable,
      @Nullable Map<String, ?> storage, String... files) {
    Curses.setText(R.id.loadeditem, what);
    for (String file : files) {
      assert file != null;
      new Xml(file).init(configurable).close();
    }
    if (storage != null) {
      Log.i("LCS", "Loaded " + what + ":" + storage.size());
    } else {
      Log.i("LCS", "Loaded " + what);
    }
  }
}
