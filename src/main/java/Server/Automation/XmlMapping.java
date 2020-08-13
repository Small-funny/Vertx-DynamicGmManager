package Server.Automation;

import Server.DatabaseHelper.JdbcMysqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

@Slf4j
public class XmlMapping {

    //页面名是键 对应的页面的element是值
    private static final HashMap<String, Element> pageElement = new HashMap<>();
    //页面类型是键 对应的类型element是值
    private static final HashMap<String, Element> typeElement = new HashMap<>();
    private static final Queue<String> iconTiList = new LinkedList<>();
    private static final Queue<String> iconFaList = new LinkedList<>();


    public XmlMapping() throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        //Element root = saxBuilder.build("src/main/java/resources/properties.xml").getDocument().getRootElement();
        Element root = saxBuilder.build("properties.xml").getDocument().getRootElement();
        List<Element> typeList = root.getChildren();
        for (Element typeE : typeList) {
            typeElement.put(typeE.getAttribute("name").getValue(), typeE);
            List<Element> pageList = typeE.getChildren();
            for (Element pageE : pageList) {
                pageElement.put(pageE.getAttribute("url").getValue(), pageE);
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

            iconString = iconTiList.poll();
            //iconTiList.remove(order);
        } else {
            int order = random.nextInt(iconFaList.size());
            iconString = iconFaList.poll();
            //iconFaList.remove(order);
        }

        return iconString;
    }

    //创建页面的字符串
    public String createElementString(Element element) {
        StringBuilder stringBuilder = new StringBuilder();

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
                //当前标签外层所需要的标签和类
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("<div class=\"card\">");
                    stringBuilder.append("<div class=\"card-header\"><strong>"+((Element) child).getAttribute("name").getValue()+"</strong></div>");
                    stringBuilder.append("<div class=\"card-body card-block\">");
                }
//                else if (((Element) child).getName().equals("checkbox")) {
//                    stringBuilder.append(" <div class=\"checkbox\">");
                else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">").append(((Element) child).getAttribute("name").getValue()).append("</label></div><div class=\"col-12 col-md-9\"><div class=\"form-check\">");
                    //不是option这种小标签的通用类 input select会用
                } else if (!(((Element) child).getName().equals("option") || ((Element) child).getName().equals("checkbox") || ((Element) child).getName().equals("radio"))) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">").append(((Element) child).getAttribute("name").getValue()).append("</label></div><div class=\"col-12 col-md-9\">");
                }
                //添加标签头 checkbox 不用写属性 有特殊写法
                if (!(((Element) child).getName().equals("checkbox") || ((Element) child).getName().equals("radio"))) {
                    stringBuilder.append("<").append(((Element) child).getName());
                    //添加属性
                    for (Attribute attribute : ((Element) child).getAttributes()) {
                        stringBuilder.append(" ").append(attribute.getName()).append("=");
                        stringBuilder.append("\"").append(attribute.getValue()).append("\"");

                    }
                }
                ///////根据不同的元素添加特别的属性


                //表单特殊属性
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("class=\"form-horizontal\">");
                    //输入框的特殊属性
                } else if (((Element) child).getName().equals("input") || ((Element) child).getName().equals("select")) {
                    stringBuilder.append("class=\"form-control\">");
                    //复选框的特殊情况 不需要读属性 写标签头 直接添加整个div
                } else if (((Element) child).getName().equals("option")) {
                    stringBuilder.append(">");
                    stringBuilder.append(child.getValue());
                } else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("class=\"form-check\">");
                    for(Element childElement :((Element) child).getChildren()){
                        stringBuilder.append("<div class=\""+childElement.getName()+"\"><label  class=\"form-check-label \"><input type=\"radio\" value=\"").append(((Element) childElement).getAttribute("value").getValue()).append("\""+((Element) child).getAttribute("name")+" class=\"form-check-input\">").append(childElement.getValue()).append("</label></div>");

                    }
                }


                //添加标签头完毕 进入递归
                stringBuilder.append(createElementString((Element) child));
                //添加标签尾
                //如果不是input就意味着是那种小标签 直接加尾部就行
                if (!(((Element) child).getName().equals("input") || ((Element) child).getName().equals("form-check"))) {
                    stringBuilder.append("</").append(((Element) child).getName()).append(" >");
                }


                //这里加结束的div
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                } else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("</div></div></div>");
                } else if (!((Element) child).getName().equals("option") && !((Element) child).getName().equals("checkbox") && !((Element) child).getName().equals("radio")) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                }
                //!(((Element) child).getName().equals("input") && ((Element) child).getAttribute("type").getValue().equals("submit")) &&
            }
//            if (child instanceof Text) {
//                //stringBuilder.append(createTextString((Text) child));
//            }
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
        System.out.println("///////////////////////////////////////////////////////");
        StringBuilder stringBuilder = new StringBuilder();
        //这里的外层是一个div，从这里是ul开始
        stringBuilder.append("<ul class=\"nav navbar-nav\" style=\"width: 280px\" > ");
        //类别使用name作为序号
        int name = 1;
        for (Map.Entry<String, Element> entry : typeElement.entrySet()) {
            //添加种类名称 每一个种类是li
            stringBuilder.append("<li class=\"menu-item-has-children dropdown  name= \"" + name + "\" \" >");
            name++;
            stringBuilder.append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" aria-haspopup=\"true\"\n" +
                    "aria-expanded=\"false\" ><i class=\"menu-icon fa " + createIconString("fa") + "\"></i>" + entry.getKey() + "</a>");
            //每一个种类还是一个ul 里面的每个页面是一个li
            stringBuilder.append(" <ul class=\"sub-menu children dropdown-menu \" >");
            //System.out.println(entry.getKey());style="padding-left: 0px"
            int ID = 1;
            for (Element element : entry.getValue().getChildren()) {
                if (element.getAttribute("url") != null) {
                    stringBuilder.append("<li id><i class=\"menu-icon " + createIconString("ti") + "\"></i><a href=\"" + element.getAttribute("url").getValue() + "\">" + element.getAttribute("name").getValue() + "</a></li>");
                    //System.out.println(element.getAttribute("name").getValue());
                } else {
                    stringBuilder.append("<li id><i class=\"menu-icon " + createIconString("ti") + "\"></i><a href=\"#\">" + element.getAttribute("name").getValue() + "</a></li>");

                }
            }
            stringBuilder.append("</ul>");
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        return stringBuilder.toString();
    }

    public String createAsideString(String pageName) {
        List<String> urlList = JdbcMysqlHelper.selectAuthority(pageName);


        StringBuilder stringBuilder = new StringBuilder();
        //从外层div开始 div类是navigation-menu-body
        //这是最外层的ul
        stringBuilder.append("<ul>");
        //这个是最大类别 暂时没什么用
        stringBuilder.append("<li class=\"navigation-divider\">最大类别</li>");
        for (Map.Entry<String, Element> entry : typeElement.entrySet()) {
            boolean flag = true;
            if (!urlList.contains(entry.getValue().getAttribute("authorization").getValue())) {
                flag = false;
                continue;
            }
            stringBuilder.append("<li> <a href=\"");
            if (entry.getValue().getAttribute("url") != null) {
                stringBuilder.append(entry.getValue().getAttribute("authorization") + "\">");
            } else {
                stringBuilder.append("#\">");
            }
            stringBuilder.append(entry.getKey() + "</a>");
            stringBuilder.append("<ul>");
            for (Element element : entry.getValue().getChildren()) {
                if (!flag) {
                    if (!urlList.contains(element.getAttribute("url").getValue())) {
                        continue;
                    }
                }
                stringBuilder.append("<li id =\" " + element.getAttribute("name").getValue() + "\"><a href=\"" + element.getAttribute("url").getValue() + "\">" + element.getAttribute("name").getValue() + "</a></li>");

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

    public List<String> createPageURLList() {
        List<String> URLList = new ArrayList<>();
        for (String string : pageElement.keySet()) {
            URLList.add(pageElement.get(string).getAttribute("url").getValue());
        }
        return URLList;
    }

    public static void main(String[] args) throws JDOMException, IOException {
        System.out.println("\"");


        XmlMapping xmlMapping = new XmlMapping();
//        System.out.println("//////////////////////////////////////////////////////////////////////");
//        System.out.println(xmlMapping.createPageString("queryLogin"));
//        System.out.println(xmlMapping.createAsideString());
        System.out.println(xmlMapping.createAsideString("root"));
        System.out.println(xmlMapping.createPageURLList());
        System.out.println(xmlMapping.createPageString("operatorManage"));

    }
}
