package fr.ralala.hexviewer;

import android.app.Application;

import fr.ralala.hexviewer.utils.Payload;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main application context
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ApplicationCtx extends Application {
  private final Payload mPayload;


  /**
   * Constructs the application context.
   */
  public ApplicationCtx() {
    super();
    mPayload = new Payload();
  }

  /**
   * Returns the payload.
   *
   * @return Payload
   */
  public Payload getPayload() {
    return mPayload;
  }

}
