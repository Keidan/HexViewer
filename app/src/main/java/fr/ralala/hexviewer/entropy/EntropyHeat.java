package fr.ralala.hexviewer.entropy;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import fr.ralala.hexviewer.models.lines.LineEntry;
/**
 * <p><b>Project HexViewer</b><br/>
 * Entropy heat
 * </p>
 * Computes and exposes a per-line "entropy heat" level for a list of {@link LineEntry} items.
 *
 * <p>
 * The computation is expensive, so it is performed asynchronously on a dedicated background
 * thread and is <em>debounced</em>: successive calls to {@link #schedule(List)} within
 * {@value #DEBOUNCE_MS} ms only result in a single computation, using the most recent data.
 * </p>
 *
 * <p>
 * <b>Threading model:</b>
 * </p>
 * <ul>
 *   <li>{@link #schedule(List)} must be called from the UI (main) thread.</li>
 *   <li>The heavy computation runs on a single background thread.</li>
 *   <li>The result is published back on the UI thread, and the {@code onChanged} callback
 *   is invoked on the UI thread.</li>
 *   <li>{@link #levelOf(int)} and {@link #clear()} are thread-safe and may be called from
 *   any thread.</li>
 * </ul>
 *
 * <p>
 * <b>Staleness handling:</b> each call to {@link #schedule(List)} increments an internal
 * version counter and raises a cancellation flag so that any computation in progress can
 * stop early. A result is only published if its version still matches the current one;
 * otherwise it is silently discarded as stale.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 * <pre>{@code
 * EntropyHeat heat = new EntropyHeat(() -> adapter.notifyDataSetChanged());
 * heat.schedule(items);
 * // later, while binding a row:
 * int level = heat.levelOf(position);
 * }</pre>
 *
 * @see EntropyMap
 * @see LineEntry
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 */
public class EntropyHeat {
  /**
   * Delay, in milliseconds, applied before starting a computation after a call to
   * {@link #schedule(List)}. Used to coalesce rapid successive changes.
   */
  private static final long DEBOUNCE_MS = 400;

  /**
   * Handler bound to the main looper, used to debounce computations and to publish results
   * on the UI thread.
   */
  private final Handler mHandler = new Handler(Looper.getMainLooper());

  /**
   * Single-threaded executor on which the entropy computation is performed, ensuring that
   * computations never run concurrently.
   */
  private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

  /**
   * Latest published heat map: one byte per line, holding the (unsigned) heat level of that
   * line. {@code null} when no result is available.
   */
  private final AtomicReference<byte[]> mHeat = new AtomicReference<>();

  /**
   * Cancellation flag shared with the background computation. When set to {@code true},
   * a running computation is expected to stop as soon as possible.
   */
  private final AtomicBoolean mCancel = new AtomicBoolean();

  /**
   * Monotonically increasing version counter, incremented on every
   * {@link #schedule(List)} call. Used to detect and discard stale results.
   */
  private final AtomicInteger mVersion = new AtomicInteger();

  /**
   * Callback invoked on the UI thread each time a new heat map has been published.
   */
  private final Runnable mOnChanged;

  /**
   * Creates a new {@code EntropyHeat} instance.
   *
   * <p>
   * This constructor creates a {@link Handler} on the main looper and a background
   * single-thread executor.
   * </p>
   *
   * @param onChanged callback invoked on the UI thread whenever a new heat map becomes
   *                  available (i.e. when the displayed heat levels should be refreshed)
   */
  public EntropyHeat(Runnable onChanged) {
    mOnChanged = onChanged;
  }

  /**
   * Schedules a (debounced) recomputation of the heat map for the given items.
   *
   * <p>
   * Any pending scheduled computation is dropped, any computation currently running is
   * asked to cancel, and its result will be ignored. The new computation starts after
   * {@value #DEBOUNCE_MS} ms if no further call to this method occurs in the meantime.
   * </p>
   *
   * <p>
   * Must be called from the UI thread.
   * </p>
   *
   * @param items the lines to analyze; the list is read on the UI thread when the debounce
   *              delay expires, so it should not be modified concurrently from another thread
   */
  public void schedule(List<LineEntry> items) {
    mVersion.incrementAndGet();
    mCancel.set(true);                       // interrupts a running computation
    mHandler.removeCallbacksAndMessages(null);
    mHandler.postDelayed(() -> start(items), DEBOUNCE_MS);
  }

  /**
   * Discards the current heat map.
   *
   * <p>
   * After this call, {@link #levelOf(int)} returns {@code 0} for every line until a new
   * result is published. This method does not cancel any pending or running computation,
   * nor does it invoke the {@code onChanged} callback.
   * </p>
   */
  public void clear() {
    mHeat.set(null);
  }

  /**
   * Returns the heat level of the given line.
   *
   * <p>
   * This method is thread-safe and never blocks.
   * </p>
   *
   * @param lineIndex zero-based index of the line
   * @return the heat level of the line as an unsigned value in the range {@code 0..255},
   * or {@code 0} if no heat map is available or if {@code lineIndex} is out of bounds
   */
  public int levelOf(int lineIndex) {
    byte[] heat = mHeat.get();
    return (heat != null && lineIndex >= 0 && lineIndex < heat.length) ? heat[lineIndex] & 0xFF : 0;
  }

  /**
   * Starts a computation for the given items.
   *
   * <p>
   * Runs on the UI thread. It takes a lightweight snapshot of the raw data references
   * (no byte copy) and of their lengths, resets the cancellation flag, and then delegates
   * the actual work to the background executor.
   * </p>
   *
   * @param items the lines to analyze
   */
  private void start(List<LineEntry> items) {
    final int version = mVersion.get();
    mCancel.set(false);
    final List<byte[]> raws = new ArrayList<>(items.size());
    final int[] lengths = new int[items.size()];
    for (int i = 0; i < lengths.length; i++) {
      byte[] raw = items.get(i).getRaw();
      raws.add(raw);
      lengths[i] = raw == null ? 0 : raw.length;
    }
    mExecutor.execute(() -> compute(version, raws, lengths));
  }

  /**
   * Performs the entropy computation.
   *
   * <p>
   * Runs on the background thread. If the computation completes without being canceled,
   * the per-line heat map is derived and posted to the UI thread for publication.
   * If the computation was canceled, nothing is posted.
   * </p>
   *
   * @param version the version of the request this computation belongs to
   * @param raws    the raw bytes of each line (elements may be {@code null})
   * @param lengths the length, in bytes, of each line ({@code 0} for {@code null} lines)
   * @see EntropyMap#computeLevels(List, AtomicBoolean)
   * @see EntropyMap#heatByLine(int[], byte[])
   */
  private void compute(int version, List<byte[]> raws, int[] lengths) {
    byte[] levels = EntropyMap.computeLevels(raws, mCancel);
    if (levels != null) {
      byte[] heat = EntropyMap.heatByLine(lengths, levels);
      mHandler.post(() -> publish(version, heat));
    }
  }

  /**
   * Publishes a computed heat map if it is still up to date.
   *
   * <p>
   * Runs on the UI thread. If the given version no longer matches the current one, the
   * content has changed since the computation started and the result is discarded.
   * Otherwise, it becomes the current heat map and the {@code onChanged} callback is invoked.
   * </p>
   *
   * @param version the version of the request that produced the heat map
   * @param heat    the computed heat map, one byte per line
   */
  private void publish(int version, byte[] heat) {
    if (version == mVersion.get()) {          // otherwise the content has changed: stale result
      mHeat.set(heat);
      mOnChanged.run();
    }
  }
}
