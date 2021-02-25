package fr.ralala.hexviewer;

import org.junit.Test;

import java.util.List;

import fr.ralala.hexviewer.utils.SysHelper;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

  @Test
  public void hexformat01_isCorrect() {
    byte[] bytes = { 0};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00                                                .", list.get(0));
  }
  @Test
  public void hexformat02_isCorrect() {
    byte[] bytes = { 0, 1};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01                                             ..", list.get(0));
  }

  @Test
  public void hexformat03_isCorrect() {
    byte[] bytes = { 0, 1, 2};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02                                          ...", list.get(0));
  }

  @Test
  public void hexformat04_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03                                       ....", list.get(0));
  }

  @Test
  public void hexformat05_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04                                    .....", list.get(0));
  }

  @Test
  public void hexformat06_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05                                 ......", list.get(0));
  }

  @Test
  public void hexformat07_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06                              .......", list.get(0));
  }

  @Test
  public void hexformat08_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07                           ........", list.get(0));
  }

  @Test
  public void hexformat09_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08                       ........ .", list.get(0));
  }

  @Test
  public void hexformat10_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09                    ........ ..", list.get(0));
  }

  @Test
  public void hexformat11_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a                 ........ ...", list.get(0));
  }

  @Test
  public void hexformat12_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b              ........ ....", list.get(0));
  }

  @Test
  public void hexformat13_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c           ........ .....", list.get(0));
  }

  @Test
  public void hexformat14_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d        ........ ......", list.get(0));
  }

  @Test
  public void hexformat15_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e     ........ .......", list.get(0));
  }

  @Test
  public void hexformat16_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
  }

  @Test
  public void hexformat17_isCorrect() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(2, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("10                                                .", list.get(1));
  }
}