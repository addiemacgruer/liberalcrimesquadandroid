package lcs.android.law;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.HashMap;
import java.util.Map;

import lcs.android.R;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.CreatureName;
import lcs.android.creature.CreatureType;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Ledger;
import lcs.android.items.Armor;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.politics.Politics;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.CourtHouse;
import lcs.android.site.type.Prison;
import lcs.android.site.type.Shelter;
import lcs.android.util.Color;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** Methods related to creatures being in jail.
 * @author addie */
public @NonNullByDefault class Justice {
  private static final int aceAttorneyCost = 5000;

  /** Do monthly update on Creatures in prison.
   * @param g Creature to update.
   * @return true if the screen was updated. */
  public static boolean prisonMonthlyUpdate(final Creature g) {
    boolean showed = false;
    /* Laws required for reeducation camp. */
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE
        && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ARCHCONSERVATIVE
        && !g.crime().deathPenalty() && g.crime().sentence() != 1) {
      reeducation(g);
    }
    if (g.crime().sentence() > 0) {
      // COMMUTE DEATH IN RIGHT CLIMATE
      if (g.crime().deathPenalty() && i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
        ui().text(
            g
                + "'s death sentence has been commuted to life, due to the abolition of the death penalty.")
            .add();
        getch();
        g.crime().sentence(-1).deathPenalty(false);
        return true;
      }
      // ADVANCE SENTENCE
      g.crime().sentence(g.crime().sentence() - 1);
      if (g.crime().sentence() == 0) {
        if (g.crime().deathPenalty()) {
          showed = execute(g);
        } else {
          showed = release(g);
        }
      }
      /* NOTIFY OF IMPENDING THINGS */
      if (g.crime().sentence() == 1) {
        if (g.crime().deathPenalty()) {
          ui().text(g + " is due to be executed next month.").color(Color.YELLOW).add();
          showed = true;
        } else {
          ui().text(g + " is due to be released next month.").add();
          showed = true;
        }
      }
    }
    return showed;
  }

  /** monthly - hold trial on a liberal */
  public static void trial(final Creature g) {
    g.crime().sentence(0);
    g.crime().deathPenalty(false);
    /* If their old base is no longer under LCS control, wander back to the homeless shelter
     * instead. */
    if (!g.base().exists() || g.base().get().renting() != null) {
      g.base(AbstractSiteType.type(Shelter.class).getLocation());
    }
    g.location(g.base().get());
    final Map<Crime, Boolean> breaker = new HashMap<Crime, Boolean>();
    setView(R.layout.generic);
    ui().text(g.toString() + " is standing trial.").add();
    if (!g.crime().isCriminal()) {
      g.crime().criminalize(Crime.LOITERING);
    }
    int typenum = 0;
    int scarefactor = 0;
    /*  *JDS* Scarefactor is the severity of the case against you; if you're a really nasty person
     * with a wide variety of major charges against you, then scarefactor can get up there */
    for (final Crime lf : Crime.values()) {
      if (g.crime().crimesSuspected(lf) > 0) {
        typenum++;
        scarefactor += lf.heat() * g.crime().crimesSuspected(lf);
        breaker.put(lf, true);
      } else {
        breaker.put(lf, false);
      }
    }
    // CHECK FOR SLEEPERS
    final boolean autoconvict = false;
    Creature sleeperjudge = null;
    Creature sleeperlawyer = null;
    int maxsleeperskill = 0;
    for (final Creature p : i.pool) {
      if (p.health().alive() && p.hasFlag(CreatureFlag.SLEEPER)) {
        if (p.type() == CreatureType.valueOf("JUDGE_CONSERVATIVE")
            || p.type() == CreatureType.valueOf("JUDGE_LIBERAL")) {
          if (p.infiltration() >= i.rng.nextInt(100)) {
            sleeperjudge = p;
          }
        }
        if (p.type() == CreatureType.valueOf("LAWYER")) {
          if (p.skill().skill(Skill.LAW) + p.skill().skill(Skill.PERSUASION) >= maxsleeperskill) {
            sleeperlawyer = p;
            maxsleeperskill = p.skill().skill(Skill.LAW)
                + sleeperlawyer.skill().skill(Skill.PERSUASION);
          }
        }
      }
    }
    // STATE CHARGES
    if (sleeperjudge != null) {
      ui().text("Sleeper " + sleeperjudge + " reads the charges, trying to hide a smile:").add();
    } else {
      ui().text("The judge reads the charges:").add();
    }
    ui().text("The defendant, " + g.properName() + ", is charged with ").color(Color.RED).add();
    final StringBuilder str = new StringBuilder();
    while (typenum > 0) {
      typenum--;
      /* Treason. Technically, treason is exposing state secrets, or somesuch. Illegal Immigrants
       * cannot commit treason, because treason can only be committed by `those owing allegiance to
       * the United States`. The above is already respected by LCS; treason occurs from exposing
       * intelligence secrets, and illegal immigrants are not taken to trial. - Jonathan S. Fox Oh,
       * right. Forgot about that. Even if the law is L+? */
      if (count(g, breaker, Crime.TREASON, "treason", str)) {
        Log.i("LCS", "Treason");
      } else if (count(g, breaker, Crime.TERRORISM, "terrorism", str)) {
        Log.i("LCS", "Terrorism");
      } else if (count(g, breaker, Crime.KIDNAPPING, "kidnapping", str)) {
        Log.i("LCS", "Kidnapping");
      } else if (breaker.get(Crime.BURNFLAG)
          && i.issue(Issue.FLAGBURNING).lawLTE(Alignment.MODERATE)) {
        if (g.crime().crimesSuspected(Crime.BURNFLAG) > 1) {
          ui().text(g.crime().crimesSuspected(Crime.BURNFLAG) + " counts of ").add();
        }
        if (i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE) {
          ui().text("Flag Murder").add();
        } else if (i.issue(Issue.FLAGBURNING).law() == Alignment.CONSERVATIVE) {
          ui().text("felony flag burning").add();
        } else if (i.issue(Issue.FLAGBURNING).law() == Alignment.MODERATE) {
          ui().text("flag burning").add();
        }
        breaker.put(Crime.BURNFLAG, false);
      } else if (count(g, breaker, Crime.SPEECH, "harmful speech", str)) {
        Log.i("LCS", "Harmful speech");
      } else if (count(g, breaker, Crime.BROWNIES, "distribution of a controlled substance", str)) {
        Log.i("LCS", "Distribution");
      } else if (count(g, breaker, Crime.ESCAPED, "escaping prison", str)) {
        Log.i("LCS", "Escaping prison");
      } else if (count(g, breaker, Crime.HELPESCAPE, "aiding a prison escape", str)) {
        Log.i("LCS", "Aiding escape");
      } else if (count(g, breaker, Crime.JURY, "jury tampering", str)) {
        Log.i("LCS", "Jury tampering");
      } else if (count(g, breaker, Crime.RACKETEERING, "racketeering", str)) {
        Log.i("LCS", "Racketeering");
      } else if (count(g, breaker, Crime.ARMEDASSAULT, "armed assault", str)) {
        Log.i("LCS", "Armed assault");
      } else if (count(g, breaker, Crime.MURDER, "murder", str)) {
        /* XXX: How about the addition of a `manslaughter` charge? -- LK Manslaughter is murder
         * without forethought or malice, IIRC. -- LK Well, I can't think of an instance of this in
         * LCS besides fruit stand employees. -Fox Possibly people you accidentally kill when
         * shooting at conservatives. -- LK */
        Log.i("LCS", "Murder");
      } else if (count(g, breaker, Crime.EXTORTION, "extortion", str)) {
        Log.i("LCS", "Extortion");
      } else if (count(g, breaker, Crime.ARSON, "arson", str)) {
        Log.i("LCS", "Arson");
      } else if (count(g, breaker, Crime.BROWNIES, "misdemeanor assault", str)) {
        Log.i("LCS", "Misdemeanory assault");
      } else if (count(g, breaker, Crime.CARTHEFT, "grand theft auto", str)) {
        Log.i("LCS", "GTA");
      } else if (count(g, breaker, Crime.CCFRAUD, "credit card fraud", str)) {
        Log.i("LCS", "Credit fraud");
      } else if (count(g, breaker, Crime.THEFT, "theft", str)) {
        Log.i("LCS", "Theft");
      } else if (count(g, breaker, Crime.PROSTITUTION, "prostitution", str)) {
        Log.i("LCS", "Prostitution");
      } else if (count(g, breaker, Crime.HIREILLEGAL, "hiring an illegal alien", str)) {
        Log.i("LCS", "Hiring illegals");
      } else if (count(g, breaker, Crime.COMMERCE, "interference with interstate commerce", str)) {
        Log.i("LCS", "Commerce");
      } else if (count(g, breaker, Crime.INFORMATION, "unlawful access of an information system",
          str)) {
        Log.i("LCS", "Unlawful access");
      } else if (count(g, breaker, Crime.BURIAL, "unlawful burial", str)) {
        Log.i("LCS", "Unlawful burial");
      } else if (count(g, breaker, Crime.BREAKING, "breaking and entering", str)) {
        Log.i("LCS", "B&E");
      } else if (count(g, breaker, Crime.VANDALISM, "vandalism", str)) {
        Log.i("LCS", "Vandalism");
      } else if (count(g, breaker, Crime.RESIST, "resisting arrest", str)) {
        Log.i("LCS", "Resist arrest");
      } else if (count(g, breaker, Crime.DISTURBANCE, "disturbing the peace", str)) {
        Log.i("LCS", "Disturb peace");
      } else if (count(g, breaker, Crime.PUBLICNUDITY, "indecent exposure", str)) {
        Log.i("LCS", "Indecent exposure");
      } else if (count(g, breaker, Crime.LOITERING, "loitering", str)) {
        Log.i("LCS", "Loitering");
      }
      if (typenum > 1) {
        str.append(", ");
      } else if (typenum == 1) {
        str.append(" and ");
      } else if (typenum == 0) {
        str.append('.');
      }
    }
    ui().text(str.toString()).color(Color.RED).add();
    if (g.crime().testimonies() > 0) {
      if (g.crime().testimonies() > 1) {
        ui().text(
            g.crime().testimonies() + " former LCS members will testify against " + g.toString()
                + ".").add();
      } else {
        ui().text("A former LCS member will testify against " + g.toString() + ".").add();
      }
    }
    // CHOOSE DEFENSE
    ui(R.id.gcontrol).text("How will you conduct the defense?").add();
    String attorneyname = CreatureName.generateName();
    ui(R.id.gcontrol).button('a').text("Use a court-appointed attorney.").add();
    ui(R.id.gcontrol).button('b').text("Defend self!").add();
    ui(R.id.gcontrol).button('c').text("Plead guilty.").add();
    maybeAddButton(R.id.gcontrol, 'd', "Pay $" + aceAttorneyCost + " to hire ace Liberal attorney "
        + attorneyname + ".", i.ledger.funds() >= aceAttorneyCost);
    if (sleeperlawyer != null) {
      ui(R.id.gcontrol).button('e')
          .text("Accept sleeper " + sleeperlawyer + "'s offer to assist pro bono.").add();
    }
    /* SAV - added in display of skills and relevant attributes to help decide when to defend self. */
    ui().text(g.toString() + ":").bold().add();
    ui().text("Heart: " + g.skill().getAttribute(Attribute.HEART, true)).add();
    ui().text("Persuasion: " + g.skill().skill(Skill.PERSUASION)).add();
    ui().text("Charisma: " + g.skill().getAttribute(Attribute.CHARISMA, true)).add();
    ui().text("Law: " + g.skill().skill(Skill.LAW)).add();
    ui().text("Intelligence: " + g.skill().getAttribute(Attribute.INTELLIGENCE, true)).add();
    int defense;
    do {
      final int c = getch();
      if (c == 'a') {
        defense = 0;
        break;
      }
      if (c == 'b') {
        defense = 1;
        break;
      }
      if (c == 'c') {
        defense = 2;
        break;
      }
      if (c == 'd' && i.ledger.funds() >= aceAttorneyCost) {
        i.ledger.subtractFunds(aceAttorneyCost, Ledger.ExpenseType.LEGAL);
        defense = 3;
        break;
      }
      if (c == 'e' && sleeperlawyer != null) {
        defense = 4;
        attorneyname = sleeperlawyer.toString();
        break;
      }
    } while (true);
    clearChildren(R.id.gcontrol);
    // TRIAL
    if (defense != 2) {
      int prosecution = 0;
      ui().text(g.toString() + " is standing trial.").add();
      ui().text("The trial proceeds.  Jury selection is first.").add();
      int jury = i.rng.nextInt(61) - 60 * Politics.publicmood(null) / 100; // Political
      // leanings // of the// population / determine // your jury
      if (sleeperjudge != null) {
        jury -= 20;
      }
      if (defense == 3) {
        if (i.rng.likely(10)) {
          ui().text(attorneyname + " ensures the jury is stacked in " + g + "'s favor!")
              .color(Color.GREEN).add();
          if (jury > 0) {
            jury = 0;
          }
          jury -= 30;
        } else {
          ui().text(attorneyname + "'s CONSERVATIVE ARCH-NEMESIS will represent the prosecution!!!")
              .color(Color.RED).add();
          jury = 0;
          prosecution += 100; // DUN DUN DUN!!
        }
      } else if (jury <= -29) {
        switch (i.rng.nextInt(4)) {
        default:
          ui().text(g.toString() + "'s best friend from childhood is a juror.").color(Color.GREEN)
              .add();
          break;
        case 1:
          ui().text("The jury is Flaming Liberal.").color(Color.GREEN).add();
          break;
        case 2:
          ui().text("Four of the jurors are closet Socialists.").color(Color.GREEN).add();
          break;// XXX: A Few?
        case 3:
          ui().text(
              "One of the jurors flashes a SECRET LIBERAL HAND SIGNAL when no one is looking.")
              .color(Color.GREEN).add();
          break;
        }
      } else if (jury <= -15) {
        ui().text("The jury is fairly Liberal.").add();
      } else if (jury < 15) {
        ui().text("The jury is quite moderate.").add();
      } else if (jury < 29) {
        ui().text("The jury is a bit Conservative.").add();
      } else {
        switch (i.rng.nextInt(4)) {
        default:
          ui().text("Such a collection of Conservative jurors has never before been assembled.")
              .color(Color.YELLOW).add();
          break;
        case 1:
          ui().text("One of the accepted jurors is a Conservative activist.").color(Color.YELLOW)
              .add();
          break;
        case 2:
          ui().text("Three of the jurors are members of the KKK.").color(Color.YELLOW).add();
          break;// XXX: A Few?
        case 3:
          ui().text("The jury is frighteningly Conservative.").color(Color.YELLOW).add();
          break;
        }
      }
      // PROSECUTION MESSAGE
      // *JDS* The bigger your record, the stronger the evidence
      prosecution += 40 + i.rng.nextInt(101);
      prosecution += scarefactor;
      prosecution += 20 * g.crime().testimonies();
      if (sleeperjudge != null) {
        prosecution >>= 1;
      }
      if (defense == 3) {
        prosecution -= 60;
      }
      if (autoconvict) {
        ui().text("There is no question of " + g.toString() + "'s guilt.").add();
      } else if (prosecution <= 50) {
        ui().text("The prosecution's presentation is terrible.").add();
      } else if (prosecution <= 75) {
        ui().text("The prosecution gives a standard presentation.").add();
      } else if (prosecution <= 125) {
        ui().text("The prosecution's case is solid.").add();
      } else if (prosecution <= 175) {
        ui().text("The prosecution makes an airtight case.").add();
      } else {
        ui().text("The prosecution is incredibly strong.").add();
      }
      jury += i.rng.nextInt(prosecution / 2 + 1) + prosecution / 2;
      // DEFENSE MESSAGE
      int defensepower = 0;
      if (defense == 0 || defense == 3 || defense == 4) {
        if (autoconvict) {
          ui().text("The defense makes a noble attempt, but the outcome is inevitable.").add();
        } else {
          if (defense == 0) {
            defensepower = i.rng.nextInt(71); // Court-appointed
          } else if (defense == 3) {
            defensepower = i.rng.nextInt(71) + 80; // Ace Liberal
          } else if (defense == 4 && sleeperlawyer != null) {
            // Sleeper attorney
            defensepower = i.rng.nextInt(71) + sleeperlawyer.skill().skill(Skill.LAW) * 2
                + sleeperlawyer.skill().skill(Skill.PERSUASION) * 2;
            sleeperlawyer.skill().train(Skill.LAW, prosecution / 4);
            sleeperlawyer.skill().train(Skill.PERSUASION, prosecution / 4);
          }
          if (defensepower <= 15) {
            ui().text(
                "The defense attorney accidentally said \"My client is GUILTY!\" during closing.")
                .add();
          } else if (defensepower <= 25) {
            ui().text("The defense is totally lame.").add();
          } else if (defensepower <= 50) {
            ui().text("The defense was lackluster.").add();
          } else if (defensepower <= 75) {
            ui().text("Defense arguments were pretty good.").add();
          } else if (defensepower <= 100) {
            ui().text("The defense was really slick.").add();
          } else if (defensepower <= 145) {
            if (prosecution < 100) {
              ui().text("The defense makes the prosecution look like amateurs.").add();
            } else {
              ui().text("The defense is extremely compelling.").add();
            }
          } else if (prosecution < 100) {
            ui().text(
                attorneyname
                    + "'s arguments made several of the jurors stand up and shout \"NOT GUILTY!\" before deliberations even began.")
                .add();
            if (defense == 4) {
              g.addJuice(10, 500); // Bow please
            }
          } else {
            ui().text(attorneyname + " conducts an incredible defense.").add();
          }
        }
      }
      if (defense == 1) {
        /*  *JDS* LEGAL SELF-REPRESENTATION: To succeed here, you really need to have two skills be
         * high: persuasion and law, with law being 1.5 times as influential. You can't have just
         * one or just the other. Even if you're a very persuasive person, the court will eat you
         * alive if you can't sound intelligent when talking about the relevant charges, and you
         * won't be able to fool the jury into letting you go if you aren't persuasive, as no matter
         * how encyclopedic your legal knowledge is, it's all in the pitch. If either your
         * persuasion or your law roll is too low, you'll end up getting a negative result that will
         * drag down your defense. So try not to suck in either area. */
        defensepower = 5 * (g.skill().skillRoll(Skill.PERSUASION) - 3) + 10
            * (g.skill().skillRoll(Skill.LAW) - 3);
        g.skill().train(Skill.PERSUASION, 50);
        g.skill().train(Skill.LAW, 50);
        if (defensepower <= 0) {
          ui().text(g.toString() + " makes one horrible mistake after another.").add();
          g.addJuice(-10, -50); // You should be ashamed
        } else if (defensepower <= 25) {
          ui().text(g.toString() + "'s case really sucked.").add();
        } else if (defensepower <= 50) {
          ui().text(g.toString() + " did all right, but made some mistakes.").add();
        } else if (defensepower <= 75) {
          ui().text(g.toString() + "'s arguments were pretty good.").add();
        } else if (defensepower <= 100) {
          ui().text(g.toString() + " worked the jury very well.").add();
        } else if (defensepower <= 150) {
          ui().text(g.toString() + " made a very powerful case.").add();
        } else {
          ui().text(g.toString() + " had the jury, judge, and prosecution crying for freedom.")
              .add();
          g.addJuice(50, 1000); // That shit is legend
        }
      }
      // DELIBERATION MESSAGE
      ui().text("The jury leaves to consider the case.").add();
      waitOnOK();
      // JURY RETURN MESSAGE
      ui().text("The jury has returned from deliberations.").add();
      boolean keeplawflags = false;
      // HUNG JURY
      if (defensepower == jury) {
        ui().text("But they can't reach a verdict!").color(Color.YELLOW).add();
        // RE-TRY
        if (i.rng.chance(2) || scarefactor >= 10 || g.crime().testimonies() > 0) {
          ui().text("The case will be re-tried next month.").add();
          final Location ps = AbstractSiteType.type(CourtHouse.class).getLocation();
          g.location(ps);
          keeplawflags = true;
        } else { // NO RE-TRY
          ui().text("The prosecution declines to re-try the case.").add();
          ui().text(g.toString() + " is free!").color(Color.GREEN).add();
        }
      } else if (!autoconvict && defensepower > jury) { // ACQUITTAL!
        ui().text("NOT GUILTY!").color(Color.GREEN).add();
        ui().text(g.toString() + " is free!").color(Color.GREEN).add();
        if (defense == 4 && sleeperlawyer != null) {
          // Juice sleeper
          sleeperlawyer.addJuice(10, 100);
        }
        if (defense == 1) {
          // Juice for self-defense
          g.addJuice(10, 100);
        }
      } else {
        // LENIENCE
        if (defense == 4 && sleeperlawyer != null) {
          // De-Juice sleeper
          sleeperlawyer.addJuice(-5, 0);
        }
        if (defense != 2) {
          // Juice for getting convicted of something :)
          g.addJuice(25, 200);
        }
        // Check for lenience; sleeper judge will always be merciful
        if (defensepower / 3 >= jury / 4 || sleeperjudge != null || defense == 2) {
          penalize(g, true);
        } else {
          penalize(g, false);
        }
      }
      // CLEAN UP LAW FLAGS
      if (!keeplawflags) {
        g.crime().clearCriminalRecord();
      }
      g.crime().heat(0).clearTestimonies();
      // PLACE PRISONER
      if (g.crime().sentence() != 0) {
        g.location(AbstractSiteType.type(Prison.class).getLocation());
      } else {
        final Armor clothes = new Armor("ARMOR_CLOTHES");
        g.giveArmor(clothes, null);
      }
    }
    // GUILTY PLEA
    // XXX: How about "nolo" (Nolo contendere) -- LK
    else {
      ui().text("The court accepts the plea.").add();
      penalize(g, i.rng.choice(true, false));
      g.crime().clearCriminalRecord();
      // PLACE PRISONER
      if (g.crime().sentence() != 0) {
        g.location(AbstractSiteType.type(Prison.class).getLocation());
      } else {
        final Armor clothes = new Armor("ARMOR_CLOTHES");
        g.giveArmor(clothes, null);
      }
    }
    waitOnOK();
  }

  private static boolean count(final Creature g, final Map<Crime, Boolean> m, final Crime lf,
      final String s, final StringBuilder str) {
    if (m.get(lf)) {
      if (g.crime().crimesSuspected(lf) > 1) {
        str.append(String.valueOf(g.crime().crimesSuspected(lf)));
        str.append(" counts of ");
      }
      str.append(s);
      m.put(lf, false);
      return true;
    }
    return false;
  }

  private static boolean execute(final Creature g) {
    boolean showed;
    ui().text("FOR SHAME:").color(Color.RED).add();
    ui().text("Today, the Conservative Machine executed " + g + "by ").add();
    if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE) {
      ui().text(
          i.rng.choice("beheading", "drawing and quartering", "disemboweling", "one thousand cuts",
              "feeding the lions", "repeated gladiatorial death matches", "burning", "crucifixion",
              "head-squishing", "piranha tank swimming exhibition",
              "forced sucking of Ronald Reagan's ass",
              "covering with peanut butter and letting rats eat",
              "burying up to the neck in a fire ant nest", "running truck over the head",
              "drowning in a sewage digester vat", "chipper-shredder", "use in lab research",
              "blood draining", "chemical weapons test", "sale to a furniture maker",
              "sale to a CEO as a personal pleasure toy", "sale to foreign slave traders",
              "exposure to degenerate Bay 12 Curses games")).add();
    } else if (i.issue(Issue.DEATHPENALTY).law() == Alignment.CONSERVATIVE
        || i.issue(Issue.DEATHPENALTY).law() == Alignment.MODERATE) {
      ui().text(i.rng.choice("lethal injection", "hanging", "firing squad", "electrocution")).add();
    } else {
      ui().text("lethal injection").add();
    }
    // dejuice boss
    final Maybe<Creature> boss = g.hire();
    if (boss.exists()) {
      ui().text(boss.get() + " has failed the Liberal Crime Squad.").color(Color.RED).add();
      ui().text("If you can't protect your own people, who can you protect?").add();
      boss.get().addJuice(-50, -50);
    }
    g.health().die();
    showed = true;
    return showed;
  }

  private static void penalize(final Creature g, final boolean aLenient) {
    boolean lenient = aLenient;
    ui().text("GUILTY!").color(Color.RED).add();
    if (g.crime().crimesSuspected(Crime.ESCAPED) > 0) {
      lenient = false;
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
        g.crime().deathPenalty(false);
      }
    } else {
      g.crime().deathPenalty(false);
    }
    if (!lenient
        && (g.crime().crimesSuspected(Crime.MURDER) > 0
            || g.crime().crimesSuspected(Crime.TREASON) > 0
            || g.crime().crimesSuspected(Crime.BURNFLAG) > 0
            && i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE || i.issue(
            Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE)) {
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ARCHCONSERVATIVE) {
        g.crime().deathPenalty(true);
      }
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.CONSERVATIVE) {
        g.crime().deathPenalty(i.rng.likely(3));
      }
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.MODERATE) {
        g.crime().deathPenalty(i.rng.chance(2));
      }
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.LIBERAL) {
        g.crime().deathPenalty(i.rng.chance(5));
      }
      if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
        g.crime().deathPenalty(false);
      }
    }
    // CALC TIME
    if (!g.crime().deathPenalty()) {
      if (!(g.crime().sentence() < 0)) {
        g.crime().sentence(
            g.crime().sentence() + (36 + i.rng.nextInt(18))
                * g.crime().prosecutableCrimes(Crime.KIDNAPPING));
        g.crime().sentence(
            g.crime().sentence() + (1 + i.rng.nextInt(4))
                * g.crime().prosecutableCrimes(Crime.THEFT));
        g.crime().sentence(
            g.crime().sentence() + (6 + i.rng.nextInt(7))
                * g.crime().prosecutableCrimes(Crime.CARTHEFT));
        g.crime().sentence(
            g.crime().sentence() + (1 + i.rng.nextInt(13))
                * g.crime().prosecutableCrimes(Crime.INFORMATION));
        g.crime().sentence(
            g.crime().sentence() + (1 + i.rng.nextInt(13))
                * g.crime().prosecutableCrimes(Crime.COMMERCE));
        g.crime().sentence(
            g.crime().sentence() + (6 + i.rng.nextInt(25))
                * g.crime().prosecutableCrimes(Crime.CCFRAUD));
        g.crime().sentence(
            g.crime().sentence() + (3 + i.rng.nextInt(12))
                * g.crime().prosecutableCrimes(Crime.BURIAL));
        g.crime().sentence(
            g.crime().sentence() + (1 + i.rng.nextInt(6))
                * g.crime().prosecutableCrimes(Crime.PROSTITUTION));
        g.crime().sentence(
            g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.DISTURBANCE));
        g.crime().sentence(
            g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.PUBLICNUDITY));
        g.crime().sentence(
            g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.HIREILLEGAL));
        g.crime().sentence(
            g.crime().sentence() + (12 + i.rng.nextInt(100))
                * g.crime().prosecutableCrimes(Crime.RACKETEERING));
        // How illegal is marijuana?
        if (i.issue(Issue.DRUGS).law() == Alignment.ARCHCONSERVATIVE) {
          g.crime().sentence(
              g.crime().sentence() + (3 + i.rng.nextInt(360))
                  * g.crime().prosecutableCrimes(Crime.BROWNIES));
        } else if (i.issue(Issue.DRUGS).law() == Alignment.CONSERVATIVE) {
          g.crime().sentence(
              g.crime().sentence() + (3 + i.rng.nextInt(120))
                  * g.crime().prosecutableCrimes(Crime.BROWNIES));
        } else if (i.issue(Issue.DRUGS).law() == Alignment.MODERATE) {
          g.crime().sentence(
              g.crime().sentence() + (3 + i.rng.nextInt(12))
                  * g.crime().prosecutableCrimes(Crime.BROWNIES));
        }
        g.crime().sentence(g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.BREAKING));
        g.crime().sentence(
            g.crime().sentence() + (60 + i.rng.nextInt(181))
                * g.crime().prosecutableCrimes(Crime.TERRORISM));
        g.crime().sentence(
            g.crime().sentence() + (30 + i.rng.nextInt(61))
                * g.crime().prosecutableCrimes(Crime.JURY));
        g.crime().sentence(
            g.crime().sentence() + (30 + i.rng.nextInt(61))
                * g.crime().prosecutableCrimes(Crime.HELPESCAPE));
        g.crime().sentence(
            g.crime().sentence() + (1 + i.rng.nextInt(1))
                * g.crime().prosecutableCrimes(Crime.RESIST));
        g.crime().sentence(
            g.crime().sentence() + (6 + i.rng.nextInt(1))
                * g.crime().prosecutableCrimes(Crime.EXTORTION));
        g.crime().sentence(
            g.crime().sentence() + (4 + i.rng.nextInt(3))
                * g.crime().prosecutableCrimes(Crime.SPEECH));
        g.crime()
            .sentence(g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.VANDALISM));
        g.crime().sentence(
            g.crime().sentence() + (12 + i.rng.nextInt(12))
                * g.crime().prosecutableCrimes(Crime.ARSON));
        g.crime().sentence(
            g.crime().sentence() + (12 + i.rng.nextInt(1))
                * g.crime().prosecutableCrimes(Crime.ARMEDASSAULT));
        g.crime().sentence(
            g.crime().sentence() + (3 + i.rng.nextInt(1))
                * g.crime().prosecutableCrimes(Crime.ASSAULT));
      }
      if (i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE) {
        if (i.rng.chance(2)) {
          g.crime().sentence(
              g.crime().sentence() + (120 + i.rng.nextInt(241))
                  * g.crime().prosecutableCrimes(Crime.BURNFLAG));
        } else if (g.crime().prosecutableCrimes(Crime.BURNFLAG) > 0) {
          g.crime().sentence(-1 * g.crime().prosecutableCrimes(Crime.BURNFLAG));
        }
      } else if (i.issue(Issue.FLAGBURNING).law() == Alignment.CONSERVATIVE) {
        g.crime()
            .sentence(g.crime().sentence() + 36 * g.crime().prosecutableCrimes(Crime.BURNFLAG));
      } else if (i.issue(Issue.FLAGBURNING).law() == Alignment.MODERATE) {
        g.crime().sentence(g.crime().sentence() + 1 * g.crime().prosecutableCrimes(Crime.BURNFLAG));
      }
      if (i.rng.nextInt(4) - g.crime().prosecutableCrimes(Crime.MURDER) > 0) {
        if (!(g.crime().sentence() < 0)) {
          g.crime().sentence(
              g.crime().sentence() + (120 + i.rng.nextInt(241))
                  * g.crime().prosecutableCrimes(Crime.MURDER));
        }
      } else if (g.crime().sentence() < 0) {
        g.crime().sentence(g.crime().sentence() - -1 * g.crime().prosecutableCrimes(Crime.MURDER));
      } else if (g.crime().prosecutableCrimes(Crime.MURDER) > 0) {
        g.crime().sentence(-1 * g.crime().prosecutableCrimes(Crime.MURDER));
      }
      if (g.crime().sentence() < 0) {
        g.crime().sentence(g.crime().sentence() - 1 * g.crime().prosecutableCrimes(Crime.ESCAPED));
      } else if (g.crime().prosecutableCrimes(Crime.ESCAPED) > 0) {
        g.crime().sentence(-1 * g.crime().prosecutableCrimes(Crime.ESCAPED));
      }
      if (g.crime().sentence() < 0) {
        g.crime().sentence(g.crime().sentence() - 1 * g.crime().prosecutableCrimes(Crime.TREASON));
      } else if (g.crime().prosecutableCrimes(Crime.TREASON) > 0) {
        g.crime().sentence(-1 * g.crime().prosecutableCrimes(Crime.TREASON));
      }
      if (lenient && g.crime().sentence() != -1) {
        g.crime().sentence(g.crime().sentence() / 2);
      }
      if (lenient && g.crime().sentence() == -1) {
        g.crime().sentence(240 + i.rng.nextInt(120));
      }
    }
    // LENIENCY AND CAPITAL PUNISHMENT DON'T MIX
    else if (g.crime().deathPenalty() && lenient) {
      g.crime().deathPenalty(false);
      g.crime().sentence(-1);
    }
    // MENTION LENIENCY
    if (lenient) {
      ui().text("During sentencing, the judge grants some leniency.").add();
    }
    // MENTION SENTENCE
    if (g.crime().deathPenalty()) {
      g.crime().sentence(3);
      ui().text(g.properName() + ", you are sentenced to DEATH!").color(Color.RED).add();
      ui().text("The execution is scheduled to occur three months from now.").add();
    } else if (g.crime().sentence() == 0) {
      ui().text(g.properName() + ", consider this a warning.  You are free to go.").add();
    } else {
      if (g.crime().sentence() >= 36) {
        g.crime().sentence(g.crime().sentence() - g.crime().sentence() % 12);
      }
      ui().text(g.properName() + ", you are sentenced to ").add();
      if (g.crime().sentence() > 1200) {
        g.crime().sentence(g.crime().sentence() / -1200);
      }
      if (g.crime().sentence() <= -1) {
        if (g.crime().sentence() < -1) {
          ui().text(-g.crime().sentence() + " consecutive life terms in prison.").add();
          ui().text("Have a nice day, " + g.properName() + ".").add();
        } else {
          ui().text("life in prison.").add();
        }
      } else if (g.crime().sentence() >= 36) {
        ui().text(g.crime().sentence() / 12 + " years in prison.").add();
      } else {
        ui().text(
            g.crime().sentence() + " month" + (g.crime().sentence() > 1 ? "s" : "") + " in prison.")
            .add();
      }
      // dejuice boss
      final Maybe<Creature> boss = g.hire();
      if (!boss.exists() && boss.get().juice() > 50) {
        int juice = g.juice() / 10;
        if (juice < 5) {
          juice = 5;
        }
        boss.get().addJuice(-juice, 0);
      }
    }
  }

  private static void reeducation(final Creature g) {
    ui().text(g + " is subjected to Conservative re-education!").add();
    if (!g.skill().attributeCheck(Attribute.HEART, CheckDifficulty.FORMIDABLE.value())) {
      if (g.juice() >= 100) {
        ui().text(g + " loses juice!").add();
        g.addJuice(-50, 100);
      } else if (i.rng.nextInt(15) > g.skill().getAttribute(Attribute.WISDOM, true)
          || g.skill().getAttribute(Attribute.WISDOM, true) < g.skill().getAttribute(
              Attribute.HEART, true)) {
        ui().text(g + " becomes Wiser!").add();
        g.skill().attribute(Attribute.WISDOM, +1);
      } else if (g.alignment() == Alignment.LIBERAL && g.hasFlag(CreatureFlag.LOVE_SLAVE)
          && i.rng.likely(4) && g.hire().exists()) {
        ui().text(g + " only resists by thinking of " + g.hire().get() + "!").add();
      } else {
        ui().text(g + " is turned Conservative!").add();
        final Maybe<Creature> contact = g.hire();
        if (contact.exists()) {
          contact.get().crime().criminalize(Crime.RACKETEERING).addTestimony();
        }
        g.health().die();
      }
    } else {
      ui().text(g + " remains strong.").add();
    }
    return;
  }

  private static boolean release(final Creature g) {
    final boolean showed;
    ui().text(g + " has been released from prison.").add();
    ui().text("No doubt there are some mental scars, but the Liberal is back.").add();
    final Armor clothes = new Armor("ARMOR_CLOTHES");
    g.giveArmor(clothes, null);
    /* If their old base is no longer under LCS control, wander back to the homeless shelter
     * instead. */
    if (!g.base().exists() || g.base().get().renting() == null) {
      g.base(AbstractSiteType.type(Shelter.class).getLocation());
    }
    g.location(g.base().get());
    showed = true;
    return showed;
  }
}
