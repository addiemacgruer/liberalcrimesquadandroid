package lcs.android.creature.skill;

import lcs.android.creature.Creature;
import lcs.android.util.Curses;
import lcs.android.util.HashCodeBuilder;
import lcs.android.util.UIElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class SkillTag implements Comparable<SkillTag> {
  public SkillTag(final String string, final int sv) {
    text = string;
    value = sv;
    maxxed = false;
  }

  public SkillTag(final String skill, final int major, final int minor, final int sortvalue) {
    if (minor > 100) {
      text = String.format("%s: %d.99+", skill, major);
    } else {
      text = String.format("%s: %d.%02d", skill, major, minor);
    }
    maxxed = false;
    value = sortvalue;
  }

  public SkillTag(final String skill, final int major, final int minor, final int sortvalue,
      final int max) {
    if (major >= max) { /* it is possible to lose attributes, and thus 'max' value is less than
                         * current */
      text = String.format("%s: (%d.00 / %d.00)", skill, major, max);
      maxxed = true;
    } else if (minor > 100) {
      text = String.format("%s: (%d.99+ / %d.00)", skill, major, max);
      maxxed = false;
    } else {
      text = String.format("%s: (%d.%02d / %d.00)", skill, major, minor, max);
      maxxed = false;
    }
    value = sortvalue;
  }

  public final String text;

  public final int value;

  private final boolean maxxed;

  public void addToView(final int viewID, final Creature c) {
    final UIElement.UIBuilder builder = Curses.ui(viewID).text(text).narrow();
    if (maxxed) {
      builder.color(c.alignment().color());
    }
    builder.add();
  }

  @Override public int compareTo(@Nullable final SkillTag another) {
    assert another != null;
    if (another.value != value)
      return another.value - value;
    return text.compareTo(another.text);
  }

  @Override public boolean equals(final @Nullable Object o) {
    if (o == this)
      return true;
    if (!(o instanceof SkillTag))
      return false;
    final SkillTag other = (SkillTag) o;
    return (value == other.value) && (maxxed == other.maxxed) && text.equals(other.text);
  }

  @Override public int hashCode() {
    return new HashCodeBuilder().add(value).add(maxxed).add(text).build();
  }
}