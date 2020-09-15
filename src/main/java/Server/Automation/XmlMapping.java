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

@Slf4j
public class XmlMapping {

    private static String returnType = null;

    //创建页面的字符串
    public static String createElementString(Element element, String route) {
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
                String childName = ((Element) child).getName();
                if ("form".equals(childName)) {
                    stringBuilder.append("<div class=\"card-header\"><strong>")
                            .append(((Element) child).getAttribute("name").getValue())
                            .append("</strong></div>")
                            .append("<div class=\"card-body card-block\">");
                } else if ("formcheck".equals(childName)) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">")
                            .append(((Element) child).getAttribute("name").getValue())
                            .append("</label></div><div class=\"col-12 col-md-9\"><div class=\"form-check\">");

                } else if ("table".equals(childName)) {

                    stringBuilder.append("<div class=\"row form-group\"><div class=\"table-responsive\">");
                } else if ("return".equals(childName)) {
                    returnType = ((Element) child).getAttributeValue("type");
                    continue;
                }//不是option这种小标签的通用类 input select会用
                else if (!("option".equals(childName) || "checkbox".equals(childName) || "radio".equals(childName))) {
                    stringBuilder.append("<div class=\"row form-group\"><div class=\"col col-md-3\"><label class=\" form-control-label\">")
                            .append(((Element) child).getAttribute("name").getValue())
                            .append("</label></div><div class=\"col-12 col-md-9\">");
                }


                //添加标签头 checkbox 和radio 不用写属性 有特殊写法 clock也不写 date 也不写
                if (!("checkbox".equals(childName) || "radio".equals(childName) || "time".equals(childName))) {
                    stringBuilder.append("<").append(childName);
                    //添加属性
                    for (Attribute attribute : ((Element) child).getAttributes()) {
                        stringBuilder.append(" ").append(attribute.getName()).append("=");
                        stringBuilder.append("\"").append(attribute.getValue()).append("\"");

                    }
                }
                ///////根据不同的元素添加特别的属性

                //表单特殊属性 method="post" target="nm_iframe"  method="post"
                if ("form".equals(childName)) {
                    stringBuilder.append(" id=\"selectForm\" class=\"form-horizontal\" >");
                    //输入框的特殊属性
                } else if ("input".equals(childName) && "file".equals(((Element) child).getAttribute("type").getValue())) {
                    stringBuilder.append("class=\"form-control-file\">");
                } else if ("input".equals(childName) && "button".equals(((Element) child).getAttributeValue("type")) && "configManage".equals(((Element) child).getAttributeValue("id"))) {
                    stringBuilder.append("class=\"form-control\" onclick=\"changeReturn('/forward')\" value=\"修改\">");
                } else if ("input".equals(childName) && "button".equals(((Element) child).getAttributeValue("type")) && "userManage".equals(((Element) child).getAttributeValue("id"))) {
                    stringBuilder.append("class=\"form-control\" onclick=\"changeReturn('/manager')\" value=\"修改\">");
                } else if ("input".equals(childName) && "text".equals(((Element) child).getAttributeValue("type"))) {
                    stringBuilder.append("class=\"form-control\" from=\"select\">");
                } else if ("input".equals(childName) || "select".equals(childName)) {
                    stringBuilder.append("class=\"form-control\">");
                } else if ("option".equals(childName)) {
                    stringBuilder.append(">");
                    stringBuilder.append(child.getValue());
                } else if ("formcheck".equals(childName)) {
                    stringBuilder.append("class=\"form-check\">");
                    for (Element childElement : ((Element) child).getChildren()) {
                        stringBuilder.append("<div class=\"")
                                .append(childElement.getName())
                                .append("\"><label  class=\"form-check-label \"><input type=\"radio\" value=\"")
                                .append(childElement.getAttribute("value").getValue())
                                .append("\"").append(((Element) child).getAttribute("name"))
                                .append(" class=\"form-check-input\">")
                                .append(childElement.getValue()).append("</label></div>");

                    }
                } else if ("time".equals(childName)) {

                    stringBuilder.append("<input type=\"text\" name=\"")
                            .append(childName)
                            .append("\" id=\"TIMESTAMP\" class=\"form-control\" autocomplete=\"off\">");
                }


                //添加标签头完毕 进入递归 table不能进入
                if (!"table".equals(childName)) {
                    stringBuilder.append(createElementString((Element) child, route));
                }


                //添加标签尾
                //如果不是input就意味着是那种小标签 直接加尾部就行
                if ("form".equals(childName)) {
                    stringBuilder.append("<input type=\"hidden\" value=\"")
                            .append(((Element) child).getAttributeValue("id"))
                            .append("\" name=\"operation\" from=\"select\"/>");
                    stringBuilder.append("<input type=\"hidden\" value=\"").append(route).append("\" name=\"route\" from=\"select\"/>");
//                    if ("table".equals(returnType)) {
//                        stringBuilder.append("<input type=\"hidden\" value=\"selectTableData\" name=\"operation\"/>");
//                    } else if ("str".equals(returnType)) {
//                        stringBuilder.append("<input type=\"hidden\" value=\"selectConfigBody\" name=\"operation\"/>");
//                    }

                } else if (!("input".equals(childName) || "form-check".equals(childName))) {
                    stringBuilder.append("</").append(childName).append(" >");
                }


                //这里加结束的div
                if ("form".equals(childName)) {

                    stringBuilder.append("</form></div>");

                    if ("str".equals(returnType)) {

                    } else if ("table".equals(returnType)) {


                    }
                } else if ("formcheck".equals(childName)) {
                    stringBuilder.append("</div></div></div>");
                } else if (!"option".equals(childName) && !"checkbox".equals(childName) && !"radio".equals(childName)) {
                    stringBuilder.append("</div>");
                    stringBuilder.append("</div>");
                }


            }

        }

    }

    //获取页面的element
    public static Element getElement(String str) {
        return PAGE_ELEMENT.get(str);
    }


    public static String createAsideString(String token, String server) {
        // List<String> urlList = JdbcMysqlHelper.selectAuthority(token);
        List<String> urlList = VerifyDatabaseHelper.selectAuthority(token, server);
        StringBuilder stringBuilder = new StringBuilder();
        //从外层div开始 div类是navigation-menu-body
        //这是最外层的ul
        stringBuilder.append("<ul>");
        //这个是最大类别 暂时没什么用
        stringBuilder.append("<li class=\"navigation-divider\">最大类别</li>");
        for (Map.Entry<String, Element> entry : TYPE_ELEMENT.entrySet()) {
            if (!urlList.contains(entry.getValue().getAttribute("authorization").getValue())) {
                continue;
            }
            stringBuilder.append("<li> <a href=\"");
            if (entry.getValue().getAttribute("url") != null) {
                stringBuilder.append(entry.getValue().getAttribute("authorization")).append("\">");
            } else {
                stringBuilder.append("#\">");
            }
            stringBuilder.append("<i class=\"nav-link-icon\" data-feather=\"")
                    .append(entry.getValue().getAttribute("icon").getValue())
                    .append("\"></i>")
                    .append(entry.getKey())
                    .append("</a><ul>");
            for (Element element : entry.getValue().getChildren()) {
                stringBuilder.append("<li id =\" ")
                        .append(element.getAttribute("name").getValue())
                        .append("\"><a href=\"#\" onclick=\"changeAside('")
                        .append(server)
                        .append("','")
                        .append(element.getAttributeValue("url"))
                        .append("')\" id=\"").append(element.getAttributeValue("url")).append("\">")
                        .append(element.getAttribute("name").getValue())
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
                    .append(SERVER_ELEMENT.get(string).getValue())
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
            stringBuilder.append("<div class=\"card-body card-block\" style=\"width: auto\">")
                    .append("<form id=\"updateForm\" class=\"form-horizontal\" style=\"width: auto\">")
                    .append("<div class=\"row form-group\" style=\"width: auto\">")
                    .append("<div class=\"col-12 col-md-9\">");
            if ("table".equals(type)) {
                System.out.println("data:" + data);
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


                    stringBuilder.append("</tr>");
                }
                stringBuilder.append("</tbody></table>").append("</div></div></div></form>");

            } else if ("str".equals(type)) {
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
                stringBuilder.append("</form>");
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

    public static void main(String[] args) throws Exception {
        System.out.println("\"");

        System.out.println(XmlMapping.createAsideString("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1OTk3MjI0MjIsImV4cCI6MTU5OTcyNjAyMn0.Z7KGUnZyYg3T8J1UzgFXhZuuYmdZh4n1Dj0Uv6tPfcs", "sandbox"));
        //System.out.println(xmlMapping.createAsideString("TOKEN","master"));
        //System.out.println(xmlMapping.createPageUrlList());
        //System.out.println(xmlMapping.createPageString("checkUserInfo"));
        //System.out.println(xmlMapping.createPageString("basicInfoManage"));
        //System.out.println(xmlMapping.createReturnString("table", "", true));
    }
}
