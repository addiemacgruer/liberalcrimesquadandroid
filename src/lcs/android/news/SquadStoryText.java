package lcs.android.news;

import static lcs.android.game.Game.*;
import lcs.android.basemode.iface.Location;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class SquadStoryText {
  static void squadstory_text_opening(final NewsStory ns, final boolean liberalguardian,
      final boolean ccs, final StringBuilder story) {
    if (ns.type == StoryType.SQUAD_SITE) {
      if (i.newscherrybusted == NewsCherryBusted.UNKNOWN && !liberalguardian) {
        if (ns.positive) {
          story.append("A group calling itself the Liberal Crime Squad ");
          story.append("burst onto the scene of political activism yesterday, according ");
          story.append("to a spokesperson from the police department.");
          story.append('\n');
        } else {
          story.append("A group of thugs calling itself the Liberal Crime Squad ");
          story.append("went on a rampage yesterday, according ");
          story.append("to a spokesperson from the police department.");
        }
      } else if (ns.positive) {
        story.append("The Liberal Crime Squad has struck again.");
        story.append('\n');
      } else {
        if (!liberalguardian) {
          story.append("The Liberal Crime Squad has gone on a rampage.");
        } else {
          story.append("A Liberal Crime Squad operation went horribly wrong.");
        }
        story.append('\n');
      }
    } else if (ns.type == StoryType.CCS_SITE) {
      if (i.newscherrybusted != NewsCherryBusted.CCS_IN_NEWS) {
        if (ns.positive && !liberalguardian) {
          story
              .append("A group of M16-wielding vigilantes calling itself the Conservative Crime Squad ");
          story.append("burst onto the scene of political activism yesterday, according ");
          story.append("to a spokesperson from the police department.");
          story.append('\n');
        } else {
          story
              .append("A group of worthless M16-toting hicks calling itself the Conservative Crime Squad ");
          story.append("went on a rampage yesterday, according ");
          story.append("to a spokesperson from the police department.");
        }
      } else if (ns.positive && !liberalguardian) {
        story.append("The Conservative Crime Squad has struck again.");
        story.append('\n');
      } else {
        story.append("The Conservative Crime Squad has gone on another rampage.");
        story.append('\n');
      }
    } else if (ns.type == StoryType.CCS_KILLED_SITE) {
      if (i.newscherrybusted != NewsCherryBusted.CCS_IN_NEWS) {
        if (ns.positive && !liberalguardian) {
          story
              .append("A group of M16-wielding vigilantes calling themselves the Conservative Crime Squad ");
          story.append("burst briefly onto the scene of political activism yesterday, according ");
          story.append("to a spokesperson from the police department. ");
          story.append('\n');
        } else {
          story.append("A group of");
          switch (i.rng.nextInt(4)) {
          default:
            story.append("pathetic,");
            break;
          case 1:
            story.append("worthless,");
            break;
          case 2:
            story.append("disheveled,");
            break;
          case 3:
            story.append("inbred,");
            break;
          }
          switch (i.rng.nextInt(3)) {
          default:
            story.append("violent,");
            break;
          case 1:
            story.append("bloodthirsty,");
            break;
          case 2:
            story.append("");
            break;
          }
          story.append("M16-toting ");
          switch (i.rng.nextInt(3)) {
          default:
            story.append("hicks");
            break;
          case 1:
            story.append("rednecks");
            break;
          case 2:
            story.append("losers");
            break;
          }
          story.append("calling themselves the Conservative Crime Squad went on a ");
          switch (i.rng.nextInt(3)) {
          default:
            story.append("suicidal");
            break;
          case 1:
            story.append("homicidal");
            break;
          case 2:
            story.append("bloodthirsty");
            break;
          }
          story
              .append(" rampage yesterday, according to a spokesperson from the police department.");
          story.append('\n');
        }
      } else if (ns.positive && !liberalguardian) {
        story.append("The Conservative Crime Squad has struck again, albeit with a tragic end.");
        story.append('\n');
      } else {
        story
            .append("The Conservative Crime Squad has gone on another rampage, and they got what they deserved.");
        story.append('\n');
      }
    } else if (i.newscherrybusted == NewsCherryBusted.UNKNOWN && !liberalguardian) {
      if (ns.positive) {
        story.append("A group calling itself the Liberal Crime Squad ");
        story.append("burst briefly onto the scene of political activism yesterday, according ");
        story.append("to a spokesperson from the police department.");
        story.append('\n');
      } else {
        story.append("A group of thugs calling itself the Liberal Crime Squad ");
        story.append("went on a suicidal rampage yesterday, according ");
        story.append("to a spokesperson from the police department.");
        story.append('\n');
      }
    } else if (ns.positive) {
      story.append("The Liberal Crime Squad has struck again, albeit with a tragic end.");
      story.append('\n');
    } else {
      if (!liberalguardian) {
        story
            .append("The Liberal Crime Squad has gone on a rampage, and they got what they deserved.");
      } else {
        story
            .append("A Liberal Crime Squad operation went horribly wrong, and came to a tragic end.");
      }
      story.append('\n');
    }
    squadstory_text_location(ns, liberalguardian, ccs, story);
    if (ns.type == StoryType.SQUAD_KILLED_SITE) {
      if (liberalguardian) {
        story.append("Unfortunately, the LCS group was defeated by the forces of evil.");
      } else if (ns.positive) {
        story.append("Everyone in the LCS group was arrested or killed.");
      } else {
        story.append("Fortunately, the LCS thugs were stopped by brave citizens.");
      }
    }
    if (ns.type == StoryType.CCS_KILLED_SITE) {
      if (ns.positive && !liberalguardian) {
        story.append("Everyone in the CCS group was arrested or killed.");
      } else {
        story.append("Fortunately, the CCS thugs were stopped by brave citizens.");
      }
    }
    story.append('\n');
  }

  private static void squadstory_text_location(final NewsStory ns, final boolean liberalguardian,
      final boolean ccs, final StringBuilder story) {
    story.append("  The events took place at the ");
    if (liberalguardian && !ccs) {
      story.append("notorious ");
    }
    final Location location = ns.location();
    if (location != null && ccs) {
      story.append(location.type().ccsSiteName());
    } else if (location != null) {
      story.append(location.toString());
    }
    if (location != null && liberalguardian && !ccs) {
      story.append(location.type().lcsSiteOpinion());
    } else if (!ccs) {
      story.append('.');
    }
  }
}
