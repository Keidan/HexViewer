package fr.ralala.hexviewer;

import org.junit.Test;

import java.util.List;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.utils.SysHelper;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

  @Test
  public void testFormatBuffer1() {
    byte[] bytes = {0};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00                                               .", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer2() {
    byte[] bytes = {0, 1};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01                                            ..", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer3() {
    byte[] bytes = {0, 1, 2};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02                                         ...", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer4() {
    byte[] bytes = {0, 1, 2, 3};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03                                      ....", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer5() {
    byte[] bytes = {0, 1, 2, 3, 4};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04                                   .....", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer6() {
    byte[] bytes = {0, 1, 2, 3, 4, 5};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05                                ......", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer7() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06                             .......", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer8() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07                          ........", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer9() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08                       .........", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer10() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09                    ..........", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer11() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a                 ...........", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer12() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b              ............", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer13() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b 0c           .............", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer14() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d        ..............", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer15() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e     ...............", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer16() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer17() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_16);

    assertEquals(2, list.size());
    assertEquals("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f  ................", list.get(0).getValue().getPlain());
    assertEquals("10                                               .", list.get(1).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer18() {
    byte[] bytes = {0};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00                       .", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer19() {
    byte[] bytes = {0, 1};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01                    ..", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer20() {
    byte[] bytes = {0, 1, 2};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02                 ...", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer21() {
    byte[] bytes = {0, 1, 2, 3};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03              ....", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer22() {
    byte[] bytes = {0, 1, 2, 3, 4};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04           .....", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer23() {
    byte[] bytes = {0, 1, 2, 3, 4, 5};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05        ......", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer24() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06     .......", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer25() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  ........", list.get(0).getValue().getPlain());
  }

  @Test
  public void testFormatBuffer26() {
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    List<LineData<Line>> list = SysHelper.formatBuffer(bytes, null, SysHelper.MAX_BY_ROW_8);

    assertEquals(2, list.size());
    assertEquals("00 01 02 03 04 05 06 07  ........", list.get(0).getValue().getPlain());
    assertEquals("08                       .", list.get(1).getValue().getPlain());
  }


}