package lcs.android.basemode.iface;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lcs.android.R;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.Gender;
import lcs.android.creature.skill.Skill;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.site.Squad;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class ReviewMode {
  private ReviewMode() {}

  public static void assemblesquad(@Nullable final Squad aSquad) {
    Squad squad = aSquad;
    boolean newsquad = false;
    if (squad == null) {
      i.setActiveSquad(squad = new Squad());
      newsquad = true;
    }
    final Location culloc = i.currentLocation; // squad.location().getNullable();
    final List<Creature> temppool = new ArrayList<Creature>();
    for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
      if (p.health().alive() && p.alignment() == Alignment.LIBERAL
          && p.health().clinicMonths() == 0 && p.datingVacation() == 0 && p.hiding() == 0
          && !p.hasFlag(CreatureFlag.SLEEPER) && p.location().exists()
          && !p.location().get().type().isPrison() && (p.location().get() == culloc)) {
        temppool.add(p);
      }
    }
    i.activeSortingChoice.get(CreatureList.ASSEMBLESQUAD).sort(temppool);
    // BUILD LIST OF BASES FOR EACH SQUAD IN CASE IT ENDS UP EMPTY
    // THEN WILL DROP ITS LOOT THERE
    final Map<Squad, Location> squadloc = new HashMap<Squad, Location>();
    // squadloc.resize(squad.size());
    for (final Squad sl : i.squad) {
      squadloc.put(sl, sl.location().getNullable());
    }
    Color color = Color.WHITE;
    final StringBuilder sb = new StringBuilder();
    do {
      final int squadsize = i.activeSquad().size();
      setView(R.layout.generic);
      if (squadsize < 6) {
        ui().text("Assemble the squad!").add();
      } else {
        ui().text("The squad is full.").add();
      }
      if (newsquad) {
        ui().text("New Squad").add();
      } else {
        ui().text("Squad: " + i.activeSquad().toString()).add();
      }
      ui(R.id.gcontrol).text("CODE NAME - SKILL - HEALTH - PROFESSION").add();
      int y = 'a';
      for (final Creature p : temppool) {
        sb.setLength(0);
        sb.append(p.toString()).append(" - ");
        int skill = 0;
        for (final Skill sk : Skill.values()) {
          skill += p.skill().skill(sk);
        }
        sb.append(skill).append(" - ").append(p.health().healthStat()).append(" - ");
        if (p.squad().getNullable() == i.activeSquad()) {
          sb.append("SQUAD");
          color = Color.GREEN;
        } else if (p.squad().getNullable() != null) {
          sb.append("OTHER");
          color = Color.YELLOW;
        } else if (i.activeSquad().location().exists()) {
          if (i.activeSquad().location().get() != p.location().get()) {
            sb.append("AWAY");
            color = Color.BLUE;
          } else if (p.activity().type() == Activity.NONE) {
            sb.append("IDLE"); // probably in the clinic or
          } else {
            color = Color.BLACK;
            sb.append(p.activity().type());
            color = Color.BLACK;
          }
        } else {
          sb.append("UNAVL"); // probably in the clinic or something
          color = Color.BLACK;
        }
        sb.append(" - ").append(p.type().jobtitle(p));
        if (squadsize == 0 || squad.location() == p.location()) {
          ui(R.id.gcontrol).button(y).text(sb.toString()).color(color).add();
        } else {
          ui(R.id.gcontrol).button().text(sb.toString()).add();
        }
        y++;
        sb.setLength(0);
      }
      ui().text("Press a button to add or remove a Liberal from the squad.").add();
      ui().button(7).text("Sort people.").add();
      // addText(R.id.gmessages,"V - View a Liberal");
      if (squadsize > 0) {
        ui().button('8').text("The squad is ready.").add();
        ui().button('9').text("Dissolve the squad.").add();
      } else {
        ui().button('8').text("I need no squad!").add();
      }
      final int c = getch();
      if (c >= 'a') {
        final Creature liberal = c - 'a' < temppool.size() ? temppool.get(c - 'a') : null;
        if (liberal == null) {
          continue;
        }
        if (squadsize != 0 && i.activeSquad().location() != liberal.location()) {
          fact("Liberals must be in the same location to form a Squad.");
        } else if (!liberal.health().canWalk() && !liberal.hasFlag(CreatureFlag.WHEELCHAIR)) {
          fact("Squad Liberals must be able to move around.  Have this Liberal procure a wheelchair.");
        } else if (liberal.squad().getNullable() == squad) {
          squad.remove(liberal);
        } else if (squadsize < 6) {
          i.activeSquad().add(liberal);
        }
      }
      if (c == '7') {
        CreatureList.ASSEMBLESQUAD.sortingPrompt();
        i.activeSortingChoice.get(CreatureList.ASSEMBLESQUAD).sort(temppool);
      }
      if (c == '8') {
        // CHECK IF GOOD
        boolean good = true;
        boolean care = false;
        for (final Creature p : i.activeSquad()) {
          if (p.alignment() == Alignment.LIBERAL) {
            care = true;
            break;
          }
          good = false;
        }
        if (good || care) {
          break;
        }
        fact("You cannot form a Squad with only Conservatives!");
      }
      if (c == '9') {
        for (final Creature p : i.activeSquad()) {
          p.squad(null);
        }
        i.activeSquad().clear();
      }
    } while (true);
    // FINALIZE NEW SQUADS
    boolean hasmembers = i.activeSquad().size() > 0;
    if (newsquad) {
      if (hasmembers) {
        i.activeSquad().name(
            query("What shall we designate this Liberal squad?", i.activeSquad().toString()));
        i.squad.add(i.activeSquad());
      } else {
        i.setActiveSquad(null);
      }
    }
  }

  protected static void review() {
    final StringBuilder sb = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text("Review your Liberals and Assemble Squads").add();
      ui().text("SQUAD NAME - LOCATION - ACTIVITY").add();
      int y = 'a';
      for (final Squad p : i.squad) {
        // if(i.activesquad==p)set_color(Color.WHITE,Color.BLACK,1);
        // else set_color(Color.WHITE,Color.BLACK,0);
        // addch(y+'A'-2);
        sb.append(p.toString());
        sb.append(" - ");
        if (p.location().exists()) {
          sb.append(p.location().get().toString());
          if (p == i.activeSquad()) {
            sb.append(" (active squad)");
          }
          sb.append(" - ");
        }
        if (p.size() > 0) {
          String str = p.activity().toString();
          if (p.activity().type() == Activity.NONE) {
            int count = 0;
            boolean haveact = false;
            for (final Creature p2 : p) {
              count++;
              if (p2.activity().type() != Activity.NONE) {
                str = p2.activity().toString();
                haveact = true;
              }
            }
            if (haveact && count > 1) {
              str = "Acting Individually";
            }
          }
          sb.append(str);
          ui(R.id.gcontrol).button(y).text(sb.toString()).add();
          sb.setLength(0);
        }
        y++;
      }
      final boolean squadless = anySquadless();
      ui(R.id.gcontrol).text("REVIEW").add();
      for (final ReviewModes rm : ReviewModes.values()) {
        final boolean any = Filter.any(i.pool, rm.category());
        if (any) {
          ui(R.id.gcontrol).button('1' + rm.ordinal()).text(rm.toString())
              .color(rm.categoryColor()).add();
        } else {
          ui(R.id.gcontrol).button().text(rm.toString()).color(rm.categoryColor()).add();
        }
      }
      if (Filter.any(i.location, Filter.HAS_LOOT)) {
        ui(R.id.gcontrol).button('8').text("Review and Move Equipment").color(Color.CYAN).add();
      } else {
        ui(R.id.gcontrol).button().text("Review and Move Equipment").color(Color.CYAN).add();
      }
      ui(R.id.gcontrol).text("ACTIONS").add();
      ui(R.id.gcontrol).button(11).text("Promote Liberals").add();
      if (squadless) {
        ui(R.id.gcontrol).button(12).text("Assemble a New Squad").add();
        ui(R.id.gcontrol).button(13).text("Assign New Bases to the Squadless").add();
      } else {
        ui(R.id.gcontrol).button().text("Assemble a New Squad").add();
        ui(R.id.gcontrol).button().text("Assign New Bases to the Squadless").add();
      }
      ui(R.id.gcontrol).button(10).text("Return").add();
      final int c = getch();
      if (c == ENTER) {
        return;
      }
      if (c >= 'a') {
        final Squad sq = i.squad.get(c - 'a');
        if (sq != null) {
          if (sq == i.activeSquad()) {
            assemblesquad(sq);
          } else {
            i.setActiveSquad(sq);
          }
        }
      }
      if (c >= '1' && c <= '7') {
        reviewMode(ReviewModes.values()[c - '1']);
      }
      if (c == '8') {
        ReviewMode.equipmentbaseassign();
      }
      if (c == 12) {
        assemblesquad(null);
        if (i.activeSquad() == null && i.squad.size() > 0) {
          i.setActiveSquad(i.squad.get(i.squad.size() - 1));
        }
      }
      if (c == 13) {
        squadlessBaseAssign();
      }
      if (c == 11) {
        promoteliberals();
      }
    } while (true);
  }

  private static boolean anySquadless() {
    for (final Creature c : Filter.of(i.pool, Filter.ACTIVE_LIBERAL)) {
      if (!c.squad().exists()) {
        return true;
      }
    }
    return false;
  }

  private static CreatureList ascFromReview(final ReviewModes mode) {
    switch (mode) {
    case AWAY:
      return CreatureList.AWAY;
    case CLINIC:
      return CreatureList.CLINIC;
    case DEAD:
      return CreatureList.DEAD;
    case HOSTAGES:
      return CreatureList.HOSTAGES;
    case JUSTICE:
      return CreatureList.JUSTICE;
    case LIBERALS:
      return CreatureList.LIBERALS;
    case SLEEPERS:
    default:
      return CreatureList.SLEEPERS;
    }
  }

  private static void equipmentbaseassign() {
    final List<AbstractItem<? extends AbstractItemType>> baseLoot = new ArrayList<AbstractItem<? extends AbstractItemType>>();
    final Map<AbstractItem<? extends AbstractItemType>, Location> lootLoc = new HashMap<AbstractItem<? extends AbstractItemType>, Location>();
    final List<Location> lcsLocs = new ArrayList<Location>();
    for (final Location l : i.location) {
      if (l.renting() != CrimeSquad.LCS || l.lcs().siege.siege) {
        continue;
      }
      lcsLocs.add(l);
      for (final AbstractItem<? extends AbstractItemType> l2 : l.lcs().loot) {
        baseLoot.add(l2);
        lootLoc.put(l2, l);
      }
    }
    if (lcsLocs.isEmpty()) {
      return;
    }
    Collections.sort(baseLoot, new Comparator<AbstractItem<? extends AbstractItemType>>() {
      @Override public int compare(final @Nullable AbstractItem<? extends AbstractItemType> lhs,
          final @Nullable AbstractItem<? extends AbstractItemType> rhs) {
        assert lhs != null;
        assert rhs != null;
        return lhs.equipTitle().compareTo(rhs.equipTitle());
      }
    });
    Location selected = lcsLocs.get(0);
    do {
      int y = '0';
      setView(R.layout.generic);
      ui().text("Moving Equipment").bold().add();
      ui().text("Locations").add();
      for (final Location l : lcsLocs) {
        ui().button(y++).text(l.toString()).color(selected == l ? Color.GREEN : Color.BLACK).add();
      }
      ui().text("Item / Current location").add();
      for (final AbstractItem<? extends AbstractItemType> j : baseLoot) {
        maybeAddButton(R.id.gmessages, y++, j.equipTitle() + '/' + lootLoc.get(j),
            lootLoc.get(j) != selected);
      }
      ui().text("Select a location and then choose equipment to move to that location.").add();
      ui().text("Moving equipment takes no time.").add();
      ui(R.id.gcontrol).button(ENTER).text("Continue the struggle").add();
      final int c = getch();
      if (c == ENTER) {
        return;
      }
      if (c >= '0') {
        if (c - '0' < lcsLocs.size()) {
          selected = lcsLocs.get(c - '0');
          continue;
        }
        final AbstractItem<? extends AbstractItemType> j = baseLoot.get(c - '0' - lcsLocs.size());
        lootLoc.get(j).lcs().loot.remove(j);
        selected.lcs().loot.add(j);
        lootLoc.put(j, selected);
      }
    } while (true);
  }

  private static boolean killSquadMember(final Creature p) {
    // Kill squad member
    if (p.hire().missing()) {
      return false; // no boss to kill the squad member
    }
    setView(R.layout.generic);
    ui(R.id.gcontrol).button('c').text("Confirm").add();
    ui(R.id.gcontrol).button('x').text("Continue").add();
    final Creature boss = p.hire().get();
    ui().text("Confirm you want to have " + boss.toString() + " kill this squad member?").add();
    ui().text("Killing your squad members is Not a Liberal Act.").add();
    final int c = getch();
    clearChildren(R.id.gcontrol);
    if (c == 'c') {
      p.health().die();
      Squad.cleanGoneSquads();
      switch (i.rng.nextInt(3)) {
      case 0:
      default:
        ui().text(boss.toString() + " executes " + p.toString() + " by strangling to death.").add();
        break;
      case 1:
        ui().text(boss.toString() + " executes " + p.toString() + " by beating to death.").add();
        break;
      case 2:
        ui().text(boss.toString() + " executes " + p.toString() + " by freezing to death.").add();
        break;
      }
      if (i.rng.nextInt(boss.skill().getAttribute(Attribute.HEART, false)) > i.rng.nextInt(3)) {
        boss.skill().attribute(Attribute.HEART, -1);
        switch (i.rng.nextInt(4)) {
        case 0:
        default:
          ui().text(
              boss.toString()
                  + " feels sick to the stomach afterward and throws up in a trash can.")
              .color(Color.GREEN).add();
          break;
        case 1:
          ui().text(
              boss.toString()
                  + " feels sick to the stomach afterward and gets drunk, eventually falling asleep.")
              .color(Color.GREEN).add();
          break;
        case 2:
          ui().text(
              boss.toString()
                  + " feels sick to the stomach afterward and curls up in a ball, crying softly.")
              .color(Color.GREEN).add();
          break;
        case 3:
          ui().text(
              boss.toString()
                  + " feels sick to the stomach afterward and shoots up and collapses in a heap on the floor.")
              .color(Color.GREEN).add();
          boss.skill().train(Skill.SHOOTINGUP, 150);
          break;
        }
        ui().text(boss.toString() + " has lost heart.").add();
      } else if (i.rng.likely(3)) {
        ui().text(boss.toString() + " grows colder.").color(Color.CYAN).add();
        boss.skill().attribute(Attribute.WISDOM, +1);
        ui().text(boss.toString() + " has gained wisdom.").color(Color.CYAN).add();
      }
      Curses.waitOnOK();
      return true;
    }
    return false;
  }

  /* equipment - assign new bases to the equipment */
  /* base - review - assign new bases to the squadless */
  private static void promoteliberals() {
    final List<Creature> temppool = new ArrayList<Creature>(Filter.of(i.pool, Filter.LIBERAL));
    if (temppool.isEmpty()) {
      return;
    }
    final StringBuilder sb = new StringBuilder();
    // PROMOTE
    do {
      setView(R.layout.generic);
      ui().text("Promote the Elite Liberals").add();
      int y = 'a';
      Boolean enabled = true;
      for (final Creature creature : temppool) {
        enabled = true;
        sb.append(creature.toString());
        // sb.append(" - ");
        if (creature.hire().missing()) {
          sb.append(" (LCS Leader)");
          enabled = false;
        } else {
          sb.append(" -> ").append(creature.hire().get().toString());
          if (creature.hasFlag(CreatureFlag.LOVE_SLAVE)) {
            enabled = false;
            sb.append("<Refuses Promotion>");
          } else if (creature.hire().get().hire().missing()) {
            enabled = false;
            sb.append(" (LCS Leader)");
          } else if (creature.hire().get().hire().get().subordinatesLeft() == 0) {
            enabled = false;
            sb.append(" -> ").append(creature.hire().get().hire().get().toString())
                .append(" <Can't Lead More>");
          } else {
            sb.append(" -> ").append(creature.hire().get().hire().get().toString());
          }
        }
        maybeAddButton(R.id.gcontrol, y++, sb.toString(), enabled);
        sb.setLength(0);
      }
      ui(R.id.gcontrol).button(10).text("Continue the struggle.").add();
      ui().text("Choose a liberal to promote. You cannot Promote Liberals in Hiding.").add();
      ui().text("Enlightened liberals follow anyone. Seduced liberals follow only their lover.")
          .add();
      ui().text("CODE NAME - CURRENT CONTACT - CONTACT AFTER PROMOTION").add();
      final int c = getch();
      if (c >= 'a') {
        final Creature creature = temppool.get(c - 'a');
        creature.hire(creature.hire().get().hire().get());
      }
      if (c == 10) {
        return;
      }
    } while (true);
  }

  private static void reviewMode(final ReviewModes mode) { // TODO this is dodgy.
    final List<Creature> temppool = Filter.of(i.pool, mode.category());
    if (temppool.isEmpty()) {
      Log.e("LCS", "No liberals in category:" + mode);
      return;
    }
    i.activeSortingChoice.get(ascFromReview(mode)).sort(temppool);
    final StringBuilder sb = new StringBuilder();
    do {
      setView(R.layout.generic);
      ui().text(mode.toString()).bold().color(mode.categoryColor()).add();
      switch (mode) {
      case LIBERALS:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - SQUAD / ACTIVITY").add();
        break;
      case HOSTAGES:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - DAYS IN CAPTIVITY").add();
        break;
      case JUSTICE:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - MONTHS LEFT").add();
        break;
      case CLINIC:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - PROGNOSIS").add();
        break;
      case SLEEPERS:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - PROFESSION").add();
        break;
      case DEAD:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - DAYS SINCE PASSING").add();
        break;
      case AWAY:
        ui().text("CODE NAME - SKILL - HEALTH - LOCATION - DAYS UNTIL RETURN").add();
        break;
      default:
        Log.e("LCS", "Undefined ReviewMode:" + mode);
      }
      int y = 'a';
      for (final Creature p : temppool) {
        // addch(y+'A'-2);addText(R.id.gmessages," - ");
        sb.append(p.toString());
        // int bright = 0;
        int skill = 0;
        for (final Skill sk : Skill.values()) {
          skill += p.skill().skill(sk);
        }
        // if (p.getSkillIp(sk) >= 100 + 10 * p.getSkill(sk)
        // && p.getSkill(sk) < p.skillCap(sk, true))
        // bright = 1;
        sb.append(" - ");
        sb.append(skill);
        sb.append(" - ");
        sb.append(p.health().healthStat());
        sb.append(" - ");
        if (p.location().missing()) {
          sb.append("Away");
        } else {
          sb.append(p.location().get().toString());
        }
        sb.append(" - ");
        switch (mode) {
        case LIBERALS: {
          boolean usepers = true;
          if (p.squad().exists() && p.squad().get().activity().type() != Activity.NONE) {
            sb.append("SQUAD");
            usepers = false;
          }
          if (usepers) {
            // Let's add some color here...
            sb.append(p.activity().type());
          }
          break;
        }
        case HOSTAGES: {
          sb.append(p.joindays() + " " + (p.joindays() > 1 ? "Days" : "Day"));
          break;
        }
        case JUSTICE: {
          if (p.crime().deathPenalty() && p.crime().sentence() != 0) {
            sb.append("DEATH ROW: ");
            sb.append(p.crime().sentence());
            sb.append(' ');
            if (p.crime().sentence() > 1) {
              sb.append("Months");
            } else {
              sb.append("Month");
            }
          } else if (p.crime().sentence() <= -1) {
            if (p.crime().sentence() < -1) {
              sb.append(-p.crime().sentence());
              sb.append(" Life Sentences");
            } else {
              sb.append("Life Sentence");
            }
          } else if (p.crime().sentence() != 0) {
            sb.append(p.crime().sentence());
            sb.append(' ');
            if (p.crime().sentence() > 1) {
              sb.append("Months");
            } else {
              sb.append("Month");
            }
          } else {
            sb.append("-------");
          }
          break;
        }
        case CLINIC: {
          sb.append("Out in ");
          sb.append(p.health().clinicMonths());
          sb.append(' ');
          if (p.health().clinicMonths() > 1) {
            sb.append("Months");
          } else {
            sb.append("Month");
          }
          break;
        }
        case SLEEPERS: {
          sb.append(p.type().jobtitle(p));
          break;
        }
        case DEAD: {
          sb.append(p.deathDays());
          sb.append(' ');
          if (p.deathDays() > 1) {
            sb.append("Days");
          } else {
            sb.append("Day");
          }
          break;
        }
        case AWAY: {
          if (p.hiding() == -1) {
            sb.append("<No Contact>");
          } else {
            sb.append(p.datingVacation() + p.hiding());
            sb.append(' ');
            if (p.datingVacation() + p.hiding() > 1) {
              sb.append("Days");
            } else {
              sb.append("Day");
            }
          }
          break;
        }
        default:
          Log.e("LCS", "Undefined ReviewMode:" + mode);
        }
        ui(R.id.gcontrol).button(y).text(sb.toString()).color(p.alignment().color()).add();
        sb.setLength(0);
        y++;
      }
      ui(R.id.gcontrol).button(10).text("Continue the Struggle").add();
      ui(R.id.gcontrol).button('9').text("Sort people.").add();
      int c = getch();
      if (c >= 'a') {
        Creature p = temppool.get(c - 'a');
        do {
          setView(R.layout.reviewprofile);
          if (p.alignment() != Alignment.LIBERAL) {
            setColor(R.id.textView1, Color.RED);
            setText(R.id.textView1, "Profile of an Automaton");
          }
          p.filloutFullStatus();
          // Add removal of squad members member
          if (Filter.ACTIVE_LIBERAL.apply(p) && !p.isFounder())
          /* If alive and not own boss? (suicide?) */
          {
            setEnabled(R.id.profileRemove, true);
            setEnabled(R.id.profileKill, true);
          }
          if (p.alignment() == Alignment.LIBERAL) {
            setEnabled(R.id.profileGender, true);
          }
          c = getch();
          if (c == '[') {
            int pos = temppool.indexOf(p);
            pos--;
            if (pos < 0) {
              pos = temppool.size() - 1;
            }
            p = temppool.get(pos);
            continue;
          }
          if (c == ']') {
            int pos = temppool.indexOf(p);
            pos++;
            if (pos > temppool.size() - 1) {
              pos = 0;
            }
            p = temppool.get(pos);
            continue;
          }
          if (c == 'n') {
            p.name(query(R.string.cdNewCodeName, p.toString()));
          }
          if (c == 'g') {
            switch (p.genderLiberal()) {
            case MALE:
              p.genderLiberal(Gender.NEUTRAL);
              break;
            case FEMALE:
              p.genderLiberal(Gender.MALE);
              break;
            default:
              p.genderLiberal(Gender.FEMALE);
              break;
            }
          }
          if (c == 'r') // If alive and not own boss? (suicide?)
          {
            final Creature boss = p.hire().getNullable();
            if (boss == null) {
              return;
            }
            setView(R.layout.generic);
            ui().text("Do you want to permanently release this squad member from the LCS?").add();
            ui().text("If the member has low heart they may go to the police.").add();
            ui(R.id.gcontrol).button('c').text("Confirm").add();
            ui(R.id.gcontrol).button('x').text("Continue").add();
            c = getch();
            clearChildren(R.id.gcontrol);
            if (c == 'c') {
              ui().text(p.toString() + " has been released.").add();
              /* Chance of member going to police if boss has criminal record and if they have low
               * heart TODO: Do law check against other members? */
              if (p.skill().getAttribute(Attribute.HEART, true) < p.skill().getAttribute(
                  Attribute.WISDOM, true)
                  + i.rng.nextInt(5)
                  && boss.crime().isCriminal()) {
                ui().text("A Liberal friend tips you off on " + p + "'s whereabouts.").add();
                ui().text(
                    "The Conservative traitor has ratted you out to the police, and sworn to testify against "
                        + boss.toString() + " in court.").add();
                boss.crime().criminalize(Crime.RACKETEERING).addTestimony();
                // TODO: Depending on the crime increase heat or
                // make siege
                if (boss.location().exists()) {
                  if (boss.location().get().lcs().heat > 20) {
                    boss.location().get().lcs().siege.timeUntilLocated = 3;
                  } else {
                    boss.location().get().lcs().heat += 20;
                  }
                }
              }
              // Remove squad member
              i.pool.remove(p);
              temppool.remove(p);
              getch();
              break;
            }
          } else if (c == 'k') {
            if (killSquadMember(p)) {
              return;
            }
          } else {
            break;
          }
        } while (true);
      }
      if (c == '9') {
        ascFromReview(mode).sortingPrompt();
        i.activeSortingChoice.get(ascFromReview(mode)).sort(temppool);
      }
      if (c == 10) {
        break;
      }
      sb.setLength(0);
    } while (true);
  }

  private static void squadlessBaseAssign() {
    final List<Creature> squadless = Filter.of(i.pool, Filter.HAS_SQUAD);
    final List<Location> lcsLocs = Filter.of(i.location, Filter.rented(CrimeSquad.LCS));
    if (lcsLocs.isEmpty()) {
      return;
    }
    Location selected = lcsLocs.get(0);
    do {
      int y = '0';
      setView(R.layout.generic);
      ui().text("Moving Liberals").bold().add();
      ui().text("Locations").add();
      for (final Location location : lcsLocs) {
        ui().button(y++).text(location.toString())
            .color(selected == location ? Color.GREEN : Color.BLACK).add();
      }
      ui().text("Item / Current location").add();
      for (final Creature liberal : squadless) {
        maybeAddButton(R.id.gmessages, y++, liberal.toString() + '/' + liberal.location().get(),
            liberal.location().get() != selected);
      }
      ui().text("Select a location and then choose Liberals to move to that location.").add();
      ui().text("Moving Liberals takes no time.").add();
      ui(R.id.gcontrol).button(ENTER).text("Continue the struggle").add();
      final int c = getch();
      if (c == ENTER) {
        return;
      }
      if (c >= '0') {
        if (c - '0' < lcsLocs.size()) {
          selected = lcsLocs.get(c - '0');
          continue;
        }
        final Creature j = squadless.get(c - '0' - lcsLocs.size());
        j.location(selected);
      }
    } while (true);
  }
}
