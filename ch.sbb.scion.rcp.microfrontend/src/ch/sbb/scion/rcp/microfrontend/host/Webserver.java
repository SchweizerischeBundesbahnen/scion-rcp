package ch.sbb.scion.rcp.microfrontend.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

/**
 * Serves passed resources on a random port.
 */
public class Webserver {

  private static final Pattern HTTP_GET_PATTERN = Pattern.compile("GET /(?<resource>.*) HTTP");
  private static final ILog LOGGER = Platform.getLog(Webserver.class);
  private static final Resource NULL_RESOURCE = new Resource(null, null, null);

  private Map<String, Resource> resources;
  private ExecutorService executor;
  private ServerSocket serverSocket;

  public Webserver(Map<String, Resource> resources) {
    this.resources = resources;
  }

  public Webserver start() {
    serverSocket = createServerSocket();
    executor = Executors.newFixedThreadPool(2);
    executor.execute(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try (var socket = serverSocket.accept()) {
          handleRequest(socket);
        }
        catch (IOException e) {
          if (!serverSocket.isClosed()) {
            LOGGER.error("Failed to handle HTTP request.", e);
          }
        }
      }
    });
    blockUntilStarted();
    return this;
  }

  private void handleRequest(Socket socket) throws IOException {
    var resource = findRequestedResource(socket);
    try (var out = new PrintWriter(socket.getOutputStream())) {
      if (resource == NULL_RESOURCE) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain; utf-8");
      }
      else if (resource == null) {
        out.println("HTTP/1.1 404 Not Found");
      }
      else {
        out.println("HTTP/1.1 200 OK");
        out.println(String.format("Content-Type: %s; charset=%s", resource.contentType, resource.encoding));
        out.println(String.format("Content-Disposition: inline; filename=\"%s\"", resource.url.getFile()));
        out.println("");
        out.println(new String(resource.url.openStream().readAllBytes()));
      }
    }
  }

  private ServerSocket createServerSocket() {
    try {
      return new ServerSocket(0, 200, InetAddress.getLoopbackAddress());
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to start HTTP server.", e);
    }
  }

  private Resource findRequestedResource(Socket socket) throws IOException {
    var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    var httpRequestLine = in.readLine();
    if (httpRequestLine == null) {
      return null;
    }
    var matcher = HTTP_GET_PATTERN.matcher(httpRequestLine);
    if (matcher.find()) {
      var resouce = matcher.group("resource");
      if ("".equals(resouce)) {
        return NULL_RESOURCE;
      }
      return resources.get(matcher.group("resource"));
    }
    return null;
  }

  public void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }

    if (serverSocket != null) {
      try {
        serverSocket.close();
      }
      catch (IOException e) {
        LOGGER.error("Failed to stop HTTP server.", e);
      }
    }
  }

  private void blockUntilStarted() {
    // Perform ping request.
    try {
      new URL("http", serverSocket.getInetAddress().getHostName(), getPort(), "").getContent();
    }
    catch (IOException e) {
      LOGGER.error("Failed to wait until webserver is ready.", e);
    }
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public static class Resource {

    public URL url;
    public String contentType;
    public String encoding;

    public Resource(URL url, String contentType, String encoding) {
      this.url = url;
      this.contentType = contentType;
      this.encoding = encoding;
    }
  }
}