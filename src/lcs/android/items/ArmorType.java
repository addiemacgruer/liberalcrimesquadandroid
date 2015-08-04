package lcs.android.items;

import static lcs.android.util.Curses.*;
import lcs.android.creature.Creature;
import lcs.android.creature.health.BodyPart;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

/** An ideal instance of the various armors found in-game */
public @NonNullByDefault class ArmorType extends AbstractItemType {
  static class Builder extends AbstractItemTypeBuilder<ArmorType> {
    private enum Children {
      ARMOR,
      BODY_COVERING,
      INTERROGATION
    }

    private int armorBody;

    private int armorHead;

    private int armorLimbs;

    private boolean concealFace;

    private int concealWeaponsize;

    private boolean coverArms;

    private boolean coverBody;

    private boolean coverHead;

    private boolean coverLegs;

    private boolean deathsquadLegality;

    private String description = "";

    private boolean fireprotection;

    private int interrogationAssaultbonus;

    private int interrogationBasepower;

    private int interrogationDrugbonus;

    private int makeDifficulty;

    private int makePrice;

    private boolean mask;

    private boolean police;

    private int professionalism;

    private int stealthValue;

    private boolean surpriseMask;

    @Nullable private Children current = null;

    @Override public ArmorType build() {
      return new ArmorType(this);
    }

    @Override public Configurable xmlChild(final String value) {
      if (value.equals("armor")) {
        current = Children.ARMOR;
      } else if (value.equals("interrogation")) {
        current = Children.INTERROGATION;
      } else if (value.equals("body_covering")) {
        current = Children.BODY_COVERING;
      } else {
        Log.e("LCS", "Unknown xmlChild for ArmorType:" + value);
      }
      return this;
    }

    @Override public void xmlFinishChild() {
      if (current != null) {
        current = null;
        return;
      }
      Builder ltb = this;
      ArmorType lt = ltb.build();
      Game.type.armor.put(lt.idname, lt);
    }

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("make_difficulty")) {
        makeDifficulty = Xml.getInt(value);
      } else if (key.equals("make_price")) {
        makePrice = Xml.getInt(value);
      } else if (key.equals("deathsquad_legality")) {
        deathsquadLegality = Xml.getBoolean(value);
      } else if (current == Children.ARMOR) {
        if (key.equals("body")) {
          armorBody = Xml.getInt(value);
        } else if (key.equals("head")) {
          armorHead = Xml.getInt(value);
        } else if (key.equals("limbs")) {
          armorLimbs = Xml.getInt(value);
        } else if (key.equals("fireprotection")) {
          fireprotection = Xml.getBoolean(value);
        }
      } else if (current == Children.BODY_COVERING) {
        if (key.equals("body")) {
          coverBody = Xml.getBoolean(value);
        } else if (key.equals("head")) {
          coverHead = Xml.getBoolean(value);
        } else if (key.equals("arms")) {
          coverArms = Xml.getBoolean(value);
        } else if (key.equals("legs")) {
          coverLegs = Xml.getBoolean(value);
        } else if (key.equals("conceals_face")) {
          concealFace = Xml.getBoolean(value);
        }
      } else if (current == Children.INTERROGATION) {
        if (key.equals("basepower")) {
          interrogationBasepower = Xml.getInt(value);
        } else if (key.equals("assault_bonus") || key.equals("assaultbonus")) {
          // used
          interrogationAssaultbonus = Xml.getInt(value);
        } else if (key.equals("drugbonus")) {
          interrogationDrugbonus = Xml.getInt(value);
        }
      } else if (key.equals("professionalism")) {
        professionalism = Xml.getInt(value);
      } else if (key.equals("conceal_weapon_size")) {
        concealWeaponsize = Xml.getInt(value);
      } else if (key.equals("stealth_value")) {
        stealthValue = Xml.getInt(value);
      } else if (key.equals("mask")) {
        mask = Xml.getBoolean(value);
      } else if (key.equals("surprise")) {
        surpriseMask = Xml.getBoolean(value);
      } else if (key.equals("description")) {
        description = Xml.getText(value);
      } else if (key.equals("appropriate_weapon")) {
        /* not used */Xml.getText(value);
      } else if (key.equals("police")) {
        police = Xml.getBoolean(value);
      } else {
        super.xmlSet(key, value);
      }
    }
  }

  /** @param builder */
  private ArmorType(Builder builder) {
    super(builder);
    armorBody = builder.armorBody;
    armorHead = builder.armorHead;
    armorLimbs = builder.armorLimbs;
    concealFace = builder.concealFace;
    concealWeaponsize = builder.concealWeaponsize;
    coverArms = builder.coverArms;
    coverBody = builder.coverBody;
    coverHead = builder.coverHead;
    coverLegs = builder.coverLegs;
    deathsquadLegality = builder.deathsquadLegality;
    description = builder.description;
    fireprotection = builder.fireprotection;
    interrogationAssaultbonus = builder.interrogationAssaultbonus;
    interrogationBasepower = builder.interrogationBasepower;
    interrogationDrugbonus = builder.interrogationDrugbonus;
    makeDifficulty = builder.makeDifficulty;
    makePrice = builder.makePrice;
    mask = builder.mask;
    police = builder.police;
    professionalism = builder.professionalism;
    stealthValue = builder.stealthValue;
    surpriseMask = builder.surpriseMask;
  }

  private final int armorBody;

  private final int armorHead;

  private final int armorLimbs;

  private final boolean concealFace;

  private final int concealWeaponsize;

  private final boolean coverArms;

  private final boolean coverBody;

  private final boolean coverHead;

  private final boolean coverLegs;

  /** whether this item would be worn by a deathsquad, id est is it the deathsquad uniform
   * @return most likely false: things need to get bad for a deathsquad to appear. */
  public final boolean deathsquadLegality;

  /** A pithy description (of masks only), used when purchasing in shops */
  public final String description;

  private final boolean fireprotection;

  private final int interrogationAssaultbonus;

  private final int interrogationBasepower;

  private final int interrogationDrugbonus;

  /** How hard it is for anyone to create this outfit. an integer, corresponding to a
   * {@link CheckDifficulty} */
  public final int makeDifficulty;

  /** How expensive it is to make this outfit. dollars, ranging from $5 (toga) to $500 (bunkergear) */
  public final int makePrice;

  /** is it a mask? probably false, masks aren't very useful in game, although they have a cheap drug
   * bonus */
  public final boolean mask;

  private final boolean police;

  private final int professionalism;

  /** The sneakiness of this outfit. an integer between 0 and 3 (ninja suit) */
  public final int stealthValue;

  private final boolean surpriseMask;

  @Override public void displayStats(final int viewID) {
    maybeAddText(viewID, description, description.length() > 0);
    maybeAddText(viewID, "Armor to body: " + armorBody, armorBody > 0);
    maybeAddText(viewID, "Armor to head: " + armorHead, armorHead > 0);
    maybeAddText(viewID, "Armor to limbs: " + armorLimbs, armorLimbs > 0);
    maybeAddText(viewID, "Conceals Face", concealFace);
    maybeAddText(viewID, "Conceals weapon of size: " + concealWeaponsize, concealWeaponsize > 0);
    maybeAddText(viewID, "Covers:" + (coverArms ? " arms" : "") + (coverBody ? " body" : "")
        + (coverHead ? " head" : "") + (coverLegs ? " legs" : ""), coverArms || coverBody
        || coverLegs || coverHead);
    maybeAddText(viewID, "Legal for Death Squads", deathsquadLegality);
    maybeAddText(viewID, "Provides fire protection", fireprotection);
    maybeAddText(viewID, "Interrogation Bonus: " + interrogationBasepower,
        interrogationBasepower > 0);
    maybeAddText(viewID, "Interrogation Assault Bonus: " + interrogationAssaultbonus,
        interrogationAssaultbonus > 0);
    maybeAddText(viewID, "Interrogation Drug Bonus: " + interrogationDrugbonus,
        interrogationDrugbonus > 0);
    maybeAddText(viewID, (surpriseMask ? "Surprise! " : "") + "Mask", surpriseMask || mask);
    maybeAddText(viewID, "Stealth bonus: " + stealthValue, stealthValue > 0);
    maybeAddText(viewID, "Difficulty to make: " + CheckDifficulty.fromInt(makeDifficulty),
        makeDifficulty > 0);
    maybeAddText(viewID, "Value: $" + makePrice, makePrice > 0);
  }

  /** How hard it is for a given creature to create this outfit.
   * @param cr The creature in question
   * @return an integer, corresponding to a {@link CheckDifficulty} */
  public int makeDifficulty(final Creature cr) {
    int basedif = makeDifficulty;
    basedif -= cr.skill().skill(Skill.TAILORING) - 3;
    if (basedif < 0) {
      basedif = 0;
    }
    return basedif;
  }

  /** Whether this armor conceal a weaponof a given size.
   * @param size the size of the weapon */
  boolean concealsWeaponsize(final int size) {
    return concealWeaponsize > size;
  }

  /** Whether this armor covers a given bodypart. Not currently used much.
   * @param bodypart */
  boolean covers(final BodyPart bodypart) {
    if (mask) {
      return bodypart == BodyPart.HEAD;
    }
    switch (bodypart) {
    case HEAD:
      return coverHead;
    case BODY:
      return coverBody;
    case ARM_RIGHT:
    case ARM_LEFT:
      return coverArms;
    case LEG_RIGHT:
    case LEG_LEFT:
      return coverLegs;
    default:
    }
    return false;
  }

  /** How much the armor protects a given bodypart
   * @param bodypart
   * @return an integer from (0 to 10): all but the heaviest of armors provide 0 */
  int defense(final BodyPart bodypart) {
    switch (bodypart) {
    case HEAD:
      return armorHead;
    case BODY:
      return armorBody;
    case ARM_RIGHT:
    case ARM_LEFT:
      return armorLimbs;
    case LEG_RIGHT:
    case LEG_LEFT:
      return armorLimbs;
    default:
    }
    return 0;
  }

  /** Bonus to interrogation, due to intimidation or authority.
   * @return an integer bonus, from 0 (naked) to 8 (death squad armor) */
  int interrogationBasepower() {
    return interrogationBasepower;
  }

  /** bonus to interrogation when our guest is drugged.
   * @return 0 (most things) to 4 (surreal things) */
  int interrogationDrugbonus() {
    if (mask) {
      return 4 + interrogationDrugbonus;
    }
    return interrogationDrugbonus;
  }

  /** whether the armor protects from fire
   * @return true if fireman outfit, false otherwise */
  boolean isFireprotection() {
    return fireprotection;
  }

  /** would the police wear this? */
  boolean isPolice() {
    return police;
  }

  /** The perceived professionalism of this outfit. Ranges from 0 for nakedness, to 4 for an
   * expensive (suit|dress)
   * @return an integer from (0 to 4) */
  int professionalism() {
    return professionalism;
  }

  private static final long serialVersionUID = Game.VERSION;
}
