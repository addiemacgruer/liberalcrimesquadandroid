package lcs.android.creature.crime;

import lcs.android.law.Crime;
import lcs.android.util.Curses;
import lcs.android.util.HashCodeBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class CrimeTag implements Comparable<CrimeTag> {
  public CrimeTag(final Crime l, final int c) {
    law = l;
    count = c;
  }

  private final int count;

  private final Crime law;

  public void addToView(final int gcrimes) {
    Curses.ui(gcrimes).text(toString()).narrow().add();
  }

  @Override public int compareTo(@Nullable final CrimeTag another) {
    assert another != null;
    if (another.count != count) {
      return another.count - count;
    }
    return law.toString().compareTo(another.law.toString());
  }

  @Override public boolean equals(final @Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CrimeTag)) {
      return false;
    }
    final CrimeTag other = (CrimeTag) o;
    if (count != other.count) {
      return false;
    }
    if (!law.equals(other.law)) {
      return false;
    }
    return true;
  }

  @Override public int hashCode() {
    return new HashCodeBuilder().add(count).add(law).build();
  }

  @Override public String toString() {
    return law + ": " + count;
  }
}