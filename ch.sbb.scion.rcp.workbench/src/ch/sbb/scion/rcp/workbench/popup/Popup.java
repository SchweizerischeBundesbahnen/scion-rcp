package ch.sbb.scion.rcp.workbench.popup;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.swt.graphics.Point;

import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopup;
import ch.sbb.scion.rcp.workbench.WorkbenchPopupOrigin;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;
import ch.sbb.scion.rcp.workbench.internal.WorkbenchCommands;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class Popup implements IWorkbenchPopup {

  private final String popupId;

  private final Map<String, Object> params;

  public final CompletableFuture<Object> whenClose = new CompletableFuture<>();

  private final Capability capability;

  private final PopupCloseStrategy closeStrategy;

  private final Point initialSize;

  private final PopupReferrer referrer;

  private Object result;

  @Inject
  private MessageClient messageClient;

  private Popup(final String popupId, final Map<String, Object> params, final Capability capability, final PopupCloseStrategy closeStrategy,
      final Point size, final PopupReferrer referrer) {
    this.popupId = popupId;
    this.params = params;
    this.capability = capability;
    this.closeStrategy = closeStrategy;
    this.initialSize = size;
    this.referrer = referrer;
  }

  @Override
  public Capability getCapability() {
    return capability;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  public void closeOnFocusLoss() {
    this.close(result);
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
  public void setResult(final Object result) {
    this.result = result;
  }

  @Override
  public String getPopupId() {
    return popupId;
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
  public PopupReferrer getReferrer() {
    return referrer;
  }

  @Override
  public ISubscription observePopupOrigin(final ISubscriber<WorkbenchPopupOrigin> subscriber) {
    return messageClient.subscribe(WorkbenchCommands.popupOriginTopic(popupId), DoublePrecisionPopupOrigin.class,
        new ISubscriber<TopicMessage<DoublePrecisionPopupOrigin>>() {

          @Override
          public void onNext(final TopicMessage<DoublePrecisionPopupOrigin> next) {
            subscriber.onNext(next.body() == null ? null : next.body().toSciWorkbenchPopupOrigin());
          }

          @Override
          public void onError(final Exception e) {
            subscriber.onError(e);
          }

          @Override
          public void onComplete() {
            subscriber.onComplete();
          }

        });
  }

  @Override
  public ISubscription observeFocus(final ISubscriber<Boolean> subscriber) {
    return messageClient.subscribe(WorkbenchCommands.popupFocusedTopic(popupId), Boolean.class, new ISubscriber<TopicMessage<Boolean>>() {

      @Override
      public void onNext(final TopicMessage<Boolean> next) {
        subscriber.onNext(next.body());
      }

      @Override
      public void onError(final Exception e) {
        subscriber.onError(e);
      }

      @Override
      public void onComplete() {
        subscriber.onComplete();
      }
    });
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
      Objects.requireNonNull(referrer);

      var popup = new Popup(popupId, params, capability, closeStrategy, getInitialSizeAsPoint(capability), referrer);
      ContextInjectors.inject(popup);
      return popup;
    }

    private static Point getInitialSizeAsPoint(final Capability capability) {
      var properties = capability.properties();
      if (properties == null || properties.get("size") == null) {
        return null;
      }
      @SuppressWarnings("unchecked")
      // currently, the intent message will always originate from the JS world and be deserialized, therefore, hence, the 'size' object will be a map:
      var size = (Map<String, String>) properties.get("size");
      var width = size.get("width");
      var height = size.get("height");
      return width == null || height == null ? null : new Point(pixelValueToInt(width), pixelValueToInt(height));
    }

    private static int pixelValueToInt(final String pixelValue) {
      var matcher = PX_VALUE_PATTERN.matcher(pixelValue);
      matcher.find();
      return Integer.parseInt(matcher.group(VALUE_GROUP_NAME));
    }

  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class DoublePrecisionPopupOrigin {

    private Double x;

    private Double y;

    private Double top;

    private Double right;

    private Double bottom;

    private Double left;

    private Double width;

    private Double height;

    public WorkbenchPopupOrigin toSciWorkbenchPopupOrigin() {
      return WorkbenchPopupOrigin.builder().x(intValueOrNull(x)).y(intValueOrNull(y)).top(intValueOrNull(top))
          .bottom(intValueOrNull(bottom)).left(intValueOrNull(left)).width(intValueOrNull(width)).height(intValueOrNull(height)).build();
    }

    @Override
    public String toString() {
      return String.format("{x=%.4f, y=%.4f, top=%.4f, right=%.4f, bottom=%.4f, left=%.4f, width=%.4f, height=%.4f}", x, y, top, right,
          bottom, left, width, height);
    }

    private static Integer intValueOrNull(final Double doubleValue) {
      return doubleValue == null ? null : Integer.valueOf(doubleValue.intValue());
    }

  }
}
