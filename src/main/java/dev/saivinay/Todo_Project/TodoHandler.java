package dev.saivinay.Todo_Project;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TodoHandler {
  private final Pool pool;

  private final Logger log = LoggerFactory.getLogger(TodoHandler.class);

  public TodoHandler(Pool pool) {
    this.pool = pool;
  }

  public void getAll(RoutingContext ctx) {
    pool.query("SELECT id, title, completed FROM todos ORDER BY title DESC")
      .execute()
      .onSuccess(rows -> {
        log.info("Successfully retreived all the todos");
        JsonArray result = new JsonArray();
        rows.forEach(row -> result.add(
          new JsonObject()
            .put("id", row.getString("id"))
            .put("title", row.getString("title"))
            .put("completed", row.getBoolean("completed"))
        ));
        sendJson(ctx, 200, result.encode());
      })
      .onFailure(err -> {
        log.error("Something went wrong in retreiving the todos{}", err.getMessage());
        sendError(ctx, 500, "Failed to fetch todos: " + err.getMessage());
      });
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    if (body == null || body.getString("title") == null) {
      sendError(ctx, 400, "Title is Mandatory");
      return;
    }
    String title = body.getString("title");
    String id = String.valueOf(UUID.randomUUID().toString());
    pool.preparedQuery("INSERT INTO todos (id, title, completed) VALUES (?, ?, ?)")
      .execute(Tuple.of(id, title, false))
      .onSuccess(rows -> {
        sendJson(ctx, 201, new JsonObject()
          .put("id", id)
          .put("title", title)
          .put("completed", false)
          .encode()
        );
      })
      .onFailure(err -> {
        sendError(ctx, 500, err.getMessage());
      });
  }

  public void update(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    String id = ctx.pathParam("id");
    pool.preparedQuery("SELECT * FROM todos WHERE id=?").execute(Tuple.of(id))
      .onSuccess(rows -> {
        if (rows.iterator().hasNext()) {
          var row = rows.iterator().next();
          String newTitle = body.getString("title") != null ? body.getString("title") : row.getString("title");
          boolean updatedStatus = body.getBoolean("completed") != null ? body.getBoolean("completed") : row.getBoolean("completed");

          pool.preparedQuery("UPDATE todos SET title = ?, completed = ? WHERE id = ?")
            .execute(Tuple.of(newTitle, updatedStatus, id))
            .onSuccess(updatedRow -> {
              sendJson(ctx, 200, new JsonObject()
                .put("id", id)
                .put("title", newTitle)
                .put("completed", updatedStatus)
                .encode());
            })
            .onFailure(err -> {
              sendError(ctx, 500, "Failed to update todo " + err.getMessage());
            });
        } else {
          sendError(ctx, 404, "Todo Not found");
        }
      })
      .onFailure(err -> {
        sendError(ctx, 404, err.getMessage());
      });
  }

  public void getById(RoutingContext ctx) {
    String id = ctx.pathParam("id");
    pool.preparedQuery("SELECT id, title, completed FROM todos WHERE id=?")
      .execute(Tuple.of(id))
      .onSuccess(rows -> {
          if (rows.iterator().hasNext()) {
            Row row = rows.iterator().next();
            sendJson(ctx, 200, new JsonObject()
              .put("id", row.getString("id"))
              .put("title", row.getString("title"))
              .put("completed", row.getBoolean("completed"))
              .encode()
            );
          } else {
            sendError(ctx, 404, "Todo not found");
          }
        }
      )
      .onFailure(
        err -> {
          sendError(ctx, 500, "Something wrong with the server:" + err.getMessage());
        }
      );
  }

  public void removeById(RoutingContext ctx) {
    String id = ctx.pathParam("id");
    pool.preparedQuery("SELECT id, title, completed FROM todos WHERE id=?")
      .execute(Tuple.of(id))
      .onSuccess(rows -> {
        if (!rows.iterator().hasNext()) {
          System.out.println("Unable to find the todo with the specified id");
          sendError(ctx, 404, "Todo not found");
          return;
        }
        pool.preparedQuery("DELETE FROM todos WHERE id=?")
          .execute(Tuple.of(id))
          .onSuccess(deleteHandler -> {
            log.info("removeById successfully removed the todo with id: {}", id);
            ctx.response().setStatusCode(204).end();
          })
          .onFailure(err -> {
            log.error("removeById method failed to remove the todo with id: {}", id);
          });
      })
      .onFailure(
        err -> {
          log.error("RemoveById method failed to retreive the todo with specified id: {}", id);
          sendError(ctx, 500, "Failed to delete todo: " + err.getMessage());
        }
      );
  }

  public void sendJson(RoutingContext ctx, int status, String json) {
    ctx.response()
      .setStatusCode(status)
      .putHeader("Content-Type", "application/json")
      .end(json);
  }

  public void sendError(RoutingContext ctx, int status, String message) {
    ctx.response()
      .setStatusCode(status)
      .putHeader("Content-Type", "application/json")
      .end(new JsonObject().put("error", message).encode());
  }

}

