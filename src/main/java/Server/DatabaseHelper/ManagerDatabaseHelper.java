package Server.DatabaseHelper;

import org.jdom2.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class ManagerDatabaseHelper {
    
    /**
     * 查询所有用户信息
     * 
     * @return
     */
    public static HashMap<String, String> allManagerInfo() {
        HashMap<String, String> result = new HashMap<>();
        List<String> colName = new ArrayList<>(DatabaseConstants.HEADER_LIST);
        List<List<String>> tableBody = new ArrayList<>();
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            result.put("colName", JSON.toJSONString(colName));
            for (Element record : data) {
                List<String> rowData = new ArrayList<>();
                for (int index = 0; index < record.getChildren().size() - 1; index++) {
                    rowData.add(record.getChildren().get(index).getAttributeValue("value"));
                }
                tableBody.add(rowData);
            }
            result.put("tableBody", JSON.toJSONString(tableBody));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除用户权限
     * 
     * @param username
     * @param server
     * @param auth
     */
    public static void deleteAuth(String username, String server, String auth) {
        Element rootData = DatabaseConstants.loadDatabase();
        try {
            for (Element record : rootData.getChildren()) {
                if (username.equals(record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    for (Element serverAuth : record.getChildren().get(DatabaseConstants.INDEX_OF_AUTH).getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue("value", server))) {
                            for (Element auth2 : serverAuth.getChildren()) {
                                if (auth.equals(auth2.getAttributeValue("value"))) {
                                    serverAuth.removeContent(auth2);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            DatabaseConstants.saveXml(rootData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加用户权限
     * 
     * @param username
     * @param server
     * @param auth
     */
    public static void addAuth(String username, String server, String auth) {
        Element rootData = DatabaseConstants.loadDatabase();
        Element newAuth = new Element("auth2");
        newAuth.setAttribute("name", "list");
        newAuth.setAttribute("value", auth);
        try {
            for (Element record : rootData.getChildren()) {
                if (username.equals(
                        record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    for (Element serverAuth : record.getChildren().get(DatabaseConstants.INDEX_OF_AUTH).getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue("value"))) {
                            serverAuth.addContent(newAuth);
                            break;
                        }
                    }
                    break;
                }
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除用户所有相关记录
     * 
     * @param username
     */
    public static void deleteUser(String username) {
        Element rootData = DatabaseConstants.loadDatabase();
        try {
            for (Element record : rootData.getChildren()) {
                if (username.equals(
                        record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    rootData.removeContent(record);
                }
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加新用户
     * 
     * @param userInfo
     */
    public static void addUser(List<String> userInfo) {
        Element rootData = DatabaseConstants.loadDatabase();
        Element newUser = new Element("record");
        try {
            for (int colIndex = 0; colIndex < DatabaseConstants.HEADER_LIST.size(); colIndex++) {
                Element column = new Element("cloumn");
                column.setAttribute("name", DatabaseConstants.HEADER_LIST.get(colIndex));
                if (!"auth".equals(DatabaseConstants.HEADER_LIST.get(colIndex))) {
                    column.setAttribute("value", userInfo.get(colIndex));
                }
                newUser.addContent(column);
            }
            for (String server : DatabaseConstants.SERVER_LIST) {
                Element auth = new Element("auth1");
                auth.setAttribute("name", "server");
                auth.setAttribute("value", server);
                newUser.getChildren().get(DatabaseConstants.INDEX_OF_AUTH).addContent(auth);
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户状态
     * 
     * @param username
     */
    public static void updateUserStatus(String username) {
        Element rootData = DatabaseConstants.loadDatabase();
        try {
            for (Element record : rootData.getChildren()) {
                if (username.equals(record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    String str = record.getChildren().get(DatabaseConstants.INDEX_OF_ENABLE).getAttributeValue("value")
                            .equals("1") ? "0" : "1";
                    record.getChildren().get(DatabaseConstants.INDEX_OF_ENABLE).setAttribute("value", str);
                    break;
                }
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户密码
     * 
     * @param username
     * @param password
     */
    public static void updateUserInfo(String username, String password) {
        Element rootData = DatabaseConstants.loadDatabase();
        try {
            for (Element record : rootData.getChildren()) {
                if (username.equals(record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    record.getChildren().get(DatabaseConstants.INDEX_OF_PASSWORD).setAttribute("value", password);
                    break;
                }
            }
            DatabaseConstants.saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 按类型获取权限列表
     * 
     * @param username
     * @param server
     * @param type
     * @return
     */
    public static List<String> selectAuthList(String username, String type, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = DatabaseConstants.loadDatabase().getChildren();
        try {
            for (Element record : data) {
                if (username.equals(record.getChildren().get(DatabaseConstants.INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    for (Element auth : record.getChildren().get(DatabaseConstants.INDEX_OF_AUTH).getChildren()
                            .get(DatabaseConstants.SERVER_LIST.indexOf(server)).getChildren()) {
                        if (type.equals(auth.getAttributeValue("name"))) {
                            result.add(auth.getAttributeValue("value"));
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
}