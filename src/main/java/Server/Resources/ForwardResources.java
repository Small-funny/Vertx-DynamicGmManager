package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.ManagerDatabaseHelper;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;

import java.util.HashMap;
import java.util.List;

/**
 * 数据转发接口
 */
@Slf4j
public class ForwardResources {

    private WebClient webClient;
    private final WebClientOptions options = new WebClientOptions().setUserAgent("My-App/1.2.3").setKeepAlive(false);

    public void registerResources(Router router, Vertx vertx) {

        webClient = WebClient.create(vertx, options);

        router.post("/forward").handler(this::forward);

    }

    private void forward(RoutingContext routingContext) {
        System.out.println("context:" + routingContext.getBodyAsString());
        XmlMapping xmlMapping = new XmlMapping();
        HashMap<String, String> data = new HashMap<>();
//        try {
//            System.out.println(URLDecoder.decode(routingContext.getBodyAsString(), "UTF-8"));
//            for (String str : URLDecoder.decode(routingContext.getBodyAsString(), "UTF-8").split("&")) {
//                data.put(str.split("=")[0], str.split("=")[1]);
//            }
//            data.remove("submit");
//            System.out.println(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);


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
        final String server;
        final String page;
        if (url != null) {
            server = url.split("/")[1];
            page = url.split("/")[2];
        } else {
            server = null;
            page = null;
        }
        webClient.post(8000, "localhost", "/GmServer")
                .sendJsonObject(jsonObject, ar -> {
                    if (ar.succeeded()) {
//                System.out.println(ar.result().body());
                        JSONObject jsonResult = JSON.parseObject(ar.result().bodyAsString());
                        System.out.println("jsonresult:" + jsonResult);

                        String type = jsonResult.getString("type");
                        String resultData = jsonResult.getString("data");
                        boolean auth;
                        if (server != null) {
                            auth = ManagerDatabaseHelper.selectAuthList(VerifyDatabaseHelper.tokenToUsername(JwtUtils.findToken(routingContext)), "btn", server).contains(page);
                        } else {
                            auth = true;
                        }
                        String returnString = xmlMapping.createReturnString(type, resultData, auth, Cache.getArgs(JwtUtils.findToken(routingContext)));
                        // routingContext.put("type", type).put("data", resultData).put("route", url).reroute("/main"+url);
                        routingContext.response().end(returnString);
                    } else {
                        System.out.println("Wrong :" + ar.cause().getMessage());
                        routingContext.response().end("Operation failed !");
                    }
                });
    }

}
 