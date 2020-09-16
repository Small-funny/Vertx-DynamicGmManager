package Server.Resources;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 路由错误处理
 * @author Betta
 */
@Slf4j
public class FailureResources {

    public void registerResources(Router router) {
        router.route("/404").handler(this::failureNotFound);
        router.route().failureHandler(this::failure);
    }

    private void failure(RoutingContext routingContext) {
        log.info("Page error 404");
        routingContext.reroute("/404");
    }

    private void failureNotFound(RoutingContext routingContext) {
        routingContext.response().sendFile("src/main/java/resources/templates/404.html");
    }
}