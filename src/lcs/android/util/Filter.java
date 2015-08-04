package lcs.android.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.skill.Skill;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Loot;
import lcs.android.politics.Alignment;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Filter {
  public static final IPredicate<Creature> ACTIVE = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return c.health().alive() && c.alignment() == Alignment.LIBERAL && c.hiding() == 0
          && !c.hasFlag(CreatureFlag.SLEEPER);
    }
  };

  public static final IPredicate<Creature> ALL = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return true;
    }
  };

  /** Alive, liberal, not in the clinic, dating, hiding; a sleeper, or in jail... */
  public static final IPredicate<Creature> AVAILABLE = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return p.health().alive() && p.alignment() == Alignment.LIBERAL
          && p.health().clinicMonths() == 0 && p.datingVacation() == 0 && p.hiding() == 0
          && !p.hasFlag(CreatureFlag.SLEEPER) && p.location().exists()
          && !p.location().get().type().isPrison();
    }
  };

  public static final IPredicate<AbstractItem<? extends AbstractItemType>> IS_LOOT = new IPredicate<AbstractItem<? extends AbstractItemType>>() {
    @Override public boolean apply(final AbstractItem<? extends AbstractItemType> item) {
      return item instanceof Loot;
    }
  };

  public static final IPredicate<Location> LCS_LOCATION = new IPredicate<Location>() {
    @Override public boolean apply(final Location item) {
      return item.renting() == CrimeSquad.LCS;
    }
  };

  public static final IPredicate<Creature> ACTIVE_LIBERAL = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return p.health().alive() && p.alignment() == Alignment.LIBERAL
          && p.health().clinicMonths() == 0 && p.datingVacation() == 0 && p.hiding() == 0
          && !p.hasFlag(CreatureFlag.SLEEPER) && p.location().exists()
          && !p.location().get().type().isPrison();
    }
  };

  public static final IPredicate<Creature> LIBERAL = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return c.health().alive() && c.alignment() == Alignment.LIBERAL;
    }
  };

  public static final IPredicate<Creature> LIVING = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return c.health().alive();
    }
  };

  public static final IPredicate<Creature> DEAD = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return !c.health().alive();
    }
  };

  public static final IPredicate<Creature> SLEEPER = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature c) {
      return c.health().alive() && c.hasFlag(CreatureFlag.SLEEPER);
    }
  };

  public static final INumberTest<Creature> SPEED = new INumberTest<Creature>() {
    @Override public int valueOf(final Creature t) {
      return t.speed();
    }
  };

  public static final IPredicate<Creature> AWAY = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return ((p.datingVacation() > 0 || p.hiding() != 0) && p.health().alive());
    }
  };

  public static final IPredicate<Creature> IN_JAIL = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return !p.hasFlag(CreatureFlag.SLEEPER) && p.health().alive()
          && p.location().get().type().isPrison();
    }
  };

  public static final IPredicate<Creature> IN_HOSPITAL = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return p.health().clinicMonths() > 0 && p.health().alive();
    }
  };

  public static final IPredicate<Creature> HOSTAGE = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature p) {
      return p.alignment() != Alignment.LIBERAL && p.health().alive();
    }
  };

  public static final IPredicate<Location> HAS_LOOT = new IPredicate<Location>() {
    @Override public boolean apply(final Location item) {
      return !item.lcs().loot.isEmpty();
    }
  };

  public static final IPredicate<Creature> HAS_SQUAD = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature item) {
      return item.squad().exists();
    }
  };

  public static final IPredicate<Creature> IS_CONSERVATIVE = new IPredicate<Creature>() {
    @Override public boolean apply(final Creature item) {
      return item.health().alive() && item.alignment() == Alignment.CONSERVATIVE;
    }
  };

  /** Tests whether any of the items in the collection match a condition.
   * @param collection
   * @param test
   * @return */
  public static <T> boolean any(final Collection<T> collection, final IPredicate<T> test) {
    for (final T item : collection) {
      if (test.apply(item)) {
        return true;
      }
    }
    return false;
  }

  public static INumberTest<Creature> attribute(final Attribute attribute, final boolean useJuice) {
    return new INumberTest<Creature>() {
      @Override public int valueOf(final Creature t) {
        return t.skill().getAttribute(attribute, useJuice);
      }
    };
  }

  public static <T> Maybe<T> best(final Collection<T> pool, final INumberTest<T> test) {
    if (pool.size() == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty pool");
    }
    T best = null;
    int highest = 0;
    boolean first = true;
    for (final T t : pool) {
      if (first) {
        first = false;
        highest = test.valueOf(t);
        best = t;
        continue;
      }
      if (test.valueOf(t) > highest) {
        best = t;
        highest = test.valueOf(t);
      }
    }
    return Maybe.ofNullable(best);
  }

  /** Count the number of things which match a condition in a collection.
   * @param collection
   * @param test
   * @return */
  public static <T> int count(final Collection<T> collection, final IPredicate<T> test) {
    int rval = 0;
    for (final T item : collection) {
      if (test.apply(item)) {
        rval++;
      }
    }
    return rval;
  }

  /** Scans through a collection for the highest value
   * @param collection to scan
   * @param test to apply
   * @return the highest value, or 0 if collection empty */
  public static <T> int highest(final Collection<T> collection, final INumberTest<T> test) {
    if (collection.isEmpty()) {
      return 0;
    }
    int rval = 0;
    boolean first = true;
    for (final T item : collection) {
      if (first) {
        rval = test.valueOf(item);
        first = false;
      } else {
        rval = Math.max(rval, test.valueOf(item));
      }
    }
    return rval;
  }

  /** all of the creatures in a location who are alive.
   * @return a filtered copy of the original list, (the original can then be modified). */
  public static IPredicate<Creature> livingIn(final Location location) {
    return new IPredicate<Creature>() {
      @Override public boolean apply(final Creature c) {
        return c.health().alive() && c.location().getNullable() == location;
      }
    };
  }

  /** Scans through a collection for the lowest value
   * @param collection to scan
   * @param test to apply
   * @return the lowest value, or 0 if collection empty */
  public static <T> int lowest(final Collection<T> collection, final INumberTest<? super T> test) {
    if (collection.isEmpty()) {
      return 0;
    }
    int rval = 0;
    boolean first = true;
    for (final T item : collection) {
      if (first) {
        rval = test.valueOf(item);
        first = false;
      } else {
        rval = Math.min(rval, test.valueOf(item));
      }
    }
    return rval;
  }

  /** all of the living liberals managed by a given creature
   * @return a filtered copy of the original list, (the original can then be modified). */
  public static IPredicate<Creature> managedBy(final Creature manager) {
    return new IPredicate<Creature>() {
      @Override public boolean apply(final Creature c) {
        return c.health().alive() && c.hire().getNullable() == manager;
      }
    };
  }

  /** Returns a copy of the collection as a list, containing only items that match a condition.
   * @param collection
   * @param test
   * @return */
  public static <T> List<T> of(final Collection<T> collection, final IPredicate<T> test) {
    final List<T> filtered = new ArrayList<T>(collection.size());
    for (final T c : collection) {
      if (test.apply(c)) {
        filtered.add(c);
      }
    }
    return filtered;
  }

  public static IPredicate<Location> rented(final CrimeSquad cs) {
    return new IPredicate<Location>() {
      @Override public boolean apply(final Location item) {
        return item.renting() == cs;
      }
    };
  }

  public static INumberTest<Creature> skill(final Skill skill) {
    return new INumberTest<Creature>() {
      @Override public int valueOf(final Creature t) {
        return t.skill().skill(skill);
      }
    };
  }

  /** @return */
  public static <N extends Number> INumberTest<N> value() {
    return new INumberTest<N>() {
      @Override public int valueOf(final N t) {
        return t.intValue();
      }
    };
  }

  /** Returns a copy of the collection as a list, containing only items that don't match a condition.
   * @param collection
   * @param test
   * @return */
  public static <T> List<T> without(final Collection<T> collection, final IPredicate<T> test) {
    final List<T> filtered = new ArrayList<T>(collection.size());
    for (final T c : collection) {
      if (!test.apply(c)) {
        filtered.add(c);
      }
    }
    return filtered;
  }
}
