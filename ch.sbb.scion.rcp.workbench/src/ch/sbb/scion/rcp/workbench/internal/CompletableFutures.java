package ch.sbb.scion.rcp.workbench.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.widgets.Display;

public class CompletableFutures {

  private CompletableFutures() {
  }

  /**
   * Blocks until the given {@link CompletableFuture} has completed and returns its result.
   * 
   * If invoked from the UI thread, continuously dispatches pending work from the operating system's event queue,
   * supporting awaiting a future in the UI thread even if the future is also executing in the UI thread. Otherwise,
   * a deadlock would occur since blocking the UI thread for the future to complete would prevent the future from
   * continuing execution.
   */
  public static <T> T await(final CompletableFuture<T> completableFuture) {
    // If not invoked from the UI thread, simply await the future.
    if (Display.getCurrent() == null) {
      return completableFuture.join();
    }

    // Continuously dispatch pending work from the operating system's event queue while waiting for the future to complete.
    while (!completableFuture.isDone()) {
      Display.getCurrent().readAndDispatch();
    }
    return completableFuture.join();
  }
}
