package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/NavigationOptions.html"
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class NavigationOptions {

  /**
   * Specifies the routing target. If not specifying an outlet and if navigating in the context of an outlet, that outlet will be used as
   * the navigation target, or the primary outlet otherwise.
   */
  private String outlet;
  /**
   * Specifies the base URL to resolve a relative url. If not specified, the current window location is used to resolve a relative path.<br>
   * <br>
   * Note that this property has no effect if navigating via intent
   */
  private String relativeTo;
  /**
   * Specifies the parameters that, if navigating via URL, are used to substitute named URL parameters or that are passed along with the
   * intent if navigating via intent.
   */
  private Map<String, ?> params;
  /**
   * Instructs the router to push a state to the browser's session history stack, allowing the user to use the back button to navigate back
   * in the outlet. By default, this behavior is disabled.
   */
  private Boolean pushStateToSessionHistoryStack;

}
