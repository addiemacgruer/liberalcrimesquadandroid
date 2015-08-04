package lcs.android.activities;

import java.io.Serializable;

import lcs.android.activities.iface.Activity;
import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Superclass of all kinds of LCS activity. This @NonNullByDefault class is immutable (although
 * subclasses might not be).
 * @author addie */
public abstract @NonNullByDefault class AbstractActivity implements Serializable {
  protected AbstractActivity(final Activity type) {
    this.type = type;
  }

  protected final Activity type;

  @Override public String toString() {
    switch (type) {
    case REPAIR_ARMOR:
      return "Repairing Clothing";
    case WHEELCHAIR:
      return "Procuring a Wheelchair";
    case STEALCARS:
      return "Stealing a Car";
    case POLLS:
      return "Gathering Opinion Info";
    case TROUBLE:
      return "Causing Trouble";
    case PROSTITUTION:
      return "Prostituting";
    case COMMUNITYSERVICE:
      return "Volunteering";
    case GRAFFITI:
      return "Making Graffiti";
    case CCFRAUD:
      return "Credit Card Fraud";
    case DOS_RACKET:
      return "Extorting Websites";
    case DOS_ATTACKS:
      return "Attacking Websites";
    case HACKING:
      return "Hacking Networks";
    case SELL_TSHIRTS:
      return "Selling T-Shirts";
    case SELL_ART:
      return "Selling Art";
    case TEACH_POLITICS:
      return "Teaching Politics";
    case TEACH_FIGHTING:
      return "Teaching Fighting";
    case TEACH_COVERT:
      return "Teaching Covert Ops";
    case SELL_MUSIC:
      return "Selling Music";
    case BURY:
      return "Disposing of Bodies";
    case DONATIONS:
      return "Soliciting Donations";
    case SELL_DRUGS:
      return "Selling Brownies";
    case HEAL:
      return "Tending to Injuries";
    case NONE:
      return "Laying Low";
    case WRITE_LETTERS:
      return "Writing letters";
    case WRITE_GUARDIAN:
      return "Writing news";
    case CLINIC:
      return "Going to Free CLINIC";
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
      return "Attending Classes";
    case SLEEPER_LIBERAL:
      return "Promoting Liberalism";
    case SLEEPER_CONSERVATIVE:
      return "Spouting Conservatism";
    case SLEEPER_SPY:
      return "Snooping Around";
    case SLEEPER_RECRUIT:
      return "Recruiting Sleepers";
    case SLEEPER_JOINLCS:
      return "Quitting Job";
    case SLEEPER_SCANDAL:
      return "Creating a Scandal";
    case SLEEPER_EMBEZZLE:
      return "Embezzling Funds";
    case SLEEPER_STEAL:
      return "Stealing Equipment";
    default:
      return "Reporting Bugs to the Dev Team";
    }
  }

  public Activity type() {
    return type;
  }

  private static final long serialVersionUID = Game.VERSION;
}