package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.DatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.Json;
import Server.Verify.JwtUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    XmlMapping xmlMapping;
    static String serverString;
    static String asideString;
    static List<String> subAuthList;

    public void registerResources(Router router, Vertx vertx) {
//        router.get("/main").handler(this::main);
        try {
            xmlMapping = new XmlMapping();
        } catch (Exception e) {
            e.printStackTrace();
        }

        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route("/main/home").handler(ctx -> {
            String token = JwtUtils.findToken(ctx);
            //asideString = xmlMapping.createAsideString(token,);
            //System.out.println("/////" + asideString);
            serverString = xmlMapping.createServerString("0");
            var obj = new JsonObject();
            obj.put("sidePanal", "");
            obj.put("content", "");
            obj.put("servers", serverString);
            thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });

        router.route("/main/:serverRouter/:pageRouter").handler(ctx -> {
            asideString = xmlMapping.createAsideString(JwtUtils.findToken(ctx), ctx.request().getParam("serverRouter"));

            String pageRouter = ctx.request().getParam("pageRouter");
            String serverRouter = ctx.request().getParam("serverRouter");
            subAuthList = DatabaseHelper.selectAuthority(JwtUtils.findToken(ctx), serverRouter);
            boolean subAuth = false;
            if (subAuthList.contains(pageRouter)) {
                subAuth = true;
            }
            String returnContent = ctx.get("returnContent");
            String type = ctx.get("type");
            String data = ctx.get("data");
            String token = JwtUtils.findToken(ctx);
            String username = DatabaseHelper.tokenToUsername(token);
            serverString = xmlMapping.createServerString(serverRouter);
            //页面中显示的东西
            String contentString;
            if ("0".equals(pageRouter)) {
                contentString = "";
            } else {
                contentString = xmlMapping.createElementString(xmlMapping.getElement(ctx.request().getParam("pageRouter")));
            }
            var obj = new JsonObject();
            obj.put("sidePanal", asideString);
            obj.put("pagename", pageRouter);
            obj.put("servers", serverString);
            obj.put("servername", serverRouter);
            obj.put("route", "/" + serverRouter + "/" + pageRouter);
            if (type != null) {
                if (type.equals("list")) {
                    try {
                        obj.put("configsName", xmlMapping.createConfigsList(data));
                        System.out.println("config:"+xmlMapping.createConfigsList(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        System.out.println("data:"+data);
                        obj.put("returnString", xmlMapping.createReturnString(type, data, subAuth));
                        System.out.println(xmlMapping.createReturnString(type, data, subAuth));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            obj.put("selectType", type);
            obj.put("selectName", Cache.getArgs(token));
            System.out.println(Cache.getArgs(token));
            obj.put("contentString", contentString);
            thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });
        router.route("/main/args").handler(ctx -> {
            String token = JwtUtils.findToken(ctx);
            String args = Cache.getArgs(token);
            ctx.response().end(args);
        });

    }
}
