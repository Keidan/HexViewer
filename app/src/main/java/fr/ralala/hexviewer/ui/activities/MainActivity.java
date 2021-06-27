package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.launchers.LauncherLineUpdate;
import fr.ralala.hexviewer.ui.launchers.LauncherOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherRecentlyOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherSave;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.MultiChoiceCallback;
import fr.ralala.hexviewer.ui.utils.PayloadPlainSwipe;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.ui.undoredo.UndoRedoManager;
import fr.ralala.hexviewer.utils.FileData;
import fr.ralala.hexviewer.utils.FileHelper;
import fr.ralala.hexviewer.utils.LineEntry;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.UserConfig;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main activity
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private HexTextArrayAdapter mAdapterHex = null;
  private FileData mFileData = null;
  private TextView mPleaseOpenFile = null;
  private ListView mPayloadHex = null;
  private MenuItem mSearchMenu = null;
  private CheckBox mPlainMenu = null;
  private TextView mSaveMenu = null;
  private TextView mSaveAsMenu = null;
  private TextView mCloseMenu = null;
  private SearchView mSearchView = null;
  private TextView mRecentlyOpen = null;
  private String mSearchQuery = "";
  private PayloadPlainSwipe mPayloadPlainSwipe;
  private AlertDialog mOrphanDialog = null;
  private LauncherLineUpdate mLauncherLineUpdate;
  private LauncherSave mLauncherSave;
  private LauncherOpen mLauncherOpen;
  private LauncherRecentlyOpen mLauncherRecentlyOpen;
  private PopupWindow mPopup;
  private UndoRedoManager mUndoRedoManager;

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
    mUndoRedoManager = new UndoRedoManager(this);

    LinearLayout mainLayout = findViewById(R.id.mainLayout);
    mPleaseOpenFile = findViewById(R.id.pleaseOpenFile);
    mPayloadHex = findViewById(R.id.payloadView);

    mPleaseOpenFile.setVisibility(View.VISIBLE);
    mPayloadHex.setVisibility(View.GONE);

    mAdapterHex = new HexTextArrayAdapter(this,
        new ArrayList<>(), new UserConfig() {
      @Override
      public float getFontSize() {
        return mApp.getHexFontSize();
      }

      @Override
      public int getRowHeight() {
        return mApp.getHexRowHeight();
      }

      @Override
      public boolean isRowHeightAuto() {
        return mApp.isHexRowHeightAuto();
      }
    });
    mPayloadHex.setAdapter(mAdapterHex);
    mPayloadHex.setOnItemClickListener(this);
    mPayloadHex.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    MultiChoiceCallback multiChoiceCallback = new MultiChoiceCallback(this, mPayloadHex, mAdapterHex);
    mPayloadHex.setMultiChoiceModeListener(multiChoiceCallback);

    mPayloadPlainSwipe = new PayloadPlainSwipe();
    mPayloadPlainSwipe.onCreate(this);

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, 1);

    mLauncherOpen = new LauncherOpen(this, mainLayout);
    mLauncherSave = new LauncherSave(this);
    mLauncherLineUpdate = new LauncherLineUpdate(this);
    mLauncherRecentlyOpen = new LauncherRecentlyOpen(this);

    handleIntent(getIntent());
  }

  /**
   * Called when the activity is resumed.
   */
  public void onResume() {
    super.onResume();
    mApp.applyApplicationLanguage(this);
    /* refresh */
    onOpenResult(!FileData.isEmpty(mFileData));
    if (mPayloadHex.getVisibility() == View.VISIBLE)
      mAdapterHex.refresh();
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
          final Runnable r = () -> mLauncherOpen.processFileOpen(uri, true, FileHelper.takeUriPermissions(this, uri, false));
          if (mUndoRedoManager.isChanged()) {// a save operation is pending?
            confirmFileChanged(r);
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
    final SearchableListArrayAdapter<?> laa = ((mPayloadPlainSwipe.isVisible()) ? mPayloadPlainSwipe.getAdapterPlain() : mAdapterHex);
    laa.getFilter().filter(queryStr);
  }

  /**
   * Method called when the file is saved.
   *
   * @param uri     The new Uri.
   * @param success The result.
   */
  @Override
  public void onSaveResult(Uri uri, boolean success) {
    if (success) {
      if (mFileData.isOpenFromAppIntent()) {
        mFileData = new FileData(uri, false);
        if (mFileData.isOpenFromAppIntent())
          mFileData.clearOpenFromAppIntent();
        setMenuEnabled(mSaveMenu, true);
        setTitle(getResources().getConfiguration());
      } else {
        mFileData = new FileData(uri, false);
        setTitle(getResources().getConfiguration());
      }
    }
  }

  /**
   * Method called when the file is opened.
   *
   * @param success The result.
   */
  @Override
  public void onOpenResult(boolean success) {
    boolean checked = false;
    setMenuVisible(mSearchMenu, success);
    if (mPlainMenu != null) {
      checked = mPlainMenu.isChecked();
      mPlainMenu.setEnabled(success);
    }
    if (mFileData != null && mFileData.isOpenFromAppIntent())
      setMenuEnabled(mSaveMenu, false);
    else
      setMenuEnabled(mSaveMenu, success);
    setMenuEnabled(mSaveAsMenu, success);
    setMenuEnabled(mCloseMenu, success);
    setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().isEmpty());
    if (success) {
      mPleaseOpenFile.setVisibility(View.GONE);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      mPayloadPlainSwipe.setVisible(checked);
    } else {
      mPleaseOpenFile.setVisibility(View.VISIBLE);
      mPayloadHex.setVisibility(View.GONE);
      mPayloadPlainSwipe.setVisible(false);
      if(mPlainMenu != null)
        mPlainMenu.setChecked(false);
      mFileData = null;
      mUndoRedoManager.clear();
    }
    setTitle(getResources().getConfiguration());
  }

  /**
   * Sets the activity title.
   *
   * @param cfg Screen configuration.
   */
  public void setTitle(Configuration cfg) {
    UIHelper.setTitle(this, cfg.orientation, true, mFileData == null ? null : mFileData.getName(), mUndoRedoManager.isChanged());
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
   * Sets whether the menu item is enabled.
   *
   * @param menu    MenuItem
   * @param enabled If true then the item will be invokable; if false it is won't be invokable.
   */
  private void setMenuEnabled(final TextView menu, final boolean enabled) {
    if (menu != null)
      menu.setEnabled(enabled);
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    // Checks the orientation of the screen
    if (!FileData.isEmpty(mFileData)) {
      setTitle(newConfig);
    }
  }

  /**
   * Handles the click on the popup menu item.
   * @param id The view id.
   */
  public void onPopupItemClick(int id) {
    if (id == R.id.action_open) {
      final Runnable r = () -> mLauncherOpen.startActivity();
      if (mUndoRedoManager.isChanged()) {// a save operation is pending?
        confirmFileChanged(r);
      } else
        r.run();
    } else if (id == R.id.action_recently_open) {
      mLauncherRecentlyOpen.startActivity();
    } else if (id == R.id.action_save) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return;
      }
      new TaskSave(this, this).execute(new TaskSave.Request(mFileData.getUri(), mAdapterHex.getItems()));
      setTitle(getResources().getConfiguration());
    } else if (id == R.id.action_save_as) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return;
      }
      mLauncherSave.startActivity();
    } else if (id == R.id.action_plain_text) {
      cancelSearch();
      boolean checked = !mPlainMenu.isChecked();
      mPayloadPlainSwipe.setVisible(checked);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      mPlainMenu.setChecked(checked);
    } else if (id == R.id.action_close) {
      final Runnable r = this::closeFile;
      if (mUndoRedoManager.isChanged()) {// a save operation is pending?
        confirmFileChanged(r);
      } else
        r.run();
    } else if (id == R.id.action_settings) {
      SettingsActivity.startActivity(this, mUndoRedoManager.isChanged());
    } else if (id == R.id.action_undo) {
      mUndoRedoManager.undo();
    } else if (id == R.id.action_redo) {
      mUndoRedoManager.redo();
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
      if(mPopup == null) {

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.main_popup, null);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int with = popupView.getMeasuredWidth();

        mPopup = new PopupWindow(popupView,
            with + 150,
            WindowManager.LayoutParams.WRAP_CONTENT, true);

        mPopup.setElevation(5.0f);
        mPopup.setOutsideTouchable(true);

        mPlainMenu = popupView.findViewById(R.id.action_plain_text);
        mSaveAsMenu = popupView.findViewById(R.id.action_save_as);
        mSaveMenu = popupView.findViewById(R.id.action_save);
        mCloseMenu = popupView.findViewById(R.id.action_close);
        mRecentlyOpen = popupView.findViewById(R.id.action_recently_open);
        ImageView actionRedo = popupView.findViewById(R.id.action_redo);
        ImageView actionUndo = popupView.findViewById(R.id.action_undo);
        FrameLayout containerRedo = popupView.findViewById(R.id.containerRedo);
        FrameLayout containerUndo = popupView.findViewById(R.id.containerUndo);

        View.OnClickListener click = (v) -> {
          mPopup.dismiss();
          onPopupItemClick(v.getId());
        };
        popupView.findViewById(R.id.action_open).setOnClickListener(click);
        popupView.findViewById(R.id.action_settings).setOnClickListener(click);
        mPlainMenu.setOnClickListener(click);
        mSaveAsMenu.setOnClickListener(click);
        mSaveMenu.setOnClickListener(click);
        mCloseMenu.setOnClickListener(click);
        mRecentlyOpen.setOnClickListener(click);
        actionRedo.setOnClickListener(click);
        actionUndo.setOnClickListener(click);
        mUndoRedoManager.setControls(containerUndo, actionUndo, containerRedo, actionRedo);
        onOpenResult(false);
      }
      //mPopup.showAtLocation(findViewById(R.id.action_more), Gravity.TOP|Gravity.END, 0, 0);
      mPopup.showAsDropDown(findViewById(R.id.action_more));
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
   * Display a confirmation message when the file is modified. A backup will automatically be made.
   *
   * @param runnable The action to be taken if the user validates or not
   */
  public void confirmFileChanged(final Runnable runnable) {
    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.action_close_title)
        .setMessage(String.format(getString(R.string.confirm_save), mFileData.getName()))
        .setPositiveButton(R.string.yes, (dialog, which) -> {
          final Uri uri = FileHelper.getParentUri(mFileData.getUri());
          if (uri != null && FileHelper.hasUriPermission(this, uri, false))
            mLauncherSave.processFileSave(uri, mFileData.getName(), false);
          else
            UIHelper.toast(this, String.format(getString(R.string.error_file_permission), mFileData.getName()));
          runnable.run();
          dialog.dismiss();
        })
        .setNegativeButton(R.string.no, (dialog, which) -> {
          runnable.run();
          dialog.dismiss();
        })
        .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
        .show();
  }

  /**
   * Closes the file context.
   */
  private void closeFile() {
    onOpenResult(false);
    mPayloadPlainSwipe.getAdapterPlain().clear();
    mAdapterHex.clear();
    cancelSearch();
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
    LineEntry e = mAdapterHex.getItem(position);
    if (e == null)
      return;
    if (mPayloadPlainSwipe.isVisible()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return;
    }
    mLauncherLineUpdate.startActivity(e.getPlain(), position);
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
        if (mUndoRedoManager.isChanged()) {// a save operation is pending?
          confirmFileChanged(() -> {
            super.onBackPressed();
            finish();
          });
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

  /**
   * Returns the menu RecentlyOpen
   *
   * @return MenuItem
   */
  public TextView getMenuRecentlyOpen() {
    return mRecentlyOpen;
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
   * Returns the hex adapter.
   *
   * @return HexTextArrayAdapter
   */
  public HexTextArrayAdapter getAdapterHex() {
    return mAdapterHex;
  }

  /**
   * Returns the PayloadPlainSwipe
   *
   * @return PayloadPlainSwipe
   */
  public PayloadPlainSwipe getPayloadPlainSwipe() {
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
   * Returns the undo/redo manager.
   *
   * @return UndoRedoManager
   */
  public UndoRedoManager getUndoRedoManager() {
    return mUndoRedoManager;
  }

}

