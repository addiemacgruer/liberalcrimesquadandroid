package lcs.android.site.map;

public enum SuccessTest {
  FAIL_NOISILY(false, true),
  FAIL_QUIETLY(false, false),
  SUCCEED_NOISILY(true, true),
  SUCCEED_QUIETLY(true, false);
  private SuccessTest(final boolean succeeded, final boolean madeNoise) {
    this.succeeded = succeeded;
    this.madeNoise = madeNoise;
  }

  private final boolean madeNoise;

  private final boolean succeeded;

  public boolean madeNoise() {
    return madeNoise;
  }

  public boolean succeeded() {
    return succeeded;
  }
}
