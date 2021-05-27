package fr.ralala.hexviewer.ui.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * This class is used to manage the modifications of a line via the dedicated dialog box.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class LineUpdateTextWatcher implements TextWatcher {
  private final Context mContext;
  private final TextView mResult;
  private final TextInputLayout mLayout;
  private final ApplicationCtx mApp;
  private String mNewString = "";
  private boolean mIgnore = false;// indicates if the change was made by the TextWatcher itself.

  public LineUpdateTextWatcher(Context context, TextView result, TextInputLayout layout, ApplicationCtx app) {
    mContext = context;
    mResult = result;
    mLayout = layout;
    mApp = app;
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
    final String str = mNewString;
    if (str.isEmpty()) {
      mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultWarning));
      mResult.setText(R.string.empty_value);
      return;
    }
    if (!s.toString().equals(str) && mLayout.getEditText() != null) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      int cursorPosition = et.getSelectionStart();
      et.setText(str);
      fixCursorPosition(cursorPosition, et, str.length());
      mIgnore = false; // release, so the TextWatcher start to listen again.
    }

    final String validate = str.trim().replaceAll(" ", "").toLowerCase(Locale.US);
    String string;
    if (validate.length() % 2 == 0 || validate.length() > (SysHelper.MAX_BY_ROW * 2)) {
      mResult.setTextColor(ContextCompat.getColor(mContext,
          validate.length() > (SysHelper.MAX_BY_ROW * 2) ? R.color.colorResultError : R.color.colorResultSuccess));
      final byte[] buf = SysHelper.hexStringToByteArray(validate);
      string = SysHelper.formatBuffer(buf, null).get(0);
    } else {
      mResult.setTextColor(ContextCompat.getColor(mContext, R.color.colorResultWarning));
      if (validate.length() == 1) {
        string = "                                                   ";
      } else {
        string = formatText(validate.substring(0, validate.length() - 1));
      }
    }
    mResult.setText(SysHelper.extractString(string));
    if (!SysHelper.isValidHexLine(validate)) {
      mLayout.setError(mContext.getString(R.string.error_entry_format));
    }
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
    // nothing to do
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
    mNewString = s.toString();
    //Log.e(getClass().getSimpleName(), "s: " + s.toString() + ", start: " + start + ", count: " + count + ", before: " + before);
    if (mApp.isSmartInput()) {
      if (count == 0) { /* remove */
        mNewString = mNewString.replaceAll("(^\\p{XDigit} | \\p{XDigit} | \\p{XDigit}$| {2})", " ").trim();
      } else {  /* add */
        mNewString = mNewString.trim();
        final String notChangedStart = mNewString.substring(0, start);
        final String notChangedEnd = mNewString.substring(start + count);
        final String newChange = mNewString.substring(start, start + count);
        final String newChangeHex = SysHelper.extractHex(SysHelper.formatBuffer(newChange.getBytes(), count, null).get(0));
        mNewString = formatText((notChangedStart + newChangeHex + notChangedEnd).replaceAll(" ", "").toLowerCase(Locale.US));
      }
    }
  }

  /**
   * Format the text taking into account the fact that it can be on several lines (within the size of the dialog box)
   *
   * @param text The text to be formatted.
   * @return The new text.
   */
  private String formatText(String text) {
    final byte[] buf = SysHelper.hexStringToByteArray(text);
    List<String> li = SysHelper.formatBuffer(buf, null);
    StringBuilder sb = new StringBuilder();
    for (String line : li) {
      sb.append(SysHelper.extractHex(line)).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Update of the cursor position.
   *
   * @param prevPosition  Previous position.
   * @param et            EditText
   * @param newTextLength New text length.
   */
  private void fixCursorPosition(final int prevPosition, final @NonNull EditText et, final int newTextLength) {
    /* fix the cursor position */
    if (newTextLength < prevPosition) {
      et.setSelection(prevPosition - (mApp.isSmartInput() ? 2 : 1));
    } else
      et.setSelection(prevPosition + (mApp.isSmartInput() ? 2 : 1));
  }
}
