package fr.ralala.hexviewer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.models.RawBuffer;
import fr.ralala.hexviewer.ui.adapters.search.EntryFilter;
import fr.ralala.hexviewer.ui.adapters.search.ISearchFrom;
import fr.ralala.hexviewer.ui.adapters.search.SearchEngine;
import fr.ralala.hexviewer.utils.SysHelper;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link SearchEngine} class, covering both
 * hexadecimal and plain-text searches.
 *
 * <p>This test class verifies the behavior of {@link SearchEngine} in various scenarios:
 * <ul>
 *     <li>Plain text search with single-line and multi-line matches.</li>
 *     <li>Hexadecimal search with spaces, without spaces, partial nibbles, and multi-line matches.</li>
 *     <li>Edge cases like queries starting mid-line and queries spanning multiple lines.</li>
 * </ul>
 *
 * <p>The tests use a buffer of bytes 0..254 formatted into {@link LineEntry} objects,
 * either in plain-text mode or hexadecimal representation.
 */
@SuppressWarnings("SpellCheckingInspection")
@RunWith(JUnit4.class)
public class SearchUnitTest {
  /** Maximum number of characters per line in plain text mode. */
  private static final int MAX_PLAIN_LINE_LEN = 47;
  /** Sample expected strings for hex mode, representing ASCII byte ranges. */
  private static final String TEXT_HEX_SLASH_16 = "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f   !\"#$%&'()*+,-./";
  private static final String TEXT_HEX_DIGIT_16 = "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f  0123456789:;<=>?";
  private static final String TEXT_HEX_ALPHA_16 = "40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f  @ABCDEFGHIJKLMNO";

  /** Sample expected strings for plain text mode. */
  private static final String TEXT_PLAIN_DIGIT_ALPHA = "0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_";
  private static final String TEXT_PLAIN_SLASH = ".........\t\n..................... !\"#$%&'()*+,-./";

  /**
   * Builds a list of {@link LineEntry} objects representing bytes 0..254 formatted
   * into lines of at most {@code maxRow} bytes per line.
   *
   * @param maxRow maximum number of bytes per line
   * @return list of {@link LineEntry} objects
   */
  private List<LineEntry> buildLineEntries(int maxRow) {
    // Create test data: bytes 0..254
    int maxBytes = 255;
    byte[] bytes = new byte[maxBytes];
    for (int i = 0; i < maxBytes; i++)
      bytes[i] = (byte) i;
    return SysHelper.formatBuffer(bytes, null, maxRow, 0);
  }

  /**
   * Performs a search over a list of {@link LineEntry} items using {@link SearchEngine}.
   *
   * @param items the lines to search
   * @param query the query string to find
   * @param maxRow max number of bytes per line
   * @param fromHex whether the search is in hexadecimal mode
   * @param list collection to store matching plain strings
   * @return a {@link BitSet} marking which lines contain matches
   */
  private BitSet search(List<LineEntry> items, String query, int maxRow, final boolean fromHex, List<String> list) {
    // Simulate the search source
    ISearchFrom searchFrom = () -> fromHex;
    SearchEngine factory = new SearchEngine(searchFrom, maxRow);
    BitSet result = new BitSet();

    char[] cQuery = query.toCharArray();
    int lineIndex = 0;

    // Iterate over each line and perform a multi-line search
    while (lineIndex < items.size()) {
      factory.multiLinesSearchBlock(items, lineIndex, cQuery, result);
      lineIndex = result.nextClearBit(lineIndex + 1); // Skip lines already marked
    }
    // Collect matched lines
    for(Integer i : EntryFilter.toSet(result))
      list.add(items.get(i).getPlain());
    return result;
  }

  /**
   * Evaluates a hex search query over the test buffer.
   *
   * @param query the query string
   * @param maxRow maximum bytes per line
   * @param founds collection of matched plain strings
   * @return {@link BitSet} of matched line indices
   */
  private BitSet evalBytes(String query, int maxRow, List<String> founds) {
    // Format buffer into LineEntry items (depends on your SysHelper)
    List<LineEntry> items = buildLineEntries(maxRow);
    return search(items, query, maxRow, true, founds);
  }

  /**
   * Splits a {@link RawBuffer} into {@link LineEntry} objects of fixed plain-text length.
   *
   * @param payload raw buffer containing the bytes
   * @return list of {@link LineEntry} representing the payload
   */
  private List<LineEntry> chunkPayload(RawBuffer payload) {
    List<LineEntry> list = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;

    for (int i = 0; i < payload.size(); i++) {
      sb.append((char) payload.get(i));
      if (nbPerLine != 0 && nbPerLine % MAX_PLAIN_LINE_LEN == 0) {
        list.add(new LineEntry(SysHelper.ignoreNonDisplayedChar(sb.toString()), null));
        sb.setLength(0);
        nbPerLine = 0;
      } else {
        nbPerLine++;
      }
    }
    if (nbPerLine != 0) list.add(new LineEntry(sb.toString(), null));
    return list;
  }

  /**
   * Evaluates a plain-text search query over the test buffer.
   *
   * @param query the query string
   * @param founds collection of matched plain strings
   * @return {@link BitSet} of matched line indices
   */
  private BitSet evalPlainBytes(String query, List<String> founds) {
    List<LineEntry> items = buildLineEntries(SysHelper.MAX_BY_ROW_16);

    // Concatenate all raw bytes into a payload
    RawBuffer payload = new RawBuffer(4096);
    items.forEach(le -> payload.addAll(le.getRaw()));

    // Split payload into lines for search
    List<LineEntry> list = chunkPayload(payload);

    return search(list, query, MAX_PLAIN_LINE_LEN, false, founds);
  }

  /**
   * Helper to assert that a list of strings matches the expected values.
   *
   * @param list actual strings
   * @param strings expected strings
   */
  private void equalsStrings(List<String> list, String... strings) {
    assertEquals(list.size(), strings.length);
    for(int i = 0; i < strings.length; i++)
      assertEquals(strings[i], list.get(i));
  }

  /**
   * Helper to test hex-line queries with expected matches.
   *
   * @param query the query string
   * @param expectedCardinality expected number of matching lines
   * @param expectedLines expected plain strings for the matches
   */
  private void testHexLine(String query, int expectedCardinality, String... expectedLines) {
    List<String> list = new ArrayList<>();
    BitSet bs = evalBytes(query, SysHelper.MAX_BY_ROW_16, list);
    assertEquals(expectedCardinality, bs.cardinality());
    equalsStrings(list, expectedLines);
  }

  // ===========================
  // ======= TEST CASES ========
  // ===========================

  @Test
  public void testSearchPlain1() {
    List<String> list = new ArrayList<>();
    String query = "0123456789";
    BitSet bs = evalPlainBytes(query, list);
    assertEquals(1, bs.cardinality());
    equalsStrings(list, TEXT_PLAIN_DIGIT_ALPHA);
  }

  @Test
  public void testSearchPlain2() {
    List<String> list = new ArrayList<>();
    String query = "/0123456789";
    BitSet bs = evalPlainBytes(query, list);
    assertEquals(2, bs.cardinality());
    equalsStrings(list, TEXT_PLAIN_SLASH, TEXT_PLAIN_DIGIT_ALPHA);
  }

  @Test
  public void testSearchPlain3() {
    List<String> list = new ArrayList<>();
    String query = "/0123456789:;<=>?@ABCD";
    BitSet bs = evalPlainBytes(query, list);
    assertEquals(2, bs.cardinality());
    equalsStrings(list, TEXT_PLAIN_SLASH, TEXT_PLAIN_DIGIT_ALPHA);
  }

  @Test
  public void testSearchHexPartPlain1() {
    List<String> list = new ArrayList<>();
    BitSet bs = evalBytes("0", SysHelper.MAX_BY_ROW_8, list);
    assertEquals(3, bs.cardinality());
    equalsStrings(list, "00 01 02 03 04 05 06 07  ........",
      "08 09 0a 0b 0c 0d 0e 0f  ........",
      "30 31 32 33 34 35 36 37  01234567");
  }

  @Test
  public void testSearchHexPartPlain2() {
    testHexLine("0123456789",
      1,
      TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartPlain3() {
    testHexLine("/0123456789",
      2,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartPlain4() {
    testHexLine("/0123456789:;<=>?@ABCD",
      3,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16, TEXT_HEX_ALPHA_16);
  }

  @Test
  public void testSearchHexPartHex1() {
    testHexLine("30",
      1,
      TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartHexNoSpace1() {
    testHexLine("30313233343536373839",
      1,
      TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartHexNoSpace2() {
    testHexLine("2f30313233343536373839",
      2,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartNoSpaceHex3() {
    testHexLine("2f303132333435363738393a3b3c3d3e3f4041424344",
      3,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16, TEXT_HEX_ALPHA_16);
  }

  @Test
  public void testSearchHexPartNoSpaceHex4() {
    testHexLine("2f303132333435363738393a3b3c3d3e3f404142434",
      3,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16, TEXT_HEX_ALPHA_16);
  }

  @Test
  public void testSearchHexPartNoSpaceHex5() {
    testHexLine("2f3",
      2,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartHexSpace1() {
    testHexLine("30 31 32 33 34 35 36 37 38 39",
      1,
      TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartHexSpace2() {
    testHexLine("2f 30 31 32 33 34 35 36 37 38 39",
      2,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16);
  }

  @Test
  public void testSearchHexPartSpaceHex3() {
    testHexLine("2f 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 40 41 42 43 44",
      3,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16, TEXT_HEX_ALPHA_16);
  }

  @Test
  public void testSearchHexPartSpaceHex4() {
    testHexLine("2f 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 40 41 42 43 4",
      3,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16, TEXT_HEX_ALPHA_16);
  }

  @Test
  public void testSearchHexPartSpaceHex5() {
    testHexLine("2f 3",
      2,
      TEXT_HEX_SLASH_16, TEXT_HEX_DIGIT_16);
  }

}
