package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;

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
public class HexTextArrayAdapter extends SearchableListArrayAdapter<Line> {
  private Set<Integer> mSelectedItemsIds;

  public HexTextArrayAdapter(final Context context, final List<LineData<Line>> objects, UserConfig userConfig) {
    super(context, objects, userConfig);
    mSelectedItemsIds = new HashSet<>();
  }

  /**
   * Sets the entry text (if updated = false)
   *
   * @param view    The text view.
   * @param text    The text.
   * @param updated The updated flag.
   */
  @Override
  protected void setEntryText(final TextView view, final Line text, final boolean updated) {
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
  protected void extraFilter(final LineData<Line> line, int index, String query, final ArrayList<LineFilter<Line>> tempList, Locale loc) {
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
    mSelectedItemsIds = new HashSet<>();
    notifyDataSetChanged();
  }


  /**
   * Returns the selected ids.
   *
   * @return SparseBooleanArray
   */
  public List<Integer> getSelectedIds() {
    List<Integer> li = new ArrayList<>(mSelectedItemsIds);
    Collections.sort(li);
    return li;
  }
}

