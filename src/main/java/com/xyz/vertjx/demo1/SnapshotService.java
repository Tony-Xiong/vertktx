package com.xyz.vertjx.demo1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotService extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(SnapshotService.class);

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(req -> {
        if (badRequest(req)) {
          req.response().setStatusCode(400).end();
        }
        req.bodyHandler(buffer -> {
          logger.info("Received body with data {}", buffer.toJson());
          req.response().end(buffer);
        });
      }).listen(4000);
    logger.info("started SnapshotService at {} port", 4000);
  }

  private boolean badRequest(HttpServerRequest req) {
    return !req.method().equals(HttpMethod.POST) || !"application/json".equals(req.getHeader("Content-Type"));
  }

}
