package lcs.android.creature.health;

import static lcs.android.creature.health.SpecialWounds.*;
import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.news.NewsEvent;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.Squad;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.site.type.Clinic;
import lcs.android.site.type.Shelter;
import lcs.android.site.type.University;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.SparseMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Health implements Serializable {
  public Health(final Creature creature) {
    c = creature;
  }

  private int blood = 100;

  private final Creature c;

  private int clinicMonths = 0;

  private boolean processedDeath = false;

  private final Map<SpecialWounds, Integer> special = SparseMap.of(SpecialWounds.class);

  private final Map<BodyPart, Set<Wound>> wounds = SparseMap.of(BodyPart.class);

  public boolean alive() {
    return blood() > 0;
  }

  public int armCount() {
    if (isCrippled()) {
      return 0;
    }
    return (missing(BodyPart.ARM_LEFT) ? 0 : 1) + (missing(BodyPart.ARM_RIGHT) ? 0 : 1);
  }

  public int blood() {
    return blood;
  }

  public Health blood(final int aBlood) {
    if (blood == 0) {
      return this;
    }
    blood = aBlood;
    if (aBlood <= 0) {
      die();
    }
    return this;
  }

  public boolean canWalk() {
    if (!alive()) {
      return false;
    }
    if (isCrippled() || special.get(SpecialWounds.LOWERSPINE).intValue() != 1) {
      return false;
    }
    if (legCount() == 0) {
      return false;
    }
    return true;
  }

  public int clinicMonths() {
    return clinicMonths;
  }

  /* common - determines how long a creature's injuries will take to heal */
  public int clinicTime() {
    int time = 0;
    for (final BodyPart w : BodyPart.values()) {
      if (wounds.get(w).contains(Wound.NASTYOFF) && blood() < 100) {
        time++;
      }
    }
    if (blood() <= 10) {
      time++;
    }
    if (blood() <= 50) {
      time++;
    }
    if (blood() < 100) {
      time++;
    }
    for (final Entry<SpecialWounds, Integer> e : special.entrySet()) {
      if (!e.getKey().defaultValue().equals(e.getValue())) {
        time++;
      }
    }
    return time;
  }

  public String describeLegCount() {
    final int legok = legCount();
    if (c.hasFlag(CreatureFlag.WHEELCHAIR)) {
      return getString(R.string.cdWheelchair);
    } else if (legok >= 1) {
      return getString(R.string.cdOnFoot);
    } else {
      return getString(R.string.cdOnFootQuote);
    }
  }

  public void die() {
    if (processedDeath) {
      return;
    }
    processedDeath = true;
    blood(0);
    c.activity(BareActivity.noActivity()); // requiescat in pace.
    c.location(Location.none());
    c.removeSquadInfo();
    stopBleeding();
    if (c.squad().exists()) {
      if (c.alignment() == Alignment.LIBERAL) {
        i.score.dead++;
      }
    } else if (c.enemy()
        && (c.type().animal() != Animal.ANIMAL || i.issue(Issue.ANIMALRESEARCH).law() == Alignment.ELITELIBERAL)) {
      i.score.kills++;
      if (i.site.current().lcs().siege.siege) {
        i.site.current().lcs().siege.kills++;
      }
      if (i.site.current().lcs().siege.siege && c.type().animal() == Animal.TANK) {
        i.site.current().lcs().siege.tanks--;
      }
      if (i.site.current().renting() == CrimeSquad.CCS) {
        i.score.ccsSiegeKills++;
      }
      if (c.type().animal() != Animal.ANIMAL
          || i.issue(Issue.ANIMALRESEARCH).law() == Alignment.ELITELIBERAL) {
        i.site.crime(i.site.crime() + 10);
        i.siteStory.addNews(NewsEvent.KILLED_SOMEBODY);
        i.activeSquad().criminalizeParty(Crime.MURDER);
      }
    }
    if (i.mode() == GameMode.SITE) {
      c.dropLoot(i.groundLoot());
    }
  }

  public int disfigurements() {
    int disfigs = 0;
    if (special.get(TEETH).intValue() < SpecialWounds.TEETH.defaultValue()) {
      disfigs++;
    }
    if (special.get(TEETH).intValue() < SpecialWounds.TEETH.defaultValue() / 2) {
      disfigs++;
    }
    if (special.get(TEETH).intValue() == 0) {
      disfigs++;
    }
    if (special.get(RIGHTEYE).intValue() == 0) {
      disfigs += 2;
    }
    if (special.get(LEFTEYE).intValue() == 0) {
      disfigs += 2;
    }
    if (special.get(TONGUE).intValue() == 0) {
      disfigs += 3;
    }
    if (special.get(NOSE).intValue() == 0) {
      disfigs += 3;
    }
    return disfigs;
  }

  public void doClinic() {
    c.health().clinicMonths(c.health().clinicMonths() - 1);
    for (final BodyPart w : BodyPart.values()) {
      if (c.health().missing(w)) {
        c.health().wounds.get(w).clear();
        c.health().wounds.get(w).add(Wound.CLEANOFF);
      } else {
        c.health().wounds.get(w).clear();
      }
    }
    int healthdamage = 0;
    if (c.health().getWound(SpecialWounds.RIGHTLUNG) != 1) {
      c.health().wound(SpecialWounds.RIGHTLUNG, 1);
      if (i.rng.chance(2)) {
        healthdamage++;
      }
    }
    if (c.health().getWound(SpecialWounds.LEFTLUNG) != 1) {
      c.health().wound(SpecialWounds.LEFTLUNG, 1);
      if (i.rng.chance(2)) {
        healthdamage++;
      }
    }
    if (c.health().getWound(SpecialWounds.HEART) != 1) {
      c.health().wound(SpecialWounds.HEART, 1);
      if (i.rng.chance(3)) {
        healthdamage++;
      }
    }
    c.health().wound(SpecialWounds.LIVER, 1);
    c.health().wound(SpecialWounds.STOMACH, 1);
    c.health().wound(SpecialWounds.RIGHTKIDNEY, 1);
    c.health().wound(SpecialWounds.LEFTKIDNEY, 1);
    c.health().wound(SpecialWounds.SPLEEN, 1);
    c.health().wound(SpecialWounds.RIBS, SpecialWounds.RIBS.defaultValue());
    if (c.health().getWound(SpecialWounds.NECK) == 0) {
      c.health().wound(SpecialWounds.NECK, 2);
    }
    if (c.health().getWound(SpecialWounds.UPPERSPINE) == 0) {
      c.health().wound(SpecialWounds.UPPERSPINE, 2);
    }
    if (c.health().getWound(SpecialWounds.LOWERSPINE) == 0) {
      c.health().wound(SpecialWounds.LOWERSPINE, 2);
    }
    // Inflict permanent health damage
    c.skill().attribute(Attribute.HEALTH,
        c.skill().getAttribute(Attribute.HEALTH, false) - healthdamage);
    if (c.skill().getAttribute(Attribute.HEALTH, false) <= 0) {
      c.skill().attribute(Attribute.HEALTH, 1);
    }
    if (c.health().blood() <= 20 && c.health().clinicMonths() <= 2) {
      c.health().blood(50);
    }
    if (c.health().blood() <= 50 && c.health().clinicMonths() <= 1) {
      c.health().blood(75);
    }
    // If at clinic and in critical condition, transfer to
    // university hospital
    if (c.health().clinicMonths() > 2 && c.location().exists()
        && c.location().get().type().isType(Clinic.class)) {
      final Location hospital = AbstractSiteType.type(University.class).getLocation();
      c.location(hospital);
      fact(c + " has been transferred to " + hospital + ".");
    }
    // End treatment
    if (c.health().clinicMonths() == 0) {
      c.health().blood(100);
      if (c.location().exists()) {
        fact(c + " has left " + c.location().get() + ".");
      } else {
        fact(c + " has left the clinic.");
      }
      final Location hs = AbstractSiteType.type(Shelter.class).getLocation();
      if (c.base().exists() && c.base().get().lcs().siege.siege) {
        c.base(hs);
      }
      c.location(c.base().get());
    }
  }

  public int getWound(final SpecialWounds sw) {
    return special.get(sw).intValue();
  }

  public String healthStat() {
    final StringBuilder str = new StringBuilder();
    // int woundsum = 0;
    boolean bleeding = false;
    for (final BodyPart w : BodyPart.values()) {
      if (wounds.get(w).contains(Wound.BLEEDING)) {
        bleeding = true;
      }
    }
    final int armok = armCount();
    final int legok = legCount();
    if (bleeding) {
      str.append("Bleeding, ");
    }
    if (!alive()) {
      str.append("Deceased");
    } else if (blood() <= 20) {
      str.append("Near Death");
    } else if (blood() <= 50) {
      str.append("Badly Wounded");
    } else if (blood() <= 75) {
      str.append("Wounded");
    } else if (blood() < 100) {
      str.append("Lightly Wounded");
    } else if (special.get(SpecialWounds.NECK).intValue() == 0) {
      str.append("Neck Broken");
    } else if (special.get(SpecialWounds.UPPERSPINE).intValue() == 0) {
      str.append("Quadraplegic");
    } else if (special.get(SpecialWounds.LOWERSPINE).intValue() == 0) {
      str.append("Paraplegic");
    } else if (special.get(SpecialWounds.RIGHTEYE).intValue() == 0
        && special.get(SpecialWounds.LEFTEYE).intValue() == 0
        && special.get(SpecialWounds.NOSE).intValue() == 0) {
      str.append("Face Gone");
    } else if (legok == 0 && armok == 0) {
      str.append("No Limbs");
    } else if (legok == 1 && armok == 0 || armok == 1 && legok == 0) {
      str.append("One Limb");
    } else if (legok == 2 && armok == 0) {
      str.append("No Arms");
    } else if (legok == 0 && armok == 2) {
      str.append("No Legs");
    } else if (legok == 1 && armok == 1) {
      str.append("One Arm, One Leg");
    } else if (armok == 1) {
      str.append("One Arm");
    } else if (legok == 1) {
      str.append("One Leg");
    } else if (special.get(SpecialWounds.RIGHTEYE).intValue() == 0
        && special.get(SpecialWounds.LEFTEYE).intValue() == 0) {
      str.append("Blind");
    } else if ((special.get(SpecialWounds.RIGHTEYE).intValue() == 0 || special.get(
        SpecialWounds.LEFTEYE).intValue() == 0)
        && special.get(SpecialWounds.NOSE).intValue() == 0) {
      str.append("Face Mutilated");
    } else if (special.get(SpecialWounds.NOSE).intValue() == 0) {
      str.append("Missing Nose");
    } else if (special.get(SpecialWounds.RIGHTEYE).intValue() == 0
        || special.get(SpecialWounds.LEFTEYE).intValue() == 0) {
      str.append("Missing Eye");
    } else if (special.get(SpecialWounds.TONGUE).intValue() == 0) {
      str.append("No Tongue");
    } else if (special.get(SpecialWounds.TEETH).intValue() == 0) {
      str.append("No Teeth");
    } else if (special.get(SpecialWounds.TEETH).intValue() < SpecialWounds.TEETH.defaultValue()) {
      str.append("Missing Teeth");
    } else if (c.alignment() == Alignment.CONSERVATIVE) {
      str.append("Conservative");
    } else if (c.alignment() == Alignment.MODERATE) {
      str.append("Moderate");
    } else if (c.type().animal() == Animal.ANIMAL) {
      str.append("Animal");
    } else {
      str.append("Liberal");
    }
    return str.toString();
  }

  public void hospitalize(final Location loc) {
    // He's dead, Jim
    if (!c.health().alive()) {
      return;
    }
    final int time = c.health().clinicTime();
    if (time > 0) {
      Squad patientsquad = null;
      if (c.squad().exists()) {
        patientsquad = c.squad().get();
      }
      clinicMonths = time;
      c.squad(null).location(loc);
      // Inform about the hospitalization
      fact(c + " will be at " + loc + " for " + time + " month" + (time > 1 ? "s" : "") + ".");
      if (patientsquad != null) {
        patientsquad.remove(c);
        patientsquad.testClear(loc);
      }
    }
  }

  public int legCount() {
    if (isCrippled()) {
      return 0;
    }
    return (missing(BodyPart.LEG_LEFT) ? 0 : 1) + (missing(BodyPart.LEG_RIGHT) ? 0 : 1);
  }

  public boolean missing(final BodyPart bp) {
    return wounds.get(bp).contains(Wound.NASTYOFF) || wounds.get(bp).contains(Wound.CLEANOFF);
  }

  /** modifier for a skill roll based on the creature's critical injuries
   * @return the modifier. */
  public int modRoll() {
    int aroll = 0;
    if (getWound(SpecialWounds.RIGHTEYE) != 1) {
      aroll -= i.rng.nextInt(2);
    }
    if (getWound(SpecialWounds.LEFTEYE) != 1) {
      aroll -= i.rng.nextInt(2);
    }
    if (getWound(SpecialWounds.RIGHTEYE) != 1 && getWound(SpecialWounds.LEFTEYE) != 1) {
      aroll -= i.rng.nextInt(20);
    }
    if (getWound(SpecialWounds.RIGHTLUNG) != 1) {
      aroll -= i.rng.nextInt(8);
    }
    if (getWound(SpecialWounds.LEFTLUNG) != 1) {
      aroll -= i.rng.nextInt(8);
    }
    if (getWound(SpecialWounds.HEART) != 1) {
      aroll -= i.rng.nextInt(10);
    }
    if (getWound(SpecialWounds.LIVER) != 1) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.STOMACH) != 1) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.RIGHTKIDNEY) != 1) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.LEFTKIDNEY) != 1) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.SPLEEN) != 1) {
      aroll -= i.rng.nextInt(4);
    }
    if (getWound(SpecialWounds.LOWERSPINE) != 1) {
      aroll -= i.rng.nextInt(100);
    }
    if (getWound(SpecialWounds.UPPERSPINE) != 1) {
      aroll -= i.rng.nextInt(200);
    }
    if (getWound(SpecialWounds.NECK) != 1) {
      aroll -= i.rng.nextInt(300);
    }
    if (getWound(SpecialWounds.RIBS) < SpecialWounds.RIBS.defaultValue()) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.RIBS) < SpecialWounds.RIBS.defaultValue() / 2) {
      aroll -= i.rng.nextInt(5);
    }
    if (getWound(SpecialWounds.RIBS) == 0) {
      aroll -= i.rng.nextInt(5);
    }
    return aroll;
  }

  public void specialWoundDescription() {
    final StringBuilder sb = new StringBuilder();
    for (final SpecialWounds sw : SpecialWounds.values()) {
      if (sw.isDamaged(special.get(sw))) {
        sb.append(sw.description(special.get(sw)));
        sb.append('\n');
      }
    }
    if (blood() <= 20) {
      sb.append("Cadaverous");
    } else if (blood() <= 50) {
      sb.append("Ashen");
    } else if (blood() <= 75) {
      sb.append("Pallid");
    } else if (blood() < 100) {
      sb.append("Pale");
    }
    if (sb.length() > 0) {
      Curses.ui(R.id.ghealth).text(sb.toString()).color(Color.RED).add();
    }
  }

  public void stopBleeding() {
    for (final BodyPart w : BodyPart.values()) {
      wounds.get(w).remove(Wound.BLEEDING);
    }
  }

  public Health wound(final SpecialWounds sw, final int damage) {
    special.put(sw, damage);
    return this;
  }

  public Map<BodyPart, Set<Wound>> wounds() {
    return wounds;
  }

  public void woundStatus() {
    final StringBuilder sb = new StringBuilder();
    Color color = Color.GREEN;
    int sum = 0;
    for (final BodyPart bp : BodyPart.values()) {
      sb.append(c.type().animal().partName(bp) + ": ");
      if (wounds.get(bp).contains(Wound.NASTYOFF)) {
        sb.append("Ripped off");
      } else if (wounds.get(bp).contains(Wound.CLEANOFF)) {
        sb.append("Severed");
      } else {
        for (final Wound w : Wound.values()) {
          if (wounds.get(bp).contains(w)) {
            sum++;
          }
        }
        if (sum == 0) {
          color = c.alignment().color();
          if (c.type().animal() == Animal.ANIMAL) {
            sb.append("animal");
          } else {
            sb.append(c.alignment());
          }
        } else {
          color = Color.RED;
          for (final Wound w : Wound.values()) {
            if (wounds.get(bp).contains(w)) {
              sb.append(w);
              sum--;
              if (sum > 0) {
                sb.append(',');
              }
            }
          }
        }
      }
      Curses.ui(R.id.ghealth).text(sb.toString()).color(color).narrow().add();
      sb.setLength(0);
    }
  }

  private Health clinicMonths(final int months) {
    clinicMonths = months;
    return this;
  }

  private boolean isCrippled() {
    return special.get(SpecialWounds.NECK).intValue() != 1
        || special.get(SpecialWounds.UPPERSPINE).intValue() != 1;
  }

  private static final long serialVersionUID = Game.VERSION;
}