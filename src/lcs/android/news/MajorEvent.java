package lcs.android.news;

import static lcs.android.game.Game.*;
import lcs.android.creature.CreatureName;
import lcs.android.creature.Gender;
import lcs.android.game.Game;
import lcs.android.politics.Alignment;
import lcs.android.politics.Exec;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

@NonNullByDefault class MajorEvent {
  enum Picture {
    BOOK,
    CEO,
    DOLLARS,
    GENETICS,
    KKK,
    MELTDOWN,
    MUTANT_BEAST,
    OIL,
    RIVERFIRE,
    TERRORISTS,
    TINKYWINKY,
    TSHIRT;
  }

  private static final String[] a = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
      "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

  private static final String[] makestate = { "Alabama", "Alaska", "Arkansas", "Arizona",
      "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho",
      "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
      "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska",
      "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina",
      "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island",
      "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia",
      "Washington", "West Virginia", "Wisconsin", "Wyoming" };

  public static void displaymajoreventstory(final NewsStory ns, final StringBuilder story) {
    if (ns.view == null) {
      Log.e("LCS", "News story with view == null:" + ns, new AssertionError());
      return;
    }
    assert ns.view != null;
    if (ns.positive) {
      switch (ns.view) {
      case ABORTION:
        News.displaycenterednewsfont("CLINIC MURDER");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case GAY:
        News.displaycenterednewsfont("CRIME OF HATE");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case DEATHPENALTY:
        News.displaycenterednewsfont("JUSTICE DEAD");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      /* case MILITARY: News.displaycenterednewsfont("CASUALTIES MOUNT",5);
       * News.displaycenteredsmallnews ("Is the latest military invasion yet another quagmire?",12);
       * story.append(""); generatefiller(story,200);
       * News.displaynewsstory(story,storyx_s,storyx_e,13); break; */
      /* case POLITICALVIOLENCE: News.displaycenterednewsfont("NIGHTMARE",5);
       * constructeventstory(story,ns.view,ns.positive); generatefiller(story,200);
       * News.displaynewsstory(story,storyx_s,storyx_e,13); break; */
      /* case GUNS: News.displaycenterednewsfont("SCHOOL SHOOTING",5);
       * constructeventstory(story,ns.view,ns.positive);
       * News.displaynewsstory(story,storyx_s,storyx_e,13); break; */
      case TAX: {
        News.displaycenterednewsfont("REAGAN FLAWED");// XXX:
        // "Reagan was wrong"
        // or
        // something?
        final StringBuilder str = new StringBuilder();
        switch (i.rng.nextInt(5)) {
        default:
          str.append("Shadow");
          break;
        case 1:
          str.append("Dark");
          break;
        case 2:
          str.append("Abyssal");
          break;
        case 3:
          str.append("Orwellian");
          break;
        case 4:
          str.append("Craggy");
          break;
        }
        str.append(' ');
        switch (i.rng.nextInt(5)) {
        default:
          str.append("Actor");
          break;
        case 1:
          str.append("Lord");
          break;
        case 2:
          str.append("Emperor");
          break;
        case 3:
          str.append("Puppet");
          break;
        case 4:
          str.append("Dementia");
          break;
        }
        str.append(": A new book further documenting the other side of Reagan.");
        News.displaycenteredsmallnews(str.toString());
        News.displaynewspicture(Picture.BOOK);
        break;
      }
      case NUCLEARPOWER:
        News.displaycenterednewsfont("MELTDOWN");
        News.displaycenteredsmallnews("A nuclear power plant suffers a catastrophic meltdown.");
        News.displaynewspicture(Picture.MELTDOWN);
        break;
      case ANIMALRESEARCH:
        News.displaycenterednewsfont("HELL ON EARTH");
        News.displaycenteredsmallnews("A mutant animal has escaped from a lab and killed thirty people.");
        News.displaynewspicture(Picture.MUTANT_BEAST);
        break;
      // case PRISONS:
      // News.displaycenterednewsfont("ON THE INSIDE",5);
      // constructeventstory(story,ns.view,ns.positive);
      // News.displaynewsstory(story,storyx_s,storyx_e,13);
      // break;
      case PRIVACY:
        News.displaycenterednewsfont("THE FBI FILES");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case FREESPEECH:
        News.displaycenterednewsfont("BOOK BANNED");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case GENETICS:
        News.displaycenterednewsfont("KILLER FOOD");
        News.displaycenteredsmallnews("Over a hundred people become sick from genetically modified food.");
        News.displaynewspicture(Picture.GENETICS);
        break;
      case JUSTICES:
        News.displaycenterednewsfont("IN CONTEMPT");
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case LABOR:
        News.displaycenterednewsfont("CHILD'S PLEA");
        News.displaycenteredsmallnews("A T-shirt in a store is found scrawled with a message from a sweatshop worker.");
        News.displaynewspicture(Picture.TSHIRT);
        break;
      case POLLUTION:
        News.displaycenterednewsfont("RING OF FIRE");
        News.displaycenteredsmallnews("The Ohio River caught on fire again.");
        News.displaynewspicture(Picture.RIVERFIRE);
        break;
      case CORPORATECULTURE:
        News.displaycenterednewsfont("BELLY UP");
        News.displaycenteredsmallnews("An enormous company files for bankruptcy, shattering the previous record.");// random
        // company
        // name
        News.displaynewspicture(Picture.DOLLARS);
        break;
      case CEOSALARY: {
        News.displaycenterednewsfont("AMERICAN CEO");
        final StringBuilder str = new StringBuilder();
        str.append("This major CEO ");
        switch (i.rng.nextInt(10)) {
        default:
          if (!i.freeSpeech() && i.issue(Issue.WOMEN).law() != Alignment.ARCHCONSERVATIVE) {
            str.append("regularly visits [working women].");
          } else if (!i.freeSpeech() && i.issue(Issue.WOMEN).law() == Alignment.ARCHCONSERVATIVE) {
            str.append("regularly [donates to sperm banks].");
          } else {
            str.append("regularly visits prostitutes.");
          }
          break;
        case 1:
          str.append("seeks the aid of psychics.");
          break;
        case 2:
          str.append("donated millions to the KKK.");
          break;
        case 3:
          str.append("hasn't paid taxes in over 20 years.");
          break;
        case 4:
          str.append("took out a contract on his wife.");
          break;
        case 5:
          str.append("doesn't know what his company does.");
          break;
        case 6:
          str.append("has a zoo of imported exotic worms.");
          break;
        case 7:
          str.append("paid millions for high-tech bondage gear.");
          break;
        case 8:
          str.append("installed a camera in an office bathroom.");
          break;
        case 9:
          str.append("owns slaves in another country.");
          break;
        }
        News.displaycenteredsmallnews(str.toString());
        News.displaynewspicture(Picture.CEO);
        break;
      }
      case AMRADIO:
        News.displaycenterednewsfont("AM IMPLOSION");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      default:
        break;
      }
    } else {
      assert ns.view != null;
      switch (ns.view) {
      case GAY:
        News.displaycenterednewsfont("KINKY WINKY");
        News.displaycenteredsmallnews("Jerry Falwell explains the truth about Tinky Winky.  Again.");
        News.displaynewspicture(Picture.TINKYWINKY);
        break;
      case DEATHPENALTY:
        News.displaycenterednewsfont("LET'S FRY 'EM");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      /* case MILITARY: News.displaycenterednewsfont("BIG VICTORY",5);
       * News.displaycenteredsmallnews(
       * "Our boys defend freedom once again, defeating an evil dictator." ,13); story.append("");
       * generatefiller(story,200); News.displaynewsstory(story,storyx_s,storyx_e,15); break; */
      /* case POLITICALVIOLENCE: News.displaycenterednewsfont("END IN TEARS",5);
       * constructeventstory(story,ns.view,ns.positive); generatefiller(story,200);
       * News.displaynewsstory(story,storyx_s,storyx_e,13); break; */
      /* case GUNS: News.displaycenterednewsfont("KILLER STOPPED",5);
       * constructeventstory(story,ns.view,ns.positive);
       * News.displaynewsstory(story,storyx_s,storyx_e,13); break; */
      case TAX: {
        News.displaycenterednewsfont("REAGAN THE MAN");
        final StringBuilder str = new StringBuilder();
        switch (i.rng.nextInt(5)) {
        default:
          str.append("Great");
          break;
        case 1:
          str.append("Noble");
          break;
        case 2:
          str.append("True");
          break;
        case 3:
          str.append("Pure");
          break;
        case 4:
          str.append("Golden");
          break;
        }
        str.append(' ');
        switch (i.rng.nextInt(5)) {
        default:
          str.append("Leadership");
          break;
        case 1:
          str.append("Courage");
          break;
        case 2:
          str.append("Pioneer");
          break;
        case 3:
          str.append("Communicator");
          break;
        case 4:
          str.append("Faith");
          break;
        }
        str.append(": A new book lauding Reagan and the greatest generation.");
        News.displaycenteredsmallnews(str.toString());
        News.displaynewspicture(Picture.BOOK);
        break;
      }
      case NUCLEARPOWER:
        News.displaycenterednewsfont("OIL CRUNCH");
        News.displaycenteredsmallnews("OPEC cuts oil production sharply in response to a US foreign policy decision.");
        News.displaynewspicture(Picture.OIL);
        break;
      case ANIMALRESEARCH:
        News.displaycenterednewsfont("APE EXPLORERS");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case POLICEBEHAVIOR:
        if (!i.freeSpeech()) {
          News.displaycenterednewsfont("[JERKS]");
        } else {
          News.displaycenterednewsfont("BASTARDS");
        }
        News.displaynewspicture(Picture.TERRORISTS);
        break;
      // case PRISONS:
      // News.displaycenterednewsfont("HOSTAGE SLAIN",5);
      // constructeventstory(story,ns.view,ns.positive);
      // News.displaynewsstory(story,storyx_s,storyx_e,13);
      // break;
      case PRIVACY:
        News.displaycenterednewsfont("DODGED BULLET");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case FREESPEECH:
        News.displaycenterednewsfont("HATE RALLY");
        News.displaycenteredsmallnews("Free speech advocates fight hard to let a white supremacist rally take place.");
        News.displaynewspicture(Picture.KKK);
        break;
      case GENETICS:
        News.displaycenterednewsfont("GM FOOD FAIRE");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case JUSTICES:
        News.displaycenterednewsfont("JUSTICE AMOK");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case LABOR:
        News.displaycenterednewsfont("THEY ARE HERE");
        if (i.score.date.month() >= 8 && i.score.date.month() <= 11) {
          News.displaycenteredsmallnews("Fall fashions hit the stores across the country.");
        } else {
          News.displaycenteredsmallnews("Fall fashions are previewed in stores across the country.");
        }
        News.displaynewspicture(Picture.TSHIRT);
        break;
      case POLLUTION:
        News.displaycenterednewsfont("LOOKING UP");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case CORPORATECULTURE:
        News.displaycenterednewsfont("NEW JOBS");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      case AMRADIO:
        News.displaycenterednewsfont("THE DEATH");
        News.displaycenterednewsfont("OF CULTURE");
        assert ns.view != null;
        constructeventstory(story, ns.view, ns.positive);
        News.displaynewsstory(story);
        break;
      default:
        break;
      }
    }
  }

  private static String a() { // alpha
    return i.rng.randFromArray(a);
  }

  private static void constructeventstory(final StringBuilder story, final Issue view,
      final boolean positive) {
    final Gender gender = i.rng.choice(Gender.MALE, Gender.FEMALE);
    final String firstname = CreatureName.firstName(gender);
    final String lastname = CreatureName.lastname();
    if (positive) {
      switch (view) {
      case ABORTION: {
        story.append(Game.cityName());
        story.append(" - ");
        if (i.issue(Issue.ABORTION).law() == Alignment.ARCHCONSERVATIVE) {
          story
              .append("A doctor that routinely performed illegal abortion-murders was ruthlessly ");
        } else if (i.issue(Issue.ABORTION).law() == Alignment.CONSERVATIVE) {
          story.append("A doctor that routinely performed illegal abortions was ruthlessly ");
        } else if (i.issue(Issue.ABORTION).law() == Alignment.MODERATE) {
          story.append("A doctor that routinely performed semi-legal abortions was ruthlessly ");
        } else {
          story.append("A doctor that routinely performed abortions was ruthlessly ");
        }
        story.append("gunned down outside of the ");
        story.append(CreatureName.lastname());
        story.append(" CLINIC yesterday.  ");
        story.append("Dr. ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        story.append(" was walking to ");
        story.append(gender.possesive);// TODO: Add more variety,
        // not
        // just in the parking lot.
        story.append(" car when, according to police reports, ");
        story.append("shots were fired from a nearby vehicle.  ");
        story.append(lastname);
        story.append(" was hit ");
        story.append(i.rng.nextInt(15) + 3);
        story.append(" times and died immediately in the parking lot.  ");
        story.append("The suspected shooter, ");
        final Gender g = i.rng.choice(Gender.MALE, Gender.FEMALE);
        final String str = CreatureName.firstName(g);
        final String str2 = CreatureName.lastname();
        story.append(str);
        story.append(' ');
        story.append(str2);
        story.append(", is in custody.\n");
        story.append("  Witnesses report that ");
        story.append(str2);
        story.append(" remained at the scene after the shooting, screaming ");
        story.append("verses of the Bible at the stunned onlookers.  Someone ");
        story.append("called the police on a cellphone and they arrived shortly thereafter.  ");
        story.append(str2);
        if (i.issue(Issue.WOMEN).law() == Alignment.ARCHCONSERVATIVE) {
          story.append(" later admitted to being a rogue FBI vigilante, hunting down ");
          story.append(" abortion doctors as opposed to arresting them.\n");
        } else {
          story.append(" surrendered without a struggle, reportedly saying that God's work ");
          story.append("had been completed.\n");
        }
        story.append("  ");
        story.append(lastname);
        story.append(" is survived by ");
        story.append(gender.possesive);
        story.append(' ');
        Gender spouse;
        if (i.issue(Issue.GAY).lawLTE(Alignment.LIBERAL)) {
          spouse = gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
        } else {
          spouse = i.rng.choice(Gender.MALE, Gender.FEMALE);
        }
        switch (spouse) {
        case MALE:
        default:
          story.append("husband");
          break;
        case FEMALE:
          story.append("wife");
          break;
        }
        story.append(" and ");
        story.append(i.rng.choice("two", "three", "four", "five"));
        story.append(" children.\n");
        break;
      }
      case GAY: {
        story.append(Game.cityName());
        story.append(" - ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        if (i.issue(Issue.GAY).law() == Alignment.ARCHCONSERVATIVE) {
          story.append(", a known sexual deviant, was ");
        } else if (i.issue(Issue.GAY).law() == Alignment.CONSERVATIVE) {
          story.append(", a known homosexual, was ");
        } else {
          story.append(", a homosexual, was ");
        }
        story.append(i.rng.choice("dragged to death behind a pickup truck", "burned alive",
            "beaten to death"));
        story.append(" here yesterday.  ");
        story.append("A police spokesperson reported that four suspects ");
        story.append("were apprehended after a high speed chase.  Their names ");
        story.append("have not yet been released.");
        story.append('\n');
        story.append("  Witnesses of the freeway chase described the pickup of the alleged ");
        story.append("murderers swerving wildly, ");
        switch (i.rng.nextInt(3)) {
        default:
          if (!i.freeSpeech()) {
            story.append("throwing [juice boxes]");
          } else {
            story.append("throwing beer bottles");
          }
          break;
        case 1:
          if (!i.freeSpeech()) {
            story.append("[relieving themselves] out the window");
          } else if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story.append("pissing out the window");
          } else {
            story.append("urinating out the window");
          }
          break;
        case 2:
          story.append("taking swipes");
        }
        story.append(" at the pursuing police cruisers.  ");
        story.append("The chase ended when ");
        story.append(i.rng.choice("the suspects ran out of gas, ",
            "the suspects collided with a manure truck, ", "the suspects veered into a ditch, ",
            "the suspects were surrounded by alert citizens, ",
            "the suspects were caught in traffic, "));
        story
            .append("at which point they were taken into custody.  Nobody was seriously injured during the incident.");
        story.append('\n');
        story.append("  Authorities have stated that they will vigorously ");
        story.append("prosecute this case as a hate crime, due to the ");
        story.append("aggravated nature of the offense");
        if (i.issue(Issue.GAY).law() == Alignment.ARCHCONSERVATIVE && i.freeSpeech()) {
          story.append(", despite the fact that ");
          story.append(firstname);
          story.append(' ');
          story.append(lastname);
          story.append(" is a known faggot");
        } else if (i.issue(Issue.GAY).law() == Alignment.ARCHCONSERVATIVE) {
          story.append(", even though being gay is deviant, as we all know.");
        } else {
          story.append('.');
        }
        story.append('\n');
        break;
      }
      case DEATHPENALTY: {
        story.append(MajorEvent.makestate());
        story.append(" - An innocent citizen has been put to death in the electric chair.  ");
        final String middlename = CreatureName.firstName(gender);
        story.append(firstname);
        story.append(' ');
        story.append(middlename);
        story.append(' ');
        story.append(lastname);
        story.append(" was pronounced dead at ");
        story.append(i.rng.nextInt(12) + 1);
        story.append(':');
        story.append(i.rng.nextInt(60));
        story.append(i.rng.choice("AM", "PM"));
        story.append(" yesterday at the ");
        story.append(CreatureName.lastname());
        story.append(" Correctional Facility.\n");
        story.append("  ");
        story.append(lastname);
        story.append(" was convicted in ");
        story.append(i.score.date.year() - i.rng.nextInt(11) - 10);
        story.append(" of 13 serial murders.  ");
        story.append("Since then, numerous pieces of exculpatory evidence ");
        story.append("have been produced, including ");
        story.append(i.rng.choice("a confession from another convict.  ",
            "a battery of negative DNA tests.  ", "an admission from a former prosecutor that "
                + lastname + " was framed.  "));
        story.append("The state still went through with the execution, with a ");
        story.append("spokesperson for the governor saying, ");
        story.append('"');
        story.append(i.rng.choice(
            "Let's not forget the convict is colored.  You know how their kind are",
            "The convict is always referred to by three names.  "
                + "Assassin, serial killer, either way -- guilty.  " + "End of story",
            "The family wants closure.  We don't have time for another trial"));
        story.append(".\"");
        story.append('\n');
        story
            .append("  Candlelight vigils were held throughout the country last night during the execution, ");
        story
            .append("and more events are expected this evening.  If there is a bright side to be found from this ");
        story
            .append("tragedy, it will be that our nation is now evaluating the ease with which people ");
        story.append("can be put to death in this country.");
        story.append('\n');
        break;
      }
      case PRIVACY: {
        story.append("Washington D.C. - The FBI might be keeping tabs on you.  ");
        story
            .append("This newspaper yesterday received a collection of files from a source in the Federal Bureau of Investigations.  ");
        story
            .append("The files contain information on which people have been attending demonstrations, organizing ");
        story.append("unions, working for liberal organizations -- even ");
        story.append(i.rng.choice("buying music with 'Explicit Lyrics' labels.",
            "helping homeless people"));
        story.append('.');
        story.append('\n');
        story.append("  More disturbingly, the files make reference to a plan to ");
        story.append("\"deal with the undesirables\", although this phrase is not clarified.  ");
        story.append('\n');
        story
            .append("  The FBI refused to comment initially, but when confronted with the information, ");
        story.append("a spokesperson stated, \"");
        story.append("Well, you know, there's privacy, and there's privacy.  ");
        story.append("It might be a bit presumptive to assume that ");
        story.append("these files deal with the one and not the other.  ");
        story.append("You think about that before you continue slanging accusations");
        story.append(".\"");
        story.append('\n');
        break;
      }
      case FREESPEECH: {
        story.append(Game.cityName());
        story.append(" - A children's story has been removed from libraries here after ");
        story.append("the city bowed to pressure from religious groups.");
        story.append('\n');
        story.append("   The book, ");
        story.append('_');
        final String nstr = CreatureName.firstName(i.rng.choice(Gender.MALE, Gender.FEMALE));
        story.append(nstr);
        story.append('_');
        story.append(CreatureName.lastname());
        story.append("_and_the_");
        story.append(i.rng.choice("Mysterious", "Magical", "Golden", "Invisible", "Amazing",
            "Secret"));
        story.append('_');
        story.append(i.rng.choice("Thing", "Stuff", "Object", "Whatever", "Something"));
        story.append("_, is the third in an immensely popular series by ");
        story.append(i.rng.choice("British", "Indian", "Chinese", "Rwandan", "Palestinian",
            "Egyptian", "French", "German", "Iraqi", "Bolivian", "Columbian"));
        story.append(" author ");
        story.append(MajorEvent.a() + ". " + MajorEvent.a() + ". " + lastname);
        story.append(".  ");
        story.append("Although the series is adored by children worldwide, ");
        story.append("some conservatives feel that the books ");
        story.append(i.rng.choice(
            "glorify Satan worship and are spawned by demons from the pit.  ",
            "teach children to kill their parents and hate life.  ",
            "cause violence in schools and are a gateway to cocaine use.  ",
            "breed demonic thoughts that manifest themselves as dreams of murder.  ",
            "contain step-by-step instructions to summon the Prince of Darkness.  "));
        story.append("In their complaint, the groups cited an incident involving ");
        story.append(i.rng.choice("a child that swore in @NonNullByDefault class",
            "a child that said a magic spell at her parents",
            "a child that " + i.rng.choice("pushed ", "hit ", "slapped ", "insulted ", "tripped ")
                + i.rng.choice("his ", "her ") + i.rng.choice("older ", "younger ", "twin ")
                + i.rng.choice("brother", "sister")));
        story.append(" as key evidence of the dark nature of the book.");
        story.append('\n');
        story.append("   When the decision to ban the book was announced yesterday, ");
        story.append("many area children spontaneously broke into tears.  One child was ");
        story.append("heard saying, \"");
        story.append(i.rng.choice("Mamma, is " + nstr + " dead?", "Mamma, why did they kill "
            + nstr + "?"));
        story.append('"');
        story.append('\n');
        break;
      }
      case JUSTICES: {
        story.append(Game.cityName());
        story.append(" - Conservative federal judge ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        if (!i.freeSpeech()) {
          story.append(" has resigned in disgrace after being caught with a [civil servant.]");
        } else {
          story.append(" has resigned in disgrace after being caught with a prostitute.");
        }
        story.append('\n');
        story.append("  ");
        story.append(lastname);
        story.append(", who once ");
        story
            .append(i.rng
                .choice(
                    "defied the federal government by putting a Ten Commandments monument in the local federal building",
                    "stated that, \"Segregation wasn't the bad idea everybody makes it out to be these days\""));
        story.append(", was found with ");
        final Gender pg = i.rng.choice(Gender.MALE, Gender.FEMALE);
        final String pstr = CreatureName.firstName(pg);
        final String pstr2 = CreatureName.lastname();
        story.append(pstr);
        story.append(' ');
        story.append(pstr2);
        story.append(" last week in a hotel during a police sting operation.  ");
        story.append("According to sources familiar with the particulars, ");
        story.append("when police broke into the hotel room they saw ");
        switch (i.rng.nextInt(3)) {
        default:
          story
              .append("\"the most perverse and spine-tingling debauchery imaginable, at least with only two people.\"");
          break;
        case 1:
          if (!i.freeSpeech()) {
            story.append("the judge [going to the bathroom near] the [civil servant.]");
          } else if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story.append("the judge pissing on the prostitute.");
          } else {
            story.append("the judge relieving himself on the prostitute.");
          }
          break; // Himself? Maybe themselves would work
        // better? -- LK
        case 2:
          if (!i.freeSpeech()) {
            story
                .append("the [civil servant] hollering like a cowboy [at a respectable distance from] the judge.");
          } else {
            story.append("the prostitute hollering like a cowboy astride the judge.");
          }
          break;
        }
        story.append("  ");
        story.append(pstr2);
        story.append(" reportedly offered ");
        story.append(i.rng.choice("the arresting officers money", "to let the officers join in",
            "the arresting officers \"favors\""));
        story.append(" in exchange for their silence.");
        story.append('\n');
        story.append("  ");
        story.append(lastname);
        story.append(" could not be reached for comment, although an aid stated that ");
        story.append("the judge would be going on a Bible retreat for a few weeks to ");
        story.append("\"Make things right with the Almighty Father.\"  ");
        story.append('\n');
        break;
      }
      case AMRADIO: {
        story.append(Game.cityName());
        story.append(" - Well-known AM radio personality ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        story.append(" went off for fifteen minutes in an inexplicable rant ");
        story.append("two nights ago during the syndicated radio program \"");
        story.append(i.rng.choice("Straight", "Real", "True"));
        story.append(' ');
        story.append(i.rng.choice("Talk", "Chat", "Discussion"));
        story.append("\".");
        story.append('\n');
        story.append("  ");
        story.append(lastname);
        story.append("'s monologue for the evening began the way that fans ");
        story
            .append("had come to expect, with attacks on the \"liberal media establishment\" and ");
        story.append("the \"elite liberal agenda\".  But when the radio icon said, \"");
        story
            .append(i.rng
                .choice(
                    "and the Grays are going to take over the planet in the End Times",
                    "a liberal chupacabra will suck the blood from us like a goat, a goat!, a goat!",
                    "I feel translucent rods passing through my body...  it's like making love to the future",
                    "and the greatest living example of a reverse racist is the "
                        + (i.execs.get(Exec.PRESIDENT).alignment() == Alignment.ELITELIBERAL ? "current president!" // Limbaugh
                            : "liberal media establishment!")));
        story.append("\", a former fan of the show, ");
        final String nstr = CreatureName.firstName();
        final String nstr2 = CreatureName.lastname();
        story.append(nstr);
        story.append(' ');
        story.append(nstr2);
        story.append(", knew that \"");
        story.append(i.rng.choice("my old hero", "my old idol", "the legend"));
        story.append(" had ");
        story.append(i.rng.choice("lost " + gender.possesive
            + (!i.freeSpeech() ? " [gosh darn] mind" : " goddamn mind"),
            "maybe gone a little off the deep end",
            "probably been listening to Art Bell in the next studio a little too long"));
        story.append(".  After that, it just got worse and worse.\"");
        story.append('\n');
        story.append("  ");
        story.append(lastname);
        story.append(" issued an apology later in the program, but ");
        story.append("the damage might already be done.  ");
        story.append("According to a poll completed yesterday, ");
        story.append("fully half of the host's most loyal supporters "); // XXX
        // How
        // many
        // of
        // them
        // switch
        // should
        story.append("have decided to leave the program for saner "); // depend
        // on
        // [LAW_FREESPEECH]
        story.append("pastures.  Of these, many said that they would be switching over ");
        story.append("to the FM band.");
        story.append('\n');
        break;
      }
      default:
        break;
      }
    } else {
      switch (view) {
      case DEATHPENALTY: {
        story.append(Game.cityName());
        story.append(" - Perhaps parents can rest easier tonight.  ");
        story.append("The authorities have apprehended their primary suspect in the ");
        story
            .append("string of brutal child killings that has kept everyone in the area on edge, ");
        story.append("according to a spokesperson for the police department here.  ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        story.append(' ');
        story.append(lastname);
        story.append(" was detained yesterday afternoon, reportedly in possession of ");
        story.append(i.rng.choice("pieces of another victim", "bloody toys",
            "a child's clothing stained with DNA evidence", "seven junior high school yearbooks",
            "two small backpacks"));
        story.append(".  Over twenty children in the past two years have gone missing, ");
        story.append("only to turn up later");
        if (!i.freeSpeech()) {
          story.append(" [in a better place]");
        } else {
          story.append(" dead and ");
          story.append(i.rng.choice("carved with satanic symbols", "sexually mutilated",
              "missing all of their teeth", "missing all of their fingers", "without eyes"));
        }
        story.append(".  Sources say that the police got a break in the case when ");
        story.append(i.rng.choice(
            "a victim called 911 just prior to being slain while still on the phone",
            "the suspect allegedly carved an address into one of the bodies",
            "an eye witness allegedly spotted the suspect luring a victim into a car",
            "a blood trail was found on a road that led them to the suspect's car trunk",
            "they found a victim in a ditch, still clinging to life"));
        story.append('.');
        story.append('\n');
        story.append("   The district attorney's office has already repeatedly said it will be ");
        story.append("seeking ");
        if (i.issue(Issue.DEATHPENALTY).law() == Alignment.ELITELIBERAL) {
          story.append("life imprisonment in this case.");
        } else {
          story.append("the death penalty in this case.");
        }
        story.append('\n');
        break;
      }
      case ANIMALRESEARCH: {
        story.append(Game.cityName());
        story.append(" - Researchers ");
        if (i.issue(Issue.ANIMALRESEARCH).law() == Alignment.ELITELIBERAL) {
          story.append("from ");
          story.append(i.rng.choice("Russia", "North Korea", "Taiwan", "Iran", "China"));
          story.append(" report that they have discovered an amazing new wonder drug. ");
        } else {
          story.append("here report that they have discovered an amazing new wonder drug.  ");
        }
        story.append("Called ");
        story.append(i.rng.choice(!i.freeSpeech() ? "Bum-Bum" : "Anal", "Colo", "Lacta", "Pur",
            "Loba"));
        story.append(i.rng.choice("nephrin", "tax", "zac", "thium", "drene"));
        story.append(", the drug apparently ");
        story.append(i.rng.choice("boosts intelligence in chimpanzees",
            !i.freeSpeech() ? "[helps chimpanzees reproduce]"
                : "corrects erectile dysfunction in chimpanzees",
            "allows chimpanzees to move blocks with their minds",
            "allows chimpanzees to fly short distances",
            "increases the attention span of young chimpanzees"));
        story.append(".  ");
        story.append('\n');
        story.append("   Along with bonobos, chimpanzees are our closest cousins");
        story.append(".  ");
        story
            .append("Fielding questions about the ethics of their experiments from reporters during a press conference yesterday, ");
        story
            .append("a spokesperson for the research team stated that, \"It really isn't so bad as all that.  Chimpanzees are very resilient creatures.  ");
        story
            .append(i.rng
                .choice(
                    "The ones that survived are all doing very well",
                    "They hardly notice when you drill their brains out, if you're fast",
                    "When we started muffling the screams of our subjects, the other chimps all calmed down quite a bit"));
        story.append(".  We have a very experienced research team.  ");
        story.append("While we understand your concerns, any worries are entirely unfounded.  ");
        story.append("I think the media should be focusing on the enormous benefits of this drug.");
        story.append('"');
        story.append('\n');
        story.append("   The first phase of human trials is slated to begin in a few months.");
        story.append('\n');
        break;
      }
      case PRIVACY: {
        story
            .append("Washington D.C. - The CIA announced yesterday that it has averted a terror attack that ");
        story.append("would have occurred on American soil.");
        story.append('\n');
        story.append("   According to a spokesperson for the agency, ");
        story.append(i.rng.choice("white supremacists", "Islamic fundamentalists",
            "outcast goths from a suburban high school"));
        story.append(" planned to ");
        story.append(i.rng.choice(!i.freeSpeech() ? "[land] planes [on apartment buildings]"
            : "fly planes into skyscrapers", "detonate a fertilizer bomb at a federal building",
            "ram a motorboat loaded with explosives into a warship",
            !i.freeSpeech() ? "[harm children]" : "detonate explosives on a school bus",
            "blow out a section of a major bridge",
            !i.freeSpeech() ? "[take] the president [on vacation]" : "kidnap the president", !i
                .freeSpeech() ? "[hurt] the president" : "assassinate the president",
            "destroy the Capitol Building", "detonate a nuclear bomb in New York"));
        story.append(".  However, intelligence garnered from deep within the mysterious ");
        story.append("terrorist organization allowed the plot to be foiled just days before it ");
        story.append("was to occur.");
        story.append('\n');
        story.append("   The spokesperson further stated, \"");
        story.append("I won't compromise our sources and methods, but let me just say ");
        story.append("that we are grateful to the Congress and this Administration for ");
        story.append("providing us with the tools we need to neutralize these enemies of ");
        story.append("civilization before they can destroy American families.  ");
        story.append("However, let me also say that there's more that needs to be done.  ");
        story.append("The Head of the Agency will be sending a request to Congress ");
        story.append("for what we feel are the essential tools for combating terrorism in ");
        story.append("this new age.");
        story.append('"');
        story.append('\n');
        break;
      }
      case GENETICS: {
        story.append(Game.cityName());
        story.append(" - The genetic foods industry staged a major event here yesterday ");
        story.append("to showcase its upcoming products.  Over thirty companies set up ");
        story.append("booths and gave talks to wide-eyed onlookers.");
        story.append('\n');
        story.append("   One such corporation, ");
        story.append(i.rng.choice("Altered", "Gene-tech", "DNA", "Proteomic", "Genomic"));
        story.append(' ');
        story.append(i.rng.choice("Foods", "Agriculture", "Meals", "Farming", "Living"));
        story.append(", presented their product, \"");
        story.append(i.rng.choice("Mega", "Epic", "Overlord", "Franken", "Transcendent"));
        story.append(' ');
        story.append(i.rng.choice("Rice", "Beans", "Corn", "Wheat", "Potatoes"));
        story.append("\", during an afternoon Power Point presentation.  ");
        story.append("According to the public relations representative speaking, ");
        story.append("this amazing new product actually ");
        story.append(i.rng.choice("extends human life by a few minutes every bite",
            "mends split-ends upon digestion.  Hair is also made glossier and thicker",
            "allows people to see in complete darkness",
            "causes a person to slowly attain their optimum weight with repeated use",
            "cures the common cold"));
        story.append('.');
        story.append('\n');
        story.append("   Spokespeople for the GM corporations were universal ");
        story.append("in their dismissal of the criticism which often follows the industry.  ");
        story.append("One in particular said, \"");
        story.append("Look, these products are safe.  That thing about the ");
        story.append(i.rng.choice("guy going on a killing spree", "gal turning blue and exploding",
            "guy speaking in tongues and worshiping Satan", "gal having a ruptured intestine"));
        story.append(" is just a load of ");
        if (!i.freeSpeech()) {
          story.append(i.rng.choice("hooey", "poppycock", "horse radish", "skunk weed", "garbage"));
        } else {
          story.append(i.rng.choice("horseshit", // Mccain
              "bullshit", "shit"));
        }
        story.append(".  Would we stake the reputation of our company on unsafe products?  ");
        story
            .append("No.  That's just ridiculous.  I mean, sure companies have put unsafe products out, ");
        story
            .append("but the GM industry operates at a higher ethical standard.  That goes without saying.");
        story.append('"');
        story.append('\n');
        break;
      }
      case JUSTICES: {
        story.append(Game.cityName());
        story.append(" - The conviction of confessed serial killer ");
        final String middlename = CreatureName.firstName(gender);
        story.append(firstname);
        story.append(' ');
        story.append(middlename);
        story.append(' ');
        story.append(lastname);
        story.append(" was overturned by a federal judge yesterday.  ");
        story.append("Justice ");
        final Gender jg = i.rng.choice(Gender.MALE, Gender.FEMALE);
        final String jstr = CreatureName.firstName(jg);
        final String jstr2 = CreatureName.lastname();
        story.append(jstr);
        story.append(' ');
        story.append(jstr2);
        story.append(" of the notoriously liberal circuit of appeals here ");
        story.append("made the decision based on ");
        story.append(gender.possesive);
        story.append(i.rng.choice("ten-year-old eye witness testimony", jg.possesive
            + " general feeling about police corruption", jg.possesive
            + " belief that the crimes were a vast right-wing conspiracy", // Clinton
            jg.possesive + " belief that " + lastname + " deserved another chance", jg.possesive
                + " personal philosophy of liberty"));
        story.append(", despite the confession of ");
        story.append(lastname);
        story.append(", which even Justice ");
        story.append(jstr2);
        story.append(" grants was not coerced in any way.\n");
        story.append("   Ten years ago, ");
        story.append(lastname);
        story.append(" was convicted of the now-infamous ");
        story.append(CreatureName.lastname());
        story.append(" slayings.  ");
        story.append("After an intensive manhunt, ");
        story.append(lastname);
        story.append(" was found with the murder weapon, ");
        story.append("covered in the victims' blood.  ");
        story.append(lastname);
        story.append(" confessed and was sentenced to life, saying \"");
        story.append("Thank you for saving me from myself.  ");
        story.append("If I were to be released, I would surely kill again.\"\n");
        story.append("   A spokesperson for the district attorney ");
        story.append("has stated that the case will not be retried, due ");
        story.append("to the current economic doldrums that have left the state ");
        story.append("completely strapped for cash.\n");
        break;
      }
      case POLLUTION:
        story.append(Game.cityName());
        story.append(" - Pollution might not be so bad after all.  The ");
        story.append(i.rng.choice("American", "United", "Patriot", "Family", "Children's",
            "National"));
        story.append(' ');
        story.append(i.rng.choice("Heritage", "Enterprise", "Freedom", "Liberty", "Charity",
            "Equality"));
        story.append(' ');
        story.append(i.rng.choice("Partnership", "Institute", "Consortium", "Forum", "Center",
            "Association"));
        story.append(" recently released a wide-ranging report detailing recent trends ");
        story.append("and the latest science on the issue.  ");
        story.append("Among the most startling of the think tank's findings is that ");
        story.append(i.rng.choice("a modest intake of radioactive waste",
            "a healthy dose of radiation", "a bath in raw sewage",
            "watching animals die in oil slicks", "inhaling carbon monoxide"));
        story.append(" might actually ");
        story.append(i.rng.choice("purify the soul", "increase test scores",
            "increase a child's attention span", "make children behave better",
            "make shy children fit in"));
        story.append('.');
        story.append('\n');
        story.append("   When questioned about the science behind these results, ");
        story.append("a spokesperson stated that, \"");
        story
            .append(i.rng
                .choice(
                    "Research is complicated, and there are always two ways to think about things",
                    "The jury is still out on pollution.  You really have to keep an open mind",
                    "They've got their scientists, and we have ours.  The issue of pollution is wide open as it stands today"));
        story.append(".  You have to realize that ");
        story.append(i.rng.choice("the elitist liberal media often distorts",
            "the vast left-wing education machine often distorts",
            "the fruits, nuts, and flakes of the environmentalist left often distort"));
        story.append(" these issues to their own advantage.  ");
        story.append("All we've done is introduced a little clarity into the ongoing debate.  ");
        story.append("Why is there contention on the pollution question?  It's because ");
        story.append("there's work left to be done.  We should study much more ");
        story.append("before we urge any action.  Society really just ");
        story
            .append("needs to take a breather on this one.  We don't see why there's such a rush to judgment here.  ");
        story.append('\n');
        break;
      case CORPORATECULTURE:
        story.append(Game.cityName());
        story.append(" - Several major companies have announced ");
        story.append("at a joint news conference here that they ");
        story.append("will be expanding their work forces considerably ");
        story.append("during the next quarter.  Over thirty thousand jobs ");
        story.append("are expected in the first month, with ");
        story.append("tech giant ");
        story.append(i.rng.choice("Ameri", "Gen", "Oro", "Amelia", "Vivo", "Benji", "Amal", "Ply",
            "Seli", "Rio"));
        story.append(i.rng.choice("tech", "com", "zap", "cor", "dyne", "bless", "chip", "co",
            "wire", "rex"));
        story.append(" increasing its payrolls by over ten thousand workers alone.  ");
        story.append("Given the state of the economy recently and in ");
        story.append("light of the tendency ");
        story.append("of large corporations to export jobs overseas these days, ");
        story
            .append("this welcome news is bound to be a pleasant surprise to those in the unemployment lines.  ");
        story.append("The markets reportedly responded to the announcement with mild interest, ");
        story.append("although the dampened movement might be expected due to the uncertain ");
        story
            .append("futures of some of the companies in the tech sector.  On the whole, however, ");
        story.append("analysts suggest that not only does the expansion speak to the health ");
        story.append("of the tech industry but is also indicative of a full economic recover.\n");
        break;
      case AMRADIO: {
        // THIS ONE IS SHORTER BECAUSE OF DOUBLE HEADLINE
        story.append(Game.cityName());
        story.append(" - Infamous FM radio shock jock ");
        story.append(firstname);
        story.append(' ');
        story.append(lastname);
        story.append(" has brought radio entertainment to a new low.  During yesterday's ");
        story.append("broadcast of the program \"");
        story.append(firstname);
        story.append("'s ");
        story.append(i.rng.choice("Morning", "Commuter", "Jam", "Talk", "Radio"));
        story.append(' ');
        story.append(i.rng.choice("Swamp", "Jolt", "Club", "Show", "Fandango"));
        story.append("\", ");
        story.append(lastname);
        story.append(" reportedly ");
        switch (i.rng.nextInt(5)) {
        default:
          if (!i.freeSpeech()) {
            story.append("[had consensual intercourse in the missionary position]");
          } else if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story.append("fucked");
          } else {
            story.append("had intercourse");
          }
          break;
        case 1:
          if (!i.freeSpeech()) {
            story.append("encouraged listeners to call in and [urinate]");
          } else if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story.append("encouraged listeners to call in and take a piss");
          } else {
            story.append("encouraged listeners to call in and relieve themselves");
          }
          break;
        case 2:
          if (i.issue(Issue.FREESPEECH).law() == Alignment.ELITELIBERAL) {
            story
                .append("screamed \"fuck the police those goddamn motherfuckers.  I got a fucking ticket this morning and I'm fucking pissed as shit.\"");
          } else if (!i.freeSpeech()) {
            story
                .append("screamed \"[darn] the police those [big dumb jerks]. I got a [stupid] ticket this morning and I'm [so angry].\"");
          } else {
            story
                .append("screamed \"f*ck the police those g*dd*mn m*th*f*ck*rs.  I got a f*cking ticket this morning and I'm f*cking p*ss*d as sh*t.\"");
          }
          break;
        case 3:
          if (!i.freeSpeech() && i.issue(Issue.WOMEN).law() == Alignment.ARCHCONSERVATIVE) {
            story.append("[fed] from [an indecent] woman");
          } else if (i.freeSpeech() && i.issue(Issue.WOMEN).law() == Alignment.ARCHCONSERVATIVE) {
            story.append("breastfed from an exposed woman");
          } else if (!i.freeSpeech() && i.issue(Issue.WOMEN).law() != Alignment.ARCHCONSERVATIVE) {
            story.append("[fed] from a [woman]");
          } else {
            story.append("breastfed from a lactating woman");
          }
          break;
        case 4:
          if (!i.freeSpeech()) {
            story.append("[had fun]");
          } else {
            story.append("masturbated");
          }
          break;
        }
        story.append(" on the air.  Although ");
        story.append(lastname);
        story.append(" later apologized, ");
        story.append("the FCC received ");
        if (!i.freeSpeech()) {
          story.append("thousands of");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.CONSERVATIVE) {
          story.append("several hundred");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.MODERATE) {
          story.append("hundreds of");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.LIBERAL) {
          story.append("dozens of");
        } else {
          story.append("some");
        }
        story.append(" complaints ");
        story.append("from irate listeners ");
        if (!i.freeSpeech()) {
          story.append("across the nation. ");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.CONSERVATIVE) {
          story.append("from all over the state. ");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.MODERATE) {
          story.append("within the county. ");
        } else if (i.issue(Issue.FREESPEECH).law() == Alignment.LIBERAL) {
          story.append("in neighboring towns. ");
        } else {
          story.append("within the town. ");
        }
        story.append(" A spokesperson for the FCC ");
        story.append("stated that the incident is under investigation.");
        story.append('\n');
      }
        break;
      default:
        break;
      }
    }
  }

  private static String makestate() {
    return i.rng.randFromArray(MajorEvent.makestate);
  }
}
