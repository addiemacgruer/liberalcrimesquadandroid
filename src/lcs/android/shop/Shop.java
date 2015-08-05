package lcs.android.shop;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.creature.Creature;
import lcs.android.game.Ledger;
import lcs.android.game.Ledger.IncomeType;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Clip;
import lcs.android.items.Loot;
import lcs.android.items.Weapon;
import lcs.android.site.Squad;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class Shop extends AbstractShopOption {
  public Shop() {
    fullscreen = true;
    onlySellLegal = false;
    increasePricesWithIllegality = true;
  }

  public Shop(final String string) {
    final Xml x = new Xml(string);
    x.init(this);
  }

  @SuppressWarnings("unused") private char letter;

  private final List<AbstractShopOption> options = new ArrayList<AbstractShopOption>();

  // This function is used to start the shop interface.
  public void enter(final Squad customers) {
    i.ledger.addFunds(1000000, IncomeType.PAWN); // TODO cheat
    choose(customers, customers.member(0));
  }

  @Override public Configurable xmlChild(final String value) {
    if ("department".equals(value)) {
      final Shop s = new Shop();
      s.fullscreen = fullscreen;
      s.onlySellLegal = onlySellLegal;
      s.increasePricesWithIllegality = increasePricesWithIllegality;
      options.add(s);
      return s;
    } else if ("option".equals(value) || "item".equals(value)) {
      final ShopItem si = new ShopItem();
      si.fullscreen = fullscreen;
      si.onlySellLegal = onlySellLegal;
      si.increasePricesWithIllegality = increasePricesWithIllegality;
      options.add(si);
      return si;
    }
    return Xml.UNCONFIGURABLE;
  }

  /* (non-Javadoc)
   * @see lcs.android.util.Xml.Configurable#xmlFinishChild(Configurable c) */
  @Override public void xmlFinishChild() {}

  @Override public void xmlSet(final String key, final String value) {
    if ("only_sell_legal_items".equals(key)) {
      onlySellLegal = Xml.getBoolean(value);
    } else if ("fullscreen".equals(key)) {
      fullscreen = Xml.getBoolean(value);
    } else if ("allow_selling".equals(key)) {
      allowSelling = Xml.getBoolean(value);
    } else if ("increase_prices_with_illegality".equals(key)) {
      increasePricesWithIllegality = Xml.getBoolean(value);
    } else if ("entry".equals(key)) {
      description = Xml.getText(value);
    } else if ("exit".equals(key)) {
      exit = Xml.getText(value);
    } else if ("letter".equals(key)) {
      letter = Xml.getText(value).charAt(0);// not used
      // letter_ = Xml.getText(value).toLowerCase().charAt(0);
      // letter_defined_ = true;
    }
  }

  @Override protected void choose(final Squad customers, final Creature buyer) {
    if (fullscreen) {
      browseFullscreen(customers, buyer);
    } else {
      browseHalfscreen(customers, buyer);
    }
  }

  @Override protected boolean isAvailable() {
    for (final AbstractShopOption so : options) {
      if (so.display()) {
        return true;
      }
    }
    return false;
  }

  private void browseFullscreen(final Squad customers, final Creature buyer) {
    do {
      setView(R.layout.generic);
      int y = 'a';
      ui().text("What will " + buyer.toString() + " buy?").add();
      for (final AbstractShopOption so : options) {
        maybeAddButton(R.id.gcontrol, y++, so.getDescriptionFullscreen(),
            so.display() && so.isAvailable());
      }
      ui(R.id.gcontrol).button(10).text(exit).add();
      final int c = getch();
      if (c == 10) {
        break;
      }
      if (c >= 'a') {
        options.get(c - 'a').choose(customers, buyer);
      }
    } while (true);
  }

  private void browseHalfscreen(final Squad customers, final Creature buyer) {
    Creature currentbuyer = buyer;
    do {
      setView(R.layout.generic);
      int y = 'a';
      ui().text("What will " + currentbuyer.toString() + " buy?").add();
      for (final AbstractShopOption so : options) {
        maybeAddButton(R.id.gcontrol, y++, so.getDescriptionFullscreen(),
            so.display() && so.isAvailable());
      }
      ui(R.id.gcontrol).text("Other actions:").add();
      ui(R.id.gcontrol).button(11).text("Look over equipment").add();
      maybeAddButton(R.id.gcontrol, 12, "Sell something", allowSelling);
      maybeAddButton(R.id.gcontrol, 13, "Chosse a buyer", customers.size() > 1);
      ui(R.id.gcontrol).button(10).text(exit).add();
      final int c = getch();
      if (c == 10) {
        break;
      } else if (c == 11) {
        AbstractItem.equip(customers.loot(), null);
      } else if (c == 12) {
        sellLoot(customers);
      } else if (c == 13) {
        currentbuyer = chooseBuyer(customers);
      }
      if (c >= 'a') {
        options.get(c - 'a').choose(customers, currentbuyer);
      }
    } while (true);
  }

  /* active squad visits the arms dealer */
  /* choose buyer */
  public static Creature choose_buyer() {
    do {
      setView(R.layout.generic);
      ui().text("Choose a Liberal squad member to SPEND.").add();
      int y = '1';
      for (final Creature p : i.activeSquad()) {
        ui(R.id.gcontrol).button(y++).text(p.toString()).add();
      }
      final int c = getch();
      if (c >= '1') {
        return i.activeSquad().member(c - '1');
      }
    } while (true);
  }

  private static Creature chooseBuyer(final Squad customers) {
    do {
      setView(R.layout.generic);
      ui().text("Choose a Liberal squad member to SPEND.").add();
      int y = '1';
      for (final Creature p : customers) {
        ui(R.id.gcontrol).button(y++).text(p.toString()).add();
      }
      final int c = getch();
      if (c >= '1') {
        return customers.member(c - '1');
      }
    } while (true);
  }

  private static int fenceselect(final Squad customers) {
    int ret = 0;
    final List<AbstractItem<? extends AbstractItemType>> source = customers.loot();
    final StringBuilder sb = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui(R.id.gcontrol).text("Choose an item to fence.").add();
      ui(R.id.gcontrol).text("$" + ret + " fenced so far.").add();
      int y = 'a';
      for (final AbstractItem<? extends AbstractItemType> l : source) {
        sb.append(l.equipTitle());
        if (l.number() > 1) {
          sb.append(" x");
          sb.append(l.number());
          sb.append(" ($");
          sb.append(l.fenceValue());
          sb.append(" ea.)");
        } else {
          sb.append(" ($");
          sb.append(l.fenceValue());
          sb.append(')');
        }
        maybeAddButton(R.id.gcontrol, y++, sb.toString(), l.isGoodForSale());
        sb.setLength(0);
      }
      ui(R.id.gcontrol).button(10).text("Done.").add();
      final int c = getch();
      if (c == 10) {
        return ret;
      }
      final AbstractItem<? extends AbstractItemType> slot;
      try {
        slot = source.get(c - 'a');
      } catch (final ArrayIndexOutOfBoundsException e) {
        Log.e("LCS", "Bad slot.  Been using keyboard?", e);
        continue;
      }
      if (slot.number() == 1) {
        source.remove(slot);
        ret += slot.fenceValue();
      } else {
        final int amount = AbstractItem.promptAmount(0, slot.number());
        slot.split(amount);
        if (slot.isEmpty()) {
          source.remove(slot);
        }
        ret += slot.fenceValue() * amount;
      }
      AbstractItem.consolidateloot(source);
    } while (true);
  }

  private static void sellLoot(final Squad customers) {
    do {
      setView(R.layout.generic);
      maybeAddButton(R.id.gcontrol, 'e', "Look over Equipment", true);
      maybeAddButton(R.id.gcontrol, 'f', "Pawn Selectively", customers.loot().size() > 0);
      maybeAddButton(R.id.gcontrol, 'w', "Pawn all Weapons", customers.loot().size() > 0);
      maybeAddButton(R.id.gcontrol, 'a', "Pawn all Ammunition", customers.loot().size() > 0);
      maybeAddButton(R.id.gcontrol, 'c', "Pawn all Clothes", customers.loot().size() > 0);
      maybeAddButton(R.id.gcontrol, 'l', "Pawn all Loot", customers.loot().size() > 0);
      ui(R.id.gcontrol).button(ENTER).text("Done pawning").add();
      int c = getch();
      if (c == ENTER) {
        break;
      }
      if (c == 'e') {
        AbstractItem.equip(customers.loot(), null);
      }
      if (c == 'w' || c == 'a' || c == 'c') {
        int c2 = 'n';
        switch (c) {
        case 'w':
          c2 = yesOrNo("Really sell all weapons?");
          break;
        case 'a':
          c2 = yesOrNo("Really sell all ammo?");
          break;
        case 'c':
          c2 = yesOrNo("Really sell all clothes?");
          break;
        default:
        }
        if (c2 != 'y') {
          c = 0;// no sale
        }
      }
      if ((c == 'w' || c == 'c' || c == 'l' || c == 'a' || c == 'f') && customers.loot().size() > 0) {
        int fenceamount = 0;
        if (c == 'f') {
          fenceamount = fenceselect(customers);
        } else {
          final Iterator<AbstractItem<? extends AbstractItemType>> li = customers.loot().iterator();
          while (li.hasNext()) {
            final AbstractItem<? extends AbstractItemType> l = li.next();
            if (c == 'w' && l instanceof Weapon && l.isGoodForSale()) {
              fenceamount += l.fenceValue() * l.number();
              li.remove();
            } else if (c == 'c' && l instanceof Armor && l.isGoodForSale()) {
              fenceamount += l.fenceValue() * l.number();
              li.remove();
            } else if (c == 'a' && l instanceof Clip && l.isGoodForSale()) {
              fenceamount += l.fenceValue() * l.number();
              li.remove();
            } else if (c == 'l' && l instanceof Loot && l.isGoodForSale()
                && !((Loot) l).noQuickFencing()) {
              fenceamount += l.fenceValue() * l.number();
              li.remove();
            }
          }
        }
        if (fenceamount > 0) {
          fact("You add $" + fenceamount + " to Liberal Funds.");
          i.ledger.addFunds(fenceamount, Ledger.IncomeType.PAWN);
        }
      }
    } while (true);
  }
}
