package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.site.Squad;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class RepairArmor extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature c : this) {
      repairarmor(c);
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  private static void repairarmor(final Creature cr) {
    Armor armor = null;
    AbstractItem<? extends AbstractItemType> pile = null;
    List<AbstractItem<? extends AbstractItemType>> pilelist = new ArrayList<AbstractItem<? extends AbstractItemType>>();
    if (!cr.isNaked() && (cr.getArmor().isBloody() || cr.getArmor().isDamaged())) {
      armor = cr.getArmor();
    } else if (cr.squad().exists()) {
      final Squad sq = cr.squad().get();
      for (final AbstractItem<? extends AbstractItemType> l : sq.loot()) {
        if (l instanceof Armor) {
          final Armor a = (Armor) l; // cast -XML
          if (a.isBloody() || a.isDamaged()) {
            armor = a;
            pile = l;
            pilelist = sq.loot();
            break;
          }
        }
      }
    }
    if (armor == null && cr.location().exists()) {
      for (final AbstractItem<? extends AbstractItemType> l : cr.location().get().lcs().loot) {
        if (l instanceof Armor) {
          final Armor a = (Armor) l; // cast -XML
          if (a.isBloody() || a.isDamaged()) {
            armor = a;
            pile = l;
            pilelist = cr.location().get().lcs().loot;
            break;
          }
        }
      }
    }
    if (armor != null) {
      boolean repairfailed = false;
      if (armor.isDamaged()) {
        int dif = armor.makeDifficulty(cr);
        dif >>= 1;
        cr.skill().train(Skill.TAILORING, dif + 1);
        if (i.rng.chance(1 + dif / 2)) {
          repairfailed = true;
        }
      }
      ui().text(cr.toString()).add();
      if (armor.isDamaged()) {
        if (repairfailed) {
          ui().text(" is working to repair ").add();
        } else {
          ui().text(" repairs ").add();
        }
      } else {
        ui().text(" cleans ").add();
      }
      ui().text(armor.toString()).add();
      ui().text(".").add();
      if (pile != null) {
        if (pile.number() > 1) {
          final AbstractItem<? extends AbstractItemType> newpile = pile.split(pile.number() - 1);
          pilelist.add(newpile);
        }
      }
      armor.setBloody(false);
      if (!repairfailed) {
        armor.setDamaged(false);
      }
      getch();
    }
  }
}
