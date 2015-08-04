package lcs.android.daily.activities;

import static lcs.android.game.Game.*;
import lcs.android.game.CheckDifficulty;
import lcs.android.law.Crime;
import lcs.android.politics.Issue;

import org.eclipse.jdt.annotation.Nullable;

enum HackTarget {
  CIA("caused a scare by breaking into a CIA network.", CheckDifficulty.SUPERHEROIC,
      Crime.INFORMATION, 25, Issue.PRIVACY, 10),
  CORPORATE("pilfered files from a Corporate server.", new String[] { "LOOT_CORPFILES" },
      CheckDifficulty.FORMIDABLE, Crime.INFORMATION, 10),
  GENETICS("sabotaged a genetics research company's network.", CheckDifficulty.FORMIDABLE,
      Crime.INFORMATION, 10, Issue.GENETICS, 2),
  JUDICIAL("discovered evidence of judicial corruption.", new String[] { "LOOT_JUDGEFILES" },
      CheckDifficulty.FORMIDABLE, Crime.INFORMATION, 10),
  MEDIA("intercepted internal media emails.", new String[] { "LOOT_CABLENEWSFILES",
      "LOOT_AMRADIOFILES" }, CheckDifficulty.FORMIDABLE, Crime.INFORMATION, 10),
  MILITARY("broke into military networks leaving LCS slogans.", CheckDifficulty.SUPERHEROIC,
      Crime.INFORMATION, 10, Issue.LIBERALCRIMESQUAD, 5),
  SCIENCE("uncovered information on dangerous research.", new String[] { "LOOT_RESEARCHFILES" },
      CheckDifficulty.FORMIDABLE, Crime.INFORMATION, 10);
  HackTarget(final String desc, final CheckDifficulty trackdif, final Crime crime, final int juice,
      final Issue changeOpinion, final int howMuch) {
    this.desc = desc;
    loot = new String[] {};
    difficulty = trackdif;
    this.crime = crime;
    this.juice = juice;
    this.changeOpinion = changeOpinion;
    this.howMuch = howMuch;
  }

  HackTarget(final String desc, final String[] loot, final CheckDifficulty trackdif,
      final Crime crime, final int juice) {
    this.desc = desc;
    this.loot = loot;
    difficulty = trackdif;
    this.crime = crime;
    this.juice = juice;
    changeOpinion = null;
    howMuch = 0;
  }

  @Nullable final Issue changeOpinion;

  final Crime crime;

  final String desc;

  final CheckDifficulty difficulty;

  final int howMuch;

  final int juice;

  final String[] loot;

  void changePublicOpinion() {
    if (changeOpinion != null) {
      i.issue(changeOpinion).changeOpinion(howMuch, 0, 75);
    }
  }
}