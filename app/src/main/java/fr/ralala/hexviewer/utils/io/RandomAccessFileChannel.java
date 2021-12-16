package fr.ralala.hexviewer.utils.io;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Random access file channel.
 * See https://stackoverflow.com/questions/28698199/how-to-get-random-access-to-a-file-on-sd-card-by-means-of-api-presented-for-loll/28805474#28805474
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class RandomAccessFileChannel {
  private final ParcelFileDescriptor mFdInput;
  private final ParcelFileDescriptor mFdOutput;
  private final FileInputStream mFileInputStream;
  private final FileOutputStream mFileOutputStream;
  private long mPosition = 0;

  /**
   * Open a raw file descriptor in read/write mode to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  public RandomAccessFileChannel(ContentResolver contentResolver, Uri fileUri) throws FileNotFoundException {
    this(contentResolver, fileUri, false);
  }

  /**
   * Open a raw file descriptor in read/write mode to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @param resetOnWrite    If true, write access to the file will be opened in truncated mode.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  public RandomAccessFileChannel(ContentResolver contentResolver, Uri fileUri, boolean resetOnWrite) throws FileNotFoundException {
    mFdInput = contentResolver.openFileDescriptor(fileUri, "r");
    mFdOutput = contentResolver.openFileDescriptor(fileUri, "rw" + (resetOnWrite ? "t" : ""));
    mFileInputStream = new FileInputStream(mFdInput.getFileDescriptor());
    mFileOutputStream = new FileOutputStream(mFdOutput.getFileDescriptor());
  }

  /**
   * Reads a ByteBuffer from the file.
   *
   * @param buffer The buffer used to store the read data.
   * @return The number of bytes read.
   * @throws IOException If an I/O error occurs.
   */
  public int read(ByteBuffer buffer) throws IOException {
    if (mFileInputStream == null)
      return -1;
    FileChannel fch = mFileInputStream.getChannel();
    fch.position(mPosition);
    int bytesRead = fch.read(buffer);
    mPosition = fch.position();
    return bytesRead;
  }

  /**
   * Writes a ByteBuffer to the file.
   *
   * @param buffer The buffer to write.
   * @param offset The offset of the sub-array to be used; must be non-negative and no larger than buffer.length.
   * @param length The length of the sub-array to be used; must be non-negative and no larger than buffer.length - offset.
   * @return The number of bytes written.
   * @throws IOException If an I/O error occurs.
   */
  public int write(byte[] buffer, int offset, int length) throws IOException {
    return write(ByteBuffer.wrap(buffer, offset, length));
  }

  /**
   * Writes a ByteBuffer to the file.
   *
   * @param buffer The buffer to write.
   * @return The number of bytes written.
   * @throws IOException If an I/O error occurs.
   */
  public int write(ByteBuffer buffer) throws IOException {
    if (mFileOutputStream == null)
      return -1;
    FileChannel fch = mFileOutputStream.getChannel();
    fch.position(mPosition);
    int bytesWrite = fch.write(buffer);
    mPosition = fch.position();
    return bytesWrite;
  }

  /**
   * Gets the position of the cursor in the file.
   *
   * @return The current position.
   */
  public long getPosition() {
    return mPosition;
  }

  /**
   * Sets the position of the cursor in the file.
   *
   * @param newPosition The new position.
   */
  public void setPosition(long newPosition) {
    mPosition = newPosition;
  }

  /**
   * Returns the file size.
   *
   * @return The size.
   * @throws IOException If an I/O error occurs.
   */
  public long getSize() throws IOException {
    return mFileInputStream == null ? 0 : mFileInputStream.getChannel().size();
  }

  /**
   * Closes the file.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void close() throws IOException {
    if (mFileOutputStream != null) {
      FileChannel fch = mFileOutputStream.getChannel();
      fch.close();
      mFileOutputStream.close();
    }
    if (mFileInputStream != null)
      mFileInputStream.close();
    if (mFdInput != null)
      mFdInput.close();
    if (mFdOutput != null)
      mFdOutput.close();
  }
}
