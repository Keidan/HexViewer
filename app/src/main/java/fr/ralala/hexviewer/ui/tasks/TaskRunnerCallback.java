package fr.ralala.hexviewer.ui.tasks;

import androidx.annotation.Nullable;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task runner callback
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public interface TaskRunnerCallback<Config, Param, Progress, Result> {

  /**
   * Runs on the UI thread.
   *
   * @param value The value indicating progress.
   */
  void onProgressUpdate(Progress value);

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Nullable
  Config onPreExecute();

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  void onPostExecute(@Nullable final Result result);

  /**
   * Performs a computation on a background thread.
   *
   * @param config The config of the task see {@link #onPreExecute}.
   * @param param  The parameters of the task.
   * @return A result, defined by the subclass of this task.
   */
  @Nullable
  Result doInBackground(@Nullable Config config, @Nullable Param param) throws Exception;

  /**
   * Called when the async task is cancelled.
   */
  void onCancelled();

  /**
   * Called when the method {@link #doInBackground} raises an exception
   *
   * @param t The exception.
   */
  void onException(Throwable t);

}
