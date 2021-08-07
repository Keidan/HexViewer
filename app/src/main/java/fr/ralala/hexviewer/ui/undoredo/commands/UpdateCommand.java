package fr.ralala.hexviewer.ui.undoredo.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.undoredo.ICommand;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Command used when updating an entry.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class UpdateCommand implements ICommand {
  private final List<LineData<Line>> mList;
  private final MainActivity mActivity;
  private final int mFirstPosition;
  private final int mRefNbLines;
  private final List<LineFilter<Line>> mPrevLines;
  private final UnDoRedo mUnDoRedo;


  public UpdateCommand(final UnDoRedo undoRedo, final MainActivity activity, final int firstPosition, final int refNbLines, List<LineData<Line>> entries) {
    mUnDoRedo = undoRedo;
    mList = entries;
    mActivity = activity;
    mFirstPosition = activity.getPayloadHex().getAdapter().getFilteredList().get(firstPosition).getOrigin();
    mRefNbLines = refNbLines;
    mPrevLines = new ArrayList<>();
  }

  /**
   * Execute the command.
   */
  public void execute() {
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    List<LineFilter<Line>> filteredList = adapter.getFilteredList();
    List<LineData<Line>> items = adapter.getItems();
    mPrevLines.clear();
    int size = mList.size();
    /* only existing elements  */
    if (size == mRefNbLines) {
      Log.i(getClass().getName(), "execute -> only existing elements");
      updateExistingElements(filteredList, items);
    } else {
      Log.i(getClass().getName(), "execute -> multiple elements");
      int diff = Math.abs(size - mRefNbLines);

      /* First we modify the existing elements */
      updateExistingElements(filteredList, items);

      /* Then we add the elements */
      LineFilter<Line> prevLine = mPrevLines.get(mPrevLines.size() - 1);
      for (int i = mRefNbLines, j = 1; i < size; i++, j++) {
        LineData<Line> value = mList.get(i);
        value.setUpdated(mUnDoRedo.isChanged());
        LineFilter<Line> lf = new LineFilter<>(value, prevLine.getOrigin() + j);
        if (prevLine.getOrigin() + j < adapter.getItems().size()) {
          items.add(prevLine.getOrigin() + j, mList.get(i));
        } else {
          items.add(mList.get(i));
        }
        if (mFirstPosition + i < filteredList.size()) {
          filteredList.add(mFirstPosition + i, lf);
        } else {
          filteredList.add(lf);
        }
      }

      /* Finally we move the existing indexes - filtered */
      for (int i = (mFirstPosition + size); i < filteredList.size(); i++) {
        LineFilter<Line> lf = filteredList.get(i);
        lf.setOrigin(lf.getOrigin() + diff);
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
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    List<LineFilter<Line>> filteredList = adapter.getFilteredList();
    List<LineData<Line>> items = adapter.getItems();

    int size = mList.size();

    /* only existing elements  */
    if (size == mRefNbLines) {
      Log.i(getClass().getName(), "unExecute -> only existing elements");
    } else {
      Log.i(getClass().getName(), "unExecute -> multiple elements");
      int diff = Math.abs(size - mRefNbLines);
      LineFilter<Line> prevLine = mPrevLines.get(mPrevLines.size() - 1);

      /* First, we delete the elements */
      for (int i = (prevLine.getOrigin() + diff); i > prevLine.getOrigin(); i--) {
        items.remove(i);
        if (i < filteredList.size())
          filteredList.remove(i);
      }

      /* Then we move the existing indexes - filtered */
      for (int i = prevLine.getOrigin() + 1; i < filteredList.size(); i++) {
        LineFilter<Line> lf = filteredList.get(i);
        lf.setOrigin(lf.getOrigin() - diff);
      }
    }
    /* Finally we restores the existing elements */
    restoreExistingElements(filteredList, items);

    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.notifyDataSetChanged();
  }

  /**
   * Restores the existing elements.
   * @param filteredList List<LineFilter<Line>>
   * @param items List<LineData<Line>>
   */
  private void restoreExistingElements(final List<LineFilter<Line>> filteredList, final List<LineData<Line>> items) {
    for(int i = 0; i < mRefNbLines; i++) {
      final LineFilter<Line> lf = filteredList.get(mFirstPosition + i);
      final LineData<Line> oldVal = new LineData<>(mPrevLines.get(i).getData());
      if (!lf.getData().toString().equals(oldVal.toString())) {
        oldVal.setUpdated(false);
        lf.setData(oldVal);
        LineData<Line> ld = items.get(lf.getOrigin());
        ld.setValue(oldVal.getValue());
        ld.setUpdated(oldVal.isUpdated());
      }
    }
  }

  /**
   * Restores the existing elements.
   * @param filteredList List<LineFilter<Line>>
   * @param items List<LineData<Line>>
   */
  private void updateExistingElements(final List<LineFilter<Line>> filteredList, final List<LineData<Line>> items) {
    for(int i = 0; i < mRefNbLines; i++) {
      LineFilter<Line> lf = filteredList.get(mFirstPosition + i);
      mPrevLines.add(new LineFilter<>(lf));
      final LineData<Line> newVal = new LineData<>(mList.get(i));
      if (!lf.getData().toString().equals(newVal.toString())) {
        newVal.setUpdated(mUnDoRedo.isChanged());
        lf.setData(newVal);
        LineData<Line> ld = items.get(lf.getOrigin());
        ld.setValue(lf.getData().getValue());
        ld.setUpdated(newVal.isUpdated());
      }
    }
  }
}
