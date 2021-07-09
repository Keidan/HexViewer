package fr.ralala.hexviewer.ui.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.EmojiSpan;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * This class is used to manage the modifications of a line via the dedicated dialog box.
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class LineUpdateTextWatcher implements TextWatcher {
  private static final Pattern PATTERN_4HEX = Pattern.compile("(\\p{XDigit}{4})");
  private final Context mContext;
  private final TextView mResult;
  private final TextInputLayout mLayout;
  private final ApplicationCtx mApp;
  private String mNewString = "";
  private String mOldString = "";
  private boolean mBetweenDigits = false;
  private int mStart = 0;
  private boolean mIgnore = false;// indicates if the change was made by the TextWatcher itself.

  public LineUpdateTextWatcher(Context context, TextView result, TextInputLayout layout, ApplicationCtx app) {
    mContext = context;
    mResult = result;
    mLayout = layout;
    mApp = app;
  }

  /**
   * Treatment if the line is empty.
   */
  private void processNewStringEmpty() {
    mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultWarning));
    mResult.setText(R.string.empty_value);
    if (mApp.isSmartInput()) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      if (et != null)
        et.setText("");
      mIgnore = false; // release, so the TextWatcher start to listen again.
    }
  }

  /**
   * Processing of the result field color, the content and also the error (if any) of the layout.
   *
   * @param strNew The new string.
   */
  private void processResultAndError(String strNew) {
    boolean isError = false; /* true = error, false = warning */
    final String validate = strNew.trim().replaceAll(" ", "").toLowerCase(Locale.US);
    final boolean validated = SysHelper.isValidHexLine(validate, false);
    if (!SysHelper.isEven(validate.length()) && validate.matches("\\p{XDigit}+"))
      mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultWarning));
    else if (!validated) {
      isError = true;
      mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultError));
    } else
      mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultSuccess));

    mResult.setText(SysHelper.hex2bin(validate));
    if (!validated) {
      mLayout.setErrorTextAppearance(isError ? R.style.AppTheme_ErrorTextAppearance : R.style.AppTheme_WarningTextAppearance);
      mLayout.setError(" "); /* only for the color */
    }
  }

  /**
   * This method is called to notify you that, somewhere within s, the text has been changed.
   * It is legitimate to make further changes to s from this callback, but be careful not to get
   * yourself into an infinite loop, because any changes you make will cause this method to be
   * called again recursively. (You are not told where the change took place because other
   * afterTextChanged() methods may already have made other changes and invalidated the offsets.
   * But if you need to know here, you can use Spannable#setSpan in
   * onTextChanged(CharSequence, int, int, int) to mark your place and then look
   * up from here where the span ended up.
   *
   * @param s Editable
   */
  public void afterTextChanged(Editable s) {
    if (mIgnore)
      return;
    mLayout.setError(null);
    final String strNew = mNewString;
    final String strOld = mOldString;
    if (strNew.isEmpty()) {
      processNewStringEmpty();
      return;
    }
    if (mApp.isSmartInput() && !mOldString.equals(strNew) && mLayout.getEditText() != null) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      et.setText(strNew);
      fixCursorPosition(et, strOld, strNew);
      mIgnore = false; // release, so the TextWatcher start to listen again.
    }
    processResultAndError(strNew);
  }

  /**
   * This method is called to notify you that, within s, the count characters beginning at start are about to be replaced
   * by new text with length after. It is an error to attempt to make changes to s from this callback.
   *
   * @param s     CharSequence
   * @param start int
   * @param count int
   * @param after int
   */
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    if (mIgnore) /* avoid unnecessary treatments */
      return;
    int len = s.length();
    mBetweenDigits = (len > 3 && s.charAt(Math.min(start + 1, len - 1)) != ' ' && s.charAt(Math.max(0, start - 1)) == ' ');
    mOldString = s.toString();
  }

  /**
   * This method is called to notify you that, within s, the count characters beginning at start have just replaced old text
   * that had length before. It is an error to attempt to make changes to s from this callback.
   *
   * @param s      CharSequence
   * @param start  int
   * @param before int
   * @param count  int
   */
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (mIgnore) /* avoid unnecessary treatments */
      return;
    mNewString = s.toString();
    if (mApp.isSmartInput()) {
      if (count == 0) { /* remove */
        mStart = start + before;
        /* remove one space */
        Matcher m = PATTERN_4HEX.matcher(mNewString);
        if (m.find()) {
          String grp = m.group(1);
          if (grp != null)
            mNewString = mNewString.replace(grp, grp.substring(2)).replaceAll(" {2}", " ");
        } else
          /* remove one char */
          mNewString = mNewString.replaceAll("(^\\p{XDigit} | \\p{XDigit} | \\p{XDigit}$| {2}|^\\p{XDigit}$)", " ").trim();
        if (mNewString.length() < 2)
          mNewString = "";
      } else {  /* add */
        mStart = start == 0 ? 0 : start + 1;
        final String notChangedStart = mNewString.substring(0, start);
        final String notChangedEnd = mNewString.substring(Math.min(start + count, mNewString.length()));
        CharSequence newChange = normalizeForEmoji(mNewString.substring(start, Math.min(start + count, mNewString.length())));
        StringBuilder newChangeHex = new StringBuilder();
        byte[] newChangeBytes = newChange.toString().getBytes();
        List<LineData<Line>> list = SysHelper.formatBuffer(newChangeBytes, newChangeBytes.length, null);
        for (LineData<Line> str : list)
          newChangeHex.append(SysHelper.extractHex(str.getValue().getPlain()));
        mNewString = formatText((notChangedStart + newChangeHex.toString() + notChangedEnd).replaceAll(" ", "").toLowerCase(Locale.US));
      }
    }
  }

  @NonNull
  public static String normalizeForEmoji(CharSequence charSequence) {
    CharSequence processed = EmojiCompat.get().process(charSequence, 0, charSequence.length() - 1, Integer.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL);
    if (processed instanceof Spannable) {
      Spannable spannable = (Spannable) processed;
      EmojiSpan[] emojiSpans = spannable.getSpans(0, spannable.length() - 1, EmojiSpan.class);
      StringBuilder sb = new StringBuilder();
      int oldStart = 0;
      for (EmojiSpan emojiSpan : emojiSpans) {
        int spanEnd = spannable.getSpanEnd(emojiSpan);
        sb.append(spannable.subSequence(oldStart, spanEnd));
        oldStart = spanEnd;
      }
      int len = charSequence.length();
      if (oldStart != len - emojiSpans.length)
        sb.append(spannable.subSequence(oldStart, len));
      return sb.toString();
    }
    return charSequence.toString();
  }

  /**
   * Format the text taking into account the fact that it can be on several lines (within the size of the dialog box)
   *
   * @param text The text to be formatted.
   * @return The new text.
   */
  private String formatText(String text) {
    final byte[] buf = SysHelper.hexStringToByteArray(text);
    List<LineData<Line>> li = SysHelper.formatBuffer(buf, null);
    StringBuilder sb = new StringBuilder();
    for (LineData<Line> line : li) {
      sb.append(SysHelper.extractHex(line.getValue().getPlain())).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Update of the cursor position.
   */
  private void fixCursorPosition(final @NonNull EditText et, final String strOld, final String strNew) {
    final int newLen = strNew.length();
    final int oldLen = strOld.length();
    final int delta = Math.abs(newLen - oldLen);
    int pos;
    if (newLen > oldLen) {
      pos = Math.abs(mStart + delta);
      if (mBetweenDigits) pos--;
    } else {
      pos = Math.abs(mStart - delta);
      if (mBetweenDigits) pos++;
    }
    et.setSelection(Math.max(0, Math.min(pos, newLen)));
  }
}
