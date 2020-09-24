package Server.DatabaseHelper;

import Server.Automation.PageUtil;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import static Server.DatabaseHelper.DbConstants.*;
import static Server.DatabaseHelper.VerifyDatabaseHelper.*;

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
                if (!VerifyDatabaseHelper.isSupLevel(row)) {
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
     * @param type
     * @return
     */
    public static String deleteAuth(String username, String server, String auth, String type) {
        Element rootData = loadDatabase();

        if (!isExisted(username)) {
            return "用户不存在，添加失败";
        } else {
            for (Element record : rootData.getChildren()) {
                Element authElement = record.getChildren().get(INDEX_OF_AUTH);
                Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
                // 定位username
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                    for (Element serverAuth : authElement.getChildren()) {
                        // 定位server
                        if (server.equals(serverAuth.getAttributeValue(DATA_VALUE, server))) {
                            for (Element auth2 : serverAuth.getChildren()) {
                                // 定位要删除的auth
                                if (auth.equals(auth2.getAttributeValue(DATA_VALUE)) && type.equals(auth2.getAttributeValue(DATA_TYPE))) {
                                    serverAuth.removeContent(auth2);
                                    saveXml(rootData);
                                    return "删除成功";
                                }
                            }
                        }
                    }
                }
            }
        }
        return "权限不存在，删除失败";
    }

    /**
     * 添加用户权限
     * 
     * @param username
     * @param server
     * @param auth
     * @return
     */
    public static String addAuth(String username, String server, String auth) {
        Element newAuth = new Element("auth2");
        String type = selectAuthType(ROOT_USER, server).get(auth);
        newAuth.setAttribute(DATA_TYPE, type);
        newAuth.setAttribute(DATA_VALUE, auth);
        Element rootData = loadDatabase();
        List<String> authList = selectAuthList(username, type, server);
        List<String> rootAuthList = selectAuthList(USER_ROOT, type, server);

        if (!isExisted(username)) {
            return "用户不存在，添加失败";
        } else if (authList.contains(auth)) {
            return "权限已存在，添加失败";
        } else if (!rootAuthList.contains(auth)) {
            return "权限输入错误，不可添加";
        } else {
            for (Element record : rootData.getChildren()) {
                Element authElement = record.getChildren().get(INDEX_OF_AUTH);
                Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                    for (Element serverAuth : authElement.getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue(DATA_VALUE))) {
                            serverAuth.addContent(newAuth);
                            saveXml(rootData);
                            return "添加成功";
                        }
                    }
                }
            }
        }
        return "服务器填写错误，添加失败";
    }

    /**
     * 删除用户所有相关记录
     * 
     * @param username
     * @return
     */
    public static String deleteUser(String username) {
        Element rootData = loadDatabase();
        if (!isExisted(username)) {
            return "用户不存在，删除失败";
        } else {
            for (Element record : rootData.getChildren()) {
                Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                    rootData.removeContent(record);
                }
            }
            saveXml(rootData);
            return "删除成功";
        }
    }

    /**
     * 增加新用户
     * 
     * @param userInfo
     * @return
     */
    public static String addUser(List<String> userInfo) {
        Element newUser = new Element("record");
        Element rootData = loadDatabase();
//        userInfo.set(INDEX_OF_USERNAME, userInfo.get(INDEX_OF_USERNAME).toLowerCase());

        if (isExisted(userInfo.get(INDEX_OF_USERNAME))) {
            return "用户存在，添加失败";
        } else {
            for (int colIndex = 0; colIndex < DB_HEADER_RECORD.size(); colIndex++) {
                Element column = new Element("cloumn");
                column.setAttribute(DATA_NAME, DB_HEADER_RECORD.get(colIndex));
                if (INDEX_OF_AUTH != colIndex) {
                    column.setAttribute(DATA_VALUE, userInfo.get(colIndex));
                } else {
                    column.setAttribute(DATA_VALUE, LEVEL_ORDINARY);
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
            return "添加成功";
        }
    }

    /**
     * 修改用户密码
     * 
     * @param username
     * @param password
     * @return
     */
    public static String updateUserInfo(String username, String password) {
        Element rootData = loadDatabase();
        if (!isExisted(username)) {
            return "用户不存在，修改失败";
        } else {
            for (Element record : rootData.getChildren()) {
                Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                    record.getChildren().get(INDEX_OF_PASSWORD).setAttribute(DATA_VALUE, password);
                    break;
                }
            }
            saveXml(rootData);
            return "修改成功";
        }
    }

    /**
     * 按类型获取权限(列表)
     * 
     * @param username
     * @param type
     * @param server
     * @return
     */
    public static List<String> selectAuthList(String username, String type, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();

        for (Element record : data) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
            if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
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
        if (ROOT_USER.equals(username)) {
            List<String> row = Arrays.asList("无法查看", "无");
            body.add(row);
        } else if (!VerifyDatabaseHelper.isExisted(username)) {
            List<String> row = Arrays.asList("无此用户", "无");
            body.add(row);
        } else {
            for (Element record : data) {
                Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
                Element authElement = record.getChildren().get(INDEX_OF_AUTH);
                Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
                if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                    for (Element auth : serverElement.getChildren()) {
                        List<String> row = new ArrayList<>();
                        row.add(auth.getAttributeValue(DATA_VALUE));
                        row.add(auth.getAttributeValue(DATA_TYPE));
                        body.add(row);
                    }
                    break;
                }
            }
        }
        result.put(COLUMN_KEY, JSON.toJSONString(colName));
        result.put(TABLE_BODY_KEY, JSON.toJSONString(body));
        return result;
    }

    /**
     * 获取权限类型
     * 
     * @param username
     * @param server
     * @return key为权限，value为权限type
     */
    public static HashMap<String, String> selectAuthType(String username, String server) {
        HashMap<String, String> result = new HashMap<>();

        List<Element> data = loadDatabase().getChildren();
        for (Element record : data) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
            if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                for (Element auth : serverElement.getChildren()) {
                    result.put(auth.getAttributeValue(DATA_VALUE), auth.getAttributeValue(DATA_TYPE));
                }
                break;
            }
        }
        return result;
    }

    /**
     * 整体更新权限列表
     *
     * @param authSettings
     * @param server
     * @param username
     * @return
     */
    public static String updateAuth(List<String> authSettings, String server, String username) {
        Element rootData = loadDatabase();
        HashMap<String, String> authTypeMap = selectAuthType(ROOT_USER, server);

        for (Element record : rootData.getChildren()) {
            Element unameElement = record.getChildren().get(INDEX_OF_USERNAME);
            Element authElement = record.getChildren().get(INDEX_OF_AUTH);
            Element serverElement = authElement.getChildren().get(DB_HEADER_SERVER.indexOf(server));
            if (username.toLowerCase().equals(unameElement.getAttributeValue(DATA_VALUE).toLowerCase())) {
                serverElement.removeContent();
                for (String auth: authSettings) {
                    Element newAuth = new Element("auth2");
                    newAuth.setAttribute(DATA_TYPE, authTypeMap.get(auth));
                    newAuth.setAttribute(DATA_VALUE, auth);
                    serverElement.addContent(newAuth);
                }
            }
        }
        saveXml(rootData);
        return "修改成功";
    }
}