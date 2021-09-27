package fr.ralala.hexviewer.ui.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuCompat;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.settings.SettingsActivity;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.dialog.GoToDialog;
import fr.ralala.hexviewer.ui.dialog.SequentialOpenDialog;
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
public class MainActivity extends AbstractBaseMainActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
  private FileData mFileData = null;
  private ConstraintLayout mIdleView = null;
  private MenuItem mSearchMenu = null;
  private String mSearchQuery = "";
  private PayloadPlainSwipe mPayloadPlainSwipe = null;
  private LauncherLineUpdate mLauncherLineUpdate = null;
  private LauncherSave mLauncherSave = null;
  private LauncherOpen mLauncherOpen = null;
  private LauncherRecentlyOpen mLauncherRecentlyOpen = null;
  private UnDoRedo mUnDoRedo = null;
  private MainPopupWindow mPopup = null;
  private PayloadHexHelper mPayloadHexHelper = null;
  private GoToDialog mGoToDialog = null;
  private SequentialOpenDialog mSequentialOpenDialog = null;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    mUnDoRedo = new UnDoRedo(this);

    mPopup = new MainPopupWindow(this, mUnDoRedo, this::onPopupItemClick);

    LinearLayout mainLayout = findViewById(R.id.mainLayout);
    mIdleView = findViewById(R.id.idleView);
    mIdleView.setVisibility(View.VISIBLE);

    findViewById(R.id.buttonOpenFile).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_open));
    findViewById(R.id.buttonRecentlyOpen).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_recently_open));
    findViewById(R.id.buttonRecentlyOpen).setEnabled(!mApp.getRecentlyOpened().list().isEmpty());
    mPayloadHexHelper = new PayloadHexHelper();
    mPayloadHexHelper.onCreate(this);

    mPayloadPlainSwipe = new PayloadPlainSwipe();
    mPayloadPlainSwipe.onCreate(this);

    mLauncherOpen = new LauncherOpen(this, mainLayout);
    mLauncherSave = new LauncherSave(this);
    mLauncherLineUpdate = new LauncherLineUpdate(this);
    mLauncherRecentlyOpen = new LauncherRecentlyOpen(this);

    mGoToDialog = new GoToDialog(this);
    mSequentialOpenDialog = new SequentialOpenDialog(this);

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
          FileData fd = new FileData(this, uri, true);
          mApp.setSequential(true);
          final Runnable r = () -> mLauncherOpen.processFileOpen(fd, addRecent);
          if (mUnDoRedo.isChanged()) {// a save operation is pending?
            UIHelper.confirmFileChanged(this, mFileData, r,
                () -> new TaskSave(this, this).execute(
                    new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
          } else {
            r.run();
          }
        }
      }
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
    setSearchView(mSearchMenu);
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
  @Override
  public void doSearch(String queryStr) {
    mSearchQuery = queryStr;
    final SearchableListArrayAdapter laa = ((mPayloadPlainSwipe.isVisible()) ?
        mPayloadPlainSwipe.getAdapter() : mPayloadHexHelper.getAdapter());
    laa.getFilter().filter(queryStr);
  }

  /**
   * Method called when the file is saved.
   *
   * @param fd           The new FileData.
   * @param success      The result.
   * @param userRunnable User runnable (can be null).
   */
  @Override
  public void onSaveResult(FileData fd, boolean success, final Runnable userRunnable) {
    if (success) {
      mUnDoRedo.refreshChange();
      if (mFileData.isOpenFromAppIntent()) {
        if (mPopup != null)
          mPopup.setSaveMenuEnable(true);
      }
      mFileData = fd;
      mFileData.clearOpenFromAppIntent();
      setTitle(getResources().getConfiguration());
      mPayloadHexHelper.resetUpdateStatus();
    } else
      mApp.getRecentlyOpened().remove(fd);
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
    if ((!FileData.isEmpty(mFileData) && !mFileData.isOpenFromAppIntent()))
      mPopup.setSaveMenuEnable(mUnDoRedo.isChanged());
  }


  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mPayloadPlainSwipe.isVisible()) {
      mPayloadPlainSwipe.refresh();
    } else if (mPayloadHexHelper.isVisible())
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
    if (id == R.id.action_open || id == R.id.action_open_sequential) {
      popupActionOpen(id == R.id.action_open_sequential);
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
      specialPopupActions(id);
    }
  }

  /**
   * Handles the click on the popup menu item.
   *
   * @param id The view id.
   */
  private void specialPopupActions(int id) {
    if (mPopup.getPlainText() != null && mPopup.getPlainText().containsId(id, false)) {
      popupActionPlainText(id, mPopup.getPlainText(), mPopup.getLineNumbers());
    } else if (mPopup.getLineNumbers() != null && mPopup.getLineNumbers().containsId(id, false)) {
      popupActionLineNumbers(id, mPopup.getLineNumbers());
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
   * Callback method to be invoked when an item in this AdapterView has been clicked.
   *
   * @param parent   The AdapterView where the click happened.
   * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter).
   * @param position The position of the view in the adapter.
   * @param id       The row id of the item that was clicked.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    LineEntry e = mPayloadHexHelper.getAdapter().getItem(position);
    if (e == null)
      return;
    if (mPayloadPlainSwipe.isVisible()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return;
    }

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    for (Byte b : e.getRaw())
      byteArrayOutputStream.write(b);
    mLauncherLineUpdate.startActivity(byteArrayOutputStream.toByteArray(), position, 1);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onExit() {
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      Runnable r = this::finish;
      UIHelper.confirmFileChanged(this, mFileData, r,
          () -> new TaskSave(this, this).execute(
              new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
    } else {
      finish();
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

  /**
   * Returns the SequentialOpenDialog.
   *
   * @return SequentialOpenDialog
   */
  public SequentialOpenDialog getSequentialOpenDialog() {
    return mSequentialOpenDialog;
  }

  /* ------------ POPUP ACTIONS ------------ */

  /**
   * Action when the user clicks on the "open" or "sequential opening" menu.
   */
  private void popupActionOpen(boolean sequential) {
    mApp.setSequential(sequential);
    final Runnable r = () -> mLauncherOpen.startActivity();
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      UIHelper.confirmFileChanged(this, mFileData, r,
          () -> new TaskSave(this, this).execute(
              new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
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
    new TaskSave(this, this).execute(new TaskSave.Request(mFileData,
        mPayloadHexHelper.getAdapter().getEntries().getItems(), null));
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
      findViewById(R.id.buttonRecentlyOpen).setEnabled(!mApp.getRecentlyOpened().list().isEmpty());
    };
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      UIHelper.confirmFileChanged(this, mFileData, r,
          () -> new TaskSave(this, this).execute(
              new TaskSave.Request(mFileData, mPayloadHexHelper.getAdapter().getEntries().getItems(), r)));
    } else
      r.run();
  }

  /**
   * Action when the user clicks on the "go to xxx" menu.
   */
  private void popupActionGoTo() {
    if (mPopup.getPlainText().isChecked())
      setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.LINE_PLAIN));
    else if (mPopup.getLineNumbers().isChecked())
      setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.ADDRESS));
    else
      setOrphanDialog(mGoToDialog.show(GoToDialog.Mode.LINE_HEX));
  }

}

