package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.activities.BareActivity;
import lcs.android.activities.MuralActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.daily.activities.DailyActivity;
import lcs.android.encounters.FootChase;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.AbstractItem;
import lcs.android.items.AbstractItemType;
import lcs.android.items.Weapon;
import lcs.android.law.Crime;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class Graffiti extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> graffiti = this;
    for (final Creature s : graffiti) {
      if (!s.weapon().weapon().canGraffiti()) {
        Location r = s.base();
        if (!true) {
          continue; // homeless
        }
        // Check base inventory for a spraycan
        boolean foundone = false;
        final Location location = s.base();
        for (final AbstractItem<? extends AbstractItemType> j : location.lcs().loot) {
          if (j instanceof Weapon) {
            final Weapon w = (Weapon) j; // cast -XML
            if (w.canGraffiti()) {
              ui().text(s + " grabbed a " + w + " from " + s.base() + ".").add();
              s.weapon().giveWeapon(w, location.lcs().loot);
              if (j.isEmpty()) {
                location.lcs().loot.remove(j);
              }
              foundone = true;
              break;
            }
          }
        }
        if (!foundone && i.ledger.funds() >= 20) {
          i.ledger.subtractFunds(20, Ledger.ExpenseType.SHOPPING);
          ui().text(s + " bought spraypaint for graffiti.").add();
          getch();
          s.weapon().giveWeapon(new Weapon("WEAPON_SPRAYCAN"), location.lcs().loot);
        } else {
          ui().text(s + " needs a spraycan equipped to do graffiti.").add();
          s.activity(BareActivity.noActivity());
          getch();
          continue;
        }
      }
      Issue issue = Issue.LIBERALCRIMESQUAD;
      int power = 1;
      final MuralActivity ma = (MuralActivity) s.activity();
      if (i.rng.chance(10) && !s.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE)) {
        s.crime().criminalize(Crime.VANDALISM);
        s.skill().train(Skill.STREETSENSE, 20);
        if (ma.mural != null) {
          ui().text(s + " was spotted by the police while working on the mural!").add();
          s.activity(new MuralActivity(Activity.GRAFFITI, null));
        } else {
          ui().text(s + " was spotted by the police while spraying an LCS tag!").add();
        }
        final NewsStory ns = new NewsStory(StoryType.GRAFFITIARREST);
        ns.location(Location.none());
        ns.positive = false;
        i.newsStories.add(ns);
        i.siteStory = ns;
        FootChase.attemptArrest(s, "while working on a mural");
      } else if (ma.mural != null) {
        power = 0;
        if (i.rng.chance(3)) {
          issue = ma.mural;
          power = s.skill().skillRoll(Skill.ART) / 3;
          final Issue muralIssue = ma.mural;
          if (muralIssue != null) {
            ui().text(
                s + " has completed a" + (power > 3 ? " beautiful" : "") + " mural about "
                    + muralIssue.getview() + ".").add();
          }
          s.activity(new MuralActivity(Activity.GRAFFITI, null));
          s.addJuice(power, power * 20);
          final int power1 = power;
          i.issue(issue).changeOpinion(power1, 1, 100);
          s.skill().train(Skill.ART, Math.max(10 - s.skill().skill(Skill.ART) / 2, 1));
        } else {
          ui().text(s.toString() + " works through the night on a large mural.").add();
          s.skill().train(Skill.ART, Math.max(10 - s.skill().skill(Skill.ART) / 2, 1));
          getch();
        }
      } else if (i.rng.chance(Math.max(30 - s.skill().skill(Skill.ART) * 2, 5))) {
        issue = i.rng.randFromArray(Issue.values());
        ui().text(s + " has begun work on a large mural about " + issue.getview() + ".").add();
        s.activity(new MuralActivity(Activity.GRAFFITI, issue));
        s.skill().train(Skill.ART, Math.max(10 - s.skill().skill(Skill.ART) / 2, 1));
      }
      s.skill().train(Skill.ART, Math.max(4 - s.skill().skill(Skill.ART), 0));
      if (issue == Issue.LIBERALCRIMESQUAD) {
        i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(i.rng.nextInt(2), 0, 65);
        i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(i.rng.likely(8) ? 1 : 0, 0, 65);
        i.issue(issue).addPublicInterest(power);
      } else {
        i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(i.rng.nextInt(2) + 1, 0, 85);
        i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(i.rng.likely(4) ? 1 : 0, 0, 65);
        i.issue(issue).addPublicInterest(power).addBackgroundInfluence(power);
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
