package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the plain text list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PlainTextListArrayAdapter extends SearchableListArrayAdapter {
  private static final int ID = R.layout.listview_simple_row;

  public PlainTextListArrayAdapter(final Context context,
                                   final List<LineEntry> objects,
                                   UserConfig userConfigPortrait,
                                   UserConfig userConfigLandscape) {
    super(context, ID, objects, userConfigPortrait, userConfigLandscape);
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
   * Test if we are from the hex view or the plain view.
   *
   * @return boolean
   */
  @Override
  public boolean isSearchFromHewView() {
    return false;
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


  /**
   * Inflate the view.
   *
   * @param convertView This value may be null.
   * @return The view.
   */
  protected @NonNull
  View inflateView(final View convertView) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        final TextView label1 = v.findViewById(R.id.label1);
        v.setTag(label1);
      }
    }
    return v == null ? new View(getContext()) : v;
  }

  /**
   * Fills the view.
   *
   * @param v        This can't be null.
   * @param position The position of the item within the adapter's data set of the item whose view we want.
   */
  @Override
  protected void fillView(final @NonNull View v, final int position) {
    if (v.getTag() != null) {
      final TextView holder = (TextView) v.getTag();
      LineEntry le = getItem(position);
      if(le != null) {
        holder.setText(ignoreNonDisplayedChar(le.getPlain()));
        holder.setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
        applyUserConfig(holder);
      }
    }
  }
}

