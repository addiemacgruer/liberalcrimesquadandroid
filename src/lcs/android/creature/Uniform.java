package lcs.android.creature;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lcs.android.game.Quality;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Uniform implements Configurable {
  private final Map<String, Quality> quality = new HashMap<String, Quality>();

  @Override public String toString() {
    return quality.toString();
  }

  @Override public Configurable xmlChild(final String value) {
    return this;
  }

  @Override public void xmlFinishChild() {}

  @Override public void xmlSet(final String key, final String value) {
    quality.put(key, Quality.valueOf(value));
  }

  Quality hasDisguise(final Creature cr) {
    Quality rval = Quality.NONE;
    for (final Entry<String, Quality> e : quality.entrySet()) {
      if (!(e.getKey().startsWith("ARMOR_") || e.getKey().startsWith("MASK_"))) {
        continue;
      }
      if (e.getKey().equals(cr.getArmor().id())) {
        rval = e.getValue();
        break;
      }
    }
    return rval;
  }

  boolean inCharacter(final Creature cr) {
    return hasDisguise(cr) != Quality.NONE && weaponCheck(cr) != Quality.NONE;
  }

  private Quality weaponCheck(final Creature cr) {
    if (cr.weapon().weapon().id().equals("WEAPON_NONE")) {
      // acceptable
      return Quality.GOOD;
    }
    Quality rval = Quality.NONE;
    for (final Entry<String, Quality> e : quality.entrySet()) {
      if (!e.getKey().startsWith("WEAPON_")) {
        continue;
      }
      if (e.getKey().equals(cr.weapon().weapon().id())) {
        rval = e.getValue();
        break;
      }
    }
    return rval;
  }
}