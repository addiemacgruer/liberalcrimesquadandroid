package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.Iterator;

import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.FootChase;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.law.Crime;
import lcs.android.news.NewsStory;
import lcs.android.news.StoryType;
import lcs.android.site.type.PoliceStation;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Burials extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    for (final Creature p : Filter.of(i.pool, Filter.ALL)) {
      if (isEmpty()) {
        return;
      }
      if (p.health().alive()) {
        continue;
      }
      // MAKE BASE LOOT
      if (p.base().exists()) {
        p.dropLoot(p.base().get().lcs().loot);
      }
      // BURY
      i.pool.remove(p);
      ui().text(p.toString() + "'s body was disposed of.").add();
      final Iterator<Creature> bi = iterator();
      while (bi.hasNext()) {
        final Creature b = bi.next();
        if (!b.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE)) {
          final NewsStory ns = new NewsStory(StoryType.BURIALARREST);
          ns.location(Location.none());
          i.newsStories.add(ns);
          i.siteStory = ns;
          b.crime().criminalize(Crime.BURIAL);
          FootChase.attemptArrest(b, "disposing of bodies");
          // If a liberal has been killed or arrested they
          // should not do more burials.
          if (!b.health().alive() || b.location().exists()
              && b.location().get().type().isType(PoliceStation.class)) {
            bi.remove();
          }
        }
      }
      p.activity(BareActivity.noActivity());
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
