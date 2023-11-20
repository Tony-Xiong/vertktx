package com.xyz.vertjx.demo1;

import com.xyz.vertjx.Main;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RxCollectionService extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(CollectionService.class);

  WebClient webClient;

  private final String host = "127.0.0.1";
  @Override
  public void start() {
    webClient = WebClient.create(vertx);
    logger.info("started RxCollectionService at {} port", 8081);
    Single<HttpServer> single = vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .rxListen(8081);
    single.subscribe(
      httpServer -> logger.info("started CollectionService at {} port", 8081), throwable -> logger.error("error:", throwable)
    );
  }

  private void handleRequest(HttpServerRequest request) {
    logger.info(request.path());
    if ("/".equals(request.path())) {
      this.handle(request);
    } else {
      request.response().setStatusCode(404).end();
    }
  }

  private void handle(HttpServerRequest request) {
    List<Single<JsonObject>> list = new LinkedList<>();
    for (Integer port : Main.ports) {
      list.add(this.fetchTemperature(port));
    }
    Single<JsonObject> data = Single.zip(list, iter -> {
      JsonArray array = new JsonArray();
      for (Object item : iter) {
        JsonObject json = (JsonObject) item;
        array.add(json);
      }
      return new JsonObject().put("data", array);
    });

    sendToSnapshot(data).subscribe(json -> {
      request.response()
        .putHeader("Content-Type", "application/json")
        .end(json.encode());
    }, err -> {
      logger.error("error: ", err);
      request.response().setStatusCode(500).end(err.getMessage());
    });
  }

  private Single<JsonObject> fetchTemperature(Integer sensorPort) {
    return webClient.get(sensorPort, host, "/").send().map(HttpResponse::bodyAsJsonObject);
  }

  private Single<JsonObject> sendToSnapshot(Single<JsonObject> data) {
    data.map(aaa->aaa);
    data.flatMap(bbb->Single.just(bbb));
    return data.flatMap(json -> webClient.post(4000, host, "/").rxSendJsonObject(json)).map(HttpResponse::bodyAsJsonObject);
  }

}
