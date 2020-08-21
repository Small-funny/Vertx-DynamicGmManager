package Server.DatabaseHelper;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper {

    public static Element loadDatabase() {
        Element data = null;
        try {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build("Database.xml");
            data = doc.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static boolean isExisted(String username, String password) {
        boolean result = false;
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element user: data) {
                if (username.equals(user.getChildren().get(0).getAttributeValue("value")) &&
                        password.equals(user.getChildren().get(1).getAttributeValue("value"))) {
                    result = true;
                }
            }
        } catch (Exception e) {

        }
        return result;
    }

    public static List<String> selectAuthority(String token, String server) {
        List<String> result = new ArrayList<>();
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element user: data) {  //遍历所有record
                if (token.equals(user.getChildren().get(2).getAttributeValue("value"))) {  //根据token定位
                    for (Element serverAuth: user.getChildren().get(4).getChildren()) {  //遍历用户所有权限
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

    public static void updateToken(String username, String token) {
        Element rootData = loadDatabase();
        List<Element> data = rootData.getChildren();
        try {
            for (Element record: data) {
                if (username.equals(record.getChildren().get(0).getAttributeValue("value"))) {
                    record.getChildren().get(2).setAttribute("value", token);
                    XMLOutputter outPutter = new XMLOutputter();
                    outPutter.output(rootData, new FileOutputStream("Database.xml"));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTokenExisted(String token) {
        boolean result = false;
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element record: data) {
                if (token.equals(record.getChildren().get(2).getAttributeValue("value"))) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void updateAuth() {

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

    public static void main(String[] args) {
        List<String> test = new ArrayList<>();
        test.add("username");
        test.add("password");
        test.add("token");
        test.add("enable");
        System.out.println(selectManagerInfo(test));
    }
}
