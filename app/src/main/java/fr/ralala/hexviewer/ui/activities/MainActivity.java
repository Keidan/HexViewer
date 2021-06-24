package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.documentfile.provider.DocumentFile;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.RecentlyOpenListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.MultiChoiceCallback;
import fr.ralala.hexviewer.ui.utils.PayloadPlainSwipe;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileData;
import fr.ralala.hexviewer.utils.FileHelper;
import fr.ralala.hexviewer.utils.SysHelper;

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
  private LinearLayout mMainLayout = null;
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
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;
  private ActivityResultLauncher<Intent> activityResultLauncherSave;
  private ActivityResultLauncher<Intent> activityResultLauncherLineUpdate;
  private PayloadPlainSwipe mPayloadPlainSwipe;
  private MultiChoiceCallback mMultiChoiceCallback;
  private AlertDialog mOrphanDialog = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mApp = ApplicationCtx.getInstance();

    mMainLayout = findViewById(R.id.mainLayout);
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
    mMultiChoiceCallback = new MultiChoiceCallback(mPayloadHex, mAdapterHex, mMainLayout);
    mPayloadHex.setMultiChoiceModeListener(mMultiChoiceCallback);

    mPayloadPlainSwipe = new PayloadPlainSwipe();
    mPayloadPlainSwipe.onCreate(this);

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, 1);

    registerOpen();
    registerSave();
    registerLineUpdate();

    handleIntent(getIntent());
  }

  /**
   * Called when the activity is resumed.
   */
  public void onResume() {
    super.onResume();
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
          final Runnable r = () -> processFileOpen(uri, true, FileHelper.takeUriPermissions(this, uri, false));
          if(mApp.getHexChanged().get()) {// a save operation is pending?
            confirmFileChanged(r);
          } else {
            r.run();
          }
        }
      }
    }
  }

  private void closeOrphanDialog() {
    if(mOrphanDialog != null ) {
      if(mOrphanDialog.isShowing())
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
  private void doSearch(String queryStr) {
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
    if(success) {
      if (mFileData.isOpenFromAppIntent()) {
        mFileData = new FileData(uri, false);
        if(mFileData.isOpenFromAppIntent())
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
    if(mFileData != null && mFileData.isOpenFromAppIntent())
      setMenuEnabled(mSaveMenu, false);
    else
      setMenuEnabled(mSaveMenu, success);
    setMenuEnabled(mSaveAsMenu, success);
    setMenuEnabled(mCloseMenu, success);
    setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().isEmpty());
    if (success) {
      String title = getString(R.string.app_name);
      title += " - " + SysHelper.abbreviate(mFileData.getName(),
          getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
              mApp.getAbbreviateLandscape() : mApp.getAbbreviatePortrait());
      setTitle(title);
      mPleaseOpenFile.setVisibility(View.GONE);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      mPayloadPlainSwipe.setVisible(checked);
    } else {
      setTitle(R.string.app_name);
      mPleaseOpenFile.setVisibility(View.VISIBLE);
      mPayloadHex.setVisibility(View.GONE);
      mPayloadPlainSwipe.setVisible(false);
      mFileData = null;
    }
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
    int length = 0;
    if (!FileData.isEmpty(mFileData)) {
      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        length = mApp.getAbbreviateLandscape();
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        length = mApp.getAbbreviatePortrait();
      }
      if (length != 0) {
        String title = getString(R.string.app_name);
        title += " - " + SysHelper.abbreviate(mFileData.getName(), length);
        setTitle(title);
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
    if (id == R.id.action_open) {
      final Runnable r = () -> UIHelper.openFilePickerInFileSelectionMode(this, activityResultLauncherOpen, mMainLayout);
      if(mApp.getHexChanged().get()) {// a save operation is pending?
        confirmFileChanged(r);
      } else
        r.run();
      return true;
    } else if (id == R.id.action_recently_open) {
      displayRecentlyOpen();
      return true;
    } else if (id == R.id.action_save) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      new TaskSave(this, this).execute(mFileData.getUri());
      mApp.getHexChanged().set(false);
      return true;
    } else if (id == R.id.action_save_as) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      UIHelper.openFilePickerInDirectorSelectionMode(activityResultLauncherSave);
      return true;
    } else if (id == R.id.action_plain_text) {
      /* to hex */
      /* to plain */
      boolean checked = !item.isChecked();
      if(checked)
        mMultiChoiceCallback.dismiss();
      mPayloadPlainSwipe.setVisible(checked);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      item.setChecked(checked);
      return true;
    } else if (id == R.id.action_close) {
      final Runnable r = this::closeFile;
      if(mApp.getHexChanged().get()) {// a save operation is pending?
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
  private void confirmFileChanged(final Runnable runnable) {
    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.action_close_title)
        .setMessage(String.format(getString(R.string.confirm_save), mFileData.getName()))
        .setPositiveButton(R.string.yes, (dialog, which) -> {
          final Uri uri = FileHelper.getParentUri(mFileData.getUri());
          if (uri != null && FileHelper.hasUriPermission(this, uri, false))
            processFileSave(uri, mFileData.getName(), false);
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
   * Displays the dialog box used to display the list of recently opened files.
   */
  @SuppressLint("InflateParams")
  private void displayRecentlyOpen() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true)
        .setTitle(getString(R.string.action_recently_open_title))
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> dialog.dismiss());

    RecentlyOpenListArrayAdapter setArrayAdapter = new RecentlyOpenListArrayAdapter(this, new ArrayList<>());
    setArrayAdapter.addAll(mApp.getRecentlyOpened());
    builder.setAdapter(setArrayAdapter, (dial, which) -> {
      mOrphanDialog = null;
      RecentlyOpenListArrayAdapter.UriData item = setArrayAdapter.getItem(which);
      if (FileHelper.isFileExists(getContentResolver(), item.uri)) {
        if (FileHelper.hasUriPermission(this, item.uri, true)) {
          final Runnable r = () -> processFileOpen(item.uri, false, true);
          if(mApp.getHexChanged().get()) {// a save operation is pending?
            confirmFileChanged(r);
          } else
            r.run();
        } else {
          UIHelper.toast(this, String.format(getString(R.string.error_file_permission), FileHelper.getFileName(item.uri)));
          setArrayAdapter.removeItem(which);
          mApp.removeRecentlyOpened(item.uri.toString());
        }
      } else {
        UIHelper.toast(this, String.format(getString(R.string.error_file_not_found), item.value));
        setArrayAdapter.removeItem(which);
        mApp.removeRecentlyOpened(item.uri.toString());
        FileHelper.releaseUriPermissions(this, item.uri);
      }
    });
    mOrphanDialog = builder.show();
  }

  /**
   * Callback method to be invoked when an item in this AdapterView has been clicked.
   * @param parent The AdapterView where the click happened.
   * @param view The view within the AdapterView that was clicked (this will be a view provided by the adapter).
   * @param position The position of the view in the adapter.
   * @param id The row id of the item that was clicked.
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
    LineUpdateActivity.startActivity(this, activityResultLauncherLineUpdate, string, mFileData.getName(), position);
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
        if(mApp.getHexChanged().get()) {// a save operation is pending?
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
   * Registers result launcher for the activity for opening a file.
   */
  private void registerOpen() {
    activityResultLauncherOpen = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              if (FileHelper.takeUriPermissions(this, data.getData(), false)) {
                processFileOpen(data);
              } else
                UIHelper.toast(this, String.format(getString(R.string.error_file_permission), FileHelper.getFileName(data.getData())));
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }


  /**
   * Registers result launcher for the activity for saving a file.
   */
  private void registerSave() {
    activityResultLauncherSave = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              if(!mFileData.isOpenFromAppIntent())
                FileHelper.takeUriPermissions(MainActivity.this, data.getData(), true);
              processFileSaveWithDialog(data.getData());
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void registerLineUpdate() {
    activityResultLauncherLineUpdate = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              Bundle bundle = data.getExtras();
              String refString = bundle.getString(LineUpdateActivity.RESULT_REFERENCE_STRING);
              String newString = bundle.getString(LineUpdateActivity.RESULT_NEW_STRING);
              int position = bundle.getInt(LineUpdateActivity.RESULT_POSITION);

              final byte[] buf = SysHelper.hexStringToByteArray(newString);
              final byte[] ref = SysHelper.hexStringToByteArray(refString);
              if (Arrays.equals(ref, buf)) {
                /* nothing to do */
                return;
              }
              mApp.getHexChanged().set(true);
              mApp.getPayload().update(position, buf);
              List<String> li = SysHelper.formatBuffer(buf, null);
              if (li.isEmpty())
                mAdapterHex.removeItem(position);
              else {
                String query = mSearchQuery;
                if (!query.isEmpty())
                  doSearch("");
                mAdapterHex.setItem(position, li);
                if (!query.isEmpty())
                  doSearch(mSearchQuery);
              }
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }

  /*-------------------------------------------*/

  /**
   * Process the opening of the file
   *
   * @param data Intent data.
   */
  private void processFileOpen(final Intent data) {
    Uri uri = data.getData();
    processFileOpen(uri, false, true);
  }

  /**
   * Process the opening of the file
   *
   * @param uri Uri data.
   */
  private void processFileOpen(final Uri uri, final boolean openFromAppIntent, final boolean addRecent) {
    if (uri != null && uri.getPath() != null) {
      mFileData = new FileData(uri, openFromAppIntent);
      new TaskOpen(this, mAdapterHex, mPayloadPlainSwipe.getAdapterPlain(), this, addRecent).execute(uri);
    } else {
      UIHelper.toast(this, getString(R.string.error_filename));
    }
  }

  /**
   * Process the saving of the file
   *
   * @param uri Uri data.
   */
  private void processFileSaveWithDialog(final Uri uri) {
    mOrphanDialog = UIHelper.createTextDialog(this, getString(R.string.action_save_title), mFileData.getName(), (dialog, content, layout) -> {
      mOrphanDialog = null;
      final String s_file = content.getText().toString();
      if (s_file.trim().isEmpty()) {
        layout.setError(getString(R.string.error_filename));
        return;
      }
      processFileSave(uri, s_file, true);
      dialog.dismiss();
    });
  }


  /**
   * Process the saving of the file
   *
   * @param uri         Uri data.
   * @param filename    The filename
   * @param showConfirm Shows confirm box.
   */
  private void processFileSave(final Uri uri, final String filename, final boolean showConfirm) {
    DocumentFile sourceDir = DocumentFile.fromTreeUri(this, uri);
    if (sourceDir == null) {
      UIHelper.toast(this, getString(R.string.uri_exception));
      Log.e(getClass().getSimpleName(), "1 - Uri exception: '" + uri + "'");
      return;
    }
    DocumentFile file = null;
    for (DocumentFile f : sourceDir.listFiles()) {
      if (f.getName() != null && f.getName().endsWith(filename)) {
        file = f;
        break;
      }
    }
    final DocumentFile f_file = file;

    if (file != null) {
      final Runnable r = () -> {
        new TaskSave(this, this).execute(f_file.getUri());
        mApp.getHexChanged().set(false);
      };
      if(showConfirm) {
        UIHelper.showConfirmDialog(this, getString(R.string.action_save_title),
            getString(R.string.confirm_overwrite),
            (view) -> r.run());
      } else {
        r.run();
      }
    } else {
      DocumentFile d_file = sourceDir.createFile("application/octet-stream", filename);
      if (d_file == null) {
        UIHelper.toast(this, getString(R.string.uri_exception));
        Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uri + "'");
      } else {
        new TaskSave(this, this).execute(d_file.getUri());
        mApp.getHexChanged().set(false);
      }
    }
  }

}

