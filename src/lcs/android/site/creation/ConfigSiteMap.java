package lcs.android.site.creation;

import java.util.ArrayList;
import java.util.List;

import lcs.android.game.Game;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.util.Log;

public @NonNullByDefault class ConfigSiteMap implements Configurable {
  private String name = "";

  private final List<AbstractConfigSiteCommand> commands = new ArrayList<AbstractConfigSiteCommand>();

  @Nullable private AbstractConfigSiteCommand currentCommand;

  private String parent = "";

  @Override public Configurable xmlChild(final String command) {
    if ("tile".equals(command)) {
      currentCommand = new ConfigSiteTile();
      commands.add(currentCommand);
    } else if ("script".equals(command)) {
      currentCommand = new ConfigSiteScript();
      commands.add(currentCommand);
    } else if ("special".equals(command)) {
      currentCommand = new ConfigSiteSpecial();
      commands.add(currentCommand);
    } else if ("unique".equals(command)) {
      currentCommand = new ConfigSiteUnique();
      commands.add(currentCommand);
    } else if ("loot".equals(command)) {
      currentCommand = new ConfigSiteLoot();
      commands.add(currentCommand);
    } else {
      throw new AssertionError("Unknown child:" + command);
    }
    assert currentCommand != null;
    return currentCommand;
  }

  @Override public void xmlFinishChild() {
    if (name.length() == 0) {
      throw new AssertionError("ConfigSiteMap: sitemap with no name defined");
    }
    Game.type.sitemaps.put(name, this);
  }

  @Override public void xmlSet(final String command, final String value) {
    if ("name".equals(command)) {
      name = Xml.getText(value);
    } else if ("use".equals(command)) {
      parent = Xml.getText(value);
    }
  }

  protected void build() {
    if (parent.length() != 0) {
      SiteMap.buildSite(parent);
    }
    for (final AbstractConfigSiteCommand step : commands) {
      Log.d(Game.LCS, "SiteMap.ConfigSiteMap.build:" + step);
      step.build();
    }
  }
}