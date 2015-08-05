package lcs.android.combat;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.R;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.combat.Fight.Fighting;
import lcs.android.creature.Attribute;
import lcs.android.creature.Creature;
import lcs.android.creature.health.Animal;
import lcs.android.creature.skill.Skill;
import lcs.android.game.GameMode;
import lcs.android.law.Crime;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.Advance;
import lcs.android.site.map.SuccessTest;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** code for making conservatives found in encounters into hostages */
public @NonNullByDefault class Kidnap {
  private Kidnap() {}

  /** Display prompt on screen, asking to choose a liberal to perform the kidnapping (or cancel),
   * choose a conservative, and then rolls some dice to see whether you are successful. */
  public static void kidnapAttempt() {
    /* determine whether we're going to kidnap */
    final Creature kidnapper = chooseKidnapper();
    if (kidnapper == null)
      return;
    clearChildren(R.id.gcontrol);
    final List<Creature> targets = getTargets();
    if (targets.isEmpty()) {
      fact("All of the targets are too dangerous.");
      return;
    }
    final Creature target = selectTarget(targets);
    if (target == null)
      return;
    /* chosen kidnapper and target. Onwards! */
    final SuccessTest attempt = grab(kidnapper, target);
    if (attempt.succeeded()) { // success
      if (i.mode() == GameMode.SITE) {
        target.dropLoot(i.groundLoot());
      }
      i.currentEncounter().creatures().remove(target);
      final int time = 20 + i.rng.nextInt(10);
      if (i.site.alarmTimer() > time || i.site.alarmTimer() == -1) {
        i.site.alarmTimer(time);
      }
    } else { // failure
      i.site.alarm(true);
    }
    if (attempt.madeNoise()) { // amateur
      if (othersPresent()) {
        i.site.alienationCheck(true);
        i.site.alarm(true);
        i.site.crime(i.site.crime() + 5);
        i.activeSquad().criminalizeParty(Crime.KIDNAPPING);
        offendSpecial(target);
      }
    }
    if (i.site.alarm()) {
      Fight.fight(Fighting.THEM);
    }
    Advance.creatureAdvance();
    ui(R.id.gcontrol).button(' ').text("OK").add();
    getch();
  }

  private static Creature chooseKidnapper() {
    setView(R.layout.generic);
    ui().text("Kidnap Attempt:").bold().add();
    do {
      ui(R.id.gcontrol).text("Choose a Liberal squad member to do the job.").add();
      int y = '1';
      for (final Creature creature : i.activeSquad()) {
        if (creature.health().alive() && creature.prisoner().missing()) {
          ui(R.id.gcontrol).button(y++).text(creature.toString()).add();
        }
      }
      if (y == '1') {
        fact("No one can do the job.");
        return null;
      }
      ui(R.id.gcontrol).text("Rethink this kidnapping").button(ENTER).add();
      final int c = getch();
      if (c == ENTER)
        return null;
      if (c >= '1' && c < y) {
        final Creature k = i.activeSquad().member(c - '1');
        if (k.health().alive() && k.prisoner().missing())
          return k;
      }
    } while (true);
  }

  private static List<Creature> getTargets() {
    final List<Creature> target = new ArrayList<Creature>();
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.health().alive()
          && e.alignment() == Alignment.CONSERVATIVE
          && (e.type().animal() == Animal.HUMAN || i.issue(Issue.ANIMALRESEARCH).law() == Alignment.ELITELIBERAL)) {
        if (e.weapon().isArmed() && e.weapon().weapon().protectsAgainstKidnapping()
            && e.health().blood() > 20 || e.type().animal() == Animal.TANK) {
          continue;
        }
        target.add(e);
      }
    }
    return target;
  }

  private static SuccessTest grab(final Creature liberal, final Creature victim) {
    setView(R.layout.generic);
    if (liberal.weapon().weapon().canTakeHostages()) { /* automatic success */
      ui().text(
          liberal + " shows " + victim + " the " + liberal.weapon().weapon().toString()
              + " and says, "
              + (!i.freeSpeech() ? "\"[Please], be cool.\"" : "\"Bitch, be cool.\""))
          .color(Color.GREEN).add();
      liberal.prisoner(victim);
      return SuccessTest.SUCCEED_QUIETLY;
    }
    final int lRoll = liberal.skill().skillRoll(Skill.HANDTOHAND);
    final int vRoll = victim.skill().attributeCheck(Attribute.AGILITY, 1) ? 1 : 0;
    liberal.skill().train(Skill.HANDTOHAND, vRoll);
    if (lRoll > vRoll) { /* success */
      ui().text(liberal.toString() + " snatches " + victim.toString() + "!").add();
      liberal.prisoner(victim);
      ui().text(victim.toString() + " is struggling and screaming!").color(Color.RED).add();
      return SuccessTest.SUCCEED_NOISILY;
    }
    /* failed */
    ui().text(
        liberal.toString() + " grabs at " + victim.toString() + " but " + victim.toString()
            + " writhes away!").color(Color.MAGENTA).add();
    return SuccessTest.FAIL_NOISILY;
  }

  private static void offendSpecial(final Creature target) {
    if (target.type().ofType("RADIOPERSONALITY")) {
      i.offended.put(CrimeSquad.AMRADIO, true);
    } else if (target.type().ofType("NEWSANCHOR")) {
      i.offended.put(CrimeSquad.CABLENEWS, true);
    }
  }

  private static boolean othersPresent() {
    for (final Creature e : i.currentEncounter().creatures()) {
      if (e.health().alive())
        return true;
    }
    return false;
  }

  private static Creature selectTarget(final List<Creature> targets) {
    if (targets.size() == 1)
      return targets.get(0);
    do {
      ui(R.id.gcontrol).text("Kidnap whom?").add();
      int y = 'a';
      for (final Creature target : targets) {
        ui(R.id.gcontrol).button(y++).text(target.toString()).add();
      }
      ui(R.id.gcontrol).button(ENTER).text("Actually, that's not such a good idea after all...")
          .add();
      final int c = getch();
      clearChildren(R.id.gcontrol);
      if (c == ENTER || c == ESCAPE || c == ' ')
        return null;
      if (c >= 'a') {
        final int offs = c - 'a';
        if (offs >= targets.size())
          return null;
        return targets.get(offs);
      }
    } while (true);
  }
}
