package lcs.android.creature;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lcs.android.R;
import lcs.android.activities.AbstractActivity;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.crime.CreatureCrime;
import lcs.android.creature.crime.CrimeTag;
import lcs.android.creature.health.Animal;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.Health;
import lcs.android.creature.skill.CreatureSkill;
import lcs.android.creature.skill.Skill;
import lcs.android.creature.skill.SkillTag;
import lcs.android.daily.Date;
import lcs.android.daily.Recruit;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.Ledger.IncomeType;
import lcs.android.game.Quality;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Armor;
import lcs.android.items.ArmorType;
import lcs.android.items.Money;
import lcs.android.items.Vehicle;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.scoring.LcsDate;
import lcs.android.site.Squad;
import lcs.android.site.map.TileSpecial;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.PoliceStation;
import lcs.android.site.type.Prison;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Getter;
import lcs.android.util.Setter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class Creature implements Serializable {
  public enum Situation {
    TURNED,
    DIED,
    CAPTURED
  }

  Creature(final Animal animal, CreatureType type) {
    genderLiberal = genderConservative = i.rng.choice(Gender.MALE, Gender.FEMALE);
    properName = new CreatureName(genderLiberal, animal);
    name = properName.toString();
    this.type = type;
  }

  private AbstractActivity activity = BareActivity.noActivity();

  private Alignment alignment = Alignment.MODERATE;

  private Armor armor = Armor.none();

  private Location base = Location.none();

  private final LcsDate birth = LcsDate.withAge(18 + i.rng.nextInt(40));

  @Nullable private Vehicle car = null;

  private final CreatureCrime crime = new CreatureCrime(this);

  private int datingVacation = 0;

  private int deathDays = 0;

  private final Set<CreatureFlag> flag = EnumSet.noneOf(CreatureFlag.class);

  private boolean forceIncapacitated = false;

  private Gender genderConservative = Gender.MALE;

  private Gender genderLiberal = Gender.MALE;

  private final Health health = new Health(this);

  private int hiding = 0; // -1 for indefinitely

  @Nullable private Creature hire = null;

  private int income;

  private int infiltration;

  private boolean isDriver = false;

  private int joindays = 0;

  private float juice = 0;

  private Location location = Location.none();

  private int money = i.rng.nextInt(21) + 20;

  private String name;

  @Nullable private Vehicle prefCar = null;

  private boolean prefIsDriver = false;

  @Nullable private Creature prisoner = null;

  private CreatureName properName;

  private Receptive receptive = Receptive.LISTENING;

  private final CreatureSkill skill = new CreatureSkill(this);

  private SpecialAttacks specialAttack = SpecialAttacks.NONE;

  @Nullable private Squad squad = null;

  private int stunned = 0;

  private CreatureType type;

  private final CreatureWeapon weapon = new CreatureWeapon(this);

  private Location workLocation = Location.none();

  public AbstractActivity activity() {
    return activity;
  }

  public Creature activity(@Nullable final AbstractActivity aActivity) {
    if (aActivity == null) {
      throw new IllegalArgumentException("activity was null");
    }
    activity = aActivity;
    return this;
  }

  /** Add a CreatureFlag to the Creature
   * @param cf a flag
   * @return true if changed. */
  public boolean addFlag(final CreatureFlag cf) {
    return flag.add(cf);
  }

  public void addJuice(final float aJuice, final int cap) {
    // Ignore zero changes
    if (Float.compare(aJuice, 0f) == 0) {
      return;
    }
    // Check against cap
    if (aJuice > 0 && juice >= cap || aJuice < 0 && juice <= cap) {
      return;
    }
    // Apply juice gain
    juice += aJuice;
    // Pyramid scheme of juice trickling up the chain
    if (hire != null) {
      hire.addJuice(aJuice / 5, (int) juice);
    }
    // Bounds check
    if (juice > 1000) {
      juice = 1000;
    }
    if (juice < -50) {
      juice = -50;
    }
  }

  @Getter public int age() {
    return birth.yearsTo(i.score.date);
  }

  @Setter public Creature age(final int age) {
    final int ageDelta = age() - age;
    birth.year(birth.year() + ageDelta);
    return this;
  }

  @Getter public Alignment alignment() {
    return alignment;
  }

  @Getter public Creature alignment(final Alignment aAlignment) {
    alignment = aAlignment;
    return this;
  }

  @Getter public Location base() {
    return base;
  }

  /** Sets a creature's new base. Prints a log trace if set to null.
   * @param aBase base to set
   * @return this */
  public Creature base(final Location aBase) {
    base = aBase;
    return this;
  }

  @Getter public int birthDay() {
    return birth.day();
  }

  @Setter public Creature birthDay(final int birthDay) {
    birth.day(birthDay);
    return this;
  }

  @Getter public int birthMonth() {
    return birth.month();
  }

  @Setter public Creature birthMonth(final int birthMonth) {
    if (birthMonth < 1 || birthMonth > 12) {
      throw new IllegalArgumentException("Bad month:" + birthMonth);
    }
    birth.month(birthMonth);
    return this;
  }

  /** Set both known-as name and proper name.
   * @param string */
  public Creature bothNames(final CreatureName newName) {
    properName = newName;
    name = newName.toString();
    return this;
  }

  public boolean canDate(final Creature a) {
    // Assume age appropriate for animals, tanks, etc.
    // (use other restrictions for these, like humorous rejections)
    if (type.animal() != Animal.HUMAN || a.type.animal() != Animal.HUMAN) {
      return true;
    }
    final int age = age(), aage = a.age();
    if (age < 11 || aage < 11) {
      return false;
    }
    if (age < 16 || aage < 16) {
      if (Math.abs(age - aage) < 5) {
        return true;
      }
      return false;
    }
    return true;
  }

  /** Send this creature to jail
   * @param reason not null */
  public Creature captureByPolice(final Crime reason) { // TODO check the logic
                                                        // of this.
    removeSquadInfo();
    car(null);
    location(AbstractSiteType.type("GOVERNMENT_POLICESTATION").getLocation());
    weapon().dropWeaponsAndClips(null);
    activity(BareActivity.noActivity());
    crime.criminalize(reason);
    freeHostage(Creature.Situation.CAPTURED);
    if (hasFlag(CreatureFlag.JUST_ESCAPED)) {
      location(i.site.current());
      if (i.site.type().isPrison()) {
        giveArmor(new Armor("ARMOR_PRISONER"), null);
      }
      if (i.site.type().isType(Prison.class)) {
        crime().clearCriminalRecord();
      }
    } else {
      giveArmor(new Armor("ARMOR_CLOTHES"), null);
      location(AbstractSiteType.type(PoliceStation.class).getLocation());
    }
    return this;
  }

  @Getter public Vehicle car() {
    return car;
  }

  @Setter public Creature car(@Nullable final Vehicle aCar) {
    car = aCar;
    return this;
  }

  public Creature checkBirthday() {
    if (!birth.dateEquals(i.score.date)) {
      return this;
    }
    switch (age()) {
    case 13:
      type = CreatureType.valueOf("TEENAGER");
      /* aww, all grown up */
      break;
    case 18:
      type = CreatureType.valueOf("POLITICALACTIVIST");
      /* ok seriously this time */
      break;
    default:
    }
    return this;
  }

  /** turns a creature into a conservative */
  public Creature conservatise() {
    if (alignment == Alignment.LIBERAL && juice > 0) {
      juice = 0;
    }
    alignment = Alignment.CONSERVATIVE;
    if (type.idName().equals("WORKER_FACTORY_UNION")) {
      name = "Ex-Union Worker";
    } else if (type.idName().equals("JUDGE_LIBERAL")) {
      name = "Jaded Liberal Judge";
    }
    return this;
  }

  @Getter public CreatureCrime crime() {
    return crime;
  }

  @Getter public int datingVacation() {
    return datingVacation;
  }

  @Setter public Creature datingVacation(final int datingVacation) {
    this.datingVacation = datingVacation;
    return this;
  }

  @Getter public int deathDays() {
    return deathDays;
  }

  @Setter public Creature deathDays(final int deathDays) {
    this.deathDays = deathDays;
    return this;
  }

  /** describes a character's death */
  public void deathMessage() {
    final StringBuilder str = new StringBuilder();
    if (health.missing(BodyPart.HEAD)) {
      str.append(name);
      switch (i.rng.nextInt(4)) {
      default:
        str.append(" reaches once where there");
        if (i.mode() != GameMode.CHASECAR) {
          str.append("is no head, and falls.");
        } else {
          str.append("is no head, and slumps over.");
        }
        break;
      case 1:
        if (i.mode() != GameMode.CHASECAR) {
          str.append(" stands headless for a ");
        } else {
          str.append(" sits headless for a");
        }
        str.append("moment then crumples over.");
        break;
      case 2:
        str.append(" squirts ");
        if (!i.freeSpeech()) {
          str.append("[red water]");
        } else {
          str.append("blood");
        }
        str.append(" out of the ");
        if (i.mode() != GameMode.CHASECAR) {
          str.append("neck and runs down the hall.");
        } else {
          str.append("neck and falls to the side.");
        }
        break;
      case 3:
        str.append(" sucks a last breath through the neck hole, then is quiet.");
        break;
      }
    } else if (health.missing(BodyPart.BODY)) {
      str.append(name);
      str.append(i.rng.choice(" breaks into pieces.", " falls apart and is dead."));
      str.append(str.toString());
    } else {
      str.append(name);
      switch (i.rng.nextInt(11)) {
      default:
        str.append(" cries out one last time then is quiet.");
        break;
      case 1:
        str.append(" gasps a last breath and ");
        if (!i.freeSpeech()) {
          str.append("[makes a mess].");
        } else {
          str.append("soils the floor.");
        }
        break;
      case 2:
        str.append(" murmurs quietly, breathing softly.  Then all is silent.");
        break;
      case 3:
        str.append(" shouts \"FATHER!  Why have you forsaken me?\" and dies in a heap.");
        break;
      case 4:
        str.append(" cries silently for mother, breathing slowly, then not at all.");
        break;
      case 5:
        str.append(" breathes heavily, coughing up blood...  then is quiet.");
        break;
      case 6:
        str.append(" silently drifts away, and is gone.");
        break;
      case 7:
        str.append(" sweats profusely, murmurs ");
        if (!i.freeSpeech()) {
          str.append("something [good] about Jesus, and dies.");
        } else {
          str.append("something about Jesus, and dies.");
        }
        break;
      case 8:
        str.append(" whines loudly, voice crackling, then curls into a ball, unmoving.");
        break;
      case 9:
        str.append(" shivers silently, whispering a prayer, then all is still.");
        break;
      case 10:
        str.append(" speaks these final words: ");
        switch (alignment) {
        case LIBERAL:
        case ELITELIBERAL:
          str.append("\"" + i.score.slogan + "\"");
          break;
        case MODERATE:
          str.append("\"A plague on both your houses...\"");
          break;
        default:
          str.append("\"Better dead than liberal...\"");
          break;
        }
      }
    }
    Curses.ui().text(str.toString()).color(Color.YELLOW).add();
  }

  @Setter public Creature driver(final boolean isDriver) {
    this.isDriver = isDriver;
    return this;
  }

  /** Drop all loot on the floor.
   * @param loot the floor, or null if none. */
  public Creature dropLoot(final List<AbstractItem<? extends AbstractItemType>> loot) {
    weapon().dropWeaponsAndClips(loot);
    strip(loot);
    if (money > 0 && i.mode() == GameMode.SITE) {
      loot.add(new Money(money));
      money = 0;
    }
    return this;
  }

  /** Whether the current creature is an enemy, ie. Conservative or Archconservative, or a moderate
   * Cop not on your team.
   * @return true if an enemy */
  public boolean enemy() {
    if (alignment == Alignment.CONSERVATIVE || alignment == Alignment.ARCHCONSERVATIVE) {
      return true;
    } else if (type == CreatureType.valueOf("COP") && alignment == Alignment.MODERATE) {
      return (!i.pool.contains(this));
    }
    return false;
  }

  public void filloutFullStatus() {
    setText(R.id.statistics, R.string.cdStatistics, //
        skill.getAttribute(Attribute.HEART, true), //
        skill.getAttribute(Attribute.INTELLIGENCE, true), //
        skill.getAttribute(Attribute.WISDOM, true),//
        skill.getAttribute(Attribute.HEALTH, true),//
        skill.getAttribute(Attribute.AGILITY, true),//
        skill.getAttribute(Attribute.STRENGTH, true),//
        skill.getAttribute(Attribute.CHARISMA, true)//
    );
    final StringBuilder str = new StringBuilder();
    str.append(getString(R.string.cdName));
    if (name.equals(properName.toString())) {
      str.append(name + ", ");
    } else {
      str.append("\"" + name + "\" (" + properName + "), ");
    }
    getTitle(str);
    str.append(" / ");
    str.append(type.jobtitle(this));
    setText(R.id.name, str.toString());
    str.setLength(0);
    setText(R.id.birth, R.string.cdDoB, birth.day(), birth.monthName(), birth.year(), age(),
        genderLiberal);
    if (car != null) {
      setText(R.id.statistics2, R.string.cdStatistics2,//
          car.shortname(),//
          weapon().weapon().toString(),//
          describeArmor());
    } else {
      setText(R.id.statistics2, R.string.cdStatistics2,//
          health.describeLegCount(),//
          weapon().weapon().toString(),//
          describeArmor());
    }
    setText(R.id.juice, R.string.cdJuice, (int) juice, nextJuice());
    final StringBuilder sb = new StringBuilder();
    // Add recruit stats
    if (!hasFlag(CreatureFlag.BRAINWASHED)) {
      sb.append(maxSubordinates() - subordinatesLeft());
      sb.append(" Recruits / ");
      sb.append(maxSubordinates());
      sb.append(" Max\n");
    } else {
      sb.append("Enlightened\n");
      sb.append("Can't Recruit\n");
    }
    // Any meetings with potential recruits scheduled?
    final int recruiting = scheduledMeetings();
    if (recruiting > 0) {
      sb.append("Scheduled Meetings: ");
      sb.append(recruiting);
      sb.append('\n');
    }
    // Add seduction stats
    int lovers = loveSlaves();
    if (hasFlag(CreatureFlag.LOVE_SLAVE)) {
      lovers++;
    }
    if (lovers > 0) {
      sb.append(lovers);
      sb.append(" Romantic Interest");
      if (lovers > 1) {
        sb.append('s');
      }
      sb.append('\n');
    }
    // Any dates with potential love interests scheduled?
    final int wooing = scheduledDates();
    if (wooing > 0) {
      sb.append("Scheduled Dates: ");
      sb.append(wooing);
      sb.append('\n');
    }
    sb.setLength(sb.length() - 1); // chop the newline
    setText(R.id.recruitstats, sb.toString());
    sb.setLength(0);
    health.woundStatus();
    health.specialWoundDescription();
    // remove last \n
    final List<SkillTag> skills = new ArrayList<SkillTag>();
    for (final Skill s : Skill.values()) {
      final int sv = skill.skill(s) * 100 + skill.skillXp(s);
      skills.add(new SkillTag(s.toString(), skill.skill(s), skill.skillXp(s), sv, skill.skillCap(s,
          true)));
    }
    Collections.sort(skills);
    for (final SkillTag s : skills) {
      if (s.value == 0) {
        break; // you never know until you try.
      }
      s.addToView(R.id.gskills, this);
    }
    final List<CrimeTag> crimes = new ArrayList<CrimeTag>();
    for (final Crime l : Crime.values()) {
      if (crime.crimesSuspected(l) == 0) {
        continue;
      }
      crimes.add(new CrimeTag(l, crime.crimesSuspected(l)));
    }
    if (!crimes.isEmpty()) {
      Collections.sort(crimes);
      for (final CrimeTag c : crimes) {
        c.addToView(R.id.gcrimes);
      }
    } else {
      ui(R.id.gcrimes).restext(R.string.cdNoCrimes).add();
    }
  }

  @Setter public Creature forceIncapacitated(final boolean forceIncapacitated) {
    this.forceIncapacitated = forceIncapacitated;
    return this;
  }

  /** hostage freed due to host unable to haul
   * @param why they've been freed, not null */
  public void freeHostage(final Situation situation) {
    final Creature mPrisoner = prisoner;
    if (mPrisoner == null || !mPrisoner.health.alive()) {
      return;
    }
    switch (situation) {
    case TURNED:
      if (mPrisoner.squad == null) {
        ui().text(" and a hostage is freed").add();
      } else {
        ui().text(" and ").add();
        ui().text(mPrisoner.name).add();
        if (mPrisoner.hasFlag(CreatureFlag.JUST_ESCAPED)) {
          ui().text(" is recaptured").add();
        } else {
          ui().text(" is captured").add();
        }
      }
      break;
    case DIED:
      if (mPrisoner.squad == null) {
        ui().text("A hostage escapes!").add();
      } else {
        ui().text(mPrisoner.name).add();
        if (mPrisoner.hasFlag(CreatureFlag.JUST_ESCAPED)) {
          ui().text(" is recaptured.").add();
        } else {
          ui().text(" is captured.").add();
        }
      }
      break;
    case CAPTURED:
    default:
      // no message
    }
    prisoner = null;
  }

  /** Sets both genders (liberal and conservative) at once. Also renames to suit.
   * @param newGender the gender to set
   * @return current creature */
  public Creature gender(final Gender newGender) {
    genderLiberal = genderConservative = newGender;
    properName = new CreatureName(newGender, type.animal());
    name = properName.toString();
    return this;
  }

  /** Sets both genders (liberal and conservative) at once, and sets name (used during character
   * creation).
   * @param newGender the gender to set
   * @return current creature */
  public Creature genderAndName(final Gender newGender, final CreatureName newName) {
    genderLiberal = genderConservative = newGender;
    properName = newName;
    name = properName.toString();
    return this;
  }

  @Getter public Gender genderConservative() {
    return genderConservative;
  }

  @Setter public Creature genderConservative(final Gender genderConservative) {
    this.genderConservative = genderConservative;
    return this;
  }

  @Getter public Gender genderLiberal() {
    return genderLiberal;
  }

  @Setter public Creature genderLiberal(final Gender genderLiberal) {
    this.genderLiberal = genderLiberal;
    return this;
  }

  @Getter public Armor getArmor() {
    if (!isNaked()) {
      return armor;
    }
    return Armor.none();
  }

  /** Equips the creature with the given Armor, discarding any clothes worn to the lootpile
   * @param a An armor
   * @param lootpile Where to discard to, if needed. */
  public void giveArmor(final Armor a,
      @Nullable final List<AbstractItem<? extends AbstractItemType>> lootpile) {
    if (!a.isEmpty()) {
      strip(lootpile);
      armor = a.split(1);
    }
  }

  public Creature giveFundsToLCS() {
    i.ledger.addFunds(money, IncomeType.RECRUITS);
    money = 0;
    return this;
  }

  public Quality hasDisguise() {
    AbstractSiteType mType = null;
    mType = i.site.current().type();
    Quality uniformed = Quality.NONE;
    if (i.site.current().lcs().siege.siege) {
      uniformed = i.site.current().lcs().siege.siegetype.uniform.hasDisguise(this);
    } else {
      if ((!isNaked() || type.animal() == Animal.ANIMAL)
          && !getArmor().id().equals("ARMOR_HEAVYARMOR")) {
        uniformed = Quality.GOOD;
      }
      if (mType.disguisesite() && i.site.currentTile().flag.contains(TileSpecial.RESTRICTED)) {
        uniformed = mType.uniform.hasDisguise(this);
      }
    }
    if (uniformed != Quality.NONE) {
      if (getArmor().isPolice()) {
        uniformed = Quality.POOR;
      }
    }
    return uniformed;
  }

  /** Test whether a CreatureFlag is present
   * @param cf a flag
   * @return true if present */
  public boolean hasFlag(final CreatureFlag cf) {
    return flag.contains(cf);
  }

  @Getter public Health health() {
    return health;
  }

  @Getter public int hiding() {
    return hiding;
  }

  @Setter public Creature hiding(final int hiding) {
    this.hiding = hiding;
    return this;
  }

  @Getter public Creature hire() {
    return hire;
  }

  @Setter public Creature hire(final Creature aHire) {
    hire = aHire;
    return this;
  }

  @Getter public int income() {
    return income;
  }

  @Setter public Creature income(final int income) {
    this.income = income;
    return this;
  }

  @Getter public int infiltration() {
    return infiltration;
  }

  @Setter public Creature infiltration(final int infiltration) {
    this.infiltration = infiltration;
    return this;
  }

  @Getter public boolean isDriver() {
    return isDriver;
  }

  public boolean isFounder() {
    return hire == null;
  }

  @Getter public boolean isIncapacitated() {
    return forceIncapacitated;
  }

  public boolean isNaked() {
    return armor == Armor.none();
  }

  public boolean isPrisoner() {
    return name.equals("Prisoner");
  }

  /** are they interested in talking about the issues? */
  public boolean isTalkReceptive() {
    if (enemy()) {
      return false;
    }
    if (hasFlag(CreatureFlag.CONVERTED)) {
      return true;
    }
    return type.receptive();
  }

  @Getter public int joindays() {
    return joindays;
  }

  @Setter public Creature joindays(final int joindays) {
    this.joindays = joindays;
    return this;
  }

  @Getter public int juice() {
    return (int) juice;
  }

  @Setter public Creature juice(final int aJuice) {
    juice = aJuice;
    return this;
  }

  /** turns a creature into a liberal
   * @param rename whether to rename them */
  public Creature liberalize(final boolean rename) {
    if (alignment == Alignment.CONSERVATIVE) {
      juice = 0;
    }
    alignment = Alignment.LIBERAL;
    if (rename) {
      if (type.idName().equals("WORKER_FACTORY_NONUNION")) {
        name = "New Union Worker";
      }
    }
    return this;
  }

  @Getter public Location location() {
    return location;
  }

  /** Sets a creatures location.
   * @param aLocation new location.
   * @return this. */
  @Setter public Creature location(final Location aLocation) {
    location = aLocation;
    return this;
  }

  /** @return */
  public String longDescription() {
    switch (type().animal()) {
    default:
      return properName + ", " + type.jobtitle(this) + vagueAge() + ", " + health.healthStat();
    case TANK:
      return type.jobtitle(this) + ", " + health.healthStat();
    }
  }

  /** Determines the number of love slaves a creature has */
  public int loveSlaves() {
    int loveSlaves = 0;
    for (final Creature p : i.pool) {
      // If subordinate and a love slave
      if (p.hire == this && p.health.alive() && p.hasFlag(CreatureFlag.LOVE_SLAVE)) {
        loveSlaves++;
      }
    }
    return loveSlaves;
  }

  /** Determines the number of love slaves a creature may recruit, based on max minus number they
   * already command */
  public int loveSlavesLeft() {
    // Get maximum lovers
    int loveslavecap = skill.skill(Skill.SEDUCTION) / 2 + 1;
    // -1 if they're a love slave (their boss is a lover)
    if (hasFlag(CreatureFlag.LOVE_SLAVE)) {
      loveslavecap--;
    }
    loveslavecap -= loveSlaves();
    return Math.max(loveslavecap, 0);
  }

  @Setter public Creature name(final String name) {
    this.name = name;
    return this;
  }

  /** Sets both location and base at once.
   * @param aLocation Where to go, not null
   * @return this. */
  public Creature newHome(final Location aLocation) {
    location(aLocation).base(aLocation);
    return this;
  }

  public CheckDifficulty observationSkill() {
    return type.observationSkill();
  }

  @Nullable @Getter public Vehicle prefCar() { // TODO make maybe
    return prefCar;
  }

  @Setter public Creature prefCar(@Nullable final Vehicle prefCar) {
    this.prefCar = prefCar;
    return this;
  }

  @Getter public boolean prefIsDriver() {
    return prefIsDriver;
  }

  @Setter public Creature prefIsDriver(final boolean prefIsDriver) {
    this.prefIsDriver = prefIsDriver;
    return this;
  }

  public void printCreatureInfo(final int knowledge) {
    setInflate(R.id.squadprecis);
    final StringBuilder str = new StringBuilder();
    str.append(name);
    str.append(", ");
    getTitle(str);
    if (prisoner != null) {
      str.append(getString(R.string.cdHolding));
      if (type.idName().equals("CORPORATE_CEO")) {
        str.append(getString(R.string.cdCorporateCeo));
      } else if (type.idName().equals("RADIOPERSONALITY")) {
        str.append(getString(R.string.cdRadioPersonality));
      } else if (type.idName().equals("NEWSANCHOR")) {
        str.append(getString(R.string.cdNewsAnchor));
      } else if (type.idName().equals("SCIENTIST_EMINENT")) {
        str.append(getString(R.string.cdScientistEminent));
      } else if (type.idName().equals("JUDGE_CONSERVATIVE")) {
        str.append(getString(R.string.cdJudgeConservative));
      } else {
        final Creature prisoner2 = prisoner;
        if (prisoner2 != null) {
          str.append(prisoner2.name);
        }
      }
    }
    setText(R.id.asName, str.toString());
    str.setLength(0);
    if (knowledge > 0) {
      format((CharSequence) str, R.string.cdStatistics, //
          skill.getAttribute(Attribute.HEART, true), //
          skill.getAttribute(Attribute.INTELLIGENCE, true), //
          skill.getAttribute(Attribute.WISDOM, true),//
          skill.getAttribute(Attribute.HEALTH, true),//
          skill.getAttribute(Attribute.AGILITY, true),//
          skill.getAttribute(Attribute.STRENGTH, true),//
          skill.getAttribute(Attribute.CHARISMA, true)//
      );
    } else {
      format((CharSequence) str, R.string.cdStatistics, "?", "?", "?", "?", "?", "?", "?");
    }
    if (car != null) {
      format((CharSequence) str, R.string.cdStatistics2,//
          car.shortname(),//
          weapon().weapon().toString(),//
          getArmor().toString());
    } else {
      format((CharSequence) str, R.string.cdStatistics2,//
          health.describeLegCount(),//
          weapon().weapon().toString(),//
          getArmor().toString());
    }
    setText(R.id.asStats, str.toString());
    str.setLength(0);
    final List<SkillTag> skills = new ArrayList<SkillTag>();
    str.append(getString(R.string.cdSkills));
    for (final Skill s : Skill.values()) {
      final int sv = skill.skill(s) * 100 + Math.min(skill.skillXp(s), 99);
      /* otherwise 2.99+ sorts above 3.00 */
      if (sv == 0) {
        continue;
      }
      if (knowledge > (7 - sv) / 100) {
        skills.add(new SkillTag(s.toString(), skill.skill(s), skill.skillXp(s), sv));
      } else if (knowledge > (5 - sv) / 100) {
        skills.add(new SkillTag("??????", skill.skill(s), skill.skillXp(s), sv));
      } else {
        skills.add(new SkillTag("??????: ?", sv));
      }
    }
    Collections.sort(skills);
    int k = 8;
    for (final SkillTag j : skills) {
      if (j.value == 0 || k-- == 0) {
        break; // you never know until you try.
      }
      str.append(j.text + "\n");
    }
    str.setLength(str.length() - 1);
    setText(R.id.asSkills, str.toString());
  }

  @Nullable @Getter public Creature prisoner() {
    return prisoner;
  }

  @Setter public Creature prisoner(final @Nullable Creature prisoner) {
    this.prisoner = prisoner;
    return this;
  }

  public boolean promoteSubordinates() {
    Creature newboss = null;
    Creature bigboss = null;
    if (hire == null) {
      bigboss = this;// Special: Founder
    }
    int maxjuice = 0; // Need more than 0 juice to get promoted
    int subordinates = 0;
    // Need REVOLUTIONARY (100+) juice to take over founder role
    if (hire == null) {
      maxjuice = 99;
    }
    // Identify big boss and top subordinate
    for (final Creature p : i.pool) {
      if (p == this) {
        continue;
      }
      if (p == hire) {
        bigboss = p;
      }
      if (p.hire == this && p.health.alive() && p.alignment == Alignment.LIBERAL) {
        subordinates++;
        /* Brainwashed people inelligible for promotion to founder */
        if (bigboss == null && p.hasFlag(CreatureFlag.BRAINWASHED)) {
          continue;
        }
        /* Loveslaves inelligible for promotion to anything unless juice is high in which case they
         * get over it and continue to serve as a normal member */
        if (p.hasFlag(CreatureFlag.LOVE_SLAVE)) {
          if (p.juice < 100) {
            continue;
          }
          p.removeFlag(CreatureFlag.LOVE_SLAVE);
        }
        /* Highest juice liberal not subject to a life sentence gets promoted */
        if (p.juice > maxjuice
            && (!p.location.type().isType("GOVERNMENT_PRISON") || p.crime.sentence() >= 0)) {
          maxjuice = (int) p.juice;
          newboss = p;
        }
      }
    }
    /* No subordinates or none with sufficient juice to carry on */
    if (subordinates == 0 || newboss == null) {
      if (hire != null) {
        return false;
      }
      if (subordinates > 0) // Disintegration of the LCS
      {
        setView(R.layout.generic);
        ui().text(name + " has died.").add();
        ui().text("There are none left with the courage and conviction to lead....").add();
        Curses.waitOnOK();
      }
      return false;
    }
    // Chain of command totally destroyed if dead person's boss also dead
    // if (bigboss == null || hire != null && bigboss != null &&
    // !bigboss.ch.alive())
    // return false;
    // Promote the new boss
    newboss.hire = hire;
    // Order secondary subordinates to follow the new boss
    if (subordinates > 1) {
      for (final Creature p : i.pool) {
        if (p.hire == this && // recruited by old boss that
            // died
            p != newboss && // not the new boss
            p.hasFlag(CreatureFlag.LOVE_SLAVE)) {
          p.hire = newboss; // promote
        }
      }
    }
    if (bigboss != null) { // Normal promotion
      setView(R.layout.generic);
      ui().text(
          bigboss.name + " has promoted " + newboss.name + "due to the death of " + name + ".")
          .add();
      if (subordinates > 1) {
        ui().text(newboss.name + " will take over for " + name + " in the command chain.").add();
      }
      Curses.waitOnOK();
    } else {// Founder level promotion
      setView(R.layout.generic);
      ui().text(name + " has died.").add();
      ui().text(newboss.name + " is the new leader of the Liberal Crime Squad!").add();
      Curses.waitOnOK();
    }
    return true;
  }

  @Getter public String properName() {
    return properName.toString();
  }

  @Getter public Receptive receptive() {
    return receptive;
  }

  @Setter public Creature receptive(final Receptive receptive) {
    this.receptive = receptive;
    return this;
  }

  /** Remove a CreatureFlag from the Creature
   * @param cf a flag
   * @return true if changed. */
  public boolean removeFlag(final CreatureFlag cf) {
    return flag.remove(cf);
  }

  /** common - removes the liberal from all squads */
  public Creature removeSquadInfo() {
    for (final Squad s : i.squad) {
      s.remove(this);
    }
    squad = null;
    return this;
  }

  /** Inspect a member of the public etc. during an encounter. */
  public void scrutinize() {
    setView(R.layout.generic);
    ui().text("Profile of a " + alignment + ": " + properName()).color(alignment.color()).bold()
        .add();
    ui().text("Age: " + vagueAge()).add();
    ui().text("Occupation:" + type.jobtitle(this)).add();
    ui().text("Health: " + health.healthStat()).add();
    ui().text("Wearing: " + getArmor()).add();
    ui().text("Carrying: " + (weapon.isArmed() ? "unarmed" : weapon.weapon())).add();
    final List<Location> workLocations = type.workLocations();
    if (!workLocations.isEmpty()) {
      ui().text(type.jobtitle(this) + "s often find work at the following locations:").add();
      for (final Location ast : workLocations) {
        ui().text("-- " + ast.toString()).add();;
      }
    }
    waitOnOK();
  }

  @Getter public CreatureSkill skill() {
    return skill;
  }

  /** Prompt to turn new recruit into a sleeper
   * @param recruiter not null */
  public Creature sleeperizePrompt(@Nullable final Creature recruiter) {
    if (recruiter == null) {
      throw new NullPointerException();
    }
    setView(R.layout.generic);
    ui(R.id.gcontrol).text(format("In what capacity will %s best serve the Liberal cause?", name))
        .add();
    ui(R.id.gcontrol).button('a')
        .text(format("Come to %s as a regular member.", recruiter.location.toString())).add();
    if (workLocation != Location.none()) {
      ui(R.id.gcontrol).button('b')
          .text(format("Stay at %s as a sleeper agent.", workLocation.toString())).add();
    }
    int keystroke;
    do {
      keystroke = Curses.getch();
    } while (keystroke != 'a' && keystroke != 'b');
    if (workLocation != Location.none() && keystroke == 'b') {
      addFlag(CreatureFlag.SLEEPER);
      location = base = workLocation;
      workLocation.interrogated(true).hidden(false);
    } else {
      location = recruiter.location;
      base = recruiter.base;
    }
    liberalize(false);
    return this;
  }

  @Getter public SpecialAttacks specialAttack() {
    return specialAttack;
  }

  /** Does an attribute roll for current speed (agility and health)..
   * @return the result, or 0 if in a wheelchair. */
  public int speed() {
    if (hasFlag(CreatureFlag.WHEELCHAIR)) {
      return 0;
    }
    return skill.attributeRoll(Attribute.AGILITY) + skill.attributeRoll(Attribute.HEALTH);
  }

  /** The creature's current sqaud.
   * @return */
  @Nullable @Getter public Squad squad() {
    return squad;
  }

  /** Assigns the creature to a squad
   * @param aSquad may be null
   * @return this */
  public Creature squad(@Nullable final Squad aSquad) {
    if (squad == aSquad) {
      return this;
    }
    if (squad != null) {
      squad.remove(this);
    }
    if (aSquad != null && !aSquad.contains(this)) {
      aSquad.add(this);
    }
    squad = aSquad;
    return this;
  }

  /** Discards any clothing worn to a lootpile
   * @param lootpile can be null */
  public void strip(@Nullable final List<AbstractItem<? extends AbstractItemType>> lootpile) {
    if (armor != Armor.none() && lootpile != null) {
      lootpile.add(armor);
    }
    armor = Armor.none();
  }

  @Getter public int stunned() {
    return stunned;
  }

  @Setter public Creature stunned(final int stunned) {
    this.stunned = stunned;
    return this;
  }

  /** Determines the number of subordinates a creature may recruit, based on their max and the number
   * they already command */
  public int subordinatesLeft() {
    int recruitcap = maxSubordinates();
    for (final Creature p : i.pool) {
      // ignore seduced and brainwashed characters
      if (p.hire == this && p.health.alive()
          && !(p.hasFlag(CreatureFlag.LOVE_SLAVE) || p.hasFlag(CreatureFlag.BRAINWASHED))) {
        recruitcap--;
      }
    }
    return Math.max(recruitcap, 0);
  }

  @Override public String toString() {
    return properName.toString();
  }

  @Getter public CreatureType type() {
    return type;
  }

  @Setter public Creature type(final CreatureType type) {
    this.type = type;
    return this;
  }

  // Add an age estimate to a person's name
  public String vagueAge() {
    // Who knows how old the purple gorilla/tank/flaming bunny/dog is?
    if (type.animal() != Animal.HUMAN) {
      return " (?)";
    }
    final StringBuilder sb = new StringBuilder();
    // For humans, estimate their age and gender
    sb.append(" (");
    // Almost precise estimates of child and teen ages
    final int age = age();
    if (age < 20) {
      /* Inaccuracy in estimating age should be the same every time a character is queried. I'm
       * using the day of the month the character was born on to determine this. */
      sb.append(String.valueOf(age + birth.day() % 3 - 1));
      sb.append('?');
    } else if (age < 90) {
      sb.append(age / 10 + "0s");
    } else {
      sb.append("Very Old");
    }
    sb.append(", ");
    sb.append(genderLiberal);
    // Note if there's some conflict with Conservative society's perceptions
    if (genderLiberal != genderConservative) {
      sb.append('?');
    }
    sb.append(')');
    return sb.toString();
  }

  @Getter public CreatureWeapon weapon() {
    return weapon;
  }

  /** Whether the weapon we're carrying is appropriate for the disguise.
   * @return GOOD if appropriate, or POOR if not. */
  public Quality weaponCheck() {
    CreatureType incharacter = CreatureType.inCharacter(this);
    final boolean concealed = weaponIsConcealed();
    if (hasDisguise() == Quality.NONE) {
      incharacter = null;
    }
    if (weapon().weapon().isSuspicious()) {
      if (incharacter != null || concealed) {
        return Quality.GOOD; // OK
      }
      return Quality.POOR;
    }
    return Quality.GOOD;
  }

  @Getter public Location workLocation() {
    return workLocation;
  }

  /** Equips the creature with the given Armor type, discarding any clothes worn to the lootpile
   * @param armorType An armortype */
  void giveArmor(final ArmorType armorType) {
    giveArmor(new Armor(armorType), null);
  }

  /** Equips the creature with the given Armor, discarding any clothes worn to the lootpile
   * @param aArmor An armor */
  void giveArmor(final String aArmor) {
    giveArmor(new Armor("ARMOR_" + aArmor), null);
  }

  @Setter Creature money(final int money) {
    this.money = money;
    return this;
  }

  /** gives a CCS member a cover name */
  Creature nameCCSMember() {
    if (getArmor().ofType("ARMOR_CIVILLIANARMOR")) {
      name = "Mercenary";
    } else if (getArmor().ofType("ARMOR_ARMYARMOR")) {
      name = "Soldier";
    } else if (getArmor().ofType("ARMOR_HEAVYARMOR")) {
      name = "Hardened Veteran";
    } else if (weapon().weapon().type.equals(Game.type.weapon.get("WEAPON_SHOTGUN_PUMP"))
        || i.rng.nextInt(2) == 1) {
      name = i.rng.choice("Country Boy", "Hick", "Redneck", "Rube", "Yokel");
    } else {
      name = i.rng.choice("Biker", "Transient", "Crackhead", "Fast Food Worker", "Telemarketer",
          "Office Worker", "Mailman", "Musician", "Hairstylist");
    }
    return this;
  }

  @Setter Creature properName(final CreatureName properName) {
    this.properName = properName;
    return this;
  }

  @Setter Creature specialAttack(final SpecialAttacks specialAttack) {
    this.specialAttack = specialAttack;
    return this;
  }

  @Setter Creature workLocation(final Location aWorkLocation) {
    workLocation = aWorkLocation;
    return this;
  }

  private String describeArmor() {
    final CreatureType ct = CreatureType.inCharacter(this);
    // if (ct == null) {
    // return getArmor() + " - not in disguise.";
    // }
    return getArmor() + " - dressed as a " + ct.jobtitle(this) + ".";
  }

  private void getTitle(final StringBuilder str) {
    str.append(alignment.getTitle(juice));
  }

  // Determines the number of subordinates a creature may command
  private int maxSubordinates() {
    // brainwashed recruits can't recruit normally
    if (hasFlag(CreatureFlag.BRAINWASHED)) {
      return 0;
    }
    // Cap for founder
    final int recruitcap = (hire == null && alignment == Alignment.LIBERAL) ? 6 : 0;
    // Cap based on juice
    if (juice >= 500) {
      return recruitcap + 6;
    } else if (juice >= 200) {
      return recruitcap + 5;
    } else if (juice >= 100) {
      return recruitcap + 3;
    } else if (juice >= 50) {
      return recruitcap + 1;
    }
    return recruitcap;
  }

  private int nextJuice() {
    for (final int nj : nextJuice) {
      if (juice < nj) {
        return nj;
      }
    }
    return nextJuice[nextJuice.length - 1];
  }

  /** Determines the number of dates a creature has scheduled */
  private int scheduledDates() {
    int dates = 0;
    for (final Date d : i.dates) {
      // Does this creature have a list of dates scheduled?
      if (d.dater == this) {
        dates = d.dates.size();
        break;
      }
    }
    return dates;
  }

  /** Determines the number of recruitment meetings a creature has scheduled */
  private int scheduledMeetings() {
    int meetings = 0;
    for (final Recruit p : i.recruits) {
      // If meeting is with this creature
      if (p.recruiter == this) {
        meetings++;
      }
    }
    return meetings;
  }

  private boolean weaponIsConcealed() {
    boolean concealed = false;
    if (weapon().isArmed() && !isNaked()) {
      concealed = armor.concealsWeapon(weapon().weapon());
    }
    return concealed;
  }

  private static final int[] nextJuice = { 0, 10, 50, 100, 200, 500, 1000 };

  private static final long serialVersionUID = Game.VERSION;
}
