package lcs.android.combat;

import lcs.android.creature.health.Wound;
import lcs.android.items.WeaponType;
import lcs.android.util.Maybe;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** An attack may contain details of a critical hit, which may cause extra damage or sever limbs. */
@NonNullByDefault class Critical implements Xml.Configurable {
  /** percentage chance of a critical, if the number of {@link #hitsRequired} is met */
  protected int chance = 0;

  /** number of hits required to cause a critical. Default 1. */
  protected int hitsRequired = 1;

  private int fixedDamage = 1;

  private boolean fixedDamageDefined = false;

  private int randomDamage = 1;

  private boolean randomDamageDefined = false;

  @Nullable private Wound severType = null;

  /** if fixed damage is defined in the critical, return that; otherwise return the old value
   * @param old the value of a non-critical attack
   * @return critical fixed damage if defined. */
  public int fixedDamage(final int old) {
    if (fixedDamageDefined) {
      return fixedDamage;
    }
    return old;
  }

  /** if random damage is defined in the critical, return that; otherwise return the old value
   * @param old the value of a non-critical attack
   * @return critical random damage if defined. */
  public int randomDamage(final int old) {
    if (randomDamageDefined) {
      return randomDamage;
    }
    return old;
  }

  /** if sever damage is defined in the critical, return that; otherwise return an emtpy maybe.
   * @return critical sever damage if defined. */
  public Maybe<Wound> severType() {
    return Maybe.ofNullable(severType);
  }

  @Override public Xml.Configurable xmlChild(final String value) {
    return this;
  }

  @Override public void xmlFinishChild() {
    // no code
  }

  @Override public void xmlSet(final String key, final String value) {
    if (key.equals("chance")) {
      chance = Xml.getInt(value);
    } else if (key.equals("hits_required")) {
      hitsRequired = Xml.getInt(value);
    } else if (key.equals("random_damage")) {
      randomDamage = Xml.getInt(value);
      randomDamageDefined = true;
    } else if (key.equals("fixed_damage")) {
      fixedDamage = Xml.getInt(value);
      fixedDamageDefined = true;
    } else if (key.equals("severtype")) {
      severType = WeaponType.severtypeStringToEnum(Xml.getText(value));
    }
  }
}