package dev.saivinay.Todo_Project;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

  private Pool pool;

  @Override
  public void start(Promise<Void> startPromise) {
    String host = System.getenv().getOrDefault("DB_HOST", "localhost");
    int port = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "3306"));
    String database = System.getenv().getOrDefault("DB_NAME", "tododb");
    String user = System.getenv().getOrDefault("DB_USER", "root");
    String password = System.getenv().getOrDefault("DB_PASSWORD", "");
    boolean useServerPrepStmts = Boolean.parseBoolean(System.getenv().getOrDefault("DB_PREP_STMTS", "false"));

    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setHost(host)
      .setPort(port)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password)
      .addProperty("useServerPrepStmts", String.valueOf(useServerPrepStmts));

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    pool = MySQLBuilder.pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    pool.getConnection()
      .onSuccess(conn -> {
        logger.info("MySQL is connected Successfully");
        conn.close();
        startPromise.tryComplete();
      })
      .onFailure(err -> {
        logger.error("Failed to connect to MySQL: {}", err.getMessage());
        startPromise.tryFail(err);
      });
  }

  public Pool getPool() {
    return pool;
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (pool != null) {
      pool.close()
        .onSuccess(v -> {
          logger.info("MySQL pool closed");
          stopPromise.tryComplete();
        })
        .onFailure(stopPromise::fail);
    } else {
      stopPromise.tryComplete();
    }
  }
}
