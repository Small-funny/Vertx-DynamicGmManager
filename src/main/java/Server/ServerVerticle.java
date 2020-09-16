package Server;

import Server.Verify.TokenCheck;
import Server.Resources.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        router.route("/forward").handler(new TokenCheck());
        router.route("/manager").handler(new TokenCheck());
        router.route("/subMain").handler(new TokenCheck());
        router.route("/*").handler(StaticHandler.create("src/main/java/resources"));

        registerResources(router);

        vertx.createHttpServer().requestHandler(router::accept).listen(8001);
        log.info("================== GM Manager Server Start ====================");
    }

    private void registerResources(Router router) {
        new LoginResources().registerResources(router);
        new MainResources().registerResources(router, vertx);
        new ManagerResources().registerResources(router);
        new ForwardResources().registerResources(router, vertx);
        new FailureResources().registerResources(router);
    }
}
