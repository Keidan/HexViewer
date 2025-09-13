package fr.ralala.hexviewer.ui.dialog;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntries;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Management of the dialog box used for the "Go to line" action
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class GoToDialog implements View.OnClickListener {
  private static final String EXCEPTION_TAG = "Exception: ";
  private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");
  private String mPreviousGoToValueAddress = "0";
  private String mPreviousGoToValueLineHex = "0";
  private String mPreviousGoToValueLinePlain = "0";
  private AlertDialog mDialog;
  private EditText mEt;
  private TextInputLayout mLayout;
  private final AppCompatActivity mActivity;
  private final ICommonUI mCommonUI;
  private int mPosition = 0;
  private Mode mMode;
  private String mTitle;
  private final ApplicationCtx mApp;

  public enum Mode {
    ADDRESS,
    LINE_HEX,
    LINE_PLAIN
  }

  public GoToDialog(AppCompatActivity activity, ICommonUI commonUI) {
    mActivity = activity;
    mCommonUI = commonUI;
    mApp = mCommonUI.getApplicationCtx();
  }

  /**
   * Displays the dialog
   */
  @SuppressLint("InflateParams")
  public AlertDialog show(Mode mode) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppTheme_DialogTheme);
    builder.setCancelable(true)
      .setTitle(R.string.action_go_to_address)
      .setPositiveButton(android.R.string.ok, null)
      .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
      });
    LayoutInflater factory = LayoutInflater.from(mActivity);
    builder.setView(factory.inflate(R.layout.content_dialog_go_to, null));
    mDialog = builder.create();
    initDialog();
    mMode = mode;
    String title1;
    String title2;
    if (mode == Mode.ADDRESS) {
      title1 = mActivity.getString(R.string.action_go_to_address);
      title2 = mActivity.getString(R.string.hexadecimal);
    } else {
      title1 = mActivity.getString(R.string.action_go_to_line);
      title2 = mActivity.getString(R.string.decimal);
    }
    mTitle = title1 + " (" + title2 + ")";
    mDialog.setTitle(mTitle);
    mDialog.show();
    mEt = mDialog.findViewById(R.id.tieValue);
    mLayout = mDialog.findViewById(R.id.tilValue);

    if (mEt != null && mLayout != null) {
      mEt.setTextDirection(SysHelper.isRTL(mActivity) ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
      if (mode == Mode.ADDRESS) {
        mEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mEt.setText(mPreviousGoToValueAddress);
      } else {
        mEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mEt.setText(mode == Mode.LINE_HEX ? mPreviousGoToValueLineHex : mPreviousGoToValueLinePlain);
      }
      if (TextUtils.isEmpty(mEt.getText()))
        mLayout.setError(" "); /* only for the color */
      mEt.addTextChangedListener(UIHelper.getResetLayoutWatcher(mLayout, true));
      mEt.setSelection(0, mEt.getText().length());

      mEt.requestFocus();
    }
    mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
    mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> mDialog.dismiss());
    return mDialog;
  }

  private void initDialog() {
    if (mDialog.isShowing())
      mDialog.dismiss();
    if (mDialog.getWindow() != null)
      mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
  }

  private boolean processPosition(final String text) {
    ListView lv = (mMode == Mode.ADDRESS || mMode == Mode.LINE_HEX) ?
      mCommonUI.getPayloadHex().getListView() : mCommonUI.getPayloadPlain().getListView();
    SearchableListArrayAdapter adapter = ((SearchableListArrayAdapter) lv.getAdapter());
    int position;
    if (mMode == Mode.ADDRESS) {
      if (validatePosition(text, adapter.getEntries().getItemsCount() - 1))
        return false;
      position = evaluatePosition(adapter, adapter.getCount());
      if (position == -1) {
        displayError(mActivity.getString(R.string.error_not_available));
        return false;
      }
    } else {
      if (validatePosition(text, lv.getAdapter().getCount() - 1))
        return false;
      position = mPosition;
    }
    lv.post(() -> {
      lv.setSelectionFromTop(position, 0);
      lv.post(() -> blinkBackground(position));
    });
    return true;
  }

  /**
   * Called when a view has been clicked.
   *
   * @param v The view that was clicked.
   */
  @Override
  public void onClick(View v) {
    if (mEt == null) {
      if (mLayout != null)
        mLayout.setError(" "); /* only for the color */
      return;
    }
    String text = mEt.getText().toString();
    if (text.isEmpty())
      return;
    if (!processPosition(text))
      return;
    if (mMode == Mode.LINE_PLAIN)
      mPreviousGoToValueLinePlain = text;
    else if (mMode == Mode.LINE_HEX)
      mPreviousGoToValueLineHex = text;
    else
      mPreviousGoToValueAddress = text;
    mDialog.dismiss();
    mCommonUI.setOrphanDialog(null);
  }

  /**
   * Evaluates the position in the list.
   *
   * @param adapter SearchableListArrayAdapter
   * @param count   Nb of elements.
   * @return The position.
   */
  private int evaluatePosition(SearchableListArrayAdapter adapter, int count) {
    LineEntries entries = adapter.getEntries();
    int position;
    if (count <= 500) {
      position = getAddressPosition(entries, 0, count);
    } else {
      int middle = count / 2;
      if (mPosition == middle)
        position = middle;
      else if (mPosition < middle) {
        position = getAddressPosition(entries, 0, middle);
      } else {
        position = getAddressPosition(entries, middle, count);
      }
    }
    return position;
  }

  /**
   * Gets the position of the address in the list.
   *
   * @param entries LineEntries
   * @param start   Start index.
   * @param end     End index.
   * @return The position.
   */
  private int getAddressPosition(LineEntries entries, int start, int end) {
    int position = -1;
    for (int i = start; i < end; i++) {
      if (entries.getItemIndex(i) == mPosition) {
        position = i;
        break;
      }
    }
    return position;
  }

  private String validatePositionModeAddress(final String text, final int max, final AtomicInteger position) {
    if (!HEXADECIMAL_PATTERN.matcher(text).matches()) {
      UIHelper.shakeError(mEt, null);
      if (mLayout != null)
        mLayout.setError(" "); /* only for the color */
      return "";
    }
    int nbBytesPerLines = mApp.getNbBytesPerLine();
    try {
      position.set(Integer.parseInt(text, 16) / nbBytesPerLines);
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
      position.set(-1);
    }
    final int maxLength = String.format("%X", max * nbBytesPerLines).length();
    final String fmt = "%0" + maxLength + "X";
    return String.format(fmt, (max * nbBytesPerLines) + nbBytesPerLines - 1);
  }

  private String validatePositionModeLines(final String text, final AtomicInteger max, final AtomicInteger position) {
    try {
      position.set(Integer.parseInt(text) - 1);
      if (position.get() < 0)
        position.set(0);
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
      position.set(-1);
    }
    final String sMax = String.valueOf(max.get() + 1);
    if (position.get() <= max.get())
      max.set(max.get() + 1);
    return sMax;
  }

  /**
   * Validates the position of the cursor.
   *
   * @param text     The input value.
   * @param maxLines The maximum number of lines.
   * @return true on error.
   */
  private boolean validatePosition(String text, int maxLines) {
    AtomicInteger position = new AtomicInteger();
    AtomicInteger max = new AtomicInteger(maxLines);
    String sMax;
    if (mMode == Mode.ADDRESS) {
      sMax = validatePositionModeAddress(text, max.get(), position);
      if (sMax.isEmpty())
        return true;
    } else {
      sMax = validatePositionModeLines(text, max, position);
    }
    if (position.get() == -1 || position.get() > max.get()) {
      String err = String.format(mActivity.getString(R.string.error_cant_exceed_xxx), sMax);
      displayError(err);
      return true;
    }
    mPosition = Math.max(0, position.get());
    return false;
  }

  /**
   * Displays an error message
   *
   * @param err The message.
   */
  private void displayError(String err) {
    if (mLayout != null) {
      UIHelper.shakeError(mEt, null);
      mLayout.setError(err);
    } else {
      mDialog.dismiss();
      UIHelper.showErrorDialog(mActivity, mTitle, err);
    }
  }

  /**
   * Blinks the background of the selected view
   */
  private void blinkBackground(int position) {
    ListView lv = (mMode == Mode.ADDRESS || mMode == Mode.LINE_HEX) ?
      mCommonUI.getPayloadHex().getListView() : mCommonUI.getPayloadPlain().getListView();
    View v = UIHelper.getViewByPosition(position, lv);
    lv.setOnScrollListener(null);
    int windowBackground = ContextCompat.getColor(mActivity, R.color.windowBackground);
    int colorAccent = ContextCompat.getColor(mActivity, R.color.colorAccent);
    ObjectAnimator anim = ObjectAnimator.ofInt(v, "backgroundColor",
      windowBackground, colorAccent, windowBackground);
    anim.setDuration(1000);
    anim.setEvaluator(new ArgbEvaluator());
    anim.setRepeatMode(ValueAnimator.REVERSE);
    anim.setRepeatCount(3);
    anim.start();
  }
}
