package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Base methods of the main activity to reduce complexity.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class AbstractBaseMainActivity extends AppCompatActivity {
  private static final int BACK_TIME_DELAY = 2000;
  private long mLastBackPressed = -1;
  private SearchView mSearchView = null;
  private AlertDialog mOrphanDialog = null;
  protected ApplicationCtx mApp = null;

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(((ApplicationCtx) base.getApplicationContext()).onAttach(base));
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApp = (ApplicationCtx) getApplicationContext();

    /* sanity check */
    String[] languages = getResources().getStringArray(R.array.languages_values);
    boolean found = false;
    for (String language : languages)
      if (language.equals(mApp.getApplicationLanguage(this))) {
        found = true;
        break;
      }
    if (!found) {
      mApp.setApplicationLanguage("en-US");
      recreate();
    }

    /* permissions */
    boolean requestPermissions = Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    if (requestPermissions)
      ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
      }, 1);

    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        back();
      }
    });
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mApp.setConfiguration(newConfig);
  }

  protected void setSearchView(MenuItem si) {
    // Searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    if (searchManager != null) {
      mSearchView = (SearchView) si.getActionView();
      mSearchView.setSearchableInfo(searchManager
        .getSearchableInfo(getComponentName()));
      mSearchView.setTextDirection(SysHelper.isRTL(this) ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
      mSearchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
      mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
          return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
          doSearch(s);
          return true;
        }
      });
    }
  }

  /**
   * Sets the visibility of the menu item.
   *
   * @param menu    MenuItem
   * @param visible If true then the item will be visible; if false it is hidden.
   */
  protected void setMenuVisible(final MenuItem menu, final boolean visible) {
    if (menu != null)
      menu.setVisible(visible);
  }

  /**
   * Sets the orphan dialog.
   *
   * @param orphan The dialog.
   */
  public void setOrphanDialog(AlertDialog orphan) {
    if (mOrphanDialog != null && mOrphanDialog.isShowing()) {
      mOrphanDialog.dismiss();
    }
    mOrphanDialog = orphan;
  }

  protected void closeOrphanDialog() {
    if (mOrphanDialog != null) {
      if (mOrphanDialog.isShowing())
        mOrphanDialog.dismiss();
      mOrphanDialog = null;
    }
  }

  /**
   * Performs the research.
   *
   * @param queryStr The query string.
   */
  public abstract void doSearch(String queryStr);

  /**
   * Cancels search.
   */
  protected void cancelSearch() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      doSearch("");
      mSearchView.setIconified(true);
    }
  }

  /**
   * Called to handle the exit of the application.
   */
  protected abstract void onExit();

  /**
   * Called to handle the click on the back button.
   */
  private void back() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      cancelSearch();
    } else {
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        onExit();
        return;
      } else {
        UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
      }
      mLastBackPressed = System.currentTimeMillis();
    }
  }
}
