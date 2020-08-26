package Server.Resources;

import Server.Automation.XmlMapping;
import Server.Verify.JwtUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MainResources extends AbstractVerticle {

    ThymeleafTemplateEngine thymeleafTemplateEngine;
    XmlMapping xmlMapping;
    static String serverString;
    static String asideString;

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
            thymeleafTemplateEngine.render(obj, "templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });
        router.route("/main/:serverRouter/:pageRouter").handler(ctx -> {
            asideString = xmlMapping.createAsideString(JwtUtils.findToken(ctx), ctx.request().getParam("serverRouter"));
            String pageRouter = ctx.request().getParam("pageRouter");
            String serverRouter = ctx.request().getParam("serverRouter");
            //String returnContent=ctx.request().getParam("returnContent");
            String returnContent =  ctx.get("returnContent");
            String selectName= ctx.get("arg");
            //System.out.println(returnContent);
            serverString = xmlMapping.createServerString(serverRouter);
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
            obj.put("route","/"+serverRouter+"/"+pageRouter);



            obj.put("returnContent",returnContent);
            obj.put("selectName",selectName);

            //obj.put("name", xmlMapping.createElementString(xmlMapping.getElement(pageRouter)));
            obj.put("content", contentString);
            thymeleafTemplateEngine.render(obj, "templates/home.html", bufferAsyncResult -> {
                ctx.response().putHeader("content-type", "text/html").end(bufferAsyncResult.result());
            });
        });

    }
}
