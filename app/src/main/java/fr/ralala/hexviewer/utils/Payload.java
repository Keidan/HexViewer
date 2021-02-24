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
    for (int i = 0; i < mPayload.size() && !cancel.get(); ++i) {
      b[i] = mPayload.get(i);
    }
    return b;
  }

  /**
   * Sets the payload content.
   *
   * @param payload The new payload.
   * @param length  The array length.
   * @param cancel Used to cancel this method.
   */
  public void add(final byte[] payload, final int length, final AtomicBoolean cancel) {
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    for (int i = 0; i < length && !cancel.get(); i++) {
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
    if (!cancel.get() && nbPerLine != 0) {
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
   * @param index   The current index.
   * @param payload The payload array.
   */
  public void update(final int index, final byte[] payload) {
    final int len = mPayload.size();
    for (int i = index + SysHelper.MAX_BY_ROW - 1; i >= index; --i) {
      if (i < len) {
        mPayload.remove(i);
      }
    }
    for (int i = index, j = 0; i < index + payload.length; ++i, ++j) {
      mPayload.add(i, payload[j]);
    }
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
