package lcs.android.game;

import static lcs.android.util.Curses.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lcs.android.R;
import lcs.android.basemode.iface.BaseMode;
import lcs.android.basemode.iface.CreatureList;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.basemode.iface.SortingChoice;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureName;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.creature.health.Animal;
import lcs.android.daily.Date;
import lcs.android.daily.Interrogation;
import lcs.android.daily.Recruit;
import lcs.android.encounters.EmptyEncounter;
import lcs.android.encounters.Encounter;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Item;
import lcs.android.items.Vehicle;
import lcs.android.monthly.EndGame;
import lcs.android.news.NewsCherryBusted;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Attitude;
import lcs.android.politics.Distribution;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.scoring.HighScore;
import lcs.android.site.Site;
import lcs.android.site.Squad;
import lcs.android.site.SquadList;
import lcs.android.site.creation.SiteMap;
import lcs.android.site.map.MapTile;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;
import lcs.android.util.Getter;
import lcs.android.util.IProtocol;
import lcs.android.util.LcsRuntimeException;
import lcs.android.util.LcsStream;
import lcs.android.util.OutputBuffer;
import lcs.android.util.Setter;
import lcs.android.util.SparseMap;
import lcs.android.util.Statics;
import lcs.android.util.ThemeName;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.os.Environment;
import android.util.Log;

/** This @NonNullByDefault class contains all of the static fields containing data about the current
 * game in play, and methods for loading, saving and initializing new games.
 * <p>
 * Fields marked as {@code transient} only affect the current site, and could possibly be moved to
 * {@link Site} as static fields. */
public final @NonNullByDefault class Game implements Serializable {
  /** The following exception gets thrown when we've finished the game, and want to return to the
   * prompt to play again. This gets caught in the {@link #main} method. */
  private static class GameOverException extends RuntimeException {
    private static final long serialVersionUID = VERSION;
  }

  /**
   *
   */
  public Game() {
    Log.i("LCS", "Rng:" + rng);
    // activeSquad = Squad.none();
  }

  private final Set<UniqueWeapons> uniqueWeaponsFound = new HashSet<UniqueWeapons>();

  /** What the save game number is for this save. Assume you won't play more than 2^64 games of LCS */
  private long saveGameNumber = 0;

  /** Are we in an encounter? */
  private Encounter currentEncounter = new EmptyEncounter();

  /** Records how the player wants various menu lists of creatures presented. */
  public final Map<CreatureList, SortingChoice> activeSortingChoice = SparseMap
      .of(CreatureList.class);

  /** How many amendments there have been to the US constitution. Starts at 28; goes up if we
   * implement some Arch-(Liberal|Conservative) amendments. */
  public int amendNum = 28;

  /** How the public feel about various issues. Essentially, this is the aim of the game:
   * {@link Views#change_public_opinion} so that the government gets changed out and liberalizes all
   * of the laws. */
  // public final Map<Issue, Integer> attitude = SparseMap.of(Issue.class);
  /** At the end of each month, there's a random chance that {@link #attitude}s might change a little
   * bit: this biases the rolls a little. Each {@link Views} fades by 2/3 every month, so you need
   * to keep busy doing lots of small-scale activities each month to have an effect. */
  // public final Map<Issue, Integer> backgroundLiberalInfluence =
  // SparseMap.of(Issue.class);
  /** The place we are looking at the moment. Used in {@link BaseMode} to keep track of where we're
   * sending orders, and during sieges to point at the right place. */
  public Location currentLocation;

  /** A list of all of your Liberals' romantic (or otherwise) activities. */
  public final List<Date> dates = new ArrayList<Date>();

  /** The year that the LCS disbanded in. More than 50 years of disbanding triggers a non-standard
   * game over. */
  public int disbandYear;

  /** Progress towards completion. Doing too well triggers the Conservative Crime Squad, and later,
   * Marshal Law, in order to hinder your progress and keep things interesting. */
  public EndGame endgameState = EndGame.NONE;

  /** The executive branch of government of the United States. LCS for DOS did have these as a pair
   * of arrays (names / alignment), but Creatures aren't that expensive to create, and it means that
   * in future we could go on a mission to the courthouse and recruit / nobble elite politicians
   * directly. */
  public final Map<Exec, Creature> execs = new HashMap<Exec, Creature>();

  /** whether the government is on its first or second term */
  public int execterm = 1;

  /** Where the LCS are based. Chosen randomly at the start of the game, and makes no difference to
   * gameplay. */
  public String homeCityName = "ERROR NOT INITIALISED";

  /** Alignment of the members of the House. */
  public final Distribution house = new Distribution();

  /** The wrong-thinkers which the LCS currently has in for interrogation. nb. {@link Interrogation}s
   * delete themselves from this list, so you need to iterate over a copy of it when doing the day's
   * rounds. */
  public final List<Interrogation> interrogations = new ArrayList<Interrogation>();

  /** Current laws of the land. TODO should be a list of Laws:Alignment, but still uses the -2 to 2
   * rating of the DOS version. */
  // public final Map<Issue, Integer> law = SparseMap.of(Issue.class);
  /** Keeps track of the LCS's spending habits. */
  public final Ledger ledger = new Ledger();

  /** Contains every location in @ #homeCityName} , including the ones we've not uncovered yet. */
  public final List<Location> location = new ArrayList<Location>();

  /** whether the LCS / CCS have made it to the news yet. */
  public NewsCherryBusted newscherrybusted = NewsCherryBusted.UNKNOWN;

  /** contains all of today's news stories, so that the highest profile one can reach the front page. */
  public final List<NewsStory> newsStories = new ArrayList<NewsStory>();

  /** whether we've annoyed any of the other groups in {@link #homeCityName}, which may cause them to
   * siege. */
  public final Map<CrimeSquad, Boolean> offended = SparseMap.of(CrimeSquad.class);

  /** All of the creatures under our control, including Liberals, sleepers, hostages, and corpses. */
  public final Set<Creature> pool = new HashSet<Creature>();

  /** who won the last presidential election. 1 for right-wing, 0 for left wing. */
  public int presparty = 1;

  /** The laws most likely to be changed are the ones with the highest public interest. The various
   * activities that the LCS get up to increase public interest in certain stories. Public interest
   * decays by half in each issue at the end of the month. The highest rated issues are chosen for
   * changes in the law when voting time comes round. */
  // public final Map<Issue, Integer> publicInterest = SparseMap.of(Issue.class);
  /** A list of the concerned citizens who are willing to hear something disturbing. And potentially
   * join the LCS. */
  public final List<Recruit> recruits = new ArrayList<Recruit>();

  /** Our random number generator. */
  public final LcsRandom rng = new LcsRandom();

  /** Information that would be used to fill the high score table. Current date, kills, recruits,
   * spending, etc. */
  public final HighScore score = new HighScore();

  /** Alignment of the members of the Senate. */
  public final Distribution senate = new Distribution(); // 100

  /** This is the current site if we are in sitemode. */
  public transient Site site = new Site();

  /** The news story for the site (or possibly daily activity) we're on at the moment. */
  public NewsStory siteStory = new NewsStory(StoryType.NO_STORY);

  /** a list of all your Liberal squads. */
  public final SquadList squad = new SquadList();

  /** The supreme court of the United States. LCS for DOS did have these as a pair of arrays (names /
   * alignment), but Creatures aren't that expensive to create, and it means that in future we could
   * go on a mission to the courthouse and recruit / nobble supreme court justices directly. */
  public final Creature[] supremeCourt = new Creature[9];

  /** Whether the constitutional amendment to minimise term limits has been passed. */
  public boolean termlimits;

  /** The highest floor in the current location which was allocated during site creation. Allocating
   * {@link SiteMap#MAPX} x{@link SiteMap#MAPY}x {@link SiteMap#MAPZ}=16100 {@link MapTile}s when we
   * visit a new location to create the {@link #siteLevelmap} is not free: takes quite a long time
   * (a few seconds) and churns through a fair amount of memory (about ten megs), and so there's a
   * few optimizations about to minimise it. */
  public transient int topfloor = 0;

  /** The vehicles available to the LCS. */
  public final List<Vehicle> vehicle = new ArrayList<Vehicle>();

  /** Whether you can see the progress of current events. Essentially, controls whether
   * {@link BaseMode} gives you an options screen or a calendar flying by. */
  public Visibility visibility = Visibility.CAN_SEE;

  /** The win condition chosen by the player at the start of the game. Tested during
   * {@link Politics#wincheck} to see whether we've got a victory on our hands. */
  public WinConditions wincondition = WinConditions.ELITE;

  /** Whether we loaded in a new game. Also gets set to false at the end of a game, to stop you
   * playing on when everyone's dead. */
  private boolean loaded = false;

  /** Which control screen you were on last: controls the kind of encounters that get created,
   * whether bloodblasts make a pool on the floor, prevents leg injuries in car crashes, and adapts
   * some re-used code to the right situation. */
  private GameMode mode = GameMode.TITLE;

  private final Map<Issue, Attitude> issues = new HashMap<Issue, Attitude>();

  public Squad activeSquad() {
    return squad.current();
  }

  /** get currentEncounter.
   * @return currentEncounter */
  @Getter public Encounter currentEncounter() {
    return currentEncounter;
  }

  /** Set currentEncounter
   * @param currentEncounter
   * @return this */
  @Setter public Game currentEncounter(final Encounter currentEncounter) {
    this.currentEncounter = currentEncounter;
    Log.i("LCS", "New currentEncounter:" + currentEncounter);
    return this;
  }

  /** whether we're disbanding
   * @return whether {@link #visibility} == {@link Visibility#DISBANDING}. */
  public boolean disbanding() {
    return visibility == Visibility.DISBANDING;
  }

  /** Add a unique weapon to the 'found' list.
   * @param uw a unique weapon
   * @return true if the set was modified (and it should be, or you've found it again). */
  public boolean findUniqueWeapon(final UniqueWeapons uw) {
    return uniqueWeaponsFound.add(uw);
  }

  /** Whether swears need to be changed into [euphemisms].
   * @return true if so (ArchConservative laws on free speech). */
  public boolean freeSpeech() {
    return issue(Issue.FREESPEECH).law() != Alignment.ARCHCONSERVATIVE;
  }

  /** The loot currently at the feet of the LCS in site mode. Stored per-tile while in site mode, or
   * per-encounter otherwise. */
  public List<AbstractItem<? extends AbstractItemType>> groundLoot() {
    if (mode == GameMode.SITE) {
      return site.currentTile().groundLoot();
    }
    return currentEncounter.groundLoot();
  }

  /** Whether we've already found a unique weapon
   * @param uw The weapon
   * @return true if already found. */
  public boolean haveFoundUniqueWeapon(final UniqueWeapons uw) {
    return uniqueWeaponsFound.contains(uw);
  }

  public Attitude issue(final Issue issue) {
    Attitude attitude = issues.get(issue);
    if (attitude == null) {
      attitude = new Attitude(issue);
      issues.put(issue, attitude);
    }
    return attitude;
  }

  /** Get game mode
   * @return mode */
  @Getter public GameMode mode() {
    return mode;
  }

  /** Set game mode
   * @param aMode */
  @Setter public void mode(final GameMode aMode) {
    Log.i("LCS", "MODE: " + mode + " -> " + aMode);
    mode = aMode;
  }

  public void setActiveSquad(Squad activeSquad) {
    squad.select(activeSquad);
  }

  private void initGame() {
    Curses.setViewIfNeeded(R.layout.main);
    Curses.setText(R.id.loadeditem, "Starting your Liberal agenda");
    Log.i(LCS, "Started loading things...");
    score.slogan = Curses.getString(R.string.slogan);
    if (rng.chance(20)) {
      score.slogan = Curses.randomString(R.array.slogans);
    }
    for (final Issue v : Issue.values()) {
      if (v.core) {
        continue;
      }
      issue(v).attitude(30 + rng.nextInt(25));
    }
    issue(Issue.LIBERALCRIMESQUAD).attitude(0);
    issue(Issue.LIBERALCRIMESQUADPOS).attitude(5);
    senate.set(25, 35, 20, 15, 5);
    house.set(50, 200, 100, 50, 35);
    final Alignment[] court = { Alignment.ARCHCONSERVATIVE, Alignment.ARCHCONSERVATIVE,
        Alignment.ARCHCONSERVATIVE, Alignment.CONSERVATIVE, Alignment.CONSERVATIVE,
        Alignment.LIBERAL, Alignment.LIBERAL, Alignment.LIBERAL, Alignment.ELITELIBERAL };
    for (int c = 0; c < i.supremeCourt.length; c++) {
      i.supremeCourt[c] = CreatureType.withType("JUDGE_SUPREME");
      final CreatureName supremeName = new CreatureName(i.supremeCourt[c].genderLiberal(),
          Animal.HUMAN);
      i.supremeCourt[c].bothNames(supremeName);
      i.supremeCourt[c].alignment(court[c]);
    }
    for (final Exec e : Exec.values()) {
      final Creature c = CreatureType.withType("POLITICIAN").gender(Gender.WHITEMALEPATRIARCH)
          .alignment(Alignment.ARCHCONSERVATIVE);
      execs.put(e, c);
    }
    homeCityName = Game.cityName();
    loaded = false;
    Log.i(LCS, "Finished loaded things...");
  }

  /** The main instance of the game in play, so that all of the static methods can access the game
   * data. */
  public static Game i;

  /** Our game string, as used in Android {@link Log} entries. <q>LCS</q>. */
  public static final String LCS = "LCS";

  /** A container for all the types of item we know about. We can recreate this easily during
   * deserialization, so it doesn't make sense to serialize the Platonic Ideal of each item each
   * time we save. This gets initialized first during a {@link #load()}, so everything else that
   * gets deserialized can refer to it. */
  public final static transient Item type = new Item();

  /** Game version. We're at 4.04, like the DOS version, even though that's not strictly accurate.
   * All of the {@link Serializable} classes in LCS (except {@link HighScore}) have this as their
   * {@link #serialVersionUID}, so that changing this value will invalidate a saved game if we do a
   * version update. */
  public static final long VERSION = 4004;

  /** Whether we write crash reports to a file on the SD card for future reference. If false, just
   * display them on the screen. Even better, don't crash at all (TODO).
   * <p>
   * Writing to the SD card requires that we have
   * {@code  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>} in our
   * {@code AndroidManifest.xml} file, otherwise we'll get security crashes when we runtime crash and
   * try to write a record. */
  private static final boolean CRASHREPORTING = false;

  private static final long serialVersionUID = VERSION;

  /** whether we can see current events
   * @return whether {@link #visibility} == {@link Visibility#CAN_SEE}. */
  public static boolean canSee() {
    return i.visibility == Visibility.CAN_SEE;
  }

  /** Get a city name from our resource file, {@code game.xml}.
   * @return A random city name, as <q>City, STATE</q> */
  public static String cityName() {
    return Curses.randomString(R.array.citynames);
  }

  /** Finishes the game in progress. Deletes the save, and throws GameOverException.
   * @throws GameOverException in order to get us out of the try: loop in {@link #main}. */
  public static void endGame() throws GameOverException {
    Statics.instance().uiHandler.obtainMessage(IProtocol.CONTROL_DELETE_SAVE,
        saveFileName(i.saveGameNumber)).sendToTarget();
    i.loaded = false;
    throw new GameOverException();
  }

  /** Start point for the whole of LCS. Either loads a file off of the disk, or initialises a new
   * game, and starts us off in {@link BaseMode#modeBase}. */
  public static void main() {
    type.init();
    try {
      do {
        try {
          key: do {
            setView(R.layout.title);
            final long nextGameNumber = makeLoadGameButtons();
            final int c = getch();
            switch (c) {
            case 'a':
              Game j = new Game();
              i = j;
              i.initGame();
              i.loaded = false;
              i.saveGameNumber = nextGameNumber;
              break key;
            case 'c':
              showHelp();
              break;
            case 'l':
              showLicense();
              continue key;
            case 'h':
              HighScore.viewhighscores();
              break;
            case 't':
              changeTheme();
              break;
            default:
              if (c >= 256) {
                try {
                  i = load(c - 256);
                  i.loaded = true;
                  break key;
                } catch (final Exception e) {
                  Log.e("LCS", "Failed to load save:", e);
                }
              }
            }
          } while (true);
          if (!i.loaded) {
            NewGame.setupNewGame();
          }
          i.mode = GameMode.BASE;
          BaseMode.mainScreen();
          Game.save();
          for (; OutputBuffer.savesInProgress > 0;) {
            Log.v("LCS", "Waiting on saves to complete:" + OutputBuffer.savesInProgress);
            pauseMs(5);
          }
        } catch (final GameOverException e) {
          Log.i("LCS", "Caught a GameOverException", e);
          // we just wanted out of a do while true loop.
        }
      } while (true);
    } catch (final RuntimeException e) {
      crashReport(e);
      throw e;
    }
  }

  /** Saves the game to internal storage. Gets called every day if interactive, or every month if
   * passing time quickly. Needs to be fast to avoid annoying the user, but still takes around 200
   * ms on my S2.
   * @return true on successful save, false otherwise. We should really just die on failure to save. */
  public static boolean save() {
    // if (i == null) {
    // Log.e(LCS, "Tried to save a (null) gamestate", new Exception("null gamestate"));
    // return false;
    // }
    final OutputBuffer out = new OutputBuffer(i.saveGameNumber);
    ObjectOutputStream outStream = null;
    Log.i("LCS", "Began serialising");
    try {
      outStream = new LcsStream.Out(out.stream());
      outStream.writeObject(i);
      new Thread(out, "Serialization").start();
    } catch (final IOException ioe) {
      Log.e(LCS, "Serialisation problem:", ioe);
      throw new LcsRuntimeException("Failed to serialise", ioe);
    } finally {
      try {
        if (outStream != null) {
          outStream.close();
        }
      } catch (final IOException ioe) {
        Log.e(LCS, "Serialisation problem:", ioe);
      }
    }
    return true;
  }

  /** Creates the nume of a save game with the specific number.
   * @param saveNumber the number ID.
   * @return the new save game name */
  public static String saveFileName(final long saveNumber) {
    return String.format(Locale.ENGLISH, "%08d.save", saveNumber);
  }

  private static void changeTheme() {
    Curses.setView(R.layout.generic);
    int choice = 'a';
    ui().text("Select a new theme (experimental):").bold().add();
    for (final ThemeName tn : ThemeName.values()) {
      Curses.ui().text(tn.toString()).button(choice).button().add();
      choice++;
    }
    final int selection = Curses.getch() - 'a';
    try {
      final ThemeName selectedTheme = ThemeName.values()[selection];
      final boolean needsRestart = selectedTheme.changeStyle();
      if (needsRestart) {
        Statics.instance().restart();
      }
    } catch (final Exception e) {
      Log.e("Game", "Theming error:", e);
    }
    return;
  }

  private static void crashReport(final Exception e) {
    Log.e("LCS", "Crashed!", e);
    final StringBuilder str = new StringBuilder();
    str.append(e.toString());
    if (e.getMessage() != null) {
      str.append(":" + e.getMessage());
    }
    str.append('\n');
    for (final StackTraceElement ste : e.getStackTrace()) {
      if (!ste.getClassName().startsWith("lcs")) {
        continue;
      }
      final String filename = ste.getFileName();
      str.append("@ " + filename.substring(0, filename.length() - 5) + " (" + ste.getLineNumber()
          + ")\n");
    }
    setView(R.layout.generic);
    ui().text("Liberal Crime Squad has crashed").bold().color(Color.RED).add();
    ui().text(
        "A programming mistake has made it through testing, and LCS cannot continue.  Sorry about that.  "
            + "The error report follows.  If you've a 'submit error' button for crashes on your android, "
            + "that would be great; google collect all the reports for me.  Otherwise, an email to "
            + "addiemacgruer@googlemail.com saying what went wrong would be much appreciated.")
        .add();
    ui().text("Fatal Exception:").color(Color.RED).add();
    ui().text(str).add();
    waitOnOK();
    if (CRASHREPORTING) {
      final String savePath = "/Android/data/lcs/files";
      final File edir = Environment.getExternalStorageDirectory();
      final File dirname = new File(edir, savePath);
      Log.i("Control", "dirs:" + dirname.mkdirs());
      final File crash = new File(dirname, "LCS_Crash_Log.txt");
      OutputStreamWriter out = null;
      try {
        out = new OutputStreamWriter(new FileOutputStream(crash, crash.exists()));
        out.write(str.toString() + "\n\n");
      } catch (final IOException ioe) {
        Log.e(LCS, "Failed to create error log:" + ioe);
      } finally {
        try {
          if (out != null) {
            out.close();
          }
        } catch (final IOException ioe) {
          Log.e("LCS", "Failed to close crash report log", ioe);
        }
      }
    }
    throw new LcsRuntimeException("Game crashed", e);
  }

  /** Loads the game from the savefile.
   * @return Whether loading was successful. */
  private static Game load(final int saveNumber) {
    ObjectInputStream in = null;
    try {
      in = new LcsStream.In(Statics.instance().openFileInput(saveFileName(saveNumber)));
      final long time = System.currentTimeMillis();
      i = (Game) in.readObject();
      Log.i(LCS, "Deserialized in: " + (System.currentTimeMillis() - time) + " ms");
    } catch (final Throwable e) {
      /* likely to be ClassNotFound, IOException, or IllegalArgumentException, but missing any makes
       * it hard to recover from save corruption and annoys the user. */
      Log.e(LCS, "Couldn't load:", e);
      Statics.instance().uiHandler.obtainMessage(IProtocol.CONTROL_DELETE_SAVE,
          saveFileName(saveNumber)).sendToTarget();
      return new Game();
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (final IOException ioe) {
        Log.e("LCS", "IOException", ioe);
      }
    }
    Log.i("Control", "Restore OK");
    return i;
  }

  /** display the save games found as buttons
   * @return the next available game ID. */
  private static long makeLoadGameButtons() {
    long rval = 0;
    for (final String save : saveGameList()) {
      Log.i("LCS", "Save Game Found:" + save);
      final int number = Integer.valueOf(save.substring(0, 8));
      Log.w("LCS", "Trying to load #" + number);
      final Game g = load(number);
      if (Filter.of(g.pool, Filter.LIVING).isEmpty()) {
        Log.w(LCS, "...but everyone's dead");
        continue;
      }
      rval = Math.max(g.saveGameNumber + 1, rval);
      ui(R.id.loadsavedgames).button((int) (256 + g.saveGameNumber))
          .text("Continue save: " + g.score.toString()).add();
    }
    if (rval == 0) {
      ui(R.id.loadsavedgames).button().text("Continue your liberal agenda").add();
    }
    return rval;
  }

  /** Get a list of the save games available for loading. @return new list of saves */
  private static List<String> saveGameList() {
    final List<String> rval = new ArrayList<String>();
    for (final String file : Statics.instance().fileList()) {
      if (file.endsWith(".save") && !file.equals("lcs.save")) {
        rval.add(file);
      }
    }
    return rval;
  }
}
