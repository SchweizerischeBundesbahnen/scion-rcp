package ch.sbb.scion.rcp.microfrontend.browser;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;

import ch.sbb.scion.rcp.microfrontend.script.Script;

public class BrowserScriptExecutor {

  private CompletableFuture<Browser> browser;
  private boolean logToConsole;
  private boolean asyncFunction;
  private Script browserScript;

  public BrowserScriptExecutor(Browser browser, String script) {
    this(CompletableFuture.completedFuture(browser), script);
  }

  public BrowserScriptExecutor(CompletableFuture<Browser> browser, String script) {
    this.browser = browser;
    this.browserScript = new Script(script);
  }

  public BrowserScriptExecutor replacePlaceholder(String name, Object value) {
    return replacePlaceholder(name, value, 0);
  }

  public BrowserScriptExecutor replacePlaceholder(String name, Object value, int flags) {
    browserScript.replacePlaceholder(name, value, flags);
    return this;
  }

  public BrowserScriptExecutor runInsideAsyncFunction() {
    asyncFunction = true;
    return this;
  }

  public BrowserScriptExecutor printScriptToConsole() {
    logToConsole = true;
    return this;
  }

  public CompletableFuture<Void> execute() {
    var script = browserScript.substitute();
    var asyncToken = asyncFunction ? " async " : "";
    var iife = "(" + asyncToken + "() => { " + script + " })();";

    if (logToConsole) {
      Platform.getLog(BrowserScriptExecutor.class).info(iife);
    }

    return browser.thenAccept(browser -> {
      var success = browser.execute(iife);
      if (!success) {
        Platform.getLog(BrowserScriptExecutor.class).error("Failed to inject or execute JavaScript: " + iife);
      }
    });
  }
}
