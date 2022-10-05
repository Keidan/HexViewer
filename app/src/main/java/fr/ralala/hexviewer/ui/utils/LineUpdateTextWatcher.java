package fr.ralala.hexviewer.ui.utils;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.EmojiSpan;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.LineUpdateHexArrayAdapter;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * This class is used to manage the modifications of a line via the dedicated dialog box.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LineUpdateTextWatcher implements TextWatcher {
  private static final Pattern PATTERN_4HEX = Pattern.compile("(\\p{XDigit}{4})");
  private final LineUpdateHexArrayAdapter mResultAdapter;
  private final TextInputLayout mLayout;
  private final ApplicationCtx mApp;
  private String mNewString = "";
  private String mOldString = "";
  private boolean mAfterSpace = false;
  private boolean mBetweenDigits = false;
  private int mStart = 0;
  private boolean mIgnore = false;// indicates if the change was made by the TextWatcher itself.
  private boolean mRemove = false;
  private int mStartOffsetForOverwrite = 0;
  private int mShiftOffset;
  private final int mMaxLengthWithPartial;
  private final boolean mSequential;

  private static class ProcessAddContext {
    String notChangedStart;
    String notChangedEnd;
    CharSequence newChange;
  }

  public LineUpdateTextWatcher(LineUpdateHexArrayAdapter resultAdapter,
                               TextInputLayout layout,
                               ApplicationCtx app,
                               final int shiftOffset,
                               final int maxLengthWithPartial,
                               final boolean sequential) {
    mResultAdapter = resultAdapter;
    mShiftOffset = shiftOffset;
    mLayout = layout;
    mApp = app;
    if (mShiftOffset > SysHelper.MAX_BY_ROW_8)
      mShiftOffset -= SysHelper.MAX_BY_ROW_8;
    mMaxLengthWithPartial = maxLengthWithPartial;
    mSequential = sequential;
    updateHelperText(Objects.requireNonNull(
        layout.getEditText()).getText().toString().replace(" ", ""), true);
  }

  /**
   * Treatment if the line is empty.
   */
  private void processNewStringEmpty() {
    if (mApp.isSmartInput()) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      if (et != null)
        et.setText("");
      mIgnore = false; // release, so the TextWatcher start to listen again.
    }
    mResultAdapter.clear();
    updateHelperText("", true);
  }

  /**
   * Processing of the result field color, the content and also the error (if any) of the layout.
   *
   * @param strNew The new string.
   */
  private void processResultAndError(String strNew) {
    final String validate = strNew.trim().replace(" ", "").toLowerCase(Locale.US);
    final boolean validated = SysHelper.isValidHexLine(validate);
    if (!validated) {
      mLayout.setErrorTextAppearance(R.style.AppTheme_ErrorTextAppearance);
      if(!mSequential)
        mLayout.setError(" "); /* only for the color */
    } else {
      byte[] bytes = SysHelper.hex2bin(validate);
      List<LineEntry> li = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8, mShiftOffset);
      mResultAdapter.clear();
      for (LineEntry ld : li)
        mResultAdapter.add(ld.toString());
      mResultAdapter.getListView().post(() ->
        // Select the last row so it will scroll into view...
        mResultAdapter.getListView().setSelection(mResultAdapter.getCount() - 1)
      );
    }
    updateHelperText(validate, validated);
  }
  private void updateHelperTextNotEmpty(final String validate, final boolean validated, final String labelBytes) {
    float nbBytesFloat = (validate.length() / 2.0f);
    int nbBytes;
    if(nbBytesFloat < mMaxLengthWithPartial)
      nbBytes = (int) Math.floor(nbBytesFloat);
    else
      nbBytes = (int) Math.ceil(nbBytesFloat);
    String newHelper = nbBytes + " " + labelBytes + " / " + mMaxLengthWithPartial + " " + labelBytes;
    if(nbBytesFloat == mMaxLengthWithPartial) {
      mLayout.setError(null);
      mLayout.setHelperText(newHelper);
    } else {
      if(!validated)
        newHelper += ": " + mApp.getString(R.string.error_invalid_value);
      mLayout.setError(newHelper);
    }
  }
  private void updateHelperText(final String validate, final boolean validated) {
    if(mSequential) {
      final String labelBytes = mApp.getString(R.string.unit_bytes_full_lc);
      mLayout.setErrorTextAppearance(R.style.AppTheme_ErrorTextAppearance);
      if(validate.isEmpty()) {
        String newHelper = "0 " + mApp.getString(R.string.unit_byte_full) + " / " + mMaxLengthWithPartial + " " + labelBytes;
        mLayout.setError(newHelper);
      } else {
        updateHelperTextNotEmpty(validate, validated, labelBytes);
      }
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
    if(!mSequential)
      mLayout.setError(null);
    final String strNew = mNewString;
    final String strOld = mOldString;
    if (strNew.isEmpty()) {
      processNewStringEmpty();
      return;
    }

    if (!processAfterTextChangeOverwrite(strNew))
      processAfterTextChangeSmartInput(strOld, strNew);

    processResultAndError(strNew);
  }

  /**
   * Management of the afterTextChanged method when the Overwrite box is checked.
   *
   * @param strNew String
   * @return True if the method has consumed the event
   */
  private boolean processAfterTextChangeOverwrite(final String strNew) {
    boolean consumed = false;
    if (!mRemove && !mApp.isSmartInput() && mApp.isOverwrite() && mLayout.getEditText() != null) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      mStart = et.getSelectionStart();
      et.setText(strNew);
      et.setSelection(Math.max(0, Math.min(mStart + mStartOffsetForOverwrite, et.getText().length())));
      mIgnore = false; // release, so the TextWatcher start to listen again.
      consumed = true;
    }
    return consumed;
  }

  /**
   * Management of the afterTextChanged method when the SmartInput box is checked.
   *
   * @param strOld String
   * @param strNew String
   */
  private void processAfterTextChangeSmartInput(final String strOld, final String strNew) {
    if (mApp.isSmartInput() && (mApp.isOverwrite() || !mOldString.equals(strNew)) && mLayout.getEditText() != null) {
      mIgnore = true; // prevent infinite loop
      final EditText et = mLayout.getEditText();
      et.setText(strNew);
      fixCursorPositionForSmartInput(et, strOld, strNew);
      mIgnore = false; // release, so the TextWatcher start to listen again.
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
    if (mIgnore) /* avoid unnecessary treatments */
      return;
    mAfterSpace = s.length() > 0 && s.charAt(Math.max(0, start - 1)) == ' ';
    boolean beforeSpace = start == 0 || s.length() > 0 && s.charAt(Math.max(0, Math.min(start, s.length() - 1))) == ' ';
    mBetweenDigits = start != s.length() && !mAfterSpace && !beforeSpace;
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
    mRemove = mOldString.length() > s.length();
    mNewString = s.toString();
    if (mApp.isSmartInput()) {
      if (mRemove) { /* remove */
        processRemoveWithSmartInput(start, before);
      } else {  /* add */
        processAddWithSmartInput(start, count);
      }
    } else if (mApp.isOverwrite()) {
      processOverwriteWithoutSmartInput(start, before, count);
    }
  }


  @NonNull
  public static String normalizeForEmoji(CharSequence charSequence) {
    if(charSequence.length() == 0)
      return "";
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
    List<LineEntry> li = SysHelper.formatBuffer(buf, null, SysHelper.MAX_BY_ROW_8);
    StringBuilder sb = new StringBuilder();
    for (LineEntry line : li) {
      sb.append(line.getPlain().substring(0, SysHelper.MAX_BYTES_ROW_8).trim()).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Update of the cursor position.
   */
  private void fixCursorPositionForSmartInput(final @NonNull EditText et, final String strOld, final String strNew) {
    final int newLen = strNew.length();
    final int oldLen = strOld.length();
    int delta = Math.abs(newLen - oldLen);
    if (delta == 0 && mApp.isOverwrite())
      delta = mStartOffsetForOverwrite;
    int pos;
    if (newLen > oldLen || (newLen == oldLen && mApp.isOverwrite())) {
      pos = Math.abs(mStart + delta);
      if (mBetweenDigits) pos--;
    } else {
      pos = Math.abs(mStart - delta);
      if (mBetweenDigits) pos++;
    }
    et.setSelection(Math.max(0, Math.min(pos, newLen)));
  }

  private int evaluateLocalStart(int start) {
    int localStart = start;
    if (mBetweenDigits && mApp.isOverwrite()) {
      localStart = Math.max(0, localStart - 1);
    }
    return localStart;
  }

  /**
   * Management of the addition of text with the SmartInput option with overwrite and between chars.
   *
   * @param start int
   * @param count int
   */
  private ProcessAddContext processAddWithSmartInputEvaluateStringsOverwriteBetweenChars(int start, int localStart, int count) {
    ProcessAddContext pac = new ProcessAddContext();
    pac.notChangedStart = localStart == 0 ? "" : mNewString.substring(0, localStart);
    String strChange;
    pac.notChangedEnd = mNewString.substring(Math.min(start + count + 1, mNewString.length()));
    strChange = mNewString.substring(pac.notChangedStart.length() + 1);
    int idxEnd = strChange.indexOf(pac.notChangedEnd);
    if (idxEnd > 0)
      strChange = strChange.substring(0, idxEnd - 1);
    else
      strChange = strChange.substring(0, count);
    pac.newChange = normalizeForEmoji(strChange);
    return pac;
  }

  /**
   * Management of the addition of text with the SmartInput option with or witnout overwrite.
   *
   * @param start int
   * @param count int
   */
  private ProcessAddContext processAddWithSmartInputEvaluateStrings(int start, int localStart, int count) {
    ProcessAddContext pac = new ProcessAddContext();
    pac.notChangedStart = localStart == 0 ? "" : mNewString.substring(0, localStart);
    pac.notChangedEnd = mNewString.substring(Math.min(start + count, mNewString.length()));
    pac.newChange = normalizeForEmoji(mNewString.substring(Math.max(0, localStart),
        Math.min(localStart + count, mNewString.length())));
    return pac;
  }

  private String processAddWithSmartInputOverwrite(final ProcessAddContext pac, final StringBuilder newChangeHex) {
    String str;
    String endStr = pac.notChangedEnd.replace(" ", "");
    String startStr = pac.notChangedStart.replace(" ", "");
    String ns = newChangeHex.toString().replace(" ", "");
    boolean odd = !SysHelper.isEven(startStr.length());
    if (odd) {
      startStr = startStr.substring(0, startStr.length() - 1);
    }
    odd = !SysHelper.isEven(endStr.length());
    if (odd) {
      endStr = endStr.substring(1);
    }
    if (!mBetweenDigits) {
      str = startStr + ns;
      if (!odd && ns.length() < endStr.length() || !odd && ns.length() == endStr.length()) {
        str += endStr.substring(ns.length());
      } else if (ns.length() <= endStr.length())
        str += endStr;
    } else
      str = startStr + ns + endStr;
    mStartOffsetForOverwrite = ns.length();
    return str;
  }

  /**
   * Management of the addition of text with the SmartInput option.
   *
   * @param start int
   * @param count int
   */
  private void processAddWithSmartInput(int start, int count) {
    ProcessAddContext pac;
    int localStart = evaluateLocalStart(start);
    mStart = localStart < 0 ? 0 : localStart + 1;
    if (mApp.isOverwrite()) {
      if (mBetweenDigits) {
        pac = processAddWithSmartInputEvaluateStringsOverwriteBetweenChars(start, localStart, count);
      } else {
        pac = processAddWithSmartInputEvaluateStrings(start, localStart, count);
      }
    } else {
      pac = processAddWithSmartInputEvaluateStrings(start, localStart, count);
    }

    StringBuilder newChangeHex = new StringBuilder();
    byte[] newChangeBytes = pac.newChange.toString().getBytes();
    List<LineEntry> list = SysHelper.formatBuffer(newChangeBytes, newChangeBytes.length, null, SysHelper.MAX_BY_ROW_8);
    for (LineEntry str : list)
      newChangeHex.append(str.getPlain().substring(0, SysHelper.MAX_BYTES_ROW_8).trim());

    String str;
    if (!mApp.isOverwrite()) {
      str = (pac.notChangedStart + newChangeHex.toString() + pac.notChangedEnd).replace(" ", "");
      if (mAfterSpace)
        mStart--;
    } else {
      str = processAddWithSmartInputOverwrite(pac, newChangeHex);
    }
    mNewString = formatText(str.replace(" ", "").toLowerCase(Locale.US));
  }

  /**
   * Management of text deletion with the SmartInput option.
   *
   * @param start  int
   * @param before int
   */
  private void processRemoveWithSmartInput(int start, int before) {
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
  }

  private void initializeStart(int start) {
    mStart = start < 0 ? 0 : start + 1;
  }

  private void fillNewString(final String notChangedEnd, String notChangedStart, final int start, final int before, final int count) {
    CharSequence newChange = mNewString.substring(Math.max(0, start), Math.min(start + count, mNewString.length())).replace(" ", "");
    int nbChars = newChange.length() - Math.max(0, before);
    if (nbChars < 0) nbChars = 0;
    if (nbChars > notChangedEnd.length())
      mNewString = notChangedStart + newChange;
    else
      mNewString = notChangedStart + newChange + (!notChangedEnd.isEmpty() ? notChangedEnd.substring(nbChars) : "");
  }
  /**
   * Management of text insertion in Overwrite mode without the SmartInput option.
   *
   * @param start  int
   * @param before int
   * @param count  int
   */
  private void processOverwriteWithoutSmartInput(int start, int before, int count) {
    if (!mRemove) { /* remove */
      initializeStart(start);

      final String notChangedStart = mNewString.substring(0, start).replace(" ", "");
      String notChangedEnd = mNewString.substring(Math.min(start + count, mNewString.length()));
      mStartOffsetForOverwrite = (notChangedEnd.startsWith(" ") || notChangedEnd.isEmpty()) ? 1 : 0;
      if (mBetweenDigits)
        mStartOffsetForOverwrite++;
      notChangedEnd = notChangedEnd.replace(" ", "");
      fillNewString(notChangedEnd, notChangedStart, start, before, count);
      formatNewString();
    }
  }
  private void formatNewString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mNewString.length(); i += 2) {
      sb.append(mNewString.charAt(i));
      if (i + 1 < mNewString.length())
        sb.append(mNewString.charAt(i + 1));
      sb.append(" ");
    }
    mNewString = sb.toString().trim();
  }
}
