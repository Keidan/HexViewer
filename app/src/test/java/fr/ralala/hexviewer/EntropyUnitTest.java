package fr.ralala.hexviewer;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.entropy.EntropyMap;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
 * <p><b>Project HexViewer</b><br/>
 * Unit tests for the {@link EntropyMap} class.
 *
 * <p>This test class verifies the behavior of {@link EntropyMap} in various scenarios:
 * <ul>
 * <li>Exact levels for distributions whose Shannon entropy is known (0, 1, 2, 4 and 8 bits per byte).</li>
 * <li>Blocks that are independent of the way the data is split into lines.</li>
 * <li>Edge cases: no data, incomplete last block, lines without raw data, cancellation.</li>
 * <li>Projection of the block levels onto lines of fixed or variable length.</li>
 * <li>An end-to-end scenario with a random region hidden in a text.</li>
 * </ul>
 *
 * <p>The levels are quantized between 0 (0 bit per byte) and 255 (8 bits per byte) and stored in
 * a signed byte, so they are always read with {@code & 0xFF}.
 *
 * <p>Only the public API of {@link EntropyMap} is used. The lines are built with
 * {@link SysHelper#formatBuffer(byte[], AtomicBoolean, int, int)}, like the hexadecimal view does.
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 */
@RunWith(JUnit4.class)
public class EntropyUnitTest {

  /**
   * Size of an entropy block (in bytes).
   */
  private static final int BLOCK = EntropyMap.BLOCK_SIZE;

  /**
   * Highest level (8 bits per byte).
   */
  private static final int LEVEL_MAX = 255;

  /**
   * Level from which a block is highlighted (about 6.6 bits per byte).
   * Random data on 256 bytes gives about 7.2 bits (a level close to 229).
   */
  private static final int LEVEL_THRESHOLD = 210;

  /**
   * Seed of the pseudo-random generator (the tests must be reproducible).
   */
  private static final long SEED = 1234;

  /**
   * A sentence repeated to simulate readable text.
   */
  private static final String TEXT = "Hello hex viewer, this is some readable text. ";

  /**
   * Builds a block of {@link #BLOCK} bytes that contains {@code nbValues} different values
   * (from 0 to {@code nbValues - 1}), each of them appearing the same number of times.
   *
   * @param nbValues The number of different values (a power of two between 1 and 256).
   * @return The block.
   */
  private byte[] uniformBlock(int nbValues) {
    byte[] block = new byte[BLOCK];
    for (int i = 0; i < block.length; i++)
      block[i] = (byte) (i % nbValues);
    return block;
  }

  /**
   * Builds a deterministic pseudo-random buffer.
   *
   * @param size The buffer size.
   * @return The buffer.
   */
  private byte[] randomBytes(int size) {
    byte[] buffer = new byte[size];
    new Random(SEED).nextBytes(buffer);
    return buffer;
  }

  /**
   * Builds a buffer that looks like a text.
   *
   * @param size The buffer size.
   * @return The buffer.
   */
  private byte[] textBytes(int size) {
    byte[] pattern = TEXT.getBytes();
    byte[] buffer = new byte[size];
    for (int i = 0; i < size; i++)
      buffer[i] = pattern[i % pattern.length];
    return buffer;
  }

  /**
   * Concatenates several buffers.
   *
   * @param buffers The buffers.
   * @return The concatenation.
   */
  private byte[] concat(byte[]... buffers) {
    int total = 0;
    for (byte[] b : buffers)
      total += b.length;
    byte[] result = new byte[total];
    int offset = 0;
    for (byte[] b : buffers) {
      System.arraycopy(b, 0, result, offset, b.length);
      offset += b.length;
    }
    return result;
  }

  /**
   * Formats a buffer into lines (like the hexadecimal view) and returns the raw bytes of each line.
   *
   * @param data   The data.
   * @param maxRow Max number of bytes per line.
   * @return The raw bytes of each line.
   */
  private List<byte[]> toRaws(byte[] data, int maxRow) {
    List<byte[]> raws = new ArrayList<>();
    for (LineEntry le : SysHelper.formatBuffer(data, null, maxRow, 0))
      raws.add(le.getRaw());
    return raws;
  }

  /**
   * Returns the length of each line.
   *
   * @param raws The raw bytes of each line.
   * @return The lengths.
   */
  private int[] toLengths(List<byte[]> raws) {
    int[] lengths = new int[raws.size()];
    for (int i = 0; i < lengths.length; i++)
      lengths[i] = raws.get(i) == null ? 0 : raws.get(i).length;
    return lengths;
  }

  /**
   * Computes the levels of a buffer split into lines of 16 bytes.
   *
   * @param data The data.
   * @return The levels (never null).
   */
  private byte[] levelsOf(byte[] data) {
    byte[] levels = EntropyMap.computeLevels(toRaws(data, SysHelper.MAX_BY_ROW_16), new AtomicBoolean());
    assertNotNull(levels);
    return levels;
  }

  /**
   * Helper to assert the level of the only block of a buffer.
   *
   * @param expected The expected level (0..255).
   * @param block    The block (exactly {@link #BLOCK} bytes).
   */
  private void assertSingleLevel(int expected, byte[] block) {
    byte[] levels = levelsOf(block);
    assertEquals(1, levels.length);
    assertEquals(expected, levels[0] & 0xFF);
  }

  // ===========================
  // ======= TEST CASES ========
  // ===========================

  @Test
  public void testLevelConstantBlock() {
    // 1 value => 0 bit per byte
    assertSingleLevel(0, uniformBlock(1));
  }

  @Test
  public void testLevelTwoValues() {
    // 2 values => 1 bit per byte => 1/8 of 255 = 31.875
    assertSingleLevel(32, uniformBlock(2));
  }

  @Test
  public void testLevelFourValues() {
    // 4 values => 2 bits per byte => 2/8 of 255 = 63.75
    assertSingleLevel(64, uniformBlock(4));
  }

  @Test
  public void testLevelSixteenValues() {
    // 16 values => 4 bits per byte => half of the scale = 127.5
    assertSingleLevel(128, uniformBlock(16));
  }

  @Test
  public void testLevelAllValues() {
    // 256 values => 8 bits per byte => maximum (stored as -1 in a signed byte)
    byte[] levels = levelsOf(uniformBlock(256));
    assertEquals(1, levels.length);
    assertEquals((byte) -1, levels[0]);
    assertEquals(LEVEL_MAX, levels[0] & 0xFF);
  }

  @Test
  public void testComputeLevelsNoData() {
    byte[] levels = EntropyMap.computeLevels(Collections.emptyList(), new AtomicBoolean());
    assertNotNull(levels);
    assertEquals(0, levels.length);
  }

  @Test
  public void testComputeLevelsLessThanOneBlock() {
    // A block that is not complete does not produce any level
    assertEquals(0, levelsOf(randomBytes(BLOCK - 1)).length);
  }

  @Test
  public void testComputeLevelsIgnoresIncompleteLastBlock() {
    // The entropy of a few bytes is artificially low, so the last block is ignored
    assertEquals(4, levelsOf(randomBytes(4 * BLOCK + 100)).length);
  }

  @Test
  public void testComputeLevelsOneLevelPerBlock() {
    byte[] levels = levelsOf(concat(uniformBlock(1), uniformBlock(256), uniformBlock(2)));
    assertArrayEquals(new byte[]{0, (byte) LEVEL_MAX, 32}, levels);
  }

  @Test
  public void testComputeLevelsIgnoresNullLines() {
    // The lines without raw data do not shift the blocks
    List<byte[]> raws = new ArrayList<>();
    raws.add(null);
    raws.addAll(toRaws(uniformBlock(256), SysHelper.MAX_BY_ROW_16));
    raws.add(null);
    byte[] levels = EntropyMap.computeLevels(raws, new AtomicBoolean());
    assertNotNull(levels);
    assertEquals(1, levels.length);
    assertEquals(LEVEL_MAX, levels[0] & 0xFF);
  }

  @Test
  public void testComputeLevelsIndependentOfLineLength() {
    byte[] data = concat(textBytes(BLOCK), randomBytes(BLOCK), uniformBlock(256), uniformBlock(2));
    byte[] with8 = EntropyMap.computeLevels(toRaws(data, SysHelper.MAX_BY_ROW_8), new AtomicBoolean());
    byte[] with16 = EntropyMap.computeLevels(toRaws(data, SysHelper.MAX_BY_ROW_16), new AtomicBoolean());
    assertNotNull(with8);
    assertEquals(4, with8.length);
    assertArrayEquals(with8, with16);
  }

  @Test
  public void testComputeLevelsOneLineMadeOfSeveralBlocks() {
    // A line (an edited line, for example) may be longer than a block
    List<byte[]> raws = Collections.singletonList(concat(uniformBlock(1), uniformBlock(256)));
    byte[] levels = EntropyMap.computeLevels(raws, new AtomicBoolean());
    assertNotNull(levels);
    assertArrayEquals(new byte[]{0, (byte) LEVEL_MAX}, levels);
  }

  @Test
  public void testComputeLevelsRandomVersusText() {
    // 256 random bytes give about 7.2 bits (not 8), but stay above the threshold, a text stays below
    byte[] levels = levelsOf(concat(textBytes(BLOCK), randomBytes(BLOCK)));
    assertEquals(2, levels.length);
    assertFalse("The text must not be highlighted", (levels[0] & 0xFF) >= LEVEL_THRESHOLD);
    assertTrue("The random data must be highlighted", (levels[1] & 0xFF) >= LEVEL_THRESHOLD);
  }

  @Test
  public void testComputeLevelsCancelled() {
    // A cancelled computation returns null, the caller must ignore the result
    AtomicBoolean cancel = new AtomicBoolean(true);
    assertNull(EntropyMap.computeLevels(toRaws(randomBytes(4 * BLOCK), SysHelper.MAX_BY_ROW_16), cancel));
  }

  @Test
  public void testHeatByLineNoLine() {
    assertEquals(0, EntropyMap.heatByLine(new int[0], new byte[]{1, 2}).length);
  }

  @Test
  public void testHeatByLineNoLevel() {
    // File shorter than one block: no level, all the lines get 0
    assertArrayEquals(new byte[]{0, 0, 0}, EntropyMap.heatByLine(new int[]{16, 16, 16}, new byte[0]));
  }

  @Test
  public void testHeatByLineFixedLength() {
    // Each line takes the level of the block that contains its first byte
    byte[] heat = EntropyMap.heatByLine(new int[]{128, 128, 128, 128}, new byte[]{10, 20});
    assertArrayEquals(new byte[]{10, 10, 20, 20}, heat);
  }

  @Test
  public void testHeatByLineVariableLength() {
    // Offsets: 0, 100, 300, 310, 610 => blocks 0, 0, 1, 1, 2 (the last one does not exist => 0)
    byte[] heat = EntropyMap.heatByLine(new int[]{100, 200, 10, 300, 5}, new byte[]{1, 2});
    assertArrayEquals(new byte[]{1, 1, 2, 2, 0}, heat);
  }

  @Test
  public void testHeatByLineKeepsMaximumLevel() {
    // The maximum level (255) is stored as -1: it must not be changed by the projection
    byte[] heat = EntropyMap.heatByLine(new int[]{16, 16, 16}, levelsOf(uniformBlock(256)));
    for (byte h : heat)
      assertEquals(LEVEL_MAX, h & 0xFF);
  }

  @Test
  public void testRandomRegionInsideText() {
    // 1 KB of random data hidden in a text: only the lines of this region must be highlighted
    byte[] data = concat(textBytes(4 * BLOCK), randomBytes(4 * BLOCK), textBytes(4 * BLOCK));
    List<byte[]> raws = toRaws(data, SysHelper.MAX_BY_ROW_16);
    byte[] levels = EntropyMap.computeLevels(raws, new AtomicBoolean());
    assertNotNull(levels);
    byte[] heat = EntropyMap.heatByLine(toLengths(raws), levels);

    int linesPerBlock = BLOCK / SysHelper.MAX_BY_ROW_16;
    int firstHot = 4 * linesPerBlock;
    int lastHot = 8 * linesPerBlock - 1;
    assertEquals(12 * linesPerBlock, heat.length);
    for (int i = 0; i < heat.length; i++) {
      boolean hot = (heat[i] & 0xFF) >= LEVEL_THRESHOLD;
      assertEquals("Line " + i, i >= firstHot && i <= lastHot, hot);
    }
  }

  @Test
  public void testRandomRegionWithIncompleteLastLine() {
    // The last line is shorter (100 bytes = 6 lines of 16 bytes + 4 bytes): the projection must not fail
    byte[] data = concat(randomBytes(BLOCK), textBytes(100));
    List<byte[]> raws = toRaws(data, SysHelper.MAX_BY_ROW_16);
    byte[] levels = EntropyMap.computeLevels(raws, new AtomicBoolean());
    assertNotNull(levels);
    assertEquals(1, levels.length);
    byte[] heat = EntropyMap.heatByLine(toLengths(raws), levels);
    assertEquals(raws.size(), heat.length);
    assertTrue((heat[0] & 0xFF) >= LEVEL_THRESHOLD);
    assertEquals(0, heat[heat.length - 1]);   // beyond the last complete block
  }
}

