package Server.Resources;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class FailureResources {
    public void resgisterResources(Router router) {
        router.route("/404").handler(this::failureNotFound);
        router.route("/").failureHandler(this::failure);
    }

    private void failure(RoutingContext routingContext) {
        System.out.println("失败");
        routingContext.reroute("/404");
    }

    private void failureNotFound(RoutingContext routingContext) {
        routingContext.response().sendFile("templates/404.html");
    }
}
