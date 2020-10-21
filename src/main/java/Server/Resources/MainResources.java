package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.VerifyDatabaseHelper;
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

import java.util.HashMap;
import java.util.List;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.*;

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

    private void failureNotFound(RoutingContext routingContext) {
        routingContext.response().sendFile("src/main/java/resources/templates/404.html");
    }

    /**
     * 登陆后的主页
     *
     * @param ctx
     */
    private void home(RoutingContext ctx) {
        serverString = XmlMapping.createServerString("0");
        String username = VerifyDatabaseHelper.tokenToUsername(JwtUtils.findToken(ctx));
        var obj = new JsonObject();
        obj.put("sidePanal", "");
        obj.put("content", "");
        obj.put("servers", serverString);
        obj.put("username", username);
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
        String username = VerifyDatabaseHelper.tokenToUsername(JwtUtils.findToken(ctx));
        obj.put("sidePanal", asideString);
        String serverRouter = ctx.request().getParam("serverRouter");
        serverString = XmlMapping.createServerString(serverRouter);
        obj.put("servers", serverString);
        obj.put("username", username);
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
        HashMap<String, String> data = JSON.parseObject(ctx.getBodyAsJson().getString("arguments"), HashMap.class);
        String server = data.get("route").toString().split("/")[1];
        String host = SERVER_ELEMENT.get(server).getAttributeValue("host");
        String suffix = SERVER_ELEMENT.get(server).getAttributeValue("url");
        String operation = data.get("list");
        int port = Integer.parseInt(SERVER_ELEMENT.get(server).getAttributeValue("port"));
        if ("null".equals(operation)) {
            ctx.response().end(" ");
        } else {

            if ("selectUserList".equals(operation)) {
                List<String> users = allUser();
                System.out.println(PAGE_ELEMENT.get(page).getAttributeValue("listId"));
                String argName = PAGE_ELEMENT.get(page).getAttributeValue("listId") == null ? PAGE_ELEMENT.get(page).getChild("form").getChild("input").getAttributeValue("id") : PAGE_ELEMENT.get(page).getAttributeValue("listId");
                String argNameName = null;
                for (Element formE : PAGE_ELEMENT.get(page).getChildren()) {
                    for (Element inputE : formE.getChildren()) {
                        if(argName.equals(inputE.getAttributeValue("id"))){
                            argNameName = inputE.getAttributeValue("name");
                            break;
                        }
                    }
                }
                ctx.response().end(XmlMapping.createConfigsList(JSON.toJSONString(users), argName,argNameName));

            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("operation", operation);
                WebClient webClient = WebClient.create(ctx.vertx());
                String argName = PAGE_ELEMENT.get(page).getChild("form").getChild("input").getAttributeValue("id");
                String argNameName = null;
                for (Element formE : PAGE_ELEMENT.get(page).getChildren()) {
                    for (Element inputE : formE.getChildren()) {
                        if(argName.equals(inputE.getAttributeValue("id"))){
                            argNameName = inputE.getAttributeValue("name");
                            break;
                        }
                    }
                }
                String finalArgNameName = argNameName;
                webClient.post(port, host, suffix).sendJsonObject(jsonObject, res -> {
                    ctx.response().end(XmlMapping.createConfigsList(JSON.parseObject(res.result().bodyAsString()).getString("data"), argName, finalArgNameName));
                });
            }
        }
    }
}
