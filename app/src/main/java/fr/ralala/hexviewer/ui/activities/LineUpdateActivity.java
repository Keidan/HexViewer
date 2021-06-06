package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.LineUpdateTextWatcher;
import fr.ralala.hexviewer.utils.SysHelper;

public class LineUpdateActivity  extends AppCompatActivity {
  private static final String ACTIVITY_EXTRA_TEXT = "ACTIVITY_EXTRA_TEXT";
  private static final String ACTIVITY_EXTRA_POSITION = "ACTIVITY_EXTRA_POSITION";
  private static final String ACTIVITY_EXTRA_FILENAME = "ACTIVITY_EXTRA_FILENAME";
  public static final String RESULT_REFERENCE_STRING = "RESULT_REFERENCE_STRING";
  public static final String RESULT_NEW_STRING = "RESULT_NEW_STRING";
  public static final String RESULT_POSITION = "RESULT_POSITION";

  private ApplicationCtx mApp = null;
  private TextInputEditText mEtInputHex;
  private TextInputLayout mTilInputHex;
  private int mPosition = -1;
  private String mHex;
  private String mFile;

  /**
   * Starts an activity.
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   * @param text The text.
   * @param file The file name.
   * @param position The item position.
   */
  public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher, final String text, final String file, final int position) {
    Intent intent = new Intent(c, LineUpdateActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_TEXT, text);
    intent.putExtra(ACTIVITY_EXTRA_POSITION, position);
    intent.putExtra(ACTIVITY_EXTRA_FILENAME, file);
    activityResultLauncher.launch(intent);
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
    mApp = (ApplicationCtx) getApplication();

    TextView tvSource = findViewById(R.id.tvSource);
    TextView tvResult = findViewById(R.id.tvResult);
    mEtInputHex = findViewById(R.id.etInputHex);
    mTilInputHex = findViewById(R.id.tilInputHex);
    AppCompatCheckBox chkSmartInput = findViewById(R.id.chkSmartInput);


    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /* init */
    String text = null;
    if(getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      text = extras.getString(ACTIVITY_EXTRA_TEXT);
      mPosition = extras.getInt(ACTIVITY_EXTRA_POSITION);
      mFile = extras.getString(ACTIVITY_EXTRA_FILENAME);
    }
    if(text == null || text.equals("null") ) {
      text = "";
    }
    if(mFile != null) {
      String title = SysHelper.abbreviate(mFile,
          getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
              mApp.getAbbreviateLandscape() : mApp.getAbbreviatePortrait());
      setTitle(title);
    }
    if(mPosition == -1) {
      mPosition = 0;
    }
    final String[] split = SysHelper.extractHexAndSplit(text);
    mHex = split[0] + " " + split[1];
    text = mHex.replaceAll(" ", "");

    chkSmartInput.setChecked(mApp.isSmartInput());
    chkSmartInput.setOnCheckedChangeListener((comp, isChecked) -> mApp.setSmartInput(isChecked));

    tvSource.setText((split[0] + "\n" + split[1]));

    tvResult.setTextColor(ContextCompat.getColor(this, R.color.colorResultSuccess));
    tvResult.setText(SysHelper.hex2bin(text));

    mEtInputHex.setText(mHex.trim());
    mEtInputHex.addTextChangedListener(new LineUpdateTextWatcher(this, tvResult, mTilInputHex, mApp));

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
    int length = 0;
    if (mFile != null && !mFile.isEmpty()) {
      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        length = mApp.getAbbreviateLandscape();
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        length = mApp.getAbbreviatePortrait();
      }
      if (length != 0 && (mFile != null && mFile.equals("null"))) {
        String title = SysHelper.abbreviate(mFile,length);
        setTitle(title);
      }
    }
  }

  /**
   * Called when the options menu is clicked.
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
      if (!SysHelper.isValidHexLine(validate, false)) {
        mTilInputHex.setError(" "); /* only for the color */
        return false;
      }
      Intent i = new Intent();
      i.putExtra(RESULT_POSITION, mPosition);
      i.putExtra(RESULT_REFERENCE_STRING, mHex.replaceAll(" ", ""));
      i.putExtra(RESULT_NEW_STRING, validate);
      setResult(RESULT_OK, i);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
