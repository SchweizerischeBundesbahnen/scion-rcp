package ch.sbb.scion.rcp.microfrontend.internal;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import ch.sbb.scion.rcp.microfrontend.Activator;

public interface Resources {

  public static URL get(String filename) {
    return Activator.getDefault().getBundle().getResource(filename);
  }

  public static String readString(String filename) {
    try {
      var url = Activator.getDefault().getBundle().getResource(filename);
      return new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
