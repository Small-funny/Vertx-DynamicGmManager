package Server.Verify;

import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtils {

    /**
     * 创建token
     * @param routingContext
     * @return
     */
    public static JWTAuth createJwt(RoutingContext routingContext) {
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("src/main/java/resources/verifies/Keystore.jceks")
                        .setPassword("secret"));

        long starttime = System.currentTimeMillis();
        log.info("Auth object create timer start");

        JWTAuth jwtAuth = JWTAuth.create(routingContext.vertx(), jwtAuthOptions);

        long endtime = System.currentTimeMillis();
        log.info("Time：" + (endtime - starttime) + "ms");

        return jwtAuth;
    }

    /**
     * 用户发送的数据中提取token
     * @param routingContext
     * @return
     */
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
