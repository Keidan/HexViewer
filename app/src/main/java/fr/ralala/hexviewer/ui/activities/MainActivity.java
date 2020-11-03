package fr.ralala.hexviewer.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
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
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private ApplicationCtx mApp = null;
  private ListArrayAdapter mAdapter = null;
  private final ViewGroup mNullParent = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    UIHelper.openTransition(this);
    super.onCreate(savedInstanceState);
    String path = null;
    if (getIntent() != null && getIntent().getData() != null)
      path = getIntent().getData().getEncodedPath();

    setContentView(R.layout.activity_main);

    final ListView payloadLV = findViewById(R.id.payloadView);
    mAdapter = new ListArrayAdapter(this, new ArrayList<>());
    payloadLV.setAdapter(mAdapter);
    payloadLV.setOnItemLongClickListener(this);

    mApp = (ApplicationCtx) getApplication();
    if (path != null) {
      mApp.setFilename(Helper.basename(path));
      final TaskOpen to = new TaskOpen(this, mAdapter);
      to.execute(getIntent().getData());
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
    // Check which request we're responding to
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        mApp.setFilename(data
            .getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY));
        new TaskOpen(this, mAdapter).execute();
      }
    } else if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == RESULT_OK) {
        final String rootDir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);


        createTextDialog(getString(R.string.chooser_save_name), R.layout.content_dialog_update, Helper.basename(mApp.getFilename()), (dialog, content) -> {
          final String file = content.getText().toString();
          if (file.trim().isEmpty()) {
            UIHelper.shakeError(content, getString(R.string.error_filename));
            return ;
          }
          String path = "" + rootDir;
          if (!path.endsWith("/")) path += "/";
          path += file;
          final String dir = path;
          if (new File(path).exists()) {
            UIHelper.showConfirmDialog(this, getString(R.string.chooser_save_name),
              getString(R.string.confirm_overwrite),
              (view) -> {
                mApp.setFilename(dir);
                new TaskSave(this).execute((Void [])null);
              });
          } else {
            mApp.setFilename(dir);
            new TaskSave(this).execute((Void [])null);
          }
          dialog.dismiss();
        });
      }
    }
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
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_FILE_ONLY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.chooser_open_name));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.chooser_open_message) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
          .getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
      Helper.switchToForResult(this, FileChooserActivity.class, extra, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
      return true;
    }if (id == R.id.action_plain_text) {
      if(item.isChecked()) {
        /* to hex */
        mApp.setPlainText(false);
        mAdapter.notifyDataSetChanged();
      } else {
        /* to plain */
        mApp.setPlainText(true);
        mAdapter.notifyDataSetChanged();
      }
      item.setChecked(!item.isChecked());
      return true;
    } else if (id == R.id.action_save) {
      if (mApp.getFilename() == null) {
        UIHelper.toast(this, "Open a file before!");
        return true;
      }
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.chooser_save_name));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.chooser_save_message) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
      Helper.switchToForResult(this, FileChooserActivity.class, extra, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
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

    createTextDialog(getString(R.string.update), R.layout.content_dialog_update, hex, (dialog, content) -> {
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
      UIHelper.closeTransition(this);
      super.onBackPressed();
      Process.killProcess(android.os.Process.myPid());
      return;
    } else {
      UIHelper.toast(this, getString(R.string.on_double_back_exit_text));
    }
    mLastBackPressed = System.currentTimeMillis();
  }


  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, EditText editText);
  }

  private void createTextDialog(String title, int contentId, String defaultValue, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> { });
    LayoutInflater factory = LayoutInflater.from(this);
    builder.setView(factory.inflate(contentId, mNullParent));
    final AlertDialog dialog = builder.create();
    dialog.show();
    EditText et = dialog.findViewById(R.id.editText);
    if(et != null)
      et.setText(defaultValue);
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, et));
  }
}

