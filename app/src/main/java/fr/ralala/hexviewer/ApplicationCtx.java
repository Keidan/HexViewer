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
  private final Payload mPayload;
  private boolean mPlainText = false;


  /**
   * Constructs the application context.
   */
  public ApplicationCtx() {
    super();
    mPayload = new Payload();
  }

  /**
   * Sets the plain text value.
   * @param plainText The new value.
   */
  public void setPlainText(boolean plainText) {
    mPlainText = plainText;
  }

  /**
   * Returns true if the file should be displayed in plain text and
   * false if the file should be displayed in hexadecimal.
   * @return boolean
   */
  public boolean isPlainText() {
    return mPlainText;
  }

  /**
   * Returns the payload.
   * @return Payload
   */
  public Payload getPayload() {
    return mPayload;
  }

}
