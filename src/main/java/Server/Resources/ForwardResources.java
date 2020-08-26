package Server.Resources;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

public class ForwardResources {

    private WebClient webClient;
    private final WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3").setKeepAlive(false);

    public void registerResources(Router router, Vertx vertx) {

        webClient = WebClient.create(vertx, options);

        router.post("/forward").handler(this::forward);

    }

    private void forward(RoutingContext routingContext) {
        HashMap<String, String> data = new HashMap<>();
        try {
            System.out.println(URLDecoder.decode(routingContext.getBodyAsString(), "UTF-8"));
            for (String str : URLDecoder.decode(routingContext.getBodyAsString(), "UTF-8").split("&")) {
                data.put(str.split("=")[0], str.split("=")[1]);
            }
            data.remove("submit");
            System.out.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject jsonObject = new JsonObject();
        for (String key : data.keySet()) {
            String value = data.get(key);
            jsonObject.put(key, value);
        }
        String url = jsonObject.getString("route");
        System.out.println(url);
        jsonObject.remove("route");
        webClient.post(8000, "localhost", "/GmServer")
                .sendJsonObject(jsonObject, ar -> {
            if (ar.succeeded()) {
//                System.out.println(ar.result().body());

                //routingContext.response().end(ar.result().body());
                String[] dataList = ar.result().body().toString().split("&");
                String arg = null;
                if (dataList.length != 1) {
                    arg = dataList[dataList.length - 1];
                }
                System.out.println("arg" + arg);
                routingContext.put("returnContent", dataList[0]).put("arg", arg).put("route",url).reroute("/main"+url);
            } else {
                System.out.println("Wrong :" +ar.cause().getMessage());
                routingContext.response().end("Operation failed !");
            }
        });
    }
}
