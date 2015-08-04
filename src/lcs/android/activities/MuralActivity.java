package lcs.android.activities;

import lcs.android.activities.iface.Activity;
import lcs.android.game.Game;
import lcs.android.politics.Issue;
import lcs.android.util.Maybe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault class MuralActivity extends AbstractActivity {
  public MuralActivity(final Activity type, @Nullable final Issue mural) {
    super(type);
    this.mural = mural;
  }

  @Nullable private final Issue mural;

  public Maybe<Issue> mural() {
    return Maybe.ofNullable(mural);
  }

  private static final long serialVersionUID = Game.VERSION;
}
