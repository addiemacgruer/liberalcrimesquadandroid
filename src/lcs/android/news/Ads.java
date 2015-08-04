package lcs.android.news;

import static lcs.android.game.Game.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault class Ads {
  private static final String[] sexdesc = { "DTE", "ND", "NS", "VGL" };

  private static final String[] sexseek = { "ISO", "LF" };

  private static final String[] sextype = { "225", "ATM", "BDSM", "CBT", "BJ", "DP", "D/s", "GB",
      "HJ", "OTK", "PNP", "TT", "SWS", "W/S" };

  private static final String[] sexwho = { "BB", "BBC", "BF", "BHM", "BiF", "BiM", "BBW", "BMW",
      "CD", "DWF", "DWM", "FTM", "GAM", "GBM", "GF", "GG", "GHM", "GWC", "GWF", "GWM", "MBC",
      "MBiC", "MHC", "MTF", "MWC", "SBF", "SBM", "SBiF", "SBiM", "SSBBW", "SWF", "SWM", "TG", "TS",
      "TV" };

  // FIXME: These ads can occur more than once on the same newspaper.
  static void displayads(final NewsStory ns, final boolean liberalguardian) {
    int adnumber = 0;
    if (!liberalguardian) {
      if (ns.page >= 10) {
        adnumber++;
      }
      if (ns.page >= 20) {
        adnumber += i.rng.nextInt(2) + 1;
      }
      if (ns.page >= 30) {
        adnumber += i.rng.nextInt(2) + 1;
      }
      if (ns.page >= 40) {
        adnumber += i.rng.nextInt(2) + 1;
      }
      if (ns.page >= 50) {
        adnumber += i.rng.nextInt(2) + 1;
      }
    } /* else { if (NewsStory.guardianpage >= 2) { adnumber++; } if (NewsStory.guardianpage >= 3) {
       * adnumber += i.rng.nextInt(2) + 1; } if (NewsStory.guardianpage >= 4) { adnumber +=
       * i.rng.nextInt(2) + 1; } if (NewsStory.guardianpage >= 5) { adnumber += i.rng.nextInt(2) +
       * 1; } if (NewsStory.guardianpage >= 6) { adnumber += i.rng.nextInt(2) + 1; } } */
    if (adnumber > 6) {
      adnumber = 6;
    }
    while (adnumber > 0) {
      displaySingleAd(liberalguardian);
      adnumber--;
    }
  }

  private static void displaySingleAd(final boolean liberalguardian) {
    // AD CONTENT
    final StringBuilder ad = new StringBuilder();
    if (!liberalguardian) {
      switch (i.rng.nextInt(6)) { // location[]->name?
      default:
        ad.append("&cNo Fee\n");
        ad.append("&cConsignment Program\n\n");
        ad.append("&cCall for Details\n");
        break;
      case 1: {
        final int chairprice = i.rng.nextInt(201) + 400;
        ad.append("&cFine Leather Chairs\n\n");
        ad.append("&cSpecial Purchase\n");
        ad.append("&cNow $" + chairprice);
        ad.append('\n');
        break;
      }
      case 2:
        ad.append("&cParis Flea Market\n\n");
        ad.append("&cSale\n");
        ad.append("&c50% Off\n");
        break;
      case 3: {
        final int caryear = i.score.date.year() - i.rng.nextInt(15);
        final int carprice = i.rng.nextInt(16) + 15;
        // int carprice2 = i.rng.getInt(1000);
        ad.append("&cQuality Pre-Owned\n");
        ad.append("&cVehicles\n");
        ad.append("&c" + caryear);
        ad.append(' ');
        // ad+=cartype;
        ad.append("Lexus GS 300\n");
        ad.append("&cSedan 4D\n");
        ad.append("&cOnly $" + carprice);
        ad.append('\n');
        break;
      }
      case 4:
        ad.append("&cSpa\n");
        ad.append("&cHealth & Beauty\n");
        ad.append("&cand Fitness\n\n");
        ad.append("&c7 Days a Week\n");
        break;
      case 5: {
        ad.append("&c");
        switch (i.rng.nextInt(5)) {
        default:
          ad.append("Searching For Love");
          break;
        case 1:
          ad.append("Seeking Love");
          break;
        case 2:
          ad.append("Are You Lonely?");
          break;
        case 3:
          ad.append("Looking For Love");
          break;
        case 4:
          ad.append("Soulmate Wanted");
          break;
        }
        ad.append("\n\n");
        ad.append("&c");
        ad.append(Ads.sexdesc());
        ad.append(' ');
        ad.append(Ads.sexwho());
        ad.append(' ');
        ad.append(Ads.sexseek());
        ad.append('\n');
        ad.append("&c");
        ad.append(Ads.sextype());
        ad.append(" w/ ");
        ad.append(Ads.sexwho());
        ad.append('\n');
        break;
      }
      }
    } else {
      switch (4) // Liberal Guardian Ads
      {
      default:
        ad.append("&cWant Organic?\n\n");
        ad.append("&cVisit The Vegan\n");
        ad.append("&cCo-Op\n");
        break;
      case 1: {
        final int numyears = i.rng.nextInt(11) + 20;
        ad.append("&cLiberal Defense Lawyer\n");
        ad.append("&c" + numyears);
        ad.append(" Years Experience\n\n");
        ad.append("&cCall Today\n");
        break;
      }
      case 2:
        ad.append("&cAbortion Clinic\n\n");
        ad.append("&cWalk-in+= No\n");
        ad.append("&cQuestions Asked\n");
        ad.append("&cOpen 24/7\n");
        break;
      case 3: {
        ad.append("&c");
        switch (i.rng.nextInt(4)) {
        default:
          ad.append("Searching For Love");
          break;
        case 1:
          ad.append("Seeking Love");
          break;
        case 2:
          ad.append("Are You Lonely?");
          break;
        case 3:
          ad.append("Looking For Love");
          break;
        }
        ad.append("\n\n");
        ad.append("&c");
        ad.append(Ads.sexdesc());
        ad.append(' ');
        ad.append(Ads.sexwho());
        ad.append(' ');
        ad.append(Ads.sexseek());
        ad.append('\n');
        ad.append("&c");
        ad.append(Ads.sextype());
        ad.append(" w/ ");
        ad.append(Ads.sexwho());
        ad.append('\n');
        break;
      }
      }
    }
    News.displaynewsstory(ad);
  }

  private static String sexdesc() {
    return i.rng.randFromArray(Ads.sexdesc);
  }

  private static String sexseek() {
    return i.rng.randFromArray(sexseek);
  }

  private static String sextype() {
    return i.rng.randFromArray(sextype);
  }

  private static String sexwho() {
    return i.rng.randFromArray(sexwho);
  }
}
