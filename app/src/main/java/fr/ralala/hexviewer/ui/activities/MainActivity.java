package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.activities.settings.SettingsActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigLandscape;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigPortrait;
import fr.ralala.hexviewer.ui.launchers.LauncherLineUpdate;
import fr.ralala.hexviewer.ui.launchers.LauncherOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherRecentlyOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherSave;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;
import fr.ralala.hexviewer.ui.utils.MultiChoiceCallback;
import fr.ralala.hexviewer.ui.utils.PayloadPlainSwipe;
import fr.ralala.hexviewer.ui.utils.PopupCheckboxHelper;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main activity
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private HexTextArrayAdapter mAdapterHex = null;
  private FileData mFileData = null;
  private RelativeLayout mIdleView = null;
  private ListView mPayloadHex = null;
  private MenuItem mSearchMenu = null;
  private TextView mSaveMenu = null;
  private TextView mSaveAsMenu = null;
  private TextView mCloseMenu = null;
  private SearchView mSearchView = null;
  private TextView mRecentlyOpen = null;
  private String mSearchQuery = "";
  private PayloadPlainSwipe mPayloadPlainSwipe = null;
  private AlertDialog mOrphanDialog = null;
  private LauncherLineUpdate mLauncherLineUpdate = null;
  private LauncherSave mLauncherSave = null;
  private LauncherOpen mLauncherOpen = null;
  private LauncherRecentlyOpen mLauncherRecentlyOpen = null;
  private PopupWindow mPopup = null;
  private UnDoRedo mUnDoRedo = null;
  private PopupCheckboxHelper mPlainText = null;
  private PopupCheckboxHelper mLineNumbers = null;

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

    LinearLayout mainLayout = findViewById(R.id.mainLayout);
    mIdleView = findViewById(R.id.idleView);
    mPayloadHex = findViewById(R.id.payloadView);

    mIdleView.setVisibility(View.VISIBLE);
    mPayloadHex.setVisibility(View.GONE);

    findViewById(R.id.buttonOpenFile).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_open));
    findViewById(R.id.buttonRecentlyOpen).setOnClickListener((v) ->
        onPopupItemClick(R.id.action_recently_open));

    mAdapterHex = new HexTextArrayAdapter(this,
        new ArrayList<>(),
        new UserConfigPortrait(true),
        new UserConfigLandscape(true));
    mPayloadHex.setAdapter(mAdapterHex);
    mPayloadHex.setOnItemClickListener(this);
    mPayloadHex.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    MultiChoiceCallback multiChoiceCallback = new MultiChoiceCallback(this, mPayloadHex, mAdapterHex);
    mPayloadHex.setMultiChoiceModeListener(multiChoiceCallback);

    mPayloadPlainSwipe = new PayloadPlainSwipe();
    mPayloadPlainSwipe.onCreate(this);

    /* permissions */
    boolean requestPermissions = true;
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
          ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        requestPermissions = false;
      }
    }
    if(requestPermissions)
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
    if (mPopup != null && mPopup.isShowing())
      mPopup.dismiss();
    mApp.applyApplicationLanguage(this);
    /* refresh */
    onOpenResult(!FileData.isEmpty(mFileData), false);
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
          boolean addRecent;
          if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            addRecent = false;
          }
          else
            addRecent = FileHelper.takeUriPermissions(this, uri, false);
          final Runnable r = () -> mLauncherOpen.processFileOpen(uri, true, addRecent);
          if (mUnDoRedo.isChanged()) {// a save operation is pending?
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
    mSearchQuery = queryStr;
    final SearchableListArrayAdapter<?> laa = ((mPayloadPlainSwipe.isVisible()) ? mPayloadPlainSwipe.getAdapterPlain() : mAdapterHex);
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
        setMenuEnabled(mSaveMenu, true);
        setTitle(getResources().getConfiguration());
      } else {
        mFileData = new FileData(uri, false);
        setTitle(getResources().getConfiguration());
      }
    }
    if(userRunnable != null)
      userRunnable.run();
  }

  /**
   * Method called when the file is opened.
   *
   * @param success The result.
   * @param fromOpen Called from open
   */
  @Override
  public void onOpenResult(boolean success, boolean fromOpen) {
    setMenuVisible(mSearchMenu, success);
    boolean checked = mPlainText != null && mPlainText.setEnable(success);
    if (!FileData.isEmpty(mFileData) && mFileData.isOpenFromAppIntent())
      setMenuEnabled(mSaveMenu, false);
    else
      setMenuEnabled(mSaveMenu, success);
    setMenuEnabled(mSaveAsMenu, success);
    setMenuEnabled(mCloseMenu, success);
    setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().isEmpty());
    if (success) {
      if(mLineNumbers != null)
        mLineNumbers.setEnable(true);
      mIdleView.setVisibility(View.GONE);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      mPayloadPlainSwipe.setVisible(checked);
      if(fromOpen)
        mUnDoRedo.clear();
    } else {
      mIdleView.setVisibility(View.VISIBLE);
      mPayloadHex.setVisibility(View.GONE);
      mPayloadPlainSwipe.setVisible(false);
      if(mPlainText != null) {
        mPlainText.setChecked(false);
        mPlainText.setEnable(false);
      }
      if(mLineNumbers != null) {
        mLineNumbers.setEnable(false);
      }
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
    if (mPayloadPlainSwipe.isVisible())
      mPayloadPlainSwipe.getAdapterPlain().notifyDataSetChanged();
    else if(mPayloadHex.getVisibility() == View.VISIBLE)
      mAdapterHex.notifyDataSetChanged();
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
      popupActionRecentlyOpen();
    } else if (id == R.id.action_save) {
      popupActionSave();
    } else if (id == R.id.action_save_as) {
      popupActionSaveAs();
    } else if (mPlainText != null && mPlainText.containsId(id, false)) {
      popupActionPlainText(id);
    } else if (mLineNumbers != null && mLineNumbers.containsId(id, false)) {
      popupActionLineNumbers(id);
    } else if (id == R.id.action_close) {
      popupActionClose();
    } else if (id == R.id.action_settings) {
      popupActionSettings();
    } else if (id == R.id.action_undo) {
      popupActionUndo();
    } else if (id == R.id.action_redo) {
      popupActionRedo();
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
      if (mPopup == null) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.main_popup, null);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int with = popupView.getMeasuredWidth();

        mPopup = new PopupWindow(popupView,
            with + 150,
            WindowManager.LayoutParams.WRAP_CONTENT, true);

        mPopup.setElevation(5.0f);
        mPopup.setOutsideTouchable(true);

        mPlainText = new PopupCheckboxHelper(popupView,
            R.id.action_plain_text_container,
            R.id.action_plain_text_tv,
            R.id.action_plain_text_cb);

        mLineNumbers = new PopupCheckboxHelper(popupView,
            R.id.action_line_numbers_container,
            R.id.action_line_numbers_tv,
            R.id.action_line_numbers_cb);

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
        mPlainText.setOnClickListener(click);
        mLineNumbers.setOnClickListener(click);
        mSaveAsMenu.setOnClickListener(click);
        mSaveMenu.setOnClickListener(click);
        mCloseMenu.setOnClickListener(click);
        mRecentlyOpen.setOnClickListener(click);
        actionRedo.setOnClickListener(click);
        actionUndo.setOnClickListener(click);
        mUnDoRedo.setControls(containerUndo, actionUndo, containerRedo, actionRedo);
        if(mLineNumbers != null) {
          mLineNumbers.setChecked(mApp.isLineNumber());
        }
        if (mFileData == null)
          onOpenResult(false, false);
        else if (mFileData.isOpenFromAppIntent()) {
          setMenuVisible(mSearchMenu, true);
          if(mPlainText != null)
            mPlainText.setEnable(true);
          setMenuEnabled(mSaveMenu, false);
          setMenuEnabled(mSaveAsMenu, true);
          setMenuEnabled(mCloseMenu, true);
          setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().isEmpty());
          mIdleView.setVisibility(View.GONE);
          mPayloadHex.setVisibility(View.VISIBLE);
          mPayloadPlainSwipe.setVisible(false);
        }
      }
      mPopup.showAtLocation(findViewById(R.id.action_more), Gravity.TOP | Gravity.END, 12, 120);
      //mPopup.showAsDropDown(findViewById(R.id.action_more));
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
    if(FileData.isEmpty(mFileData)) {
      runnable.run();
      return;
    }
    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.action_close_title)
        .setMessage(String.format(getString(R.string.confirm_save), mFileData.getName()))
        .setPositiveButton(R.string.yes, (dialog, which) -> {
          new TaskSave(this, this).execute(new TaskSave.Request(mFileData.getUri(), mAdapterHex.getItems(), runnable));
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
   * Callback method to be invoked when an item in this AdapterView has been clicked.
   *
   * @param parent   The AdapterView where the click happened.
   * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter).
   * @param position The position of the view in the adapter.
   * @param id       The row id of the item that was clicked.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    LineData<Line> e = mAdapterHex.getItem(position);
    if (e == null)
      return;
    if (mPayloadPlainSwipe.isVisible()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return;
    }
    mLauncherLineUpdate.startActivity(e.getValue().getPlain(), position);
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

  /* ------------ EXPORTED METHODS ------------ */

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
      confirmFileChanged(r);
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
    new TaskSave(this, this).execute(new TaskSave.Request(mFileData.getUri(), mAdapterHex.getItems(), null));
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
   * Action when the user clicks on the "recently open" menu.
   */
  private void popupActionRecentlyOpen() {
    mLauncherRecentlyOpen.startActivity();
  }

  /**
   * Action when the user clicks on the "plain text" menu.
   *
   * @param id Action id.
   */
  private void popupActionPlainText(int id) {
    if (mPlainText != null && mPlainText.containsId(id, true))
      mPlainText.toggleCheck();
    boolean checked = mPlainText != null && mPlainText.isChecked();
    mPayloadPlainSwipe.setVisible(checked);
    mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
    if (mSearchQuery != null && !mSearchQuery.isEmpty())
      doSearch(mSearchQuery);
    refreshLineNumbers();
  }

  /**
   * Refreshes the lines number
   */
  private void refreshLineNumbers() {
    if (mLineNumbers != null) {
      boolean checked = mLineNumbers.isChecked();
      if(mPayloadHex.getVisibility() == View.VISIBLE) {
        if(mApp.isLineNumber() && !checked) {
          mLineNumbers.setChecked(true);
          mAdapterHex.notifyDataSetChanged();
        }
        mLineNumbers.setEnable(true);
      } else if(mPayloadPlainSwipe.isVisible()) {
        if(checked) {
          mLineNumbers.setChecked(false);
          mAdapterHex.notifyDataSetChanged();
        }
        mLineNumbers.setEnable(false);
      }
    }
  }

  /**
   * Action when the user clicks on the "line numbers" menu.
   *
   * @param id Action id.
   */
  private void popupActionLineNumbers(int id) {
    if (mLineNumbers != null && mLineNumbers.containsId(id, true))
      mLineNumbers.toggleCheck();
    boolean checked = mLineNumbers != null && mLineNumbers.isChecked();
    mApp.setLineNumber(checked);
    if(mPayloadHex.getVisibility() == View.VISIBLE)
      mAdapterHex.notifyDataSetChanged();
  }

  /**
   * Action when the user clicks on the "close" menu.
   */
  private void popupActionClose() {
    final Runnable r = () -> {
      onOpenResult(false, false);
      mPayloadPlainSwipe.getAdapterPlain().clear();
      mAdapterHex.clear();
      cancelSearch();
    };
    if (mUnDoRedo.isChanged()) {// a save operation is pending?
      confirmFileChanged(r);
    } else
      r.run();
  }

  /**
   * Action when the user clicks on the "settings" menu.
   */
  private void popupActionSettings() {
    SettingsActivity.startActivity(this, mUnDoRedo.isChanged());
  }

  /**
   * Action when the user clicks on the "undo" menu.
   */
  private void popupActionUndo() {
    mUnDoRedo.undo();
  }

  /**
   * Action when the user clicks on the "redo" menu.
   */
  private void popupActionRedo() {
    mUnDoRedo.redo();
  }
}

