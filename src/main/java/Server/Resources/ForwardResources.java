package Server.Resources;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class ForwardResources {

    private WebClient webClient;
    private final WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3").setKeepAlive(false);

    public void registerResources(Router router, Vertx vertx) {

        webClient = WebClient.create(vertx, options);

        router.post("/forward").handler(this::sendConfigs);
    }

    private void sendConfigs(RoutingContext routingContext) {
        System.out.println(routingContext.request().getFormAttribute("data"));
        webClient.head(8000, "", "").send(ar -> {
            if (ar.succeeded()) {
                System.out.println(ar.result().body());
                routingContext.response().end(ar.result().bodyAsBuffer());
            } else {
                System.out.println("Wrong :" +ar.cause().getMessage());
            }
        });
    }

}
