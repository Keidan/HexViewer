package fr.ralala.hexviewer.ui.dialog;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.utils.UIHelper;

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
public class GoToDialog implements View.OnClickListener, AbsListView.OnScrollListener {
  private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");
  private String mPreviousGoToValueAddress = "0";
  private String mPreviousGoToValueLineHex = "0";
  private String mPreviousGoToValueLinePlain = "0";
  private final AlertDialog mDialog;
  private EditText mEt;
  private TextInputLayout mLayout;
  private final MainActivity mActivity;
  private int mPosition = 0;
  private boolean mStarted = false;
  private Mode mMode;
  private String mTitle;

  public enum Mode {
    ADDRESS,
    LINE_HEX,
    LINE_PLAIN
  }

  @SuppressLint("InflateParams")
  public GoToDialog(MainActivity activity) {
    mActivity = activity;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setCancelable(true)
        .setTitle(R.string.action_go_to_address)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(activity);
    builder.setView(factory.inflate(R.layout.content_dialog_go_to, null));
    mDialog = builder.create();
  }

  /**
   * Displays the dialog
   */
  public void show(Mode mode) {
    mMode = mode;
    mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    String title1, title2;
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
      if (mode == Mode.ADDRESS) {
        mEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mEt.setText(mPreviousGoToValueAddress);
      } else {
        mEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mEt.setText(mode == Mode.LINE_HEX ? mPreviousGoToValueLineHex : mPreviousGoToValueLinePlain);
      }
      if (mEt.getText().length() == 0)
        mLayout.setError(" "); /* only for the color */
      mEt.addTextChangedListener(UIHelper.getResetLayoutWatcher(mLayout, true));
      mEt.setSelection(0, mEt.getText().length());

      mEt.requestFocus();
    }
    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
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

    ListView lv = (mMode == Mode.ADDRESS || mMode == Mode.LINE_HEX) ?
        mActivity.getPayloadHex().getListView() : mActivity.getPayloadPlain().getListView();
    int position;
    int max = lv.getAdapter().getCount() - 1;
    String s_max;
    if (mMode == Mode.ADDRESS) {
      if (!HEXADECIMAL_PATTERN.matcher(text).matches()) {
        if (mLayout != null)
          mLayout.setError(" "); /* only for the color */
        return;
      }
      int nbBytesPerLines = ApplicationCtx.getInstance().getNbBytesPerLine();
      try {
        position = Integer.parseInt(text, 16) / nbBytesPerLines;
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        position = -1;
      }
      final int maxLength = String.format("%X", max * nbBytesPerLines).length();
      s_max = String.format("%0" + maxLength + "X", (max * nbBytesPerLines) + nbBytesPerLines - 1);
    } else {
      try {
        position = Integer.parseInt(text) - 1;
        if(position < 0)
          position = 0;
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        position = -1;
      }
      s_max = String.valueOf(max + 1);
      if(position <= max)
        max++;
    }
    if (position == -1 || position > max) {
      String err = String.format(mActivity.getString(R.string.error_cant_exceed_xxx), s_max);
      if (mLayout != null) {
        UIHelper.shakeError(mEt, null);
        mLayout.setError(err);
      } else {
        mDialog.dismiss();
        UIHelper.showErrorDialog(mActivity, mTitle, err);
      }
      return;
    }
    lv.setOnScrollListener(this);
    mPosition = Math.max(0, position);
    lv.post(() -> {
      mStarted = true;
      lv.smoothScrollToPositionFromTop(mPosition, 0, 500);
    });
    if (mMode == Mode.LINE_PLAIN)
      mPreviousGoToValueLinePlain = text;
    else if (mMode == Mode.LINE_HEX)
      mPreviousGoToValueLineHex = text;
    else
      mPreviousGoToValueAddress = text;
    mDialog.dismiss();
  }

  /**
   * Callback method to be invoked while the list view or grid view is being scrolled.
   *
   * @param view        The view whose scroll state is being reported.
   * @param scrollState The current scroll state.
   */
  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    if (scrollState == SCROLL_STATE_IDLE) {
      //we reached the target position
      blinkBackground();
    }
  }

  /**
   * Callback method to be invoked when the list or grid has been scrolled.
   *
   * @param view             The view whose scroll state is being reported.
   * @param firstVisibleItem the index of the first visible cell (ignore if
   *                         visibleItemCount == 0).
   * @param visibleItemCount the number of visible cells.
   * @param totalItemCount   the number of items in the list adapter.
   */
  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    if (mStarted && mPosition >= firstVisibleItem && mPosition <= visibleItemCount) {
      blinkBackground();
    }
    /* nothing to do */
  }

  /**
   * Blinks the background of the selected view
   */
  private void blinkBackground() {
    mStarted = false;
    ListView lv = (mMode == Mode.ADDRESS || mMode == Mode.LINE_HEX) ?
        mActivity.getPayloadHex().getListView() : mActivity.getPayloadPlain().getListView();
    View v = UIHelper.getViewByPosition(mPosition, lv);
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
