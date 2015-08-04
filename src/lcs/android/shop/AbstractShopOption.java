package lcs.android.shop;

import lcs.android.creature.Creature;
import lcs.android.site.Squad;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

abstract @NonNullByDefault class AbstractShopOption implements Configurable {
  protected boolean allowSelling = false;

  protected String description = "UNDEFINED";

  protected boolean descriptionDefined = false;

  protected String exit;

  protected boolean fullscreen;

  protected boolean increasePricesWithIllegality;

  protected boolean onlySellLegal;

  protected abstract void choose(Squad customers, Creature buyer);

  protected boolean display() {
    return true;
  }

  protected String getDescriptionFullscreen() {
    return description;
  }

  protected boolean isAvailable() {
    return true;
  }
}