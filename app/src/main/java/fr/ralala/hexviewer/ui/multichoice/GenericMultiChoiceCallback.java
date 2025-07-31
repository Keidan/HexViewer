package fr.ralala.hexviewer.ui.multichoice;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation (generic view)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class GenericMultiChoiceCallback implements AbsListView.MultiChoiceModeListener {
  private final ListView mListView;
  protected final SearchableListArrayAdapter mAdapter;
  protected final MainActivity mActivity;
  private final ClipboardManager mClipboard;
  private int mFirstSelection = -1;
  private final AlertDialog mProgress;
  private MenuItem mMenuItemSelectAll;

  @SuppressLint("InflateParams")
  protected GenericMultiChoiceCallback(MainActivity mainActivity, final ListView listView, final SearchableListArrayAdapter adapter) {
    mActivity = mainActivity;
    mListView = listView;
    mAdapter = adapter;
    mClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
    mProgress = UIHelper.createCircularProgressDialog(mActivity, null);
  }

  /**
   * Returns the menu id.
   *
   * @return R.menu.xxx
   */
  public abstract int getMenuId();

  /**
   * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
   *
   * @param mode ActionMode being created.
   * @param menu Menu used to populate action buttons.
   * @return true if the action mode should be created, false if entering this mode should be aborted.
   */
  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(getMenuId(), menu);
    mMenuItemSelectAll = menu.findItem(R.id.action_select_all);
    return true;
  }

  /**
   * Called to refresh an action mode's action menu whenever it is invalidated.
   *
   * @param mode ActionMode being prepared.
   * @param menu Menu used to populate action buttons.
   * @return true if the menu or action mode was updated, false otherwise.
   */
  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  /**
   * Called to report a user click on an action button.
   *
   * @param mode The current ActionMode.
   * @param item The item that was clicked.
   * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
   */
  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    if (item.getItemId() == R.id.action_clear) {
      if (mActivity.getFileData().isSequential()) {
        UIHelper.showErrorDialog(mActivity, mActivity.getFileData().getName(),
          mActivity.getString(R.string.error_open_sequential_add_or_delete_data));
        return false;
      }
      actionClear(item, mode);
      return true;
    } else if (item.getItemId() == R.id.action_select_all) {
      actionSelectAll(item);
      return true;
    } else if (item.getItemId() == R.id.action_edit) {
      return actionEdit(mode);
    } else if (item.getItemId() == R.id.action_copy) {
      return actionCopy(mode);
    }
    return false;
  }

  /**
   * Called when an action mode is about to be exited and destroyed.
   *
   * @param mode The current ActionMode being destroyed.
   */
  @Override
  public void onDestroyActionMode(ActionMode mode) {
    mAdapter.removeSelection();
    if (mProgress.isShowing())
      mProgress.dismiss();
  }

  /**
   * Called when an item is checked or unchecked during selection mode.
   *
   * @param mode     The ActionMode providing the selection mode.
   * @param position Adapter position of the item that was checked or unchecked.
   * @param id       Adapter ID of the item that was checked or unchecked.
   * @param checked  true if the item is now checked, false if the item is now unchecked.
   */
  @Override
  public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    final int checkedCount = mListView.getCheckedItemCount();
    mode.setTitle(String.format(mActivity.getString(R.string.items_selected), checkedCount));
    mAdapter.toggleSelection(position, checked);
    if (checkedCount == 1)
      mFirstSelection = mAdapter.getSelectedIds().get(0);
    if (mMenuItemSelectAll != null)
      mMenuItemSelectAll.setChecked(!mMenuItemSelectAll.isChecked() &&
        mAdapter.getSelectedCount() == mAdapter.getCount());
  }

  /**
   * Select all action.
   *
   * @param item The item that was clicked.
   */
  private void actionSelectAll(MenuItem item) {
    final boolean checked = mAdapter.getSelectedCount() != mAdapter.getCount();
    setActionView(item, () -> {
      final int count = mAdapter.getCount();
      for (int i = 0; i < count; i++) {
        if (mFirstSelection == i && !checked)
          continue;
        mListView.setItemChecked(i, checked);
      }
    });
  }

  /**
   * Copy action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  protected abstract boolean actionCopy(ActionMode mode);

  /**
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  protected abstract void actionClear(MenuItem item, ActionMode mode);

  /**
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  protected abstract boolean actionEdit(ActionMode mode);

  /**
   * Closing the action mode.
   *
   * @param mode    The ActionMode providing the selection mode.
   * @param delayed Delayed ?
   */
  protected void closeActionMode(ActionMode mode, boolean delayed) {
    if (delayed)
      new Handler(Looper.getMainLooper()).postDelayed(mode::finish, 500);
    else
      mode.finish();
  }

  /**
   * Displays an error message.
   *
   * @param message The message.
   */
  protected void displayError(@StringRes int message) {
    UIHelper.showErrorDialog(mActivity, mActivity.getString(R.string.error_title), mActivity.getString(message));
  }

  /**
   * Sets the action view.
   *
   * @param item   MenuItem
   * @param action Runnable
   */
  protected void setActionView(final MenuItem item, final Runnable action) {
    UIHelper.showCircularProgressDialog(mProgress);
    final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(() -> {
      action.run();
      if (item != null) {
        item.setCheckable(true);
        item.setChecked(mAdapter.getSelectedCount() == mAdapter.getCount());
        View view = item.getActionView();
        if (view != null) {
          view.clearAnimation();
          item.setActionView(null);
        }
      }
      mProgress.dismiss();
    }, 500);
  }

  /**
   * Copy sb to Android clipboard then close action mode.
   *
   * @param logTitle Title in log.
   * @param mode     ActionMode
   * @param sb       String to copy.
   * @return false in case on error
   */
  protected boolean copyAndClose(String logTitle, ActionMode mode, StringBuilder sb) {
    try {
      ClipData clip = ClipData.newPlainText(mActivity.getString(R.string.app_name), sb);
      mClipboard.setPrimaryClip(clip);
    } catch (Exception exception) {
      ApplicationCtx.addLog(mActivity, logTitle,
        "E: TransactionTooLargeException size: " + sb.toString().length());
      displayError(R.string.error_too_many_text_copied);
      return false;
    }
    UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.text_copied),
      SysHelper.sizeToHuman(mActivity, sb.length(), true, true, false)));
    closeActionMode(mode, true);
    return true;
  }
}

