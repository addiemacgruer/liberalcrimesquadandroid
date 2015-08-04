package lcs.android.creature;

import static lcs.android.game.Game.*;
import lcs.android.combat.Hit;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;

public enum SpecialAttacks {
  CANNON {
    @Override public Hit damage(final Creature a, final int bursthits) {
      return new Hit().damageAmount(i.rng.nextInt(5000) + 5000).armorPiercing(20)
          .addDamageType(Wound.BURNED).addDamageType(Wound.TORN).addDamageType(Wound.SHOT)
          .addDamageType(Wound.BLEEDING).strengthMin(0).strengthMax(0);
    }
  },
  FLAME {
    @Override public Hit damage(final Creature a, final int bursthits) {
      return baseDamage(a, bursthits).addDamageType(Wound.BURNED);
    }
  },
  NONE {
    @Override public Hit damage(final Creature a, final int bursthits) {
      return baseDamage(a, bursthits).addDamageType(Wound.TORN);
    }
  },
  SUCK {
    @Override public Hit damage(final Creature a, final int bursthits) {
      return baseDamage(a, bursthits).addDamageType(Wound.CUT);
    }
  };
  abstract public Hit damage(Creature a, int bursthits);

  private static Hit baseDamage(final Creature a, final int bursthits) {
    final Hit dam = new Hit().strengthMin(5).strengthMax(10);
    for (int x = 0; x < bursthits; x++) {
      dam.damageAmount(dam.damageAmount() + i.rng.nextInt(5 + a.skill().skill(Skill.HANDTOHAND))
          + 1 + a.skill().skill(Skill.HANDTOHAND));
    }
    dam.severType(Wound.NASTYOFF);
    return dam;
  }
}