package ch.sbb.scion.rcp.workbench.popup;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.swt.graphics.Point;

import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Properties;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopup;
import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupOrigin;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;

public class Popup implements ISciWorkbenchPopup {

  public final PopupInput input;

  public final CompletableFuture<Object> whenClose = new CompletableFuture<>();

  private final Capability capability;

  private final PopupCloseStrategy closeStrategy;

  private final Point initialSize;

  @Inject
  private SciMessageClient messageClient;

  private Popup(final PopupInput input, final Capability capability, final PopupCloseStrategy closeStrategy, final Point size) {
    this.input = input;
    this.capability = capability;
    this.closeStrategy = closeStrategy;
    this.initialSize = size;
  }

  @Override
  public Capability getCapability() {
    return capability;
  }

  @Override
  public Map<String, Object> getParams() {
    return input.params;
  }

  @Override
  public void close(final Object result) {
    this.whenClose.complete(result);
  }

  @Override
  public void closeWithException(final Exception exception) {
    Objects.requireNonNull(exception, "Exception must not be null!");
    this.whenClose.complete(exception instanceof PopupException ? exception : new PopupException(exception));
  }

  @Override
  public String getPopupId() {
    return input.popupId;
  }

  @Override
  public Optional<Point> getInitialSize() {
    return Optional.ofNullable(initialSize);
  }

  @Override
  public boolean closeOnEscape() {
    return closeStrategy.onEscape.booleanValue();
  }

  @Override
  public boolean closeOnFocusLost() {
    return closeStrategy.onFocusLost.booleanValue();
  }

  @Override
  public Optional<String> getReferrerViewId() {
    return Optional.ofNullable(input.referrer).map(referrer -> referrer.viewId);
  }

  @Override
  public ISubscription observePopupOrigin(final ISubscriber<SciWorkbenchPopupOrigin> subscriber) {
    var topic = String.format("ɵworkbench/popups/%s/origin", input.popupId);
    return messageClient.subscribe(topic, new ISubscriber<TopicMessage<PopupOrigin>>() {

      @Override
      public void onNext(final TopicMessage<PopupOrigin> next) {
        subscriber.onNext(next.body == null ? null : next.body.toSciWorkbenchPopupOrigin());
      }

      @Override
      public void onError(final Exception e) {
        subscriber.onError(e);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
      }

    }, PopupOrigin.class);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private static final String VALUE_GROUP_NAME = "value";
    private static final Pattern PX_VALUE_PATTERN = Pattern.compile(String.format("(?<%s>\\d+)px", VALUE_GROUP_NAME));

    private String popupId;

    private Capability capability;

    private Map<String, Object> params;

    private PopupCloseStrategy closeStrategy;

    private PopupReferrer referrer;

    public Builder popupId(final String popupId) {
      this.popupId = popupId;
      return this;
    }

    public Builder capability(final Capability capability) {
      this.capability = capability;
      return this;
    }

    public Builder params(final Map<String, Object> params) {
      this.params = params;
      return this;
    }

    public Builder closeStrategy(final PopupCloseStrategy closeStrategy) {
      this.closeStrategy = closeStrategy;
      return this;
    }

    public Builder referrer(final PopupReferrer referrer) {
      this.referrer = referrer;
      return this;
    }

    public Popup build() {
      Objects.requireNonNull(popupId);
      Objects.requireNonNull(capability);
      Objects.requireNonNull(params);
      Objects.requireNonNull(closeStrategy);

      var popupCapability = createPopupCapability(capability);
      var initialSize = coercePoint(popupCapability.properties.get("size"));

      var input = new PopupInput().popupId(popupId).capability(popupCapability).params(params).referrer(referrer)
          .closeOnFocusLost(closeStrategy.onFocusLost.booleanValue());
      var popup = new Popup(input, capability, closeStrategy, initialSize);
      ContextInjectors.inject(popup);
      return popup;
    }

    private static Capability createPopupCapability(final Capability capability) {
      var properties = new Properties();
      if (capability.properties != null) {
        properties.set("path", capability.properties.get("path")).set("size", createPopupSize(capability.properties.get("size")))
            .set("cssClass", capability.properties.get("cssClass"));
      }

      var popupCapability = new Capability().type(capability.type).qualifier(capability.qualifier).params(capability.params)
          .isPrivate(capability.isPrivate).description(capability.description).properties(properties);
      popupCapability.metadata = capability.metadata;
      return popupCapability;
    }

    @SuppressWarnings("unchecked")
    private static PopupSize createPopupSize(final Object size) {
      if (size == null) {
        return null;
      }
      var sizeMap = (Map<String, String>) size;
      return new PopupSize().width(sizeMap.get("width")).height(sizeMap.get("height"));
    }

    private static Point coercePoint(final PopupSize popupSize) {
      return (popupSize == null || popupSize.width == null || popupSize.height == null) ? null
          : new Point(pixelValueToInt(popupSize.width), pixelValueToInt(popupSize.height));
    }

    private static int pixelValueToInt(final String pixelValue) {
      var matcher = PX_VALUE_PATTERN.matcher(pixelValue);
      matcher.find();
      return Integer.parseInt(matcher.group(VALUE_GROUP_NAME));
    }

  }

  private static class PopupSize {

    public String width;

    public String height;

    public PopupSize width(final String widht) {
      this.width = widht;
      return this;
    }

    public PopupSize height(final String height) {
      this.height = height;
      return this;
    }

  }

}
