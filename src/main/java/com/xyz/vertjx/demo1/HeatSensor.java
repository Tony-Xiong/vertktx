package com.xyz.vertjx.demo1;

import com.xyz.vertjx.Main;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeatSensor extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(HeatSensor.class);
  private final Random random = new Random();
  private final String sensorId = UUID.randomUUID().toString();
  private double temperature = 21.0;

  private void scheduleNextUpdate() {
    vertx.setTimer(random.nextInt(5000) + 1000, this::update);
  }

  private void update(long timerId) {
    temperature = temperature + (delta() / 10);
    vertx.eventBus().publish("sensor.updates", "{\"id\":\"" + sensorId + "\",\"temp\":" + temperature + "}");
    scheduleNextUpdate();
  }

  private double delta() {
    if (random.nextInt() > 0) {
      return random.nextGaussian();
    } else {
      return -random.nextGaussian();
    }
  }

  @Override
  public void start() {
    var port = this.creatPort();
    vertx.createHttpServer().requestHandler(this::handleRequest).listen(port);
    logger.info("started Heat, port: {}", port);
  }

  private int creatPort() {
      while (true){
        int port = random.nextInt(3000, 3030);
        Set<Integer> ports = Main.ports;
        if (!ports.contains(port)) {
          ports.add(port);
          return port;
        }
      }
  }

  private void handleRequest(HttpServerRequest request) {
    logger.info("request: {}, date: {}", request.path(), new Date());
    var data = new JsonObject()
      .put("id", sensorId)
      .put("temp", temperature);
    request.response()
      .putHeader("Content-Type", "application/json")
      .end(data.encode());
    logger.info("data : {}", data);
  }

}
