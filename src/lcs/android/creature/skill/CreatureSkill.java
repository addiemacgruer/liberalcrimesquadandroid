package lcs.android.creature.skill;

import static lcs.android.creature.Attribute.*;
import static lcs.android.creature.health.SpecialWounds.*;

import java.io.Serializable;
import java.util.Map;

import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Quality;
import lcs.android.game.SkillRoll;
import lcs.android.politics.Alignment;
import lcs.android.util.SparseMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class CreatureSkill implements Serializable { // NO_UCD
  public CreatureSkill(final Creature creature) {
    c = creature;
  }

  private final Map<Attribute, Integer> attributes = SparseMap.of(Attribute.class);

  private final Creature c;

  private final Map<Skill, Integer> skillExperience = SparseMap.of(Skill.class);

  private final Map<Skill, Integer> skills = SparseMap.of(Skill.class);

  public CreatureSkill attribute(final Attribute at, final int i) {
    attributes.put(at, attributes.get(at) + i);
    return this;
  }

  public boolean attributeCheck(final Attribute attribute, final int difficulty) {
    if (attributeRoll(attribute) >= difficulty)
      return true;
    return false;
  }

  public int attributeRoll(final Attribute a) {
    return SkillRoll.roll(getAttribute(a, true));
  }

  public int getAttribute(final Attribute attribute, final boolean usejuice) {
    int ret = attributes.get(attribute).intValue();
    switch (attribute) {
    case STRENGTH:
      if (c.age() < 11) {
        ret >>= 1; // Strength is lowest at the beginning and end of
      } else if (c.age() < 16) {
        ret -= 1;
      } else if (c.age() > 70) {
        ret -= 6;
      } else if (c.age() > 52) {
        ret -= 3;
      } else if (c.age() > 35) {
        ret -= 1;
      }
      break;
    case AGILITY:
      if (c.age() > 70) {
        ret -= 6; // Agility is weakened with c.age
      } else if (c.age() > 52) {
        ret -= 3;
      } else if (c.age() > 35) {
        ret -= 1;
      }
      break;
    case HEALTH:
      if (c.age() < 11) {
        ret -= 2;
      } else if (c.age() < 16) {
        ret -= 1; // Physical immaturity weakens health
      }
      /* Aging actually damc.ages base health and eventually kills, so no aging effects here */
      break;
    case CHARISMA:
      if (c.age() < 11) {
        ret += 2; // Lots of folks like kids
      } else if (c.age() < 16) {
        ret -= 1; // Teenc.agers have communication difficulties and
      } else if (c.age() > 70) {
        ret += 3; // Authority and experience in life then enhance
      } else if (c.age() > 52) {
        ret += 2;
      } else if (c.age() > 35) {
        ret += 1;
      }
      break;
    case INTELLIGENCE:
      if (c.age() < 11) {
        ret -= 3; // Experience enhances Intelligence with c.age
      } else if (c.age() < 16) {
        ret -= 1;
      } else if (c.age() > 70) {
        ret += 3;
      } else if (c.age() > 52) {
        ret += 2;
      } else if (c.age() > 35) {
        ret += 1;
      }
      break;
    case WISDOM:
      if (c.age() < 11) {
        ret -= 2; // Experience grants Wisdom with c.age
      } else if (c.age() < 16) {
        ret -= 1;
      } else if (c.age() > 70) {
        ret += 2;
      } else if (c.age() > 52) {
        ret += 1;
      }
      break;
    case HEART:
      if (c.age() < 11) {
        ret += 2; // Experience saps Heart with c.age due to
      } else if (c.age() < 16) {
        ret += 1; // No wonder it's typically the young who are most
      } else if (c.age() > 70) {
        ret -= 2;
      } else if (c.age() > 52) {
        ret -= 1;
      }
      break;
    default:
    }
    // Physical stats want to know: Are you paralyzed?
    if (attribute == STRENGTH || attribute == AGILITY || attribute == HEALTH) {
      if (c.health().getWound(NECK) != 1 || c.health().getWound(UPPERSPINE) != 1) {
        ret = 1;
      } else if (c.health().getWound(LOWERSPINE) != 1) {
        ret >>= 2;
      }
    }
    // Agility wants to know: Do you have legs?
    if (attribute == AGILITY) {
      final int legok = c.health().legCount();
      if (legok == 0) {
        ret >>= 2;
      } else if (legok == 1) {
        ret >>= 1;
      }
    }
    // Charisma wants to know: How fucked up does your face look?
    if (attribute == CHARISMA) {
      ret -= c.health().disfigurements();
    }
    /* Effects of c.juice on the character's attributes. Never use c.juice to increase stats for the
     * opposite ideology! */
    if (usejuice && !(attribute == WISDOM && c.alignment() != Alignment.CONSERVATIVE)
        && !(attribute == Attribute.HEART && c.alignment() != Alignment.LIBERAL)) {
      if (c.juice() <= -50) {
        ret = 1; // Damn worthless
      } else if (c.juice() <= -10) {
        ret *= 0.6; // Society's dregs
      } else if (c.juice() < 0) {
        ret *= 0.8; // Punk
      } else if (c.juice() >= 10) {
        if (c.juice() < 50) {
          ret += 1; // Activist
        } else if (c.juice() < 100) {
          ret = (int) (ret * 1.1 + 2); // Socialist Threat
        } else if (c.juice() < 200) {
          ret = (int) (ret * 1.2 + 3); // Revolutionary
        } else if (c.juice() < 500) {
          ret = (int) (ret * 1.3 + 4); // Urban Guerrilla
        } else if (c.juice() < 1000) {
          ret = (int) (ret * 1.4 + 5); // Liberal Guardian
        } else {
          ret = (int) (ret * 1.5 + 6); // Elite Liberal
        }
      }
    }
    /* Debilitations for temporary injuries in attributes based on physical appearance or
     * performance, because people who are bleeding all over are less strong, agile, and charismatic */
    if (attribute == STRENGTH || attribute == AGILITY || attribute == CHARISMA) {
      if (c.health().blood() <= 20) {
        ret >>= 2;
      } else if (c.health().blood() <= 50) {
        ret >>= 1;
      } else if (c.health().blood() <= 75) {
        ret *= 3;
        ret >>= 2;
      }
    }
    // Bounds check attributes
    return Math.max(ret, 1);
  }

  public void setSkill(final Skill skill, final int i) {
    skills.put(skill, i);
  }

  public int skill(final Skill skill) {
    return skills.get(skill).intValue();
  }

  public CreatureSkill skill(final Skill skill, final int mod) {
    skills.put(skill, skills.get(skill) + mod);
    return this;
  }

  public int skillCap(final Skill skill, final boolean useJuice) {
    return getAttribute(skill.getAttribute(), useJuice);
  }

  public boolean skillCheck(final Skill skill, final CheckDifficulty difficulty) {
    return skillCheck(skill, difficulty.value());
  }

  public boolean skillCheck(final Skill skill, final int difficulty) {
    Log.i(Game.LCS, "Creature.skillCheck:" + skill + "/" + difficulty);
    if (skillRoll(skill) >= difficulty) {
      Log.i(Game.LCS, "Passed!");
      return true;
    }
    Log.i(Game.LCS, "Failed!");
    return false;
  }

  public int skillRoll(final Skill skill) {
    // Take skill strength
    final int skill_value = skills.get(skill).intValue();
    // plus the skill's associate attribute
    final int attribute_value = getAttribute(skill.getAttribute(), true);
    int adjusted_attribute_value = 0;
    switch (skill) {
    case SECURITY:
      adjusted_attribute_value = skill_value;
      break;
    default:
      adjusted_attribute_value = Math.min(attribute_value / 2, skill_value + 3);
    }
    /* add the adjusted attribute and skill to get the adjusted skill total that will be rolled on */
    int returnValue = SkillRoll.roll(skill_value + adjusted_attribute_value);
    // Special skill handling
    switch (skill) {
    // Skills that cannot be used if zero skill:
    case PSYCHOLOGY:
    case LAW:
    case SECURITY:
    case COMPUTERS:
    case MUSIC:
    case ART:
    case RELIGION:
    case SCIENCE:
    case BUSINESS:
    case TEACHING:
    case FIRSTAID:
      if (skills.get(skill).intValue() == 0) {
        returnValue = 0; // Automatic failure
      }
      break;
    // Skills that should depend on clothing:
    case STEALTH: {
      final int stealth = c.getArmor().stealthValue();
      if (stealth == 0)
        return 0;
      returnValue *= stealth;
      returnValue /= 2;
    }
      break;
    case SEDUCTION:
    case PERSUASION:
      break;
    // Unique disguise handling
    case DISGUISE:
      // Check for appropriate uniform
      final Quality uniformed = c.hasDisguise();
      // Ununiformed disguise checks automatically fail
      if (uniformed == Quality.NONE) {
        returnValue = 0;
        break;
      } else if (uniformed == Quality.POOR) {
        returnValue >>= 1;
      }
      // Bloody, damaged clothing hurts disguise check
      if (c.getArmor().isBloody()) {
        returnValue >>= 1;
      }
      if (c.getArmor().isDamaged()) {
        returnValue >>= 1;
      }
      // Carrying corpses or having hostages is very bad for disguise
      if (c.prisoner().exists()) {
        returnValue >>= 2;
      }
      break;
    default:
    }
    Log.i(Game.LCS, "Creature.skillRoll(" + skill + "/" + skill.getAttribute() + ")=" + skill_value
        + "+" + adjusted_attribute_value + " got " + returnValue);
    return returnValue;
  }

  public void skillUp() {
    for (final Skill s : Skill.values()) {
      while (skillExperience.get(s).intValue() >= 100 + 10 * skills.get(s).intValue()
          && skills.get(s).intValue() < skillCap(s, true)) {
        skillExperience.put(s, skillExperience.get(s) - (100 + 10 * skills.get(s).intValue()));
        skill(s, 1);
      }
      if (skills.get(s).intValue() == skillCap(s, true)) {
        skillExperience.remove(s);
      }
    }
  }

  public int skillXp(final Skill skill) {
    return skillExperience.get(skill).intValue();
  }

  public void train(final Skill trainedSkill, final int experience) {
    train(trainedSkill, experience, 20);
  }

  private void train(final Skill trainedSkill, final int experience, final int upto) {
    // Don't give experience if already maxed out
    if (skillCap(trainedSkill, true) <= skills.get(trainedSkill).intValue()
        || upto <= skills.get(trainedSkill).intValue())
      return;
    // Don't give experience if requested to give none
    if (experience == 0)
      return;
    // Skill gain scaled by ability in the area
    skillExperience.put(
        trainedSkill,
        skillExperience.get(trainedSkill)
            + Math.max(1, (int) (experience * skillCap(trainedSkill, false) / 6.0)));
    int abovenextlevel;
    /* only allow gaining experience on the new level if it doesn't put us over a level limit */
    if (skills.get(trainedSkill).intValue() >= upto - 1
        || skills.get(trainedSkill).intValue() >= skillCap(trainedSkill, true) - 1) {
      abovenextlevel = 0;
    } else {
      abovenextlevel = 50 + 5 * (1 + skills.get(trainedSkill).intValue());
    }
    // enough skill points to get halfway through the next skill level
    skillExperience.put(
        trainedSkill,
        Math.min(skillExperience.get(trainedSkill).intValue(), 100 + 10
            * skills.get(trainedSkill).intValue() + abovenextlevel));
  }

  private static final long serialVersionUID = Game.VERSION;
}