package lcs.android.activities.iface;

import lcs.android.creature.Creature;
import lcs.android.creature.skill.Skill;
import lcs.android.util.Color;

public enum Activity {
  BURY,
  CCFRAUD,
  CLINIC,
  COMMUNITYSERVICE,
  DONATIONS,
  DOS_ATTACKS,
  DOS_RACKET,
  GRAFFITI,
  HACKING,
  HEAL,
  HOSTAGETENDING,
  MAKE_ARMOR,
  NONE,
  POLLS,
  PROSTITUTION,
  REPAIR_ARMOR,
  SELL_ART,
  SELL_DRUGS,
  SELL_MUSIC,
  SELL_TSHIRTS,
  SLEEPER_CONSERVATIVE,
  SLEEPER_EMBEZZLE, // WRITE_BLOG,
  SLEEPER_JOINLCS,
  SLEEPER_LIBERAL,
  SLEEPER_RECRUIT,
  SLEEPER_SCANDAL,
  SLEEPER_SPY,
  SLEEPER_STEAL,
  STEALCARS,
  STUDY_ART,
  STUDY_BUSINESS,
  STUDY_DEBATING,
  STUDY_DISGUISE,
  STUDY_DRIVING,
  STUDY_FIRST_AID,
  STUDY_GYMNASTICS,
  STUDY_LAW,
  STUDY_MARTIAL_ARTS,
  STUDY_MUSIC,
  STUDY_PSYCHOLOGY,
  STUDY_SCIENCE,
  STUDY_TEACHING,
  STUDY_WRITING,
  TEACH_COVERT,
  TEACH_FIGHTING,
  TEACH_POLITICS,
  TROUBLE,
  VISIT,
  WHEELCHAIR,
  WRITE_GUARDIAN,
  WRITE_LETTERS;
  public String longdesc(final Creature cr) {
    switch (this) {
    case COMMUNITYSERVICE:
      return "%1$s will help the elderly, local library, anything that is liberal.";
    case TROUBLE:
      return "%1$s will create public disturbances. ";
    case GRAFFITI:
      return "%1$s will spray political graffiti. Art and Heart will enhance the liberal effect.";
    case POLLS:
      return "%1$s will search the internet for public opinion polls.  Polls will give an idea on how the liberal agenda is going. Computers and intelligence will provide better results.";
    case DOS_ATTACKS:
      return "%1$s will harass Conservative websites. Computer skill will give greater effect.";
    case HACKING:
      return "%1$s will harass websites and hack private networks.  Computer skill and intelligence will give more frequent results.  Multiple hackers will increase chances of both success and detection.";
    case WRITE_LETTERS:
      return "%1$s will write letters to newspapers about current events.";
    case WRITE_GUARDIAN:
      return "%1$s will write articles for the LCS\'s newspaper.";
    case DONATIONS:
      return "%1$s will walk around and ask for donations to the LCS.  Based on persuasion, public\'s view on the cause, and how well dressed the activist is.";
    case SELL_TSHIRTS:
      if (cr.skill().skill(Skill.TAILORING) > 4)
        return "%1$s will embroider shirts and sell them on the street.";
      return "%1$s will tie-dye T-shirts and sell them on the street.";
    case SELL_ART:
      return "%1$s will sketch people and sell portraits back to them.";
    case SELL_MUSIC:
      return "%1$s will go out into the streets and drum on buckets, or play guitar if one is equipped.";
    case SELL_DRUGS:
      return "%1$s will bake and sell special adult brownies that open magical shimmering doorways to the adamantium pits.";
    case PROSTITUTION:
      return "%1$s will trade sex for money.";
    case CCFRAUD:
      return "%1$s will commit credit card fraud online.";
    case DOS_RACKET:
      return "%1$s will demand money in exchange for not bringing down major websites.";
    case TEACH_POLITICS:
      return "%1$sSkills Trained: Writing, Persuasion, Law, Street Sense, Science, Religion, Business, Music, Art.  Classes cost up to $20/day to conduct. All Liberals able will attend.";
    case TEACH_COVERT:
      return "%1$sSkills Trained: Computers, Security, Stealth, Disguise, Tailoring, Seduction, Psychology, Driving.  Classes cost up to $60/day to conduct. All Liberals able will attend.";
    case TEACH_FIGHTING:
      return "%1$sSkills Trained: All Weapon Skills, Martial Arts, Dodge, First Aid.  Classes cost up to $100/day to conduct. All Liberals able will attend.";
    case STUDY_DEBATING:
    case STUDY_MARTIAL_ARTS:
    case STUDY_DRIVING:
    case STUDY_PSYCHOLOGY:
    case STUDY_FIRST_AID:
    case STUDY_LAW:
    case STUDY_DISGUISE:
    case STUDY_SCIENCE:
    case STUDY_BUSINESS:
    case STUDY_GYMNASTICS:
    case STUDY_WRITING:
    case STUDY_ART:
    case STUDY_MUSIC:
    case STUDY_TEACHING:
      return "%1$s will attend classes in the University District at a cost of $60 a day.";
    default:
      return "";
    }
  }

  public Color set_activity_color() {
    switch (this) {
    // Liberal actvism
    case SLEEPER_LIBERAL:
    case TROUBLE:
    case GRAFFITI:
    case DOS_ATTACKS:
    case HACKING:
    case WRITE_LETTERS:
    case WRITE_GUARDIAN:
      return Color.GREEN;
      // Less exciting liberal activities
    case SLEEPER_SPY:
    case COMMUNITYSERVICE:
    case POLLS:
      return Color.BLUE;
      // Stealing things
    case SLEEPER_STEAL:
    case WHEELCHAIR:
    case STEALCARS:
      return Color.CYAN;
      // Illegal fundraising
    case SLEEPER_EMBEZZLE:
    case SELL_DRUGS:
    case PROSTITUTION:
    case CCFRAUD:
    case DOS_RACKET:
      return Color.RED;
      // Legal fundraising
    case DONATIONS:
    case SELL_TSHIRTS:
    case SELL_ART:
    case SELL_MUSIC:
      return Color.CYAN;
      // Clothing/garment stuff
    case REPAIR_ARMOR:
    case MAKE_ARMOR:
      return Color.CYAN;
      // Teaching
    case TEACH_POLITICS:
    case TEACH_FIGHTING:
    case TEACH_COVERT:
      // and studying
    case STUDY_DEBATING:
    case STUDY_MARTIAL_ARTS:
    case STUDY_DRIVING:
    case STUDY_PSYCHOLOGY:
    case STUDY_FIRST_AID:
    case STUDY_LAW:
    case STUDY_DISGUISE:
    case STUDY_SCIENCE:
    case STUDY_BUSINESS:
    case STUDY_GYMNASTICS:
    case STUDY_ART:
    case STUDY_MUSIC:
    case STUDY_TEACHING:
    case STUDY_WRITING:
      return Color.MAGENTA;
      // Interrogating
    case HOSTAGETENDING:
      return Color.YELLOW;
      // Dealing with your injuries
    case CLINIC:
      return Color.RED;
      // Doing something Conservative
    case SLEEPER_CONSERVATIVE:
      return Color.RED;
      // Dealing with the dead
    case BURY:
      return Color.BLACK;
      // Nothing terribly important
    case HEAL: // Identical to none in practice
    case NONE:
    case VISIT: // Shouldn't show on activate screens at all
    default:
      return Color.WHITE;
    }
  }
}