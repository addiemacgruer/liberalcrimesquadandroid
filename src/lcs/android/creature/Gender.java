package lcs.android.creature;

public enum Gender {
  FEMALE("her", "Her", "she", "She"),
  MALE("his", "His", "he", "He"),
  NEUTRAL("their", "Their", "its", "Its"),
  WHITEMALEPATRIARCH("his", "His", "he", "He");
  private Gender(final String p, final String pU, final String s, final String sU) {
    possesive = p;
    possesiveUpper = pU;
    subject = s;
    subjectUpper = sU;
  }

  public final String possesive;

  public final String possesiveUpper;

  public final String subject;

  public final String subjectUpper;

  public String heShe() {
    switch (this) {
    case NEUTRAL:
      return "they";
    case FEMALE:
      return "she";
    default:
      return "he";
    }
  }

  @Override public String toString() {
    switch (this) {
    case FEMALE:
      return "Female";
    case NEUTRAL:
      return "Unknown";
    default:
      return "Male";
    }
  }
}
