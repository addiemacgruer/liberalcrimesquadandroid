package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.activities.BareActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.creature.Creature;
import lcs.android.creature.health.SpecialWounds;
import lcs.android.creature.skill.Skill;
import lcs.android.encounters.FootChase;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Trouble extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> trouble = this;
    if (trouble.size() > 0) {
      int juiceval = 0;
      boolean done = false;
      Crime crime = null;
      final StringBuilder str = new StringBuilder();
      if (trouble.size() > 1) {
        str.append("Your Activists have ");
      } else {
        str.append(trouble.get(0).toString());
        str.append(" has ");
      }
      int power = 0;
      for (final Creature t : trouble) {
        power += t.skill().skillRoll(Skill.PERSUASION) + t.skill().skillRoll(Skill.STREETSENSE);
      }
      int mod = 1;
      if (i.rng.nextInt(10) < power) {
        mod++;
      }
      if (i.rng.nextInt(20) < power) {
        mod++;
      }
      if (i.rng.nextInt(40) < power) {
        mod++;
      }
      if (i.rng.nextInt(60) < power) {
        mod++;
      }
      if (i.rng.nextInt(80) < power) {
        mod++;
      }
      if (i.rng.nextInt(100) < power) {
        mod++;
      }
      do {
        switch (i.rng.nextInt(7)) {
        default:
          str.append("run around uptown splashing paint on fur coats!");
          juiceval = 2;
          crime = Crime.ASSAULT;
          final int power1 = mod;
          i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power1, 1, 100);
          i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(mod / 2, 0, 70);
          i.issue(Issue.ANIMALRESEARCH).addPublicInterest(mod).addBackgroundInfluence(mod);
          done = true;
          break;
        case 1: {
          if (i.issue(Issue.GAY).lawLT(Alignment.ELITELIBERAL)) {
            str.append("disrupted a traditional wedding at a church!");
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.GAY).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 2;
            crime = Crime.DISTURBANCE;
            done = true;
          }
          break;
        }
        case 2: {
          if (i.issue(Issue.ABORTION).lawLT(Alignment.ELITELIBERAL)) {
            str.append("posted horrifying dead abortion doctor pictures downtown!");
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.ABORTION).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 1;
            done = true;
          }
          break;
        }
        case 3: {
          if (i.issue(Issue.POLICEBEHAVIOR).lawLT(Alignment.ELITELIBERAL)) {
            str.append("gone downtown and reenacted a police beating!");
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.POLICEBEHAVIOR).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 2;
            crime = Crime.DISTURBANCE;
            done = true;
          }
          break;
        }
        case 4: {
          if (i.issue(Issue.NUCLEARPOWER).lawLT(Alignment.ELITELIBERAL)) {
            if (trouble.size() > 1) {
              str.append("dressed up and pretended to be radioactive mutants!");
            } else {
              str.append("dressed up and pretended to be a radioactive mutant!");
            }
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.NUCLEARPOWER).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 2;
            crime = Crime.DISTURBANCE;
            done = true;
          }
          break;
        }
        case 5: {
          if (i.issue(Issue.POLLUTION).lawLT(Alignment.ELITELIBERAL)) {
            str.append("squirted business people with fake polluted water!");
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.POLLUTION).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 2;
            crime = Crime.DISTURBANCE;
            done = true;
          }
          break;
        }
        case 6: {
          if (i.issue(Issue.DEATHPENALTY).lawLT(Alignment.ELITELIBERAL)) {
            str.append("distributed fliers graphically illustrating executions!");
            final int power2 = mod;
            i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
            final int power3 = mod;
            i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
            i.issue(Issue.DEATHPENALTY).addPublicInterest(mod).addBackgroundInfluence(mod);
            juiceval = 1;
            done = true;
          }
          break;
        }
        case 7: {
          str.append("distributed fliers graphically illustrating CIA torture!");
          final int power2 = mod;
          i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(power2, 1, 100);
          final int power3 = mod;
          i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power3, 0, 70);
          i.issue(Issue.TORTURE).addPublicInterest(mod).addBackgroundInfluence(mod);
          juiceval = 1;
          done = true;
          break;
        }
        }
      } while (!done);
      if (crime != null) {
        for (final Creature t : trouble) {
          Color color = Color.WHITE;
          if (i.rng.chance(30) && !t.skill().skillCheck(Skill.STREETSENSE, CheckDifficulty.AVERAGE)) {
            if (i.rng.chance(4)) {
              FootChase.attemptArrest(t, "causing trouble");
            } else if (!t.weapon().isArmed() && t.skill().skill(Skill.HANDTOHAND) < 4) {
              str.append(t.toString());
              str.append(" is cornered by a mob of angry rednecks.");
              boolean wonfight = false;
              if (t.weapon().isArmed() && t.weapon().weapon().isThreatening()) {
                str.append(t.toString());
                str.append(" brandishes the ");
                str.append(t.weapon().weapon().toString());
                str.append('!');
                str.append("The mob scatters!");
                t.addJuice(5, 50);
                wonfight = true;
              } else {
                int count = 0;
                while (count <= i.rng.nextInt(5) + 2) {
                  if (t.skill().skillRoll(Skill.HANDTOHAND) > i.rng.nextInt(6) + count) {
                    color = Color.CYAN;
                    str.append(t.toString()
                        + i.rng.choice(" breaks the arm of the nearest person!",
                            " knees a guy in the balls!",
                            " knocks one out with a fist to the face!",
                            " bites some hick's ear off!", " smashes one of them in the jaw!",
                            " shakes off a grab from behind!", " yells the slogan!",
                            " knocks two of their heads together!"));
                    wonfight = true;
                  } else {
                    color = Color.YELLOW;
                    str.append(t.toString()
                        + i.rng.choice(" is held down and kicked by three guys!",
                            " gets pummelled!", " gets hit by a sharp rock!",
                            " is thrown against the sidewalk!",
                            " is bashed in the face with a shovel!", " is forced into a headlock!",
                            " crumples under a flurry of blows!",
                            " is hit in the chest with a pipe!"));
                    count++; // fight goes faster when
                    // you're losing
                    wonfight = false;
                  }
                  count++;
                }
                if (wonfight) {
                  color = Color.GREEN;
                  str.append(t.toString());
                  str.append(" beat the ");
                  if (!i.freeSpeech()) {
                    str.append("[tar]");
                  } else {
                    str.append("shit");
                  }
                  str.append(" out of everyone who got close!");
                  t.addJuice(30, 300);
                  if (t.health().blood() > 70) {
                    t.health().blood(70);
                  }
                }
              }
              if (!wonfight) {
                color = Color.RED;
                str.append(t.toString());
                str.append(" is severely beaten before the mob is broken up.");
                t.activity(new BareActivity(Activity.CLINIC));
                getch();
                t.addJuice(-10, -50);
                if (t.health().blood() > 10) {
                  t.health().blood(10);
                }
                if (i.rng.chance(5)) {
                  switch (i.rng.nextInt(10)) {
                  case 0:
                    if (t.health().getWound(SpecialWounds.LOWERSPINE) == 1) {
                      str.append(t.toString());
                      str.append("'s lower spine has been broken!");
                      t.health().wound(SpecialWounds.LOWERSPINE, 0);
                      getch();
                    }
                    break;
                  case 1:
                    if (t.health().getWound(SpecialWounds.UPPERSPINE) == 1) {
                      str.append(t.toString());
                      str.append("'s upper spine has been broken!");
                      t.health().wound(SpecialWounds.UPPERSPINE, 0);
                      getch();
                    }
                    break;
                  case 2:
                    if (t.health().getWound(SpecialWounds.NECK) == 1) {
                      str.append(t.toString());
                      str.append("'s neck has been broken!");
                      t.health().wound(SpecialWounds.NECK, 0);
                      getch();
                    }
                    break;
                  case 3:
                    if (t.health().getWound(SpecialWounds.TEETH) > 0) {
                      str.append(t.toString());
                      if (t.health().getWound(SpecialWounds.TEETH) > 1) {
                        str.append("'s teeth have been smashed out on the curb.");
                      } else {
                        str.append("'s tooth has been pulled out with pliers!");
                      }
                      t.health().wound(SpecialWounds.TEETH, 0);
                      getch();
                    }
                    break;
                  default: {
                    if (t.health().getWound(SpecialWounds.RIBS) > 0) {
                      int ribminus = i.rng.nextInt(SpecialWounds.RIBS.defaultValue()) + 1;
                      if (ribminus > t.health().getWound(SpecialWounds.RIBS)) {
                        ribminus = t.health().getWound(SpecialWounds.RIBS);
                      }
                      if (ribminus > 1) {
                        str.append(ribminus);
                        str.append(" of ");
                        str.append(t.toString());
                        str.append("'s ribs are ");
                      } else if (t.health().getWound(SpecialWounds.RIBS) > 1) {
                        str.append("One of ");
                        str.append(t.toString());
                        str.append("'s rib is ");
                      } else {
                        str.append(t.toString());
                        str.append("'s last unbroken rib is ");
                      }
                      str.append("broken!");
                      t.health().wound(SpecialWounds.RIBS,
                          t.health().getWound(SpecialWounds.RIBS) - ribminus);
                    }
                    break;
                  }
                  }
                }
              }
            }
          }
          ui().text(str.toString()).color(color).add();
          str.setLength(0);
        }
      }
      for (final Creature h : trouble) {
        h.addJuice(juiceval, 40);
      }
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
