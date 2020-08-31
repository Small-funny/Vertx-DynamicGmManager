package Server.DatabaseHelper;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 数据库操作
 */
public class DatabaseHelper {

    private static final List<String> HEADE_LIST = Arrays.asList("username", "password", "token", "enable", "auth");    //用户记录模板

    private static final List<String> SERVER_LIST = Arrays.asList("sandbox", "test", "master");    //服务器记录模板

    private static final int INDEX_OF_USERNAME = 0;    //用户名记录索引

    private static final int INDEX_OF_PASSWORD = 1;    //密码记录索引

    private static final int INDEX_OF_TOKEN = 2;    //令牌记录索引

    private static final int INDEX_OF_ENABLE = 3;    //账户状态记录索引

    private static final int INDEX_OF_AUTH = 4;    //权限记录索引

    /**
     * 获取数据库根元素
     * @return
     */
    public static Element loadDatabase() {
        Element data = null;
        try {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build("src/main/java/resources/Database.xml");
            data = doc.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 用户是否存在
     * @param username
     * @param password
     * @return
     */
    public static boolean isExisted(String username, String password) {
        boolean result = false;
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element user: data) {
                if (username.equals(user.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value")) &&
                        password.equals(user.getChildren().get(INDEX_OF_PASSWORD).getAttributeValue("value"))) {
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
     * @param token
     * @param server
     * @return
     */
    public static List<String> selectAuthority(String token, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element user: data) {  //遍历所有record
                if (token.equals(user.getChildren().get(INDEX_OF_TOKEN).getAttributeValue("value"))) {  //根据token定位
                    for (Element serverAuth: user.getChildren().get(INDEX_OF_AUTH).getChildren()) {  //遍历用户所有权限
                        if (server.equals(serverAuth.getAttributeValue("value"))) {   //定位server级别的权限
                            //取出该server级别权限下的所有子权限
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
     * @param username
     * @param token
     */
    public static void updateToken(String username, String token) {
        Element rootData = loadDatabase();
        List<Element> data = rootData.getChildren();
        try {
            for (Element record: data) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    record.getChildren().get(INDEX_OF_TOKEN).setAttribute("value", token);
                    break;
                }
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Token是否存在
     * @param token
     * @return
     */
    public static boolean isTokenExisted(String token) {
        boolean result = false;
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element record: data) {
                if (token.equals(record.getChildren().get(INDEX_OF_TOKEN).getAttributeValue("value"))) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<HashMap<String, String>> selectManagerInfo(List<String> Attributes) {
        List<HashMap<String, String>> result = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element record: data) {
                HashMap<String, String> map = new HashMap<>();
                for (String key: Attributes) {
                    for (int i = 0; i < record.getChildren().size(); i++) {
                        if (key.equals(record.getChildren().get(i).getAttributeValue("name"))) {
                            map.put(key, record.getChildren().get(i).getAttributeValue("value"));
                        }
                    }
                }
                result.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 将Token转化为用户名
     * @param token
     * @return
     */
    public static String tokenToUsername(String token) {
        String username = null;
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element record: data) {
                if (token.equals(record.getChildren().get(INDEX_OF_TOKEN).getAttributeValue("value"))) {
                    username = record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return username;
    }

    /**
     * 查询所有用户信息
     * @return
     */
    public static HashMap<String, Object> allUserInfo() {
        HashMap<String, Object> result = new HashMap<>();
        List<String> colName = new ArrayList<>(HEADE_LIST);
        List<List<String>> tableBody = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();
        try {
            result.put("colName", colName);
            for (Element record: data) {
                List<String> rowData = new ArrayList<>();
                for (int index = 0; index < record.getChildren().size() - 1; index++) {
                    rowData.add(record.getChildren().get(index).getAttributeValue("value"));
                }
                tableBody.add(rowData);
            }
            result.put("tableBody", tableBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 删除用户权限
     * @param username
     * @param server
     * @param auth
     */
    public static void deleteAuth(String username, String server, String auth) {
        Element rootData = loadDatabase();
        try {
            for (Element record: rootData.getChildren()) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    for (Element serverAuth: record.getChildren().get(INDEX_OF_AUTH).getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue("value", server))) {
                            for (Element auth2: serverAuth.getChildren()) {
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
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加用户权限
     * @param username
     * @param server
     * @param auth
     */
    public static void addAuth(String username, String server, String auth) {
        Element rootData = loadDatabase();
        Element newAuth = new Element("auth2");
        newAuth.setAttribute("name", "list");
        newAuth.setAttribute("value", auth);
        try {
            for (Element record: rootData.getChildren()) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    for (Element serverAuth: record.getChildren().get(INDEX_OF_AUTH).getChildren()) {
                        if (server.equals(serverAuth.getAttributeValue("value"))) {
                            serverAuth.addContent(newAuth);
                            break;
                        }
                    }
                    break;
                }
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除用户所有相关记录
     * @param username
     */
    public static void deleteUser(String username) {
        Element rootData = loadDatabase();
        try {
            for (Element record: rootData.getChildren()) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    rootData.removeContent(record);
                }
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加新用户
     * @param userInfo
     */
    public static void addUser(List<String> userInfo) {
        Element rootData = loadDatabase();
        Element newUser = new Element("record");
        try {
            for (int colIndex = 0; colIndex < HEADE_LIST.size(); colIndex ++) {
                Element column = new Element("cloumn");
                column.setAttribute("name", HEADE_LIST.get(colIndex));
                column.setAttribute("value", userInfo.get(colIndex));
                newUser.addContent(column);
            }
            for (String server: SERVER_LIST) {
                Element auth = new Element("auth1");
                auth.setAttribute("name", "server");
                auth.setAttribute("value", server);
                newUser.getChildren().get(INDEX_OF_AUTH).addContent(auth);
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户状态
     * @param username
     */
    public static void changeUserStatus(String username) {
        Element rootData = loadDatabase();
        try {
            for (Element record: rootData.getChildren()) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    String str = record.getChildren().get(INDEX_OF_ENABLE).getAttributeValue("value").equals("1") ? "0" : "1";
                    record.getChildren().get(INDEX_OF_ENABLE).setAttribute("value", str);
                    break;
                }
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户密码
     * @param username
     * @param password
     */
    public static void updateUserInfo(String username, String password) {
        Element rootData = loadDatabase();
        try {
            for (Element record: rootData.getChildren()) {
                if (username.equals(record.getChildren().get(INDEX_OF_USERNAME).getAttributeValue("value"))) {
                    record.getChildren().get(INDEX_OF_PASSWORD).setAttribute("value", password);
                    break;
                }
            }
            saveXml(rootData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * 保存数据库的修改结果
     * @param data
     * @throws Exception
     */
    private static void saveXml(Element data) throws Exception{
        XMLOutputter outPutter = new XMLOutputter();
        outPutter.output(data, new FileOutputStream("src/main/java/resources/Database.xml"));
    }

    public static void main(String[] args) {
        changeUserStatus("username");
    }
}
