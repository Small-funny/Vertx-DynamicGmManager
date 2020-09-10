package Server.Automation;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageUtil {
    public static final String MAIN_PAGE_ROUTER="0";
    public static final String TYPE_LIST ="list";
    public static final String TYPE_TABLE="table";
    public static final HashMap<String, Element> PAGE_ELEMENT =new HashMap<>();
    public static final HashMap<String, Element> TYPE_ELEMENT = new HashMap<>();
    public static final HashMap<String, Element> SERVER_ELEMENT = new HashMap<>();
    public static final List<String> USER_MANAGE_PAGES = new ArrayList<>();
    public static final List<String> CONFIG_MANAGE_PAGES = new ArrayList<>();
    static {
        SAXBuilder saxBuilder = new SAXBuilder();
        Element root = null;
        try {
            root = saxBuilder.build("src/main/java/resources/properties.xml").getDocument().getRootElement();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        List<Element> typeList = root.getChild("pages").getChildren();
        for (Element typeE : typeList) {
            TYPE_ELEMENT.put(typeE.getAttributeValue("name"), typeE);
            List<Element> pageList = typeE.getChildren();
            for (Element pageE : pageList) {
                PAGE_ELEMENT.put(pageE.getAttributeValue("url"), pageE);
                if("userManage".equals(pageE.getAttributeValue("type"))){
                    USER_MANAGE_PAGES.add(pageE.getAttributeValue("authorization"));
                }else if("configManage".equals(pageE.getAttributeValue("type"))){
                    CONFIG_MANAGE_PAGES.add(pageE.getAttributeValue("authorization"));
                }
            }
        }
        List<Element> serverList = root.getChild("servers").getChildren();
        for (Element element : serverList) {
            SERVER_ELEMENT.put(element.getAttributeValue("value"), element);
        }
    }
}
