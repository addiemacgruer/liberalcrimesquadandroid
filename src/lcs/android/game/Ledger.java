package lcs.android.game;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.Map;

import lcs.android.R;
import lcs.android.util.Color;
import lcs.android.util.DefaultValueKey;
import lcs.android.util.SparseMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** The LCS's bank statements. */
public @NonNullByDefault class Ledger implements Serializable { // NO_UCD
  /** Things we spend money on. */
  public enum ExpenseType implements DefaultValueKey<Integer> {
    CARS,
    COMPOUND,
    CONFISCATED,
    DATING,
    FOOD,
    HOSTAGE,
    LEGAL,
    MANUFACTURE,
    RECRUITMENT,
    RENT,
    SHOPPING,
    SKETCHES,
    TRAINING,
    TROUBLEMAKING,
    TSHIRTS;
    @Override public Integer defaultValue() {
      return Ledger.DELNADA;
    }
  }

  /** Things we obtain money from. */
  public enum IncomeType implements DefaultValueKey<Integer> {
    BROWNIES,
    BUSKING,
    CARS,
    CCFRAUD,
    DONATIONS,
    EMBEZZLEMENT,
    EXTORTION,
    HUSTLING,
    PAWN,
    PROSTITUTION,
    RECRUITS,
    SKETCHES,
    THIEVERY,
    TSHIRTS;
    @Override public Integer defaultValue() {
      return Ledger.DELNADA;
    }
  }

  private final Map<ExpenseType, Integer> expenses = SparseMap.of(ExpenseType.class);

  private int funds = 7;

  private final Map<IncomeType, Integer> incomes = SparseMap.of(IncomeType.class);

  /** Add funds to the ledger.
   * @param amount How much
   * @param it How it was obtained. */
  public void addFunds(final int amount, final IncomeType it) {
    funds += amount;
    incomes.put(it, incomes.get(it) + amount);
    i.score.funds += amount;
  }

  /** Sets our balance to a given amount, id est at the start of the game.
   * @param amount some dollars */
  public void forceFunds(final int amount) {
    funds = amount;
  }

  /** How much we have to spend, right now.
   * @return some dollars */
  public int funds() {
    return funds;
  }

  /** Spend some funds
   * @param amount some dollars
   * @param et what it went on */
  public void subtractFunds(final int amount, final ExpenseType et) {
    funds -= amount;
    expenses.put(et, expenses.get(et) + amount);
    i.score.spent += amount;
  }

  private boolean anythingToReport() {
    for (final int income : expenses.values()) {
      if (income != 0)
        return true;
    }
    for (final int income : incomes.values()) {
      if (income != 0)
        return true;
    }
    return false;
  }

  private void clearLedgers() {
    incomes.clear();
    expenses.clear();
  }

  private final static Integer DELNADA = Integer.valueOf(0);

  private static final long serialVersionUID = Game.VERSION;

  /** Displays the LCS finances report (only if there's anything to report). Redraws the screen if we
   * made or spent any money this month. */
  public static void fundReport() {
    if (i.disbanding())
      return;
    if (!i.ledger.anythingToReport())
      return;
    setView(R.layout.generic);
    ui().text("Liberal Crime Squad:   Monthly Action Report").bold().add();
    int totalmoney = 0;
    for (final IncomeType it : IncomeType.values()) {
      totalmoney += i.ledger.incomes.get(it);
      if (i.ledger.incomes.get(it) != 0) {
        ui().text("$" + i.ledger.incomes.get(it) + ": " + it).color(Color.GREEN).add();
      }
    }
    for (final ExpenseType et : ExpenseType.values()) {
      totalmoney -= i.ledger.expenses.get(et);
      if (i.ledger.expenses.get(et) != 0) {
        ui().text("$" + i.ledger.expenses.get(et) + ": " + et).color(Color.RED).add();
      }
    }
    i.ledger.clearLedgers();
    ui().text("Total: $" + totalmoney).color(totalmoney >= 0 ? Color.GREEN : Color.RED).add();
    ui(R.id.gcontrol).button(' ').text("Reflect on the report.").add();
    getch();
  }
}