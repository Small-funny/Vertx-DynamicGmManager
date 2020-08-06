package Server;


import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.util.*;

@Slf4j
public class XmlMapping {

    //页面名是键 对应的页面的element是值
    private static final HashMap<String, Element> pageElement = new HashMap<>();
    //页面类型是键 对应的类型element是值
    private static final HashMap<String, Element> typeElement = new HashMap<>();
    private static final List<String> iconTiList = new ArrayList<>();
    private static final List<String> iconFaList = new ArrayList<>();

    public XmlMapping() throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Element root = saxBuilder.build("properties.xml").getDocument().getRootElement();

        List<Element> typeList = root.getChildren();
        for (Element typeE : typeList) {
            typeElement.put(typeE.getAttribute("name").getValue(), typeE);
            List<Element> pageList = typeE.getChildren();
            for (Element pageE : pageList) {
                pageElement.put(pageE.getAttribute("name").getValue(), pageE);
            }
        }
        iconTiList.add("ti-heart");
        iconTiList.add("ti-folder");
        iconTiList.add("ti-export");
        iconTiList.add("ti-eye");
        iconTiList.add("ti-crown");
        iconTiList.add("ti-comment");
        iconTiList.add("ti-camera");
        iconTiList.add("ti-zip");
        iconTiList.add("ti-truck");
        iconTiList.add("ti-printer");
        iconFaList.add("fa-safari");
        iconFaList.add("fa-chrome");
        iconFaList.add("fa-firefox");
        iconFaList.add("fa-opera");
        iconFaList.add("fa-contao");
        iconFaList.add("fa-industry");
        iconFaList.add("fa-map");
        iconFaList.add("fa-vimeo");
        iconFaList.add("fa-edge");
        iconFaList.add("fa-modx");
    }

    private String createIconString(String str) {
        String iconString;
        Random random = new Random();
        if (str.equals("ti")) {
            int order = random.nextInt(iconTiList.size());
            iconString = iconTiList.get(order);
//            iconTiList.remove(order);
        }else{
            int order = random.nextInt(iconFaList.size());
            iconString = iconFaList.get(order);
//            iconFaList.remove(order);
        }

        return iconString;
    }

    //创建页面的字符串
    public String createElementString(Element element) {

        StringBuilder stringBuilder = new StringBuilder();

//        if(element==null){
//            return "";
//        }
        Iterator it = element.getContent().iterator();
        while (true) {
            Content child;
            do {
                if (!it.hasNext()) {
                    return stringBuilder.toString();
                }
                child = (Content) it.next();
            } while (!(child instanceof Element) && !(child instanceof Text));
            if (child instanceof Element) {
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("<div class=\"card-body card-block\">");
                } else if ((((Element) child).getName().equals("input") && ((Element) child).getAttribute("type").getValue().equals("text")) || ((Element) child).getName().equals("select")) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">" +
                            ((Element) child).getAttribute("name").getValue() +
                            "</label></div><div class=\"col-12 col-md-9\">");
                }
                //添加标签头
                stringBuilder.append("<" + ((Element) child).getName());
                //添加属性
                for (Attribute attribute : ((Element) child).getAttributes()) {
                    stringBuilder.append(" " + attribute.getName() + "=");
                    stringBuilder.append("\"" + attribute.getValue() + "\"");

                }
                ///////根据不同的元素添加特别的属性


                //表单特殊属性
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("class=\"form-horizontal\">");
                    //输入框的特殊属性
                } else if (((Element) child).getName().equals("input") || ((Element) child).getName().equals("select")) {
                    stringBuilder.append("class=\"form-control\">");
                } else if (((Element) child).getName().equals("option")) {
                    stringBuilder.append(">");
                    stringBuilder.append(child.getValue());
                }

                //添加标签头完毕 进入递归
                stringBuilder.append(createElementString((Element) child));
                //添加标签尾
                if (!((Element) child).getName().equals("input")) {
                    stringBuilder.append("</" + ((Element) child).getName() + " >");
                }
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("</div>");
                } else if (!(((Element) child).getName().equals("input") && ((Element) child).getAttribute("type").getValue().equals("submit")) && !((Element) child).getName().equals("option")) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                }

            }
            if (child instanceof Text) {
                //stringBuilder.append(createTextString((Text) child));
            }
        }
    }

    //生成text类型对象的字符串
    public String createTextString(Text text) {
        return text.getValue();
    }

    //获取页面的element
    public Element getElement(String str) {
        return pageElement.get(str);
    }

    public String createPageString(String pageName) {
        return createElementString(getElement(pageName));
    }

    //生成侧边栏的字符串
    public String createAsideString() {
        System.out.println("主页面切换");
        StringBuilder stringBuilder = new StringBuilder();
        //这里的外层是一个div，从这里是ul开始
        stringBuilder.append("<ul class=\"nav navbar-nav\" style=\"width: 280px\" > ");

        for (Map.Entry<String, Element> entry : typeElement.entrySet()) {
            //添加种类名称 每一个种类是li
            stringBuilder.append("<li class=\"menu-item-has-children dropdown \" >");
            stringBuilder.append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" aria-haspopup=\"true\"\n" +
                    "aria-expanded=\"false\" ><i class=\"menu-icon fa " + createIconString("fa") + "\"></i>" + entry.getKey() + "</a>");
            //每一个种类还是一个ul 里面的每个页面是一个li
            stringBuilder.append(" <ul class=\"sub-menu children dropdown-menu \" >");
            //System.out.println(entry.getKey());style="padding-left: 0px"
            for (Element element : entry.getValue().getChildren()) {
                stringBuilder.append("<li><i class=\"menu-icon " + createIconString("ti") + "\"></i><a href=\"" + element.getAttribute("url").getValue() + "\">" + element.getAttribute("name").getValue() + "</a></li>");
                //System.out.println(element.getAttribute("name").getValue());
            }
            stringBuilder.append("</ul>");
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        return stringBuilder.toString();
    }

    public List<String> createPageNameList() {
        List<String> nameList = new ArrayList<>();
        nameList.addAll(pageElement.keySet());
        return nameList;
    }

//    public static void main(String[] args) throws JAXBException, JDOMException, IOException {
//        System.out.println("\"");
//        new Beans.XmlMapping();
//        Beans.XmlMapping xmlMapping = new Beans.XmlMapping();
//        System.out.println("//////////////////////////////////////////////////////////////////////");
//        System.out.println(xmlMapping.createPageString("queryLogin"));
//        System.out.println(xmlMapping.createAsideString());
//    }
}
