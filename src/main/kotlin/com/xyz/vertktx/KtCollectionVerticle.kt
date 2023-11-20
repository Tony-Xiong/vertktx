package com.xyz.vertktx

import com.xyz.vertjx.Main
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.predicate.ResponsePredicate
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class KtCollectionVerticle : CoroutineVerticle() {

  private val logger = LoggerFactory.getLogger(KtCollectionVerticle::class.java)

  private lateinit var webClient: WebClient

  override suspend fun start() {
    webClient = WebClient.create(vertx)
    vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .listen(8082).await()
  }

  private fun handleRequest(request: HttpServerRequest) {
    launch {
      try {
        val array = Json.array()
        Main.ports.map {
          val t = async { fetchTemperature(it)}
          array.add(t.await())
        }
        val json = json { obj("data" to array) }

        sendToSnapshot(json)
        request.response()
          .putHeader("Content-Type", "application/json")
          .end(json.encode())

      } catch (err: Throwable) {
        logger.error("Something went wrong", err)
        request.response().setStatusCode(500).end()
      }
    }
  }

  private suspend fun fetchTemperature(port: Int): JsonObject {
    return webClient
      .get(port, "localhost", "/")
      .expect(ResponsePredicate.SC_SUCCESS)
      .`as`(BodyCodec.jsonObject())
      .send().await()
      .body()
  }

  private suspend fun sendToSnapshot(json: JsonObject) {
    webClient
      .post(4000, "localhost", "/")
      .expect(ResponsePredicate.SC_SUCCESS)
      .sendJson(json).await()
  }
}
