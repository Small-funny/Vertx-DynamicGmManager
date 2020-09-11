package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;

import java.net.URLDecoder;
import java.util.HashMap;

public class ForwardResources {

    private WebClient webClient;
    private final WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3").setKeepAlive(false);

    public void registerResources(Router router, Vertx vertx) {

        webClient = WebClient.create(vertx, options);

        router.post("/forward").handler(this::forward);

    }

    private void forward(RoutingContext routingContext) {
        System.out.println("context:"+routingContext.getBodyAsString());
        XmlMapping xmlMapping = new XmlMapping();
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

        String token = JwtUtils.findToken(routingContext);

        data.remove("route");
        data.remove("operation");
        Cache.setArgs(token, data);
        String server = url.split("/")[0];
        String page = url.split("/")[1];
        webClient.post(8000, "localhost", "/GmServer")
                .sendJsonObject(jsonObject, ar -> {
                    if (ar.succeeded()) {
//                System.out.println(ar.result().body());
                        JSONObject jsonResult = JSON.parseObject(ar.result().bodyAsString());
                        System.out.println("jsonresult:" + jsonResult);

                        String type = jsonResult.getString("type");
                        String resultData = jsonResult.getString("data");
                        String returnString = xmlMapping.createReturnString(type, resultData, VerifyDatabaseHelper.selectAuthority(JwtUtils.findToken(routingContext), server).contains(page), Cache.getArgs(JwtUtils.findToken(routingContext)));
                        // routingContext.put("type", type).put("data", resultData).put("route", url).reroute("/main"+url);
                        routingContext.response().end(returnString);
                    } else {
                        System.out.println("Wrong :" + ar.cause().getMessage());
                        routingContext.response().end("Operation failed !");
                    }
                });
    }
}
 