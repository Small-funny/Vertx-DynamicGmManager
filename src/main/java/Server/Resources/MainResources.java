package Server.Resources;

import Server.Automation.XmlMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import io.vertx.reactivex.redis.client.Request;

import java.util.List;

public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    XmlMapping xmlMapping;
    static String asideString ;
    public void registerResources(Router router, Vertx vertx) {
//        router.get("/main").handler(this::main);
        try {
            xmlMapping = new XmlMapping();
        } catch (Exception e) {
            e.printStackTrace();
        }

        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route().handler(StaticHandler.create());
        List<String> pageList = xmlMapping.createPageURLList();
        router.route("/main/home").handler(ctx->{
            var userName = ctx.request().getParam("name");
            asideString = xmlMapping.createAsideString(userName);
            var obj = new JsonObject();
            obj.put("sidePanal", asideString);
            obj.put("name", "");
            thymeleafTemplateEngine.render(obj, "templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });
        for(String pageRouter: pageList ){
            router.route("/main/"+pageRouter).handler(ctx->{
                var userName = ctx.request().getParam("userName");
                //asideString = xmlMapping.createAsideString(userName);
                var obj = new JsonObject();
                obj.put("sidePanal", asideString);
                obj.put("pagename",pageRouter);
                obj.put("name", xmlMapping.createElementString(xmlMapping.getElement(pageRouter)));
                thymeleafTemplateEngine.render(obj, "templates/home.html", bufferAsyncResult -> {
                    ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
                });
            });
        }
    }

//    private void main(RoutingContext routingContext) {
//        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("templates/index.html");
//    }
}
