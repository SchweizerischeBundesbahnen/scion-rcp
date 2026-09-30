/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;

/**
 * Provides some useful helpers for working with {@link CompletableFuture}s.
 */
public final class CompletableFutures {

  /**
   * Blocks until the given {@link CompletableFuture} has completed and returns its result. If invoked from the UI thread, continuously
   * dispatches pending work from the operating system's event queue, supporting awaiting a future in the UI thread even if the future is
   * also executing in the UI thread. Otherwise, a deadlock would occur since blocking the UI thread for the future to complete would
   * prevent the future from continuing execution.
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

  public static RuntimeException launderException(final Throwable exception) {
    if (exception instanceof RuntimeException re) {
      return re;
    }
    return new CompletionException(exception);
  }

  public static void rethrow(final Throwable exception) {
    if (exception != null) {
      throw launderException(exception);
    }
  }

  public static <T> BiConsumer<T, Throwable> logOnException(final Class<?> loggingClass) {
    return (result, ex) -> {
      if (ex != null) {
        Platform.getLog(loggingClass).error("Failed", ex);
      }
    };
  }

  private CompletableFutures() {
    // utility
  }

}
