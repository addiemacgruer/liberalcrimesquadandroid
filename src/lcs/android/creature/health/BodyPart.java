package lcs.android.creature.health;

import java.util.EnumSet;
import java.util.Set;

import lcs.android.util.DefaultValueKey;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Parts of the body.
 * @author addie */
public @NonNullByDefault enum BodyPart implements DefaultValueKey<Set<Wound>> {
  ARM_LEFT("left arm", 20),
  ARM_RIGHT("right arm", 20),
  BODY("body", 100),
  HEAD("head", 10),
  LEG_LEFT("left leg", 40),
  LEG_RIGHT("right leg", 40);
  BodyPart(final String name, final int sever) {
    displayName = name;
    severAmount = sever;
  }

  private final String displayName;

  private final int severAmount;

  @Override public Set<Wound> defaultValue() {
    return EnumSet.noneOf(Wound.class);
  }

  /** Amount of damage required to sever.
   * @return a damage amount. */
  public int severAmount() {
    return severAmount;
  }

  @Override public String toString() {
    return displayName;
  }
}
