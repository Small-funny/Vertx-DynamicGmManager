package Server.Resources;

import Server.Authorization.JwtUtils;
import Server.SecretKey.RSAUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import Server.DatabaseHelper.JdbcMysqlHelper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * 用户验证相关路由
 */
public class LoginResources extends AbstractVerticle {

    public void registerResources(Router router){
        router.get("/login").handler(this::login);
        router.get("/login/publicKey").handler(this::sendPublicKey);
        router.get("/login/authenticity").handler(this::authenticity);
        router.post("/login/createToken").handler(this::createToken);
    }

    private void login(RoutingContext routingContext) {
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("templates/login.html");
    }

    private void sendPublicKey(RoutingContext routingContext) {
        String publicKey = "";
        publicKey = getKeys("publicKey");
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(publicKey);
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

//        System.out.println("账号密码：");
//        System.out.println("账号:（加密后）"+username);
//        System.out.println("账号:（加密后）"+password);

        String privateKey = null;
        privateKey = getKeys("privateKey");

        username = RSAUtil.decrypt(privateKey, username);
        password = RSAUtil.decrypt(privateKey, password);

        System.out.println(username + " " + password);

        if (JdbcMysqlHelper.isExisted(username, password)) {
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

        String token = JwtUtils.findToken(routingContext);
        System.out.println("登录验证收到的token:"+token);

        JWTAuth jwtAuth =  JwtUtils.createJwt(routingContext);

        JsonObject config = new JsonObject().put("jwt", token);

        jwtAuth.authenticate(config, res -> {
            if (res.succeeded()) {
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Verification succeeded!");
            } else {
                routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Token expired, Verification failed!");
            }
        });
    }

    private String getKeys(String fileName) {
        String key = "";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //自定义缓冲区
            byte[] buffer = new byte[10240];
            int flag = 0;
            while ((flag = bis.read(buffer)) != -1) {
                key += new String(buffer, 0, flag);
            }
            //关闭的时候只需要关闭最外层的流就行了
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }
}
