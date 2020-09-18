package Server.DatabaseHelper;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库通用工具
 */
@Slf4j
public class DbConstants {
    // 数据库层级结构
    public static final List<String> DB_STRUCTURE = Arrays.asList("record", "column", "auth1", "auth2");
    // 数据库用户记录表头
    public static final List<String> DB_HEADER_RECORD = Arrays.asList("username", "password", "token", "auth"); 
    // 数据库服务器记录表头
    public static final List<String> DB_HEADER_SERVER = Arrays.asList("sandbox", "test", "master");
    // 用户名记录索引
    public static final int INDEX_OF_USERNAME = 0; 
    // 密码记录索引
    public static final int INDEX_OF_PASSWORD = 1; 
    // 令牌记录索引
    public static final int INDEX_OF_TOKEN = 2;
    // 权限记录索引
    public static final int INDEX_OF_AUTH = 3;
    // 数据名称
    public static final String DATA_NAME = "name";
    // 数据值
    public static final String DATA_VALUE = "value";
    // 数据类型
    public static final String DATA_TYPE = "type";
    // 根用户名
    public static final String ROOT_USER = "root";
    // 构造表格属性列表的key
    public static final String COLUMN_KEY = "colName";
    // 构造表格内容列表的key
    public static final String TABLE_BODY_KEY = "tableBody";
    // 用户信息表头
    public static final List<String> TABLE_USER_HEADER = Arrays.asList("用户名", "密码");
    // 权限信息表头
    public static final List<String> TABLE_AUTH_HEADER = Arrays.asList("权限", "类型");
    // 目录级别权限类型
    public static final String TYPE_AUTH_CATALOG = "list";
    // 按钮级别权限类型
    public static final String TYPE_AUTH_CONTROL = "btn";
    // 管理员权限
    public static final String AUTH_ROOT = "角色管理";
    // 根管理员(拥有所有权限，可根据需要分配多个)
    public static final String USER_ROOT = "root";
    // 最高等级管理员
    public static final String LEVEL_SUPER = "sup";
    // 第二等级管理员
    public static final String LEVEL_MIDDLE = "mid";
    // 最低等级管理员
    public static final String LEVEL_LOW = "low";

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
    public static void saveXml(Element data) {
        Format format = Format.getCompactFormat();
        format.setEncoding("utf-8");
        format.setIndent("    ");
        XMLOutputter outPutter = new XMLOutputter(format);
        try {
            outPutter.output(data, new FileOutputStream("src/main/java/resources/Database.xml"));
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }
}