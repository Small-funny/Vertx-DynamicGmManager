package Server.Verify;

import Server.DatabaseHelper.VerifyDatabaseHelper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 拦截器类
 */
@Slf4j
public class TokenCheck implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        log.info("Interceptor receive request");
        try {
            String token = JwtUtils.findToken(routingContext);

            log.info("Interceptor receive token: " + token);

            JWTAuth jwtAuth = JwtUtils.createJwt(routingContext);
            JsonObject config = new JsonObject().put("jwt", token);
            jwtAuth.authenticate(config, res -> {
                if (!res.succeeded()) {
                    log.info("Token authenticate failed");
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                    routingContext.reroute("/login");
                } else {
                    if (VerifyDatabaseHelper.isTokenExisted(token)) {
                        log.info("Token authenticate succeed");
                        routingContext.next();
                    } else {
                        log.info("Token is not existed");
                        routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                        routingContext.reroute("/login");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}