package com.xyz.vertjx.demo1;

import com.xyz.vertjx.Main;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CollectionService extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(CollectionService.class);
  private WebClient webClient;

  private final String host = "127.0.0.1";

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .listen(8080);
    webClient = WebClient.create(vertx);
    logger.info("started CollectionService at {} port", 8080);
  }

  private void handleRequest(HttpServerRequest request) {
    logger.info("request: {}, date: {}", request.path(), System.currentTimeMillis());
    if ("/".equals(request.path())) {
      this.handleWithFuture(request);
    } else {
      request.response().setStatusCode(404).end();
    }
  }

  private void handleWithFuture(HttpServerRequest request) {
    Set<Integer> sensorPorts = Main.ports;
    List<Future<?>> futures = new LinkedList<>();

    for (Integer sensorPort : sensorPorts) {
      futures.add(fetchTemperature(sensorPort));
    }
    Future.all(futures).flatMap(this::sendToSnapshot)
      .onSuccess(ar -> request.response().end(ar))
      .onFailure(err -> request.response().setStatusCode(500).end(err.getMessage()));
  }

  private Future<Buffer> fetchTemperature(Integer sensorPort) {
    return webClient.get(sensorPort, host, "/").send().map(HttpResponse::body);
  }

  private Future<Buffer> sendToSnapshot(CompositeFuture temps) {
    List<Buffer> tempsList = temps.list();
    var data = new JsonArray(tempsList.stream().map(Buffer::toJsonObject).toList());
    return webClient.post(4000, host, "/").sendJson(data).map(HttpResponse::body);
  }

}
