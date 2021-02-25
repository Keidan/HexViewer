package fr.ralala.hexviewer.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Helper functions.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SysHelper {
  private static final float SIZE_1KB = 0x400;
  private static final float SIZE_1MB = 0x100000;
  private static final float SIZE_1GB = 0x40000000;
  public static final int MAX_BY_ROW = 16;
  public static final int MAX_BY_LINE = ((MAX_BY_ROW * 2) + MAX_BY_ROW) + 19; /* 19 = nb spaces */

  /**
   * Abbreviate a string.
   *
   * @param src Source String.
   * @param max Max length.
   * @return New String
   */
  public static String abbreviate(String src, int max) {
    return src.substring(0, Math.min(max, src.length())) + (src.length() > max ? "..." : "");
  }

  /**
   * Returns the base name of a path.
   *
   * @param path The path.
   * @return String.
   */
  public static String basename(final String path) {
    String s = path;
    final int i = s.lastIndexOf('/');
    if (i != -1)
      s = s.substring(i + 1);
    return s;
  }

  /**
   * Converts hex string to byte array.
   *
   * @param s The hex string.
   * @return byte []
   */
  public static byte[] hexStringToByteArray(final String s) {
    final int len = s.length();
    final byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Extract the hexadecimal part of a string formatted with the formatBuffer function.
   *
   * @param string The hex string.
   * @return String
   */
  public static String extractString(final String string) {
    return (string.substring(0, 24).trim() + " " + string.substring(25, 49).trim()).trim();
  }

  /**
   * Converts a size into a humanly understandable string.
   * @param f The size.
   * @return The String.
   */
  public static String sizeToHuman(float f) {
    DecimalFormat df= new DecimalFormat("#.##");
    df.setRoundingMode(RoundingMode.FLOOR);
    String sf;
    if (f < 1000) {
      sf = String.format(Locale.US, "%d o", (int) f);
    } else if (f < 1000000)
      sf = df.format((f / SIZE_1KB))  + " Ko";
    else if (f < 1000000000)
      sf = df.format((f / SIZE_1MB))  + " Mo";
    else
      sf = df.format((f / SIZE_1GB))  + " Go";
    return sf;
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param buffer The input buffer.
   * @param cancel Used to cancel this method.
   * @return List<String>
   */
  public static List<String> formatBuffer(final byte[] buffer, AtomicBoolean cancel) {
    List<String> lines;
    try {
      lines = formatBuffer(buffer, buffer.length, cancel);
    } catch (IllegalArgumentException iae) {
      lines =  new ArrayList<>();
    }
    return lines;
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param buffer The input buffer.
   * @param length The input buffer length.
   * @param cancel Used to cancel this method.
   * @return List<String>
   */
  public static List<String> formatBuffer(final byte[] buffer, final int length, AtomicBoolean cancel) throws IllegalArgumentException {
    int len = length;
    if(len > buffer.length)
      throw new IllegalArgumentException("length > buffer.length");
    StringBuilder currentLine = new StringBuilder();
    StringBuilder currentEndLine = new StringBuilder();
    final List<String> lines = new ArrayList<>();
    int currentIndex = 0;
    int bufferIndex = 0;
    while (len > 0) {
      if(cancel != null && cancel.get())
        break;
      final byte c = buffer[bufferIndex++];
      currentLine.append(String.format("%02x ", c));
      /* only the visible char */
      currentEndLine.append((c >= 0x20 && c <= 0x7e) ? (char)c : (char) 0x2e); /* 0x2e = . */
      /* Prepare the new index. If the index is equal to MAX_BY_ROW - 1, currentLine and currentEndLine will be added to the list and then deleted. */
      currentIndex = formatBufferPrepareLineComplete(lines, currentIndex, currentLine, currentEndLine);
      /* add a space in the half of the line */
      formatBufferManageHalfLine(currentIndex, currentLine, currentEndLine);
      /* next */
      len--;
    }
    if(cancel != null && cancel.get())
      return lines;
    formatBufferAlign(lines, currentIndex, currentLine.toString(), currentEndLine.toString());
    return lines;
  }

  /**
   * If we get to the half of the line we add an extra space.
   * @param currentIndex The current index.
   * @param currentLine The current line.
   * @param currentEndLine The end of the current line.
   */
  private static void formatBufferManageHalfLine(final int currentIndex, final StringBuilder currentLine, final StringBuilder currentEndLine) {
    if (currentIndex == MAX_BY_ROW / 2) {
      currentLine.append(" ");
      currentEndLine.append(" ");
    }
  }

  /**
   * Prepare the new index. If the index is equal to MAX_BY_ROW - 1, currentLine and currentEndLine will be added to the list and then deleted.
   * @param lines The lines.
   * @param currentIndex The current index.
   * @param currentLine The current line.
   * @param currentEndLine The end of the current line.
   * @return The nex index.
   */
  private static int formatBufferPrepareLineComplete(final List<String> lines, final int currentIndex, final StringBuilder currentLine, final StringBuilder currentEndLine) {
    if (currentIndex == MAX_BY_ROW - 1) {
      lines.add(currentLine + " " + currentEndLine);
      currentEndLine.delete(0, currentEndLine.length());
      currentLine.delete(0, currentLine.length());
      return 0;
    }
    return currentIndex + 1;
  }


  /**
   * Alignment of the end of a line (if the line is not complete).
   * @param lines The lines.
   * @param currentIndex The current index.
   * @param currentLine The current line.
   * @param currentEndLine The end of the current line.
   */
  private static void formatBufferAlign(final List<String> lines, int currentIndex, final String currentLine, final String currentEndLine) {
    /* align 'line' */
    int i = currentIndex;
    if (i != 0 && (i < MAX_BY_ROW || i <= currentLine.length())) {
      int mid = MAX_BY_ROW / 2;
      StringBuilder off = new StringBuilder((i == mid) ? " " : "");
      while (i++ <= MAX_BY_ROW - 1)
        off.append((i == mid) ? "    " : "   "); /* 4 spaces ex: "00  " or 3 spaces ex: "00 " */
      off.append("  "); /* 2 spaces separator */
      String s = currentLine.trim();
      lines.add(s + off.toString() + currentEndLine.trim());
    }
  }
}
