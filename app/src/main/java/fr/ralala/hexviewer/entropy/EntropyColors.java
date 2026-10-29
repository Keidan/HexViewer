package fr.ralala.hexviewer.entropy;

import androidx.annotation.ColorRes;

import java.util.HashMap;
import java.util.Map;

import fr.ralala.hexviewer.R;
/**
 * <p><b>Project HexViewer</b><br/>
 * Entropy colors
 * </p>
 * Utility class that maps entropy color keys to Android color resources.
 *
 * <p>This class holds a fixed, immutable-by-convention lookup table associating
 * human-readable color names (for example {@code "red"} or {@code "blue"}) with
 * the corresponding color resource identifiers declared in the application's
 * {@code R.color} resources. It is typically used to translate a color key coming
 * from configuration, a server response, or a persisted setting into a value that
 * can be passed to Android APIs such as
 * {@link androidx.core.content.ContextCompat#getColor(android.content.Context, int)}.
 *
 * <p>Supported keys are:
 * <ul>
 *   <li>{@code "red"} &rarr; {@code R.color.entropyRed}</li>
 *   <li>{@code "orange"} &rarr; {@code R.color.entropyOrange}</li>
 *   <li>{@code "green"} &rarr; {@code R.color.entropyGreen}</li>
 *   <li>{@code "blue"} &rarr; {@code R.color.entropyBlue}</li>
 * </ul>
 *
 * <p>Keys are matched exactly and are case-sensitive.
 *
 * <p>This class is not instantiable and cannot be subclassed. All members are static.
 *
 * <p><b>Thread safety:</b> the internal map is populated once in a static
 * initializer and never modified afterward, so concurrent calls to
 * {@link #resolve(String)} are safe.
 *
 * <p>Example usage:
 * <pre>{@code
 * @ColorRes int colorRes = EntropyColors.resolve("green");
 * if (colorRes != 0) {
 *     int color = ContextCompat.getColor(context, colorRes);
 *     textView.setTextColor(color);
 * }
 * }</pre>
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 */
public final class EntropyColors {

  /**
   * Lookup table from color key to Android color resource identifier.
   * Populated once in the static initializer and treated as read-only afterwards.
   */
  private static final Map<String, Integer> COLORS = new HashMap<>();

  static {
    COLORS.put("red", R.color.entropyRed);
    COLORS.put("orange", R.color.entropyOrange);
    COLORS.put("green", R.color.entropyGreen);
    COLORS.put("blue", R.color.entropyBlue);
    COLORS.put("magenta", R.color.entropyMagenta);
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private EntropyColors() {
  }

  /**
   * Resolves a color key to its corresponding Android color resource identifier.
   *
   * <p>The lookup is case-sensitive. The returned value is a resource ID, not an
   * actual color value; use {@code ContextCompat.getColor(Context, int)} or an
   * equivalent method to obtain the resolved color.
   *
   * <p>A return value of {@code 0} is never a valid resource ID in Android, so it
   * can safely be used as a "no color" sentinel.
   *
   * @param key the color key to resolve (for example {@code "red"}, {@code "orange"},
   *            {@code "green"} or {@code "blue"}); may be {@code null}
   * @return the matching {@link ColorRes color resource} identifier, or {@code 0}
   *         if the key is {@code null}, unknown, or the feature is disabled
   */
  public static @ColorRes int resolve(String key) {
    Integer res = COLORS.get(key);
    return res == null ? 0 : res;
  }
}
