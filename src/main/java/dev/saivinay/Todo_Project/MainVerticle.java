package dev.saivinay.Todo_Project;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) {
    DatabaseVerticle databaseVerticle = new DatabaseVerticle();
    vertx.deployVerticle(databaseVerticle)
      .compose(id ->{
        log.info("Database verticle deployed: {}", id);
         return vertx.deployVerticle(new HttpVerticle(databaseVerticle));
        })
      .onSuccess(id -> {
        log.info("Httpverticle deployed: {}", id);
        startPromise.tryComplete();
      })
      .onFailure(startPromise::fail);
  }
}
