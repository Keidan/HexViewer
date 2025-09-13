package fr.ralala.hexviewer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
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
 */
@RunWith(JUnit4.class)
public class FormatBufferUnitTest {

  // ===========================
  // ======== TEST CASES =======
  // ===========================
  @Test
  public void testFormatBuffer01() {
    assertFormat(1,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00                                               .");
  }

  @Test
  public void testFormatBuffer02() {
    assertFormat(2,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01                                            ..");
  }

  @Test
  public void testFormatBuffer03() {
    assertFormat(3,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02                                         ...");
  }

  @Test
  public void testFormatBuffer04() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03                                      ....");
  }

  @Test
  public void testFormatBuffer05() {
    assertFormat(5,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04                                   .....");
  }

  @Test
  public void testFormatBuffer06() {
    assertFormat(6,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05                                ......");
  }

  @Test
  public void testFormatBuffer07() {
    assertFormat(7,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06                             .......");
  }

  @Test
  public void testFormatBuffer08() {
    assertFormat(8,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07                          ........");
  }

  @Test
  public void testFormatBuffer09() {
    assertFormat(9,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08                       .........");
  }

  @Test
  public void testFormatBuffer10() {
    assertFormat(10,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09                    ..........");
  }

  @Test
  public void testFormatBuffer11() {
    assertFormat(11,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a                 ...........");
  }

  @Test
  public void testFormatBuffer12() {
    assertFormat(12,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b              ............");
  }

  @Test
  public void testFormatBuffer13() {
    assertFormat(13,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c           .............");
  }

  @Test
  public void testFormatBuffer14() {
    assertFormat(14,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d        ..............");
  }

  @Test
  public void testFormatBuffer15() {
    assertFormat(15,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e     ...............");
  }

  @Test
  public void testFormatBuffer16() {
    assertFormat(16,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................");
  }

  @Test
  public void testFormatBuffer17() {
    assertFormat(17,
      SysHelper.MAX_BY_ROW_16,
      0,
      "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................",
      "10                                               .");
  }

  @Test
  public void testFormatBuffer18() {
    assertFormat(1,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00                       .");
  }

  @Test
  public void testFormatBuffer19() {
    assertFormat(2,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01                    ..");
  }

  @Test
  public void testFormatBuffer20() {
    assertFormat(3,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02                 ...");
  }

  @Test
  public void testFormatBuffer21() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03              ....");
  }

  @Test
  public void testFormatBuffer22() {
    assertFormat(5,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04           .....");
  }

  @Test
  public void testFormatBuffer23() {
    assertFormat(6,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05        ......");
  }

  @Test
  public void testFormatBuffer24() {
    assertFormat(7,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06     .......");
  }

  @Test
  public void testFormatBuffer25() {
    assertFormat(8,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06 07  ........");
  }

  @Test
  public void testFormatBuffer26() {
    assertFormat(9,
      SysHelper.MAX_BY_ROW_8,
      0,
      "00 01 02 03 04 05 06 07  ........",
      "08                       .");
  }

  @Test
  public void testFormatBuffer37() {
    assertFormat(4,
      SysHelper.MAX_BY_ROW_8,
      1,
      "   00 01 02 03           ....");
  }

  @Test
  public void testFormatBufferShift() {
    String[][] array = new String[][]{
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
