package lcs.android.combat;

import static lcs.android.game.Game.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import lcs.android.creature.Creature;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.items.ClipType;
import lcs.android.items.WeaponType;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** One of the attacks that any given weapon can perform */
public @NonNullByDefault class Attack implements Xml.Configurable {
  /** The ammo (if any) used in the weapon. */
  public ClipType ammotype;

  /** whether the attack is ranged, id est whether you can use it in a car chase. */
  public boolean ranged = false;

  /** the creature skill associated with this weapon */
  public Skill skill = Skill.CLUB;

  /** bonus chance to hit, but not cause extra damage. Applies to some machine guns. 5 for the M249. */
  protected int accuracyBonus = 0;

  /** warrants always describing the hit. Currently only used for the molotov. */
  protected boolean alwaysDescribeHit = false;

  /** special description of the attack. Used for most weapons: A <q>does something to</q> B. */
  protected String attackDescription = "assaults";

  /** Used if the item causes fire: flamethrower and molotov only. */
  protected final Fire fire = new Fire();

  /** special description of the hit. Currently only used for the molotov. */
  protected String hitDescription = "striking";

  /** punctuation mark after a hit. molotov gets an exclamation mark */
  protected String hitPunctuation = ".";

  /** the number of times a weapon fires in each attack. Up to five for some machineguns */
  protected int numberAttacks = 1;

  /** the difficulty in landing successive attacks on target. only for (machine) guns; multiplied by
   * the number of shots fired; typical value 3. */
  protected int successiveAttacksDifficulty = 0;

  private int armorPiercing = 0;

  private final Critical critical = new Critical();

  private boolean damagesArmor = false;

  private final Set<Wound> damtype = EnumSet.noneOf(Wound.class);

  private int fixedDamage = 0;

  @SuppressWarnings("unused") private int priority = 1;

  private int randomDamage = 0;

  private Wound severtype = Wound.NONE;

  private int strengthMax = 10;

  private int strengthMin = 5;

  private boolean thrown;

  /** whether a weapon requires reloading when its clip is empty. */
  private boolean usesAmmo = false;

  /** Whether the weapon is thrown in the attack.
   * @return true if so. */
  public boolean thrown() {
    return thrown;
  }

  @Override public String toString() {
    final StringBuilder rval = new StringBuilder();
    rval.append('[');
    rval.append(skill.toString());
    rval.append("] ");
    if (ranged) {
      rval.append("(ranged) ");
    }
    rval.append("Damage: ");
    if (numberAttacks > 1) {
      rval.append(numberAttacks + "x ");
    }
    rval.append(fixedDamage + "-" + (fixedDamage + randomDamage));
    if (armorPiercing > 0) {
      rval.append(" AP: " + armorPiercing);
    }
    if (accuracyBonus != 0) {
      rval.append(" Accuracy: " + accuracyBonus);
    }
    rval.append(" Strength: " + strengthMin + "-" + strengthMax);
    return rval.toString();
  }

  /** Whether the attack uses ammo.
   * @return True if so. */
  public boolean usesAmmo() {
    return usesAmmo;
  }

  @Override public Configurable xmlChild(final String value) {
    if (value.equals("fire")) {
      return fire;
    } else if (value.equals("critical")) {
      return critical;
    } else {
      throw new IllegalArgumentException(value);
    }
  }

  @Override public void xmlFinishChild() {
    if (damtype.isEmpty()) {
      damtype.add(Wound.BRUISED); // If no type specified, then bruise
    }
  }

  @Override public void xmlSet(final String key, final String value) {
    if (key.equals("ranged")) {
      ranged = Xml.getBoolean(value);
    } else if (key.equals("thrown")) {
      thrown = Xml.getBoolean(value);
    } else if (key.equals("ammotype")) {
      ammotype = Game.type.clip.get(Xml.getText(value));
      usesAmmo = true;
    } else if (key.equals("attack_description")) {
      attackDescription = Xml.getText(value);
    } else if (key.equals("always_describe_hit")) {
      alwaysDescribeHit = Xml.getBoolean(value);
    } else if (key.equals("hit_punctuation")) {
      hitPunctuation = Xml.getText(value);
    } else if (key.equals("skill")) {
      skill = WeaponType.skillStringToEnum(Xml.getText(value));
    } else if (key.equals("accuracy_bonus")) {
      accuracyBonus = Xml.getInt(value);
    } else if (key.equals("number_attacks")) {
      numberAttacks = Xml.getInt(value);
    } else if (key.equals("successive_attacks_difficulty")) {
      successiveAttacksDifficulty = Xml.getInt(value);
    } else if (key.equals("strength_min")) {
      strengthMin = Xml.getInt(value);
    } else if (key.equals("strength_max")) {
      strengthMax = Xml.getInt(value);
    } else if (key.equals("random_damage")) {
      randomDamage = Xml.getInt(value);
    } else if (key.equals("fixed_damage")) {
      fixedDamage = Xml.getInt(value);
    } else if (key.equals("bruises")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.BRUISED);
      }
    } else if (key.equals("tears")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.TORN);
      }
    } else if (key.equals("cuts")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.CUT);
      }
    } else if (key.equals("burns")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.BURNED);
      }
    } else if (key.equals("shoots")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.SHOT);
      }
    } else if (key.equals("bleeding")) {
      if (Xml.getBoolean(value)) {
        damtype.add(Wound.BLEEDING);
      }
    } else if (key.equals("severtype")) {
      severtype = WeaponType.severtypeStringToEnum(Xml.getText(value));
    } else if (key.equals("damages_armor")) {
      damagesArmor = Xml.getBoolean(value);
    } else if (key.equals("armorpiercing")) {
      armorPiercing = Xml.getInt(value);
    } else if (key.equals("hit_description")) {
      hitDescription = Xml.getText(value);
    } else if (key.equals("priority")) {
      priority = Xml.getInt(value);
    } else if (key.equals("no_damage_reduction_for_limbs_chance")) {
      Log.i("LCS", "Obsolete property:" + key);
    } else {
      throw new IllegalArgumentException("Unknown Attack property:" + key + "=" + value);
    }
  }

  /** creates a new {@link Hit} containing details of the hit, and calculates critical chances.
   * @param attacker the creature attacking
   * @param burstHits how many burst hits were successful
   * @return details of the hit */
  protected Hit weaponDamage(final Creature attacker, final int burstHits) {
    final Hit hit = new Hit().damageType(Collections.unmodifiableSet(damtype))
        .strengthMin(strengthMin).strengthMax(strengthMax).severType(severtype);
    final boolean criticalSuccess = burstHits >= critical.hitsRequired
        && i.rng.nextInt(100) < critical.chance;
    final int random = criticalSuccess ? critical.randomDamage(randomDamage) : randomDamage;
    final int fixed = criticalSuccess ? critical.fixedDamage(fixedDamage) : fixedDamage;
    if (criticalSuccess && critical.severType().exists()) {
      hit.severType(critical.severType().get());
    }
    if (randomDamage == 0 && fixedDamage == 0) {
      for (int x = 0; x < burstHits; x++) {
        hit.damageAmount(hit.damageAmount() + 1 + i.rng.nextInt(5 + attacker.skill().skill(skill))
            + attacker.skill().skill(skill));
      }
    } else {
      for (int x = 0; x < burstHits; x++) {
        hit.damageAmount(hit.damageAmount() + i.rng.nextInt(random) + fixed);
      }
    }
    hit.damageArmor(damagesArmor);
    hit.armorPiercing(armorPiercing);
    return hit;
  }

  /** @return */
  public static Attack empty() {
    return new Attack();
  }
}
