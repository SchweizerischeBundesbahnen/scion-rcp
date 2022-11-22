package ch.sbb.scion.rcp.microfrontend.script;

public interface Scripts {

  public static final String Storage = "window['__SCION_RCP'].storage";

  public static class Refs {
    public static final String MicrofrontendPlatform = "window['__SCION_RCP'].refs.MicrofrontendPlatform";
    public static final String MessageClient = "window['__SCION_RCP'].refs.MessageClient";
    public static final String IntentClient = "window['__SCION_RCP'].refs.IntentClient";
    public static final String OutletRouter = "window['__SCION_RCP'].refs.OutletRouter";
    public static final String ManifestService = "window['__SCION_RCP'].refs.ManifestService";
    public static final String Beans = "window['__SCION_RCP'].refs.Beans";
    public static final String MessageInterceptor = "window['__SCION_RCP'].refs.MessageInterceptor";
    public static final String IntentInterceptor = "window['__SCION_RCP'].refs.IntentInterceptor";
    public static final String TopicMatcher = "window['__SCION_RCP'].refs.TopicMatcher";
    public static final String QualifierMatcher = "window['__SCION_RCP'].refs.QualifierMatcher";
    public static final String UUID = "window['__SCION_RCP'].refs.UUID";
  }

  public static class Helpers {
    public static final String toJson = "window['__SCION_RCP'].helpers.toJson";
    public static final String fromJson = "window['__SCION_RCP'].helpers.fromJson";
  }
}
