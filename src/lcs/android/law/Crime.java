package lcs.android.law;

import static lcs.android.game.Game.*;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.util.DefaultValueKey;

public enum Crime implements DefaultValueKey<Integer> {
  ARMEDASSAULT("armed assault"),
  ARSON(5, "arson"),
  ASSAULT("assault"),
  BREAKING("breaking and entering"),
  BROWNIES(5, "drug dealing"),
  BURIAL("unlawful burial"),
  BURNFLAG("") {
    @Override public String toString() {
      return i.issue(Issue.FLAGBURNING).law() == Alignment.ARCHCONSERVATIVE ? "flag murder"
          : "flag burning";
    }
  },
  CARTHEFT("motor theft"),
  CCFRAUD(2, "credit card fraud"),
  COMMERCE(2, "interfering with interstate commerce"),
  DISTURBANCE("disturbing the peace"),
  ESCAPED(5, "escaping prison"),
  EXTORTION(1, "extortion"),
  HELPESCAPE(5, "aiding a prison escape"),
  HIREILLEGAL(1, "hiring an illegal alien"),
  INFORMATION(5, "unlawfully accessing an information system"),
  JURY("jury tampering"),
  KIDNAPPING(2, "kidnapping"),
  LOITERING("loitering"),
  MURDER(2, "murder"),
  PROSTITUTION("prostitution"),
  PUBLICNUDITY("public indecency"),
  RACKETEERING(5, "racketeering"),
  RESIST(1, "resisting arrest"),
  SPEECH("harmful speech"),
  TERRORISM(10, "terrorism"),
  THEFT("theft"),
  TREASON(10, "treason"),
  VANDALISM("vandalism");
  /** A crime with some heat
   * @param heat how much heat */
  private Crime(final int heat, final String description) {
    this.heat = heat;
    mDescription = description;
  }

  /** A crime with no heat */
  private Crime(final String description) {
    heat = 0;
    mDescription = description;
  }

  private final int heat;

  private final String mDescription;

  @Override public Integer defaultValue() {
    return Integer.valueOf(0);
  }

  /** returns the amount of heat associated with a given crime. Note that for the purposes of this
   * function, we're not looking at how severe the crime is, but how vigorously it is pursued by law
   * enforcement. This determines how quickly they raid you for it, and how much of a penalty you
   * get in court for it. Some crimes are inflated heat, others are deflated (such as the violent
   * crimes). - Jonathan S. Fox
   * @return the amount of heat */
  public int heat() {
    return heat;
  }

  @Override public String toString() {
    return mDescription;
  }

  /** When the cops come knocking, this is the priority list. police won't raid for public nudity,
   * loitering */
  public static final Crime[] PRIORITY = { TREASON, TERRORISM, MURDER, KIDNAPPING, BURNFLAG,
      SPEECH, BROWNIES, ESCAPED, HELPESCAPE, JURY, RACKETEERING, EXTORTION, ARMEDASSAULT, ASSAULT,
      ARSON, CARTHEFT, CCFRAUD, THEFT, PROSTITUTION, HIREILLEGAL, COMMERCE, INFORMATION, BURIAL,
      BREAKING, VANDALISM, RESIST, DISTURBANCE };
}