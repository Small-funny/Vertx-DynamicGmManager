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

    public static HashMap<String, List<String>> selectAuthority(String token, String server) {
        HashMap<String, List<String>> result = new HashMap<>();
        List<Element> data = loadDatabase().getChildren();
        try {
            for (Element user: data) {
                if (token.equals(user.getChildren().get(2).getAttributeValue("value"))) {
                    for (Element serverAuth: user.getChildren().get(4).getChildren()) {
                        for (Element list: serverAuth.getChildren()) {
                            List<String> authList = new ArrayList<>();
                            for (Element auth: list.getChildren()) {
                                authList.add(auth.getAttributeValue("value"));
                            }
                            result.put(list.getAttributeValue("value"), authList);
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

    public static void updateToken(String token) {
        Element rootData = loadDatabase();
        List<Element> data = rootData.getChildren();
        try {
            for (Element record: data) {
                if (token.equals(record.getChildren().get(2).getAttributeValue("value"))) {
                    record.getChildren().get(2).setAttribute("value", "token");
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

//    public static void main(String[] args) {
//
//    }
}
