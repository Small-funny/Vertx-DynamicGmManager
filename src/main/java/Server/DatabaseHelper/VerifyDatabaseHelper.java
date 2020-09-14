package Server.DatabaseHelper;

import org.jdom2.Element;
import java.util.ArrayList;
import java.util.List;

/**
 * GM管理系统验证帮助类
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
                Element unameElement = user.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME);
                Element pwordElement = user.getChildren().get(DatabaseConstants.INDEX_OF_PASSWORD);
                if (username.equals(unameElement.getAttributeValue("value"))
                        && password.equals(pwordElement.getAttributeValue("value"))) {
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
            for (Element user : data) {
                Element tkElement = user.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN);
                Element authElement = user.getChildren().get(DatabaseConstants.INDEX_OF_AUTH);
                if (token.equals(tkElement.getAttributeValue("value"))) {
                    for (Element serverAuth : authElement.getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue("value"))) {
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
                Element unameElement = record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME);
                Element tkElement = record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN);
                if (username.equals(unameElement.getAttributeValue("value"))) {
                    tkElement.setAttribute("value", token);
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
                Element tkElement = record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN);
                if (token.equals(tkElement.getAttributeValue("value"))) {
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
                Element tkElement = record.getChildren().get(DatabaseConstants.INDEX_OF_TOKEN);
                Element unameElement = record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME);
                if (token.equals(tkElement.getAttributeValue("value"))) {
                    username = unameElement.getAttributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return username;
    }

}
