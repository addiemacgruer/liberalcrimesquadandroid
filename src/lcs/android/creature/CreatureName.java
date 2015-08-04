package lcs.android.creature;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;

import lcs.android.R;
import lcs.android.creature.health.Animal;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** Stores the names of creatures: they're chosen from a list, and can be serialised by number alone.
 * @author addie */
public @NonNullByDefault class CreatureName implements Serializable {
  /** Returns a human name of random gender. */
  public CreatureName() {
    this(i.rng.choice(Gender.MALE, Gender.FEMALE), Animal.HUMAN);
  }

  /** Returns a random name of specified gender and creature.
   * @param gender
   * @param animal */
  public CreatureName(final Gender gender, final Animal animal) {
    this.gender = gender;
    switch (animal) {
    case HUMAN:
    default:
      first = i.rng.nextInt(stringArray(arrayForGender(gender)).length);
      last = i.rng.nextInt(stringArray(R.array.lastname).length);
      break;
    case ANIMAL:
      first = i.rng.nextInt(stringArray(arrayForGender(gender)).length);
      last = -1;
      break;
    case TANK:
      first = last = -1;
      break;
    }
  }

  /** Creates a CreatureName with the given first and last name. Used for the character creation
   * choice screen. Will overflow if missing names are passed in.
   * @param gender
   * @param first
   * @param last */
  public CreatureName(final Gender gender, final String first, final String last) {
    Log.v("LCS", "new CreatureName:" + gender + "," + first + "," + last);
    int mFirst = 0, mLast = 0;
    final String[] firsts = stringArray(arrayForGender(gender));
    final String[] lasts = stringArray(R.array.lastname);
    while (!firsts[mFirst].equals(first)) {
      mFirst++;
    }
    while (!lasts[mLast].equals(last)) {
      mLast++;
    }
    this.gender = gender;
    this.first = mFirst;
    this.last = mLast;
  }

  private final int first;

  private final int last;

  private final Gender gender;

  @Override public String toString() {
    if (first == -1)
      return "Tank";
    final StringBuilder name = new StringBuilder();
    if (first != -1) {
      name.append(stringArray(arrayForGender(gender))[first]);
    }
    name.append(' ');
    if (last != -1) {
      name.append(stringArray(R.array.lastname)[last]);
    }
    return name.toString();
  }

  private static final long serialVersionUID = Game.VERSION;

  /** gets a random first name of random gender */
  public static String firstName() {
    return firstName(i.rng.choice(Gender.MALE, Gender.FEMALE));
  }

  /** gets a random first name */
  public static String firstName(final Gender gender) {
    return randomString(arrayForGender(gender));
  }

  /** gets a random name of random gender */
  public static String generateName() {
    return generateName(i.rng.choice(Gender.MALE, Gender.FEMALE));
  }

  /** fills a string with a proper name */
  public static String generateName(final Gender gender) {
    return firstName(gender) + " " + lastname();
  }

  /** gets a random last name */
  public static String lastname() {
    return randomString(R.array.lastname);
  }

  /** @param gender which gender, not null
   * @return the android array for this gender */
  private static int arrayForGender(final Gender gender) {
    switch (gender) {
    case MALE:
      return R.array.male;
    case FEMALE:
      return R.array.female;
    case NEUTRAL:
    default:
      return R.array.neutral;
    case WHITEMALEPATRIARCH:
      return R.array.whitemalepatriarch;
    }
  }
}
