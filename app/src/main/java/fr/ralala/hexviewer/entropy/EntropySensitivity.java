package fr.ralala.hexviewer.entropy;

/**
 * <p><b>Project HexViewer</b><br/>
 * Entropy map
 * </p>
 * Defines the sensitivity levels used when evaluating entropy.
 *
 * <p>Each level is identified by a stable string key (used for persistence or
 * configuration) and carries two integer values that tune how entropy is
 * interpreted:
 * <ul>
 *   <li>a <em>threshold</em>, the value at which the evaluation is triggered
 *       or considered significant;</li>
 *   <li>a <em>full</em> value, the value at which the evaluation is considered
 *       fully saturated or at its maximum.</li>
 * </ul>
 *
 * <p>Levels are ordered from the most demanding to the most permissive in terms
 * of threshold: {@link #STRICT} has the highest threshold, {@link #HIGH} the
 * lowest, and {@link #NORMAL} sits in between.
 *
 * <p>Use {@link #fromKey(String)} to safely convert a stored key back into a
 * constant; it never fails and falls back to {@link #NORMAL} on unknown input.
 *
 * <p>This enum is immutable and thread-safe.
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * @see #fromKey(String)
 */
public enum EntropySensitivity {

  /**
   * Strict sensitivity ; Purely random (only), no false positives.
   *
   * <p>Key: {@code "strict"}, threshold: {@code 220}, full: {@code 234}.
   * Uses the highest threshold of all levels.
   */
  STRICT("strict", 220, 234),

  /**
   * Normal (default) sensitivity ; The measured setting.
   *
   * <p>Key: {@code "normal"}, threshold: {@code 210}, full: {@code 232}.
   * This is the fallback level returned by {@link #fromKey(String)} when the
   * key is unknown.
   * The measured setting
   */
  NORMAL("normal", 210, 232),

  /**
   * High sensitivity ; also detects “loose” compressed or encrypted data, with a few false positives (tables, dense code).
   *
   * <p>Key: {@code "high"}, threshold: {@code 195}, full: {@code 228}.
   * Uses the lowest threshold of all levels.
   */
  HIGH("high", 195, 228);

  /**
   * Stable string identifier of this level, used for lookup and persistence.
   */
  private final String mKey;

  /**
   * Value at which the entropy evaluation is triggered or considered significant.
   */
  private final int mThreshold;

  /**
   * Value at which the entropy evaluation is considered fully saturated.
   */
  private final int mFull;

  /**
   * Creates a sensitivity level.
   *
   * @param key       the stable string identifier of this level; must be unique
   *                  among all constants of this enum
   * @param threshold the entropy threshold associated with this level
   * @param full      the "full" entropy value associated with this level
   */
  EntropySensitivity(String key, int threshold, int full) {
    mKey = key;
    mThreshold = threshold;
    mFull = full;
  }

  /**
   * Returns the entropy threshold of this sensitivity level.
   *
   * @return the threshold value ({@code 220} for {@link #STRICT},
   * {@code 210} for {@link #NORMAL}, {@code 195} for {@link #HIGH})
   */
  public int getThreshold() {
    return mThreshold;
  }

  /**
   * Returns the "full" entropy value of this sensitivity level.
   *
   * @return the full value ({@code 234} for {@link #STRICT},
   * {@code 232} for {@link #NORMAL}, {@code 228} for {@link #HIGH})
   */
  public int getFull() {
    return mFull;
  }

  /**
   * Resolves a sensitivity level from its string key.
   *
   * <p>The comparison is case-sensitive and based on the exact key of each
   * constant ({@code "strict"}, {@code "normal"} or {@code "high"}). If no
   * constant matches, including when {@code key} is {@code null}, empty or
   * corrupted, this method does not throw and returns {@link #NORMAL} instead.
   *
   * @param key the key to look up; may be {@code null}
   * @return the matching {@code EntropySensitivity}, or {@link #NORMAL} if the
   * key is unknown or invalid
   */
  public static EntropySensitivity fromKey(String key) {
    for (EntropySensitivity s : values())
      if (s.mKey.equals(key))
        return s;
    return NORMAL;                 // unknown or corrupted value
  }
}