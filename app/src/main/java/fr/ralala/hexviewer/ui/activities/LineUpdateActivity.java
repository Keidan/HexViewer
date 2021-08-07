package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.LineUpdateHexArrayAdapter;
import fr.ralala.hexviewer.ui.utils.LineUpdateTextWatcher;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * LineUpdate activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LineUpdateActivity extends AppCompatActivity {
  private static final String ACTIVITY_EXTRA_TEXTS = "ACTIVITY_EXTRA_TEXTS";
  private static final String ACTIVITY_EXTRA_POSITION = "ACTIVITY_EXTRA_POSITION";
  private static final String ACTIVITY_EXTRA_NB_LINES = "ACTIVITY_EXTRA_NB_LINES";
  private static final String ACTIVITY_EXTRA_FILENAME = "ACTIVITY_EXTRA_FILENAME";
  private static final String ACTIVITY_EXTRA_CHANGE = "ACTIVITY_EXTRA_CHANGE";
  public static final String RESULT_REFERENCE_STRING = "RESULT_REFERENCE_STRING";
  public static final String RESULT_NEW_STRING = "RESULT_NEW_STRING";
  public static final String RESULT_POSITION = "RESULT_POSITION";
  public static final String RESULT_NB_LINES = "RESULT_NB_LINES";

  private ApplicationCtx mApp = null;
  private TextInputEditText mEtInputHex;
  private TextInputLayout mTilInputHex;
  private int mPosition = -1;
  private int mNbLines = 0;
  private String mFile;
  private boolean mChange;
  private String mHex;

  /**
   * Starts an activity.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   * @param texts                  The texts.
   * @param file                   The file name.
   * @param position               The item position.
   * @param nbLines                The number of lines.
   * @param change                 A change is detected?
   */
  public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher,
                                   final byte[] texts, final String file,
                                   final int position,
                                   final int nbLines,
                                   final boolean change) {
    Intent intent = new Intent(c, LineUpdateActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_TEXTS, texts);
    intent.putExtra(ACTIVITY_EXTRA_POSITION, position);
    intent.putExtra(ACTIVITY_EXTRA_NB_LINES, nbLines);
    intent.putExtra(ACTIVITY_EXTRA_FILENAME, file);
    intent.putExtra(ACTIVITY_EXTRA_CHANGE, change);
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
    super.attachBaseContext(ApplicationCtx.getInstance().onAttach(base));
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_line_update);
    mApp = ApplicationCtx.getInstance();

    ListView lvSource = findViewById(R.id.lvSource);
    ListView lvResult = findViewById(R.id.lvResult);
    HexTextArrayAdapter.LineNumbersTitle titleSource = new HexTextArrayAdapter.LineNumbersTitle();
    titleSource.titleContent = findViewById(R.id.titleContentSource);
    titleSource.titleLineNumbers = findViewById(R.id.titleLineNumbersSource);
    HexTextArrayAdapter.LineNumbersTitle titleResult = new HexTextArrayAdapter.LineNumbersTitle();
    titleResult.titleContent = findViewById(R.id.titleContentResult);
    titleResult.titleLineNumbers = findViewById(R.id.titleLineNumbersResult);
    AppCompatCheckBox chkSmartInput = findViewById(R.id.chkSmartInput);
    AppCompatCheckBox chkOverwrite = findViewById(R.id.chkOverwrite);

    mEtInputHex = findViewById(R.id.etInputHex);
    mTilInputHex = findViewById(R.id.tilInputHex);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /* init */
    mChange = false;
    List<String> list = new ArrayList<>();
    StringBuilder sbHex = new StringBuilder();
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      List<LineData<Line>> li = SysHelper.formatBuffer(extras.getByteArray(ACTIVITY_EXTRA_TEXTS), null, SysHelper.MAX_BY_ROW_8);
      mPosition = extras.getInt(ACTIVITY_EXTRA_POSITION);
      mNbLines = extras.getInt(ACTIVITY_EXTRA_NB_LINES);
      mFile = extras.getString(ACTIVITY_EXTRA_FILENAME);
      mChange = extras.getBoolean(ACTIVITY_EXTRA_CHANGE);
      for (LineData<Line> ld : li) {
        String s = ld.toString();
        list.add(s);
        sbHex.append(s.substring(0, 23).trim()).append(" ");
      }
    }
    LineUpdateHexArrayAdapter adapterSource = new LineUpdateHexArrayAdapter(this, lvSource, titleSource, list);
    LineUpdateHexArrayAdapter adapterResult = new LineUpdateHexArrayAdapter(this, lvResult, titleResult, new ArrayList<>(list));
    lvSource.setAdapter(adapterSource);
    lvResult.setAdapter(adapterResult);

    chkSmartInput.setChecked(mApp.isSmartInput());
    chkSmartInput.setOnCheckedChangeListener((comp, isChecked) -> mApp.setSmartInput(isChecked));
    chkOverwrite.setChecked(mApp.isOverwrite());
    chkOverwrite.setOnCheckedChangeListener((comp, isChecked) -> mApp.setOverwrite(isChecked));


    if (mFile != null) {
      UIHelper.setTitle(this, getResources().getConfiguration().orientation, false, mFile, mChange);
    }
    if (mPosition == -1) {
      mPosition = 0;
    }
    mHex = sbHex.toString();
    if (mHex.endsWith(" "))
      mHex = mHex.substring(0, mHex.length() - 1);
    mEtInputHex.setText(mHex);
    mEtInputHex.addTextChangedListener(new LineUpdateTextWatcher(adapterResult, mTilInputHex, mApp));
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    // Checks the orientation of the screen
    if (mFile != null && !mFile.isEmpty()) {
      UIHelper.setTitle(this, newConfig.orientation, false, mFile, mChange);
    }
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
    inflater.inflate(R.menu.line_update, menu);
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
    if (item.getItemId() == android.R.id.home) {
      setResult(RESULT_CANCELED);
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_delete) {
      mEtInputHex.setText("");
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      final String validate = mEtInputHex.getText() == null ? "" : mEtInputHex.getText().toString().trim().replaceAll(" ", "").toLowerCase(Locale.US);
      if (!SysHelper.isValidHexLine(validate)) {
        mTilInputHex.setError(" "); /* only for the color */
        return false;
      }
      Intent i = new Intent();
      i.putExtra(RESULT_POSITION, mPosition);
      i.putExtra(RESULT_NB_LINES, mNbLines);
      i.putExtra(RESULT_REFERENCE_STRING, mHex.replaceAll(" ", ""));
      i.putExtra(RESULT_NEW_STRING, validate);
      setResult(RESULT_OK, i);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
