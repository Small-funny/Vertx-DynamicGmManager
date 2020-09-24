package Server.Resources;

import Server.Automation.XmlMapping;
import Server.Verify.JwtUtils;
import com.alibaba.fastjson.JSON;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.allManagerInfo;

/**
 * 系统主页路由
 */
@SuppressWarnings("unchecked")
public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    static String serverString;
    static String asideString;
    static List<String> subAuthList;

    public void registerResources(Router router, Vertx vertx) {
        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route("/main/home").handler(this::home);
        router.route("/main/:serverRouter/:pageRouter").handler(this::mainPage);
        router.route("/main/configsName").handler(this::preloadingList);
        router.route("/main/userInfo").handler(this::preloadingTable);
        router.route("/subMain/:serverRouter/:pageRouter").handler(this::subMain);
    }

    /**
     * 登陆后的主页
     *
     * @param ctx
     */
    private void home(RoutingContext ctx) {
        serverString = XmlMapping.createServerString("0");
        var obj = new JsonObject();
        obj.put("sidePanal", "");
        obj.put("content", "");
        obj.put("servers", serverString);
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
    }

    /**
     * 选择服务器后的主页
     *
     * @param ctx
     */
    private void mainPage(RoutingContext ctx) {
        var obj = new JsonObject();

        //侧边栏菜单
        asideString = XmlMapping.createAsideString(JwtUtils.findToken(ctx), ctx.request().getParam("serverRouter"));
        obj.put("sidePanal", asideString);
        String serverRouter = ctx.request().getParam("serverRouter");
        serverString = XmlMapping.createServerString(serverRouter);
        obj.put("servers", serverString);
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
    }

    /**
     * 点击目录页面局部刷新
     *
     * @param ctx
     */
    private void subMain(RoutingContext ctx) {
        String pageRouter = ctx.request().getParam("pageRouter");
        //服务器路由
        String serverRouter = ctx.request().getParam("serverRouter");
        //总路由
        String route = "/" + serverRouter + "/" + pageRouter;
        Element element;
        element = XmlMapping.getElement(pageRouter);
        ctx.response().end(XmlMapping.createElementString(element, route));
    }

    /**
     * 预加载页面表格
     *
     * @param ctx
     */
    private void preloadingTable(RoutingContext ctx) {
        String page = ctx.getBodyAsJson().getString("page");
        HashMap<String, String> hashMap = JSON.parseObject(ctx.getBodyAsJson().getString("arguments"), HashMap.class);
        if (USER_MANAGE_PAGES.contains(page)) {
            ctx.response().end(
                    XmlMapping.createReturnString(TYPE_TABLE, JSON.toJSONString(allManagerInfo()), false, hashMap));
        } else {
            ctx.response().end("");
        }
    }

    /**
     * 预加载页面列表
     *
     * @param ctx
     */
    private void preloadingList(RoutingContext ctx) {
        String page = ctx.getBodyAsJson().getString("page");
        System.out.println(ctx);
        if (CONFIG_MANAGE_PAGES.contains(page)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("operation", "selectConfigName");
            WebClient webClient = WebClient.create(ctx.vertx());
            webClient.post(8000, "localhost", "/GmServer").sendJsonObject(jsonObject, res -> {
                ctx.response().end(
                        XmlMapping.createConfigsList(JSON.parseObject(res.result().bodyAsString()).getString("data")));
            });
        } else if (USER_AUTH_MANAGE_PAGES.contains(page)) {
            HashMap<String, String> hashMap = JSON.parseObject(ctx.getBodyAsJson().getString("arguments"), HashMap.class);
            if ("true".equals(hashMap.get("flag"))) {
                List<String> list = new ArrayList<>();
                for (Map.Entry<String, Element> entry : TYPE_ELEMENT.entrySet()) {
                    list.add(entry.getValue().getAttributeValue("name"));
                }
                ctx.response().end(XmlMapping.createConfigsList(JSON.toJSONString(list)));
            }
        } else {
            ctx.response().end(" ");
        }
    }
}
