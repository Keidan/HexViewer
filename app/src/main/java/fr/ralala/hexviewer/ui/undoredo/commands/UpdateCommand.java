package fr.ralala.hexviewer.ui.undoredo.commands;

import java.util.List;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.undoredo.ICommand;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Command used when updating an entry.
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class UpdateCommand implements ICommand {
  private final List<LineData<Line>> mList;
  private final MainActivity mActivity;
  private final int mRealIndex;
  private LineFilter<Line> mPrevLine;


  public UpdateCommand(final MainActivity activity, final int firstPosition, List<LineData<Line>> entries) {
    mList = entries;
    mActivity = activity;
    mRealIndex = activity.getAdapterHex().getFilteredList().get(firstPosition).getOrigin();
  }

  /**
   * Execute the command.
   */
  public void execute() {
    HexTextArrayAdapter adapter = mActivity.getAdapterHex();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    int size = mList.size();
    /* only one element */
    if (size == 1) {
      LineFilter<Line> fd = adapter.getFilteredList().get(mRealIndex);
      mPrevLine = new LineFilter<>(fd);
      fd.setData(mList.get(0));
      fd.getData().setUpdated(true);
      adapter.getItems().set(fd.getOrigin(), mList.get(0));
    } else {
      /* first we move the existing indexes - filtered */
      for (int i = mRealIndex + 1; i < adapter.getFilteredList().size(); i++)
        adapter.getFilteredList().get(i).setOrigin(adapter.getFilteredList().get(i).getOrigin() + size);

      /* Then we modify the existing element */
      LineFilter<Line> fd = adapter.getFilteredList().get(mRealIndex);
      mPrevLine = new LineFilter<>(fd);
      final LineData<Line> newVal = mList.get(0);
      if (!fd.getData().equals(newVal)) {
        fd.setData(newVal);
        fd.getData().setUpdated(true);
        adapter.getItems().set(fd.getOrigin(), mList.get(0));
      }

      /* finally we add the elements */
      for (int i = 1; i < size; i++) {
        LineData<Line> value = mList.get(i);
        fd = new LineFilter<>(value, mPrevLine.getOrigin() + i);
        fd.getData().setUpdated(true);
        if (mPrevLine.getOrigin() + i < adapter.getItems().size())
          adapter.getItems().add(mPrevLine.getOrigin() + i, mList.get(i));
        else
          adapter.getItems().add(mList.get(i));
        if (mRealIndex + i < adapter.getFilteredList().size())
          adapter.getFilteredList().add(mRealIndex + i, fd);
        else
          adapter.getFilteredList().add(fd);
      }
    }
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.notifyDataSetChanged();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    HexTextArrayAdapter adapter = mActivity.getAdapterHex();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    int size = mList.size();
    /* only one element */
    if (size == 1) {
      LineFilter<Line> fd = adapter.getFilteredList().get(mRealIndex);
      fd.setData(mPrevLine.getData());
      fd.getData().setUpdated(false);
      adapter.getItems().set(fd.getOrigin(), mPrevLine.getData());
    } else {
      /* First, we delete the elements*/
      for (int i = size - 1; i > 0; i--) {
        adapter.getItems().remove(mPrevLine.getOrigin() + i);
        if (mRealIndex + i < adapter.getFilteredList().size())
          adapter.getFilteredList().remove(mRealIndex + i);
      }
      /* Then we restores the existing element */
      LineFilter<Line> fd = adapter.getFilteredList().get(mRealIndex);
      fd.setData(mPrevLine.getData());
      fd.getData().setUpdated(false);
      adapter.getItems().set(fd.getOrigin(), mPrevLine.getData());

      /* finally we move the existing indexes - filtered */
      for (int i = mRealIndex + 1; i < adapter.getFilteredList().size(); i++)
        adapter.getFilteredList().get(i).setOrigin(adapter.getFilteredList().get(i).getOrigin() - size);
    }
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.notifyDataSetChanged();
  }
}
