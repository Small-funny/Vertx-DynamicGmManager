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

import static Server.Automation.PageUtil.*;

/**
 * 数据转发接口
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ForwardResources {



    public void registerResources(Router router, Vertx vertx) {

        router.post("/forward").handler(this::forward);
    }

    private void forward(RoutingContext routingContext) {
        String server;
        String page;
        HashMap<String, String> data;
        data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);
        JsonObject jsonObject = new JsonObject();
        for (String key : data.keySet()) {
            String value = data.get(key);
            jsonObject.put(key, value);
        }
        String url = jsonObject.getString("route");
        String token = JwtUtils.findToken(routingContext);
        Cache.setArgs(token, data);
        server = url.split("/")[1];
        page = url.split("/")[2];

        String host = SERVER_ELEMENT.get(server).getAttributeValue("host");
        String suffix = SERVER_ELEMENT.get(server).getAttributeValue("url");
        int port = Integer.parseInt(SERVER_ELEMENT.get(server).getAttributeValue("port"));
       WebClient webClient = WebClient.create(routingContext.vertx());
        webClient.post(port, host, suffix).sendJsonObject(jsonObject, ar -> {
            if (ar.succeeded()) {

                JSONObject jsonResult = JSON.parseObject(ar.result().bodyAsString());
                String type = jsonResult.getString("type");
                String resultData = jsonResult.getString("data");
                boolean auth;
                if (server != null) {
                    auth = ManagerDatabaseHelper
                            .selectAuthList(VerifyDatabaseHelper.tokenToUsername(JwtUtils.findToken(routingContext)),
                                    "btn", server)
                            .contains(page);
                } else {
                    auth = true;
                }
                String returnString = XmlMapping.createReturnString(type, resultData, auth,
                        Cache.getArgs(JwtUtils.findToken(routingContext)));
                routingContext.response().end(returnString);
            } else {
                log.info("Wrong :" + ar.cause().getMessage());
                String returnString = XmlMapping.createReturnString("return","请求失败",false,null);
                routingContext.response().end(returnString);
            }
        });
    }
}
