package lcs.android.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class HashCodeBuilder implements IBuilder<Integer> {
  private int hashCode = 17;

  public HashCodeBuilder add(final boolean b) {
    hashCode += b ? 31 : 0;
    return this;
  }

  public HashCodeBuilder add(final double d) {
    final long bits = Double.doubleToLongBits(d);
    hashCode += 31 * (int) (bits ^ bits >> 32);
    return this;
  }

  public HashCodeBuilder add(final float f) {
    hashCode += 31 * Float.floatToIntBits(f);
    return this;
  }

  public HashCodeBuilder add(final int x) {
    hashCode += 31 * x;
    return this;
  }

  public HashCodeBuilder add(final long l) {
    hashCode += 31 * (int) (l ^ l >> 32);
    return this;
  }

  public HashCodeBuilder add(@Nullable final Object o) {
    if (o != null) {
      hashCode += 31 * o.hashCode();
    }
    return this;
  }

  @Override public Integer build() {
    return hashCode;
  }
}
