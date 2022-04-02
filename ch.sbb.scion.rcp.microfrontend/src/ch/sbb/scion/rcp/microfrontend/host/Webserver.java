package ch.sbb.scion.rcp.microfrontend.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

  private Map<String, Resource> resources;
  private ExecutorService executor;
  private ServerSocket serverSocket;
  private CompletableFuture<Void> whenStarted = new CompletableFuture<>();

  public Webserver(Map<String, Resource> resources) {
    this.resources = resources;
  }

  public Webserver start() {
    serverSocket = createServerSocket();
    executor = Executors.newSingleThreadExecutor();
    executor.execute(() -> {
      whenStarted.complete(null);
      while (!Thread.currentThread().isInterrupted()) {
        try {
          handleRequest(serverSocket.accept());
        }
        catch (IOException e) {
          if (!serverSocket.isClosed()) {
            LOGGER.error("Failed to handle HTTP request.", e);            
          }
        }
      }
    });
    return this;
  }

  private void handleRequest(Socket socket) throws IOException {
    var resource = findRequestedResource(socket);
    try (var out = new PrintWriter(socket.getOutputStream())) {
      if (resource == null) {
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
      return new ServerSocket(0);
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
    if (matcher.find() && resources.containsKey(matcher.group("resource"))) {
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

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public CompletableFuture<Void> whenStarted() {
    return this.whenStarted;
  }

  public static record Resource(URL url, String contentType, String encoding) {
  }
}