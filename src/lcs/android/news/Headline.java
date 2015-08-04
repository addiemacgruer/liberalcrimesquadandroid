package lcs.android.news;

import static lcs.android.game.Game.*;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class Headline {
  static void displaystoryheader(final NewsStory ns, final boolean liberalguardian,
      final Issue header) {
    switch (ns.type) {
    case NUDITYARREST:
    case CARTHEFT:
    case WANTEDARREST:
    case DRUGARREST:
    case GRAFFITIARREST:
    case BURIALARREST:
      News.displaycenterednewsfont("POLICE KILLED");
      break;
    case SQUAD_ESCAPED:
    case SQUAD_FLEDATTACK:
      News.displaycenterednewsfont("LCS ESCAPES");
      News.displaycenterednewsfont("POLICE SIEGE");
      break;
    case SQUAD_DEFENDED:
    case SQUAD_BROKESIEGE:
      News.displaycenterednewsfont("LCS FIGHTS");
      News.displaycenterednewsfont("OFF COPS");
      break;
    case SQUAD_KILLED_SIEGEATTACK:
    case SQUAD_KILLED_SIEGEESCAPE:
      if (!liberalguardian) {
        News.displaycenterednewsfont("LCS SIEGE");
        News.displaycenterednewsfont("TRAGIC END");
      } else {
        News.displaycenterednewsfont("POLICE KILL");
        News.displaycenterednewsfont("LCS MARTYRS");
      }
      break;
    case CCS_SITE:
    case CCS_KILLED_SITE:
      if (i.newscherrybusted != NewsCherryBusted.CCS_IN_NEWS) {
        News.displaycenterednewsfont("CONSERVATIVE");
        News.displaycenterednewsfont("CRIME SQUAD");
      } else if (ns.positive) {
        News.displaycenterednewsfont("CCS STRIKES");// AGAIN?
      } else {
        News.displaycenterednewsfont("CCS RAMPAGE");
      }
      break;
    default:
      if (ns.positive) {
        if (i.newscherrybusted != NewsCherryBusted.UNKNOWN || liberalguardian) {
          if (!liberalguardian) {
            if (ns.priority > 250) {
              News.displaycenterednewsfont("UNSTOPPABLE");
            } else {
              News.displaycenterednewsfont("LCS STRIKES");
            }
          } else if (ns.priority > 150) {
            i.issue(header).changeOpinion(5, 1, 100); // Bonus
            // for
            // big story
            switch (header) {
            case TAX:
            case LABOR:
            case CEOSALARY:
              News.displaycenterednewsfont("CLASS WAR");
              break;
            case NUCLEARPOWER:
              News.displaycenterednewsfont("NO NUKE POWER");
              break;
            case POLICEBEHAVIOR:
              News.displaycenterednewsfont("LCS VS COPS");
              break;
            case DEATHPENALTY:
              News.displaycenterednewsfont("PRISON WAR");
              break;
            case PRIVACY:
              News.displaycenterednewsfont("LCS VS CIA");
              break;
            case ANIMALRESEARCH:
            case GENETICS:
              News.displaycenterednewsfont("EVIL RESEARCH");
              break;
            case FREESPEECH:
            case GAY:
            case JUSTICES:
              News.displaycenterednewsfont("NO JUSTICE");
              break;
            case POLLUTION:
              News.displaycenterednewsfont("POLLUTER HIT");
              break;
            case CORPORATECULTURE:
              News.displaycenterednewsfont("LCS HITS CORP");
              break;
            case AMRADIO:
              News.displaycenterednewsfont("LCS HITS AM");
              break;
            case CABLENEWS:
              News.displaycenterednewsfont("LCS HITS TV");
              break;
            default:
              News.displaycenterednewsfont("HEROIC STRIKE");
            }
          } else {
            News.displaycenterednewsfont("LCS STRIKES");
          }
        } else {
          News.displaycenterednewsfont("LIBERAL CRIME");
          News.displaycenterednewsfont("SQUAD STRIKES");
        }
      } else if (i.newscherrybusted != NewsCherryBusted.UNKNOWN || liberalguardian) {
        if (!liberalguardian) {
          News.displaycenterednewsfont("LCS RAMPAGE");
        } else {
          News.displaycenterednewsfont("LCS SORRY");
        }
      } else {
        News.displaycenterednewsfont("LIBERAL CRIME");
        News.displaycenterednewsfont("SQUAD RAMPAGE");
      }
      break;
    }
  }
}
