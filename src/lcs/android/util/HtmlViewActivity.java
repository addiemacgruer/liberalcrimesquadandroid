package lcs.android.util;

import java.io.IOException;
import java.util.Scanner;

import lcs.android.R;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** displays the html file from assets/*.html as specified in {@link Statics#htmlPage} as a new
 * Android activity. Used for the help file and the license page */
public @NonNullByDefault class HtmlViewActivity extends Activity { // NO_UCD (instantiated by
  // Android Runtime)
  private class HelpClient extends WebViewClient {
    @Override public boolean shouldOverrideUrlLoading(final @Nullable WebView view,
        final @Nullable String url) {
      fillContent();
      return true;
    }
  }

  @Nullable private WebView mWebView;

  /** Called by Android when ready to display. */
  @Override protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.web);
    mWebView = (WebView) findViewById(R.id.webview);
    if (mWebView == null) {
      Log.e("LCS", "Couldn't find R.id.webview");
      return;
    }
    assert mWebView != null;
    mWebView.setWebViewClient(new HelpClient());
    fillContent();
  }

  private void fillContent() {
    Scanner ins;
    final StringBuilder output = new StringBuilder();
    try {
      ins = new Scanner(Statics.instance().getAssets().open(Statics.htmlPage()), "utf-8");
      while (ins.hasNext()) {
        output.append(ins.nextLine() + " ");
      }
    } catch (final IOException e) {
      throw new LcsRuntimeException("failed while reading html page", e);
    }
    assert mWebView != null;
    mWebView.loadDataWithBaseURL(null, output.toString(), "text/html", "utf-8", null);
  }
}
