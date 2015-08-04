package lcs.android.util;

import java.util.Iterator;
import java.util.Map.Entry;

import lcs.android.game.Game;
import lcs.android.util.UIElement.AbstractElement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** The GUI thread @NonNullByDefault class for LCS: responsible for drawing and updating the screen,
 * and responding to events */
@SuppressLint("HandlerLeak") public @NonNullByDefault class LiberalCrimeSquadActivity extends
    Activity implements IProtocol, // NO_UCD
    OnClickListener {
  /** This actually does the work of keeping the GUI up-to-date. Use:<br>
   * Statics.instance().uiHandler.sendEmptyMessage(Protocol.SOMETHING);<br>
   * Statics.instance().uiHandler.obtainMessage(Protocol.SOMETHING, AnObject).sendMessage();
   * @see IProtocol */
  public class LCSHandler extends Handler implements OnClickListener { // NO_UCD
    @Override public void handleMessage(final @Nullable Message msg) {
      if (msg == null)
        return;
      switch (msg.what) {
      case UPDATE_VIEW:
        Statics.setLastView((Integer) msg.obj);
        clearLastDisplay();
        //$FALL-THROUGH$
      case ON_CREATE:
        restoreDisplay();
        break;
      case ON_CLICK:
        Log.d(Game.LCS, "UI onClick (activity)");
        Statics.GETCH.setKeyPressed(' ');
        break;
      case CONTROL_FORCE_CLICK:
        Log.d(Game.LCS, "UI force click");
        Statics.GETCH.setKeyPressed(' ');
        break;
      case ON_TOUCH_EVENT:
        Log.d(Game.LCS, "UI onTouchEvent");
        Statics.GETCH.setKeyPressed(' ');
        break;
      case ON_START:
      case ON_RESUME:
      case ON_PAUSE:
      case ON_STOP:
      case ON_DESTROY:
        break;
      case UPDATE_TEXT:
      case UPDATE_COLOR:
        final View v = findViewById(msg.arg1);
        if (v == null) {
          Log.e(Game.LCS, "UI update-text couldn't find view:" + msg.arg1 + " (" + msg.obj + ")");
          break;
        }
        if (!(v instanceof TextView || v instanceof EditText)) {
          Log.e(Game.LCS, "UI update-text not a textview:" + msg.arg1 + " (" + msg.obj + ")");
          break;
        }
        if (msg.what == UPDATE_TEXT) {
          Statics.VIEWTEXT.put(msg.arg1, (String) msg.obj);
          ((TextView) v).setText((String) msg.obj);
        } else {
          int c = Color.BLUE.androidValue();
          if (msg.obj != null) {
            c = ((Color) msg.obj).androidValue();
          }
          Statics.VIEWCOLOR.put(msg.arg1, c);
          ((TextView) v).setTextColor(c);
        }
        break;
      case UPDATE_ENABLED:
        final View ve = findViewById(msg.arg1);
        if (ve == null) {
          Log.e(Game.LCS, "UI update-text couldn't find view:" + msg.arg1);
          break;
        }
        Statics.VIEWENABLED.put(msg.arg1, (Boolean) msg.obj);
        ve.setEnabled((Boolean) msg.obj);
        break;
      case UPDATE_INFLATE:
        final View vs = findViewById(msg.arg1);
        if (!(vs instanceof ViewStub)) {
          Log.e(Game.LCS, "UI inflate couldn't find viewstub:" + msg.arg1);
          break;
        }
        Statics.INFLATE.add(msg.arg1);
        ((ViewStub) vs).inflate();
        setListeners((ViewGroup) getWindow().getDecorView().getRootView());
        break;
      case DISPLAY_TOAST:
        Toast.makeText(LiberalCrimeSquadActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
        Log.i(Game.LCS, (String) msg.obj);
        break;
      case ADD_ELEMENT:
        final UIElement.AbstractElement e = (AbstractElement) msg.obj;
        final View p = findViewById(e.parent);
        if (p == null) {
          Log.e(Game.LCS, "Add couldn't find parent:" + msg.arg1 + " (" + msg.obj + ")");
          break;
        }
        Statics.EXTRAS.add(e);
        e.add(LiberalCrimeSquadActivity.this, this);
        break;
      case CONTROL_DELETE_SAVE:
        deleteFile((String) msg.obj);
        break;
      case CLEAR_CHILDREN:
        final View c = findViewById(msg.arg1);
        if (c == null) {
          Log.e(Game.LCS, "Add couldn't find parent to clear:" + msg.arg1);
          break;
        }
        ((ViewGroup) c).removeAllViews();
        for (final Iterator<UIElement.AbstractElement> ci = Statics.EXTRAS.iterator(); ci.hasNext();) {
          final UIElement.AbstractElement ex = ci.next();
          if (ex.parent == msg.arg1) {
            ci.remove();
          }
        }
        break;
      case CONTROL_HELP:
      case SHOW_HELP_PAGE:
        Statics.setHtmlPage("CrimeSquadManual.html");
        Intent i = new Intent(LiberalCrimeSquadActivity.this, HtmlViewActivity.class);
        startActivity(i);
        break;
      case CONTROL_ABOUT:
      case SHOW_LICENSE:
        Statics.setHtmlPage("license.html");
        i = new Intent(LiberalCrimeSquadActivity.this, HtmlViewActivity.class);
        startActivity(i);
        break;
      case SET_THEME:
        final ThemeName t = (ThemeName) msg.obj;
        setTheme(t.styleName());
        break;
      default:
        Log.w(Game.LCS, "UI oops - " + msg.what + ":" + msg);
      }
    }

    @Override public void onClick(@Nullable final View v) {
      int kp = ' ';
      if (v != null && v.getTag() != null) {
        kp = ((String) v.getTag()).charAt(0);
      }
      Log.i(Game.LCS, "UI onClick = '" + kp + "' (" + (kp > 13 ? (char) kp : '#') + ")");
      Statics.GETCH.setKeyPressed(kp);
    }

    private void clearLastDisplay() {
      Statics.VIEWTEXT.clear();
      Statics.VIEWCOLOR.clear();
      Statics.VIEWENABLED.clear();
      Statics.INFLATE.clear();
      Statics.EXTRAS.clear();
    }

    private void restoreDisplay() {
      ThemeName.restoreTheme();
      if (Statics.lastView() != 0) {
        setContentView(Statics.lastView());
        for (final Integer i : Statics.INFLATE) {
          ((ViewStub) findViewById(i)).inflate();
        }
        for (final Entry<Integer, String> i : Statics.VIEWTEXT.entrySet()) {
          final TextView tv = (TextView) findViewById(i.getKey());
          tv.setText(i.getValue());
        }
        for (final Entry<Integer, Integer> i : Statics.VIEWCOLOR.entrySet()) {
          final TextView tv = (TextView) findViewById(i.getKey());
          tv.setTextColor(i.getValue());
        }
        for (final Entry<Integer, Boolean> i : Statics.VIEWENABLED.entrySet()) {
          final TextView tv = (TextView) findViewById(i.getKey());
          tv.setEnabled(i.getValue());
        }
        for (final UIElement.AbstractElement e : Statics.EXTRAS) {
          e.add(LiberalCrimeSquadActivity.this, this);
        }
        setListeners((ViewGroup) getWindow().getDecorView().getRootView());
      }
    }

    private void setListeners(@Nullable final ViewGroup vg) {
      if (vg == null) {
        Log.e(Game.LCS, "UI setListeners viewgroup is null");
        return;
      }
      for (int i = 0, j = vg.getChildCount(); i < j; i++) {
        final View v = vg.getChildAt(i);
        final Class<? extends View> c = v.getClass();
        if (v instanceof ViewGroup) {
          v.setOnClickListener(this);
          setListeners((ViewGroup) v);
        } else if (c == Button.class || c == CheckBox.class) {
          ((Button) v).setOnClickListener(this);
        } else if (c == ScrollView.class) {
          ((ScrollView) v).setOnClickListener(this);
        }
      }
    }
  }

  public final Handler uiHandler = new LCSHandler();

  private MenuItem[] mis = new MenuItem[0];

  private final int[] responses = { IProtocol.CONTROL_ABOUT, IProtocol.CONTROL_HELP,
      IProtocol.CONTROL_DELETE_SAVE, IProtocol.CONTROL_FORCE_CLICK };

  private final String[] text = { "About", "Help", "Delete Save", "Force Click" };

  /** If there's a text box on-screen, get the currently entered value, eg. if we're asking the user
   * their name to the people.
   * @param viewId A TextEditView, presumably, eg. R.id.something
   * @return Some text, or an error message. */
  public String getEditText(final int viewId) {
    final View v = findViewById(viewId);
    if (v == null) {
      Log.e(Game.LCS, "Did not find ViewId:" + viewId);
      return "I AM ERROR";
    }
    if (!(v instanceof EditText)) {
      Log.e(Game.LCS, "ViewID is not EditText:" + viewId);
      return "I AM ERROR";
    }
    return ((EditText) v).getText().toString();
  }

  @Override public void onClick(final @Nullable View v) {
    uiHandler.sendEmptyMessage(ON_CLICK);
  }

  /** Android calls this to start us up. Lets {@link Statics} know we're here, makes us fullscreen,
   * and redraws what was on the screen before, if anything. */
  @Override public void onCreate(final @Nullable Bundle savedInstanceState) {
    Log.d(Game.LCS, "Hello, world!");
    Statics.setInstance(this);
    ThemeName.restoreTheme();
    setTheme(ThemeName.currentTheme.styleName());
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    uiHandler.sendEmptyMessage(ON_CREATE);
  }

  @Override public boolean onCreateOptionsMenu(final @Nullable Menu menu) {
    Log.d("Control", "OCOM");
    if (menu == null)
      return false;
    mis = new MenuItem[text.length];
    for (int j = 0; j < text.length; j++) {
      mis[j] = menu.add(text[j]);
    }
    return true;
  }

  @Override public boolean onOptionsItemSelected(final @Nullable MenuItem item) {
    int selected = -1;
    for (int i = 0; i < mis.length; ++i) {
      if (item == mis[i]) {
        selected = i;
      }
    }
    uiHandler.sendEmptyMessage(responses[selected]);
    return true;
  }

  @Override public boolean onTouchEvent(final @Nullable MotionEvent event) {
    uiHandler.obtainMessage(ON_TOUCH_EVENT, event).sendToTarget();
    return true;
  }

  @SuppressLint("NewApi") public void restart() {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      Statics.instance().runOnUiThread(new Runnable() {
        @Override public void run() {
          recreate();
        }
      });
    } else { // then we are on an older version of android
      final LiberalCrimeSquadActivity lcsa = Statics.instance();
      final Intent i = lcsa.getBaseContext().getPackageManager()
          .getLaunchIntentForPackage(lcsa.getBaseContext().getPackageName());
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      lcsa.startActivity(i);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    uiHandler.sendEmptyMessage(ON_DESTROY);
  }

  @Override protected void onPause() {
    super.onPause();
    uiHandler.sendEmptyMessage(ON_PAUSE);
  }

  @Override protected void onResume() {
    super.onResume();
    uiHandler.sendEmptyMessage(ON_RESUME);
  }

  @Override protected void onStart() {
    super.onStart();
    uiHandler.sendEmptyMessage(ON_START);
  }

  @Override protected void onStop() {
    super.onStop();
    uiHandler.sendEmptyMessage(ON_STOP);
  }
}