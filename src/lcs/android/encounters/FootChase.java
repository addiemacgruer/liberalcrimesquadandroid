package lcs.android.encounters;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Iterator;
import java.util.List;

import lcs.android.R;
import lcs.android.combat.Fight;
import lcs.android.combat.Fight.Fighting;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureType;
import lcs.android.creature.health.Animal;
import lcs.android.daily.Interrogation;
import lcs.android.game.Game;
import lcs.android.game.GameMode;
import lcs.android.items.AbstractItem;
import lcs.android.law.Crime;
import lcs.android.monthly.EndGame;
import lcs.android.news.NewsEvent;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.Advance;
import lcs.android.site.Squad;
import lcs.android.util.Color;
import lcs.android.util.Filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class FootChase extends Encounter {
  /** Brand new foot chase. */
  public FootChase() {}

  /** Continue an encounter (normally bail from a car chase) with a foot chase.
   * @param e the encounter to continue from */
  public FootChase(final Encounter e) {
    creatures().addAll(e.creatures());
  }

  /** Chase someone on foot. IT SHOULD NOT DELETE SQUADS OR CREATURES SQUADS MAY BE FICTITIOUS AND
   * BOTH WILL BE DELETED LATER ANYWAY
   * @return true if anyone escaped. */
  @Override public boolean encounter() { // TODO too long.
    Fight.reloadparty();
    if (creatures().isEmpty())
      return true;
    for (final Creature e : creatures()) {
      e.car(null);
    }
    i.mode(GameMode.CHASEFOOT);
    do {
      final int partyalive = Filter.of(i.activeSquad(), Filter.LIVING).size();
      setView(R.layout.hospital);
      if (i.activeSquad().location().exists()) {
        i.activeSquad().location().get().printLocationHeader();
      }
      printEncounter();
      i.activeSquad().printParty();
      if (partyalive == 0) {
        fact("Reflect on your lack of skill.");
        for (final Creature p : i.activeSquad()) {
          if (p.car().exists()) {
            i.vehicle.remove(p.car().get());
          }
        }
        i.activeSquad().clear();
        EndGame.endcheck(null);
        i.mode(GameMode.BASE);
        return false;
      }
      ui(R.id.gcontrol).button('d').text("Try to lose them!").add();
      ui(R.id.gcontrol).button('e').text("Equip").add();
      ui(R.id.gcontrol).button('f').text("Fight!").add();
      ui(R.id.gcontrol).button('o').text("Order the party").add();
      final int c = getch();
      clearChildren(R.id.gcontrol);
      if (i.activeSquad().displaySquadInfo(c)) {
        continue;
      }
      if (c == 'd') {
        if (creatures().get(0).type() == CreatureType.valueOf("COP")) {
          i.siteStory.addNews(NewsEvent.FOOT_CHASE);
          i.activeSquad().criminalizeParty(Crime.RESIST);
        }
        evasiveRun();
        setView(R.layout.generic);
        Fight.fight(Fighting.THEM);
        Advance.creatureAdvance();
      }
      if (c == 'f') {
        if (creatures().get(0).type() == CreatureType.valueOf("COP")) {
          i.siteStory.addNews(NewsEvent.FOOT_CHASE);
          i.activeSquad().criminalizeParty(Crime.RESIST);
        }
        setView(R.layout.generic);
        Fight.fight(Fighting.BOTH);
        Advance.creatureAdvance();
      }
      if (c == 'e') {
        AbstractItem.equip(i.activeSquad().loot(), null);
      }
      final boolean liberalsAlive = Filter.any(i.activeSquad(), Filter.LIVING);
      final boolean enemiesAlive = Filter.any(creatures(), Filter.LIVING);
      if (liberalsAlive && !enemiesAlive) {
        fact("It looks like you've lost them!");
        for (final Creature p : i.pool) {
          p.health().stopBleeding();
        }
        i.mode(GameMode.BASE);
        return true;
      }
      ui(R.id.gcontrol).button(' ').text("OK").add();
      getch();
    } while (true);
  }

  /** the next function forces a chase sequence with a specific liberal
   * @param liberal the liberal to chase. */
  public boolean footchase(final Creature liberal) {
    final Squad oldSquad = liberal.squad().getNullable();
    final Squad newSquad = new Squad();
    newSquad.add(liberal);
    liberal.car(null);
    final Squad oldActiveSquad = i.activeSquad();
    i.setActiveSquad(newSquad);
    i.activeSquad().highlightedMember(0);
    final boolean escaped = encounter();
    if (escaped) {
      liberal.squad(oldSquad);
    } else if (oldSquad != null) {
      oldSquad.add(liberal);
    }
    i.setActiveSquad(oldActiveSquad);
    return escaped;
  }

  private void evasiveRun() {
    final List<Creature> living = Filter.of(i.activeSquad(), Filter.LIVING);
    final int yourWorst = flavorTextForEvasion(Filter.lowest(living, Filter.SPEED));
    loseTheirSlowest(yourWorst);
    if (creatures().isEmpty()) {
      ui().text("That looks like the last of them!").add();
      return;
    }
    final int theirbest = Filter.highest(creatures(), Filter.SPEED);
    /* This last loop can be used to have fast people in your i.squad() escape one by one just as
     * the enemy falls behind one by one */
    for (final Creature liberal : Filter.of(i.activeSquad(), Filter.LIVING)) {
      if (liberal.speed() > theirbest) {
        escapeChase(liberal);
      }
      if (liberal.speed() + 10 > theirbest) { /* try another round */
        continue;
      }
      /* caught! */
      liberal.captureByPolice(Crime.RESIST);
      i.activeSquad().remove(liberal);
      ui().text(liberal.toString()).color(Color.CYAN).add();
      if (creatures().get(0).type().ofType("COP")) {
        ui().text(" is seized, ").add();
        if (i.issue(Issue.POLICEBEHAVIOR).lawGTE(Alignment.LIBERAL)) {
          ui().text("pushed to the ground, and handcuffed!").add();
        } else {
          ui().text(
              "thrown to the ground, and tazed "
                  + (liberal.health().blood() < 10 ? "to death!" : "repeatedly!")).add();
          liberal.health().blood(liberal.health().blood() - 10);
        }
      } else if (creatures().get(0).type().ofType("DEATHSQUAD")) {
        ui().text(" is seized, thrown to the ground, and shot in the head!").add();
        liberal.health().die();
        /* deathsquads don't mess around, keep going... */
      } else if (creatures().get(0).type().ofType("TANK")) {
        ui().text(" crushed beneath the tank's treads!").add();
        liberal.health().die();
        break; // end of punishment this turn
      } else {
        ui().text(
            " is seized, thrown to the ground, and beaten "
                + (liberal.health().blood() < 60 ? "to death!" : "senseless!")).add();
        liberal.health().blood(liberal.health().blood() - 60);
        break; // end of punishment this turn
      }
    }
  }

  private void loseTheirSlowest(final int yourWorst) {
    for (final Iterator<Creature> ei = creatures().iterator(); ei.hasNext();) {
      final Creature enemy = ei.next();
      final int chaseSpeed = enemy.speed();
      if (enemy.type().animal() == Animal.TANK && i.rng.likely(10)) {
        /* can't escape tanks that easily */
        ui().text(
            enemy
                + i.rng.choice(" plows through a brick wall like it was nothing!",
                    " charges down an alley, smashing both side walls out!",
                    " smashes straight through traffic, demolishing cars!",
                    " destroys everything in its path, closing the distance!")).color(Color.YELLOW)
            .add();
      } else if (chaseSpeed < yourWorst) { /* getting away */
        if (enemy.type().animal() == Animal.TANK) {
          ui().text(enemy + " tips into a pool. The tank is trapped!").color(Color.CYAN).add();
        } else {
          ui().text(enemy + " can't keep up!").color(Color.CYAN).add();
        }
        ei.remove();
      } else {
        ui().text(enemy.toString() + " is still on your tail!").color(Color.YELLOW).add();
      }
    }
  }

  private static final long serialVersionUID = Game.VERSION;

  /** Chase sequence! Wee!
   * @param liberal who's being chased
   * @param activity what they were doing when they started being chased.
   * @return true if they escaped */
  public static boolean attemptArrest(final Creature liberal, final String activity) {
    fact("Police are attempting to arrest " + liberal.toString() + " while " + activity + '!');
    return new FootChase(Encounter.createEncounter(null, 5)).footchase(liberal);
  }

  private static void escapeChase(final Creature liberal) {
    ui().text(liberal + " breaks away!").color(Color.CYAN).add();
    /* Unload hauled hostage or body when they get back to the safehouse */
    if (liberal.prisoner().exists()) {
      if (liberal.prisoner().get().squad().exists()) {
        liberal.prisoner().get().removeSquadInfo();
        liberal.prisoner().get().newHome(i.activeSquad().base().getNullable());
      } else {
        Interrogation.create(liberal.prisoner().get(), i.activeSquad().member(0));
      }
      liberal.prisoner(null);
    }
    liberal.removeSquadInfo();
    i.activeSquad().remove(liberal);
    i.activeSquad().printParty();
  }

  private static int flavorTextForEvasion(final int yourworst) {
    if (yourworst > 14) {
      switch (i.rng.nextInt(yourworst / 5)) {
      case 1:
        ui().text("You run as fast as you can!").add();
        break;
      case 2:
        ui().text("You climb a fence in record time!").add();
        break;
      case 3:
        ui().text("You scale a small building and leap between rooftops!").add();
        break;
      default:
        ui().text("You suddenly dart into an alley!").add();
        break;
      }
      return yourworst + i.rng.nextInt(5);
    }
    return yourworst;
  }
}
