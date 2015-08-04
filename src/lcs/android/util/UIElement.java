package lcs.android.util;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A @NonNullByDefault class for adding discrete elements (textboxes and buttons, though extensible)
 * to the xml layout views. */
public @NonNullByDefault class UIElement {
  /** Builder @NonNullByDefault class for all UI elements. Likely use {@link Curses#ui} to gain an
   * instance of this builder. Call {@link #add} once completed to add a concrete instance to the
   * current xml layout.
   * <p>
   * Example use:<br>
   * ui().text("Example").ch('a').add(); */
  public final static class UIBuilder {
    /** Get new builder. Must specify the parent View, which has already been instantiated.
     * @param parent The parent View */
    UIBuilder(final int parent) {
      this.parent = parent;
    }

    private boolean bold;

    private boolean button = false;

    private int c;

    private boolean centered = false;

    private Color color = Color.BLACK;

    private boolean headline = false;

    private boolean narrow = false;

    private final int parent;

    private final StringBuilder text = new StringBuilder();

    /** Finalises the builder, creates an appropriate concrete UI element, and adds it to the current
     * display. Must be the final action in the builder chain.
     * @throws IllegalStateException if you've not specified any text for this element. */
    public void add() throws IllegalStateException {
      AbstractElement e;
      if (button) {
        e = new UIButton(this);
      } else {
        e = new UIText(this);
      }
      Statics.instance().uiHandler.obtainMessage(IProtocol.ADD_ELEMENT, e).sendToTarget();
    }

    /** Makes the text bold. nb. Android doesn't support buttons with bold text.
     * @return The UIBuilder chain. */
    public UIBuilder bold() {
      if (button) {
        Log.w(Game.LCS, "Tried to make a bold button");
      }
      bold = true;
      return this;
    }

    /** Makes the element a (disabled) button. nb. Android doesn't support buttons with bold text.
     * @return the UIBuilder chain. */
    public UIBuilder button() {
      if (bold) {
        Log.w(Game.LCS, "Tried to make a bold button");
      }
      button = true;
      return this;
    }

    /** Sets the <q>keyboard response</q> for this button. Makes it a button, too. This is what gets
     * passed on to getch() and friends. Buttons with no input will be disabled.
     * @param character The <q>key</q> that the button corresponds to. Typically an ASCII value.
     * @return the UIBuilder chain. */
    public UIBuilder button(final int character) {
      if (button) {
        Log.w("LCS", "Made a button a button", new IllegalArgumentException("doubled"));
      }
      button = true;
      c = character;
      return this;
    }

    /** Centers the text. Buttons are already centered by default.
     * @return the UIBuilder chain */
    public UIBuilder center() {
      if (button) {
        Log.w(Game.LCS, "Tried to make a centered button");
      }
      centered = true;
      return this;
    }

    /** Sets the text color of the element
     * @param aColor
     * @return the UIBuilder chain. */
    public UIBuilder color(final Color aColor) {
      color = aColor;
      return this;
    }

    /** Makes the text <q>headline size</q> for newspaper articles. nb. unused for buttons.
     * @return the UIBuilder chain. */
    public UIBuilder headline() {
      if (button) {
        Log.w(Game.LCS, "Tried to make a headline button");
      }
      headline = true;
      return this;
    }

    /** Removes the margins from a TextView, to make lists fit together in less space.
     * @return the UIBuilder chain. */
    public UIBuilder narrow() {
      narrow = true;
      return this;
    }

    /** Adds to the TextView / Button text from a resource file (uses a StringBuilder).
     * @param resId Typically an R.string.something value;
     * @return the UIBuilder chain */
    public UIBuilder restext(final int resId) {
      try {
        text.append(Statics.instance().getString(resId));
      } catch (final NotFoundException e) {
        Log.e(Game.LCS, "String resource not found: " + Integer.toHexString(resId), e);
        text.append("String resource not found: " + Integer.toHexString(resId));
      }
      return this;
    }

    /** Adds text to the TextView / Button text
     * @param aText The text
     * @return the UIBuilder chain. */
    public UIBuilder text(final CharSequence aText) {
      text.append(aText);
      return this;
    }
  }

  /** Discrete element types all inherit from abstract element. Constructors are protected, use the
   * builder @NonNullByDefault class {@link UIBuilder} to instantiate. */
  abstract static class AbstractElement {
    protected AbstractElement(final UIBuilder u) { // NO_UCD
      parent = u.parent;
      text = u.text.toString();
      color = u.color;
    }

    protected final Color color;

    protected final int parent;

    protected final String text;

    @Override public String toString() {
      return this.getClass().toString() + ":" + text;
    }

    /** Add the UI element to the display.
     * @param a The GUI thread
     * @param ocl The listener @NonNullByDefault class. */
    abstract protected void add(Activity a, OnClickListener ocl);
  }

  private final static class UIButton extends AbstractElement {
    private UIButton(final UIBuilder u) {
      super(u);
      c = u.c;
    }

    private final int c;

    @Override protected void add(final Activity a, final OnClickListener ocl) {
      final Button b = new Button(a);
      b.setText(text);
      final LinearLayout v = (LinearLayout) a.findViewById(parent);
      b.setTag(Character.toString((char) c));
      b.setOnClickListener(ocl);
      v.addView(b);
      b.setTextColor(color.androidValue());
      if (c == 0) {
        b.setEnabled(false);
      }
    }
  }

  private final static class UIText extends AbstractElement {
    private UIText(final UIBuilder u) {
      super(u);
      bold = u.bold;
      narrow = u.narrow;
      centered = u.centered;
      headline = u.headline;
    }

    private final boolean bold;

    private final boolean centered;

    private final boolean headline;

    private final boolean narrow;

    @Override protected void add(final Activity a, final OnClickListener ocl) {
      final TextView tv = new TextView(a);
      if (!narrow) {
        tv.setPadding(0, 0, 0, 10);
      }
      tv.setText(text);
      final LinearLayout v = (LinearLayout) a.findViewById(parent);
      tv.setTextColor(color.androidValue());
      if (bold) {
        tv.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
      }
      if (centered) {
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
      }
      if (headline) {
        tv.setTextSize(22);
      }
      v.addView(tv);
    }
  }
}
