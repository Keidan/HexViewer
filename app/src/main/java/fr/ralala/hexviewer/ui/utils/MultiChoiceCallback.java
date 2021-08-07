package fr.ralala.hexviewer.ui.utils;

import android.annotation.SuppressLint;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class MultiChoiceCallback implements AbsListView.MultiChoiceModeListener {
  private final ListView mListView;
  private final HexTextArrayAdapter mAdapter;
  private final MainActivity mActivity;
  private final ImageView mRefreshActionViewSelectAll;
  private final ImageView mRefreshActionViewDelete;

  @SuppressLint("InflateParams")
  public MultiChoiceCallback(MainActivity mainActivity, final ListView listView, final HexTextArrayAdapter adapter) {
    mActivity = mainActivity;
    mListView = listView;
    mAdapter = adapter;
    /* Attach a rotating ImageView to the refresh item as an ActionView */
    LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mRefreshActionViewSelectAll = (ImageView) inflater.inflate(R.layout.refresh_action_view_select_all, null);
    mRefreshActionViewDelete = (ImageView) inflater.inflate(R.layout.refresh_action_view_delete, null);

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
    mode.getMenuInflater().inflate(R.menu.main_multi_choice, menu);
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
      actionClear(item, mode);
      return true;
    } else if (item.getItemId() == R.id.action_select_all) {
      actionSelectAll(item);
      return true;
    } else if (item.getItemId() == R.id.action_edit) {
      return actionEdit(mode);
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
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  private void actionClear(MenuItem item, ActionMode mode) {
    setActionView(item, mRefreshActionViewDelete, () -> {
      final List<Integer> selected = mAdapter.getSelectedIds();
      Map<Integer, LineFilter<Line>> map = new HashMap<>();
      List<LineFilter<Line>> filteredList = mAdapter.getFilteredList();
      // Captures all selected ids with a loop
      for (int i = selected.size() - 1; i >= 0; i--) {
        int position = selected.get(i);
        LineFilter<Line> lf = filteredList.get(position);
        map.put(lf.getOrigin(), lf);
      }
      mActivity.getUnDoRedo().insertInUnDoRedoForDelete(mActivity, map).execute();
      mActivity.setTitle(mActivity.getResources().getConfiguration());
      // Close CAB
      mode.finish();
    });
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
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  private boolean actionEdit(ActionMode mode) {
    if (!mActivity.getSearchQuery().trim().isEmpty()) {
      displayError(R.string.error_edition_search_in_progress);
      return false;
    }
    List<Integer> selected = new ArrayList<>(mAdapter.getSelectedIds());
    if (selected.isEmpty()) {
      displayError(R.string.error_no_line_selected);
      return false;
    }
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    List<LineData<Line>> items = mAdapter.getItems();
    int previous = selected.get(0);
    for (Integer i : selected) {
      if (previous != i && previous + 1 != i) {
        displayError(R.string.error_edition_continuous_selection);
        return false;
      }
      previous = i;
      LineData<Line> ld = items.get(i);
      for (Byte b : ld.getValue().getRaw())
        byteArrayOutputStream.write(b);
    }
    mActivity.getLauncherLineUpdate().startActivity(byteArrayOutputStream.toByteArray(),
        selected.get(0), selected.size());
    // Close CAB
    new Handler().postDelayed(mode::finish, 500);
    return true;
  }

  /**
   * Displays an error message.
   *
   * @param message The message.
   */
  private void displayError(@StringRes int message) {
    new AlertDialog.Builder(mActivity)
        .setCancelable(false)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.error_title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> dialog.dismiss()).show();
  }

  /**
   * Sets the action view.
   *
   * @param item              MenuItem
   * @param refreshActionView ImageView
   * @param action            Runnable
   */
  private void setActionView(final MenuItem item, final ImageView refreshActionView, final Runnable action) {
    if (item != null) {
      refreshActionView.clearAnimation();
      Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.clockwise_refresh);
      rotation.setRepeatCount(Animation.INFINITE);
      item.setCheckable(false);// Do not accept any click events while scanning
      refreshActionView.startAnimation(rotation);
      item.setActionView(refreshActionView);
    }
    final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(() -> {
      action.run();
      if (item != null) {
        item.setCheckable(true);
        View view = item.getActionView();
        if (view != null) {
          view.clearAnimation();
          item.setActionView(null);
        }
      }
    }, 1000);
  }
}
