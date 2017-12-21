package fr.ralala.hexviewer;

import android.app.Application;

import fr.ralala.hexviewer.utils.Payload;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ApplicationCtx extends Application {
  private Payload mPayload  = null;
  private String     filename = null;

  /**
   * Constructs the application context.
   */
  public ApplicationCtx() {
    super();
    mPayload = new Payload();
  }

  /**
   * Returns the payload.
   * @return Payload
   */
  public Payload getPayload() {
    return mPayload;
  }

  /**
   * Returns the filename of the file to export.
   * @return String
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the filename of the to export.
   * @param filename The new filename
   */
  public void setFilename(final String filename) {
    this.filename = filename;
  }

}
