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
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, TaskOpen.OpenResultListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private SearchableListArrayAdapter mAdapterHex = null;
  private SearchableListArrayAdapter mAdapterPlain = null;
  private LinearLayout mMainLayout = null;
  private FileData mFileData = null;
  private TextView mPleaseOpenFile = null;
  private ListView mPayloadHex = null;
  private ListView mPayloadPlain = null;
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

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mApp = (ApplicationCtx) getApplication();

    mMainLayout = findViewById(R.id.mainLayout);
    mPleaseOpenFile = findViewById(R.id.pleaseOpenFile);
    mPayloadPlain = findViewById(R.id.payloadPlain);
    mPayloadHex = findViewById(R.id.payloadView);

    mPleaseOpenFile.setVisibility(View.VISIBLE);
    mPayloadHex.setVisibility(View.GONE);
    mPayloadPlain.setVisibility(View.GONE);

    mAdapterHex = new SearchableListArrayAdapter(this, DisplayCharPolicy.DISPLAY_ALL, new ArrayList<>(), new UserConfig() {
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
    mPayloadHex.setMultiChoiceModeListener(new MultiChoiceCallback(mPayloadHex, mAdapterHex, mMainLayout));

    mAdapterPlain = new SearchableListArrayAdapter(this, DisplayCharPolicy.IGNORE_NON_DISPLAYED_CHAR, new ArrayList<>(), new UserConfig() {
      @Override
      public float getFontSize() {
        return mApp.getPlainFontSize();
      }

      @Override
      public int getRowHeight() {
        return mApp.getPlainRowHeight();
      }

      @Override
      public boolean isRowHeightAuto() {
        return mApp.isPlainRowHeightAuto();
      }
    });
    mPayloadPlain.setAdapter(mAdapterPlain);

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
    else if (mPayloadPlain.getVisibility() == View.VISIBLE)
      mAdapterPlain.refresh();
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
        Uri uri = getIntent().getData();

        if (uri != null) {
          mFileData = new FileData(uri);
          final TaskOpen to = new TaskOpen(this, mAdapterHex, mAdapterPlain, this);
          to.execute(uri);
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
    final SearchableListArrayAdapter laa = ((mPayloadPlain.getVisibility() == View.VISIBLE) ? mAdapterPlain : mAdapterHex);
    laa.getFilter().filter(queryStr);
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
      mPayloadPlain.setVisibility(checked ? View.VISIBLE : View.GONE);
    } else {
      setTitle(R.string.app_name);
      mPleaseOpenFile.setVisibility(View.VISIBLE);
      mPayloadHex.setVisibility(View.GONE);
      mPayloadPlain.setVisibility(View.GONE);
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
      UIHelper.openFilePickerInFileSelectionMode(this, activityResultLauncherOpen, mMainLayout);
      return true;
    } else if (id == R.id.action_recently_open) {
      displayRecentlyOpen();
      return true;
    } else if (id == R.id.action_save) {
      if (FileData.isEmpty(mFileData)) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      final Uri uri = FileHelper.getParentUri(mFileData.getUri());
      if (uri != null && FileHelper.hasUriPermission(this, uri, false))
        processFileSave(uri, mFileData.getName(), false);
      else
        UIHelper.toast(this, String.format(getString(R.string.error_file_permission), mFileData.getName()));
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
      mPayloadPlain.setVisibility(checked ? View.VISIBLE : View.GONE);
      mPayloadHex.setVisibility(checked ? View.GONE : View.VISIBLE);
      item.setChecked(checked);
      return true;
    } else if (id == R.id.action_close) {
      onOpenResult(false);
      mAdapterPlain.clear();
      mAdapterHex.clear();
      if (mSearchView != null && !mSearchView.isIconified()) {
        mSearchView.setIconified(true);
      }
    } else if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    }
    return super.onOptionsItemSelected(item);
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
      RecentlyOpenListArrayAdapter.UriData item = setArrayAdapter.getItem(which);
      if (FileHelper.isFileExists(getContentResolver(), item.uri)) {
        if (FileHelper.hasUriPermission(this, item.uri, true))
          processFileOpen(item.uri);
        else {
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
    builder.show();
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
    if (mPayloadPlain.getVisibility() == View.VISIBLE) {
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
        super.onBackPressed();
        finish();
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
    processFileOpen(uri);
  }

  /**
   * Process the opening of the file
   *
   * @param uri Uri data.
   */
  private void processFileOpen(final Uri uri) {
    if (uri != null && uri.getPath() != null) {
      mFileData = new FileData(uri);
      new TaskOpen(this, mAdapterHex, mAdapterPlain, this).execute(uri);
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

    UIHelper.createTextDialog(this, getString(R.string.action_save_title), mFileData.getName(), (dialog, content, layout) -> {
      final String sfile = content.getText().toString();
      if (sfile.trim().isEmpty()) {
        layout.setError(getString(R.string.error_filename));
        return;
      }
      processFileSave(uri, sfile, true);
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
    final DocumentFile ffile = file;

    if (file != null) {
      if(showConfirm) {
        UIHelper.showConfirmDialog(this, getString(R.string.action_save_title),
            getString(R.string.confirm_overwrite),
            (view) -> new TaskSave(this).execute(ffile.getUri()));
      } else
        new TaskSave(this).execute(ffile.getUri());
    } else {
      DocumentFile dfile = sourceDir.createFile("application/octet-stream", filename);
      if (dfile == null) {
        UIHelper.toast(this, getString(R.string.uri_exception));
        Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uri + "'");
      } else
        new TaskSave(this).execute(dfile.getUri());
    }
  }

}

