package lcs.android.creature.health;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault enum Animal {
  ANIMAL {
    @Override public String partName(final BodyPart bp) {
      switch (bp) {
      case HEAD:
        return "head";
      case BODY:
      default:
        return "body";
      case ARM_RIGHT:
        return "right front leg";
      case ARM_LEFT:
        return "left front leg";
      case LEG_RIGHT:
        return "right rear leg";
      case LEG_LEFT:
        return "left rear leg";
      }
    }
  },
  HUMAN {
    @Override public String partName(final BodyPart bp) {
      return bp.toString();
    }
  },
  TANK {
    @Override public String partName(final BodyPart bp) {
      switch (bp) {
      case HEAD:
        return "turret";
      case BODY:
      default:
        return "front";
      case ARM_RIGHT:
        return "right side";
      case ARM_LEFT:
        return "left side";
      case LEG_RIGHT:
        return "right tread";
      case LEG_LEFT:
        return "left tread";
      }
    }
  };
  abstract public String partName(BodyPart bp);
}