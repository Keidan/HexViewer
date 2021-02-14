package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FilePath;
import fr.ralala.hexviewer.utils.Helper;
import fr.ralala.hexviewer.ui.adapters.ListArrayAdapter;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
  private static final int FILE_OPEN_CODE = 101;
  private static final int FILE_SAVE_CODE = 102;
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private ListArrayAdapter mAdapter = null;
  private LinearLayout mMainLayout = null;
  private String mFile = "";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Uri uri = null;
    if (getIntent() != null && getIntent().getData() != null)
      uri = getIntent().getData();

    setContentView(R.layout.activity_main);

    mMainLayout = findViewById(R.id.mainLayout);

    final ListView payloadLV = findViewById(R.id.payloadView);
    mAdapter = new ListArrayAdapter(this, new ArrayList<>());
    payloadLV.setAdapter(mAdapter);
    payloadLV.setOnItemLongClickListener(this);

    mApp = (ApplicationCtx) getApplication();
    if (uri != null) {
      File f = new File(Objects.requireNonNull(uri.getPath()));
      mFile = Helper.basename(f.getName());
      final TaskOpen to = new TaskOpen(this, mAdapter);
      to.execute(uri);
    }

    /* permissions */
    ActivityCompat.requestPermissions(this,new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    },1);
  }

  /**
   * Called when the activity getsa result after a call to the startActivityForResult method.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The result data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
                                  final Intent data) {
    switch (requestCode) {
      case FILE_OPEN_CODE:
        if (resultCode == RESULT_OK) {
          Uri uri = data.getData();
          if(uri != null && uri.getPath() != null) {
            File file = new File(uri.getPath());
            mFile = file.getName();
            new TaskOpen(this, mAdapter).execute(uri);
          } else {
            UIHelper.toast(this, getString(R.string.error_filename));
          }
        }
        break;
      case FILE_SAVE_CODE:
        if (resultCode == RESULT_OK) {
          Uri uri = data.getData();
          Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
              DocumentsContract.getTreeDocumentId(uri));
          final String rootDir = FilePath.getPath(this, docUri);

          createTextDialog(getString(R.string.action_save_title), mFile, (dialog, content) -> {
            final String file = content.getText().toString();
            if (file.trim().isEmpty()) {
              UIHelper.shakeError(content, getString(R.string.error_filename));
              return ;
            }
            final File filepath = new File(rootDir, mFile);
            if (filepath.exists()) {
              UIHelper.showConfirmDialog(this, getString(R.string.action_save_title),
                  getString(R.string.confirm_overwrite),
                  (view) -> new TaskSave(this).execute(filepath));
            } else {
              new TaskSave(this).execute(filepath);
            }
            dialog.dismiss();
          });
        }

        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Called to create the option menu.
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  /**
   * Called when the user select an option menu item.
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_open) {
      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("*/*");
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      try {
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_file_to_open)), FILE_OPEN_CODE);
      } catch (android.content.ActivityNotFoundException ex) {
        Snackbar customSnackBar = Snackbar.make(mMainLayout, getString(R.string.error_no_file_manager), Snackbar.LENGTH_LONG );
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
      startActivityForResult(intent, FILE_SAVE_CODE);
      return true;
    } else if (id == R.id.action_plain_text) {
      /* to hex */
      /* to plain */
      mApp.setPlainText(!item.isChecked());
      mAdapter.notifyDataSetChanged();
      item.setChecked(!item.isChecked());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when the used perform a long press on a listview item.
   * @param parent The adapter view.
   * @param view The current view
   * @param position The position
   * @param id The id.
   * @return boolean
   */
  @Override
  public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                 final int position, final long id) {
    String string = mAdapter.getItem(position);
    if(string == null)
      return false;

    if(mApp.isPlainText()) {
      UIHelper.toast(this, getString(R.string.error_not_supported_in_plain_text));
      return false;
    }
    final String hex = Helper.extractString(string);

    createTextDialog(getString(R.string.update), hex, (dialog, content) -> {
      final String validate = content.getText().toString().trim().replaceAll(" ", "").toLowerCase(Locale.US);
      if (!validate.matches("\\p{XDigit}+") || !(validate.length() % 2 == 0) || validate.length() > (Helper.MAX_BY_ROW * 2)) {
        UIHelper.shakeError(content, getString(R.string.error_entry_format));
        return;
      }
      final byte[] buf = Helper.hexStringToByteArray(validate);
      final int pos = (position * Helper.MAX_BY_ROW);
      mApp.getPayload().update(pos, buf);
      mAdapter.setItem(position, Helper.formatBuffer(buf).get(0));
      dialog.dismiss();
    });
    return false;
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      super.onBackPressed();
      finish();
      return;
    } else {
      UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
    }
    mLastBackPressed = System.currentTimeMillis();
  }


  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, EditText editText);
  }

  @SuppressLint("InflateParams")
  private void createTextDialog(String title, String defaultValue, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> { });
    LayoutInflater factory = LayoutInflater.from(this);
    builder.setView(factory.inflate(R.layout.content_dialog_update, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    EditText et = dialog.findViewById(R.id.editText);
    if(et != null)
      et.setText(defaultValue);
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, et));
  }
}

