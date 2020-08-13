package Server.Authorization;

import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.List;

public class JwtUtils {

    public static JWTAuth createJwt(RoutingContext routingContext) {

        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("Keystore.jceks")
                        .setPassword("secret"));

        long starttime = System.currentTimeMillis();
        System.out.println("验证令牌计时开始");

        JWTAuth jwtAuth = JWTAuth.create(routingContext.vertx(), jwtAuthOptions);
        long endtime = System.currentTimeMillis();

        System.out.println("程序运行时间：" + (endtime - starttime) + "ms");

        return jwtAuth;
    }

    public static String findToken(RoutingContext routingContext) {
        String token = "token";
        String Cookies = routingContext.request().getHeader("Cookie");
        List<String> CookieList = Arrays.asList(Cookies.split("; "));
        for (String str : CookieList) {
            str = str.replaceAll(" ","");
            String cookie = str.split("=")[0];
            if (cookie.equals("Token")) {
                token = str.split("=")[1];
            }
        }
        return token;
    }

}
