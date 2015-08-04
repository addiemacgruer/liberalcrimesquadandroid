package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Teaching extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> teachers = this;
    // Teaching
    for (final Creature t : teachers) {
      Skill[] skillarray = null;
      int cost = 0, studentcount = 0;
      // Build a list of skills to train and determine the cost for
      // running
      // a @NonNullByDefault class depending on what the teacher is teaching
      switch (t.activity().type()) {
      case TEACH_POLITICS:
        cost = 2;
        skillarray = new Skill[9];
        skillarray[0] = Skill.LAW;
        skillarray[1] = Skill.PERSUASION;
        skillarray[2] = Skill.WRITING;
        skillarray[3] = Skill.RELIGION;
        skillarray[4] = Skill.BUSINESS;
        skillarray[5] = Skill.SCIENCE;
        skillarray[6] = Skill.STREETSENSE;
        skillarray[7] = Skill.MUSIC;
        skillarray[8] = Skill.ART;
        break;
      case TEACH_COVERT:
        cost = 6;
        skillarray = new Skill[8];
        skillarray[0] = Skill.SECURITY;
        skillarray[1] = Skill.COMPUTERS;
        skillarray[2] = Skill.DISGUISE;
        skillarray[3] = Skill.TAILORING;
        skillarray[4] = Skill.STEALTH;
        skillarray[5] = Skill.SEDUCTION;
        skillarray[6] = Skill.PSYCHOLOGY;
        skillarray[7] = Skill.DRIVING;
        break;
      case TEACH_FIGHTING:
      default:
        cost = 10;
        skillarray = new Skill[13];
        skillarray[0] = Skill.KNIFE;
        skillarray[1] = Skill.SWORD;
        skillarray[2] = Skill.CLUB;
        skillarray[3] = Skill.PISTOL;
        skillarray[4] = Skill.RIFLE;
        skillarray[5] = Skill.SHOTGUN;
        skillarray[6] = Skill.HEAVYWEAPONS;
        skillarray[7] = Skill.AXE;
        skillarray[8] = Skill.SMG;
        skillarray[9] = Skill.THROWING;
        skillarray[10] = Skill.HANDTOHAND;
        skillarray[11] = Skill.DODGE;
        skillarray[12] = Skill.FIRSTAID;
        break;
      }
      // Count potential students for this teacher to get an idea of
      // efficiency
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(t.location().getNullable()))) {
        // Step through the array of skills to train
        for (final Skill j : skillarray) {
          // Otherwise, if the student has less skill than the
          // teacher, train the student
          // proportional to the difference in skill between
          // teacher and student times the
          // teacher's ability at teaching
          if (p.skill().skill(j) < t.skill().skill(j) - 1
              && p.skill().skill(j) < t.skill().skill(Skill.TEACHING) + 2
              && i.ledger.funds() > cost && p.skill().skill(j) < p.skill().skillCap(j, true)) {
            studentcount++;
          }
        }
      }
      ui().text(
          t.toString() + " taught " + studentcount + " student" + (studentcount != 1 ? "s" : "")
              + " about " + t.activity().type()).add();
      // Walk through and train people
      for (final Creature p : Filter.of(i.pool, Filter.livingIn(t.location().getNullable()))) {
        // Step through the array of skills to train
        for (final Skill j : skillarray) {
          // Otherwise, if the student has less skill than the
          // teacher, train the student
          // proportional to the difference in skill between
          // teacher and student times the
          // teacher's ability at teaching
          if (p.skill().skill(j) < t.skill().skill(j) - 1
              && p.skill().skill(j) < t.skill().skill(Skill.TEACHING) + 2
              && i.ledger.funds() > cost && p.skill().skill(j) < p.skill().skillCap(j, true)) {
            // Teach based on teacher's skill in the topic
            // plus skill in teaching, minus
            // student's skill in the topic
            int teach = t.skill().skill(j) + t.skill().skill(Skill.TEACHING) - p.skill().skill(j);
            // at ten students, cost no longer goes up, but
            // effectiveness goes down.
            if (studentcount > 10) {
              // teach = (teach * 10) / students; //teach
              // at 50% speed with twice as many students.
              teach = (teach * 30 / studentcount + teach) / 4; // 62.5%
            }
            // speed
            // with
            // twice
            // as
            // many
            // students.
            if (teach < 1) {
              teach = 1;
            }
            // Cap at 10 points per day
            if (teach > 10) {
              teach = 10;
            }
            p.skill().train(j, teach);
            /* if(students<10) { students++; i.ledger.subtract_funds
             * (cost,Ledger.ExpenseType.TRAINING); if(students==10)cost=0; } */
          }
        }
      }
      i.ledger.subtractFunds(cost * Math.min(studentcount, 10), Ledger.ExpenseType.TRAINING);
      t.skill().train(Skill.TEACHING, Math.min(studentcount, 10));
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
