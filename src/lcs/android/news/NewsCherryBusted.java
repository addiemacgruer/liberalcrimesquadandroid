package lcs.android.news;

/** Whether we (or the CCS) have been in the news. Basically only changes the headlines in the
 * newspapers when their first event occurs. */
public enum NewsCherryBusted {
  CCS_IN_NEWS,
  LCS_IN_NEWS,
  UNKNOWN
}