package lcs.android.game;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureName;
import lcs.android.creature.CreatureType;
import lcs.android.creature.Gender;
import lcs.android.creature.skill.Skill;
import lcs.android.items.Armor;
import lcs.android.items.Clip;
import lcs.android.items.Vehicle;
import lcs.android.items.Weapon;
import lcs.android.monthly.EndGame;
import lcs.android.politics.Alignment;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;
import lcs.android.site.Squad;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.Apartment;
import lcs.android.site.type.ApartmentUpscale;
import lcs.android.site.type.CrackHouse;
import lcs.android.site.type.Shelter;
import lcs.android.site.type.Tenement;
import lcs.android.util.Color;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Does all the prompting (On my 18th birthday, I...) at the start of a game. Does all the set-up
 * based on those choices */
public @NonNullByDefault class NewGame {
  private enum Recruits {
    GANG {
      @Override void create(final Creature boss, final Squad squad, final Location where) {
        for (int j = 0; j < 4; j++) {
          final Creature recruit = CreatureType.withType(CreatureType.valueOf("GANGMEMBER"));
          if (recruit.weapon().weapon().ideal()
              .equals(Game.type.weapon.get("WEAPON_AUTORIFLE_AK47"))
              || recruit.weapon().weapon().ideal().equals(Game.type.weapon.get("WEAPON_SMG_MP5"))
              || !recruit.weapon().isArmed()) {
            recruit.weapon().giveWeapon(new Weapon("WEAPON_SEMIPISTOL_9MM"), null)
                .takeClips(new Clip("CLIP_9", 4), 4);
            recruit.weapon().reload(false);
          }
          recruit.alignment(Alignment.LIBERAL);
          recruit.skill().attribute(
              Attribute.HEART,
              recruit.skill().getAttribute(Attribute.HEART, false)
                  + recruit.skill().getAttribute(Attribute.WISDOM, false) / 2);
          recruit.skill().attribute(Attribute.WISDOM,
              recruit.skill().getAttribute(Attribute.WISDOM, false) / 2);
          recruit.newHome(where).hire(boss);
          squad.add(recruit);
          i.pool.add(recruit);
        }
      }
    },
    NONE {
      @Override void create(final Creature boss, final Squad squad, final Location where) {
        // so alone...
      }
    };
    abstract void create(Creature boss, Squad squad, Location where);
  }

  private NewGame() {}

  /** During initial set-up of the game, whether the player chose to have maps of various buildings
   * downtown.
   * <p>
   * Needs to be set {@code false} after we've loaded in our locations; leaving it {@code true} would
   * cause players to have maps of eg. the police station if it had been closed down and re-opened
   * elsewhere. The maps aren't that prescient.
   * <p>
   * For this reason, it is also not serialized. */
  public static boolean hasmaps = false;

  private static AbstractSiteType base = AbstractSiteType.type(Shelter.class);

  private static boolean datingLawyer = false, gayLawyer = false;

  final private static String[] fate = { "The day I was born in 1984", "When I was bad",
      "In elementary school", "When I turned 10", "In junior high school",
      "Things were getting really bad", "Well, I knew it had reached a crescendo when",
      "I was only 15 when I ran away, and", "Life went on. On my 18th birthday",
      "For the past few years, I've been." };

  final private static String[] fate0 = {
      "the Polish priest Popieluszko was kidnapped by government agents.",
      "was the 3rd anniversary of the assassination attempt on Ronald Reagan.",
      "the Macintosh was introduced.",
      "the Nobel Peace Prize went to Desmond Tutu for opposition to apartheid.",
      "the Sandanista Front won the elections in Nicaragua." };

  final private static String[] fate1 = {
      "my parents grounded me and hid my toys, but I knew where they put them.",
      "my father beat me. I learned to take a punch earlier than most.",
      "I was sent to my room, where I studied quietly by myself, alone.",
      "my parents argued with each other about me, but I was never punished.",
      "my father lectured me endlessly, trying to make me think like him." };

  final private static String[] fate2 = { "I was mischievous, and always up to something.",
      "I had a lot of repressed anger. I hurt animals.",
      "I was at the head of the class, and I worked very hard.",
      "I was unruly and often fought with the other children.",
      "I was the class clown. I even had some friends." };

  final private static String[] fate3 = {
      "my parents divorced. Whenever I talked, they argued, so I stayed quiet.",
      "my parents divorced. Violently.",
      "my parents divorced. Acrimoniously. I once tripped over the paperwork!",
      "my parents divorced. Mom slept with the divorce lawyer.",
      "my parents divorced. It still hurts to read my old diary." };

  final private static String[] fate4 = {
      "I was into chemistry. I wanted to know what made the world tick.",
      "I played guitar in a grunge band. We sucked, but so did life.",
      "I drew things, a lot. I was drawing a world better than this.",
      "I played violent video games at home. I was a total outcast.",
      "I was obsessed with swords, and started lifting weights." };

  final private static String[] fate5 = {
      "when I stole my first car. I got a few blocks before I totaled it.",
      "and I went to live with my dad. He had been in Nam and he still drank.",
      "and I went completely goth. I had no friends and made costumes by myself.",
      "when I was sent to religious counseling, just stressing me out more.",
      "and I tried being a teacher's assistant. It just made me a target." };

  final private static String[] fate6 = {
      "I stole a cop car when I was only 14. I went to juvie for 6 months.",
      "my step mom shot her ex-husband, my dad, with a shotgun. She got off.",
      "I tried wrestling for a quarter, desperate to fit in.",
      "I got caught making out, and now I needed to be 'cured' of homosexuality.",
      "I resorted to controlling people. Had my own clique of outcasts." };

  final private static String[] fate7 = {
      "I started robbing houses: rich people only. I was fed up with their crap.",
      "I hung out with thugs and beat the shit out of people.",
      "I got a horrible job working fast food, smiling as people fed the man.",
      "I let people pay me for sex. I needed the money to survive.",
      "I volunteered for a left-wing candidate. It wasn't *real*, though, you know?" };

  final private static String[] fate8 = {
      "I got my hands on an old Beetle. It's still in great shape.",
      "I bought myself an assault rifle.", "I celebrated. I'd saved a thousand bucks!",
      "I went to a party and met a cool law student. We've been dating since.",
      "I managed to acquire secret maps of several major buildings downtown." };

  final private static String[] fate9 = {
      "stealing from Corporations. I know they're still keeping more secrets.",
      "a violent criminal. Nothing can change me, or stand in my way.",
      "taking college courses. I can see how much the country needs help.",
      "surviving alone, just like anyone. But we can't go on like this.",
      "writing my manifesto and refining my image. I'm ready to lead." };

  final private static String[][] fates = { fate0, fate1, fate2, fate3, fate4, fate5, fate6, fate7,
      fate8, fate9 };

  private static Recruits recruits = Recruits.NONE;

  @Nullable private static Vehicle startcar = null;

  /** Draws all of the prompts for what kind of game type we want to play, and prompts for the
   * player's name, gender, and history. */
  protected static void setupNewGame() {
    boolean classicmode = false;
    boolean strongccs = false;
    boolean nightmarelaws = false;
    setView(R.layout.generic);
    ui().text(getString(R.string.gtNewGameTitle)).bold().add();
    gamemode: while (true) {
      ui(R.id.gcontrol).button('a')
          .text((classicmode ? "[X] - " : "[ ] - ") + getString(R.string.gtA)).add();
      maybeAddButton(R.id.gcontrol, 'b', (strongccs ? "[X] - " : "[ ] - ")
          + getString(R.string.gtB), !classicmode);
      ui(R.id.gcontrol).button('c')
          .text((nightmarelaws ? "[X] - " : "[ ] - ") + getString(R.string.gtC)).add();
      ui(R.id.gcontrol).button('d').text(getString(R.string.gtD)).add();
      final int c = getch();
      clearChildren(R.id.gcontrol);
      switch (c) {
      case 'a':
        classicmode = !classicmode;
        if (classicmode) {
          strongccs = false;
        }
        break;
      case 'b':
        strongccs = !strongccs;
        break;
      case 'c':
        nightmarelaws = !nightmarelaws;
        break;
      case 'd':
        break gamemode;
      default:
      }
    }
    if (nightmarelaws) {
      nightmare();
    }
    if (classicmode) {
      i.endgameState = EndGame.CCS_DEFEATED;
    } else if (strongccs) {
      i.endgameState = EndGame.CCS_ATTACKS;
    }
    determineGameAgenda();
    makeCharacter();
  }

  private static void aNewConservativeEra() {
    setView(R.layout.generic);
    ui().text(getString(R.string.newEraTitle)).bold().add();
    ui().text(format(R.string.newEra1, i.score.date.year())).add();
    ui().text(
        format(R.string.newEra2, CreatureName.generateName(Gender.WHITEMALEPATRIARCH),
            i.execs.get(Exec.PRESIDENT).toString())).add();
    ui().text(getString(R.string.newEra3)).add();
    ui().text(format(R.string.newEra4, i.execs.get(Exec.PRESIDENT).toString())).color(Color.RED)
        .add();
    ui().text(getString(R.string.newEra5)).add();
    ui().text(getString(R.string.newEra6)).add();
  }

  private static void dayIWasBorn(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().attribute(Attribute.AGILITY, 2);
      newcr.birthMonth(10).birthDay(19);
      break;
    case 'b':
      newcr.skill().attribute(Attribute.STRENGTH, 2);
      newcr.birthMonth(3).birthDay(3);
      break;
    case 'c':
      newcr.skill().attribute(Attribute.INTELLIGENCE, 2);
      newcr.birthMonth(1).birthDay(24);
      break;
    case 'd':
      newcr.skill().attribute(Attribute.HEART, 2);
      newcr.birthMonth(10).birthDay(16);
      break;
    case 'e':
      newcr.skill().attribute(Attribute.CHARISMA, 2);
      newcr.birthMonth(9).birthDay(4);
      break;
    default:
    }
    newcr.age(i.score.date.year() - 1984);
    if (i.score.date.month() < newcr.birthMonth() || i.score.date.month() == newcr.birthMonth()
        && i.score.date.day() < newcr.birthDay()) {
      newcr.age(newcr.age() - 1);
    }
  }

  private static void determineGameAgenda() {
    while (true) {
      setView(R.layout.generic);
      ui().text(getString(R.string.gaAgenda)).bold().add();
      ui(R.id.gcontrol).button('a').text(getString(R.string.gaA)).add();
      ui(R.id.gcontrol).button('b').text(getString(R.string.gaB)).add();
      final int c = getch();
      switch (c) {
      case 'a':
        i.wincondition = WinConditions.ELITE;
        return;
      case 'b':
        i.wincondition = WinConditions.EASY;
        return;
      default:
      }
    }
  }

  private static void eighteenthBirthday(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      startcar = new Vehicle("VEHICLE_BUG");
      i.vehicle.add(startcar);
      newcr.prefCar(startcar);
      break;
    case 'b':
      newcr.weapon().giveWeapon(new Weapon("WEAPON_AUTORIFLE_AK47"), null);
      newcr.weapon().takeClips(new Clip("CLIP_ASSAULT", 9), 9);
      break;
    case 'c':
      i.ledger.forceFunds(1000);
      break;
    case 'd':
      datingLawyer = true;
      break;
    case 'e':
      hasmaps = true;
      break;
    default:
    }
  }

  private static void inElementarySchool(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.DISGUISE, 1);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.PSYCHOLOGY, 1);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      newcr.skill().attribute(Attribute.HEART, -1);
      newcr.skill().attribute(Attribute.STRENGTH, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.WRITING, 1);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 1);
      break;
    case 'd':
      newcr.skill().skill(Skill.HANDTOHAND, 1);
      newcr.skill().attribute(Attribute.STRENGTH, 1);
      break;
    case 'e':
      newcr.skill().skill(Skill.PERSUASION, 1);
      newcr.skill().attribute(Attribute.CHARISMA, 1);
      break;
    default:
    }
  }

  private static void inJuniorHighSchool(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.SCIENCE, 2);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 2);
      break;
    case 'b':
      newcr.skill().skill(Skill.MUSIC, 2);
      newcr.skill().attribute(Attribute.CHARISMA, 2);
      break;
    case 'c':
      newcr.skill().skill(Skill.ART, 2);
      newcr.skill().attribute(Attribute.HEART, 2);
      break;
    case 'd':
      newcr.skill().skill(Skill.COMPUTERS, 2);
      newcr.skill().attribute(Attribute.AGILITY, 2);
      break;
    case 'e':
      newcr.skill().skill(Skill.SWORD, 2);
      newcr.skill().attribute(Attribute.STRENGTH, 1);
      break;
    default:
    }
  }

  private static void iWasOnlyFifteen(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.SECURITY, 1);
      newcr.skill().skill(Skill.STEALTH, 1);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.HANDTOHAND, 2);
      newcr.skill().attribute(Attribute.STRENGTH, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.BUSINESS, 2);
      newcr.skill().attribute(Attribute.CHARISMA, 1);
      break;
    case 'd':
      newcr.skill().skill(Skill.SEDUCTION, 2);
      newcr.skill().attribute(Attribute.CHARISMA, 1);
      break;
    case 'e':
      newcr.skill().skill(Skill.LAW, 1);
      newcr.skill().skill(Skill.PERSUASION, 1);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 1);
      break;
    default:
    }
  }

  private static void makeCharacter() {
    final Creature newcr = CreatureType.withType("LEADER");
    i.pool.add(newcr);
    String male = CreatureName.firstName(Gender.MALE);
    String female = CreatureName.firstName(Gender.FEMALE);
    String last = CreatureName.lastname();
    Gender gender = i.rng.choice(Gender.MALE, Gender.FEMALE);
    newcr.genderConservative(gender);
    newcr.giveArmor(new Armor("ARMOR_CLOTHES"), null);
    boolean choices = true;
    founder: while (true) {
      setView(R.layout.founder);
      if (gender == Gender.MALE) {
        setText(R.id.first, male);
        setColor(R.id.sex, Color.CYAN);
      } else {
        setText(R.id.first, female);
        setColor(R.id.sex, Color.MAGENTA);
      }
      setText(R.id.last, last);
      setText(R.id.sex, gender.toString());
      setText(R.id.history, choices ? R.string.founderChoose : R.string.founderFate);
      setColor(R.id.history, choices ? Color.GREEN : Color.RED);
      final int c = getch();
      switch (c) {
      case 'a':
        if (gender == Gender.MALE) {
          male = CreatureName.firstName(gender);
        } else {
          female = CreatureName.firstName(gender);
        }
        break;
      case 'b':
        last = CreatureName.lastname();
        break;
      case 'c':
        gender = gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
        break;
      case 'd':
        choices = !choices;
        break;
      case 'e':
        break founder;
      default:
      }
    }
    newcr.genderAndName(gender, new CreatureName(gender, gender == Gender.MALE ? male : female,
        last));
    setView(R.layout.generic);
    ui().text("Insight Into A Revolution: My Traumatic Childhood").bold().add();
    for (int j = 0; j < fates.length; ++j) {
      if (j == 0) {
        ui().text(format("The doctor said I was a %s.", gender == Gender.MALE ? "boy" : "girl"))
            .add();
        ui().text(format("My parents named me %s.", newcr.properName())).add();
      }
      int c = ' ';
      if (choices) {
        ui(R.id.gcontrol).text(fate[j] + "...").add();
        int y = 'a';
        for (final String x : fates[j]) {
          ui(R.id.gcontrol).button(y++).text(x).add();
        }
        do {
          c = getch();
        } while (c == ' ');
        clearChildren(R.id.gcontrol);
      } else {
        c = i.rng.choice('a', 'b', 'c', 'd', 'e');
      }
      ui().text(fate[j] + " " + fates[j][c - 'a']).add();
      switch (j) {
      case 0:
        dayIWasBorn(newcr, c);
        break;
      case 1:
        whenIWasBad(newcr, c);
        break;
      case 2:
        inElementarySchool(newcr, c);
        break;
      case 3:
        whenITurnedTen(newcr, c);
        break;
      case 4:
        inJuniorHighSchool(newcr, c);
        break;
      case 5:
        thingsWereGettingReallyBad(newcr, c);
        break;
      case 6:
        reachedACrescendo(newcr, c);
        break;
      case 7:
        iWasOnlyFifteen(newcr, c);
        break;
      case 8:
        eighteenthBirthday(newcr, c);
        break;
      case 9:
      default:
        pastFewYears(newcr, c);
        break;
      }
    }
    ui().text(format("I live in %s, and it's about to experience real change.", i.homeCityName))
        .add();
    waitOnOK();
    aNewConservativeEra();
    new Xml("location.xml").init(Location.CONFIG).close();
    hasmaps = false;
    final Squad newsq = new Squad();
    // newsq.id = 0;
    // i.curSquadId++;
    newsq.add(newcr);
    newsq.name("The Liberal Crime Squad");
    for (final Location l : i.location) {
      if (l.type() == base) {
        newcr.newHome(l);
        if (startcar != null) {
          startcar.setLocation(l);
        }
        l.renting(CrimeSquad.LCS);
        l.rent(base.rent());
        if (base.isType(CrackHouse.class)) {
          l.lcs().compoundStores += 100;
        }
        l.lcs().newRental = true;
        recruits.create(newcr, newsq, l);
      }
    }
    // newcr.juice=0;
    i.squad.add(newsq);
    i.activeSquad = newsq;
    if (datingLawyer) {
      makeLawyer(newcr, gayLawyer);
    }
    waitOnOK();
    newcr.name(query(R.string.foundername, newcr.properName()));
  }

  private static void makeLawyer(final Creature newcr, final boolean gaylawyer) {
    final Creature lawyer = CreatureType.withType("LAWYER");
    /* Make sure lawyer is of the appropriate gender for dating the main character; opposite sex by
     * default, same sex if the option was chosen that mentions homosexuality */
    if (gaylawyer) {
      lawyer.gender(newcr.genderConservative());
    } else {
      if (newcr.genderConservative() == Gender.MALE) {
        lawyer.gender(Gender.FEMALE);
      }
      if (newcr.genderConservative() == Gender.FEMALE) {
        lawyer.gender(Gender.MALE);
      }
    }
    // Ensure the lawyer has good heart/wisdom stats
    if (lawyer.skill().getAttribute(Attribute.HEART, false) < newcr.skill().getAttribute(
        Attribute.HEART, false) - 2) {
      lawyer.skill().attribute(Attribute.HEART, +2);
    }
    lawyer.skill().attribute(Attribute.WISDOM, 1);
    lawyer.addFlag(CreatureFlag.SLEEPER);
    lawyer.addFlag(CreatureFlag.LOVE_SLAVE);
    lawyer.alignment(Alignment.LIBERAL).age(28).base(lawyer.workLocation());
    lawyer.infiltration(30);
    lawyer.workLocation().interrogated(true);
    lawyer.hire(newcr);
    i.pool.add(lawyer);
    lawyer.location(lawyer.workLocation());
  }

  private static void nightmare() {
    for (final Issue l : Issue.values()) {
      i.issue(l).law(Alignment.ARCHCONSERVATIVE);
    }
    for (final Issue v : Issue.values()) {
      if (v == Issue.LIBERALCRIMESQUAD || v == Issue.LIBERALCRIMESQUADPOS
          || v == Issue.CONSERVATIVECRIMESQUAD) {
        continue;
      }
      i.issue(v).attitude(i.rng.nextInt(20));
    }
    i.senate.set(55, 15, 10, 17, 3);
    i.house.set(220, 130, 50, 25, 10);
    final Alignment[] court = { Alignment.ARCHCONSERVATIVE, Alignment.ARCHCONSERVATIVE,
        Alignment.ARCHCONSERVATIVE, Alignment.ARCHCONSERVATIVE, Alignment.ARCHCONSERVATIVE,
        Alignment.CONSERVATIVE, Alignment.CONSERVATIVE, Alignment.LIBERAL, Alignment.ELITELIBERAL };
    for (int c = 0; c < i.supremeCourt.length; c++) {
      i.supremeCourt[c].alignment(court[c]);
    }
  }

  private static void pastFewYears(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().attribute(Attribute.INTELLIGENCE, 2);
      newcr.skill().attribute(Attribute.AGILITY, 2);
      newcr.skill().skill(Skill.SECURITY, 2);
      newcr.skill().skill(Skill.STEALTH, 2);
      newcr.type(CreatureType.valueOf("THIEF"));
      base = AbstractSiteType.type(ApartmentUpscale.class);
      i.ledger.forceFunds(i.ledger.funds() + 500);
      newcr.giveArmor(new Armor("ARMOR_BLACKCLOTHES"), null);
      break;
    case 'b':
      newcr.skill().attribute(Attribute.AGILITY, 2);
      newcr.skill().attribute(Attribute.HEALTH, 2);
      newcr.skill().skill(Skill.RIFLE, 2);
      newcr.skill().skill(Skill.PISTOL, 2);
      newcr.type(CreatureType.valueOf("GANGMEMBER"));
      base = AbstractSiteType.type(CrackHouse.class);
      recruits = Recruits.GANG;
      break;
    case 'c':
      newcr.skill().attribute(Attribute.INTELLIGENCE, 4);
      newcr.skill().skill(Skill.SCIENCE, 2);
      newcr.skill().skill(Skill.COMPUTERS, 2);
      newcr.skill().skill(Skill.WRITING, 2);
      newcr.skill().skill(Skill.TEACHING, 2);
      newcr.skill().skill(Skill.BUSINESS, 1);
      newcr.skill().skill(Skill.LAW, 1);
      newcr.type(CreatureType.valueOf("COLLEGESTUDENT"));
      base = AbstractSiteType.type(Apartment.class);
      break;
    case 'd':
      newcr.skill().attribute(Attribute.INTELLIGENCE, 1);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      newcr.skill().attribute(Attribute.HEALTH, 2);
      newcr.skill().skill(Skill.FIRSTAID, 2);
      newcr.skill().skill(Skill.STREETSENSE, 2);
      newcr.type(CreatureType.valueOf("HSDROPOUT"));
      base = AbstractSiteType.type(Shelter.class);
      for (final Attribute a : Attribute.values()) {
        newcr.skill().attribute(a, 1);
      }
      break;
    case 'e':
      newcr.skill().attribute(Attribute.CHARISMA, 2);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 2);
      newcr.skill().skill(Skill.LAW, 1);
      newcr.skill().skill(Skill.WRITING, 1);
      newcr.skill().skill(Skill.PERSUASION, 2);
      newcr.type(CreatureType.valueOf("POLITICALACTIVIST"));
      base = AbstractSiteType.type(Tenement.class);
      i.ledger.forceFunds(i.ledger.funds() + 50);
      newcr.juice(newcr.juice() + 50);
      break;
    default:
    }
  }

  private static void reachedACrescendo(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.DRIVING, 1);
      newcr.skill().skill(Skill.SECURITY, 1);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.SHOTGUN, 2);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.HANDTOHAND, 2);
      newcr.skill().attribute(Attribute.STRENGTH, 1);
      break;
    case 'd':
      newcr.skill().skill(Skill.SEDUCTION, 1);
      newcr.skill().skill(Skill.RELIGION, 1);
      newcr.skill().attribute(Attribute.HEART, 1);
      gayLawyer = true;
      break;
    case 'e':
      newcr.skill().skill(Skill.PERSUASION, 2);
      newcr.skill().attribute(Attribute.CHARISMA, 1);
      break;
    default:
    }
  }

  private static void thingsWereGettingReallyBad(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.DRIVING, 1);
      newcr.skill().skill(Skill.SECURITY, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.SHOTGUN, 1);
      newcr.skill().skill(Skill.RIFLE, 1);
      newcr.skill().skill(Skill.PSYCHOLOGY, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.TAILORING, 2);
      break;
    case 'd':
      newcr.skill().skill(Skill.RELIGION, 1);
      newcr.skill().skill(Skill.PSYCHOLOGY, 1);
      break;
    case 'e':
      newcr.skill().skill(Skill.TEACHING, 2);
      break;
    default:
    }
  }

  private static void whenITurnedTen(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.STEALTH, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.HANDTOHAND, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.LAW, 1);
      break;
    case 'd':
      newcr.skill().skill(Skill.SEDUCTION, 1);
      break;
    case 'e':
      newcr.skill().skill(Skill.WRITING, 1);
      break;
    default:
    }
  }

  private static void whenIWasBad(final Creature newcr, final int c) {
    switch (c) {
    case 'a':
      newcr.skill().skill(Skill.SECURITY, 1);
      newcr.skill().attribute(Attribute.AGILITY, 1);
      break;
    case 'b':
      newcr.skill().skill(Skill.HANDTOHAND, 1);
      newcr.skill().attribute(Attribute.HEALTH, 1);
      break;
    case 'c':
      newcr.skill().skill(Skill.WRITING, 1);
      newcr.skill().attribute(Attribute.INTELLIGENCE, 1);
      break;
    case 'd':
      newcr.skill().skill(Skill.PERSUASION, 1);
      newcr.skill().attribute(Attribute.HEART, 1);
      break;
    case 'e':
      newcr.skill().skill(Skill.PSYCHOLOGY, 1);
      newcr.skill().attribute(Attribute.CHARISMA, 1);
      break;
    default:
    }
  }
}
