package lcs.android.items;

import static lcs.android.game.Game.*;
import static lcs.android.util.Curses.*;

import java.util.ArrayList;
import java.util.List;

import lcs.android.game.Game;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** The factory blueprints. */
public @NonNullByDefault class VehicleType extends AbstractItemType {
  static class Builder extends AbstractItemTypeBuilder<VehicleType> {
    private final Configurable COLORS = new Configurable() {
      @Override public Configurable xmlChild(String value) {
        return Xml.UNCONFIGURABLE;
      }

      @Override public void xmlFinishChild() {}

      @Override public void xmlSet(String key, String value) {
        if (key.equals("color")) {
          color_.add(Xml.getText(value));
        } else if (key.equals("display_color")) {
          displaycolor_ = Xml.getBoolean(value);
        } else {
          throw new IllegalArgumentException(key + "=" + value);
        }
      }
    };

    private final Configurable YEAR = new Configurable() {
      @Override public Configurable xmlChild(String value) {
        return Xml.UNCONFIGURABLE;
      }

      @Override public void xmlFinishChild() {}

      @Override public void xmlSet(String key, String value) {
        if (key.equals("start_at_current_year")) {
          year_startcurrent_ = Xml.getBoolean(value);
        } else if (key.equals("start_at_year")) {
          year_start_ = Xml.getInt(value);
        } else if (key.equals("add_random_up_to_current_year")) {
          year_randomuptocurrent_ = Xml.getBoolean(value);
        } else if (key.equals("add_random")) {
          year_addrandom_ = Xml.getInt(value);
        } else if (key.equals("add")) {
          year_add_ = Xml.getInt(value);
        } else {
          throw new IllegalArgumentException(key + "=" + value);
        }
      }
    };

    private final Configurable STEALING = new Configurable() {
      @Override public Configurable xmlChild(String value) {
        return Xml.UNCONFIGURABLE;
      }

      @Override public void xmlFinishChild() {}

      @Override public void xmlSet(String key, String value) {
        if (key.equals("difficulty_to_find")) {
          steal_difficultytofind_ = Xml.getInt(value);
        } else if (key.equals("extra_heat")) {
          steal_extraheat_ = Xml.getInt(value);
        } else if (key.equals("sense_alarm_chance")) {
          sensealarmchance_ = Xml.getInt(value);
        } else if (key.equals("juice")) {
          steal_juice_ = Xml.getInt(value);
        } else if (key.equals("touch_alarm_chance")) {
          touchalarmchance_ = Xml.getInt(value);
        } else {
          throw new IllegalArgumentException(key + "=" + value);
        }
      }
    };

    private boolean availableatshop_;

    private final List<String> color_ = new ArrayList<String>();

    private boolean displaycolor_;

    private int drivebonus_;

    private int price_;

    private int sensealarmchance_;

    private int steal_difficultytofind_;

    private int steal_extraheat_;

    private int steal_juice_;

    private int touchalarmchance_;

    private int year_add_;

    private int year_addrandom_;

    private boolean year_randomuptocurrent_;

    private int year_start_;

    private boolean year_startcurrent_;

    @Override public VehicleType build() {
      return new VehicleType(this);
    }

    @Override public Configurable xmlChild(final String value) {
      if ("colors".equals(value)) {
        return COLORS;
      } else if ("stealing".equals(value)) {
        return STEALING;
      } else if ("year".equals(value)) {
        return YEAR;
      }
      return Xml.UNCONFIGURABLE;
    }

    @Override public void xmlFinishChild() {
      Builder ltb = this;
      VehicleType lt = ltb.build();
      Game.type.vehicle.put(lt.idname, lt);
    }

    @Override public void xmlSet(final String key, final String value) {
      if (key.equals("drivebonus")) {
        drivebonus_ = Xml.getInt(value);
      } else if (key.equals("longname")) {
        super.xmlSet("name", value);
      } else if (key.equals("available_at_dealership")) {
        availableatshop_ = Xml.getBoolean(value);
      } else if (key.equals("price")) {
        price_ = Xml.getInt(value);
      } else {
        super.xmlSet(key, value);
      }
    }
  }

  /** @param builder */
  public VehicleType(Builder builder) {
    super(builder);
    availableatshop_ = builder.availableatshop_;
    color_.addAll(builder.color_);
    displaycolor_ = builder.displaycolor_;
    drivebonus_ = builder.drivebonus_;
    price_ = builder.price_;
    sensealarmchance_ = builder.sensealarmchance_;
    steal_difficultytofind_ = builder.steal_difficultytofind_;
    steal_extraheat_ = builder.steal_extraheat_;
    steal_juice_ = builder.steal_juice_;
    touchalarmchance_ = builder.touchalarmchance_;
    year_add_ = builder.year_add_;
    year_addrandom_ = builder.year_addrandom_;
    year_randomuptocurrent_ = builder.year_randomuptocurrent_;
    year_start_ = builder.year_start_;
    year_startcurrent_ = builder.year_startcurrent_;
  }

  private final boolean availableatshop_;

  private final List<String> color_ = new ArrayList<String>();

  private final boolean displaycolor_;

  private final int drivebonus_;

  private final int price_;

  private final int sensealarmchance_;

  private final int steal_difficultytofind_;

  private final int steal_extraheat_;

  private final int steal_juice_;

  private final int touchalarmchance_;

  private final int year_add_;

  private final int year_addrandom_;

  private final boolean year_randomuptocurrent_;

  private final int year_start_;

  private final boolean year_startcurrent_;

  public boolean availableatshop() {
    return availableatshop_;
  }

  public List<String> color() {
    if (color_.size() == 0) {
      color_.add("Translucent");
    }
    return color_;
  }

  @Override public void displayStats(final int viewID) {
    ui(viewID).text("Driving bonus: " + drivebonus_).add();
    ui(viewID).text("Value: $" + price_).add();
  }

  public int price() {
    return price_;
  }

  public int stealDifficultyToFind() {
    return steal_difficultytofind_;
  }

  boolean displayscolor() {
    return displaycolor_;
  }

  int drivebonus() {
    return drivebonus_;
  }

  int makeyear() {
    int myear = 0;
    if (year_startcurrent_) {
      myear = i.score.date.year();
    } else {
      myear = year_start_;
    }
    if (year_randomuptocurrent_) {
      myear += i.rng.nextInt(i.score.date.year() - year_start_ + 1);
    }
    if (year_addrandom_ > 0) {
      myear += i.rng.nextInt(year_addrandom_);
    } else if (year_addrandom_ < 0) {
      myear -= i.rng.nextInt(-year_addrandom_);
    }
    myear += year_add_;
    return myear;
  }

  int sensealarmchance() {
    return sensealarmchance_;
  }

  int steal_extraheat() {
    return steal_extraheat_;
  }

  int steal_juice() {
    return steal_juice_;
  }

  int touchalarmchance() {
    return touchalarmchance_;
  }

  /**
   *
   */
  private static final long serialVersionUID = Game.VERSION;
}
