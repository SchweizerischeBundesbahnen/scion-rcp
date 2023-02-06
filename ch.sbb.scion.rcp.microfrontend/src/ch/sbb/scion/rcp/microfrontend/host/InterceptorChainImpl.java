package ch.sbb.scion.rcp.microfrontend.host;

import org.eclipse.swt.browser.Browser;

import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.interceptor.InterceptorChain;
import ch.sbb.scion.rcp.microfrontend.model.Message;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;

class InterceptorChainImpl implements InterceptorChain {

  private Browser hostBrowser;
  private String nextCallbackName;

  InterceptorChainImpl(Browser hostBrowser, String nextCallbackName) {
    this.hostBrowser = hostBrowser;
    this.nextCallbackName = nextCallbackName;
  }

  @Override
  public void doContinue(Message messageOut) {
    new JavaScriptExecutor(hostBrowser, "/@@storage@@/['/@@nextCallbackName@@/'](/@@helpers.fromJson@@/('/@@messageOut@@/'));")
        .replacePlaceholder("nextCallbackName", nextCallbackName).replacePlaceholder("messageOut", messageOut, Flags.ToJson)
        .replacePlaceholder("storage", Scripts.Storage).replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
  }

  @Override
  public void doSwallow() {
    new JavaScriptExecutor(hostBrowser, "/@@storage@@/['/@@nextCallbackName@@/'](null);")
        .replacePlaceholder("nextCallbackName", nextCallbackName).replacePlaceholder("storage", Scripts.Storage).runInsideAsyncFunction()
        .execute();
  }

  @Override
  public void doReject(String error) {
    new JavaScriptExecutor(hostBrowser, "/@@storage@@/['/@@nextCallbackName@@/'](new Error(/@@helpers.fromJson@@/('/@@error@@/')));")
        .replacePlaceholder("nextCallbackName", nextCallbackName).replacePlaceholder("storage", Scripts.Storage)
        .replacePlaceholder("error", error, Flags.ToJson).replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
  }
}
