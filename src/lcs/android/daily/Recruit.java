package lcs.android.daily;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.io.Serializable;

import lcs.android.R;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.skill.CreatureSkill;
import lcs.android.creature.skill.Skill;
import lcs.android.game.Game;
import lcs.android.game.Ledger;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class Recruit implements Serializable {
  public Recruit(final Creature recruit, final Creature recruiter) {
    this.recruit = recruit;
    this.recruiter = recruiter;
    if (i.rng.nextInt(100) < i.issue(Issue.LIBERALCRIMESQUAD).attitude()) {
      if (i.rng.nextInt(100) < i.issue(Issue.LIBERALCRIMESQUADPOS).attitude()) {
        eagerness = 3;
      } else {
        eagerness = 0;
      }
    } else {
      eagerness = 2;
    }
  }

  public final Creature recruiter;

  protected final Creature recruit;

  private int eagerness;

  private int level;

  /* daily - recruit - recruit meeting */
  boolean completeRecruitMeeting() {
    final StringBuilder sb = new StringBuilder();
    setView(R.layout.date);
    setText(R.id.dateTitle, "Meeting with " + recruit + ", " + recruit.type().jobtitle(recruit));
    setText(R.id.dateMoney, "Money: " + i.ledger.funds());
    recruit.printCreatureInfo(level);
    sb.append(recruit);
    switch (adjustedEagerness()) {
    case 1:
      sb.append(" will take a lot of persuading.");
      break;
    case 2:
      sb.append(" is interested in learning more.");
      break;
    case 3:
      sb.append(" feels something needs to be done.");
      break;
    default:
      if (adjustedEagerness() >= 4) {
        sb.append(" is ready to fight for the Liberal Cause.");
      } else {
        sb.append(" kind of regrets agreeing to this.");
      }
      break;
    }
    sb.append("\n\nHow should " + recruiter + " approach the situation?");
    ui().text(sb.toString()).add();
    sb.setLength(0);
    maybeAddButton(R.id.gcontrol, 'a', "Spend $50 on props and a book for them to keep afterward.",
        i.ledger.funds() >= 50);
    ui(R.id.gcontrol).button('b').text("Just casually chat with them and discuss politics.").add();
    if (recruiter.subordinatesLeft() > 0 && adjustedEagerness() >= 4) {
      ui(R.id.gcontrol).button('c')
          .text("Offer to let " + recruit + " join the LCS as a full member.").add();
    } else if (recruiter.subordinatesLeft() != 0) {
      ui(R.id.gcontrol).button().text(recruiter + " needs more Juice to recruit.").add();
    } else {
      ui(R.id.gcontrol).button().text(recruit + " isn't ready to join the LCS.").add();
    }
    ui(R.id.gcontrol).button('d').text("Break off the meetings.").add();
    int c;
    do {
      c = getch();
    } while (!(c >= 'a' && c <= 'd'));
    clearChildren(R.id.gcontrol);
    final CreatureSkill iSkill = recruiter.skill();
    final CreatureSkill rSkill = recruit.skill();
    if (c == 'c' && recruiter.subordinatesLeft() > 0 && adjustedEagerness() >= 4) {
      ui().text(recruiter + " offers to let " + recruit + " join the Liberal Crime Squad.").add();
      ui().text(recruit + " accepts, and is eager to get started.").color(Color.GREEN).add();
      getch();
      recruit.liberalize(false).giveFundsToLCS().hire(recruiter).sleeperizePrompt(recruiter);
      iSkill.train(Skill.PERSUASION, 25);
      i.pool.add(recruit);
      i.score.recruits++;
      return true;
    }
    if (c == 'b' || c == 'a' && i.ledger.funds() >= 50) {
      if (c == 'a') {
        i.ledger.subtractFunds(50, Ledger.ExpenseType.RECRUITMENT);
      }
      /* SAV - You can get your skill up to a 3 by chatting. Past that, you must successfully
       * recruit people. Training is slower the better you are. JDS - Increased max skill to get to
       * 12 under this system, gave minimum of 5 exp for the action. */
      iSkill.train(Skill.PERSUASION, Math.max(12 - iSkill.skill(Skill.PERSUASION), 5));
      iSkill.train(Skill.SCIENCE,
          Math.max(rSkill.skill(Skill.SCIENCE) - iSkill.skill(Skill.SCIENCE), 0));
      iSkill.train(Skill.RELIGION,
          Math.max(rSkill.skill(Skill.RELIGION) - iSkill.skill(Skill.RELIGION), 0));
      iSkill.train(Skill.LAW, Math.max(rSkill.skill(Skill.LAW) - iSkill.skill(Skill.LAW), 0));
      iSkill.train(Skill.BUSINESS,
          Math.max(rSkill.skill(Skill.BUSINESS) - iSkill.skill(Skill.BUSINESS), 0));
      final int libPersuasiveness = iSkill.skill(Skill.BUSINESS) + iSkill.skill(Skill.SCIENCE)
          + iSkill.skill(Skill.RELIGION) + iSkill.skill(Skill.LAW)
          + iSkill.getAttribute(Attribute.INTELLIGENCE, true);
      int recruitReluctance = 5 + rSkill.skill(Skill.BUSINESS) + rSkill.skill(Skill.SCIENCE)
          + rSkill.skill(Skill.RELIGION) + rSkill.skill(Skill.LAW)
          + rSkill.getAttribute(Attribute.WISDOM, true)
          + rSkill.getAttribute(Attribute.INTELLIGENCE, true);
      recruitReluctance = libPersuasiveness > recruitReluctance ? 0 : recruitReluctance
          - libPersuasiveness;
      if (c == 'a') {
        recruitReluctance -= 5;
        ui().text(recruiter + " shares " + getInterviewProp() + '.').add();
      } else {
        ui().text(
            recruiter + " explains " + recruiter.genderLiberal().possesive + " views on "
                + i.rng.randFromArray(Issue.values()).getviewsmall() + '.').add();
      }
      if (iSkill.skillCheck(Skill.PERSUASION, recruitReluctance)) {
        level++;
        eagerness++;
        ui().text(
            recruit + " found " + recruiter
                + "'s views to be insightful.\n\nThey'll definitely meet again tomorrow.")
            .color(Color.CYAN).add();
      } else if (iSkill.skillCheck(Skill.PERSUASION, recruitReluctance)) {
        level++;
        eagerness--;
        ui().text(
            recruit + " is skeptical about some of " + recruiter
                + "'s arguments.\n\nThey'll meet again tomorrow.").add();
      } else {
        if (recruit.isTalkReceptive() && recruit.alignment() == Alignment.LIBERAL) {
          ui().text(
              recruit + " isn't convinced " + recruiter
                  + " really understands the problem.\n\nMaybe " + recruiter
                  + " needs more experience.").color(Color.MAGENTA).add();
        } else {
          ui().text(
              recruiter
                  + " comes off as slightly insane.\n\nThis whole thing was a mistake. There won't be another meeting.")
              .color(Color.MAGENTA).add();
        }
        getch();
        return true;
      }
      getch();
      return false;
    }
    return true;
  }

  private int adjustedEagerness() {
    if (recruit.alignment() == Alignment.MODERATE)
      return eagerness - 2;
    if (recruit.alignment() == Alignment.CONSERVATIVE)
      return eagerness - 4;
    return eagerness;
  }

  private static final long serialVersionUID = Game.VERSION;

  private static String getInterviewProp() {
    return i.rng.choice("a collection of studies on the health effects of marijuana",
        "a book on the history of military atrocities",
        "a reality TV episode on the lives of immigrants",
        "a documentary on the civil rights struggle",
        "a documentary on the women's rights struggle", "a documentary on the gay rights struggle",
        "a research paper on abuses of the death penalty",
        "an economic paper on the flaws of trickle-down",
        "a video tour of the Chernobyl dead zone", "a documentary on animal research",
        "a hand-recorded video of police brutality",
        "a government inquiry into military interrogations", "a documentary on privacy rights",
        "a collection of banned books", "a video about genetic engineering accidents",
        "a Liberal policy paper inquiring into judicial decisions",
        "a book profiling school shootings", "a hand-recorded video of unregulated sweatshops",
        "a leaked government paper on environmental conditions",
        "a documentary on life under corporate culture",
        "a Liberal think-tank survey of top CEO salaries",
        "a collection of Conservative radio host rants",
        "a collection of leaked Conservative cable news memos",
        "a documentary about progress made by direct action");
  }
}
