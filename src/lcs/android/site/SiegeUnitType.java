package lcs.android.site;

import static lcs.android.game.Game.*;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.encounters.Encounter;
import lcs.android.politics.Alignment;

public enum SiegeUnitType {
  HEAVYUNIT,
  TRAP,
  UNIT,
  UNIT_DAMAGED;
  /** generates a new siege encounter */
  boolean addSiegeEncounter() {
    final int freeslots = Encounter.ENCMAX - i.currentEncounter().size();
    if (freeslots < 1) {
      return false;
    }
    Creature cr = null;
    switch (this) {
    case UNIT:
    case UNIT_DAMAGED: {
      if (freeslots < 6) {
        return false;
      }
      final int num = i.rng.nextInt(3) + 4;
      for (int e = 0; e < num; e++) {
        if (i.site.current().lcs().siege.siege) {
          switch (i.site.current().lcs().siege.siegetype) {
          case POLICE:
            if (i.site.current().lcs().siege.escalationState == 0) {
              cr = CreatureType.withType(CreatureType.valueOf("SWAT"));
            } else {
              cr = CreatureType.withType(CreatureType.valueOf("SOLDIER"));
            }
            break;
          case CIA:
            cr = CreatureType.withType(CreatureType.valueOf("AGENT"));
            break;
          case HICKS:
            cr = CreatureType.withType(CreatureType.valueOf("HICK"));
            break;
          case CORPORATE:
            cr = CreatureType.withType(CreatureType.valueOf("MERC"));
            break;
          case CCS:
            if (i.rng.chance(12)) {
              cr = CreatureType.withType(CreatureType.valueOf("CCS_ARCHCONSERVATIVE"));
            } else if (i.rng.chance(11)) {
              cr = CreatureType.withType(CreatureType.valueOf("CCS_MOLOTOV"));
            } else if (i.rng.chance(10)) {
              cr = CreatureType.withType(CreatureType.valueOf("CCS_SNIPER"));
            } else {
              cr = CreatureType.withType(CreatureType.valueOf("CCS_VIGILANTE"));
            }
            break;
          case FIREMEN:
            cr = CreatureType.withType(CreatureType.valueOf("FIREFIGHTER"));
            break;
          default:
            cr = CreatureType.withType(CreatureType.valueOf(i.site.type().siegeUnit()));
            cr.alignment(Alignment.CONSERVATIVE);
          }
        } else {
          cr = CreatureType.withType(CreatureType.valueOf(i.site.type().siegeUnit()));
          cr.alignment(Alignment.CONSERVATIVE);
        }
        // if (cr == null) {
        // continue;
        // }
        if (this == UNIT_DAMAGED) {
          cr.health().blood(i.rng.nextInt(75) + 1);
        }
        i.currentEncounter().creatures().add(cr);
      }
      break;
    }
    case HEAVYUNIT: {
      i.currentEncounter().makeEncounterCreature("TANK");
      break;
    }
    default:
      break;
    }
    return true;
  }
}
