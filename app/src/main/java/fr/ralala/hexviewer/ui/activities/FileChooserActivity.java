package fr.ralala.hexviewer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.models.FileChooserOption;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * File chooser activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserActivity extends AbstractFileChooserActivity {
  public static final String FILECHOOSER_SELECTION_KEY = "selection";
  public static final int FILECHOOSER_SELECTION_TYPE_FILE = 1;
  public static final int FILECHOOSER_SELECTION_TYPE_DIRECTORY = 2;
  private static final int MSG_ERR    = 0;
  private static final int MSG_OK     = 1;
  private static final int MSG_CANCEL = 2;
  private FileChooserOption mOpt = null;
  private Handler mHandler = null;
  private AlertDialog mProgress = null;
  public enum ErrorStatus {
    NO_ERROR, CANCEL, ERROR_NOT_MOUNTED, ERROR_CANT_READ
  }

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHandler = new IncomingHandler(this);
  }


  /**
   * Called when the options item is clicked (cancel).
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == R.id.action_cancel) {
      cancel();
    }
    return false;
  }

  /**
   * Called when the options menu is clicked.
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.filechooser_menu, menu);
    return true;
  }

  /**
   * Called when a file is selected.
   * @param opt The file chooser option.
   */
  @Override
  protected void onFileSelected(final FileChooserOption opt) {
    mProgress = UIHelper.showProgressDialog(this, R.string.loading);
    mProgress.show();
    // useful code, variables declarations...
    new Thread((() -> {
      // starts the first long operation
      runOnUiThread(() -> {
        Message msg;
        ErrorStatus status = doComputeHandler(opt);
        if (status == ErrorStatus.CANCEL) {
          msg = mHandler.obtainMessage(MSG_CANCEL, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        } else if (status != ErrorStatus.NO_ERROR) {
          // error management, creates an error message
          msg = mHandler.obtainMessage(MSG_ERR, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        } else {
          msg = mHandler.obtainMessage(MSG_OK, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        }
      });
    })).start();
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    File parent = mCurrentDir.getParentFile();
    if (parent == null || parent.equals(mDefaultDir.getParentFile())) {
      super.onBackPressed();
      cancel();
    } else {
      mCurrentDir = parent;
      fill(mCurrentDir);
    }
  }

  /**
   * Cancel the file chooser.
   */
  private void cancel() {
    finish();
  }

  /**
   * Called when the activity is destroyed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    cancel();
  }

  /**
   * Compute the the response.
   * @param userObject The user object.
   * @return ErrorStatus
   */
  public ErrorStatus doComputeHandler(final FileChooserOption userObject) {
    mOpt = userObject;
    if (mOpt == null)
      return ErrorStatus.CANCEL; /* cancel action */
    if (!isMountedSdcard())
      return ErrorStatus.ERROR_NOT_MOUNTED;

    final File file = new File(new File(mOpt.getPath()).getParent(),
        mOpt.getName());
    if (!file.canRead())
      return ErrorStatus.ERROR_CANT_READ;
    return ErrorStatus.NO_ERROR;
  }

  /**
   * Handle a success response.
   */
  public void onSuccessHandler() {
    final Intent returnIntent = new Intent();
    int result = RESULT_CANCELED;
    if (mOpt != null) {
      final File file = new File(new File(mOpt.getPath()).getParent(),
          mOpt.getName());
      returnIntent.putExtra(FILECHOOSER_SELECTION_KEY, file.getAbsolutePath());
      if(getUserMessage() != null)
        returnIntent.putExtra(FILECHOOSER_USER_MESSAGE, getUserMessage());
      result = RESULT_OK;
    }
    setResult(result, returnIntent);
    mOpt = null;
    cancel();
  }

  /**
   * Handle a cancel request.
   */
  public void onCancelHandler() {

  }

  /**
   * Handle an error.
   */
  public void onErrorHandler() {
    mOpt = null;
    final Intent returnIntent = new Intent();
    setResult(RESULT_CANCELED, returnIntent);
    onBackPressed();
  }

  /**
   * Tests id the sdcard is mounted.
   * @return boolean
   */
  private boolean isMountedSdcard() {
    final String state = Environment.getExternalStorageState();
    return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
  }


  private static class IncomingHandler extends Handler {

    private FileChooserActivity adaptee = null;

    private IncomingHandler(FileChooserActivity adaptee) {
      this.adaptee = adaptee;
    }

    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {
        case MSG_ERR:
          final String err = "Activity compute failed !";
          if (adaptee.mProgress.isShowing())
            adaptee.mProgress.dismiss();
          Toast.makeText(adaptee, err, Toast.LENGTH_SHORT).show();
          adaptee.onErrorHandler();
          break;
        case MSG_OK:
          if (adaptee.mProgress.isShowing())
            adaptee.mProgress.dismiss();
          adaptee.onSuccessHandler();
          break;
        case MSG_CANCEL:
          if (adaptee.mProgress.isShowing()) adaptee.mProgress.dismiss();
          adaptee.onCancelHandler();
          break;
        default: // should never happen
          break;
      }
    }
  }
}
