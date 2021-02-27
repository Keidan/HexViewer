package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
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

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.DisplayCharPolicy;
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
  private static final int ABBREVIATE_LANDSCAPE = 16;
  private static final int ABBREVIATE_PORTRAIT = 8;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private SearchableListArrayAdapter mAdapter = null;
  private SearchableListArrayAdapter mAdapterPlain = null;
  private LinearLayout mMainLayout = null;
  private String mFile = "";
  private TextView mPleaseOpenFile = null;
  private ListView mPayloadView = null;
  private ListView mPayloadPlain = null;
  private MenuItem mSearchMenu = null;
  private MenuItem mPlainMenu = null;
  private MenuItem mSaveMenu = null;
  private MenuItem mCloseMenu = null;
  private SearchView mSearchView = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    mMainLayout = findViewById(R.id.mainLayout);
    mPleaseOpenFile = findViewById(R.id.pleaseOpenFile);
    mPayloadPlain = findViewById(R.id.payloadPlain);
    mPayloadView = findViewById(R.id.payloadView);

    mPleaseOpenFile.setVisibility(View.VISIBLE);
    mPayloadView.setVisibility(View.GONE);
    mPayloadPlain.setVisibility(View.GONE);

    mAdapter = new SearchableListArrayAdapter(this, DisplayCharPolicy.DISPLAY_ALL, new ArrayList<>());
    mPayloadView.setAdapter(mAdapter);
    mPayloadView.setOnItemLongClickListener(this);

    mAdapterPlain = new SearchableListArrayAdapter(this, DisplayCharPolicy.IGNORE_NON_DISPLAYED_CHAR, new ArrayList<>());
    mPayloadPlain.setAdapter(mAdapterPlain);
    mPayloadPlain.setOnItemLongClickListener(this);

    mApp = (ApplicationCtx) getApplication();

    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    }, 1);

    handleIntent(getIntent());
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
          final TaskOpen to = new TaskOpen(this, mAdapter, mAdapterPlain, this);
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
          Uri uri = data.getData();
          if (uri != null && uri.getPath() != null) {
            File file = new File(uri.getPath());
            mFile = file.getName();
            new TaskOpen(this, mAdapter, mAdapterPlain, this).execute(uri);
          } else {
            UIHelper.toast(this, getString(R.string.error_filename));
          }
        }
        break;
      case FILE_SAVE_CODE:
        if (resultCode == RESULT_OK) {
          Uri uriDir = data.getData();

          getContentResolver().takePersistableUriPermission(uriDir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

          UIHelper.createTextDialog(this, getString(R.string.action_save_title), mFile, (dialog, content) -> {
            final String sfile = content.getText().toString();
            if (sfile.trim().isEmpty()) {
              UIHelper.shakeError(content, getString(R.string.error_filename));
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
    mSearchMenu = menu.findItem(R.id.action_search);
    mPlainMenu = menu.findItem(R.id.action_plain_text);
    mSaveMenu = menu.findItem(R.id.action_save);
    mCloseMenu = menu.findItem(R.id.action_close);

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
    final SearchableListArrayAdapter laa = ((mPayloadPlain.getVisibility() == View.VISIBLE) ? mAdapterPlain : mAdapter);
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
    if (mSearchMenu != null)
      mSearchMenu.setVisible(success);
    if (mPlainMenu != null) {
      checked = mPlainMenu.isChecked();
      mPlainMenu.setEnabled(success);
    }
    if (mSaveMenu != null)
      mSaveMenu.setEnabled(success);
    if (mCloseMenu != null)
      mCloseMenu.setEnabled(success);
    if (success) {
      String title = getString(R.string.app_name);
      title += " - " + SysHelper.abbreviate(mFile,
          getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
              ABBREVIATE_LANDSCAPE : ABBREVIATE_PORTRAIT);
      setTitle(title);
      mPleaseOpenFile.setVisibility(View.GONE);
      mPayloadView.setVisibility(checked ? View.GONE : View.VISIBLE);
      mPayloadPlain.setVisibility(checked ? View.VISIBLE : View.GONE);
    } else {
      setTitle(R.string.app_name);
      mPleaseOpenFile.setVisibility(View.VISIBLE);
      mPayloadView.setVisibility(View.GONE);
      mPayloadPlain.setVisibility(View.GONE);
    }
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
        length = ABBREVIATE_LANDSCAPE;
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        length = ABBREVIATE_PORTRAIT;
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
      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("*/*");
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      try {
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_file_to_open)), FILE_OPEN_CODE);
      } catch (android.content.ActivityNotFoundException ex) {
        Snackbar customSnackBar = Snackbar.make(mMainLayout, getString(R.string.error_no_file_manager), Snackbar.LENGTH_LONG);
        customSnackBar.setAction(getString(R.string.install), (v) -> {
          final String search = getString(R.string.file_manager_keyword);
          try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + search + "&c=apps")));
          } catch (android.content.ActivityNotFoundException ignore) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + search + "&c=apps")));
          }
        });
        customSnackBar.show();
      }
      return true;
    } else if (id == R.id.action_save) {
      if (mFile == null || mFile.isEmpty()) {
        UIHelper.toast(this, getString(R.string.open_a_file_before));
        return true;
      }
      /* Here the FileManager should already be installed */
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
      startActivityForResult(intent, FILE_SAVE_CODE);
      return true;
    } else if (id == R.id.action_plain_text) {
      /* to hex */
      /* to plain */
      boolean checked = !item.isChecked();
      mPayloadPlain.setVisibility(checked ? View.VISIBLE : View.GONE);
      mPayloadView.setVisibility(checked ? View.GONE : View.VISIBLE);
      item.setChecked(checked);
      return true;
    } else if (id == R.id.action_close) {
      onOpenResult(false);
      mAdapterPlain.clear();
      mAdapter.clear();
      if (mSearchView != null && !mSearchView.isIconified()) {
        mSearchView.setIconified(true);
      }
    }
    return super.onOptionsItemSelected(item);
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
  @Override
  public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                 final int position, final long id) {
    String string = mAdapter.getItem(position);
    if (string == null)
      return false;
    if (mPayloadPlain.getVisibility() == View.VISIBLE) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return false;
    }

    final String hex = SysHelper.extractString(string);

    UIHelper.createTextDialog(this, getString(R.string.update), hex, (dialog, content) -> {
      final String validate = content.getText().toString().trim().replaceAll(" ", "").toLowerCase(Locale.US);

      if (!validate.matches("\\p{XDigit}+") || (validate.length() % 2 != 0) || validate.length() > (SysHelper.MAX_BY_ROW * 2)) {
        UIHelper.shakeError(content, getString(R.string.error_entry_format));
        return;
      }
      final byte[] buf = SysHelper.hexStringToByteArray(validate);
      mApp.getPayload().update(position, buf);
      mAdapter.setItem(position, SysHelper.formatBuffer(buf, null).get(0));
      dialog.dismiss();
    });
    return false;
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

}

