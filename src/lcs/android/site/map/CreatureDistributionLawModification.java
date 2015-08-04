package lcs.android.site.map;

import static lcs.android.game.Game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import lcs.android.creature.CreatureType;
import lcs.android.politics.Alignment;
import lcs.android.politics.Issue;
import lcs.android.site.type.AbstractSiteType;
import lcs.android.util.Xml;
import lcs.android.util.Xml.Configurable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Allow creatures which may be found at sites under certain legal conditions, eg. mutants appearing
 * when pollution is high.
 * @author addie */
public @NonNullByDefault class CreatureDistributionLawModification implements Configurable {
  /** Attach a CDLM to a given site type
   * @param parent the site type. */
  public CreatureDistributionLawModification(final AbstractSiteType parent) {
    this.parent = parent;
  }

  private final Map<CreatureType, Integer> legalarray = new HashMap<CreatureType, Integer>();

  @Nullable private AbstractSiteType parent;

  private final List<String> reqs = new ArrayList<String>();

  /** Modify the creature distribution at a site.
   * @param ast a site. */
  public void apply(final AbstractSiteType ast) {
    boolean applies = false;
    each: for (final String req : reqs) {
      for (final String ands : req.split("\\+")) {
        final String[] and = ands.split("=");
        if (i.issue(Issue.valueOf(and[0].toUpperCase(Locale.US))).law() == Alignment
            .fromInt(Integer.valueOf(and[1]))) {
          applies = true;
        } else {
          applies = false;
          continue each;
        }
      }
      if (applies) {
        break each;
      }
    }
    if (!applies) {
      return;
    }
    for (final Entry<CreatureType, Integer> e : legalarray.entrySet()) {
      AbstractSiteType.adjust(ast.creaturearray, e.getKey(), e.getValue());
    }
  }

  @Override public Configurable xmlChild(final String value) {
    return this;
  }

  @Override public void xmlFinishChild() {
    if (parent == null) {
      throw new AssertionError("Parent was null");
    }
    assert parent != null;
    parent.xmlFinishChild();
    parent = null;
  }

  @Override public void xmlSet(final String key, final String value) {
    if ("req".equals(key)) {
      reqs.add(Xml.getText(value));
    } else {
      legalarray.put(CreatureType.valueOf(key.toUpperCase(Locale.US)), Xml.getInt(value));
    }
  }
}