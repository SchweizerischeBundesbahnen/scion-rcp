package ch.sbb.scion.rcp.microfrontend.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

import ch.sbb.scion.rcp.microfrontend.model.IDisposable;

/**
 * Wraps a {@link BrowserFunction} to receive messages sent by the browser.
 */
public class BrowserCallback implements IDisposable {

  public final String name;

  private BrowserFunction browserFunction;
  private List<Consumer<Object[]>> callbacks = new ArrayList<>();

  public BrowserCallback(Browser browser) {
    this(CompletableFuture.completedFuture(browser), Options.EMPTY);
  }

  public BrowserCallback(CompletableFuture<Browser> whenBrowser) {
    this(whenBrowser, Options.EMPTY);
  }

  public BrowserCallback(Browser browser, Options options) {
    this(CompletableFuture.completedFuture(browser), options);
  }

  public BrowserCallback(CompletableFuture<Browser> whenBrowser, Options options) {
    name = toValidJavaScriptIdentifier("__scion_rcp_browserfunction_" + UUID.randomUUID());
    if (options.callback != null) {
      callbacks.add(options.callback);
    }

    whenBrowser.thenAccept(browser -> {
      browserFunction = new BrowserFunction(browser, name) {
        @Override
        public Boolean function(Object[] arguments) {
          callbacks.forEach(listener -> listener.accept(arguments));

          if (options.once) {
            browserFunction.dispose();
          }
          return true;
        }
      };
      browser.addDisposeListener(event -> browserFunction = null);
    });
  }

  public IDisposable addCallback(Consumer<Object[]> callback) {
    callbacks.add(callback);
    return () -> callbacks.remove(callback);
  }
  
  @Override
  public String toString() {
    return name;
  }

  /**
   * Disposes of the resources associated with this BrowserFunction. Applications
   * must dispose of all BrowserFunctions that they create.
   * <p>
   * Note that disposing a Browser automatically disposes all BrowserFunctions
   * associated with it.
   * </p>
   */
  @Override
  public void dispose() {
    if (browserFunction != null && !browserFunction.isDisposed()) {
      browserFunction.dispose();
      browserFunction = null;
    }
  }

  private String toValidJavaScriptIdentifier(String name) {
    if (Pattern.matches("^\\d.+", name)) {
      throw new IllegalArgumentException(
          String.format("JavaScript identifier must not start with a digit. [name=%s]", name));
    }
    return name.replaceAll("[^\\w\\d\\$]", "_");
  }

  public static class Options {

    public static final Options EMPTY = new Options();

    private Consumer<Object[]> callback;
    private boolean once;

    public Options onCallback(Consumer<Object[]> callback) {
      this.callback = callback;
      return this;
    }

    public Options once() {
      once = true;
      return this;
    }
  }
}