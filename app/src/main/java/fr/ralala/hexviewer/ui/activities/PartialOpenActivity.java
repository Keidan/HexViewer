package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Partial open activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PartialOpenActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TextWatcher {
  public static final String RESULT_START_OFFSET = "startOffset";
  public static final String RESULT_END_OFFSET = "endOffset";
  private static final String ACTIVITY_EXTRA_IS_SEQUENTIAL = "ACTIVITY_EXTRA_IS_SEQUENTIAL";
  private static final String ACTIVITY_EXTRA_REAL_SIZE = "ACTIVITY_EXTRA_REAL_SIZE";
  private static final String ACTIVITY_EXTRA_SIZE = "ACTIVITY_EXTRA_SIZE";
  private static final String ACTIVITY_EXTRA_START_OFFSET = "ACTIVITY_EXTRA_START_OFFSET";
  private static final String ACTIVITY_EXTRA_END_OFFSET = "ACTIVITY_EXTRA_END_OFFSET";
  private static final int IDX_DECIMAL = 0;
  private static final int IDX_HEXADECIMAL = 1;
  private static final int IDX_BYTES = 0;
  private static final int IDX_K_BYTES = 1;
  private static final int IDX_M_BYTES = 2;
  private static final int IDX_G_BYTES = 3;
  private static final int ERROR_START = 1;
  private static final int ERROR_END = 2;
  private TextView mTextSizePart;
  private AppCompatSpinner mSpUnit;
  private AppCompatSpinner mSpInputType;
  private TextInputLayout mTilStart;
  private TextInputEditText mTietStart;
  private TextInputLayout mTilEnd;
  private TextInputEditText mTietEnd;
  private boolean mCurrentIsHex = false;
  private boolean mIgnoreInputEvent = true;
  private boolean mIgnore = false;
  private long mRealSize = 0L;
  private InputFilter[] mDefaultStartInputFiler = new InputFilter[0];
  private InputFilter[] mDefaultEndInputFiler = new InputFilter[0];
  /* https://stackoverflow.com/questions/10648449/how-do-i-set-a-edittext-to-the-input-of-only-hexadecimal-numbers/17355026 */
  private final InputFilter mInputFilterTextHex = (source, start, end, dest, dstart, dend) -> {
    Pattern pattern = Pattern.compile("^\\p{XDigit}+$");
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
        return "";
      }
      Matcher matcher = pattern.matcher(String.valueOf(source.charAt(i)));
      if (!matcher.matches()) {
        return "";
      }
      sb.append(source.charAt(i));
    }
    return sb.toString();
  };

  /**
   * Starts an activity.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   */
  public static void startActivity(final Context c,
                                   final ActivityResultLauncher<Intent> activityResultLauncher,
                                   FileData fd) {
    Intent intent = new Intent(c, PartialOpenActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_IS_SEQUENTIAL, fd.isSequential());
    intent.putExtra(ACTIVITY_EXTRA_REAL_SIZE, fd.getRealSize());
    intent.putExtra(ACTIVITY_EXTRA_SIZE, fd.getSize());
    intent.putExtra(ACTIVITY_EXTRA_START_OFFSET, fd.getStartOffset());
    intent.putExtra(ACTIVITY_EXTRA_END_OFFSET, fd.getEndOffset());

    ApplicationCtx.addLog(c, "PartialOpen",
      String.format(Locale.US, "Open file '%s', sequential: %b, rsize: %d, size: %d, start: %d, end: %d",
        fd.getName(), fd.isSequential(), fd.getRealSize(), fd.getSize(), fd.getStartOffset(), fd.getEndOffset()));
    activityResultLauncher.launch(intent);
  }

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(((ApplicationCtx) base.getApplicationContext()).onAttach(base));
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_partial_open);

    TextView textFileSize = findViewById(R.id.textSize);
    mTextSizePart = findViewById(R.id.textSizePart);
    mSpUnit = findViewById(R.id.spUnit);
    mSpInputType = findViewById(R.id.spInputType);
    mTilStart = findViewById(R.id.tilStart);
    TextInputEditText tietStart = findViewById(R.id.tietStart);
    mTilEnd = findViewById(R.id.tilEnd);
    TextInputEditText tietEnd = findViewById(R.id.tietEnd);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    boolean isSequential = false;
    long startOffset = 0L;
    long endOffset = 0L;
    long size = 0L;

    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      isSequential = extras.getBoolean(ACTIVITY_EXTRA_IS_SEQUENTIAL);
      startOffset = extras.getLong(ACTIVITY_EXTRA_START_OFFSET);
      endOffset = extras.getLong(ACTIVITY_EXTRA_END_OFFSET);
      size = extras.getLong(ACTIVITY_EXTRA_SIZE);
      mRealSize = extras.getLong(ACTIVITY_EXTRA_REAL_SIZE);
    }

    long max;
    long start;
    long end;
    long real = mRealSize;
    if (isSequential) {
      start = startOffset;
      end = endOffset;
      max = size;
    } else {
      start = 0;
      end = mRealSize;
      max = end;
    }
    if (mSpUnit != null) {
      List<String> units = new ArrayList<>();
      units.add(getString(R.string.unit_byte_full));
      units.add(getString(R.string.unit_kbyte));
      units.add(getString(R.string.unit_mbyte));
      units.add(getString(R.string.unit_gbyte));
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item,
        units);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpUnit.setSelection(IDX_BYTES);
      mSpUnit.setAdapter(adapter);
      mSpUnit.setOnItemSelectedListener(this);
    }
    if (mSpInputType != null) {
      List<String> base = new ArrayList<>();
      base.add(getString(R.string.decimal));
      base.add(getString(R.string.hexadecimal));
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item,
        base);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpInputType.setSelection(IDX_DECIMAL);
      mSpInputType.setAdapter(adapter);
      mSpInputType.setOnItemSelectedListener(this);
    }

    if (tietStart != null) {
      tietStart.setText(String.valueOf(start));
      if (mTietStart != null)
        mTietStart.removeTextChangedListener(this);
      tietStart.addTextChangedListener(this);
      mTietStart = tietStart;
      mDefaultStartInputFiler = mTietStart.getFilters();
    }
    if (tietEnd != null) {
      tietEnd.setText(String.valueOf(end));
      if (mTietEnd != null)
        mTietEnd.removeTextChangedListener(this);
      tietEnd.addTextChangedListener(this);
      mTietEnd = tietEnd;
      mDefaultEndInputFiler = mTietEnd.getFilters();
    }
    if (textFileSize != null)
      textFileSize.setText(SysHelper.sizeToHuman(this, real));
    if (mTextSizePart != null)
      mTextSizePart.setText(SysHelper.sizeToHuman(this, max));
    evaluateSize();
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    ((ApplicationCtx) getApplicationContext()).setConfiguration(newConfig);
  }

  /**
   * Called when the options menu is clicked.
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.partial_open, menu);
    return true;
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home || item.getItemId() == R.id.action_cancel) {
      setResult(RESULT_CANCELED);
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_done && checkValues()) {
      UIHelper.hideKeyboard(this);
      Intent i = new Intent();
      long start = getValue(Objects.requireNonNull(mTietStart.getText()).toString(), null);
      long end = getValue(Objects.requireNonNull(mTietEnd.getText()).toString(), null);
      if (((ApplicationCtx) getApplicationContext()).isPartialOpenButWholeFileIsOpened() && start == 0L && end == mRealSize)
        end = 0L;
      i.putExtra(RESULT_START_OFFSET, start);
      i.putExtra(RESULT_END_OFFSET, end);
      setResult(RESULT_OK, i);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  /**
   * Evaluates the size.
   */
  private void evaluateSize() {
    mTextSizePart.setText("0");
    if (!checkValues())
      return;
    String sStart = Objects.requireNonNull(mTietStart.getText()).toString();
    String sEnd = Objects.requireNonNull(mTietEnd.getText()).toString();
    long start = getValue(sStart, mTietStart);
    long end = getValue(sEnd, mTietEnd);
    long size = Math.abs(end - start);
    String sSize = SysHelper.sizeToHuman(this, size);
    sSize += "\n(" + Long.toHexString(size).toUpperCase() + ")";
    mTextSizePart.setText(sSize);
  }

  /**
   * Checks the values.
   *
   * @return False on error.
   */
  private boolean checkValues() {
    String sStart = mTietStart == null || mTietStart.getText() == null ? "" : mTietStart.getText().toString();
    String sEnd = mTietEnd == null || mTietEnd.getText() == null ? "" : mTietEnd.getText().toString();
    boolean valid = checkEmpty(sStart, sEnd);
    if (valid) {
      long start = getValue(sStart, null);
      long end = getValue(sEnd, null);
      int ret = checkForSize(start, end);
      if (ret == 0) {
        valid = checkValuesValidSize(start, end);
      } else {
        valid = false;
        checkValuesInValidSize(ret);
      }
    }
    return valid;
  }

  /**
   * Checks the values (if checkForSize returns ERROR_START and/or ERROR_END).
   *
   * @param ret int
   */
  private void checkValuesInValidSize(int ret) {
    if ((ret & ERROR_START) != ERROR_START)
      setErrorMessage(mTilStart, null);
    if ((ret & ERROR_END) != ERROR_END)
      setErrorMessage(mTilEnd, null);
  }

  /**
   * Checks the values (if checkForSize returns 0).
   *
   * @param start long
   * @param end   long
   * @return false on error.
   */
  private boolean checkValuesValidSize(long start, long end) {
    boolean valid = true;
    if (start == -1 || end == -1) {
      valid = false;
    } else if (end <= start) {
      valid = false;
      setErrorMessage(mTilStart, R.string.error_less_than, end);
      setErrorMessage(mTilEnd, R.string.error_less_than, start);
    } else {
      setErrorMessage(mTilStart, null);
      setErrorMessage(mTilEnd, null);
    }
    return valid;
  }

  /**
   * Gets the value according to the unit.
   *
   * @param sValue The string value.
   * @param edit   The EditText.
   * @return The long value.
   */
  private long getValue(String sValue, EditText edit) {
    String val = sValue;
    if (mSpInputType.getSelectedItemId() == IDX_HEXADECIMAL) {
      try {
        val = String.valueOf(Long.parseLong(val, 16));
      } catch (Exception e) {
        return -1;
      }
    }
    return convert(val, edit);
  }

  /**
   * Convert the value according to the unit.
   *
   * @param val  The string value.
   * @param edit The EditText.
   * @return The long value.
   */
  private long convert(final String val, final EditText edit) {
    if (mSpUnit.getSelectedItemId() == IDX_K_BYTES) {
      try {
        return (long) (Float.parseFloat(val) * SysHelper.SIZE_1KB);
      } catch (Exception e) {
        return -1;
      }
    } else if (mSpUnit.getSelectedItemId() == IDX_M_BYTES) {
      try {
        return (long) (Float.parseFloat(val) * SysHelper.SIZE_1MB);
      } catch (Exception e) {
        return -1;
      }
    } else if (mSpUnit.getSelectedItemId() == IDX_G_BYTES) {
      try {
        return (long) (Float.parseFloat(val) * SysHelper.SIZE_1GB);
      } catch (Exception e) {
        return -1;
      }
    } else {
      return convertBytes(val, edit);
    }
  }

  /**
   * Convert the value in byte.
   *
   * @param val  The string value.
   * @param edit The EditText.
   * @return The long value.
   */
  private long convertBytes(final String val, final EditText edit) {
    try {
      String copy = val;
      int idx;
      if ((idx = copy.indexOf('.')) != -1) {
        copy = copy.substring(0, idx);
        if (edit != null) {
          edit.setText(copy);
          edit.setSelection(copy.length());
        }
      }
      if ((idx = copy.indexOf(',')) != -1) {
        copy = copy.substring(0, idx);
        if (edit != null) {
          edit.setText(copy);
          edit.setSelection(copy.length());
        }
      }
      return Long.parseLong(copy);
    } catch (Exception e) {
      return -1;
    }
  }

  /**
   * Converts the value according to the unit.
   *
   * @param val The string value.
   * @return The String value.
   */
  private String convertValueTo(String val) {
    if (mCurrentIsHex) {
      /* hex to long */
      try {
        return String.valueOf(Long.parseLong(val, 16));
      } catch (Exception ex) {
        Log.e(getClass().getSimpleName(), ex.getMessage(), ex);
      }
    } else {
      /* long to hex */
      long value = -1;
      if (val.indexOf('.') == -1)
        try {
          value = Long.parseLong(val);
        } catch (Exception ex) {
          Log.e(getClass().getSimpleName(), ex.getMessage(), ex);
        }
      else
        value = convert(val, null);
      if (value != -1)
        try {
          return Long.toHexString(Long.parseLong(val)).toUpperCase();
        } catch (Exception ex) {
          Log.e(getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }
    return "";
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
    if (parent.equals(mSpUnit)) {
      evaluateSize();
      changeInputType();
    } else if (parent.equals(mSpInputType)) {
      if (!mIgnoreInputEvent) {
        updateText(mTietStart);
        updateText(mTietEnd);
        mCurrentIsHex = !mCurrentIsHex;
      } else
        mIgnoreInputEvent = false;
      evaluateSize();
      changeInputType();
    }
  }

  /**
   * Sets the text according to the converted value.
   *
   * @param output TextInputEditText
   */
  private void updateText(TextInputEditText output) {
    if (output != null) {
      String s = convertValueTo(Objects.requireNonNull(output.getText()).toString());
      if (!s.isEmpty())
        output.setText(s);
    }
  }

  private void changeInputType() {
    if (mSpInputType.getSelectedItemId() == IDX_DECIMAL) {
      mTietStart.setFilters(mDefaultStartInputFiler);
      mTietEnd.setFilters(mDefaultEndInputFiler);
      mTietStart.setInputType(InputType.TYPE_CLASS_NUMBER);
      mTietEnd.setInputType(InputType.TYPE_CLASS_NUMBER);
      mTietStart.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
      mTietEnd.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
    } else {
      mTietStart.setFilters(new InputFilter[]{mInputFilterTextHex});
      mTietStart.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
      mTietEnd.setFilters(new InputFilter[]{mInputFilterTextHex});
      mTietEnd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
    mTietStart.setTypeface(Typeface.MONOSPACE);
    mTietEnd.setTypeface(Typeface.MONOSPACE);
    if (SysHelper.isRTL(this)) {
      mTietStart.setTextDirection(View.TEXT_DIRECTION_RTL);
      mTietEnd.setTextDirection(View.TEXT_DIRECTION_RTL);
    } else {
      mTietStart.setTextDirection(View.TEXT_DIRECTION_LTR);
      mTietEnd.setTextDirection(View.TEXT_DIRECTION_LTR);
    }
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
    if (mIgnore)
      return;
    mIgnore = true; // prevent infinite loop
    evaluateSize();
    mIgnore = false; // release, so the TextWatcher start to listen again.
  }

  /**
   * Checks if the fields are empty or not.
   *
   * @param start String value for the start field.
   * @param end   String value for the end field.
   * @return boolean
   */
  private boolean checkEmpty(String start, String end) {
    boolean valid = true;
    if (start.isEmpty()) {
      setErrorMessage(mTilStart, getString(R.string.error_less_than) + " 0");
      valid = false;
    }
    if (end.isEmpty()) {
      setErrorMessage(mTilEnd, getString(R.string.error_less_than) + " 0");
      valid = false;
    }
    return valid;
  }


  /**
   * Checks if the fields are not larger than the file size.
   *
   * @param start Long value for the start field.
   * @param end   Long value for the end field.
   * @return 0 on success, ERROR_START or ERROR_END otherwise
   */
  private int checkForSize(long start, long end) {
    int valid = 0;
    if (start > mRealSize) {
      valid |= ERROR_START;
      setErrorMessage(mTilStart, R.string.error_greater_than, mRealSize);
    } else if (start < 0) {
      valid |= ERROR_START;
      setErrorMessage(mTilStart, getString(R.string.error_invalid_value));
    }

    if (end > mRealSize) {
      valid |= ERROR_END;
      setErrorMessage(mTilEnd, R.string.error_greater_than, mRealSize);
    } else if (end < 0) {
      valid |= ERROR_END;
      setErrorMessage(mTilEnd, getString(R.string.error_invalid_value));
    }
    return valid;
  }

  private void setErrorMessage(TextInputLayout til, @StringRes int textId, long size) {
    setErrorMessage(til, getString(textId) + " " +
      SysHelper.sizeToHuman(this, size) + " (" + Long.toHexString(size).toUpperCase() + ")");
  }

  private void setErrorMessage(TextInputLayout til, String message) {
    if (til != null)
      til.setError(message);
  }
}
