package Server;

import Server.Automation.PageUtil;
import Server.Resources.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        System.out.println(123);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ServerVerticle());
        PageUtil.generateAuthMap();
    }

    @Override
    public void start() {

        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        // router.route("/main/*").handler(new TokenCheck());
//        router.route("/forward").handler(new TokenCheck());
//        router.route("/manager").handler(new TokenCheck());
//        router.route("/subMain").handler(new TokenCheck());
        router.route("/*").handler(StaticHandler.create("src/main/java/resources"));
        // router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.POST));
        registerResources(router);
        vertx.createHttpServer().requestHandler(router::accept).listen(8003);
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
