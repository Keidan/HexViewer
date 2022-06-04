package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the hex text list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class HexTextArrayAdapter extends SearchableListArrayAdapter {
  private static final int ID = R.layout.listview_hex_row;
  private Set<Integer> mSelectedItemsIds;
  private final ApplicationCtx mApp;
  private final LineNumbersTitle mTitle;
  private long mStartOffset;

  public static class LineNumbersTitle {
    public TextView titleLineNumbers;
    public TextView titleContent;
  }

  public HexTextArrayAdapter(final Context activity, final List<LineEntry> objects,
                             LineNumbersTitle title,
                             UserConfig userConfigPortrait,
                             UserConfig userConfigLandscape) {
    super(activity, ID, objects, userConfigPortrait, userConfigLandscape);
    mStartOffset = 0;
    mTitle = title;
    mSelectedItemsIds = new HashSet<>();
    mApp = (ApplicationCtx)activity.getApplicationContext();
  }

  public void setStartOffset(final long startOffset) {
    mStartOffset = startOffset;
  }

  public long getStartOffset() {
    return mStartOffset;
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
   * Test if we are from the hex view or the plain view.
   *
   * @return boolean
   */
  public boolean isSearchFromHewView() {
    return true;
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
        Holder holder = new Holder();
        holder.content = v.findViewById(R.id.content);
        holder.lineNumbers = v.findViewById(R.id.lineNumbers);
        v.setTag(holder);
      }
    }
    return v == null ? new View(getContext()) : v;
  }


  /**
   * Applies the necessary changes if the "updated" field is true.
   *
   * @param tv TextView
   * @param fd FilterData
   */
  private void applyUpdated(final TextView tv, final LineEntry fd) {
    String str = getTextAccordingToUserConfig(fd.getPlain());
    if (fd.isUpdated()) {
      SpannableString spanString = new SpannableString(str);
      spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
      tv.setText(spanString);
    } else {
      tv.setText(str);
    }
  }

  /**
   * Gets the text according to the user's configuration.
   *
   * @param text Text.
   * @return The new text.
   */
  protected String getTextAccordingToUserConfig(final String text) {
    String txt;
    Configuration cfg = getContext().getResources().getConfiguration();
    if (mUserConfigLandscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE && mUserConfigLandscape.isDataColumnNotDisplayed())
      txt = text.substring(0, mApp.getNbBytesPerLine() == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BYTES_ROW_16 : SysHelper.MAX_BYTES_ROW_8);
    else if (mUserConfigPortrait != null && cfg.orientation == Configuration.ORIENTATION_PORTRAIT && mUserConfigPortrait.isDataColumnNotDisplayed())
      txt = text.substring(0, mApp.getNbBytesPerLine() == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BYTES_ROW_16 : SysHelper.MAX_BYTES_ROW_8);
    else
      txt = text;
    return txt;
  }

  public long getCurrentLine(int position) {
    return UIHelper.getCurrentLine(position, mStartOffset, mApp.getNbBytesPerLine());
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
      final Holder holder = (Holder) v.getTag();
      LineEntry fd = getItem(position);

      if (mApp.isLineNumber()) {
        final int maxLength = String.format("%X", getCurrentLine(getEntries().getItemsCount())).length();
        final String s = String.format("%0" + maxLength + "X", getCurrentLine(fd.getIndex()));
        final @ColorInt int colorTitle = ContextCompat.getColor(getContext(), R.color.colorLineNumbers);
        final @ColorInt int colorLine = ContextCompat.getColor(getContext(),
            isSelected(position) ? R.color.colorAccentDisabled : R.color.colorLineNumbers);
        holder.lineNumbers.setText(s);
        holder.lineNumbers.setTextColor(colorLine);
        applyUserConfig(holder.lineNumbers);
        holder.lineNumbers.setVisibility(View.VISIBLE);

        if (position == 0) {
          mTitle.titleLineNumbers.setText(String.format("%" + maxLength + "s", " "));
          mTitle.titleContent.setText(getContext().getString(mApp.getNbBytesPerLine() == SysHelper.MAX_BY_ROW_16 ?
              R.string.title_content : R.string.title_content8));
          mTitle.titleContent.setTextColor(colorTitle);
        }
        applyUserConfig(mTitle.titleContent);
        applyUserConfig(mTitle.titleLineNumbers);

      } else {
        holder.lineNumbers.setVisibility(View.GONE);
      }
      applyUpdated(holder.content, fd);
      holder.content.setTextColor(ContextCompat.getColor(getContext(),
          fd.isUpdated() ? R.color.colorTextUpdated : isSelected(position) ? R.color.colorPrimaryDark : R.color.textColor));
      applyUserConfig(holder.content);
      v.setBackgroundColor(ContextCompat.getColor(getContext(), isSelected(position) ? R.color.colorAccent : R.color.windowBackground));
    }
  }

  public static class Holder {
    public TextView lineNumbers;
    public TextView content;
  }
}

