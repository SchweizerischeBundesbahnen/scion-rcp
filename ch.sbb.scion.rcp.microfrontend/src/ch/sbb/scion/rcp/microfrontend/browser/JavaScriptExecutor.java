package ch.sbb.scion.rcp.microfrontend.browser;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;

import ch.sbb.scion.rcp.microfrontend.script.Script;

public class JavaScriptExecutor {

  private CompletableFuture<Browser> browser;
  private boolean logToConsole;
  private boolean asyncFunction;
  private Script browserScript;

  public JavaScriptExecutor(Browser browser, String script) {
    this(CompletableFuture.completedFuture(browser), script);
  }

  public JavaScriptExecutor(CompletableFuture<Browser> browser, String script) {
    this.browser = browser;
    this.browserScript = new Script(script);
  }

  public JavaScriptExecutor replacePlaceholder(String name, Object value) {
    return replacePlaceholder(name, value, 0);
  }

  public JavaScriptExecutor replacePlaceholder(String name, Object value, int flags) {
    browserScript.replacePlaceholder(name, value, flags);
    return this;
  }

  public JavaScriptExecutor runInsideAsyncFunction() {
    asyncFunction = true;
    return this;
  }

  public JavaScriptExecutor printScriptToConsole() {
    logToConsole = true;
    return this;
  }

  public CompletableFuture<Void> execute() {
    var script = browserScript.substitute();
    var asyncToken = asyncFunction ? " async " : "";
    var iife = "(" + asyncToken + "() => { " + script + " })();";

    if (logToConsole) {
      Platform.getLog(JavaScriptExecutor.class).info(iife);
    }

    return browser.thenAccept(browser -> {
      var success = browser.execute(iife);
      if (!success) {
        Platform.getLog(JavaScriptExecutor.class).error("Failed to inject or execute JavaScript: " + iife);
      }
    });
  }
}
