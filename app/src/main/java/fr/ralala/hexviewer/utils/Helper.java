package fr.ralala.hexviewer.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Helper functions.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Helper {
  public static final int MAX_BY_ROW = 16;

  /**
   * Returns the base name of a path.
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
   * @param string The hex string.
   * @return String
   */
  public static String extractString(final String string) {
    return (string.substring(0, 24).trim() + " " + string.substring(25, 49).trim()).trim();
  }

  /**
   * Formats a buffer (wireshark like).
   * @param buffer The input buffer.
   * @return List<String>
   */
  public static List<String> formatBuffer(final byte[] buffer) {
    final int max = MAX_BY_ROW;
    int length = buffer.length;
    StringBuilder line = new StringBuilder();
    StringBuilder eline = new StringBuilder();
    final List<String> lines = new ArrayList<>();
    int i = 0, j = 0;
    while (length > 0) {
      final byte c = buffer[j++];
      line.append(String.format("%02x ", c));
      /* only the visibles char */
      if (c >= 0x20 && c <= 0x7e)
        eline.append((char) c);
      else
        eline.append((char) 0x2e); /* . */
      if (i == max - 1) {
        lines.add(line + " " + eline);
        eline.delete(0, eline.length());
        line.delete(0, line.length());
        i = 0;
      } else
        i++;
      /* add a space in the midline */
      if (i == max / 2) {
        line.append(" ");
        eline.append(" ");
      }
      length--;
    }
    /* align 'line' */
    if (i != 0 && (i < max || i <= line.length())) {
      StringBuilder off = new StringBuilder();
      while (i++ <= max - 1)
        off.append("   "); /* 3 spaces ex: "00 " */
      off.append("  "); /* 2 spaces separator */
      String s = line.toString().trim();
      lines.add(s + off.toString() + eline.toString());
    }
    return lines;
  }

}
