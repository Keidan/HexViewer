package fr.ralala.hexviewer.entropy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p><b>Project HexViewer</b><br/>
 * Entropy map
 * </p>
 * Utility class that computes Shannon entropy levels over a stream of bytes and projects
 * them onto lines.
 *
 * <p>
 * The concatenation of all the raw lines is viewed as a single continuous byte stream, split
 * into consecutive, non-overlapping blocks of {@value #BLOCK_SIZE} bytes. For each full block,
 * the Shannon entropy (in bits per byte, in the range {@code 0..8}) is computed from the byte
 * histogram and quantized to a level in the range {@code 0..255}:
 * </p>
 * <ul>
 *   <li>{@code 0} means a block with no randomness at all (e.g. a single repeated byte);</li>
 *   <li>{@code 255} means a block with the maximum entropy (e.g. encrypted or compressed data).</li>
 * </ul>
 *
 * <p>
 * The block levels can then be mapped to lines with {@link #heatByLine(int[], byte[])}, which
 * produces one level per line, suitable for displaying an "entropy heat map".
 * </p>
 *
 * <p>
 * Levels are stored in {@code byte} arrays, so they must be read as unsigned values using
 * {@code level & 0xFF}.
 * </p>
 *
 * <p>
 * This class is not instantiable and all its methods are stateless, hence thread-safe.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * @see #computeLevels(List, AtomicBoolean)
 * @see #heatByLine(int[], byte[])
 */
public final class EntropyMap {
  /**
   * Size, in bytes, of a block on which an entropy level is computed.
   *
   * <p>
   * Since this value equals the number of distinct byte values, it is also the maximum count
   * that a single histogram bucket can hold.
   * </p>
   */
  public static final int BLOCK_SIZE = 256;

  /**
   * Highest quantized level (the levels range from {@code 0} to this value, inclusive).
   */
  private static final int MAX_LEVEL = 255;

  /**
   * Maximum entropy, in bits per byte, reached when the 256 possible byte values are
   * equiprobable (i.e. {@code log2(256)}).
   */
  private static final double MAX_BITS = 8.0;

  /**
   * Precomputed table of {@code c * log2(c)} for {@code c} in {@code 0..BLOCK_SIZE}.
   * Entry {@code 0} is {@code 0}, following the convention {@code 0 * log2(0) = 0}.
   */
  private static final double[] C_LOG_C = buildTable();

  /**
   * Not instantiable: this class only exposes static methods.
   */
  private EntropyMap() {
  }

  /**
   * Builds the lookup table of {@code c * log2(c)} used to speed up the entropy computation.
   *
   * @return an array of {@code BLOCK_SIZE + 1} values, where the element at index {@code c} is
   * {@code c * log2(c)} (and {@code 0} for {@code c = 0})
   */
  private static double[] buildTable() {
    double[] table = new double[BLOCK_SIZE + 1];
    for (int c = 1; c <= BLOCK_SIZE; c++)
      table[c] = c * Math.log(c) / Math.log(2);
    return table;
  }

  /**
   * Computes the Shannon entropy, in bits per byte ({@code 0..8}), from the histogram of a
   * full block.
   *
   * <p>
   * The classic formula {@code H = -sum(p * log2(p))}, with {@code p = c / BLOCK_SIZE}, is
   * rewritten as {@code H = log2(BLOCK_SIZE) - (sum(c * log2(c))) / BLOCK_SIZE}, which allows
   * the use of the precomputed {@link #C_LOG_C} table and avoids any logarithm call at runtime.
   * Since {@code BLOCK_SIZE} is 256, {@code log2(BLOCK_SIZE)} is exactly {@link #MAX_BITS}.
   * </p>
   *
   * @param histogram the byte histogram of a full block, i.e. an array where each count is in
   *                  the range {@code 0..BLOCK_SIZE} and the sum of all the counts is exactly
   *                  {@value #BLOCK_SIZE}
   * @return the entropy of the block in bits per byte, in the range {@code 0..8}
   * @throws ArrayIndexOutOfBoundsException if a count is negative or greater than
   *                                        {@value #BLOCK_SIZE}
   */
  static double entropy(int[] histogram) {
    double sum = 0;
    for (int c : histogram)
      sum += C_LOG_C[c];
    return MAX_BITS - sum / BLOCK_SIZE;
  }

  /**
   * Quantizes an entropy value to a level in the range {@code 0..255}.
   *
   * <p>
   * The returned {@code byte} is signed in Java: values above 127 appear as negative numbers
   * and must be read with {@code & 0xFF} to recover the unsigned level.
   * </p>
   *
   * @param bits the entropy in bits per byte, expected in the range {@code 0..8}
   * @return the quantized level, to be read as an unsigned value with {@code & 0xFF}
   */
  static byte toLevel(double bits) {
    return (byte) Math.round(bits / MAX_BITS * MAX_LEVEL);
  }

  /**
   * Incrementally accumulates bytes into blocks of {@link #BLOCK_SIZE} bytes and records the
   * entropy level of each completed block.
   *
   * <p>
   * Bytes can be fed from several arrays in a row: a block may span multiple arrays, as the
   * accumulator does not reset between calls to {@link #addAll(byte[])}. A trailing partial
   * block is never flushed and is therefore ignored.
   * </p>
   *
   * <p>
   * This class is not thread-safe.
   * </p>
   */
  private static final class Accumulator {

    /**
     * Histogram of the byte values of the block being filled (one bucket per value, indexed
     * by the unsigned byte value).
     */
    private final int[] mHistogram = new int[256];

    /**
     * Quantized levels of the completed blocks, in stream order. Its length is the expected
     * number of full blocks.
     */
    private final byte[] mLevels;

    /**
     * Number of bytes accumulated so far in the current block.
     */
    private int mCount;

    /**
     * Index of the next block to complete, i.e. the next free slot in {@link #mLevels}.
     */
    private int mBlock;

    /**
     * Creates an accumulator able to hold the given number of blocks.
     *
     * @param nbBlocks the number of full blocks that will be completed; feeding more bytes
     *                 than {@code nbBlocks * BLOCK_SIZE} results in an
     *                 {@link ArrayIndexOutOfBoundsException}
     */
    Accumulator(int nbBlocks) {
      mLevels = new byte[nbBlocks];
    }

    /**
     * Adds all the bytes of the given array to the stream.
     *
     * @param raw the bytes to add; {@code null} is accepted and ignored
     */
    void addAll(byte[] raw) {
      if (raw != null)
        for (byte b : raw)
          add(b);
    }

    /**
     * Adds a single byte to the current block and completes the block when it is full.
     *
     * @param b the byte to add
     */
    private void add(byte b) {
      mHistogram[b & 0xFF]++;
      if (++mCount == BLOCK_SIZE)
        flush();
    }

    /**
     * Completes the current block: stores its entropy level, then resets the histogram and
     * the byte counter to start a new block.
     */
    private void flush() {
      mLevels[mBlock++] = toLevel(entropy(mHistogram));
      Arrays.fill(mHistogram, 0);
      mCount = 0;
    }
  }

  /**
   * Computes the entropy level of every full block of the byte stream formed by the
   * concatenation of the given arrays.
   *
   * <p>
   * The arrays are processed in order and treated as one continuous stream, so a block may
   * span several consecutive arrays. {@code null} elements are treated as empty arrays.
   * The last partial block (fewer than {@value #BLOCK_SIZE} bytes), if any, is ignored.
   * </p>
   *
   * <p>
   * The cancellation flag is checked before processing each array (not within an array), so
   * cancellation is honored with the granularity of one element of {@code raws}. It is also
   * checked once more at the end, so a cancellation raised during the computation always
   * results in {@code null}.
   * </p>
   *
   * @param raws   the raw bytes of each line, in order (elements may be {@code null})
   * @param cancel the cancellation flag; when it becomes {@code true}, the computation stops
   *               as soon as possible
   * @return an array containing one level per full block (to be read as unsigned values with
   * {@code & 0xFF}), which may be empty if the stream is shorter than {@value #BLOCK_SIZE}
   * bytes, or {@code null} if the computation was cancelled
   */
  public static byte[] computeLevels(List<byte[]> raws, AtomicBoolean cancel) {
    long total = 0;
    for (byte[] r : raws)
      total += r == null ? 0 : r.length;
    Accumulator acc = new Accumulator((int) (total / BLOCK_SIZE));
    for (int i = 0; i < raws.size() && !cancel.get(); i++)
      acc.addAll(raws.get(i));
    return cancel.get() ? null : acc.mLevels;
  }

  /**
   * Projects the block levels onto the lines, producing one level per line.
   *
   * <p>
   * Each line is assigned the level of the block that contains its first byte, i.e. the block
   * at index {@code startOffset / BLOCK_SIZE}, where {@code startOffset} is the sum of the
   * lengths of all the preceding lines. A line that starts beyond the last full block (in the
   * ignored trailing partial block) gets the level {@code 0}.
   * </p>
   *
   * @param lengths the length, in bytes, of each line, in order (use {@code 0} for lines
   *                without data)
   * @param levels  the block levels, as returned by {@link #computeLevels(List, AtomicBoolean)}
   * @return an array of the same length as {@code lengths}, holding the level of each line
   * (to be read as unsigned values with {@code & 0xFF})
   */
  public static byte[] heatByLine(int[] lengths, byte[] levels) {
    byte[] heat = new byte[lengths.length];
    long offset = 0;
    for (int i = 0; i < lengths.length; i++) {
      int block = (int) (offset / BLOCK_SIZE);
      heat[i] = block < levels.length ? levels[block] : 0;
      offset += lengths[i];
    }
    return heat;
  }
}