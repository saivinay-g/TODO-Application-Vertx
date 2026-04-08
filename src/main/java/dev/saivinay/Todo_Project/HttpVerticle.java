package dev.saivinay.Todo_Project;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpVerticle extends AbstractVerticle {
  private final Logger log = LoggerFactory.getLogger(HttpVerticle.class);
  private final DatabaseVerticle databaseVerticle;

  public HttpVerticle(DatabaseVerticle databaseVerticle) {
    this.databaseVerticle = databaseVerticle;
  }

  @Override
  public void start() {
    Router router = Router.router(vertx);
    TodoHandler handler = new TodoHandler(databaseVerticle.getPool());

    router.route().handler(BodyHandler.create());
    router.get("/api/todos").handler(handler::getAll);
    router.get("/api/todos/:id").handler(handler::getById);
    router.post("/api/todos").handler(handler::create);
    router.put("/api/todos/:id").handler(handler::update);
    router.delete("/api/todos/:id").handler(handler::removeById);

    router.route().failureHandler(ctx -> {
      log.error("Unhandled error: {}", ctx.failure().getMessage());
      ctx.response()
        .setStatusCode(500)
        .putHeader("Content-Type", "application/json")
        .end("{\" error\":\"Internal Server Error\"}");
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8888)
      .onSuccess(s -> log.info("Todo App is running on http://localhost:8888"))
      .onFailure(err -> log.error("Failed to start: {}", err.getMessage()));
  }
}
