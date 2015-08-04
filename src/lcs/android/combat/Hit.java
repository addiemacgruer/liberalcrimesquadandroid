package lcs.android.combat;

import java.util.EnumSet;
import java.util.Set;

import lcs.android.creature.health.Wound;
import lcs.android.util.Getter;
import lcs.android.util.Setter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** POJO for the amount of damage done in a hit.
 * @author addie */
public @NonNullByDefault class Hit {
  private int armorPiercing = 0;

  private int damageAmount = 0;

  private boolean damageArmor = false;

  @Nullable private Set<Wound> mDamageType = EnumSet.noneOf(Wound.class);

  @Nullable private Wound severType = null;

  private int strengthMax = 1;

  private int strengthMin = 1;

  public Hit addDamageType(final Wound damageType) {
    mDamageType.add(damageType);
    return this;
  }

  @Setter public Hit armorPiercing(final int piercing) {
    armorPiercing = piercing;
    return this;
  }

  @Getter public int damageAmount() {
    return damageAmount;
  }

  @Setter public Hit damageAmount(final int amount) {
    damageAmount = amount;
    return this;
  }

  @Setter public Hit severType(final Wound type) {
    severType = type;
    return this;
  }

  @Setter public Hit strengthMax(final int max) {
    strengthMax = max;
    return this;
  }

  @Setter public Hit strengthMin(final int min) {
    strengthMin = min;
    return this;
  }

  @Getter protected boolean damageArmor() {
    return damageArmor;
  }

  @Setter protected Hit damageArmor(final boolean damage) {
    damageArmor = damage;
    return this;
  }

  @Getter int armorPiercing() {
    return armorPiercing;
  }

  @Nullable @Getter Set<Wound> damageType() {
    return mDamageType;
  }

  @Setter Hit damageType(@Nullable final Set<Wound> attackWounds) {
    if (attackWounds == null)
      throw new IllegalArgumentException("attackWounds was null");
    mDamageType = attackWounds;
    return this;
  }

  @Nullable @Getter Wound severType() {
    return severType;
  }

  @Getter int strengthMax() {
    return strengthMax;
  }

  @Getter int strengthMin() {
    return strengthMin;
  }
}
