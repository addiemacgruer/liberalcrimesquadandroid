package lcs.android.site.map;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.game.CheckDifficulty;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Weapon;
import lcs.android.util.Color;

/** Things which can be (opened) by bashing.
 * @author addie */
public enum Bashable {
  /** A door to bash in. Automatically adjusts to the site security level. */
  DOOR {
    @Override public SuccessTest bash() {
      final SecurityLevel securityLevel = i.site.current().type().securityLevel();
      final CheckDifficulty difficulty = securityLevel.doorDifficulty();
      boolean crowable = securityLevel.crowbarable();
      if (crowable) {
        crowable = teamHasCrowbar() ? true : crowbarInLoot();
      }
      int maxattack = 0;
      Creature maxp = i.activeSquad().member(0);
      if (!crowable) {
        for (final Creature p : i.activeSquad()) {
          if (p.health().alive()
              && p.skill().getAttribute(Attribute.STRENGTH, true)
                  * p.weapon().weapon().bashstrengthmod() > maxattack) {
            maxattack = (int) (p.skill().getAttribute(Attribute.STRENGTH, true) * p.weapon()
                .weapon().bashstrengthmod());
            maxp = p;
          }
        }
      }
      final int difficultyvalue = (int) (difficulty.value() / maxp.weapon().weapon()
          .bashstrengthmod());
      if (crowable || maxp.skill().attributeCheck(Attribute.STRENGTH, difficultyvalue)) {
        if (crowable) {
          ui().text(maxp.toString() + " uses a crowbar on the door!").add();
        } else if (maxp.weapon().weapon().bashstrengthmod() > 1) {
          ui().text(maxp.toString() + " smashes in the door!").add();
        } else {
          ui().text(maxp.toString() + " kicks in the door!").add();
        }
        getch();
        int timer = 5;
        if (crowable) {
          timer = 20;
        }
        if (i.site.alarmTimer() < 0 || i.site.alarmTimer() > timer) {
          i.site.alarmTimer(timer);
        } else {
          i.site.alarmTimer(0);
        }
        // Bashing doors in secure areas sets off alarms
        if (securityLevel != SecurityLevel.POOR) {
          i.site.alarm(true);
          ui().text("Alarms go off!").color(Color.RED).add();
          getch();
        }
        return SuccessTest.SUCCEED_NOISILY;
      }
      ui().text(maxp.toString() + " kicks the door!  It remains closed.").add();
      getch();
      if (i.site.alarmTimer() < 0) {
        i.site.alarmTimer(25);
      } else if (i.site.alarmTimer() > 10) {
        i.site.alarmTimer(i.site.alarmTimer() - 10);
      } else {
        i.site.alarmTimer(0);
      }
      return SuccessTest.FAIL_NOISILY;
    }
  };
  /** bash attempt */
  public abstract SuccessTest bash();

  /** Test whether there is a crowbar-like item on the ground.
   * @return true if so. */
  protected boolean crowbarInLoot() {
    for (final AbstractItem<? extends AbstractItemType> l : i.activeSquad().loot()) {
      if (l instanceof Weapon) {
        final Weapon w = (Weapon) l;
        if (w.autoBreaksLocks())
          return true;
      }
    }
    return false;
  }

  /** Check whether anyone on the team has a crowbar
   * @return true if so. */
  protected boolean teamHasCrowbar() {
    for (final Creature p : i.activeSquad()) {
      if (p.weapon().weapon().autoBreaksLocks())
        return true;
    }
    return false;
  }
}
