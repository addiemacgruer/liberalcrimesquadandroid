package lcs.android.site;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Iterator;
import java.util.Set;

import lcs.android.R;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.site.map.MapChangeRecord;
import lcs.android.site.map.SpecialBlocks;
import lcs.android.site.map.TileSpecial;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class Advance {
  /** handles end of round stuff for living creatures */
  public static void creatureAdvance() {
    for (final Creature p : Filter.of(i.activeSquad(), Filter.LIVING)) {
      advanceCreature(p);
      if (p.prisoner() != null) {
        advancePrisoner(p);
      }
    }
    if (i.site.current().lcs().siege.siege) { // oops!
      // NPE
      for (final Creature p : i.pool) {
        if (!p.health().alive()) {
          continue;
        }
        if (p.squad() != null) {
          continue;
        }
        if (p.location() != i.site.current()) {
          continue;
        }
        advanceCreature(p); // TODO
      }
      i.site.current().autoPromote();
    }
    if (!i.currentEncounter().isEmpty()) {
      for (final Creature e : i.currentEncounter().creatures()) {
        if (!e.health().alive()) {
          continue;
        }
        advanceCreature(e); // TODO
      }
    }
    if (i.mode() != GameMode.CHASECAR) {
      Advance.squadgrabImmobile(false);
      Advance.squadgrabImmobile(true);
    }
    if (!i.currentEncounter().isEmpty()) {
      for (final Iterator<Creature> ei = i.currentEncounter().creatures().iterator(); ei.hasNext();) {
        final Creature e = ei.next();
        if (!e.health().alive()) {
          if (i.mode() == GameMode.SITE) {
            e.dropLoot(i.groundLoot());
          }
          ei.remove();
        }
      }
    }
    if (i.mode() == GameMode.SITE) {
      spreadFire();
    }
    if (Curses.lastView() == R.layout.generic) {
      waitOnOK();
    }
  }

  public static int treatBleeding(final Creature creature, final int aBleed,
      final Creature topmedical) {
    int bleed = aBleed;
    for (final BodyPart w : BodyPart.values()) {
      if (creature.health().wounds().get(w).contains(Wound.BLEEDING)) {
        if (i.rng.nextInt(500) < creature.skill().getAttribute(Attribute.HEALTH, true)) {
          creature.health().wounds().get(w).remove(Wound.BLEEDING);
        } else if (creature.squad() != null
            && topmedical.skill().skillCheck(Skill.FIRSTAID, CheckDifficulty.FORMIDABLE)) {
          setViewIfNeeded(R.layout.generic);
          ui().text(
              topmedical.toString() + " was able to slow the bleeding of " + creature.toString()
                  + "'s wounds.").color(Color.GREEN).add();
          topmedical.skill().train(Skill.FIRSTAID,
              Math.max(50 - topmedical.skill().skill(Skill.FIRSTAID) * 2, 0));
          creature.health().wounds().get(w).remove(Wound.BLEEDING);
        }
        bleed++;
      }
    }
    return bleed;
  }

  private static void advanceCreature(final Creature creature) {
    int bleed = 0;
    final Creature topmedical = getTopMedical(creature);
    if (topmedical != null) {
      bleed = treatBleeding(creature, bleed, topmedical);
    }
    final Set<TileSpecial> flag = i.site.currentTile().flag;
    if (i.mode() == GameMode.SITE && i.rng.likely(3)
        && (flag.contains(TileSpecial.FIRE_PEAK) || flag.contains(TileSpecial.FIRE_END))) {
      int burndamage = flag.contains(TileSpecial.FIRE_PEAK) ? 40 : 20;
      if (creature.getArmor().isFireprotection()) {
        int denom = creature.getArmor().isDamaged() ? 6 : 4;
        denom += creature.getArmor().quality().num - 1;
        burndamage = (int) (burndamage * (3.0 / denom));
      }
      creature.health().blood(creature.health().blood() - burndamage);
      if (!creature.health().alive()) {
        setViewIfNeeded(R.layout.generic);
        creature.deathMessage();
        if (creature.prisoner() != null) {
          creature.freeHostage(Creature.Situation.DIED);
        }
      } else {
        setViewIfNeeded(R.layout.generic);
        ui().text(creature + " is burned!").color(Color.RED).add();
      }
    }
    if (bleed > 0) {
      creature.health().blood(creature.health().blood() - bleed);
      // if (i.site.siteLevelmap() != null) {
      flag.add(TileSpecial.BLOODY);
      // }
      if (!creature.isNaked()) {
        creature.getArmor().setBloody(true);
      }
      if (!creature.health().alive()) {
        setViewIfNeeded(R.layout.generic);
        creature.deathMessage();
        if (creature.prisoner() != null) {
          creature.freeHostage(Creature.Situation.DIED);
        }
      }
    }
  }

  private static void advancePrisoner(final Creature p) {
    if (p.prisoner() == null) {
      return;
    }
    advanceCreature(p.prisoner());
    if (p.prisoner().health().alive() || p.prisoner().squad() != null) {
      return;
    }
    setViewIfNeeded(R.layout.generic);
    ui().text(p.toString() + " drops " + p.prisoner().toString() + "'s body.").add();
    p.prisoner().dropLoot(i.groundLoot());
    i.site.crime(i.site.crime() + 10);
    i.siteStory.addNews(NewsEvent.KILLED_SOMEBODY);
    if (p
        .prisoner()
        .type()
        .ofType(
            new String[] { "CORPORATE_CEO", "RADIOPERSONALITY", "NEWSANCHOR", "SCIENTIST_EMINENT",
                "JUDGE_CONSERVATIVE" })) {
      i.site.crime(i.site.crime() + 30);
    }
    p.prisoner(null);
  }

  @Nullable private static Creature getTopMedical(final Creature cr) {
    int topmedicalskill = -1;
    Creature topmedical = null;
    for (final Creature p : i.activeSquad()) {
      if (p.health().alive() && p.stunned() == 0 && p.health().blood() > 40 && p != cr
          && p.skill().skill(Skill.FIRSTAID) > topmedicalskill) {
        topmedical = p;
        topmedicalskill = p.skill().skill(Skill.FIRSTAID);
      }
    }
    return topmedical;
  }

  private static void spreadFire() {
    if (i.site.alarm() && i.site.crime() > 10) {
      i.site.postAlarmTimer(i.site.postAlarmTimer() + 1);
    }
    if (i.site.alarmTimer() > 0 && i.site.alarm() && i.site.crime() > 5) {
      i.site.alarmTimer(i.site.alarmTimer() - 1);
      if (i.site.alarmTimer() <= 0) {
        i.site.alarmTimer(0);
        setViewIfNeeded(R.layout.generic);
        ui().text("The Squad smells Conservative panic.").color(Color.YELLOW).add();
      }
    }
    final int MAPZ = i.site.siteLevelmap()[0][0].length;
    final int MAPY = i.site.siteLevelmap()[0].length;
    final int MAPX = i.site.siteLevelmap().length;
    for (int z = 0; z < MAPZ; z++) {
      boolean stairs = false; // Will check if higher levels are
      // accessible
      for (int y = 0; y < MAPY; y++) {
        for (int x = 0; x < MAPX; x++) {
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.EXIT)) {
            continue;
          }
          if (i.site.siteLevelmap()[x][y][z].special != null) {
            if (i.site.siteLevelmap()[x][y][z].special == SpecialBlocks.STAIRS_UP) {
              stairs = true;
            }
          }
          // Extinguish ending fires
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.FIRE_END)) {
            if (i.rng.chance(15)) {
              i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.FIRE_END);
              i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.DEBRIS);
            }
          }
          // Cool/spread peak fires
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.FIRE_PEAK)) {
            i.site.onFire(true);
            if (i.rng.chance(10)) {
              i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.FIRE_PEAK);
              i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.FIRE_END);
            } else if (i.rng.chance(4)) // Spread fire
            {
              int dir = i.rng.nextInt(4); // Random initial
              // direction
              int tries = 0; // Will try all four directions
              // before giving up
              while (tries < 4) {
                int xmod = 0;
                int ymod = 0;
                switch (dir) {
                case 0:
                  xmod = -1;
                  break;
                case 1:
                  xmod = 1;
                  break;
                case 2:
                  ymod = -1;
                  break;
                case 3:
                  ymod = 1;
                  break;
                default:
                }
                // Check if the tile is a valid place to
                // spread fire to
                if (x + xmod < MAPX
                    && x + xmod >= 0
                    && y + ymod < MAPY
                    && y + ymod >= 0
                    && !i.site.siteLevelmap()[x + xmod][y + ymod][z].flag
                        .contains(TileSpecial.FIRE_START)
                    && !i.site.siteLevelmap()[x + xmod][y + ymod][z].flag
                        .contains(TileSpecial.DEBRIS)
                    && !i.site.siteLevelmap()[x + xmod][y + ymod][z].flag
                        .contains(TileSpecial.FIRE_PEAK)
                    && !i.site.siteLevelmap()[x + xmod][y + ymod][z].flag
                        .contains(TileSpecial.FIRE_END)
                    && !i.site.siteLevelmap()[x + xmod][y + ymod][z].flag
                        .contains(TileSpecial.EXIT)) {
                  // Spread it
                  i.site.siteLevelmap()[x + xmod][y + ymod][z].flag.add(TileSpecial.FIRE_START);
                  break;
                }
                // Else try another direction
                tries++;
                dir++;
                dir %= 4;
              }
              if (tries == 5) {
                // Check if up is valid
                if (z < MAPZ
                    && !i.site.siteLevelmap()[x][y][z + 1].flag.contains(TileSpecial.FIRE_START)
                    && !i.site.siteLevelmap()[x][y][z + 1].flag.contains(TileSpecial.DEBRIS)
                    && !i.site.siteLevelmap()[x][y][z + 1].flag.contains(TileSpecial.FIRE_PEAK)
                    && !i.site.siteLevelmap()[x][y][z + 1].flag.contains(TileSpecial.FIRE_END)) {
                  // Spread it
                  i.site.siteLevelmap()[x][y][z + 1].flag.add(TileSpecial.FIRE_START);
                }
              }
            }
          }
          // Aggravate starting fires
          if (i.site.siteLevelmap()[x][y][z].flag.contains(TileSpecial.FIRE_START)) {
            if (i.rng.chance(5)) {
              final MapChangeRecord change = new MapChangeRecord(x, y, z, TileSpecial.DEBRIS);
              i.site.current().changes().add(change);
              i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.BLOCK);
              i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.DOOR);
              i.site.siteLevelmap()[x][y][z].flag.remove(TileSpecial.FIRE_START);
              i.site.siteLevelmap()[x][y][z].flag.add(TileSpecial.FIRE_PEAK);
              i.site.crime(i.site.crime() + 5);
            }
          }
        }
      }
      // If no stairs to the next level were found, don't continue to
      // that level
      if (!stairs) {
        break;
      }
    }
  }

  /** haul dead/paralyzed */
  private static void squadgrabImmobile(final boolean dead) {
    // DRAGGING PEOPLE OUT IF POSSIBLE
    int hostslots = 0;
    for (final Creature p : i.activeSquad()) {
      if (p.health().alive() && (p.health().canWalk() || p.hasFlag(CreatureFlag.WHEELCHAIR))
          && p.prisoner() != null) {
        hostslots++;
      } else if ((!p.health().alive() || !p.health().canWalk()
          && !p.hasFlag(CreatureFlag.WHEELCHAIR))
          && p.prisoner() != null) {
        setViewIfNeeded(R.layout.generic);
        ui().text(p + " can no longer handle " + p.prisoner() + ".").add();
        p.prisoner().freeHostage(Creature.Situation.DIED);
      }
    }
    for (final Creature p : Filter.of(i.activeSquad(), Filter.ALL)) {
      if (!p.health().alive() && dead || p.health().alive() && !p.hasFlag(CreatureFlag.WHEELCHAIR)
          && !p.health().canWalk() && !dead) {
        if (hostslots == 0) {
          if (!p.health().alive()) {
            setViewIfNeeded(R.layout.generic);
            ui().text("Nobody can carry Martyr " + p + ".").add();
            // DROP LOOT
            p.dropLoot(i.groundLoot());
          } else {
            setViewIfNeeded(R.layout.generic);
            ui().text(p + " is left to be captured.").add();
            p.captureByPolice(Crime.LOITERING);
          }
        } else {
          for (final Creature carrier : i.activeSquad()) {
            if (carrier == p) {
              continue;
            }
            if (carrier.health().alive()
                && (carrier.health().canWalk() || carrier.hasFlag(CreatureFlag.WHEELCHAIR))
                && carrier.prisoner() == null) {
              carrier.prisoner(p);
              setViewIfNeeded(R.layout.generic);
              ui().text(
                  carrier.toString() + " hauls " + p + (!p.health().alive() ? "'s body" : "") + ".")
                  .add();
              break;
            }
          }
          hostslots--;
        }
        i.activeSquad().remove(p);
      }
    }
  }
}