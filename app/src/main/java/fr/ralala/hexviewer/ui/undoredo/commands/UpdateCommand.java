package fr.ralala.hexviewer.ui.undoredo.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.models.LineEntry;
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
  private final List<LineEntry> mList;
  private final MainActivity mActivity;
  private final int mFirstPosition;
  private final int mRefNbLines;
  private final List<LineEntry> mPrevLines;
  private final UnDoRedo mUnDoRedo;


  public UpdateCommand(final UnDoRedo undoRedo, final MainActivity activity, final int firstPosition, final int refNbLines, List<LineEntry> entries) {
    mUnDoRedo = undoRedo;
    mList = entries;
    mActivity = activity;
    mFirstPosition = activity.getPayloadHex().getAdapter().getEntries().getItemIndex(firstPosition);
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

    mPrevLines.clear();
    int size = mList.size();
    /* only existing elements  */
    if (size == mRefNbLines) {
      Log.i(getClass().getName(), "execute -> only existing elements");
      updateExistingElements(adapter);
    } else {
      Log.i(getClass().getName(), "execute -> multiple elements");
      int diff = Math.abs(size - mRefNbLines);

      /* First we modify the existing elements */
      updateExistingElements(adapter);

      /* Then we add the elements */
      LineEntry prevLine = mPrevLines.get(mPrevLines.size() - 1);
      for (int i = mRefNbLines, j = 1; i < size; i++, j++) {
        LineEntry value = mList.get(i);
        value.setIndex(prevLine.getIndex() + j);
        value.setUpdated(mUnDoRedo.isChanged());
        if (value.getIndex() < adapter.getEntries().getItems().size()) {
          adapter.getEntries().addItem(value.getIndex(), mList.get(i));
        } else {
          adapter.getEntries().addItem(mList.get(i));
        }
      }

      /* Finally we move the existing indexes */
      if ((mFirstPosition + size) < adapter.getEntries().getItems().size())
        adapter.getEntries().moveIndexes((mFirstPosition + size), diff, true);
    }
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    int size = mList.size();

    /* only existing elements  */
    if (size == mRefNbLines) {
      Log.i(getClass().getName(), "unExecute -> only existing elements");
    } else {
      Log.i(getClass().getName(), "unExecute -> multiple elements");
      int diff = Math.abs(size - mRefNbLines);
      LineEntry prevLine = mPrevLines.get(mPrevLines.size() - 1);

      /* First, we delete the elements */
      for (int i = (prevLine.getIndex() + diff); i > prevLine.getIndex(); i--) {
        adapter.getEntries().removeItem(i);
      }

      /* Then we move the existing indexes - filtered */
      adapter.getEntries().moveIndexes(prevLine.getIndex() + 1, diff, false);
    }
    /* Finally we restores the existing elements */
    restoreExistingElements(adapter);

    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }

  /**
   * Restores the existing elements.
   *
   * @param adapter HexTextArrayAdapter
   */
  private void restoreExistingElements(HexTextArrayAdapter adapter) {
    for (int i = 0; i < mRefNbLines; i++) {
      final LineEntry le = adapter.getItem(mFirstPosition + i);
      final LineEntry oldVal = new LineEntry(mPrevLines.get(i));
      if (!le.toString().equals(oldVal.toString())) {
        oldVal.setUpdated(false);
        le.setValues(oldVal.getPlain(), oldVal.getRaw());
        LineEntry ld = adapter.getItem(le.getIndex());
        ld.setValues(oldVal.getPlain(), oldVal.getRaw());
        ld.setUpdated(oldVal.isUpdated());
      }
    }
  }

  /**
   * Restores the existing elements.
   *
   * @param adapter HexTextArrayAdapter
   */
  private void updateExistingElements(HexTextArrayAdapter adapter) {
    for (int i = 0; i < mRefNbLines; i++) {
      final LineEntry le = adapter.getItem(mFirstPosition + i);
      mPrevLines.add(new LineEntry(le));
      final LineEntry newVal = new LineEntry(mList.get(i));
      if (!le.toString().equals(newVal.toString())) {
        newVal.setUpdated(mUnDoRedo.isChanged());
        le.setValues(newVal.getPlain(), newVal.getRaw());
        LineEntry ld = adapter.getItem(le.getIndex());
        ld.setValues(le.getPlain(), le.getRaw());
        ld.setUpdated(newVal.isUpdated());
      }
    }
  }
}
