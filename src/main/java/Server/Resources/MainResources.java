package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.DatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import jdk.swing.interop.SwingInterOpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //服务器列表
            serverString = xmlMapping.createServerString(serverRouter);
            obj.put("servers", serverString);
            //权限列表 用于分辨要不要有修改的按钮
            subAuthList = DatabaseHelper.selectAuthority(JwtUtils.findToken(ctx), serverRouter);
            boolean subAuth = false;
            if (subAuthList.contains(pageRouter)) {
                subAuth = true;
            }
            //返回值的类型和名字
            String type = ctx.get("type");
            obj.put("selectType", type);
            String token = JwtUtils.findToken(ctx);
            HashMap<String, String> argNames = Cache.getArgs(token);
            obj.put("args",argNames);
            //返回的数据
            String data = ctx.get("data");
            if (type != null) {
                if ("list".equals(type)) {
                    try {
                        obj.put("configsName", xmlMapping.createConfigsList(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        obj.put("returnString", xmlMapping.createReturnString(type, data, subAuth,argNames));
                        Cache.removeArgs(token);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //页面中显示的东西
            String contentString;
            if ("0".equals(pageRouter)) {
                contentString = "";
            } else {
                contentString = xmlMapping.createElementString(xmlMapping.getElement(ctx.request().getParam("pageRouter")));
            }
            obj.put("contentString", contentString);
            obj.put("route", "/" + serverRouter + "/" + pageRouter);


            thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });
        router.route("/main/args").handler(ctx -> {
            String token = JwtUtils.findToken(ctx);
            HashMap<String, String> args = Cache.getArgs(token);
            ctx.response().end(args.toString());
        });

    }
}
