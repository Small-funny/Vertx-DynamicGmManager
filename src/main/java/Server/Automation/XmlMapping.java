package Server.Automation;

import Server.DatabaseHelper.VerifyDatabaseHelper;
import Server.Verify.Cache;
import Server.Verify.JwtUtils;
import com.alibaba.fastjson.JSON;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;

import java.util.*;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.*;

/**
 * @author Wen
 */
@Slf4j
@SuppressWarnings("unchecked")
public class XmlMapping {

    /**
     * 根据xml生成页面的内容
     *
     * @param element 页面的元素
     * @param route   当前页面
     */
//    public static String createElementString(Element element, String route) {
//        StringBuilder stringBuilder = new StringBuilder();
//
//        Iterator it = element.getContent().iterator();
//        while (true) {
//            Content child;
//            do {
//                if (!it.hasNext()) {
//                    return stringBuilder.toString();
//                }
//                child = (Content) it.next();
//            } while (!(child instanceof Element) && !(child instanceof Text));
//            if (child instanceof Element) {
//                //当前标签外层所需要的标签和类
//                String childName = ((Element) child).getName();
//                if ("form".equals(childName)) {
//                    stringBuilder.append("<div class=\"card-header\"><strong>")
//                            .append(((Element) child).getAttribute("name").getValue())
//                            .append("</strong></div>")
//                            .append("<div class=\"card-body card-block\">");
//                } else if ("formcheck".equals(childName)) {
//                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">")
//                            .append(((Element) child).getAttribute("name").getValue())
//                            .append("</label></div><div class=\"col-12 col-md-9\"><div class=\"form-check\">");
//                } else if ("table".equals(childName)) {
//                    stringBuilder.append("<div class=\"row form-group\"><div class=\"table-responsive\">");
//                } //不是option这种小标签的通用类 input select会用
//                else if (!("option".equals(childName) || "checkbox".equals(childName) || "radio".equals(childName))) {
//                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">")
//                            .append(((Element) child).getAttribute("name").getValue())
//                            .append("</label></div><div class=\"col-12 col-md-9\">");
//                }
//                //添加标签头 checkbox 和radio 不用写属性 有特殊写法 clock也不写 date 也不写
//                if (!("checkbox".equals(childName) || "radio".equals(childName) || "time".equals(childName))) {
//                    stringBuilder.append("<").append(childName);
//                    //添加属性
//                    for (Attribute attribute : ((Element) child).getAttributes()) {
//                        stringBuilder.append(" ").append(attribute.getName()).append("=");
//                        stringBuilder.append("\"").append(attribute.getValue()).append("\"");
//                    }
//                }
//                ///////根据不同的元素添加特别的属性
//                //表单特殊属性 method="post" target="nm_iframe"  method="post"
//                if ("form".equals(childName)) {
//                    stringBuilder.append("  class=\"form-horizontal\" >");
//                    //输入框的特殊属性
//                } else if ("input".equals(childName) && "file".equals(((Element) child).getAttribute("type").getValue())) {
//                    stringBuilder.append("class=\"form-control-file\">");
//                } else if ("input".equals(childName) && "button".equals(((Element) child).getAttributeValue("type")) && "configManage".equals(((Element) child).getAttributeValue("id"))) {
//                    stringBuilder.append("class=\"form-control\" onclick=\"changeReturn('/forward')\" ");
//                } else if ("input".equals(childName) && "button".equals(((Element) child).getAttributeValue("type")) && "userManage".equals(((Element) child).getAttributeValue("id"))) {
//                    stringBuilder.append("class=\"form-control\" onclick=\"changeReturn('/manager')\" ");
//                } else if ("input".equals(childName) && "text".equals(((Element) child).getAttributeValue("type"))) {
//                    stringBuilder.append("class=\"form-control\" from=\"select\">");
//                } else if ("input".equals(childName) || "select".equals(childName)) {
//                    stringBuilder.append("class=\"form-control\">");
//                } else if ("option".equals(childName)) {
//                    stringBuilder.append(">");
//                    stringBuilder.append(child.getValue());
//                } else if ("formcheck".equals(childName)) {
//                    stringBuilder.append("class=\"form-check\">");
//                    for (Element childElement : ((Element) child).getChildren()) {
//                        stringBuilder.append("<div class=\"")
//                                .append(childElement.getName())
//                                .append("\"><label  class=\"form-check-label \"><input type=\"radio\" value=\"")
//                                .append(childElement.getAttribute("value").getValue())
//                                .append("\"").append(((Element) child).getAttribute("name"))
//                                .append(" class=\"form-check-input\">")
//                                .append(childElement.getValue()).append("</label></div>");
//                    }
//                } else if ("time".equals(childName)) {
//
//                    stringBuilder.append("<input type=\"text\" name=\"")
//                            .append(childName)
//                            .append("\" id=\"TIMESTAMP\" class=\"form-control\" autocomplete=\"off\">");
//                }
//                //添加标签头完毕 进入递归 table不能进入
//                if (!"table".equals(childName)) {
//                    stringBuilder.append(createElementString((Element) child, route));
//                }
//                //添加标签尾
//                //如果不是input就意味着是那种小标签 直接加尾部就行
//                if ("form".equals(childName)) {
//                    stringBuilder.append("<input type=\"hidden\" value=\"")
//                            .append(((Element) child).getAttributeValue("operation"))
//                            .append("\" name=\"operation\" from=\"select\"/>");
//                    stringBuilder.append("<input type=\"hidden\" value=\"").append(route).append("\" name=\"route\" from=\"select\"/>");
//                } else if (!("input".equals(childName) || "form-check".equals(childName))) {
//                    stringBuilder.append("</").append(childName).append(" >");
//                }
//                //这里加结束的div
//                if ("form".equals(childName)) {
//                    stringBuilder.append("</form></div>");
//
//
//                } else if ("formcheck".equals(childName)) {
//                    stringBuilder.append("</div></div></div>");
//                } else if (!"option".equals(childName) && !"checkbox".equals(childName) && !"radio".equals(childName)) {
//                    stringBuilder.append("</div>");
//                    stringBuilder.append("</div>");
//                }
//            }
//        }
//    }

    //获取页面的element
    public static String createElementString(Element element, String route) {
        String authorization = element.getAttributeValue("authorization");
        String pageType = element.getAttributeValue("type");
        StringBuilder stringBuilder = new StringBuilder();
        for (Element child : element.getChildren()) {
            stringBuilder.append(elementForm(child, route));
        }

        return stringBuilder.toString();
    }

    private static String elementForm(Element element, String route) {
        StringBuilder stringBuilder = new StringBuilder();
        String from = element.getAttributeValue("from");
        stringBuilder.append("<div class=\"card-header\"><strong>")
                .append(element.getAttributeValue("name"))
                .append("</strong></div>")
                .append("<div class=\"card-body card-block\">")
                .append("<form name=\"").append(element.getAttributeValue("name"))
                .append("\" operation=\"").append(element.getAttributeValue("operation")).append("\" class=\"form-horizontal\">");
        for (Element child : element.getChildren()) {
            switch (child.getName()) {
                case "input":
                    stringBuilder.append(elementInput(child, from));
                    break;
                case "select":
                    stringBuilder.append(elementSelect(child));
                    break;
                case "formcheck":
                    stringBuilder.append(elementFormCheck(child));
                    break;
                default:
                    break;
            }
        }

        stringBuilder.append("<input type=\"hidden\" value=\"").append(element.getAttributeValue("operation")).append("\" name=\"operation\" from=\"").append(from).append("\"/>")
                .append("<input type=\"hidden\" value=\"").append(route).append("\" name=\"route\" from=\"").append(from).append("\"/>")
                .append("</form>")
                .append("</div>");
        return stringBuilder.toString();

    }

    private static String elementInput(Element element, String from) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"row form-group\">")
                .append("<div class=\"col col-md-3\">")
                .append("<label  class=\" form-control-label\">")
                .append(element.getAttributeValue("name"))
                .append("</label>")
                .append("</div>")
                .append("<div class=\"col-12 col-md-9\">")
                .append("<input class=\"form-control\" type=\"")
                .append(element.getAttributeValue("type"))
                .append("\" name=\"")
                .append(element.getAttributeValue("name"))
                .append("\" id=\"")
                .append(element.getAttributeValue("id"))
                .append("\" from=\"").append(from).append("\"");
        if (element.getAttributeValue("type").equals("button")) {
            stringBuilder.append("value=\"")
                    .append(element.getAttributeValue("value"))
                    .append("\" onclick=\"changeReturn('/");
            if (element.getAttributeValue("id").equals("userManage")) {
                stringBuilder.append("manager");
            } else if (element.getAttributeValue("id").equals("configManage")) {
                stringBuilder.append("forward");
            }
            stringBuilder.append("','").append(from).append("')\"");
        }


        stringBuilder.append("/>").append("</div>").append("</div>");

        return stringBuilder.toString();
    }

    private static String elementSelect(Element element) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }

    private static String elementFormCheck(Element element) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }

    public static Element getElement(String str) {
        return PAGE_ELEMENT.get(str);
    }

    public static String createAsideString(String token, String server) {
        List<String> urlList = VerifyDatabaseHelper.selectAuthority(token, server);
        StringBuilder stringBuilder = new StringBuilder();
        //从外层div开始 div类是navigation-menu-body
        //这是最外层的ul
        stringBuilder.append("<ul>");
        //这个是最大类别 暂时没什么用
        stringBuilder.append("<li class=\"navigation-divider\">最大类别</li>");
        for (Map.Entry<String, Element> entry : TYPE_ELEMENT.entrySet()) {
            if (!urlList.contains(entry.getValue().getAttributeValue("authorization"))) {
                continue;
            }
            stringBuilder.append("<li> <a href=\"");
            if (entry.getValue().getAttribute("authorization") != null) {
                stringBuilder.append(entry.getValue().getAttributeValue("authorization")).append("\">");
            } else {
                stringBuilder.append("#\">");
            }
            stringBuilder.append("<i class=\"nav-link-icon\" data-feather=\"")
                    .append(entry.getValue().getAttributeValue("icon"))
                    .append("\"></i>")
                    .append(entry.getKey())
                    .append("</a><ul>");
            for (Element element : entry.getValue().getChildren()) {
                stringBuilder.append("<li id =\" ")
                        .append(element.getAttributeValue("name"))
                        .append("\"><a href=\"#\" onclick=\"changeAside('")
                        .append(server)
                        .append("','")
                        .append(element.getAttributeValue("authorization"))
                        .append("')\" id=\"").append(element.getAttributeValue("authorization")).append("\">")
                        .append(element.getAttributeValue("name"))
                        .append("</a></li>");
            }
            stringBuilder.append("</ul>");
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        return stringBuilder.toString();
    }

    public static String createServerString(String selected) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<option value=\"0\"> 选择服务器 </option>");
        for (String string : SERVER_ELEMENT.keySet()) {
            stringBuilder.append("<option value=\"")
                    .append(SERVER_ELEMENT.get(string).getAttribute("value").getValue())
                    .append("\"");
            if (selected.equals(SERVER_ELEMENT.get(string).getAttribute("value").getValue())) {
                stringBuilder.append(" selected");
            }
            stringBuilder.append(">")
                    .append(SERVER_ELEMENT.get(string).getAttributeValue("name"))
                    .append("</option>");
        }
        return stringBuilder.toString();
    }

    public static String createConfigsList(String data) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"card\"><div class=\"card-body card-block\" style=\"width: auto\">" +
                "<div class=\"row form-group\"><div class=\"col col-md-12\">" +
                "<select name=\"multiple-select\" id=\"multiple-select\" multiple=\"\" class=\"form-control\" style=\"height: 500px\" >");
        List<String> list = JSON.parseObject(data, List.class);
        for (String s : list) {
            stringBuilder.append("<option>").append(s).append("</option>");
        }
        stringBuilder.append("</select></div></div></div></div>");

        return stringBuilder.toString();
    }

    public static String createReturnString(String type, String data, boolean auth, HashMap<String, String> argsName) {
        StringBuilder stringBuilder = new StringBuilder();
        if ("list".equals(type)) {
            List<String> list = JSON.parseObject(data, List.class);
            for (String s : list) {
                stringBuilder.append("<option/>").append(s);
            }
            return stringBuilder.toString();
        } else {

            if ("table".equals(type)) {
                HashMap<String, String> hashMap1 = JSON.parseObject(data, HashMap.class);
                List<String> colName = JSON.parseObject(hashMap1.get("colName"), List.class);
                List<List<String>> tableBody = JSON.parseObject(hashMap1.get("tableBody"), List.class);
                stringBuilder.append("<table class=\"table table-bordered\">").append("<thead><tr>");
                for (String s : colName) {
                    stringBuilder.append("<th scope=\"col\">").append(s).append("</th>");
                }
                stringBuilder.append("</tr></thead><tbody>");
                for (List<String> subTableBody : tableBody) {
                    stringBuilder.append("<tr>");
                    for (String s : subTableBody) {
                        stringBuilder.append("<td>").append(s).append("</td>");
                    }
                    stringBuilder.append("<td><input type=\"button\" operation=\"delete \" onclick=\"tableDelete($(this))\" value=\"删除\"/></td>");
                    stringBuilder.append("</tr>");
                }
                stringBuilder.append("</tbody></table>").append("</div></div></div></form>");

            } else if ("str".equals(type)) {
                stringBuilder.append(" <div class=\"card\"><div class=\"card-body card-block\" style=\"width: auto\">")
                        .append("<form id=\"updateForm\" class=\"form-horizontal\" style=\"width: auto\">")
                        .append("<div class=\"row form-group\" style=\"width: auto\">")
                        .append("<div class=\"col-12 \">");
                stringBuilder.append("<textarea id=\"text\" name=\"body\" rows=\"19\" placeholder=\"Cont.\" class=\"form-control\" style=\"height:700px\" from=\"return\">")
                        .append(data)
                        .append("</textarea></div></div>");
                if (auth) {
                    stringBuilder
                            .append("<input type=\"button\" name=\"submit\" onclick=\"updateReturn('/forward')\" class=\"form-control\" value=\"修改\"></div></div>")
                            .append("<input type=\"hidden\" value=\"updateConfigBody\" name=\"operation\" from=\"return\">");
                }
                if (argsName != null) {
                    for (Map.Entry<String, String> entry : argsName.entrySet()) {
                        stringBuilder.append("<input type=\"hidden\" name=\"")
                                .append(entry.getKey())
                                .append("\" value =\"")
                                .append(entry.getValue())
                                .append("\" class=\"form-control\" from=\"return\">");
                    }
                }
                stringBuilder.append("</form></div>");
            } else if ("return".equals(type)) {
                stringBuilder.append(" <div class=\"card\"><div class=\"card-body card-block\" style=\"width: auto\">")
                        .append("<div class=\"row form-group\" style=\"width: auto\">")
                        .append("<div class=\"col-12 \">");
                stringBuilder.append("<p id=\"text\" name=\"body\" class=\"form-control\"  from=\"return\">")
                        .append(data)
                        .append("</p></div></div>");
                if (auth) {
                    stringBuilder
                            .append("<input type=\"button\" name=\"submit\" onclick=\"updateReturn('/forward')\" class=\"form-control\" value=\"修改\"></div></div>")
                            .append("<input type=\"hidden\" value=\"updateConfigBody\" name=\"operation\" from=\"return\">");
                }

                stringBuilder.append("</div>");
            }
        }
        return stringBuilder.toString();
    }

    public static String createPageString(String route, String type, String data, String pageRouter, List subAuthList, RoutingContext ctx, Vertx vertx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"card\">");
        if (!pageRouter.equals(MAIN_PAGE_ROUTER)) {
            stringBuilder.append(createElementString(getElement(pageRouter), route));
        }
        stringBuilder.append("</div>")
                .append("<div class=\"card\" style=\"width: auto\">");
        if (type != null) {
            if (!type.equals(TYPE_LIST)) {
                stringBuilder.append(createReturnString(type, data, subAuthList.contains(pageRouter), Cache.getArgs(JwtUtils.findToken(ctx))));
            }
        }
        stringBuilder.append("</div>")
                .append("<div>");
        if (USER_MANAGE_PAGES.contains(pageRouter)) {
            stringBuilder.append(createReturnString(TYPE_TABLE, JSON.toJSONString(allManagerInfo()), false, null));
        }
        stringBuilder.append("</div>")
                .append("</div>")
                .append(" <div class=\"col-lg-3\" style=\"flex: 0 0 auto;margin-left:50px\">");

        return stringBuilder.toString();
    }
}