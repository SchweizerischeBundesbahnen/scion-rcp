package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/Intent.html"
 */
public class Intent {

  public String type;
  public Qualifier qualifier;
  public Map<String, Object> params;

  public String getType() {
    return type;
  }

  public Intent type(final String type) {
    this.type = type;
    return this;
  }

  public Qualifier getQualifier() {
    return qualifier;
  }

  public Intent qualifier(final Qualifier qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public Intent params(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
