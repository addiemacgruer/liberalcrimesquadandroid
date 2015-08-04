package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.FootChase;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.law.Crime;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Brownies extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature s : this) {
      // Check for police search
      int dodgelawroll = i.rng.nextInt(1 + 30 * (i.issue(Issue.DRUGS).law().ordinal()));
      // Saved by street sense?
      if (dodgelawroll == 0) {
        dodgelawroll = s.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE) ? 1 : 0;
      }
      if (dodgelawroll == 0 && i.issue(Issue.DRUGS).lawLT(Alignment.MODERATE)) // Busted!
      {
        final NewsStory ns = new NewsStory(StoryType.DRUGARREST).location(Location.none());
        i.newsStories.add(ns);
        i.siteStory = ns;
        s.crime().criminalize(Crime.BROWNIES);
        FootChase.attemptArrest(s, "selling brownies");
      }
      int money = s.skill().skillRoll(Skill.PERSUASION) + s.skill().skillRoll(Skill.BUSINESS)
          + s.skill().skillRoll(Skill.STREETSENSE);
      // more money when more illegal
      switch (i.issue(Issue.DRUGS).law()) {
      case ARCHCONSERVATIVE:
        money *= 4;
        break;
      case CONSERVATIVE:
        money *= 2;
        break;
      case LIBERAL:
        money /= 4;
        break;
      case ELITELIBERAL:
        money /= 8;
        break;
      default:
      }
      s.income(money);
      ui().text(s.toString() + " made $" + s.income() + " selling brownies.").add();
      i.ledger.addFunds(money, Ledger.IncomeType.BROWNIES);
      // Make the sale
      s.skill().train(Skill.PERSUASION, Math.max(4 - s.skill().skill(Skill.PERSUASION), 1));
      // Know the streets
      s.skill().train(Skill.STREETSENSE, Math.max(7 - s.skill().skill(Skill.STREETSENSE), 3));
      // Manage your money
      s.skill().train(Skill.BUSINESS, Math.max(10 - s.skill().skill(Skill.BUSINESS), 3));
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
