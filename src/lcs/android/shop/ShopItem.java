package lcs.android.shop;

import static lcs.android.game.Game.*;
import lcs.android.creature.Creature;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.Armor.Rating;
import lcs.android.items.Clip;
import lcs.android.items.Loot;
import lcs.android.items.Weapon;
import lcs.android.politics.Issue;
import lcs.android.site.Squad;
import lcs.android.util.LcsRuntimeException;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

@NonNullByDefault class ShopItem extends AbstractShopOption {
  private enum ItemClassEnum // Could be solved better without enum. -XML
  {
    ARMOR,
    CLIP,
    LOOT,
    WEAPON
  }

  @Nullable private ItemClassEnum itemclass = null;

  private String itemtypename;

  private int price = 0;

  @Override public Configurable xmlChild(final String value) {
    return Xml.UNCONFIGURABLE;
  }

  @Override public void xmlFinishChild() {}

  @Override public void xmlSet(final String key, final String value) {
    if ("@NonNullByDefault class".equals(key)) {
      if (Xml.getText(value).equals("WEAPON")) {
        itemclass = ItemClassEnum.WEAPON;
      } else if (Xml.getText(value).equals("CLIP")) {
        itemclass = ItemClassEnum.CLIP;
      } else if (Xml.getText(value).equals("ARMOR")) {
        itemclass = ItemClassEnum.ARMOR;
      } else if (Xml.getText(value).equals("LOOT")) {
        itemclass = ItemClassEnum.LOOT;
      } else {
        throw new LcsRuntimeException("Unknown item @NonNullByDefault class" + Xml.getText(value));
      }
    } else if ("type".equals(key)) {
      itemtypename = Xml.getText(value);
    } else if ("description".equals(key)) {
      description = Xml.getText(value);
      descriptionDefined = true;
    } else if ("letter".equals(key)) {
      Log.w("LCS", "Redundant key:" + key + "=" + value);
    } else if ("price".equals(key)) {
      price = Xml.getInt(value);
    }
  }

  @Override protected void choose(final Squad customers, final Creature buyer) {
    if (isAvailable()) {
      i.ledger.subtractFunds(adjustedPrice(), Ledger.ExpenseType.SHOPPING);
      switch (itemclass) {
      case WEAPON: {
        final Weapon j = new Weapon(itemtypename);
        if (customers.base()!= null) {
          buyer.weapon().giveWeapon(j, customers.base().lcs().loot);
          if (!j.isEmpty()) {
            customers.base().lcs().loot.add(j);
          }
        }
        break;
      }
      case CLIP: {
        final Clip j = new Clip(itemtypename);
        buyer.weapon().takeClips(j, 1);
        if (!j.isEmpty() && customers.base()!= null) {
          customers.base().lcs().loot.add(j);
        }
        break;
      }
      case ARMOR: {
        final Armor j = new Armor(itemtypename, Rating.FIRST); // shops
        // sell
        // the
        // good
        // stuff.
        if (customers.base()!= null) {
          buyer.giveArmor(j, customers.base().lcs().loot);
          if (!j.isEmpty()) {
            customers.base().lcs().loot.add(j);
          }
        }
        break;
      }
      case LOOT: {
        if (customers.base()!= null) {
          customers.base().lcs().loot.add(new Loot(itemtypename));
        }
        break;
      }
      default:
      }
    }
  }

  @Override protected boolean display() {
    return validItem() && (!onlySellLegal || legal());
  }

  @Override protected String getDescriptionFullscreen() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getDescription());
    sb.append(" $");
    sb.append(adjustedPrice());
    return sb.toString();
  }

  @Override protected boolean isAvailable() {
    return validItem() && (!onlySellLegal || legal()) && canAfford();
  }

  private int adjustedPrice() {
    int p = price;
    if (increasePricesWithIllegality && itemclass == ItemClassEnum.WEAPON && validItem()) {
      for (int j = Game.type.weapon.get(itemtypename).legality_, k = i.issue(Issue.GUNCONTROL)
          .law().trueOrdinal(); j < k; ++j) {
        p *= 2;
      }
    }
    return p;
  }

  private boolean canAfford() {
    return adjustedPrice() <= i.ledger.funds();
  }

  private String getDescription() {
    if (descriptionDefined) {
      return description;
    }
    switch (itemclass) {
    case WEAPON:
      return Game.type.weapon.get(itemtypename).toString();
    case CLIP:
      return Game.type.clip.get(itemtypename).toString();
    case ARMOR:
      return Game.type.armor.get(itemtypename).toString();
    case LOOT:
    default:
      return Game.type.loot.get(itemtypename).toString();
    }
  }

  private boolean legal() {
    boolean r = true;
    switch (itemclass) {
    case WEAPON:
      r = Game.type.weapon.get(itemtypename).is_legal();
      break;
    default:
      break;
    }
    return r;
  }

  private boolean validItem() {
    AbstractItemType r = null;
    switch (itemclass) {
    case WEAPON:
      r = Game.type.weapon.get(itemtypename);
      break;
    case CLIP:
      r = Game.type.clip.get(itemtypename);
      break;
    case ARMOR:
      r = Game.type.armor.get(itemtypename);
      break;
    case LOOT:
      r = Game.type.loot.get(itemtypename);
      break;
    default:
    }
    return r != null;
  }
}