package fr.ralala.hexviewer.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import fr.ralala.hexviewer.R;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * UI Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class UIHelper {

  /**
   * Shake a view on error.
   * @param owner The owner view.
   * @param errText The error text.
   */
  public static void shakeError(TextView owner, String errText) {
    TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
    shake.setDuration(500);
    shake.setInterpolator(new CycleInterpolator(5));
    if(owner != null) {
      if(errText != null)
        owner.setError(errText);
      owner.clearAnimation();
      owner.startAnimation(shake);
    }
  }

  /**
   * Starts open transition.
   * @param a Activity.
   */
  public static void openTransition(Activity a) {
    a.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  /**
   * Starts close transition.
   * @param a Activity.
   */
  public static void closeTransition(Activity a) {
    a.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
  }
  /**
   * Displays a progress dialog.
   * @param context The Android context.
   * @param message The progress message.
   * @return AlertDialog
   */
  public static AlertDialog showProgressDialog(Context context, int message) {
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    final ViewGroup nullParent = null;
    View view = layoutInflater.inflate(R.layout.progress_dialog, nullParent);
    AlertDialog progress = new AlertDialog.Builder(context).create();
    TextView tv = view.findViewById(R.id.text);
    tv.setText(message);
    progress.setCancelable(false);
    progress.setView(view);
    return progress;
  }


  /**
   * Displays a confirm dialog.
   * @param c The Android context.
   * @param title The dialog title.
   * @param message The dialog message.
   * @param yes Listener used when the 'yes' button is clicked.
   */
  public static void showConfirmDialog(final Context c, String title,
                                       String message, final android.view.View.OnClickListener yes) {
    new AlertDialog.Builder(c)
        .setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
          if(yes != null) yes.onClick(null);
        })
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        }).show();
  }

  /**
   * Displays a toast.
   * @param c The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final String message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
    TextView tv = toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = ContextCompat.getDrawable(c, R.mipmap.ic_launcher);
      if(drawable != null) {
        final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
        tv.setCompoundDrawablePadding(5);
      }
    }
    toast.show();
  }

  /**
   * Displays a long toast.
   * @param c The Android context.
   * @param message The toast message.
   */
  public static void toastLong(final Context c, final int message) {
    toast(c, c.getString(message));
  }
}
