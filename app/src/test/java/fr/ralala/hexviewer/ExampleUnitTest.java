package fr.ralala.hexviewer;

import org.junit.Test;

import java.util.List;

import fr.ralala.hexviewer.utils.Payload;
import fr.ralala.hexviewer.utils.SysHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

  @Test
  public void testFormatBuffer1() {
    byte[] bytes = { 0};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00                                                .", list.get(0));
  }
  @Test
  public void testFormatBuffer2() {
    byte[] bytes = { 0, 1};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01                                             ..", list.get(0));
  }

  @Test
  public void testFormatBuffer3() {
    byte[] bytes = { 0, 1, 2};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02                                          ...", list.get(0));
  }

  @Test
  public void testFormatBuffer4() {
    byte[] bytes = { 0, 1, 2, 3};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03                                       ....", list.get(0));
  }

  @Test
  public void testFormatBuffer5() {
    byte[] bytes = { 0, 1, 2, 3, 4};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04                                    .....", list.get(0));
  }

  @Test
  public void testFormatBuffer6() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05                                 ......", list.get(0));
  }

  @Test
  public void testFormatBuffer7() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06                              .......", list.get(0));
  }

  @Test
  public void testFormatBuffer8() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07                           ........", list.get(0));
  }

  @Test
  public void testFormatBuffer9() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08                       ........ .", list.get(0));
  }

  @Test
  public void testFormatBuffer10() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09                    ........ ..", list.get(0));
  }

  @Test
  public void testFormatBuffer11() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a                 ........ ...", list.get(0));
  }

  @Test
  public void testFormatBuffer12() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b              ........ ....", list.get(0));
  }

  @Test
  public void testFormatBuffer13() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c           ........ .....", list.get(0));
  }

  @Test
  public void testFormatBuffer14() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d        ........ ......", list.get(0));
  }

  @Test
  public void testFormatBuffer15() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e     ........ .......", list.get(0));
  }

  @Test
  public void testFormatBuffer16() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
  }

  @Test
  public void testFormatBuffer17() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    List<String> list = SysHelper.formatBuffer(bytes, null);

    assertEquals(2, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("10                                                .", list.get(1));
  }

  @Test
  public void testPayloadUpdate1() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final int nbLines = 3;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));

    byte[] bytes1 = { 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39};
    payload.updateLine(0, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("39 39 39 39 39 39 39 39  39 39 39 39 39 39 39 39  99999999 99999999", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));
  }

  @Test
  public void testPayloadUpdate2() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    final int nbLines = 3;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));

    byte[] bytes1 = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    payload.updateLine(1, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 30 30 30 30 30  30 30 30 30 30 30 30 30  00000000 00000000", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));
  }

  @Test
  public void testPayloadUpdate3() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    final int nbLines = 4;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));
    assertEquals("00                                                .", list.get(3));

    byte[] bytes1 = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    byte[] bytes2 = { 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31};
    payload.updateLine(1, bytes1);
    payload.updateLine(2, bytes2);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 30 30 30 30 30  30 30 30 30 30 30 30 30  00000000 00000000", list.get(1));
    assertEquals("31 31 31 31 31 31 31 31  31 31 31 31 31 31 31 31  11111111 11111111", list.get(2));
    assertEquals("00                                                .", list.get(3));
  }

  @Test
  public void testPayloadUpdate4() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    final int nbLines = 4;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));
    assertEquals("00                                                .", list.get(3));

    byte[] bytes1 = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    byte[] bytes2 = { 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31};
    byte[] bytes3 = { 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32};
    payload.updateLine(1, bytes1);
    payload.updateLine(2, bytes2);
    payload.updateLine(3, bytes3);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 30 30 30 30 30  30 30 30 30 30 30 30 30  00000000 00000000", list.get(1));
    assertEquals("31 31 31 31 31 31 31 31  31 31 31 31 31 31 31 31  11111111 11111111", list.get(2));
    assertEquals("32 32 32 32 32 32 32 32  32 32 32 32 32 32 32 32  22222222 22222222", list.get(3));
  }

  @Test
  public void testPayloadUpdate5() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    final int nbLines = 4;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(2));
    assertEquals("00                                                .", list.get(3));

    byte[] bytes1 = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    byte[] bytes2 = { 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31};
    byte[] bytes3 = { 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32};
    payload.updateLine(1, bytes1);
    payload.updateLine(2, bytes2);
    payload.updateLine(3, bytes3);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 30 30 30 30 30  30 30 30 30 30 30 30 30  00000000 00000000", list.get(1));
    assertEquals("31 31 31 31 31 31 31 31  31 31 31 31 31 31 31 31  11111111 11111111", list.get(2));
    assertEquals("32 32 32 32 32 32 32 32  32 32 32 32 32 32 32 32  22222222 22222222", list.get(3));


    byte[] bytes4 = { 0x32, 0x32, 0x32, 0x32, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x32, 0x32, 0x32, 0x32, 0x32};
    payload.updateLine(3, bytes4);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 30 30 30 30 30  30 30 30 30 30 30 30 30  00000000 00000000", list.get(1));
    assertEquals("31 31 31 31 31 31 31 31  31 31 31 31 31 31 31 31  11111111 11111111", list.get(2));
    assertEquals("32 32 32 32 32 33 34 35  36 37 38 32 32 32 32 32  22222345 67822222", list.get(3));
  }

  @Test
  public void testPayloadUpdate6() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    final int nbLines = 3;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00                                                .", list.get(2));

    byte[] bytes1 = { 0x30, 0x30, 0x30};
    payload.updateLine(1, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines - 1, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("30 30 30 00                                       000.", list.get(1));
  }

  @Test
  public void testPayloadUpdate7() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    final int nbLines = 3;
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00                                                .", list.get(2));


    byte[] bytes1 = { 0x32, 0x32, 0x32, 0x32, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x32, 0x32, 0x32, 0x32, 0x32};
    payload.updateLine(2, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("32 32 32 32 32 33 34 35  36 37 38 32 32 32 32 32  22222345 67822222", list.get(2));

    byte[] bytes2 = { 0x30, 0x30, 0x30};
    payload.updateLine(2, bytes2);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(nbLines, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("30 30 30                                          000", list.get(2));
  }

  @Test
  public void testPayloadUpdate8() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    Payload payload = new Payload();
    payload.add(bytes, bytes.length, null);
    List<String> list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(3, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ........ ........", list.get(1));
    assertEquals("00                                                .", list.get(2));


    byte[] bytes1 = {  };
    payload.updateLine(1, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(2, list.size());
    assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f  ........ ........", list.get(0));
    assertEquals("00                                                .", list.get(1));

    payload.updateLine(0, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(1, list.size());
    assertEquals("00                                                .", list.get(0));

    payload.updateLine(0, bytes1);
    list = SysHelper.formatBuffer(payload.to(null), null);
    assertEquals(0, list.size());
    assertTrue(list.isEmpty());
  }
}