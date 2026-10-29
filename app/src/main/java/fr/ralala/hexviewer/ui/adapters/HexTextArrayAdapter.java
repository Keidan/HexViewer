package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.List;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.entropy.EntropyHeat;
import fr.ralala.hexviewer.entropy.EntropySensitivity;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.ui.adapters.holders.HolderHex;
import fr.ralala.hexviewer.ui.adapters.holders.LineNumbersTitle;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.system.SysHelper;

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
  private static final float HEAT_MAX_ALPHA = 0.6f;
  private final ApplicationCtx mApp;
  private final LineNumbersTitle mTitle;
  private long mStartOffset;
  private final EntropyHeat mEntropy;
  private int mHeatColor = 0;
  private int mHeatThreshold = 0;
  private int mHeatFull = 0;

  public HexTextArrayAdapter(final Context activity, final List<LineEntry> objects,
                             LineNumbersTitle title,
                             UserConfig userConfigPortrait,
                             UserConfig userConfigLandscape) {
    super(activity, ID, objects, userConfigPortrait, userConfigLandscape);
    mStartOffset = 0;
    mTitle = title;
    mApp = (ApplicationCtx) activity.getApplicationContext();
    mEntropy = new EntropyHeat(this::notifyDataSetChanged);
  }

  public void setStartOffset(final long startOffset) {
    mStartOffset = startOffset;
  }

  /**
   * Test if we are from the hex view or the plain view.
   *
   * @return boolean
   */
  @Override
  public boolean isSearchFromHexView() {
    return true;
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
        HolderHex holder = new HolderHex(v.findViewById(R.id.content));
        holder.setLineNumbers(v.findViewById(R.id.line_numbers));
        v.setTag(holder);
      }
    }
    return v == null ? new View(getContext()) : v;
  }


  /**
   * Applies the necessary changes if the "updated" field is true.
   *
   * @param tv AppCompatTextView
   * @param fd FilterData
   */
  private void applyUpdated(final AppCompatTextView tv, final LineEntry fd) {
    if (fd != null) {
      String str = getTextAccordingToUserConfig(fd.getPlain());
      if (fd.isUpdated()) {
        SpannableString spanString = new SpannableString(str);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        tv.setText(spanString);
      } else {
        tv.setText(str);
      }
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
      v.setBackgroundColor(getBackgroundColorWithHeat(fd, position));
    }
  }

  private int getContentTextColor(final LineEntry fd, final int position) {
    if (fd != null && fd.isUpdated())
      return R.color.colorTextUpdated;
    return isSelected(position) ? R.color.colorPrimaryDark : R.color.textColor;
  }

  private int getContentBackgroundColor(final int position) {
    return isSelected(position) ? R.color.colorSelectedLine : R.color.windowBackground;
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
          isSelected(position) ? R.color.colorSelectedNumbers : R.color.colorNumbers);
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
      final @ColorInt int colorTitle = ContextCompat.getColor(getContext(), R.color.colorNumbers);
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

  @Override
  public void refresh() {
    super.refresh();
    final int res = mApp.getEntropyColorRes();
    if (res == 0) {
      mHeatColor = 0;
      mEntropy.clear();
      return;
    }
    final EntropySensitivity s = mApp.getEntropySensitivity();
    mHeatColor = ContextCompat.getColor(getContext(), res);
    mHeatThreshold = s.getThreshold();
    mHeatFull = s.getFull();
    mEntropy.schedule(getEntries().getItems());
  }

  /**
   * Computes the background color of a line, tinted according to its entropy heat level.
   *
   * <p>
   * The base color is the regular content background color of the given position. When the
   * entropy level of the line reaches {@code HEAT_THRESHOLD}, the base color is blended with
   * the {@code R.color.entropyHot} color. The more entropic the line, the stronger the blend:
   * </p>
   * <ul>
   *   <li>a level below {@code HEAT_THRESHOLD} leaves the base color untouched;</li>
   *   <li>a level equal to {@code HEAT_THRESHOLD} yields a blend ratio of {@code 0}
   *   (base color);</li>
   *   <li>the maximum level ({@code 255}) yields a blend ratio of {@code HEAT_MAX_ALPHA},
   *   which caps the intensity of the heat tint so that the base color always remains
   *   visible.</li>
   * </ul>
   *
   * <p>
   * The base color is returned unchanged, without any heat tint, when:
   * </p>
   * <ul>
   *   <li>{@code fd} is {@code null};</li>
   *   <li>the line at {@code position} is selected, so that the selection color remains
   *   readable;</li>
   *   <li>the entropy level of the line is below {@code HEAT_THRESHOLD} (or no heat map is
   *   available, in which case the level is {@code 0}).</li>
   * </ul>
   *
   * @param fd       the line entry to colorize; its {@link LineEntry#getIndex() index} is used to
   *                 look up the heat level. May be {@code null}
   * @param position the adapter position of the line, used to determine the base background
   *                 color and the selection state
   * @return the resulting background color as an ARGB color int: either the base color, or the
   * base color blended with {@code R.color.entropyHot}
   * @see EntropyHeat#levelOf(int)
   * @see ColorUtils#blendARGB(int, int, float)
   */
  private int getBackgroundColorWithHeat(final LineEntry fd, final int position) {
    final int base = ContextCompat.getColor(getContext(), getContentBackgroundColor(position));
    if (fd == null || isSelected(position))
      return base;  // A selected line retains its color

    final int level = mEntropy.levelOf(fd.getIndex());
    if (level < mHeatThreshold)
      return base;
    int div = (mHeatFull - mHeatThreshold);
    if(div == 0) div = 1;
    final float ratio = Math.min(1f, (float) (level - mHeatThreshold) / div);
    return ColorUtils.blendARGB(base, mHeatColor, ratio * HEAT_MAX_ALPHA);
  }
}

