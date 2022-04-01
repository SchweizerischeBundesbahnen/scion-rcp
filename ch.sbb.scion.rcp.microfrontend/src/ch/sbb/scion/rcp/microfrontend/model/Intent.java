package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/Intent.html
 */
public class Intent {

  public String type;
  public Map<String, Object> qualifier;
  public Map<String, Object> params;

  public String getType() {
    return type;
  }

  public Intent type(String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getQualifier() {
    return qualifier;
  }

  public Intent qualifier(Map<String, Object> qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public Intent params(Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
