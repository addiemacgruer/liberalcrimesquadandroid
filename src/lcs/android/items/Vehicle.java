package lcs.android.items;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.basemode.iface.Location;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** Something the LCS can drive around in. */
public @NonNullByDefault class Vehicle extends AbstractItem<VehicleType> {
  public Vehicle(final String string) {
    this(Game.type.vehicle.get(vehicled(string)));
  }

  public Vehicle(final VehicleType seed) {
    this(seed, i.rng.randFromList(seed.color()), seed.makeyear());
  }

  public Vehicle(final VehicleType seed, final String color, final int myear) {
    super(seed);
    Log.w(Game.LCS, "Vehicle:" + seed + ":" + color + ":" + myear);
    heat = 0;
    // location_ = null;
    location = Location.none();
    this.color = color;
    manufacturedYear = myear;
  }

  private final String color;

  private int heat;

  private Location location;

  private final int manufacturedYear;

  /** Increases the heat on a vehicle. */
  public void addHeat(final int addition) {
    heat += addition;
  }

  @Override public void displayStats(final int viewID) {
    ui(viewID).text("Color: " + color).add();
    ui(viewID).text("Manufactured: " + manufacturedYear).add();
    maybeAddText(viewID, "Heat: " + heat, heat > 0);
    super.displayStats(viewID);
  }

  /** How much of a getaway bonus this vehicle is to drive. Up to 3 for sports and agent cars */
  public int driveBonus() {
    return type.drivebonus_;
  }

  @Override public String equipTitle() {
    return toString();
  }

  @Override public int fenceValue() {
    return price();
  }

  /** Returns the fullname of the vehicle, [Stolen] [Red] [Manf.] [Type]
   * @param shortName use the short description of Type
   * @return a string describing the car. */
  public String fullname(final boolean shortName) {
    String s = "";
    int words = 0;
    if (heat > 0) {
      s = "Stolen ";
      words++;
    }
    if (displayscolor()) {
      s += color + " ";
      words++;
    }
    if (manufacturedYear != -1 && words < 2) {
      s += manufacturedYear + " ";
    }
    if (shortName) {
      s += shortname();
    } else {
      s += longName();
    }
    return s;
  }

  /** @return */
  public Vehicle get() { // TODO inline
    return this;
  }

  /** Whether the vehicle has any heat on it. Only affects how much you can sell it for in the
   * dealership: any heat at all means you're only getting 10% of the normal price. */
  public int heat() {
    return heat;
  }

  /** Where the vehicle is kept. */
  public Location location() {
    return location;
  }

  /** Long description. */
  public String longName() {
    return type.toString();
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> other) {
    return false;
  }

  /** Cost to buy. Up to $80k for the (military) hummvee. */
  public int price() {
    return type.price_;
  }

  /** Likelihood a sensor alarm is fitted. 15 for a sportscar
   * @return percentage chance. */
  public int senseAlarmChance() {
    return type.sensealarmchance_;
  }

  /** Change where the vehicle is kept. */
  public void setLocation(final Location loc) {
    location = loc;
  }

  public String shortname() {
    return type.shortname();
  }

  @Override public AbstractItem<VehicleType> split(final int count) {
    throw new UnsupportedOperationException("Tried to split a vehicle");
  }

  /** Bonus heat for thieving one of these. Up to 16 for police cars, prevents you from ever selling
   * them full prices. */
  public int stealExtraHeat() {
    return type.steal_extraheat_;
  }

  /** Bonus juice for stealing one of these. 2 for a police car. */
  public int stealJuice() {
    return type.steal_juice_;
  }

  @Override public String toString() {
    return longName();
  }

  /** Likelihood a touch alarm is fitted. 95 for a sportscar
   * @return percentage chance. */
  public int touchAlarmChance() {
    return type.touchalarmchance_;
  }

  private boolean displayscolor() {
    return type.displaycolor_;
  }

  private static final long serialVersionUID = Game.VERSION;

  private static String vehicled(final String string) {
    if (!string.startsWith("VEHICLE_")) {
      return "VEHICLE_" + string;
    }
    return string;
  }
}
