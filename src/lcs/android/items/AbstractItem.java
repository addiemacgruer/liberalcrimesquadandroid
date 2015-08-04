package lcs.android.items;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.game.Game;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** definite item(s) of some type. Keeps track of the count of items in the pile; subclasses may also
 * have other quality modifiers. */
abstract public @NonNullByDefault class AbstractItem<AIT extends AbstractItemType> implements
    Serializable {
  protected AbstractItem(final AIT platonicIdeal) {
    this.platonicIdeal = platonicIdeal;
  }

  /** The count of items in this pile. */
  protected int number = 1;

  /** The kind of items in this pile (<a
   * href="http://en.wikipedia.org/wiki/Platonic_idealism">Platonic Ideal</a>) */
  protected final AIT platonicIdeal;

  /** Reduces this pile of items by some amount
   * @param decrease the amount to decrease. */
  public void decrease(final int decrease) {
    number -= decrease;
  }

  /** Display a help screen for the item */
  public void displayStats(final int viewID) {
    platonicIdeal.displayStats(viewID);
  }

  /** Returns the long name for this item, used when equipping
   * @return The long human-readable name. */
  abstract public String equipTitle();

  /** How many $$$ you get for selling this item.
   * @return dollars */
  public int fenceValue() {
    return platonicIdeal.fencevalue;
  }

  /** The unique ID for an item of this type. Generally all upper-cased, and starting with an
   * indicator of what type it is: ARMOR_, CLIP_, LOOT_, MONEY_, VEHICLE, WEAPON_.
   * @return unique ID. */
  public String id() {
    return platonicIdeal.idname;
  }

  /** Returns the AbstractItemType (the Platonic ideal) for the item
   * @return the ideal. */
  public AIT ideal() {
    return platonicIdeal;
  }

  /** whether an item is used up.
   * @return true if number == 0 */
  public boolean isEmpty() {
    return number == 0;
  }

  /** whether this item is sellable. Generally yes, although sub-types may vary depending on the
   * current laws of the land.
   * @return true generically; sub-classes may change */
  public boolean isGoodForSale() {
    return true;
  }

  /** if the other item is the same, move all instances from that to this and leave the other empty.
   * @param other another item
   * @return true if the item was merged, false otherwise. */
  abstract public boolean merge(AbstractItem<? extends AbstractItemType> other);

  /** The current count of this item
   * @return number */
  public int number() {
    return number;
  }

  /** Whether the abstract type ID is equal to a given ID.
   * @param id ID to check against, can be null.
   * @return whether the types are equal. */
  public boolean ofType(final String id) {
    return id().equals(id);
  }

  /** Splits these items into two piles, creating a new item in the process. The number of items in
   * this pile will be decreased.
   * @param split how many to remove
   * @return the new item */
  abstract public AbstractItem<AIT> split(int split);

  /** Returns whether this item has the same Platonic ideal as another.
   * @param ai another item
   * @return true if the item is the same type (not necessarily {@link #merge} able) */
  protected boolean isSameType(final AbstractItem<? extends AbstractItemType> ai) {
    return platonicIdeal == ai.platonicIdeal;
  }

  final static private Comparator<AbstractItem<? extends AbstractItemType>> ALPHABETICALLY = new Comparator<AbstractItem<? extends AbstractItemType>>() {
    @Override public int compare(final @Nullable AbstractItem<? extends AbstractItemType> object1,
        final @Nullable AbstractItem<? extends AbstractItemType> object2) {
      assert object1 != null;
      assert object2 != null;
      return object1.toString().compareTo(object2.toString());
    }
  };

  private static final long serialVersionUID = Game.VERSION;

  /** merge all of the items in the given list if possible.
   * @param loot a list to merge */
  public static void consolidateloot(final List<AbstractItem<? extends AbstractItemType>> loot) {
    for (int j = 0, k = loot.size(); j < k; j++) {
      for (int l = j + 1; l < k; l++) {
        loot.get(j).merge(loot.get(l));
      }
    }
    for (final Iterator<AbstractItem<? extends AbstractItemType>> li = loot.iterator(); li
        .hasNext();) {
      if (li.next().isEmpty()) {
        li.remove();
      }
    }
    Collections.sort(loot, ALPHABETICALLY);
  }

  /** Brings up a prompt screen to equip the i.activesquad with a selection of items: this will
   * typically be the i.activesquad.loot. Redraws the screen and will need clearing afterwards.
   * @param loot The items to select from
   * @param loc The location of the squad */
  public static void equip(final List<AbstractItem<? extends AbstractItemType>> loot,
      @Nullable final Location loc) {
    // if (i.activeSquad == null) {
    // return;
    // }
    consolidateloot(loot);
    if (loc != null) {
      consolidateloot(loc.lcs().loot);
    }
    String errmsg = null;
    int c;
    do {
      setView(R.layout.generic);
      ui().text("Equip the Squad").add();
      int y = 'a';
      for (final Creature p : i.activeSquad) {
        ui(R.id.gcontrol).button(y++).text(p.toString()).add();
      }
      ui(R.id.gcontrol).button(10).text("Continue the struggle.").add();
      c = getch();
      if (c == 10) {
        return;
      }
      if (c < 'a') {
        continue;
      }
      clearChildren(R.id.gcontrol);
      final Creature squaddie = i.activeSquad.member(c - 'a');
      do {
        setView(R.layout.generic);
        ui().text("Equipping " + squaddie.toString()).bold().add();
        ui().text("Wearing: " + squaddie.getArmor().equipTitle()).color(Color.CYAN).add();
        squaddie.getArmor().displayStats(R.id.gmessages);
        ui().text("Carrying: " + (squaddie.weapon().weapon().equipTitle())).color(Color.CYAN).add();
        if (squaddie.weapon().weapon() != Weapon.none()) {
          squaddie.weapon().weapon().displayStats(R.id.gmessages);
        }
        ui().text(
            "Clips: " + squaddie.weapon().countClips() + squaddie.weapon().clipType().toString())
            .color(Color.CYAN).add();
        // TODO clip stats
        if (errmsg != null) {
          ui(R.id.gcontrol).text(errmsg).color(Color.RED).add();
          errmsg = null;
        }
        ui(R.id.gcontrol).text("Choose an item to equip.").add();
        y = 'a';
        for (final AbstractItem<? extends AbstractItemType> l : loot) {
          String s = l.equipTitle();
          if (l.number() > 1) {
            s += " x" + l.number();
          }
          if (l instanceof Weapon || l instanceof Armor || l instanceof Clip) {
            ui(R.id.gcontrol).button(y++).text(s).add();
          } else {
            ui(R.id.gcontrol).button().text(s).add();
            y++;
          }
        }
        ui(R.id.gcontrol).text("Other actions:").add();
        maybeAddButton(R.id.gcontrol, 11, "Drop " + squaddie.toString() + "'s Conservative weapon",
            squaddie.weapon().weapon().platonicIdeal != Weapon.none().platonicIdeal);
        maybeAddButton(R.id.gcontrol, 12, "Liberally Strip " + squaddie.toString(),
            !squaddie.isNaked());
        maybeAddButton(R.id.gcontrol, 13, "Change ammo allocation", squaddie.weapon().weapon()
            .usesAmmo());
        if (loc != null) {
          maybeAddButton(R.id.gcontrol, 14, "Get things from " + loc.toString(),
              loc.lcs().loot.size() > 0);
          maybeAddButton(R.id.gcontrol, 15, "Stash things at " + loc.toString(), loot.size() > 0);
        }
        ui(R.id.gcontrol).button(ENTER).text("Done").add();
        c = getch();
        clearChildren(R.id.gcontrol);
        final boolean increaseammo = false, decreaseammo = false;
        if (c >= 'a') {
          AbstractItem<? extends AbstractItemType> slot = loot.get(c - 'a');
          if (!(slot instanceof Weapon) && !(slot instanceof Armor) && !(slot instanceof Clip)) {
            errmsg = "You can't equip that.";
            continue;
          }
          if (decreaseammo) {
            consolidateloot(loot);
            if (!squaddie.weapon().weapon().usesAmmo()) {
              errmsg = "No ammo to drop!";
              continue;
            }
            errmsg = "No spare clips!";
            continue;
          }
          if (increaseammo) {
            if (!squaddie.weapon().isArmed() || !squaddie.weapon().weapon().usesAmmo()) {
              errmsg = "No ammo required!";
              continue;
            }
            slot = null;
            for (final AbstractItem<? extends AbstractItemType> sl : loot) {
              if (sl instanceof Clip && squaddie.weapon().weapon().acceptableAmmo(sl)) {
                slot = sl;
                break;
              } else if (sl instanceof Weapon && sl.isSameType(squaddie.weapon().weapon())) // For
              // throwing
              // weapons.
              // -XML
              {
                final Weapon w = (Weapon) sl; // cast -XML
                if (w.isThrowable()) {
                  slot = sl;
                  break;
                }
              }
            }
            if (slot == null) {
              errmsg = "No ammo available!";
              continue;
            }
          }
          final int armok = squaddie.health().armCount();
          if (slot instanceof Weapon && armok > 0) {
            final Weapon w = (Weapon) slot; // cast -XML
            squaddie.weapon().giveWeapon(w, loot);
            if (slot.isEmpty()) {
              loot.remove(slot);
            }
          } else if (slot instanceof Armor) {
            final Armor a = (Armor) slot; // cast -XML
            squaddie.giveArmor(a, loot);
            if (slot.isEmpty()) {
              loot.remove(slot);
            }
          } else if (slot instanceof Clip && armok > 0) {
            final int space = 9 - squaddie.weapon().countClips();
            if (!squaddie.weapon().isArmed()) {
              errmsg = "Can't carry ammo without a gun.";
              continue;
            } else if (!squaddie.weapon().weapon().acceptableAmmo(slot)) {
              errmsg = "That ammo doesn't fit.";
              continue;
            } else if (space < 1) {
              errmsg = "Can't carry any more ammo.";
              continue;
            } else {
              int amount = 1;
              if (slot.number() > 1) {
                if (increaseammo) {
                  amount = 1;
                } else {
                  amount = AbstractItem.promptAmount(0, Math.min(slot.number(), space));
                }
              }
              squaddie.weapon().takeClips((Clip) slot, amount);
              if (slot.isEmpty()) {
                loot.remove(slot);
              }
            }
            consolidateloot(loot);
          }
        }
        if (c == 12) {
          if (!squaddie.isNaked()) {
            squaddie.strip(loot);
            consolidateloot(loot);
          }
        }
        if (c == ENTER) {
          break;
        }
        if (loc != null) {
          if (c == 14 && loc.lcs().loot.size() > 0) {
            AbstractItem.moveloot(loc.lcs().loot, loot);
          }
          if (c == 15 && loot.size() > 0) {
            AbstractItem.moveloot(loot, loc.lcs().loot);
          }
        }
        if (c == 11) {
          if (squaddie.weapon().isArmed()) {
            squaddie.weapon().dropWeaponsAndClips(loot);
            consolidateloot(loot);
          }
        }
      } while (true);
    } while (true);
  }

  /** Number query. Redraws the screen.
   * @param min
   * @param max
   * @return the user's choice between the parameter range. */
  public static int promptAmount(final int min, final int max) {
    int amount = numberquery("How many? (" + min + "-" + max + ")", max);
    amount = Math.max(amount, min);
    amount = Math.min(amount, max);
    return amount;
  }

  /** Brings up a prompt screen to move loot between two lists: typically the location.lcs.loot for
   * two places. Redraws the screen.
   * @param source where from
   * @param dest where to. */
  private static void moveloot(final List<AbstractItem<? extends AbstractItemType>> source,
      final List<AbstractItem<? extends AbstractItemType>> dest) {
    do {
      setView(R.layout.generic);
      ui(R.id.gcontrol).text("Choose an item to move.").add();
      int y = 'a';
      for (final AbstractItem<? extends AbstractItemType> l : source) {
        String s = l.equipTitle();
        if (l.number() > 1) {
          s += " x" + l.number();
        }
        ui(R.id.gcontrol).button(y++).text(s).add();
      }
      ui(R.id.gcontrol).button(10).text("Done.").add();
      final int c = getch();
      if (c == 10) {
        return;
      }
      final int choice = c - 'a';
      if (choice < 0 || choice >= source.size()) {
        continue; // this AIOBE'd. Presumably someone used the on-screen
      }
      // keyboard to enter an invalid value?
      final AbstractItem<? extends AbstractItemType> slot = source.get(choice);
      if (slot.number() == 1) {
        source.remove(slot);
        dest.add(slot);
      } else {
        final int amount = AbstractItem.promptAmount(0, slot.number());
        final AbstractItem<? extends AbstractItemType> split = slot.split(amount);
        dest.add(split);
        if (slot.isEmpty()) {
          source.remove(slot);
        }
      }
      consolidateloot(dest);
    } while (true);
  }
}
