package Server.Resources;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardResources {

    private WebClient webClient;
    private final WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3").setKeepAlive(false);

    public void registerResources(Router router, Vertx vertx) {

        webClient = WebClient.create(vertx, options);

        router.post("/forward").handler(this::forward);

    }

    @SuppressWarnings("unchecked")
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
                JSONObject jsonResult = JSON.parseObject(ar.result().bodyAsString());
                HashMap<String, Object> hashMap = new HashMap<>();
                List<String> list = new ArrayList<>();

                String type = jsonResult.getString("type");
                String resultData = jsonResult.getString("data");

                routingContext.put("type", type).put("data", resultData).put("route", url).reroute("/main"+url);
            } else {
                System.out.println("Wrong :" +ar.cause().getMessage());
                routingContext.response().end("Operation failed !");
            }
        });
    }
}
