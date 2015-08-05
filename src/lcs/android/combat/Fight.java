package lcs.android.combat;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lcs.android.R;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.Receptive;
import lcs.android.creature.SpecialAttacks;
import lcs.android.creature.health.Animal;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.SpecialWounds;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.GameMode;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Pair;

public final @NonNullByDefault class Fight {
  public enum Fighting {
    STEALTH {
      @Override List<Creature> fighters() {
        return US.fighters();
      }
    },
    US {
      @Override List<Creature> fighters() {
        return new ArrayList<Creature>(i.activeSquad());
      }
    },
    THEM {
      @Override List<Creature> fighters() {
        return new ArrayList<Creature>(i.currentEncounter().creatures());
      }
    },
    BOTH {
      @Override List<Creature> fighters() {
        final List<Creature> rval = new ArrayList<Creature>();
        rval.addAll(i.activeSquad());
        rval.addAll(i.currentEncounter().creatures());
        return rval;
      }
    };
    abstract List<Creature> fighters();
  }

  private Fight() {}

  private static final BodyPart[] TARGET = { BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT,
      BodyPart.ARM_LEFT, BodyPart.ARM_RIGHT, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT,
      BodyPart.ARM_LEFT, BodyPart.ARM_RIGHT, BodyPart.BODY, BodyPart.BODY, BodyPart.BODY,
      BodyPart.BODY, BodyPart.HEAD };

  private static final Comparator<Creature> FASTEST = new Comparator<Creature>() {
    @Override public int compare(final @Nullable Creature lhs, final @Nullable Creature rhs) {
      assert lhs != null;
      assert rhs != null;
      return rhs.skill().getAttribute(Attribute.AGILITY, true)
          - lhs.skill().getAttribute(Attribute.AGILITY, true);
    }
  };

  /** Start fighting.
   * @param who Whether it's us, them, or both. */
  public static void fight(final Fighting who) {
    /* anger the locals */
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.enemy()) {
        e.receptive(Receptive.ANGERED);
      }
    }
    final List<Creature> fighters = who.fighters();
    Collections.sort(fighters, Fight.FASTEST);
    for (final Creature p : fighters) {
      if (i.activeSquad().contains(p)) {
        youattack(p);
      } else {
        enemyattack(p);
      }
    }
    if (i.site.current() != null && i.site.current().lcs().siege.siege) {
      coverFire();
    }
  }

  /** Reload, non-wastefully. */
  public static void reloadparty() {
    Fight.reloadparty(false);
  }

  /** everybody reload!
   * @param wasteful true will dispose of part-filled clips */
  public static void reloadparty(final boolean wasteful) {
    if (i.activeSquad() == null) {
      return;
    }
    for (final Creature p : i.activeSquad()) {
      if (!p.health().alive()) {
        continue;
      }
      if (p.weapon().hasThrownWeapon()) {
        p.weapon().readyAnotherThrowingWeapon();
      } else if (p.weapon().canReload()) {
        p.weapon().reload(wasteful);
      }
    }
  }

  private static void addHandToHandDescription(final StringBuilder str, final Creature attacker) {
    if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND) + 1)) {
      str.append("punches");
    } else if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND))) {
      str.append("swings at");
    } else if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND) - 1)) {
      str.append("grapples with");
    } else if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND) - 2)) {
      str.append("kicks");
    } else if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND) - 3)) {
      str.append("strikes at");
    } else if (i.rng.chance(attacker.skill().skill(Skill.HANDTOHAND) - 4)) {
      str.append("jump kicks");
    } else {
      str.append("gracefully strikes at");
    }
  }

  /** attack handling for an individual creature and its target. Does not force melee.
   * @param attacker who's attacking
   * @param target who their target is
   * @param mistake whether it's by mistake
   * @return true if the attack succeeded */
  private static boolean attack(final Creature p, final Creature target, final boolean mistake) {
    return attack(p, target, mistake, false);
  }

  /** attack handling for an individual creature and its target
   * @param attacker who's attacking
   * @param target who their target is
   * @param mistake whether it's by mistake
   * @param forceMelee whether they're being forced to melee
   * @return true if the attack succeeded */
  private static boolean attack(final Creature attacker, final Creature target,
      final boolean mistake, final boolean forceMelee) {
    // INCAPACITATED
    attacker.forceIncapacitated(false);
    if (incapacitated(attacker, false, R.id.gmessages).first) {
      attacker.forceIncapacitated(true);
      return false;
    }
    if (!forceMelee
        && (attacker.type().ofType("COP") && attacker.alignment() == Alignment.MODERATE
            && attacker.enemy() || attacker.type().ofType("SCIENTIST_EMINENT")
            || attacker.type().ofType("JUDGE_LIBERAL")
            || attacker.type().ofType("JUDGE_CONSERVATIVE")
            || attacker.type().ofType("CORPORATE_CEO") && i.rng.chance(2)
            || attacker.type().ofType("RADIOPERSONALITY") || attacker.type().ofType("NEWSANCHOR") || attacker
            .weapon().weapon().isMusical())
        && !mistake
        && (attacker.weapon().weapon().isMusical() || !attacker.weapon().isArmed() || attacker
            .alignment() != Alignment.LIBERAL)) {
      if (attacker.alignment() == Alignment.LIBERAL) {
        specialAttack(attacker, target);
        return false;
      }
    }
    // RELOAD
    if ((attacker.weapon().willReload(i.mode() == GameMode.CHASECAR, forceMelee) || attacker
        .weapon().hasThrownWeapon() && attacker.weapon().getExtraThrowingWeapons().size() != 0)
        && !forceMelee) {
      final StringBuilder str = new StringBuilder();
      if (attacker.weapon().willReload(i.mode() == GameMode.CHASECAR, forceMelee)) {
        attacker.weapon().reload(false);
        str.append(attacker.toString()).append(" reloads.");
      } else if (attacker.weapon().hasThrownWeapon()
          && attacker.weapon().getExtraThrowingWeapons().size() != 0) {
        attacker.weapon().readyAnotherThrowingWeapon();
        str.append(attacker.toString()).append(" readies another ")
            .append(attacker.weapon().weapon().toString()).append('.');
      }
      ui().text(str.toString()).color(attacker.alignment().color()).add();
      return false;
    } else if (attacker.weapon().hasThrownWeapon()) {
      attacker.weapon().setHasThrownWeapon(false);
    }
    final Attack attackUsed = attacker
        .weapon()
        .weapon()
        .attack(i.mode() == GameMode.CHASECAR, forceMelee,
            forceMelee || !attacker.weapon().canReload());
    if (attackUsed == null) {
      return false; // Then bail, they can't fight
    }
    boolean melee = attackUsed.ranged ? true : false;
    melee = describeAttack(attacker, target, mistake, attackUsed, melee);
    int aroll = attackRoll(attacker, attackUsed);
    final int droll = defenseRoll(target);
    final int bonus = attackBonus(target, attackUsed);
    attacker.skill().train(attackUsed.skill, droll * 2 + 5);
    target.skill().train(Skill.DODGE, aroll * 2);
    final int bursthits = hitCount(attacker, aroll, droll, bonus, attackUsed);
    if (aroll + bonus > droll) {
      final StringBuilder str = new StringBuilder();
      str.append(attacker.toString()).append(" hits the ");
      final BodyPart bp = randomBodyPart(target, aroll, droll);
      if (target.type().animal() == Animal.HUMAN && i.mode() == GameMode.CHASECAR
          && (bp == BodyPart.LEG_LEFT || bp == BodyPart.LEG_RIGHT)) {
        str.append("car");
        aroll = -20;
      } else {
        str.append(target.type().animal().partName(bp));
      }
      // show multiple hits
      if (bursthits > 1) {
        describeBurstHits(attacker, str, attackUsed, bursthits);
      } else if (attackUsed.alwaysDescribeHit) {
        str.append(", ");
        str.append(attackUsed.hitDescription);
      }
      Hit dam;
      if (attacker.type().animal() == Animal.HUMAN) {
        dam = attackUsed.weaponDamage(attacker, bursthits);
      } else {
        dam = attacker.specialAttack().damage(attacker, bursthits);
      }
      if (target.squad() != null && target.hire() == null) {
        /* Plot Armor: if the founder is hit, inflict 1/2 damage, because founders are cool */
        dam.damageAmount(dam.damageAmount() / 2);
      }
      int mod = 0;
      if (dam.strengthMax() > dam.strengthMin()) {
        // Melee attacks: Maximum strength bonus, minimum
        // strength to deliver full damage
        int strength = attacker.skill().attributeRoll(Attribute.STRENGTH);
        if (strength > dam.strengthMax()) {
          strength = (dam.strengthMax() + strength) / 2;
        }
        mod += strength - dam.strengthMin();
        dam.armorPiercing(dam.armorPiercing() + (strength - dam.strengthMin()) / 4);
      }
      // SKILL BONUS FOR GOOD ROLL
      mod += aroll - droll - 5;
      // DO THE HEALTH MOD ON THE WOUND
      mod -= attacker.skill().attributeRoll(Attribute.HEALTH) - 5;
      // Health and poor accuracy will only avoid critical hits, not stop
      // low-damage attacks
      mod = Math.max(mod, 0);
      dam.damageAmount(damagemod(target, dam.damageType(), dam.damageAmount(), bp,
          dam.armorPiercing(), mod));
      if (i.mode() == GameMode.CHASECAR && (bp == BodyPart.LEG_LEFT || bp == BodyPart.LEG_RIGHT)) {
        dam.damageAmount(0); // no damage to shots to the car body
        str.append(" hitting the car!");
        ui().text(str.toString()).add();
      } else if (dam.damageAmount() > 0) {
        describeDamage(attacker, target, str, attackUsed, bp, dam.damageType(), dam.damageAmount(),
            dam.severType(), dam.damageArmor());
      } else {
        str.append(" to no effect.");
        ui().text(str.toString()).add();
      }
    } else if (melee && aroll < droll - 10 && target.health().blood() > 70
        && target.type().animal() == Animal.HUMAN && target.weapon().isArmed()
        && target.weapon().weapon().attack(false, true, true) != null) {
      final StringBuilder str = new StringBuilder();
      str.append(target.toString());
      str.append(" knocks the blow aside and counters!");
      ui().text(str.toString()).add();
      attack(target, attacker, false);
    } else {
      ui().text(attacker + " misses.").add();
    }
    return true;
  }

  /** Accuracy bonus or penalty that does NOT affect damage or counterattack chance */
  private static int attackBonus(final Creature creature, final Attack attackUsed) {
    int bonus = 0;
    // Hostages interfere with attack
    if (creature.prisoner() != null) {
      bonus -= i.rng.nextInt(10);
    }
    // Weapon accuracy bonuses and penalties
    bonus += attackUsed.accuracyBonus;
    return bonus;
  }

  private static int attackRoll(final Creature creature, final Attack attackUsed) {
    int aroll = creature.skill().skillRoll(attackUsed.skill);
    aroll -= creature.health().modRoll();
    if (creature.prisoner() != null) {
      aroll -= i.rng.nextInt(10);
    }
    // If in a foot chase, double the debilitating effect of injuries
    if (i.mode() == GameMode.CHASEFOOT) {
      aroll -= creature.health().modRoll();
    }
    return Math.max(0, aroll);
  }

  private static void coverFire() {
    for (final Creature p : i.pool) {
      if (!p.health().alive()) {
        continue;
      }
      if (p.alignment() != Alignment.LIBERAL) {
        continue;
      }
      if (p.squad() != null) {
        continue;
      }
      if (p.location() != i.site.current()) {
        continue;
      }
      /* Juice check to engage in cover fire 10% chance for every 10 juice, starting at 10% chance
       * for 0 juice -- caps out at 100% chance to fire at 90 juice
       * if(LCSrandom(10)-i.pool[p].juice/10>0)continue; */
      if (p.weapon().isArmed() && p.weapon().weapon().attack(true, false, false) != null) {
        boolean conf = false;
        if (p.weapon().weapon().get_ammoamount() > 0) {
          conf = true;
        }
        if (p.weapon().weapon().attack(true, false, false).usesAmmo() && p.weapon().canReload()) {
          conf = true;
        }
        if (conf) {
          final List<Creature> goodtarg = new ArrayList<Creature>();
          final List<Creature> badtarg = new ArrayList<Creature>();
          for (final Creature e : i.currentEncounter().creatures()) {
            if (e.health().alive()) {
              if (e.enemy()) {
                goodtarg.add(e);
              } else {
                badtarg.add(e);
              }
            }
          }
          if (goodtarg.isEmpty()) {
            return;
          }
          Creature target = i.rng.randFromList(goodtarg);
          boolean mistake = false;
          if (!badtarg.isEmpty() && i.rng.chance(10)) {
            target = i.rng.randFromList(badtarg);
            mistake = true;
          }
          final boolean actual = attack(p, target, mistake);
          if (actual) {
            if (mistake) {
              i.site.alienationCheck(mistake);
              i.siteStory.addNews(NewsEvent.ATTACKED_MISTAKE);
              i.site.crime(i.site.crime() + 10);
            }
            if (!p.weapon().isArmed()) {
              p.crime().criminalize(Crime.ASSAULT);
            } else {
              p.crime().criminalize(Crime.ARMEDASSAULT);
            }
          }
          if (!target.health().alive()) {
            if (i.mode() == GameMode.SITE) {
              final Creature cr = target;
              cr.dropLoot(i.groundLoot());
            }
            i.currentEncounter().creatures().remove(target);
          }
        }
      }
    }
  }

  /** adjusts attack damage based on armor, other factors */
  private static int damagemod(final Creature t, final Set<Wound> damtype, final int damamount,
      final BodyPart hitlocation, final int armorpenetration, final int mod) {
    int modDamamount = damamount;
    int armor = t.getArmor().defense(hitlocation);
    if (t.type().animal() == Animal.TANK) {
      armor = damtype.contains(Wound.BURNED) ? 15 : 10;
    }
    armor -= t.getArmor().quality().num;
    if (t.getArmor().isDamaged()) {
      armor -= 1;
    }
    armor = Math.max(armor, 0); // Possible from second-rate clothes
    final int mod2 = -Math.abs(armor + i.rng.nextInt(armor + 1) - armorpenetration);
    // Cap damage multiplier (every 5 points adds 1x damage)
    modDamamount = modifedDamageAmount(Math.min(mod2 + mod, 10), modDamamount);
    // Firefighter's bunker gear reduces fire damage by 3/4
    if (damtype.contains(Wound.BURNED) && t.getArmor().isFireprotection()) {
      // Damaged gear isn't as effective as undamaged gear
      if (t.getArmor().isDamaged()) {
        modDamamount >>= 1; // Only half as much damage reduction
      } else {
        modDamamount >>= 2; // Full damage reduction
      }
    }
    return Math.max(modDamamount, 0);
  }

  private static int defenseRoll(final Creature t) {
    int droll = t.skill().skillRoll(Skill.DODGE) / 2;
    droll -= t.health().modRoll();
    // If in a foot chase, double the debilitating effect of injuries
    if (i.mode() == GameMode.CHASEFOOT) {
      droll -= t.health().modRoll();
    }
    return Math.max(0, droll);
  }

  private static boolean describeAttack(final Creature attacker, final Creature target,
      final boolean mistake, final Attack attackUsed, final boolean melee) {
    final StringBuilder str = new StringBuilder();
    boolean rval = melee;
    str.append(attacker.toString()).append(' ');
    if (mistake) {
      str.append("MISTAKENLY ");
    }
    if (!attacker.weapon().isArmed()) {
      if (attacker.type().animal() == Animal.HUMAN) {
        addHandToHandDescription(str, attacker);
      } else if (attacker.specialAttack() == SpecialAttacks.CANNON) {
        str.append("fires a 120mm shell at");
        rval = false;
      } else if (attacker.specialAttack() == SpecialAttacks.FLAME) {
        str.append("breathes fire at");
      } else if (attacker.specialAttack() == SpecialAttacks.SUCK) {
        str.append("stabs");
      } else {
        str.append("bites");
      }
    } else {
      str.append(attackUsed.attackDescription);
    }
    str.append(' ').append(target.toString());
    if (attacker.weapon().isArmed() && !attackUsed.thrown()) {
      str.append(" with a ").append(attacker.weapon().weapon());
    }
    str.append('!');
    ui().text(str.toString()).color(attacker.alignment().color()).add();
    return rval;
  }

  private static void describeBurstHits(final Creature a, final StringBuilder str,
      final Attack attack_used, final int bursthits) {
    str.append(", ");
    if (!a.weapon().isArmed()) {
      str.append("striking");
    } else {
      str.append(attack_used.hitDescription);
    }
    switch (bursthits) {
    case 1:
      break;
    case 2:
      str.append(" twice");
      break;
    case 3:
      str.append(" three times");
      break;
    case 4:
      str.append(" four times");
      break;
    case 5:
      str.append(" five times");
      break;
    default:
      str.append(" " + bursthits + " times");
    }
  }

  private static void describeDamage(final Creature a, final Creature t, final StringBuilder str,
      final Attack attack_used, final BodyPart w, final Set<Wound> damtype, final int damamount,
      @Nullable final Wound severtype, final boolean damagearmor) {
    final Creature target = protectTheFounder(t, w, damamount);
    target.health().wounds().get(w).addAll(damtype);
    if (severtype != null && damamount >= w.severAmount()) {
      target.health().wounds().get(w).add(severtype);
    }
    if (damagearmor) {
      target.getArmor().damagePart(w);
    }
    target.health().blood(target.health().blood() - damamount);
    if (i.site.siteLevelmap() != null) {
      i.site.currentTile().flag.add(TileSpecial.BLOODY);
    }
    if (target.health().missing(BodyPart.HEAD) || target.health().missing(BodyPart.BODY)
        || target.health().blood() <= 0) {
      if (target.health().wounds().get(BodyPart.HEAD).contains(Wound.NASTYOFF)
          || target.health().wounds().get(BodyPart.BODY).contains(Wound.NASTYOFF)) {
        target.getArmor().bloodblast();
      }
      target.health().die();
      if (t.alignment().trueOrdinal() == -a.alignment().trueOrdinal()) {
        t.addJuice(5 + t.juice() / 20, 1000); // Instant juice
      } else {
        t.addJuice(-(5 + t.juice() / 20), 1000);
      }
      if (target.health().wounds().get(BodyPart.HEAD).contains(Wound.CLEANOFF)) {
        str.append(" CUTTING IT OFF!");
      } else if (target.health().wounds().get(BodyPart.BODY).contains(Wound.CLEANOFF)) {
        str.append(" CUTTING IT IN HALF!");
      } else if (target.health().wounds().get(BodyPart.HEAD).contains(Wound.NASTYOFF)) {
        str.append(" BLOWING IT APART!");
      } else if (target.health().wounds().get(BodyPart.BODY).contains(Wound.NASTYOFF)) {
        str.append(" BLOWING IT IN HALF!");
      } else {
        str.append(attack_used.hitPunctuation);
      }
      ui().text(str.toString()).color(a.alignment().color()).add();
      str.setLength(0);
      severloot(t, i.groundLoot());
      target.deathMessage();
      if (target.prisoner() != null) {
        t.freeHostage(Creature.Situation.DIED);
      }
    } else {
      if (target.health().wounds().get(w).contains(Wound.CLEANOFF)) {
        str.append(" CUTTING IT OFF!");
      } else if (target.health().wounds().get(w).contains(Wound.NASTYOFF)) {
        str.append(" BLOWING IT OFF!");
      } else {
        str.append(attack_used.hitPunctuation);
      }
      ui().text(str.toString()).color(a.alignment().color()).add();
      str.setLength(0);
      if (target.health().wounds().get(w).contains(Wound.NASTYOFF)) {
        target.getArmor().bloodblast();
      }
      // SPECIAL WOUNDS
      if (!(target.health().wounds().get(w).contains(Wound.CLEANOFF) || target.health().wounds()
          .get(w).contains(Wound.NASTYOFF))
          && target.type().animal() != Animal.HUMAN) {
        boolean heavydam = false;
        boolean breakdam = false;
        boolean pokedam = false;
        if (damamount >= 12) // JDS -- 2x damage needed
        {
          if (damtype.contains(Wound.SHOT)) {
            heavydam = true;
          }
          if (damtype.contains(Wound.BURNED)) {
            heavydam = true;
          }
          if (damtype.contains(Wound.TORN)) {
            heavydam = true;
          }
          if (damtype.contains(Wound.CUT)) {
            heavydam = true;
          }
        }
        if (damamount >= 0) // JDS -- 2x damage needed
        {
          if (damtype.contains(Wound.SHOT)) {
            pokedam = true;
          }
          if (damtype.contains(Wound.TORN)) {
            pokedam = true;
          }
          if (damtype.contains(Wound.CUT)) {
            pokedam = true;
          }
        }
        if (damtype.contains(Wound.BRUISED) && damamount >= 50) {
          breakdam = true;
        }
        if (damtype.contains(Wound.SHOT) && damamount >= 50) {
          breakdam = true;
        }
        if (damtype.contains(Wound.TORN) && damamount >= 50) {
          breakdam = true;
        }
        if (damtype.contains(Wound.CUT) && damamount >= 50) {
          breakdam = true;
        }
        if (w == BodyPart.HEAD) {
          switch (i.rng.nextInt(7)) {
          case 0:
          default:
            if ((target.health().getWound(SpecialWounds.RIGHTEYE) != 0
                || target.health().getWound(SpecialWounds.LEFTEYE) != 0 || target.health()
                .getWound(SpecialWounds.NOSE) != 0) && heavydam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s face is blasted off!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("'s face is burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s face is torn off!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("'s face is cut away!").add();
              } else {
                ui().text("'s face is removed!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.RIGHTEYE, 0);
              target.health().wound(SpecialWounds.LEFTEYE, 0);
              target.health().wound(SpecialWounds.NOSE, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          case 1:
            if (target.health().getWound(SpecialWounds.TEETH) > 0) {
              int teethminus = i.rng.nextInt(SpecialWounds.TEETH.defaultValue()) + 1;
              if (teethminus > target.health().getWound(SpecialWounds.TEETH)) {
                teethminus = target.health().getWound(SpecialWounds.TEETH);
              }
              // char num[20];
              if (teethminus > 1) {
                ui().text(teethminus + " of " + target.toString() + "'s teeth are ").add();
              } else if (target.health().getWound(SpecialWounds.TEETH) > 1) {
                ui().text("One of " + target.toString() + "'s teeth is ").add();
              } else {
                ui().text(target.toString() + "'s last tooth is ").add();
              }
              if (damtype.contains(Wound.SHOT)) {
                ui().text("shot out!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("gouged out!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("cut out!").add();
              } else {
                ui().text("knocked out!").add();
              }
              target.health().wound(SpecialWounds.TEETH,
                  target.health().getWound(SpecialWounds.TEETH) - teethminus);
            }
            break;
          case 2:
            if (target.health().getWound(SpecialWounds.RIGHTEYE) != 0 && heavydam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s right eye is blasted out!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("'s right eye is burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s right eye is torn out!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("'s right eye is poked out!").add();
              } else {
                ui().text("'s right eye is removed!").add();
              }
              target.health().wound(SpecialWounds.RIGHTEYE, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 3:
            if (target.health().getWound(SpecialWounds.LEFTEYE) != 0 && heavydam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s left eye is blasted out!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("'s left eye is burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s left eye is torn out!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("'s left eye is poked out!").add();
              } else {
                ui().text("'s left eye is removed!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.LEFTEYE, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 4:
            if (target.health().getWound(SpecialWounds.TONGUE) != 0 && heavydam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s tongue is blasted off!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("'s tongue is burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s tongue is torn out!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("'s tongue is cut off!").add();
              } else {
                ui().text("'s tongue is removed!").add();
              }
              target.health().wound(SpecialWounds.TONGUE, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 5:
            if (target.health().getWound(SpecialWounds.NOSE) != 0 && heavydam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s nose is blasted off!").add();
              } else if (damtype.contains(Wound.BURNED)) {
                ui().text("'s nose is burned away!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s nose is torn off!").add();
              } else if (damtype.contains(Wound.CUT)) {
                ui().text("'s nose is cut off!").add();
              } else {
                ui().text("'s nose is removed!").add();
              }
              target.health().wound(SpecialWounds.NOSE, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 6:
            if (target.health().getWound(SpecialWounds.NECK) != 0 && breakdam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s neck bones are shattered!").add();
              } else {
                ui().text("'s neck is broken!").add();
              }
              target.health().wound(SpecialWounds.NECK, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          }
        }
        if (w == BodyPart.BODY) {
          switch (i.rng.nextInt(11)) {
          case 0:
          default:
            if (target.health().getWound(SpecialWounds.UPPERSPINE) != 0 && breakdam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s upper spine is shattered!").add();
              } else {
                ui().text("'s upper spine is broken!").add();
              }
              target.health().wound(SpecialWounds.UPPERSPINE, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          case 1:
            if (target.health().getWound(SpecialWounds.LOWERSPINE) != 0 && breakdam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s lower spine is shattered!").add();
              } else {
                ui().text("'s lower spine is broken!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.LOWERSPINE, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          case 2:
            if (target.health().getWound(SpecialWounds.RIGHTLUNG) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s right lung is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s right lung is torn!").add();
              } else {
                ui().text("'s right lung is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.RIGHTLUNG, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          case 3:
            if (target.health().getWound(SpecialWounds.LEFTLUNG) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s left lung is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s left lung is torn!").add();
              } else {
                ui().text("'s left lung is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.LEFTLUNG, 0);
              if (target.health().blood() > 20) {
                target.health().blood(20);
              }
            }
            break;
          case 4:
            if (target.health().getWound(SpecialWounds.HEART) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s heart is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s heart is torn!").add();
              } else {
                ui().text("'s heart is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.HEART, 0);
              if (target.health().blood() > 3) {
                target.health().blood(3);
              }
            }
            break;
          case 5:
            if (target.health().getWound(SpecialWounds.LIVER) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s liver is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s liver is torn!").add();
              } else {
                ui().text("'s liver is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.LIVER, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 6:
            if (target.health().getWound(SpecialWounds.STOMACH) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s stomach is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s stomach is torn!").add();
              } else {
                ui().text("'s stomach is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.STOMACH, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 7:
            if (target.health().getWound(SpecialWounds.RIGHTKIDNEY) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s right kidney is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s right kidney is torn!").add();
              } else {
                ui().text("'s right kidney is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.RIGHTKIDNEY, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 8:
            if (target.health().getWound(SpecialWounds.LEFTKIDNEY) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s left kidney is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s left kidney is torn!").add();
              } else {
                ui().text("'s left kidney is punctured!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.LEFTKIDNEY, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 9:
            if (target.health().getWound(SpecialWounds.SPLEEN) != 0 && pokedam) {
              ui().text(target.toString()).add();
              if (damtype.contains(Wound.SHOT)) {
                ui().text("'s spleen is blasted!").add();
              } else if (damtype.contains(Wound.TORN)) {
                ui().text("'s spleen is torn!").add();
              } else {
                ui().text("'s spleen is punctured!").add();
              }
              target.health().wound(SpecialWounds.SPLEEN, 0);
              if (target.health().blood() > 50) {
                target.health().blood(50);
              }
            }
            break;
          case 10:
            if (target.health().getWound(SpecialWounds.RIBS) > 0 && breakdam) {
              int ribminus = i.rng.nextInt(SpecialWounds.RIBS.defaultValue()) + 1;
              if (ribminus > target.health().getWound(SpecialWounds.RIBS)) {
                ribminus = target.health().getWound(SpecialWounds.RIBS);
              }
              if (ribminus > 1) {
                ui().text(String.valueOf(ribminus)).add();
                ui().text(" of ").add();
                ui().text(target.toString()).add();
                ui().text("'s ribs are ").add();
              } else if (target.health().getWound(SpecialWounds.RIBS) > 1) {
                ui().text("One of ").add();
                ui().text(target.toString()).add();
                ui().text("'s rib is ").add();
              } else {
                ui().text(target.toString()).add();
                ui().text("'s last unbroken rib is ").add();
              }
              if (damtype.contains(Wound.SHOT)) {
                ui().text("shot apart!").add();
              } else {
                ui().text("broken!").add();
              }
              // getch();
              target.health().wound(SpecialWounds.RIBS,
                  target.health().getWound(SpecialWounds.RIBS) - ribminus);
            }
            break;
          }
        }
        severloot(target, i.groundLoot());
      }
    }
  }

  private static BodyPart determineTarget(final int aroll, final int droll) {
    int offset = 0;
    if (aroll > droll + 5 || i.mode() == GameMode.CHASECAR) {
      offset = 4; // NICE SHOT; MORE LIKELY TO HIT BODY/HEAD or
    }
    // it's a car chase and we don't want to hit the
    // car too much
    if (aroll > droll + 10) {
      offset = 8; // NO LIMB HITS HERE YOU AWESOME PERSON
    }
    if (aroll > droll + 15) {
      offset = 12; // BOOM AUTOMATIC HEADSHOT MOTHA******
    }
    // Weighted i.location roll:
    // 200% chance to hit body
    // 50% chance to hit head
    int v = offset + i.rng.nextInt(13 - offset);
    v = Math.min(Math.max(0, v), TARGET.length - 1);
    return TARGET[v];
  }

  private static boolean doesFleeForInjury(final Creature e) {
    return e.health().blood() < 45;
  }

  private static boolean doesFleeFromFire(final Creature e) {
    final int fire = siteBlockFire();
    return fire * i.rng.nextInt(5) >= 3 && !e.type().ofType("FIREFIGHTER");
  }

  private static void enemyattack(final Creature encounter) {
    if (!encounter.health().alive()) {
      return;
    }
    if (i.site.alarm() && encounter.type().ofType("BOUNCER")
        && encounter.alignment() != Alignment.LIBERAL) {
      encounter.conservatise();
    }
    if (encounter.enemy()) {
      encounter.receptive(Receptive.ANGERED);
    }
    if (i.mode() != GameMode.CHASECAR && encounter.type().animal() == Animal.HUMAN) {
      if (isNotEnemyOrConverted(encounter) || isUnarmedOrDemoralised(encounter)
          || doesFleeForInjury(encounter) || doesFleeFromFire(encounter)) {
        if (!incapacitated(encounter, false, R.id.gmessages).first) {
          final StringBuilder str = new StringBuilder();
          str.append(encounter.toString());
          if (encounter.health().legCount() < 2 || doesFleeForInjury(encounter)) {
            str.append(i.rng.choice(" crawls off moaning...", " crawls off whimpering...",
                " crawls off trailing blood...", " crawls off screaming...",
                " crawls off crying...", " crawls off sobbing...", " crawls off whispering...",
                " crawls off praying...", " crawls off cursing..."));
          } else {
            str.append(i.rng.choice(" makes a break for it!", " escapes crying!", " runs away!",
                " gets out of there!", " runs hollering!", " bolts out of there!",
                " runs away screaming!"));
          }
          ui().text(str.toString()).add();
          if (i.mode() == GameMode.SITE) {
            encounter.dropLoot(i.groundLoot());
          }
          i.currentEncounter().creatures().remove(encounter);
        }
        return;
      }
    }
    final List<Creature> goodtarg = new ArrayList<Creature>();
    final List<Creature> badtarg = new ArrayList<Creature>();
    if (encounter.enemy()) {
      for (final Creature p : i.activeSquad()) {
        if (p.health().alive()) {
          goodtarg.add(p);
        }
      }
    } else {
      for (final Creature e2 : i.currentEncounter().creatures()) {
        if (!e2.health().alive()) {
          continue;
        }
        if (e2.alignment() == Alignment.CONSERVATIVE) {
          goodtarg.add(e2);
        }
      }
    }
    for (final Creature e2 : i.currentEncounter().creatures()) {
      if (!e2.health().alive()) {
        continue;
      }
      if (!e2.enemy()) {
        badtarg.add(e2);
      }
    }
    if (goodtarg.isEmpty()) {
      return;
    }
    Creature target = i.rng.randFromList(goodtarg);
    final boolean canmistake = (encounter.type().ofType("SCIENTIST_EMINENT")
        || encounter.type().ofType("JUDGE_LIBERAL")
        || encounter.type().ofType("JUDGE_CONSERVATIVE")
        || encounter.type().ofType("CORPORATE_CEO") || encounter.type().ofType("RADIOPERSONALITY") || encounter
        .type().idName().equals("NEWSANCHOR"));
    if (canmistake) {
      if (encounter.enemy()) {
        if (target.prisoner() != null && i.rng.chance(2)) {
          attack(encounter, target.prisoner(), true);
          if (!target.prisoner().health().alive() && target.prisoner().squad() == null) {
            ui().text(target + " drops " + target.prisoner() + "'s body.").add();
            if (target.prisoner().alignment() == Alignment.CONSERVATIVE) {
              i.site.crime(i.site.crime() + 30);
            }
            target.prisoner(null);
          }
          return;
        }
      }
      if (!badtarg.isEmpty() && i.rng.chance(10)) {
        target = i.rng.randFromList(badtarg);
        attack(encounter, target, target.hasFlag(CreatureFlag.CONVERTED));
        if (!target.health().alive()) {
          i.currentEncounter().creatures().remove(encounter);
        }
        return;
      }
    }
    attack(encounter, target, false);
  }

  private static int hitCount(final Creature a, final int aroll, final int droll, final int bonus,
      final Attack attack_used) {
    int bursthits = 0; // Tracks number of hits
    if (!a.weapon().isArmed()) {
      bursthits = martialArtistry(a);
    } else {
      attack_used.fire.consider(a);
      for (int j = 0; j < attack_used.numberAttacks; ++j) {
        if (attack_used.usesAmmo()) {
          if (a.weapon().weapon().get_ammoamount() > 0) {
            a.weapon().weapon().decreaseAmmo(1);
          } else {
            break;
          }
        } else if (attack_used.thrown()) {
          if (a.weapon().hasThrownWeapon()) {
            a.weapon().readyAnotherThrowingWeapon();
          } else {
            break;
          }
          a.weapon().dropWeapon(null);
        }
        // Each shot in a burst is increasingly less likely to hit
        if (aroll + bonus - j * attack_used.successiveAttacksDifficulty > droll) {
          bursthits++;
        }
      }
    }
    return bursthits;
  }

  /** checks if the creature can fight and prints flavor text if they can't.
   * @param attacker
   * @param noncombat
   * @param viewId
   * @return a pair - first is whether they are incapacitated, second is whether there was some
   *         drawing on the screen. */
  private static Pair<Boolean, Boolean> incapacitated(final Creature attacker,
      final boolean noncombat, final int viewId) {
    boolean printed = false;
    if (attacker.type().animal() == Animal.TANK) {
      if (attacker.health().blood() <= 20 || attacker.health().blood() <= 50
          && (i.rng.likely(2) || attacker.isIncapacitated())) {
        attacker.forceIncapacitated(false);
        if (noncombat) {
          setViewIfNeeded(R.layout.generic);
          ui(viewId)
              .text("The " + attacker + i.rng.choice(" smokes...", " smolders.", " burns..."))
              .add();
          printed = true;
        }
        return Pair.create(true, printed);
      }
      return Pair.create(false, printed);
    }
    if (attacker.type().animal() == Animal.ANIMAL) {
      if (attacker.health().blood() <= 20 || attacker.health().blood() <= 50
          && (i.rng.chance(2) || attacker.isIncapacitated())) {
        attacker.forceIncapacitated(false);
        if (noncombat) {
          setViewIfNeeded(R.layout.generic);
          ui(viewId).text("The " + attacker.toString()).add();
          switch (i.rng.nextInt(3)) {
          case 0:
          default:
            ui(viewId).text(" yelps in pain...").add();
            break;
          case 1:
            if (!i.freeSpeech()) {
              ui(viewId).text(" [makes a stinky].").add();
            } else {
              ui(viewId).text(" soils the floor.").add();
            }
            break;
          case 2:
            ui(viewId).text(" yowls pitifully...").add();
            break;
          }
          printed = true;
        }
        return Pair.create(true, printed);
      }
      return Pair.create(false, printed);
    }
    if (attacker.health().blood() <= 20 || attacker.health().blood() <= 50
        && (i.rng.chance(2) || attacker.isIncapacitated())) {
      attacker.forceIncapacitated(false);
      if (noncombat) {
        setViewIfNeeded(R.layout.generic);
        ui(viewId)
            .text(
                attacker.toString()
                    + i.rng.choice(
                        " desperately cries out to Jesus.",
                        !i.freeSpeech() ? " [makes a stinky]." : " soils the floor.",
                        " whimpers in a corner.",
                        " begins to weep.",
                        " vomits.",
                        " chortles...",
                        " screams in pain.",
                        " asks for mother.",
                        " prays softly...",
                        " clutches at the wounds.",
                        " reaches out and moans.",
                        " hollers in pain.",
                        " groans in agony.",
                        " begins hyperventilating.",
                        " shouts a prayer.",
                        " coughs up blood.",
                        i.mode() != GameMode.CHASECAR ? " stumbles against a wall."
                            : " leans against the door.",
                        " begs for forgiveness.",
                        " shouts \"Why have you forsaken me?\"",
                        " murmurs \"Why Lord?   Why?\"",
                        " whispers \"Am I dead?\"",
                        !i.freeSpeech() ? " [makes a mess], moaning."
                            : " pisses on the floor, moaning.",
                        " whispers incoherently.",
                        attacker.health().getWound(SpecialWounds.RIGHTEYE) == 0
                            || attacker.health().getWound(SpecialWounds.LEFTEYE) == 0 ? " stares off into space."
                            : " stares out with hollow sockets.",
                        " cries softly.",
                        " yells until the scream cracks dry.",
                        attacker.health().getWound(SpecialWounds.TEETH) > 1 ? "'s teeth start chattering."
                            : attacker.health().getWound(SpecialWounds.TEETH) == 1 ? "'s tooth starts chattering."
                                : "'s gums start chattering.",
                        " starts shaking uncontrollably.",
                        " looks strangely calm.",
                        " nods off for a moment.",
                        " starts drooling.",
                        " seems lost in memories.",
                        " shakes with fear.",
                        " murmurs \"I'm so afraid...\"",
                        " cries \"It can't be like this...\"",
                        attacker.type().ofType("TEENAGER")
                            || attacker.type().ofType("WORKER_FACTORY_CHILD") ? " cries \"Mommy!\""
                            : " murmurs \"What about my children?\"", " shudders quietly.",
                        " yowls pitifully.", " begins losing faith in God.",
                        " muses quietly about death.", " asks for a blanket.", " shivers softly.",
                        !i.freeSpeech() ? " [makes a mess]." : " vomits up a clot of blood.", !i
                            .freeSpeech() ? " [makes a mess]."
                            : " spits up a cluster of bloody bubbles.", " pleads for mercy.",
                        " quietly asks for coffee.", " looks resigned.", " scratches at the air.",
                        " starts to giggle uncontrollably.", " wears a look of pain.",
                        " questions God.", " whispers \"Mama baby.  Baby loves mama.\"",
                        " asks for childhood toys frantically.",
                        " murmurs \"But I go to church...\"")).add();
        printed = true;
      }
      return Pair.create(true, printed);
    } else if (attacker.stunned() != 0) {
      if (noncombat) {
        attacker.stunned(attacker.stunned() - 1);
        setViewIfNeeded(R.layout.generic);
        ui(viewId).text(
            attacker.toString()
                + i.rng.choice(" seems hesitant.", " is caught in self-doubt.",
                    " looks around uneasily.", " begins to weep.", " asks \"Is this right?\"",
                    " asks for guidance.", " is caught in indecision.", " feels numb.",
                    " prays softly.", " searches for the truth.", " tears up.")).add();
        printed = true;
      }
      return Pair.create(true, printed);
    }
    if (attacker.health().getWound(SpecialWounds.NECK) == 2
        || attacker.health().getWound(SpecialWounds.UPPERSPINE) == 2) {
      if (!noncombat) {
        setViewIfNeeded(R.layout.generic);
        ui(viewId).text(
            attacker.toString()
                + i.rng.choice(" looks on with authority.", " waits patiently.",
                    " sits in thought.", " breathes slowly.", " considers the situation.")).add();
        printed = true;
      }
      return Pair.create(true, printed);
    }
    return Pair.create(false, printed);
  }

  private static boolean isNotEnemyOrConverted(final Creature e) {
    return !e.enemy() && !e.hasFlag(CreatureFlag.CONVERTED);
  }

  private static boolean isUnarmedOrDemoralised(final Creature e) {
    return e.juice() == 0 && !e.weapon().isArmed() && squadIsArmed()
        && e.health().blood() < 70 + i.rng.nextInt(61) && !e.hasFlag(CreatureFlag.CONVERTED);
  }

  private static int martialArtistry(final Creature artist) {
    if (artist.type().animal() != Animal.HUMAN) {
      return 1; // Whoops, must be human to use martial arts fanciness
    }
    // Martial arts multi-strikes
    return Math.min(1 + i.rng.nextInt(artist.skill().skill(Skill.HANDTOHAND) / 3 + 1), 5);
  }

  private static int modifedDamageAmount(final int mod, final int damamount) {
    if (mod <= -8) {
      return damamount >> 6;
    } else if (mod <= -6) {
      return damamount >> 5;
    } else if (mod <= -4) {
      return damamount >> 4;
    } else if (mod <= -3) {
      return damamount >> 3;
    } else if (mod <= -2) {
      return damamount >> 2;
    } else if (mod <= -1) {
      return damamount >> 1;
    } else if (mod >= 0) {
      return (int) (damamount * (1.0f + 0.2f * mod));
    }
    return damamount;
  }

  private static Creature protectTheFounder(final Creature target, final BodyPart bp,
      final int damAmount) {
    /* if the founder is hit and lethal or potentially crippling damage is done... */
    if (target.squad() != null && target.isFounder()
        && (damAmount > target.health().blood() || damAmount >= 10)
        && (bp == BodyPart.HEAD || bp == BodyPart.BODY)) {
      /* Oh Noes!!!! Find a liberal to jump in front of the bullet!!! */
      for (final Creature liberal : i.activeSquad()) {
        if (liberal == target) {
          continue;
        }
        if (liberal.skill().getAttribute(Attribute.HEART, true) > 8
            && liberal.skill().getAttribute(Attribute.AGILITY, true) > 4) {
          if (target.health().alive()) {
            ui().text(liberal + " heroically shields " + target + "!").color(Color.GREEN).add();
          } else {
            ui().text(liberal + " misguidedly shields " + target + "'s corpse!").color(Color.GREEN)
                .add();
          }
          /* Instant juice!! Way to take the bullet!! */
          target.addJuice(10, 1000);
          return liberal;
        }
      }
    }
    return target;
  }

  @Nullable private static BodyPart randomBodyPart(final Creature target, final int aroll,
      final int droll) {
    BodyPart bp = null;
    boolean canhit = false;
    for (final BodyPart x : BodyPart.values()) {
      if (!target.health().missing(x)) {
        canhit = true;
        break;
      }
    }
    do {
      bp = determineTarget(aroll, droll);
    } while (target.health().missing(bp) && canhit);
    return bp;
  }

  /* destroys armor, masks, drops weapons based on severe damage */
  private static void severloot(final Creature cr,
      final List<AbstractItem<? extends AbstractItemType>> loot) {
    int armok = cr.health().armCount();
    if (cr.health().getWound(SpecialWounds.NECK) != 1) {
      armok = 0;
    }
    if (cr.health().getWound(SpecialWounds.UPPERSPINE) != 1) {
      armok = 0;
    }
    if (cr.weapon().isArmed() && armok == 0) {
      ui().text(
          "The " + cr.weapon().weapon().toString() + " slips from" + cr.toString() + "'s grasp.")
          .color(Color.YELLOW).add();
      // getch();
      if (i.mode() == GameMode.SITE) {
        cr.weapon().dropWeaponsAndClips(loot);
      } else {
        cr.weapon().dropWeaponsAndClips(null);
      }
    }
    if (cr.health().missing(BodyPart.BODY) && !cr.isNaked() && cr.getArmor().covers(BodyPart.BODY)) {
      ui().text(cr.toString() + "'s " + cr.getArmor().toString() + " has been destroyed.")
          .color(Color.YELLOW).add();
      cr.strip(null);
    }
    if (cr.health().wounds().get(BodyPart.HEAD).contains(Wound.NASTYOFF) && cr.getArmor().isMask()) {
      ui().text(cr.toString() + "'s " + cr.getArmor().toString() + " has been destroyed.")
          .color(Color.YELLOW).add();
      cr.strip(null);
    }
  }

  private static int siteBlockFire() {
    int fire = 0;
    if (i.mode() == GameMode.SITE) {
      final Set<TileSpecial> flag = i.site.currentTile().flag;
      if (flag.contains(TileSpecial.FIRE_START) || flag.contains(TileSpecial.FIRE_END)) {
        fire = 1;
      } else if (flag.contains(TileSpecial.FIRE_PEAK)) {
        fire = 2;
      }
    }
    return fire;
  }

  private static boolean specialAttack(final Creature attacker, final Creature target) {
    int resist = 0;
    int attack = 0;
    final StringBuilder str = new StringBuilder();
    str.append(attacker.toString());
    str.append(' ');
    if (attacker.alignment() != Alignment.LIBERAL) {
      attack = attacker.skill().attributeRoll(Attribute.WISDOM)
          + target.skill().getAttribute(Attribute.WISDOM, false);
    } else {
      attack = attacker.skill().attributeRoll(Attribute.HEART)
          + target.skill().getAttribute(Attribute.HEART, false);
    }
    if (attacker.type().ofType("COP")) {
      str.append(i.rng.choice("reasons with", "promises a fair trial to", "offers a kind ear to",
          "urges cooperation from", "offers a hug to", "suggests counseling to",
          "gives a teddy bear to"));
      resist = target.skill().attributeRoll(Attribute.HEART);
      attack += attacker.skill().skillRoll(Skill.PERSUASION);
    } else if (attacker.type().ofType("JUDGE_CONSERVATIVE")
        || attacker.type().ofType("JUDGE_LIBERAL")) {
      str.append(i.rng.choice("debates the death penalty with", "debates gay rights with",
          "debates free speech with", "debates the Second Amendment with"));
      if (target.alignment() == Alignment.LIBERAL) {
        resist = target.skill().skillRoll(Skill.LAW)
            + target.skill().attributeRoll(Attribute.HEART);
      } else {
        resist = target.skill().skillRoll(Skill.LAW)
            + target.skill().attributeRoll(Attribute.WISDOM);
      }
      attack += attacker.skill().skillRoll(Skill.LAW);
    } else if (attacker.type().ofType("SCIENTIST_EMINENT")) {
      str.append(i.rng.choice("debates scientific ethics with",
          attacker.alignment() == Alignment.CONSERVATIVE ? "explains the benefits of research to"
              : "explains ethical research to", "discusses the scientific method with"));
      if (target.alignment() == Alignment.LIBERAL) {
        resist = target.skill().skillRoll(Skill.SCIENCE)
            + target.skill().attributeRoll(Attribute.HEART);
      } else {
        resist = target.skill().skillRoll(Skill.SCIENCE)
            + target.skill().attributeRoll(Attribute.WISDOM);
      }
      attack += attacker.skill().skillRoll(Skill.SCIENCE);
    } else if (attacker.type().ofType("CORPORATE_CEO")) {
      if (attacker.alignment() == Alignment.CONSERVATIVE) {
        str.append(i.rng.choice("explains the derivatives market to",
            "justifies voodoo economics to", "extols the Reagan presidency to",
            "argues about tax cuts with", "explains Conservative philosophy to",
            "extends a dinner invitation to", "offers a VP position to", "shows a $1000 bill to",
            "debates fiscal policy with", "offers stock options to"));
      } else {
        str.append(i.rng.choice("debates fiscal policy with", "derides voodoo economics to",
            "dismisses the Reagan presidency to", "argues about tax cuts with",
            "explains Liberal philosophy to"));
      }
      if (target.alignment() == Alignment.LIBERAL) {
        resist = target.skill().skillRoll(Skill.BUSINESS)
            + target.skill().attributeRoll(Attribute.HEART);
      } else {
        resist = target.skill().skillRoll(Skill.BUSINESS)
            + target.skill().attributeRoll(Attribute.WISDOM);
      }
      attack += attacker.skill().skillRoll(Skill.BUSINESS);
    } else if (attacker.type().ofType("RADIOPERSONALITY") || attacker.type().ofType("NEWSANCHOR")) {
      str.append(i.rng.choice("winks at", "smiles at", "smirks at", "chats warmly with",
          "yells slogans at"));
      if (target.alignment() == Alignment.LIBERAL) {
        resist = target.skill().attributeRoll(Attribute.HEART);
      } else {
        resist = target.skill().attributeRoll(Attribute.WISDOM);
      }
      attack += attacker.skill().attributeRoll(Attribute.CHARISMA);
    } else if (attacker.weapon().weapon().isMusical()) {
      str.append(i.rng.choice("plays a song for", "sings to", "strums the "
          + attacker.weapon().weapon().toString() + " at",
          attacker.alignment() == Alignment.LIBERAL ? "plays protest songs at"
              : "plays country songs at", "rocks out at"));
      attack = attacker.skill().skillRoll(Skill.MUSIC);
      if (target.alignment() == Alignment.LIBERAL) {
        resist = target.skill().attributeRoll(Attribute.HEART);
      } else {
        resist = target.skill().attributeRoll(Attribute.WISDOM);
      }
      if (resist > 0) {
        attacker.skill().train(Skill.MUSIC, i.rng.nextInt(resist) + 1);
      } else {
        attacker.skill().train(Skill.MUSIC, 1);
      }
    }
    str.append(' ');
    str.append(target.toString());
    str.append('!');
    ui().text(str.toString()).add();
    if (target.type().animal() == Animal.TANK || target.type().animal() == Animal.ANIMAL
        && i.issue(Issue.ANIMALRESEARCH).lawLT(Alignment.ELITELIBERAL)) {
      ui().text(target.toString() + " is immune to the attack!").add();
    } else if (attacker.enemy() && target.hasFlag(CreatureFlag.BRAINWASHED)) {
      ui().text(target + " is immune to the attack!").add();
    } else if (attack > resist) {
      target.stunned(target.stunned() + (attack - resist) / 4);
      if (attacker.enemy()) {
        if (target.juice() >= 100) {
          ui().text(target.toString() + " loses juice!").add();
          target.addJuice(-50, 100);
        } else if (i.rng.nextInt(15) > target.skill().getAttribute(Attribute.WISDOM, true)
            || target.skill().getAttribute(Attribute.WISDOM, true) < target.skill().getAttribute(
                Attribute.HEART, true)) {
          ui().text(target.toString() + " becomes Wiser!").add();
          target.skill().attribute(Attribute.WISDOM, +1);
        } else if (target.alignment() == Alignment.LIBERAL
            && target.hasFlag(CreatureFlag.LOVE_SLAVE)) {
          ui().text(target.toString() + " can't bear to leave!").add();
        } else {
          if (attacker.alignment() == Alignment.CONSERVATIVE) {
            attacker.alignment(Alignment.CONSERVATIVE);
            ui().text(target.toString() + " is turned Conservative!").add();
            target.stunned(0);
            if (target.prisoner() != null) {
              target.freeHostage(Creature.Situation.TURNED);
            }
            ui().text("!").add();
          } else {
            ui().text(target.toString() + " doesn't want to fight anymore!").add();
            target.stunned(0);
            if (target.prisoner() != null) {
              target.freeHostage(Creature.Situation.TURNED);
            }
          }
          for (final Creature e : i.currentEncounter().creatures()) {
            if (attacker.alignment() == Alignment.CONSERVATIVE) {
              e.conservatise();
            }
            e.receptive(Receptive.ANGERED);
            e.squad(null);
            break;
          }
          target.health().die();
          i.activeSquad().remove(target);
        }
      } else if (target.juice() >= 100) {
        ui().text(target.toString()).add();
        ui().text(" seems less badass!").add();
        target.addJuice(-50, 100);
      } else if (!target.skill().attributeCheck(Attribute.HEART, CheckDifficulty.AVERAGE.value())
          || target.skill().getAttribute(Attribute.HEART, true) < target.skill().getAttribute(
              Attribute.WISDOM, true)) {
        ui().text(target.toString() + "'s Heart swells!").add();
        target.skill().attribute(Attribute.HEART, +1);
      } else {
        ui().text(target.toString() + " has turned Liberal!").add();
        target.stunned(0).liberalize(false).receptive(Receptive.LISTENING)
            .addFlag(CreatureFlag.CONVERTED);
      }
    } else {
      ui().text(target.toString()).add();
      ui().text(" remains strong.").add();
    }
    i.currentEncounter().printEncounter();
    return true;
  }

  private static boolean squadIsArmed() {
    for (final Creature j : i.activeSquad()) {
      if (j.weapon().isArmed()) {
        return true;
      }
    }
    return false;
  }

  /** attack handling for each side as a whole */
  private static void youattack(final Creature p) {
    if (!p.health().alive()) {
      return;
    }
    final boolean wasalarm = i.site.alarm();
    i.site.alarm(true);
    final List<Creature> dangerousEnemies = new ArrayList<Creature>();
    final List<Creature> enemies = new ArrayList<Creature>();
    final List<Creature> nonEnemies = new ArrayList<Creature>();
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.health().alive()) {
        if (e.enemy()) {
          if (e.weapon().isArmed() && e.health().blood() >= 40) {
            dangerousEnemies.add(e);
          } else {
            enemies.add(e);
          }
        } else {
          nonEnemies.add(e);
        }
      }
    }
    if (dangerousEnemies.isEmpty() && enemies.isEmpty()) {
      return;
    }
    Creature target;
    /* Roll 1dX-1, where X is the number of "dangerous enemies", plus one if there are
     * "other enemies" */
    final int targetnum = i.rng.nextInt(dangerousEnemies.size() + (enemies.isEmpty() ? 0 : 1));
    /* If the result is less than the number of "dangerous enemies", the result indicates which of
     * these to shoot at */
    if (targetnum != dangerousEnemies.size()) {
      target = dangerousEnemies.get(targetnum);
      /* Else, roll again on the list of "other enemies" to pick one of them to shoot at */
    } else {
      target = i.rng.randFromList(enemies);
    }
    boolean mistake = false;
    /* Less likely to accidentally hit bystanders, and never hit the wrong person if not using a
     * ranged weapon */
    if (!nonEnemies.isEmpty() && i.rng.chance(60)
        && p.weapon().willDoRangedAttack(i.mode() == GameMode.CHASECAR, false)) {
      target = i.rng.randFromList(nonEnemies);
      mistake = true;
    }
    final int beforeblood = target.health().blood();
    mistake |= target.alignment() == Alignment.LIBERAL;
    if (attack(p, target, mistake)) {
      if (mistake) {
        i.site.alienationCheck(mistake);
        i.siteStory.addNews(NewsEvent.ATTACKED_MISTAKE);
        i.site.crime(i.site.crime() + 10);
      } else {
        i.site.crime(i.site.crime() + 3);
        p.addJuice(1, 200);
      }
      i.siteStory.addNews(NewsEvent.ATTACKED);
      // Charge with assault if (a) first strike, or (b) hit enemy
      if (!wasalarm || beforeblood > target.health().blood()) {
        p.crime().criminalize(p.weapon().isArmed() ? Crime.ARMEDASSAULT : Crime.ASSAULT);
      }
    }
    if (!target.health().alive()) {
      i.currentEncounter().creatures().remove(target);
      if (!mistake) {
        for (final Creature q : i.activeSquad()) {
          if (q.health().alive()) {
            q.addJuice(5, 500);
          }
        }
      }
    }
  }
}
