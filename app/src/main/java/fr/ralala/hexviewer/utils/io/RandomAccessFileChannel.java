package fr.ralala.hexviewer.utils.io;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

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
  private static final String EXCEPTION_TAG = "Exception: ";

  private enum Mode {
    RO, /* read only */
    WO, /* write only */
    RW /* read/write */
  }

  private ParcelFileDescriptor mFdInput = null;
  private ParcelFileDescriptor mFdOutput = null;
  private FileInputStream mFileInputStream = null;
  private FileOutputStream mFileOutputStream = null;
  private long mPosition = 0;
  private final Mode mMode;

  /**
   * Open a raw file descriptor in write mode to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @param resetOnWrite    If true, write access to the file will be opened in truncated mode.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  public static RandomAccessFileChannel openForWriteOnly(ContentResolver contentResolver, Uri fileUri, boolean resetOnWrite) throws FileNotFoundException {
    return new RandomAccessFileChannel(contentResolver, fileUri, resetOnWrite, Mode.WO);
  }

  /**
   * Open a raw file descriptor in read mode to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  public static RandomAccessFileChannel openForReadOnly(ContentResolver contentResolver, Uri fileUri) throws FileNotFoundException {
    return new RandomAccessFileChannel(contentResolver, fileUri, false, Mode.RO);
  }

  /**
   * Open a raw file descriptor in read mode to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  @SuppressWarnings("unused")
  public static RandomAccessFileChannel openForReadWrite(ContentResolver contentResolver, Uri fileUri) throws FileNotFoundException {
    return new RandomAccessFileChannel(contentResolver, fileUri, false, Mode.RW);
  }

  /**
   * Open a raw file descriptor in mode specified by "mode" to access the data.
   *
   * @param contentResolver ContentResolver
   * @param fileUri         The URI of the file.
   * @param resetOnWrite    If true, write access to the file will be opened in truncated mode.
   * @param mode            The mode to use.
   * @throws FileNotFoundException If the file designated by the specified URI has failed.
   */
  private RandomAccessFileChannel(ContentResolver contentResolver, Uri fileUri, boolean resetOnWrite, Mode mode) throws FileNotFoundException {
    mMode = mode;
    if (mode == Mode.RO || mode == Mode.RW) {
      mFdInput = contentResolver.openFileDescriptor(fileUri, "r");
      mFileInputStream = new FileInputStream(mFdInput.getFileDescriptor());
    }
    if (mode == Mode.WO || mode == Mode.RW) {
      mFdOutput = contentResolver.openFileDescriptor(fileUri, "rw" + (resetOnWrite ? "t" : ""));
      mFileOutputStream = new FileOutputStream(mFdOutput.getFileDescriptor());
    }
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
    if (mMode == Mode.WO)
      return mFileOutputStream == null ? 0 : mFileOutputStream.getChannel().size();
    return mFileInputStream == null ? 0 : mFileInputStream.getChannel().size();
  }

  private void closeOutputStreams() {
    if (mFileOutputStream != null) {
      try {
        FileChannel fch = mFileOutputStream.getChannel();
        fch.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      try {
        mFileOutputStream.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      mFileOutputStream = null;
    }
  }

  private void closeInputStreams() {
    if (mFileInputStream != null) {
      try {
        FileChannel fch = mFileInputStream.getChannel();
        fch.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      try {
        mFileInputStream.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      mFileInputStream = null;
    }
  }

  /**
   * Closes the file.
   */
  public void close() {
    if (mMode == Mode.WO) {
      closeOutputStreams();
    }
    closeInputStreams();
    if (mFdInput != null) {
      try {
        mFdInput.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      mFdInput = null;
    }
    if (mFdOutput != null) {
      try {
        mFdOutput.close();
      } catch (IOException ioException) {
        Log.e(getClass().getSimpleName(), EXCEPTION_TAG + ioException.getMessage(), ioException);
      }
      mFdOutput = null;
    }
  }
}
