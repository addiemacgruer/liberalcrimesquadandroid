package lcs.android.items;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Map.Entry;

import lcs.android.creature.Creature;
import lcs.android.creature.health.BodyPart;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.site.map.TileSpecial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Specific instances of the various armors found in-game */
public @NonNullByDefault class Armor extends AbstractItem<ArmorType> {
  /** Armor has a quality rating, which affects how well it defends from damage, and whether you can
   * get past bouncers in it. */
  public enum Rating {
    FIRST(1),
    FOURTH(4),
    SECOND(2),
    THIRD(3);
    Rating(final int num) {
      this.num = num;
    }

    /** number value of the rating (1 to 4) */
    public final int num;

    @Override public String toString() {
      switch (this) {
      case FIRST:
      default:
        return "1st";
      case SECOND:
        return "2nd";
      case THIRD:
        return "3rd";
      case FOURTH:
        return "4th";
      }
    }
  }

  /** Creates a new armor of a given type: quality weighted (1x1, 2x3, 3x1, 4x1)
   * @param at */
  public Armor(final ArmorType at) {
    super(at);
    quality = i.rng.choice(Rating.FIRST, Rating.SECOND, Rating.SECOND, Rating.SECOND, Rating.THIRD,
        Rating.THIRD, Rating.FOURTH);
  }

  public Armor(final ArmorType at, final Rating quality) {
    super(at);
    this.quality = quality;
  }

  public Armor(final String string) {
    super(stringToArmorType(string));
    quality = Rating.FIRST;
  }

  public Armor(final String newArmorType, final Rating quality) {
    super(stringToArmorType(newArmorType));
    this.quality = quality;
  }

  private boolean bloody, damaged;

  private Rating quality;

  /** causes a blood explosion. Covers the armor (and your surroundings, squadmates, and the people
   * surrounding, if on site) with gore. Unless they're naked, then it misses. */
  public void bloodblast() {
    setBloody(true);
    if (i.mode() != GameMode.SITE)
      return;
    i.site.currentTile().flag.add(TileSpecial.BLOODY2);
    // HIT EVERYTHING
    for (final Creature p : i.activeSquad) {
      if (!p.isNaked()) {
        if (i.rng.chance(2)) {
          p.getArmor().setBloody(true);
        }
      }
    }
    for (final Creature e : i.currentEncounter().creatures()) {
      if (!e.isNaked()) {
        if (i.rng.chance(2)) {
          e.getArmor().setBloody(true);
        }
      }
    }
  }

  /** Whether this armor conceal a weapon. Depends only on size.
   * @param weapon */
  public boolean concealsWeapon(final Weapon weapon) {
    return platonicIdeal.concealsWeaponsize(weapon.size());
  }

  /** Whether this armor covers a given bodypart. Not currently used much.
   * @param bodypart */
  public boolean covers(final BodyPart bodypart) {
    return platonicIdeal.covers(bodypart);
  }

  /** Damages the part of the armor (if it covers a specific bodypart)
   * @param bp */
  public void damagePart(final BodyPart bp) {
    if (covers(bp)) {
      damaged = true;
    }
  }

  /** How much the armor protects a given bodypart
   * @param bodypart
   * @return an integer from (0 to 10): all but the heaviest of armors provide 0 */
  public int defense(final BodyPart bodypart) {
    return platonicIdeal.defense(bodypart);
  }

  @Override public void displayStats(final int viewID) {
    ui(viewID)
        .text(
            "Quality: " + quality + " rate" + (bloody ? ", bloody" : "")
                + (damaged ? ", damaged" : "")).add();
    platonicIdeal.displayStats(viewID);
  }

  @Override public String equipTitle() {
    String et = platonicIdeal.toString();
    if (bloody || damaged || quality != Rating.FIRST) {
      et += "[";
      if (quality != Rating.FIRST) {
        et += quality;
      }
      if (bloody) {
        et += "B";
      }
      if (damaged) {
        et += "D";
      }
      et += "]";
    }
    return et;
  }

  /** Bonus to interrogation, due to intimidation or authority.
   * @return an integer bonus, from 0 (naked) to 8 (death squad armor) */
  public int interrogationBasepower() {
    return platonicIdeal.interrogationBasepower();
  }

  /** bonus to interrogation when our guest is drugged.
   * @return 0 (most things) to 4 (surreal things) */
  public int interrogationDrugbonus() {
    return platonicIdeal.interrogationDrugbonus();
  }

  /** you've got red on you */
  public boolean isBloody() {
    return bloody;
  }

  /** armor in good condtion */
  public boolean isDamaged() {
    return damaged;
  }

  /** whether the armor protects from fire
   * @return true if fireman outfit, false otherwise */
  public boolean isFireprotection() {
    return platonicIdeal.isFireprotection();
  }

  /** whether the armor is unbloody and undamaged */
  @Override public boolean isGoodForSale() {
    return !bloody && !damaged;
  }

  /** is it a mask?
   * @return probably false, masks aren't very useful in game, although they have a cheap drug bonus */
  public boolean isMask() {
    return platonicIdeal.mask;
  }

  /** would the police wear this? */
  public boolean isPolice() {
    return platonicIdeal.isPolice();
  }

  /** How hard it is for a given creature to create this outfit.
   * @param cr The creature in question
   * @return an integer, corresponding to a {@link CheckDifficulty} */
  public int makeDifficulty(final Creature cr) {
    return platonicIdeal.makeDifficulty(cr);
  }

  @Override public boolean merge(final AbstractItem<? extends AbstractItemType> ai) {
    if (ai instanceof Armor && platonicIdeal == ai.platonicIdeal) {
      final Armor a = (Armor) ai; // cast -XML
      if (bloody == a.bloody && damaged == a.damaged && quality == a.quality) {
        number += a.number;
        a.number = 0;
        return true;
      }
    }
    return false;
  }

  /** The perceived professionalism of this outfit. Ranges from 0 for nakedness, to 4 for an
   * expensive (suit|dress)
   * @return an integer from (0 to 4) */
  public int professionalism() {
    return platonicIdeal.professionalism();
  }

  /** the quality rating of this outfit
   * @return {@link Rating#FIRST} if bought in a shop, a random choice (but weighted to
   *         {@link Rating#SECOND} or {@link Rating#THIRD} if found; depends on the tailor skill if
   *         made */
  public Rating quality() {
    return quality;
  }

  /** Set the quality rating of this outfit.
   * @param aQuality
   * @return */
  public Armor quality(final Armor.Rating aQuality) {
    quality = aQuality;
    return this;
  }

  /** Sets whether the armor is bloody
   * @param b */
  public void setBloody(final boolean b) {
    bloody = b;
  }

  /** Sets whether the armor is damaged.
   * @param d */
  public void setDamaged(final boolean d) {
    damaged = d;
  }

  @Override public Armor split(final int aNumber) {
    final int n = Math.min(aNumber, number);
    final Armor newi = new Armor(platonicIdeal, quality);
    newi.number = n;
    number -= n;
    return newi;
  }

  /** The sneakiness of this outfit.
   * @return an integer between 0 and 3 (ninja suit) */
  public int stealthValue() {
    return platonicIdeal.stealthValue;
  }

  @Override public String toString() {
    return equipTitle();
  }

  @Nullable private static Armor none = null;

  private static final long serialVersionUID = Game.VERSION;

  /** nudity is shared throughout the game, so we can check more easily whether armor==none. It's
   * always high quality.
   * @return nakedness, which is shared by all mankind (and animals) */
  public static Armor none() { // TODO should be lazy init.
    if (none == null) {
      none = new Armor("ARMOR_NONE", Rating.FIRST);
    }
    assert none != null;
    return none;
  }

  private static ArmorType stringToArmorType(final String string) {
    ArmorType r = null;
    for (final Entry<String, ArmorType> at : Game.type.armor.entrySet()) {
      if (at.getKey().equals(string)) {
        r = at.getValue();
        break;
      }
    }
    if (r == null)
      throw new RuntimeException("Couldn't create: " + string);
    return r;
  }
}
