package Server.DatabaseHelper;

import org.jdom2.Element;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作
 */
public class VerifyDatabaseHelper {

    /**
     * 用户是否存在
     * 
     * @param username
     * @param password
     * @return
     */
    public static boolean isExisted(String username, String password) {
        boolean result = false;
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            for (Element user : data) {
                if (username
                        .equals(user.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))
                        && password.equals(user.getChildren().get(DatabaseConstants.INDEX_OF_PASSWORD)
                                .getAttributeValue("value"))) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询特定用户的所有权限
     * 
     * @param token
     * @param server
     * @return
     */
    public static List<String> selectAuthority(String token, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            for (Element user : data) { // 遍历所有record
                if (token.equals(user.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN).getAttributeValue("value"))) { // 根据token定位
                    for (Element serverAuth : user.getChildren().get(DatabaseConstants.INDEX_OF_AUTH).getChildren()) { // 遍历用户所有权限
                        if (server.equals(serverAuth.getAttributeValue("value"))) { // 定位server级别的权限
                            // 取出该server级别权限下的所有子权限
                            for (Element list : serverAuth.getChildren()) {
                                result.add(list.getAttributeValue("value"));
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新Token
     * 
     * @param username
     * @param token
     */
    public static void updateToken(String username, String token) {
        Element rootData = DatabaseConstants.loadDatabase();
        List<Element> data = rootData.getChildren();
        try {
            for (Element record : data) {
                if (username.equals(
                        record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN).setAttribute("value", token);
                    break;
                }
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Token是否存在
     * 
     * @param token
     * @return
     */
    public static boolean isTokenExisted(String token) {
        boolean result = false;
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            for (Element record : data) {
                if (token.equals(
                        record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN).getAttributeValue("value"))) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将Token转化为用户名
     * 
     * @param token
     * @return
     */
    public static String tokenToUsername(String token) {
        String username = null;
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            for (Element record : data) {
                if (token.equals(
                        record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN).getAttributeValue("value"))) {
                    username = record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return username;
    }

}
