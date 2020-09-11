package Server.Resources;

import Server.Automation.PageUtil;
import Server.Automation.XmlMapping;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRotY;

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
        try {
            xmlMapping = new XmlMapping();
        } catch (Exception e) {
            e.printStackTrace();
        }

        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route("/main/home").handler(ctx -> {
            String token = JwtUtils.findToken(ctx);
            serverString = xmlMapping.createServerString("0");
            var obj = new JsonObject();
            obj.put("sidePanal", "");
            obj.put("content", "");
            obj.put("servers", serverString);
            thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });
        router.route("/main/:serverRouter/:pageRouter").handler(this::mainPage);

        router.route("/maintest/:serverRouter/:pageRouter").handler(ctx -> {
            String pageRouter = ctx.request().getParam("pageRouter");
            String serverRouter = ctx.request().getParam("serverRouter");
            String route = "/" + serverRouter + "/" + pageRouter;
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.put("userList", JSON.toJSONString(USER_MANAGE_PAGES));
//            jsonObject.put("configList", JSON.toJSONString(CONFIG_MANAGE_PAGES));
//            jsonObject.put("pageContent", );
            //ctx.response().putHeader("content-type", "text/json").end(JSON.toJSONString(jsonObject));
            ctx.response().end(xmlMapping.createElementString(xmlMapping.getElement(ctx.request().getParam("pageRouter")), route));
        });
        router.route("/main/configsName").handler(this::configsName);

        router.route("/main/returnContent").handler(ctx -> {

        });
        router.route("/main/userInfo").handler(ctx -> {
            String page = ctx.getBodyAsJson().getString("page");
            if (USER_MANAGE_PAGES.contains(page)) {
                System.out.println();
                ctx.response().end(xmlMapping.createReturnString(TYPE_TABLE, JSON.toJSONString(allManagerInfo()), false, null));
            } else {
                ctx.response().end("");
            }
        });
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
        obj.put("pagename", pageRouter);
        //服务器路由
        String serverRouter = ctx.request().getParam("serverRouter");
        obj.put("servername", serverRouter);
        //总路由
        String route = "/" + serverRouter + "/" + pageRouter;
        obj.put("route", route);

        //服务器列表
        serverString = xmlMapping.createServerString(serverRouter);
        obj.put("servers", serverString);

        //权限列表 用于分辨要不要有修改的按钮
        subAuthList = VerifyDatabaseHelper.selectAuthority(JwtUtils.findToken(ctx), serverRouter);


        //页面中显示的东西
//        String contentString;
//        if ("0".equals(pageRouter)) {
//            contentString = "";
//        } else {
//            contentString = xmlMapping.createElementString(xmlMapping.getElement(ctx.request().getParam("pageRouter")), route);
//        }
//        obj.put("contentString", contentString);


        //返回值的类型和名字
        String type = ctx.get("type");
        obj.put("selectType", type);
        String token = JwtUtils.findToken(ctx);
        HashMap<String, String> argNames = Cache.getArgs(token);
        obj.put("args", argNames);
        //返回的数据
        String data = ctx.get("data");
//        if (type != null) {
//            if ("list".equals(type)) {
//                obj.put("configsName", xmlMapping.createConfigsList(data));
//            } else {
//                obj.put("returnString", xmlMapping.createReturnString(type, data, subAuthList.contains(pageRouter), argNames));
//                Cache.removeArgs(token);
//            }
//        }
//        if (CONFIG_MANAGE_PAGES.contains(pageRouter)) {
//            configManagePage(vertx, obj, ctx);
//        } else if (USER_MANAGE_PAGES.contains(pageRouter)) {
//            userManagePage(vertx, obj, ctx);
//        } else {
//            normalPage(vertx, obj, ctx);
//        }

        String allPage = xmlMapping.createPageString(route, type, data, pageRouter, subAuthList, ctx, ctx.vertx());
        obj.put("allPage", allPage);
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {

            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
        //ctx.response().putHeader("content-type", "text/json").end(JSON.toJSONString(obj));
    }

    private void configManagePage(Vertx vertx, JsonObject obj, RoutingContext ctx) {
        vertx.executeBlocking(future -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("operation", "selectConfigName");
            WebClient webClient = WebClient.create(vertx);
            webClient.post(8000, "localhost", "/GmServer")
                    .sendJsonObject(jsonObject, res -> {
                        if (res.succeeded()) {
                            JSONObject jsonRes = JSON.parseObject(res.result().bodyAsString());
                            String resData = jsonRes.getString("data");
                            System.out.println(System.currentTimeMillis());
                            future.complete(resData);
                        }
                    });
        }, false, res -> {
            try {
                obj.put("configsName", xmlMapping.createConfigsList(res.result().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(res.result().toString());
            thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {

                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });

        });
    }

    private void userManagePage(Vertx vertx, JsonObject obj, RoutingContext ctx) {
        HashMap<String, String> userInfo = allManagerInfo();
        String userInfoStr = JSON.toJSONString(userInfo);
        obj.put("userInfo", xmlMapping.createReturnString("table", userInfoStr, false, null));
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
    }

    private void normalPage(Vertx vertx, JsonObject obj, RoutingContext ctx) {
        thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
            ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
        });
    }


}
