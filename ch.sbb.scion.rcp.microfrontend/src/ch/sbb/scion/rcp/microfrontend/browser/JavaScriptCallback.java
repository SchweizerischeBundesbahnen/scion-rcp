package ch.sbb.scion.rcp.microfrontend.browser;

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
 * The function's name can be obtained via {@link JavaScriptCallback#name}.
 * 
 * Wraps a SWT {@link BrowserFunction}.
 */
public class JavaScriptCallback implements IDisposable {

  public final String name;

  private CompletableFuture<Browser> whenBrowser;
  private BrowserFunction browserFunction;
  private Consumer<Object[]> callback;

  public JavaScriptCallback(Browser browser, Consumer<Object[]> callback) {
    this(CompletableFuture.completedFuture(browser), callback);
  }

  public JavaScriptCallback(CompletableFuture<Browser> whenBrowser, Consumer<Object[]> callback) {
    this.whenBrowser = whenBrowser;
    this.name = toValidJavaScriptIdentifier("__scion_rcp_browserfunction_" + UUID.randomUUID());
    this.callback = callback;
  }

  /**
   * Installs this callback on the {Window} of the currently loaded document in
   * the browser. Applications must dispose this function if not used anymore.
   */
  public JavaScriptCallback install() {
    install(false);
    return this;
  }

  /**
   * Installs this callback on the {Window} of the currently loaded document in
   * the browser.
   * 
   * This callback is automatically uninstalled after first invocvation.
   */
  public JavaScriptCallback installOnce() {
    install(true);
    return this;
  }

  private void install(boolean once) {
    whenBrowser.thenAccept(browser -> {
      browserFunction = new BrowserFunction(browser, name) {
        @Override
        public Boolean function(Object[] arguments) {
          try {
            callback.accept(arguments);
          }
          finally {
            if (once) {
              dispose();
            }
          }
          return true;
        }
      };
    });
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