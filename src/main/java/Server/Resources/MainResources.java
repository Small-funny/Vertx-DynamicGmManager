package Server.Resources;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
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
        System.out.println("strat"+System.currentTimeMillis());
        router.route("/main/:serverRouter/:pageRouter").handler(ctx -> {
            var obj = new JsonObject();
            vertx.executeBlocking(future -> {
//                vertx.eventBus().request("vertx", "information", req -> {
//                    if (req.succeeded()) {
//                        System.out.println(req.result().body());
//                    } else {
//                        System.out.println("bad");
//                    }
//                });
//                vertx.eventBus().consumer("configsList", message -> {
//                    System.out.println("receive:" + message.body());
//                });

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
                subAuthList = VerifyDatabaseHelper.selectAuthority(JwtUtils.findToken(ctx), serverRouter);
                boolean subAuth = false;
                if (subAuthList.contains(pageRouter)) {
                    subAuth = true;
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


                //返回值的类型和名字
                String type = ctx.get("type");
                obj.put("selectType", type);
                String token = JwtUtils.findToken(ctx);
                HashMap<String, String> argNames = Cache.getArgs(token);
                obj.put("args", argNames);
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
                            obj.put("returnString", xmlMapping.createReturnString(type, data, subAuth, argNames));
                            Cache.removeArgs(token);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println(System.currentTimeMillis());
                System.out.println("11" + obj);


                JsonObject jsonObject = new JsonObject();
                jsonObject.put("operation", "selectConfigName");
                WebClient webClient = WebClient.create(vertx);
                webClient.post(8000, "localhost", "/GmServer")
                        .sendJsonObject(jsonObject, res -> {
                            if (res.succeeded()) {
                                JSONObject jsonRes = JSON.parseObject(res.result().bodyAsString());
                                String resData = jsonRes.getString("data");
                                System.out.println(System.currentTimeMillis());
                                obj.put("resData", resData);
                                future.complete(resData);
                                System.out.println("55" + obj);
                            }
                        });
            }, false, res -> {
                try {
                    obj.put("configsName",xmlMapping.createReturnString("list",res.result().toString(),false,null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(res.result().toString());
                thymeleafTemplateEngine.render(obj, "src/main/java/resources/templates/home.html", bufferAsyncResult -> {
                    System.out.println(System.currentTimeMillis());
                    System.out.println("66" + obj.toString());
                    ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
                });

            });
//            vertx.eventBus().consumer("configsName", message -> {
//                System.out.println(message.body());
//                String body = (String) message.body();
//                System.out.println(body);
//                System.out.println(System.currentTimeMillis());
//                System.out.println("22" + obj.toString());
//                obj.put("configsName", body);
//                obj.put("test", "test");
//                System.out.println(System.currentTimeMillis());
//                System.out.println("33" + obj.toString());
//            });
//            System.out.println(System.currentTimeMillis());
//            System.out.println("44" + obj);
//            checkConfigName(vertx);


        });
        System.out.println("end"+System.currentTimeMillis());
//        router.route("/main/args").handler(ctx -> {
//            String token = JwtUtils.findToken(ctx);
//            HashMap<String, String> args = Cache.getArgs(token);
//            ctx.response().end(args.toString());
//        });


    }

    public List<String> checkConfigName(Vertx vertx) {

        List<String> configsName = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", "selectConfigName");
        vertx.eventBus().send("configsName", "data");

        return configsName;

    }
}
