package ch.sbb.scion.rcp.microfrontend.browser;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

import ch.sbb.scion.rcp.microfrontend.model.IDisposable;

/**
 * Allows interaction from JavaScript with Java code.
 * 
 * Injects a function to the {Window} of the currently loaded document that can
 * be invoked from JavaScript. Invoking that function calls the passed callback.
 * 
 * The function's name can be obtained via {@link JavaCallback#name}.
 * 
 * Wraps a SWT {@link BrowserFunction}.
 */
public class JavaCallback implements IDisposable {

  public final String name;

  private CompletableFuture<Browser> whenBrowser;
  private BrowserFunction browserFunction;
  private Consumer<Object[]> callback;

  public JavaCallback(Browser browser, Consumer<Object[]> callback) {
    this(CompletableFuture.completedFuture(browser), callback);
  }

  public JavaCallback(CompletableFuture<Browser> whenBrowser, Consumer<Object[]> callback) {
    this.whenBrowser = whenBrowser;
    this.name = toValidJavaScriptIdentifier("__scion_rcp_browserfunction_" + UUID.randomUUID());
    this.callback = callback;
  }

  /**
   * Installs this callback on the {Window} of the currently loaded document in
   * the browser. Applications must dispose this function if not used anymore.
   * 
   * This method resolves to the callback when installed the callback.
   */
  public CompletableFuture<JavaCallback> install() {
    return install(false);
  }

  /**
   * Installs this callback on the {Window} of the currently loaded document in
   * the browser.
   * 
   * This callback is automatically uninstalled after first invocvation.
   * 
   * This method resolves to the callback when installed the callback.
   */
  public CompletableFuture<JavaCallback> installOnce() {
    return install(true);
  }

  private CompletableFuture<JavaCallback> install(boolean once) {
    return whenBrowser
        .thenAccept(browser -> {
          browserFunction = new BrowserFunction(browser, name) {
            @Override
            public Boolean function(Object[] arguments) {
              if (once) {
                dispose();
              }
              // Invoke the callback asynchronously to first complete the invocation of this browser function.
              // Otherwise, creating a new {@link Browser} instance in the callback would lead to a deadlock.
              browser.getDisplay().asyncExec(() -> callback.accept(arguments));
              return true;
            }
          };
        })
        .thenApply(browser -> this);
  }

  /**
   * Adds this {@link JavaCallback} to the passed collection.
   */
  public JavaCallback addTo(Collection<IDisposable> disposables) {
    disposables.add(this);
    return this;
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
}