package fr.ralala.hexviewer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.BuildConfig;

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
   * Updates the payload.
   *
   * @param line    The current line.
   * @param payload The payload array.
   */
  public void updateLine(final int line, final byte[] payload) {
    if (BuildConfig.DEBUG && line < 0) {
      throw new AssertionError("Assertion failed");
    }
    update(line * SysHelper.MAX_BY_ROW, payload);
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
   * Updates the payload.
   *
   * @param index   The current index (min 0).
   * @param payload The payload array.
   */
  public void update(final int index, final byte[] payload) {
    final int len = mPayload.size();
    if ((index + payload.length) > len) {
      /* There won't be enough space in the payload. */
      appendZero((index + payload.length) - len);
    } else if (payload.length < SysHelper.MAX_BY_ROW) {
      /* We need to remove the excess entries from the line. */
      remove(index, payload.length);
    }
    set(index, payload);
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
