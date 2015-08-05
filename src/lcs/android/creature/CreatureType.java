package lcs.android.creature;

import static lcs.android.creature.Attribute.*;
import static lcs.android.creature.skill.Skill.*;
import static lcs.android.game.Game.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.health.Animal;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.game.Range;
import lcs.android.items.ArmorType;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.site.Alienation;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.BarAndGrill;
import lcs.android.site.type.BombShelter;
import lcs.android.site.type.Bunker;
import lcs.android.util.Getter;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** A specific type of Creature which we might meet.
 * @author addie */
public @NonNullByDefault class CreatureType implements Configurable, Serializable {
  private enum Age {
    CHILD(7, 4),
    COLLEGE(18, 6),
    DOGYEARS(2, 5),
    GRADUATE(26, 34),
    MATURE(20, 40),
    MIDDLEAGED(35, 25),
    SENIOR(65, 30),
    TEENAGER(14, 4),
    YOUNGADULT(18, 18),
    YOUNGISH(14, 12);
    Age(final int base, final int var) {
      this.base = base;
      this.var = var;
    }

    int base, var;

    int age() {
      return base + i.rng.nextInt(var);
    }
  }

  private static class CTSerialProxy implements Serializable {
    private CTSerialProxy(final String idname) {
      this.idname = idname;
    }

    private final String idname;

    @Override public String toString() {
      return idname;
    }

    Object readResolve() {
      return Game.type.creature.get(idname);
    }

    private static final long serialVersionUID = Game.VERSION;
  }

  private enum Mode {
    ARMOR,
    ATTCAP,
    ATTRIBUTE,
    CRIMES,
    DEFAULT,
    NAMES,
    SKILL,
    SPECIALS,
    WEAPON,
    WORKLOCATION
  }

  private enum Specials {
    BOUNCER {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.mode() == GameMode.SITE && i.site.current().highSecurity() != 0) {
          cr.name("Enforcer");
          cr.skill().setSkill(CLUB, i.rng.nextInt(3) + 3);
        }
        cr.weapon().giveCreatureWeapon(
            ct.weapontypes.get(i.issue(Issue.GUNCONTROL).law().trueOrdinal() + 2));
        if (i.site.type().disguisesite()) {
          cr.alignment(Alignment.CONSERVATIVE);
          cr.infiltration(i.rng.nextInt(40));
        } else {
          cr.alignment(Alignment.MODERATE);
        }
      }
    },
    CARRIES38 {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon("REVOLVER_38:38=4");
        }
      }
    },
    CCSWEAPONS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        final int level = i.rng.nextInt(5) + i.endgameState.ordinal();
        cr.weapon().giveCreatureWeapon(ct.weapontypes.get(level));
        cr.giveArmor(ct.armorTypes.get(level));
      }
    },
    /* Weapon assignment macros */
    CIVILIANWEAPON {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.CONSERVATIVE && i.rng.chance(30)) {
          cr.weapon().giveCreatureWeapon("REVOLVER_38:38=4");
        } else if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE) {
          if (i.rng.chance(10)) {
            cr.weapon().giveCreatureWeapon("SEMIPISTOL_9MM:9=4");
          } else if (i.rng.chance(9)) {
            cr.weapon().giveCreatureWeapon("SEMIPISTOL_45:45=4");
          }
        }
      }
    },
    CONSERVATIVEJUDGE {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon("REVOLVER_44:44=4");
        } else if (i.rng.likely(2)) {
          cr.weapon().giveCreatureWeapon("GAVEL");
        }
      }
    },
    COPWEAPONS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon("SMG_MP5:SMG=4");
        } else if (i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon("SEMIPISTOL_9MM:9=6");
        } else if (i.rng.chance(2)) {
          cr.weapon().giveCreatureWeapon("SHOTGUN_PUMP:BUCKSHOT=4");
        } else {
          cr.weapon().giveCreatureWeapon("NIGHTSTICK");
        }
      }
    },
    CORPORATECEO {
      @Override void apply(final Creature cr, final CreatureType ct) {
        cr.properName(new CreatureName(Gender.WHITEMALEPATRIARCH, Animal.HUMAN));
        cr.name("CEO " + cr.properName());
      }
    },
    FEMALEOFSORTS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.rng.likely(7)) {
          cr.gender(Gender.FEMALE);
        } else if (i.rng.likely(3)) {
          cr.genderLiberal(Gender.FEMALE);
        }
      }
    },
    FIREEMERGENCY {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.site.alarm()) {
          cr.giveArmor("BUNKERGEAR");
        }
      }
    },
    FORCEGENDER {
      @Override void apply(final Creature cr, final CreatureType ct) {
        cr.gender(ct.gender);
      }
    },
    GANGWEAPONS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.rng.chance(20) || i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE
            && i.rng.chance(5)) {
          cr.weapon().giveCreatureWeapon("AUTORIFLE_AK47:ASSAULT=3");
        } else if (i.rng.chance(16)
            || i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.chance(5)) {
          cr.weapon().giveCreatureWeapon("SMG_MP5:SMG=4");
        } else if (i.rng.chance(15)) {
          cr.weapon().giveCreatureWeapon("SEMIPISTOL_45:45=4");
        } else if (i.rng.chance(10)) {
          cr.weapon().giveCreatureWeapon("SHOTGUN_PUMP:BUCKSHOT=4");
        } else if (i.rng.chance(4)) {
          cr.weapon().giveCreatureWeapon("SEMIPISTOL_9MM:9=4");
        } else if (i.rng.chance(2)) {
          cr.weapon().giveCreatureWeapon("REVOLVER_38:38=4");
        } else {
          cr.weapon().giveCreatureWeapon("KNIFE");
        }
      }
    },
    GENETICMONSTER {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.site.current().type().isType("CORPORATE_HOUSE")) {
          cr.name("Pet ");
        } else {
          cr.name("");
        }
        switch (i.rng.nextInt(11)) {
        default:
          cr.name(cr.toString() + "Genetic Monster");
          break;
        case 1: {
          cr.name(cr.toString() + "Flaming Rabbit");
          cr.specialAttack(SpecialAttacks.FLAME);
          break;
        }
        case 2:
          cr.name(cr.toString() + "Genetic Nightmare");
          break;
        case 3:
          cr.name(cr.toString() + "Mad Cow");
          break;
        case 4: {
          cr.name(cr.toString() + "Giant Mosquito");
          cr.specialAttack(SpecialAttacks.SUCK);
          break;
        }
        case 5:
          cr.name(cr.toString() + "Six-legged Pig");
          break;
        case 6:
          cr.name(cr.toString() + "Purple Gorilla");
          break;
        case 7:
          cr.name(cr.toString() + "Warped Bear");
          break;
        case 8:
          cr.name(cr.toString() + "Writhing Mass");
          break;
        case 9:
          cr.name(cr.toString() + "Something Bad");
          break;
        case 10:
          cr.name(cr.toString() + "Pink Elephant");
          break;
        }
      }
    },
    HICKWEAPONS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.chance(2)
            || i.rng.chance(10)) {
          cr.weapon().giveCreatureWeapon("SHOTGUN_PUMP:BUCKSHOT=4");
        } else if (i.rng.chance(2)) {
          cr.weapon().giveCreatureWeapon("TORCH");
        } else {
          cr.weapon().giveCreatureWeapon("PITCHFORK");
        }
      }
    },
    ILLEGALALIEN {
      @Override void apply(final Creature cr, final CreatureType ct) {
        cr.addFlag(CreatureFlag.ILLEGAL_ALIEN);
      }
    },
    LIKESBOOMSTICKS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE && i.rng.likely(3)) {
          cr.weapon().giveCreatureWeapon(ct.weapontypes.get(0));
        } else if (i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon(ct.weapontypes.get(1));
        } else {
          cr.weapon().giveCreatureWeapon(ct.weapontypes.get(2));
        }
      }
    },
    MERCWEAPONS {
      @Override void apply(final Creature cr, final CreatureType ct) {
        cr.name("Mercenary");
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE) {
          cr.weapon().giveCreatureWeapon("AUTORIFLE_M16:ASSAULT=7");
        } else {
          cr.weapon().giveCreatureWeapon("SEMIRIFLE_AR15:ASSAULT=7");
        }
      }
    },
    NOTCONSERVATIVE {
      @Override void apply(final Creature cr, final CreatureType ct) {
        cr.alignment(i.rng.choice(Alignment.MODERATE, Alignment.LIBERAL));
      }
    },
    PRISONER {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.rng.chance(10)) {
          // Thief
          cr.skill().setSkill(SECURITY, i.rng.nextInt(5) + 3);
          cr.skill().setSkill(DISGUISE, i.rng.nextInt(5) + 3);
          cr.skill().setSkill(STEALTH, i.rng.nextInt(5) + 3);
          // cr.set_skill(THEFT,i.rng.getInt(5)+3);
          cr.type(CreatureType.valueOf("THIEF"));
          cr.age(Age.MATURE.age());
        } else {
          switch (i.rng.nextInt(5)) {
          default:
            // Gang member
            cr.skill().setSkill(PISTOL, i.rng.nextInt(2) + 1);
            cr.skill().setSkill(SHOTGUN, i.rng.nextInt(2) + 1);
            cr.skill().setSkill(RIFLE, i.rng.nextInt(2) + 1);
            cr.type(CreatureType.valueOf("GANGMEMBER"));
            cr.age(Age.YOUNGADULT.age());
            break;
          case 1:
            // Prostitute
            cr.skill().setSkill(PERSUASION, i.rng.nextInt(4) + 2);
            cr.skill().setSkill(SEDUCTION, i.rng.nextInt(4) + 2);
            cr.type(CreatureType.valueOf("PROSTITUTE"));
            cr.age(Age.YOUNGADULT.age());
            break;
          case 2:
            // Crack head
            cr.skill().setSkill(Skill.SHOOTINGUP, 10);
            cr.type(CreatureType.valueOf("CRACKHEAD"));
            cr.age(Age.YOUNGADULT.age());
            break;
          case 3:
            // Teenager
            cr.age(Age.TEENAGER.age());
            cr.type(CreatureType.valueOf("TEENAGER"));
            break;
          case 4:
            // HS Dropout
            cr.age(Age.TEENAGER.age());
            cr.type(CreatureType.valueOf("HSDROPOUT"));
            break;
          }
        }
      }
    },
    SECURITYWEAPON {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (i.issue(Issue.GUNCONTROL).law() == Alignment.ARCHCONSERVATIVE) {
          cr.weapon().giveCreatureWeapon("SMG_MP5:SMG=4"); // oops, was SGM
        } else if (i.issue(Issue.GUNCONTROL).law() != Alignment.ELITELIBERAL && i.rng.chance(3)) {
          cr.weapon().giveCreatureWeapon("REVOLVER_38:38=4");
        } else {
          cr.weapon().giveCreatureWeapon("NIGHTSTICK");
        }
      }
    },
    WEARSCHEAPCLOTHES {
      @Override void apply(final Creature cr, final CreatureType ct) {
        if (cr.genderLiberal() == Gender.MALE || i.rng.chance(2)) {
          cr.giveArmor("CHEAPSUIT");
        } else {
          cr.giveArmor("CHEAPDRESS");
        }
      }
    };
    abstract void apply(Creature cr, CreatureType ct);
  }

  private Age age = Age.MIDDLEAGED;

  private Alignment alignment = Alignment.MODERATE;

  private Animal animalGloss = Animal.HUMAN;

  private final List<ArmorType> armorTypes = new ArrayList<ArmorType>();

  private Range attnum = new Range("40");

  private final Map<Attribute, Range> attribute = new EnumMap<Attribute, Range>(Attribute.class);

  private final Map<Attribute, Range> attributeCap = new EnumMap<Attribute, Range>(Attribute.class);

  private boolean ccs;

  @Nullable private Skill coreSkill = null;

  private Gender gender = Gender.MALE;

  private String idname = "";

  private Range infiltration = Range.of(0);

  private int influence = 2;

  private Range juice = Range.of(0);

  private String lcsname = "";

  private Mode mode = Mode.DEFAULT;

  private Range money = Range.of(0);

  private final List<String> names = new ArrayList<String>();

  private CheckDifficulty observationSkill = CheckDifficulty.VERYEASY;

  private final List<Crime> offences = new ArrayList<Crime>();

  private boolean police;

  private boolean receptive = false;

  private CheckDifficulty seduce = CheckDifficulty.HARD;

  private final Map<Skill, Range> skills = new HashMap<Skill, Range>();

  private final List<Specials> specials = new ArrayList<Specials>();

  private boolean stub = false;

  private Activity teaches = Activity.TEACH_POLITICS;

  @Nullable private Uniform uniform;

  private final List<WeaponAssigner> weapontypes = new ArrayList<WeaponAssigner>();

  private final Set<AbstractSiteType> workLocation = new HashSet<AbstractSiteType>();

  /** What animal (etc) this is.
   * @return */
  @Getter public Animal animal() {
    return animalGloss;
  }

  /** Any particular skill highly-associated with the job, for when influencing public opinion.
   * @return A skill, may be null. */
  @Nullable public Skill coreSkill() {
    return coreSkill;
  }

  /** ID name of this CreatureType, for serialization.
   * @return */
  public String idName() {
    return idname;
  }

  /** Influence associated with this CreatureType while working as a sleeper. Up to 20 for corporate
   * CEO.
   * @return an added influence value. */
  public int influence() {
    return influence;
  }

  /** Is a police type.
   * @return true if police. */
  public boolean isPolice() {
    return police;
  }

  /** Suitable job title for this CreatureType. Might be pseudorandom from several, if it's a CCS
   * disguise type.
   * @param c Which creature to give the job title for (random choices are based on their birthday,
   *          so they don't change).
   * @return a suitable job title */
  public String jobtitle(final Creature c) {
    if (lcsname.length() != 0 && c.squad()!= null) {
      return lcsname;
    }
    if (names.size() == 1) {
      return names.get(0);
    } else if (names.size() == 0) {
      return "Unemployed";
    }
    return names.get(c.birthDay() % names.size());
  }

  /** Whether the CreatureType id is any of the following types.
   * @param types
   * @return true if so. */
  public boolean ofType(final String... types) {
    for (final String t : types) {
      if (idname.equals(t)) {
        return true;
      }
    }
    return false;
  }

  /** How hard this type is to seduce (less for hippies - free love! - but lots for the priesthood).
   * @return a CheckDifficulty. */
  public CheckDifficulty seductionDifficulty() {
    return seduce;
  }

  /** What activity this CreatureType can teach, if any
   * @return an activity, may be null. */
  public Activity teaches() {
    return teaches;
  }

  @Override public String toString() {
    if (names.size() > 0) {
      return names.get(0);
    }
    return "Unemployed";
  }

  /** Places where these creatures might work.
   * @return a set, which may be empty. */
  public List<Location> workLocations() {
    final List<Location> rval = new ArrayList<Location>();
    for (final AbstractSiteType ast : workLocation) {
      rval.add(ast.getLocation());
    }
    return rval;
  }

  @Override public Configurable xmlChild(final String value) {
    if ("worklocation".equals(value)) {
      mode = Mode.WORKLOCATION;
    } else if ("skill".equals(value)) {
      mode = Mode.SKILL;
    } else if ("attribute".equals(value)) {
      mode = Mode.ATTRIBUTE;
    } else if ("armor".equals(value)) {
      mode = Mode.ARMOR;
    } else if ("weapon".equals(value)) {
      mode = Mode.WEAPON;
    } else if ("attcap".equals(value)) {
      mode = Mode.ATTCAP;
    } else if ("special".equals(value)) {
      mode = Mode.SPECIALS;
    } else if ("crime".equals(value)) {
      mode = Mode.CRIMES;
    } else if ("name".equals(value)) {
      mode = Mode.NAMES;
      if (names.size() > 0) {
        Log.w(Game.LCS, "Double-dip naming:" + idname);
      }
    } else if ("uniform".equals(value)) {
      uniform = new Uniform();
      return uniform;
    } else {
      Log.w(Game.LCS, "No such creaturetype mode:" + value + " (" + this + ")");
    }
    return this;
  }

  @Override public void xmlFinishChild() {
    // if (mode == Mode.DEFAULT) {
    // if (age == null) {
    // Log.w(Game.LCS, "No age set for " + this);
    // }
    // }
    mode = Mode.DEFAULT;
  }

  @Override public void xmlSet(final String key, final String value) {
    switch (mode) {
    case DEFAULT:
      if ("age".equals(key)) {
        age = Age.valueOf(Xml.getText(value));
      } else if ("gender".equals(key)) {
        gender = Gender.valueOf(Xml.getText(value));
      } else if ("alignment".equals(key)) {
        alignment = Enum.valueOf(Alignment.class, Xml.getText(value));
      } else if ("money".equals(key)) {
        money = new Range(Xml.getText(value));
      } else if ("juice".equals(key)) {
        juice = new Range(Xml.getText(value));
      } else if ("infiltration".equals(key)) {
        infiltration = new Range(Xml.getText(value));
      } else if ("ccs".equals(key)) {
        ccs = Xml.getBoolean(value);
      } else if ("animalgloss".equals(key)) {
        animalGloss = Animal.valueOf(value);
      } else if ("lcsname".equals(key)) {
        lcsname = Xml.getText(value);
      } else if ("attnum".equals(key)) {
        attnum = new Range(Xml.getText(value));
      } else if ("teaches".equals(key)) {
        teaches = Activity.valueOf(value);
      } else if ("police".equals(key)) {
        police = Xml.getBoolean(value);
      } else if ("observationskill".equals(key)) {
        observationSkill = CheckDifficulty.valueOf(value);
      } else if ("receptive".equals(key)) {
        receptive = Xml.getBoolean(value);
      } else if ("coreskill".equals(key)) {
        coreSkill = Skill.valueOf(value);
      } else if ("influence".equals(key)) {
        influence = Xml.getInt(value);
      } else if ("seduce".equals(key)) {
        seduce = CheckDifficulty.valueOf(value);
      } else if ("stub".equals(key)) {
        stub = Xml.getBoolean(value);
      } else {
        Log.w(Game.LCS, "Oops: " + key + "=" + value);
      }
      break;
    case ARMOR:
      if ("item".equals(key)) {
        final ArmorType at = Game.type.armor.get("ARMOR_" + Xml.getText(value));
        if (at == null) {
          Log.e(Game.LCS, "No such armortype:" + Xml.getText(value));
        } else {
          armorTypes.add(at);
        }
      } else {
        Log.w(Game.LCS, "Bad key:" + key + "=" + value);
      }
      break;
    case WEAPON:
      if ("item".equals(key)) {
        weapontypes.add(new WeaponAssigner(Xml.getText(value)));
      } else {
        Log.w(Game.LCS, "Bad key:" + key + "=" + value);
      }
      break;
    case NAMES:
      if ("item".equals(key)) {
        names.add(Xml.getText(value));
      } else {
        Log.w(Game.LCS, "Bad key:" + key + "=" + value);
      }
      break;
    case SPECIALS:
      if ("item".equals(key)) {
        specials.add(Specials.valueOf(Xml.getText(value)));
      } else {
        Log.w(Game.LCS, "Bad key:" + key + "=" + value);
      }
      break;
    case WORKLOCATION:
      if ("item".equals(key)) {
        workLocation.add(AbstractSiteType.type(value));
      } else {
        Log.w(Game.LCS, "Bad key:" + key + "=" + value);
      }
      break;
    case SKILL:
      skills.put(Skill.valueOf(key), new Range(value));
      break;
    case ATTCAP:
      attributeCap.put(Attribute.valueOf(key), new Range(value));
      break;
    case ATTRIBUTE:
      attribute.put(Attribute.valueOf(key), new Range(value));
      break;
    case CRIMES:
      if (value.length() != 0) {
        offences.add(Crime.valueOf(value));
      } else {
        offences.add(null);
      }
      break;
    default:
      Log.e("LCS", "CreatureType xmlSet default:" + mode);
    }
  }

  /** Creature's observation skill.
   * @return a CheckDifficulty */
  @Getter CheckDifficulty observationSkill() {
    return observationSkill;
  }

  /** Recover a fully-constructed CreatureType after serializing.
   * @return A proper CreatureType, which must already have been init'd before loading */
  Object readResolve() {
    return Game.type.creature.get(idname);
  }

  /** Whether this CreatureType will naturally talk about the issues.
   * @return true if so */
  @Getter boolean receptive() {
    return receptive;
  }

  /** When serializing, store a proxy with only the id-name.
   * @return */
  Object writeReplace() {
    return new CTSerialProxy(idname);
  }

  private boolean hasUniform() {
    return uniform != null;
  }

  @Nullable private Uniform uniform() {
    return uniform;
  }

  private void updateCcsLocations() {
    workLocation.clear();
    if (i.score.ccsKills == 2) {
      workLocation.add(AbstractSiteType.type(Bunker.class));
    }
    if (i.score.ccsKills == 1) {
      workLocation.add(AbstractSiteType.type(BombShelter.class));
    }
    if (i.score.ccsKills == 0) {
      workLocation.add(AbstractSiteType.type(BarAndGrill.class));
    }
  }

  private boolean verifyworklocation(@Nullable final Location location) {
    if (location == null) {
      return false;
    }
    if (ccs) {
      updateCcsLocations();
    }
    return workLocation.contains(location.type());
  }

  private Location workLocation() {
    if (ccs) {
      updateCcsLocations();
    }
    if (workLocation.size() == 0) {
      return Location.none();
    }
    return i.rng.randFromSet(workLocation).getLocation();
  }

  /** XML configurable. */
  public final static Configurable configurable;

  private static final long serialVersionUID = Game.VERSION;
  static {
    configurable = new Configurable() {
      @Override public Configurable xmlChild(final String value) {
        final CreatureType ct = new CreatureType();
        ct.idname = value;
        Game.type.creature.put(value, ct);
        return ct;
      }

      @Override public void xmlFinishChild() {
        // no action
      }

      @Override public void xmlSet(final String key, final String value) {
        Log.e(Game.LCS, "Tried to set CT configureable");
      }
    };
  }

  /** Make an appropriate police creature for the laws currently in force. @return some police
   * creature */
  public static Creature makePolice() {
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE) {
      return CreatureType.withType(CreatureType.valueOf("DEATHSQUAD"));
    } else if (i.issue(Issue.POLICEBEHAVIOR).lawLTE(Alignment.CONSERVATIVE)) {
      return CreatureType.withType(CreatureType.valueOf("GANGUNIT"));
    }
    return CreatureType.withType(CreatureType.valueOf("COP"));
  }

  /** Return the CreatureType with the given id
   * @param id
   * @return a CreatureType, or null. */
  public static CreatureType valueOf(final String id) {
    return Game.type.creature.get(id);
  }

  /** Create a Creature with a given type.
   * @param template the CreatureType to create.
   * @return a Creature. */
  public static Creature withType(final CreatureType template) {
    CreatureType modifiedTemplate = template;
    if (modifiedTemplate == CreatureType.valueOf("COP")
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ELITELIBERAL
        && template.alignment == Alignment.LIBERAL && i.rng.likely(3)) {
      modifiedTemplate = CreatureType.valueOf("POLICE_NEGOTIATOR");
    }
    if (modifiedTemplate == CreatureType.valueOf("DEATHSQUAD")
        && i.issue(Issue.DEATHPENALTY).law() != Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() != Alignment.ARCHCONSERVATIVE) {
      if (i.issue(Issue.POLICEBEHAVIOR).lawLT(Alignment.CONSERVATIVE) && i.rng.chance(2)) {
        modifiedTemplate = CreatureType.valueOf("GANGUNIT");
      } else {
        modifiedTemplate = CreatureType.valueOf("SWAT");
      }
    }
    if (modifiedTemplate == CreatureType.valueOf("FIREFIGHTER") && !i.freeSpeech()) {
      modifiedTemplate = CreatureType.valueOf("FIREMAN");
    }
    final Creature cr = new Creature(modifiedTemplate.animalGloss, modifiedTemplate);
    cr.squad(null);
    cr.type(modifiedTemplate);
    if (modifiedTemplate.names.size() > 0) {
      cr.name(i.rng.randFromList(modifiedTemplate.names));
    } else if (modifiedTemplate.ccs && i.mode() == GameMode.SITE) {
      cr.nameCCSMember();
    } else {
      cr.name(modifiedTemplate.toString());
    }
    if (modifiedTemplate.armorTypes.size() > 0) {
      cr.giveArmor(i.rng.randFromList(modifiedTemplate.armorTypes));
    } else {
      cr.giveArmor("CLOTHES");
    }
    if (modifiedTemplate.weapontypes.size() > 0) {
      cr.weapon().giveCreatureWeapon(i.rng.randFromList(modifiedTemplate.weapontypes));
    }
    cr.juice(modifiedTemplate.juice.aValue());
    cr.money(modifiedTemplate.money.aValue());
    final int mood = Politics.publicmood();
    cr.conservatise();
    if (i.rng.nextInt(100) < mood) {
      cr.alignment(cr.alignment().next());
    }
    if (i.rng.nextInt(100) < mood) {
      cr.alignment(cr.alignment().next());
    }
    if (modifiedTemplate.animalGloss == Animal.TANK) {
      cr.specialAttack(SpecialAttacks.CANNON);
    }
    if (modifiedTemplate.verifyworklocation(i.site.current())) {
      cr.workLocation(i.site.current());
    } else {
      cr.workLocation(modifiedTemplate.workLocation());
    }
    cr.age(modifiedTemplate.age.age());
    final boolean traditionalgenderroles = i.issue(Issue.WOMEN).law() == Alignment.ARCHCONSERVATIVE
        || i.issue(Issue.WOMEN).law() == Alignment.CONSERVATIVE && i.rng.likely(25)
        || i.issue(Issue.WOMEN).law() == Alignment.MODERATE && i.rng.likely(10)
        || i.issue(Issue.WOMEN).law() == Alignment.LIBERAL && i.rng.likely(4);
    if (traditionalgenderroles) {
      if (modifiedTemplate.gender == Gender.MALE) {
        cr.gender(Gender.MALE);
      } else if (modifiedTemplate.gender == Gender.FEMALE) {
        cr.gender(Gender.FEMALE);
      }
    }
    cr.alignment(modifiedTemplate.alignment);
    for (final Entry<Skill, Range> e : modifiedTemplate.skills.entrySet()) {
      cr.skill().setSkill(e.getKey(), e.getValue().aValue());
    }
    for (final Entry<Attribute, Range> e : modifiedTemplate.attribute.entrySet()) {
      cr.skill().attribute(e.getKey(), e.getValue().aValue());
    }
    if (modifiedTemplate.offences.size() > 0) {
      final Crime lf = i.rng.randFromList(modifiedTemplate.offences);
      cr.crime().criminalize(lf);
    }
    for (final Specials s : modifiedTemplate.specials) {
      s.apply(cr, modifiedTemplate);
    }
    doAttributes(cr, modifiedTemplate);
    if (modifiedTemplate.stub) {
      return cr;
    }
    doInfiltration(cr, modifiedTemplate);
    doRandomSkills(cr);
    // ALIENATION
    if (i.site.alienate() != Alienation.NONE && cr.alignment() == Alignment.MODERATE) {
      cr.conservatise();
    }
    if (i.site.alienate() == Alienation.ALL && cr.alignment() == Alignment.LIBERAL) {
      cr.conservatise();
    }
    return cr;
  }

  /** rolls up a creature's stats and equipment */
  public static Creature withType(final String s) {
    return withType(CreatureType.valueOf(s));
  }

  /** Whether we are disguised as a particular creature...
   * @param cr a creature to test (in the case of multiple matches, their birthday is used to
   *          determine which one they're disguised as).
   * @return A CreatureType, or null. */
  static CreatureType inCharacter(final Creature cr) {
    final List<CreatureType> valid = new ArrayList<CreatureType>();
    for (final CreatureType c : Game.type.creature.values()) {
      if (!c.hasUniform()) {
        continue;
      }
      if (c.uniform != null && c.uniform.inCharacter(cr)) {
        valid.add(c);
      }
    }
    if (valid.size() == 0) {
      return cr.type();
    }
    return valid.get(cr.birthDay() % valid.size());
  }

  private static void doAttributes(final Creature cr, final CreatureType ct) {
    int attnum = ct.attnum.aValue();
    final EnumMap<Attribute, Integer> attcap = new EnumMap<Attribute, Integer>(Attribute.class);
    for (final Attribute at : Attribute.values()) {
      if (ct.attributeCap.containsKey(at)) {
        attcap.put(at, ct.attributeCap.get(at).aValue());
      } else {
        attcap.put(at, 10);
      }
    }
    for (final Attribute a : Attribute.values()) {
      attnum -= Math.min(4, cr.skill().getAttribute(a, false));
    }
    while (attnum > 0) {
      Attribute a = i.rng.randFromArray(Attribute.values());
      if (a == WISDOM && cr.alignment() == Alignment.LIBERAL && i.rng.likely(4)) {
        a = HEART;
      }
      if (a == HEART && cr.alignment() == Alignment.CONSERVATIVE && i.rng.likely(4)) {
        a = WISDOM;
      }
      if (cr.skill().getAttribute(a, false) < attcap.get(a)) {
        cr.skill().attribute(a, +1);
        attnum--;
      }
    }
  }

  private static void doInfiltration(final Creature cr, final CreatureType ct) {
    if (ct.infiltration.base != 0 && ct.infiltration.range != 0) {
      cr.infiltration(ct.infiltration.aValue());
    } else if (cr.alignment() == Alignment.LIBERAL) {
      cr.infiltration(15 + i.rng.nextInt(10) - 5);
    } else if (cr.alignment() == Alignment.MODERATE) {
      cr.infiltration(25 + i.rng.nextInt(10) - 5);
    } else {
      cr.infiltration(cr.infiltration() + 35 * (1 - cr.infiltration()) + i.rng.nextInt(10) - 5);
    }
    if (cr.infiltration() < 0) {
      cr.infiltration(0);
    }
    if (cr.infiltration() > 100) {
      cr.infiltration(100);
    }
  }

  private static void doRandomSkills(final Creature cr) {
    int randomskills = i.rng.nextInt(4) + 4;
    if (cr.age() > 20) {
      randomskills += randomskills * (cr.age() - 20.0) / 20.0;
    } else {
      randomskills -= (20 - cr.age()) / 2;
    }
    // RANDOM STARTING SKILLS
    while (randomskills > 0) {
      final Skill randomskill = i.rng.randFromArray(Skill.values());
      // 95% chance of not allowing some skills for anybody...
      if (randomskill == Skill.SHOOTINGUP) {
        continue; // don't automatically give skill in this, it's not useful
      }
      if (i.rng.likely(20)
          && (randomskill == HEAVYWEAPONS || randomskill == SMG || randomskill == SWORD
              || randomskill == RIFLE || randomskill == AXE || randomskill == CLUB || randomskill == PSYCHOLOGY)) {
        continue;
      }
      // 90% chance of not allowing some skills, other than
      // for conservatives
      if (i.rng.likely(10) && cr.alignment() != Alignment.CONSERVATIVE) {
        if (randomskill == SHOTGUN) {
          continue;
        }
        if (randomskill == PISTOL) {
          continue;
        }
      }
      if (cr.skill().skillCap(randomskill, true) > cr.skill().skill(randomskill)) {
        cr.skill().setSkill(randomskill, cr.skill().skill(randomskill) + 1);
        randomskills--;
      }
      while (true) {
        if (randomskills != 0 && i.rng.likely(2)
            && cr.skill().skillCap(randomskill, true) > cr.skill().skill(randomskill)
            && cr.skill().skill(randomskill) < 4) {
          cr.skill().setSkill(randomskill, cr.skill().skill(randomskill) + 1);
          randomskills--;
        } else {
          break;
        }
      }
    }
  }
}
