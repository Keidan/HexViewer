package fr.ralala.hexviewer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
 * <p><b>Project HexViewer</b><br/>
 * Unit tests for SysHelper.formatBuffer.
 *
 * <p>These tests verify that a byte array is correctly formatted into lines
 * of hex and ASCII representations, with:
 * <ul>
 *     <li>Correct number of lines for given row lengths</li>
 *     <li>Correct ASCII representation (dots for non-printable characters)</li>
 *     <li>Correct handling of line offsets (shifted output)</li>
 *     <li>Correct handling of large buffers (100k+, 250k bytes)</li>
 * </ul>
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 */
@RunWith(JUnit4.class)
public class FormatBufferUnitTest {

  // ===========================
  // ======== TEST CASES =======
  // ===========================
  @Test
  public void testMaxBytes1row16() {
    assertFormat(1,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00                                               .");
  }

  @Test
  public void testMaxBytes2row16() {
    assertFormat(2,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01                                            ..");
  }

  @Test
  public void testMaxBytes3row16() {
    assertFormat(3,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02                                         ...");
  }

  @Test
  public void testMaxBytes4row16() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03                                      ....");
  }

  @Test
  public void testMaxBytes5row16() {
    assertFormat(5,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04                                   .....");
  }

  @Test
  public void testMaxBytes6row16() {
    assertFormat(6,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05                                ......");
  }

  @Test
  public void testMaxBytes7row16() {
    assertFormat(7,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06                             .......");
  }

  @Test
  public void testMaxBytes8row16() {
    assertFormat(8,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07                          ........");
  }

  @Test
  public void testMaxBytes9row16() {
    assertFormat(9,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08                       .........");
  }

  @Test
  public void testMaxBytes10row16() {
    assertFormat(10,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09                    ..........");
  }

  @Test
  public void testMaxBytes11row16() {
    assertFormat(11,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a                 ...........");
  }

  @Test
  public void testMaxBytes12row16() {
    assertFormat(12,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b              ............");
  }

  @Test
  public void testMaxBytes13row16() {
    assertFormat(13,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c           .............");
  }

  @Test
  public void testMaxBytes14row16() {
    assertFormat(14,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d        ..............");
  }

  @Test
  public void testMaxBytes15row16() {
    assertFormat(15,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e     ...............");
  }

  @Test
  public void testMaxBytes16row16() {
    assertFormat(16,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................");
  }

  @Test
  public void testMaxBytes17row16() {
    assertFormat(17,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................",
      "10                                               .");
  }

  @Test
  public void testMaxBytes1row8() {
    assertFormat(1,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00                       .");
  }

  @Test
  public void testMaxBytes2row8() {
    assertFormat(2,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01                    ..");
  }

  @Test
  public void testMaxBytes3row8() {
    assertFormat(3,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02                 ...");
  }

  @Test
  public void testMaxBytes4row8() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03              ....");
  }

  @Test
  public void testMaxBytes5row8() {
    assertFormat(5,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04           .....");
  }

  @Test
  public void testMaxBytes6row8() {
    assertFormat(6,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05        ......");
  }

  @Test
  public void testMaxBytes7row8() {
    assertFormat(7,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06     .......");
  }

  @Test
  public void testMaxBytes8row8() {
    assertFormat(8,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06 07  ........");
  }

  @Test
  public void testMaxBytes9row8() {
    assertFormat(9,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06 07  ........",
      "08                       .");
  }

  @Test
  public void testMaxBytes4row8shift1() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_8,
      1,
      "   00 01 02 03           ....");
  }

  @Test
  public void testMaxBytes9row8shiftN() {
    String[][] array = {
      {"00 01 02 03 04 05 06 07  ........", "08                       ."},
      {"   00 01 02 03 04 05 06   .......", "07 08                    .."},
      {"      00 01 02 03 04 05    ......", "06 07 08                 ..."},
      {"         00 01 02 03 04     .....", "05 06 07 08              ...."},
      {"            00 01 02 03      ....", "04 05 06 07 08           ....."},
      {"               00 01 02       ...", "03 04 05 06 07 08        ......"},
      {"                  00 01        ..", "02 03 04 05 06 07 08     ......."},
      {"                     00         .", "01 02 03 04 05 06 07 08  ........"}
    };
    for (int i = 0; i < array.length; i++) {
      assertFormat(9,
        SysHelper.MAX_BY_ROW_8,
        i,
        array[i]);
    }
  }

  @Test
  public void testFormatBuffer100k() {
    // Test very large buffer (100,000 bytes)
    testXk(100000);
  }

  @Test
  public void testFormatBuffer250k() {
    // Test very large buffer (250,000 bytes)
    testXk(250000);
  }

  // ===========================
  // ======== HELPERS ==========
  // ===========================
  /**
   * Helper to assert that formatting a buffer of {@code maxBytes} bytes
   * with given row length and shift offset matches expected strings.
   *
   * @param maxBytes number of bytes in the buffer
   * @param maxByRow maximum bytes per row
   * @param shiftOffset offset to shift the line output
   * @param expected expected lines (plain string representation)
   */
  private void assertFormat(int maxBytes, int maxByRow, int shiftOffset, String... expected) {
    byte[] bytes = new byte[maxBytes];
    for (int i = 0; i < maxBytes; i++)
      bytes[i] = (byte) i;

    List<LineEntry> list = SysHelper.formatBuffer(bytes, null, maxByRow, shiftOffset);

    // Check number of lines
    assertEquals(expected.length, list.size());

    // Check each line content
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], list.get(i).getPlain());
    }
  }

  /**
   * Helper to test formatting of very large buffers.
   *
   * @param maxBytes total number of bytes
   */
  private void testXk(int maxBytes) {
    byte[] bytes = new byte[maxBytes];
    for (int i = 0; i < maxBytes; i++)
      bytes[i] = (byte) i;

    // Calculate expected number of lines based on MAX_BY_ROW_16
    int maxLines = maxBytes / SysHelper.MAX_BY_ROW_16;
    int remain = maxBytes % SysHelper.MAX_BY_ROW_16;
    if (remain != 0)
      maxLines++;

    List<LineEntry> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    // Assert the number of lines is correct
    assertEquals(maxLines, list.size());
  }
}
