package lcs.android.site.type;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.Armor;
import lcs.android.items.ArmorType;
import lcs.android.items.Weapon;
import lcs.android.items.WeaponType;
import lcs.android.shop.Shop;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Xml.Name(name = "BUSINESS_HALLOWEEN") public @NonNullByDefault class HalloweenStore extends
    AbstractSiteType implements IShop {
  @Override public void generateName(final Location l) {
    l.setName("The Oubliette");
  }

  /* oubliette - buy a mask */
  @Override public void shop(final Location loc) {
    Creature buyer = i.activeSquad().member(0);
    int in_halloween = 0;
    i.activeSquad().location(loc);
    final int partysize = i.activeSquad().size();
    do {
      WeaponType weaponbought = null;
      ArmorType armorbought = null;
      ui().text("Buyer: ").add();
      ui().text(buyer.toString()).add();
      if (in_halloween == 1) {
        setView(R.layout.generic);
        maybeAddButton(R.id.gcontrol, 't', "Buy a Trench Coat ($70)", i.ledger.funds() >= 70);
        maybeAddButton(R.id.gcontrol, 'w', "Buy Work Clothes ($50)", i.ledger.funds() >= 50);
        maybeAddButton(R.id.gcontrol, 'l', "Buy a Lab Coat ($200)", i.ledger.funds() >= 200);
        maybeAddButton(R.id.gcontrol, 'r', "Buy a Black Judge's Robe ($200)",
            i.ledger.funds() >= 200);
        maybeAddButton(R.id.gcontrol, 'c', "Buy a Clown Suit ($200)", i.ledger.funds() >= 200);
        maybeAddButton(R.id.gcontrol, 'g', "Buy Bondage Gear ($350)", i.ledger.funds() >= 350);
        maybeAddButton(R.id.gcontrol, 'm', "Buy a Mask ($15)", i.ledger.funds() >= 15);
        maybeAddButton(R.id.gcontrol, 'o', "Buy a Toga ($90)", i.ledger.funds() >= 90);
        maybeAddButton(R.id.gcontrol, 'e', "Buy an Elephant Suit ($1000)", i.ledger.funds() >= 1000);
        maybeAddButton(R.id.gcontrol, 'd', "Buy a Donkey Suit ($1000)", i.ledger.funds() >= 1000);
        ui(R.id.gcontrol).button(10).text("Done").add();
      } else if (in_halloween == 2) {
        setView(R.layout.generic);
        if (i.score.date.year() < 2100) {
          maybeAddButton(R.id.gcontrol, 'k', "Buy a Knife ($20)", i.ledger.funds() >= 20);
          maybeAddButton(R.id.gcontrol, 's', "Buy the Sword of Morfiegor ($250)",
              i.ledger.funds() >= 250);
          maybeAddButton(R.id.gcontrol, 'a', "Buy a Katana and Wakizashi ($250)",
              i.ledger.funds() >= 250);
        } else {
          maybeAddButton(R.id.gcontrol, 'k', "Buy a Vibro-Knife ($20)", i.ledger.funds() >= 20);
          maybeAddButton(R.id.gcontrol, 's', "Buy a Light Sword ($250)", i.ledger.funds() >= 250);
          maybeAddButton(R.id.gcontrol, 'a', "Buy the Liberal Twin Swords($250)",
              i.ledger.funds() >= 250);
        }
        maybeAddButton(R.id.gcontrol, 'h', "Buy a Dwarven Hammer ($250)", i.ledger.funds() >= 250);
        maybeAddButton(R.id.gcontrol, 'm', "Buy the Maul of Anrin ($250)", i.ledger.funds() >= 250);
        maybeAddButton(R.id.gcontrol, 'c', "Buy a Silver Cross ($250)", i.ledger.funds() >= 250);
        maybeAddButton(R.id.gcontrol, 'w', "Buy a Wizard's Staff ($250)", i.ledger.funds() >= 250);
        maybeAddButton(R.id.gcontrol, '!', "Buy Mithril Mail ($1000)", i.ledger.funds() >= 1000);
        ui(R.id.gcontrol).button(10).text("Done").add();
      } else {
        setView(R.layout.hospital);
        i.activeSquad().location().get().printLocationHeader();
        i.activeSquad().printParty();
        ui(R.id.gcontrol).button('c').text("Purchase Halloween Costumes").add();
        ui(R.id.gcontrol).button('m').text("Purchase Medieval Gear").add();
        ui().button(10).text("Leave").add();
      }
      ui(R.id.gcontrol).button('e').text("Look over Equipment").add();
      maybeAddButton(R.id.gcontrol, 'b', "Choose a buyer", partysize >= 2);
      final int c = getch();
      if (in_halloween == 1) {
        if (c == 10) {
          in_halloween = 0;
        }
        if (c == 't' && i.ledger.funds() >= 70) {
          armorbought = Game.type.armor.get("ARMOR_TRENCHCOAT");
          i.ledger.subtractFunds(70, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'w' && i.ledger.funds() >= 50) {
          armorbought = Game.type.armor.get("ARMOR_WORKCLOTHES");
          i.ledger.subtractFunds(50, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'l' && i.ledger.funds() >= 200) {
          armorbought = Game.type.armor.get("ARMOR_LABCOAT");
          i.ledger.subtractFunds(200, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'r' && i.ledger.funds() >= 200) {
          armorbought = Game.type.armor.get("ARMOR_BLACKROBE");
          i.ledger.subtractFunds(200, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'c' && i.ledger.funds() >= 200) {
          armorbought = Game.type.armor.get("ARMOR_CLOWNSUIT");
          i.ledger.subtractFunds(200, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'g' && i.ledger.funds() >= 350) {
          armorbought = Game.type.armor.get("ARMOR_BONDAGEGEAR");
          i.ledger.subtractFunds(350, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'e' && i.ledger.funds() >= 1000) {
          armorbought = Game.type.armor.get("ARMOR_ELEPHANTSUIT");
          i.ledger.subtractFunds(1000, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'd' && i.ledger.funds() >= 1000) {
          armorbought = Game.type.armor.get("ARMOR_DONKEYSUIT");
          i.ledger.subtractFunds(1000, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'm' && i.ledger.funds() >= 15) {
          final ArmorType mask = HalloweenStore.maskselect(buyer);
          armorbought = mask;
          i.ledger.subtractFunds(15, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'o' && i.ledger.funds() >= 90) {
          armorbought = Game.type.armor.get("ARMOR_TOGA");
          i.ledger.subtractFunds(90, Ledger.ExpenseType.SHOPPING);
        }
      } else if (in_halloween == 2) {
        if (c == 10) {
          in_halloween = 0;
        }
        if (c == 'k' && i.ledger.funds() >= 20) {
          weaponbought = Game.type.weapon.get("WEAPON_KNIFE");
          i.ledger.subtractFunds(20, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 's' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_SWORD");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'a' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_DAISHO");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'h' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_HAMMER");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'm' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_MAUL");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'c' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_CROSS");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == 'w' && i.ledger.funds() >= 250) {
          weaponbought = Game.type.weapon.get("WEAPON_STAFF");
          i.ledger.subtractFunds(250, Ledger.ExpenseType.SHOPPING);
        }
        if (c == '!' && i.ledger.funds() >= 1000) {
          armorbought = Game.type.armor.get("ARMOR_MITHRIL");
          i.ledger.subtractFunds(1000, Ledger.ExpenseType.SHOPPING);
        }
      } else {
        if (c == 10) {
          break;
        }
        if (c == 'c') {
          in_halloween = 1;
        }
        if (c == 'm') {
          in_halloween = 2;
        }
      }
      if (armorbought != null && i.activeSquad().base().exists()) {
        final Armor a = new Armor(armorbought);
        buyer.giveArmor(a, i.activeSquad().base().get().lcs().loot);
      }
      if (weaponbought != null && i.activeSquad().base().exists()) {
        Weapon w = new Weapon(weaponbought);
        buyer.weapon().giveWeapon(w, i.activeSquad().base().get().lcs().loot);
        if (w.isEmpty()) {
          w = null;
        } else {
          i.activeSquad().base().get().lcs().loot.add(w);
        }
      }
      if (c == 'e' && i.activeSquad().location().exists()) {
        AbstractItem.equip(i.activeSquad().location().get().lcs().loot, null);
      }
      if (c == 'b') {
        buyer = Shop.choose_buyer();
      }
      i.activeSquad().displaySquadInfo(c);
    } while (true);
  }

  private static final long serialVersionUID = Game.VERSION;

  @Nullable public static ArmorType maskselect(final Creature cr) {
    final List<ArmorType> masks = new ArrayList<ArmorType>();
    for (final ArmorType j : Game.type.armor.values()) {
      if (j.mask) {
        masks.add(j);
      }
    }
    do {
      setView(R.layout.generic);
      ui().text("Which mask will " + cr.toString() + " buy?").add();
      ui(R.id.gcontrol).button(12).text("Surprise " + cr.toString() + " with a Random Mask").add();
      int y = 'a';
      for (final ArmorType m : masks) {
        ui(R.id.gcontrol).button(y++).text(m.toString() + ": " + m.description).add();
      }
      final int c = getch();
      if (c >= 'a')
        return masks.get(c - 'a');
      if (c == 12)
        return i.rng.randFromList(masks);
      if (c == 10) {
        break;
      }
    } while (true);
    return null;
  }
}
