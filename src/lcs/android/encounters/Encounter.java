package lcs.android.encounters;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.game.Game;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Vehicle;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Common methods for a CarChase / FootChase / SiteEncounter
 * @author addie */
public abstract @NonNullByDefault class Encounter implements Serializable {
  /** Any stuff on the ground */
  private final List<AbstractItem<? extends AbstractItemType>> groundLoot = new ArrayList<AbstractItem<? extends AbstractItemType>>();

  /** The creatures that you meet while out and about. */
  protected final List<Creature> encounter = new ArrayList<Creature>();

  public List<Creature> creatures() {
    return encounter;
  }

  /** Carry out the encounter.
   * @return true if anyone escapes. */
  public abstract boolean encounter();

  /** Counts how many enemies are present.
   * @return the number of enemies in this encounter. */
  public int enemyCount() {
    return Filter.count(encounter, Filter.IS_CONSERVATIVE);
  }

  /** Whether there's any loot on the ground.
   * @return a list of loot items. */
  public List<AbstractItem<? extends AbstractItemType>> groundLoot() {
    return groundLoot;
  }

  /** Whether there is anyone in this location.
   * @return true if so. */
  public boolean isEmpty() {
    return encounter.isEmpty();
  }

  /** Add a creature to this encounter
   * @param ct the creature type to add. */
  public void makeEncounterCreature(final CreatureType ct) {
    encounter.add(CreatureType.withType(ct));
  }

  /** Add a creature to this encounter
   * @param ct the creature type to add. */
  public void makeEncounterCreature(final String string) {
    encounter.add(CreatureType.withType(CreatureType.valueOf(string)));
  }

  /** Clears the R.id.smencounter, and fills it with a description of the people following you. */
  public void printEncounter() {
    clearChildren(R.id.smencounter);
    ui(R.id.smencounter).text("Encounter:").bold().add();
    int higher = 128;
    for (final Creature e : creatures()) {
      ui(R.id.smencounter).text(e.longDescription()).button(higher++).color(e.alignment().color())
          .add();
    }
  }

  /** Encounter size.
   * @return how many creatures are in the current encounter. */
  public int size() {
    return encounter.size();
  }

  /** Maximum number of creatures that we can meet at once (18). */
  public static final int ENCMAX = 18;

  private static final long serialVersionUID = Game.VERSION;

  public static Encounter createEncounter(@Nullable final AbstractSiteType sitetype,
      final int siteCrime) {
    if (siteCrime == 0) {
      return new EmptyEncounter();
    }
    String cartype = "VEHICLE_POLICECAR"; // Temporary (transitionally) solution. -XML
    final int pursuers;
    boolean canpullover = false;
    final List<Creature> encounter = new ArrayList<Creature>();
    /* 50% of CCS harassing your teams once they reach the "attacks" stage (but not for activities,
     * which are law enforcement response specific) */
    if (i.endgameState.ccsActive() && i.rng.chance(2) && sitetype != null) {
      cartype = "VEHICLE_SUV";
      /* A CCS property, not a i.vehicle property. Temporary solution -XML */
      pursuers = Math.min(i.rng.nextInt(siteCrime / 5 + 1) + 1, 12);
      for (int n = 0; n < pursuers; n++) {
        final Creature c = CreatureType.withType("CCS_VIGILANTE");
        encounter.add(c);
      }
    } else if (sitetype == null) {
      canpullover = true;
      cartype = "VEHICLE_POLICECAR";
      pursuers = Math.min(6, i.rng.nextInt(siteCrime / 5 + 1) + 1);
      for (int n = 0; n < pursuers; n++) {
        final Creature c = CreatureType.makePolice();
        canpullover &= c.type().ofType("DEATHSQUAD");
        encounter.add(c);
      }
    } else {
      cartype = sitetype.carChaseCar();
      final String creatureType = sitetype.carChaseCreature();
      pursuers = Math.min(sitetype.carChaseIntensity(siteCrime), sitetype.carChaseMax());
      for (int n = 0; n < pursuers; n++) {
        final Creature c = CreatureType.withType(creatureType);
        encounter.add(c);
      }
    }
    for (final Creature n : encounter) {
      n.conservatise();
    }
    // ASSIGN CARS TO CREATURES
    final int carnum = calculateCarCount(pursuers);
    final Encounter rval;
    if (carnum > 0) {
      rval = new CarChase(Location.none());
      ((CarChase) rval).canpullover = canpullover;
      for (int c = 0; c < carnum; c++) {
        final Vehicle v = new Vehicle(Game.type.vehicle.get(cartype));
        /* If car type is unknown, due to change in xml file, the game will crash here. -XML */
        ((CarChase) rval).enemycar.add(v);
        for (final Creature n : encounter) {
          if (n.car()==null) {
            n.car(v).driver(true);
            break;
          }
        }
      }
      for (final Creature enemy : encounter) {
        if (enemy.car()==null) {
          enemy.car(i.rng.randFromList(((CarChase) rval).enemycar)).driver(false);
        }
      }
    } else {
      rval = new FootChase();
    }
    rval.encounter.addAll(encounter);
    return rval;
  }

  /** How many cars your pursuers bundle into.
   * @param pursuers pursuer count
   * @return car count */
  private static int calculateCarCount(final int pursuers) {
    int carnum;
    if (pursuers <= 2) {
      carnum = 1;
    } else if (pursuers <= 3) {
      carnum = i.rng.nextInt(2) + 1;
    } else if (pursuers <= 5) {
      carnum = i.rng.nextInt(2) + 2;
    } else if (pursuers <= 7) {
      carnum = i.rng.nextInt(2) + 3;
    } else {
      carnum = 4;
    }
    return carnum;
  }
}
