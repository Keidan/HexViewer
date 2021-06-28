package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.ralala.hexviewer.models.FilterData;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the plain text list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PlainTextListArrayAdapter extends SearchableListArrayAdapter<String> {

  public PlainTextListArrayAdapter(final Context context, final List<String> objects, UserConfig userConfig) {
    super(context, objects, userConfig);
  }

  /**
   * Sets the entry text (if updated = false)
   *
   * @param view The text view.
   * @param text The text.
   * @param updated The updated flag.
   */
  @Override
  protected void setEntryText(final TextView view, final String text, final boolean updated) {
    if (updated) {
      SpannableString spanString = new SpannableString(text);
      spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
      view.setText(spanString);
    } else {
      view.setText(ignoreNonDisplayedChar(text));
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
    return false;
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
  protected void extraFilter(final String line, int index, String query, final ArrayList<FilterData<String>> tempList, Locale loc) {
    StringBuilder sb = new StringBuilder();
    for (char c : line.toCharArray())
      sb.append(String.format("%02X", (byte) c));
    if (sb.toString().toLowerCase(loc).contains(query)) {
      tempList.add(new FilterData<>(line, index));
    }
  }

  /**
   * Ignore non displayed char
   *
   * @param ref Ref string.
   * @return Patched string.
   */
  private String ignoreNonDisplayedChar(final String ref) {
    StringBuilder sb = new StringBuilder();
    for (char c : ref.toCharArray())
      sb.append((c == 0x09 || c == 0x0A || (c >= 0x20 && c < 0x7F)) ? c : '.');
    return sb.toString();
  }
}

