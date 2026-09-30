/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Provides some useful helpers for working with {@link CompletableFuture}s.
 */
public final class CompletableFutures {

  static RuntimeException launderException(final Throwable exception) {
    if (exception instanceof RuntimeException re) {
      return re;
    }
    return new CompletionException(exception);
  }

  static void rethrow(final Throwable exception) {
    if (exception != null) {
      throw launderException(exception);
    }
  }

  private CompletableFutures() {
    // utility
  }

}
