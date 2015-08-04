package lcs.android.items;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** The almighty buck. Presumably as a stack of $1 bills, the LCS can always make change. */
public @NonNullByDefault class Money extends AbstractItem<MoneyType> {
  public Money(final int amount) {
    super(MoneyType.instance());
    this.amount = amount;
  }

  private int amount;

  @Override public String equipTitle() {
    return "$" + amount;
  }

  @Override public int fenceValue() {
    return amount * number;
  }

  /** The amount of money we're talking about.
   * @return dollars */
  public int getAmount() {
    return amount;
  }

  @Override public boolean isEmpty() {
    return amount == 0;
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> i) {
    flatten();
    if (i instanceof Money) {
      final Money m = (Money) i; // cast -XML
      m.flatten();
      amount += m.amount;
      m.amount = 0;
      m.number = 0;
      return true;
    }
    return false;
  }

  @Override public Money split(final int aNumber) {
    flatten();
    int lNumber = aNumber;
    if (lNumber > amount) {
      lNumber = amount;
    }
    final Money newi = new Money(0);
    newi.amount = lNumber;
    amount -= lNumber;
    return newi;
  }

  @Override public String toString() {
    return "Money";
  }

  private void flatten() {
    amount *= number;
    number = 1;
  }

  private static final long serialVersionUID = Game.VERSION;
}
