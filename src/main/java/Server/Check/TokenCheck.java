package Server.Check;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;

public class TokenCheck implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("Receive request: authenticity token");
        System.out.println("进入拦截器");
        try {
            var token = routingContext.request().getParam("Authorization");
            System.out.println(token);
            if (token == null) {
                routingContext.reroute("/login");
                return ;
            }
            JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                    .setKeyStore(new KeyStoreOptions()
                            .setPath("Keystore.jceks")
                            .setPassword("secret"));

            long starttime = System.currentTimeMillis();
            System.out.println("验证令牌计时开始");

            JWTAuth jwtAuth = JWTAuth.create(routingContext.vertx(), jwtAuthOptions);
            long endtime = System.currentTimeMillis();

            System.out.println("程序运行时间：" + (endtime - starttime) + "ms");

            System.out.println("Only token = " + token);

            JsonObject config = new JsonObject().put("jwt", token);

            jwtAuth.authenticate(config, res -> {
                if (!res.succeeded()) {
                    routingContext.reroute("/login");
                } else {
                    routingContext.next();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}