package fr.ralala.hexviewer.utils.io.file;

import android.content.ContentResolver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.utils.io.RandomAccessFileChannel;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Processor for reading a file and converting it into LineEntry objects.
 * Optimized for very large files with batched reads and sequential offset support.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class FileOpenProcessor {
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW_16 * 20000;
  private final ApplicationCtx mApp;
  private final ContentResolver mContentResolver;
  private RandomAccessFileChannel mFileChannel;
  private final AtomicBoolean mCancel;
  private final IFileProgress mFileProgress;

  public FileOpenProcessor(ApplicationCtx app, ContentResolver contentResolver, AtomicBoolean cancel, IFileProgress fileProgress) {
    mApp = app;
    mContentResolver = contentResolver;
    mCancel = cancel;
    mFileProgress = fileProgress;
  }

  /**
   * Closes the file channel safely.
   */
  public void close() {
    if (mFileChannel != null) {
      mFileChannel.close();
      mFileChannel = null;
    }
  }

  /**
   * Reads a file and converts it into a list of LineEntry objects.
   *
   * @param fd FileData containing URI, start/end offsets, etc.
   * @return List of LineEntry objects representing the file contents.
   * @throws IOException on I/O errors
   */
  public List<LineEntry> readFile(FileData fd) throws IOException {
    final List<LineEntry> list = new ArrayList<>();
    try {
      // Publish initial progress
      mFileProgress.onFileProgress(0L);

      // Open file channel for read-only
      mFileChannel = RandomAccessFileChannel.openForReadOnly(mContentResolver, fd.getUri());

      int maxLength = moveCursorIfSequential(fd);

      /* prepare buffer */
      long totalSequential = fd.getStartOffset();
      evaluateShiftOffset(fd, totalSequential);

      // Read file data in batches
      processRead(fd, list, totalSequential, maxLength);
    } catch (final Exception e) {
      // Capture any exception in the result
      throw new IOException(e.getMessage(), e);
    } finally {
      // Ensure the file channel is closed at the end
      close();
    }
    // Return the final result object
    return list;
  }

  /**
   * Reads data from the file in batches using ByteBuffer.
   * Each read is processed and formatted into LineEntry objects.
   */
  private void processRead(final FileData fd,
                           final List<LineEntry> list,
                           long totalSequential,
                           int maxLength) throws IOException {
    boolean first = true;
    int reads;
    boolean forceBreak = false;
    // Allocate a buffer for batching reads
    ByteBuffer buffer = ByteBuffer.allocate(maxLength);
    // Read the file in chunks until EOF or cancellation
    while (!mCancel.get() && (reads = mFileChannel.read(buffer)) != -1) {
      try {
        // Convert raw bytes into LineEntry objects
        SysHelper.formatBuffer(list, buffer.array(), reads, mCancel,
          mApp.getNbBytesPerLine(), first ? fd.getShiftOffset() : 0);

        first = false;
        buffer.clear();
        mFileProgress.onFileProgress(reads);

        // Track sequential reads and enforce end offset
        if (fd.isSequential()) {
          totalSequential += reads;
          if (totalSequential >= fd.getEndOffset())
            forceBreak = true;
        }
      } catch (IllegalArgumentException iae) {
        throw new IOException(iae.getMessage(), iae);
      }
      if (forceBreak)
        break;
    }
  }


  private int moveCursorIfSequential(FileData fd) throws IOException {
    int maxLength = MAX_LENGTH;
    if (fd.isSequential()) {
      mFileChannel.setPosition(fd.getStartOffset());
      if (mFileChannel.getPosition() != fd.getStartOffset()) {
        throw new IOException("Unable to skip file data!");
      }
      maxLength = (int) Math.min(fd.getSize(), MAX_LENGTH);
    }
    return maxLength;
  }

  private void evaluateShiftOffset(FileData fd, long totalSequential) {
    if (totalSequential != 0) {
      final int nbBytesPerLine = mApp.getNbBytesPerLine();
      fd.setShiftOffset((int) (totalSequential % nbBytesPerLine));
    }
  }
}
