package fr.ralala.hexviewer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Payload management.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class Payload {
  private final List<Byte> mPayload;
  private final List<String> mPlain;

  public Payload() {
    mPlain = new ArrayList<>();
    mPayload = new ArrayList<>();
  }

  /**
   * Returns the payload.
   *
   * @param cancel Used to cancel this method.
   * @return byte[]
   */
  public byte[] to(final AtomicBoolean cancel) {
    final byte[] b = new byte[mPayload.size()];
    for (int i = 0; i < mPayload.size() && (cancel == null || !cancel.get()); ++i) {
      b[i] = mPayload.get(i);
    }
    return b;
  }

  /**
   * Sets the payload content.
   *
   * @param payload The new payload.
   * @param length  The array length.
   * @param cancel  Used to cancel this method.
   */
  public void add(final byte[] payload, final int length, final AtomicBoolean cancel) {
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    for (int i = 0; i < length && (cancel == null || !cancel.get()); i++) {
      mPayload.add(payload[i]);
      if (nbPerLine != 0 && (nbPerLine % SysHelper.MAX_BY_LINE) == 0) {
        sb.append((char) payload[i]);
        mPlain.add(sb.toString());
        nbPerLine = 0;
        sb.setLength(0);
      } else {
        sb.append((char) payload[i]);
        nbPerLine++;
      }
    }
    if ((cancel == null || !cancel.get()) && nbPerLine != 0) {
      mPlain.add(sb.toString());
    }
  }

  /**
   * Clear the payload content.
   */
  public void clear() {
    mPlain.clear();
    mPayload.clear();
  }

  /**
   * Converts a line to index value.
   *
   * @param line The line.
   * @return The index.
   */
  public static int line2index(final int line) {
    return Math.max(0, line * SysHelper.MAX_BY_ROW);
  }

  /**
   * Add zeros to the payload.
   *
   * @param length Number of zeros to add.
   */
  private void appendZero(int length) {
    for (int i = 0; i < length; i++)
      mPayload.add((byte) 0);
  }

  /**
   * Removes elements from the payload.
   *
   * @param index  Starting index.
   * @param length Number of elements to be deleted.
   */
  private void remove(int index, int length) {
    final int len = mPayload.size();
    for (int i = index + SysHelper.MAX_BY_ROW - 1; i >= (index + length); --i) {
      if (i < len) {
        mPayload.remove(i);
      }
    }
  }

  /**
   * Change the elements present in the payload by the new ones.
   *
   * @param index   Starting index.
   * @param payload The payload array.
   */
  private void set(final int index, final byte[] payload) {
    for (int i = index, j = 0; i < index + payload.length; ++i, ++j) {
      mPayload.set(i, payload[j]);
    }
  }

  /**
   * Change the elements present in the payload by the new ones.
   *
   * @param index   Starting index.
   * @param payload The payload array.
   */
  private void add(final int index, final byte[] payload) {
    for (int i = index, j = 0; i < index + payload.length; ++i, ++j) {
      mPayload.add(i, payload[j]);
    }
  }

  /**
   * Tests if the value is in the range
   *
   * @param index Index
   * @return boolean
   */
  private boolean isInRange(int index) {
    final int size = mPayload.size();
    return (size > index && size < index + SysHelper.MAX_BY_ROW);
  }

  /**
   * Updates the payload.
   *
   * @param line    The current line (the line is calculated by step of step SysHelper.MAX_BY_ROW).
   * @param payload The payload array.
   */
  public void update(final int line, final byte[] payload) {
    final int li = Math.max(0, line);
    final int size = mPayload.size();
    int idx = line2index(li);
    /* the line is removed ? */
    if (payload.length == 0) {
      mPayload.subList(idx, Math.min(size, idx + SysHelper.MAX_BY_ROW)).clear();
    }
    /* multiple lines ? */
    else if (payload.length > SysHelper.MAX_BY_ROW) {
      processMultipleLines(idx, size, payload);
    }
    /* single line ? */
    else {
      processSingleLine(idx, size, payload);
    }
  }


  /**
   * Processing the addition of a multiple lines.
   *
   * @param idx     Index.
   * @param size    Size.
   * @param payload Payload.
   */
  private void processMultipleLines(final int idx, final int size, byte[] payload) {
    /* append */
    if (idx >= size) {
      add(payload, payload.length, null);
    } else {
      /* insert */
      final byte[] currentLine = new byte[SysHelper.MAX_BY_ROW];
      System.arraycopy(payload, 0, currentLine, 0, currentLine.length);

      if (isInRange(idx)) {
        if ((idx + currentLine.length) > size) {
          /* There won't be enough space in the payload. */
          appendZero((idx + currentLine.length) - size);
        }
      }
      set(idx, currentLine);
      /* replace line */
      final byte[] newLines = new byte[payload.length - currentLine.length];
      System.arraycopy(payload, currentLine.length, newLines, 0, newLines.length);
      add(idx + currentLine.length, newLines);
    }
  }

  /**
   * Processing the addition of a single line.
   *
   * @param idx     Index.
   * @param size    Size.
   * @param payload Payload.
   */
  private void processSingleLine(final int idx, final int size, byte[] payload) {
    if ((idx + payload.length) > size) {
      /* There won't be enough space in the payload. */
      appendZero((idx + payload.length) - size);
    } else if (payload.length < SysHelper.MAX_BY_ROW) {
      /* We need to remove the excess entries from the line. */
      remove(idx, payload.length);
    }
    set(idx, payload);
  }

  /**
   * Returns the payload in plain text.
   *
   * @return String
   */
  public List<String> getPlain() {
    return mPlain;
  }
}
