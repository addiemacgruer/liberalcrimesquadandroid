package lcs.android.items;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lcs.android.combat.Attack;
import lcs.android.creature.health.Wound;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

public @NonNullByDefault class WeaponType extends AbstractItemType {
  static class Builder extends AbstractItemTypeBuilder<WeaponType> {
    private final List<Attack> attacks_ = new ArrayList<Attack>();

    private boolean auto_break_lock_;

    private float bashstrengthmod_;

    private boolean can_graffiti_;

    private boolean can_take_hostages_;

    private boolean can_threaten_hostages_;

    private boolean instrument_;

    private int legality_;

    private boolean musical_attack_;

    private boolean protects_against_kidnapping_;

    private int size_;

    private boolean suspicious_;

    private boolean threatening_;

    @Override public WeaponType build() {
      return new WeaponType(this);
    }

    @Override public Configurable xmlChild(final String value) {
      if (value.equals("attack")) {
        final Attack a = new Attack();
        attacks_.add(a);
        return a;
      }
      return Xml.UNCONFIGURABLE;
    }

    @Override public void xmlFinishChild() {
      WeaponType lt = this.build();
      Game.type.weapon.put(lt.idname, lt);
    }

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("can_take_hostages")) {
        can_take_hostages_ = Xml.getBoolean(value);
      } else if (key.equals("threatening")) {
        threatening_ = Xml.getBoolean(value);
      } else if (key.equals("can_threaten_hostages")) {
        can_threaten_hostages_ = Xml.getBoolean(value);
      } else if (key.equals("protects_against_kidnapping")) {
        protects_against_kidnapping_ = Xml.getBoolean(value);
      } else if (key.equals("musical_attack")) {
        musical_attack_ = Xml.getBoolean(value);
      } else if (key.equals("instrument")) {
        instrument_ = Xml.getBoolean(value);
      } else if (key.equals("graffiti")) {
        can_graffiti_ = Xml.getBoolean(value);
      } else if (key.equals("legality")) {
        legality_ = Xml.getInt(value);
      } else if (key.equals("bashstrengthmod")) {
        bashstrengthmod_ = Xml.getInt(value);
      } else if (key.equals("auto_break_locks")) {
        auto_break_lock_ = Xml.getBoolean(value);
      } else if (key.equals("suspicious")) {
        suspicious_ = Xml.getBoolean(value);
      } else if (key.equals("size")) {
        size_ = Xml.getInt(value);
      } else {
        super.xmlSet(key, value);
      }
    }
  }

  /** @param builder */
  public WeaponType(Builder b) {
    super(b);
    this.attacks_.addAll(b.attacks_);
    this.auto_break_lock_ = b.auto_break_lock_;
    this.bashstrengthmod_ = b.bashstrengthmod_;
    this.can_graffiti_ = b.can_graffiti_;
    this.can_take_hostages_ = b.can_take_hostages_;
    this.can_threaten_hostages_ = b.can_threaten_hostages_;
    this.instrument_ = b.instrument_;
    this.legality_ = b.legality_;
    this.musical_attack_ = b.musical_attack_;
    this.protects_against_kidnapping_ = b.protects_against_kidnapping_;
    this.size_ = b.size_;
    this.suspicious_ = b.suspicious_;
    this.threatening_ = b.threatening_;
  }

  private final List<Attack> attacks_ = new ArrayList<Attack>();

  private final boolean auto_break_lock_;

  private final float bashstrengthmod_;

  private final boolean can_graffiti_;

  private final boolean can_take_hostages_;

  private final boolean can_threaten_hostages_;

  private final boolean instrument_;

  private final int legality_;

  private final boolean musical_attack_;

  private final boolean protects_against_kidnapping_;

  private final int size_;

  private final boolean suspicious_;

  private final boolean threatening_;

  @Override public void displayStats(final int viewID) {
    maybeAddText(viewID, "Size: " + size_, size_ > 0);
    if (!attacks_.isEmpty()) {
      ui(viewID).text("Weapon attacks:").add();
    }
    for (final Attack a : attacks_) {
      ui(viewID).text(" \u25e6 " + a.toString()).add();
    }
    maybeAddText(viewID, "Automatically Breaks Locks.", auto_break_lock_);
    maybeAddText(viewID, "Bash bonus: " + bashstrengthmod_, bashstrengthmod_ > 0);
    maybeAddText(viewID, "Can graffiti", can_graffiti_);
    maybeAddText(viewID, "Can take hostages.", can_take_hostages_);
    maybeAddText(viewID, "Can threaten hostages.", can_threaten_hostages_);
    maybeAddText(viewID, "Is an instrument" + (musical_attack_ ? " with a musical attack." : "."),
        instrument_);
    maybeAddText(viewID, "Legality: "
        + nameForLegality(legality_)
        + (i.issue(Issue.GUNCONTROL).lawLTE(Alignment.values()[legality_ + 2]) ? " [LEGAL]"
            : " [ILLEGAL]"), true);
    maybeAddText(viewID, "Protects against kidnapping.", protects_against_kidnapping_);
    maybeAddText(viewID, "Suspicious.", suspicious_);
    maybeAddText(viewID, "Threatening.", threatening_);
  }

  // Returns the most liberal gun control law for the weapon to be legal.
  // -2, -1, 0, 1 and 2 for C+, C, M, L and L+ respectively. -3 always
  // illegal.
  public int get_legality() {
    return legality_;
  }

  public boolean is_legal() {
    return i.issue(Issue.GUNCONTROL).lawGTE(Alignment.values()[legality_ + 2]);
  }

  boolean acceptableAmmo(final ClipType clipname) {
    for (final Attack as : attacks_) {
      if (as.ammotype == clipname) {
        return true;
      }
    }
    return false;
  }

  // Returns if the weapon type should always succeed breaking locks.
  boolean auto_breaks_locks() {
    return auto_break_lock_;
  }

  // Checks if the weapon uses ammo in any of its attacks.
  // Returns if the weapon type can be used to make graffiti.
  boolean can_graffiti() {
    return can_graffiti_;
  }

  // Returns if the weapon type can be used to take hostages without causing
  // alarm.
  boolean can_take_hostages() {
    return can_take_hostages_;
  }

  boolean can_threaten_hostages() {
    return can_threaten_hostages_;
  }

  // Gives a reference to the vector of all possible attacks made by the
  // weapon type.
  List<Attack> get_attacks() {
    return attacks_;
  }

  // Checks if the weapon type is legal.
  // Returns the bash bonus provided by the weapon type.
  float get_bashstrengthmod() {
    return bashstrengthmod_;
  }

  // Returns the size of the weapon type. Used for concealment under clothes.
  int get_size() {
    return size_;
  }

  // Returns if the weapon will use a musical attack in combat.
  boolean has_musical_attack() {
    return musical_attack_;
  }

  // Returns whether the weapon type is considered an instrument when fund
  // raising with music.
  boolean is_instrument() {
    return instrument_;
  }

  boolean is_ranged() {
    for (final Attack as : attacks_) {
      if (as.ranged) {
        return true;
      }
    }
    return false;
  }

  //
  boolean is_suspicious() {
    return suspicious_;
  }

  // Checks if the given clip type is used by any of the weapon's attacks.
  // Checks if any of the weapon's attacks are ranged.
  // Returns if the weapon type can be used to threaten landlords.
  boolean is_threatening() {
    return threatening_;
  }

  boolean is_throwable() {
    for (final Attack as : attacks_) {
      if (as.thrown()) {
        return true;
      }
    }
    return false;
  }

  boolean protects_against_kidnapping() {
    return protects_against_kidnapping_;
  }

  boolean usesAmmo() {
    for (final Attack as : attacks_) {
      if (as.usesAmmo()) {
        return true;
      }
    }
    return false;
  }

  /**
     *
     */
  private static final long serialVersionUID = Game.VERSION;

  public static Wound severtypeStringToEnum(final String severtype) {
    if (severtype.equals("NASTY")) {
      return Wound.NASTYOFF;
    }
    if (severtype.equals("CLEAN")) {
      return Wound.CLEANOFF;
    }
    return Wound.NONE;
  }

  public static Skill skillStringToEnum(final String skillname) {
    try {
      return Skill.valueOf(skillname.toUpperCase(Locale.ENGLISH));
    } catch (final IllegalArgumentException e) {
      Log.e(Game.LCS, "No such Skill: \"" + skillname + "\"");
      throw e;
    }
  }

  /** @param legality_2
   * @return */
  private static String nameForLegality(final int legality) {
    switch (legality) {
    case -3:
      return "Illegal under any conditions";
    case -2:
      return "Legal under archconservative gun-control laws";
    case -1:
      return "Legal under conservative gun-control laws";
    case 0:
      return "Legal under moderate gun-control laws";
    case 1:
      return "Legal under moderate gun-control laws";
    case 2:
      return "Legal even under ELITE LIBERAL gun-control laws";
    default:
      return "ERROR";
    }
  }
}
