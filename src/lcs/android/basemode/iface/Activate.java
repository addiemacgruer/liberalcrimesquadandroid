package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lcs.android.R;
import lcs.android.activities.AbstractActivity;
import lcs.android.activities.BareActivity;
import lcs.android.activities.CreatureActivity;
import lcs.android.activities.ItemActivity;
import lcs.android.activities.MuralActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.items.ArmorType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

@NonNullByDefault class Activate {
  private Activate() {}

  private static final Activity[] CLASSES = { Activity.STUDY_DEBATING, Activity.STUDY_BUSINESS,
      Activity.STUDY_PSYCHOLOGY, Activity.STUDY_LAW, Activity.STUDY_SCIENCE,
      Activity.STUDY_DRIVING, Activity.STUDY_FIRST_AID, Activity.STUDY_ART,
      Activity.STUDY_DISGUISE, Activity.STUDY_MARTIAL_ARTS, Activity.STUDY_GYMNASTICS,
      Activity.STUDY_WRITING, Activity.STUDY_TEACHING, Activity.STUDY_MUSIC };

  protected static void activate() {
    final List<Creature> pool = new ArrayList<Creature>();
    for (final Creature p : Filter.of(i.pool, Filter.AVAILABLE)) {
      if (p.location().exists() && !p.location().get().type().isPrison() && p.squad().exists()
          && p.squad().get().activity().type() != Activity.NONE) {
        continue;
      }
      pool.add(p);
    }
    if (pool.isEmpty())
      return;
    i.activeSortingChoice.get(CreatureList.ACTIVATE).sort(pool);
    final StringBuilder str = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text(format("Money: ", i.ledger.funds())).add();
      ui().text("Activate Uninvolved Liberals").add();
      ui().text("CODE NAME - SKILL - HEALTH - LOCATION -ACTIVITY").add();
      int y = 'a';
      for (final Creature p : pool) {
        str.append(p.toString());
        int skill = 0;
        for (final Skill sk : Skill.values()) {
          skill += p.skill().skill(sk);
        }
        str.append(" - ").append(skill).append(" - ").append(p.health().healthStat()).append(" - ");
        if (p.location().exists()) {
          str.append(p.location().get().toString()).append(" - ");
        } else {
          Log.e("LCS", "Missing location:" + p);
          str.append("HOMELESS - ");
        }
        str.append(p.activity().toString());
        ui(R.id.gcontrol).button(y++).text(str.toString())
            .color(p.activity().type().set_activity_color()).add();
        str.setLength(0);
      }
      ui(R.id.gcontrol).button(12).text("Sort people.").add();
      ui(R.id.gcontrol).button(11).text("Assign simple tasks in bulk.").add();
      ui(R.id.gcontrol).button(10).text("Continue the struggle.").add();
      final int c = getch();
      if (c >= 'a') {
        activate(pool.get(c - 'a'));
      }
      if (c == 12) {
        CreatureList.ACTIVATE.sortingPrompt();
        i.activeSortingChoice.get(CreatureList.ACTIVATE).sort(pool);
      }
      if (c == 11) {
        activateBulk();
      }
      if (c == 10) {
        break;
      }
    } while (true);
  }

  protected static void activateSleepers() {
    final List<Creature> pool = Filter.of(i.pool, Filter.AVAILABLE);
    if (pool.isEmpty())
      return;
    final StringBuilder str = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text(format("Money: ", i.ledger.funds())).add();
      ui().text("Activate Sleeper Agents").add();
      ui().text("CODE NAME - JOB - SITE (EFFECTIVENESS) - ACTIVITY").add();
      int y = 'a';
      for (final Creature p : pool) {
        str.append(p.toString()).append(" - ").append(p.type().jobtitle(p)).append(" - ");
        if (p.location().exists()) {
          str.append(p.location().get().toString());
        }
        str.append(" (").append(p.infiltration()).append("%)").append(" - ")
            .append(p.activity().toString());
        ui(R.id.gcontrol).button(y++).text(str.toString())
            .color(p.activity().type().set_activity_color()).add();
        str.setLength(0);
      }
      ui().button(12).text("Sort people.").add();
      ui(R.id.gcontrol).button(10).text("Continue the struggle.").add();
      final int c = getch();
      if (c >= 'a') {
        Activate.activateSleeper(pool.get(c - 'a'));
      }
      if (c == 12) {
        CreatureList.ACTIVATE.sortingPrompt();
        i.activeSortingChoice.get(CreatureList.ACTIVATE).sort(pool);
      }
      if (c == 10) {
        break;
      }
    } while (true);
  }

  private static void activate(final Creature cr) {
    int hostagecount = 0;
    int state = 0;
    int choice = 0;
    boolean havedead = false;
    for (final Creature p : i.pool) {
      if (p.health().alive() && p.alignment() != Alignment.LIBERAL && p.location() == cr.location()) {
        hostagecount++;
      }
      if (!p.health().alive()) {
        havedead = true;
      }
    }
    do {
      setView(R.layout.hospital);
      i.activeSquad.location().get().printLocationHeader();
      cr.printCreatureInfo(255);
      if (cr.income() > 0) {
        ui().text(format("%1$s made %2$s yesterday. What now?", cr.toString(), cr.income())).add();
      } else {
        ui().text(format("Taking Action: What will %1$s be doing today?", cr.toString())).add();
      }
      ui(R.id.gcontrol).button('a').text("Engaging in Liberal Activism").add();
      ui(R.id.gcontrol).button('b').text("Legal Fundraising").add();
      ui(R.id.gcontrol).button('c').text("Illegal Fundraising").add();
      ui(R.id.gcontrol).button('d').text("Make/Repair Clothing").add();
      maybeAddButton(R.id.gcontrol, 'h', "Heal Liberals", cr.skill().skill(Skill.FIRSTAID) != 0);
      if (cr.health().canWalk()) {
        ui(R.id.gcontrol).button('s').text("Stealing a Car").add();
      } else {
        maybeAddButton(R.id.gcontrol, 's', "Procuring a Wheelchair",
            !cr.hasFlag(CreatureFlag.WHEELCHAIR));
      }
      ui(R.id.gcontrol).button('t').text("Teaching Other Liberals").add();
      maybeAddButton(R.id.gcontrol, 'i', "Tend to a Conservative hostage", hostagecount > 0);
      maybeAddButton(R.id.gcontrol, 'm', "Move to the Free CLINIC", cr.health().clinicTime() > 0);
      maybeAddButton(R.id.gcontrol, 'l', "Learn in the University District", cr.health()
          .clinicTime() == 0);
      maybeAddButton(R.id.gcontrol, 'z', "Dispose of bodies", havedead);
      if (state == 'a' || state == 'b' || state == 'c' || state == 'd') {
        ui().button('?').text("Help").add();
      }
      ui(R.id.gcontrol).button(ENTER).text("Confirm Selection").add();
      ui(R.id.gcontrol).button(11).text("Nothing for Now").add();
      ui().text(format(cr.activity().type().longdesc(cr), cr.toString())).add();
      int c = getch();
      if (c == 'h' && cr.skill().skill(Skill.FIRSTAID) != 0) {
        cr.activity(new BareActivity(Activity.HEAL));
        break;
      } else if (c == 'i' && hostagecount > 0) {
        final AbstractActivity oact = cr.activity();
        cr.activity(BareActivity.noActivity());
        selectTendhostage(cr);
        if (cr.activity().type() == Activity.HOSTAGETENDING) {
          break;
        }
        cr.activity(oact);
      } else if (c == 's') {
        if (cr.health().canWalk()) {
          cr.activity(new BareActivity(Activity.STEALCARS));
          return;
        } else if (!cr.hasFlag(CreatureFlag.WHEELCHAIR)) {
          cr.activity(new BareActivity(Activity.WHEELCHAIR));
          return;
        }
      } else if (c == 'm' && cr.health().clinicTime() > 0) {
        cr.activity(new BareActivity(Activity.CLINIC));
        return;
      } else if (c == 'z' && havedead) {
        cr.activity(new BareActivity(Activity.BURY));
        return;
      } else if (c == 11) {
        cr.activity(BareActivity.noActivity());
        return;
      } else if (c == ENTER)
        return;
      else if (c == 63) {
        if (state == 'a' || state == 'b' || state == 'c' || state == 'd') {
          Curses.showHelp();
        }
      } else if (c >= 'a' && c <= 'z') {
        state = c;
        switch (state) {
        case 'a':
          c = liberalActivism(cr);
          break;
        case 'b':
          c = earningMoney(cr);
          break;
        case 'c':
          c = earningMoneyIllegally();
          break;
        case 'd':
          c = alteringClothing();
          break;
        case 't':
          c = teaching();
          break;
        default:
          break;
        }
        choice = c;
        switch (state) {
        case 'a':
        default:
          selectedActivism(cr, choice);
          return;
        case 'b':
          selectedRaisingMoney(cr, choice);
          return;
        case 'c':
          selectedIllegalFundraising(cr, choice);
          return;
        case 'd':
          selectedClothing(cr, choice);
          return;
        case 'l':
          updateClassChoice(cr);
          return;
        case 't':
          selectedTeaching(cr, choice);
          return;
        }
      }
    } while (true);
  }

  private static void activateBulk() {
    final List<Creature> pool = new ArrayList<Creature>();
    for (final Creature p : Filter.of(i.pool, Filter.AVAILABLE)) {
      if (p.squad().exists() && p.squad().get().activity().type() != Activity.NONE) {
        continue;
      }
      pool.add(p);
    }
    if (pool.isEmpty())
      return;
    int selectedactivity = 0;
    do {
      setView(R.layout.generic);
      ui().text("Money: %1$s" + i.ledger.funds()).add();
      ui().text("Activate Uninvolved Liberals").add();
      ui(R.id.gcontrol).text("BULK ACTIVITY").add();
      maybeAddGreenButton(R.id.gcontrol, '1', "Engaging in Liberal Activism", selectedactivity == 0);
      maybeAddGreenButton(R.id.gcontrol, '2', "Legal Fundraising", selectedactivity == 1);
      maybeAddGreenButton(R.id.gcontrol, '3', "Illegal Fundraising", selectedactivity == 2);
      maybeAddGreenButton(R.id.gcontrol, '4', "Checking Polls", selectedactivity == 3);
      maybeAddGreenButton(R.id.gcontrol, '5', "Stealing Cars", selectedactivity == 4);
      maybeAddGreenButton(R.id.gcontrol, '6', "Community Service", selectedactivity == 5);
      ui(R.id.gcontrol).text("CODE NAME - CURRENT ACTIVITY").add();
      int y = 'a';
      for (final Creature p : pool) {
        ui(R.id.gcontrol).button(y++).text(p.toString() + " - " + p.activity().toString())
            .color(p.activity().type().set_activity_color()).add();
      }
      ui(R.id.gcontrol).button(10).text("Continue the struggle.").add();
      final int c = getch();
      if (c >= 'a') {
        final Creature liberal = pool.get(c - 'a');
        switch (selectedactivity) {
        case 0: // Activism
        default:
          if (liberal.skill().getAttribute(Attribute.WISDOM, true) > 7) {
            liberal.activity(new BareActivity(Activity.COMMUNITYSERVICE));
          } else if (liberal.skill().getAttribute(Attribute.WISDOM, true) > 4) {
            liberal.activity(new BareActivity(Activity.TROUBLE));
          } else if (liberal.skill().skill(Skill.COMPUTERS) > 2) {
            liberal.activity(new BareActivity(Activity.HACKING));
          } else if (liberal.skill().skill(Skill.ART) > 1) {
            liberal.activity(new MuralActivity(Activity.GRAFFITI, null));
          } else {
            liberal.activity(new BareActivity(Activity.TROUBLE));
          }
          break;
        case 1: // Fundraising
          if (liberal.skill().skill(Skill.ART) > 1) {
            liberal.activity(new BareActivity(Activity.SELL_ART));
          } else if (liberal.skill().skill(Skill.MUSIC) > 1) {
            liberal.activity(new BareActivity(Activity.SELL_MUSIC));
          } else if (liberal.skill().skill(Skill.TAILORING) > 1) {
            liberal.activity(new BareActivity(Activity.SELL_TSHIRTS));
          } else {
            liberal.activity(new BareActivity(Activity.DONATIONS));
          }
          break;
        case 2: // Illegal Fundraising
          if (liberal.skill().skill(Skill.COMPUTERS) > 1) {
            liberal.activity(new BareActivity(Activity.CCFRAUD));
          } else if (liberal.skill().skill(Skill.SEDUCTION) > 1) {
            liberal.activity(new BareActivity(Activity.PROSTITUTION));
          } else {
            liberal.activity(new BareActivity(Activity.SELL_DRUGS));
          }
          break;
        case 3: // Check polls
          liberal.activity(new BareActivity(Activity.POLLS));
          break;
        case 4: // Steal cars
          liberal.activity(new BareActivity(Activity.STEALCARS));
          break;
        case 5: // Volunteer
          liberal.activity(new BareActivity(Activity.COMMUNITYSERVICE));
          break;
        }
      }
      if (c >= '1' && c <= '6') {
        selectedactivity = c - '1';
      }
      if (c == 10) {
        break;
      }
    } while (true);
  }

  private static void activateSleeper(final Creature cr) {
    do {
      setView(R.layout.hospital);
      i.activeSquad.location().get().printLocationHeader();
      cr.printCreatureInfo(255);
      ui().text("Taking undercover action:").bold().add();
      ui().text(format("What will %s focus on?", cr.toString())).add();
      ui(R.id.gcontrol).button('a').text("Communication and Advocacy").add();
      ui(R.id.gcontrol).button('b').text("Espionage").add();
      ui(R.id.gcontrol).button('c').text("Join the Active LCS").add();
      ui(R.id.gcontrol).button(10).text("Confirm Selection").add();
      final int category = getch();
      int choice = 0;
      if (category == 10)
        return;
      else if (category >= 'a') {
        switch (category) {
        case 'a':
        default:
          setView(R.layout.generic);
          ui(R.id.gcontrol).button('1').text("Lay low").add();
          ui(R.id.gcontrol).button('2').text("Advocate Liberalism").add();
          maybeAddButton(R.id.gcontrol, '3', "Expand Sleeper Network", cr.subordinatesLeft() > 0);
          choice = getch();
          break;
        case 'b':
          setView(R.layout.generic);
          ui(R.id.gcontrol).button('1').text("Uncover Secrets").add();
          ui(R.id.gcontrol).button('2').text("Embezzle Funds").add();
          ui(R.id.gcontrol).button('3').text("Steal Equipment").add();
          choice = getch();
          break;
        }
      }
      switch (category) {
      case 'a':
      default:
        switch (choice) {
        case '1':
        default:
          cr.activity(BareActivity.noActivity());
          return;
        case '2':
          cr.activity(new BareActivity(Activity.SLEEPER_LIBERAL));
          return;
        case '3':
          cr.activity(new BareActivity(Activity.SLEEPER_RECRUIT));
          return;
        }
      case 'b':
        switch (choice) {
        case '1':
        default:
          cr.activity(new BareActivity(Activity.SLEEPER_SPY));
          return;
        case '2':
          cr.activity(new BareActivity(Activity.SLEEPER_EMBEZZLE));
          return;
        case '3':
          cr.activity(new BareActivity(Activity.SLEEPER_STEAL));
          return;
        }
      case 'c':
        cr.activity(new BareActivity(Activity.SLEEPER_JOINLCS));
        return;
      }
    } while (true);
  }

  private static int alteringClothing() {
    int c;
    setView(R.layout.generic);
    ui(R.id.gcontrol).button('1').text("Make Clothing").add();
    ui(R.id.gcontrol).button('2').text("Repair Clothing").add();
    c = getch();
    return c;
  }

  private static int earningMoney(final Creature cr) {
    int c;
    setView(R.layout.generic);
    ui(R.id.gcontrol).button('1').text("Solicit Donations").add();
    if (cr.skill().skill(Skill.TAILORING) > 4) {
      ui(R.id.gcontrol).button('2').text("Sell Embroidered Shirts").add();
    } else {
      ui(R.id.gcontrol).button('2').text("Sell Tie-Dyed T-Shirts").add();
    }
    ui(R.id.gcontrol).button('3').text("Sell Portrait Sketches").add();
    ui(R.id.gcontrol).button('4').text("Play Street Music").add();
    c = getch();
    return c;
  }

  private static int earningMoneyIllegally() {
    int c;
    setView(R.layout.generic);
    ui(R.id.gcontrol).button('1').text("Sell Brownies").add();
    ui(R.id.gcontrol).button('2').text("Prostitution").add();
    ui(R.id.gcontrol).button('3').text("Steal Credit Card Numbers").add();
    c = getch();
    return c;
  }

  private static int liberalActivism(final Creature cr) {
    int c;
    setView(R.layout.generic);
    ui(R.id.gcontrol).button('1').text("Community Service").add();
    ui(R.id.gcontrol).button('2').text("Liberal Disobedience").add();
    ui(R.id.gcontrol).button('3').text("Graffiti").add();
    ui(R.id.gcontrol).button('4').text("Search Opinion Polls").add();
    ui(R.id.gcontrol).button('5').text("Hacking").add();
    ui(R.id.gcontrol).button('6').text("Write to Newspapers").add();
    if (cr.location().exists()
        && cr.location().get().compoundWalls().contains(Compound.PRINTINGPRESS)) {
      ui(R.id.gcontrol).button('7').text("Write for The Liberal Guardian").add();
    }
    c = getch();
    return c;
  }

  private static void maybeAddGreenButton(final int parent, final char ch, final String text,
      final boolean green) {
    if (green) {
      ui(parent).button(ch).text(text).color(Color.GREEN).add();
    } else {
      ui(parent).button(ch).text(text).add();
    }
  }

  private static void selectedActivism(final Creature cr, final int choice) {
    switch (choice) {
    case '1':
      cr.activity(new BareActivity(Activity.COMMUNITYSERVICE));
      return;
    case '2':
      cr.activity(new BareActivity(Activity.TROUBLE));
      return;
    case '3':
      cr.activity(new MuralActivity(Activity.GRAFFITI, null));
      return;
    case '4':
      cr.activity(new BareActivity(Activity.POLLS));
      return;
    case '5':
      cr.activity(new BareActivity(Activity.HACKING));
      return;
    case '6':
      cr.activity(new BareActivity(Activity.WRITE_LETTERS));
      return;
    case '7':
      if (cr.location().exists()
          && cr.location().get().compoundWalls().contains(Compound.PRINTINGPRESS)) {
        cr.activity(new BareActivity(Activity.WRITE_GUARDIAN));
      }
      return;
    default:
      if (cr.skill().getAttribute(Attribute.WISDOM, true) > 7) {
        cr.activity(new BareActivity(Activity.COMMUNITYSERVICE));
      } else if (cr.skill().getAttribute(Attribute.WISDOM, true) > 4) {
        cr.activity(new BareActivity(Activity.TROUBLE));
      } else if (cr.skill().skill(Skill.COMPUTERS) > 2) {
        cr.activity(new BareActivity(Activity.HACKING));
      } else if (cr.skill().skill(Skill.ART) > 1) {
        cr.activity(new MuralActivity(Activity.GRAFFITI, null));
      } else {
        cr.activity(new BareActivity(Activity.TROUBLE));
      }
    }
  }

  private static void selectedClothing(final Creature cr, final int choice) {
    switch (choice) {
    case '1':
      final AbstractActivity oact = cr.activity();
      cr.activity(BareActivity.noActivity());
      selectMakeclothing(cr);
      if (cr.activity().type() == Activity.MAKE_ARMOR) {
        break;
      }
      cr.activity(oact);
      break;
    case '2':
    default:
      cr.activity(new BareActivity(Activity.REPAIR_ARMOR));
      break;
    }
  }

  private static void selectedIllegalFundraising(final Creature cr, final int choice) {
    switch (choice) {
    case '1':
      cr.activity(new BareActivity(Activity.SELL_DRUGS));
      break;
    case '2':
      cr.activity(new BareActivity(Activity.PROSTITUTION));
      break;
    case '3':
      cr.activity(new BareActivity(Activity.CCFRAUD));
      break;
    default:
      if (cr.skill().skill(Skill.COMPUTERS) > 1) {
        cr.activity(new BareActivity(Activity.CCFRAUD));
      } else if (cr.skill().skill(Skill.SEDUCTION) > 1) {
        cr.activity(new BareActivity(Activity.PROSTITUTION));
      } else {
        cr.activity(new BareActivity(Activity.SELL_DRUGS));
      }
    }
  }

  private static void selectedRaisingMoney(final Creature cr, final int choice) {
    switch (choice) {
    case '1':
      cr.activity(new BareActivity(Activity.DONATIONS));
      return;
    case '2':
      cr.activity(new BareActivity(Activity.SELL_TSHIRTS));
      return;
    case '3':
      cr.activity(new BareActivity(Activity.SELL_ART));
      return;
    case '4':
      cr.activity(new BareActivity(Activity.SELL_MUSIC));
      return;
    default:
      if (cr.skill().skill(Skill.ART) > 1) {
        cr.activity(new BareActivity(Activity.SELL_ART));
      } else if (cr.skill().skill(Skill.TAILORING) > 1) {
        cr.activity(new BareActivity(Activity.SELL_TSHIRTS));
      } else if (cr.skill().skill(Skill.MUSIC) > 1) {
        cr.activity(new BareActivity(Activity.SELL_MUSIC));
      } else {
        cr.activity(new BareActivity(Activity.DONATIONS));
      }
    }
  }

  private static void selectedTeaching(final Creature cr, final int choice) {
    switch (choice) {
    case '1':
      cr.activity(new BareActivity(Activity.TEACH_POLITICS));
      return;
    case '2':
      cr.activity(new BareActivity(Activity.TEACH_COVERT));
      return;
    case '3':
      cr.activity(new BareActivity(Activity.TEACH_FIGHTING));
      return;
    default:
      cr.activity(new BareActivity(cr.type().teaches()));
      return;
    }
  }

  private static void selectMakeclothing(final Creature cr) {
    final List<ArmorType> armortypei = new ArrayList<ArmorType>();
    for (final ArmorType a : Game.type.armor.values()) {
      int difficulty = 0;
      if (a.makeDifficulty == 0) {
        continue;
      }
      if (a.deathsquadLegality
          && (i.issue(Issue.POLICEBEHAVIOR).lawGT(Alignment.ARCHCONSERVATIVE) || i.issue(
              Issue.DEATHPENALTY).lawGT(Alignment.ARCHCONSERVATIVE))) {
        continue;
      }
      difficulty = a.makeDifficulty(cr);
      if (difficulty > cr.skill().skill(Skill.TAILORING) * 2 + 5) {
        continue;
      }
      armortypei.add(a);
    }
    Collections.sort(armortypei, new Comparator<ArmorType>() {
      @Override public int compare(final @Nullable ArmorType object1,
          final @Nullable ArmorType object2) {
        assert object1 != null;
        assert object2 != null;
        return object1.toString().compareTo(object2.toString());
      }
    });
    final StringBuilder str = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text(
          format("Which will %1$s try to make?   (Note: Half Cost if you have cloth)",
              cr.toString())).add();
      ui().text("NAME - DIFFICULTY - COST").add();
      int y = 'a', difficulty;
      Color color = Color.WHITE;
      for (final ArmorType p : armortypei) {
        difficulty = p.makeDifficulty(cr);
        str.append(p.toString());
        str.append(" - ");
        final int displayDifficulty = Math.max(difficulty - cr.skill().skill(Skill.TAILORING), 0);
        switch (displayDifficulty) {
        case 0:
          color = Color.GREEN;
          str.append("Simple");
          break;
        case 1:
          color = Color.CYAN;
          str.append("Very Easy");
          break;
        case 2:
          color = Color.CYAN;
          str.append("Easy");
          break;
        case 3:
          color = Color.BLUE;
          str.append("Below Average");
          break;
        case 4:
          color = Color.WHITE;
          str.append("Average");
          break;
        case 5:
          color = Color.WHITE;
          str.append("Above Average");
          break;
        case 6:
          color = Color.YELLOW;
          str.append("Hard");
          break;
        case 7:
          color = Color.MAGENTA;
          str.append("Very Hard");
          break;
        case 8:
          color = Color.MAGENTA;
          str.append("Extremely Difficult");
          break;
        case 9:
          color = Color.RED;
          str.append("Nearly Impossible");
          break;
        default:
          color = Color.RED;
          str.append("Impossible");
          break;
        }
        str.append(" - $" + p.makePrice);
        ui(R.id.gcontrol).button(y++).text(str.toString()).color(color).add();
        str.setLength(0);
      }
      ui().text("Press a Letter to select a Type of Clothing").add();
      final int c = getch();
      if (c >= 'a') {
        cr.activity(new ItemActivity(Activity.MAKE_ARMOR, armortypei.get(c - 'a')));
        return;
      }
      if (c == Curses.ENTER) {
        break;
      }
    } while (true);
  }

  private static void selectTendhostage(final Creature cr) {
    final List<Creature> pool = Filter.of(i.pool, Filter.livingIn(cr.location().get()));
    if (pool.isEmpty())
      return;
    if (pool.size() == 1) {
      cr.activity(new CreatureActivity(Activity.HOSTAGETENDING, pool.get(0)));
      return;
    }
    final StringBuilder str = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text("Which hostage will " + cr.toString() + " be watching over?").add();
      ui().text("CODE NAME / SKILL / HEALTH / LOCATION").add();
      ui().text("DAYS IN CAPTIVITY").add();
      int y = 'a';
      for (final Creature p : pool) {
        str.append(p.toString());
        boolean bright = false;
        int skill = 0;
        for (final Skill sk : Skill.values()) {
          skill += p.skill().skill(sk);
          if (p.skill().skillXp(sk) >= 100 + 10 * p.skill().skill(sk)
              && p.skill().skill(sk) < p.skill().skillCap(sk, true)) {
            bright = true;
          }
        }
        str.append(skill + (bright ? "*" : ""));
        str.append(p.health().healthStat());
        if (p.location().exists()) {
          str.append(p.location().get().toString());
        }
        str.append(cr.joindays() + (cr.joindays() != 1 ? " days" : " day"));
        ui(R.id.gcontrol).button(y++).text(str.toString()).add();
        str.setLength(0);
      }
      ui().text("Press a Letter to select a Conservative").add();
      final int c = getch();
      if (c >= 'a') {
        cr.activity(new CreatureActivity(Activity.HOSTAGETENDING, pool.get(c - 'a')));
        return;
      }
      if (c == Curses.ENTER) {
        break;
      }
    } while (true);
  }

  private static int teaching() {
    int c;
    setView(R.layout.generic);
    ui().text("Teach Liberals About What?").add();
    ui(R.id.gcontrol).button('1').text("Political Activism").add();
    ui(R.id.gcontrol).button('2').text("Infiltration").add();
    ui(R.id.gcontrol).button('3').text("Urban Warfare").add();
    c = getch();
    return c;
  }

  private static void updateClassChoice(final Creature cr) {
    while (true) {
      setView(R.layout.generic);
      ui().text("Classes cost $60 a day. Study what?").add();
      int key = 'a';
      for (final Activity a : CLASSES) {
        ui(R.id.gcontrol).button(key++).text(a.toString()).add();
      }
      final int choice = getch();
      if (choice == Curses.ENTER)
        return;
      if (choice >= 'a' && choice < key) {
        cr.activity(new BareActivity(CLASSES[choice - 'a']));
        return;
      }
    }
  }
}
