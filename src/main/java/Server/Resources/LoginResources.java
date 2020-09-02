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

import javax.imageio.ImageIO;
import java.io.*;

/**
 * 用户验证相关路由
 */
public class LoginResources extends AbstractVerticle {

    public void registerResources(Router router){
        router.get("/login").handler(this::login);
        router.get("/login/logout").handler(this::logout);
        router.get("/login/verifyCode").handler(this::verifyCode);
        router.get("/login/publicKey").handler(this::sendPublicKey);
        router.get("/login/authenticity").handler(this::authenticity);
        router.get("/login/verifyCodePic").handler(this::verifyCodePic);
        router.post("/login/createToken").handler(this::createToken);
    }

    private void login(RoutingContext routingContext) {
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("src/main/java/resources/templates/login.html");
    }

    private void logout(RoutingContext routingContext) {
        System.out.println("token置空");
        String token = JwtUtils.findToken(routingContext);
        Cache.removeArgs(token);
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
    }

    private void sendPublicKey(RoutingContext routingContext) {
        String publicKey = "";
        publicKey = getString("src/main/java/resources/verifies/publicKey");
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(publicKey);
    }

    private void createToken(RoutingContext routingContext){
        System.out.println("Receive request: create token");

        JWTAuth jwtAuth = JwtUtils.createJwt(routingContext);

        var username = routingContext.getBodyAsJson().getString("username");
        var password = routingContext.getBodyAsJson().getString("password");

//        System.out.println("账号密码：");
//        System.out.println("账号:（加密后）"+username);
//        System.out.println("账号:（加密后）"+password);

        String privateKey = null;
        privateKey = getString("src/main/java/resources/verifies/privateKey");

        username = RSAUtil.decrypt(privateKey, username);
        password = RSAUtil.decrypt(privateKey, password);

        System.out.println(username + " " + password);

        if (VerifyDatabaseHelper.isExisted(username, password)) {
            String newToken = jwtAuth.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(3600L));
            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(newToken);
            System.out.println("token写入数据库...");
            VerifyDatabaseHelper.updateToken(username, newToken);
            System.out.println("Username or password right, Verification succeed!");

        } else {
            System.out.println("Username or password error, Verification failed!");
            routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Username or password error, Verification failed!");
        }

    }

    private void authenticity(RoutingContext routingContext) {
        System.out.println("开始登录验证");

        String token = JwtUtils.findToken(routingContext);
        System.out.println("登录验证收到的token:"+token);

        JWTAuth jwtAuth =  JwtUtils.createJwt(routingContext);

        JsonObject config = new JsonObject().put("jwt", token);

        jwtAuth.authenticate(config, res -> {
            if (res.succeeded()) {
                if (VerifyDatabaseHelper.isTokenExisted(token)) {
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Verification succeeded!");
                } else {
                    routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Token expired, Verification failed!");
                }
            } else {
                routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end("Token expired, Verification failed!");
            }
        });
    }

    private void verifyCodePic(RoutingContext routingContext) {
        File dir = new File("src/main/java/resources/verifies");
        if (!dir.exists()) {
            System.out.println("创建图片存储目录...");
            dir.mkdir();
        }

        VerifyCode instance = new VerifyCode();
        String verifyCode = instance.getCode();

        File code = new File("src/main/java/resources/verifies/code");
        if (!code.exists()) {
            try {
                System.out.println("创建验证码存储文件...");
                code.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(dir,  "verifyCode.jpg");

        try {
            System.out.println("开始写入图片...");
            ImageIO.write(instance.getBuffImg(), "jpg", file);

            System.out.println("开始写入验证码...");
            FileOutputStream privateFileStream = new FileOutputStream("src/main/java/resources/verifies/code");
            BufferedOutputStream privateBuffer =new BufferedOutputStream(privateFileStream);
            privateBuffer.write(verifyCode.getBytes(),0,verifyCode.getBytes().length);
            privateBuffer.flush();
            privateBuffer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).sendFile("src/main/java/resources/verifies/verifyCode.jpg");
    }

    private void verifyCode(RoutingContext routingContext) {
        String code = getString("src/main/java/resources/verifies/code");
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(code);
    }

    private String getString(String fileName) {
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
