package lcs.android.util;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

/** Initialises something implementing {@link Xml.Configurable} from an XML file. Android pre-comiles
 * xml to binary, so this is super-fast: takes about a millisecond to configure each item (eg.
 * Armor, WeaponType) on my S2. */
public final @NonNullByDefault class Xml {
  /** Things to be set-up by XML need to implement this. */
  public interface Configurable {
    /** In the event of nested xml values, return something than can deal with it. eg. in the case of
     * &lt;armor>&lt;/armor>, return a new ArmorType(): it will then receive the xmlSet messages
     * contained within the nest. */
    Configurable xmlChild(String value);

    /** Do any finalisation for the new item. You might want to throw an exception for illegal
     * set-up, or calculate values based on the completed item, or add your new item to the Types
     * list. */
    void xmlFinishChild();

    /** set key-value pairs for the item. eg &lt;weight>5&lt;/weight> => xmlSet("weight","5") */
    void xmlSet(String key, String value);
  }

  /** An annotation which gives an XmlName to a @NonNullByDefault class.
   * @author addie */
  @Retention(RetentionPolicy.RUNTIME) public @interface Name {
    /** The XmlName of the @NonNullByDefault class.
     * @return */
    public String name();
  }

  /** creates a new instance using the xml file stored in res/xml/*.xml
   * @param file The filename, eg <q>armors.xml</q> */
  public Xml(final String file) {
    try {
      xrp = Statics.instance().getAssets().openXmlResourceParser("res/xml/" + file);
      xrp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    } catch (final IOException e) {
      throw new LcsRuntimeException("couldn't load file:" + file, e);
    } catch (final XmlPullParserException e) {
      throw new LcsRuntimeException("couldn't parse file:" + file, e);
    }
  }

  /** The names of the tags we've opened while parsing. */
  private final List<String> parents = new ArrayList<String>();

  /** The list of XmlConfigurables we've got open while parsing. */
  private final List<Configurable> xcs = new ArrayList<Configurable>();

  /** The Android XmlResourceParser we're reading from. */
  private final XmlResourceParser xrp;

  /** Close the resource parser. */
  public void close() {
    parents.clear();
    xcs.clear();
    xrp.close();
  }

  /** Applies the new XML instance to the given {@link Configurable}, and closes the file once
   * completed.
   * @param initial The thing to be configured.
   * @throws LcsRuntimeException in the case of any errors: these would be initial set-up mistakes,
   *           and not recoverable.
   * @return this */
  public Xml init(final Configurable initial) throws LcsRuntimeException {
    Configurable xc = initial;
    try {
      xcs.add(xc);
      parseloop: while (true) {
        try {
          final int xrpval = xrp.getEventType();
          switch (xrpval) {
          case XmlPullParser.START_DOCUMENT:
            break;
          case XmlPullParser.END_DOCUMENT:
            break parseloop;
          case XmlPullParser.START_TAG: // 2
            final String tagname = xrp.getName();
            parents.add(tagname);
            if (parents.size() > xcs.size() + 1 && xc != null) {
              xc = xc.xmlChild(parents.get(parents.size() - 2));
              xcs.add(xc);
            }
            break;
          case XmlPullParser.END_TAG: // 3
            final String endname = xrp.getName();
            if (!endname.equals(parents.get(parents.size() - 1))) {
              throw new LcsRuntimeException("Badly formed xml");
            }
            parents.remove(parents.size() - 1);
            if (parents.size() < xcs.size() && xc != null) { // then we've
              // closed two or
              // more tags
              xc.xmlFinishChild();
              xcs.remove(xcs.size() - 1);
              if (xcs.isEmpty()) {
                xc = null;
              } else {
                xc = xcs.get(xcs.size() - 1);
              }
            }
            break;
          case XmlPullParser.TEXT:
            if (xc != null) {
              xc.xmlSet(parents.get(parents.size() - 1), xrp.getText());
            }
            break;
          default:
            if (XmlPullParser.TYPES != null) {
              Log.i(Game.LCS, "Xml event:" + XmlPullParser.TYPES[xrpval]);
            }
            break;
          }
          xrp.next();
        } catch (final XmlPullParserException e) {
          throw new LcsRuntimeException("couldn't parse file", e);
        } catch (final IOException e) {
          throw new LcsRuntimeException("couldn't read from file", e);
        }
      }
      if (!parents.isEmpty()) {
        throw new IllegalArgumentException("Bad XML (insufficient close tags)");
      }
    } finally {
      xrp.close();
    }
    return this;
  }

  public static final Configurable UNCONFIGURABLE = new Configurable() {
    @Override public Configurable xmlChild(final String value) {
      Log.e("LCS", "Tried to get Configurable of EMPTY_CHILD");
      return this;
    }

    @Override public void xmlFinishChild() {
      Log.e("LCS", "Set Xml Finish Child on EMPTY_CHILD:" + this);
    }

    @Override public void xmlSet(final String key, final String value) {
      Log.e("LCS", "Set Xml value of EMPTY_CHILD:" + key + "=" + value);
    }
  };

  /** Convenience method: changes the text <q>true</q> or <q>false</q> to a boolean value. Missing
   * values equal to null will return false.
   * @param value Likely the value passed in by {@link Configurable#xmlSet} */
  public static boolean getBoolean(final CharSequence value) {
    return "true".equals(value);
  }

  /** Convenience method: changes the text value to an integer value. Missing values equal to null
   * will return 0.
   * @param value Likely the value passed in by {@link Configurable#xmlSet} */
  public static int getInt(final CharSequence value) {
    try {
      return Integer.parseInt(value.toString());
    } catch (final NumberFormatException e) {
      Log.e("LCS", "Bad number format for int:" + value, e);
      return 0;
    }
  }

  /** Get the declared name of an XmlName'd @NonNullByDefault class.
   * @param aClass the @NonNullByDefault class to get the name of.
   * @return the @NonNullByDefault class name, or RuntimeException if missing. */
  public static String getName(final Class<?> aClass) {
    if (aClass.isAnnotationPresent(Name.class)) {
      final Name name = aClass.getAnnotation(Name.class);
      return name.name();
    }
    throw new RuntimeException("Class does not have XmlName:" + aClass);
  }

  /** Convenience method: changes the text value to an string value. Missing values equal to null
   * will return <q></q>.
   * @param value Likely the value passed in by {@link Configurable#xmlSet} */
  public static String getText(@Nullable final CharSequence value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }
}
