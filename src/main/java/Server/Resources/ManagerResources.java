package Server.Resources;

import java.util.*;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.ManagerDatabaseHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import javax.sql.rowset.spi.SyncResolver;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.*;

/**
 * 管理员相关操作接口
 */
@Slf4j
public class ManagerResources {
    // 删除权限
    private static final String OPERATION_DELETE_AUTH = "deleteAuth";
    // 增加权限
    private static final String OPERATION_ADD_AUTH = "addAuth";
    // 删除用户
    private static final String OPERATION_DELETE_USERS = "deleteUsers";
    // 增加用户
    private static final String OPERATION_ADD_USER = "addUser";
    // 更新用户信息
    private static final String OPERATION_UPDATE_USERINFO = "updateUserInfo";
    // 查询权限列表
    private static final String OPERATION_SELECT_AUTHLIST = "selectAuthList";
    // 更新用户服务器权限
    private static final String OPERATION_UPDATE_AUTH = "updateAuth";
    // 前端请求的主要数据
    private HashMap<String, String> data;

    public void registerResources(Router router) {
        router.route("/manager").handler(this::GmSystemServer);
    }

    /**
     * 数据库操作
     *
     * @param routingContext
     */
    @SuppressWarnings("unchecked")
    private void GmSystemServer(RoutingContext routingContext) {
        data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);
        System.out.println(data);
        String server;
        String password;
        String username;
        String operation = data.get("operation");

        log.info("Manager receive args：" + data);

        switch (operation) {
            case OPERATION_DELETE_USERS:
                List<String> userList = JSONArray.parseArray(JSON.toJSONString(data.get("authList")), String.class);
                returnResult(routingContext, "return", deleteUsers(userList));
                break;
            case OPERATION_ADD_USER:
                username = data.get("username");
                password = data.get("password");
                List<String> userInfo = Arrays.asList(username, password, "token");
                returnResult(routingContext, "return", addUser(userInfo));
                break;
            case OPERATION_UPDATE_USERINFO:
                username = data.get("username");
                password = data.get("password");
                returnResult(routingContext, "return", updateUserInfo(username, password));
                break;
            case OPERATION_SELECT_AUTHLIST:
                username = data.get("username");
                server = data.get("serverAuth");
                String authType = data.get("authType");
                List<String>resultList = selectAuthList(username, authType, server);
                String hashStr = JSON.toJSONString(resultList);
                returnResult(routingContext, "checkbox", hashStr);
                break;
            case OPERATION_UPDATE_AUTH:
                username = data.get("username");
                server = data.get("serverAuth");
                List<String> authSettings = JSONArray.parseArray(JSON.toJSONString(data.get("authList")), String.class);
                returnResult(routingContext, "return ", updateAuth(authSettings, server, username));
            default:
                break;
        }
    }

    /**
     * 结果处理
     *
     * @param routingContext
     * @param type
     * @param resultData
     */
    private void returnResult(RoutingContext routingContext, String type, String resultData) {
        if ("table" .equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("table", resultData, false, data));
        } else if ("str" .equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("str", JSON.toJSONString(resultData), false, data));
        } else if ("return" .equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("return", resultData, false, null));
        } else if ("checkbox" .equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("checkbox", resultData, false, data));
        }
    }
}