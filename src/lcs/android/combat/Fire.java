package lcs.android.combat;

import static lcs.android.game.Game.*;
import lcs.android.creature.Creature;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Xml;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Weapon attacks may have a chance of causing a fire, or debris damage */
@NonNullByDefault class Fire implements Xml.Configurable { // NO_UCD
  @SuppressWarnings("unused") private boolean causesDebris = false;

  private int chance = 0;

  private int chanceCausesDebris = 0;

  /** if the weapon has fire damage defined, cause debris damage or a fire to start. Starting fire
   * adds juice, but criminalizes the party for arson.
   * @param a the creature causing a fire. */
  public void consider(final Creature a) {
    if (i.mode() == GameMode.SITE && i.rng.nextInt(100) < chanceCausesDebris) {
      i.site.current().changes().add(new MapChangeRecord(TileSpecial.DEBRIS));
    }
    if (i.mode() == GameMode.SITE && i.rng.nextInt(100) < chance
        && !i.site.currentTile().fireDamaged()) {
      i.site.currentTile().flag.add(TileSpecial.FIRE_START);
      i.site.crime(i.site.crime() + 3);
      a.addJuice(5, 500);
      i.activeSquad().criminalizeParty(Crime.ARSON);
      i.siteStory.addNews(NewsEvent.ARSON);
    }
  }

  @Override public Xml.Configurable xmlChild(final String value) {
    return this;
  }

  @Override public void xmlFinishChild() {
    // no action
  }

  @Override public void xmlSet(final String key, final String value) {
    if (key.equals("chance")) {
      chance = Xml.getInt(value);
    } else if (key.equals("chance_causes_debris")) {
      chanceCausesDebris = Xml.getInt(value);
    } else if (key.equals("causes_debris")) {
      causesDebris = Xml.getBoolean(value);
    }
  }
}