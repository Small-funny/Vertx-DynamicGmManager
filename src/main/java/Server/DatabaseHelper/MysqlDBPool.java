package Server.DatabaseHelper;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class MysqlDBPool {

    private static DruidDataSource dbpool;
    private static boolean isInited = false;

    public synchronized static boolean init(InputStream in) {
        if (isInited) {
            return true;
        }
        try {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(in);
            Element pool = doc.getRootElement();
            dbpool = (DruidDataSource) DruidDataSourceFactory.createDataSource(getPropertiesMap(pool));
            isInited = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean init(String fileName) {
        boolean b = false;
        try {
            FileInputStream in = new FileInputStream(fileName);
            b = init(in);
            in.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    private static HashMap<String, String> getPropertiesMap(Element element) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        List<Element> list = element.getChildren("property");
        for (Element propertyElement : list) {
            map.put(propertyElement.getAttributeValue("name"), propertyElement.getAttributeValue("value"));
        }
        return map;
    }

    public static Connection getConnection() {
        try {
            return dbpool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
