package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import fr.ralala.hexviewer.utils.FileData;
import fr.ralala.hexviewer.utils.FileHelper;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.DisplayCharPolicy;
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
  private SearchableListArrayAdapter mAdapterHex = null;
  private FileData mFileData = null;
  private TextView mPleaseOpenFile = null;
  private ListView mPayloadHex = null;
  private MenuItem mSearchMenu = null;
  private MenuItem mPlainMenu = null;
  private MenuItem mSaveMenu = null;
  private MenuItem mSaveAsMenu = null;
  private MenuItem mCloseMenu = null;
  private SearchView mSearchView = null;
  private MenuItem mRecentlyOpen = null;
  private String mSearchQuery = "";
  private PayloadPlainSwipe mPayloadPlainSwipe;
  private MultiChoiceCallback mMultiChoiceCallback;
  private AlertDialog mOrphanDialog = null;
  private LauncherLineUpdate mLauncherLineUpdate;
  private LauncherSave mLauncherSave;
  private LauncherOpen mLauncherOpen;
  private LauncherRecentlyOpen mLauncherRecentlyOpen;

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

    LinearLayout mainLayout = findViewById(R.id.mainLayout);
    mPleaseOpenFile = findViewById(R.id.pleaseOpenFile);
    mPayloadHex = findViewById(R.id.payloadView);

    mPleaseOpenFile.setVisibility(View.VISIBLE);
    mPayloadHex.setVisibility(View.GONE);

    mAdapterHex = new SearchableListArrayAdapter(this,
        DisplayCharPolicy.DISPLAY_ALL, new ArrayList<>(), new UserConfig() {
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
    mMultiChoiceCallback = new MultiChoiceCallback(this, mPayloadHex, mAdapterHex, mainLayout);
    mPayloadHex.setMultiChoiceModeListener(mMultiChoiceCallback);

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
          if (mApp.getHexChanged().get()) {// a save operation is pending?
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
    mPlainMenu = menu.findItem(R.id.action_plain_text);
    mSaveAsMenu = menu.findItem(R.id.action_save_as);
    mSaveMenu = menu.findItem(R.id.action_save);
    mCloseMenu = menu.findItem(R.id.action_close);
    mRecentlyOpen = menu.findItem(R.id.action_recently_open);

    onOpenResult(false);

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
    final SearchableListArrayAdapter laa = ((mPayloadPlainSwipe.isVisible()) ? mPayloadPlainSwipe.getAdapterPlain() : mAdapterHex);
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
      } else {
        mFileData = new FileData(uri, false);
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
      mFileData = null;
    }
    setTitle(getResources().getConfiguration());
  }

  /**
   * Sets the activity title.
   *
   * @param cfg Screen configuration.
   */
  public void setTitle(Configuration cfg) {
    UIHelper.setTitle(this, cfg.orientation, true, mFileData == null ? null : mFileData.getName());
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
  private void setMenuEnabled(final MenuItem menu, final boolean enabled) {
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
   * Called when the user select an option menu item.
   *
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_open) {
      final Runnable r = () -> mLauncherOpen.startActivity();
      if (mApp.getHexChanged().get()) {// a save operation is pending?
        confirmFileChanged(r);
      } else
        r.run();
      return true;
    } else if (id == R.id.action_recently_open) {
      mLauncherRecentlyOpen.startActivity();
      return true;
    } else if (id == R.id.action_save) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      new TaskSave(this, this).execute(mFileData.getUri());
      mApp.getHexChanged().set(false);
      setTitle(getResources().getConfiguration());
      return true;
    } else if (id == R.id.action_save_as) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      mLauncherSave.startActivity();
      return true;
    } else if (id == R.id.action_plain_text) {
      /* to hex */
      /* to plain */
      boolean checked = !item.isChecked();
      if (checked)
        mMultiChoiceCallback.dismiss();
      mPayloadPlainSwipe.setVisible(checked);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      item.setChecked(checked);
      return true;
    } else if (id == R.id.action_close) {
      final Runnable r = this::closeFile;
      if (mApp.getHexChanged().get()) {// a save operation is pending?
        confirmFileChanged(r);
      } else
        r.run();
    } else if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    }
    return super.onOptionsItemSelected(item);
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
    mApp.getHexChanged().set(false);
    mApp.getPayload().clear();
    onOpenResult(false);
    mPayloadPlainSwipe.getAdapterPlain().clear();
    mAdapterHex.clear();
    if (mSearchView != null && !mSearchView.isIconified()) {
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
    String string = mAdapterHex.getItem(position);
    if (string == null)
      return;
    if (mPayloadPlainSwipe.isVisible()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return;
    }
    mLauncherLineUpdate.startActivity(string, position);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mSearchView != null && !mSearchView.isIconified()) {
      mSearchView.setIconified(true);
    } else {
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        if (mApp.getHexChanged().get()) {// a save operation is pending?
          confirmFileChanged(() -> {
            mApp.getHexChanged().set(false);
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
  public MenuItem getMenuRecentlyOpen() {
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
   * @return SearchableListArrayAdapter
   */
  public SearchableListArrayAdapter getAdapterHex() {
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

}

