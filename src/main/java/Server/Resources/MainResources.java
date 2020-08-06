package Server.Resources;

import Server.XmlMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import java.util.List;

public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    XmlMapping xmlMapping;

    public void registerResources(Router router, Vertx vertx) {
//        router.get("/main").handler(this::main);
        try {
            xmlMapping = new XmlMapping();
        } catch (Exception e) {
            e.printStackTrace();
        }
        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        router.route().handler(StaticHandler.create());
        List<String> pageList = xmlMapping.createPageNameList();
        for(String pageRouter: pageList ){
            router.get("/main/"+pageRouter).handler(ctx->{
                var obj = new JsonObject();
                obj.put("sidePanal", xmlMapping.createAsideString());
                obj.put("name", xmlMapping.createElementString(xmlMapping.getElement(pageRouter)));
                thymeleafTemplateEngine.render(obj, "templates/queryLogin.html", bufferAsyncResult -> {
                    ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
                });
            });
        }
    }

//    private void main(RoutingContext routingContext) {
//        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("templates/index.html");
//    }
}
