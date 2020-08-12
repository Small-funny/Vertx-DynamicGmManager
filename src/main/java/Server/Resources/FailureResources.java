package Server.Resources;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * 路由错误处理
 */
public class FailureResources {

    public void registerResources(Router router) {
        router.route("/404").handler(this::failureNotFound);
        router.route().failureHandler(this::failure);
    }

    private void failure(RoutingContext routingContext) {
        System.out.println("页面异常");
        routingContext.reroute("/404");
    }

    private void failureNotFound(RoutingContext routingContext) {
        routingContext.response().sendFile("templates/404.html");
    }
}
