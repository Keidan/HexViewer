package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.activities.settings.SettingsActivity;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.dialog.GoToDialog;
import fr.ralala.hexviewer.ui.launchers.LauncherLineUpdate;
import fr.ralala.hexviewer.ui.launchers.LauncherOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherRecentlyOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherSave;
import fr.ralala.hexviewer.ui.payload.PayloadHexHelper;
import fr.ralala.hexviewer.ui.payload.PayloadPlainSwipe;
import fr.ralala.hexviewer.ui.popup.MainPopupWindow;
import fr.ralala.hexviewer.ui.popup.PopupCheckboxHelper;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private FileData mFileData = null;
  private ConstraintLayout mIdleView = null;
  private MenuItem mSearchMenu = null;
  private SearchView mSearchView = null;
  private String mSearchQuery = "";
  private PayloadPlainSwipe mPayloadPlainSwipe = null;
  private AlertDialog mOrphanDialog = null;
  private LauncherLineUpdate mLauncherLineUpdate = null;
  private LauncherSave mLauncherSave = null;
  private LauncherOpen mLauncherOpen = null;
  private LauncherRecentlyOpen mLauncherRecentlyOpen = null;
  private UnDoRedo mUnDoRedo = null;
  private MainPopupWindow mPopup = null;
  private PayloadHexHelper mPayloadHexHelper = null;
  private GoToDialog mGoToDialog = null;

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(ApplicationCtx.getInstance().onAttach(base));
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mApp = ApplicationCtx.getInstance();

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

    mUnDoRedo = new UnDoRedo(this);

    mPopup = new MainPopupWindow(this, mUnDoRedo, this::onPopupItemClick);

    LinearLayout mainLayout = findViewById(R.id.mainLayout);
    mIdleView = findViewById(R.id.idleView);
    mIdleView.setVisibility(View.VISIBLE);

    findViewById(R.id.buttonOpenFile).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_open));
    findViewById(R.id.buttonRecentlyOpen).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_recently_open));
    findViewById(R.id.buttonRecentlyOpen).setEnabled(!mApp.getRecentlyOpened().isEmpty());
    mPayloadHexHelper = new PayloadHexHelper();
    mPayloadHexHelper.onCreate(this);

    mPayloadPlainSwipe = new PayloadPlainSwipe();
    mPayloadPlainSwipe.onCreate(this);

    /* permissions */
    boolean requestPermissions = true;
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
          ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        requestPermissions = false;
      }
    }
    if (requestPermissions)
      ActivityCompat.requestPermissions(this, new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
      }, 1);

    mLauncherOpen = new LauncherOpen(this, mainLayout);
    mLauncherSave = new LauncherSave(this);
    mLauncherLineUpdate = new LauncherLineUpdate(this);
    mLauncherRecentlyOpen = new LauncherRecentlyOpen(this);

    mGoToDialog = new GoToDialog(this);

    if (savedInstanceState == null)
      handleIntent(getIntent());
  }

  /**
   * Called when the activity is resumed.
   */
  public void onResume() {
    super.onResume();
    setRequestedOrientation(mApp.getScreenOrientation(null));
    if (mPopup != null)
      mPopup.dismiss();
    mApp.applyApplicationLanguage(this);
    /* refresh */
    onOpenResult(!FileData.isEmpty(mFileData), false);
    if (mPayloadHexHelper.isVisible())
      mPayloadHexHelper.refreshAdapter();
    else if (mPayloadPlainSwipe.isVisible())
      mPayloadPlainSwipe.refreshAdapter();
  }

  /**
   * Handles activity intents.
   *
   * @param intent The intent.
   */
  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
      doSearch(mSearchQuery == null ? "" : mSearchQuery);
    } else {
      if (intent.getData() != null) {
        closeOrphanDialog();
        Uri uri = getIntent().getData();
        if (uri != null) {
          boolean addRecent;
          if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            addRecent = false;
          } else
            addRecent = FileHelper.takeUriPermissions(this, uri, false);
          final Runnable r = () -> mLauncherOpen.processFileOpen(uri, true, addRecent);
          if (mUnDoRedo.isChanged()) {// a save operation is pending?
            UIHelper.confirmFileChanged(this, mFileData, r,
                () -> new TaskSave(this, this).execute(
                    new TaskSave.Request(mFileData.getUri(), mPayloadHexHelper.getAdapter().getItems(), r)));
          } else {
            r.run();
          }
        }
      }
    }
  }

  private void closeOrphanDialog() {
    if (mOrphanDialog != null) {
      if (mOrphanDialog.isShowing())
        mOrphanDialog.dismiss();
      mOrphanDialog = null;
    }
  }

  /**
   * Called to create the option menu.
   *
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    MenuCompat.setGroupDividerEnabled(menu, true);

    mSearchMenu = menu.findItem(R.id.action_search);
    mSearchMenu.setVisible(false);
    // Searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    if (searchManager != null) {
      mSearchView = (SearchView) mSearchMenu.getActionView();
      mSearchView.setSearchableInfo(searchManager
          .getSearchableInfo(getComponentName()));
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
    return true;
  }

  /**
   * This is called for activities that set launchMode to "singleTop" in their package,
   * or if a client used the Intent#FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
   *
   * @param intent The new intent that was started for the activity.
   */
  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
  }

  /**
   * Performs the research.
   *
   * @param queryStr The query string.
   */
  public void doSearch(String queryStr) {
    mSearchQuery = queryStr;
    final SearchableListArrayAdapter<?> laa = ((mPayloadPlainSwipe.isVisible()) ?
        mPayloadPlainSwipe.getAdapter() : mPayloadHexHelper.getAdapter());
    laa.getFilter().filter(queryStr);
  }

  /**
   * Method called when the file is saved.
   *
   * @param uri          The new Uri.
   * @param success      The result.
   * @param userRunnable User runnable (can be null).
   */
  @Override
  public void onSaveResult(Uri uri, boolean success, final Runnable userRunnable) {
    if (success) {
      mUnDoRedo.refreshChange();
      if (mFileData.isOpenFromAppIntent()) {
        mFileData = new FileData(uri, false);
        if (mFileData.isOpenFromAppIntent())
          mFileData.clearOpenFromAppIntent();
        if (mPopup != null)
          mPopup.setSaveMenuEnable(true);
        setTitle(getResources().getConfiguration());
      } else {
        mFileData = new FileData(uri, false);
        setTitle(getResources().getConfiguration());
      }
      mPayloadHexHelper.resetUpdateStatus();
    } else
      mApp.removeRecentlyOpened(uri.toString());
    if (userRunnable != null)
      userRunnable.run();
  }

  /**
   * Method called when the file is opened.
   *
   * @param success  The result.
   * @param fromOpen Called from open
   */
  @Override
  public void onOpenResult(boolean success, boolean fromOpen) {
    setMenuVisible(mSearchMenu, success);
    boolean checked = mPopup != null && mPopup.getPlainText() != null && mPopup.getPlainText().setEnable(success);
    if (!FileData.isEmpty(mFileData) && mFileData.isOpenFromAppIntent()) {
      if (mPopup != null)
        mPopup.setSaveMenuEnable(false);
    } else {
      if (mPopup != null)
        mPopup.setSaveMenuEnable(success);
    }
    if (mPopup != null) {
      mPopup.setMenusEnable(success);
    }
    if (success) {
      mIdleView.setVisibility(View.GONE);
      mPayloadHexHelper.setVisible(!checked);
      mPayloadPlainSwipe.setVisible(checked);
      if (fromOpen)
        mUnDoRedo.clear();
    } else {
      mIdleView.setVisibility(View.VISIBLE);
      mPayloadHexHelper.setVisible(false);
      mPayloadPlainSwipe.setVisible(false);
      mFileData = null;
      mUnDoRedo.clear();
    }
    setTitle(getResources().getConfiguration());
  }

  /**
   * Sets the activity title.
   *
   * @param cfg Screen configuration.
   */
  public void setTitle(Configuration cfg) {
    UIHelper.setTitle(this, cfg.orientation, true, FileData.isEmpty(mFileData) ? null : mFileData.getName(), mUnDoRedo.isChanged());
  }

  /**
   * Sets the visibility of the menu item.
   *
   * @param menu    MenuItem
   * @param visible If true then the item will be visible; if false it is hidden.
   */
  private void setMenuVisible(final MenuItem menu, final boolean visible) {
    if (menu != null)
      menu.setVisible(visible);
  }


  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mPayloadPlainSwipe.isVisible())
      mPayloadPlainSwipe.getAdapter().notifyDataSetChanged();
    else if (mPayloadHexHelper.isVisible())
      mPayloadHexHelper.getAdapter().notifyDataSetChanged();
    // Checks the orientation of the screen
    if (!FileData.isEmpty(mFileData)) {
      setTitle(newConfig);
    }
  }

  /**
   * Handles the click on the popup menu item.
   *
   * @param id The view id.
   */
  public void onPopupItemClick(int id) {
    if (id == R.id.action_open) {
      popupActionOpen();
    } else if (id == R.id.action_recently_open) {
      mLauncherRecentlyOpen.startActivity();
    } else if (id == R.id.action_save) {
      popupActionSave();
    } else if (id == R.id.action_save_as) {
      popupActionSaveAs();
    } else if (id == R.id.action_close) {
      popupActionClose();
    } else if (id == R.id.action_settings) {
      SettingsActivity.startActivity(this, !FileData.isEmpty(mFileData), mUnDoRedo.isChanged());
    } else if (id == R.id.action_undo) {
      mUnDoRedo.undo();
    } else if (id == R.id.action_redo) {
      mUnDoRedo.redo();
    } else if (id == R.id.action_go_to) {
      popupActionGoTo();
    } else if (mPopup != null) {
      if (mPopup.getPlainText() != null && mPopup.getPlainText().containsId(id, false)) {
        popupActionPlainText(id, mPopup.getPlainText(), mPopup.getLineNumbers());
      } else if (mPopup.getLineNumbers() != null && mPopup.getLineNumbers().containsId(id, false)) {
        popupActionLineNumbers(id, mPopup.getLineNumbers());
      }
    }
  }

  /**
   * Called when the user select an option menu item.
   *
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_more) {
      mPopup.show(findViewById(R.id.action_more));
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Cancels search.
   */
  private void cancelSearch() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      doSearch("");
      mSearchView.setIconified(true);
    }
  }

  /**
   * Callback method to be invoked when an item in this AdapterView has been clicked.
   *
   * @param parent   The AdapterView where the click happened.
   * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter).
   * @param position The position of the view in the adapter.
   * @param id       The row id of the item that was clicked.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    LineData<Line> e = mPayloadHexHelper.getAdapter().getItem(position);
    if (e == null)
      return;
    if (mPayloadPlainSwipe.isVisible()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return;
    }
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    for (Byte b : e.getValue().getRaw())
      byteArrayOutputStream.write(b);
    mLauncherLineUpdate.startActivity(byteArrayOutputStream.toByteArray(), position, 1);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      cancelSearch();
    } else {
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        if (mUnDoRedo.isChanged()) {// a save operation is pending?
          Runnable r = () -> {
            super.onBackPressed();
            finish();
          };
          UIHelper.confirmFileChanged(this, mFileData, r,
              () -> new TaskSave(this, this).execute(
                  new TaskSave.Request(mFileData.getUri(), mPayloadHexHelper.getAdapter().getItems(), r)));
        } else {
          super.onBackPressed();
          finish();
        }
        return;
      } else {
        UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
      }
      mLastBackPressed = System.currentTimeMillis();
    }
  }

  /* ------------ EXPORTED METHODS ------------ */

  /**
   * Returns the menu RecentlyOpen
   *
   * @return MenuItem
   */
  public TextView getMenuRecentlyOpen() {
    return mPopup == null ? null : mPopup.getMenuRecentlyOpen();
  }

  /**
   * Sets the orphan dialog.
   *
   * @param orphan The dialog.
   */
  public void setOrphanDialog(AlertDialog orphan) {
    mOrphanDialog = orphan;
  }

  /**
   * Returns the file data.
   *
   * @return FileData
   */
  public FileData getFileData() {
    return mFileData;
  }

  /**
   * Sets the file data.
   *
   * @param fd FileData
   */
  public void setFileData(FileData fd) {
    mFileData = fd;
  }

  /**
   * Returns the search query.
   *
   * @return String
   */
  public String getSearchQuery() {
    return mSearchQuery;
  }

  /**
   * Returns the PayloadHexHelper
   *
   * @return PayloadHexHelper
   */
  public PayloadHexHelper getPayloadHex() {
    return mPayloadHexHelper;
  }

  /**
   * Returns the PayloadPlainSwipe
   *
   * @return PayloadPlainSwipe
   */
  public PayloadPlainSwipe getPayloadPlain() {
    return mPayloadPlainSwipe;
  }

  /**
   * Returns the LauncherOpen
   *
   * @return LauncherOpen
   */
  public LauncherOpen getLauncherOpen() {
    return mLauncherOpen;
  }

  /**
   * Returns the LauncherLineUpdate
   *
   * @return LauncherLineUpdate
   */
  public LauncherLineUpdate getLauncherLineUpdate() {
    return mLauncherLineUpdate;
  }

  /**
   * Returns the undo/redo.
   *
   * @return UnDoRedo
   */
  public UnDoRedo getUnDoRedo() {
    return mUnDoRedo;
  }

  /* ------------ POPUP ACTIONS ------------ */

  /**
   * Action when the user clicks on the "open" menu.
   */
  private void popupActionOpen() {
    final Runnable r = () -> mLauncherOpen.startActivity();
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      UIHelper.confirmFileChanged(this, mFileData, r,
          () -> new TaskSave(this, this).execute(
              new TaskSave.Request(mFileData.getUri(), mPayloadHexHelper.getAdapter().getItems(), r)));
    } else
      r.run();
  }

  /**
   * Action when the user clicks on the "save" menu.
   */
  private void popupActionSave() {
    if (FileData.isEmpty(mFileData)) {
      UIHelper.toast(this, getString(R.string.open_a_file_before));
      return;
    }
    new TaskSave(this, this).execute(new TaskSave.Request(mFileData.getUri(),
        mPayloadHexHelper.getAdapter().getItems(), null));
    setTitle(getResources().getConfiguration());
  }

  /**
   * Action when the user clicks on the "save as" menu.
   */
  private void popupActionSaveAs() {
    if (FileData.isEmpty(mFileData)) {
      UIHelper.toast(this, getString(R.string.open_a_file_before));
      return;
    }
    mLauncherSave.startActivity();
  }

  /**
   * Action when the user clicks on the "plain text" menu.
   *
   * @param id          Action id.
   * @param plainText   Plain text checkbox.
   * @param lineNumbers Line numbers checkbox.
   */
  private void popupActionPlainText(int id, PopupCheckboxHelper plainText, PopupCheckboxHelper lineNumbers) {
    if (plainText.containsId(id, true))
      plainText.toggleCheck();
    boolean checked = plainText.isChecked();
    mPayloadPlainSwipe.setVisible(checked);
    mPayloadHexHelper.setVisible(!checked);
    if (mSearchQuery != null && !mSearchQuery.isEmpty())
      doSearch(mSearchQuery);
    refreshLineNumbers(lineNumbers);
  }

  /**
   * Refreshes the lines number
   *
   * @param lineNumbers Line numbers checkbox.
   */
  private void refreshLineNumbers(PopupCheckboxHelper lineNumbers) {
    if (lineNumbers != null) {
      boolean checked = lineNumbers.isChecked();
      if (mPayloadHexHelper.isVisible()) {
        if (mApp.isLineNumber() && !checked) {
          lineNumbers.setChecked(true);
          mPayloadHexHelper.refreshLineNumbers();
        }
        lineNumbers.setEnable(true);
      } else if (mPayloadPlainSwipe.isVisible()) {
        if (checked) {
          lineNumbers.setChecked(false);
          mPayloadHexHelper.refreshLineNumbers();
        }
        lineNumbers.setEnable(false);
      }
      mPopup.refreshGoToName();
    }
  }

  /**
   * Action when the user clicks on the "line numbers" menu.
   *
   * @param id          Action id.
   * @param lineNumbers Line numbers checkbox.
   */
  private void popupActionLineNumbers(int id, PopupCheckboxHelper lineNumbers) {
    if (lineNumbers.containsId(id, true))
      lineNumbers.toggleCheck();
    boolean checked = lineNumbers.isChecked();
    mApp.setLineNumber(checked);
    if (mPayloadHexHelper.isVisible())
      mPayloadHexHelper.refreshLineNumbers();
    mPopup.refreshGoToName();
  }

  /**
   * Action when the user clicks on the "close" menu.
   */
  private void popupActionClose() {
    final Runnable r = () -> {
      onOpenResult(false, false);
      mPayloadPlainSwipe.getAdapter().clear();
      mPayloadHexHelper.getAdapter().clear();
      cancelSearch();
      findViewById(R.id.buttonRecentlyOpen).setEnabled(!mApp.getRecentlyOpened().isEmpty());
    };
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      UIHelper.confirmFileChanged(this, mFileData, r,
          () -> new TaskSave(this, this).execute(
              new TaskSave.Request(mFileData.getUri(), mPayloadHexHelper.getAdapter().getItems(), r)));
    } else
      r.run();
  }

  /**
   * Action when the user clicks on the "go to xxx" menu.
   */
  private void popupActionGoTo() {
    if (mPopup.getPlainText().isChecked())
      mGoToDialog.show(GoToDialog.Mode.LINE_PLAIN);
    else if (mPopup.getLineNumbers().isChecked())
      mGoToDialog.show(GoToDialog.Mode.ADDRESS);
    else
      mGoToDialog.show(GoToDialog.Mode.LINE_HEX);
  }

}

