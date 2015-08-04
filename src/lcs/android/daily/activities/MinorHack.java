package lcs.android.daily.activities;

import lcs.android.law.Crime;

enum MinorHack {
  DEFACE("defaced", Crime.INFORMATION),
  HACKED("hacked", Crime.INFORMATION),
  KNOCKOUT("knocked out", Crime.COMMERCE),
  THREATENED("threatened", Crime.SPEECH);
  MinorHack(final String desc, final Crime crime) {
    this.desc = desc;
    this.crime = crime;
  }

  final Crime crime;

  final String desc;
}