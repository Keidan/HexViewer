package fr.ralala.hexviewer.ui.dialog;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Management of the dialog box used for the "Sequential opening" action
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SequentialOpenDialog implements View.OnClickListener, AdapterView.OnItemSelectedListener, TextWatcher {
  private static final int IDX_BYTES = 0;
  private static final int IDX_K_BYTES = 1;
  private static final int IDX_M_BYTES = 2;
  private static final int IDX_G_BYTES = 3;
  private final AlertDialog mDialog;
  private final MainActivity mActivity;
  private TextView mTextSize;
  private AppCompatSpinner mSpUnit;
  private TextInputLayout mTilStart;
  private TextInputEditText mTietStart;
  private TextInputLayout mTilEnd;
  private TextInputEditText mTietEnd;
  private FileData mFd;
  private SequentialOpenListener mSequentialOpenListener;

  public interface SequentialOpenListener {
    void onSequentialOpen();
  }


  @SuppressLint("InflateParams")
  public SequentialOpenDialog(MainActivity activity) {
    mActivity = activity;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setCancelable(true)
        .setTitle(R.string.action_open_sequential_title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(activity);
    builder.setView(factory.inflate(R.layout.content_dialog_open_sequential, null));
    mDialog = builder.create();
  }

  /**
   * Displays the dialog
   */
  public AlertDialog show(FileData fd, SequentialOpenListener listener) {
    mFd = fd;
    mSequentialOpenListener = listener;
    mDialog.show();
    mTextSize = mDialog.findViewById(R.id.textSize);
    mSpUnit = mDialog.findViewById(R.id.spUnit);
    mTilStart = mDialog.findViewById(R.id.tilStart);
    TextInputEditText tietStart = mDialog.findViewById(R.id.tietStart);
    mTilEnd = mDialog.findViewById(R.id.tilEnd);
    TextInputEditText tietEnd = mDialog.findViewById(R.id.tietEnd);

    long max;
    long start;
    long end;
    if (fd != null && fd.isSequential()) {
      start = fd.getStartOffset();
      end = fd.getEndOffset();
      max = fd.getSize();
    } else {
      start = 0;
      end = fd == null ? 0 : fd.getRealSize();
      max = end;
    }
    Log.e("exc", "start: " + start + ", end: " + end + ", max: " + max);
    if (mSpUnit != null) {
      List<String> units = new ArrayList<>();
      units.add(mActivity.getString(R.string.unit_byte_full));
      units.add(mActivity.getString(R.string.unit_kbyte));
      units.add(mActivity.getString(R.string.unit_mbyte));
      units.add(mActivity.getString(R.string.unit_gbyte));
      ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
          android.R.layout.simple_spinner_item,
          units);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpUnit.setSelection(IDX_BYTES);
      mSpUnit.setAdapter(adapter);
      mSpUnit.setOnItemSelectedListener(this);
    }

    if (tietStart != null) {
      tietStart.setText(String.valueOf(start));
      if (mTietStart != null)
        mTietStart.removeTextChangedListener(this);
      tietStart.addTextChangedListener(this);
      mTietStart = tietStart;
    }
    if (tietEnd != null) {
      tietEnd.setText(String.valueOf(end));
      if (mTietEnd != null)
        mTietEnd.removeTextChangedListener(this);
      tietEnd.addTextChangedListener(this);
      mTietEnd = tietEnd;
    }
    if (mTextSize != null)
      mTextSize.setText(SysHelper.sizeToHuman(mActivity, max));
    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    evaluateSize();
    return mDialog;
  }

  /**
   * Evaluates the size.
   */
  private void evaluateSize() {
    mTextSize.setText("0");
    if (!checkValues())
      return;
    String s_start = Objects.requireNonNull(mTietStart.getText()).toString();
    String s_end = Objects.requireNonNull(mTietEnd.getText()).toString();
    if (mSpUnit.getSelectedItemId() == IDX_BYTES) {
      long start = getValue(s_start);
      long end = getValue(s_end);
      long size = Math.abs(end - start);
      mTextSize.setText(SysHelper.sizeToHuman(mActivity, size));
    }
  }

  /**
   * Checks the values.
   *
   * @return False on error.
   */
  private boolean checkValues() {
    String s_start = mTietStart == null || mTietStart.getText() == null ? "" : mTietStart.getText().toString();
    String s_end = mTietEnd == null || mTietEnd.getText() == null ? "" : mTietEnd.getText().toString();
    boolean valid = checkEmpty(s_start, s_end);
    if (valid) {
      long start = getValue(s_start);
      long end = getValue(s_end);
      valid = checkForSize(start, end);
      if (valid) {
        if (end <= start) {
          valid = false;
          if (mTilStart != null)
            mTilStart.setError(mActivity.getString(R.string.error_greater_than) + " " +
                SysHelper.sizeToHuman(mActivity, end));
          if (mTilEnd != null)
            mTilEnd.setError(mActivity.getString(R.string.error_less_than) + " " +
                SysHelper.sizeToHuman(mActivity, start));
        } else {
          if (mTilStart != null)
            mTilStart.setError(null);
          if (mTilEnd != null)
            mTilEnd.setError(null);
        }
      }
    }
    return valid;
  }

  /**
   * Gets the value according to the unit.
   *
   * @param s_value The string value.
   * @return The long value.
   */
  private long getValue(String s_value) {
    if (mSpUnit.getSelectedItemId() == IDX_K_BYTES) {
      try {
        return (long) (Float.parseFloat(s_value) * SysHelper.SIZE_1KB);
      } catch (Exception e) {
        return 0;
      }
    } else if (mSpUnit.getSelectedItemId() == IDX_M_BYTES) {
      try {
        return (long) (Float.parseFloat(s_value) * SysHelper.SIZE_1MB);
      } catch (Exception e) {
        return 0;
      }
    } else if (mSpUnit.getSelectedItemId() == IDX_G_BYTES) {
      try {
        return (long) (Float.parseFloat(s_value) * SysHelper.SIZE_1GB);
      } catch (Exception e) {
        return 0;
      }
    } else {
      try {
        return (long) Float.parseFloat(s_value);
      } catch (Exception e) {
        return 0;
      }
    }
  }

  /**
   * Returns the file data.
   *
   * @return FileData
   */
  public FileData getFileData() {
    return mFd;
  }

  /**
   * Called when a view has been clicked.
   *
   * @param v The view that was clicked.
   */
  @Override
  public void onClick(View v) {
    if (checkValues()) {
      mFd.setOffsets(getValue(Objects.requireNonNull(mTietStart.getText()).toString()),
          getValue(Objects.requireNonNull(mTietEnd.getText()).toString()));
      mDialog.dismiss();
      mActivity.setOrphanDialog(null);
      if (mSequentialOpenListener != null)
        mSequentialOpenListener.onSequentialOpen();
    }
  }

  /**
   * <p>Callback method to be invoked when an item in this view has been
   * selected.</p>
   *
   * @param parent   The AdapterView where the selection happened
   * @param view     The view within the AdapterView that was clicked
   * @param position The position of the view in the adapter
   * @param id       The row id of the item that is selected
   */
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    evaluateSize();
  }

  /**
   * Callback method to be invoked when the selection disappears from this
   * view.
   *
   * @param parent The AdapterView that now contains no selected item.
   */
  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    /* nothing */
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
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    /* nothing */
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
  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    /* nothing */
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
  @Override
  public void afterTextChanged(Editable s) {
    evaluateSize();
  }

  /**
   * Checks if the fields are empty or not.
   * @param start String value for the start field.
   * @param end String value for the end field.
   * @return boolean
   */
  private boolean checkEmpty(String start, String end) {
    boolean valid = true;
    if (start.isEmpty()) {
      if (mTilStart != null)
        mTilStart.setError(mActivity.getString(R.string.error_less_than) + " 0");
      valid = false;
    }
    if (end.isEmpty()) {
      if (mTilEnd != null)
        mTilEnd.setError(mActivity.getString(R.string.error_less_than) + " 0");
      valid = false;
    }
    return valid;
  }

  /**
   * Checks if the fields are not larger than the file size.
   * @param start Long value for the start field.
   * @param end Long value for the end field.
   * @return boolean
   */
  private boolean checkForSize(long start, long end) {
    boolean valid = true;
    if (start > mFd.getRealSize()) {
      valid = false;
      if (mTilStart != null)
        mTilStart.setError(mActivity.getString(R.string.error_greater_than) + " " +
            SysHelper.sizeToHuman(mActivity, mFd.getRealSize()));
    }
    if (end > mFd.getRealSize()) {
      valid = false;
      if (mTilEnd != null)
        mTilEnd.setError(mActivity.getString(R.string.error_greater_than) + " " +
            SysHelper.sizeToHuman(mActivity, mFd.getRealSize()));
    }
    return valid;
  }
}
