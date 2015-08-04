package lcs.android.creature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lcs.android.game.Game;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Clip;
import lcs.android.items.ClipType;
import lcs.android.items.Weapon;
import lcs.android.util.Setter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class CreatureWeapon implements Serializable {
  CreatureWeapon(final Creature creature) {
    this.creature = creature;
    weapon = Weapon.none();
  }

  private final List<Clip> clips = new ArrayList<Clip>();

  private final Creature creature;

  private final List<Weapon> extraThrowingWeapons = new ArrayList<Weapon>();

  private boolean hasThrownWeapon = false;

  private Weapon weapon = Weapon.none();

  public boolean canReload() {
    for (final Clip clip : clips()) {
      if (weapon().acceptableAmmo(clip))
        return true;
    }
    return false;
  }

  public ClipType clipType() {
    if (!clips().isEmpty())
      return clips().get(0).ideal();
    return ClipType.none();
  }

  public int countClips() {
    int sum = 0;
    for (final Clip clip : clips()) {
      sum += clip.number();
    }
    return sum;
  }

  public int countWeapons() {
    int sum = 0;
    if (weapon() != Weapon.none()) {
      ++sum;
    }
    for (int j = 0; j < extraThrowingWeapons.size(); ++j) {
      sum += extraThrowingWeapons.get(j).number();
    }
    return sum;
  }

  public void dropWeapon(@Nullable final List<AbstractItem<? extends AbstractItemType>> lootpile) {
    if (extraThrowingWeapons.size() == 0) {
      hasThrownWeapon = true;
    }
    if (isArmed() && lootpile != null) {
      lootpile.add(weapon);
    }
    weapon = Weapon.none();
  }

  public void dropWeaponsAndClips(
      @Nullable final List<AbstractItem<? extends AbstractItemType>> lootpile) {
    hasThrownWeapon = false;
    dropWeapon(lootpile);
    if (lootpile != null) {
      lootpile.addAll(extraThrowingWeapons);
      lootpile.addAll(clips());
    }
    extraThrowingWeapons.clear();
    clips().clear();
  }

  public List<Weapon> getExtraThrowingWeapons() {
    return extraThrowingWeapons;
  }

  public void giveCreatureWeapon(final WeaponAssigner wa) {
    wa.assign(creature);
  }

  public CreatureWeapon giveWeapon(final Weapon w,
      @Nullable final List<AbstractItem<? extends AbstractItemType>> lootpile) {
    if (w.size() != 0) {
      if (weapon.isThrowable() && weapon.is_same_type(w)) {
        final int take_number = 10 - countWeapons();
        if (take_number > 0) {
          extraThrowingWeapons.add(w.split(1));
        }
      } else if (lootpile == null) {
        weapon = Weapon.none();
        extraThrowingWeapons.clear();
      } else {
        lootpile.add(weapon);
        lootpile.addAll(extraThrowingWeapons);
        extraThrowingWeapons.clear();
      }
    }
    weapon = w.split(1);
    if (lootpile == null) {
      for (final Iterator<Clip> iter = clips().iterator(); iter.hasNext();) {
        if (!weapon.acceptableAmmo(iter.next())) {
          iter.remove();
        }
      }
    } else {
      for (final Iterator<Clip> iter = clips().iterator(); iter.hasNext();) {
        final Clip clip = iter.next();
        if (!weapon.acceptableAmmo(clip)) {
          lootpile.add(clip);
          iter.remove();
        }
      }
    }
    return this;
  }

  public boolean hasThrownWeapon() {
    return hasThrownWeapon;
  }

  public boolean isArmed() {
    return !weapon.id().equals("WEAPON_NONE");
  }

  public boolean readyAnotherThrowingWeapon() {
    if (extraThrowingWeapons.size() != 0) {
      weapon = extraThrowingWeapons.get(0);
      extraThrowingWeapons.remove(0);
      return true;
    }
    hasThrownWeapon = false;
    return false;
  }

  public boolean reload(final boolean wasteful) {
    boolean r;
    if (weapon().usesAmmo() && clips().size() > 0 && (wasteful || weapon().get_ammoamount() == 0)) {
      r = weapon().reload(clips().get(0));
      if (clips().get(0).isEmpty()) {
        clips().remove(0);
      }
    } else {
      r = false;
    }
    return r;
  }

  @Setter public void setHasThrownWeapon(final boolean hasThrownWeapon) {
    this.hasThrownWeapon = hasThrownWeapon;
  }

  public boolean takeClips(final Clip clip, final int initialNumber) {
    int number = initialNumber;
    if (number + countClips() >= 9) {
      number = 9 - countClips();
    }
    if (number > clip.number()) {
      number = clip.number();
    }
    if (number > 0 && weapon().acceptableAmmo(clip)) {
      final Clip clip2 = clip.split(number);
      clips().add(clip2);
      return true;
    }
    return false;
  }

  @Nullable @Override public String toString() {
    return creature + ":" + weapon;
  }

  public Weapon weapon() {
    return weapon;
  }

  public boolean willDoRangedAttack(final boolean forceRanged, final boolean forceMelee) {
    final boolean r;
    if (weapon != Weapon.none()) // Is the creature armed?
    {
      final boolean reload_allowed = canReload();
      return
      /* Any attacks possible under circumstances? */
      weapon.attack(forceRanged, forceMelee, reload_allowed).ranged
          /* Is the attacked ranged? */
          && (!weapon.attack(forceRanged, forceMelee, reload_allowed).usesAmmo() || weapon
              .get_ammoamount() != 0);
      /* or does it have ammo? */
    }
    return false;
  }

  public boolean willReload(final boolean forceRanged, final boolean forceMelee) {
    return weapon().usesAmmo() && weapon().get_ammoamount() == 0 && canReload()
        && weapon().attack(forceRanged, forceMelee, false).usesAmmo();
  }

  void giveCreatureWeapon(final String string) {
    giveCreatureWeapon(new WeaponAssigner(string));
  }

  private List<Clip> clips() {
    return clips;
  }

  private static final long serialVersionUID = Game.VERSION;
}