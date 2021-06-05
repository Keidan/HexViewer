package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.documentfile.provider.DocumentFile;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.RecentlyOpenListArrayAdapter;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.LineUpdateTextWatcher;
import fr.ralala.hexviewer.ui.utils.UIHelper;
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
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, TaskOpen.OpenResultListener {
  private static final int FILE_OPEN_CODE = 101;
  private static final int FILE_SAVE_CODE = 102;
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private SearchableListArrayAdapter mAdapterHex = null;
  private SearchableListArrayAdapter mAdapterPlain = null;
  private LinearLayout mMainLayout = null;
  private String mFile = "";
  private TextView mPleaseOpenFile = null;
  private ListView mPayloadHex = null;
  private ListView mPayloadPlain = null;
  private MenuItem mSearchMenu = null;
  private MenuItem mPlainMenu = null;
  private MenuItem mSaveMenu = null;
  private MenuItem mCloseMenu = null;
  private SearchView mSearchView = null;
  private MenuItem mRecentlyOpen = null;

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
    mPayloadHex.setOnItemLongClickListener(this);

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
    mPayloadPlain.setOnItemLongClickListener(this);

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, 1);

    handleIntent(getIntent());
  }

  /**
   * Called when the activity is resumed.
   */
  public void onResume() {
    super.onResume();
    /* refresh */
    onOpenResult(mFile != null && !mFile.isEmpty());
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
      String query = intent.getStringExtra(SearchManager.QUERY);
      doSearch(query == null ? "" : query);
    } else {
      if (intent.getData() != null) {
        Uri uri = getIntent().getData();

        if (uri != null) {
          File f = new File(Objects.requireNonNull(uri.getPath()));
          mFile = SysHelper.basename(f.getName());
          final TaskOpen to = new TaskOpen(this, mAdapterHex, mAdapterPlain, this);
          to.execute(uri);
        }
      }
    }
  }

  /**
   * Called when the activity getsa result after a call to the startActivityForResult method.
   *
   * @param requestCode The request code.
   * @param resultCode  The result code.
   * @param data        The result data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
                                  final Intent data) {
    switch (requestCode) {
      case FILE_OPEN_CODE:
        if (resultCode == RESULT_OK) {
          if(FileHelper.takeUriPermissions(this, data.getData(), true)) {
            processFileOpen(data);
          }
          else
            UIHelper.toast(this, String.format(getString(R.string.error_file_permission), FileHelper.getFileName(data.getData())));
        }
        break;
      case FILE_SAVE_CODE:
        if (resultCode == RESULT_OK) {
          FileHelper.takeUriPermissions(this, data.getData(), false);
          processFileSave(data);
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
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
    setMenuEnabled(mCloseMenu, success);
    setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().isEmpty());
    if (success) {
      String title = getString(R.string.app_name);
      title += " - " + SysHelper.abbreviate(mFile,
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
      mFile = null;
    }
  }

  /**
   * Sets the visibility of the menu item.
   * @param menu MenuItem
   * @param visible If true then the item will be visible; if false it is hidden.
   */
  private void setMenuVisible(final MenuItem menu, final boolean visible) {
    if (menu != null)
      menu.setVisible(visible);
  }

  /**
   * Sets whether the menu item is enabled.
   * @param menu MenuItem
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
    if (mFile != null && !mFile.isEmpty()) {
      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        length = mApp.getAbbreviateLandscape();
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        length = mApp.getAbbreviatePortrait();
      }
      if (length != 0) {
        String title = getString(R.string.app_name);
        title += " - " + SysHelper.abbreviate(mFile, length);
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
      UIHelper.openFilePickerInFileSelectionMode(this, mMainLayout, FILE_OPEN_CODE);
      return true;
    } else if (id == R.id.action_recently_open) {
      displayRecentlyOpen();
      return true;
    } else if (id == R.id.action_save) {
      if (mFile == null || mFile.isEmpty()) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      UIHelper.openFilePickerInDirectorSelectionMode(this, FILE_SAVE_CODE);
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
      if(FileHelper.isFileExists(getContentResolver(), item.uri)) {
        if(FileHelper.hasUriPermission(this, item.uri, true))
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
        FileHelper.releaseUriPermissions(this, item.uri, true);
      }
    });
    builder.show();
  }

  /**
   * Called when the used perform a long press on a listview item.
   *
   * @param parent   The adapter view.
   * @param view     The current view
   * @param position The position
   * @param id       The id.
   * @return boolean
   */
  @SuppressLint("InflateParams")
  @Override
  public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                 final int position, final long id) {
    String string = mAdapterHex.getItem(position);
    if (string == null)
      return false;
    if (mPayloadPlain.getVisibility() == View.VISIBLE) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return false;
    }

    final String[] split = SysHelper.extractHexAndSplit(string);
    final String hex = split[0] + " " + split[1];
    string = hex.replaceAll(" ", "");
    /* Dialog creation */
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(getString(R.string.update))
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(this);
    builder.setView(factory.inflate(R.layout.content_dialog_update_text, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    /* default values */
    TextView source = dialog.findViewById(R.id.tvSource);
    final TextView result = dialog.findViewById(R.id.tvResult);
    final TextInputEditText inputHex = dialog.findViewById(R.id.etInputHex);
    final TextInputLayout inputHexLayout = dialog.findViewById(R.id.tilInputHex);
    final AppCompatCheckBox chkSmartInput = dialog.findViewById(R.id.chkSmartInput);

    if (chkSmartInput != null) {
      chkSmartInput.setChecked(mApp.isSmartInput());
      chkSmartInput.setOnCheckedChangeListener((comp, isChecked) -> mApp.setSmartInput(isChecked));
    }
    if (source != null)
      source.setText((split[0] + "\n" + split[1]));
    if (result != null) {
      result.setTextColor(ContextCompat.getColor(this, R.color.colorResultSuccess));
      result.setText(SysHelper.hex2bin(string));
    }

    if (inputHex != null && inputHexLayout != null) {
      inputHex.setText(hex.trim());
      inputHex.addTextChangedListener(new LineUpdateTextWatcher(this, result, inputHexLayout, mApp));
    }

    /* main action */
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> {
      if (inputHex != null && inputHexLayout != null) {
        if(validateDialog(hex.replaceAll(" ", ""), inputHex, inputHexLayout, position))
          dialog.dismiss();
      } else
        dialog.dismiss();
    });
    return false;
  }

  /**
   * Validation of the modification.
   *
   * @param reference The reference bytes.
   * @param input     Input EditText.
   * @param inputLayout     Input EditText layout.
   * @param position  ListView position.
   * @return false on error
   */
  private boolean validateDialog(final String reference, final EditText input, final TextInputLayout inputLayout, final int position) {
    final String validate = input.getText().toString().trim().replaceAll(" ", "").toLowerCase(Locale.US);
    if (!SysHelper.isValidHexLine(validate)) {
      inputLayout.setError(getString(R.string.error_entry_format));
      return false;
    }
    final byte[] buf = SysHelper.hexStringToByteArray(validate);
    final byte[] ref = SysHelper.hexStringToByteArray(reference);
    if (Arrays.equals(ref, buf)) {
      /* nothing to do */
      return true;
    }
    mApp.getPayload().update(position, buf);
    List<String> li = SysHelper.formatBuffer(buf, null);
    if (li.isEmpty())
      mAdapterHex.removeItem(position);
    else
      mAdapterHex.setItem(position, li.get(0));
    return true;
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
      mFile = FileHelper.getFileName(uri);
      new TaskOpen(this, mAdapterHex, mAdapterPlain, this).execute(uri);
    } else {
      UIHelper.toast(this, getString(R.string.error_filename));
    }
  }

  /**
   * Process the saving of the file
   *
   * @param data Intent data.
   */
  private void processFileSave(final Intent data) {
    Uri uriDir = data.getData();

    getContentResolver().takePersistableUriPermission(uriDir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

    UIHelper.createTextDialog(this, getString(R.string.action_save_title), mFile, (dialog, content, layout) -> {
      final String sfile = content.getText().toString();
      if (sfile.trim().isEmpty()) {
        layout.setError(getString(R.string.error_filename));
        return;
      }

      DocumentFile sourceDir = DocumentFile.fromTreeUri(this, uriDir);
      if (sourceDir == null) {
        UIHelper.toast(this, getString(R.string.uri_exception));
        Log.e(getClass().getSimpleName(), "1 - Uri exception: '" + uriDir + "'");
        dialog.dismiss();
        return;
      }
      DocumentFile file = null;
      for (DocumentFile f : sourceDir.listFiles()) {
        if (f.getName() != null && f.getName().endsWith(sfile)) {
          file = f;
          break;
        }
      }
      final DocumentFile ffile = file;

      if (file != null) {
        UIHelper.showConfirmDialog(this, getString(R.string.action_save_title),
            getString(R.string.confirm_overwrite),
            (view) -> new TaskSave(this).execute(ffile.getUri()));
      } else {
        DocumentFile dfile = sourceDir.createFile("application/octet-stream", sfile);
        if (dfile == null) {
          UIHelper.toast(this, getString(R.string.uri_exception));
          Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uriDir + "'");
        } else
          new TaskSave(this).execute(dfile.getUri());
      }
      dialog.dismiss();
    });
  }

}

