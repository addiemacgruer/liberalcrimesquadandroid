package lcs.android.scoring;

import static lcs.android.game.Game.*;

import java.io.Serializable;

import lcs.android.game.Game;
import lcs.android.util.HashCodeBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class LcsDate implements Comparable<LcsDate>, Serializable {
  LcsDate(final int day, final int month, final int year) {
    this.day = day;
    this.month = month;
    this.year = year;
  }

  private int day;

  private int month;

  private int year;

  public String calendar() {
    final StringBuilder str = new StringBuilder();
    str.append("\nSMTWTFS\n\n");
    int j = 0;
    final int tonderOffset = tonderingAlgorithm();
    for (; j < tonderOffset; j++) {
      str.append(' ');
    }
    for (; j < i.score.date.day() + tonderOffset; j++) {
      str.append('X');
      if (j % 7 == 6) {
        str.append('\n');
      }
    }
    final int monthdays = i.score.date.daysInMonth() + tonderOffset;
    for (; j < monthdays; j++) {
      str.append('-');
      if (j % 7 == 6) {
        str.append('\n');
      }
    }
    while (j % 7 != 6) {
      str.append(' ');
      j++;
    }
    str.append(' ');
    return str.toString();
  }

  @Override public int compareTo(final @Nullable LcsDate another) {
    assert another != null;
    if (year != another.year)
      return year - another.year;
    if (month != another.month)
      return month - another.month;
    return day - another.day;
  }

  public boolean dateEquals(final LcsDate date) {
    return month == date.month && day == date.day;
  }

  public int day() {
    return day;
  }

  public LcsDate day(final int aDay) {
    day = aDay;
    return this;
  }

  public int daysInMonth() {
    return daysInMonth(month, year);
  }

  @Override public boolean equals(final @Nullable Object o) {
    if (o == this)
      return true;
    if (!(o instanceof LcsDate))
      return false;
    final LcsDate odate = (LcsDate) o;
    return day == odate.day && month == odate.month && year == odate.year;
  }

  @Override public int hashCode() {
    return new HashCodeBuilder().add(day).add(month).add(year).build();
  }

  public boolean isMonthEnd() {
    return day >= daysInMonth();
  }

  public boolean isMonthOver() {
    return day > daysInMonth();
  }

  /** Long format of the date (Jan 1, 2015)
   * @return the date as a string */
  public String longString() {
    return monthName() + ' ' + day + ", " + year;
  }

  public int month() {
    return month;
  }

  public LcsDate month(final int aMonth) {
    month = aMonth;
    return this;
  }

  public String monthName() {
    return monthName(month);
  }

  public void nextDay() {
    day++; // end-of-month handled elsewhere
  }

  public void nextMonth() {
    day = 1;
    month++;
    if (month == 13) {
      month = 1;
      year++;
    }
  }

  @Override public String toString() {
    return month + "/" + day + "/" + year;
  }

  public int year() {
    return year;
  }

  public LcsDate year(final int aYear) {
    year = aYear;
    return this;
  }

  public int yearsTo(final LcsDate date) {
    int years = date.year - year;
    if (date.month < month || date.month == month && date.day <= day) {
      years++;
    }
    return years;
  }

  private int tonderingAlgorithm() {
    final int d = 1; // we want to know the offset of the first day
    final int m = month;
    final int y = m < 3 ? year - 1 : year;
    final int w = (y + y / 4 - y / 100 + y / 400 + tondering[m - 1] + d) % 7;
    return (w >= 0) ? w : w + 7;
  }

  private static final String[] MONTHS = { "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December" };

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  private final static int[] tondering = { 0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4 };

  public static void monthName(final StringBuilder sb, final int month) {
    sb.append(MONTHS[month]);
  }

  public static LcsDate withAge(final int age) {
    final int month = i.rng.nextInt(12) + 1;
    final int day = i.rng.nextInt(daysInMonth(month, 2001)); // not a leap year
    int year = i.score.date.year - age;
    /* have they had their birthday yet this year? */
    if (i.score.date.month < month || i.score.date.month == month && i.score.date.day < day) {
      year++;
    }
    return new LcsDate(day, month, year);
  }

  private static int daysInMonth(final int month, final int year) {
    switch (month) {
    case 4:
    case 6:
    case 9:
    case 11:
      return 30;
    case 2:
      if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
        return 29;
      return 28;
    default:
      return 31;
    }
  }

  private static String monthName(final int month) {
    return MONTHS[month - 1];
  }
}
