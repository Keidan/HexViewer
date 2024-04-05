package fr.ralala.hexviewer.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;

import androidx.core.view.ViewCompat;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Helper functions.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class SysHelper {
  @SuppressWarnings("SpellCheckingInspection")
  private static final String HEX_UPPERCASE = "0123456789ABCDEF";
  @SuppressWarnings("SpellCheckingInspection")
  private static final String HEX_LOWERCASE = "0123456789abcdef";
  public static final float SIZE_1KB = 0x400;
  public static final float SIZE_1MB = 0x100000;
  public static final float SIZE_1GB = 0x40000000;
  public static final int MAX_BY_ROW_16 = 16;
  public static final int MAX_BY_ROW_8 = 8;
  public static final int MAX_BYTES_ROW_16 = 48;
  public static final int MAX_BYTES_ROW_8 = MAX_BYTES_ROW_16 / 2;
  private static final String FORMAT_INT = "%d %s%s";
  private static final String FORMAT_STR = "%s %s%s";

  private SysHelper() {
  }

  /**
   * Tests if the RTL mode is activated or not.
   *
   * @param view The reference view.
   * @return boolean
   */
  public static boolean isRTL(final View view) {
    final int layoutDirection = ViewCompat.getLayoutDirection(view);
    return layoutDirection == View.LAYOUT_DIRECTION_RTL;
  }

  /**
   * Tests if the RTL mode is activated or not.
   *
   * @param context The reference context.
   * @return boolean
   */
  public static boolean isRTL(final Context context) {
    Configuration cfg = ((ApplicationCtx) context.getApplicationContext()).getConfiguration();
    return cfg.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  /**
   * Sorts keys.
   *
   * @return List<Integer>
   */
  public static <T> List<Integer> getMapKeys(final Map<Integer, T> map) {
    List<Integer> sortedKeys = new ArrayList<>(map.keySet());
    Collections.sort(sortedKeys);
    return sortedKeys;
  }

  /**
   * Returns the byte array.
   *
   * @param bytes  The source list.
   * @param cancel Used to cancel this method.
   * @return byte[]
   */
  public static byte[] toByteArray(final List<Byte> bytes, final AtomicBoolean cancel) {
    final byte[] b = new byte[bytes.size()];
    for (int i = 0; i < bytes.size() && (cancel == null || !cancel.get()); ++i) {
      b[i] = bytes.get(i);
    }
    return b;
  }

  /**
   * Converts hex string to byte array.
   *
   * @param s The hex string.
   * @return byte []
   */
  public static byte[] hexStringToByteArray(final String s) {
    final int len = s.length();
    final int arrayLength = isEven(len) ? len : len + 1;
    final byte[] data = new byte[arrayLength / 2];
    for (int i = 0; i < len; i += 2) {
      if (i + 1 == len) {
        data[i / 2] = (byte) 0; /* nothing done */
      } else {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
      }
    }
    return data;
  }

  /**
   * Converts a size into a humanly understandable string.
   *
   * @param ctx Android context.
   * @param f   The size.
   * @return The String.
   */
  public static String sizeToHuman(Context ctx, float f) {
    return sizeToHuman(ctx, f, true, false);
  }

  /**
   * Converts a size into a humanly understandable string.
   *
   * @param ctx Android context.
   * @param f   The size.
   * @return The String.
   */
  public static String sizeToHuman(Context ctx, float f, boolean addUnit, boolean addHex) {
    return sizeToHuman(ctx, f, false, addUnit, addHex);
  }

  /**
   * Converts a size into a humanly understandable string.
   *
   * @param ctx Android context.
   * @param f   The size.
   * @return The String.
   */
  public static String sizeToHuman(Context ctx, float f, boolean fullByteName, boolean addUnit, boolean addHex) {
    DecimalFormat df = new DecimalFormat("#.##");
    df.setRoundingMode(RoundingMode.FLOOR);
    df.setMinimumFractionDigits(2);
    String sf;
    String hex = (!addHex ? "" : "(0x" + Long.toHexString((long) f).toUpperCase() + ") ");
    if (f < 1000) {
      String unit = fullByteName ? ctx.getString(R.string.unit_bytes_full_lc) : ctx.getString(R.string.unit_byte);
      sf = String.format(Locale.US, FORMAT_INT, (int) f, hex, !addUnit ? "" : unit);
    } else if (f < 1000000) {
      sf = String.format(Locale.US, FORMAT_STR, df.format((f / SIZE_1KB)),
        hex, !addUnit ? "" : ctx.getString(R.string.unit_kbyte));
    } else if (f < 1000000000) {
      sf = String.format(Locale.US, FORMAT_STR, df.format((f / SIZE_1MB)),
        hex, !addUnit ? "" : ctx.getString(R.string.unit_mbyte));
    } else {
      sf = String.format(Locale.US, FORMAT_STR, df.format((f / SIZE_1GB)),
        hex, !addUnit ? "" : ctx.getString(R.string.unit_gbyte));
    }
    return sf;
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param buffer   The input buffer.
   * @param cancel   Used to cancel this method.
   * @param maxByRow Max bytes by row.
   * @return List<LineEntry>
   */
  public static List<LineEntry> formatBuffer(final byte[] buffer, AtomicBoolean cancel,
                                             final int maxByRow) {
    List<LineEntry> lines;
    try {
      lines = formatBuffer(buffer, buffer.length, cancel, maxByRow);
    } catch (IllegalArgumentException iae) {
      lines = new ArrayList<>();
    }
    return lines;
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param buffer      The input buffer.
   * @param cancel      Used to cancel this method.
   * @param maxByRow    Max bytes by row.
   * @param shiftOffset Offset used to shift the text to the end of the line.
   * @return List<LineEntry>
   */
  public static List<LineEntry> formatBuffer(final byte[] buffer, AtomicBoolean cancel,
                                             final int maxByRow, final int shiftOffset) {
    List<LineEntry> lines = new ArrayList<>();
    try {
      formatBuffer(lines, buffer, buffer.length, cancel, maxByRow, shiftOffset);
    } catch (IllegalArgumentException ex) {
      Log.e(SysHelper.class.getSimpleName(), ex.getMessage(), ex);
    }
    return lines;
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param lines    The lines.
   * @param buffer   The input buffer.
   * @param length   The input buffer length.
   * @param cancel   Used to cancel this method.
   * @param maxByRow Max bytes by row.
   */
  public static void formatBuffer(final List<LineEntry> lines, final byte[] buffer,
                                  final int length,
                                  AtomicBoolean cancel,
                                  final int maxByRow) throws IllegalArgumentException {
    formatBuffer(lines, buffer, length, cancel, maxByRow, 0);
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param lines       The lines.
   * @param buffer      The input buffer.
   * @param length      The input buffer length.
   * @param cancel      Used to cancel this method.
   * @param maxByRow    Max bytes by row.
   * @param shiftOffset Offset used to shift the text to the end of the line.
   */
  public static void formatBuffer(final List<LineEntry> lines, final byte[] buffer,
                                  final int length,
                                  AtomicBoolean cancel,
                                  final int maxByRow, final int shiftOffset) throws IllegalArgumentException {
    int len = length;
    if (len > buffer.length)
      throw new IllegalArgumentException("length > buffer.length");
    StringBuilder currentLine = new StringBuilder();
    StringBuilder currentEndLine = new StringBuilder();
    final List<Byte> currentLineRaw = new ArrayList<>();
    int currentIndex = 0;
    int bufferIndex = 0;
    if (shiftOffset != 0) {
      currentIndex = shiftOffset;
      int n = shiftOffset;
      while (n > 0) {
        currentLine.append("   ");
        currentEndLine.append(" ");
        n--;
      }
    }
    while (len > 0) {
      if (cancel != null && cancel.get())
        break;
      final byte c = buffer[bufferIndex++];
      currentLine.append(formatHex((char) c, false)).append(" ");
      currentLineRaw.add(c);
      /* only the visible char */
      currentEndLine.append((c >= 0x20 && c <= 0x7e) ? (char) c : (char) 0x2e); /* 0x2e = . */
      /* Prepare the new index. If the index is equal to MAX_BY_ROW - 1, currentLine and currentEndLine will be added to the list and then deleted. */
      currentIndex = formatBufferPrepareLineComplete(lines, currentIndex, currentLine, currentEndLine, currentLineRaw, maxByRow);

      /* next */
      len--;
    }
    if (cancel != null && cancel.get())
      return;
    formatBufferAlign(lines, currentIndex, currentLine.toString(),
      currentEndLine.toString(), currentLineRaw, maxByRow);
    if (!lines.isEmpty())
      lines.get(0).setShiftOffset(shiftOffset);
  }

  /**
   * Formats a buffer (wireshark like).
   *
   * @param buffer   The input buffer.
   * @param length   The input buffer length.
   * @param cancel   Used to cancel this method.
   * @param maxByRow Max bytes by row.
   * @return List<String>
   */
  public static List<LineEntry> formatBuffer(final byte[] buffer,
                                             final int length,
                                             AtomicBoolean cancel,
                                             final int maxByRow) throws IllegalArgumentException {
    final List<LineEntry> lines = new ArrayList<>();
    formatBuffer(lines, buffer, length, cancel, maxByRow);
    return lines;
  }

  /**
   * Formats a character into a hexadecimal string (2 digits).
   * Note: Using String.format("%02X") is ~50x slower.
   *
   * @param b         The character.
   * @param upperCase Uppercase ?
   * @return The string.
   */
  public static String formatHex(char b, boolean upperCase) {
    return "" + (upperCase ? HEX_UPPERCASE : HEX_LOWERCASE).charAt((b & 0xF0) >> 4) +
      (upperCase ? HEX_UPPERCASE : HEX_LOWERCASE).charAt((b & 0x0F));
  }


  /**
   * Converts hex string to a binary array.
   *
   * @param hex The hex string.
   * @return String
   */
  public static byte[] hex2bin(final String hex) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final String h = hex.replace(" ", "");
    int len = h.length();
    if (len == 0)
      return byteArrayOutputStream.toByteArray();
    int max = isEven(len) ? len : len - 1;
    for (int i = 0; i < max; i += 2) {
      byte b = (byte) ((Character.digit(h.charAt(i), 16) << 4) + Character
        .digit(h.charAt(i + 1), 16));
      byteArrayOutputStream.write(b);
    }
    return byteArrayOutputStream.toByteArray();
  }

  /**
   * Prepare the new index. If the index is equal to MAX_BY_ROW - 1, currentLine and currentEndLine will be added to the list and then deleted.
   *
   * @param lines          The lines.
   * @param currentIndex   The current index.
   * @param currentLine    The current line.
   * @param currentEndLine The end of the current line.
   * @param currentLineRaw The current line in raw.
   * @param maxByRow       Max bytes by row.
   * @return The nex index.
   */
  private static int formatBufferPrepareLineComplete(final List<LineEntry> lines,
                                                     final int currentIndex,
                                                     final StringBuilder currentLine,
                                                     final StringBuilder currentEndLine,
                                                     final List<Byte> currentLineRaw,
                                                     final int maxByRow) {
    if (currentIndex == maxByRow - 1) {
      lines.add(new LineEntry(currentLine + " " + currentEndLine,
        new ArrayList<>(currentLineRaw)));
      currentEndLine.setLength(0);
      currentLine.setLength(0);
      currentLineRaw.clear();
      return 0;
    }
    return currentIndex + 1;
  }


  /**
   * Alignment of the end of a line (if the line is not complete).
   *
   * @param lines          The lines.
   * @param currentIndex   The current index.
   * @param currentLine    The current line.
   * @param currentEndLine The end of the current line.
   * @param maxByRow       Max bytes by row.
   */
  private static void formatBufferAlign(final List<LineEntry> lines,
                                        int currentIndex,
                                        final String currentLine,
                                        final String currentEndLine,
                                        final List<Byte> currentLineRaw,
                                        final int maxByRow) {
    /* align 'line' */
    int i = currentIndex;
    if (i != 0 && (i < maxByRow || i <= currentLine.length())) {
      StringBuilder off = new StringBuilder();
      while (i++ <= maxByRow - 1)
        off.append("   "); /* 3 spaces ex: "00 " */
      off.append("  "); /* 1 or 2 spaces separator */
      String s = currentLine;
      if (s.endsWith(" "))
        s = s.substring(0, s.length() - 1);
      lines.add(new LineEntry(s + off + currentEndLine.trim(), new ArrayList<>(currentLineRaw)));
    }
  }

  /**
   * Tests if the hexadecimal line is valid or not.
   *
   * @param line The line to test
   * @return True if the line is valid
   */
  public static boolean isValidHexLine(final String line) {
    if (line.isEmpty())
      return true;
    return line.matches("\\p{XDigit}+") && isEven(line.length());
  }

  /**
   * Test if the number is even or odd.
   *
   * @param num The number to test.
   * @return Returns true if the number is even
   */
  public static boolean isEven(final int num) {
    return (num % 2) == 0;
  }

  /**
   * Ignore non displayed char
   *
   * @param ref Ref string.
   * @return Patched string.
   */
  public static String ignoreNonDisplayedChar(final String ref) {
    StringBuilder sb = new StringBuilder();
    for (char c : ref.toCharArray())
      sb.append((c == 0x09 || c == 0x0A || (c >= 0x20 && c < 0x7F)) ? c : '.');
    return sb.toString();
  }
}
