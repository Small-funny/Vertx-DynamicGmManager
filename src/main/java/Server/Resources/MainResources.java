package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.complex.RootsOfUnity;

import java.util.HashMap;
import java.util.List;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.allManagerInfo;

@Slf4j
public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    XmlMapping xmlMapping;
    static String serverString;
    static String asideString;
    static List<String> subAuthList;

    public void registerResources(Router router, Vertx vertx) {
        xmlMapping = new XmlMapping();
        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route("/main/home").handler(this::home);
        router.route("/main/:serverRouter/:pageRouter").handler(this::mainPage);
        router.route("/main/configsName").handler(this::configsName);
        router.route("/main/userInfo").handler(this::userInfo);
        router.route("/subMain/:serverRouter/:pageRouter").handler(this::subMain);
    }

    private void configsName(RoutingContext ctx) {
        String page = ctx.getBodyAsJson().getString("page");
        if (CONFIG_MANAGE_PAGES.contains(page)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("operation", "selectConfigName");
            WebClient webClient = WebClient.create(ctx.vertx());
            webClient.post(8000, "localhost", "/GmServer").sendJsonObject(jsonObject, res -> {
                ctx.response().end(xmlMapping.createConfigsList(JSON.parseObject(res.result().bodyAsString()).getString("data")));
            });
        } else {
            ctx.response().end("");
        }
    }

    private void mainPage(RoutingContext ctx) {
        var obj = new JsonObject();
        //侧边栏菜单
        asideString = xmlMapping.createAsideString(JwtUtils.findToken(ctx), ctx.request().getParam("serverRouter"));
        obj.put("sidePanal", asideString);

        //页面路由
        String pageRouter = ctx.request().getParam("pageRouter");
        //服务器路由
        String serverRouter = ctx.request().getParam("serverRouter");
//        //总路由
        String route = "/" + serverRouter + "/" + pageRouter;


        //服务器列表
        serverString = xmlMapping.createServerString(serverRouter);
        obj.put("servers", serverString);

        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });


    }

    private void userInfo(RoutingContext ctx) {
        String page = ctx.getBodyAsJson().getString("page");
        if (USER_MANAGE_PAGES.contains(page)) {
            System.out.println();
            ctx.response().end(xmlMapping.createReturnString(TYPE_TABLE, JSON.toJSONString(allManagerInfo()), false, null));
        } else {
            ctx.response().end("");
        }
    }

    private void home(RoutingContext ctx) {
        String token = JwtUtils.findToken(ctx);
        serverString = xmlMapping.createServerString("0");
        var obj = new JsonObject();
        obj.put("sidePanal", "");
        obj.put("content", "");
        obj.put("servers", serverString);
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
    }

    private void subMain(RoutingContext ctx) {
        String pageRouter = ctx.request().getParam("pageRouter");
        //服务器路由
        String serverRouter = ctx.request().getParam("serverRouter");
//        //总路由
        String route = "/" + serverRouter + "/" + pageRouter;
        ctx.response().end(xmlMapping.createElementString(xmlMapping.getElement(ctx.request().getParam("pageRouter")), route));
    }

}
