package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Iterator;

import lcs.android.activities.ItemActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.ArmorType;
import lcs.android.items.Loot;
import lcs.android.site.Squad;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class MakeArmor extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature c : this) {
      makearmor(c);
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  private static void makearmor(final Creature cr) {
    final ArmorType at = (ArmorType) ((ItemActivity) cr.activity()).itemType();
    final int cost = at.makePrice;
    final int hcost = cost / 2 + 1;
    final int dif = at.makeDifficulty(cr);
    if (i.ledger.funds() < hcost) {
      ui().text(cr.toString() + " cannot afford material for clothing.").add();
      return;
    }
    boolean foundcloth = false;
    if (cr.squad()!= null) {
      final Squad sq = cr.squad();
      for (final Iterator<AbstractItem<? extends AbstractItemType>> li = sq.loot().iterator(); li
          .hasNext();) {
        final AbstractItem<? extends AbstractItemType> l = li.next();
        if (l instanceof Loot && ((Loot) l).isCloth()) // cast -XML
        {
          if (l.number() == 1) {
            li.remove();
          } else {
            l.decrease(1);
          }
          foundcloth = true;
          break;
        }
      }
    }
    Location r = cr.location();
    if (!foundcloth && true) {
      for (final Iterator<AbstractItem<? extends AbstractItemType>> li = cr.location().lcs().loot
          .iterator(); li.hasNext();) {
        final AbstractItem<? extends AbstractItemType> l = li.next();
        if (l instanceof Loot && ((Loot) l).isCloth()) // cast -XML
        {
          if (l.number() == 1) {
            li.remove();
          } else {
            l.decrease(1);
          }
          foundcloth = true;
          break;
        }
      }
    }
    if (!foundcloth && i.ledger.funds() < cost) {
      ui().text(cr.toString() + " cannot find enough cloth to reduce clothing costs.").add();
    } else {
      if (foundcloth) {
        i.ledger.subtractFunds(hcost, Ledger.ExpenseType.MANUFACTURE);
      } else {
        i.ledger.subtractFunds(cost, Ledger.ExpenseType.MANUFACTURE);
      }
      cr.skill().train(Skill.TAILORING, dif * 2 + 1);
      Armor.Rating quality = Armor.Rating.FIRST;
      if (!cr.skill().skillCheck(Skill.TAILORING, dif)) {
        quality = Armor.Rating.SECOND;
        if (!cr.skill().skillCheck(Skill.TAILORING, dif)) {
          quality = Armor.Rating.THIRD;
          if (!cr.skill().skillCheck(Skill.TAILORING, dif)) {
            quality = Armor.Rating.FOURTH;
          }
        }
      }
      Location r1 = cr.location();
      if (true) {
        final AbstractItem<? extends AbstractItemType> it = new Armor(at, quality);
        cr.location().lcs().loot.add(it);
        final StringBuilder str = new StringBuilder();
        str.append(cr.toString());
        str.append(" has made a ");
        switch (quality) {
        case FIRST:
          str.append("first-rate");
          break;
        case SECOND:
          str.append("second-rate");
          break;
        case THIRD:
          str.append("third-rate");
          break;
        case FOURTH:
        default:
          str.append("fourth-rate");
          break;
        }
        str.append(' ');
        str.append(it.toString());
        str.append('.');
        ui().text(str.toString()).add();
      }
    }
  }
}
