package lcs.android.news;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import lcs.android.activities.BareActivity;
import lcs.android.activities.iface.Activity;
import lcs.android.basemode.iface.Compound;
import lcs.android.basemode.iface.CrimeSquad;
import lcs.android.creature.Creature;
import lcs.android.creature.CreatureName;
import lcs.android.creature.skill.Skill;
import lcs.android.game.CheckDifficulty;
import lcs.android.game.Game;
import lcs.android.law.Crime;
import lcs.android.monthly.EndGame;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.Color;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault class News {
  /* news - major newspaper reporting on lcs and other topics */
  public static void majornewspaper() {
    if (i.endgameState.ordinal() < EndGame.CCS_DEFEATED.ordinal()
        && i.rng.nextInt(30) < i.endgameState.ordinal()) {
      ccsStrikes();
    }
    if (i.rng.chance(60)) {
      majorEvent();
    }
    deleteLowContent();
    // DO TELEVISION AND OTHER NON-NEWS STORIES, THEN DELETE THEM
    if (i.newsStories.size() > 0) {
      televisionStories();
    }
    // ASSIGN PAGE NUMBERS TO STORIES BASED ON THEIR PRIORITY
    for (final Iterator<NewsStory> iterator = i.newsStories.iterator(); iterator.hasNext();) {
      final NewsStory n = iterator.next();
      n.setpriority();
      // Suppress squad actions that aren't worth a story
      if (n.type == StoryType.SQUAD_SITE && (n.priority < 50 && n.isClaimed() || n.priority < 4)) {
        iterator.remove();
        continue;
      }
    }
    if (i.newsStories.isEmpty())
      return;
    // Sort the major newspapers
    Collections.sort(i.newsStories);
    // DISPLAY PAPER
    final NewsStory topNews = i.newsStories.get(0);
    boolean liberalguardian = false;
    Issue header = null;
    if (writingSkill() > 0 && topNews.type != StoryType.MAJOREVENT) {
      liberalguardian = true;
    }
    switch (topNews.type) {
    case SQUAD_SITE:
    case SQUAD_KILLED_SITE:
      if (!topNews.location().exists()) {
        break;
      }
      header = topNews.location().get().type().topNews();
      break;
    case SQUAD_ESCAPED:
    case SQUAD_FLEDATTACK:
    case SQUAD_DEFENDED:
    case SQUAD_BROKESIEGE:
    case SQUAD_KILLED_SIEGEATTACK:
    case SQUAD_KILLED_SIEGEESCAPE:
      break;
    default:
      break;
    }
    if (liberalguardian) {
      if (topNews.type == StoryType.CCS_SITE || topNews.type == StoryType.CCS_KILLED_SITE) {
        topNews.positive = false;
      }
      displaystory(topNews, true, header);
    } else {
      displaystory(topNews, false, null);
    }
    Issue.randomissueinit(true);
    for (final Creature p : i.pool) {
      // Letters to the editor
      if (p.activity().type() == Activity.WRITE_LETTERS) {
        if (p.skill().skillCheck(Skill.WRITING, CheckDifficulty.EASY)) {
          final Issue issue = Issue.randomissue();
          i.issue(issue).addBackgroundInfluence(5);
        }
        p.skill().train(Skill.WRITING, i.rng.nextInt(5) + 1);
      }
      // Guardian Essays
      // Basically letters to the editor, but thrice as potent, and can
      // backfire
      if (p.activity().type() == Activity.WRITE_GUARDIAN) {
        final Issue issue = Issue.randomissue();
        if (p.skill().skillCheck(Skill.WRITING, CheckDifficulty.EASY)) {
          i.issue(issue).addBackgroundInfluence(15);
        } else {
          i.issue(issue).addBackgroundInfluence(-15);
        }
        p.skill().train(Skill.WRITING, i.rng.nextInt(5) + 1);
      }
    }
    // CHANGE FOR SQUAD ACTS PUBLIC OPINION BASED ON PAGE NUMBERS
    // AND OVERALL POWER OF THE STORY
    int power;
    for (final NewsStory n : i.newsStories) {
      if (n.type == StoryType.SQUAD_SITE || n.type == StoryType.SQUAD_ESCAPED
          || n.type == StoryType.SQUAD_FLEDATTACK || n.type == StoryType.SQUAD_DEFENDED
          || n.type == StoryType.SQUAD_BROKESIEGE || n.type == StoryType.SQUAD_KILLED_SIEGEATTACK
          || n.type == StoryType.SQUAD_KILLED_SIEGEESCAPE || n.type == StoryType.SQUAD_KILLED_SITE
          || n.type == StoryType.WANTEDARREST || n.type == StoryType.GRAFFITIARREST
          || n.type == StoryType.CCS_SITE || n.type == StoryType.CCS_KILLED_SITE) {
        power = n.priority;
        // PAGE BONUS
        if (n.page == 1) {
          power *= 5;
        } else if (n.page == 2) {
          power *= 3;
        } else if (n.page == 3) {
          power *= 2;
        }
        int maxpower;
        if (n.page == 1) {
          maxpower = 100;
        } else if (n.page < 5) {
          maxpower = 100 - 10 * n.page;
        } else if (n.page < 10) {
          maxpower = 40;
        } else if (n.page < 20) {
          maxpower = 20;
        } else if (n.page < 30) {
          maxpower = 10;
        } else if (n.page < 40) {
          maxpower = 5;
        } else {
          maxpower = 1;
        }
        // Five times effectiveness with the Liberal Guardian
        if (n.positive) {
          power *= 5;
        }
        if (power > maxpower) {
          power = maxpower;
        }
        power /= 10;
        power++;
        int colored = 0;
        if (!(n.type == StoryType.CCS_SITE) && !(n.type == StoryType.CCS_KILLED_SITE)) {
          i.issue(Issue.LIBERALCRIMESQUAD).changeOpinion(2 + power, 1, 100);
          if (n.positive) {
            colored = 1;
          } else {
            power = -power;
          }
          final int power1 = power;
          i.issue(Issue.LIBERALCRIMESQUADPOS).changeOpinion(power1, 1, 100);
        }
        if (n.type == StoryType.CCS_SITE || n.type == StoryType.CCS_KILLED_SITE) {
          if (n.positive) {
            colored = -1;
            power = -power;
          }
          final int power1 = power;
          i.issue(Issue.CONSERVATIVECRIMESQUAD).changeOpinion(power1, 0, 100);
        }
        i.issue(Issue.GUNCONTROL).changeOpinion(Math.abs(power) / 10, 0, Math.abs(power) * 10);
        if (n.location().exists()) {
          for (final Issue v : n.location().get().type().opinionsChanged()) {
            final int power1 = power;
            final int aAffect = colored;
            i.issue(v).changeOpinion(power1, aAffect, power * 10);
          }
        }
      }
    }
    i.newsStories.clear();
  }

  public static String newspaperName() {
    return i.rng.choice("Daily", "Nightly", "Current", "Pressing", "Socialist", "American",
        "National", "Union", "Foreign", "Associated", "International", "County")
        + " "
        + i.rng.choice("Reporter", "Issue", "Take", "Constitution", "Times", "Post", "News",
            "Affair", "Statesman", "Star", "Inquirer");
  }

  static void displaycenterednewsfont(final String str) {
    ui().text(str).color(Color.BLACKBLACK).bold().center().headline().add();
  }

  static void displaycenteredsmallnews(final String str) {
    ui().text(str).color(Color.BLACKBLACK).bold().center().add();
  }

  static void displaynewspicture(@SuppressWarnings("unused") final MajorEvent.Picture picture) {
    // TODO adapt for android
  }

  /* news - draws the specified block of text to the screen */
  static void displaynewsstory(final StringBuilder story) {
    ui().text(story.toString()).color(Color.BLACKBLACK).add();
    story.setLength(0);
  }

  private static void ccsStrikes() {
    // 10% chance of CCS squad wipe
    final NewsStory ns = new NewsStory(i.rng.chance(10) ? StoryType.CCS_SITE
        : StoryType.CCS_KILLED_SITE);
    // 20% chance of rampage
    ns.positive = i.rng.chance(5);
    do {
      ns.location(i.rng.randFromList(i.location));
    } while (ns.location().get().renting() != null);
    i.newsStories.add(ns);
  }

  private static Map<NewsEvent, Integer> crimeListToMap(final NewsStory ns) {
    final Map<NewsEvent, Integer> crime = new EnumMap<NewsEvent, Integer>(NewsEvent.class);
    for (final NewsEvent c : NewsEvent.values()) {
      crime.put(c, 0);
    }
    for (final NewsEvent c : ns.crimes) {
      crime.put(c, crime.get(c) + 1);
    }
    return crime;
  }

  private static void deleteLowContent() {
    // DELETE STORIES THAT HAVE NO CONTENT
    for (final Iterator<NewsStory> iterator = i.newsStories.iterator(); iterator.hasNext();) {
      final NewsStory n = iterator.next();
      if (n.type == StoryType.SQUAD_SITE && n.crimeCount() == 0) {
        iterator.remove();
        continue;
      }
      if (n.type == StoryType.CARTHEFT || n.type == StoryType.NUDITYARREST
          || n.type == StoryType.WANTEDARREST || n.type == StoryType.DRUGARREST
          || n.type == StoryType.GRAFFITIARREST || n.type == StoryType.BURIALARREST) {
        boolean conf = false;
        for (int c = 0; c < n.crimes.size(); c++) {
          if (n.crimes.get(c) == NewsEvent.KILLED_SOMEBODY) {
            conf = true;
            break;
          }
        }
        if (!conf) {
          iterator.remove();
          continue;
        }
      }
      // Suppress news about sieges that aren't police actions
      if ((n.type == StoryType.SQUAD_ESCAPED || n.type == StoryType.SQUAD_FLEDATTACK
          || n.type == StoryType.SQUAD_DEFENDED || n.type == StoryType.SQUAD_BROKESIEGE
          || n.type == StoryType.SQUAD_KILLED_SIEGEATTACK || n.type == StoryType.SQUAD_KILLED_SIEGEESCAPE)
          && n.siegetype != CrimeSquad.POLICE) {
        iterator.remove();
        continue;
      }
    }
  }

  /* news - show major news story */
  private static void displaystory(final NewsStory ns, final boolean liberalguardian,
      final Issue header) {
    Layout.preparepage(ns, liberalguardian);
    final StringBuilder story = new StringBuilder();
    Ads.displayads(ns, liberalguardian);
    switch (ns.type) {
    case MAJOREVENT:
      MajorEvent.displaymajoreventstory(ns, story);
      break;
    case SQUAD_SITE:
    case SQUAD_ESCAPED:
    case SQUAD_FLEDATTACK:
    case SQUAD_DEFENDED:
    case SQUAD_BROKESIEGE:
    case SQUAD_KILLED_SIEGEATTACK:
    case SQUAD_KILLED_SIEGEESCAPE:
    case SQUAD_KILLED_SITE:
    case CCS_SITE:
    case CCS_KILLED_SITE:
    case CARTHEFT:
    case NUDITYARREST:
    case WANTEDARREST:
    case DRUGARREST:
    case GRAFFITIARREST:
    case BURIALARREST: {
      if (!liberalguardian && ns.page == 1 || liberalguardian /* && NewsStory.guardianpage == 1 */) {
        Headline.displaystoryheader(ns, liberalguardian, header);
      }
      story.setLength(0);
      story.append(i.homeCityName);
      story.append(" - ");
      switch (ns.type) {
      case WANTEDARREST:
      case GRAFFITIARREST: {
        final Map<NewsEvent, Integer> crime = crimeListToMap(ns);
        if (crime.get(NewsEvent.KILLED_SOMEBODY) > 1) {
          if (crime.get(NewsEvent.KILLED_SOMEBODY) == 2) {
            story.append("Two");
          } else {
            story.append("Several");
          }
          story.append(" police officers were");
        } else {
          story.append("A police officer was");
        }
        story.append(" killed in the line of duty yesterday, ");
        story.append("according to a spokesperson from the police department.");
        story.append('\n');
        story.append("  A suspect, identified only as a member of the ");
        story
            .append("radical political group known as the Liberal Crime Squad, is believed to have killed ");
        if (crime.get(NewsEvent.KILLED_SOMEBODY) > 1) {
          story.append(crime.get(NewsEvent.KILLED_SOMEBODY));
          story.append(" officers ");
        } else {
          story.append("the police officer ");
        }
        story.append(" while they were attempting to perform an arrest.  ");
        story
            .append("The names of the officers have not been released pending notification of their families.");
        story.append('\n');
        break;
      }
      case NUDITYARREST:
      case CARTHEFT:
      case DRUGARREST:
      case BURIALARREST: {
        final Map<NewsEvent, Integer> crime = crimeListToMap(ns);
        story.append("A routine arrest went horribly wrong yesterday, ");
        story.append("according to a spokesperson from the police department.");
        story.append('\n');
        story.append("  A suspect, whose identify is unclear, ");
        story.append("killed ");
        if (crime.get(NewsEvent.KILLED_SOMEBODY) > 1) {
          story.append(crime.get(NewsEvent.KILLED_SOMEBODY));
          story.append(" police officers that were");
        } else {
          story.append("a police officer that was");
        }
        story.append(" attempting to perform an arrest.  ");
        if (ns.type == StoryType.NUDITYARREST) {
          story
              .append("The incident apparently occurred as a response to a public nudity complaint.  ");
        } else if (ns.type == StoryType.DRUGARREST) {
          story.append("The suspect was allegedly selling \"pot brownies\".  ");
        } else if (ns.type == StoryType.BURIALARREST) {
          story
              .append("A passerby allegedly called the authorities after seeing the suspect dragging what ");
          story.append("appeared to be a corpse through an empty lot.  ");
        } else {
          story.append("A passerby had allegedly spotted the suspect committing a car theft.  ");
        }
        if (crime.get(NewsEvent.KILLED_SOMEBODY) > 1) {
          story
              .append("The names of the officers have not been released pending notification of their families.");
        } else {
          story
              .append("The name of the officer has not been released pending notification of the officer's family.");
        }
        story.append('\n');
        break;
      }
      case SQUAD_ESCAPED:
        story.append("Members of the Liberal Crime Squad ");
        story.append("escaped from a police siege yesterday, according ");
        if (!liberalguardian) {
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      case SQUAD_FLEDATTACK:
        story.append("Members of the Liberal Crime Squad ");
        story.append("escaped from police officers during a raid yesterday, according ");
        if (!liberalguardian) {
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      case SQUAD_DEFENDED:
        story.append("Members of the Liberal Crime Squad ");
        story.append("fought off a police raid yesterday, according ");
        if (!liberalguardian) {
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      case SQUAD_BROKESIEGE:
        story.append("Members of the Liberal Crime Squad ");
        story.append("violently broke a police siege yesterday, according ");
        if (!liberalguardian) {
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      case SQUAD_KILLED_SIEGEATTACK:
        story.append("Members of the Liberal Crime Squad were ");
        if (!liberalguardian) {
          story.append("slain during a police raid yesterday, according ");
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("murdered during a police raid yesterday, according ");
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      case SQUAD_KILLED_SIEGEESCAPE:
        story.append("Members of the Liberal Crime Squad were ");
        if (!liberalguardian) {
          story.append("slain trying to escape from a police siege yesterday, according ");
          story.append("to a spokesperson from the police department.");
        } else {
          story.append("murdered trying to escape from a police siege yesterday, according ");
          story.append("to a Liberal Crime Squad spokesperson.");
        }
        story.append('\n');
        break;
      default: {
        boolean ccs = false;
        if (ns.type == StoryType.CCS_KILLED_SITE || ns.type == StoryType.CCS_SITE) {
          ccs = true;
        }
        SquadStoryText.squadstory_text_opening(ns, liberalguardian, ccs, story);
        final Map<NewsEvent, Integer> crime = crimeListToMap(ns);
        int typesum = 0;
        for (final NewsEvent c : NewsEvent.values()) {
          // Count crimes of each type
          if (crime.get(c) == 0) {
            continue;
          }
          // Special crimes are described at the start or end
          // of the
          // article;
          // others should be recorded in the body
          switch (c) {
          case HOUSE_PHOTOS:
          case CORP_FILES:
          case SHUTDOWN_REACTOR:
          case POLICE_LOCKUP:
          case COURTHOUSE_LOCKUP:
          case PRISON_RELEASE:
          case JURY_TAMPERING:
          case HACK_INTEL:
          case ARMY_ARMORY:
          case CARCHASE:
          case CARCRASH:
          case FOOT_CHASE:
            continue;
          default:
            typesum++;
          }
        }
        if (crime.get(NewsEvent.SHUTDOWN_REACTOR) > 0) {
          if (i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
            if (!liberalguardian) {
              story.append("  According to sources that were at the scene, ");
              story.append("the Liberal Crime Squad contaminated the state's water supply");
              story.append("yesterday by tampering with equipment on the site.");
              story.append('\n');
            } else {
              story
                  .append("  The Liberal Crime Squad tampered with the state's water supply yesterday, ");
              story.append("demonstrating the extreme dangers of Nuclear Waste. ");
              story.append('\n');
            }
          } else if (!liberalguardian) {
            story.append("  According to sources that were at the scene, ");
            story.append("the Liberal Crime Squad caused the power out that struck the state ");
            story.append("yesterday by tampering with equipment on the site.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad caused the power outage that struck the state yesterday, ");
            story
                .append("demonstrating the extreme vulnerability and danger of Nuclear Power Plants. ");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.POLICE_LOCKUP) > 0) {
          if (!liberalguardian) {
            story.append("  According to sources that were at the scene, ");
            story
                .append("the Liberal Crime Squad allegedly freed or attempted to free prisoners from the police lockup.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad attempted to rescue innocent people from the police lockup, ");
            story
                .append("saving them from torture and brutality at the hands of Conservative police interrogators.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.COURTHOUSE_LOCKUP) > 0) {
          if (!liberalguardian) {
            story.append("  According to sources that were at the scene, ");
            story
                .append("the Liberal Crime Squad allegedly freed or attempted to free prisoners from the court house lockup.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad attempted to rescue innocent people from the court house lockup, ");
            story.append("saving them from the highly corrupt Conservative justice system.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.PRISON_RELEASE) > 0) {
          if (!liberalguardian) {
            story.append("  According to sources that were at the scene, ");
            story
                .append("the Liberal Crime Squad allegedly freed prisoners while in the facility.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad attempted to rescue innocent people from the abusive Conservative conditions ");
            story.append("at the prison.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.JURY_TAMPERING) > 0) {
          if (!liberalguardian) {
            story.append("  According to police sources that were at the scene, ");
            story.append("the Liberal Crime Squad allegedly violated the sacred ");
            story.append("trust and attempted to influence a jury.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad has apologized over reports that the operation ");
            story.append("may have interfered with jury deliberations.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.HACK_INTEL) > 0) {
          if (!liberalguardian) {
            story.append("  According to police sources that were at the scene, ");
            story.append("intelligence officials seemed very nervous about something.");
            story.append('\n');
          } else {
            story
                .append("  Liberal Crime Squad computer specialists worked to liberate information from CIA computers.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.ARMY_ARMORY) > 0) {
          if (!liberalguardian) {
            story.append("  According to a military spokesperson, ");
            story.append("the Liberal Crime Squad attempted to break into the armory.");
            story.append('\n');
          } else {
            story
                .append("  Liberal Crime Squad infiltration specialists worked to liberate weapons from the oppressors.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.HOUSE_PHOTOS) > 0) {
          if (!liberalguardian) {
            story.append("  According to police sources that were at the scene, ");
            story.append("the owner of the house seemed very frantic about some missing property.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad was attempting to uncover the CEO's Conservative corruption.");
            story.append('\n');
          }
        }
        if (crime.get(NewsEvent.CORP_FILES) > 0) {
          if (!liberalguardian) {
            story.append("  According to police sources that were at the scene, ");
            story.append("executives on the scene seemed very nervous about something.");
            story.append('\n');
          } else {
            story
                .append("  The Liberal Crime Squad was attempting to uncover the company's Conservative corruption.");
            story.append('\n');
          }
        }
        if (liberalguardian && !ccs) {
          if (crime.get(NewsEvent.ATTACKED_MISTAKE) > 0) {
            typesum--;
          }
          if (crime.get(NewsEvent.KILLED_SOMEBODY) > 0) {
            typesum--;
          }
        }
        if (typesum > 0) {
          if (typesum > 0) {
            if (!ccs) {
              if (!liberalguardian) {
                story
                    .append("  Further details are sketchy, but police sources suggest that the LCS ");
                story.append("engaged in ");
              } else {
                story.append("  The Liberal Crime Squad ");
              }
            } else {
              story
                  .append("  Further details are sketchy, but police sources suggest that the CCS ");
              story.append("engaged in ");
            }
            if (crime.get(NewsEvent.ARSON) > 0) {
              if (!liberalguardian || ccs) {
                story.append("arson");
              } else {
                story.append("set fire to Conservative property");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (!liberalguardian || ccs) {
              if (crime.get(NewsEvent.KILLED_SOMEBODY) > 0) {
                story.append("murder");
                if (typesum > 2) {
                  story.append(", ");
                } else if (typesum == 2) {
                  story.append(" and ");
                }
                typesum--;
              }
              if (crime.get(NewsEvent.ATTACKED_MISTAKE) > 0) {
                story.append("violence");
                if (typesum > 2) {
                  story.append(", ");
                } else if (typesum == 2) {
                  story.append(" and ");
                }
                typesum--;
              }
              if (crime.get(NewsEvent.ATTACKED) > 0) {
                if (crime.get(NewsEvent.ATTACKED_MISTAKE) > 0) {
                  story.append("more violence");
                } else {
                  story.append("violence");
                }
                if (typesum > 2) {
                  story.append(", ");
                } else if (typesum == 2) {
                  story.append(" and ");
                }
                typesum--;
              }
            } else if (crime.get(NewsEvent.ATTACKED) > 0) {
              story.append("engaged in combat with Conservative forces");
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.STOLE_GROUND) > 0) {
              if (!liberalguardian || ccs) {
                story.append("theft");
              } else {
                story.append("liberated enemy resources");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.FREE_RABBITS) > 0 || crime.get(NewsEvent.FREE_BEASTS) > 0) {
              if (!liberalguardian) {
                story.append("tampering with lab animals");
              } else {
                story.append("liberated abused animals");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.BREAK_SWEATSHOP) > 0 || crime.get(NewsEvent.BREAK_FACTORY) > 0) {
              if (!liberalguardian || ccs) {
                story.append("destruction of private property");
              } else {
                story.append("damaged enemy infrastructure");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.TAGGING) > 0) {
              if (!liberalguardian || ccs) {
                story.append("vandalism");
              } else {
                story.append("marked the site for Liberation");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.BROKE_DOWN_DOOR) > 0) {
              if (!liberalguardian || ccs) {
                story.append("breaking and entering");
              } else {
                story.append("infiltration of a conservative hot spot");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            if (crime.get(NewsEvent.UNLOCKED_DOOR) > 0) {
              if (!liberalguardian || ccs) {
                story.append("unlawful entry");
              } else {
                story.append("evaded Conservative security measures");
              }
              if (typesum > 2) {
                story.append(", ");
              } else if (typesum == 2) {
                story.append(" and ");
              }
              typesum--;
            }
            story.append('.');
          }
          story.append('\n');
        }
        if (crime.get(NewsEvent.CARCHASE) > 0) {
          if (!liberalguardian || ccs) {
            story.append("  It is known that there was a high-speed chase ");
            story.append("following the incident.  ");
          } else {
            story.append("  Conservative operatives engaged in a reckless ");
            story.append("pursuit of the LCS.  ");
          }
          if (crime.get(NewsEvent.CARCRASH) > 0) {
            if (crime.get(NewsEvent.CARCRASH) > 1) {
              story.append(crime.get(NewsEvent.CARCRASH));
              story.append(" vehicles crashed.  ");
            } else {
              story.append("One vehicle crashed.  ");
            }
            if (!liberalguardian || ccs) {
              story.append("Details about injuries were not released.  ");// XXX:
              // Why
              // not
              // turn
              // them
              // into
              // martyrs?
            }
          }
          if (crime.get(NewsEvent.FOOT_CHASE) > 0) {
            if (!liberalguardian || ccs) {
              story
                  .append("There was also a foot chase when the suspect or suspects bailed out after the high-speed pursuit.  ");
            } else {
              story
                  .append("The Liberal Crime Squad ended the dangerous high-speed chase in order to protect the public, and attempted to escape on foot.  ");
            }
          }
          story.append('\n');
        }
        if (!ccs) {
          if (i.rng.likely(8)) {
            if (crime.get(NewsEvent.TAGGING) > 0) {
              story.append("  The slogan, \"");
              story.append(i.score.slogan);
              story.append("\" was found painted on the walls.");
            } else {
              switch (i.rng.nextInt(3)) {
              default:
                if (ns.type == StoryType.SQUAD_KILLED_SITE) {
                  story.append("  One uttered the words, \"");
                  story.append(i.score.slogan);
                  story.append("\" before passing out.");
                } else {
                  story.append("  As they left, they shouted, \"");
                  story.append(i.score.slogan);
                  story.append('"');
                }
                break;
              case 1:
                story.append("  One of them was rumored to have cried out, \"");
                story.append(i.score.slogan);
                story.append('"');
                break;
              case 2:
                story.append("  Witnesses reported hearing the phrase, \"");
                story.append(i.score.slogan);
                story.append('"');
                break;
              }
            }
          }
        }
        break;
      }
      }
      if (i.newscherrybusted == NewsCherryBusted.UNKNOWN) {
        i.newscherrybusted = NewsCherryBusted.LCS_IN_NEWS;
      }
      if (ns.type == StoryType.CCS_SITE || ns.type == StoryType.CCS_KILLED_SITE) {
        i.newscherrybusted = NewsCherryBusted.CCS_IN_NEWS;
      }
      break;
    }
    case MASSACRE: {
      // int y = 3;
      if (ns.page == 1) {
        // y = 21;
        if (ns.siegetype == CrimeSquad.CCS) {
          displaycenterednewsfont("CCS MASSACRE");
        } else if (!liberalguardian) {
          displaycenterednewsfont("MYSTERIOUS");
          displaycenterednewsfont("MASSACRE");
        } else {
          displaycenterednewsfont("CONSERVATIVE");
          displaycenterednewsfont("MASSACRE");
        }
      }
      story.setLength(0);
      story.append(i.homeCityName);
      story.append(" - ");
      if (ns.count > 2) {
        story.append(ns.count);
        story.append(" bodies were "); // Gruesome pile, large pile.
      } else if (ns.count > 1) {
        story.append(" Two bodies were ");
      } else {
        story.append(" A body was ");
      }
      story.append(" found");
      if (ns.location().exists()) {
        story.append("in the ");
        story.append(ns.location().get().toString());
      }
      story.append(" yesterday.");
      if (!liberalguardian) {
        story.append("  According to a spokesperson for ");
        story.append("the police department, the matter is under investigation as a homicide.");
        story.append('\n');
        story
            .append("  Privately, sources in the department confide that there aren't any leads.  ");
        story.append("According to one person familiar with the case, \"");
      } else {
        story.append("  The police have opened an investigation into the massacre, but seem ");
        story.append("unwilling to pursue the case with any serious effort.");
        story.append('\n');
        story.append("  The Liberal Crime Squad has claimed that the ");
        if (ns.count > 1) {
          story.append("victims were members ");
        } else {
          story.append("victim was a member ");
        }
        story.append("of the LCS targeted simply due to their political beliefs.  ");
        story.append("According to an LCS spokesperson, \"");
      }
      switch (ns.siegetype) {
      case CIA:
        if (!liberalguardian) {
          if (ns.count > 1) {
            story.append("The bodies had no faces or ");
          } else {
            story.append("The body had no face or ");
          }
          story.append("fingerprints.  Like, it was all smooth.  ");
          if (!i.freeSpeech()) {
            story.append("[Craziest] thing I've ever seen");
          } else if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story.append("Damnedest thing I've ever seen");
          } else {
            story.append("D*mnd*st thing I've ever seen");
          }
        } else {
          story.append("We have strong evidence that this was an extra-judicial slaughter ");
          story.append("carried out by the Central Intelligence Agency in retaliation for our ");
          story.append("previous actions to uncover human rights abuses and corruption in the ");
          story.append("intelligence community");
        }
        break;
      case POLICE:
      case HICKS:
        if (!liberalguardian) {
          story.append("Burned...  stabbed with, maybe, pitchforks.  There may have ");
          story.append("been bite marks.  Nothing recognizable left.  Complete carnage.");
        } else {
          story.append("We have reason to believe that this brutal massacre was ");
          story.append("inspired by the Conservative media's brainwashing propaganda");
        }
        break;
      case CORPORATE:
        if (!liberalguardian) {
          story.append("It was execution style.  Professional.  We've got nothing");
        } else {
          story.append("This massacre has the signature mark of a group of mercenaries ");
          story.append("known to work with several corporations we've had confrontations ");
          story.append("with in the past.  *When* the police can't figure this one out, they're ");
          story.append("just covering it up");
        }
        break;
      case CCS:
        if (!liberalguardian) {
          story.append("Look, it was a Conservative Crime Squad hit, that's all we know, ");
          story.append("no names, no faces, not even where it happened really");
        } else {
          story.append("This is the doing of the Conservative Crime Squad butchers.  ");
          story.append("They have to be stopped before they kill again");
        }
        break;
      case FIREMEN:
        if (!liberalguardian) {
          if (ns.count > 1) {
            story.append("The recovered bodies were ");
          } else {
            story.append("The recovered body was ");
          }
          story.append("burned unrecognizable.  ");
          story.append("Scorch marks throughout the site indicate that this was no accident; ");
          story
              .append("we are working closely with the Fire Department to track down the arsonist.  ");
          story
              .append("Fortunately, firemen were able to respond before the fire could spread to other buildings");
        } else {
          if (ns.count > 1) {
            story.append("The murdered were reporters ");
          } else {
            story.append("The murdered was a reporter ");
          }
          story.append("working for this very paper. ");
          story
              .append("This is clearly the work of conservative butchers enforcing the prohibition on a free press");
        }
        break;
      default:
        break;
      }
      story.append(".\"  ");
      story.append('\n');
      break;
    }
    case KIDNAPREPORT: {
      // int y = 2;
      if (ns.page == 1) {
        // y = 21;
        if (liberalguardian) {
          displaycenterednewsfont("LCS DENIES");
          displaycenterednewsfont("KIDNAPPING");
          break;
        } else if (ns.creature().type().idName().equals("CORPORATE_CEO")) {
          displaycenterednewsfont("CEO");
          displaycenterednewsfont("KIDNAPPED");
        } else if (ns.creature().type().idName().equals("RADIOPERSONALITY")) {
          displaycenterednewsfont("RADIO HOST");
          displaycenterednewsfont("KIDNAPPED");
        } else if (ns.creature().type().idName().equals("NEWSANCHOR")) {
          displaycenterednewsfont("NEWS ANCHOR");
          displaycenterednewsfont("KIDNAPPED");
        } else if (ns.creature().type().idName().equals("SCIENTIST_EMINENT")) {
          displaycenterednewsfont("SCIENTIST");
          displaycenterednewsfont("KIDNAPPED");
        } else if (ns.creature().type().idName().equals("JUDGE_CONSERVATIVE")) {
          displaycenterednewsfont("JUDGE");
          displaycenterednewsfont("KIDNAPPED");
        } else if (ns.creature().type().isPolice()) {
          displaycenterednewsfont("COP");
          displaycenterednewsfont("KIDNAPPED");
        } else {
          displaycenterednewsfont("SOMEONE");
          displaycenterednewsfont("KIDNAPPED");
        }
      }
      story.setLength(0);
      story.append(i.homeCityName);
      story.append(" - The disappearance of ");
      story.append(ns.creature().properName());
      story.append(" is now considered a kidnapping, ");
      story.append("according to a police spokesperson.");
      story.append('\n');
      final String dstr = CreatureName.firstName();
      final String dstr2 = CreatureName.lastname();
      story.append(dstr);
      story.append(' ');
      story.append(dstr2);
      story.append(", speaking on behalf of the police department, stated ");
      story.append("\"We now believe that ");
      story.append(ns.creature().properName());
      story.append(" was taken ");
      story.append(ns.creature().joindays() - 1);
      story.append(" days ago, by a person or persons as yet undetermined.  ");
      story.append("We have several leads and are confident that we will ");
      story.append("bring ");
      story.append(ns.creature().properName());
      story.append(" back home and bring the kidnappers to justice.  ");
      story.append("As the investigation is ongoing, I cannot be more specific at this time.  ");
      story
          .append("To the citizens, please contact the department if you have any additional information.");
      story.append('"');
      story.append('\n');
      story.append("  According to sources, ");
      story.append(ns.creature().properName());
      story.append("'s last known location was the ");
      story.append(ns.creature().workLocation().toString());
      story.append(".  Police were seen searching the surrounding area yesterday.");
      story.append('\n');
      break;
    }
    default:
      break;
    }
    displaynewsstory(story);
    Filler.constructfillerstory();
    generatefiller(200);
    int c;
    do {
      c = getch();
    } while (c != 27 && c != 10 && c != 32);
  }

  /* news - make some filler junk */
  private static void generatefiller(final int aAmount) {
    int amount = aAmount;
    final StringBuilder story = new StringBuilder();
    // TODO: Use text from filler.cpp
    story.append(Game.cityName());
    story.append(" - ");
    int par = 0;
    while (amount > 0) {
      par++;
      for (int j = 0; j < i.rng.nextInt(10) + 3; j++) {
        story.append('~');
      }
      if (amount > 1) {
        story.append(' ');
      }
      if (par >= 50 && i.rng.chance(5) && amount > 20) {
        par = 0;
        story.append('\n');
        story.append("  ");
      }
      amount--;
    }
    displaynewsstory(story);
  }

  private static void majorEvent() {
    final NewsStory ns = new NewsStory(StoryType.MAJOREVENT);
    do {
      ns.view = i.rng.randFromArray(Issue.coreValues());
      ns.positive = i.rng.chance(2);
      // Skip issues that we have no news stories for
      if (ns.view == Issue.IMMIGRATION) {
        continue;
      }
      if (ns.view == Issue.DRUGS) {
        continue;
      }
      if (ns.view == Issue.MILITARY) {
        continue;
      }
      if (ns.view == Issue.CIVILRIGHTS) {
        continue;
      }
      if (ns.view == Issue.TORTURE) {
        continue;
      }
      if (ns.view == Issue.GUNCONTROL) {
        continue;
      }
      // if(ns.view==Issue.POLITICALVIOLENCE)continue;
      // NO ABORTION
      if (ns.view == Issue.ABORTION && ns.positive
          && i.issue(Issue.ABORTION).law() == Alignment.ARCHCONSERVATIVE) {
        continue;
      }
      // NO PARTIAL BIRTH ABORTION
      if (ns.view == Issue.ABORTION && !ns.positive
          && i.issue(Issue.ABORTION).lawLT(Alignment.ELITELIBERAL)) {
        continue;
      }
      // NO DEATH PENALTY
      if (ns.view == Issue.DEATHPENALTY
          && i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO NUCLEAR POWER
      if (ns.view == Issue.NUCLEARPOWER && ns.positive
          && i.issue(Issue.NUCLEARPOWER).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO ANIMAL RESEARCH
      if (ns.view == Issue.ANIMALRESEARCH
          && i.issue(Issue.ANIMALRESEARCH).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO BAD COPS
      if (ns.view == Issue.POLICEBEHAVIOR && ns.positive
          && i.issue(Issue.POLICEBEHAVIOR).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO PRIVACY VIOLATIONS
      if (ns.view == Issue.PRIVACY && ns.positive
          && i.issue(Issue.PRIVACY).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO SWEATSHOPS
      if (ns.view == Issue.LABOR && ns.positive
          && i.issue(Issue.LABOR).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO POLLUTION
      if (ns.view == Issue.POLLUTION && ns.positive
          && i.issue(Issue.POLLUTION).lawGTE(Alignment.LIBERAL)) {
        continue;
      }
      // NO ENRONS
      if (ns.view == Issue.CORPORATECULTURE && ns.positive
          && i.issue(Issue.CORPORATECULTURE).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO CEOS
      if (ns.view == Issue.CEOSALARY && ns.positive
          && i.issue(Issue.CORPORATECULTURE).law() == Alignment.ELITELIBERAL) {
        continue;
      }
      // NO FREEDOM OF SPEECH
      if (ns.view == Issue.AMRADIO && !ns.positive && !i.freeSpeech()) {
        continue;
      }
      break;
    } while (true);
    i.newsStories.add(ns);
    if (ns.positive) {
      i.issue(ns.view).changeOpinion(20, 0, 100);
    } else {
      i.issue(ns.view).changeOpinion(-20, 0, 100);
    }
    i.issue(ns.view).addPublicInterest(50);
  }

  private static void televisionStories() {
    boolean del = false;
    for (final Iterator<NewsStory> iterator = i.newsStories.iterator(); iterator.hasNext();) {
      final NewsStory n = iterator.next();
      del = false;
      if (n.type == StoryType.MAJOREVENT) {
        if (n.positive) {
          switch (n.view) {
          case POLICEBEHAVIOR:
            // movie.loadmovie("art\\lacops.cmv");
            // movie.playmovie(0,0);
            // nodelay(stdscr,FALSE);
            ui().text("/----------------------------------------------------\\").add();
            ui().text("|     The  police  have  beaten  a  black  man  in    |").add();
            ui().text("|   Los Angeles again.  This time, the incident is    |").add();
            ui().text("|   taped by  a passerby  and saturates  the news.    |").add();
            ui().text("\\----------------------------------------------------/").add();
            getch();
            del = true;
            break;
          case CABLENEWS: {
            final StringBuilder str = new StringBuilder();
            str.setLength(0);
            str.append("Tonight on a Cable News channel: ");
            switch (i.rng.nextInt(5)) {
            default:
              str.append("Cross");
              break;
            case 1:
              str.append("Hard");
              break;
            case 2:
              str.append("Lightning");
              break;
            case 3:
              str.append("Washington");
              break;
            case 4:
              str.append("Capital");
              break;
            }
            switch (i.rng.nextInt(5)) {
            default:
              str.append(" Fire");
              break;
            case 1:
              str.append(" Ball");
              break;
            case 2:
              str.append(" Talk");
              break;
            case 3:
              str.append(" Insider");
              break;
            case 4:
              str.append(" Gang");
              break;
            }
            str.append(" with ");
            String bname = CreatureName.generateName();
            str.append(bname);
            ui().text(bname).add();
            ui().text("Washington D.C.").add();
            bname = CreatureName.generateName();
            ui().text(bname).add();
            switch (i.rng.nextInt(3)) {
            default:
              ui().text("Eugene, OR").add();
              break;
            case 1:
              ui().text("San Francisco, CA").add();
              break;
            case 2:
              ui().text("Cambridge, MA").add();
              break;
            }
            // movie.loadmovie("art\\newscast.cmv");
            //
            // movie.playmovie(1,1);
            // nodelay(stdscr,FALSE);
            ui().text("/----------------------------------------------------\\").add();
            ui().text("|     A  Cable  News  anchor  accidentally  let  a   |").add();
            ui().text("|   bright Liberal guest  finish a sentence.  Many   |").add();
            ui().text("|   viewers  across  the  nation  were  listening.   |").add();
            ui().text("\\----------------------------------------------------/").add();
            getch();
            del = true;
            break;
          }
          default:
            break;
          }
        } else {
          switch (n.view) {
          case CEOSALARY:
            // movie.loadmovie("art\\glamshow.cmv");
            // movie.playmovie(0,0);
            // nodelay(stdscr,FALSE);
            ui().text("/----------------------------------------------------\\").add();
            ui().text("|     A new show glamorizing the lives of the rich   |").add();
            ui().text("|   begins airing  this week.  With the nationwide   |").add();
            ui().text("|   advertising  blitz, it's bound  to be popular.   |").add();
            ui().text("\\----------------------------------------------------/").add();
            getch();
            del = true;
            break;
          case CABLENEWS:
            // movie.loadmovie("art\\anchor.cmv");
            // movie.playmovie(0,0);
            // nodelay(stdscr,FALSE);
            ui().text("/----------------------------------------------------\\").add();
            ui().text("|     A major Cable News channel has hired a slick   |").add();
            ui().text("|   new anchor for one of  its news shows.  Guided   |").add();
            ui().text("|   by impressive  advertising, America  tunes in.   |").add();
            ui().text("\\----------------------------------------------------/").add();
            getch();
            del = true;
            break;
          case ABORTION:
            // movie.loadmovie("art\\abort.cmv");
            // movie.playmovie(0,0);
            // nodelay(stdscr,FALSE);
            ui().text("/----------------------------------------------------\\").add();
            ui().text("|     A  failed partial  birth abortion  goes on a   |").add();
            ui().text("|   popular  afternoon  talk  show.    The  studio   |").add();
            ui().text("|   audience and viewers nationwide feel its pain.   |").add();
            ui().text("\\----------------------------------------------------/").add();
            getch();
            del = true;
            break;
          default:
            break;
          }
        }
      }
      if (del) {
        iterator.remove();
      }
    }
  }

  private static int writingSkill() {
    int writingSkill = 0;
    for (final Creature p : i.pool) {
      if (p.health().alive() && p.activity().type() == Activity.WRITE_GUARDIAN) {
        if (p.location().exists()
            && p.location().get().lcs().compoundWalls.contains(Compound.PRINTINGPRESS)) {
          p.skill().train(Skill.WRITING, i.rng.nextInt(3));
          writingSkill += p.skill().skillRoll(Skill.WRITING);
          p.crime().criminalize(Crime.SPEECH);
        } else {
          p.activity(BareActivity.noActivity());
        }
      }
    }
    return writingSkill;
  }
}
