package lcs.android.site.type;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureName;
import lcs.android.creature.Gender;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.Vehicle;
import lcs.android.items.VehicleType;
import lcs.android.shop.Shop;
import lcs.android.util.Curses;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

@Xml.Name(name = "BUSINESS_CARDEALERSHIP") public @NonNullByDefault class CarDealership extends
    AbstractSiteType implements IShop {
  @Override public void generateName(final Location l) {
    l.setName(CreatureName.firstName(Gender.WHITEMALEPATRIARCH) + " " + CreatureName.lastname()
        + "'s Used Cars");
  }

  /* active squad visits the car dealership */
  @Override public void shop(final Location loc) {
    Creature buyer = i.activeSquad.member(0);
    i.activeSquad.location(loc);
    final int partysize = i.activeSquad.size();
    do {
      setView(R.layout.hospital);
      i.activeSquad.location().get().printLocationHeader();
      i.activeSquad.printParty();
      Vehicle car_to_sell = null;
      int price = 0;
      if (buyer.car().exists()) {
        car_to_sell = buyer.car().get();
      }
      maybeAddButton(R.id.gcontrol, 'g', "Get a Liberal car", car_to_sell == null);
      if (car_to_sell != null) {
        price = (int) (0.8 * car_to_sell.price());
        if (car_to_sell.heat() > 0) {
          price /= 10;
        }
        ui(R.id.gcontrol).button('s')
            .text("Sell the " + car_to_sell.fullname(false) + " ($" + price + ")").add();
      } else {
        ui(R.id.gcontrol).button('a').text("Sell a car").add();
      }
      maybeAddButton(R.id.gcontrol, 'b', "Choose a buyer", partysize >= 2);
      ui(R.id.gcontrol).button(10).text("Leave").add();
      final int c = getch();
      // Leave
      if (c == 10) {
        break;
      }
      // Sell the car
      if (c == 's') {
        i.ledger.addFunds(price, Ledger.IncomeType.CARS);
        if (car_to_sell == buyer.car().getNullable()) {
          buyer.car(null);
          buyer.prefCar(null);
        }
        i.vehicle.remove(car_to_sell);
      }
      // Get a car
      if (c == 'g') {
        int carchoice;
        final List<VehicleType> availablevehicle = new ArrayList<VehicleType>();
        final List<String> vehicleoption = new ArrayList<String>();
        for (final VehicleType v : Game.type.vehicle.values()) {
          if (v.availableatshop_) {
            availablevehicle.add(v);
            vehicleoption.add(v.toString() + " ($" + v.price_ + ")");
          }
        }
        do {
          carchoice = Curses.choiceprompt("Choose a vehicle", vehicleoption, true,
              "We don't need a Conservative car");
          if (carchoice != -1 && availablevehicle.get(carchoice).price_ > i.ledger.funds()) {
            fact("You don't have enough money!");
          } else {
            break;
          }
        } while (true);
        if (carchoice == -1) {
          continue;
        }
        // Picked a car, pick color
        final int colorchoice = Curses.choiceprompt("Choose a color",
            availablevehicle.get(carchoice).color(), true, "These colors are Conservative");
        if (colorchoice == -1) {
          continue;
        }
        final Vehicle v = new Vehicle(availablevehicle.get(carchoice), availablevehicle
            .get(carchoice).color().get(colorchoice), i.score.date.year());
        buyer.prefCar(v);
        i.vehicle.add(v);
        i.ledger.subtractFunds(v.price(), Ledger.ExpenseType.CARS);
      }
      if (c == 'b') {
        buyer = Shop.choose_buyer();
      }
      i.activeSquad.displaySquadInfo(c);
    } while (true);
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
