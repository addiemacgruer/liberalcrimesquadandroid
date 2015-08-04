package lcs.android.news;

import static lcs.android.game.Game.*;
import lcs.android.creature.CreatureName;
import lcs.android.creature.Gender;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class Filler {
  static void constructfillerstory() {
    final StringBuilder story = new StringBuilder();
    final Gender gender = i.rng.choice(Gender.MALE, Gender.FEMALE);
    final String firstname = CreatureName.firstName(gender);
    final String lastname = CreatureName.lastname();
    switch (i.rng.nextInt(5)) {
    default:// VIEW_GUNS:
    {
      story.append(Game.cityName());
      story.append(" - ");
      story.append("A student has gone on a ");
      if (!i.freeSpeech()) {
        story.append("[violent]");
      } else {
        story.append("shooting");
      }
      story.append(" rampage at a local high school.  ");
      story.append(firstname);
      story.append(' ');
      story.append(lastname);
      story.append(", ");
      final int killerage = i.rng.nextInt(14) + 5;
      story.append(killerage);
      story.append(", used a variety of ");
      if (i.issue(Issue.GUNCONTROL).law() == Alignment.LIBERAL) {
        story.append("illegal ");
      } else if (i.issue(Issue.GUNCONTROL).law() == Alignment.ELITELIBERAL) {
        story.append("highly illegal ");
      }
      story.append("weapons to shoot more than a dozen classmates and two teachers at ");
      story.append(CreatureName.lastname());
      story.append(" High School, before committing suicide.\n");
      story.append("  ");
      story.append(firstname);
      story.append(" was considered an outcast ");
      break;
    }
    case 1:// VIEW_PRISONS:
    {
      story.append(Game.cityName());
      story.append(" - A former prisoner has written a book describing in horrifying ");
      story.append("detail what goes on behind bars.  ");
      story.append("Although popular culture has used, or perhaps overused, the ");
      story.append("prison theme lately in its offerings for mass consumption, rarely ");
      story.append("have these works been as poignant as ");
      story.append(firstname);
      story.append(' ');
      story.append(lastname);
      story.append("'s new tour-de-force, _");
      switch (i.rng.nextInt(6)) {
      default:
        story.append("Nightmare");
        break;
      case 1:
        story.append("Primal");
        break;
      case 2:
        story.append("Animal");
        break;
      case 3:
        story.append("American");
        break;
      case 4:
        story.append("Solitary");
        break;
      case 5:
        story.append("Painful");
        break;// Painful Soap, anyone?
      }
      story.append('_');
      switch (i.rng.nextInt(8)) {
      default:
        story.append("Packer");
        break;
      case 1:
        story.append("Soap");
        break;// Nightmare Soap, anyone?
      case 2:
        story.append("Punk");
        break;
      case 3:
        story.append("Kid");
        break;
      case 4:
        story.append("Cell");
        break;
      case 5:
        story.append("Shank");
        break;
      case 6:
        story.append("Lockdown");
        break;
      case 7:
        if (!i.freeSpeech()) {
          story.append("[Bum]lord");
        } else {
          story.append("Buttlord");
        }
        break;
      case 8:
        story.append("Shower");
        break;
      }
      story.append("_.\n");
      story.append("   Take this excerpt, \"");// TODO: Add more
      // excerpts,
      // more
      // variety.
      story.append("The steel bars grated forward in their rails, ");
      story.append("coming to a halt with a deafening clang that said it all -- ");
      story.append("I was trapped with them now.  There were three, looking me over ");
      story
          .append("with dark glares of bare lust, as football players might stare at a stupefied, drunken, helpless teenager.  ");
      story
          .append("My shank's under the mattress.  Better to be a man and fight or a punk and let them take it?  ");
      story.append("Maybe lose an eye the one way, maybe catch ");
      if (i.issue(Issue.GAY).law() == Alignment.ARCHCONSERVATIVE) {
        story.append("GRIDS");// Gay Related Immunodeficiency
        // Syndrome,
        // an
        // obsoleted/politically incorrect name for
        // "AIDS".
      } else {
        story.append("AIDS");
      }
      story.append(" the other.  A ");
      if (!i.freeSpeech()) {
        story.append("[heck]uva");
      } else {
        story.append("helluva");
      }
      story.append(" choice, and I would only have a few seconds before they made it for me");
      story.append(".\"");
      story.append('\n');
      break;
    }
    case 2:// VIEW_POLITICALVIOLENCE:
    {
      story.append(Game.cityName());
      story.append(" - The dismissal of " + gender.possesive);
      story.append(" final appeal ended the tragic tale of ");
      story.append(firstname + " " + lastname);
      story.append(" today. ");
      story.append(firstname);
      story.append(", " + (30 + i.rng.nextInt(40)));
      story.append(", is the central figure of what many consider to be the greatest political "
          + "scandal in years. A successful owner of a French language-themed bookshop "
          + "until two years ago, ");
      story.append(firstname);
      story.append("'s downfall began when local police unlawfully confiscated much of ");
      story.append(gender.possesive);
      story
          .append(" stock for 'Un-American content'. Attempts to take the matter to the legal arena "
              + "failed after the judge - who happened to be a brother-in-law of one of the "
              + "accused officers - threw out the case and the state supreme court declined to "
              + "get involved despite widespread media publication of the case.");
      story.append("\n "); // comment this line to create a wall of
      // text
      story.append("  Four months ago, a disillusioned and bankrupt ");
      story.append(firstname);
      story
          .append(" was caught in the act of vandalizing the courthouse windows with a baseball bat. ");
      if (gender == Gender.MALE) {
        story.append("He");
      } else {
        story.append("She");
      }
      story
          .append(" was subsequently convicted of breaking the peace, vandalism, attempted murder "
              + "and terrorism and sentenced to life in prison. The presiding judge was a childhood "
              + "friend of (continued: Nightmare, ");
      // story.append(Misc.a());
      // story.append(Misc.d());
      story.append(')');
      story.append('\n');
      break;
    }
    case 3:// VIEW_PRISONS:
    {
      final Gender hgender = i.rng.choice(Gender.MALE, Gender.FEMALE);
      final String hfirstname = CreatureName.firstName(gender);
      final String hlastname = CreatureName.lastname();
      final String facilityName = CreatureName.lastname();
      story.append(Game.cityName());
      story.append(" - The hostage crisis at the ");
      story.append(facilityName);
      story.append(" Correctional Facility ended tragically yesterday with the ");
      story.append("death of both the prison guard being held hostage and ");
      story.append(hgender.possesive);
      story.append(" captor.");
      story.append('\n');
      if (!i.freeSpeech()) {
        story.append("   Two weeks ago, convicted [reproduction fiend] ");
      } else {
        story.append("   Two weeks ago, convicted rapist ");
      }
      story.append(firstname);
      story.append(' ');
      story.append(lastname);
      story.append(", an inmate at ");
      story.append(facilityName);
      story.append(", overpowered ");
      story.append(hfirstname);
      story.append(' ');
      story.append(hlastname);
      story.append(" and barricaded ");
      story.append(gender == Gender.MALE ? "himself" : "herself");
      story.append(" with the guard in a prison tower.  ");
      story.append("Authorities locked down the prison and ");
      story.append("attempted to negotiate by phone for ");
      final int numdays = i.rng.nextInt(18) + 5;
      story.append(numdays);
      story.append(" days, but talks were cut short when ");
      story.append(lastname);
      story.append(" reportedly screamed into the receiver \"");
      switch (i.rng.nextInt(3)) {
      default:
        if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
          story.append("Ah, fuck this shit.  This punk bitch is fuckin' dead!");
        } else if (!i.freeSpeech()) {
          story.append("Ah, [no way.]  This [police officer will be harmed!]");
        } else {
          story.append("Ah, f*ck this sh*t.  This punk b*tch is f*ckin' dead!");
        }
        break;
      case 1:
        if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
          story.append("Fuck a muthafuckin' bull.  I'm killin' this pig shit.");
        } else if (!i.freeSpeech()) {
          story.append("[Too late.]  [I am going to harm this police officer.]");
        } else {
          story.append("F*ck a m*th*f*ck*n' bull.  I'm killin' this pig sh*t.");
        }
        break;
      case 2:
        if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
          story.append("Why the fuck am I talkin' to you?  I'd rather kill this pig.");
        } else if (!i.freeSpeech()) {
          story.append("Why [am I] talkin' to you?  I'd rather [harm this police officer.]");
        } else {
          story.append("Why the f*ck am I talkin' to you?  I'd rather kill this pig.");
        }
        break;
      }
      story.append('"');
      story.append("  The tower was breached in an attempt to reach ");
      story.append("the hostage, but ");
      story.append(lastname);
      story.append(" had already ");
      if (!i.freeSpeech()) {
        story.append("[harmed] the guard");
      } else if (i.issue(Issue.FREESPEECH).law() == Alignment.CONSERVATIVE) {
        story.append("killed the guard");
      } else {
        switch (i.rng.nextInt(3)) // TODO: More variety.
        {
        default:
          story.append("slit the guard's throat with a shank");
          break;
        case 1:
          story.append("strangled the guard to death with a knotted bed sheet");
          break;
        case 2:
          story.append("chewed out the guard's throat");
          break;
        }
      }
      story.append(".  The prisoner was beaten to death while ");
      story.append("\"resisting capture\", according to a prison spokesperson.");
      story.append('\n');
      break;
    }
    case 4:// VIEW_POLITICALVIOLENCE:
    {
      story.append(Game.cityName());
      story.append(" - Nine people were killed today as special forces brought the three-day "
          + "hostage crisis at the ");
      switch (i.rng.nextInt(5)) {
      default:
        story.append("City Hall");
        break;
      case 1:
        story.append(firstname);
        story.append(" Disco");
        break;
      case 2:
        story.append(lastname);
        story.append(" Cafeteria");
        break;
      case 3:
        story.append("Unemployment Office");
        break;
      case 4:
        story.append(lastname);
        story.append(" Orphanage");
        break;
      }
      story.append(", instigated by members of the ");
      switch (i.rng.nextInt(4)) {
      default:
        story.append("Stalinist Comrade Squad");
        break;
      case 1:
        story.append("Radical Feminist Brigades");
        break;
      case 2:
        story.append("2nd Amendment Society");
        break;
      case 3:
        story.append("Anarcho-Fascist Collective");
        break;
      }
      story
          .append(", to a bloody end. Prior to the raid, the group had taken over 80 people captive and demanded, "
              + "among other things: safe passage to North Korea, 10 billion dollars in cash, "
              + "2 fully-fueled airliners and the dissolution of the United States of America. "
              + "According to police sources, the raid became necessary after the kidnappers "
              + "revealed they had been executing twenty people at the end of each day of "
              + "non-compliance with their demands.");
      story.append('\n');
      break;
    }
    }
    News.displaynewsstory(story);
  }
}
