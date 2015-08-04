package lcs.android.news;

import static lcs.android.game.Game.*;
import lcs.android.R;
import lcs.android.util.Color;
import lcs.android.util.Curses;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class Layout {
  static void preparepage(final NewsStory ns, final boolean liberalGuardian) {
    Curses.setView(R.layout.genericnews);
    if (liberalGuardian) {
      Curses.ui().text("The Liberal Guardian").bold().color(Color.BLACKBLACK).add();
    } else {
      Curses
          .ui()
          .text(
              News.newspaperName() + ", " + i.score.date.monthName() + " " + i.score.date.day()
                  + ", " + i.score.date.year()).bold().color(Color.BLACKBLACK).add();
    }
    Curses.ui().text("(page " + ns.page + ")").color(Color.BLACKBLACK).add();
  }
}
