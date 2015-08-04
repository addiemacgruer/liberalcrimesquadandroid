package lcs.android.creature.crime;

import static lcs.android.game.Game.*;

import java.io.Serializable;
import java.util.Map;

import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.creature.Creature;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Getter;
import lcs.android.util.Setter;
import lcs.android.util.SparseMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class CreatureCrime implements Serializable {
  public CreatureCrime(final Creature creature) {
    c = creature;
  }

  private final Creature c;

  private int testimonies = 0;

  private final Map<Crime, Integer> crimesSuspected = SparseMap.of(Crime.class);

  private boolean deathPenalty = false;

  private int heat = 0;

  private int sentence = 0;

  /** Someone else has testified against this Creature.
   * @return this. */
  public CreatureCrime addTestimony() {
    testimonies++;
    return this;
  }

  /** Clear the criminal record of this creature.
   * @return this. */
  public CreatureCrime clearCriminalRecord() {
    heat = 0;
    testimonies = 0;
    crimesSuspected.clear();
    return this;
  }

  /** Clear the testimony record against this creature. */
  public CreatureCrime clearTestimonies() {
    // TODO Auto-generated method stub
    testimonies = 0;
    return this;
  }

  public int crimesSuspected(final Crime lf) {
    // Cleanse record on things that aren't illegal right now
    if (lf == Crime.BURNFLAG && i.issue(Issue.FLAGBURNING).lawGT(Alignment.MODERATE))
      return 0;
    if (lf == Crime.BROWNIES && i.issue(Issue.DRUGS).lawGT(Alignment.MODERATE))
      return 0;
    if (lf == Crime.HIREILLEGAL && i.issue(Issue.IMMIGRATION).law() == Alignment.ELITELIBERAL)
      return 0;
    if (lf == Crime.SPEECH && i.issue(Issue.FREESPEECH).lawGT(Alignment.ARCHCONSERVATIVE))
      return 0;
    return crimesSuspected.get(lf).intValue();
  }

  public CreatureCrime criminalize(final Crime crime) {
    if (i.mode() == GameMode.SITE) {
      if (i.site.current().lcs().siege.siege) {
        // Do not criminalize the LCS for self-defense against
        // extrajudicial raids
        if (i.site.current().lcs().siege.siegetype != CrimeSquad.POLICE)
          return this;
      } else if (i.site.current().renting() == CrimeSquad.CCS)
        // Do not criminalize the LCS for crimes against the CCS
        return this;
    }
    crimesSuspected.put(crime, crimesSuspected.get(crime) + 1);
    heat(heat() + crime.heat());
    return this;
  }

  @Getter public boolean deathPenalty() {
    return deathPenalty;
  }

  @Setter public CreatureCrime deathPenalty(final boolean aDeathPenalty) {
    deathPenalty = aDeathPenalty;
    return this;
  }

  @Getter public int heat() {
    return heat;
  }

  @Getter public CreatureCrime heat(final int aHeat) {
    heat = aHeat;
    return this;
  }

  @Setter public void incrementCrime(final Crime lf) {
    incrementCrime(lf, 1);
  }

  @Setter public void incrementCrime(final Crime lf, final int inc) {
    crimesSuspected.put(lf, crimesSuspected.get(lf) + inc);
  }

  public boolean isCriminal() {
    for (final Crime lf : Crime.values()) {
      if (crimesSuspected.get(lf).intValue() != 0)
        return true;
    }
    return false;
  }

  public int prosecutableCrimes(final Crime lf) {
    // courts are lazy
    return Math.min(crimesSuspected(lf), 10);
  }

  @Getter public int sentence() {
    return sentence;
  }

  @Setter public CreatureCrime sentence(final int aSentence) {
    sentence = aSentence;
    return this;
  }

  /** How many people have testified against this Creature, usually for racketeering.
   * @return a number */
  @Getter public int testimonies() {
    return testimonies;
  }

  @Override public String toString() {
    return "Crimes:" + c;
  }

  private static final long serialVersionUID = Game.VERSION;
}
