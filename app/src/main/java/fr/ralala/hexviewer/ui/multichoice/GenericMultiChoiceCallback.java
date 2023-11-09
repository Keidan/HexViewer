package fr.ralala.hexviewer.ui.multichoice;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.StringRes;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;

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
  private final ImageView mRefreshActionViewSelectAll;
  protected final ClipboardManager mClipboard;
  protected final LayoutInflater mLayoutInflater;

  @SuppressLint("InflateParams")
  protected GenericMultiChoiceCallback(MainActivity mainActivity, final ListView listView, final SearchableListArrayAdapter adapter) {
    mActivity = mainActivity;
    mListView = listView;
    mAdapter = adapter;
    /* Attach a rotating ImageView to the refresh item as an ActionView */
    mLayoutInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mRefreshActionViewSelectAll = (ImageView) mLayoutInflater.inflate(R.layout.refresh_action_view_select_all, null);
    mClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
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
  }

  /**
   * Select all action.
   *
   * @param item The item that was clicked.
   */
  private void actionSelectAll(MenuItem item) {
    setActionView(item, mRefreshActionViewSelectAll, () -> {
      final int count = mAdapter.getCount();
      for (int i = 0; i < count; i++) {
        mListView.setItemChecked(i, true);
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
   * @param item              MenuItem
   * @param refreshActionView ImageView
   * @param action            Runnable
   */
  protected void setActionView(final MenuItem item, final ImageView refreshActionView, final Runnable action) {
    AtomicBoolean checkable = new AtomicBoolean(false);
    if (item != null) {
      refreshActionView.clearAnimation();
      Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.clockwise_refresh);
      rotation.setRepeatCount(Animation.INFINITE);
      checkable.set(item.isCheckable());
      item.setCheckable(false);// Do not accept any click events while scanning
      refreshActionView.startAnimation(rotation);
      item.setActionView(refreshActionView);
    }
    final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(() -> {
      action.run();
      if (item != null) {
        item.setCheckable(checkable.get());
        View view = item.getActionView();
        if (view != null) {
          view.clearAnimation();
          item.setActionView(null);
        }
      }
    }, 1000);
  }
}

