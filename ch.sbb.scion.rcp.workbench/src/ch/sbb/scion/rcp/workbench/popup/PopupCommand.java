package ch.sbb.scion.rcp.workbench.popup;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class PopupCommand {

  public String popupId;

  @SerializedName("context")
  public PopupReferrer referrer;

  public PopupCloseStrategy closeStrategy;

  public PopupCommand() {
    this.popupId = UUID.randomUUID().toString();
  }

  public PopupCommand context(final PopupReferrer referrer) {
    this.referrer = referrer;
    return this;
  }

  public PopupCommand closeStrategy(final PopupCloseStrategy closeStrategy) {
    this.closeStrategy = closeStrategy;
    return this;
  }

}
