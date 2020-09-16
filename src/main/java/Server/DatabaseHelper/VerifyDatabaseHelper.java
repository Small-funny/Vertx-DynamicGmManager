package Server.DatabaseHelper;

import org.jdom2.Element;
import java.util.ArrayList;
import java.util.List;

/**
 * GM管理系统验证帮助类
 */
public class VerifyDatabaseHelper {

    /**
     * 验证用户账号密码
     * 
     * @param username
     * @param password
     * @return
     */
    public static boolean verifyIsExisted(String username, String password) {
        boolean result = false;
        List<Element> data = DbConstants.loadDatabase().getChildren();
        try {
            for (Element user : data) {
                Element unameElement = user.getChildren().get(DbConstants.INDEX_OF_USERNAME);
                Element pwordElement = user.getChildren().get(DbConstants.INDEX_OF_PASSWORD);
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DbConstants.DATA_VALUE).toLowerCase())
                        && password.equals(pwordElement.getAttributeValue(DbConstants.DATA_VALUE))) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 用户是否存在
     * 
     * @param username
     * @param password
     * @return
     */
    public static boolean isExisted(String username) {
        boolean result = false;
        List<Element> data = DbConstants.loadDatabase().getChildren();
        try {
            for (Element user : data) {
                Element unameElement = user.getChildren().get(DbConstants.INDEX_OF_USERNAME);
                if (username.toLowerCase()
                        .equals(unameElement.getAttributeValue(DbConstants.DATA_VALUE).toLowerCase())) {
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
        List<Element> data = DbConstants.loadDatabase().getChildren();
        try {
            for (Element user : data) {
                Element tkElement = user.getChildren().get(DbConstants.INDEX_OF_TOKEN);
                Element authElement = user.getChildren().get(DbConstants.INDEX_OF_AUTH);
                if (token.equals(tkElement.getAttributeValue(DbConstants.DATA_VALUE))) {
                    for (Element serverAuth : authElement.getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue(DbConstants.DATA_VALUE))) {
                            for (Element list : serverAuth.getChildren()) {
                                result.add(list.getAttributeValue(DbConstants.DATA_VALUE));
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
        Element rootData = DbConstants.loadDatabase();
        List<Element> data = rootData.getChildren();
        try {
            for (Element record : data) {
                Element unameElement = record.getChildren().get(DbConstants.INDEX_OF_USERNAME);
                Element tkElement = record.getChildren().get(DbConstants.INDEX_OF_TOKEN);
                if (username.equals(unameElement.getAttributeValue(DbConstants.DATA_VALUE))) {
                    tkElement.setAttribute(DbConstants.DATA_VALUE, token);
                    break;
                }
            }
            DbConstants.saveXml(rootData);
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
        List<Element> data = DbConstants.loadDatabase().getChildren();
        try {
            for (Element record : data) {
                Element tkElement = record.getChildren().get(DbConstants.INDEX_OF_TOKEN);
                if (token.equals(tkElement.getAttributeValue(DbConstants.DATA_VALUE))) {
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
        List<Element> data = DbConstants.loadDatabase().getChildren();
        try {
            for (Element record : data) {
                Element tkElement = record.getChildren().get(DbConstants.INDEX_OF_TOKEN);
                Element unameElement = record.getChildren().get(DbConstants.INDEX_OF_USERNAME);
                if (token.equals(tkElement.getAttributeValue(DbConstants.DATA_VALUE))) {
                    username = unameElement.getAttributeValue(DbConstants.DATA_VALUE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return username;
    }

}
