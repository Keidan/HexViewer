package fr.ralala.hexviewer;

import org.junit.Test;

import java.util.List;

import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.utils.SysHelper;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class FormatBufferUnitTest {
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
    String [][] array = new String[][] {
      {"00 01 02 03 04 05 06 07  ........", "08                       ."},
      {"   00 01 02 03 04 05 06   .......", "07 08                    .."},
      {"      00 01 02 03 04 05    ......", "06 07 08                 ..."},
      {"         00 01 02 03 04     .....", "05 06 07 08              ...."},
      {"            00 01 02 03      ....", "04 05 06 07 08           ....."},
      {"               00 01 02       ...", "03 04 05 06 07 08        ......"},
      {"                  00 01        ..", "02 03 04 05 06 07 08     ......."},
      {"                     00         .", "01 02 03 04 05 06 07 08  ........"}
    };
    for(int i = 0; i < array.length; i++) {
      assertFormat(9,
        SysHelper.MAX_BY_ROW_8,
        i,
        array[i]);
    }
  }

  @Test
  public void testFormatBuffer100k() {
    testXk(100000);
  }
  @Test
  public void testFormatBuffer250k() {
    testXk(250000);
  }

  /* Static functions */
  private void assertFormat(int maxBytes, int maxByRow, int shiftOffset, String... expected) {
    byte[] bytes = new byte[maxBytes];
    for (int i = 0; i < maxBytes; i++)
      bytes[i] = (byte) i;
    List<LineEntry> list = SysHelper.formatBuffer(bytes, null, maxByRow, shiftOffset);
    assertEquals(expected.length, list.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], list.get(i).getPlain());
    }
  }

  private void testXk(int maxBytes) {
    byte[] bytes = new byte[maxBytes];
    for (int i = 0; i < maxBytes; i++)
      bytes[i] = (byte) i;
    int maxLines = maxBytes / SysHelper.MAX_BY_ROW_16;
    int remain = maxBytes % SysHelper.MAX_BY_ROW_16;
    if(remain != 0)
      maxLines++;
    List<LineEntry> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);
    assertEquals(maxLines, list.size());
  }
}
