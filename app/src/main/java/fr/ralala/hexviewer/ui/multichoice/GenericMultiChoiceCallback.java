package fr.ralala.hexviewer.ui.multichoice;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.system.SysHelper;

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
public abstract class GenericMultiChoiceCallback implements ActionMode.Callback {
  private static final String TAG = "Multichoice";
  protected final SearchableListArrayAdapter mAdapter;
  protected final AppCompatActivity mActivity;
  protected final ICommonUI mCommonUI;
  private final ClipboardManager mClipboard;
  private int mFirstSelection = -1;
  private final AlertDialog mProgress;
  private MenuItem mMenuItemSelectAll;
  protected boolean mFromAll = false;
  private ActionMode mActionMode;
  private int mPreviousCount = 0;
  private boolean mMultiSelectMode = false;

  /**
   * Functional interface representing an asynchronous action
   * that accepts a Runnable callback to signal completion.
   */
  protected interface AsyncAction {
    /**
     * Runs the asynchronous action.
     *
     * @param done Runnable to be executed when the action is complete.
     */
    void run(Runnable done);
  }

  @SuppressLint("InflateParams")
  protected GenericMultiChoiceCallback(AppCompatActivity activity, ICommonUI commonUI, final ListView listView, final SearchableListArrayAdapter adapter) {
    mActivity = activity;
    mCommonUI = commonUI;
    mAdapter = adapter;
    mClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
    mProgress = UIHelper.createCircularProgressDialog(mActivity, null);
    setListViewListeners(listView);
  }

  private void setListViewListeners(final ListView listView) {
    listView.setOnItemClickListener((parent, view, position, id) -> {
      if (mMultiSelectMode) {
        final Integer pos = position;
        final boolean currentlySelected = mAdapter.getSelectedItemsIds().contains(pos);
        onItemCheckedStateChanged(mActionMode, position, !currentlySelected);
      } else {
        mCommonUI.onLineItemClick(position);
      }
    });
    listView.setOnItemLongClickListener((parent, view, position, id) -> {
      if (mActionMode == null) {
        mActionMode = mActivity.startSupportActionMode(this);
        mAdapter.toggleSelection(position, true, true);
        updateTitle(mActionMode);
        mFirstSelection = position;
      }
      return true;
    });
  }

  protected SearchableListArrayAdapter getAdapter() {
    return mAdapter;
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
    setMultiSelectMode(true);
    mPreviousCount = mAdapter.getCount();
    mActionMode = mode;
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
    int id = item.getItemId();
    if (id == R.id.action_clear) {
      if (mCommonUI.getFileData().isSequential()) {
        UIHelper.showErrorDialog(mActivity, mCommonUI.getFileData().getName(),
          mActivity.getString(R.string.error_open_sequential_add_or_delete_data));
        return false;
      }
      actionClear(item, mode);
      return true;
    } else if (id == R.id.action_select_all) {
      actionSelectAll(item, mode);
      return true;
    } else if (id == R.id.action_edit) {
      return actionEdit(mode);
    } else if (id == R.id.action_copy) {
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
    setMultiSelectMode(false);
    mActionMode = null;
    mFromAll = false;
    mAdapter.removeSelection();
    if (mProgress.isShowing())
      mProgress.dismiss();
  }

  private void updateTitle(ActionMode mode) {
    if (mode != null) {
      final int checkedCount = mAdapter.getSelectedCount();
      mode.setTitle(String.format(mActivity.getString(R.string.items_selected), checkedCount));
    }
  }

  /**
   * Called when an item is checked or unchecked during selection mode.
   *
   * @param mode     The ActionMode providing the selection mode.
   * @param position Adapter position of the item that was checked or unchecked.
   * @param checked  true if the item is now checked, false if the item is now unchecked.
   */
  public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked) {
    final int checkedCount = mAdapter.getSelectedCount();
    if (!mFromAll) {
      mAdapter.toggleSelection(position, checked, true);
      updateTitle(mode);
      if (mAdapter.getSelectedCount() == 0) {
        mode.finish();
        return;
      }

      // Only update firstSelection if NOT in mass selection mode
      if (checkedCount == 1 && !mAdapter.getSelectedIds().isEmpty())
        mFirstSelection = mAdapter.getSelectedIds().get(0);

      if (mMenuItemSelectAll != null)
        mMenuItemSelectAll.setChecked(
          !mMenuItemSelectAll.isChecked() &&
            mAdapter.getSelectedCount() == mAdapter.getCount()
        );
    } else
      // When fromAll is true, just toggle without updating firstSelection or UI
      mAdapter.toggleSelection(position, checked, false);
  }

  /**
   * Handles the "Select All" action in batches asynchronously
   * to avoid blocking the UI thread.
   *
   * @param item The menu item that triggered the action.
   * @param mode The current ActionMode providing selection context.
   */
  private void actionSelectAll(MenuItem item, ActionMode mode) {
    // Get total number of items in adapter
    final int totalCount = mAdapter.getCount();
    // Get number of currently selected items
    final int selectedCount = mAdapter.getSelectedCount();
    // Determine whether to select all or deselect all based on current selection
    final boolean selectAll = selectedCount != totalCount;

    // Lock flag to prevent triggering onItemCheckedStateChanged during batch processing
    mFromAll = true;

    // Use setActionView to perform the batch selection asynchronously
    setActionView(item, mode, done -> {
      // Evaluate batch size dynamically based on total count
      final int batchSize = evaluateBatch(totalCount);

      // Create a handler for running batched tasks on the main thread
      Handler handler = new Handler(Looper.getMainLooper());

      // Index to track current batch start
      final int[] index = {0};

      // Runnable to perform selection in batches
      Runnable batchRunnable = new Runnable() {
        @Override
        public void run() {
          // Calculate the end index for the current batch
          int end = Math.min(index[0] + batchSize, totalCount);

          // Iterate over the current batch and set checked state
          for (int i = index[0]; i < end; i++) {
            // Skip the first selected item to preserve its state
            if (mFirstSelection == i) continue;

            // Toggle selection state in the adapter without triggering callbacks
            mAdapter.toggleSelection(i, selectAll, false);
          }


          // After batch loop, ensure first selection remains checked if valid
          if (mFirstSelection >= 0 && mFirstSelection < totalCount) {
            mAdapter.toggleSelection(mFirstSelection, true, false);
          }

          // Update index to next batch start
          index[0] = end;

          // If there are more items to process, post this runnable again with a short delay
          if (index[0] < totalCount) {
            handler.postDelayed(this, 1);
          } else {
            // All batches processed, signal completion
            done.run();
          }
        }
      };

      // Start processing the first batch immediately
      handler.postDelayed(batchRunnable, getOverlayDelay(totalCount));
    });
  }

  public void refresh() {
    if (isActionMode()) {
      // Inconsistency detected, refresh is launched to correct the selection
      doRefreshOrCorrection();
    }
    // No inconsistencies, we avoid repeating heavy treatment unnecessarily.
  }


  public boolean isActionMode() {
    return (mActionMode != null);
  }

  public void doRefreshOrCorrection() {

    // Get the total number of items in the adapter
    final int totalCount = mAdapter.getCount();
    ApplicationCtx.addLog(mActivity, TAG, "Refresh nb items: " + totalCount);
    if (mPreviousCount == totalCount)
      return;
    mPreviousCount = totalCount;

    final Set<Integer> items = new HashSet<>(mAdapter.getSelectedItemsIds());

    // Prevent triggering onItemCheckedStateChanged during the batch process
    mFromAll = true;

    // Show the progress dialog
    UIHelper.showCircularProgressDialog(mProgress);
    // Show the refresh animation in the "Select All" menu item if available
    mMenuItemSelectAll.setActionView(UIHelper.startRefreshAnimation(mActivity));

    // Clear all current selections
    mAdapter.setLockRefresh(true);
    mAdapter.removeSelection();

    // Dynamically adjust batch size based on totalCount to improve performance
    final int dynamicBatchSize = evaluateBatch(totalCount);
    ApplicationCtx.addLog(mActivity, TAG, "Dynamic batch size: " + dynamicBatchSize);

    // Handler to run tasks on the main thread
    Handler handler = new Handler(Looper.getMainLooper());
    final int[] index = {0}; // Track current batch processing index

    Runnable batchRunnable = new Runnable() {
      @SuppressLint("NotifyDataSetChanged")
      @Override
      public void run() {
        // Calculate the end index for the current batch
        int end = Math.min(index[0] + dynamicBatchSize, totalCount);

        // Apply selection state to each item in the batch
        applySelectionState(items, index[0], end);

        // Update the index for the next batch
        index[0] = end;

        // Continue with the next batch if there are remaining items
        if (index[0] < totalCount) {
          handler.postDelayed(this, 1); // Small delay to keep UI smooth
        } else {
          // All batches are complete, refresh the adapter once
          mAdapter.setLockRefresh(false);
          mAdapter.notifyDataSetChanged();
          // Update the ActionMode title with the final count
          int checkedCount = mAdapter.getSelectedCount();
          mActionMode.setTitle(String.format(mActivity.getString(R.string.items_selected), checkedCount));
          ApplicationCtx.addLog(mActivity, TAG, "Checked count: " + checkedCount);

          // Update the "Select All" menu item state
          boolean allSelected = (checkedCount == totalCount) && (totalCount > 0);
          mMenuItemSelectAll.setChecked(allSelected);
          mMenuItemSelectAll.setCheckable(true);

          // Stop animation
          View view = mMenuItemSelectAll.getActionView();
          if (view != null) {
            view.clearAnimation();
            mMenuItemSelectAll.setActionView(null);
          }
          // Hide the progress dialog
          if (mProgress.isShowing()) {
            mProgress.dismiss();
          }
          // Unlock the mFromAll flag
          mFromAll = false;
        }
      }
    };

    // Start the batch process
    handler.postDelayed(batchRunnable, getOverlayDelay(totalCount));
  }

  private void applySelectionState(Set<Integer> items, int index, int end) {
    for (int i = index; i < end; i++) {
      final Integer pos = i;
      boolean selected = mMenuItemSelectAll != null && mMenuItemSelectAll.isChecked();
      if (items.contains(pos))
        selected = true;
      mAdapter.toggleSelection(i, selected, false);
    }
  }

  protected int evaluateBatch(int totalCount) {
    final int batchSize;
    if (totalCount <= 1000) {
      batchSize = totalCount; // small list → single batch
    } else if (totalCount <= 5000) {
      batchSize = 2000; // medium list → bigger batch
    } else {
      batchSize = Math.max(2000, totalCount / 5); // large dataset → ~5 batches
    }
    return batchSize;
  }

  /**
   * Returns the delay before showing the overlay in milliseconds
   * based on the total number of items.
   */
  protected long getOverlayDelay(int totalCount) {
    if (totalCount <= 5000) {
      // Small lists → long delay to avoid flickering
      return 300;
    } else if (totalCount <= 50000) {
      // Medium lists → medium delay
      return 100;
    } else {
      // Large lists → show immediately
      return 0;
    }
  }

  /**
   * Test whether we are in plain text implementation or not
   *
   * @return boolean
   */
  protected abstract boolean isFromPlainText();

  /**
   * Copy action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  private boolean actionCopy(ActionMode mode) {
    boolean plain = isFromPlainText();
    List<Integer> selected = new ArrayList<>(mAdapter.getSelectedIds());
    if (selected.isEmpty()) {
      displayError(R.string.error_no_line_selected);
      return false;
    }
    StringBuilder sb = new StringBuilder();
    for (Integer i : selected) {
      LineEntry ld = mAdapter.getItem(i);
      if (ld != null) {
        if (plain)
          sb.append(SysHelper.ignoreNonDisplayedChar(ld.getPlain()));
        else
          sb.append(ld.getPlain()).append("\n");
      }
    }
    return copyAndClose(plain ? "CopyPlain" : "CopyHex", mode, sb);
  }

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
      new Handler(Looper.getMainLooper()).postDelayed(mode::finish, 100);
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
   * Sets an action view with a progress animation while performing
   * an asynchronous action. Once the action completes, updates the UI accordingly.
   *
   * @param item   The menu item on which to set the action view.
   * @param mode   The current ActionMode.
   * @param action The asynchronous action to run, which accepts a completion callback.
   */
  @SuppressLint("NotifyDataSetChanged")
  protected void setActionView(final MenuItem item, final ActionMode mode, final AsyncAction action) {
    // Show the circular progress dialog
    UIHelper.showCircularProgressDialog(mProgress);
    // Start the refresh animation on the menu item
    item.setActionView(UIHelper.startRefreshAnimation(mActivity));

    // Run the async action and pass a Runnable to execute when done
    action.run(() -> {
      // Notify adapter that data set has changed to refresh UI
      mAdapter.notifyDataSetChanged();

      // Update the action mode title with the current selection count
      updateTitle(mode);

      // Reset flag indicating batch operation has finished
      mFromAll = false;

      // Make menu item checkable again
      item.setCheckable(true);

      // Set menu item checked if all items are selected
      item.setChecked(mAdapter.getSelectedCount() == mAdapter.getCount());

      // Get the current action view (animation view) of the menu item
      View view = item.getActionView();

      // If there is an animation view, stop and clear it
      if (view != null) {
        view.clearAnimation();
        item.setActionView(null);
      }

      // Dismiss the progress dialog if still showing
      if (mProgress.isShowing()) {
        mProgress.dismiss();
      }
    });
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

  /* CLICKS */

  /**
   * Enables or disables multi-select mode.
   * This is called by the ActionMode.Callback implementation.
   */
  public void setMultiSelectMode(boolean enabled) {
    mMultiSelectMode = enabled;
    mAdapter.notifyDataSetChanged();
  }
}
