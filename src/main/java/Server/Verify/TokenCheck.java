package Server.Verify;

import Server.DatabaseHelper.DatabaseHelper;
import Server.DatabaseHelper.JdbcMysqlHelper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;

public class TokenCheck implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("进入拦截器");
        try {
            String token = JwtUtils.findToken(routingContext);

            System.out.println("拦截器收到的token:"+token);

            JWTAuth jwtAuth = JwtUtils.createJwt(routingContext);

            JsonObject config = new JsonObject().put("jwt", token);

            jwtAuth.authenticate(config, res -> {
                if (!res.succeeded()) {
                    System.out.println("token无效");
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                    routingContext.reroute("/login");
                } else {
//                    long starttime = System.currentTimeMillis();
//                    System.out.println("拦截器数据库验证程序计时开始");
                    if (DatabaseHelper.isTokenExisted(token)) {
                        System.out.println("有效token，验证成功");
                        routingContext.next();
                    } else {
                        System.out.println("token无效");
                        routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                        routingContext.reroute("/login");
                    }
//                    long endtime = System.currentTimeMillis();
//                    System.out.println("程序运行时间：" + (endtime - starttime) + "ms");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}