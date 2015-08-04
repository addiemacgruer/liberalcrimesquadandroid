package lcs.android.basemode.iface;

import lcs.android.creature.Creature;
import lcs.android.util.Color;
import lcs.android.util.Filter;
import lcs.android.util.IPredicate;

enum ReviewModes {
  LIBERALS("Active Liberals", Color.GREEN, Filter.ACTIVE_LIBERAL),
  HOSTAGES("Hostages", Color.RED, Filter.HOSTAGE),
  CLINIC("Hospital", Color.BLACK, Filter.IN_HOSPITAL),
  JUSTICE("Justice System", Color.YELLOW, Filter.IN_JAIL),
  SLEEPERS("Sleepers", Color.MAGENTA, Filter.SLEEPER),
  DEAD("The Dead", Color.BLACK, Filter.DEAD),
  AWAY("Away", Color.BLUE, Filter.AWAY);
  private ReviewModes(final String description, final Color color,
      final IPredicate<Creature> predicate) {
    this.description = description;
    this.color = color;
    this.predicate = predicate;
  }

  private final IPredicate<Creature> predicate;

  private final String description;

  private final Color color;

  public IPredicate<Creature> category() {
    return predicate;
  }

  public Color categoryColor() {
    return color;
  }

  @Override public String toString() {
    return description;
  }
}