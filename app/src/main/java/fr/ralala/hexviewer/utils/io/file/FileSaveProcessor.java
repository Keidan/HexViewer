package fr.ralala.hexviewer.utils.io.file;

import android.content.ContentResolver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.RawBuffer;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.utils.io.RandomAccessFileChannel;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Processor for writing LineEntry objects and converting it into a file.
 * Optimized for very large files with batched writes and sequential offset support.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class FileSaveProcessor {
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW_16 * 10000;
  private final AtomicBoolean mCancel;
  private final ContentResolver mContentResolver;
  private final IFileProgress mFileProgress;
  private RandomAccessFileChannel mFileChannel;


  public FileSaveProcessor(ContentResolver contentResolver, AtomicBoolean cancel, IFileProgress fileProgress) {
    mContentResolver = contentResolver;
    mCancel = cancel;
    mFileProgress = fileProgress;
  }

  public void writeFile(FileData fd, List<LineEntry> entries) throws IOException {
    try {
      mFileProgress.onFileProgress(0L);
      // Open the file channel in write-only mode
      mFileChannel = RandomAccessFileChannel.openForWriteOnly(mContentResolver, fd.getUri(), !fd.isSequential());

      // Set the maximum batch size
      int maxLength = MAX_LENGTH;

      // Set initial file position for writing
      mFileChannel.setPosition(fd.getStartOffset());

      // Adjust maxLength for sequential files if file size is smaller
      if (fd.isSequential()) {
        maxLength = (int) Math.min(fd.getSize(), MAX_LENGTH);
      }

      // Temporary storage for a batch of bytes
      RawBuffer batch = new RawBuffer(2048);

      // Iterate over all line entries
      for (LineEntry entry : entries) {
        // Add the raw bytes of the current entry to the batch
        batch.addAll(entry.getRaw());

        // If batch reaches maxLength, write it to the file
        if (batch.size() >= maxLength) {
          byte[] data = batch.array();
          mFileChannel.write(data, 0, data.length);

          // Update progress on UI thread
          mFileProgress.onFileProgress(data.length);

          // Clear batch for next iteration
          batch.clear();

          // Stop if the task was cancelled
          if (mCancel.get()) break;
        }
      }

      // Write any remaining bytes in the last batch
      if (!mCancel.get() && batch.size() != 0) {
        byte[] data = batch.array();
        mFileChannel.write(data, 0, data.length);
        // Update progress on UI thread
        mFileProgress.onFileProgress(data.length);
      }
    } catch (final Exception e) {
      // Capture any exception in the result
      throw new IOException(e.getMessage());
    } finally {
      // Ensure the file channel is closed at the end
      close();
    }
  }


  /**
   * Closes the stream.
   */
  public void close() {
    if (mFileChannel != null) {
      mFileChannel.close();
      mFileChannel = null;
    }
  }
}
