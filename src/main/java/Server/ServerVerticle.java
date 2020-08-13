package Server;

import Server.Authorization.TokenCheck;
import Server.Resources.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ServerVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ServerVerticle());
    }

    @Override
    public void start() {

        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route("/main/*").handler(new TokenCheck());
        router.route("/manager/*").handler(new TokenCheck());
        router.route("/webroot/*").handler(StaticHandler.create());

        registerResources(router);

        vertx.createHttpServer().requestHandler(router::accept).listen(8001);
    }

    private void registerResources(Router router) {
        new LoginResources().registerResources(router);
        new MainResources().registerResources(router, vertx);
        new ManagerResources().registerResources(router);
//        new FailureResources().registerResources(router);
    }
}
