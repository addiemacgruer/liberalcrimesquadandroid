package lcs.android.politics;

import static lcs.android.game.Game.*;
import lcs.android.util.Color;

public enum Alignment {
  ARCHCONSERVATIVE(-2) {
    @Override public String getTitle(final float juice) {
      return CONSERVATIVE.getTitle(juice);
    }
  },
  CONSERVATIVE(-1) {
    @Override public String getTitle(final float juice) {
      if (juice <= -50) {
        if (!i.freeSpeech()) {
          return ("[Darn] Worthless");
        }
        return ("Damn Worthless");
      } else if (juice <= -10) {
        return ("Conservative Dregs");
      } else if (juice < 0) {
        return ("Conservative Punk");
      } else if (juice < 10) {
        return ("Mindless Conservative");
      } else if (juice < 50) {
        return ("Wrong-Thinker");
      } else if (juice < 100) {
        if (!i.freeSpeech()) {
          return ("Stubborn as [Heck]");
        }
        return ("Stubborn as Hell");
      } else if (juice < 200) {
        if (!i.freeSpeech()) {
          return ("Heartless [Jerk]");
        }
        return ("Heartless Bastard");
      } else if (juice < 500) {
        return ("Insane Vigilante");
      } else if (juice < 1000) {
        return ("Arch-Conservative");
      } else {
        return ("Evil Incarnate");
      }
    }
  },
  ELITELIBERAL(2) {
    @Override public String getTitle(final float juice) {
      return LIBERAL.getTitle(juice);
    }
  },
  LIBERAL(1) {
    @Override public String getTitle(final float juice) {
      if (juice <= -50) {
        if (!i.freeSpeech()) {
          return ("[Darn] Worthless");
        }
        return ("Damn Worthless");
      } else if (juice <= -10) {
        return ("Society's Dregs");
      } else if (juice < 0) {
        return ("Punk");
      } else if (juice < 10) {
        return ("Civilian");
      } else if (juice < 50) {
        return ("Activist");
      } else if (juice < 100) {
        return ("Socialist Threat");
      } else if (juice < 200) {
        return ("Revolutionary");
      } else if (juice < 500) {
        return ("Urban Commando");
      } else if (juice < 1000) {
        return ("Liberal Guardian");
      } else {
        return ("Elite Liberal");
      }
    }
  },
  MODERATE(0) {
    @Override public String getTitle(final float juice) {
      if (juice <= -50) {
        if (!i.freeSpeech()) {
          return ("[Darn] Worthless");
        }
        return ("Damn Worthless");
      } else if (juice <= -10) {
        return ("Society's Dregs");
      } else if (juice < 0) {
        return ("Non-Liberal Punk");
      } else if (juice < 10) {
        return ("Non-Liberal");
      } else if (juice < 50) {
        return ("Hard Working");
      } else if (juice < 100) {
        return ("Respected");
      } else if (juice < 200) {
        return ("Upstanding Citizen");
      } else if (juice < 500) {
        return ("Great Person");
      } else if (juice < 1000) {
        return ("Peacemaker");
      } else {
        return ("Peace Prize Winner");
      }
    }
  };
  private Alignment(final int o) {
    ord = o;
  }

  final int ord;

  public Color color() {
    switch (this) {
    case ARCHCONSERVATIVE:
    case CONSERVATIVE:
      return Color.RED;
    default:
    case MODERATE:
      return Color.WHITE;
    case LIBERAL:
    case ELITELIBERAL:
      return Color.GREEN;
    }
  }

  public abstract String getTitle(float juice);

  public Color lawColor() {
    switch (this) {
    case ARCHCONSERVATIVE:
      return Color.RED;
    case CONSERVATIVE:
      return Color.MAGENTA;
    default:
    case MODERATE:
      return Color.WHITE;
    case LIBERAL:
      return Color.CYAN;
    case ELITELIBERAL:
      return Color.GREEN;
    }
  }

  public Alignment next() {
    switch (this) {
    case ARCHCONSERVATIVE:
      return CONSERVATIVE;
    case CONSERVATIVE:
      return MODERATE;
    case MODERATE:
      return LIBERAL;
    case LIBERAL:
      return ELITELIBERAL;
    default:
      return ELITELIBERAL;
    }
  }

  @Override public String toString() {
    switch (this) {
    case ARCHCONSERVATIVE:
      return "Arch-Conservative";
    case CONSERVATIVE:
      return "Conservative";
    default:
      return "moderate";
    case LIBERAL:
      return "Liberal";
    case ELITELIBERAL:
      return "Elite Liberal";
    }
  }

  public int trueOrdinal() {
    return ord;
  }

  public static Alignment fromInt(final int integer) {
    switch (integer) {
    case -2:
      return ARCHCONSERVATIVE;
    case -1:
      return CONSERVATIVE;
    case 1:
      return LIBERAL;
    case 2:
      return ELITELIBERAL;
    default:
      return MODERATE;
    }
  }
}
