package fr.ralala.hexviewer.utils.io.file;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Interface for notifying read/write progress in bytes.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public interface IFileProgress {
  /**
   * Notifies the progress of a file operation.
   * <p>
   * This method is called to report the number of bytes processed
   * during a file read or write operation. Implementations can use
   * this information to update progress indicators, logs, or other
   * feedback mechanisms.
   *
   * @param bytes The number of bytes processed in the most recent batch.
   */
  void onFileProgress(long bytes);
}
