package lcs.android.creature.skill;

import static lcs.android.creature.Attribute.*;

import java.util.Locale;

import lcs.android.creature.Attribute;
import lcs.android.util.DefaultValueKey;

public enum Skill implements DefaultValueKey<Integer> {
  ART(HEART),
  AXE(STRENGTH),
  BUSINESS(INTELLIGENCE),
  CLUB(STRENGTH),
  COMPUTERS(INTELLIGENCE),
  DISGUISE(CHARISMA),
  DODGE(AGILITY),
  DRIVING(AGILITY),
  FIRSTAID(INTELLIGENCE),
  HANDTOHAND(AGILITY),
  HEAVYWEAPONS(STRENGTH),
  KNIFE(AGILITY),
  LAW(INTELLIGENCE),
  MUSIC(HEART),
  PERSUASION(CHARISMA),
  PISTOL(AGILITY),
  PSYCHOLOGY(INTELLIGENCE),
  RELIGION(INTELLIGENCE),
  RIFLE(AGILITY),
  SCIENCE(INTELLIGENCE),
  SECURITY(INTELLIGENCE),
  SEDUCTION(CHARISMA),
  SHOOTINGUP(WISDOM),
  SHOTGUN(AGILITY),
  SMG(AGILITY),
  STEALTH(AGILITY),
  STREETSENSE(INTELLIGENCE),
  SWORD(AGILITY),
  TAILORING(INTELLIGENCE),
  TEACHING(INTELLIGENCE),
  THROWING(AGILITY),
  WRITING(INTELLIGENCE);
  Skill(final Attribute a) {
    attribute = a;
  }

  final Attribute attribute;

  @Override public Integer defaultValue() {
    return unskilled;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  @Override public String toString() {
    switch (this) {
    case HANDTOHAND:
      return "Hand-to-hand";
    case FIRSTAID:
      return "First aid";
    case STREETSENSE:
      return "Street sense";
    case HEAVYWEAPONS:
      return "Heavy weapons";
    case SHOOTINGUP:
      return "Shooting up";
    default:
      final String def = super.toString();
      return def.charAt(0) + def.substring(1).toLowerCase(Locale.ENGLISH);
    }
  }

  private static final Integer unskilled = Integer.valueOf(0);
}
