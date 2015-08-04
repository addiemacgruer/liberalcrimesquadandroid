package lcs.android.scoring;

public enum EndType {
  CCS("The Liberal Crime Squad was out-Crime Squadded in "), //
  CIA("The Liberal Crime Squad was blotted out in "), //
  CORP("The Liberal Crime Squad was downsized in "), //
  DATING("The Liberal Crime Squad was on vacation in "), //
  DEAD("The Liberal Crime Squad was KIA in "), //
  DISBANDLOSS("The Liberal Crime Squad was hunted down in "), //
  DISPERSED("The Liberal Crime Squad was scattered in "), //
  EXECUTED("The Liberal Crime Squad was executed in "), //
  FIREMEN("The Liberal Crime Squad was burned in "), //
  HICKS("The Liberal Crime Squad was mobbed in "), //
  HIDING("The Liberal Crime Squad was in permanent hiding in "), //
  HIGH_SCORE_AGGREGATE(""), //
  POLICE("The Liberal Crime Squad was brought to justice in "), //
  PRISON("The Liberal Crime Squad died in prison in "), //
  REAGAN("The country was Reaganified in "), //
  WON("The Liberal Crime Squad liberalized the country in "),
  QUIT("The struggle was abandoned in ");
  EndType(final String desc) {
    this.desc = desc;
  }

  private final String desc;

  @Override public String toString() {
    return desc;
  }
}