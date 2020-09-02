package Server.DatabaseHelper;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class DatabaseConstants {
    public static final List<String> HEADER_LIST = Arrays.asList("username", "password", "token", "enable", "auth"); // 用户记录模板

    public static final List<String> SERVER_LIST = Arrays.asList("sandbox", "test", "master"); // 服务器记录模板

    public static final int INDEX_OF_USERNAME = 0; // 用户名记录索引

    public static final int INDEX_OF_PASSWORD = 1; // 密码记录索引

    public static final int INDEX_OF_TOKEN = 2; // 令牌记录索引

    public static final int INDEX_OF_ENABLE = 3; // 账户状态记录索引

    public static final int INDEX_OF_AUTH = 4; // 权限记录索引

    /**
     * 获取数据库根元素
     * 
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
     * 保存数据库的修改结果
     * 
     * @param data
     * @throws Exception
     */
    public static void saveXml(Element data) throws Exception {
        XMLOutputter outPutter = new XMLOutputter();
        outPutter.output(data, new FileOutputStream("src/main/java/resources/Database.xml"));
    }

}
