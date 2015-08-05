package lcs.android.news;

import static lcs.android.game.Game.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.game.Game;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class NewsStory implements Serializable, Comparable<NewsStory> {
  public NewsStory(final StoryType type) {
    this.type = type;
  }

  public int count; // this is crime[1]

  public boolean positive;

  @Nullable public CrimeSquad siegetype; // this is crime[0]

  protected final List<NewsEvent> crimes = new ArrayList<NewsEvent>();

  protected StoryType type;

  int page = 1;

  int priority;

  @Nullable Issue view;

  private boolean claimed = true;

  @Nullable private Creature creature;

  private Location location = Location.none();

  private int politicsLevel = 0; // TODO

  private int violenceLevel = 0; // TODO

  public NewsStory addNews(final NewsEvent aCrime) {
    crimes.add(aCrime);
    return this;
  }

  public NewsStory claimed(final boolean aClaimed) {
    claimed = aClaimed;
    return this;
  }

  /** we define compareTo so that we can sort new stories more easily: we're only interested in the
   * day's top story anyway. FindBugs complains that we still use Object.equals, but that's alright:
   * even if the events were precisely the same, news stories are unique. */
  @Override public int compareTo(final @Nullable NewsStory another) {
    assert another != null;
    return page - another.page;
  }

  public NewsStory creature(final Creature aCreature) {
    creature = aCreature;
    return this;
  }

  public int crimeCount() {
    return crimes.size();
  }

  public boolean isClaimed() {
    return claimed;
  }

  public Location location() {
    return location;
  }

  public NewsStory location(final Location aLocation) {
    location = aLocation;
    return this;
  }

  public NewsStory type(final StoryType aType) {
    type = aType;
    return this;
  }

  Creature creature() {
    return creature != null ? creature : CreatureType.withType(i.rng.randFromMap(i.type.creature));
  }

  /** Priority is set differently based on the type of the news story */
  void setpriority() {
    switch (type) {
    // Major events always muscle to the front page by having a very high
    // priority
    case MAJOREVENT:
      priority = 30000;
      break;
    // LCS-related news stories are more important if they involve lots
    // of
    // headline-grabbing
    // crimes
    case SQUAD_SITE:
    case SQUAD_ESCAPED:
    case SQUAD_FLEDATTACK:
    case SQUAD_DEFENDED:
    case SQUAD_BROKESIEGE:
    case SQUAD_KILLED_SIEGEATTACK:
    case SQUAD_KILLED_SIEGEESCAPE:
    case SQUAD_KILLED_SITE:
    case CARTHEFT:
    case NUDITYARREST:
    case WANTEDARREST:
    case DRUGARREST:
    case GRAFFITIARREST:
    case BURIALARREST:
    default:
      priority = 0;
      final Map<NewsEvent, Integer> crime = crimeListToMap();
      // Cap publicity for more than ten repeats of an action of some
      // type
      for (final NewsEvent dc : dullCrimes) {
        if (crime.get(dc) > 10) {
          crime.put(dc, 10);
        }
      }
      // Increase news story priority based on the number of instances
      // of
      // various crimes, scaled by a factor dependant on the crime
      // Unique site crimes
      priority += crime.get(NewsEvent.SHUTDOWN_REACTOR) * 100;
      priority += crime.get(NewsEvent.HACK_INTEL) * 100;
      priority += crime.get(NewsEvent.ARMY_ARMORY) * 100;
      priority += crime.get(NewsEvent.HOUSE_PHOTOS) * 100;
      priority += crime.get(NewsEvent.CORP_FILES) * 100;
      priority += crime.get(NewsEvent.PRISON_RELEASE) * 50;
      priority += crime.get(NewsEvent.JURY_TAMPERING) * 30;
      priority += crime.get(NewsEvent.POLICE_LOCKUP) * 30;
      priority += crime.get(NewsEvent.COURTHOUSE_LOCKUP) * 30;
      // Common site crimes
      priority += crime.get(NewsEvent.KILLED_SOMEBODY) * 30;
      priority += crime.get(NewsEvent.FREE_BEASTS) * 12;
      priority += crime.get(NewsEvent.BREAK_SWEATSHOP) * 8;
      priority += crime.get(NewsEvent.BREAK_FACTORY) * 8;
      priority += crime.get(NewsEvent.FREE_RABBITS) * 8;
      priority += crime.get(NewsEvent.ATTACKED_MISTAKE) * 7;
      priority += crime.get(NewsEvent.ATTACKED) * 4;
      priority += crime.get(NewsEvent.TAGGING) * 2;
      // Set story's political and violence levels for determining
      // whether
      // a story becomes positive or negative
      if (claimed) {
        politicsLevel = 5;
      } else {
        politicsLevel = 0;
      }
      politicsLevel += crime.get(NewsEvent.SHUTDOWN_REACTOR) * 100;
      politicsLevel += crime.get(NewsEvent.HACK_INTEL) * 100;
      politicsLevel += crime.get(NewsEvent.HOUSE_PHOTOS) * 100;
      politicsLevel += crime.get(NewsEvent.CORP_FILES) * 100;
      politicsLevel += crime.get(NewsEvent.PRISON_RELEASE) * 50;
      politicsLevel += crime.get(NewsEvent.POLICE_LOCKUP) * 30;
      politicsLevel += crime.get(NewsEvent.COURTHOUSE_LOCKUP) * 30;
      politicsLevel += crime.get(NewsEvent.FREE_BEASTS) * 10;
      politicsLevel += crime.get(NewsEvent.BREAK_SWEATSHOP) * 10;
      politicsLevel += crime.get(NewsEvent.BREAK_FACTORY) * 10;
      politicsLevel += crime.get(NewsEvent.FREE_RABBITS) * 10;
      politicsLevel += crime.get(NewsEvent.TAGGING) * 3;
      violenceLevel = 0;
      violenceLevel += crime.get(NewsEvent.ARMY_ARMORY) * 100;
      violenceLevel += crime.get(NewsEvent.KILLED_SOMEBODY) * 20;
      violenceLevel += crime.get(NewsEvent.ATTACKED_MISTAKE) * 12;
      violenceLevel += crime.get(NewsEvent.ATTACKED) * 4;
      Log.i("LCS", "Calculated the otherwise-unused politics and violence levels:" + politicsLevel
          + " :" + violenceLevel); // TODO
      // if(violence_level / (politics_level+1) >
      // violence_threshhold)
      // positive = 0;
      // else positive = 1;
      // Add additional priority based on the type of news story
      // and how high profile the LCS is
      switch (type) {
      case SQUAD_ESCAPED:
      case SQUAD_KILLED_SITE:
      case SQUAD_KILLED_SIEGEATTACK:
        priority += 10 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 3;
        break;
      case SQUAD_FLEDATTACK:
      case SQUAD_KILLED_SIEGEESCAPE:
        priority += 15 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 3;
        break;
      case SQUAD_DEFENDED:
        priority += 30 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 3;
        break;
      case SQUAD_BROKESIEGE:
        priority += 45 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 3;
        break;
      default:
        // Suppress action at CCS safehouses
        if (location != null && location.renting() == CrimeSquad.CCS) {
          priority = 0;
        }
        break;
      }
      // Double profile if the squad moved out in full battle colors
      if (claimed) {
        priority *= 2;
      }
      // Modify notability by location
      if (location != null) {
        priority = location.type().priority(priority);
      }
      // Cap news priority, in part so it can't displace major news
      // stories
      if (priority > 20000) {
        priority = 20000;
      }
      break;
    case KIDNAPREPORT:
      // Kidnappings are higher priority if they're an
      // archconservative
      priority = 20;
      if (creature != null
          && creature.type().ofType("CORPORATE_CEO", "RADIOPERSONALITY", "NEWSANCHOR",
              "SCIENTIST_EMINENT", "JUDGE_CONSERVATIVE")) {
        priority = 40;
      }
      break;
    case MASSACRE:
      // More people massacred, higher priority (I think; not verified
      // count is people present)
      priority = 10 + count * 5;
      break;
    case CCS_SITE:
    case CCS_KILLED_SITE:
      // CCS action loosely simulate LCS action here it adds some
      // random site crimes to the story and increases the
      // priority accordingly
      crimes.add(NewsEvent.BROKE_DOWN_DOOR);
      priority = 1;
      politicsLevel += 20;
      if (positive) {
        crimes.add(NewsEvent.ATTACKED_MISTAKE);
        priority += 7;
        violenceLevel += 12;
      }
      crimes.add(NewsEvent.ATTACKED);
      priority += 4 * (i.rng.nextInt(10) + 1);
      violenceLevel += i.rng.nextInt(10) * 4;
      if (i.rng.chance(i.endgameState.ordinal() + 1)) {
        crimes.add(NewsEvent.KILLED_SOMEBODY);
        priority += i.rng.nextInt(10) * 30;
        violenceLevel += i.rng.nextInt(10) * 20;
      }
      if (i.rng.chance(i.endgameState.ordinal() + 1)) {
        crimes.add(NewsEvent.STOLE_GROUND);
        priority += i.rng.nextInt(10);
      }
      if (!i.rng.chance(i.endgameState.ordinal() + 4)) {
        crimes.add(NewsEvent.BREAK_FACTORY);
        priority += i.rng.nextInt(10) * 2;
        politicsLevel += i.rng.nextInt(10) * 10;
      }
      if (i.rng.chance(2)) {
        crimes.add(NewsEvent.CARCHASE);
      }
      break;
    case CCS_DEFENDED:
    case CCS_KILLED_SIEGEATTACK:
      priority = 40 + i.issue(Issue.LIBERALCRIMESQUAD).attitude() / 3;
      break;
    }
    if (priority < 30) {
      page = 2;
    }
    if (priority < 25) {
      page = 3 + i.rng.nextInt(2);
    }
    if (priority < 20) {
      page = 5 + i.rng.nextInt(5);
    }
    if (priority < 15) {
      page = 10 + i.rng.nextInt(10);
    }
    if (priority < 10) {
      page = 20 + i.rng.nextInt(10);
    }
    if (priority < 5) {
      page = 30 + i.rng.nextInt(20);
    }
  }

  private Map<NewsEvent, Integer> crimeListToMap() {
    final Map<NewsEvent, Integer> crime = new EnumMap<NewsEvent, Integer>(NewsEvent.class);
    for (final NewsEvent c : NewsEvent.values()) {
      crime.put(c, 0);
    }
    for (final NewsEvent c : crimes) {
      crime.put(c, crime.get(c) + 1);
    }
    return crime;
  }

  /** only the first ten of these crimes are interesting... */
  private static final NewsEvent[] dullCrimes = { NewsEvent.STOLE_GROUND,
      NewsEvent.BROKE_DOWN_DOOR, NewsEvent.ATTACKED_MISTAKE, NewsEvent.ATTACKED,
      NewsEvent.BREAK_SWEATSHOP, NewsEvent.BREAK_FACTORY, NewsEvent.FREE_RABBITS,
      NewsEvent.FREE_BEASTS, NewsEvent.TAGGING };

  private static final long serialVersionUID = Game.VERSION;
}