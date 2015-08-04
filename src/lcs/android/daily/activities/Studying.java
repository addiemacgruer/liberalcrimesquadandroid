package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.activities.BareActivity;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.Ledger;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Studying extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> students = this;
    for (final Creature s : students) {
      if (i.ledger.funds() < 60) {
        break;
      }
      i.ledger.subtractFunds(60, Ledger.ExpenseType.TRAINING);
      Skill skill = null;
      int effectiveness = 20;
      switch (s.activity().type()) {
      case STUDY_DEBATING:
        skill = Skill.PERSUASION;
        break;
      case STUDY_MARTIAL_ARTS:
        skill = Skill.HANDTOHAND;
        break;
      case STUDY_DRIVING:
        skill = Skill.DRIVING;
        break;
      case STUDY_PSYCHOLOGY:
        skill = Skill.PSYCHOLOGY;
        break;
      case STUDY_FIRST_AID:
        skill = Skill.FIRSTAID;
        break;
      case STUDY_LAW:
        skill = Skill.LAW;
        break;
      case STUDY_DISGUISE:
        skill = Skill.DISGUISE;
        break;
      case STUDY_SCIENCE:
        skill = Skill.SCIENCE;
        break;
      case STUDY_BUSINESS:
        skill = Skill.BUSINESS;
        break;
      case STUDY_GYMNASTICS:
        skill = Skill.DODGE;
        break;
      case STUDY_MUSIC:
        skill = Skill.MUSIC;
        break;
      case STUDY_ART:
        skill = Skill.ART;
        break;
      case STUDY_TEACHING:
        skill = Skill.TEACHING;
        break;
      case STUDY_WRITING:
        skill = Skill.WRITING;
        break;
      default:
        continue;
      }
      boolean worthcontinuing = false;
      // rapid decrease in effectiveness as your skill gets
      // higher.
      effectiveness /= s.skill().skill(skill) + 1;
      if (effectiveness < 1) {
        effectiveness = 1;
      }
      s.skill().train(skill, effectiveness);
      if (s.skill().skill(skill) < s.skill().skillCap(skill, true)) {
        worthcontinuing = true;
      }
      if (!worthcontinuing) {
        s.activity(BareActivity.noActivity());
        ui().text(s + " has learned as much as " + s.genderLiberal().heShe() + " can.").add();
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
