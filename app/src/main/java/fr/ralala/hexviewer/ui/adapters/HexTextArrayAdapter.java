package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fr.ralala.hexviewer.models.FilterData;
import fr.ralala.hexviewer.models.LineEntry;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the hex text list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class HexTextArrayAdapter extends SearchableListArrayAdapter<LineEntry> {
  private List<Integer> mSelectedItemsIds;

  public HexTextArrayAdapter(final Context context, final List<LineEntry> objects, UserConfig userConfig) {
    super(context, objects, userConfig);
    mSelectedItemsIds = new ArrayList<>();
  }

  /**
   * Sets the entry text (if updated = false)
   *
   * @param view The text view.
   * @param text The text.
   * @param updated The updated flag.
   */
  @Override
  protected void setEntryText(final TextView view, final LineEntry text, final boolean updated) {
    if (updated) {
      SpannableString spanString = new SpannableString(text.getPlain());
      spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
      view.setText(spanString);
    } else {
      view.setText(text.getPlain());
    }
  }

  /**
   * Returns true if the item is selected.
   *
   * @param position The position
   * @return boolean
   */
  @Override
  protected boolean isSelected(int position) {
    return mSelectedItemsIds.contains(position);
  }

  /**
   * Performs a hexadecimal search in a plain text string.
   *
   * @param line     The current line.
   * @param index    The line index.
   * @param query    The query.
   * @param tempList The output list.
   * @param loc      The locale.
   */
  @Override
  protected void extraFilter(final LineEntry line, int index, String query, final ArrayList<FilterData<LineEntry>> tempList, Locale loc) {
    /* nothing */
  }

  /**
   * Toggles the item selection.
   *
   * @param position Item position.
   */
  public void toggleSelection(int position, boolean checked) {
    if (checked) {
      mSelectedItemsIds.add(position);
    } else {
      mSelectedItemsIds.remove(position);
    }
    notifyDataSetChanged();
  }

  /**
   * Removes the item selection.
   */
  public void removeSelection() {
    mSelectedItemsIds = new ArrayList<>();
    notifyDataSetChanged();
  }


  /**
   * Returns the selected ids.
   *
   * @return SparseBooleanArray
   */
  public List<Integer> getSelectedIds() {
    Collections.sort(mSelectedItemsIds);
    return mSelectedItemsIds;
  }
}

