package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;
import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.activities.Art;
import lcs.android.daily.activities.Brownies;
import lcs.android.daily.activities.Burials;
import lcs.android.daily.activities.CarTheft;
import lcs.android.daily.activities.DailyActivity;
import lcs.android.daily.activities.Hack;
import lcs.android.daily.activities.MakeArmor;
import lcs.android.daily.activities.Music;
import lcs.android.daily.activities.Prostitution;
import lcs.android.daily.activities.RepairArmor;
import lcs.android.daily.activities.Solicit;
import lcs.android.daily.activities.Studying;
import lcs.android.daily.activities.Survey;
import lcs.android.daily.activities.TShirts;
import lcs.android.daily.activities.Teaching;
import lcs.android.daily.activities.Trouble;
import lcs.android.daily.activities.WheelChair;
import lcs.android.encounters.FootChase;
import lcs.android.game.Visibility;
import lcs.android.law.Crime;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.Clinic;
import lcs.android.site.type.Shelter;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Activities {
  private static final DailyActivity[] activities;

  private static final DailyActivity art = new Art();

  private static final DailyActivity brownies = new Brownies();

  private static final DailyActivity bury = new Burials();

  private static final DailyActivity graffiti = new Graffiti();

  private static final DailyActivity hack = new Hack();

  private static final DailyActivity makeArmor = new MakeArmor();

  private static boolean mess = false;

  private static final DailyActivity music = new Music();

  private static final DailyActivity prostitutes = new Prostitution();

  private static final DailyActivity repairArmor = new RepairArmor();

  private static final DailyActivity solicit = new Solicit();

  private static final DailyActivity stealcars = new CarTheft();

  private static final DailyActivity students = new Studying();

  private static final DailyActivity survey = new Survey();

  private static final DailyActivity teachers = new Teaching();

  private static final DailyActivity trouble = new Trouble();

  private static final DailyActivity tshirts = new TShirts();

  private static final DailyActivity wheelchair = new WheelChair();
  static {
    activities = new DailyActivity[] { stealcars, solicit, tshirts, art, music, brownies, hack,
        graffiti, prostitutes, students, trouble, teachers, bury, survey, makeArmor, repairArmor,
        wheelchair };
  }

  public static boolean checkForArrest(final Creature liberal, final String reason) {
    boolean arrest = false;
    if (liberal.isNaked() && i.rng.chance(2)) {
      liberal.crime().criminalize(Crime.DISTURBANCE);
      final NewsStory ns = new NewsStory(StoryType.NUDITYARREST);
      ns.location(Location.none());
      i.newsStories.add(ns);
      i.siteStory = ns;
      arrest = true;
    } else if (liberal.crime().heat() > liberal.skill().skill(Skill.STREETSENSE) * 10) {
      if (i.rng.chance(50)) {
        final NewsStory ns = new NewsStory(StoryType.WANTEDARREST);
        ns.location(Location.none());
        i.newsStories.add(ns);
        i.siteStory = ns;
        arrest = true;
      }
    }
    if (arrest) {
      FootChase.attemptArrest(liberal, reason);
    }
    return arrest;
  }

  static void funds_and_trouble() {
    if (i.visibility == Visibility.DISBANDING) {
      return;
    }
    // ACTIVITIES FOR INDIVIDUALS
    mess = false;
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      p.income(0);
      if (!p.location().exists()) {
        p.location(p.base().get());
        p.activity(BareActivity.noActivity());
        continue;
      }
      if (p.health().clinicMonths() != 0 && p.location().exists()
          && (p.location().get().type().isHospital())) {
        continue;
      }
      if (p.datingVacation() != 0 || p.hiding() != 0) {
        continue;
      }
      // CLEAR ACTIONS FOR PEOPLE UNDER SIEGE
      if (p.location().exists() && p.location().get().lcs().siege.siege) {
        p.activity(BareActivity.noActivity());
        continue;
      }
      switch (p.activity().type()) {
      case REPAIR_ARMOR:
        repairArmor.add(p);
        break;
      case MAKE_ARMOR:
        makeArmor.add(p);
        break;
      case WHEELCHAIR:
        wheelchair.add(p);
        break;
      case STEALCARS:
        stealcars.add(p);
        break;
      case POLLS:
        survey.add(p);
        break;
      case VISIT:
        p.activity(BareActivity.noActivity());
        break;
      case NONE: /* repair armor if idle... */
        if (p.alignment() == Alignment.LIBERAL && p.location().exists()
            && !p.location().get().type().isPrison()
            && (p.getArmor().isBloody() || p.getArmor().isDamaged())) {
          repairArmor.add(p);
        }
        break;
      case TEACH_FIGHTING:
      case TEACH_POLITICS:
      case TEACH_COVERT:
        teachers.add(p);
        break;
      case CCFRAUD:
      case DOS_RACKET:
      case DOS_ATTACKS:
      case HACKING:
        hack.add(p);
        break;
      case GRAFFITI:
        graffiti.add(p);
        break;
      case TROUBLE:
        trouble.add(p);
        break;
      case COMMUNITYSERVICE:
        p.addJuice(1, 0);
        if (p.crime().heat() > 0 && i.rng.chance(3)) {
          p.crime().heat(p.crime().heat() - 1);
        }
        i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(1, 0, 80);
        break;
      case SELL_TSHIRTS:
        tshirts.add(p);
        break;
      case SELL_ART:
        art.add(p);
        break;
      case SELL_MUSIC:
        music.add(p);
        break;
      case DONATIONS:
        solicit.add(p);
        break;
      case SELL_DRUGS:
        brownies.add(p);
        break;
      case PROSTITUTION:
        prostitutes.add(p);
        break;
      case BURY:
        bury.add(p);
        break;
      case CLINIC:
        p.health().hospitalize(AbstractSiteType.type(Clinic.class).getLocation());
        p.activity(BareActivity.noActivity());
        break;
      case STUDY_DEBATING:
      case STUDY_MARTIAL_ARTS:
      case STUDY_DRIVING:
      case STUDY_PSYCHOLOGY:
      case STUDY_FIRST_AID:
      case STUDY_LAW:
      case STUDY_DISGUISE:
      case STUDY_SCIENCE:
      case STUDY_BUSINESS:
      case STUDY_GYMNASTICS:
      case STUDY_ART:
      case STUDY_TEACHING:
      case STUDY_MUSIC:
      case STUDY_WRITING:
        students.add(p);
        break;
      case SLEEPER_JOINLCS:
        final Location shelter = AbstractSiteType.type(Shelter.class).getLocation();
        if (!shelter.lcs().siege.siege) {
          p.activity(BareActivity.noActivity()).location(shelter).base(shelter);
          p.removeFlag(CreatureFlag.SLEEPER);
        }
        break;
      default:
        break;
      }
    }
    anyEmpty: {
      for (final DailyActivity da : activities) {
        if (!da.isEmpty()) {
          break anyEmpty;
        }
      }
      return; // nothing to display, leave.
    }
    activityHeader();
    for (final DailyActivity da : activities) {
      da.daily();
      da.clear();
    }
    waitOnOK();
  }

  private static void activityHeader() {
    if (!mess) {
      setView(R.layout.generic);
      ui().text("Activities").bold().add();
    }
    mess = true;
  }
}
