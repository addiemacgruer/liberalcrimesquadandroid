package lcs.android.items;

import java.util.List;

import lcs.android.combat.Attack;
import lcs.android.game.Game;
import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class Weapon extends AbstractItem<WeaponType> {
  private static class LazyInit {
    private static Weapon none;
    static {
      Log.i("LCS", "Lazy init of WEAPON_NONE");
      none = new Weapon("WEAPON_NONE");
    }
  }

  public Weapon(final String string) {
    super(stringToWeaponType(string));
  }

  public Weapon(final WeaponType platonicIdeal) {
    super(platonicIdeal);
  }

  private int ammo_;

  @Nullable private ClipType loaded_cliptype_;

  public boolean acceptableAmmo(final Clip c) {
    return type.acceptableAmmo(c.type);
  }

  public boolean acceptableAmmo(final ClipType c) {
    return type.acceptableAmmo(c);
  }

  public Attack attack(final boolean force_ranged, final boolean force_melee,
      final boolean force_no_reload) {
    final List<Attack> attacks = type.get_attacks();
    for (final Attack as : attacks) {
      if (force_ranged && !as.ranged) {
        continue;
      }
      if (force_melee && as.ranged) {
        continue;
      }
      if (force_no_reload && as.usesAmmo() && ammo_ == 0) {
        continue;
      }
      if (as.usesAmmo() && as.ammotype != loaded_cliptype_ && ammo_ != 0) {
        continue;
      }
      return as;
    }
    return Attack.empty();
  }

  public boolean autoBreaksLocks() {
    return type.auto_break_lock_;
  }

  public float bashstrengthmod() {
    return type.bashstrengthmod_;
  }

  public boolean canGraffiti() {
    return type.can_graffiti_;
  }

  public boolean canTakeHostages() {
    return type.can_take_hostages_;
  }

  public boolean canThreatenHostages() {
    return type.can_threaten_hostages_;
  }

  public void decreaseAmmo(final int d) {
    ammo_ -= d;
  }

  @Override public String equipTitle() {
    String et = type.toString();
    if (usesAmmo()) {
      et += " (" + ammo_ + ")";
    }
    return et;
  }

  public int get_ammoamount() {
    return ammo_;
  }

  public boolean is_same_type(final Weapon w) {
    return type == w.type;
  }

  @Override public boolean isEmpty() {
    return number == 0;
  }

  public boolean isInstrument() {
    return type.instrument_;
  }

  public boolean isMusical() {
    return type.musical_attack_;
  }

  public boolean isRanged() {
    return type.is_ranged();
  }

  public boolean isSuspicious() {
    return type.suspicious_;
  }

  public boolean isThreatening() {
    return type.threatening_;
  }

  public boolean isThrowable() {
    return type.is_throwable();
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> i) {
    if (i instanceof Weapon && i.type == type) {
      final Weapon w = (Weapon) i; // cast -XML
      if (loaded_cliptype_ == w.loaded_cliptype_ && ammo_ == w.ammo_ || ammo_ == 0 && w.ammo_ == 0) {
        number += w.number;
        w.number = 0;
        return true;
      }
    }
    return false;
  }

  public boolean protectsAgainstKidnapping() {
    return type.protects_against_kidnapping_;
  }

  public boolean reload(final Clip clip) {
    if (acceptableAmmo(clip) && !clip.isEmpty()) {
      loaded_cliptype_ = clip.type;
      ammo_ = clip.ammoAmmount();
      clip.decrease(1);
      return true;
    }
    return false;
  }

  public String shortName() {
    return type.shortname();
  }

  public int size() {
    return type.size_;
  }

  @Override public Weapon split(final int aNumber) throws IllegalArgumentException {
    int lNumber = aNumber;
    if (lNumber < 0)
      throw new IllegalArgumentException("Tried to split fewer than zero:" + lNumber);
    if (lNumber > number) {
      lNumber = number;
    }
    final Weapon newi = new Weapon(type);
    newi.number = lNumber;
    number -= lNumber;
    return newi;
  }

  @Override public String toString() {
    return type.toString();
  }

  public boolean usesAmmo() {
    return type.usesAmmo();
  }

  boolean acceptableAmmo(final AbstractItem<? extends AbstractItemType> c) {
    if (c instanceof Clip)
      return type.acceptableAmmo((ClipType) c.type);
    return false;
  }

  @Nullable private static Weapon none = null;

  private static final long serialVersionUID = Game.VERSION;

  public static Weapon none() {
    return LazyInit.none;
  }

  private static WeaponType stringToWeaponType(final String string) {
    final WeaponType r = Game.type.weapon.get(string);
    if (r == null) {
      Curses.fact("Couldn't create: " + string);
      if (!string.equals("WEAPON_NONE"))
        return stringToWeaponType("WEAPON_NONE");
      throw new RuntimeException("Couldn't create: " + string);
    }
    return r;
  }
}
