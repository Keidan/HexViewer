package fr.ralala.hexviewer.ui.utils;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MultiChoiceCallback implements AbsListView.MultiChoiceModeListener {
  private final ApplicationCtx mApp;
  private final ListView mListView;
  private final SearchableListArrayAdapter mAdapter;
  private final View mSnackBarLayout;
  private final MainActivity mMainActivity;
  private SparseBooleanArray mBackup;
  private Snackbar mCustomSnackBar;

  public MultiChoiceCallback(MainActivity mainActivity, final ListView listView, final SearchableListArrayAdapter adapter, final View snackBarLayout) {
    mApp = ApplicationCtx.getInstance();
    mMainActivity = mainActivity;
    mListView = listView;
    mAdapter = adapter;
    mSnackBarLayout = snackBarLayout;
  }

  /**
   * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
   *
   * @param mode ActionMode being created.
   * @param menu Menu used to populate action buttons.
   * @return true if the action mode should be created, false if entering this mode should be aborted.
   */
  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.main_clear, menu);
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
    dismiss();
    return false;
  }

  /**
   * Forces a dismiss of the snackbar.
   */
  public void dismiss() {
    if(mCustomSnackBar != null && mCustomSnackBar.isShown()) {
      mCustomSnackBar.dismiss();
    }
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
      final SparseBooleanArray selected = mAdapter.getSelectedIds();
      mBackup = selected.clone();
      // Captures all selected ids with a loop
      for (int i = (selected.size() - 1); i >= 0; i--) {
        if (selected.valueAt(i)) {
          final int position = selected.keyAt(i);
          // Remove selected items following the ids
          mAdapter.removeItem(position);
        }
      }
      mApp.getHexChanged().set(true); /* Prevents any action while the snack is open */
      showUndoSnackbar();
      // Close CAB
      mode.finish();
      return true;
    } else if (item.getItemId() == R.id.action_select_all) {
      final int count = mAdapter.getCount();
      for(int i = 0; i < count; i++) {
        if(!mAdapter.isPositionChecked(i)) {
          onItemCheckedStateChanged(mode, i, -1, true);
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Shows the undo snack bar.
   */
  private void showUndoSnackbar() {
    final Context c = mSnackBarLayout.getContext();
    final int checkedCount = mAdapter.getSelectedCount();
    mCustomSnackBar = Snackbar.make(mSnackBarLayout, String.format(c.getString(R.string.items_deleted), checkedCount), Snackbar.LENGTH_LONG);
    mCustomSnackBar.setAction(c.getString(R.string.cancel), (v) -> mAdapter.undoDelete());
    mCustomSnackBar.addCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
          mAdapter.clearRecentlyDeleted();
          // Captures all selected ids with a loop
          for (int i = (mBackup.size() - 1); i >= 0; i--) {
            if (mBackup.valueAt(i)) {
              // Remove selected items following the ids
              final int position = mBackup.keyAt(i);
              final byte[] buf = SysHelper.hexStringToByteArray("");
              mApp.getHexChanged().set(true);
              mApp.getPayload().update(position, buf);
            }
          }
        }
        else
          mApp.getHexChanged().set(false);
        mMainActivity.setTitle(mMainActivity.getResources().getConfiguration());
        mCustomSnackBar = null;
      }

      @Override
      public void onShown(Snackbar snackbar) {
        // nothing to do
      }
    });
    mCustomSnackBar.show();
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
    mode.setTitle(String.format(mMainActivity.getString(R.string.items_selected), checkedCount));
    mAdapter.toggleSelection(position);
  }
}
