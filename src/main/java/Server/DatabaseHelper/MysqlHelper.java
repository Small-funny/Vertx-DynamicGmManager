package Server.DatabaseHelper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;

public class MysqlHelper extends AbstractVerticle {

    private MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    private PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    private MySQLPool client;

    public void isExisted(RoutingContext routingContext, String username, String password, JWTAuth jwtAuth) {
        initMysql(routingContext);
        this.getConn()
                .compose(conn -> this.select(conn, "SELECT password FROM user WHERE username = '" + username + "'"))
                .onSuccess(rows -> {
                    List<Row> res = new ArrayList<>();
                    rows.forEach(row -> {
                        Row user = row;
                        if (row.getString("password").equals(password)) {
                            res.add(row);
                        }
                    });
                    if (res.isEmpty()) {
                        System.out.println("Username or password error, Verification failed!");
                        routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Username or password error, Verification failed!");
                    } else {
                        String newToken = jwtAuth.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(3600));
                        System.out.println("Username or password right, Verification succeed!");
                        routingContext.response().putHeader("Content-Type", "text/plain").setStatusCode(HttpResponseStatus.OK.code()).end(newToken);
                    }
                });
    }

    private void initMysql(RoutingContext routingContext) {
        connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("49.51.178.56")
                .setDatabase("test")
                .setUser("BettaDisc")
                .setPassword("BettaFotoab1e");
        client = MySQLPool.pool(routingContext.vertx(), connectOptions, poolOptions);
    }

    private Future<SqlConnection> getConn() {
        Promise<SqlConnection> promise = Promise.promise();
        client.getConnection(ar -> {
            if (ar.succeeded()) {
                SqlConnection conn = ar.result();
                System.out.println("Connected");
                promise.complete(conn);
            } else {
                promise.fail("Could not connect: " + ar.cause().getMessage());
            }
        });
        return promise.future();
    }

    private Future<RowSet<Row>> select(SqlConnection conn, String sql) {
        Promise<RowSet<Row>> promise = Promise.promise();
        conn
            .query(sql)
            .execute(ar -> {
                conn.close();
                if (ar.succeeded()) {
                    promise.complete(ar.result());
                } else {
                    promise.fail("查询失败" + ar.failed());
                }
            });
        return promise.future();
    }
}
