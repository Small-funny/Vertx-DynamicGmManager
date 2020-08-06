package Server.Resources;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import Server.DatabaseHelper.JdbcMysqlHelper;

public class LoginResources extends AbstractVerticle {

    public void registerResources(Router router){
        router.get("/login").handler(this::login);
        router.post("/login/createToken").handler(this::createToken);
        router.post("/login/authenticity").handler(this::authenticity);
    }

    private void login(RoutingContext routingContext) {
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("templates/login.html");
    }

    private void createToken(RoutingContext routingContext){

        System.out.println("Receive request: create token");

        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore.jceks")
                        .setPassword("secret"));

        JWTAuth jwtAuth = JWTAuth.create(routingContext.vertx(), config);

        var username = routingContext.getBodyAsJson().getString("username");
        var password = routingContext.getBodyAsJson().getString("password");

        System.out.println(username + password);

        if (new JdbcMysqlHelper().isExisted(username, password)) {
            String newToken = jwtAuth.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(3600));
            System.out.println("Username or password right, Verification succeed!");
            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(newToken);
        } else {
            System.out.println("Username or password error, Verification failed!");
            routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Username or password error, Verification failed!");
        }
    }

    private void authenticity(RoutingContext routingContext) {

        System.out.println("Receive request: authenticity token");

        var token = "";

        token = routingContext.request().getHeader("Authorization");

        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                                .setPath("Keystore.jceks")
                                .setPassword("secret"));

        long starttime = System.currentTimeMillis();
        System.out.println("验证令牌计时开始");

        JWTAuth jwtAuth = JWTAuth.create(routingContext.vertx(), jwtAuthOptions);
        long endtime = System.currentTimeMillis();

        System.out.println("程序运行时间：" + (endtime - starttime) + "ms");

        String token_jwt = token.split(" ")[1];

        System.out.println("Only token = " + token_jwt);

        JsonObject config = new JsonObject().put("jwt", token_jwt);

        jwtAuth.authenticate(config, res -> {
            if (res.succeeded()) {
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Verification succeeded!");
            } else {
                routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Token expired, Verification failed!");
            }
        });

    }

}
