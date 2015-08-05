package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lcs.android.R;
import lcs.android.activities.BareActivity;
import lcs.android.activities.CreatureActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Location;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureFlag;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.Armor;
import lcs.android.politics.Issue;
import lcs.android.util.Color;
import lcs.android.util.Curses;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Pair;

/** If we managed to get a hostage home, convert them to the cause */
public @NonNullByDefault class Interrogation implements Serializable {
  private enum Techniques {
    TALK, // 1
    RESTRAIN, // 2
    BEAT, // 3
    PROPS, // 4
    DRUGS, // 5
    KILL // 6
  }

  /** Creates a new Interrogation. modifies the i.interrogations list, names the kidnappee, sets them
   * as missing, and adds them to the pool.
   * @param victim
   * @param kidnapper */
  private Interrogation(final Creature victim, final Creature kidnapper) {
    cr = victim;
    cr.name(query("The Education of " + cr.properName() + "\n\n"
        + "What name will you use for this " + cr.type().jobtitle(cr) + " in its presence?",
        cr.properName()));
    cr.location(kidnapper.base());
    cr.base(kidnapper.base());
    cr.addFlag(CreatureFlag.MISSING);
    // Kidnapped wearing normal clothes and no weapon
    if (kidnapper.base()!= null) {
      cr.weapon().dropWeaponsAndClips(kidnapper.base().lcs().loot);
      cr.strip(kidnapper.base().lcs().loot);
    }
    cr.giveArmor(new Armor("ARMOR_CLOTHES"), null);
    if (i.pool.contains(cr)) {
      throw new RuntimeException("HaulKidnap.kidnaptransfer: Adding a pool member as hostage.");
    }
    cr.giveFundsToLCS();
    i.pool.add(cr);
    i.score.kidnappings++;
  }

  private final Creature cr;

  private int drugUse;

  private final List<Creature> goodp = new ArrayList<Creature>();

  private final Map<Creature, Integer> rapport = new HashMap<Creature, Integer>();

  private final Set<Techniques> techniques = EnumSet.of(Techniques.TALK, Techniques.RESTRAIN);

  private boolean turned = false;

  /** does all of the daily tasks related to hostage tending. Checks whether the hostage is
   * unsupervised, and maybe makes an escape attempt. If supervised, determines who's got the best
   * psychology skills and makes them lead interrogator. Prompts the user for which techniques of
   * persuasion to use today. Works the hostage over, and sees whether they've been succesfully
   * converted to the cause.
   * <p>
   * nb! removes itself from the i.interrogations if the interrogation is concluded: you need to
   * iterate over a copy of the interrogations to use this method. */
  void tendhostage() {
    if (!cr.health().alive()) {
      return;
    }
    final List<Creature> temppool = generateTempPool();
    if (isEscapeSuccessful(temppool)) {
      return;
    }
    if (temppool.size() == 0) {
      return;
    }
    final Pair<Creature, Integer> leaderp = determineInterrogationLeader(temppool);
    final Creature leader = leaderp.first;
    int attack = leaderp.second;
    updateTechniques(leader);
    if (techniques.contains(Techniques.PROPS) && i.ledger.funds() >= 250) {
      i.ledger.subtractFunds(250, Ledger.ExpenseType.HOSTAGE);
    } else {
      techniques.remove(Techniques.PROPS);
    }
    if (techniques.contains(Techniques.DRUGS) && i.ledger.funds() >= 50) {
      i.ledger.subtractFunds(50, Ledger.ExpenseType.HOSTAGE);
    } else {
      techniques.remove(Techniques.DRUGS);
    }
    if (techniques.contains(Techniques.KILL)) {
      if (executeHostage(temppool)) {
        return;
      }
    }
    if (techniques.contains(Techniques.RESTRAIN)) {
      ui().text(
          "The Automaton is tied hands and feet to a metal chair in the middle of a back room.")
          .add();
      attack += 5;
    } else {
      ui().text("The Automaton is locked in a back room converted into a makeshift cell.").add();
    }
    if (techniques.contains(Techniques.DRUGS)) {
      attack += drugHostage(leader);
    }
    if (techniques.contains(Techniques.BEAT) && cr.health().alive()) {
      doBeating(leader, temppool);
    }
    // Verbal Interrogation
    if (techniques.contains(Techniques.TALK) && cr.health().alive()) {
      attack = doTalking(leader, attack);
    }
    // Lead interrogator gets bonus experience
    if (!techniques.contains(Techniques.KILL)) {
      leader.skill().train(Skill.PSYCHOLOGY, attack / 2 + 1);
    }
    // Others also get experience
    for (final Creature p : temppool) {
      p.skill().train(Skill.PSYCHOLOGY, attack / 4 + 1);
    }
    suicideWatch();
    // Death
    if (!cr.health().alive()) {
      killedHostage(leader, temppool);
    } else if (turned) {
      turnAutomaton(leader, temppool);
    }
    ui(R.id.gcontrol).button(' ').text("Reflect on this.").add();
    getch();
  }

  /** Failure to persuade entrenched capitalists */
  private void conversionFailedBusiness(final Creature leader) {
    switch (i.rng.nextInt(4)) {
    default:
      ui().text(
          cr.toString() + " will never be moved by " + leader.toString()
              + "'s pathetic economic ideals.").add();
      break;
    case 1:
      ui().text(cr.toString() + " is pretty sure LCS actions hurt businesses.").add();
      break;
    case 2:
      ui().text(cr.toString() + " explains to " + leader.toString() + " why communism failed.")
          .add();
      break;
    case 3:
      ui().text(cr.toString() + " mumbles incoherently about Reaganomics.").add();
      break;
    }
    leader.skill().train(Skill.BUSINESS, cr.skill().skill(Skill.BUSINESS) * 4);
  }

  private void conversionFailedPsychology(final Creature leader) {
    switch (i.rng.nextInt(4)) {
    default:
      ui().text(cr.toString() + " plays mind games with " + leader.toString() + ".").add();
      break;
    case 1:
      ui().text(cr.toString() + " knows how this works, and won't budge.").add();
      break;
    case 2:
      ui().text(cr.toString() + " asks if Liberal mothers would approve of this.").add();
      break;
    case 3:
      ui().text(cr.toString() + " seems resistant to this form of interrogation.").add();
      break;
    }
  }

  private void conversionFailedReligion(final Creature leader) {
    switch (i.rng.nextInt(4)) {
    default:
      ui().text(
          leader.toString() + " is unable to shake " + cr.toString() + "'s religious conviction.")
          .add();
      break;
    case 1:
      ui().text(cr.toString() + " will never be broken so long as God grants it strength.").add();
      break;
    case 2:
      ui().text(
          leader.toString() + "'s efforts to question " + cr.toString() + "'s faith seem futile.")
          .add();
      break;
    case 3:
      ui().text(cr.toString() + " calmly explains the Conservative tenets of its faith.").add();
      break;
    }
    leader.skill().train(Skill.RELIGION, cr.skill().skill(Skill.RELIGION) * 4);
  }

  private void coversionFailedScience(final Creature leader) {
    switch (i.rng.nextInt(4)) {
    default:
      ui().text(
          cr.toString() + " wonders what mental disease has possessed " + leader.toString() + ".")
          .add();
      break;
    case 1:
      ui().text(cr.toString() + " explains why nuclear energy is safe.").add();
      break;
    case 2:
      ui().text(cr.toString() + " makes Albert Einstein faces at " + leader.toString() + ".").add();
      break;
    case 3:
      ui().text(cr.toString() + " pities " + leader.toString() + "'s blind ignorance of science.")
          .add();
      break;
    }
    leader.skill().train(Skill.SCIENCE, cr.skill().skill(Skill.SCIENCE) * 4);
  }

  private Pair<Creature, Integer> determineInterrogationLeader(final List<Creature> temppool) {
    /* each day, the attack roll is initialized to the number of days of the stay with the LCS --
     * they will eventually break, but also eventually become too traumatized to continue */
    final Map<Creature, Integer> attack = new HashMap<Creature, Integer>();
    int cattack = 0;
    int business = 0;
    int religion = 0;
    int science = 0;
    for (final Creature p : temppool) {
      attack.put(p, 0);
      if (!rapport.containsKey(p)) {
        rapport.put(p, 0);
      }
      business = Math.max(business, p.skill().skill(Skill.BUSINESS));
      religion = Math.max(religion, p.skill().skill(Skill.RELIGION));
      science = Math.max(science, p.skill().skill(Skill.SCIENCE));
      attack.put(p, baseAttack(p));
      if (attack.get(p) < 0) {
        attack.put(p, 0);
      }
      if (attack.get(p) > cattack) {
        cattack = attack.get(p);
        goodp.clear();
        goodp.add(p);
      } else if (attack.get(p) == cattack) {
        goodp.add(p);
      }
    }
    final Creature cleader = i.rng.randFromList(goodp);
    cattack += temppool.size();
    cattack += cr.joindays();
    cattack += business - cr.skill().skill(Skill.BUSINESS);
    cattack += religion - cr.skill().skill(Skill.RELIGION);
    cattack += science - cr.skill().skill(Skill.SCIENCE);
    cattack += cleader.skill().skillRoll(Skill.PSYCHOLOGY) - cr.skill().skillRoll(Skill.PSYCHOLOGY);
    cattack += cr.skill().attributeRoll(Attribute.HEART);
    cattack -= cr.skill().attributeRoll(Attribute.WISDOM) * 2;
    final Pair<Creature, Integer> leaderp = Pair.create(cleader, cattack);
    return leaderp;
  }

  private void doBeating(final Creature leader, final List<Creature> temppool) {
    int forceroll = 0;
    boolean tortured = false;
    for (final Creature p : temppool) {
      // add interrogator's strength to Techniques.beating strength
      forceroll += p.skill().attributeRoll(Attribute.STRENGTH);
      // reduce rapport with each interrogator
      rapport.put(p, rapport.get(p) - 4);
    }
    // Torture captive if lead interrogator has low heart
    // and you funded using extra supplies
    //
    // Yeah, you kinda don't want this to happen
    if (!leader.skill().attributeCheck(Attribute.HEART, CheckDifficulty.EASY.value())
        && techniques.contains(Techniques.PROPS)) {
      tortured = true;
      // Torture more devastating than normal Techniques.beating
      forceroll *= 2;
      // Extremely bad for rapport with lead interrogator
      rapport.put(leader, rapport.get(leader) - 4);
      ui().text(
          leader.toString()
              + i.rng.choice(" reenacts scenes from Abu Ghraib",
                  " whips the Automaton with a steel cable",
                  " holds the hostage's head under water",
                  " pushes needles under the Automaton's fingernails",
                  " Techniques.beats the hostage with a metal bat",
                  " Techniques.beats the hostage with a belt")
              + ", screaming \""
              + i.rng.choice("I hate you", "Does it hurt?", "Nobody loves you", "God hates you",
                  "Don't fuck with me",
                  /* NOTE: Don't censor profanity in interrogations, even if C+ free speech */
                  "This is Liberalism", "Convert, bitch", "I'm going to kill you",
                  "Do you love me?", "I am your God") + "!\" in its face.").add();
      if (cr.skill().getAttribute(Attribute.HEART, true) > 1) {
        cr.skill().attribute(Attribute.HEART, -1);
      }
      if (cr.skill().getAttribute(Attribute.WISDOM, true) > 1) {
        cr.skill().attribute(Attribute.WISDOM, -1);
      }
    } else {
      final StringBuilder str = new StringBuilder();
      if (temppool.size() == 1) {
        str.append(temppool.get(0).toString() + " Techniques.beats the Automaton");
      } else if (temppool.size() == 2) {
        str.append(temppool.get(0).toString() + " and " + temppool.get(1).toString()
            + " Techniques.beat the Automaton");
      } else {
        str.append(cr.toString() + "'s guards Techniques.beat the Automaton");
      }
      if (techniques.contains(Techniques.PROPS)) {
        str.append(i.rng
            .choice(" with a flagpole", " with a flag", " with a dildo", " with a book"));
      }
      str.append(',');
      str.append(i.rng.choice("scream", "yell", "shout", "holler"));
      str.append("ing \"");
      int j = 0;
      while (j < 3) {
        str.append(i.rng.randFromArray(conservativeEvil));
        if (++j < 3) {
          str.append("! ");
        }
      }
      str.append("!\" in its face.");
      ui().text(str.toString()).add();
    }
    if (techniques.contains(Techniques.PROPS)) {
      forceroll *= 2;
    }
    cr.health().blood(
        cr.health().blood() - (5 + i.rng.nextInt(5))
            * (1 + (techniques.contains(Techniques.PROPS) ? 1 : 0)));
    if (!cr.skill().attributeCheck(Attribute.HEALTH, forceroll)) {
      final StringBuilder str = new StringBuilder();
      if (cr.skill().skillCheck(Skill.RELIGION, forceroll)) {
        str.append(cr.toString());
        if (!techniques.contains(Techniques.DRUGS)) {
          str.append(i.rng.choice(" prays...", " cries out for God."));
        } else {
          str.append(i.rng.choice(" takes solace in the personal appearance of God.",
              " appears to be having a religious experience."));
        }
        ui().text(str.toString()).add();
      } else if (forceroll > cr.skill().getAttribute(Attribute.WISDOM, true) * 3
          + cr.skill().getAttribute(Attribute.HEART, true) * 3
          + cr.skill().getAttribute(Attribute.HEALTH, true) * 3) {
        str.append(cr.toString());
        switch (i.rng.nextInt(4)) {
        default:
          str.append(" screams helplessly for ");
          if (techniques.contains(Techniques.DRUGS)) {
            str.append("John Lennon's mercy.");
          } else if (cr.skill().skill(Skill.RELIGION) > 0) {
            str.append("God's mercy.");
          } else {
            str.append("mommy.");
          }
          break;
        case 1:
          if (techniques.contains(Techniques.RESTRAIN)) {
            str.append(" goes limp in the restraints.");
          } else {
            str.append(" curls up in the corner and doesn't move.");
          }
          break;
        case 2:
          if (techniques.contains(Techniques.DRUGS) && i.rng.likely(5)) {
            str.append(" barks helplessly.");
          } else {
            str.append(" cries helplessly.");
          }
          break;
        case 3:
          if (techniques.contains(Techniques.DRUGS) && i.rng.likely(3)) {
            str.append(" wonders about apples.");
          } else {
            str.append(" wonders about death.");
          }
          break;
        }
        if (cr.skill().getAttribute(Attribute.HEART, false) > 1) {
          cr.skill().attribute(Attribute.HEART, -1);
        }
        if (i.rng.chance(2) && cr.juice() > 0) {
          cr.juice(cr.juice() - forceroll);
          if (cr.juice() < 0) {
            cr.juice(0);
          }
        } else if (cr.skill().getAttribute(Attribute.WISDOM, false) > 1) {
          cr.skill().attribute(Attribute.WISDOM,
              cr.skill().getAttribute(Attribute.WISDOM, false) - forceroll / 10);
          if (cr.skill().getAttribute(Attribute.WISDOM, false) < 1) {
            cr.skill().attribute(Attribute.WISDOM, 1);
          }
        }
        if (!cr.workLocation().isInterrogated() && i.rng.chance(5)) {
          str.append(leader.toString());
          str.append(" Techniques.beats information out of the pathetic thing.");
          if (cr.workLocation().parent() == null) {
            str.append("Unfortunately, none of it is useful to the LCS.");
          } else {
            str.append("A detailed map has been created of ");
            str.append(cr.workLocation().toString());
            str.append('.');
          }
          cr.workLocation().interrogated(true);
          cr.workLocation().hidden(false);
        }
        ui().text(str.toString()).add();
      } else {
        ui().text(cr.toString() + " seems to be getting the message.").add();
        if (cr.juice() > 0) {
          cr.juice(cr.juice() - forceroll);
          if (cr.juice() < 0) {
            cr.juice(0);
          }
        }
        if (cr.skill().getAttribute(Attribute.WISDOM, false) > 1) {
          cr.skill().attribute(Attribute.WISDOM,
              cr.skill().getAttribute(Attribute.WISDOM, false) - (forceroll / 10 + 1));
          if (cr.skill().getAttribute(Attribute.WISDOM, false) < 1) {
            cr.skill().attribute(Attribute.WISDOM, 1);
          }
        }
      }
      if (!cr.skill().attributeCheck(Attribute.HEALTH, forceroll / 3)) {
        // show_interrogation_sidebar(cr,a);
        if (cr.skill().getAttribute(Attribute.HEALTH, false) > 1) {
          cr.skill().attribute(Attribute.HEALTH, -1);
          ui().text(cr.toString() + " is badly hurt.").add();
        } else {
          cr.skill().attribute(Attribute.HEALTH, 0);
          ui().text(cr.toString() + "'s weakened body crumbles under the brutal assault.").add();
          cr.health().die();
        }
      }
    } else {
      ui().text(cr.toString() + " takes it well.").add();
    }
    if (tortured && !cr.health().alive()) {
      if (i.rng.nextInt(leader.skill().getAttribute(Attribute.HEART, false)) > i.rng.nextInt(3)) {
        leader.skill().attribute(Attribute.HEART, -1);
        ui().text(
            leader.toString()
                + " feels sick to the stomach afterward and "
                + i.rng.choice("throws up in a trash can.",
                    "gets drunk, eventually falling asleep.", "curls up in a ball, crying softly.",
                    "shoots up and collapses in a heap on the floor.")).color(Color.GREEN).add();
        leader.skill().train(Skill.SHOOTINGUP, 150);
      } else if (i.rng.chance(3)) {
        ui().text(leader.toString() + " grows colder.").color(Color.CYAN).add();
        leader.skill().attribute(Attribute.WISDOM, +1);
      }
    }
  }

  private int doProps(final Creature leader, final int attack) {
    final StringBuilder str = new StringBuilder();
    str.append(leader.toString());
    str.append(i.rng.choice(" plays violent video games with ", " reads Origin of the Species to ",
        " burns flags in front of ", " explores an elaborate political fantasy with ",
        " watches controversial avant-garde films with ", " plays the anime film Bible Black for ",
        /* Wasn't this basically a porno? Yes. -Fox */
        " watches a documentary about Emmett Till with ", " watches Michael Moore films with ",
        " listens to Liberal radio shows with "));
    str.append(cr.toString());
    str.append('.');
    ui().text(str.toString()).add();
    return attack + 10;
  }

  private void doReason(final Creature leader) {
    final StringBuilder str = new StringBuilder();
    str.append(leader.toString());
    switch (i.rng.nextInt(4)) {
    default:
      str.append(" Techniques.talks about ");
      str.append(i.rng.randFromArray(Issue.coreValues()).getviewsmall());
      str.append(" with ");
      break;
    case 1:
      str.append(" argues about ");
      str.append(i.rng.randFromArray(Issue.coreValues()).getviewsmall());
      str.append(" with ");
      break;
    case 2:
      str.append(" tries to expose the true Liberal side of ");
      break;
    case 3:
      str.append(" attempts to recruit ");
      break;
    }
    str.append(cr.toString());
    str.append('.');
    ui().text(str.toString()).add();
  }

  private int doTalking(final Creature leader, final int attack) {
    int modAttack = attack;
    int rapport_temp = rapport.get(leader);
    if (!techniques.contains(Techniques.RESTRAIN)) {
      modAttack += 5;
    }
    modAttack += rapport_temp * 3 / 10;
    if (techniques.contains(Techniques.PROPS)) {
      modAttack = doProps(leader, modAttack);
    } else {
      doReason(leader);
    }
    /* Hallucinogenic Techniques.drugs: Re-interprets lead interrogator */
    if (techniques.contains(Techniques.DRUGS)) {
      if (cr.skill().skillCheck(Skill.PSYCHOLOGY, CheckDifficulty.CHALLENGING)) {
        switch (i.rng.nextInt(4)) {
        default:
          ui().text(cr.toString() + " takes the drug-induced hallucinations with stoicism.").add();
          break;
        case 1:
          ui().text(cr.toString() + " mutters its initials over and over again.").add();
          break;
        case 2:
          ui().text(cr.toString() + " babbles continuous numerical sequences.").add();
          break;
        case 3:
          ui().text(cr.toString() + " manages to remain grounded through the hallucinations.")
              .add();
          break;
        }
      } else if (rapport.get(leader) > 10 && i.rng.likely(3) || i.rng.chance(10)) {
        rapport_temp = 100;
        switch (i.rng.nextInt(4)) {
        default:
          ui().text(cr.toString() + " hallucinates and sees " + leader.toString() + " as an angel.")
              .add();
          break;
        case 1:
          ui().text(
              cr.toString() + " realizes with joy that " + leader.toString() + " is Ronald Reagan!")
              .add();
          break;
        case 2:
          ui().text(
              cr.toString()
                  + " stammers and "
                  + (techniques.contains(Techniques.RESTRAIN) ? "Techniques.talks about hugging "
                      : "hugs ") + leader.toString() + ".").add();
          break;
        case 3:
          ui().text(
              cr.toString() + " salutes " + leader.toString()
                  + " as the Knight Guardian of Humanity.").add();
          break;
        }
      } else if (rapport.get(leader) < -10 && i.rng.likely(3) || i.rng.chance(5)) {
        modAttack = 0;
        switch (i.rng.nextInt(4)) {
        default:
          ui().text(
              cr.toString() + " shouts in numb terror at the sight of " + leader.toString() + ".")
              .add();
          break;
        case 1:
          ui().text(
              cr.toString()
                  + (!techniques.contains(Techniques.RESTRAIN) ? " curls into a ball and" : "")
                  + " squeals in fear.").add();
          break;
        case 2:
          ui().text(
              cr.toString() + " watches " + leader.toString()
                  + " shift from one monstrous form to another.").add();
          break;
        case 3:
          ui().text(
              cr.toString() + " begs Hitler to tell them where " + leader.toString() + " went.")
              .add();
          break;
        }
      } else {
        switch (i.rng.nextInt(4)) {
        default:
          ui().text(
              cr.toString() + " comments on the shimmering clothing " + leader.toString()
                  + " is wearing.").add();
          break;
        case 1:
          ui().text(cr.toString() + " can't stop looking at the moving colors.").add();
          break;
        case 2:
          ui().text(
              cr.toString() + " laughs hysterically at the sight of " + leader.toString() + ".")
              .add();
          break;
        case 3:
          ui().text(cr.toString() + " barks like a dog.").add();
          break;
        }
      }
    }
    if (cr.skill().skill(Skill.PSYCHOLOGY) > leader.skill().skill(Skill.PSYCHOLOGY)) {
      conversionFailedPsychology(leader);
    } else if (techniques.contains(Techniques.BEAT) || rapport_temp < -20) {
      ui().text(
          cr.toString()
              + i.rng.choice(" babbles mindlessly.", " just whimpers.", " cries helplessly.",
                  " is losing faith in the world.", " only grows more distant.",
                  " is too terrified to even speak to " + leader.toString() + ".",
                  " just hates the LCS even more.")).add();
      if (leader.skill().skillCheck(Skill.SEDUCTION, -rapport_temp * 2 / 10)) {
        switch (i.rng.nextInt(7)) {
        default:
          ui().text(leader.toString() + " consoles the Conservative automaton.").add();
          break;
        case 1:
          ui().text(leader.toString() + " shares some chocolates.").add();
          break;
        case 2:
          ui().text(leader.toString() + " provides a shoulder to cry on.").add();
          break;
        case 3:
          ui().text(leader.toString() + " understands " + cr.toString() + "'s pain.").add();
          break;
        case 4:
          ui().text(leader.toString() + "'s heart opens to the poor Conservative.").add();
          break;
        case 5:
          ui().text(leader.toString() + " helps the poor thing to come to terms with captivity.")
              .add();
          break;
        case 6:
          ui().text(
              leader.toString() + "'s patience and kindness leaves the Conservative confused.")
              .add();
          break;
        }
        rapport.put(leader, rapport.get(leader) + 7);
        if (rapport.get(leader) > 30) {
          switch (i.rng.nextInt(7)) {
          default:
            ui().text(
                cr.toString() + " emotionally clings to " + leader.toString() + "'s sympathy.")
                .add();
            break;
          case 1:
            ui().text(cr.toString() + " begs " + leader.toString() + " for help.").add();
            break;
          case 2:
            ui().text(cr.toString() + " promises to be good.").add();
            break;
          case 3:
            ui().text(cr.toString() + " reveals childhood pains.").add();
            break;
          case 4:
            ui().text(cr.toString() + " thanks " + leader.toString() + " for being merciful.")
                .add();
            break;
          case 5:
            ui().text(cr.toString() + " cries in " + leader.toString() + "'s arms.").add();
            break;
          case 6:
            ui().text(cr.toString() + " really likes " + leader.toString() + ".").add();
            break;
          }
          if (rapport.get(leader) > 50) {
            turned = true;
          }
        }
      }
      if (cr.skill().getAttribute(Attribute.HEART, false) > 1) {
        cr.skill().attribute(Attribute.HEART, -1);
      }
    } else if (cr.skill().skill(Skill.RELIGION) > leader.skill().skill(Skill.RELIGION)
        + leader.skill().skill(Skill.PSYCHOLOGY)
        && !techniques.contains(Techniques.DRUGS)) {
      conversionFailedReligion(leader);
    } else if (cr.skill().skill(Skill.BUSINESS) > leader.skill().skill(Skill.BUSINESS)
        + leader.skill().skill(Skill.PSYCHOLOGY)
        && !techniques.contains(Techniques.DRUGS)) {
      conversionFailedBusiness(leader);
    } else if (cr.skill().skill(Skill.SCIENCE) > leader.skill().skill(Skill.SCIENCE)
        + leader.skill().skill(Skill.PSYCHOLOGY)
        && !techniques.contains(Techniques.DRUGS)) {
      coversionFailedScience(leader);
    } else if (!cr.skill().attributeCheck(Attribute.WISDOM, modAttack / 6)) {
      cr.juice(cr.juice() - modAttack);
      if (cr.juice() < 0) {
        cr.juice(0);
      }
      if (cr.skill().getAttribute(Attribute.HEART, false) < 10) {
        cr.skill().attribute(Attribute.HEART, +1);
      }
      // Improve rapport with interrogator
      rapport.put(leader, rapport.get(leader) + 15);
      // Join LCS??
      // 1) They were liberalized
      if (cr.skill().getAttribute(Attribute.HEART, true) > cr.skill().getAttribute(
          Attribute.WISDOM, true) + 4) {
        turned = true;
      }
      // 2) They were befriended
      if (rapport.get(leader) > 40) {
        turned = true;
      }
      switch (i.rng.nextInt(5)) {
      default:
        ui().text(cr.toString() + "'s Conservative beliefs are shaken.").add();
        break;
      case 1:
        ui().text(cr.toString() + " quietly considers these ideas.").add();
        break;
      case 2:
        ui().text(cr.toString() + " is beginning to see Liberal reason.").add();
        break;
      case 3:
        ui().text(cr.toString() + " has a revelation of understanding.").add();
        break;
      case 4:
        ui().text(cr.toString() + " grudgingly admits sympathy for LCS ideals.").add();
        break;
      }
      if (i.rng.chance(5)) {
        revealWorkLocation(leader);
      }
    }
    /* Target is not sold on the LCS arguments and holds firm This is the worst possible outcome if
     * you use Techniques.props */
    else if (!cr.skill().skillCheck(Skill.PERSUASION,
        leader.skill().getAttribute(Attribute.HEART, true))
        || techniques.contains(Techniques.PROPS)) {
      // Not completely unproductive; builds rapport
      rapport.put(leader, rapport.get(leader) + 2);
      ui().text(cr.toString() + " holds firm.").add();
    }
    /* Target actually wins the argument so successfully that the Liberal interrogator's convictions
     * are the ones that are shaken. */
    else {
      /* Consolation prize is that they end up liking the liberal more */
      rapport.put(leader, rapport.get(leader) + 5);
      leader.skill().attribute(Attribute.WISDOM, +1);
      ui().text(cr.toString() + " turns the tables on " + leader.toString() + "!").add();
      ui().text(leader.toString() + " has gained wisdom!").add();
    }
    return modAttack;
  }

  private int drugHostage(final Creature leader) {
    ui().text("It is subjected to dangerous hallucinogens.").add();
    final int drugbonus = 10 + leader.getArmor().interrogationDrugbonus();
    // Possible permanent health damage
    if (i.rng.nextInt(50) < ++drugUse) {
      cr.skill().attribute(Attribute.HEALTH, -1);
    }
    if (cr.skill().getAttribute(Attribute.HEALTH, false) <= 0) {
      ui().text("It is a lethal overdose in " + cr.toString() + "'s weakened state.")
          .color(Color.YELLOW).add();
      cr.health().die();
    }
    return drugbonus;
  }

  private boolean executeHostage(final List<Creature> temppool) {
    ui().text("The Final Education of " + cr.toString() + ": Day " + cr.joindays()).add();
    Creature leader = null;
    for (final Creature p : temppool) {
      if (i.rng.nextInt(50) < p.juice()
          || i.rng.nextInt(9) + 1 >= p.skill().getAttribute(Attribute.HEART, false)) {
        leader = p;
        break;
      }
    }
    if (leader != null) {
      cr.health().die();
      ui().text(
          leader.toString()
              + " executes "
              + cr.toString()
              + " by "
              + i.rng.choice("strangling it to death.", "Techniques.beating it to death.",
                  "burning photos of Reagan in front of it.",
                  "telling it that taxes have been increased.",
                  "telling it its parents wanted to abort it.")).color(Color.MAGENTA).add();
      if (i.rng.nextInt(leader.skill().getAttribute(Attribute.HEART, false)) > i.rng.nextInt(3)) {
        leader.skill().attribute(Attribute.HEART, -1);
        ui().text(
            leader.toString()
                + " feels sick to the stomach afterward and "
                + i.rng.choice("throws up in a trash can.",
                    "gets drunk, eventually falling asleep.", "curls up in a ball, crying softly.",
                    "shoots up and collapses in a heap on the floor.")).color(Color.GREEN).add();
        leader.skill().train(Skill.SHOOTINGUP, 150);
      } else if (i.rng.likely(3)) {
        ui().text(leader.toString() + " grows colder.").color(Color.CYAN).add();
        leader.skill().attribute(Attribute.WISDOM, +1);
      }
    } else {
      ui().text(
          "There is no one able to get up the nerve to execute " + cr.toString()
              + " in cold blood.").color(Color.YELLOW).add();
      /* Interrogation will continue as planned, with these restrictions: */
      techniques.remove(Techniques.TALK);
      techniques.remove(Techniques.BEAT);
      techniques.remove(Techniques.DRUGS);
      /* Food and restraint settings will be applied as normal */
    }
    if (!cr.health().alive()) {
      clearTempPool(temppool);
      return true;
    }
    return false;
  }

  private List<Creature> generateTempPool() {
    final List<Creature> temppool = new ArrayList<Creature>();
    // Find all tenders who are set to this hostage
    for (final Creature p : Filter.of(i.pool, Filter.LIVING)) {
      if (p.activity().type() == Activity.HOSTAGETENDING) {
        final CreatureActivity ca = (CreatureActivity) p.activity();
        if (ca.creature() == cr) {
          /* If they're in the same location as the hostage, include them in the interrogation */
          if (p.location() == cr.location()) {
            if (p.location() != Location.none()) {
              temppool.add(p);
            }
          } else {
            p.activity(BareActivity.noActivity());
          }
        }
      }
    }
    return temppool;
  }

  private boolean isEscapeSuccessful(final List<Creature> temppool) {
    if (temppool.size() == 0 || !techniques.contains(Techniques.RESTRAIN)) {
      if (i.rng.nextInt(200) + 25 * temppool.size() < cr.skill().getAttribute(
          Attribute.INTELLIGENCE, true)
          + cr.skill().getAttribute(Attribute.AGILITY, true)
          + cr.skill().getAttribute(Attribute.STRENGTH, true)
          && cr.joindays() >= 5) {
        fact(cr.toString() + " has escaped!");
        if (cr.location()!= null) {
          cr.location().lcs().siege.timeUntilLocated = 3;
        }
        // clear activities for tenders
        i.interrogations.remove(this);
        i.pool.remove(cr);
        clearTempPool(temppool);
        return true;
      }
    }
    return false;
  }

  private void killedHostage(@Nullable final Creature leader, final List<Creature> temppool) {
    // delete interrogation information
    i.interrogations.remove(this);
    cr.health().die();
    clearTempPool(temppool);
    ui().text(
        cr.toString() + " is dead"
            + (leader != null ? " under " + leader.toString() + "'s interrogation." : "."))
        .color(Color.RED).add();
    if (leader != null) {
      if (i.rng.likely(leader.skill().getAttribute(Attribute.HEART, false))) {
        leader.skill().attribute(Attribute.HEART, -1);
        ui().text(
            leader.toString()
                + " feels sick to the stomach afterward and "
                + i.rng.choice("throws up in a trash can.",
                    "gets drunk, eventually falling asleep.", "curls up in a ball, crying softly."))
            .color(Color.GREEN).add();
      } else if (i.rng.likely(3)) {
        ui().text(leader.toString() + " grows colder.").color(Color.CYAN).add();
        leader.skill().attribute(Attribute.WISDOM, +1);
      }
    }
  }

  private void revealWorkLocation(final Creature a) {
    ui().text(cr.toString() + " reveals details about the " + cr.workLocation().toString() + ".")
        .add();
    if (cr.workLocation().isInterrogated() || cr.workLocation().parent() == null) {
      ui().text("Unfortunately, none of it is useful to the LCS.").add();
    } else {
      ui().text(a.toString() + " was able to create a map of the site with this information.")
          .add();
    }
    cr.workLocation().interrogated(true).hidden(false);
  }

  /** Shows the interrogation data at the right side of the screen */
  private void showInterrogationSidebar(final Creature prisoner, final Creature lead) {
    final int arapport = rapport.get(lead);
    ui().text(
        "Prisoner: " + prisoner.toString() + "\nHealth: " + prisoner.health().healthStat()
            + "\nHeart: " + prisoner.skill().getAttribute(Attribute.HEART, true) + "\nWisdom: "
            + prisoner.skill().getAttribute(Attribute.WISDOM, true) + "\nHealth: "
            + prisoner.skill().getAttribute(Attribute.HEALTH, true)).color(Color.RED).add();
    ui().text(
        "Lead Interrogator: " + lead.toString() + "\nHealth: " + lead.health().healthStat()
            + "\nPsychology Skill: " + lead.skill().skill(Skill.PSYCHOLOGY) + "\nHeart: "
            + lead.skill().getAttribute(Attribute.HEART, true) + "\nWisdom: "
            + lead.skill().getAttribute(Attribute.WISDOM, true) + "\nOutfit: "
            + lead.getArmor().toString()).color(Color.GREEN).add();
    if (arapport > 30) {
      ui().text("The Conservative clings helplessly to " + lead.toString() + " as its only friend.")
          .add();
    } else if (arapport > 10) {
      ui().text("The Conservative likes " + lead.toString() + ".").add();
    } else if (arapport > -10) {
      ui().text("The Conservative is uncooperative toward " + lead.toString() + ".").add();
    } else if (arapport > -40) {
      ui().text("The Conservative hates " + lead.toString() + ".").add();
    } else {
      ui().text("The Conservative would like to murder " + lead.toString() + ".").add();
    }
  }

  private void suicideWatch() {
    // can't commit suicide if restrained
    if (!turned && cr.health().alive() && cr.skill().getAttribute(Attribute.HEART, false) == 1
        && i.rng.likely(3) && cr.joindays() > 6) {
      if (i.rng.chance(6) || techniques.contains(Techniques.RESTRAIN)) {
        // can't cut self if restrained
        switch (i.rng.nextInt(5 - (techniques.contains(Techniques.RESTRAIN) ? 1 : 0))) {
        default:
          ui().text(cr.toString() + " mutters about death.").color(Color.MAGENTA).add();
          break;
        case 1:
          ui().text(cr.toString() + " broods darkly.").color(Color.MAGENTA).add();
          break;
        case 2:
          ui().text(cr.toString() + " has lost hope of rescue.").color(Color.MAGENTA).add();
          break;
        case 3:
          ui().text(cr.toString() + " is making peace with God.").color(Color.MAGENTA).add();
          break;
        case 4:
          ui().text(cr.toString() + " is bleeding from self-inflicted wounds.")
              .color(Color.MAGENTA).add();
          cr.health().blood(cr.health().blood() - (i.rng.nextInt(15) + 10));
          break;
        }
      } else {
        ui().text(cr.toString() + " has committed suicide.").color(Color.RED).add();
        cr.health().die();
      }
    }
  }

  private void turnAutomaton(final Creature leader, final List<Creature> temppool) {
    i.interrogations.remove(this);
    ui().text("The Automaton has been Enlightened!   Your Liberal ranks are swelling!").add();
    if (cr.skill().getAttribute(Attribute.HEART, true) > 7
        && cr.skill().getAttribute(Attribute.WISDOM, true) > 2 && i.rng.chance(4)
        && cr.hasFlag(CreatureFlag.KIDNAPPED)) {
      ui().text(
          "The conversion is convincing enough that the police no longer consider it a kidnapping.")
          .add();
      cr.removeFlag(CreatureFlag.MISSING);
      cr.removeFlag(CreatureFlag.KIDNAPPED);
    }
    cr.addFlag(CreatureFlag.BRAINWASHED);
    clearTempPool(temppool);
    cr.liberalize(false);
    cr.hire(leader);
    i.score.recruits++;
    if (!cr.workLocation().isInterrogated() || cr.workLocation().isHidden()) {
      revealWorkLocation(leader);
    }
    if (cr.hasFlag(CreatureFlag.MISSING) && !cr.hasFlag(CreatureFlag.KIDNAPPED)) {
      ui().text(cr.toString() + "'s disappearance has not yet been reported.").add();
      Curses.waitOnOK();
      cr.sleeperizePrompt(leader);
      cr.removeFlag(CreatureFlag.MISSING);
    }
  }

  private void updateTechniques(final Creature a) {
    while (true) {
      setView(R.layout.generic);
      ui().text("The Education of " + cr.toString() + ": Day " + cr.joindays()).add();
      showInterrogationSidebar(cr, a);
      if (techniques.contains(Techniques.KILL)) {
        ui().text("The Execution of an Automaton").color(Color.RED).add();
      } else {
        ui(R.id.gcontrol).text("Selecting a Liberal Interrogation Plan").color(Color.YELLOW).add();
      }
      if (!techniques.contains(Techniques.KILL)) {
        ui(R.id.gcontrol)
            .button()
            .button('a')
            .text(techniques.contains(Techniques.TALK) ? "Attempt to Convert" : "No Verbal Contact")
            .add();
        ui(R.id.gcontrol).button('b')
            .text((techniques.contains(Techniques.RESTRAIN) ? "" : "No ") + "Physical Restraints")
            .add();
        ui(R.id.gcontrol)
            .button()
            .button('c')
            .text(
                (techniques.contains(Techniques.BEAT) ? "" : "Not ")
                    + "Violently Techniques.Beaten").add();
        maybeAddButton(R.id.gcontrol, 'd', (techniques.contains(Techniques.PROPS) ? "" : "No ")
            + "Expensive Techniques.Props ($250)", i.ledger.funds() > 250);
        maybeAddButton(R.id.gcontrol, 'e', (techniques.contains(Techniques.DRUGS) ? "" : "No ")
            + "Hallucinogenic Techniques.Drugs ($50)", i.ledger.funds() > 50);
      }
      ui(R.id.gcontrol).button('k')
          .text((techniques.contains(Techniques.KILL) ? "" : "Don't ") + "Kill the Hostage").add();
      ui(R.id.gcontrol).button(10).text("Confirm the Plan").add();
      final int c = getch();
      if (c == 10) {
        break;
      }
      Techniques it;
      if (c >= 'a' && c <= 'e') {
        it = Techniques.values()[c - 'a'];
      } else if (c == 'k') {
        it = Techniques.KILL;
      } else {
        continue;
      }
      if (techniques.contains(it)) {
        techniques.remove(it);
      } else {
        techniques.add(it);
      }
    }
    clearChildren(R.id.gcontrol);
  }

  private static final String[] conservativeEvil = { "McDonalds", "Microsoft", "Bill Gates",
      "Wal-Mart", "George W. Bush", "ExxonMobil", "Trickle-down economics", "Family values",
      "Conservatism", "War on Techniques.Drugs", "War on Terror", "Ronald Reagan", "Rush Limbaugh",
      "Tax cuts", "Military spending", "Ann Coulter", "Deregulation", "Police", "Corporations",
      "Wiretapping" };

  private static final long serialVersionUID = Game.VERSION;

  public static void create(final Creature victim, final Creature kidnapper) {
    i.interrogations.add(new Interrogation(victim, kidnapper));
  }

  private static int baseAttack(final Creature p) {
    return p.skill().getAttribute(Attribute.HEART, true)
        - p.skill().getAttribute(Attribute.WISDOM, true) + p.skill().skill(Skill.PSYCHOLOGY) * 2
        + p.getArmor().interrogationBasepower();
  }

  private static void clearTempPool(final List<Creature> temppool) {
    for (final Creature p : temppool) {
      p.activity(BareActivity.noActivity());
    }
  }
}