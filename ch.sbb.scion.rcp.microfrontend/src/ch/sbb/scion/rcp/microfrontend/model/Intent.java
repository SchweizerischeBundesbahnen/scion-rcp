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
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/Intent.html">Intent</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Intent {

  private String type;
  private Qualifier qualifier;
  private Map<String, Object> params;

}
