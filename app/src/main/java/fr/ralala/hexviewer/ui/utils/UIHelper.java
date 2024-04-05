package fr.ralala.hexviewer.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.utils.io.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * UI Helper functions
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class UIHelper {
  private UIHelper() {
  }

  /**
   * Show the progress dialog.
   */
  public static void showCircularProgressDialog(AlertDialog dialog) {
    dialog.show();
    Window window = dialog.getWindow();
    if (window != null) {
      window.setLayout(350, 350);
      View v = window.getDecorView();
      v.setBackgroundResource(R.drawable.rounded_border);
    }
  }

  /**
   * Displays a circular progress dialog.
   *
   * @param context The Android context.
   * @param cancel  The cancel event callback (if null the dialog is not cancelable).
   * @return AlertDialog
   */
  public static AlertDialog createCircularProgressDialog(Context context, DialogInterface.OnCancelListener cancel) {
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    final ViewGroup nullParent = null;
    View view = layoutInflater.inflate(R.layout.circular_progress, nullParent);
    AlertDialog progress = new AlertDialog.Builder(context).create();
    if (cancel != null) {
      progress.setOnCancelListener(cancel);
      progress.setCancelable(true);
    } else
      progress.setCancelable(false);
    progress.setView(view);
    return progress;
  }

  /**
   * Hides the soft keyboard.
   *
   * @param activity The Android activity.
   */
  public static void hideKeyboard(final Activity activity) {
    /* hide keyboard */
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm.isAcceptingText()) {
      //Find the currently focused view, so we can grab the correct window token from it.
      View view = activity.getCurrentFocus();
      //If no view currently has focus, create a new one, just so we can grab a window token from it
      if (view == null) {
        view = new View(activity);
      }
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  /**
   * Gets the number of chars by lines
   *
   * @param context   The Android context.
   * @param landscape UserConfigLandscape.
   * @param portrait  UserConfigPortrait.
   * @return The width.
   */
  public static int getMaxByLine(final Context context, final UserConfig landscape, final UserConfig portrait) {
    int width = getTextWidth(context, landscape, portrait);
    return width == 0 ? 70 : (getScreenWidth(context) / width) - 2;
  }

  /**
   * Gets the width of the screen.
   *
   * @param context The Android context.
   * @return The width.
   */
  public static int getScreenWidth(final Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    /* The current view has a padding of 1dp */
    return (int) (displayMetrics.widthPixels - getSize(context, TypedValue.COMPLEX_UNIT_DIP, 1.0f));
  }

  /**
   * Gets the width of the text according to the font size and family (monospace).
   *
   * @param context   The Android context.
   * @param landscape UserConfigLandscape.
   * @param portrait  UserConfigPortrait.
   * @return The width.
   */
  public static int getTextWidth(final Context context, final UserConfig landscape, final UserConfig portrait) {
    final Typeface monospace = Typeface.MONOSPACE;
    final String text = "a";
    float fontSize = 12.0f;
    /* Solution 1: We get the width of the text. */
    TextView tv = new TextView(context);
    tv.setText(text);
    tv.setTypeface(monospace);
    ApplicationCtx app = (ApplicationCtx) context.getApplicationContext();
    Configuration cfg = app.getConfiguration();
    if (landscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      fontSize = landscape.getFontSize();
    } else if (portrait != null) {
      fontSize = portrait.getFontSize();
    }
    tv.setTextSize(fontSize);
    tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    int width = tv.getMeasuredWidth();
    /* Solution 2: If we can't get the width, then we try another method (obviously less accurate) */
    if (width < 1) {
      Paint paint = new Paint();
      paint.setTypeface(monospace);
      float scaledSizeInPixels = getSize(context, TypedValue.COMPLEX_UNIT_SP, fontSize);
      paint.setTextSize(scaledSizeInPixels);
      Rect bounds = new Rect();
      paint.getTextBounds(text, 0, text.length(), bounds);
      width = bounds.width();
    }
    return width;
  }

  /**
   * Gets the size according to the display metrics.
   *
   * @param context The Android context.
   * @param unit    The unit to convert from.
   * @param value   The value to apply the unit to.
   * @return The new value.
   */
  private static float getSize(final Context context, int unit, float value) {
    return TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
  }

  /**
   * Calculates the current line number from the file view.
   *
   * @param position    The position in the list adapter.
   * @param startOffset The start offset in the file (in case of partial opening).
   * @param maxByRow    Max bytes by row (see SysHelper.MAX_BY_ROW_xx).
   * @return The current actual line number.
   */
  public static long getCurrentLine(int position, long startOffset, int maxByRow) {
    final long so = startOffset == 0 ? 0 : (startOffset / maxByRow);
    return (so + position) * maxByRow;
  }

  /**
   * Displays an error dialog.
   *
   * @param context The Android context.
   * @param title   The dialog title.
   * @param message The dialog message.
   */
  public static void showErrorDialog(final Context context, CharSequence title, String message) {
    new AlertDialog.Builder(context)
      .setCancelable(false)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> dialog.dismiss()).show();
  }

  /**
   * Displays an error dialog.
   *
   * @param context The Android context.
   * @param title   The dialog title.
   * @param message The dialog message.
   */
  public static void showErrorDialog(final Context context, int title, String message) {
    showErrorDialog(context, context.getString(title), message);
  }

  /**
   * Displays an error dialog.
   *
   * @param context The Android context.
   * @param title   The dialog title.
   * @param message The dialog message.
   */
  public static void showErrorDialog(final Context context, int title, int message) {
    showErrorDialog(context, context.getString(title), context.getString(message));
  }

  /**
   * Returns the view associated with a position
   *
   * @param position The position.
   * @param listView The list view
   * @return View
   */
  public static View getViewByPosition(int position, ListView listView) {
    final int count = listView.getAdapter().getCount() - 1;
    int pos = Math.max(0, Math.min(position, count));
    final int firstListItemPosition = listView.getFirstVisiblePosition();
    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
    if (pos < firstListItemPosition || pos > lastListItemPosition) {
      return listView.getAdapter().getView(pos, null, listView);
    } else {
      final int childIndex = Math.max(0, Math.min(pos - firstListItemPosition, count));
      return listView.getChildAt(childIndex);
    }
  }

  /**
   * Sets the activity title.
   *
   * @param activity Activity.
   * @param filename The file name.
   * @param change   A change is detected?
   */
  public static void setTitle(final Activity activity, final String filename, final boolean change) {
    String title = "";
    if (filename == null)
      title = activity.getString(R.string.app_name);
    else {
      if (change)
        title += "*";
      title += filename;
    }
    activity.setTitle(title);
  }

  /**
   * Opens the file picker in directory selection mode.
   *
   * @param activityResultLauncher Activity Result Launcher.
   */
  public static void openFilePickerInDirectorSelectionMode(final ActivityResultLauncher<Intent> activityResultLauncher) {
    activityResultLauncher.launch(FileHelper.prepareForOpenDirectory());
  }

  /**
   * Opens the file picker in file selection mode.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   * @param snackBarLayout         Layout used to attach the snack bar.
   */
  public static void openFilePickerInFileSelectionMode(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher, final View snackBarLayout) {
    try {
      activityResultLauncher.launch(
        Intent.createChooser(FileHelper.prepareForOpenFile(), c.getString(R.string.select_file_to_open)));
    } catch (android.content.ActivityNotFoundException ex) {
      Snackbar customSnackBar = Snackbar.make(snackBarLayout, c.getString(R.string.error_no_file_manager), BaseTransientBottomBar.LENGTH_LONG);
      customSnackBar.setAction(c.getString(R.string.install), v -> {
        final String search = c.getString(R.string.file_manager_keyword);
        try {
          c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + search + "&c=apps")));
        } catch (android.content.ActivityNotFoundException ignore) {
          c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + search + "&c=apps")));
        }
      });
      customSnackBar.show();
    }
  }

  /**
   * Shake a view on error.
   *
   * @param owner   The owner view.
   * @param errText The error text.
   */
  public static void shakeError(EditText owner, String errText) {
    TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
    shake.setDuration(500);
    shake.setInterpolator(new CycleInterpolator(5));
    if (owner != null) {
      if (errText != null)
        owner.setError(errText);
      owner.clearAnimation();
      owner.startAnimation(shake);
    }
  }

  /**
   * Displays a confirm dialog.
   *
   * @param c       The Android context.
   * @param title   The dialog title.
   * @param message The dialog message.
   * @param yes     Listener used when the 'yes' button is clicked.
   */
  public static void showConfirmDialog(final Context c, String title,
                                       String message, final View.OnClickListener yes) {
    new AlertDialog.Builder(c)
      .setCancelable(false)
      .setIcon(R.mipmap.ic_launcher_round)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
        if (yes != null) yes.onClick(null);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
      }).show();
  }

  /**
   * Displays a toast.
   *
   * @param c       The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final String message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
    toast.show();
  }


  /**
   * Returns a TextWatcher that simply resets the layout error as soon as the user enters a text.
   *
   * @param layout       TextInputLayout
   * @param errorIfEmpty Activates the color change in error if the text is empty.
   * @return TextWatcher
   */
  public static TextWatcher getResetLayoutWatcher(final TextInputLayout layout, final boolean errorIfEmpty) {
    return new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // nothing to do
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (errorIfEmpty && s.length() == 0)
          layout.setError(" "); /* only for the color */
        else
          layout.setError(null);
      }

      @Override
      public void afterTextChanged(Editable s) {
        // nothing to do
      }
    };
  }

  /**
   * Display a confirmation message when the file is modified. A backup will automatically be made.
   *
   * @param c            The Android context.
   * @param fd           FileData.
   * @param runnable     The action to be taken if the user validates or not.
   * @param runnableSave To call TaskSave.
   */
  public static void confirmFileChanged(final Context c, final FileData fd, final Runnable runnable, final Runnable runnableSave) {
    if (FileData.isEmpty(fd)) {
      runnable.run();
      return;
    }
    new AlertDialog.Builder(c)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setTitle(R.string.action_close_title)
      .setMessage(String.format(c.getString(R.string.confirm_save), fd.getName()))
      .setPositiveButton(R.string.yes, (dialog, which) -> {
        runnableSave.run();
        dialog.dismiss();
      })
      .setNegativeButton(R.string.no, (dialog, which) -> {
        runnable.run();
        dialog.dismiss();
      })
      .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
      .show();
  }
}
