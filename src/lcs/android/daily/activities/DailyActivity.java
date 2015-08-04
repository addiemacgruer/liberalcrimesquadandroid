package lcs.android.daily.activities;

import java.util.List;

import lcs.android.creature.Creature;

/** We request that liberals might perform certain activities every day, like selling art, brownies,
 * etc. These aren't usually directed in detail by the player. The various activities are stored as
 * lists, and then the task is started with the daily() method.
 * @author addie */
public interface DailyActivity extends List<Creature> {
  /** Make all liberals in this list perform the given daily activity. */
  void daily();
}
