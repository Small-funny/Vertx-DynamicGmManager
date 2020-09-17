package Server.Resources;

import Server.DatabaseHelper.VerifyDatabaseHelper;
import Server.Verify.VerifyCode;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import Server.SecretKey.RSAUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.*;

/**
 * 用户验证相关接口
 */
@Slf4j
public class LoginResources extends AbstractVerticle {

    private static final Long TOKEN_TIME_LIMIT = 36000L;

    public void registerResources(Router router) {
        router.get("/login").handler(this::login);
        router.get("/login/logout").handler(this::logout);
        router.get("/login/verifyCode").handler(this::verifyCode);
        router.get("/login/publicKey").handler(this::sendPublicKey);
        router.get("/login/authenticity").handler(this::authenticity);
        router.get("/login/verifyCodePic").handler(this::verifyCodePic);
        router.post("/login/createToken").handler(this::createToken);
    }

    /**
     * 发送静态登录页面
     * 
     * @param routingContext
     */
    private void login(RoutingContext routingContext) {
        log.info("Receive request: login web page");

        routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
                .sendFile("src/main/java/resources/templates/login.html");
    }

    /**
     * 登出后的数据处理
     * 
     * @param routingContext
     */
    private void logout(RoutingContext routingContext) {
        log.info("Receive request: logout processing");

        String token = JwtUtils.findToken(routingContext);
        Cache.removeArgs(token);
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
    }

    /**
     * 发送公钥
     * 
     * @param routingContext
     */
    private void sendPublicKey(RoutingContext routingContext) {
        log.info("Receive request: send publickey");

        String publicKey = "";
        publicKey = getString("src/main/java/resources/verifies/publicKey");
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(publicKey);
    }

    /**
     * 为新登录的用户创建token
     * 
     * @param routingContext
     */
    private void createToken(RoutingContext routingContext) {
        log.info("Receive request: create token");

        JWTAuth jwtAuth = JwtUtils.createJwt(routingContext);

        var username = routingContext.getBodyAsJson().getString("username");
        var password = routingContext.getBodyAsJson().getString("password");

        String privateKey = null;
        privateKey = getString("src/main/java/resources/verifies/privateKey");

        username = RSAUtil.decrypt(privateKey, username);
        password = RSAUtil.decrypt(privateKey, password);

        if (VerifyDatabaseHelper.verifyIsExisted(username, password)) {
            String newToken = jwtAuth.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(TOKEN_TIME_LIMIT));
            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(newToken);

            VerifyDatabaseHelper.updateToken(username, newToken);
            log.info("Username or password right, Verification succeed!");

        } else {
            log.info("Username or password error, Verification failed!");
            routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                    .end("Username or password error, Verification failed!");
        }
    }

    /**
     * 登录验证
     * 
     * @param routingContext
     */
    private void authenticity(RoutingContext routingContext) {
        log.info("Receive request: login authentication");

        String token = JwtUtils.findToken(routingContext);
        log.info("Receive token: " + token);
        JWTAuth jwtAuth = JwtUtils.createJwt(routingContext);
        JsonObject config = new JsonObject().put("jwt", token);

        // 时效验证
        jwtAuth.authenticate(config, res -> {
            if (res.succeeded()) {
                if (VerifyDatabaseHelper.isTokenExisted(token)) {
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
                            .end("Verification succeeded!");
                } else {
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                            .end("Token expired, Verification failed!");
                }
            } else {
                routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                        .end("Token expired, Verification failed!");
            }
        });
    }

    /**
     * 发送验证码图片
     * 
     * @param routingContext
     */
    private void verifyCodePic(RoutingContext routingContext) {
        log.info("Receive request: verify code picture");

        File dir = new File("src/main/java/resources/verifies");
        if (!dir.exists()) {
            dir.mkdir();
        }

        VerifyCode instance = new VerifyCode();
        String verifyCode = instance.getCode();

        File code = new File("src/main/java/resources/verifies/code");
        if (!code.exists()) {
            try {
                code.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(dir, "verifyCode.jpg");

        try {
            log.info("Writing verify code picture ...");
            ImageIO.write(instance.getBuffImg(), "jpg", file);

            log.info("Writing verify code file ...");
            FileOutputStream privateFileStream = new FileOutputStream("src/main/java/resources/verifies/code");
            BufferedOutputStream privateBuffer = new BufferedOutputStream(privateFileStream);
            privateBuffer.write(verifyCode.getBytes(), 0, verifyCode.getBytes().length);
            privateBuffer.flush();
            privateBuffer.close();
        } catch (Exception e) {
            log.error("asdasdsad");
        }
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
                .sendFile("src/main/java/resources/verifies/verifyCode.jpg");
    }

    /**
     * 发送验证码
     * 
     * @param routingContext
     */
    private void verifyCode(RoutingContext routingContext) {
        log.info("Receive request: verify code");

        String code = getString("src/main/java/resources/verifies/code");
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(code);
    }

    /**
     * 获取文件内的字符串
     * 
     * @param fileName
     * @return
     */
    private String getString(String fileName) {
        String key = "";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // 自定义缓冲区
            byte[] buffer = new byte[10240];
            int flag = 0;
            while ((flag = bis.read(buffer)) != -1) {
                key += new String(buffer, 0, flag);
            }
            // 关闭的时候只需要关闭最外层的流就行了
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }
}