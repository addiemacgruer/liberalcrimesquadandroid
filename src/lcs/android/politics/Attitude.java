/**
 *
 */
package lcs.android.politics;

import static lcs.android.game.Game.*;

import java.io.Serializable;

import lcs.android.util.Getter;
import lcs.android.util.Setter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** @author addie */
public @NonNullByDefault class Attitude implements Serializable {
  public Attitude(final Issue issue) {
    this.issue = issue;
    law = issue.gameStartAlignment;
  }

  private final Issue issue;

  private int attitude;

  private Alignment law;

  private int backgroundLiberalInfluence;

  private int publicInterest;

  /** @param power */
  public Attitude addBackgroundInfluence(final int power) {
    backgroundLiberalInfluence += power;
    return this;
  }

  /** @param power */
  public Attitude addPublicInterest(final int power) {
    publicInterest += power;
    return this;
  }

  @Getter public int attitude() {
    return attitude;
  }

  @Setter public Attitude attitude(final int i) {
    attitude = i;
    return this;
  }

  /** @return */
  @Getter public int backgroundInfluence() {
    return backgroundLiberalInfluence;
  }

  /** @param i */
  @Setter public void backgroundInfluence(final int i) {
    backgroundLiberalInfluence = i;
  }

  /** common - shifts public opinion on an issue @param power
   * @param i
   * @param j */
  public void changeOpinion(final int power, final int aAffect, final int aCap) { // TODO this.
    // if (true)
    // throw new UnsupportedOperationException();
    int affect = aAffect;
    int cap = aCap;
    /* First note this in the liberal influence -- mostly for the sake of the nice visual
     * intelligence report entry */
    if (issue.core) {
      backgroundLiberalInfluence += power * 10;
    }
    if (issue == Issue.LIBERALCRIMESQUAD) {
      affect = 0;
    }
    if (issue == Issue.LIBERALCRIMESQUADPOS) {
      affect = 0;
      final int mood = Politics.publicmood(null);
      if (cap > mood + 40) {
        cap = mood + 40;
      }
    }
    int effpower = power;
    /* Affect is whether the LCS is publically known to be behind the circumstances creating the
     * public opinion change */
    if (affect == 1) {
      // Aff is the % of people who know/care about the LCS
      final int aff = i.issue(Issue.LIBERALCRIMESQUAD).attitude;
      /* Rawpower is the amount of the action proportional to the people who, not having heard of
       * the LCS, do not allow the LCS' reputation to affect their opinions */
      final int rawpower = (int) (power * (100 - aff) / 100.0);
      /* Affected power is the remainder of the action besides rawpower, the amount of the people
       * who know of the LCS and have it alter their opinion */
      int affectedpower = power - rawpower;
      if (affectedpower > 0) {
        /* Dist is a combination of the relative popularity of the LCS to the issue and the absolute
         * popularity of the LCS. Very popular LCS on a very unpopular issue is very influential.
         * Very unpopular LCS on a very popular issue has the ability to actually have a reverse
         * effect. */
        final int dist = 2 * i.issue(Issue.LIBERALCRIMESQUADPOS).attitude - attitude - 50;
        /* Affected power is then scaled by dist -- if the LCS is equally popular as the issue, it's
         * equally powerful as the rawpower. For every 10% up or down past there, it's 10% more or
         * less powerful. */
        affectedpower = (int) (affectedpower * (100.0 + dist) / 100.0f);
      }
      /* Effpower is then the sum of the rawpower (people who don't know about the LCS) and the
       * affectedpower (people who do know about the LCS and had their judgment swayed by their
       * opinion of it). */
      effpower = rawpower + affectedpower;
    } else if (affect == -1) {
      /* Simplifed algorithm for affect by CCS respect */
      effpower = power * (100 - i.issue(Issue.CONSERVATIVECRIMESQUAD).attitude / 100);
    }
    if (issue == Issue.LIBERALCRIMESQUAD) {
      /* Only half the country will ever hear about the LCS at one time, and people will only
       * grudgingly lose fear of it */
      if (effpower < -5) {
        effpower = -5;
      }
      if (effpower > 50) {
        effpower = 50;
      }
    } else if (issue == Issue.LIBERALCRIMESQUADPOS) {
      /* Only 50% of the country can be swayed at once in their views of the LCS negatively, 5%
       * positively */
      if (effpower < -50) {
        effpower = -50;
      }
      if (effpower > 5) {
        effpower = 5;
      }
    }
    /* Scale the magnitude of the effect based on how much people are paying attention to the issue */
    effpower = (int) (effpower * (1 + (float) publicInterest / 50));
    /* Then affect public interest */
    if (publicInterest < cap || issue == Issue.LIBERALCRIMESQUADPOS && publicInterest < 100) {
      publicInterest += Math.abs(effpower);
    }
    if (effpower > 0) {
      /* Some things will never persuade the last x% of the population. If there's a cap on how many
       * people will be impressed, this is where that's handled. */
      if (attitude + effpower > cap) {
        if (attitude > cap) {
          effpower = 0;
        } else {
          effpower = cap - attitude;
        }
      }
    }
    // Finally, apply the effect.
    attitude += effpower;
    attitude = Math.max(0, Math.min(attitude, 100));
  }

  @Getter public Alignment law() {
    return law;
  }

  @Setter public Attitude law(final Alignment law) {
    this.law = law;
    return this;
  }

  public boolean lawGT(final Alignment test) {
    return law.ord > test.ord;
  }

  public boolean lawGTE(final Alignment test) {
    return law.ord >= test.ord;
  }

  public boolean lawLT(final Alignment test) {
    return law.ord < test.ord;
  }

  public boolean lawLTE(final Alignment test) {
    return law.ord <= test.ord;
  }

  /** @return */
  @Getter public int publicInterest() {
    return publicInterest;
  }

  /** @param i */
  public Attitude publicInterest(final int i) {
    publicInterest = i;
    return this;
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;
}
