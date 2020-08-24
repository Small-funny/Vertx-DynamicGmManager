package Server.Automation;

import Server.DatabaseHelper.DatabaseHelper;
import Server.DatabaseHelper.JdbcMysqlHelper;
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
    private static final HashMap<String, Element> serverElement = new HashMap<>();
    private static List<HashMap<String, String>> tableContent = new ArrayList<>();

    public XmlMapping() throws JDOMException, IOException {

        SAXBuilder saxBuilder = new SAXBuilder();
        //Element root = saxBuilder.build("src/main/java/resources/properties.xml").getDocument().getRootElement();
        Element root = saxBuilder.build("properties.xml").getDocument().getRootElement();
        List<Element> typeList = root.getChild("pages").getChildren();
        for (Element typeE : typeList) {
            typeElement.put(typeE.getAttribute("name").getValue(), typeE);
            List<Element> pageList = typeE.getChildren();
            for (Element pageE : pageList) {
                pageElement.put(pageE.getAttribute("url").getValue(), pageE);
            }
        }
        List<Element> serverlist = root.getChild("servers").getChildren();
        for (Element element : serverlist) {
            serverElement.put(element.getAttribute("value").getValue(), element);
        }

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
                    stringBuilder.append("<div class=\"card\">")
                            .append("<div class=\"card-header\"><strong>")
                            .append(((Element) child).getAttribute("name").getValue())
                            .append("</strong></div>")
                            .append("<div class=\"card-body card-block\">");
                } else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">").append(((Element) child).getAttribute("name").getValue()).append("</label></div><div class=\"col-12 col-md-9\"><div class=\"form-check\">");

                } else if (((Element) child).getName().equals("table")) {
                    List<String> tableName = new ArrayList<>();
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"table-responsive\"><table class=\"table table-bordered\"><thead><tr>")
                            .append("<th scope=\"col\">#</th>");
                    for (Element tableChildren : ((Element) child).getChildren()) {
                        stringBuilder.append("<th scope=\"col\">").append(tableChildren.getAttribute("name").getValue()).append("</th>");
                        tableName.add(tableChildren.getAttribute("name").getValue());
                    }
                    tableContent = DatabaseHelper.selectManagerInfo(tableName);
                    stringBuilder.append("</tr></thead><tbody>");
                    int order = 1;
                    for (Map<String, String> map : tableContent) {
                        stringBuilder.append("<tr>");
                        stringBuilder.append("<td>");
                        stringBuilder.append(order);
                        stringBuilder.append("</td>");
                        for (String name : tableName) {
                            stringBuilder.append("<td>");
                            stringBuilder.append(map.get(name));
                            stringBuilder.append("</td>");
                        }
                        stringBuilder.append("</tr>");
                        order++;
                    }
                    stringBuilder.append("</tbody>");
                }//不是option这种小标签的通用类 input select会用
                else if (!(((Element) child).getName().equals("option") || ((Element) child).getName().equals("checkbox") || ((Element) child).getName().equals("radio"))) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">").append(((Element) child).getAttribute("name").getValue()).append("</label></div><div class=\"col-12 col-md-9\">");
                }


                //添加标签头 checkbox 和radio 不用写属性 有特殊写法 clock也不写 date 也不写
                if (!(((Element) child).getName().equals("checkbox") || ((Element) child).getName().equals("radio") || ((Element) child).getName().equals("time"))) {
                    stringBuilder.append("<").append(((Element) child).getName());
                    //添加属性
                    for (Attribute attribute : ((Element) child).getAttributes()) {
                        stringBuilder.append(" ").append(attribute.getName()).append("=");
                        stringBuilder.append("\"").append(attribute.getValue()).append("\"");

                    }
                }
                ///////根据不同的元素添加特别的属性


                //表单特殊属性 method="post"
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("  class=\"form-horizontal\">");
                    //输入框的特殊属性
                } else if (((Element) child).getName().equals("input") || ((Element) child).getName().equals("select")) {
                    stringBuilder.append("class=\"form-control\">");
                } else if (((Element) child).getName().equals("option")) {
                    stringBuilder.append(">");
                    stringBuilder.append(child.getValue());
                } else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("class=\"form-check\">");
                    for (Element childElement : ((Element) child).getChildren()) {
                        stringBuilder.append("<div class=\"")
                                .append(childElement.getName())
                                .append("\"><label  class=\"form-check-label \"><input type=\"radio\" value=\"")
                                .append(((Element) childElement).getAttribute("value").getValue())
                                .append("\"").append(((Element) child).getAttribute("name"))
                                .append(" class=\"form-check-input\">")
                                .append(childElement.getValue()).append("</label></div>");

                    }
                } else if (((Element) child).getName().equals("time")) {
                    stringBuilder.append("<input type=\"text\" name=\"")
                            .append(((Element) child).getName())
                            .append("\" id=\"TIMESTAMP\" class=\"form-control\" autocomplete=\"off\">");
                }


                //添加标签头完毕 进入递归 table不能进入
                if (!((Element) child).getName().equals("table")) {
                    stringBuilder.append(createElementString((Element) child));
                }        //添加标签尾
                //如果不是input就意味着是那种小标签 直接加尾部就行
                if (!(((Element) child).getName().equals("input") || ((Element) child).getName().equals("form-check"))) {
                    stringBuilder.append("</").append(((Element) child).getName()).append(" >");
                }


                //这里加结束的div
                if (((Element) child).getName().equals("form")) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                    stringBuilder.append("<div class=\"card\"><div class=\"card-body card-block\">" +
                            "<div class=\"row form-group\"><div class=\"col col-md-3\"><label for=\"textarea-input\" class=\" form-control-label\">Textarea</label></div>" +
                            "<div class=\"col-12 col-md-9\"><textarea name=\"textarea-input\" id=\"textarea-input\" rows=\"9\" placeholder=\"Content...\" class=\"form-control\">" +
                            "</textarea></div></div></div></div>");
                } else if (((Element) child).getName().equals("formcheck")) {
                    stringBuilder.append("</div></div></div>");
                } else if (!((Element) child).getName().equals("option") && !((Element) child).getName().equals("checkbox") && !((Element) child).getName().equals("radio")) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                }


            }

        }

    }

    //获取页面的element
    public Element getElement(String str) {
        return pageElement.get(str);
    }

    public String createPageString(String pageName) {
        return createElementString(getElement(pageName));
    }

    public String createTable(List<HashMap<String, String>> list) {
        Set<String> title = list.get(1).keySet();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table class=\"table table-bordered\"><thead><tr>")
                .append("<th scope=\"col\">#</th>");
        for (String string : title) {
            stringBuilder.append("<th scope=\"col\">").append(string).append("</th>");
        }
        stringBuilder.append("</thead><tbody>");
        for (HashMap<String, String> hashMap : list) {
            stringBuilder.append("");
        }
        return stringBuilder.toString();
    }
    //生成侧边栏的字符串
//    public String createAsideString() {
//        System.out.println("///////////////////////////////////////////////////////");
//        StringBuilder stringBuilder = new StringBuilder();
//        //这里的外层是一个div，从这里是ul开始
//        stringBuilder.append("<ul class=\"nav navbar-nav\" style=\"width: 280px\" > ");
//        //类别使用name作为序号
//        int name = 1;
//        for (Map.Entry<String, Element> entry : typeElement.entrySet()) {
//            //添加种类名称 每一个种类是li
//            stringBuilder.append("<li class=\"menu-item-has-children dropdown  name= \"" + name + "\" \" >");
//            name++;
//            stringBuilder.append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" aria-haspopup=\"true\"\n" +
//                    "aria-expanded=\"false\" ><i class=\"menu-icon fa " + createIconString("fa") + "\"></i>" + entry.getKey() + "</a>");
//            //每一个种类还是一个ul 里面的每个页面是一个li
//            stringBuilder.append(" <ul class=\"sub-menu children dropdown-menu \" >");
//            //System.out.println(entry.getKey());style="padding-left: 0px"
//            int ID = 1;
//            for (Element element : entry.getValue().getChildren()) {
//                if (element.getAttribute("url") != null) {
//                    stringBuilder.append("<li id><i class=\"menu-icon " + createIconString("ti") + "\"></i><a href=\"" + element.getAttribute("url").getValue() + "\">" + element.getAttribute("name").getValue() + "</a></li>");
//                    //System.out.println(element.getAttribute("name").getValue());
//                } else {
//                    stringBuilder.append("<li id><i class=\"menu-icon " + createIconString("ti") + "\"></i><a href=\"#\">" + element.getAttribute("name").getValue() + "</a></li>");
//
//                }
//            }
//            stringBuilder.append("</ul>");
//            stringBuilder.append("</li>");
//        }
//        stringBuilder.append("</ul>");
//        return stringBuilder.toString();
//    }

    public String createAsideString(String token, String server) {
        // List<String> urlList = JdbcMysqlHelper.selectAuthority(token);
        List<String> urlList = DatabaseHelper.selectAuthority(token, server);
        System.out.println("/////" + urlList);
        StringBuilder stringBuilder = new StringBuilder();
        //从外层div开始 div类是navigation-menu-body
        //这是最外层的ul
        stringBuilder.append("<ul>");
        //这个是最大类别 暂时没什么用
        stringBuilder.append("<li class=\"navigation-divider\">最大类别</li>");
        for (Map.Entry<String, Element> entry : typeElement.entrySet()) {
            boolean flag = true;
            if (!urlList.contains(entry.getValue().getAttribute("authorization").getValue())) {
                continue;
            }
            stringBuilder.append("<li> <a href=\"");
            if (entry.getValue().getAttribute("url") != null) {
                stringBuilder.append(entry.getValue().getAttribute("authorization")).append("\">");
            } else {
                stringBuilder.append("#\">");
            }
            //iconList.add("data-feather=\"anchor\"");
            stringBuilder.append("<i class=\"nav-link-icon\" data-feather=\"")
                    .append(entry.getValue().getAttribute("icon").getValue())
                    .append("\"></i>")
                    .append(entry.getKey())
                    .append("</a>")
                    .append("<ul>");
            for (Element element : entry.getValue().getChildren()) {
                stringBuilder.append("<li id =\" ")
                        .append(element.getAttribute("name").getValue())
                        .append("\"><a href=\"")
                        .append(element.getAttribute("url").getValue())
                        .append("\">")
                        .append(element.getAttribute("name").getValue())

                        .append("</a>")

                        .append("</li>");

            }
            stringBuilder.append("</ul>");
            stringBuilder.append("</li>");

        }
        stringBuilder.append("</ul>");
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }


    public List<String> createPageUrlList() {
        List<String> urlList = new ArrayList<>();
        for (String string : pageElement.keySet()) {
            urlList.add(pageElement.get(string).getAttribute("url").getValue());
        }
        return urlList;

    }

    public String createServerString(String selected) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<option value=\"0\"> 选择服务器 </option>");
        for (String string : serverElement.keySet()) {
            stringBuilder.append("<option value=\"")
                    .append(serverElement.get(string).getAttribute("value").getValue())
                    .append("\"");
            if (selected.equals(serverElement.get(string).getAttribute("value").getValue())) {
                stringBuilder.append(" selected");
            }
            stringBuilder.append(">")
                    .append(serverElement.get(string).getValue())
                    .append("</option>");
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws JDOMException, IOException {
        System.out.println("\"");


        XmlMapping xmlMapping = new XmlMapping();

        //System.out.println(xmlMapping.createAsideString("TOKEN","master"));
        //System.out.println(xmlMapping.createPageUrlList());
        System.out.println(xmlMapping.createPageString("checkUserInfo"));

    }
}
