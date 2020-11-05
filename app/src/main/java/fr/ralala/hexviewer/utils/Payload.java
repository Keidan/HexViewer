package fr.ralala.hexviewer.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Payload management.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Payload {
  private final List<Byte> mPayload;

  public Payload() {
    mPayload = new ArrayList<>();
  }

  /**
   * Returns the payload.
   * @return byte[]
   */
  public byte[] to() {
    final byte[] b = new byte[mPayload.size()];
    for (int i = 0; i < mPayload.size(); ++i)
      b[i] = mPayload.get(i);
    return b;
  }

  /**
   * Sets the payload content.
   * @param payload The new payload.
   * @param length  The array length.
   */
  public void add(final byte[] payload, final int length) {
    for(int i = 0; i < length; i++)
      mPayload.add(payload[i]);
  }

  /**
   * Clear the payload content.
   */
  public void clear() {
    mPayload.clear();
  }

  /**
   * Updates the payload.
   * @param index The current index.
   * @param payload The payload array.
   */
  public void update(final int index, final byte[] payload) {
    final int len = mPayload.size();
    for (int i = index + Helper.MAX_BY_ROW - 1; i >= index; --i)
      if (i < len)
        mPayload.remove(i);
    for (int i = index, j = 0; i < index + payload.length; ++i, ++j)
      mPayload.add(i, payload[j]);
  }
}
