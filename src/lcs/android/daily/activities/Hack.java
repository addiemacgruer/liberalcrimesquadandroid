package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.basemode.iface.Location;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.items.Loot;
import lcs.android.law.Crime;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Hack extends ArrayList<Creature> implements DailyActivity {
  @Override public void daily() {
    final List<Creature> hack = this;
    if (hack.size() > 0) {
      final List<Creature> cc = new ArrayList<Creature>();
      final List<Creature> web = new ArrayList<Creature>();
      final List<Creature> ddos = new ArrayList<Creature>();
      final List<Creature> truehack = new ArrayList<Creature>();
      // First, do accounting to figure out who's doing what
      for (final Creature h : hack) {
        switch (h.activity().type()) {
        case CCFRAUD:
          h.skill().train(Skill.COMPUTERS, 2);
          cc.add(h);
          break;
        case DOS_ATTACKS: // not used
          h.skill().train(Skill.COMPUTERS, 2);
          web.add(h);
          break;
        case DOS_RACKET: // not used
          h.skill().train(Skill.COMPUTERS, 4);
          ddos.add(h);
          break;
        case HACKING:
          h.skill().train(Skill.COMPUTERS, 4);
          truehack.add(h);
          break;
        default:
          break;
        }
      }
      doMajorHacking(truehack);
      doCreditCardFraud(cc);
    }
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;

  private static void doCreditCardFraud(final List<Creature> cc) {
    int hack_skill;
    int difficulty;
    // CREDIT CARD FRAUD
    for (final Creature h : cc) {
      hack_skill = h.skill().skillRoll(Skill.COMPUTERS);
      difficulty = CheckDifficulty.CHALLENGING.value();
      if (difficulty <= hack_skill) {
        /*  *JDS* You get between $1 and $100, plus an extra $1-50 every time you pass a check
         * against your hacking skill, where chance of failure is one over the adjusted hackers'
         * skill divided by four. Once a check fails, no more money is gained. This check will be
         * made up to 20 times, at which point the loop breaks. The skill check here doesn't take
         * into account funding. */
        int fundgain = i.rng.nextInt(101);
        while (difficulty < hack_skill) {
          fundgain += i.rng.nextInt(51);
          difficulty += 2;
        }
        i.ledger.addFunds(fundgain, Ledger.IncomeType.CCFRAUD);
        h.income(fundgain / cc.size());
        ui().text(h.toString() + " made $" + h.income() + " defrauding credit cards.").add();
        if (fundgain / 25 > i.rng.nextInt(hack_skill + 1)) {
          h.crime().criminalize(Crime.CCFRAUD);
        }
      }
      ui().text(h.toString() + " didn't succeed in hacking any credit cards.").add();
      // if (msg.length() > 0)
      // addText(R.id.gmessages, msg.toString());
      // msg.setLength(0);
    }
  }

  private static void doMajorHacking(final List<Creature> truehack) {
    int hack_skill = 0;
    for (final Creature th : truehack) {
      hack_skill = Math.max(hack_skill, th.skill().skillRoll(Skill.COMPUTERS));
    }
    final StringBuilder msg = new StringBuilder();
    if (CheckDifficulty.HEROIC.value() <= hack_skill + truehack.size() - 1) {
      if (truehack.size() > 1) {
        msg.append("Your Hackers have ");
      } else {
        msg.append(truehack.get(0).toString());
        msg.append(" has ");
      }
      final HackTarget ht = i.rng.randFromArray(HackTarget.values());
      msg.append(ht.desc);
      final String loot = i.rng.randFromArray(ht.loot);
      Location r = truehack.get(0).location();
      if (true) {
        truehack.get(0).location().lcs().loot.add(new Loot(loot));
      }
      ht.changePublicOpinion();
      if (ht.difficulty.value() > hack_skill + i.rng.nextInt(5) - 2) {
        for (final Creature h2 : truehack) {
          h2.crime().criminalize(ht.crime);
        }
      }
      // Award juice to the hacking team for a job well done
      for (final Creature h2 : truehack) {
        h2.addJuice(ht.juice, 200);
      }
    } else if (CheckDifficulty.FORMIDABLE.value() <= hack_skill + truehack.size() - 1) {
      final Issue issue = i.rng.randFromArray(Issue.values());
      // Maybe do a switch on issue here to specify which website it
      // was, but I don't feel like
      // doing that right now
      if (truehack.size() > 1) {
        msg.append("Your hackers have ");
      } else {
        msg.append(truehack.get(0).toString());
        msg.append(" has ");
      }
      final MinorHack mh = i.rng.randFromArray(MinorHack.values());
      msg.append(mh.desc);
      msg.append(" a ");
      msg.append(i.rng.choice("corporate website", "Conservative forum", "Conservative blog",
          "news website", "government website"));
      msg.append('.');
      i.issue(issue).changeOpinion(1, 1, 100);
      if (CheckDifficulty.FORMIDABLE.value() < hack_skill + i.rng.nextInt(5) - 2) {
        for (final Creature h : truehack) {
          h.crime().criminalize(mh.crime);
        }
      }
      // Award juice to the hacking team for a job well done
      for (final Creature h : truehack) {
        h.addJuice(5, 100);
      }
    } else if (truehack.size() > 0) {
      ui().text("Your hackers had no successes.").add();
    }
    ui().text(msg.toString()).add();
  }
}
