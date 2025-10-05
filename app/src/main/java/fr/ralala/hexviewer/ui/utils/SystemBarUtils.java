package fr.ralala.hexviewer.ui.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Utility class to handle status bar and navigation bar appearance.
 * <p>
 * Supports light/dark mode for status bar and navigation bar icons, with proper
 * fallback for different API levels.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SystemBarUtils {
  private SystemBarUtils() {

  }

  /**
   * Sets the status bar and navigation bar colors and icons according to the theme mode.
   *
   * @param activity  The current Activity
   * @param lightMode True for light mode (dark icons), false for dark mode (light icons)
   */
  public static void setNavAndStatusBarColor(Activity activity, boolean lightMode) {
    setStatusBarAppearance(activity, lightMode);
    setNavigationBarAppearance(activity, lightMode);
  }

  // -----------------------------
  // PRIVATE HELPERS
  // -----------------------------

  /**
   * Sets the status bar icons to light or dark mode.
   * <p>
   * - API 30+ uses WindowInsetsController for appearance.
   * - API 23–29 uses system UI visibility flags.
   *
   * @param activity  The current Activity
   * @param lightMode True for light mode (dark icons), false for dark mode (light icons)
   */
  @SuppressWarnings("squid:S1874")
  private static void setStatusBarAppearance(Activity activity, boolean lightMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowInsetsController controller = activity.getWindow().getInsetsController();
      if (controller != null) {
        int appearance = lightMode ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0;
        controller.setSystemBarsAppearance(appearance, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
      }
    } else {
      View decor = activity.getWindow().getDecorView();
      setSystemUiFlags(decor, lightMode);
    }
  }

  /**
   * Sets the navigation bar color and icons according to the API level.
   * <p>
   * - API 30+ uses WindowInsetsController for appearance.
   * - API 26–29 uses system UI visibility flags for dark icons.
   * - API 23–25 only sets the background color (icons remain white).
   *
   * @param activity  The current Activity
   * @param lightMode True for light mode (dark icons if supported), false for dark mode
   */
  private static void setNavigationBarAppearance(Activity activity, boolean lightMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowInsetsController controller = activity.getWindow().getInsetsController();
      if (controller != null) {
        int appearance = lightMode ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0;
        controller.setSystemBarsAppearance(appearance, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
      }
      setNavigationBarColor(activity, R.color.navigationBarColor);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      setNavigationBarColor(activity, R.color.navigationBarColor);
      setLightNavigationBarForO(activity, lightMode);
    } else {
      setNavigationBarColor(activity, R.color.navigationBarColor23);
    }
  }

  @SuppressWarnings({"deprecation", "RedundantSuppression", "squid:S1874"})
  private static void setNavigationBarColor(Activity activity, @ColorRes int id) {
    activity.getWindow().setNavigationBarColor(
      ContextCompat.getColor(activity, id)
    );
  }

  /**
   * Sets the status bar icons to light/dark for API 23–29 using system UI visibility flags.
   *
   * @param decor     The decor view of the current window
   * @param lightMode True for light mode (dark icons), false for dark mode
   */
  @SuppressWarnings({"deprecation", "RedundantSuppression", "squid:S1874"})
  private static void setSystemUiFlags(View decor, boolean lightMode) {
    int flags = decor.getSystemUiVisibility();
    if (lightMode) {
      flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    } else {
      flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    }
    decor.setSystemUiVisibility(flags);
  }

  /**
   * Sets the navigation bar icons for API 26–29 using system UI visibility flags.
   *
   * @param activity  The current Activity
   * @param lightMode True for light mode (dark icons), false for dark mode
   */
  @SuppressWarnings({"deprecation", "RedundantSuppression", "squid:S1874"})
  private static void setLightNavigationBarForO(Activity activity, boolean lightMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      View decor = activity.getWindow().getDecorView();
      int flags = decor.getSystemUiVisibility();
      if (lightMode) {
        flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
      } else {
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
      }
      decor.setSystemUiVisibility(flags);
    }
  }
}
