package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.ui.adapters.holders.HolderHex;
import fr.ralala.hexviewer.ui.adapters.holders.LineNumbersTitle;
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
  private final ApplicationCtx mApp;
  private final LineNumbersTitle mTitle;
  private long mStartOffset;

  public HexTextArrayAdapter(final Context activity, final List<LineEntry> objects,
                             LineNumbersTitle title,
                             UserConfig userConfigPortrait,
                             UserConfig userConfigLandscape) {
    super(activity, ID, objects, userConfigPortrait, userConfigLandscape);
    mStartOffset = 0;
    mTitle = title;
    mApp = (ApplicationCtx) activity.getApplicationContext();
  }

  public void setStartOffset(final long startOffset) {
    mStartOffset = startOffset;
  }

  /**
   * Test if we are from the hex view or the plain view.
   *
   * @return boolean
   */
  public boolean isSearchNotFromHexView() {
    return false;
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
        HolderHex holder = new HolderHex();
        holder.setContent(v.findViewById(R.id.content));
        holder.setLineNumbers(v.findViewById(R.id.lineNumbers));
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
    Configuration cfg = mApp.getConfiguration();
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
      final HolderHex holder = (HolderHex) v.getTag();
      LineEntry fd = getItem(position);
      updateLineNumbers(fd, holder, position);
      applyUpdated(holder.getContent(), fd);
      holder.getContent().setTextColor(ContextCompat.getColor(getContext(), getContentTextColor(fd, position)));
      applyUserConfig(holder.getContent());
      v.setBackgroundColor(ContextCompat.getColor(getContext(), getContentBackgroundColor(position)));
    }
  }

  private int getContentTextColor(final LineEntry fd, final int position) {
    if (fd.isUpdated())
      return R.color.colorTextUpdated;
    return isSelected(position) ? R.color.colorPrimaryDark : R.color.textColor;
  }

  private int getContentBackgroundColor(final int position) {
    return isSelected(position) ? R.color.colorAccent : R.color.windowBackground;
  }

  /**
   * Update of the line numbers part.
   *
   * @param fd       LineEntry
   * @param holder   Holder
   * @param position position
   */
  private void updateLineNumbers(final LineEntry fd, final HolderHex holder, final int position) {
    if (mApp.isLineNumber()) {
      final int maxLength = String.format("%X", getCurrentLine(getEntries().getItemsCount())).length();
      String fmt = "%0" + maxLength + "X";
      final String s = String.format(fmt, getCurrentLine(fd.getIndex()));
      final @ColorInt int colorLine = ContextCompat.getColor(getContext(),
        isSelected(position) ? R.color.colorAccentDisabled : R.color.colorLineNumbers);
      holder.getLineNumbers().setText(s);
      holder.getLineNumbers().setTextColor(colorLine);
      applyUserConfig(holder.getLineNumbers());
      holder.getLineNumbers().setVisibility(View.VISIBLE);

      if (position == 0) {
        displayTitle();
      }
      applyUserConfig(mTitle.getTitleContent());
      applyUserConfig(mTitle.getTitleLineNumbers());

    } else {
      holder.getLineNumbers().setVisibility(View.GONE);
    }
  }

  /**
   * Displays the row with the columns (if the option is available).
   */
  public void displayTitle() {
    if (mApp.isLineNumber()) {
      final @ColorInt int colorTitle = ContextCompat.getColor(getContext(), R.color.colorLineNumbers);
      final int maxLength = String.format("%X", getCurrentLine(getEntries().getItemsCount())).length();
      final String fmt = "%" + maxLength + "s";
      mTitle.getTitleLineNumbers().setText(String.format(fmt, " "));
      mTitle.getTitleContent().setText(getContext().getString(mApp.getNbBytesPerLine() == SysHelper.MAX_BY_ROW_16 ?
        R.string.title_content : R.string.title_content8));
      mTitle.getTitleContent().setTextColor(colorTitle);
      applyUserConfig(mTitle.getTitleContent());
      applyUserConfig(mTitle.getTitleLineNumbers());
    }
  }
}

