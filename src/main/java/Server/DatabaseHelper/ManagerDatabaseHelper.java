package Server.DatabaseHelper;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import static Server.DatabaseHelper.DbConstants.*;

/**
 * GM管理系统数据库基本操作帮助类
 */
public class ManagerDatabaseHelper {

    /**
     * 查询所有用户信息
     *
     * @return
     */
    public static HashMap<String, String> allManagerInfo() {
        HashMap<String, String> result = new HashMap<>();
        List<List<String>> tableBody = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();
        List<String> colName = new ArrayList<>(TABLE_USER_HEADER);
        result.put(COLUMN_KEY, JSON.toJSONString(colName));

        for (Element record : data) {
            List<String> rowData = new ArrayList<>();
            for (int index = 0; index <= INDEX_OF_PASSWORD; index++) {
                String row = record.getChildren().get(index).getAttributeValue(DATA_VALUE);
                if (!ROOT_USER.equals(row)) {
                    rowData.add(row);
                }
            }
            if (rowData.size() != 0) {
                tableBody.add(rowData);
            }
        }
        result.put(TABLE_BODY_KEY, JSON.toJSONString(tableBody));
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
        Element rootData = loadDatabase();
        for (Element record : rootData.getChildren()) {
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            // 定位username
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                for (Element serverAuth : authElement.getChildren()) {
                    // 定位server
                    if (server.equals(serverAuth.getAttributeValue(DATA_VALUE, server))) {
                        for (Element auth2 : serverAuth.getChildren()) {
                            // 定位要删除的auth
                            if (auth.equals(auth2.getAttributeValue(DATA_VALUE))) {
                                serverAuth.removeContent(auth2);
                                saveXml(rootData);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加用户权限
     *
     * @param username
     * @param server
     * @param auth
     */
    public static void addAuth(String username, String server, String auth, String type) {
        Element newAuth = new Element("auth2");
        newAuth.setAttribute(DATA_TYPE, type);
        newAuth.setAttribute(DATA_VALUE, auth);
        Element rootData = loadDatabase();

        for (Element record : rootData.getChildren()) {
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                for (Element serverAuth : authElement.getChildren()) {
                    if (server.equals(serverAuth.getAttributeValue(DATA_VALUE))) {
                        serverAuth.addContent(newAuth);
                        saveXml(rootData);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 删除用户所有相关记录
     *
     * @param username
     */
    public static void deleteUser(String username) {
        Element rootData = loadDatabase();

        for (Element record : rootData.getChildren()) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                rootData.removeContent(record);
            }
        }
        saveXml(rootData);
    }

    /**
     * 增加新用户
     *
     * @param userInfo
     */
    public static void addUser(List<String> userInfo) {
        Element newUser = new Element("record");
        Element rootData = loadDatabase();

        for (int colIndex = 0; colIndex < DB_HEADER_RECORD.size(); colIndex++) {
            Element column = new Element("cloumn");
            column.setAttribute(DATA_NAME, DB_HEADER_RECORD.get(colIndex));
            if (INDEX_OF_AUTH != colIndex) {
                column.setAttribute(DATA_VALUE, userInfo.get(colIndex));
            }
            newUser.addContent(column);
        }
        for (String server : DB_HEADER_SERVER) {
            Element auth = new Element("auth1");
            auth.setAttribute(DATA_NAME, "server");
            auth.setAttribute(DATA_VALUE, server);
            newUser.getChildren().get(INDEX_OF_AUTH).addContent(auth);
        }
        rootData.addContent(newUser);
        saveXml(rootData);
    }

    /**
     * 修改用户密码
     *
     * @param username
     * @param password
     */
    public static void updateUserInfo(String username, String password) {
        Element rootData = loadDatabase();

        for (Element record : rootData.getChildren()) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                record.getChildren().get(INDEX_OF_PASSWORD).setAttribute(DATA_VALUE, password);
                break;
            }
        }
        saveXml(rootData);
    }

    /**
     * 按类型获取权限(列表)
     *
     * @param username
     * @param server
     * @param type
     * @return
     */
    public static List<String> selectAuthList(String username, String type, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();

        for (Element record : data) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                for (Element auth : serverElement.getChildren()) {
                    if (type.equals(auth.getAttributeValue(DATA_TYPE))) {
                        result.add(auth.getAttributeValue(DATA_VALUE));
                    }
                }
                break;
            }
        }
        return result;
    }

    /**
     * 获取所有权限(表格)
     *
     * @param username
     * @param server
     * @return
     */
    public static HashMap<String, String> selectAuthTable(String username, String server) {
        HashMap<String, String> result = new HashMap<>();
        List<String> colName = TABLE_AUTH_HEADER;
        List<List<String>> body = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();

        for (Element record : data) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
            if (username.equals(unameElement.getAttributeValue(DATA_VALUE))) {
                for (Element auth : serverElement.getChildren()) {
                    List<String> row = new ArrayList<>();
                    row.add(auth.getAttributeValue(DATA_VALUE));
                    row.add(auth.getAttributeValue(DATA_TYPE));
                    body.add(row);
                }
                break;
            }
        }
        result.put(COLUMN_KEY, JSON.toJSONString(colName));
        result.put(TABLE_BODY_KEY, JSON.toJSONString(body));
        return result;
    }
}