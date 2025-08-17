package fr.ralala.hexviewer.models;

import java.util.Arrays;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * A dynamic byte buffer that can grow automatically as bytes are added.
 * Provides random access, sequential addition, and batch addition of bytes.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class RawBuffer {
  private static final int DEFAULT_CAPACITY = 1024;
  private byte[] mBytes = null;
  private int mSize = 0;
  private int mOffset = 0;

  public RawBuffer(int initialCapacity) {
    alloc(initialCapacity);
  }

  /**
   * Allocates a new buffer of the given capacity and resets the write offset.
   *
   * @param initialCapacity the initial capacity of the buffer
   */
  public void alloc(int initialCapacity) {
    mBytes = new byte[initialCapacity];
    mSize = initialCapacity;
    mOffset = 0;
  }

  /**
   * Returns the byte at the specified index in the buffer.
   *
   * <p>The index must be within the range of written bytes (0 <= index < size()).</p>
   *
   * @param index the position of the byte to retrieve
   * @return the byte at the specified index
   * @throws IndexOutOfBoundsException if the index is out of the valid range
   */
  public byte get(int index) {
    if (mBytes == null)
      return 0;
    if (index < 0 || index >= mOffset) throw new IndexOutOfBoundsException();
    return mBytes[index];
  }

  /**
   * Returns the number of bytes currently written in the buffer.
   *
   * @return the number of bytes stored
   */
  public int size() {
    return mOffset;
  }

  /**
   * Adds a single byte to the buffer at the current offset and increments the offset.
   * Automatically expands the buffer if necessary.
   *
   * @param b the byte to add
   */
  public void add(byte b) {
    ensureCapacity(mOffset + 1);
    mBytes[mOffset++] = b;
  }

  /**
   * Returns the underlying byte array of the buffer.
   *
   * @return the internal byte array, or {@code null} if no buffer has been allocated
   */
  public byte[] getBytes() {
    return mBytes;
  }

  /**
   * Returns a copy of the written bytes in the buffer.
   *
   * @return a new byte array containing only the used portion of the buffer
   */
  public byte[] array() {
    byte[] bytes = new byte[mOffset];
    System.arraycopy(mBytes, 0, bytes, 0, bytes.length);
    return bytes;
  }

  /**
   * Clears the buffer by resetting the write offset to zero.
   * The allocated memory remains unchanged.
   */
  public void clear() {
    mOffset = 0;
  }

  /**
   * Appends all bytes from the given array to this buffer.
   * Automatically expands the buffer if necessary.
   *
   * @param src the source byte array to append
   */
  public void addAll(byte[] src) {
    if (src == null)
      return;
    ensureCapacity(mOffset + src.length);
    System.arraycopy(src, 0, mBytes, mOffset, src.length);
    mOffset += src.length;
  }

  /**
   * Ensures that the buffer has at least the specified capacity.
   * If the current capacity is insufficient, the buffer grows by 50% or
   * up to the required minimum.
   *
   * @param minCapacity the minimum required capacity
   */
  private void ensureCapacity(int minCapacity) {
    if (mBytes == null) {
      alloc(Math.max(minCapacity, DEFAULT_CAPACITY));
    } else if (minCapacity > mSize) {
      int newCapacity = Math.max(minCapacity, mSize + mSize / 2);
      mBytes = Arrays.copyOf(mBytes, newCapacity);
      mSize = newCapacity;
    }
  }
}
