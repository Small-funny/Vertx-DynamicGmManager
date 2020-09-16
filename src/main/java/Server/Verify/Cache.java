package Server.Verify;

import java.util.HashMap;
import Server.DatabaseHelper.VerifyDatabaseHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存操作类
 */
@Slf4j
public class Cache {
    //表单数据缓存
    private static HashMap<String, HashMap<String, String>> formData = new HashMap<>();

    /**
     * 设置表单缓存
     * 
     * @param token
     * @param args
     */
    public static void setArgs(String token, HashMap<String, String> args) {
        log.info("Set args cache");
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            formData.put(username, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取表单缓存
     * 
     * @param token
     * @return
     */
    public static HashMap<String, String> getArgs(String token) {
        log.info("Get args cache");
        HashMap<String, String> args = null;
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            args = formData.get(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return args;
    }

    /**
     * 删除表单缓存
     * 
     * @param token
     */
    public static void removeArgs(String token) {
        log.info("Delete args cache");
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            formData.remove(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}